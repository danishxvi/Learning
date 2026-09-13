# 64 - Resilience with Circuit Breakers (Resilience4j)

Lesson 60 already showed what happens when a discovered service turns out
to be unreachable: a real `ConnectException`, surfaced straight up to the
caller. Discovery finds an address; it says nothing about whether calling
that address is currently a good idea. A **circuit breaker** is the pattern
that closes that gap: it watches a dependency's real failure rate, and once
that dependency looks broken, it stops calling it at all for a while -
failing fast with a fallback instead of piling up slow, doomed calls against
a service that's already down. Every state transition below was observed
directly from Resilience4j's own `CircuitBreaker.getState()`, not inferred.

Two separate runnable Maven projects:

```
64-resilience-with-circuit-breakers/
├── inventory-service/   - a dependency that can be toggled between healthy and broken
└── order-service/       - calls it THROUGH a circuit breaker, with a fallback
```

```bash
mvn -f Spring/12-microservices-with-spring-cloud/64-resilience-with-circuit-breakers/inventory-service spring-boot:run
mvn -f Spring/12-microservices-with-spring-cloud/64-resilience-with-circuit-breakers/order-service spring-boot:run
```

## The dependency: toggleable, and it proves whether it was really called

[`InventoryController.java`](inventory-service/src/main/java/com/danish/spring/inventoryservice/InventoryController.java)
starts in failure mode on purpose, and counts every request it genuinely
receives - independent evidence for whether the circuit breaker actually let
a call reach it:

```java
private final AtomicBoolean failing = new AtomicBoolean(true);
private final AtomicInteger callCount = new AtomicInteger();

@GetMapping("/inventory/{productId}")
public String getStock(@PathVariable String productId) {
    callCount.incrementAndGet();
    if (failing.get()) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "inventory-service is currently failing (simulated)");
    }
    return "product " + productId + " has 42 units in stock";
}
```

`POST /toggle-failure` flips it between broken and healthy without a
restart; `GET /call-count` reports how many requests genuinely arrived.

## Wrapping the call: `@CircuitBreaker` and a fallback

[`InventoryService.java`](order-service/src/main/java/com/danish/spring/orderservice/InventoryService.java):

```java
@CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackStock")
public String getStock(String productId) {
    return restTemplate.getForObject(inventoryServiceUrl + "/inventory/" + productId, String.class);
}

public String fallbackStock(String productId, Throwable throwable) {
    return "FALLBACK: stock for " + productId + " unavailable right now (" + throwable.getClass().getSimpleName() + ")";
}
```

Deliberately small thresholds, so the whole state machine plays out in
seconds rather than minutes:

```properties
resilience4j.circuitbreaker.instances.inventoryService.sliding-window-size=4
resilience4j.circuitbreaker.instances.inventoryService.minimum-number-of-calls=4
resilience4j.circuitbreaker.instances.inventoryService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.inventoryService.wait-duration-in-open-state=4s
resilience4j.circuitbreaker.instances.inventoryService.permitted-number-of-calls-in-half-open-state=2
resilience4j.circuitbreaker.instances.inventoryService.automatic-transition-from-open-to-half-open-enabled=true
```

[`OrderController.java`](order-service/src/main/java/com/danish/spring/orderservice/OrderController.java)
exposes the breaker's real, live state directly from Resilience4j's own
registry - not guessed from HTTP responses:

```java
@GetMapping("/circuit-state")
public String circuitState() {
    CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("inventoryService");
    return "state=" + breaker.getState() + ", failureRate=" + breaker.getMetrics().getFailureRate() + "%" + ...;
}
```

## CLOSED → OPEN: four real failures trip the breaker

With `inventory-service` still in its default failure mode, four calls to
`/order-with-stock/widget`:

```
FALLBACK: stock for widget unavailable right now (InternalServerError)
FALLBACK: stock for widget unavailable right now (InternalServerError)
FALLBACK: stock for widget unavailable right now (InternalServerError)
FALLBACK: stock for widget unavailable right now (InternalServerError)
```

```bash
curl -s http://localhost:8082/circuit-state
```
```
state=OPEN, failureRate=100.0%, bufferedCalls=4, failedCalls=4
```

```bash
curl -s http://localhost:8081/call-count
```
```
4
```

All four calls genuinely reached `inventory-service` (`call-count` is 4) -
the breaker was `CLOSED` the whole time, so every call was attempted for
real, and every one failed. With `sliding-window-size=4` and
`failure-rate-threshold=50`, a 100% failure rate across that window tripped
the breaker to `OPEN` the moment the fourth failure was recorded. Note the
fallback method's own log of *why* each call failed: `InternalServerError` -
the real exception type from the real, failed HTTP call, not yet a
short-circuited one.

## An honest, more nuanced OPEN → HALF_OPEN cycle than expected

The plan was simple: confirm `OPEN` blocks further real calls, then wait for
`wait-duration-in-open-state` and watch a clean recovery. What actually
happened, in real, measured sequence, was more interesting. A few seconds
after confirming `OPEN` (long enough, it turned out, for the 4-second
`wait-duration-in-open-state` to have already elapsed), more calls were
made - still with `inventory-service` in failure mode:

```bash
curl -s http://localhost:8082/order-with-stock/widget   # 1
curl -s http://localhost:8082/order-with-stock/widget   # 2
curl -s http://localhost:8082/order-with-stock/widget   # 3
```
```
FALLBACK: stock for widget unavailable right now (InternalServerError)
FALLBACK: stock for widget unavailable right now (InternalServerError)
FALLBACK: stock for widget unavailable right now (CallNotPermittedException)
```

```bash
curl -s http://localhost:8081/call-count
```
```
6
```

**What actually happened**: `automatic-transition-from-open-to-half-open-enabled=true`
meant the breaker didn't just sit `OPEN` forever - once
`wait-duration-in-open-state` (4s) elapsed, it moved itself to `HALF_OPEN`
and let exactly `permitted-number-of-calls-in-half-open-state` (2) **real**
trial calls through, to test whether the dependency had recovered. Both
trial calls genuinely reached `inventory-service` (`call-count` went from
4 to 6) and both genuinely failed, since `inventory-service` was still
broken - hence real `InternalServerError` fallbacks for the first two calls
of this batch, and only the *third* call (after both trial slots were
already used) got the short-circuited `CallNotPermittedException`, with no
real network call behind it at all.

Checking state again confirmed it: the failed trial sent the breaker
straight back to `HALF_OPEN` waiting for its next probe window (not back to
a settled `OPEN` - Resilience4j keeps re-entering `HALF_OPEN` on the same
timer as long as the dependency keeps failing its probes):

```bash
curl -s http://localhost:8082/circuit-state
```
```
state=HALF_OPEN, failureRate=-1.0%, bufferedCalls=0, failedCalls=0
```

**The real, valuable lesson here**: a circuit breaker with automatic
half-open transitions doesn't just "wait and then reopen" - it actively,
repeatedly *probes* the dependency with a small number of real calls on a
fixed interval, for as long as the dependency stays broken. This is by
design: `OPEN` protects the caller from a known-broken dependency, but the
breaker still needs a way to notice recovery without an operator manually
intervening - the periodic trial calls are that mechanism, and they do
reach the real service, on purpose, exactly `permitted-number-of-calls-in-half-open-state`
at a time.

## HALF_OPEN → CLOSED: real recovery, once the dependency is actually fixed

`inventory-service`'s failure mode was turned off - a real fix to the real
dependency, not a workaround in `order-service`:

```bash
curl -s -X POST http://localhost:8081/toggle-failure
```
```
failing=false
```

After the next automatic `HALF_OPEN` probe window opened, two calls:

```bash
curl -s http://localhost:8082/order-with-stock/widget
curl -s http://localhost:8082/order-with-stock/widget
```
```
product widget has 42 units in stock
product widget has 42 units in stock
```

Genuine successful responses - `inventory-service` really answered, and
answered correctly. And the breaker noticed:

```bash
curl -s http://localhost:8082/circuit-state
```
```
state=CLOSED, failureRate=-1.0%, bufferedCalls=0, failedCalls=0
```

Two successful trial calls in `HALF_OPEN` (matching
`permitted-number-of-calls-in-half-open-state=2`) were enough to convince
the breaker the dependency had genuinely recovered, closing it back to
normal operation - real calls flowing through again, no fallback needed.

## Key takeaways

- `@CircuitBreaker(name = ..., fallbackMethod = ...)` wraps a method so its
  calls are tracked against a named breaker; once that breaker's failure
  rate crosses its configured threshold over its sliding window, it opens
  and the fallback runs *instead of* the real call - not after it fails,
  before it's even attempted.
- An `OPEN` breaker throws `CallNotPermittedException` internally and routes
  straight to the fallback - confirmed here by the real dependency's own
  call counter staying flat while the breaker is genuinely blocking calls.
- With automatic half-open transitions enabled, an `OPEN` breaker does not
  stay inert - it periodically (every `wait-duration-in-open-state`) allows
  a small number of real trial calls through to test for recovery, cycling
  back to `OPEN` if they fail and closing for good if they succeed.
- The breaker's actual state (`CLOSED`/`OPEN`/`HALF_OPEN`) and metrics are
  queryable directly from `CircuitBreakerRegistry` at runtime - genuinely
  observable, not something that has to be inferred from response codes or
  timing.
- Circuit breakers complement service discovery (lesson 60) rather than
  replacing it: discovery answers "where is this service?"; a circuit
  breaker answers "should I even bother calling it right now?" - both real,
  independent concerns a resilient service-to-service call needs to handle.
