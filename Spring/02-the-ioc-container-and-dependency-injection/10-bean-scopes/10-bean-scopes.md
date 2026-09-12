# 10 · Bean scopes

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/10-bean-scopes spring-boot:run
> ```

Every bean in every earlier lesson has been a **singleton** — one instance, shared by the
whole application, exactly like `MiniContainer`'s cache in lesson 01. This lesson adds
**prototype** scope, reproduces the single most common mistake made with it, and fixes
that mistake two different ways.

---

## 1. Singleton — the default

```java
@Component               // no @Scope at all - singleton either way
public class SingletonCounter {
    private int count = 0;
    public int increment() { return ++count; }
}
```

Calling `context.getBean(SingletonCounter.class)` three times and incrementing each time
produces `1, 2, 3` — every call returned the **same object**, sharing its state. This has
been true since lesson 02 (`context.getBean(OrderService.class) ==` itself, twice); this
lesson just names it. Singleton is correct for the overwhelming majority of beans —
services, repositories, controllers — anything that either holds no mutable state or is
meant to share it deliberately.

---

## 2. Prototype — a new instance every time

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Counter {
    private final int instanceId = ...;   // unique per instance
    ...
}
```

Asking the container for this bean three times in a row:

```
instanceId=2, count=1
instanceId=3, count=1
instanceId=4, count=1
```

Three different objects, each starting fresh. Unlike singleton, **Spring does not manage
a prototype bean's full lifecycle** — it constructs and initializes it (steps 1–4 from
lesson 06), then hands it over completely; there's no reference kept to call
`@PreDestroy` on later, because the container doesn't know when you're done with it.

---

## 3. The mistake: injecting a prototype into a singleton

```java
@Component                                  // singleton (default, no @Scope)
public class BrokenPrototypeHolder {
    private final Counter counter;          // Counter is @Scope("prototype")

    public BrokenPrototypeHolder(Counter counter) {
        this.counter = counter;             // called ONCE, ever
    }
}
```

Calling `incrementAndPrint()` three times:

```
instanceId=1, count=1
instanceId=1, count=2
instanceId=1, count=3
```

**Same `instanceId` every time.** `BrokenPrototypeHolder` is a singleton, so its
constructor runs exactly once, at startup. Whatever `Counter` instance exists at that one
moment gets frozen into the `counter` field forever — every later call reuses that same
frozen object. "Prototype" scope never actually manifests from this consumer's
perspective; the constructor injection pattern this whole stack has recommended since
lesson 08 is exactly what causes the problem here, because it resolves the dependency
**once**, which is precisely wrong when the dependency's whole point is to be
re-resolved.

---

## 4. Fix #1: `ObjectProvider<T>` — ask again, lazily, on every use

```java
public class ObjectProviderHolder {
    private final ObjectProvider<Counter> counterProvider;

    public void incrementAndPrint() {
        Counter counter = counterProvider.getObject();   // resolved NOW, not at construction
        ...
    }
}
```

```
instanceId=5, count=1
instanceId=6, count=1
instanceId=7, count=1
```

Different `instanceId` every call. `ObjectProvider<Counter>` is itself a singleton-safe
handle — constructing `ObjectProviderHolder` creates no `Counter` at all. Every call to
`counterProvider.getObject()` asks the container for a fresh lookup at that exact moment,
respecting whatever scope `Counter` actually has. This is the **general-purpose fix**: it
requires no special annotation on `Counter` itself, and works identically for prototype,
request, or session scope.

---

## 5. Fix #2: a scoped proxy — inject the "real" bean anyway

```java
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ScopedProxyCounter { ... }
```

```java
public class ScopedProxyHolder {
    private final ScopedProxyCounter counter;   // looks IDENTICAL to the broken version

    public ScopedProxyHolder(ScopedProxyCounter counter) {
        this.counter = counter;
    }
}
```

```
instanceId=1, count=1
instanceId=3, count=1
instanceId=5, count=1
```

Different `instanceId` every call again — from code that looks **exactly** like
`BrokenPrototypeHolder`'s mistake. The difference lives entirely on `ScopedProxyCounter`'s
own declaration: `proxyMode = ScopedProxyMode.TARGET_CLASS` tells Spring to inject a
lightweight CGLIB proxy instead of a real instance. The proxy holds no state of its own —
every method call on it looks up the current prototype instance fresh, then delegates.
`ScopedProxyHolder`'s code never needed to change; the fix is entirely a property of the
bean being injected. This is the required mechanism for `request` and `session` scope,
where there genuinely is no "the" instance until an HTTP request exists — the proxy is
what lets you inject a request-scoped bean into an ordinary singleton controller safely.

---

## 6. The web scopes: `request` and `session`

Two more scopes exist beyond singleton and prototype:

| Scope | Lifetime |
| --- | --- |
| `request` | One instance per HTTP request — created fresh, discarded when the response is sent. |
| `session` | One instance per user session — lives as long as the `HttpSession` does. |

Both require an active web request to mean anything at all, so neither can run in this
lesson's plain console application — they return once section 04 introduces Spring MVC and
real HTTP requests come into play. The scoped-proxy technique from this lesson is exactly
how a `request`-scoped bean gets safely injected into a singleton `@RestController`
without every request needing to look it up manually.

---

## 7. Summary

- **Singleton** (the default): one shared instance for the whole container.
- **Prototype**: a new instance every time the container is asked — but the container
  never tracks it afterward, so no `@PreDestroy` callback ever fires on it.
- **Injecting a prototype into a singleton via a normal constructor freezes one instance
  forever** — the constructor runs once, so the "fresh every time" promise breaks
  silently, with no error at any point.
- **`ObjectProvider<T>`**: the general-purpose fix — resolve the dependency lazily, on
  every use, instead of once at construction.
- **A scoped proxy** (`proxyMode = ScopedProxyMode.TARGET_CLASS`): fixes the same problem
  from the *bean's* side — consumers keep writing normal-looking injection code, and the
  proxy re-resolves on every call.
- **`request`** and **`session`** scope exist for web applications and need an active HTTP
  request to make sense — covered again once section 04 introduces Spring MVC.

---

**Previous:** [09 — `@Configuration` classes and `@Bean` methods](../09-configuration-classes-and-bean-methods/09-configuration-classes-and-bean-methods.md) ·
**Next:** [11 — Qualifiers and resolving ambiguous beans](../11-qualifiers-and-resolving-ambiguous-beans/11-qualifiers-and-resolving-ambiguous-beans.md)
