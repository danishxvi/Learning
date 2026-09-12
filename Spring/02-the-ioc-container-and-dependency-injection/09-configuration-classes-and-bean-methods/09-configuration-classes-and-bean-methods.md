# 09 · `@Configuration` classes and `@Bean` methods

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/09-configuration-classes-and-bean-methods spring-boot:run
> ```

`@Component` requires editing the class itself. This lesson covers the tool for
everything `@Component` can't reach: classes you didn't write, plus a CGLIB detail in
`@Configuration` that changes what a plain Java method call actually does.

---

## 1. `@Bean`: registering a class you can't annotate

`Engine` and `Car` in this lesson carry no Spring annotations at all — standing in for a
class from a library you depend on but don't own:

```java
@Configuration
public class ThirdPartyConfig {
    @Bean
    public Engine engine() {
        return new Engine();
    }

    @Bean
    public Car car(Engine engine) {      // Spring injects "engine" as a plain method parameter
        return new Car(engine);
    }
}
```

The method **name becomes the bean name** (`engine`, `car`) unless overridden with
`@Bean(name = "...")`. A `@Bean` method's parameters are resolved exactly like a
`@Component`'s constructor parameters — Spring matches `Engine engine` to the `engine`
bean by type, the same mechanism from lesson 08.

---

## 2. Bridging a third-party lifecycle with `initMethod`/`destroyMethod`

```java
@Bean(initMethod = "connect", destroyMethod = "shutdown")
public ThirdPartyConnectionPool connectionPool() {
    return new ThirdPartyConnectionPool();
}
```

`ThirdPartyConnectionPool` has its own `connect()`/`shutdown()` methods — not
`@PostConstruct`/`@PreDestroy` (lesson 06), because you don't own the class to add them.
`initMethod`/`destroyMethod` tell Spring to call these specific methods at exactly the
same points in the lifecycle it would otherwise call the annotated ones. Running this
lesson shows `connect()` firing during startup and `shutdown()` firing on `context.close()`
— the same lifecycle lesson 06 mapped out, hooked onto methods that were never written
with Spring in mind.

---

## 3. "Full" mode: what `@Configuration` actually does to your class

```java
@Configuration                 // proxyBeanMethods defaults to true
public class FullProxyConfig {
    @Bean
    public Engine engine() { return new Engine(); }

    @Bean
    public Car car() {
        return new Car(engine());   // a DIRECT JAVA METHOD CALL, not injection
    }
}
```

`car()` calls `engine()` directly — ordinary Java, no parameter, no `@Autowired`. Running
this lesson proves something that looks impossible for ordinary Java:

```
[FullProxyConfig] engine() method body actually executing     <- printed ONCE
engine() ran ONCE despite two references to it (context bean + car's copy).
contextEngine == carsEngine ? true
```

The reason: **`@Configuration` classes are not used directly.** At startup, Spring
generates a **CGLIB subclass** of `FullProxyConfig` and registers *that* as the real
`@Configuration` bean. Every `@Bean` method call — including internal calls like
`engine()` inside `car()` — is intercepted by the subclass, which checks "has this bean
already been created in this context?" before ever running your method's actual body.
The second (and every later) call returns the cached singleton instead of re-running the
method. This is called **"full" mode**, and it's the default the moment you write
`@Configuration` with no arguments — it exists specifically to make calling one `@Bean`
method from another behave the way you'd naturally expect: like referencing another bean,
not like duplicating its construction.

---

## 4. "Lite" mode: what happens without the proxy

```java
@Configuration(proxyBeanMethods = false)   // <- the only difference from FullProxyConfig
public class LiteConfig {
    @Bean
    public Engine engine() { return new Engine(); }

    @Bean
    public Car car() {
        return new Car(engine());
    }
}
```

Same code, one attribute changed, and the outcome flips completely:

```
[LiteConfig] engine() method body actually executing     <- printed TWICE
engine() ran TWICE - once for the context bean, once inside car().
contextEngine == carsEngine ? false  <-- car's Engine is NOT the one registered in the context!
```

With no CGLIB subclass generated, `engine()` inside `car()` is an **ordinary Java method
call** — it runs its body again, in full, producing a second `Engine` that is never
registered as a bean anywhere. `car`'s engine and the context's `engine` bean are now two
different objects, silently. This is a real, easy-to-write bug: it compiles cleanly, and
nothing fails at startup — it only surfaces as two objects that should be "the same
engine" behaving independently at runtime.

---

## 5. When to use which

**Default to full mode (just `@Configuration`, no arguments) whenever `@Bean` methods call
each other** — it's the only way inter-method calls behave correctly, and it's already the
default, so this requires doing nothing.

**Use `proxyBeanMethods = false` when a `@Configuration` class's `@Bean` methods are
independent** — no method calls another. Lite mode needs no CGLIB subclass (a small
startup-time saving, and one less generated class in the JVM), and avoids CGLIB's own
requirements (the class can't be `final`, and needs a usable constructor for the subclass
to extend). Most auto-configuration classes inside Spring Boot itself use lite mode for
exactly this reason. If in doubt, or if the methods *do* call each other, leave the
default full mode in place — the cost of getting this wrong (silently duplicated beans, as
seen above) is much higher than the small saving lite mode offers.

---

## 6. Summary

- `@Bean` methods register a class you cannot (or should not) annotate directly — a
  third-party type, or one needing constructor arguments computed by code.
- `@Bean(initMethod = ..., destroyMethod = ...)` maps a class's own lifecycle method names
  onto Spring's lifecycle, without touching that class.
- **Full mode** (`@Configuration`, the default) generates a CGLIB subclass that intercepts
  calls between `@Bean` methods, so calling one from another returns the cached singleton
  instead of re-running its body.
- **Lite mode** (`proxyBeanMethods = false`) skips the proxy — inter-method calls become
  ordinary Java calls, silently producing a second, unregistered instance if any method
  calls another.
- Keep full mode whenever `@Bean` methods reference each other; switch to lite mode only
  when they're independent.

---

**Previous:** [08 — Constructor vs field vs setter injection](../08-constructor-vs-field-vs-setter-injection/08-constructor-vs-field-vs-setter-injection.md) ·
**Next:** [10 — Bean scopes](../10-bean-scopes/10-bean-scopes.md)
