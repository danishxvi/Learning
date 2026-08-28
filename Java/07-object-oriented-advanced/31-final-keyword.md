# 31 · The `final` Keyword

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/31-final-keyword.java
> ```

`final` means "cannot be changed after initialisation" — but *what* cannot be changed
depends entirely on where you write it. It does three quite different jobs.

---

## 1. The three uses

| Where | Meaning |
| --- | --- |
| `final` **variable** | Cannot be **reassigned** after its first assignment |
| `final` **method** | Cannot be **overridden** by a subclass |
| `final` **class** | Cannot be **extended** at all |

The single most important thing to understand is what `final` on a variable does *not* do.

---

## 2. `final` prevents reassignment, never mutation

```java
final List<String> names = new ArrayList<>();

names.add("Danish");        // FINE — mutating the object
names.add("Aisha");         // FINE
names.clear();              // FINE

names = new ArrayList<>();  // ERROR: cannot assign a value to final variable
```

> **`final` locks the *reference*, not the *object*.**

This is the single most common misunderstanding about `final`, and it produces the
"immutable constant" that is not:

```java
public static final List<String> ALLOWED = new ArrayList<>();   // a mutable global!
ALLOWED.add("anything");                                        // legal from anywhere
```

For a genuinely immutable constant:

```java
public static final List<String> ALLOWED = List.of("a", "b");   // truly unmodifiable
```

For primitives and `String` (which is immutable anyway), `final` *does* give you a real
constant — because there is nothing to mutate.

---

## 3. `final` variables in detail

### Local variables

```java
final int max = 100;
max = 200;                  // ERROR
```

### Fields

A `final` field must be assigned **exactly once**, either at declaration or in **every**
constructor:

```java
class Point {
    private final int x;
    private final int y = 0;      // at declaration

    Point(int x) {
        this.x = x;               // in the constructor — must happen on every path
    }
}
```

The compiler enforces "definitely assigned exactly once". Assign it twice, or leave a path
where it is never assigned, and you get an error.

A `final` field with **no initialiser** is a *blank final*, and it is how you make an
immutable object whose values come from the constructor.

### Method parameters

```java
void process(final String input) {
    input = "changed";          // ERROR
}
```

This prevents reassigning the parameter inside the method. It is stylistic — some teams
require it, most do not. It has no effect on the caller (Java is pass-by-value, lesson 17).

### `static final` — constants

```java
public static final int MAX_RETRIES = 3;
```

By convention, `static final` constants are `SCREAMING_SNAKE_CASE`.

A `static final` initialised with a **compile-time constant expression** is *inlined* by
the compiler — the value is copied into every calling class's bytecode. That has a real
consequence:

> If you change `public static final int MAX = 3;` to `4` and recompile only that class,
> classes compiled against the old value **still use 3** until they are recompiled.

This is a genuine deployment hazard with library constants. It does not apply to
non-constant expressions like `static final int MAX = computeMax();`.

---

## 4. Effectively final (Java 8+)

A variable that is never reassigned is **effectively final** even without the keyword.
This matters because lambdas and anonymous classes may only capture effectively-final
locals:

```java
int count = 0;
Runnable r = () -> System.out.println(count);   // OK — count is effectively final

int total = 0;
Runnable bad = () -> System.out.println(total);
total = 5;                                      // now `total` is NOT effectively final
                                                // → the lambda no longer compiles
```

### Why the restriction exists

A lambda captures the **value** of a local, not the variable — because the local lives on
the stack and the lambda may outlive the method. Allowing reassignment would mean two
copies silently diverging.

Instance and static **fields** have no such restriction: they live on the heap, so the
lambda captures a reference to the object and always sees the current value. That is the
standard workaround when you genuinely need mutable capture — that, or a single-element
array, or an `AtomicInteger`:

```java
int[] counter = {0};
list.forEach(item -> counter[0]++);      // legal: the ARRAY reference is effectively final
```

---

## 5. `final` methods

```java
class Base {
    final void criticalStep() { ... }    // cannot be overridden
}
```

Use it when overriding would break a guarantee:

- A **template method** whose sequence must not change (lesson 28).
- A method called from a constructor — `final` makes that safe, because there is no
  overridden version to dispatch to (lesson 22).
- A security or invariant check.

`private` methods are implicitly final (they are not visible to subclasses, so they cannot
be overridden). `static` methods cannot be overridden either — they are *hidden*
(lesson 25).

---

## 6. `final` classes

```java
public final class String { ... }         // in the JDK
public final class Integer { ... }
```

Nothing can extend a `final` class. Use it when:

- The class is **immutable** and subclassing could break that guarantee.
- The class was not designed for inheritance and you do not want to support it forever.
- A security-sensitive class must not be subverted by a subclass.

`String` is `final` for exactly these reasons: a mutable `String` subclass would break the
string pool, break `HashMap` keys, and break every security check that validates a filename
or URL before using it.

### The trade-off

`final` classes cannot be mocked by frameworks that work through subclassing (older
Mockito, older EasyMock). Modern Mockito can mock them via an inline agent, so this is much
less of an issue than it was — but it is why some teams avoid `final` on classes.

The counter-argument, from *Effective Java*: **"design and document for inheritance, or
else prohibit it."** A class that is neither designed for extension nor `final` is a
hazard, because subclasses will depend on internals you never intended to freeze
(lesson 26's fragile base class problem).

---

## 7. `final` and performance

The old advice was "`final` helps the JIT inline". **This is largely obsolete.** Modern
JITs perform *class hierarchy analysis*: if only one implementation is loaded, they inline
it and de-optimise later if another appears.

`final` still has narrow performance relevance:

- `static final` compile-time constants are **inlined by javac**, not the JIT.
- `final` fields have **memory-model guarantees**: values assigned in a constructor are
  guaranteed visible to other threads once the constructor completes, without
  synchronisation. This is a genuine and important concurrency property (lesson 63) and is
  a large part of why immutable objects are thread-safe.

**Use `final` for design and clarity, not for speed.**

---

## 8. Style: how much `final` to write?

Two defensible positions:

**Liberal** — `final` on everything that never changes: locals, parameters, fields, classes.
Makes intent explicit and prevents accidental reassignment.

**Minimal** — `final` on fields and constants only. Argues that `final` on every local is
visual noise, and effectively-final already gives you the lambda benefit.

**A reasonable middle ground, and the one this repository uses:**

| Thing | `final`? |
| --- | --- |
| Fields | **Yes**, wherever possible — it is the foundation of immutability |
| Constants | **Yes**, always `static final` |
| Classes not designed for inheritance | **Yes** |
| Methods called from constructors | **Yes** |
| Local variables | Only when it clarifies |
| Parameters | Usually not |

The important one is the first. **Default your fields to `final`** and make them mutable
only when something genuinely has to change. That single habit gets you most of the way to
lesson 38.

---

## 9. Summary

- `final` **variable** = no reassignment; **method** = no overriding; **class** = no
  extending.
- **`final` locks the reference, not the object.** A `final List` can still be modified —
  use `List.of(...)` for a real constant.
- A `final` field must be definitely assigned **exactly once**, at declaration or in every
  constructor. A blank final is how immutable objects get constructor-supplied values.
- **Effectively final** (Java 8+) lets lambdas capture locals that are never reassigned.
  Fields have no such restriction because they live on the heap.
- `private` methods are implicitly final; `static` methods are hidden, not overridden.
- `String` and the wrappers are `final` to protect immutability, the string pool, and
  security checks.
- `static final` **compile-time constants are inlined by javac** — changing one requires
  recompiling every dependent class.
- `final` fields carry **memory-model guarantees** that make immutable objects safely
  publishable across threads. Otherwise, use `final` for design, not performance.
- **Default your fields to `final`.**

---

**Previous:** [30 — Packages and access modifiers](30-packages-and-access-modifiers.md) ·
**Next:** [32 — The `Object` class](32-object-class-methods.md)
