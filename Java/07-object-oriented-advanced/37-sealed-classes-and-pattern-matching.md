# 37 · Sealed Classes and Pattern Matching

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/37-sealed-classes-and-pattern-matching.java
> ```

Sealed types (Java 17) let you say **"these are the only subtypes there will ever be."**
Combined with records and pattern matching, they give Java a genuine *algebraic data type*
— and change how you model data.

---

## 1. `sealed` and `permits`

```java
sealed interface Shape permits Circle, Rectangle, Triangle { }

record Circle(double radius) implements Shape { }
record Rectangle(double w, double h) implements Shape { }
record Triangle(double base, double height) implements Shape { }
```

`permits` names the **complete, closed** list of direct subtypes. Nobody — not even code in
your own project — can add a fourth.

### The rules

Every permitted subtype must be exactly one of:

| Modifier | Meaning |
| --- | --- |
| `final` | No further extension. Records are implicitly final. |
| `sealed` | Continues the closed hierarchy with its own `permits` |
| `non-sealed` | **Deliberately reopens** the hierarchy at that branch |

```java
sealed interface Shape permits Circle, Polygon { }
record Circle(double r) implements Shape { }                    // final
sealed interface Polygon extends Shape permits Square { }       // sealed
non-sealed interface Decorated extends Shape { }                // reopened
```

`non-sealed` is the only hyphenated keyword in Java, and it exists so you can close *most*
of a hierarchy while leaving one branch extensible.

Other rules:

- Permitted subtypes must be in the **same module**, or the **same package** if unnamed.
- `permits` may be **omitted** if all subtypes are in the same file — the compiler infers
  them.

---

## 2. Why this matters: exhaustiveness

This is the whole point.

```java
double area(Shape shape) {
    return switch (shape) {
        case Circle c    -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.w() * r.h();
        case Triangle t  -> 0.5 * t.base() * t.height();
        // NO default needed — the compiler knows the list is complete
    };
}
```

Add a fourth shape and **every such `switch` stops compiling** until you handle it. The
compiler hands you the list of places to update.

Compare with an open hierarchy: you would need a `default`, which silently swallows the new
case and produces a wrong answer at runtime instead of an error at compile time.

> **Omit `default` on a sealed switch.** Adding one throws away the entire benefit.

---

## 3. The two ways to model data

This is the design decision sealed types force you to make consciously.

### Polymorphism — open types, closed operations

```java
interface Shape { double area(); }        // each subtype implements it
```

- **Easy to add a type**: write a new class. Nothing else changes.
- **Hard to add an operation**: `perimeter()` means editing *every* subclass.

### Sealed + switch — closed types, open operations

```java
sealed interface Shape permits Circle, Rectangle { }
double area(Shape s)      { return switch (s) { ... }; }
double perimeter(Shape s) { return switch (s) { ... }; }
```

- **Easy to add an operation**: one new method, in one place.
- **Hard to add a type**: every `switch` must be updated — but the compiler tells you
  exactly where.

This is the **expression problem**, and neither answer is universally right:

| Your situation | Choose |
| --- | --- |
| Types change often, operations rarely | **Polymorphism** |
| Operations change often, types rarely | **Sealed + switch** |
| Operations do not belong on the type | **Sealed + switch** |
| Types come from outside your control | **Polymorphism** |

A JSON node, an arithmetic expression, a protocol message, a result type — these have a
*fixed* set of shapes and a *growing* set of things you do to them. Sealed types fit
perfectly. A plugin system does not.

---

## 4. Pattern matching, in full

### Type patterns

```java
if (o instanceof String s) { ... }              // Java 16
case Integer i -> ...                            // Java 21
```

### Record patterns — destructuring

```java
case Circle(double radius) -> ...
case Line(Point(var x1, var y1), Point(var x2, var y2)) -> ...   // nested
```

`var` is allowed inside patterns and is usually the readable choice.

### Guarded patterns — `when`

```java
case Rectangle(double w, double h) when w == h -> "a square";
case Rectangle(double w, double h)             -> "a rectangle";
```

Order matters: the **first matching case wins**, so the more specific guard must come first.
A guarded case never counts toward exhaustiveness — the compiler still requires an
unguarded case for that type.

### `null` handling

Historically `switch` threw `NullPointerException` on a `null` selector. Since Java 21 you
may write:

```java
case null -> "nothing";
case null, default -> "nothing or unknown";
```

If you do **not** write `case null`, the old NPE behaviour is preserved for compatibility.

---

## 5. The classic use: a result type

```java
sealed interface Result<T> permits Success, Failure { }
record Success<T>(T value) implements Result<T> { }
record Failure<T>(String error) implements Result<T> { }

String render(Result<User> result) {
    return switch (result) {
        case Success<User>(User user) -> "Welcome, " + user.name();
        case Failure<User>(String error) -> "Failed: " + error;
    };
}
```

The compiler **forces** the caller to handle failure. There is no way to forget, no `null`
to check, and no exception to catch. This is how Rust's `Result` and Kotlin's sealed classes
work, and it is now expressible in Java.

Other natural fits: JSON trees, arithmetic expression evaluators, state machines, protocol
messages, and the states of a UI.

---

## 6. Sealed classes vs enums

Both model a closed set. They differ in what varies:

| | Enum | Sealed hierarchy |
| --- | --- | --- |
| Instances | A **fixed number** of singletons | **Unlimited** instances |
| Each variant carries | The same fields | **Different** fields |
| Best for | Fixed constants — days, statuses | Fixed *shapes* with differing data |

`Status.ACTIVE` is one value. `Circle` is a *kind* of value with infinitely many instances.
When your variants need different data, you want a sealed hierarchy; when they are just
named constants, you want an enum.

---

## 7. Practical notes

- **Sealed hierarchies are an API commitment.** Adding a permitted subtype is a
  source-compatible but *behaviourally* breaking change for every downstream `switch`. That
  is by design, and it is why you seal deliberately.
- **Records + sealed is the natural pairing.** Records are implicitly final, so they satisfy
  the permitted-subtype rule with no extra keyword.
- **`permits` can be omitted** when everything is in one file — convenient for small
  hierarchies.
- **Reflection can enumerate them**: `Shape.class.getPermittedSubclasses()`.
- **Exhaustiveness applies to switch *expressions*** and to switch *statements* over sealed
  types since Java 21. A pre-21 switch statement gets no such check.

---

## 8. Summary

- `sealed ... permits` declares the **complete list** of direct subtypes. Every one must be
  `final`, `sealed`, or `non-sealed`.
- `non-sealed` deliberately reopens one branch; it is Java's only hyphenated keyword.
- The payoff is **exhaustiveness**: a `switch` needs no `default`, and adding a subtype
  breaks compilation everywhere that must change. **Omit `default`** or you lose this.
- Sealed types make the **expression problem** an explicit choice: polymorphism is easy to
  extend with new *types*; sealed + switch is easy to extend with new *operations*.
- **Record patterns** destructure, and nest. `when` guards add conditions, and the first
  match wins.
- Since Java 21, `case null` is writable; without it, the historical NPE is preserved.
- **Enums** fix the number of *instances*; **sealed hierarchies** fix the number of *shapes*
  while allowing unlimited instances with differing data.
- A `Result`/`Success`/`Failure` hierarchy makes handling failure **compulsory** at compile
  time.

---

**Previous:** [36 — Records](36-records.md) ·
**Next:** [38 — Immutability and defensive copying](38-immutability-and-defensive-copying.md)
