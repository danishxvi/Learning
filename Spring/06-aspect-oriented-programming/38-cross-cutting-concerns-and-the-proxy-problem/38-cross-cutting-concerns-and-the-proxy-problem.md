# 38 · Cross-cutting concerns and the proxy problem

> **Run the code for this lesson** (Spring Boot's default — CGLIB even for an
> interface-backed bean)
> ```bash
> mvn -f Spring/06-aspect-oriented-programming/38-cross-cutting-concerns-and-the-proxy-problem spring-boot:run
> ```
> Forcing JDK dynamic proxies where possible:
> ```bash
> mvn -f Spring/06-aspect-oriented-programming/38-cross-cutting-concerns-and-the-proxy-problem spring-boot:run -Dspring-boot.run.arguments=--spring.aop.proxy-target-class=false
> ```

`@Transactional` (section 05), `@Cacheable` (section 10), and Spring's own logging and
security checks all share one mechanism: a **proxy** wrapping the real bean. This lesson
builds that mechanism by hand, then asks Spring exactly what kind of proxy it actually
built.

---

## 1. What a "cross-cutting concern" is

Logging, timing, transaction management, and security checks all share a shape: they
need to happen **around** many otherwise-unrelated methods, in a consistent way, without
duplicating the same handful of lines inside every one of those methods. Neither
inheritance nor composition expresses this well — a logging concern doesn't belong in a
base class every service happens to extend, and threading a `Logger` parameter through
every method call is exactly the kind of boilerplate lesson 01 showed dependency
injection eliminating for constructors. **Aspect-Oriented Programming (AOP)** is the
answer: express the cross-cutting behavior once, and apply it to many methods
declaratively, without editing any of them.

---

## 2. Building the mechanism by hand: a JDK dynamic proxy

```java
interface Notifier { void send(String message); }
class RealNotifier implements Notifier { ... }

class LoggingInvocationHandler implements InvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("BEFORE " + method.getName());
        Object result = method.invoke(target, args);
        System.out.println("AFTER " + method.getName());
        return result;
    }
}

Notifier proxy = (Notifier) Proxy.newProxyInstance(
        classLoader, new Class<?>[]{Notifier.class}, new LoggingInvocationHandler(real));
```

```
proxy.getClass() = class jdk.proxy2.$Proxy49
[proxy] BEFORE send
[RealNotifier] sending: Hello from a hand-built proxy
[proxy] AFTER send
```

`java.lang.reflect.Proxy.newProxyInstance` builds a real class, **at runtime**,
implementing `Notifier` — every method call on it routes through the
`InvocationHandler` first, which decides what happens before and after actually
delegating to the real object. This is the exact mechanism lesson 31 found underneath a
plain `JpaRepository` interface (`jdk.proxy2.$Proxy94`) — Spring didn't invent a new
idea for repositories; it used this one.

---

## 3. Which kind of proxy does Spring actually build?

Spring has **two** proxy mechanisms available:

| | JDK dynamic proxy | CGLIB proxy |
| --- | --- | --- |
| Requires | The target to implement an interface | Nothing — works on any non-final class |
| How | Implements the interface, like the hand-built demo above | Generates a runtime **subclass** of the target class (lesson 09's `@Configuration` full-mode mechanism) |
| Limitation | Cannot proxy a class with no interface | Cannot proxy a `final` class or `final` methods |

Given a bean implementing an interface, which one does Spring pick? Running this lesson
with Spring Boot's own defaults:

```
GreetingService (implements an interface):
  getClass() = class com.danish.spring.proxies.GreetingServiceImpl$$SpringCGLIB$$0
  isCglibProxy = true

ClassOnlyGreeter (NO interface):
  getClass() = class com.danish.spring.proxies.ClassOnlyGreeter$$SpringCGLIB$$0
  isCglibProxy = true
```

**Both are CGLIB — even the interface-backed one.** Spring Boot sets
`spring.aop.proxy-target-class=true` by default (plain Spring Framework alone defaults
to `false` — prefer JDK proxies when possible). Boot's reasoning: CGLIB proxies work
identically whether or not a class has an interface, so defaulting to it avoids a class
of confusing "why does this bean behave differently now that I added/removed an
interface" surprises. Flipping the property back:

```
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.aop.proxy-target-class=false
```
```
GreetingService (implements an interface):
  getClass() = class jdk.proxy2.$Proxy45
  isCglibProxy = false

ClassOnlyGreeter (NO interface):
  getClass() = class com.danish.spring.proxies.ClassOnlyGreeter$$SpringCGLIB$$0   <-- unchanged
```

The interface-backed bean switches to a real JDK proxy; the class-only bean **cannot**
— it has no interface for `java.lang.reflect.Proxy` to implement, so Spring falls back
to CGLIB for it regardless of the setting.

---

## 4. A proxy only exists where something needs it

Neither `GreetingServiceImpl` nor `ClassOnlyGreeter` would be proxied at all without the
`@Transactional` annotation on each — a plain `@Service` with no cross-cutting behavior
applying to it is registered as the bare object it actually is, no proxy involved.
Proxies are created **only** where genuine advice (a transaction boundary, in this
lesson; a custom `@Aspect`, in lesson 39) needs to intercept calls to the bean.

---

## 5. The self-invocation problem, restated precisely

```java
public String greetTwice(String name) {
    return greet(name) + " " + greet(name);   // `this.greet(...)`, not proxy.greet(...)
}
```

```
Calling greetTwice(), which internally calls greet() via `this.`:
  Hello, Danish! Hello, Danish!
```

Both calls to `greet` ran correctly — nothing is broken about ordinary method calls.
**What breaks is advice specifically targeting `greet` itself** — a hypothetical
`@Transactional(propagation = REQUIRES_NEW)` on `greet` (lesson 33's exact scenario)
would never see this internal call, because `this.greet(...)` is a direct call on the
real object, never routed through the proxy that wraps `GreetingServiceImpl` from the
outside. This is precisely why lesson 33's `REQUIRES_NEW` demo silently produced ordinary
behavior instead of a genuinely new transaction: the proxy the whole `@Transactional`
mechanism depends on was never in the call path for that particular call.

---

## 6. Summary

- **Cross-cutting concerns** (logging, transactions, security checks) need to wrap many
  otherwise-unrelated methods consistently — AOP applies that behavior declaratively,
  without editing each method.
- A **JDK dynamic proxy** (`java.lang.reflect.Proxy`) implements an interface at
  runtime, routing every call through an `InvocationHandler` — buildable by hand, and
  exactly what lesson 31's repository proxy turned out to be.
- A **CGLIB proxy** generates a runtime subclass instead — works on any non-final class,
  interface or not.
- **Spring Boot defaults to CGLIB even for interface-backed beans**
  (`spring.aop.proxy-target-class=true`) — plain Spring Framework defaults the other way.
- **A bean is only proxied where something (a `@Transactional` method, a matching
  `@Aspect`) actually needs to intercept it** — a plain bean stays a plain object.
- **Self-invocation bypasses whatever proxy wraps a bean** — ordinary method calls still
  work, but any advice targeting the called method specifically (a transaction boundary,
  a custom aspect) never sees a call made via `this.` from inside the same class.

---

**Previous:** [37 — Connecting to a real database](../../05-data-access-with-spring-data-jpa/37-connecting-to-a-real-database/37-connecting-to-a-real-database.md) ·
**Next:** [39 — Aspects, pointcuts and advice](../39-aspects-pointcuts-and-advice/39-aspects-pointcuts-and-advice.md)
