# 29 · Interfaces

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/29-interfaces.java
> ```

An interface is a **contract**: a set of methods a class promises to provide. It is the
most important tool in Java for decoupling code, and it has changed substantially since
Java 8.

---

## 1. The basics

```java
interface Drawable {
    void draw();                     // implicitly public abstract
}

class Circle implements Drawable {
    @Override
    public void draw() { ... }       // MUST be public
}
```

- Interface methods are implicitly **`public abstract`** — writing those keywords is
  redundant, and most style guides say to omit them.
- Interface fields are implicitly **`public static final`** — constants only, never
  instance state.
- Implementing methods **must be `public`**, because you cannot narrow access
  (lesson 27).

```java
interface Config {
    int MAX = 100;                   // implicitly public static final
    // int counter;                  // ERROR: needs an initialiser; no instance fields
}
```

A class may implement **many** interfaces:

```java
class Circle implements Drawable, Comparable<Circle>, Serializable { }
```

That is the central advantage over abstract classes: interfaces do not consume the single
inheritance slot.

---

## 2. What changed in Java 8, 9 and beyond

| Feature | Version | Purpose |
| --- | --- | --- |
| `abstract` methods | 1.0 | The contract |
| Constants | 1.0 | `public static final` |
| **`default` methods** | **8** | Add methods without breaking implementers |
| **`static` methods** | **8** | Utility/factory methods on the interface itself |
| **`private` methods** | **9** | Share code between default methods |
| **`sealed`** | **17** | Restrict who may implement it |

### `default` methods and why they exist

Before Java 8, adding a method to a published interface **broke every implementation in the
world**. That is why `Collection` could not gain `stream()` — millions of classes implement
it.

```java
interface Collection<E> {
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
```

Existing implementers get the new method for free. This is called **interface evolution**,
and it is the entire reason `default` was added — it was not meant to turn interfaces into
abstract classes.

### `static` methods on interfaces

```java
interface Validator {
    boolean test(String input);

    static Validator notBlank() {
        return s -> s != null && !s.isBlank();
    }
}
```

Before Java 8 these lived in a companion class (`Collections` for `Collection`,
`Arrays` for arrays). Now they can live on the interface itself, which is why new JDK APIs
have `List.of`, `Map.entry`, `Comparator.comparing`, `Stream.of`.

Static interface methods are **not inherited** by implementing classes — you must call
`Validator.notBlank()`, never `myValidator.notBlank()`.

### `private` methods (Java 9)

```java
interface Logger {
    default void info(String msg)  { log("INFO", msg); }
    default void error(String msg) { log("ERROR", msg); }

    private void log(String level, String msg) {        // shared, not part of the API
        System.out.println("[" + level + "] " + msg);
    }
}
```

Without `private`, that shared helper would have to be a `default` method — and therefore
part of the public contract, visible to every caller.

---

## 3. The diamond problem, resolved

```java
interface A { default void greet() { System.out.println("A"); } }
interface B { default void greet() { System.out.println("B"); } }

class C implements A, B { }        // ERROR: inherits unrelated defaults
```

Java's rule: if two interfaces provide conflicting `default` methods, the implementing
class **must** override the method. You can delegate explicitly:

```java
class C implements A, B {
    @Override
    public void greet() {
        A.super.greet();           // pick one explicitly
    }
}
```

`A.super.greet()` is the syntax for "the default from interface A".

### The resolution rules, in order

1. **A class wins over an interface.** A concrete method in a superclass beats any
   `default`.
2. **The most specific interface wins.** If `B extends A`, `B`'s default beats `A`'s.
3. **Otherwise, you must override.** Ambiguity is a compile error, not a silent choice.

Rule 1 is sometimes called "the class always wins", and it exists to guarantee that adding
a `default` to an interface can never change the behaviour of existing code.

---

## 4. Functional interfaces

An interface with **exactly one abstract method** is a *functional interface*, and can be
implemented with a lambda:

```java
@FunctionalInterface
interface Calculator {
    int apply(int a, int b);         // exactly ONE abstract method
}

Calculator add = (a, b) -> a + b;                    // lambda
Calculator max = Math::max;                          // method reference
```

`default` and `static` methods do **not** count toward the one-abstract-method limit, so an
interface can be functional and still have many methods.

`@FunctionalInterface` is optional but recommended: it makes the compiler enforce that
there is exactly one abstract method, so someone adding a second gets an error rather than
silently breaking every lambda.

The JDK provides the common shapes in `java.util.function` — `Function`, `Predicate`,
`Consumer`, `Supplier` and friends. Lessons 51 and 52 cover them.

---

## 5. `sealed` interfaces (Java 17)

```java
sealed interface Shape permits Circle, Rectangle, Triangle { }
```

`permits` names the **complete** list of allowed implementers. Every permitted type must be
`final`, `sealed`, or `non-sealed`.

This turns the interface into a **closed** hierarchy, which lets the compiler verify a
`switch` is exhaustive:

```java
double area = switch (shape) {
    case Circle c    -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t  -> 0.5 * t.base() * t.height();
    // no default needed — the compiler knows the list is complete
};
```

Add a fourth shape and every such `switch` stops compiling until you handle it. That is a
genuinely different guarantee from an open interface, and it makes
**sealed interface + records + switch** a strong alternative to polymorphism when the
variants are fixed and the operations keep changing. Lesson 37 covers it.

---

## 6. Marker interfaces

An interface with **no members at all**, used purely to tag a type:

```java
interface Serializable { }
interface Cloneable { }
```

Code then checks `if (obj instanceof Serializable)`. Annotations largely replaced this
pattern, but marker interfaces have one advantage: they create a **type**, so the compiler
can enforce them:

```java
void save(Serializable data) { }    // compile-time check
```

An annotation could only be checked at runtime.

---

## 7. Programming to interfaces

```java
// BAD — the declared type ties you to an implementation
ArrayList<String> names = new ArrayList<>();
void process(ArrayList<String> items) { }

// GOOD — the declared type states only what you need
List<String> names = new ArrayList<>();
void process(List<String> items) { }
```

The second version lets you switch to `LinkedList`, `List.of(...)` or anything else without
touching a single caller. Callers can also pass whatever they already have.

**Rule: declare the most general type that supplies what you actually use.**

Do not overdo it, though. If your method genuinely needs `O(1)` random access, saying
`List` and then calling `get(i)` in a loop is a lie that a `LinkedList` will punish
(lesson 45).

### Interfaces and testing

This is where interfaces pay for themselves most obviously:

```java
class OrderService {
    private final PaymentGateway gateway;         // an INTERFACE

    OrderService(PaymentGateway gateway) {        // injected
        this.gateway = gateway;
    }
}
```

In production you pass a real gateway; in tests you pass a fake. Without the interface,
`OrderService` would be untestable without a network. This is **dependency inversion** —
the "D" in SOLID — and it is the practical reason interfaces matter.

---

## 8. Interface design

**Keep interfaces small.** The **interface segregation principle** (the "I" in SOLID): many
small interfaces beat one large one. A class should not be forced to implement methods it
does not need.

```java
// BAD — a printer that cannot fax must still implement fax()
interface Machine { void print(); void scan(); void fax(); }

// GOOD
interface Printer { void print(); }
interface Scanner { void scan(); }
class AllInOne implements Printer, Scanner { }
```

**Name interfaces for capability.** `Comparable`, `Runnable`, `Closeable`, `Iterable` —
the `-able` suffix reads well for capabilities. Avoid Hungarian-style `IUserService`; that
is a C# convention, not a Java one.

**Do not add `default` methods casually.** They are for evolving published interfaces, not
for avoiding an abstract class. Every `default` is a decision you impose on every
implementer forever.

**Prefer an interface to an abstract class** unless you need state (lesson 28).

---

## 9. Summary

- An interface is a contract. Methods are implicitly `public abstract`; fields are
  implicitly `public static final` constants — **no instance state**.
- A class implements **many** interfaces but extends **one** class. That is the main reason
  to prefer interfaces.
- **`default`** methods (Java 8) exist for **interface evolution** — adding methods without
  breaking implementers. **`static`** methods replaced companion utility classes.
  **`private`** methods (Java 9) share code between defaults.
- Conflicting defaults are a **compile error**; resolve with `A.super.method()`.
  A class method always wins over a `default`; a more specific interface wins over a
  general one.
- One abstract method makes an interface **functional**, so a lambda can implement it.
  `@FunctionalInterface` makes the compiler enforce that.
- **`sealed`** interfaces close the hierarchy so `switch` can be checked for exhaustiveness.
- **Marker interfaces** create a type the compiler can check; annotations cannot.
- **Program to interfaces**: declare `List`, not `ArrayList`. It is what makes code
  testable.
- Keep interfaces small (interface segregation) and name them for capability.

---

**Previous:** [28 — Abstract classes](28-abstract-classes.md) ·
**Next:** [30 — Packages and access modifiers](../07-object-oriented-advanced/30-packages-and-access-modifiers.md)
