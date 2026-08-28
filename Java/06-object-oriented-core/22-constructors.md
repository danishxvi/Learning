# 22 · Constructors and Initialisation Order

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/22-constructors.java
> ```

A constructor's job is to produce an object that is **valid from its first instant**. Get
this right and whole categories of bug become impossible.

---

## 1. What a constructor is

```java
class Person {
    private String name;

    Person(String name) {        // ← constructor
        this.name = name;
    }
}
```

Rules:

- The name **must** match the class name exactly.
- It has **no return type** — not even `void`.
- It runs automatically when `new` is used, and cannot be called like a method.

### The `void` trap

```java
class Person {
    void Person() { }        // NOT a constructor — it is a METHOD called "Person"
}
```

Adding `void` silently turns a constructor into an ordinary method. The class then gets a
default constructor, your initialisation never runs, and nothing warns you. This compiles,
and it is a genuinely confusing bug the first time you meet it.

---

## 2. The default constructor

If you write **no constructor at all**, the compiler inserts one:

```java
class Empty { }
// becomes
class Empty {
    Empty() {
        super();
    }
}
```

It takes no arguments and does nothing but call `super()`.

> **The moment you write any constructor, the default disappears.**

```java
class Person {
    Person(String name) { ... }
}

new Person();          // COMPILE ERROR — no no-arg constructor exists
```

This bites when adding a constructor to an existing class: code that said `new Person()`
stops compiling. If you need both, write both.

The default constructor also gets the **same access modifier as the class**, which is why a
`public class` gets a `public` default constructor.

---

## 3. Constructor overloading and `this(...)`

Constructors overload by the rules in lesson 18. Chain them with `this(...)`:

```java
class Rectangle {
    private final int width, height;

    Rectangle() {
        this(1, 1);                  // delegate
    }

    Rectangle(int side) {
        this(side, side);            // delegate
    }

    Rectangle(int width, int height) {   // THE PRIMARY CONSTRUCTOR
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        this.width = width;
        this.height = height;
    }
}
```

**Rules for `this(...)`:**

- It must be the **first statement** in the constructor.
- Therefore you can have `this(...)` **or** `super(...)`, never both.
- It cannot be recursive — `A()` calling `this()` is a compile error.

**Why chain?** Validation lives in exactly one place. Add a rule to the primary constructor
and every other constructor inherits it automatically. Duplicating validation across
constructors is how objects end up half-validated.

---

## 4. `super(...)` and the constructor chain

Every constructor calls a superclass constructor, whether you write it or not:

```java
class Animal {
    Animal(String name) { ... }
}

class Dog extends Animal {
    Dog(String name) {
        super(name);        // explicit
    }
}
```

If you write neither `this(...)` nor `super(...)`, the compiler inserts **`super()`** — the
*no-argument* superclass constructor.

### The most common inheritance error

```java
class Animal {
    Animal(String name) { }      // no no-arg constructor!
}

class Dog extends Animal {
    Dog() { }                    // ERROR: no suitable constructor found for Animal()
}
```

The compiler inserted `super()`, and `Animal` has no no-arg constructor. Fix it by calling
`super(name)` explicitly, or by giving `Animal` a no-arg constructor.

### The full initialisation order

For `new Dog()` where `Dog extends Animal extends Object`:

```
1. Animal's static blocks and static field initialisers   (once, at class load)
2. Dog's static blocks and static field initialisers      (once, at class load)
3. Object's constructor
4. Animal's instance initialisers and field initialisers
5. Animal's constructor body
6. Dog's instance initialisers and field initialisers
7. Dog's constructor body
```

**Statics once, ever. Instance members bottom-up: superclass fully before subclass.**

---

## 5. The dangerous consequence: calling an overridable method from a constructor

```java
class Parent {
    Parent() {
        init();                 // calls the OVERRIDDEN version!
    }
    void init() { }
}

class Child extends Parent {
    private String value = "set";

    @Override
    void init() {
        System.out.println(value);   // prints null!
    }
}

new Child();      // prints null
```

Trace it against the order above: `Parent`'s constructor (step 5) runs **before** `Child`'s
field initialisers (step 6). `init()` is overridden, so `Child.init()` runs — while
`value` is still `null`.

> **Never call an overridable method from a constructor.** Make such methods `private`,
> `final`, or `static`.

This is a real, subtle bug that has bitten every Java codebase of size.

---

## 6. Static vs instance initialisers

```java
class Example {
    static int staticField = init("static field");
    static { System.out.println("static block"); }

    int instanceField = init("instance field");
    { System.out.println("instance block"); }

    Example() { System.out.println("constructor"); }
}
```

| | Static initialiser | Instance initialiser |
| --- | --- | --- |
| Runs | **Once**, when the class is loaded | On **every** object creation |
| Order | Before any instance member | After `super()`, before the constructor body |
| Can access | Static members only | Everything |
| Typical use | Loading constants, registering drivers | Rare — prefer a constructor |

**Instance initialiser blocks are almost never the right tool.** Their one legitimate use
is sharing code between several constructors that cannot chain — and even then, a private
method called from each constructor is clearer. You will meet them mainly in anonymous
classes (lesson 34).

Static blocks are more useful: initialising a complex constant, or building a lookup table.

---

## 7. Constructor design

### Validate everything

```java
Person(String name, int age) {
    this.name = Objects.requireNonNull(name, "name");
    if (age < 0 || age > 150) {
        throw new IllegalArgumentException("age out of range: " + age);
    }
    this.age = age;
}
```

An object should never exist in an invalid state. Validating in the constructor means every
method can then assume the state is sane.

### Assign `final` fields exactly once

```java
class Point {
    private final int x, y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;         // MUST be assigned here or at declaration
    }
}
```

The compiler enforces that every `final` field is definitely assigned exactly once on every
path through the constructor.

### Keep constructors cheap

A constructor should assign fields and validate. It should **not** open files, hit the
network, or start threads:

- It makes the class untestable without that resource.
- Failure mid-construction leaves a partly-built object.
- Starting a thread from a constructor can leak `this` before the object is finished.

Use a static factory or an `init()` method the caller calls explicitly.

### Consider static factory methods

```java
class Temperature {
    private final double celsius;

    private Temperature(double celsius) { this.celsius = celsius; }

    static Temperature ofCelsius(double c)    { return new Temperature(c); }
    static Temperature ofFahrenheit(double f) { return new Temperature((f - 32) * 5 / 9); }
}
```

Advantages over constructors:

- **They have names.** `ofCelsius` versus `ofFahrenheit` — two constructors both taking one
  `double` would be impossible.
- **They can return a cached instance** — `Integer.valueOf`, `Boolean.valueOf`.
- **They can return a subtype** — `List.of` returns different implementations by size.

This is why the JDK is full of `valueOf`, `of`, `from` and `getInstance`.

### The telescoping-constructor problem

```java
Pizza(int size)
Pizza(int size, boolean cheese)
Pizza(int size, boolean cheese, boolean pepperoni)
Pizza(int size, boolean cheese, boolean pepperoni, boolean mushroom)
```

Call sites become `new Pizza(12, true, false, true)` — unreadable and easy to get wrong.
The fix is the **builder pattern** (lesson 76):

```java
Pizza pizza = new Pizza.Builder(12)
        .cheese()
        .mushroom()
        .build();
```

---

## 8. Copy constructors

Java has no built-in copy constructor, but the convention is standard:

```java
Person(Person other) {
    this.name = other.name;
    this.addresses = new ArrayList<>(other.addresses);   // DEEP copy of mutable state
}
```

Note the deep copy of the list. A shallow copy would leave both objects sharing one list,
so mutating either would affect both. Lesson 38 covers this.

Copy constructors are generally preferable to `clone()`, which has a famously awkward
contract (lesson 32).

---

## 9. Summary

- A constructor has the class's name and **no return type**. Adding `void` makes it an
  ordinary method — a silent, confusing bug.
- If you write **no** constructor, you get a default no-arg one. Writing **any**
  constructor removes it.
- `this(...)` delegates to another constructor; `super(...)` calls the superclass. Either
  must be the **first statement**, so you can never have both.
- If you write neither, the compiler inserts `super()` — which fails if the superclass has
  no no-arg constructor.
- Initialisation order: **statics once** (superclass first), then per object — superclass
  field initialisers and constructor, then subclass field initialisers and constructor.
- **Never call an overridable method from a constructor.** The subclass's fields are still
  `null`.
- Instance initialiser blocks are rarely the right tool; static blocks are useful for
  constants and lookup tables.
- Validate everything, assign `final` fields exactly once, and keep constructors cheap — no
  I/O, no threads.
- Prefer **static factory methods** when you need names, caching, or to return a subtype;
  prefer a **builder** over a telescoping constructor.

---

**Previous:** [21 — Classes and objects](21-classes-and-objects.md) ·
**Next:** [23 — The `this` keyword](23-this-keyword.md)
