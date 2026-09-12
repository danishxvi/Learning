# 03 · Your first Spring Boot project

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/01-introduction-and-setup/03-your-first-spring-boot-project spring-boot:run
> ```

Lesson 02 started a plain Spring container by hand and listed four things it was missing.
This lesson adds Spring Boot and gets all four practically for free.

---

## 1. The parent POM: starter dependency management

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
</parent>
```

Compare this to lesson 02's `pom.xml`, which hand-picked `spring-context` version
`6.1.14`. This parent does that job for **every** Spring Boot artifact at once: it is a
list of dependency versions that are tested to work together. Because of it, every Boot
dependency below can drop its `<version>` entirely:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <!-- no <version> - the parent supplies it -->
</dependency>
```

Bump one number (`3.3.4` → a newer release) and every Spring Boot dependency in the
project upgrades together, to versions the Spring team has already verified are
compatible. This is the "dependency management" from lesson 02's ecosystem table.

**A "starter"** is a dependency with no code of its own — just a curated list of other
dependencies for one job. `spring-boot-starter` (used here) pulls in auto-configuration,
logging (Logback) and the core Boot classes. Later lessons add
`spring-boot-starter-web` (section 04), `spring-boot-starter-data-jpa` (section 05), and
so on — each one a single line that used to be several hand-picked, hand-matched
dependencies.

---

## 2. `@SpringBootApplication` — one annotation standing in for three

```java
@SpringBootApplication
public class FirstBootApplication {
```

This single annotation is exactly equivalent to writing all three of these on the same
class:

| Annotation | What it does |
| --- | --- |
| `@SpringBootConfiguration` | A specialised `@Configuration` — this class can define `@Bean` methods, same as lesson 02's `AppConfig`. |
| `@EnableAutoConfiguration` | Turns on auto-configuration: Spring Boot inspects the classpath and registers extra beans it thinks you'll need (lesson 15, in full). |
| `@ComponentScan` | Scans the package this class lives in, and everything below it — same job as lesson 02's explicit `@ComponentScan(basePackages = ...)`, just inferred instead of typed out. |

That is why `FirstBootApplication` needed no separate `AppConfig` class the way lesson
02's project did — the one annotation does the work of both `@Configuration` and
`@ComponentScan`, plus turns on auto-configuration on top.

---

## 3. `SpringApplication.run` — one line replacing several

```java
ConfigurableApplicationContext context = SpringApplication.run(FirstBootApplication.class, args);
```

Line for line, this replaces lesson 02's:

```java
ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
```

`SpringApplication.run` does everything that line did — build the `ApplicationContext`,
scan for `@Component` beans, resolve the dependency graph — **plus**:

- reads `application.properties` (or `.yml`) from `src/main/resources` automatically,
- configures logging before anything else runs,
- prints the startup banner and log lines you saw when you ran this lesson,
- decides, via auto-configuration, which additional beans to register based on what
  starters are on the classpath (there is nothing to auto-configure yet with only
  `spring-boot-starter` — this becomes visible the moment `spring-boot-starter-web` is
  added in section 04),
- registers a JVM shutdown hook, so — unlike lesson 02 — a real Boot application does not
  need to call `context.close()` itself. This lesson still calls it explicitly only so the
  program exits and prints its final line for the demo.

It still returns the exact same kind of object lesson 02 got back —
`ConfigurableApplicationContext` — which is why `context.getBean(HelloBean.class)` at the
bottom of this lesson works with no changes at all from how lesson 02 called `getBean`.

---

## 4. Seeing your own code run: `CommandLineRunner`

```java
@Component
static class HelloBean implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println(">>> Hello, Spring Boot!");
    }
}
```

`CommandLineRunner` is a Spring Boot interface with one method, `run`, which Boot calls
automatically on every bean that implements it, exactly once, right after the context
finishes starting. It needs no wiring beyond `@Component` — Boot finds it by type during
startup. This is the simplest way to run your own code inside a Boot application before
section 04 introduces web endpoints; lesson 17 covers it (and its sibling
`ApplicationRunner`) in full.

---

## 5. What just happened, end to end

1. `mvn spring-boot:run` compiles the project and starts `FirstBootApplication.main`.
2. `SpringApplication.run` builds an `ApplicationContext`, reads
   `application.properties`, scans this project's package, and finds one `@Component`:
   `HelloBean`.
3. Because `HelloBean` implements `CommandLineRunner`, Boot calls its `run` method once
   the context is ready — printing the `>>> Hello, Spring Boot!` lines.
4. `main` gets the running context back, pulls `HelloBean` out of it by hand (proving it's
   an ordinary bean, reachable the same way lesson 02's beans were), and closes the
   context.

Nothing here required an XML file, a `web.xml`, or an external application server. That
absence is the entire pitch of Spring Boot.

---

## 6. Summary

- **`spring-boot-starter-parent`** centralises version numbers for every Boot dependency,
  so individual `<dependency>` entries don't need their own `<version>`.
- **A starter** (`spring-boot-starter`, `spring-boot-starter-web`, ...) is a single
  dependency standing in for a curated group of others.
- **`@SpringBootApplication`** = `@SpringBootConfiguration` + `@EnableAutoConfiguration` +
  `@ComponentScan`, combined onto one class.
- **`SpringApplication.run`** replaces `new AnnotationConfigApplicationContext(...)` and
  additionally loads configuration, sets up logging, runs auto-configuration, and installs
  a shutdown hook.
- **`CommandLineRunner`** beans run automatically, once, right after startup — the
  simplest way to execute your own code in a Boot application.
- Everything from lesson 02 (`@Component`, constructor injection, singleton beans,
  `getBean`) still works completely unchanged — Spring Boot is additive, not a
  replacement.

---

**Previous:** [02 — Spring vs Spring Boot, and the ecosystem](../02-spring-vs-spring-boot-and-the-ecosystem/02-spring-vs-spring-boot-and-the-ecosystem.md) ·
**Next:** [04 — Anatomy of a Spring Boot project](../04-anatomy-of-a-spring-boot-project/04-anatomy-of-a-spring-boot-project.md)
