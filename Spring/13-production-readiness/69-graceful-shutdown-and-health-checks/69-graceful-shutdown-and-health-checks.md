# 69 - Graceful Shutdown and Health Checks

The last lesson before the capstone answers two closely related questions
about a running production service: "can this instance be trusted to
receive traffic right now?" (health checks, specifically liveness vs.
readiness), and "when this instance is told to stop, does it finish what
it's already doing first?" (graceful shutdown). Both were demonstrated
against a real, running application, not described in the abstract.

```bash
mvn -f Spring/13-production-readiness/69-graceful-shutdown-and-health-checks spring-boot:run
```

## Liveness vs. readiness: two different questions

```properties
management.endpoint.health.probes.enabled=true
management.health.livenessstate.enabled=true
management.health.readinessstate.enabled=true
```

This exposes `/actuator/health/liveness` and `/actuator/health/readiness` -
the same two probe types Kubernetes uses to decide, respectively, "should
this container be restarted?" (liveness) and "should this instance receive
traffic right now?" (readiness). They answer genuinely different questions:
a healthy-but-temporarily-overloaded instance should fail readiness (stop
new traffic) without failing liveness (it shouldn't be killed and
restarted for being busy).

[`DemoController.java`](src/main/java/com/danish/spring/gracefulshutdown/DemoController.java)
lets readiness be flipped manually, exactly the mechanism a real health
check (e.g. "database connection pool exhausted") would use internally:

```java
@PostMapping("/readiness/{state}")
public String setReadiness(@PathVariable String state) {
    ReadinessState newState = state.equalsIgnoreCase("up")
            ? ReadinessState.ACCEPTING_TRAFFIC
            : ReadinessState.REFUSING_TRAFFIC;
    AvailabilityChangeEvent.publish(publisher, this, newState);
    return "readiness set to " + newState;
}
```

Real output, before and after:

```bash
curl -s http://localhost:8080/actuator/health/readiness
curl -s http://localhost:8080/actuator/health/liveness
```
```json
{"status":"UP"}
{"status":"UP"}
```

```bash
curl -s -X POST http://localhost:8080/readiness/down
curl -s http://localhost:8080/actuator/health/readiness
curl -s http://localhost:8080/actuator/health/liveness
```
```
readiness set to REFUSING_TRAFFIC
{"status":"OUT_OF_SERVICE"}
{"status":"UP"}
```

Readiness flipped to `OUT_OF_SERVICE`; liveness stayed `UP` - exactly the
independence the two probes are supposed to have. A real, additional
finding: the **main** `/actuator/health` endpoint also flipped to
`OUT_OF_SERVICE`:

```bash
curl -s http://localhost:8080/actuator/health
```
```json
{"status":"OUT_OF_SERVICE","groups":["liveness","readiness"]}
```

The overall health aggregation (same rule from lesson 65 - any `DOWN`/
`OUT_OF_SERVICE` component drags down the whole status) includes the
readiness state by default, alongside the dedicated `/liveness` and
`/readiness` sub-paths - all three are real, independently queryable views
into the same underlying `ApplicationAvailability` state.

## Graceful shutdown: does an in-flight request get to finish?

```properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=10s
```

[`DemoController.slowTask()`](src/main/java/com/danish/spring/gracefulshutdown/DemoController.java)
simulates a real slow request:

```java
@GetMapping("/slow-task")
public String slowTask() throws InterruptedException {
    Thread.sleep(3000);
    return "completed";
}
```

The experiment: fire a request to `/slow-task`, and 300ms later - while it's
still sleeping - trigger shutdown via the real `/actuator/shutdown` endpoint
(a legitimate, if less commonly used than an orchestrator's `SIGTERM`,
Actuator feature that closes the `ApplicationContext` and runs the exact
same shutdown lifecycle a real `SIGTERM` would). Real output, with
`server.shutdown=graceful`:

```bash
curl -s http://localhost:8080/slow-task &
sleep 0.3
curl -s -X POST http://localhost:8080/actuator/shutdown
wait
```
```
{"message":"Shutting down, bye..."}
slow-task result: completed
elapsed: 3145ms
```

The request **completed successfully** - `"completed"`, after the full
~3.1 seconds the sleep actually needed - even though shutdown was triggered
only 300ms in. The application's own log confirms exactly what happened:

```
[Thread-1] o.s.b.w.e.tomcat.GracefulShutdown : Commencing graceful shutdown. Waiting for active requests to complete
[tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown : Graceful shutdown complete
```

~2.16 seconds elapsed between those two log lines - the server genuinely
waited for the in-flight request to finish, refusing new connections in
the meantime, before finally completing its shutdown.

## The contrast: `server.shutdown=immediate` (the default) cuts it off

The exact same experiment, with only `server.shutdown=immediate` changed
(Spring Boot's default, if `graceful` is never configured):

```bash
curl -s http://localhost:8080/slow-task &
sleep 0.3
curl -s -X POST http://localhost:8080/actuator/shutdown
wait
```
```
{"message":"Shutting down, bye..."}
elapsed: 1005ms
(curl exit code 52 - empty reply from server)
```

The request never got a response at all - `curl` reported "empty reply
from server," and the whole exchange took ~1 second instead of the ~3
seconds the sleep needed. The application's own log shows exactly why -
no graceful-shutdown log lines at all, and a real exception from the
in-flight request itself:

```
ERROR ... Servlet.service() ... threw exception [Request processing failed: java.lang.InterruptedException: sleep interrupted]
```

The server didn't wait for anything - it tore down immediately, and the
in-flight request's own thread was interrupted mid-`Thread.sleep`, exactly
as the exception says. This is the real, concrete cost of the default
shutdown mode: any request in flight at the moment of shutdown is simply
abandoned, mid-execution, with no chance to finish or even respond with an
error - the client just sees a dropped connection.

## Key takeaways

- Liveness and readiness are genuinely separate signals -
  `/actuator/health/liveness` answers "should this be restarted?",
  `/actuator/health/readiness` answers "should this receive traffic?" - and
  flipping one independently of the other is exactly the real mechanism
  behind graceful, zero-downtime deployments (mark not-ready, drain
  traffic, *then* actually stop).
- `/actuator/health`'s overall status aggregates the readiness/liveness
  groups alongside any custom health indicators (lesson 65) - a real,
  single point of truth for orchestration tooling.
- `server.shutdown=graceful` measurably changes real behavior: an in-flight
  request was allowed to run to completion (confirmed by its actual
  successful response and the real `~3.1s` elapsed time) before the
  process finished shutting down, logged explicitly by Spring's own
  `GracefulShutdown` component.
- The default (`server.shutdown=immediate`) has a real, observable cost:
  the identical in-flight request was abandoned mid-execution, its thread
  interrupted, and its caller got a dropped connection instead of a
  response - a genuine difference a real deployment (especially one behind
  a load balancer performing rolling restarts) should not accept by
  accident.
