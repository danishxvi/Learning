# 20 · Actuator basics

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/20-actuator-basics spring-boot:run
> ```
> Then, in another terminal:
> ```bash
> curl http://localhost:8081/actuator/health
> ```
> Note the port — **8081**, not 8080. See section 3 below for why.

Every lesson in this stack so far has been a black box from the outside — no way to ask a
running instance "are you healthy," "what version are you," or "how much memory are you
using" without reading source code. Actuator answers exactly those questions, over HTTP,
with almost no code.

---

## 1. Adding it

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Actuator's endpoints are exposed over HTTP, so this lesson is the first in the stack to
add `spring-boot-starter-web` — the exact starter section 04 builds full REST APIs on.
Here it exists only to give Actuator a server to publish endpoints through; nothing in
this lesson defines a `@RestController`.

Starting the app prints a real, unedited log line:

```
Exposing 5 endpoints beneath base path '/actuator'
```

By default only `health` is exposed over HTTP — this lesson's `application.yml` widens
that list explicitly:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,beans,env
```

**Exposure is opt-in on purpose.** Endpoints like `env` or `beans` can reveal internal
configuration structure — widen this list deliberately, per endpoint, not with a
blanket `include: "*"` in anything that isn't a fully trusted internal network.

---

## 2. `/actuator/health` — including a custom check

```bash
curl http://localhost:8081/actuator/health
```
```json
{"status":"UP","components":{
  "diskSpace":{"status":"UP","details":{"total":302167093248,"free":275922894848, ...}},
  "paymentGateway":{"status":"UP","details":{"gateway":"stripe-sandbox","latencyMs":42}},
  "ping":{"status":"UP"}
}}
```

`diskSpace` and `ping` are built in. `paymentGateway` is **this lesson's own bean**:

```java
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {
    public Health health() {
        return Health.up().withDetail("gateway", "stripe-sandbox").withDetail("latencyMs", 42).build();
    }
}
```

Actuator finds any bean implementing `HealthIndicator` automatically — the same
"discover every bean of this type" pattern lesson 11 covered with `List<T>` injection —
and folds its result into the **overall** status: if `paymentGateway.health()` had
returned `Health.down()`, the top-level `"status"` would flip to `"DOWN"` too, exactly
the signal a Kubernetes liveness/readiness probe or a load balancer's health check is
built to watch for. This is how a real application reports "a critical dependency is
unreachable" without inventing a custom protocol for it.

---

## 3. Running Actuator on a separate port

```yaml
server:
  port: 8080
management:
  server:
    port: 8081
```

Starting this lesson prints **two** embedded Tomcat instances coming up:

```
Tomcat initialized with port 8080 (http)
...
Tomcat initialized with port 8081 (http)
```

And confirms the split live: `curl -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health`
returns **`404`** — the application port has no Actuator endpoints at all; they only
exist on `8081`. This is a deliberate security boundary: the app's public port (8080,
what a load balancer or the internet reaches) never carries operational internals, and
the management port (8081, reachable only inside a private network or a cluster's
internal routing) carries them exclusively. In production, this port split is usually
paired with a firewall rule blocking external access to it entirely.

---

## 4. `/actuator/info` and `/actuator/metrics`

```yaml
management:
  info:
    env:
      enabled: true   # without this, info.* properties are silently ignored
info:
  app:
    name: Actuator Basics Demo
    version: "1.0.0"
```

```bash
curl http://localhost:8081/actuator/info
```
```json
{"app":{"name":"Actuator Basics Demo","description":"Lesson 20 of the Spring stack","version":"1.0.0"}}
```

Any `info.*` property becomes part of this endpoint's response — a standard place to
publish a build version or git commit hash without writing an endpoint by hand.
**`management.info.env.enabled: true` is required** for this in the version used here;
without it, `/actuator/info` returns an empty `{}` even with `info.*` properties set —
confirmed by hitting the endpoint both ways while writing this lesson.

```bash
curl http://localhost:8081/actuator/metrics/jvm.memory.used
```
```json
{"name":"jvm.memory.used","baseUnit":"bytes","measurements":[{"statistic":"VALUE","value":73961416}], ...}
```

`/actuator/metrics` exposes a whole tree of runtime measurements — JVM memory, garbage
collection counts, HTTP request timings once section 04 adds real endpoints — collected
by **Micrometer**, the metrics facade Spring Boot builds this endpoint on top of (the
same one lesson 13's dependency tree would show if `spring-boot-starter` were inspected
closely — `micrometer-observation` was already there, quietly, since lesson 03).

---

## 5. `/actuator/env` redacts values by default — with no security library present

```bash
curl http://localhost:8081/actuator/env/java.version
```
```json
{"property":{"source":"systemProperties","value":"******"}, ...}
```

**Every value comes back as `******`** — not just genuinely sensitive keys like
passwords, *every* property, including a harmless one like `java.version`. This is
`management.endpoint.env.show-values` defaulting to `WHEN_AUTHORIZED`: Actuator only
reveals real values to a request it can confirm is authorized, and with no Spring
Security (section 07) on the classpath at all, **no request can ever be classified as
authorized** — so the safe default redacts everything, unconditionally, rather than
guess. This is a genuine safety net: adding `spring-boot-starter-actuator` to a project
with no security configured yet does not silently leak configuration to the internet.

---

## 6. `/actuator/beans`

```bash
curl http://localhost:8081/actuator/beans
```

Returns a JSON dump of **every bean definition in the context** — type, scope, resource
origin, dependencies — a live, queryable version of what
`context.getBeanDefinitionNames()` printed as plain text back in lesson 04. Useful for
answering "is my bean actually registered" or "which auto-configuration created this"
against a running instance, without attaching a debugger.

---

## 7. Summary

- **`spring-boot-starter-actuator`** needs a web starter to expose endpoints over HTTP;
  by default only `health` is exposed — widen `management.endpoints.web.exposure.include`
  deliberately, per endpoint.
- **Custom health checks** are just beans implementing `HealthIndicator` — Actuator
  discovers them automatically and folds their status into the overall
  `/actuator/health` result, which is exactly what container orchestrators poll.
- **`management.server.port`** runs Actuator on a separate port from the application —
  confirmed here by the app port genuinely returning 404 for `/actuator/*`.
- **`/actuator/info`** publishes arbitrary `info.*` properties, but needs
  `management.info.env.enabled: true` to actually include them.
- **`/actuator/metrics`** exposes Micrometer-collected runtime measurements — JVM memory
  and GC stats are present even with zero application code contributing metrics.
- **`/actuator/env` redacts every value by default** when no security mechanism is
  present to authorize the request — a genuine safety default, not a bug.
- **`/actuator/beans`** is `context.getBeanDefinitionNames()` (lesson 04), queryable live
  over HTTP against a running instance.

---

**Previous:** [19 — DevTools and the inner development loop](../19-devtools-and-the-inner-development-loop/19-devtools-and-the-inner-development-loop.md) ·
**Next:** [21 — `@RestController` and the `@RequestMapping` family](../../04-building-rest-apis-with-spring-mvc/21-restcontroller-and-request-mapping/21-restcontroller-and-request-mapping.md)
