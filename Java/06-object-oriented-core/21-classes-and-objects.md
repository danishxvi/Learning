# 21 · Classes and Objects

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/21-classes-and-objects.java
> ```

This is where Java stops being "C with a JVM" and becomes object-oriented. Everything from
here to lesson 38 builds on the ideas in this file.

---

## 1. Class vs object

> A **class** is a blueprint. An **object** is a thing built from it.

```java
class Car {                    // the blueprint — written once
    String model;
    int speed;

    void accelerate() {
        speed += 10;
    }
}

Car myCar = new Car();         // an OBJECT — built at runtime
Car yourCar = new Car();       // another one, entirely independent
```

- The class exists once, at compile time, and is loaded once by the JVM.
- Each object has **its own copy of every instance field**, but they **share** the method
  code. There is exactly one copy of `accelerate()` in memory no matter how many `Car`
  objects exist.

A blueprint for a house is not a house. You cannot live in it, and changing the blueprint
does not move anyone's walls.

---

## 2. What is in a class

```java
class BankAccount {

    // 1. FIELDS (instance variables) — the state
    private String accountNumber;
    private double balance;

    // 2. CONSTRUCTOR — how an object is built
    BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    // 3. METHODS — the behaviour
    void deposit(double amount) {
        balance += amount;
    }

    // 4. STATIC MEMBERS — belong to the class, not any object
    static int accountCount = 0;

    // 5. NESTED TYPES — classes, enums, records, interfaces
    static class TransactionRecord { }

    // 6. INITIALISER BLOCKS — rarely needed (lesson 22)
    { /* instance initialiser */ }
    static { /* static initialiser */ }
}
```

Fields and methods together are called **members**.

---

## 3. Creating an object — what `new` actually does

```java
BankAccount account = new BankAccount("ACC-001", 5000);
```

Five distinct things happen:

1. **Memory is allocated** on the heap, large enough for all instance fields.
2. **Fields are zeroed** — `0`, `false`, `null`, depending on type.
3. **Field initialisers and instance blocks run**, top to bottom.
4. **The constructor body runs.**
5. **The reference is returned** and stored in `account`.

Steps 2 and 3 are why fields have defaults but local variables do not (lesson 03) — the
JVM zeroes the allocated memory as part of creating the object.

### Stack and heap

```java
BankAccount account = new BankAccount("ACC-001", 5000);
```

```
      STACK                          HEAP
  ┌─────────────┐             ┌──────────────────────┐
  │  account    │────────────►│ BankAccount          │
  │ (reference) │             │  accountNumber ──────┼──► "ACC-001"
  └─────────────┘             │  balance = 5000.0    │
                              └──────────────────────┘
```

- The **variable** lives on the stack (if it is a local) and holds a reference.
- The **object** always lives on the heap.
- When nothing references the object any more, it becomes eligible for garbage collection.

This is the picture that makes lesson 17's pass-by-value discussion make sense.

---

## 4. Instance state is per-object

```java
Car a = new Car();
Car b = new Car();

a.speed = 100;
System.out.println(b.speed);    // 0 — completely independent
```

Each object carries its own `speed`. This is the whole point: a class describes *a kind
of thing*, and each object is *one of those things* with its own values.

### The reference-sharing trap

```java
Car a = new Car();
Car b = a;              // NOT a new car — a second name for the same one
b.speed = 100;
System.out.println(a.speed);    // 100
```

Assignment copies the reference, never the object. Exactly as in lesson 12 with arrays and
lesson 17 with parameters. To get a genuinely separate object you must create one.

---

## 5. `null` and `NullPointerException`

```java
Car car = null;             // a valid reference value meaning "points at nothing"
car.accelerate();           // NullPointerException
```

`null` is legal to *hold* and illegal to *dereference*. Since Java 14, **helpful NPE
messages** tell you precisely which part was null:

```
Cannot invoke "Car.accelerate()" because "car" is null
```

Before Java 14 you got a line number and had to guess which of five dots on that line was
the problem. This alone is a good reason to be on a modern JDK.

Defences, in order of preference:

1. **Do not produce `null`** — return empty collections and `Optional` (lessons 17, 56).
2. **Fail fast** — `Objects.requireNonNull(x, "x must not be null")` in constructors.
3. **Guard** — `if (x != null)`, or `Objects.equals`, or safe ordering (`"lit".equals(x)`).

---

## 6. Encapsulation — a first look

```java
class BankAccount {
    private double balance;                 // nobody outside can touch this

    public double getBalance() {            // controlled read
        return balance;
    }

    public void deposit(double amount) {    // controlled write, WITH RULES
        if (amount <= 0) {
            throw new IllegalArgumentException("deposit must be positive");
        }
        balance += amount;
    }
}
```

Making `balance` public would allow `account.balance = -5000;`. The `private` field plus a
validating method makes that impossible. **This is the entire point of encapsulation:
the object protects its own invariants.**

Lesson 24 covers it properly.

---

## 7. `toString()` — always override it

```java
Car car = new Car("Tesla", 0);
System.out.println(car);        // Car@1b6d3586  ← useless
```

The default `toString()` prints the class name and identity hash. Override it and every
`println`, string concatenation, debugger view and log line improves:

```java
@Override
public String toString() {
    return "Car[model=" + model + ", speed=" + speed + "]";
}
```

`System.out.println(object)` calls `toString()` automatically, and so does `"" + object`.
**Override `toString()` on every class you write** — it costs three lines and pays for
itself the first time you debug.

Records (lesson 36) generate it for you, which is one of their main attractions.

---

## 8. Object lifecycle

1. **Creation** — `new` allocates and initialises.
2. **In use** — reachable from a live reference.
3. **Unreachable** — nothing refers to it any more.
4. **Garbage collected** — the JVM reclaims the memory, at a time of its choosing.

```java
Car car = new Car();
car = null;             // the object is now unreachable
                        // the GC will collect it eventually — not immediately
```

You cannot force collection. `System.gc()` is a *suggestion* the JVM may ignore.

`finalize()` — a method that once ran before collection — is **deprecated for removal** and
must not be used. It was unpredictable, could resurrect objects, and delayed collection.
Use try-with-resources (lesson 41) for cleanup instead.

---

## 9. Class design — the first principles

**One class, one responsibility.** If describing the class needs "and", it is two classes.

**Fields private, methods public by default.** Widen access only when there is a reason.

**Prefer immutability.** Fields `final` where possible, no setters unless required. An
immutable object is automatically thread-safe and cannot be corrupted by a caller
(lesson 38).

**Name classes as nouns, methods as verbs.** `Invoice.calculateTotal()`, not
`InvoiceManager.doStuff()`.

**Be suspicious of `-Manager`, `-Helper`, `-Util`, `-Processor` names.** They usually mark
a class with no clear responsibility that has become a dumping ground.

---

## 10. Summary

- A **class** is a blueprint; an **object** is an instance. Each object has its own fields;
  all objects share one copy of the method code.
- `new` allocates on the heap, zeroes the fields, runs initialisers, then the constructor,
  then returns a reference.
- The **variable** holds a reference; the **object** lives on the heap. Assignment copies
  the reference, not the object.
- `null` means "points at nothing" — legal to hold, illegal to dereference. Java 14+ gives
  helpful NPE messages naming the exact expression.
- Keep fields `private` and expose validating methods so the object protects its own
  invariants.
- **Always override `toString()`.** The default is a useless identity hash.
- You cannot force garbage collection, and `finalize()` is deprecated for removal.
- One class, one responsibility; nouns for classes, verbs for methods; prefer immutability.

---

**Previous:** [20 — Recursion](../05-methods/20-recursion.md) ·
**Next:** [22 — Constructors and initialisation order](22-constructors.md)
