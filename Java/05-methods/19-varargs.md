# 19 · Varargs (Variable-Length Argument Lists)

> **Run the code for this lesson**
> ```bash
> java Java/05-methods/19-varargs.java
> ```

Varargs let a method accept any number of arguments. Added in Java 5, they are what make
`String.format`, `List.of` and `System.out.printf` possible. They are also a small pile of
sharp edges.

---

## 1. The syntax

```java
static int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) {     // `numbers` IS an int[]
        total += n;
    }
    return total;
}

sum();              // 0
sum(1);             // 1
sum(1, 2, 3);       // 6
sum(new int[]{1, 2, 3});   // 6 — you can pass an array directly
```

Three dots after the type. Inside the method, the parameter **is an array** — you can call
`.length`, index it, and pass it to anything expecting an array.

### What the compiler actually does

`sum(1, 2, 3)` is compiled into `sum(new int[]{1, 2, 3})`. Varargs is pure syntactic
sugar: **the compiler creates the array at the call site.** There is no special runtime
support.

That single fact explains every rule below.

---

## 2. The rules

**1. Only one varargs parameter per method.**

```java
void f(int... a, int... b)     // ERROR
```

There would be no way to decide where the first list ends.

**2. It must be the last parameter.**

```java
void f(int... a, String b)     // ERROR
void f(String b, int... a)     // fine
```

Same reason.

**3. Other parameters may precede it.**

```java
static void log(String level, String... messages)
log("INFO", "started", "connected");
```

This is the common and useful shape.

---

## 3. Varargs is an array, with all that implies

```java
static void f(String... items) {
    System.out.println(items.length);
    System.out.println(items.getClass());   // class [Ljava.lang.String;
    items[0] = "changed";                   // mutable!
}
```

Because it is a real array:

- It is **allocated on every call** — even `sum()` with no arguments creates an empty array
  (though the compiler and JIT often optimise the empty case).
- It can be **mutated**, and if the caller passed an existing array, the caller sees the change.
- It can be `null`.

### The `null` trap

```java
sum(null);        // NullPointerException inside the loop — `numbers` IS null
sum((int[]) null);   // same
sum(new int[0]);  // fine — empty
```

Passing a literal `null` sets the whole array reference to `null`, not "one null element".
Defensive methods should check:

```java
static int sum(int... numbers) {
    if (numbers == null) return 0;
    ...
}
```

For an *object* varargs, `f(null)` is genuinely ambiguous between "null array" and "array
containing one null", and the compiler warns about it.

---

## 4. Varargs is the *last* resort in overload resolution

From lesson 18: resolution tries **widening → boxing → varargs**. Varargs is phase 3, so
a non-varargs overload always wins:

```java
static void f(int x)      { System.out.println("int"); }
static void f(int... x)   { System.out.println("varargs"); }

f(5);        // "int" — the fixed-arity method wins
f(5, 6);     // "varargs" — nothing else matches
f();         // "varargs"
```

This is why you can safely *add* a varargs overload to an existing API: existing call
sites keep binding to the old method.

### Ambiguity between two varargs methods

```java
static void f(int... x)     { }
static void f(Integer... x) { }

f(1, 2);     // ERROR: ambiguous
```

Both need phase 3 and neither is more specific. Avoid overloading varargs methods at all.

---

## 5. The generic varargs problem — "heap pollution"

Generics are erased (lesson 43), so `T...` becomes `Object[]` at runtime. The array's
**real** element type is chosen at the *call site* — and when the caller is itself generic,
the compiler has nothing better to create than a plain `Object[]`.

Here is the canonical example. It contains no visible cast anywhere, and it throws
`ClassCastException`:

```java
static <T> T[] toArray(T... items) {
    return items;                    // THE MISTAKE: letting the array escape
}

static <T> T[] pickTwo(T a, T b) {
    return toArray(a, b);            // inside here, T is still unknown
}

String[] pair = pickTwo("a", "b");   // ClassCastException!
// class [Ljava.lang.Object; cannot be cast to class [Ljava.lang.String;
```

Step by step:

1. `pickTwo` calls `toArray(a, b)`.
2. Inside `pickTwo`, `T` is not yet known, so the compiler creates an **`Object[]`** for
   the varargs array.
3. `toArray` returns that `Object[]`.
4. `pickTwo` returns it as `T[]`, which the caller believes is a `String[]`.
5. The caller's *implicit* cast to `String[]` fails.

The heap now holds an object whose real type contradicts its declared type. That is
**heap pollution**.

**The rule that prevents all of it:** never let a generic varargs array escape the method.
Do not return it, and do not pass it to another method. Read it, and copy out what you
need.

Java warns on both the declaration and the call site. If your method genuinely is safe —
it only *reads* the array and never stores anything into it — say so:

```java
@SafeVarargs
static <T> List<T> listOf(T... items) { ... }
```

`@SafeVarargs` can only be applied to methods that cannot be overridden: `static`, `final`,
or `private` (Java 9+). That restriction exists because a subclass could otherwise override
the method unsafely.

`List.of`, `Arrays.asList` and `EnumSet.of` are all annotated this way in the JDK.

---

## 6. Performance

Each varargs call allocates an array. That is normally irrelevant, but it is measurable in
a hot loop called millions of times. This is exactly why the JDK writes things like:

```java
public static <E> List<E> of()                 { ... }   // 0 elements
public static <E> List<E> of(E e1)             { ... }   // 1
public static <E> List<E> of(E e1, E e2)       { ... }   // 2
// ... up to 10 ...
public static <E> List<E> of(E... elements)    { ... }   // 11+
```

Ten fixed-arity overloads to avoid array allocation in the common cases, with varargs as
the fallback. `EnumSet.of` and `Map.of` do the same.

**Do not copy this pattern yourself** unless profiling proves you need it. It is a
JDK-scale optimisation for code called billions of times.

---

## 7. Where you already use varargs

```java
String.format("%s is %d", name, age)
System.out.printf("%s%n", value)
List.of(1, 2, 3)
Set.of("a", "b")
Arrays.asList(1, 2, 3)
String.join(", ", "a", "b", "c")
EnumSet.of(Day.MON, Day.TUE)
Objects.hash(a, b, c)
```

`Objects.hash` is a good example of the trade-off: it is convenient, allocates an array
every call, and the JDK documents that `Integer.hashCode` style manual computation is
preferable inside a hot `hashCode()` implementation.

---

## 8. Passing an array to a varargs method

```java
int[] existing = {1, 2, 3};
sum(existing);          // works — passed directly, NOT wrapped
```

But watch the object case:

```java
Object[] array = {1, 2, 3};
System.out.printf("%s %s %s%n", array);      // spreads into 3 arguments
System.out.printf("%s%n", (Object) array);   // ONE argument: the array itself
```

The cast to `Object` forces the compiler to wrap rather than spread. This is the standard
trick when you need to pass an array *as a single argument*.

Related, and a classic puzzle:

```java
Integer[] boxed = {1, 2, 3};
System.out.println(Arrays.asList(boxed).size());   // 3 — spread

int[] primitives = {1, 2, 3};
System.out.println(Arrays.asList(primitives).size());   // 1 — wrapped!
```

`int[]` cannot spread into `T...` because generics cannot hold primitives, so the whole
array becomes a single element. Covered in lesson 16.

---

## 9. Summary

- `type... name` accepts any number of arguments; inside the method it **is an array**.
- The compiler creates the array at the **call site** — varargs is syntactic sugar.
- Only **one** varargs parameter, and it must be **last**.
- The array is real: it allocates, it is mutable, and it can be `null`. `f(null)` gives a
  null array, not an array containing null.
- Varargs is **phase 3** in overload resolution, so a fixed-arity overload always wins.
  Two competing varargs overloads are ambiguous.
- Generic varargs (`T...`) risks **heap pollution** because of erasure. Annotate genuinely
  safe methods with `@SafeVarargs` — allowed only on `static`, `final` or `private` methods.
- Each call allocates an array. The JDK writes fixed-arity overloads to avoid this in hot
  paths; you almost certainly should not.
- Cast to `(Object)` to pass an array as a **single** argument rather than spreading it.

---

**Previous:** [18 — Method overloading](18-method-overloading.md) ·
**Next:** [20 — Recursion and the call stack](20-recursion.md)
