# 42 · Wrapper Classes, Autoboxing and Caching

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/42-wrapper-classes-and-autoboxing.java
> ```

Collections and generics cannot hold primitives. Wrapper classes bridge that gap — and the
automatic conversion between the two hides several genuinely expensive traps.

---

## 1. The eight wrappers

| Primitive | Wrapper | Cached range |
| --- | --- | --- |
| `byte` | `Byte` | all 256 values |
| `short` | `Short` | −128…127 |
| `int` | `Integer` | −128…127 (upper bound configurable) |
| `long` | `Long` | −128…127 |
| `float` | `Float` | **none** |
| `double` | `Double` | **none** |
| `char` | `Character` | 0…127 |
| `boolean` | `Boolean` | both values |

Every wrapper is **immutable** and **`final`**. `Byte`, `Short`, `Integer`, `Long`,
`Float` and `Double` all extend `Number`; `Character` and `Boolean` do not.

### Why they exist

```java
List<int> numbers;          // does not compile — generics need a reference type
List<Integer> numbers;      // fine
```

Generics work by erasure (lesson 43) and erase to `Object`, which a primitive can never be.
Wrappers also give you `null` for "absent", plus useful statics: `Integer.parseInt`,
`Integer.MAX_VALUE`, `Integer.toBinaryString`.

---

## 2. Autoboxing and unboxing

```java
Integer boxed = 42;         // autoboxing:   Integer.valueOf(42)
int unboxed = boxed;        // unboxing:     boxed.intValue()
```

Added in Java 5. The compiler inserts the conversion, which is convenient and is precisely
why the traps below are easy to miss.

### The `null` unboxing trap

```java
Map<String, Integer> counts = new HashMap<>();
int count = counts.get("missing");     // NullPointerException
```

`get` returned `null`, and unboxing called `null.intValue()`. There is no visible
dereference in that line, which makes it a genuinely confusing NPE.

```java
Integer value = null;
if (value == 0) { }                    // NullPointerException — value is unboxed to compare
```

**Guard, or use `getOrDefault`:**

```java
int count = counts.getOrDefault("missing", 0);
```

### The ternary unboxing trap

```java
Integer result = condition ? 1 : nullInteger();    // NPE when condition is false
```

The `int` literal forces the whole expression to type `int`, which unboxes the `null`
branch (lessons 05 and 08). Keep both branches the same type.

---

## 3. The `Integer` cache

This is the trap that reaches production.

```java
Integer a = 127, b = 127;
a == b;              // true

Integer c = 128, d = 128;
c == d;              // false
```

`Integer.valueOf` returns a **cached instance** for −128…127 and a new object outside that
range. So `==` compares identity and appears to work for small numbers.

Code passes its tests with small values and fails in production with large ones — a bug
that survives review because the test data is unrepresentative.

**Always use `.equals()` on wrappers.** Or better, use primitives where you can.

> `new Integer(5)` always created a new object and bypassed the cache. It is **deprecated
> for removal** — use `Integer.valueOf(5)`, or just `5`.

The upper bound is configurable with `-XX:AutoBoxCacheMax=<n>`, which is worth knowing
mainly because it means you cannot rely on 127 either.

---

## 4. The `==` rules, exhaustively

```java
Integer a = 127, b = 127;
int primitive = 127;

a == b            // true  — both cached
a == primitive    // true  — a is UNBOXED, so this compares values
a.equals(b)       // true  — content
```

> **If either operand is a primitive, `==` unboxes and compares values.** If both are
> wrappers, it compares references.

That asymmetry is why `a == primitive` behaves differently from `a == b`, and it is worth
knowing precisely.

### Mixing types breaks `equals`

```java
Integer i = 1;
Long l = 1L;
i.equals(l);      // FALSE — Integer.equals checks the TYPE first
i == l;           // does not compile
```

`Integer.equals` returns `false` for anything that is not an `Integer`, whatever its value.

---

## 5. The performance cost

Boxing allocates. In a loop, that is measurable:

```java
Long sum = 0L;                     // WRONG — Long, not long
for (long i = 0; i < 1_000_000; i++) {
    sum += i;                      // unbox, add, box — a new Long every iteration
}
```

One character's difference from `long sum = 0L` and it allocates a million objects. This is
the classic accidental-boxing bug.

Memory cost is also real: an `int` is 4 bytes; an `Integer` is typically 16 bytes plus an
8-byte reference — roughly **6×**.

### Use the primitive-specialised types

```java
IntStream.range(0, n).sum()        // no boxing
Stream<Integer>                     // boxes every element

int[] array                         // no boxing
List<Integer>                       // boxes everything
```

`IntStream`, `LongStream`, `DoubleStream`, `IntPredicate`, `ToIntFunction`,
`OptionalInt` and `AtomicInteger` all exist to avoid boxing (lessons 52 and 54).

---

## 6. Useful wrapper statics

```java
Integer.parseInt("42")            // → int
Integer.valueOf("42")             // → Integer (cached)
Integer.MAX_VALUE / MIN_VALUE
Integer.toBinaryString(10)        // "1010"
Integer.toHexString(255)          // "ff"
Integer.compare(a, b)             // overflow-safe, unlike a - b
Integer.sum / max / min           // for method references
Integer.bitCount(7)               // 3

Character.isDigit / isLetter / isWhitespace / toUpperCase
Boolean.parseBoolean("true")      // anything else is false, no exception
Double.isNaN / isInfinite / compare
```

`Integer.compare(a, b)` matters: the old `a - b` trick **overflows** for large values and
silently returns the wrong sign.

---

## 7. Summary

- Eight wrappers, all **immutable** and **`final`**. Six extend `Number`.
- They exist because generics erase to `Object`, so `List<int>` is impossible.
- Autoboxing is a compiler insertion — convenient, and the source of every trap here.
- **Unboxing a `null` throws `NullPointerException`** with no visible dereference. Use
  `getOrDefault`, or guard.
- **`Integer` caches −128…127**, so `==` works for small values and fails for large ones.
  **Always use `.equals()`.**
- If either operand is a primitive, `==` **unboxes** and compares values; two wrappers
  compare references.
- `Integer.equals(Long)` is **`false`** regardless of value — `equals` checks the type.
- `Long sum = 0L` in a loop allocates one object per iteration. Use the primitive.
- Wrappers cost roughly **6×** the memory of a primitive.
- Prefer `IntStream`, `int[]`, `OptionalInt` and friends to avoid boxing.
- Use `Integer.compare(a, b)`, never `a - b` — the subtraction overflows.

---

**Previous:** [41 — try-with-resources](../08-exception-handling/41-try-with-resources-and-best-practices.md) ·
**Next:** [43 — Generics](43-generics.md)
