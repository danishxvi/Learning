# 12 · Conditional beans and profiles

> **Run the code for this lesson** (default profile)
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/12-conditional-beans-and-profiles spring-boot:run
> ```
> With "dev" active: `-Dspring-boot.run.profiles=dev` · with "prod" active: `-Dspring-boot.run.profiles=prod` ·
> with the feature flag on: `-Dspring-boot.run.arguments=--feature.new-ui=true`

Lesson 05 used profiles to override a *property's value*. This lesson uses them (and
their more general cousin, `@Conditional`) to switch a **whole bean** on or off — the
exact mechanism behind roughly all 52 of lesson 04's auto-configured beans.

---

## 1. `@Profile` — a bean that exists only for certain profiles

```java
@Profile("dev")  @Component  public class DevGreetingService  implements GreetingService { ... }
@Profile("prod") @Component  public class ProdGreetingService implements GreetingService { ... }
```

Running this lesson three ways, with the exact same `GreetingService` lookup code:

| Active profile | Result |
| --- | --- |
| *(none)* | `No GreetingService bean exists - neither "dev" nor "prod" is active.` |
| `dev` | `DevGreetingService: Hey! (verbose dev greeting...)` |
| `prod` | `ProdGreetingService: Welcome.` |

**A non-matching `@Profile` bean is never constructed at all** — not built and hidden,
genuinely absent from the container. That's why the default-profile run above needed an
`ObjectProvider<GreetingService>` (lesson 10) rather than a direct constructor injection —
a plain `GreetingService` parameter would make the whole application fail to start
whenever neither profile is active, the same unsatisfied-dependency failure lesson 08
produced for a missing bean.

---

## 2. `@Profile("!prod")` — negation

```java
@Profile("!prod")
@Component
public class DebugToolbar { ... }
```

| Active profile | `DebugToolbar` present? |
| --- | --- |
| *(none)* | Yes — "default" is not "prod" |
| `dev` | Yes |
| `prod` | **No** |

`!prod` reads as "active whenever prod is NOT among the active profiles" — which includes
`dev`, any other profile, and no profile at all. Only an explicit `prod` activation turns
it off. This is the standard shape for "something safe everywhere except production" —
verbose logging, debug endpoints, permissive CORS — without needing to separately list
every non-production profile that might ever exist.

---

## 3. `@ConditionalOnMissingBean` — Spring Boot's own override pattern

```java
@Configuration
public class DefaultNotifierConfig {
    @Bean
    @ConditionalOnMissingBean(Notifier.class)
    public Notifier notifier() { return () -> "DefaultNotifier ..."; }
}
```

```java
@Configuration
@Profile("dev")
public class CustomNotifierConfig {
    @Bean
    public Notifier notifier() { return () -> "CustomNotifier ..."; }
}
```

| Active profile | Which `Notifier` wins |
| --- | --- |
| *(none)* | `DefaultNotifier` — nothing else registered one |
| `dev` | `CustomNotifier` — and `DefaultNotifierConfig`'s bean is **skipped entirely**, because one already exists by the time it's evaluated |

This is precisely how lesson 04's 52 auto-configured beans coexist peacefully with your
own code: Spring Boot registers a sensible default for almost everything
(`ObjectMapper`, a `DataSource`, a `RestTemplate` builder), each guarded by
`@ConditionalOnMissingBean` — and the moment your own code defines a bean of that type,
Boot's default silently steps aside. No flag to flip, no bean to explicitly exclude —
supplying a competing bean is the entire override mechanism.

---

## 4. `@ConditionalOnProperty` — a bean gated by configuration

```java
@Bean
@ConditionalOnProperty(prefix = "feature", name = "new-ui", havingValue = "true")
public NewUiFeature newUiFeature() { return new NewUiFeature(); }
```

With `feature.new-ui: false` (the default in `application.yml`), this bean does not
exist. Overriding it from the command line —
`-Dspring-boot.run.arguments=--feature.new-ui=true` — and it appears:

```
New UI feature flag is ON - this bean only exists because of that.
```

This is the standard shape of a **feature flag** implemented natively in Spring, needing
no third-party feature-flag library for the simple case: flip one configuration value
(lesson 05's whole precedence chain still applies — an environment variable or command-line
argument works exactly the same as editing the YAML file) and an entire bean, with
everything it wires together, switches on or off.

---

## 5. `@Conditional` — the mechanism underneath both of the above

```java
public class OnWindowsCondition implements Condition {
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
```

```java
@Bean
@Conditional(OnWindowsCondition.class)
public String windowsPathTip() { ... }
```

Both `@Profile` and `@ConditionalOnProperty` are themselves meta-annotated with
`@Conditional`, pointing at their own `Condition` implementations —
`OnWindowsCondition` uses exactly the same interface, spelled out directly. A `Condition`
can inspect anything reachable at startup: system properties, environment variables, the
classpath (this is literally how Boot's auto-configuration decides "is
`spring-boot-starter-web` present? then configure an embedded Tomcat"), or the beans
already registered so far. Running this lesson on Windows genuinely activates
`windowsPathTip` — `os.name` really does contain `"win"` — which is why the output printed
the tip rather than "does NOT exist."

---

## 6. Summary

- **`@Profile("name")`**: a bean exists only when that profile is active — genuinely never
  constructed otherwise, which can turn a plain constructor dependency into a startup
  failure if nothing satisfies it.
- **`@Profile("!name")`**: negation — active for every profile *except* the named one,
  including no active profile at all.
- **`@ConditionalOnMissingBean(Type.class)`**: registers a fallback only if nothing else
  already provided a bean of that type — the exact mechanism behind Spring Boot's own
  auto-configuration defaults, and the reason supplying your own bean is enough to
  override one.
- **`@ConditionalOnProperty`**: gates a bean's existence on a configuration value — a
  native feature-flag mechanism, reusing lesson 05's entire property-source precedence
  chain.
- **`@Conditional(CustomCondition.class)`**: the general mechanism both of the above are
  built from — implement `Condition.matches()` to gate a bean on anything inspectable at
  startup.

---

**Previous:** [11 — Qualifiers and resolving ambiguous beans](../11-qualifiers-and-resolving-ambiguous-beans/11-qualifiers-and-resolving-ambiguous-beans.md) ·
**Next:** [13 — `@Value` injection and externalized configuration](../13-value-injection-and-externalized-configuration/13-value-injection-and-externalized-configuration.md)
