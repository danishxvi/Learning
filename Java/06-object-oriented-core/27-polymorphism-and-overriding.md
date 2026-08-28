# 27 · Polymorphism and Method Overriding

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/27-polymorphism-and-overriding.java
> ```

Polymorphism — "many forms" — is the mechanism that lets one line of code do different
things depending on what object it is holding. It is the reason object-oriented programming
exists as a distinct idea.

---

## 1. The one-sentence version

```java
Animal animal = new Dog();
animal.speak();          // calls Dog.speak(), not Animal.speak()
```

> **The method that runs is chosen by the object's *runtime* type, not by the reference's
> *declared* type.**

This is **dynamic dispatch** (or *late binding*, or *runtime polymorphism*). The JVM looks
at the actual object at the moment of the call and selects its version of the method.

Compare with everything else you have seen, which is chosen at **compile time**:

| Mechanism | Chosen by | When |
| --- | --- | --- |
| **Overriding** | The object's **runtime** type | **Runtime** |
| Overloading (lesson 18) | The argument's **declared** type | Compile time |
| Field access (lesson 26) | The reference's **declared** type | Compile time |
| Static methods (lesson 25) | The reference's **declared** type | Compile time |

**Overriding is the only one of the four that uses the runtime type.** Every confusing
behaviour in this area comes from expecting one of the other three to behave like
overriding.

---

## 2. Why it matters

Without polymorphism you write this:

```java
void makeSound(Object animal) {
    if (animal instanceof Dog)      System.out.println("woof");
    else if (animal instanceof Cat) System.out.println("meow");
    else if (animal instanceof Cow) System.out.println("moo");
    // add a new animal → edit this method, and every method like it
}
```

With polymorphism you write this:

```java
void makeSound(Animal animal) {
    animal.speak();
}
```

Add a new `Animal` subclass and **this method does not change**. That is the
**open/closed principle** — open for extension, closed for modification — and it is the
"O" in SOLID.

The `instanceof` chain is a genuine code smell. When you see one, ask whether the behaviour
belongs on the objects instead.

---

## 3. The rules of overriding

For a subclass method to **override** rather than merely coexist:

| Rule | Detail |
| --- | --- |
| **Same name** | Exactly |
| **Same parameter list** | Exactly — different parameters means *overloading*, not overriding |
| **Return type** | Same, or a **subtype** (covariant return) |
| **Access** | Same or **wider**. Cannot narrow |
| **Checked exceptions** | Same, narrower, or fewer. Cannot add new ones |
| **Not `static`** | Static methods are hidden, not overridden |
| **Not `final`** | `final` methods cannot be overridden |
| **Not `private`** | Private methods are not visible to subclasses |

### Covariant return types

```java
class Animal { Animal reproduce() { return new Animal(); } }
class Dog extends Animal {
    @Override Dog reproduce() { return new Dog(); }     // legal since Java 5
}
```

Returning a *more specific* type is allowed and useful — callers holding a `Dog` reference
get a `Dog` back without casting.

### Access can widen, never narrow

```java
class Animal { protected void eat() { } }
class Dog extends Animal {
    @Override public void eat() { }      // widening protected → public: fine
}

class Cat extends Animal {
    @Override private void eat() { }     // ERROR: attempting to assign weaker access
}
```

Narrowing would break substitutability: code holding an `Animal` reference expects to be
able to call `eat()`.

### Exceptions can only shrink

```java
class Animal { void eat() throws IOException { } }

class Dog extends Animal {
    @Override void eat() throws FileNotFoundException { }   // narrower: fine
    @Override void eat() { }                                // fewer: fine
    @Override void eat() throws SQLException { }            // ERROR: new checked exception
}
```

Same reasoning: a caller holding an `Animal` catches `IOException` and would be blindsided
by an `SQLException`.

**Unchecked exceptions are unrestricted** — any method can throw any `RuntimeException`
without declaring it.

---

## 4. `@Override` — always write it

```java
class Animal { void speak() { } }

class Dog extends Animal {
    void speek() { }          // TYPO — a new method, silently. Nothing warns you.
}

class Cat extends Animal {
    @Override
    void speek() { }          // COMPILE ERROR: method does not override
}
```

`@Override` has no runtime effect whatsoever. Its entire value is that the **compiler
checks your claim**. Without it, a typo, a wrong parameter type, or a superclass method
being renamed all produce a silently-not-overriding method.

This is one of the highest-value habits in Java. It costs one line and catches a whole
class of invisible bugs.

---

## 5. Overriding `Object` methods

Every class inherits `toString()`, `equals()`, `hashCode()` from `Object`. The classic
`equals` mistake:

```java
class Point {
    @Override
    public boolean equals(Point other) { ... }     // NOT an override — it OVERLOADS
}
```

`Object.equals` takes an `Object`, not a `Point`. This creates a *second* method and leaves
the inherited identity-comparison in place — so collections, which call `equals(Object)`,
use the wrong one. `@Override` catches this immediately. Lesson 33 covers the full contract.

---

## 6. How dynamic dispatch actually works

Each class has a **virtual method table** (vtable) — an array of pointers to its method
implementations. Every object header points at its class, and therefore at its vtable.

```
Dog object ──► Dog class ──► vtable
                              [0] speak() → Dog.speak
                              [1] eat()   → Animal.eat   (not overridden)
                              [2] fetch() → Dog.fetch
```

Calling `animal.speak()` compiles to `invokevirtual`, which looks up a fixed slot in
whatever vtable the object points at. It is a couple of memory reads — genuinely fast,
though not free.

### The performance question

The JIT optimises aggressively:

- **Monomorphic call site** (only ever one type seen): the JIT *inlines* the method, and
  the virtual call disappears entirely.
- **Bimorphic** (two types): a cheap type check plus two inlined bodies.
- **Megamorphic** (many types): a real vtable lookup, harder to optimise.

In practice, virtual dispatch is almost never your bottleneck. **Do not avoid polymorphism
for performance** — write it clearly and let the JIT do its job.

`final` on a class or method lets the JIT skip the lookup, but modern JITs detect this
themselves via class-hierarchy analysis. Use `final` for **design** reasons, not
performance.

---

## 7. Upcasting, downcasting and `instanceof`

```java
Dog dog = new Dog();
Animal animal = dog;              // UPCAST — implicit, always safe
Dog again = (Dog) animal;         // DOWNCAST — explicit, checked at runtime
Cat wrong = (Cat) animal;         // compiles, throws ClassCastException
```

The modern, safe pattern is **pattern matching for `instanceof`** (Java 16+):

```java
if (animal instanceof Dog dog) {
    dog.fetch();                  // already typed, already null-safe
}
```

`instanceof` returns `false` for `null`, so it guards against `NullPointerException` too.

### `instanceof` vs `getClass()`

```java
animal instanceof Animal          // true for Dog, Cat, and every subclass
animal.getClass() == Animal.class // true ONLY for an exact Animal
```

`instanceof` accepts subtypes; `getClass()` demands exactness. This distinction matters
enormously when writing `equals()` — lesson 33.

---

## 8. When polymorphism is the wrong tool

**Do not create a hierarchy for two cases.** A `boolean` or an enum is often clearer than
two subclasses.

**Do not use inheritance purely to share code.** That is what composition and static
helpers are for (lesson 26).

**Prefer interfaces to abstract classes for polymorphism.** A class can implement many
interfaces but extend only one class, so interfaces leave callers more room. Lessons 28
and 29.

**Sealed types (Java 17+) make `switch` a legitimate alternative.** When the set of
subtypes is fixed and known, a sealed hierarchy plus an exhaustive `switch` is
type-checked, keeps related logic in one place, and the compiler tells you when you miss a
case. That is a real alternative to spreading behaviour across subclasses. Lesson 37.

---

## 9. Summary

- **Overriding is resolved by the object's runtime type.** Overloading, field access and
  static methods are all resolved by the declared type.
- Polymorphism replaces `instanceof` chains, giving you the **open/closed principle**.
- To override: same name, same parameters, return type same or a **subtype**, access same
  or **wider**, checked exceptions same or **narrower**. Not `static`, `final` or `private`.
- **Always write `@Override`.** It has no runtime effect and catches typos, wrong
  signatures, and renamed superclass methods.
- `public boolean equals(Point other)` **overloads** rather than overrides — `@Override`
  catches it.
- Dispatch uses a **vtable** and `invokevirtual`. The JIT inlines monomorphic call sites, so
  virtual calls are rarely a bottleneck. Use `final` for design, not speed.
- Upcasts are implicit; downcasts are runtime-checked. Prefer
  `if (o instanceof Dog dog)`.
- `instanceof` accepts subtypes; `getClass() ==` demands exactness.

---

**Previous:** [26 — Inheritance and `super`](26-inheritance-and-super.md) ·
**Next:** [28 — Abstract classes](28-abstract-classes.md)
