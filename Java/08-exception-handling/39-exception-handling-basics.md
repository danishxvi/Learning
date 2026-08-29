# 39 · Exceptions — Hierarchy, `try`/`catch`/`finally`

> **Run the code for this lesson**
> ```bash
> java Java/08-exception-handling/39-exception-handling-basics.java
> ```

An exception is an object describing something that went wrong. Java's exception system is
one of its most distinctive features — and the one most consistently misused.

---

## 1. The hierarchy

Everything throwable descends from `Throwable`:

```
                    Throwable
                   /         \
              Error           Exception
             /     \         /         \
StackOverflow  OutOfMemory  RuntimeException   IOException
                            /      |       \    SQLException
              NullPointer  Illegal  IndexOutOf  (checked)
              (unchecked)  Argument  Bounds
```

Three branches, and the distinction between them is the whole design:

| Branch | Checked? | Meaning | You should |
| --- | --- | --- | --- |
| **`Error`** | No | The JVM is in trouble | **Never catch** |
| **`RuntimeException`** | No | A **programming bug** | Fix the bug, do not catch |
| **Other `Exception`** | **Yes** | An expected external failure | Handle or declare |

> **`RuntimeException` extends `Exception`.** That trips people up: catching `Exception`
> catches unchecked exceptions too, including `NullPointerException`. It is exactly why
> `catch (Exception e)` is so dangerous.

### Errors — do not catch them

`OutOfMemoryError`, `StackOverflowError`, `NoClassDefFoundError`. Catching one usually
means your handler also fails, or the program limps on in a corrupt state. The only
legitimate case is a top-level handler that logs and shuts down.

### Unchecked exceptions — bugs, not conditions

```java
NullPointerException          // you forgot a null check
IllegalArgumentException      // the caller passed nonsense
IllegalStateException         // the object is in the wrong state
IndexOutOfBoundsException     // your index was wrong
ArithmeticException           // integer divide by zero
ClassCastException            // your cast was wrong
NumberFormatException         // you parsed unvalidated input
```

Every one signals a **defect in code**. The fix is to correct the code, not to wrap it in a
`try`.

### Checked exceptions — expected failures

```java
IOException, FileNotFoundException, SQLException, InterruptedException
```

These describe things that go wrong **even when your code is perfect**: a network drops, a
file vanishes, a disk fills. The compiler forces you to acknowledge them.

---

## 2. `try` / `catch` / `finally`

```java
try {
    riskyOperation();
} catch (FileNotFoundException e) {     // most specific FIRST
    // handle
} catch (IOException e) {               // broader
    // handle
} finally {
    // always runs
}
```

### Ordering matters

Catch blocks are tested **top to bottom**, and the first match wins. A broader type before
a narrower one is a **compile error**:

```java
catch (Exception e) { }
catch (IOException e) { }     // ERROR: IOException has already been caught
```

### Multi-catch (Java 7+)

```java
catch (IOException | SQLException e) {
    log(e);
}
```

The variable is implicitly `final`, and its static type is the **nearest common supertype**
— so you can only call methods available on that.

### `finally` always runs

It runs on normal completion, on an exception, and even on `return`. The only things that
prevent it are `System.exit()`, a JVM crash, or the thread being killed.

```java
try {
    return "from try";
} finally {
    System.out.println("this still runs");   // prints BEFORE the method returns
}
```

---

## 3. The `finally` traps

### A `return` in `finally` swallows everything

```java
try {
    throw new RuntimeException("real problem");
} finally {
    return "everything is fine";      // the exception VANISHES
}
```

The exception is discarded silently. So is any `return` value from the `try` block. **Never
put `return`, `break` or `continue` in a `finally`** — most linters flag it, and it is one
of the nastiest bugs in Java because the evidence disappears.

### `finally` overwrites the `try`'s return value

```java
int compute() {
    int value = 1;
    try {
        return value;          // the value 1 is SAVED here
    } finally {
        value = 99;            // too late — the return value was already captured
    }
}                              // returns 1, not 99
```

The return value is evaluated *before* `finally` runs, so mutating the variable afterwards
has no effect. This surprises everyone once.

### An exception in `finally` masks the original

```java
try {
    throw new IllegalStateException("the real cause");
} finally {
    throw new RuntimeException("from cleanup");   // the real cause is LOST
}
```

The original is discarded entirely. This is precisely the problem try-with-resources solves
with **suppressed exceptions** (lesson 41).

---

## 4. `throw` and `throws`

```java
void readFile(String path) throws IOException {   // DECLARES it may happen
    if (path == null) {
        throw new IllegalArgumentException("path");   // THROWS one now
    }
}
```

- `throw` is a **statement** that raises an exception.
- `throws` is a **clause** on a method signature declaring what may escape.

Only **checked** exceptions must be declared. Unchecked ones may be declared for
documentation, but the compiler does not require it and it is usually noise.

### The override rule

A subclass method may throw the **same, narrower, or fewer** checked exceptions — never
more (lesson 27). Otherwise a caller holding a supertype reference would be blindsided.

---

## 5. Reading a stack trace

```
Exception in thread "main" java.lang.NullPointerException: Cannot invoke
        "String.length()" because "text" is null
    at com.example.Service.process(Service.java:42)     ← where it was THROWN
    at com.example.Service.handle(Service.java:28)
    at com.example.Main.main(Main.java:12)              ← where it STARTED
Caused by: java.sql.SQLException: connection refused
    at ...
    ... 12 more
```

**Read from the top.** The first line is the exception and message; the first `at` is where
it was thrown; each line below is the caller. `Caused by` shows the **original** exception
in a chain, and is usually the interesting part.

Since Java 14, **helpful NullPointerException messages** name the exact expression that was
null — a large improvement over guessing which of five dots on a line was the problem.

---

## 6. Exception chaining

When you catch and rethrow, **always preserve the cause**:

```java
try {
    database.query(sql);
} catch (SQLException e) {
    throw new DataAccessException("failed to load user " + id, e);   // ← the cause
}
```

Omitting that second argument throws away the entire original stack trace, and the person
debugging it at 3 a.m. is left with nothing.

---

## 7. Anti-patterns

| Anti-pattern | Why it is wrong |
| --- | --- |
| `catch (Exception e) { }` | Swallows everything, including bugs. The failure becomes invisible. |
| `catch (Exception e) { e.printStackTrace(); }` | Prints to stderr and continues as if nothing happened. Not logging. |
| `catch (Exception e)` as a catch-all | Also catches `NullPointerException` and every other bug |
| Exceptions for control flow | Slow, and it hides intent |
| Losing the cause on rethrow | Throws away the stack trace |
| `throws Exception` on every method | Tells the caller nothing |
| Catching `Throwable` | Now you have caught `OutOfMemoryError` too |

### The empty catch block

If you genuinely must ignore an exception, **say so and say why**:

```java
try {
    Thread.sleep(100);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();     // restore the flag — never just ignore
}
```

`InterruptedException` deserves special mention: catching it **clears the interrupt flag**.
If you do not restore it, code above you can never learn the thread was asked to stop.
Swallowing it is one of the most common concurrency bugs there is.

---

## 8. Checked vs unchecked — the real debate

Java is the only mainstream language with checked exceptions, and opinion is genuinely
divided.

**The case for:** the compiler documents and enforces failure handling. You cannot forget
that a file read might fail.

**The case against:** they leak through abstractions, force `throws` clauses up entire call
stacks, do not compose with lambdas (a `Function` cannot throw a checked exception), and in
practice cause people to write `catch (Exception e) { }` just to make the compiler quiet.

**The pragmatic position most modern Java takes:**

- Use **unchecked** exceptions for programming errors and for failures the caller cannot
  meaningfully recover from.
- Use **checked** exceptions only when the caller can realistically do something about it.
- Wrap low-level checked exceptions in domain-specific unchecked ones at your layer
  boundary, preserving the cause.

Spring, Hibernate and most modern frameworks converted their checked exceptions to
unchecked for exactly these reasons.

---

## 9. Cost

Creating an exception is expensive, because filling in the stack trace walks every frame.
Throwing and catching one is cheap by comparison.

That is why exceptions are wrong for control flow — not the throw, but the **construction**.
In the rare hot path where you genuinely need a signal, you can override `fillInStackTrace`
to return `this`, giving a stackless exception. Doing so is a specialised optimisation, not
a default.

---

## 10. Summary

- `Throwable` → `Error` (never catch), `RuntimeException` (bugs — fix them), and other
  `Exception` (checked — expected failures).
- **`RuntimeException` extends `Exception`**, so `catch (Exception e)` catches bugs too.
- Catch blocks match **top to bottom**; a broader type before a narrower one is a compile
  error. Multi-catch gives an implicitly `final` variable typed as the common supertype.
- `finally` always runs — except on `System.exit()`.
- **Never `return` from `finally`**: it silently discards exceptions and return values.
  A variable mutated in `finally` does not change an already-evaluated return value.
  An exception thrown in `finally` **masks the original**.
- `throw` raises; `throws` declares. Only checked exceptions must be declared.
- Read a stack trace **from the top**; `Caused by` holds the original.
- **Always pass the cause** when wrapping and rethrowing.
- Never swallow an exception. Restore the interrupt flag when catching
  `InterruptedException`.
- Exception **creation** is expensive (the stack trace), which is why they are wrong for
  control flow.

---

**Previous:** [38 — Immutability](../07-object-oriented-advanced/38-immutability-and-defensive-copying.md) ·
**Next:** [40 — Custom exceptions](40-custom-exceptions.md)
