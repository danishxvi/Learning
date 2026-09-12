# 08 · Constructor vs field vs setter injection

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/08-constructor-vs-field-vs-setter-injection spring-boot:run
> ```

Every lesson so far quietly used constructor injection. This lesson compares all three
styles side by side, breaks one of them on purpose, and reproduces — then fixes — the
single most common Spring startup error: a circular dependency.

---

## 1. Constructor injection — the default

```java
@Component
public class ConstructorInjectedOrderService {
    private final Notifier notifier;          // final - set once, never reassigned

    public ConstructorInjectedOrderService(Notifier notifier) {
        this.notifier = notifier;
    }
}
```

Two properties fall out of this shape for free:

- **The field can be `final`.** Nothing in this class can ever reassign `notifier` after
  construction — the compiler enforces it.
- **It works with plain `new`, no Spring involved:**
  ```java
  new ConstructorInjectedOrderService(fakeNotifier).placeOrder("test-widget");  // no context needed
  ```
  This is the entire reason lesson 01's manually-written classes could be tested without a
  framework — constructor injection doesn't stop being ordinary Java just because Spring
  also knows how to call it.

**This is the recommended default for every required dependency.**

---

## 2. Field injection — why it's usually discouraged

```java
@Component
public class FieldInjectedOrderService {
    @Autowired
    private Notifier notifier;    // NOT final - Spring sets it via reflection, after construction
}
```

Run this lesson and compare the two calls to `placeOrder`:

```
new FieldInjectedOrderService()   ->  notifier is NULL
context.getBean(...)              ->  works fine
```

Spring builds this object with a normal no-arg constructor, *then* reaches into the
private field with reflection and sets it. That means:

- The field **cannot be `final`** — reflection can't set a `final` field after
  construction (short of unsafe tricks nobody should use).
- **Testing without a Spring context is impossible through normal Java** — there's no
  constructor parameter to hand a fake dependency to. The demo above shows exactly this:
  built by hand, `notifier` is silently `null`.

Field injection isn't broken — Spring handles it correctly. It's discouraged because it
hides a class's real dependencies (you have to read every field, not just one constructor
signature) and makes plain-Java testing impossible.

---

## 3. Setter injection — for genuinely optional dependencies

```java
@Component
public class ReportGenerator {
    private AuditLogger auditLogger;   // no AuditLogger bean exists in this lesson at all

    @Autowired(required = false)
    public void setAuditLogger(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }
}
```

`required = false` tells Spring: call this setter if a matching bean exists; otherwise,
leave the field alone and move on. Compare this to constructor injection, where a missing
dependency is **always** a startup failure — there's no way to make one constructor
parameter optional without redesigning the constructor. Running this lesson shows
`ReportGenerator` working perfectly with `auditLogger` left `null`, because no
`AuditLogger` bean was ever registered — the setter was simply never called, no error, no
warning.

**Use setter injection specifically when a dependency is optional** — a required one
belongs in the constructor, where its absence fails immediately and loudly instead of as a
`NullPointerException` deep inside a method, days later.

---

## 4. More than one constructor: `@Autowired` picks

```java
public class GreetingService {
    @Autowired
    public GreetingService(Notifier notifier) { ... }        // Spring calls THIS one

    public GreetingService(Notifier notifier, String prefix) { ... }  // for manual/test use only
}
```

With exactly one constructor (every other class in this lesson), Spring uses it
automatically — no annotation required. The moment a class has more than one, Spring has
no way to guess which one you meant, and `@Autowired` becomes **mandatory** on exactly one
of them. Leaving it off every constructor, or putting it on more than one, is a startup
error.

---

## 5. Circular dependencies: constructor injection fails fast

`BrokenServiceA` needs a `BrokenServiceB`; `BrokenServiceB` needs a `BrokenServiceA`.
Registering both in a context reproduces the real error, unedited:

```
UnsatisfiedDependencyException: Error creating bean with name 'brokenServiceA':
  ... Error creating bean with name 'brokenServiceB':
    ... Requested bean is currently in creation: Is there an unresolvable circular reference?
```

The cause, read from the inside out: Spring starts building `brokenServiceA`, which needs
`brokenServiceB` first. It starts building `brokenServiceB`, which needs `brokenServiceA`
— but `brokenServiceA` is **already in progress** (its constructor hasn't returned yet, so
there's no finished instance to hand over). Spring detects this and refuses to guess;
**startup fails immediately**, with a stack trace that names both beans. Constructor
injection cannot resolve a cycle, structurally — there is no instance of either class in
existence until *both* constructors have already run.

---

## 6. Breaking the cycle with `@Lazy`

`FixedServiceA` and `FixedServiceB` have the exact same cyclical shape, and start up fine:

```java
public FixedServiceA(@Lazy FixedServiceB serviceB) {   // <- @Lazy is the only change
    this.serviceB = serviceB;
}
```

`@Lazy` on a constructor parameter tells Spring to inject a **proxy** instead of resolving
the real bean immediately. `FixedServiceA`'s constructor finishes right away, holding a
proxy standing in for `FixedServiceB`. Spring can then build the real `FixedServiceB` —
which needs `FixedServiceA`, and by now `FixedServiceA` genuinely exists — without ever
needing an unfinished object. The proxy only resolves to the real `FixedServiceB` the
first time a method is actually called on it, which the demo's log lines show happening in
exactly this order:

```
FixedServiceA constructed (serviceB is a lazy proxy so far, not the real bean yet)
FixedServiceB constructed (holds the REAL, already-built FixedServiceA)
```

**`@Lazy` is a fix for a symptom, not a design goal.** A genuine two-way dependency
between classes is usually a sign the two classes should be merged, or that a third class
should own the interaction between them. Reach for `@Lazy` to unblock a cycle you can't
immediately redesign away — not as a first choice.

---

## 7. Summary

- **Constructor injection**: `final` fields, fails fast on a missing dependency (a compile
  error, not a runtime surprise), testable with plain `new` — the default choice.
- **Field injection**: cannot be `final`, cannot be tested without Spring or reflection —
  generally discouraged despite looking the shortest.
- **Setter injection**: the right tool specifically for an **optional** dependency, via
  `@Autowired(required = false)`.
- More than one constructor requires `@Autowired` on exactly one of them, or startup
  fails.
- A **circular dependency** between two constructor-injected beans is a startup error
  (`BeanCurrentlyInCreationException`), because neither object can finish constructing
  first.
- **`@Lazy`** on one side of the cycle injects a proxy instead of the real bean, deferring
  resolution until first use — a genuine fix, but a sign to reconsider the design first.

---

**Previous:** [07 — Stereotype annotations and component scanning](../07-stereotype-annotations-and-component-scanning/07-stereotype-annotations-and-component-scanning.md) ·
**Next:** [09 — `@Configuration` classes and `@Bean` methods](../09-configuration-classes-and-bean-methods/09-configuration-classes-and-bean-methods.md)
