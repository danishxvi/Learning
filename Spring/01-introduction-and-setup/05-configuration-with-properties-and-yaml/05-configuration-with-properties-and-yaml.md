# 05 · Configuration with `application.properties` and YAML

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/01-introduction-and-setup/05-configuration-with-properties-and-yaml spring-boot:run
> ```
> With the "dev" profile active:
> ```bash
> mvn -f Spring/01-introduction-and-setup/05-configuration-with-properties-and-yaml spring-boot:run -Dspring-boot.run.profiles=dev
> ```
> Overriding one value from the command line:
> ```bash
> mvn -f Spring/01-introduction-and-setup/05-configuration-with-properties-and-yaml spring-boot:run -Dspring-boot.run.arguments=--app.notification.retry-count=99
> ```

Every lesson so far has had an `application.properties` sitting in `src/main/resources`
doing almost nothing. This lesson uses it for real, switches to YAML, and shows how
configuration actually reaches your code.

---

## 1. Properties vs YAML

Both formats configure Spring Boot identically — pick one per project, don't mix them.
This project uses YAML (`application.yml`); everything before it used
`.properties`. They're equivalent:

```properties
app.notification.default-sender=no-reply@example.com
app.notification.retry-count=3
app.notification.allowed-channels[0]=email
app.notification.allowed-channels[1]=sms
```

```yaml
app:
  notification:
    default-sender: no-reply@example.com
    retry-count: 3
    allowed-channels:
      - email
      - sms
```

YAML wins for anything nested or list-shaped — no `[0]`/`[1]` index noise, and the
indentation mirrors the structure you're actually building. Properties files win when you
want `grep`-ability and zero ambiguity about whitespace. Both compile down to the same
flat key space internally (`app.notification.default-sender` either way) — that's how
lesson 05's code can read a YAML file with the exact same `Environment.getProperty`
API lesson 04 used on a `.properties` file.

---

## 2. Two ways to read a value: `@Value` vs `@ConfigurationProperties`

**`@Value`** pulls exactly one key, using Spring Expression Language:

```java
@Value("${app.notification.timeout-seconds:30}")
private int timeoutSeconds;
```

The `:30` after the colon is a **default** — used only if the key is missing entirely.
`app.notification.timeout-seconds` doesn't exist in either YAML file in this lesson, so
this always resolves to `30`. `@Value` is fine for one or two settings, but it does not
scale: ten related settings means ten separate `@Value` fields, scattered wherever they're
needed, with no single place that documents "these ten keys form one configuration
block."

**`@ConfigurationProperties`** binds an entire subtree onto one typed object at once:

```java
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
        String defaultSender,
        int retryCount,
        List<String> allowedChannels,
        RateLimit rateLimit
) {
    public record RateLimit(int maxPerMinute, int burst) {}
}
```

Every key under `app.notification.*` in the YAML becomes one field, including the nested
`rate-limit` block, bound recursively into the nested `RateLimit` record. This is a
**record**, and since Spring Boot 3 records bind via **constructor binding**
automatically — Spring calls the canonical constructor with values read from
configuration, matched by parameter name. No setters exist to call.

**`@ConfigurationPropertiesScan`** (on the application class) tells Spring Boot to find
every `@ConfigurationProperties`-annotated type in the package and register it as a bean —
the `@ConfigurationProperties` equivalent of `@ComponentScan`. Once registered, it's an
ordinary bean, injectable anywhere by constructor — this lesson injects the same
`NotificationProperties` instance into two unrelated beans to show that.

**Rule of thumb**: one or two flags → `@Value`. A related group of settings → a
`@ConfigurationProperties` type. Almost everything past a handful of keys belongs in the
second category.

---

## 3. Relaxed binding: why `default-sender` becomes `defaultSender`

The YAML key is `default-sender`; the record component is `defaultSender`. Spring's
**relaxed binding** treats these as the same key — kebab-case, camelCase, and
`UPPER_SNAKE_CASE` (for environment variables) are all considered equivalent forms of the
same property name. This exists specifically so an environment variable
(`APP_NOTIFICATION_DEFAULT_SENDER`, shell-safe, no dots or hyphens) can satisfy the exact
same binding as `app.notification.default-sender` in YAML. **Convention: always write
YAML/properties keys in kebab-case** — it's the form every other source can map onto
without ambiguity.

---

## 4. Profiles: environment-specific overrides

`application-dev.yml` in this lesson only loads when the `dev` profile is active:

```yaml
app:
  notification:
    default-sender: dev-no-reply@example.com
    retry-count: 0
```

Running with `-Dspring-boot.run.profiles=dev` merges this file on top of
`application.yml`: `default-sender` and `retry-count` change, but `allowed-channels` and
`rate-limit` — not repeated in the dev file — keep their base values. This is how the same
codebase runs with different database URLs, log levels, or (as here) notification
settings in dev, staging and production, without an `if (environment == "dev")` anywhere
in application code. Profiles come back in force in lesson 12 (conditional beans) — an
entire `@Bean` can be switched on or off by active profile, not just a property value.

---

## 5. Precedence: who wins when the same key is set twice

Spring Boot reads configuration from many sources and applies them in a strict priority
order — roughly, from highest to lowest:

1. Command-line arguments (`--app.notification.retry-count=99`)
2. JVM system properties (`-Dapp.notification.retry-count=99`)
3. OS environment variables
4. Profile-specific files (`application-dev.yml`)
5. The base `application.yml`

Running this lesson three ways proves it, using the exact same
`environment.getProperty("app.notification.retry-count")` call each time:

| Command | Result |
| --- | --- |
| Plain `spring-boot:run` | `3` — from `application.yml` |
| `-Dspring-boot.run.profiles=dev` | `0` — `application-dev.yml` overrides the base file |
| `-Dspring-boot.run.arguments=--app.notification.retry-count=99` | `99` — a command-line argument outranks every file |

`Environment.getProperty` always returns the **winning** value — your code never needs to
know, or care, which source it actually came from.

---

## 6. Summary

- `.properties` and YAML are interchangeable; use YAML for anything nested or list-shaped.
- **`@Value("${key:default}")`** reads one property, with an optional fallback.
- **`@ConfigurationProperties`** binds a whole subtree onto one typed object — prefer it
  for anything beyond a couple of related settings.
- A `record` annotated `@ConfigurationProperties` binds via **constructor binding**
  automatically in Spring Boot 3 — no setters needed.
- **`@ConfigurationPropertiesScan`** registers `@ConfigurationProperties` types as beans,
  the way `@ComponentScan` registers `@Component` types.
- **Relaxed binding** treats `default-sender`, `defaultSender` and `DEFAULT_SENDER` as the
  same key — always write config files in kebab-case.
- **`application-<profile>.yml`** overrides only the keys it repeats; everything else
  falls back to the base file.
- **Precedence, highest to lowest**: command-line args → JVM system properties →
  environment variables → profile-specific file → base `application.yml`.

---

**Previous:** [04 — Anatomy of a Spring Boot project](../04-anatomy-of-a-spring-boot-project/04-anatomy-of-a-spring-boot-project.md) ·
**Next:** [06 — The `ApplicationContext` and the bean lifecycle](../../02-the-ioc-container-and-dependency-injection/06-the-applicationcontext-and-the-bean-lifecycle/06-the-applicationcontext-and-the-bean-lifecycle.md)
