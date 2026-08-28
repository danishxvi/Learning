# 26 · Inheritance and `super`

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/26-inheritance-and-super.java
> ```

Inheritance lets one class build on another. It is the most **overused** feature in
object-oriented programming, so this lesson covers both how it works and — just as
important — when not to use it.

---

## 1. The mechanics

```java
class Animal {
    protected String name;

    Animal(String name) { this.name = name; }

    void eat() { System.out.println(name + " is eating"); }
}

class Dog extends Animal {
    Dog(String name) { super(name); }

    void bark() { System.out.println(name + " says woof"); }
}
```

`Dog` gets everything `Animal` has, plus its own additions. The relationship is
**"is-a"**: a `Dog` *is an* `Animal`, so a `Dog` can be used anywhere an `Animal` is
expected.

### What is inherited

| Member | Inherited? |
| --- | --- |
| `public` / `protected` fields and methods | **Yes** |
| Package-private members | Only within the same package |
| `private` members | **No** — they exist in the object but are not accessible |
| Constructors | **No** — but they are *called* via `super(...)` |
| Static members | Yes, but they are **hidden**, not overridden (lesson 25) |

> `private` members are not *inherited*, but they still **exist** in the subclass object.
> A `Dog` object contains `Animal`'s private fields; `Dog` code simply cannot name them.
> An inherited public getter can still read them.

---

## 2. Java has single inheritance

```java
class Dog extends Animal { }              // fine
class Dog extends Animal, Pet { }         // ERROR — only one superclass
```

A class may extend **exactly one** class, but implement **many interfaces**:

```java
class Dog extends Animal implements Pet, Comparable<Dog> { }
```

### Why single inheritance?

The **diamond problem**. If `C` extended both `A` and `B`, and both defined `greet()`,
which one does `C` get? C++ allows this and needs virtual inheritance to resolve it. Java
sidestepped the whole question.

Interfaces avoid the problem because (historically) they carried no implementation. Java 8
added `default` methods, which reintroduced it — so Java added a rule: if two interfaces
provide conflicting defaults, the implementing class **must** override the method. Lesson
29 covers this.

### Everything extends `Object`

```java
class Animal { }            // implicitly: class Animal extends Object
```

Every class has `Object` at the root, which is where `toString()`, `equals()`,
`hashCode()`, `getClass()` and the rest come from (lesson 32).

---

## 3. `super` — three jobs

### 1. Calling the superclass constructor

```java
Dog(String name) {
    super(name);        // MUST be the first statement
}
```

If you write neither `super(...)` nor `this(...)`, the compiler inserts `super()`. That
fails if the superclass has no no-arg constructor — the most common inheritance error
(lesson 22).

### 2. Calling the superclass version of an overridden method

```java
@Override
void eat() {
    super.eat();                       // do what Animal does...
    System.out.println("...noisily");  // ...then add to it
}
```

This is how you **extend** behaviour rather than replacing it. Without `super.eat()`, the
`Animal` version never runs.

Note `super.super.method()` is **not** legal — you can reach one level up, never two.

### 3. Accessing a shadowed superclass field

```java
class Parent { protected String name = "parent"; }
class Child extends Parent {
    protected String name = "child";        // SHADOWS, does not override

    void show() {
        System.out.println(name);           // "child"
        System.out.println(super.name);     // "parent"
    }
}
```

**Fields are shadowed, not overridden.** Both fields exist in the object simultaneously,
and which one you get depends on the **declared type of the reference**, not the runtime
type. This is a genuine trap, and the reason to keep fields `private`.

---

## 4. `protected` — more public than it looks

```java
class Animal {
    protected String name;      // subclasses can read AND WRITE this
}
```

`protected` means: same class, same package, **or any subclass anywhere**.

That last part matters. Anyone can write `class Evil extends Animal` and gain full access
to every `protected` member. So:

> **A `protected` member is part of your public API.** Once published, you can never remove
> or narrow it without breaking someone.

Prefer `private` fields with `protected` accessor methods, or just `private` with a
`public` getter. `protected` fields are almost always a mistake — they hand subclasses the
ability to corrupt state that the superclass is supposed to be guarding.

---

## 5. The `super()` / initialisation-order consequence

From lesson 22: the superclass constructor runs **before** the subclass's field
initialisers. This produces the bug worth repeating:

```java
class Animal {
    Animal() { describe(); }             // calls the OVERRIDDEN version
    void describe() { }
}

class Dog extends Animal {
    private String breed = "Labrador";
    @Override
    void describe() { System.out.println(breed); }   // prints null!
}
```

**Never call an overridable method from a constructor.**

---

## 6. When inheritance is wrong

This is the important half of the lesson.

### Rule: inherit for "is-a", compose for "has-a"

```java
class Car extends Engine { }        // WRONG — a car is not an engine
class Car { private Engine engine; } // RIGHT — a car HAS an engine
```

If the sentence "a `Subclass` is a `Superclass`" sounds wrong, inheritance is wrong.

### The classic counter-example: `Stack extends Vector`

The JDK's own `java.util.Stack` extends `Vector`, which means:

```java
Stack<Integer> stack = new Stack<>();
stack.push(1);
stack.push(2);
stack.add(0, 99);      // inherited from Vector — inserts at the BOTTOM
stack.remove(1);       // removes from the middle
```

A stack is supposed to allow only push and pop. By inheriting, it inherited every method
that breaks that guarantee. `Stack` is now effectively deprecated — use `ArrayDeque`
(lesson 48).

The same mistake appears in `Properties extends Hashtable`, which lets you store non-string
values in something that is supposed to hold only strings.

### The `Square extends Rectangle` problem

```java
class Rectangle {
    void setWidth(int w)  { this.width = w; }
    void setHeight(int h) { this.height = h; }
}

class Square extends Rectangle {
    void setWidth(int w)  { this.width = w; this.height = w; }   // must keep them equal
    void setHeight(int h) { this.width = h; this.height = h; }
}

void test(Rectangle r) {
    r.setWidth(5);
    r.setHeight(4);
    assert r.getArea() == 20;      // FAILS for a Square — the area is 16
}
```

Mathematically a square *is* a rectangle. In code, a mutable `Square` cannot substitute for
a mutable `Rectangle` without breaking callers.

This violates the **Liskov Substitution Principle**: *subtypes must be usable anywhere
their supertype is, without the caller noticing*. It is the "L" in SOLID, and it is the
single most useful test for whether an inheritance relationship is legitimate.

(Note the problem vanishes if both are **immutable** — with no setters, there is nothing to
break. Another argument for immutability.)

### The fragile base class problem

Changing a superclass can silently break subclasses you have never seen:

```java
class CountingSet<E> extends HashSet<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) { addCount++; return super.add(e); }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);        // HashSet.addAll internally calls add()!
    }
}

set.addAll(List.of("a", "b", "c"));    // addCount is 6, not 3
```

The subclass double-counts because it depended on an **implementation detail** of the
superclass — that `addAll` does not call `add`. Change that detail in a future JDK and the
subclass breaks.

**Composition fixes it:**

```java
class CountingSet<E> {
    private final Set<E> delegate = new HashSet<>();
    private int addCount = 0;

    public boolean add(E e) { addCount++; return delegate.add(e); }
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return delegate.addAll(c);     // whatever HashSet does internally is invisible
    }
}
```

---

## 7. Composition over inheritance

**Prefer composition.** The guidance from *Effective Java* is blunt: inherit only when

1. there is a genuine "is-a" relationship, **and**
2. the superclass was **designed and documented** for inheritance, **or** you control both.

Otherwise, hold an instance and delegate.

| | Inheritance | Composition |
| --- | --- | --- |
| Coupling | Tight — subclass depends on internals | Loose — only the public API |
| Changeable at runtime | No | **Yes** — swap the delegate |
| Multiple sources | One superclass | Any number of fields |
| Breaks when the base changes | **Often** | Rarely |
| Exposes the base's API | **All of it** | Only what you choose |

`final` on a class prevents inheritance entirely, which is the right default for classes
not designed to be extended (lesson 31).

---

## 8. Summary

- `extends` creates an **is-a** relationship; the subclass inherits accessible members.
- `private` members are not inherited but still **exist** in the object.
- Java has **single** class inheritance (the diamond problem) but multiple interface
  implementation. Everything extends `Object`.
- `super` does three jobs: call the superclass constructor, call the superclass version of
  an overridden method, and reach a shadowed field. `super.super` does not exist.
- **Fields are shadowed, not overridden** — resolved by the declared type. Keep fields
  private.
- `protected` is effectively public, because anyone can subclass. It is a permanent API
  commitment.
- Never call an overridable method from a constructor.
- Use inheritance for **is-a**, composition for **has-a**. If the sentence sounds wrong, it
  is wrong.
- `Stack extends Vector` and `Square extends Rectangle` are the canonical failures;
  Liskov Substitution is the test.
- The **fragile base class** problem means subclasses can break when a superclass's
  internals change. Composition avoids it.

---

**Previous:** [25 — `static` members](25-static-members.md) ·
**Next:** [27 — Polymorphism and overriding](27-polymorphism-and-overriding.md)
