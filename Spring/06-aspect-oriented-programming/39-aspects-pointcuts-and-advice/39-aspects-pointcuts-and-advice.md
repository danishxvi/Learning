# 39 · Aspects, pointcuts and advice

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/06-aspect-oriented-programming/39-aspects-pointcuts-and-advice spring-boot:run
> ```

Lesson 38 built the proxy mechanism by hand and confirmed Spring uses it for real beans.
This lesson writes real `@Aspect` classes — the declarative layer on top of that
mechanism — and applies them to an `OrderService` that has no idea either one exists.

---

## 1. `@Aspect` + `@Component` — both required

```java
@Aspect
@Component
public class LoggingAspect { ... }
```

`@Aspect` marks a class as a source of advice; `@Component` is what actually registers
it as a Spring bean. An `@Aspect` that's never picked up as a bean does nothing at all —
the same "declared but never wired in" trap lesson 15 covered for auto-configuration
classes not listed in `AutoConfiguration.imports`.

---

## 2. A pointcut expression — matching by method signature

```java
@Pointcut("execution(* com.danish.spring.aspects.service..*(..))")
public void loggableMethod() { }
```

`execution(...)` matches by **method signature**: `*` (any return type),
`com.danish.spring.aspects.service` (this package), `..` (and every sub-package),
`*` (any method name), `(..)` (any arguments). Defining it once as a named pointcut
(`loggableMethod()`) and reusing that name across four advice methods below avoids
repeating the expression string four times — change the scope once, every advice method
picks it up.

---

## 3. Four advice types, and exactly when each one fires

```java
@Before("loggableMethod()")
public void logBefore(JoinPoint joinPoint) { ... }

@AfterReturning(pointcut = "loggableMethod()", returning = "result")
public void logAfterReturning(JoinPoint joinPoint, Object result) { ... }

@AfterThrowing(pointcut = "loggableMethod()", throwing = "ex")
public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) { ... }

@After("loggableMethod()")
public void logAfter(JoinPoint joinPoint) { ... }
```

Running the happy path (`placeOrder`, which returns normally):

```
[Before] OrderService.placeOrder(..) called with [desk-lamp]
[AfterReturning] OrderService.placeOrder(..) returned: Order placed for desk-lamp
[After] OrderService.placeOrder(..) finished (success or not)
```

Running the failure path (`cancelOrder("missing")`, which throws):

```
[Before] OrderService.cancelOrder(..) called with [missing]
[AfterThrowing] OrderService.cancelOrder(..) threw: IllegalArgumentException - No such order: missing
[After] OrderService.cancelOrder(..) finished (success or not)
Exception reached the caller anyway: No such order: missing
```

**`@AfterReturning` did not fire on the failure path** — it only runs when the method
returns normally. **`@After` fired on both paths** — it's the AOP equivalent of a
`finally` block, running regardless of outcome. **The exception still reached the
original caller** — `@AfterThrowing` observes it and can log it, but does not swallow it
(an `@Around` advice, next, is what would actually need to catch and suppress an
exception on purpose).

`returning = "result"` and `throwing = "ex"` bind the actual return value or thrown
exception into the advice method's parameter, matched **by name** — the parameter must
be called `result`/`ex` to line up with the string in the annotation.

---

## 4. `@Around` — the only advice that controls whether the method runs at all

```java
@Around("@annotation(Timed)")
public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    Object result = joinPoint.proceed();   // the real method body runs HERE
    long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
    System.out.println(joinPoint.getSignature().toShortString() + " took " + elapsedMillis + " ms");
    return result;
}
```

```
[Timed] OrderService.slowReport() took 88 ms
```

`@Around` receives a `ProceedingJoinPoint` and decides **whether and when** to call
`proceed()` — the other three advice types can only observe around a call that's
happening regardless. This is what makes `@Around` capable of things the others can't
do at all: skip the real method entirely, call it more than once, retry it, or — as
here — wrap it with timing measured around the *actual* method body (`slowReport`'s real
`Thread.sleep(80)`), not just the advice's own overhead. `88ms` measured against a real
`80ms` sleep confirms this is timing the genuine method execution, not a coincidence.

---

## 5. `@annotation(Timed)` — a different matching style entirely

`LoggingAspect` matches by **package and signature** (`execution(...)`). `TimingAspect`
matches by **presence of an annotation** (`@annotation(Timed)`) — any method, in any
class, anywhere, carrying `@Timed`. `@Timed` itself carries no code (exactly like lesson
26's `@ValidIsbn`); `TimingAspect` is what gives it meaning. This is the same technique
`@Transactional` (section 05) and `@Cacheable` (section 10) use to opt individual
methods into cross-cutting behavior — a custom annotation plus an aspect matching on it
is a completely ordinary, buildable pattern, not something reserved for Spring's own
annotations.

---

## 6. Summary

- **`@Aspect`** declares advice; **`@Component`** is what actually registers it — both
  are required.
- **`execution(...)`** pointcuts match by method signature (return type, package, class,
  method name, parameters); **`@annotation(...)`** pointcuts match by an annotation's
  presence, regardless of location — two genuinely different matching styles for two
  different situations.
- **`@Before`** runs before the call; **`@AfterReturning`** only on a normal return;
  **`@AfterThrowing`** only on an exception (without swallowing it); **`@After`** always,
  the AOP equivalent of `finally`.
- **`@Around`** is the only advice that receives a `ProceedingJoinPoint` and decides
  whether/when to call `proceed()` — the only one capable of skipping, retrying, or
  wrapping the real call with something measured around its actual execution.
- **A custom marker annotation plus a matching `@Aspect`** is exactly how
  `@Transactional`-style, opt-in cross-cutting behavior is built — nothing about the
  technique is exclusive to Spring's own annotations.

---

**Previous:** [38 — Cross-cutting concerns and the proxy problem](../38-cross-cutting-concerns-and-the-proxy-problem/38-cross-cutting-concerns-and-the-proxy-problem.md) ·
**Next:** [40 — Authentication vs authorization](../../07-spring-security/40-authentication-vs-authorization/40-authentication-vs-authorization.md)
