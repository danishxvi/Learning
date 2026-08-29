# 41 · try-with-resources and Best Practices

> **Run the code for this lesson**
> ```bash
> java Java/08-exception-handling/41-try-with-resources-and-best-practices.java
> ```

Anything you open, you must close. Doing that correctly by hand is startlingly hard —
which is why Java 7 added syntax for it.

---

## 1. The problem with `finally`

The pre-Java-7 idiom, written correctly:

```java
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader(path));
    return reader.readLine();
} finally {
    if (reader != null) {        // must null-check — the constructor may have thrown
        try {
            reader.close();      // close() itself throws IOException
        } catch (IOException ignored) {
            // and if we let this propagate, it MASKS the real exception
        }
    }
}
```

Eight lines of ceremony for one line of work, and three separate ways to get it wrong:

1. Forgetting the null check — the resource may never have been assigned.
2. Letting `close()`'s exception propagate — it **masks** the original (lesson 39 §3).
3. Two resources means **nested** try/finally, and the nesting compounds.

---

## 2. try-with-resources

```java
try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
    return reader.readLine();
}
```

That is the whole thing. The compiler generates the null check, the `finally`, the nested
try, and the suppression handling.

### The rules

- The resource must implement **`AutoCloseable`** (or `Closeable`, which extends it).
- The variable is implicitly **`final`** — you cannot reassign it.
- Resources close in **reverse order** of declaration, which is what you want when one
  wraps another.
- `close()` runs **before** any `catch` or `finally` block you write.

### Multiple resources

```java
try (var input = new FileInputStream(source);
     var output = new FileOutputStream(target)) {
    input.transferTo(output);
}
```

Separated by semicolons. Both close, in reverse order, even if the body throws.

### Effectively-final resources (Java 9+)

```java
var reader = openReader();
try (reader) {              // an existing effectively-final variable
    ...
}
```

Useful when the resource is created elsewhere. Before Java 9 you needed a redundant
`try (var r = reader)`.

---

## 3. Suppressed exceptions

This is the feature that makes try-with-resources genuinely better, not just shorter.

When the body throws **and** `close()` throws, the body's exception wins and `close()`'s is
attached as **suppressed** — not discarded:

```java
try (var resource = new FailingResource()) {
    throw new IllegalStateException("the real problem");
}
// IllegalStateException propagates,
// with the close() failure available via getSuppressed()
```

```java
catch (Exception e) {
    System.out.println(e.getMessage());                    // "the real problem"
    for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("  suppressed: " + suppressed); // the close() failure
    }
}
```

The hand-written `finally` version **loses one of the two exceptions entirely**. Stack
traces print suppressed exceptions automatically, marked `Suppressed:`.

---

## 4. Writing your own resource

```java
class Connection implements AutoCloseable {
    @Override
    public void close() {          // narrow the exception, or drop it entirely
        // release
    }
}
```

`AutoCloseable.close()` declares `throws Exception`, but you should **narrow it** — a
`close()` that throws `Exception` forces every caller to catch `Exception`.

Three rules for `close()`:

1. **Make it idempotent.** Calling it twice must be safe.
2. **Never throw `InterruptedException`** from it — it would be suppressed, silently losing
   an interrupt.
3. **Prefer not throwing at all**, if the cleanup cannot meaningfully fail.

`Closeable` (from `java.io`) narrows `close()` to `throws IOException` and requires
idempotency. Prefer it for I/O types.

---

## 5. Best practices, gathered

### Catch narrowly

```java
catch (IOException e) { }          // yes
catch (Exception e) { }            // catches every bug too
catch (Throwable t) { }            // now you have caught OutOfMemoryError
```

### Never swallow

```java
catch (IOException e) { }                      // the failure is now invisible
catch (IOException e) { e.printStackTrace(); } // printed to stderr, then carried on
```

`printStackTrace()` is not logging: it bypasses your log configuration, has no severity or
timestamp, and in a server is often discarded entirely.

### Restore the interrupt flag

```java
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return;
}
```

Catching `InterruptedException` **clears** the flag. Not restoring it means nothing above
you can learn the thread was asked to stop.

### Fail fast

Validate at the boundary, so the exception names the real cause rather than surfacing three
layers later as a `NullPointerException`.

### Do not use exceptions for control flow

```java
try { return map.get(key).trim(); }
catch (NullPointerException e) { return ""; }     // no

String value = map.get(key);                       // yes
return value == null ? "" : value.trim();
```

### Log **or** rethrow, never both

Doing both produces the same failure in the log twice, from different places, which makes
an incident harder to read rather than easier.

### Clean up in `finally` or try-with-resources — never only on the happy path

---

## 6. Where exceptions and lambdas collide

A lambda cannot throw a checked exception unless the functional interface declares it:

```java
paths.stream().map(p -> Files.readString(p))    // does not compile
```

Three workarounds:

1. **A helper method** that catches and returns a value — usually the clearest.
2. **A try inside the lambda** — verbose but explicit.
3. **A custom functional interface** that declares `throws`, plus an adapter that wraps
   into an unchecked exception.

This friction is one of the strongest practical arguments for unchecked exceptions in
modern Java.

---

## 7. Summary

- The hand-written `finally` idiom needs a null check, a nested try, and still **loses one
  exception**. try-with-resources generates all of it correctly.
- The resource must implement `AutoCloseable`; the variable is implicitly `final`; multiple
  resources close in **reverse order**; `close()` runs **before** your `catch`/`finally`.
- Java 9+ allows an existing **effectively-final** variable in the resource list.
- **Suppressed exceptions** are the real win: the body's exception propagates and
  `close()`'s is attached via `getSuppressed()` rather than discarded.
- When writing a resource: **narrow** `close()`'s declared exception, make it
  **idempotent**, and never throw `InterruptedException` from it.
- Catch narrowly, never swallow, never use `printStackTrace()` as logging.
- **Restore the interrupt flag** when catching `InterruptedException`.
- Do not use exceptions for control flow. Log **or** rethrow, not both.
- Checked exceptions do not compose with lambdas — wrap in a helper, or use unchecked.

---

**Previous:** [40 — Custom exceptions](40-custom-exceptions.md) ·
**Next:** [42 — Wrapper classes and autoboxing](../09-generics-and-collections/42-wrapper-classes-and-autoboxing.md)
