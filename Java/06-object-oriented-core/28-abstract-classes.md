# 28 · Abstract Classes

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/28-abstract-classes.java
> ```

An abstract class is one that **cannot be instantiated** and may leave some methods
unimplemented for subclasses to fill in. It sits between a concrete class and an interface.

---

## 1. The mechanics

```java
abstract class Shape {

    private final String name;              // state — interfaces cannot have this

    Shape(String name) {                    // a constructor, called via super(...)
        this.name = name;
    }

    abstract double area();                 // NO BODY — subclasses must supply one

    String describe() {                     // a concrete method, inherited as-is
        return name + " with area " + area();
    }
}
```

- `abstract` on the **class** means it cannot be instantiated.
- `abstract` on a **method** means no body, and every concrete subclass must implement it.
- An abstract class may contain **any mixture** of abstract and concrete methods,
  including none of either.

```java
new Shape("x");                  // ERROR: Shape is abstract; cannot be instantiated
Shape s = new Circle(5);         // fine — Circle is concrete
```

### The rules

| Rule | Detail |
| --- | --- |
| Cannot be instantiated | But **can** have constructors, called via `super(...)` |
| Can have abstract methods | And concrete ones, and fields, and static members |
| A class with an abstract method **must** be abstract | The compiler enforces it |
| An abstract class need **not** have any abstract methods | Legal, and occasionally useful to prevent instantiation |
| A subclass must implement all abstract methods | Or be declared `abstract` itself |
| `abstract` and `final` are contradictory | Compile error together |
| `abstract` and `private` are contradictory | A private method cannot be overridden |
| `abstract` and `static` are contradictory | Static methods are not overridden |

---

## 2. Why it exists: a partial implementation

The value of an abstract class is that it can define an algorithm and leave *holes*:

```java
abstract class Shape {
    abstract double area();
    abstract double perimeter();

    // Written ONCE, works for every shape that ever exists
    String summary() {
        return String.format("area=%.2f, perimeter=%.2f, ratio=%.2f",
                area(), perimeter(), area() / perimeter());
    }
}
```

`summary()` calls methods that do not exist yet. Every subclass gets it for free, correctly,
without writing a line.

This is the **template method pattern**, and it is the main reason abstract classes exist.

---

## 3. The template method pattern

```java
abstract class DataImporter {

    // The ALGORITHM. final, so subclasses cannot reorder the steps.
    final void importData(String source) {
        validate(source);            // shared
        var raw = read(source);      // subclass decides HOW
        var parsed = parse(raw);     // subclass decides HOW
        save(parsed);                // shared
        log(source, parsed.size());  // shared
    }

    protected abstract List<String> read(String source);
    protected abstract List<Record> parse(List<String> raw);

    private void validate(String source) { ... }   // fixed, shared
    private void save(List<Record> records) { ... }
    private void log(String source, int count) { ... }
}
```

The base class owns the **sequence**; subclasses own the **steps**. A CSV importer and a
JSON importer differ in two methods and share everything else.

Note the details that make it work:

- `importData` is **`final`** — subclasses cannot change the order of operations.
- The hooks are **`protected abstract`** — visible to subclasses, not to callers.
- The shared steps are **`private`** — subclasses cannot break them.

You have already used this pattern: `AbstractList`, `AbstractMap` and
`InputStream` in the JDK are all built this way.

---

## 4. Abstract class vs interface

This is the question that matters, and Java 8 changed the answer.

| | Abstract class | Interface |
| --- | --- | --- |
| Instance fields (state) | **Yes** | **No** — only `public static final` constants |
| Constructors | **Yes** | No |
| How many can you have | **One** | Many |
| Method bodies | Yes | Yes, since Java 8 (`default`, `static`, `private`) |
| Access modifiers on members | All four | `public` (or `private` for helpers, Java 9+) |
| Can extend/implement | One class, many interfaces | Many interfaces |

### Since Java 8, interfaces can have method bodies

```java
interface Greeter {
    String name();

    default String greet() {              // a body, in an interface
        return "Hello, " + name();
    }

    static Greeter of(String name) {      // a static factory, in an interface
        return () -> name;
    }
}
```

So "interfaces cannot have implementations" is **no longer true** and has not been since
2014. The remaining differences are what matter:

> **An abstract class can hold state. An interface cannot. A class can implement many
> interfaces but extend only one class.**

### Which to choose

**Use an interface when:**
- You are defining a **capability** or contract — `Comparable`, `Runnable`, `Serializable`.
- Unrelated classes might implement it.
- You want implementers free to extend something else.
- **This is the default.** Reach for an interface first.

**Use an abstract class when:**
- Subclasses genuinely share **state** — fields, a constructor.
- You want a **template method** controlling an algorithm.
- You need non-public members.
- The subtypes are closely related and you control them all.

**Often the best answer is both:** an interface for the type, and an abstract "skeletal"
class for convenience:

```java
interface List<E> { ... }
abstract class AbstractList<E> implements List<E> { ... }
class ArrayList<E> extends AbstractList<E> { ... }
```

Callers depend on `List`. Implementers may extend `AbstractList` for a head start, or
implement `List` directly if they need to extend something else. That is the JDK's own
pattern, and it is worth copying.

---

## 5. Common mistakes

### Making everything abstract

If every method is abstract and there are no fields, **you have written an interface** —
and one that costs your users their single inheritance slot. Use an interface.

### Abstract classes with too much state

The more state a base class holds, the more tightly subclasses are coupled to it, and the
worse the fragile base class problem gets (lesson 26). Keep abstract classes thin.

### Calling an abstract method from the constructor

```java
abstract class Base {
    Base() { init(); }                    // calls the SUBCLASS version
    abstract void init();
}

class Derived extends Base {
    private String value = "set";
    @Override void init() { print(value); }   // prints null
}
```

Exactly the bug from lesson 22, and abstract classes make it *more* tempting because the
hole is right there asking to be called.

### Forgetting that constructors still run

An abstract class's constructor executes on every subclass instantiation. It just cannot be
called with `new`. Validation there still applies.

---

## 6. When not to use either

Before building a hierarchy, ask whether you need one:

- **Two cases?** A `boolean` or an enum is usually clearer.
- **Sharing code only?** Composition or a static helper (lesson 26).
- **Fixed set of variants?** A **sealed** interface with records, plus an exhaustive
  `switch` (lesson 37). This is often better than an abstract class for closed hierarchies,
  because the compiler verifies you handled every case.
- **Just data?** A `record` (lesson 36).

Abstract classes are a good tool with a narrow purpose: **shared state plus a controlled
algorithm.** Reach for an interface first, and use an abstract class when you actually need
what it uniquely offers.

---

## 7. Summary

- `abstract` on a class prevents instantiation; on a method it means "no body, subclasses
  must supply one".
- An abstract class **can** have constructors, fields, concrete methods and static members.
- A class with an abstract method must itself be abstract; a subclass must implement every
  abstract method or be abstract too.
- `abstract` cannot combine with `final`, `private` or `static`.
- The main reason abstract classes exist is the **template method pattern**: a `final`
  algorithm with `protected abstract` holes.
- Since Java 8, interfaces have `default` and `static` methods. The real differences are
  **state** and **single inheritance**.
- **Default to an interface.** Use an abstract class for shared state or a template method.
  Often use both — interface for the type, skeletal abstract class for convenience.
- Never call an abstract method from a constructor.
- Consider `sealed` interfaces + records + `switch` for a fixed set of variants.

---

**Previous:** [27 — Polymorphism and overriding](27-polymorphism-and-overriding.md) ·
**Next:** [29 — Interfaces](29-interfaces.md)
