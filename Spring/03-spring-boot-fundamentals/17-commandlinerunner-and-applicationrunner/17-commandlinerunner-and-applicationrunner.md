# 17 · `CommandLineRunner` and `ApplicationRunner`

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/17-commandlinerunner-and-applicationrunner spring-boot:run
> ```
> With arguments: `"-Dspring-boot.run.arguments=--name=Danish --retries=3 hello"` ·
> triggering the abort: `-Dspring-boot.run.arguments=--fail`

Lesson 03 used a `CommandLineRunner` to run code once, right after startup, with no
explanation of what it actually receives or how it relates to `ApplicationRunner`. This
lesson answers both, and shows what happens when one throws.

---

## 1. Raw args vs parsed args

```java
public class RawArgsRunner implements CommandLineRunner {
    public void run(String... args) { ... }        // exactly main(String[] args) would get
}

public class ParsedArgsRunner implements ApplicationRunner {
    public void run(ApplicationArguments args) { ... }   // a WRAPPER that understands --key=value
}
```

Running with `--name=Danish --retries=3 hello`:

```
[1] CommandLineRunner - raw args: [--name=Danish, --retries=3, hello]

[2] ApplicationRunner - option names:  [retries, name]
    --retries = [3]
    --name = [Danish]
[2] ApplicationRunner - non-option args: [hello]
[2] ApplicationRunner - raw source args: [--name=Danish, --retries=3, hello]
```

`CommandLineRunner` hands you the same flat `String[]` `main` would — `"--name=Danish"`
and `"hello"` are indistinguishable, just strings in an array, and you'd have to parse
`--key=value` yourself. `ApplicationRunner` receives an `ApplicationArguments` object that
has **already done that parsing**: `getOptionNames()`/`getOptionValues(name)` for anything
matching Spring Boot's `--key=value` convention, `getNonOptionArgs()` for everything else,
and `getSourceArgs()` if you need the original unparsed array back. **Prefer
`ApplicationRunner`** whenever the arguments have any structure worth naming — which is
almost always.

---

## 2. Ordering multiple runners with `@Order`

```java
@Order(1)  class RawArgsRunner    implements CommandLineRunner { ... }
@Order(2)  class ParsedArgsRunner implements ApplicationRunner { ... }
@Order(3)  class FailingRunner    implements ApplicationRunner { ... }
```

The numbered output above prints in exactly `1, 2, 3` order, every run. `@Order` here is
the same annotation lesson 11 used to sequence a `List<T>` injection — Spring Boot runs
every `CommandLineRunner` and `ApplicationRunner` bean in the context, together, sorted by
`@Order` (lowest first), regardless of which of the two interfaces each one implements.
With no `@Order` at all, the sequence is undefined — don't rely on declaration order or
bean name.

---

## 3. A runner that throws aborts the whole application

```java
public void run(ApplicationArguments args) {
    if (args.containsOption("fail")) {
        throw new IllegalStateException("FailingRunner: --fail was passed, aborting startup on purpose.");
    }
}
```

Running with `--fail`, runners `[1]` and `[2]` still print — they ran first — but the
final line in `main` (`"This line only prints if EVERY runner completed..."`) never
appears, and the process exits with status `1`:

```
java.lang.IllegalStateException: FailingRunner: --fail was passed, aborting startup on purpose.
	at com.danish.spring.runners.FailingRunner.run(FailingRunner.java:18)
	at org.springframework.boot.SpringApplication.lambda$callRunner$4(SpringApplication.java:786)
	...
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1352)
```

`SpringApplication.run` calls every runner from inside its own method — the stack trace
above shows exactly that (`SpringApplication.callRunners` → `callRunner` → your `run`
method) — and does not swallow an exception thrown from one. **A runner is not a
best-effort background task; it's treated as part of startup itself.** If one throws, the
`ApplicationContext` that was just built is closed again and `SpringApplication.run` never
returns normally — which is exactly the right behavior for validating "is a required
external system reachable" or "did required setup succeed" before letting the rest of the
application receive traffic.

---

## 4. Summary

- **`CommandLineRunner`**: raw `String[] args`, unparsed — the same shape `main` itself
  receives.
- **`ApplicationRunner`**: an `ApplicationArguments` wrapper that separates
  `--key=value` options from plain positional arguments automatically — prefer this one.
- Every runner bean in the context runs once, automatically, right after the context
  finishes starting — **sorted by `@Order`**, regardless of which interface each
  implements.
- **An exception thrown from any runner aborts the entire application** — startup fails,
  the context is closed, and `SpringApplication.run` never returns normally. Runners are
  a legitimate place to fail fast on a broken precondition, not just to print a greeting.

---

**Previous:** [16 — Starters and dependency management](../16-starters-and-dependency-management/16-starters-and-dependency-management.md) ·
**Next:** [18 — Logging with SLF4J and Logback](../18-logging-with-slf4j-and-logback/18-logging-with-slf4j-and-logback.md)
