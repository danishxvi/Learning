# 13 · `@Value` injection and externalized configuration, deeper

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/13-value-injection-and-externalized-configuration spring-boot:run
> ```

Lesson 05 covered `@Value("${key:default}")` — one property, resolved from
`application.yml`. This lesson covers everything `@Value` can do beyond that: loading a
second file explicitly, and Spring Expression Language (SpEL), which turns `@Value` from
"read a string" into a small, genuinely useful expression evaluator.

---

## 1. `@PropertySource` — loading a file Boot wouldn't find on its own

```java
@PropertySource("classpath:extra.properties")
@Component
public class AppInfo {
    public AppInfo(@Value("${app.version}") String version, ...) { ... }
}
```

Spring Boot automatically loads `application.yml`/`.properties` and their
profile-specific variants (lesson 05) — nothing else. `extra.properties` in this lesson
is a second file, deliberately given a different name, that would otherwise never be
read. `@PropertySource` on a class pulls it in explicitly, and everything inside it
becomes resolvable through the exact same `${...}` syntax as any other property. This is
the standard way to load configuration shipped inside a library JAR, or a file
intentionally kept separate from the main application config.

Notice `AppInfo` also uses **constructor-based `@Value`** — `@Value` isn't limited to
fields; putting it on constructor parameters gets the same testability and `final`-field
benefits lesson 08 argued for constructor injection generally.

---

## 2. SpEL: `#{...}` vs `${...}`

Every `@Value` so far has used `${...}` — a **property placeholder**: look up this exact
key, resolved through lesson 05's whole precedence chain. `#{...}` is **Spring Expression
Language (SpEL)** — a small expression language that can do far more than look up a
string:

```java
@Value("${app.allowed-currencies}")              // property placeholder: "USD,EUR,INR,GBP"
private String allowedCurrenciesRaw;

@Value("#{'${app.allowed-currencies}'.split(',')}")   // SpEL: resolve the placeholder FIRST, then call .split()
private List<String> allowedCurrencies;
```

Running this lesson: `allowedCurrenciesRaw` is the raw string; `allowedCurrencies` is an
actual `List<String>` with four elements — the placeholder resolved to text first, and
then SpEL ran `.split(",")` on that text, entirely inside the annotation. Nesting
`${...}` inside `#{...}` like this is the standard way to turn a plain configuration
string into a richer Java type without writing a converter class.

---

## 3. What SpEL can do that a plain lookup can't

```java
@Value("#{60 * 60 * 24}")                       secondsPerDay = 86400
@Value("#{T(java.lang.Math).PI}")               pi = 3.141592653589793
@Value("#{systemProperties['os.name']}")        operatingSystem = "Windows 11"     (on this machine)
@Value("#{appInfo.version}")                    versionCopiedFromAppInfo = "3.2.1"
```

| Expression | What it does |
| --- | --- |
| `#{60 * 60 * 24}` | Ordinary arithmetic, evaluated once at injection time. |
| `#{T(java.lang.Math).PI}` | `T()` is SpEL's **Type** operator — reaches a static field or calls a static method on any class, by fully-qualified name. |
| `#{systemProperties['os.name']}` | Reads a JVM system property (or `#{systemEnvironment['PATH']}` for an OS environment variable) directly — separate from `application.yml` entirely. |
| `#{appInfo.version}` | References **another bean by name** (`appInfo` — `AppInfo`'s default bean name) and calls its `getVersion()` getter. |

That last one is the most powerful: one bean's configuration can be *derived from
another bean's state*, declaratively, with no code connecting them beyond the
expression itself. `versionCopiedFromAppInfo` above genuinely equals `appInfo.getVersion()`
— proven by running the lesson and seeing both print `3.2.1`.

---

## 4. When to reach for SpEL, and when not to

SpEL is a real embedded language — it supports conditionals (`?:`), collection
projection/selection (`?[...]`, `![...]`), and safe navigation (`?.`), well beyond what
this lesson demonstrates. **Use it for the cases above**: deriving one property from
another, doing trivial arithmetic on a config value, or reaching a static constant.
**Don't use it for business logic** — an expression buried inside an annotation string is
much harder to read, test, and debug than the equivalent Java in a method body. If an
expression starts needing more than one operation, it usually belongs in a
`@ConfigurationProperties` class (lesson 05) with ordinary Java doing the derivation
instead.

---

## 5. Summary

- **`@PropertySource("classpath:file.properties")`** loads a file Spring Boot wouldn't
  read automatically, making its keys resolvable through the normal `${...}` syntax.
- **`@Value`** works on constructor parameters, not just fields — preferred for the same
  reasons constructor injection is preferred generally (lesson 08).
- **`#{...}`** is SpEL — a real expression language, distinct from `${...}`'s plain
  placeholder lookup. Nest a `${...}` inside `#{...}` to post-process a resolved value
  (e.g., splitting a CSV string into a `List`).
- SpEL can do arithmetic, call static members via `T(FullyQualifiedClassName)`, read
  system properties/environment variables directly, and reference **another bean's
  property by name** — `#{beanName.property}`.
- Keep SpEL expressions small — anything beyond a single operation reads better as
  ordinary Java in a `@ConfigurationProperties` class.

---

**Previous:** [12 — Conditional beans and profiles](../12-conditional-beans-and-profiles/12-conditional-beans-and-profiles.md) ·
**Next:** [14 — The `Environment` abstraction](../14-the-environment-abstraction/14-the-environment-abstraction.md)
