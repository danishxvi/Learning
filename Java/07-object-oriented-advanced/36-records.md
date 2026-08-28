# 36 · Records

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/36-records.java
> ```

A record is a **transparent carrier for immutable data**. One line replaces about fifty of
boilerplate — and, more importantly, replaces fifty lines you would probably get subtly
wrong.

Records are standard since **Java 16**.

---

## 1. What one line generates

```java
record Point(int x, int y) { }
```

The compiler generates all of this:

- A `private final` field for each component.
- A **canonical constructor** taking all components in order.
- An **accessor** per component — named `x()`, **not** `getX()`.
- `equals(Object)` comparing every component.
- `hashCode()` consistent with that `equals`.
- `toString()` as `Point[x=1, y=2]`.

The equivalent hand-written class is roughly 50 lines, and getting `equals`/`hashCode`
right by hand is exactly what lesson 33 was about.

### What you cannot change

| Property | Detail |
| --- | --- |
| Implicitly `final` | Cannot be extended |
| Implicitly extends `Record` | So it **cannot extend any class** |
| Components are `private final` | No setters, ever |
| Cannot add instance fields | Only the declared components |
| **Can** implement interfaces | Yes, any number |
| **Can** have static fields and methods | Yes |
| **Can** have extra instance methods | Yes |

That "cannot add instance fields" rule is the one that catches people. A record's state is
**exactly** its components — which is precisely what makes `equals` and `toString`
trustworthy.

---

## 2. Accessors are `x()`, not `getX()`

```java
Point p = new Point(1, 2);
p.x();          // correct
p.getX();       // does not exist
```

This is deliberate. Records follow the "component accessor" convention rather than
JavaBeans. Older frameworks that scan for `getX()` may need configuration — Jackson and
Spring have supported records natively for years now, but very old tooling may not.

---

## 3. Compact constructors — validation and normalisation

```java
record Range(int low, int high) {
    Range {                                    // COMPACT constructor — no parameter list
        if (low > high) {
            throw new IllegalArgumentException("low > high");
        }
    }
}
```

The compact form has **no parameter list and no field assignments**. The parameters are
implicitly in scope, and the compiler assigns them to the fields *after* your code runs.

That means you can also **normalise** by reassigning the parameter:

```java
record Email(String address) {
    Email {
        address = address.strip().toLowerCase();   // reassigns the PARAMETER
    }                                              // the compiler then assigns the field
}
```

Writing `this.address = ...` in a compact constructor is a compile error — the compiler
does that for you at the end.

### Defensive copying

A record is only **shallowly** immutable. A `List` component is still mutable:

```java
record Team(String name, List<String> members) {
    Team {
        members = List.copyOf(members);       // copy IN
    }
}
```

Without that, the caller keeps a reference to the list and can modify the record's contents
afterwards. `List.copyOf` also rejects `null` elements, which is a bonus. Lesson 38 covers
this properly.

---

## 4. What else a record can have

```java
record Point(int x, int y) {

    static final Point ORIGIN = new Point(0, 0);        // static field: fine

    Point {                                              // compact constructor
        // validation
    }

    Point(int both) { this(both, both); }                // extra constructor — must delegate

    double distanceFromOrigin() {                        // instance method: fine
        return Math.sqrt(x * x + y * y);
    }

    static Point of(int x, int y) { return new Point(x, y); }   // static factory: fine

    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }   // override: fine
}
```

Any additional constructor **must** delegate to the canonical one with `this(...)`, so
validation can never be bypassed.

You *may* override `equals`, `hashCode` or an accessor — but think hard first. The generated
versions are correct, and overriding them is usually a sign the type is not really a record.

---

## 5. Records and pattern matching

This is where records become more than boilerplate reduction.

```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}

double area = switch (shape) {
    case Circle(double r)              -> Math.PI * r * r;
    case Rectangle(double w, double h) -> w * h;
};
```

`case Circle(double r)` is a **record pattern** — it tests the type *and* destructures the
components in one step. Patterns nest:

```java
case Line(Point(var x1, var y1), Point(var x2, var y2)) -> ...
```

**Sealed interface + records + switch** is the combination that makes Java's data modelling
genuinely pleasant. Lesson 37.

---

## 6. When to use a record

**Use one when:**

- The type is a **transparent data carrier** — its state *is* its components.
- It should be **immutable**.
- You want `equals`/`hashCode`/`toString` for free and correct.

Typical: DTOs, API request/response types, coordinates, money, database rows, event
payloads, map keys, method return types carrying several values (lesson 17), and the
variants of a sealed hierarchy.

**Do not use one when:**

- The object needs **mutable** state.
- It must **extend a class**.
- The internal representation should be **hidden** — a record publishes its components by
  design, so it is the wrong choice when you want to keep the representation changeable
  (lesson 24's `Temperature` example).
- Identity matters more than value — an entity with a lifecycle, not a value.

### The encapsulation trade-off

A record deliberately gives up representation hiding. That is a real trade-off, not an
oversight: you are declaring "this type *is* these values, and that will not change".
When you might want to swap the internal representation later, write a class.

---

## 7. Local records (Java 16+)

```java
void process(List<String> lines) {
    record Parsed(String key, int value) { }     // declared inside a method

    List<Parsed> parsed = lines.stream()
            .map(line -> new Parsed(line.split("=")[0], Integer.parseInt(line.split("=")[1])))
            .toList();
}
```

A record scoped to one method — ideal for intermediate results in a stream pipeline, where
previously you would have used an `Object[]`, a `Map.Entry`, or an awkward two-element list.
Local records are implicitly `static`, so they capture nothing.

---

## 8. Serialization

Records serialize safely, and better than ordinary classes: deserialization goes **through
the canonical constructor**, so validation runs. An ordinary `Serializable` class bypasses
its constructors entirely on deserialization — which is a long-standing source of
invariant-breaking bugs.

---

## 9. Summary

- `record Point(int x, int y) { }` generates the fields, canonical constructor, accessors,
  `equals`, `hashCode` and `toString` — about 50 correct lines.
- Accessors are `x()`, **not** `getX()`.
- Records are implicitly `final`, extend `Record`, and **cannot extend a class**. They
  **can** implement interfaces and have static members and extra methods.
- **You cannot add instance fields.** A record's state is exactly its components.
- The **compact constructor** validates and normalises by reassigning parameters; the
  compiler assigns the fields afterwards. Do not write `this.x = ...` there.
- Records are only **shallowly immutable** — defensively copy mutable components with
  `List.copyOf`.
- Extra constructors must delegate to the canonical one, so validation cannot be bypassed.
- **Record patterns** destructure in `switch` and `instanceof`; with sealed interfaces this
  is Java's best data-modelling tool.
- **Local records** are excellent for intermediate values in stream pipelines.
- Deserialization runs the canonical constructor, so validation is preserved — unlike
  ordinary serializable classes.
- Use a record for **immutable, transparent data**; use a class when you need mutability,
  inheritance, or a hidden representation.

---

**Previous:** [35 — Enums](35-enums.md) ·
**Next:** [37 — Sealed classes and pattern matching](37-sealed-classes-and-pattern-matching.md)
