# 01 · Introduction — What Java Is and How It Actually Runs

> **Run the code for this lesson**
> ```bash
> java Java/01-getting-started/01-introduction-and-how-java-runs.java
> ```

---

## 1. What Java is, precisely

Java is a **statically typed, class-based, object-oriented, compiled-then-interpreted,
garbage-collected, general-purpose programming language** first released by Sun
Microsystems in 1995 and now maintained by Oracle together with the OpenJDK community.

That sentence is dense, so take it apart word by word — every one of those words changes
how you will write code:

| Property | What it means for you |
| --- | --- |
| **Statically typed** | Every variable's type is fixed at compile time. `int x = 5;` can never later hold `"hello"`. Mistakes are caught before the program ever runs. |
| **Class-based / object-oriented** | All code lives inside classes. There are no free-floating functions like in C or JavaScript. |
| **Compiled *and* interpreted** | Your source is compiled to **bytecode**, not to machine code. The JVM then executes that bytecode. This is the whole trick behind portability — see §3. |
| **Garbage-collected** | You never call `free()` or `delete`. The JVM reclaims unused memory automatically. |
| **General-purpose** | Backends, Android apps, big-data tooling (Kafka, Spark, Elasticsearch), desktop apps, embedded devices. |

### The one-line promise

Java's original slogan was **"Write Once, Run Anywhere" (WORA)**. The same compiled
`.class` file runs unmodified on Windows, macOS, Linux, and anything else with a JVM.
C, by contrast, has to be recompiled for every target platform.

---

## 2. JDK vs JRE vs JVM — the question everyone gets wrong

This is the single most common early confusion, and it appears in almost every entry-level
interview. The three are **nested**, like Russian dolls:

```
┌─────────────────────────────────────────────────────────┐
│ JDK — Java Development Kit                              │
│ Everything below, PLUS the tools to CREATE programs:    │
│ javac (compiler), javadoc, jar, jdb (debugger), jshell  │
│                                                          │
│   ┌───────────────────────────────────────────────┐     │
│   │ JRE — Java Runtime Environment                │     │
│   │ Everything below, PLUS the standard library:  │     │
│   │ java.lang, java.util, java.io, java.net ...   │     │
│   │                                                │     │
│   │   ┌─────────────────────────────────────┐     │     │
│   │   │ JVM — Java Virtual Machine          │     │     │
│   │   │ The engine that actually EXECUTES   │     │     │
│   │   │ bytecode. Also does JIT compilation │     │     │
│   │   │ and garbage collection.             │     │     │
│   │   └─────────────────────────────────────┘     │     │
│   └───────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
```

| | Contains | You need it to… | Contains a compiler? |
| --- | --- | --- | --- |
| **JVM** | The execution engine only | Execute bytecode | No |
| **JRE** | JVM + standard class library | **Run** a Java program | No |
| **JDK** | JRE + development tools | **Write and compile** Java programs | **Yes** (`javac`) |

**Practical takeaway:** as a developer you install the **JDK**. You never need to install
a JRE separately — you already have one inside your JDK.

> **Modern note.** Since Java 11, Oracle stopped shipping a standalone JRE download. The
> modern approach is to ship your app with a *custom runtime image* built by `jlink`,
> containing only the modules your app actually uses. So "download the JRE" is now
> legacy advice you will still see in old tutorials.

---

## 3. What actually happens when you run a Java program

Here is the full journey of your code, start to finish. Understanding this diagram
explains nearly every "why does Java behave like this?" question you will ever have.

```
   Hello.java                  ┌── you write this
       │
       │  javac Hello.java     ← COMPILE TIME (happens once, on your machine)
       ▼
   Hello.class                 ┌── platform-independent BYTECODE, not machine code
       │
       │  java Hello           ← RUN TIME (happens on any machine with a JVM)
       ▼
 ┌────────────────────────────────────────────────────┐
 │                     THE  JVM                       │
 │                                                    │
 │  1. Class Loader   → finds and loads Hello.class   │
 │  2. Bytecode Verifier → checks the bytecode is     │
 │       safe and well-formed (security!)             │
 │  3. Interpreter    → executes bytecode instruction │
 │       by instruction, immediately                  │
 │  4. JIT Compiler   → notices "hot" methods running │
 │       thousands of times, compiles THOSE to native │
 │       machine code for full speed                  │
 │  5. Garbage Collector → reclaims unreachable       │
 │       objects in the background                    │
 └────────────────────────────────────────────────────┘
       │
       ▼
   Native machine instructions → your CPU
```

### Why two stages instead of one?

Because the two stages have different jobs:

- **`javac` (compile time)** guarantees *correctness*. It type-checks everything, rejects
  malformed code, and produces bytecode. This step is why a typo in a variable name is a
  build failure in Java but a 3 a.m. production crash in a dynamically typed language.
- **The JVM (runtime)** guarantees *portability and speed*. Bytecode is generic, so it
  runs anywhere; the JIT then specialises it for the specific CPU it finds itself on.

### Isn't interpreting slow?

It would be — which is exactly why the JVM does not just interpret. The **JIT
(Just-In-Time) compiler** watches which methods run most often and compiles those to
native machine code while the program is running. It can even optimise using information
that a C compiler could never have, because it can see the *actual* runtime behaviour
(which branch is usually taken, which type actually shows up). This is why long-running
Java server applications routinely reach speeds within a few percent of C.

**The trade-off:** Java is slow to *start* (the JVM has to boot, load classes, and warm
up the JIT) but fast once *warm*. That is why Java dominates long-running servers and is
less common in short-lived command-line tools.

---

## 4. The `main` method — the one entry point

Every runnable Java program starts at a method with this exact signature:

```java
public static void main(String[] args)
```

Each keyword is mandatory and each one is there for a reason:

| Part | Why it must be there |
| --- | --- |
| `public` | The JVM lives outside your class and must be able to call it. |
| `static` | The JVM calls it **before any object exists**, so it cannot be an instance method. |
| `void` | The JVM ignores return values; a program's exit status is set with `System.exit()`. |
| `main` | The fixed name the JVM looks for. Change it and you get `NoSuchMethodError`. |
| `String[] args` | Command-line arguments, as text. Always non-null; empty if none given. |

`String... args` (varargs) is also legal and identical at the bytecode level.
`static public void main` also works — modifier order does not matter.

---

## 5. Java versions — which one to care about

Java releases every six months, but only some releases are **LTS (Long-Term Support)**
and those are the ones companies actually run.

| Version | Year | Why it matters |
| --- | --- | --- |
| **Java 8** | 2014 | Lambdas, Streams, `Optional`, new date/time API. Still very widespread in legacy systems. |
| **Java 11** | 2018 | First LTS after 8. `var`, running `.java` files directly, HTTP client. |
| **Java 17** | 2021 | Records, sealed classes, text blocks, pattern matching for `instanceof`. |
| **Java 21** | 2023 | **Virtual threads**, pattern matching for `switch`, sequenced collections. |
| **Java 25** | 2025 | Latest LTS; refinement rather than revolution. |

This repository targets **Java 21**, and every lesson flags features newer than Java 8 so
the material stays usable on older codebases.

---

## 6. Common misconceptions, corrected

| Myth | Reality |
| --- | --- |
| "Java is interpreted, so it's slow." | It is interpreted *then JIT-compiled*. Warm Java is close to native speed. |
| "Java and JavaScript are related." | They are not. The name was 1995 marketing. Different designers, syntax, type systems, runtimes. |
| "Java is pure object-oriented." | It is not — **primitives** (`int`, `char`, `boolean`…) are not objects. That is a deliberate performance decision, covered in lesson 03. |
| "The JVM only runs Java." | It runs any language that emits JVM bytecode: Kotlin, Scala, Groovy, Clojure. |
| "Garbage collection means no memory leaks." | You can still leak by *holding references* to objects you no longer need. GC only frees what is unreachable. |
| "You need an IDE to write Java." | You need a text editor and a JDK. An IDE is convenience, not a requirement. |

---

## 7. The three commands you will use forever

```bash
java -version
```
Prints your installed JDK version. Your first check whenever anything is odd.

```bash
javac Hello.java
```
Compiles source to `Hello.class` (bytecode) in the same folder.

```bash
java Hello
```
Runs the class. **Note: no `.class` extension.** You pass the *class name*, not a filename.

Since Java 11 you can also collapse compile-and-run into one step for single files, which
is exactly what this repository relies on:

```bash
java Hello.java
```

Nothing is written to disk; the class is compiled in memory and executed immediately.

---

## 8. Bonus: `jshell`, the Java REPL

The JDK ships an interactive shell. It is genuinely the fastest way to test a one-liner
without creating a file:

```bash
jshell
```

Then type `1 + 1`, press Enter, and you get `$1 ==> 2`. Type `/exit` to leave. Use it
constantly while working through this repository — it turns "I wonder what that does"
into a five-second experiment.

---

## 9. Summary

- Java compiles to **bytecode**, which the **JVM** executes — this is what makes it portable.
- **JDK ⊃ JRE ⊃ JVM.** Install the JDK; it contains the other two.
- The **JIT compiler** turns hot bytecode into native code, so Java is slow to start but
  fast once warm.
- Execution always begins at `public static void main(String[] args)`.
- Java is statically typed and garbage-collected: many errors are caught at compile time,
  and memory is managed for you — but you can still leak by holding references.
- Target **Java 21**; know that Java 8 and 17 are still everywhere in industry.

---

**Next:** [02 — Your first program, line by line](02-your-first-program.md)
