# 02 · Your First Program, Line by Line

> **Run the code for this lesson**
> ```bash
> java Java/01-getting-started/02-your-first-program.java
> ```

Lesson 01 explained what happens *around* your code. This lesson explains the code
itself — every single character of the smallest Java program, plus the rules about files,
classes, comments and packages that trip people up in week one.

---

## 1. The smallest complete Java program

```java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

Four lines, and not one of them is decoration. Here is every piece:

| Token | Meaning |
| --- | --- |
| `public` | Access modifier — this class is visible from everywhere. (Lesson 30.) |
| `class` | Keyword that declares a class. All Java code lives inside one. |
| `Hello` | The class name. **PascalCase** by convention. |
| `{ ... }` | The class **body**. Braces delimit every block in Java. |
| `public static void main(String[] args)` | The entry point the JVM calls. Explained in lesson 01 §4. |
| `System` | A built-in class in `java.lang`. |
| `.out` | A `static` field of `System` — a `PrintStream` connected to your console. |
| `.println(...)` | A method on that stream: print the argument, then a newline. |
| `"Hello, World!"` | A **String literal**. Double quotes only — single quotes mean `char`. |
| `;` | Statement terminator. **Mandatory.** Not optional like in JavaScript. |

Read `System.out.println("Hi")` as a sentence: *"in the `System` class, take the `out`
stream, and call `println` on it with the text `Hi`."* Each dot means "go inside".

---

## 2. The filename rule

This rule causes more day-one errors than anything else:

> **If a class is declared `public`, the file must be named exactly `ClassName.java`** —
> same spelling, same capitalisation.

```java
// File MUST be Hello.java
public class Hello { }
```

Break it and `javac` says:

```
error: class Hello is public, should be declared in a file named Hello.java
```

### The escape hatch

The rule only applies to **`public`** classes. Drop `public` and the class becomes
*package-private*, and the filename can be anything:

```java
// File can be called literally anything.java
class Hello { }
```

This is precisely why every file in this repository declares its class **without**
`public` — it lets `02-your-first-program.java` compile even though that is not a valid
Java identifier.

### Related rules worth memorising

- A file may contain **many** classes, but **at most one `public`** class.
- The file may contain classes with `main` methods that are never run — the JVM only
  calls `main` in the class you actually launch.
- Case matters everywhere. `String` ≠ `string`. `Main` ≠ `main`.

---

## 3. Compiling and running, both ways

### Way 1 — the classic two-step

```bash
javac Hello.java
```

This produces `Hello.class` — bytecode. Then:

```bash
java Hello
```

**No `.class` extension.** You pass the *class name*, and the JVM finds the file. Typing
`java Hello.class` is the single most common beginner error; you will get
`ClassNotFoundException: Hello.class`.

Keep output tidy with `-d`, which writes class files into a folder:

```bash
javac -d out Hello.java
```

```bash
java -cp out Hello
```

`-cp` (classpath) tells the JVM where to hunt for classes.

### Way 2 — single-file source mode (Java 11+)

```bash
java Hello.java
```

Compiles in memory and runs immediately. Nothing is written to disk. Perfect for learning
and for scripts; not used for real multi-file projects. This is what this repository uses.

---

## 4. Comments — all three kinds

```java
// Single-line. Everything after // on this line is ignored.

/*
   Multi-line (block) comment.
   Spans as many lines as you like.
*/

/**
 * Javadoc comment. Starts with TWO asterisks.
 * The `javadoc` tool turns these into HTML API documentation.
 * @param name  the person to greet
 * @return      the greeting text
 */
```

Javadoc is not a style preference — it is a tool. Run `javadoc Hello.java` and the JDK
generates a browsable HTML site from those comments. Every method in the Java standard
library is documented this way, which is why your IDE can show you documentation on hover.

> **Gotcha:** block comments do **not** nest. `/* outer /* inner */ still outer */` is a
> syntax error, because the first `*/` closes the comment.

---

## 5. Printing — the three methods you need

| Call | Behaviour |
| --- | --- |
| `System.out.println(x)` | Print `x`, then move to a new line. |
| `System.out.print(x)` | Print `x`, stay on the same line. |
| `System.out.printf(fmt, ...)` | Print with C-style formatting. Lesson 07 covers this fully. |

There is also `System.err.println(...)`, which writes to the **error stream** instead of
the output stream. That matters in real programs, because a user can redirect the two
separately:

```bash
java Hello > output.txt 2> errors.txt
```

Rule of thumb: normal results go to `out`, diagnostics and failures go to `err`.

---

## 6. Statements, blocks and whitespace

Java is **free-form**: the compiler cares about semicolons and braces, not about layout.
All of these compile to identical bytecode:

```java
System.out.println("Hi");

    System.out.println( "Hi" ) ;

System.out.
        println("Hi");
```

You could write an entire program on one line. **Do not.** The compiler does not care;
the next human to read your code does — and that human is usually you, six months later.

### The conventions everyone follows

| Thing | Convention | Example |
| --- | --- | --- |
| Class name | `PascalCase` | `BankAccount` |
| Method name | `camelCase`, starts with a verb | `calculateInterest()` |
| Variable name | `camelCase` | `accountBalance` |
| Constant | `SCREAMING_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Package name | `all.lowercase.dotted` | `com.danish.banking` |
| Indentation | 4 spaces | — |
| Opening brace | End of the same line | `class Hello {` |

These are not enforced by the compiler. They are enforced by every code review you will
ever be in.

---

## 7. Packages — a preview

Real projects put classes into **packages**, which are namespaces backed by folders:

```java
package com.danish.demo;   // must be the FIRST statement in the file

class Hello { }
```

The file must then live in `com/danish/demo/Hello.java`, and you run it with its **fully
qualified name**:

```bash
javac -d out com/danish/demo/Hello.java
```

```bash
java -cp out com.danish.demo.Hello
```

A file with no `package` statement is in the *default package*, which is fine for
learning and unacceptable in production. Lesson 30 covers packages properly. The files in
this repository omit `package` deliberately, so each one runs with a single command.

---

## 8. Reading your first errors

You will meet these four constantly. Learn them now and save yourself hours.

| Message | Cause | Fix |
| --- | --- | --- |
| `';' expected` | Missing semicolon (usually on the line *above* the one reported). | Add it. |
| `cannot find symbol` | Typo, wrong case, or a variable used before declaration. | Check spelling and capitalisation. |
| `class X is public, should be declared in a file named X.java` | Filename/class-name mismatch. | Rename file, or drop `public`. |
| `Could not find or load main class X` | You ran the wrong name, added `.class`, or the classpath is wrong. | Use `java X`, and check `-cp`. |

**Always fix the first error first.** One missing brace can cascade into twenty
downstream errors that all vanish when you fix the real one.

### Compile-time vs runtime errors

| | Compile-time error | Runtime error (exception) |
| --- | --- | --- |
| When | While `javac` runs | While the program executes |
| Example | Missing semicolon, type mismatch | Divide by zero, null reference |
| Program produced? | No | Yes — it just crashes partway |
| Covered in | This lesson | Lesson 39 |

---

## 9. Summary

- Every Java program lives in a class; execution starts at `main`.
- A `public` class must match its filename exactly; a package-private class need not.
- `javac File.java` compiles, `java ClassName` runs — **no `.class` extension**.
- `java File.java` (Java 11+) compiles and runs in one step, in memory.
- Three comment forms: `//`, `/* */`, and `/** */` (Javadoc, which generates real docs).
- `println` adds a newline, `print` does not, `err` is a separate stream from `out`.
- Whitespace is free-form; naming conventions are not optional in practice.
- Fix compiler errors top-down — the first one usually causes the rest.

---

**Previous:** [01 — Introduction](01-introduction-and-how-java-runs.md) ·
**Next:** [03 — Variables and data types](03-variables-and-data-types.md)
