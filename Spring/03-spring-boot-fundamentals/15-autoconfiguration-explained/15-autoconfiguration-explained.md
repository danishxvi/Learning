# 15 · Auto-configuration, explained

> **Run the code for this lesson** (our auto-configuration supplies the bean)
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/15-autoconfiguration-explained spring-boot:run
> ```
> With the application overriding it: `-Dspring-boot.run.profiles=custom` ·
> to see Boot's own reasoning: `-Dspring-boot.run.arguments=--debug`

Lesson 04 found 52 beans in a project with zero of its own. Lesson 12 explained
`@ConditionalOnMissingBean`. This lesson writes an actual auto-configuration class —
the same shape every one of those 52 beans, and every real starter library, uses — and
registers it the exact way Spring Boot discovers them.

---

## 1. `@AutoConfiguration` is not found by `@ComponentScan`

```java
@AutoConfiguration
@ConditionalOnClass(GreetingService.class)
public class GreetingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GreetingService.class)
    public GreetingService greetingService() {
        return () -> "Hello from GreetingAutoConfiguration's DEFAULT bean - nobody overrode it.";
    }
}
```

This class carries no `@Component`. Component scanning (lesson 07) never finds it, and
that's deliberate — scanning registers things unconditionally, which would defeat the
entire point of a bean that's supposed to back off when the application supplies its own.
`@AutoConfiguration` is a specialised `@Configuration` meant for a **different**
discovery path entirely.

---

## 2. The file that actually registers it

```
src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```
```
com.danish.spring.autoconfig.GreetingAutoConfiguration
```

This is the file `@EnableAutoConfiguration` — one third of `@SpringBootApplication`
(lesson 03) — actually reads at startup: one fully-qualified class name per line. Every
real starter (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, and so on) ships
this exact file, listing its own auto-configuration classes. Running this lesson proves
the mechanism works end to end: `GreetingAutoConfiguration` was never scanned, never
manually registered, and still supplied a working bean — found *only* because its name
appears in this file.

---

## 3. `@ConditionalOnClass` — gating on the classpath itself

```java
@ConditionalOnClass(GreetingService.class)
```

Before Boot even considers evaluating this configuration's `@Bean` methods, it checks
whether `GreetingService` is loadable at all. This is precisely how
`spring-boot-starter-web`'s auto-configuration knows to configure an embedded Tomcat only
when a servlet API is actually on the classpath, and stays completely inert in a plain
`spring-boot-starter` project like every earlier lesson in this stack. `@ConditionalOnClass`
is a `@Conditional` (lesson 12) checking one specific thing: can this class be loaded?

---

## 4. `@ConditionalOnMissingBean` — the override, reused exactly

```java
@Bean
@ConditionalOnMissingBean(GreetingService.class)
public GreetingService greetingService() { ... }
```

Running the lesson two ways:

| Command | Winning bean |
| --- | --- |
| Default | `Hello from GreetingAutoConfiguration's DEFAULT bean - nobody overrode it.` |
| `-Dspring-boot.run.profiles=custom` | `Hello from the APPLICATION'S OWN bean - GreetingAutoConfiguration backed off.` |

`UserGreetingConfig` (active only under the `custom` profile, purely so this lesson can
show both outcomes from one codebase) is an ordinary `@Configuration` with a `@Bean`
method — nothing about it needs to know `GreetingAutoConfiguration` exists.
`@ConditionalOnMissingBean` on the auto-configuration's side is what does all the work:
by the time it's evaluated, a `GreetingService` bean already exists, so the whole method
is skipped — not overridden, never even run. **This is exactly lesson 12's pattern**,
applied to a real auto-configuration instead of a lesson demo.

---

## 5. Seeing Boot's actual reasoning: `--debug`

```bash
mvn -f Spring/03-spring-boot-fundamentals/15-autoconfiguration-explained spring-boot:run -Dspring-boot.run.arguments=--debug
```

Running this prints a **CONDITIONS EVALUATION REPORT** — real Boot startup diagnostics,
naming this lesson's own class:

```
GreetingAutoConfiguration matched:
   - @ConditionalOnClass found required class 'com.danish.spring.autoconfig.GreetingService' (OnClassCondition)

GreetingAutoConfiguration#greetingService matched:
   - @ConditionalOnMissingBean (types: com.danish.spring.autoconfig.GreetingService; SearchStrategy: all) did not find any beans (OnBeanCondition)
```

Every one of lesson 04's 52 mystery beans has an entry exactly like this — a "matched" or
"did not match" line per condition, per auto-configuration class, for every single class
Spring Boot ships. `--debug` is the single most useful tool for the eventual "why didn't
my starter's bean show up" question: it names precisely which condition failed, on which
class, instead of leaving you to guess.

---

## 6. Summary

- **`@AutoConfiguration`** classes are deliberately invisible to `@ComponentScan` —
  they're discovered through a separate mechanism so they can be conditional by design.
- **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**
  is the literal file `@EnableAutoConfiguration` reads — one fully-qualified class name
  per line, exactly what every real starter library ships.
- **`@ConditionalOnClass`** gates a configuration on whether a class can be loaded at
  all — how a starter's auto-configuration stays inert until its own dependency is
  actually present.
- **`@ConditionalOnMissingBean`** is the override mechanism: supplying your own bean of
  the same type is enough to make an auto-configured default step aside, with zero
  configuration flags.
- **`--debug`** prints the real CONDITIONS EVALUATION REPORT — every auto-configuration
  class Boot considered, and exactly which condition matched or failed for each one.

---

**Previous:** [14 — The `Environment` abstraction](../../02-the-ioc-container-and-dependency-injection/14-the-environment-abstraction/14-the-environment-abstraction.md) ·
**Next:** [16 — Starters and dependency management](../16-starters-and-dependency-management/16-starters-and-dependency-management.md)
