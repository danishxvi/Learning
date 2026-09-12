# 07 · Stereotype annotations and component scanning

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/07-stereotype-annotations-and-component-scanning spring-boot:run
> ```

Every lesson so far used `@Component`, `@Service` or `@Repository` without asking what
actually separates them. This lesson answers that, then uses `@ComponentScan`'s
`excludeFilters` to keep a real, `@Component`-annotated class out of the context on
purpose — something `@SpringBootApplication`'s implicit scan doesn't expose directly.

---

## 1. The four stereotypes, and what actually differs

```java
@Component class Anything { }
@Service    class OrderService { }
@Repository class OrderRepository { }
@Controller class HomeController { }
```

Run this lesson and it prints, straight from reflection:

```
@Repository is meta-annotated with @Component: true
@Service is meta-annotated with @Component:    true
@Controller is meta-annotated with @Component: true
```

**`@Service`, `@Repository`, and `@Controller` are `@Component`, meta-annotated onto more
specific names.** The component scanner does not special-case any of the four — it looks
for anything carrying `@Component`, directly or transitively through meta-annotation,
which is exactly why all three specialised ones are found the same way `@Component` is.

| Annotation | Extra behaviour beyond `@Component`? |
| --- | --- |
| `@Component` | None — the base case. |
| `@Service` | **None.** Purely documents intent: "this class holds business logic." |
| `@Controller` | None on its own — becomes meaningful once Spring MVC is on the classpath (section 04), where `DispatcherServlet` looks specifically for it to map HTTP requests. |
| `@Repository` | **One real difference**: Spring registers a `PersistenceExceptionTranslationPostProcessor` that wraps `@Repository` beans so raw JDBC/JPA exceptions get translated into Spring's own `DataAccessException` hierarchy. Covered fully in section 05, once there's a real database to throw exceptions from. |

**Use the specific one that matches the class's role.** There is no compiler benefit to
`@Service` over `@Component` for a service class — the benefit is entirely for the next
person (or tool) reading the codebase, who can tell a class's architectural layer from its
annotation alone.

---

## 2. Inventing your own stereotype

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component                       // <- THIS is what makes it a stereotype
public @interface BusinessLogic {
}
```

```java
@BusinessLogic
public class PricingEngine { ... }
```

`PricingEngine` carries no `@Component` and no `@Service` — only `@BusinessLogic` — and
component scanning still finds it, because `@BusinessLogic` is itself meta-annotated with
`@Component`. This is precisely how `@Service` and `@Repository` are built; nothing in
Spring's own source does anything more special than this file does. Codebases (and
internal platform teams) use this to invent domain-specific stereotypes — `@UseCase`,
`@DomainEvent`, `@Adapter` — searchable by annotation instead of by convention alone, and
you can find every bean carrying one programmatically:

```java
context.getBeansWithAnnotation(BusinessLogic.class)   // { "pricingEngine": PricingEngine@... }
```

---

## 3. Excluding a `@Component` from scanning

`OldPaymentGateway` **is** annotated `@Component` and lives in the same scanned package as
everything else — and still never becomes a bean:

```java
@Component
@Legacy                 // a plain marker annotation, meaningless to Spring on its own
public class OldPaymentGateway { ... }
```

```java
@ComponentScan(
        basePackages = "com.danish.spring.stereo",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Legacy.class)
)
```

`context.getBeanNamesForType(OldPaymentGateway.class)` returns an **empty array** —
proof the class exists on the classpath, is `@Component`-annotated, and was still filtered
out before the container ever tried to instantiate it. `FilterType.ANNOTATION` is one of
several filter strategies:

| `FilterType` | Matches |
| --- | --- |
| `ANNOTATION` | Classes carrying a given annotation (used above) |
| `ASSIGNABLE_TYPE` | Classes that are (or extend/implement) a given class |
| `REGEX` | Class names matching a regular expression |
| `ASPECTJ` | Class names matching an AspectJ type pattern |
| `CUSTOM` | Your own `TypeFilter` implementation, for anything the others can't express |

`includeFilters` works the same way, in reverse — useful for scanning classes that aren't
annotated at all (e.g., third-party classes you can't add annotations to).

---

## 4. Why this lesson wrote out `@SpringBootApplication` by hand

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "...", excludeFilters = ...)
public class StereotypeApplication {
```

Lesson 03 established that `@SpringBootApplication` is exactly these three annotations
combined. `excludeFilters` isn't an attribute `@SpringBootApplication` exposes — so getting
it means writing the three annotations out separately and adding it to the one that's
actually `@ComponentScan`. This is a completely ordinary thing to do in a real project the
moment you need a scanning option `@SpringBootApplication`'s shorthand doesn't cover.

---

## 5. What default scanning actually covers

Without `basePackages` (as in every earlier lesson), `@ComponentScan` defaults to the
package of the class it's declared on, plus every sub-package beneath it. A class sitting
in a **sibling** package — not nested under the application class's package — is invisible
to scanning entirely, filter or no filter. That's why every lesson in this stack keeps its
application class at the top of its own package tree: it's what makes "just add
`@Component`" work with zero extra configuration.

---

## 6. Summary

- `@Service`, `@Repository` and `@Controller` are `@Component`, meta-annotated onto more
  specific names — the scanner finds all four the same way.
- `@Service` and `@Controller` (without Spring MVC) add **no** behaviour beyond
  `@Component` — they exist to document a class's role.
- `@Repository` is the one exception: it also enables persistence exception translation
  (section 05).
- Meta-annotating your own `@interface` with `@Component` creates a custom stereotype,
  discoverable via `context.getBeansWithAnnotation(...)` — exactly how `@Service` itself
  is built.
- `@ComponentScan(excludeFilters = ...)` can keep a genuinely `@Component`-annotated class
  out of the context — `ANNOTATION`, `ASSIGNABLE_TYPE`, `REGEX`, `ASPECTJ` and `CUSTOM` are
  the five filter strategies.
- Default scanning only reaches the application class's own package and its
  sub-packages — nothing outside that tree is found without explicitly listing it.

---

**Previous:** [06 — The `ApplicationContext` and the bean lifecycle](../06-the-applicationcontext-and-the-bean-lifecycle/06-the-applicationcontext-and-the-bean-lifecycle.md) ·
**Next:** [08 — Constructor vs field vs setter injection](../08-constructor-vs-field-vs-setter-injection/08-constructor-vs-field-vs-setter-injection.md)
