# 35 · Enums

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/35-enums.java
> ```

An enum is a class with a **fixed, known set of instances**. Most people use them as
named constants and stop there — which misses almost everything they can do.

---

## 1. Why they exist

The alternative is *int constants*, and it is genuinely bad:

```java
public static final int STATUS_ACTIVE = 0;
public static final int STATUS_INACTIVE = 1;

void setStatus(int status) { ... }

setStatus(47);              // compiles. Nothing stops it.
setStatus(DAY_MONDAY);      // compiles — a completely unrelated constant
System.out.println(status); // prints "0". Good luck debugging that.
```

An enum fixes all of it:

```java
enum Status { ACTIVE, INACTIVE }

void setStatus(Status status) { ... }

setStatus(Status.ACTIVE);   // the ONLY thing that compiles
System.out.println(status); // prints "ACTIVE"
```

| Problem with `int` constants | Enum |
| --- | --- |
| Any `int` is accepted | **Type-safe** — only enum constants compile |
| Prints as a meaningless number | Prints its **name** |
| No namespace — `STATUS_` prefixes everywhere | Namespaced: `Status.ACTIVE` |
| Cannot add behaviour | **Can have fields, methods, constructors** |
| Compile-time inlined (lesson 31 hazard) | Real objects, resolved at runtime |
| No exhaustiveness checking | `switch` **exhaustiveness** is verified |

---

## 2. Enums are real classes

```java
enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    EARTH(5.976e+24, 6.37814e6);          // ← semicolon before other members

    private final double mass;
    private final double radius;

    Planet(double mass, double radius) {   // implicitly private
        this.mass = mass;
        this.radius = radius;
    }

    double surfaceGravity() {
        return 6.67300E-11 * mass / (radius * radius);
    }
}
```

- Constants come **first**, then a **semicolon**, then everything else.
- The constructor is implicitly `private` — you cannot write `new Planet(...)`.
- Enums extend `java.lang.Enum` implicitly, so they **cannot extend anything else**. They
  *can* implement interfaces.

Every enum constant is a `public static final` instance created once at class
initialisation.

---

## 3. The built-in methods

| Method | Returns |
| --- | --- |
| `values()` | An array of all constants, in declaration order |
| `valueOf(String)` | The constant with that exact name — throws if absent |
| `name()` | The exact declared name |
| `ordinal()` | The zero-based declaration position |
| `compareTo()` | Compares by `ordinal` |
| `toString()` | `name()` by default — overridable |

### `values()` returns a fresh copy every call

```java
for (int i = 0; i < 1_000_000; i++) {
    Status[] all = Status.values();     // a NEW array allocated each time
}
```

It must, because arrays are mutable and the JDK cannot hand out its internal one. In a hot
loop, cache it in a `private static final` array — this is a real allocation issue in
performance-sensitive code.

### Never persist `ordinal()`

```java
enum Status { ACTIVE, INACTIVE }         // ACTIVE=0, INACTIVE=1
enum Status { ACTIVE, PENDING, INACTIVE } // INACTIVE is now 2!
```

Storing `ordinal()` in a database or a file means inserting a constant silently
reinterprets every existing row. **Persist `name()`**, or an explicit code field you
control. `ordinal()` exists for `EnumMap`/`EnumSet` internals, not for you.

### `valueOf` throws

```java
Status.valueOf("ACTIVE")      // fine
Status.valueOf("active")      // IllegalArgumentException — case-sensitive
Status.valueOf("UNKNOWN")     // IllegalArgumentException
Status.valueOf(null)          // NullPointerException
```

For user input, write a lenient lookup that returns `Optional` rather than throwing.

---

## 4. Constant-specific behaviour

Each constant can override methods — effectively an anonymous subclass per constant:

```java
enum Operation {
    PLUS  { int apply(int a, int b) { return a + b; } },
    MINUS { int apply(int a, int b) { return a - b; } },
    TIMES { int apply(int a, int b) { return a * b; } };

    abstract int apply(int a, int b);
}

Operation.PLUS.apply(3, 4);     // 7
```

This is a **strictly better** alternative to a `switch` inside the enum: adding a constant
without implementing `apply` is a **compile error**, whereas a `switch` would silently fall
through to a default.

The modern alternative is a constructor field holding a lambda:

```java
enum Operation {
    PLUS((a, b) -> a + b),
    MINUS((a, b) -> a - b);

    private final IntBinaryOperator operation;
    Operation(IntBinaryOperator operation) { this.operation = operation; }
    int apply(int a, int b) { return operation.applyAsInt(a, b); }
}
```

Both are good. The lambda form is more compact; the constant-body form allows several
methods per constant.

---

## 5. `EnumMap` and `EnumSet`

These are specialised, dramatically faster collections that most people never use.

```java
Map<Day, String> schedule = new EnumMap<>(Day.class);
Set<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
```

**`EnumMap`** is backed by a plain **array indexed by `ordinal`**. No hashing, no
collisions, no boxing — a direct array access. It also iterates in **declaration order**,
for free.

**`EnumSet`** is a **bit vector**. An enum with ≤64 constants fits in a single `long`, so
`contains` is one bitwise AND, and union/intersection are single machine instructions.

```java
EnumSet.allOf(Day.class)
EnumSet.noneOf(Day.class)
EnumSet.range(Day.MONDAY, Day.FRIDAY)
EnumSet.complementOf(weekend)
```

> **If your key or element type is an enum, use `EnumMap`/`EnumSet`.** They are faster,
> smaller, and iterate in a sensible order. `HashMap` with an enum key works, but there is
> no reason to accept it.

---

## 6. Enums implementing interfaces

```java
interface Describable { String describe(); }

enum Priority implements Describable {
    LOW, HIGH;

    public String describe() { return "priority " + name(); }
}
```

An enum cannot extend a class (it already extends `Enum`) but may implement any number of
interfaces. This is how you make a fixed set of strategies share a contract with
non-enum implementations.

---

## 7. The singleton enum

```java
enum Configuration {
    INSTANCE;

    private final Map<String, String> settings = new HashMap<>();
    public String get(String key) { return settings.get(key); }
}
```

*Effective Java* calls this **the best way to implement a singleton**, because the JVM
guarantees:

- exactly **one** instance, created safely at class initialisation;
- **serialization** cannot create a second one (a real hole in classic singletons);
- **reflection** cannot construct one — `Constructor.newInstance` explicitly rejects enums.

The classic `private static Instance instance` singleton is vulnerable to all three.

---

## 8. Enums and `switch`

```java
switch (status) {
    case ACTIVE -> "running";       // NOT Status.ACTIVE — the type is inferred
    case INACTIVE -> "stopped";
}
```

Inside a `case` label you write the bare constant name.

**Omit `default` in a switch *expression* over an enum.** Then adding a constant becomes a
compile error at every switch that needs updating — which is exactly the reminder you want.
Adding a `default` silently swallows the new case. Lesson 09.

---

## 9. Common mistakes

| Mistake | Why it hurts |
| --- | --- |
| Persisting `ordinal()` | Reordering constants corrupts stored data |
| `values()` in a hot loop | Allocates an array every call |
| `HashMap<MyEnum, V>` | `EnumMap` is strictly better |
| `switch` with `default` over an enum | Hides missing cases when constants are added |
| Mutable state in an enum | Enum constants are global singletons — mutable state is a global variable |
| `valueOf` on user input | Throws instead of returning something handleable |

---

## 10. Summary

- An enum is a class with a **fixed set of instances** — type-safe, self-describing,
  namespaced, and capable of holding fields and behaviour.
- Constants first, then a **semicolon**, then fields, constructors and methods. The
  constructor is implicitly `private`.
- Enums implicitly extend `Enum`, so they **cannot extend a class** but **can implement
  interfaces**.
- `values()` allocates a **new array each call** — cache it in hot paths.
- **Never persist `ordinal()`.** Persist `name()` or an explicit code.
- **Constant-specific method bodies** turn "forgot a case" into a compile error.
- **`EnumMap` is an ordinal-indexed array; `EnumSet` is a bit vector.** Use them whenever
  the key or element is an enum.
- The **enum singleton** is the safest singleton — immune to serialization and reflection
  attacks.
- In a `switch`, write the bare constant name, and **omit `default`** so the compiler
  catches new constants.
- Avoid mutable state in enum constants — they are global singletons.

---

**Previous:** [34 — Inner and anonymous classes](34-inner-and-anonymous-classes.md) ·
**Next:** [36 — Records](36-records.md)
