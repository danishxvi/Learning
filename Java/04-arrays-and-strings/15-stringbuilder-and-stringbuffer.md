# 15 · `StringBuilder` and `StringBuffer`

> **Run the code for this lesson**
> ```bash
> java Java/04-arrays-and-strings/15-stringbuilder-and-stringbuffer.java
> ```

Lesson 14 established that `String` is immutable. That is the right default — but it makes
building a string piece by piece expensive. `StringBuilder` is the mutable answer.

---

## 1. The problem

```java
String result = "";
for (int i = 0; i < 100000; i++) {
    result += i;
}
```

Because strings are immutable, `result += i` cannot append. It must:

1. Allocate a **new** string large enough for the old contents plus the new piece.
2. **Copy** every character of the old string into it.
3. Append the new piece.
4. Leave the old string as garbage.

At iteration `n`, the copy costs `n` characters. Total work is
`1 + 2 + 3 + … + n`, which is **n²/2** — quadratic. Doubling the loop count
**quadruples** the time.

`StringBuilder` keeps one growable `byte[]` and writes into it, so appending is amortised
**O(1)** and the whole loop is **O(n)**.

The difference is not academic. At 100,000 iterations it is typically the difference
between a few milliseconds and several seconds.

---

## 2. `StringBuilder` basics

```java
StringBuilder sb = new StringBuilder();
sb.append("Hello");
sb.append(", ");
sb.append("world");
sb.append('!');
sb.append(42);
sb.append(3.14);
sb.append(true);

String result = sb.toString();     // "Hello, world!423.14true"
```

`append` is overloaded for every primitive, `char[]`, `String`, `CharSequence` and
`Object` — so you can append anything without converting first. Appending `null` gives the
literal text `"null"`.

### It returns `this`, so calls chain

```java
String result = new StringBuilder()
        .append("Hello")
        .append(", ")
        .append("world")
        .toString();
```

Every mutating method returns the same builder. This is the **fluent interface** pattern,
and it is why the chained form reads well.

---

## 3. The full API

### Adding

```java
sb.append(x)                      // add at the end
sb.insert(index, x)               // add at a position
```

### Removing

```java
sb.delete(start, end)             // remove [start, end) — end exclusive
sb.deleteCharAt(index)            // remove one character
sb.setLength(0)                   // CLEAR the builder, keeping its capacity
```

### Modifying

```java
sb.replace(start, end, "text")    // replace a range
sb.setCharAt(index, 'x')          // overwrite one character (returns void)
sb.reverse()                      // reverse in place
```

### Reading

```java
sb.charAt(index)
sb.indexOf("x")
sb.length()                       // a METHOD, like String
sb.capacity()                     // the buffer size — usually > length
sb.substring(start, end)          // returns a String
sb.isEmpty()                      //                              (Java 15+)
sb.toString()
```

> **`String` has no `reverse()`.** `new StringBuilder(s).reverse().toString()` is the
> idiomatic way, and it is the reason `StringBuilder` shows up in string-manipulation
> puzzles so often.

---

## 4. Capacity and growth

A `StringBuilder` wraps an array with spare room:

```java
new StringBuilder()             // capacity 16
new StringBuilder(100)          // capacity 100
new StringBuilder("hello")      // capacity 5 + 16 = 21
```

When the buffer fills, it grows to `oldCapacity * 2 + 2` and copies. That doubling is what
makes appending **amortised O(1)**: growth happens logarithmically often, and each element
is copied a constant number of times on average.

### Presize when you know the size

```java
StringBuilder sb = new StringBuilder(expectedLength);
```

This avoids every intermediate reallocation and copy. For a builder that will hold a
megabyte of text it is a genuine win; for a handful of appends it does not matter.

### `setLength(0)` vs `new StringBuilder()`

```java
sb.setLength(0);                // reuses the existing buffer — no allocation
sb = new StringBuilder();       // allocates a new one
```

In a loop that builds many strings, `setLength(0)` avoids repeated allocation.

---

## 5. `StringBuilder` vs `StringBuffer`

They have **identical APIs**. The only difference:

| | `StringBuilder` | `StringBuffer` |
| --- | --- | --- |
| Introduced | Java 5 | Java 1.0 |
| Thread-safe | **No** | Yes — every method is `synchronized` |
| Speed | **Faster** | Slower (lock acquire/release per call) |
| Use it | **Always, by default** | Practically never |

`StringBuffer` came first, so every method was made `synchronized` "just in case".
Java 5 added `StringBuilder` as the unsynchronised version, because the overwhelming
majority of string building happens inside one method on one thread, where the locking is
pure waste.

**When would you actually need `StringBuffer`?** Only if multiple threads append to the
*same* builder instance — which is a strange design. The normal answer is to give each
thread its own `StringBuilder`, or use a proper concurrent structure. In practice you can
treat `StringBuffer` as legacy.

Note that even `StringBuffer` is only *method-level* safe. A sequence of calls is still
not atomic, so it rarely solves a real concurrency problem anyway.

---

## 6. When `+` is fine

Do **not** conclude that `+` is always wrong. The compiler is smarter than that.

```java
String s = "Hello, " + name + "!";
```

`javac` compiles a single-expression concatenation into an efficient operation — historically
a `StringBuilder`, and since Java 9 an `invokedynamic` call to `StringConcatFactory`, which
the JIT can optimise further. Writing this with an explicit `StringBuilder` makes the code
worse for **zero** gain.

**The rule is about loops, not about `+`:**

| Situation | Use |
| --- | --- |
| A fixed number of pieces in one expression | `+` |
| Concatenation inside a **loop** | `StringBuilder` |
| Building from a collection | `String.join` or `Collectors.joining` |
| A template with formatting | `String.format` or `"...".formatted(...)` |

```java
// Fine — one expression
String message = "User " + id + " logged in at " + time;

// Wrong — repeated reallocation
for (String part : parts) { result += part; }

// Right
String result = String.join("", parts);
```

---

## 7. `String.join` and `Collectors.joining`

For assembling a collection, these beat a manual builder because they handle the separator
logic for you:

```java
String.join(", ", List.of("a", "b", "c"));           // "a, b, c"

list.stream()
    .map(Object::toString)
    .collect(Collectors.joining(", ", "[", "]"));    // "[a, b, c]"
```

`Collectors.joining` takes a separator, a prefix and a suffix — which removes the classic
"trailing comma" bug entirely:

```java
// The bug this replaces
StringBuilder sb = new StringBuilder();
for (String s : list) {
    sb.append(s).append(", ");
}
sb.setLength(sb.length() - 2);    // fragile: breaks on an empty list
```

---

## 8. `StringJoiner`

Between the two sits `StringJoiner` (Java 8), which is what `Collectors.joining` uses
internally:

```java
StringJoiner joiner = new StringJoiner(", ", "[", "]");
joiner.add("a").add("b").add("c");
joiner.toString();                    // "[a, b, c]"

joiner.setEmptyValue("(none)");       // what to return when nothing was added
```

Use it when you need incremental building *and* separators, and a stream does not fit.

---

## 9. Summary

- `String` is immutable, so `+=` in a loop is **O(n²)**. `StringBuilder` makes it **O(n)**.
- Every `StringBuilder` mutator returns `this`, so calls chain fluently.
- `setLength(0)` clears the builder while **keeping its buffer** — cheaper than a new one.
- Capacity starts at 16 (or `initialString.length() + 16`) and grows to `2n + 2`. Presize
  when you know the final size.
- `StringBuffer` is the synchronised, slower, legacy version. **Default to
  `StringBuilder`.**
- A single `+` expression is **fine** — the compiler optimises it. The rule is about
  **loops**.
- Use `String.join` or `Collectors.joining` for collections; they remove the trailing-
  separator bug.
- `String` has no `reverse()`; use `new StringBuilder(s).reverse().toString()`.

---

**Previous:** [14 — Strings](14-strings.md) ·
**Next:** [16 — The `Arrays` utility class](16-arrays-utility-class.md)
