# 12 · Arrays — Memory Layout, Creation and Iteration

> **Run the code for this lesson**
> ```bash
> java Java/04-arrays-and-strings/12-arrays.java
> ```

An array is Java's only built-in data structure. Everything else — `ArrayList`,
`HashMap`, `StringBuilder` — is a class built *on top of* arrays. Understanding them
properly explains a lot of later behaviour.

---

## 1. What an array actually is

> An array is a **fixed-size**, **contiguous** block of memory holding elements of a
> **single type**, accessed by a **zero-based index**.

Four properties, and every one of them matters:

| Property | Consequence |
| --- | --- |
| **Fixed size** | You choose the length at creation and can never change it |
| **Contiguous** | Element `i` is at `base + i × elementSize`, so access is **O(1)** |
| **Single type** | `int[]` holds only `int`; the compiler enforces it |
| **Zero-based** | The first element is `[0]`, the last is `[length - 1]` |

That contiguity is why `array[500000]` is exactly as fast as `array[0]` — the JVM
computes an address rather than walking a chain. It is also why arrays cannot grow: the
memory after them belongs to something else.

**Arrays are objects.** `int[]` is a reference type even though `int` is not. It lives on
the heap, it can be `null`, and it inherits from `Object`.

---

## 2. Declaring and creating

```java
// Declaration only — no array exists yet, the variable is null
int[] numbers;

// Creation with a size — every element gets the type's default value
int[] numbers = new int[5];        // {0, 0, 0, 0, 0}

// Creation with values
int[] numbers = {1, 2, 3, 4, 5};                // shorthand
int[] numbers = new int[]{1, 2, 3, 4, 5};       // explicit form
```

### The shorthand only works in a declaration

```java
int[] a = {1, 2, 3};        // fine
int[] b;
b = {1, 2, 3};              // ERROR
b = new int[]{1, 2, 3};     // fine — the explicit form works anywhere
```

This is why passing a literal array to a method needs the long form:
`process(new int[]{1, 2, 3})`.

### Where the brackets go

```java
int[] preferred;      // Java style — the type is "array of int"
int legacy[];         // C style — legal, but do not
int[] a, b;           // BOTH are int[]
int a[], b;           // a is int[], b is a plain int  (!!)
```

Always put the brackets on the **type**. The last line above is a genuine trap.

### Default values

Every element is initialised, unlike local variables:

| Element type | Default |
| --- | --- |
| `byte`, `short`, `int`, `long` | `0` |
| `float`, `double` | `0.0` |
| `char` | the null character (code 0) |
| `boolean` | `false` |
| Any reference type | `null` |

An `String[5]` therefore contains five `null`s — not five empty strings. Dereferencing
one is an immediate `NullPointerException`, and this is a very common bug.

---

## 3. `length` is a field, not a method

```java
array.length      // arrays — a FIELD, no parentheses
string.length()   // String — a METHOD
list.size()       // collections — a METHOD
```

Three different spellings for the same idea is a genuine Java wart. There is no logic to
memorise; you simply learn it. `array.length()` and `string.length` are both compile
errors.

`length` is also `final` — you cannot assign to it to resize the array.

---

## 4. Bounds checking

```java
int[] a = new int[5];
a[5] = 10;      // ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 5
a[-1] = 10;     // ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 5
```

Java checks **every** array access at runtime. This is a real safety guarantee: C would
happily write past the end and corrupt whatever memory was there, which is the mechanism
behind decades of buffer-overflow security vulnerabilities.

The check costs a comparison per access. The JIT eliminates most of them when it can
prove the index is in range — which is one reason a simple `for (int i = 0; i < a.length; i++)`
often runs faster than a cleverer version.

**The last valid index is `length - 1`.** Writing `i <= array.length` is the single most
common array bug.

---

## 5. Copying — and the reference trap

```java
int[] original = {1, 2, 3};
int[] copy = original;      // NOT a copy! Both names refer to the SAME array
copy[0] = 99;
System.out.println(original[0]);   // 99
```

Assignment copies the **reference**, not the contents. To actually copy:

```java
int[] c1 = Arrays.copyOf(original, original.length);       // preferred
int[] c2 = Arrays.copyOfRange(original, 1, 3);             // elements 1 and 2
int[] c3 = original.clone();                               // fine for 1D
int[] c4 = new int[original.length];
System.arraycopy(original, 0, c4, 0, original.length);     // fastest, ugliest
```

`Arrays.copyOf` can also resize:

```java
int[] bigger = Arrays.copyOf(original, 5);    // {1, 2, 3, 0, 0}
int[] smaller = Arrays.copyOf(original, 2);   // {1, 2}
```

This is exactly how `ArrayList` grows: allocate a bigger array, copy, discard the old one.

### Shallow vs deep copy

For an array of **objects**, every copy method above is **shallow** — it copies the
references, so both arrays point at the same objects:

```java
StringBuilder[] original = { new StringBuilder("a") };
StringBuilder[] copy = original.clone();
copy[0].append("!");
System.out.println(original[0]);   // "a!" — the same object
```

A deep copy requires copying each element yourself. Lesson 38 covers this properly.

---

## 6. Comparing arrays

```java
int[] a = {1, 2, 3};
int[] b = {1, 2, 3};

a == b              // false — different objects
a.equals(b)         // false — Object.equals is identity; arrays do not override it
Arrays.equals(a, b) // TRUE — this is the one you want
```

Arrays inherit `equals()` from `Object` and do not override it, so `a.equals(b)` is
identical to `a == b`. Always use `Arrays.equals` (or `Arrays.deepEquals` for nested
arrays).

The same applies to printing:

```java
System.out.println(a);                    // [I@1b6d3586   — type tag and hash code
System.out.println(Arrays.toString(a));   // [1, 2, 3]
System.out.println(Arrays.deepToString(nested));   // for 2D and deeper
```

`[I@1b6d3586` decodes as: `[` = array, `I` = of `int`, then `@` and the identity hash in
hex. Seeing that in your output always means you forgot `Arrays.toString`.

---

## 7. The `Arrays` utility class

`java.util.Arrays` holds the operations arrays lack as objects. Lesson 16 covers it fully;
these are the ones you need immediately:

```java
Arrays.toString(a)              // readable printing
Arrays.sort(a)                  // sorts in place
Arrays.binarySearch(a, key)     // O(log n) — array MUST be sorted first
Arrays.fill(a, value)           // set every element
Arrays.copyOf(a, newLength)     // copy, resizing
Arrays.equals(a, b)             // element-wise comparison
Arrays.stream(a)                // turn it into a Stream
```

> `binarySearch` on an **unsorted** array returns a meaningless value rather than
> throwing. It is one of the quieter bugs in the standard library.

---

## 8. Arrays vs `ArrayList`

| | Array | `ArrayList` |
| --- | --- | --- |
| Size | Fixed at creation | Grows automatically |
| Holds primitives | **Yes** (`int[]`) | No — only objects (`List<Integer>`) |
| Length | `.length` field | `.size()` method |
| Access element | `a[i]` | `list.get(i)` |
| Type safety | Runtime (see below) | Compile time (generics) |
| Memory | Compact | ~4× more for boxed primitives |
| Add / remove | Not supported | `add()`, `remove()` |

**Use an array when** the size is genuinely fixed and known, you are storing primitives in
bulk and memory matters, or you are working with an API that requires one.

**Use `ArrayList` for everything else.** In practice that is the large majority of code.

### Array covariance — a hole in the type system

```java
Object[] objects = new String[3];    // legal! arrays are COVARIANT
objects[0] = 42;                     // compiles, but throws ArrayStoreException
```

`String[]` is treated as a subtype of `Object[]`, which lets you write code the compiler
cannot check. The JVM re-checks on every store, so it fails at runtime instead. Generics
were deliberately designed **not** to work this way — `List<String>` is *not* a
`List<Object>` — which is why generic code catches these errors at compile time. Lesson 43
explains the trade-off.

---

## 9. Summary

- An array is a fixed-size, contiguous, single-type, zero-indexed **object** on the heap.
- Contiguity gives **O(1)** access and makes resizing impossible.
- Elements get default values (`0`, `false`, `null`); local variables do not.
- `array.length` is a **field**; `String.length()` and `list.size()` are methods.
- Every access is bounds-checked — the last valid index is `length - 1`.
- Assignment copies the **reference**. Use `Arrays.copyOf`, `clone()`, or
  `System.arraycopy` to copy contents, and remember all of them are **shallow**.
- `a.equals(b)` on arrays is identity. Use `Arrays.equals`, and `Arrays.toString` to print.
- Arrays are **covariant**, so `Object[] o = new String[3]` compiles and can throw
  `ArrayStoreException`. Generics deliberately are not.
- Prefer `ArrayList` unless the size is fixed or you need primitive storage.

---

**Previous:** [11 — break, continue and labels](../03-control-flow/11-break-continue-and-labels.md) ·
**Next:** [13 — Multidimensional arrays](13-multidimensional-arrays.md)
