# 65 - Actuator Deep Dive and Metrics with Micrometer

Lesson 20 introduced Actuator's default endpoints - `/health`, `/info`,
and the rest, mostly static and generic. This lesson goes further: writing a
**custom health indicator** that reflects real application state, and
instrumenting real code with **Micrometer** - the metrics facade Spring Boot
builds Actuator's `/metrics` endpoint on top of - with a counter, a timer,
and a gauge. Every number below is real, measured against a running app.

```bash
mvn -f Spring/13-production-readiness/65-actuator-deep-dive-and-metrics-with-micrometer spring-boot:run
```

## A custom `HealthIndicator` that can actually go DOWN

[`DownstreamHealthIndicator.java`](src/main/java/com/danish/spring/actuatordeepdive/DownstreamHealthIndicator.java):

```java
@Component("downstreamService")
public class DownstreamHealthIndicator implements HealthIndicator {
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    @Override
    public Health health() {
        if (healthy.get()) {
            return Health.up().withDetail("checkedAt", System.currentTimeMillis()).build();
        }
        return Health.down().withDetail("reason", "simulated downstream outage").build();
    }

    public void setHealthy(boolean healthy) { this.healthy.set(healthy); }
}
```

Any `HealthIndicator` bean becomes a named component under
`/actuator/health` automatically - "downstreamService" here, from the bean
name. Real output while healthy:

```bash
curl -s http://localhost:8080/actuator/health
```
```json
{"status":"UP","components":{
  "diskSpace":{"status":"UP","details":{...}},
  "downstreamService":{"status":"UP","details":{"checkedAt":1789324560588}},
  "ping":{"status":"UP"}
}}
```

After `POST /toggle-downstream-health/false` (flips the indicator live, no
restart), real output:

```json
{"status":"DOWN","components":{
  "diskSpace":{"status":"UP","details":{...}},
  "downstreamService":{"status":"DOWN","details":{"reason":"simulated downstream outage"}},
  "ping":{"status":"UP"}
}}
```

The **overall** status flipped to `DOWN`, even though `diskSpace` and `ping`
were still fine - Spring Boot's default aggregation rule is that if *any*
registered indicator reports `DOWN`, the whole application is considered
`DOWN`. This is what makes `/actuator/health` genuinely useful to a load
balancer or Kubernetes readiness probe: a single custom indicator that
tracks something real (a database connection, a critical downstream
dependency, a queue backlog) can take the whole instance out of rotation the
moment that specific thing breaks, without anyone writing aggregation logic
by hand.

## Instrumenting real code with Micrometer

[`OrderMetrics.java`](src/main/java/com/danish/spring/actuatordeepdive/OrderMetrics.java)
registers three different kinds of meter, each answering a different
question:

```java
this.ordersPlaced = Counter.builder("orders.placed").register(registry);
this.orderPlacementTimer = Timer.builder("orders.placement.duration").register(registry);
Gauge.builder("orders.pending", pendingOrders, AtomicInteger::get).register(registry);
```

- **Counter** (`orders.placed`) only ever goes up - "how many orders have
  ever been placed since this JVM started."
- **Timer** (`orders.placement.duration`) records both a count and a
  duration per sample - "how many times, and how long did each one take."
- **Gauge** (`orders.pending`) reads a live value at scrape time from the
  `AtomicInteger` it's registered against - "what is the value *right now*,"
  not an accumulated total.

[`OrderController.java`](src/main/java/com/danish/spring/actuatordeepdive/OrderController.java)
uses all three around a real (simulated) unit of work:

```java
@PostMapping("/orders")
public String placeOrder() throws InterruptedException {
    orderMetrics.incrementPending();
    Timer.Sample sample = orderMetrics.startTimer();
    try {
        Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        orderMetrics.recordOrderPlaced();
        return "order placed";
    } finally {
        orderMetrics.stopTimer(sample);
        orderMetrics.decrementPending();
    }
}
```

After placing 5 real orders, real output:

```bash
curl -s http://localhost:8080/actuator/metrics/orders.placed
```
```json
{"name":"orders.placed","measurements":[{"statistic":"COUNT","value":5.0}]}
```

```bash
curl -s http://localhost:8080/actuator/metrics/orders.placement.duration
```
```json
{"name":"orders.placement.duration","baseUnit":"seconds",
 "measurements":[
   {"statistic":"COUNT","value":5.0},
   {"statistic":"TOTAL_TIME","value":0.9586448},
   {"statistic":"MAX","value":0.274774}
 ]}
```

5 counted, ~0.96s total across all 5 calls, a max of ~0.27s for the slowest
one - consistent with the `100-300ms` random sleep in the code. These are
real durations of real work, aggregated by Micrometer with no extra code
beyond starting and stopping the timer sample.

The gauge really does reflect the current, live value rather than a running
total - caught mid-flight by checking it while a 6th order was still in
progress on a background thread:

```bash
curl -s http://localhost:8080/actuator/metrics/orders.pending
```
```json
{"measurements":[{"statistic":"VALUE","value":1.0}]}
```

...and back to `0.0` once that order finished - a `Gauge` is a live read,
not an accumulator.

## A real naming translation: Micrometer's dimensional model → Prometheus text format

`/actuator/metrics/<name>` is Spring Boot's own JSON view. With
`micrometer-registry-prometheus` on the classpath, the exact same
underlying meters are also exposed at `/actuator/prometheus`, in Prometheus's
plain-text exposition format - and the names genuinely change shape in the
process:

```bash
curl -s http://localhost:8080/actuator/prometheus | grep -E "orders_placed|orders_placement|orders_pending"
```
```
# HELP orders_pending Orders currently being processed
# TYPE orders_pending gauge
orders_pending 0.0
# HELP orders_placed_total Total number of orders placed
# TYPE orders_placed_total counter
orders_placed_total 5.0
# HELP orders_placement_duration_seconds Time taken to place an order
# TYPE orders_placement_duration_seconds summary
orders_placement_duration_seconds_count 5
orders_placement_duration_seconds_sum 0.9586448
# HELP orders_placement_duration_seconds_max Time taken to place an order
# TYPE orders_placement_duration_seconds_max gauge
orders_placement_duration_seconds_max 0.274774
```

Dots become underscores (`orders.placed` → `orders_placed`), Prometheus's
naming convention appends `_total` to counters, and a single `Timer` meter
becomes *three* separate Prometheus series (`_count`, `_sum`, and a
`_max` gauge) rather than one. None of this required any code change -
Micrometer's registry implementations translate the same underlying meter
data into whatever shape each monitoring backend expects; the application
code (`OrderMetrics`) has no idea Prometheus is even involved.

## `/actuator/info` from plain properties

```properties
info.app.name=actuator-deep-dive
info.app.description=Lesson 65 - custom health indicators and Micrometer metrics
```

```bash
curl -s http://localhost:8080/actuator/info
```
```json
{"app":{"name":"actuator-deep-dive","description":"Lesson 65 - custom health indicators and Micrometer metrics"}}
```

Any `info.*` property is surfaced verbatim, nested by dot segments - a
simple way to expose build metadata, version info, or environment details
without writing a controller for it.

## Key takeaways

- A `HealthIndicator` bean automatically becomes a named component of
  `/actuator/health`; if any indicator reports `DOWN`, the aggregated
  overall status is `DOWN` too, regardless of the others - a real,
  actionable signal, not just a liveness ping.
- `Counter`, `Timer`, and `Gauge` answer three different questions:
  cumulative total, count-and-duration-per-call, and current live value,
  respectively - picking the wrong one (a gauge for a running total, a
  counter for something that can decrease) produces misleading metrics.
- `/actuator/metrics/<name>` and `/actuator/prometheus` expose the exact
  same underlying Micrometer data in two different shapes - the Prometheus
  format renames and restructures meters (dots to underscores, `_total`
  suffix on counters, a `Timer` becoming multiple series) to match that
  backend's conventions, entirely inside the registry, with zero
  application code changes.
- `info.*` properties are exposed verbatim at `/actuator/info`, nested by
  dot segments - the simplest way to surface static build/version metadata.
