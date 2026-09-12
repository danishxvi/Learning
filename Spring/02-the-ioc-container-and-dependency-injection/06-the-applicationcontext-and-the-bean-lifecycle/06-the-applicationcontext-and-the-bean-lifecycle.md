# 06 · The `ApplicationContext` and the bean lifecycle

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/06-the-applicationcontext-and-the-bean-lifecycle spring-boot:run
> ```

Section 01 used the container without asking what happens, in order, between "Spring
decides to create this bean" and "Spring hands it to you." This lesson answers that with
one bean that implements every lifecycle hook available, so the order isn't a diagram you
have to trust — it's console output you just watched.

---

## 1. `BeanFactory` vs `ApplicationContext`

`BeanFactory` is the root interface: the minimal container contract — register bean
definitions, resolve dependencies, hand out instances. `ApplicationContext` extends it
with everything an application actually needs: automatic `BeanPostProcessor` registration,
event publishing, environment/property access (lesson 14), and internationalization
support. **You almost never use `BeanFactory` directly.** Every example in this stack —
`AnnotationConfigApplicationContext` in lesson 02, the context `SpringApplication.run`
returns from lesson 03 onward — is an `ApplicationContext`. The distinction matters mostly
for reading Spring's own source and documentation, where `BeanFactory` is the term for
"the underlying machinery."

---

## 2. The full sequence, in the order it actually runs

Running this lesson prints exactly this order:

```
1. Constructor ran
2. BeanNameAware.setBeanName()
   [BeanPostProcessor] BEFORE initialization
3. @PostConstruct
4. InitializingBean.afterPropertiesSet()
   [BeanPostProcessor] AFTER initialization
   ... bean is now in service ...
5. @PreDestroy
6. DisposableBean.destroy()
```

| # | Step | When it runs |
| --- | --- | --- |
| 1 | Constructor | Dependencies are already resolved and passed in — this is ordinary Java constructor injection, nothing lifecycle-specific about it. |
| 2 | `*Aware` interfaces (`BeanNameAware` here) | The container tells the bean things about itself or the container — its registered name, or (via `ApplicationContextAware`) a handle back to the context itself. |
| — | `BeanPostProcessor.postProcessBeforeInitialization` | Runs for **every** bean in the context, right before its own init callbacks. |
| 3 | `@PostConstruct` | The **recommended** way to run setup logic. A standard `jakarta.annotation`, not Spring-specific. |
| 4 | `InitializingBean.afterPropertiesSet()` | The older, interface-based equivalent of #3. Runs immediately after it. |
| — | `BeanPostProcessor.postProcessAfterInitialization` | Runs after init callbacks — the bean returned from here is what callers actually get from `getBean()`. |
| 5 | `@PreDestroy` | The **recommended** way to release resources. Runs on `context.close()`. |
| 6 | `DisposableBean.destroy()` | The older, interface-based equivalent of #5. |

**A real bean should pick one initialization mechanism and one destruction mechanism —
never both.** `FullLifecycleBean` implements every one of them purely to make the ordering
visible; in production code, prefer `@PostConstruct`/`@PreDestroy` because they need no
Spring-specific interface on your class (compare this to lesson 01's whole point about not
coupling a class to something it doesn't need).

---

## 3. `BeanPostProcessor` — the mechanism most of Spring is built on

```java
@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        ...
        return bean;   // must return the bean, or a REPLACEMENT
    }
}
```

A `BeanPostProcessor` runs around **every single bean's** initialization, not just one.
This is not a minor utility — it is the extension point that lets Spring wrap a bean in a
dynamic proxy without you writing any proxy code yourself. `@Transactional` (lesson 33),
`@Async` (lesson 53), and Spring AOP itself (lesson 38) are all implemented as
`BeanPostProcessor`s that return a **proxy** instead of the original bean from
`postProcessAfterInitialization`. Every time you've wondered "how does one annotation add
all this behavior with zero code," a `BeanPostProcessor` is almost always the answer.

---

## 4. Aware interfaces

`BeanNameAware` is one of a family of `*Aware` interfaces a bean can implement to have the
container inject container-level information instead of a regular dependency:

| Interface | Gives the bean |
| --- | --- |
| `BeanNameAware` | Its own registered bean name |
| `ApplicationContextAware` | A reference to the whole `ApplicationContext` |
| `EnvironmentAware` | The `Environment` (lesson 14) without needing it as a constructor parameter |

These exist for framework-level code that genuinely needs container internals. Application
code almost never should — needing `ApplicationContextAware` in a business class is usually
a sign that a normal `@Autowired` dependency would do the same job more simply.

---

## 5. Startup and shutdown, end to end

`SpringApplication.run` doesn't return until every bean in the context has finished this
entire sequence — that's why the startup log line ("Started LifecycleApplication in ...")
appears only *after* all six numbered steps in the demo output, not before. Symmetrically,
`context.close()` blocks until every bean's destruction callbacks have run. A Spring Boot
web application never calls `close()` itself in normal operation — it registers a JVM
shutdown hook that calls it when the process receives a termination signal (`Ctrl+C`, or
`SIGTERM` in a container), which is exactly how graceful shutdown works in production
(lesson 69).

---

## 6. Summary

- **`ApplicationContext`** extends the minimal `BeanFactory` with event publishing,
  environment access, and automatic `BeanPostProcessor` registration — use it, not
  `BeanFactory`, directly.
- The real initialization order is: constructor → `*Aware` callbacks →
  `BeanPostProcessor` (before) → `@PostConstruct` → `InitializingBean.afterPropertiesSet`
  → `BeanPostProcessor` (after) → bean is in service.
- Destruction order on `context.close()`: `@PreDestroy` → `DisposableBean.destroy()`.
- **Prefer `@PostConstruct`/`@PreDestroy`** over `InitializingBean`/`DisposableBean` — same
  effect, no Spring-specific interface needed on your class.
- **`BeanPostProcessor`** wraps or replaces every bean in the context around its own init
  callbacks — it's the mechanism `@Transactional`, `@Async` and Spring AOP are built on.
- `*Aware` interfaces hand a bean container-level information (its name, the context
  itself) — reach for a normal `@Autowired` dependency first; these are for framework code.

---

**Previous:** [05 — Configuration with properties and YAML](../../01-introduction-and-setup/05-configuration-with-properties-and-yaml/05-configuration-with-properties-and-yaml.md) ·
**Next:** [07 — Stereotype annotations and component scanning](../07-stereotype-annotations-and-component-scanning/07-stereotype-annotations-and-component-scanning.md)
