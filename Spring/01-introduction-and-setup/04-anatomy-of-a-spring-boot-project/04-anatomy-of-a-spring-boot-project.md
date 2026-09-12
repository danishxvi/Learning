# 04 · Anatomy of a Spring Boot project

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/01-introduction-and-setup/04-anatomy-of-a-spring-boot-project spring-boot:run
> ```

This lesson is about the **folder structure and the build output**, not new Spring
concepts. Everything in `AnatomyApplication.java` is lesson 03's code, instrumented to
print out what's actually there.

---

## 1. The source tree

```
04-anatomy-of-a-spring-boot-project/
├── pom.xml
└── src
    ├── main
    │   ├── java/com/danish/spring/anatomy/AnatomyApplication.java
    │   └── resources/application.properties
    └── test
        └── java/com/danish/spring/anatomy/AnatomyApplicationTests.java
```

This is Maven's **standard directory layout** — every Maven project, Spring or not, uses
exactly this shape:

| Path | Purpose |
| --- | --- |
| `src/main/java` | Your application code, in a folder tree matching its package name. |
| `src/main/resources` | Non-Java files bundled into the build — `application.properties`, later `static/` and `templates/` for a web app (section 04), database migration scripts (section 05). |
| `src/test/java` | Test code, mirroring the package structure of `src/main/java` exactly. `AnatomyApplicationTests` sits in `com.danish.spring.anatomy`, the same package as `AnatomyApplication` — that mirroring is a convention, not a requirement, but Spring Initializr always generates it this way. |
| `pom.xml` | Declares dependencies, the parent, and build plugins — everything lesson 03 covered. |

Notice the package name repeats the folder path: `com/danish/spring/anatomy/` on disk is
`package com.danish.spring.anatomy;` in every file inside it. This is a Java requirement
(lesson 30 of the `Java/` stack), not a Spring one.

---

## 2. The smoke test Spring Initializr always generates

```java
@SpringBootTest
class AnatomyApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

An empty test method looks useless until you notice what `@SpringBootTest` does:
it starts the **entire** `ApplicationContext`, the same way `spring-boot:run` does. If any
bean fails to construct — a missing dependency, a broken `@Configuration`, a typo in
`application.properties` that breaks a property binding — this test fails immediately,
with a real stack trace, in seconds, with no server or browser involved. It is the
cheapest test you can write and it catches an entire category of "the app doesn't even
start" bugs. Lesson 49 covers `@SpringBootTest` for real; this is here only to show where
it lives.

---

## 3. What auto-configuration silently added

Run the lesson and look at this line:

```
Total bean definitions in the context: 52
```

**This project does not define a single `@Component`, `@Service`, or `@Bean` of its
own.** Every one of those 52 beans was registered by Spring Boot itself — internal
infrastructure beans (`PropertyPlaceholderAutoConfiguration`,
`ConfigurationPropertiesAutoConfiguration`, `AopAutoConfiguration`,
`ApplicationAvailabilityAutoConfiguration`, and dozens more) that `@SpringBootApplication`
and `@EnableAutoConfiguration` decide to create based on nothing more than
`spring-boot-starter` being on the classpath. Add `spring-boot-starter-web` later and this
number jumps sharply — an embedded Tomcat, a `DispatcherServlet`, JSON message converters,
all appear the same way, for the same reason. Lesson 15 explains exactly how
auto-configuration decides what to add and how to see (and override) any single decision.

---

## 4. Where your compiled code actually lives

Under `mvn spring-boot:run`, this line:

```java
AnatomyApplication.class.getProtectionDomain().getCodeSource().getLocation()
```

prints:

```
file:/.../04-anatomy-of-a-spring-boot-project/target/classes/
```

`target/` is Maven's build output directory — not checked into version control (see this
repository's `.gitignore`). `target/classes` holds your compiled `.class` files, laid out
in the same package-mirrored folder tree as `src/main/java`.

---

## 5. Packaging it: the executable "fat jar"

```bash
mvn -f Spring/01-introduction-and-setup/04-anatomy-of-a-spring-boot-project clean package
```

produces `target/04-anatomy-of-a-spring-boot-project-1.0.0.jar`. Unlike a plain Java jar,
this one is **executable on its own** and bundles every dependency inside it — that's why
it's called a "fat" (or "uber") jar:

```bash
java -jar Spring/01-introduction-and-setup/04-anatomy-of-a-spring-boot-project/target/04-anatomy-of-a-spring-boot-project-1.0.0.jar
```

Inspecting it (`jar tf the-jar.jar`) shows the actual layout Spring Boot builds:

```
BOOT-INF/classes/com/danish/spring/anatomy/AnatomyApplication.class   <- your code
BOOT-INF/lib/spring-boot-3.3.4.jar                                    <- every dependency,
BOOT-INF/lib/spring-core-6.1.13.jar                                      unpacked as jars
org/springframework/boot/loader/launch/JarLauncher.class              <- Boot's own bootstrap
```

And the manifest inside `META-INF/MANIFEST.MF`:

```
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: com.danish.spring.anatomy.AnatomyApplication
```

This solves a real problem: a plain `java -jar` can only run a **single** jar — it has no
concept of "also load these other 50 jars from the classpath." Spring Boot's
`Main-Class` is never your own class; it's `JarLauncher`, a tiny bootstrap whose only job
is to build a classpath out of everything in `BOOT-INF/lib/`, then hand off to the real
entry point named in `Start-Class` — your `AnatomyApplication`. That's also why the
class-origin line prints differently when run this way:

```
jar:nested:/.../04-anatomy-of-a-spring-boot-project-1.0.0.jar/!BOOT-INF/classes/!/
```

— your code is loading from *inside* the jar, through `JarLauncher`'s nested-jar class
loader, instead of from a plain folder on disk. Lesson 67 covers packaging and this launch
mechanism in full, including layered jars for Docker.

---

## 6. Summary

- The Maven layout is fixed: `src/main/java`, `src/main/resources`,
  `src/test/java` — Spring Boot changes nothing about this shape.
- `src/test/java` mirrors `src/main/java`'s package structure by convention.
- `@SpringBootTest` with an empty test method is a real, valuable smoke test: it starts
  the whole context and fails loudly if wiring is broken.
- Even a project with **zero** of your own beans has dozens of bean definitions —
  auto-configuration reacts to what's on the classpath, not to what you wrote.
- `target/` is Maven's disposable build output — never committed.
- Packaging produces a **fat jar**: your compiled classes under `BOOT-INF/classes/`, every
  dependency jar under `BOOT-INF/lib/`, and a `JarLauncher` bootstrap as the real
  `Main-Class`, which builds the classpath and hands off to your actual application class
  (`Start-Class`).

---

**Previous:** [03 — Your first Spring Boot project](../03-your-first-spring-boot-project/03-your-first-spring-boot-project.md) ·
**Next:** [05 — Configuration with properties and YAML](../05-configuration-with-properties-and-yaml/05-configuration-with-properties-and-yaml.md)
