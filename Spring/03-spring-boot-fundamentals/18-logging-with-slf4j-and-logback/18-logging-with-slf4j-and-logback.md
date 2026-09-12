# 18 · Logging with SLF4J and Logback

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/18-logging-with-slf4j-and-logback spring-boot:run
> ```
> With the file appender active: `-Dspring-boot.run.profiles=verbose`, then read
> `target/verbose-demo.log`.

Every lesson so far has printed with `System.out.println` for demo output, but Spring
Boot's own startup lines (`Starting LoggingApplication...`, `Started ... in 1.4
seconds`) have always come from somewhere else. This lesson is that somewhere else.

---

## 1. SLF4J is a facade; Logback is the implementation actually running

```java
private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);
```

`org.slf4j.Logger` and `org.slf4j.LoggerFactory` are the **only** logging types this
class ever mentions. Lesson 16's dependency tree showed `spring-boot-starter-logging`
pulling in `logback-classic` — SLF4J is the facade every call goes through; Logback is
the concrete engine underneath actually formatting and writing lines. This separation is
the entire point: swapping to Log4j2 means changing one dependency, never touching a
single `log.info(...)` call anywhere in the codebase.

---

## 2. Five levels, and what "the threshold" means

```java
log.trace(...);  log.debug(...);  log.info(...);  log.warn(...);  log.error(...);
```

Only levels **at or above the configured threshold** print. `application.yml` in this
lesson sets:

```yaml
logging:
  level:
    root: INFO
    com.danish.spring.logging: DEBUG
```

Running the lesson: `TRACE` never appears (below even `DEBUG`); `DEBUG` through `ERROR`
all print, because this specific package's threshold was raised. A class in a different
package would fall back to `root: INFO` and never show `DEBUG`. This is `application.yml`
configuration reaching all the way into a specific package's runtime log verbosity — no
code change, no redeploy, just a property (lesson 05's whole precedence chain applies
here too — a `-D` flag or environment variable overrides this the same way).

---

## 3. Parameterized logging vs string concatenation

```java
log.debug("Concatenated (bad): order=" + expensiveDescription());   // ALWAYS calls expensiveDescription()
log.debug("Parameterized (good): order={}", expensiveDescription()); // SLF4J decides whether to call toString()
```

With string concatenation, `expensiveDescription()` runs and its result is thrown away
the instant `DEBUG` is disabled — Java has to build the concatenated string *before*
`log.debug` is even called, regardless of whether the level is enabled. With the `{}`
placeholder form, the argument is passed as an object; SLF4J only converts it to a string
if the level check actually passes. **The difference is invisible when the level is
enabled** (both ran `expensiveDescription()` in this lesson's output, because `DEBUG` is
on for this package) **and significant when it's disabled** — at `INFO` in production,
the parameterized form skips the expensive call entirely; the concatenated form still
pays for it on every single call, forever. Always prefer `{}` placeholders over building
the string yourself.

---

## 4. Logging an exception properly

```java
log.error("Order processing failed", ex);   // ex as the LAST argument
```

This prints the message **and the full stack trace** — visible in this lesson's output,
naming every frame from `OrderProcessor.demonstrateExceptionLogging` down through
`SpringApplication.run`. Calling `log.error("Order processing failed: " + ex.getMessage())`
instead is a common mistake: it keeps only the one-line message and throws away the stack
trace that would have shown *where* the exception actually came from — exactly the
information needed to debug it later.

---

## 5. `logback-spring.xml` — Boot-aware configuration

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] [requestId=%X{requestId:-none}] %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

Named `logback-spring.xml`, **not** `logback.xml` — Boot looks for this specific name so
it can process Spring-only tags (like `<springProfile>` below) before Logback's own
initialization, which happens too early to see Boot's configuration otherwise. This
lesson's custom pattern adds `[requestId=%X{requestId:-none}]`, printing `none` by default
and the actual value once one is set — see MDC, next.

```xml
<springProfile name="verbose">
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>target/verbose-demo.log</file>
        ...
    </appender>
    <root level="INFO"><appender-ref ref="FILE"/></root>
</springProfile>
```

`<springProfile>` — a tag that only means something inside `logback-spring.xml` — gates
this entire file appender on the `verbose` profile being active, using the exact same
profile mechanism as lesson 12's `@Profile`. Running with `-Dspring-boot.run.profiles=verbose`
produces a real `target/verbose-demo.log`, while the default run produces none.

---

## 6. MDC — contextual data without threading a parameter everywhere

```java
MDC.put("requestId", requestId);
try {
    log.info("Handling request: {}", action);
    orderProcessor.demonstrateLevels();   // logs from a DIFFERENT class
    log.info("Request complete");
} finally {
    MDC.remove("requestId");
}
```

Every line logged while `requestId` is set in MDC — even from `OrderProcessor`, a
completely different class that never received `requestId` as a parameter — carries it in
the output, because the pattern's `%X{requestId}` reads Logback's **thread-local** MDC
map, not anything passed explicitly. This is how a real web application correlates every
log line produced while handling one HTTP request: a filter puts a request ID into MDC
once, at the start, and every class touched during that request's handling logs it
automatically. **Always clear MDC in a `finally` block** — it's thread-local, and thread
pools reuse threads, so a forgotten entry silently leaks into the next, unrelated request
handled by the same thread later.

---

## 7. Summary

- **SLF4J is the facade** every call goes through; **Logback is the implementation**
  actually running — swapping implementations needs a dependency change, not a code
  change.
- Only levels **at or above the configured threshold** print; `logging.level.<package>`
  in `application.yml` overrides the root threshold for one package.
- **Parameterized `{}` logging** defers the expensive `toString()`/argument evaluation
  until SLF4J confirms the level is actually enabled — string concatenation always pays
  the cost, even when the line will be discarded.
- **Pass the exception as the last argument** to log a full stack trace — concatenating
  `ex.getMessage()` into the string throws that trace away.
- **`logback-spring.xml`** (not `logback.xml`) lets Boot process Spring-specific tags
  like `<springProfile>` before Logback initializes.
- **MDC** attaches contextual data (a request ID) to every log line on the current
  thread, across every class, with no parameter threading — always cleared in a `finally`
  block because it's thread-local and thread pools reuse threads.

---

**Previous:** [17 — `CommandLineRunner` and `ApplicationRunner`](../17-commandlinerunner-and-applicationrunner/17-commandlinerunner-and-applicationrunner.md) ·
**Next:** [19 — DevTools and the inner development loop](../19-devtools-and-the-inner-development-loop/19-devtools-and-the-inner-development-loop.md)
