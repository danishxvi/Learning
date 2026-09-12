# 16 · Starters and dependency management

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/16-starters-and-dependency-management spring-boot:run
> ```
> See the actual dependency tree:
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/16-starters-and-dependency-management dependency:tree
> ```

Lesson 03 called a starter "a curated list of other dependencies for one job." This
lesson opens that list up, shows exactly where its versions come from, and overrides one
of them on purpose.

---

## 1. One line, several jars

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

`mvn dependency:tree` shows what that one line actually resolves to:

```
+- org.springframework.boot:spring-boot-starter-validation:jar:3.3.4:compile
   +- org.apache.tomcat.embed:tomcat-embed-el:jar:10.1.30:compile
   \- org.hibernate.validator:hibernate-validator:jar:8.0.1.Final:compile
      +- jakarta.validation:jakarta.validation-api:jar:3.0.2:compile
      +- org.jboss.logging:jboss-logging:jar:3.5.3.Final:compile
      \- com.fasterxml:classmate:jar:1.7.0:compile
```

Four real jars, from three different `groupId`s (`org.apache.tomcat.embed`,
`org.hibernate.validator`, `jakarta.validation`, plus two smaller support libraries),
pulled in by one dependency with **no version number written anywhere in this project's
`pom.xml`**. `spring-boot-starter-validation` itself has no code of its own — it exists
purely to name this specific, tested-together set of jars in one place.

---

## 2. Where the version numbers actually come from

None of the jars above have a `<version>` in this project. Lesson 03 established that
`spring-boot-starter-parent` supplies them — specifically, that parent POM's own
`<dependencyManagement>` **imports** a much larger BOM (Bill of Materials),
`spring-boot-dependencies`, which is nothing but a giant list of `<dependencyManagement>`
entries: hundreds of artifacts, each pinned to one version, chosen and tested by the
Spring Boot team to work together. `hibernate-validator:8.0.1.Final` is Boot 3.3.4's
pinned choice — bump the parent's version (lesson 03) and this number moves too, along
with every other managed dependency, together, as one coordinated upgrade.

**`<dependencyManagement>` sets a version *if* a dependency is used; it does not add the
dependency itself.** That's why `spring-boot-starter-validation` still has to appear under
`<dependencies>` in this lesson's `pom.xml` — the parent's BOM only says *what version* to
use if you ask for it.

---

## 3. Overriding one managed version

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.0.Final</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Adding this to the lesson's own `pom.xml` and re-running `dependency:tree` shows the
override took effect:

```
\- org.hibernate.validator:hibernate-validator:jar:8.0.0.Final:compile
```

`8.0.1.Final` → `8.0.0.Final` — one line, and Maven resolves this project's own
`<dependencyManagement>` **before** the inherited one from `spring-boot-starter-parent`,
because a POM's own entries always take precedence over an inherited BOM. This is the
escape hatch for the rare case where Boot's chosen version of one specific library
conflicts with something else in a project — override just that one artifact, and leave
every other managed version exactly as Boot intends.

> **This is a last resort, not a habit.** The whole value of `spring-boot-starter-parent`
> is that its versions are tested together. Overriding one, as done here, opts back into
> the version-conflict risk Boot's BOM exists specifically to remove — only do it with a
> concrete reason (a known CVE fix, a library requiring a newer minor version) and revert
> it once the underlying constraint is gone.

---

## 4. Proof the starter works at runtime, not just at build time

`dependency:tree` only proves the jars *resolve*. This lesson also uses them:

```java
private final Validator validator;   // auto-configured because Hibernate Validator is present

validator.validate(new SignupRequest("", "not-an-email"));
```

```
username must not be blank (was: "")
email must be a well-formed email address (was: "not-an-email")
```

A `Validator` bean exists in the context with **zero configuration written for it** —
`spring-boot-starter-validation` being on the classpath is enough for Boot's own
auto-configuration (lesson 15's exact mechanism) to register one automatically, and the
real messages above come from the pinned-down `8.0.0.Final` Hibernate Validator jar
actually running. Bean Validation itself — what `@NotBlank`/`@Email` mean and how they
integrate with a REST API — is covered fully in lesson 26; this lesson only needed enough
of it to prove the dependency chain is real, not just declared.

---

## 5. Summary

- A **starter** has no code of its own — it's a curated list of other dependencies for
  one job, so one line in `pom.xml` can pull in several coordinated jars.
- **`spring-boot-starter-parent`**'s `<dependencyManagement>` imports the
  `spring-boot-dependencies` BOM — a list of pinned versions for hundreds of artifacts,
  which is why individual `<dependency>` entries in a Boot project rarely need a
  `<version>`.
- **`<dependencyManagement>` sets a version if a dependency is used — it does not add the
  dependency.** The `<dependencies>` block still has to list what you actually want.
- A project's **own** `<dependencyManagement>` entry overrides an inherited BOM version
  for that one artifact, verifiable with `mvn dependency:tree` — a deliberate, occasional
  escape hatch, not a routine practice.
- Bumping `spring-boot-starter-parent`'s version upgrades every managed dependency
  together, as a single coordinated, pre-tested set.

---

**Previous:** [15 — Auto-configuration, explained](../15-autoconfiguration-explained/15-autoconfiguration-explained.md) ·
**Next:** [17 — `CommandLineRunner` and `ApplicationRunner`](../17-commandlinerunner-and-applicationrunner/17-commandlinerunner-and-applicationrunner.md)
