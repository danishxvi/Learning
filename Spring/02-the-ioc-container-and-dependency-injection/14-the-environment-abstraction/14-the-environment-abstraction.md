# 14 · The `Environment` abstraction

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/02-the-ioc-container-and-dependency-injection/14-the-environment-abstraction spring-boot:run
> ```
> With "dev" active: add `-Dspring-boot.run.profiles=dev`

`Environment` has appeared since lesson 04, always through the one method,
`getProperty(key)`. This lesson is everything else it can do — including reaching into
its internals and adding configuration to it at runtime, which is exactly what
`@ConfigurationProperties`, `@Value`, and profile matching are all built on top of.

---

## 1. `getProperty` has more than one shape

```java
environment.getProperty("app.max-retries")                    // "5"  - a String, or null if absent
environment.getProperty("app.max-retries", Integer.class)     // 5    - converted to the requested type
environment.getProperty("app.missing-key", "fallback")        // "fallback" - a default if absent
environment.getRequiredProperty("app.missing-key")            // throws IllegalStateException if absent
```

`getRequiredProperty` is the one worth remembering: instead of silently returning `null`
for a missing key (which usually surfaces later as a confusing `NullPointerException`
somewhere else), it fails immediately, at the point where the property was actually
needed, with a message naming the exact missing key. Running this lesson reproduces that
exact exception on demand.

---

## 2. `containsProperty` — checking existence without triggering a default

```java
environment.containsProperty("app.max-retries")   // true
environment.containsProperty("app.missing-key")   // false
```

Useful specifically when the presence or absence of a key changes behaviour, rather than
its value — "is a database URL configured at all" versus "what is the database URL."

---

## 3. `acceptsProfiles` — checking profile activation from code

Lesson 12 used `@Profile` to switch a whole bean on or off declaratively. The same check
is available imperatively:

```java
environment.acceptsProfiles(Profiles.of("dev | test"))   // true if EITHER is active
environment.acceptsProfiles(Profiles.of("!prod"))         // true whenever prod is NOT active
```

Running this lesson with no profile active: the first returns `false`, the second `true`
— then running it again with `dev` active flips the first to `true`, matching exactly the
same profile-matching logic `@Profile` uses declaratively. `Profiles.of(...)` accepts the
same boolean expressions `@Profile` does — `&`, `|`, and `!` — because it's the same
underlying matcher both features share.

---

## 4. Property sources — what `getProperty` actually searches

`Environment` doesn't store properties itself — it's a facade over an ordered list of
**`PropertySource`s**, each representing one origin. Iterating them shows the real,
concrete layers behind lesson 05's abstract "precedence" table:

```
configurationProperties
systemProperties
systemEnvironment
random
Config resource 'class path resource [application.yml]' via location 'optional:classpath:/'
```

`getProperty` checks these **in order** and returns the first match — which is the actual
mechanism behind every precedence claim lesson 05 made. `systemProperties` (JVM
`-D` flags) sits ahead of `application.yml`'s config resource in this exact list, which is
*why* a `-D` flag overrides the YAML file, not because of a rule stated in documentation
somewhere — the rule is this list's literal order.

---

## 5. Adding a property source at runtime

```java
environment.getPropertySources()
        .addFirst(new MapPropertySource("runtime-override", Map.of("app.max-retries", "99")));
```

Before this call, `getProperty("app.max-retries")` returns `5`, from `application.yml`.
After it, the same call returns `99` — `addFirst` inserts the new source ahead of every
existing one, so it wins whenever it defines a key, and every key it doesn't define falls
through to the sources beneath it unchanged. This is precisely how tools like Spring
Cloud Config Client and testing utilities (`@TestPropertySource`, `@DynamicPropertySource`
in Spring Boot's test support) inject configuration that didn't come from any file at
all — they aren't special-cased by `Environment`; they're just another `PropertySource`,
added programmatically, exactly like this.

---

## 6. Summary

- **`getProperty`** has four shapes: plain (nullable), type-converting, with a default,
  and `getRequiredProperty` (throws if missing) — prefer the required form for
  configuration your application cannot run without.
- **`containsProperty`** checks existence, independent of value.
- **`acceptsProfiles(Profiles.of(...))`** runs the exact same profile-matching logic
  `@Profile` uses declaratively (lesson 12), callable from ordinary code.
- **`Environment` is a facade over an ordered list of `PropertySource`s** —
  `getPropertySources()` exposes that list directly, and its order *is* the precedence
  rule from lesson 05, not a separate policy layered on top of it.
- **`addFirst`/`addLast`** on that list inject configuration from anywhere — a test
  harness, a remote config server, generated values — without needing a file on disk at
  all.

---

**Previous:** [13 — `@Value` injection and externalized configuration](../13-value-injection-and-externalized-configuration/13-value-injection-and-externalized-configuration.md) ·
**Next:** [15 — Auto-configuration, explained](../../03-spring-boot-fundamentals/15-autoconfiguration-explained/15-autoconfiguration-explained.md)
