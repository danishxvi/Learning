# 25 · `static` Members and Static Blocks

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/25-static-members.java
> ```

`static` means **"belongs to the class, not to any object"**. One word, and it changes
where the data lives, when it is initialised, and what code can see it.

---

## 1. Static vs instance

```java
class Counter {
    static int totalCreated = 0;    // ONE copy, shared by every object
    int myNumber;                   // one copy PER object

    Counter() {
        totalCreated++;
        myNumber = totalCreated;
    }
}
```

| | `static` member | Instance member |
| --- | --- | --- |
| Belongs to | The **class** | Each **object** |
| Copies in memory | Exactly **one** | One per object |
| Created when | The class is **loaded** | The object is created |
| Accessed via | `Counter.totalCreated` | `counter.myNumber` |
| Can access | Only static members | Static **and** instance members |
| Exists without objects | **Yes** | No |

The asymmetry in "can access" is the rule that generates most compile errors:

```java
class Example {
    int instanceField;

    static void staticMethod() {
        System.out.println(instanceField);
        // ERROR: non-static variable instanceField cannot be referenced
        //        from a static context
    }
}
```

The reason is simple: a static method can be called before any object exists, so **there
is no `this`** and therefore no instance field to read. It is not an arbitrary restriction
— the question "which object's `instanceField`?" has no answer.

The reverse is always fine: an instance method can read static members, because by the time
an object exists the class is definitely loaded.

---

## 2. Why `main` is static

```java
public static void main(String[] args)
```

The JVM must call `main` **before any object of your class exists**. If `main` were an
instance method, the JVM would have to construct your class first — and it would have no
idea which constructor to use or what arguments to pass.

`static` is what makes `main` callable from nothing.

---

## 3. Accessing static members

```java
Counter.totalCreated;       // correct — via the CLASS
counter.totalCreated;       // legal, but MISLEADING — via an instance
```

Accessing a static member through an instance compiles and works, but reads as though it
were per-object. Most IDEs and linters flag it. **Always use the class name.**

The most confusing form:

```java
Counter counter = null;
System.out.println(counter.totalCreated);   // works! prints the value, no NPE
```

No exception, because the reference is never actually dereferenced — the compiler resolves
it to `Counter.totalCreated` at compile time. This is a genuinely surprising piece of
trivia and a good reason to never write it.

---

## 4. Static fields — the good and the dangerous

### Legitimate uses

**Constants:**

```java
public static final double PI = 3.14159;
public static final int MAX_CONNECTIONS = 10;
```

`static final` with a compile-time constant value is inlined by the compiler — there is no
runtime lookup at all.

**Counters and caches shared by all instances:**

```java
private static int instanceCount = 0;
private static final Map<String, Config> CACHE = new ConcurrentHashMap<>();
```

**Utility methods with no state:** `Math.max`, `Collections.sort`, `Arrays.toString`.

### The dangers

**1. Mutable static state is a global variable.**

```java
public static List<String> log = new ArrayList<>();
```

Any code anywhere can modify it. It makes tests order-dependent, because state leaks from
one test to the next. It is exactly the thing object orientation was invented to avoid.

**2. Mutable static state is not thread-safe.**

```java
private static int counter = 0;
counter++;      // NOT atomic — read, increment, write. Two threads can collide.
```

`counter++` compiles to three bytecode operations. Two threads running it concurrently can
both read `5`, both write `6`, and one increment vanishes. Lesson 63 covers this; the fix
is `AtomicInteger` or synchronisation.

**3. `static final` on a collection is not immutability.**

```java
public static final List<String> NAMES = new ArrayList<>();
NAMES.add("anyone can do this");     // perfectly legal
```

`final` prevents **reassignment**, never **mutation**. Use `List.of(...)` for a genuinely
immutable constant. Lesson 31 covers this.

**4. Statics are never garbage collected** while the class is loaded. A static `Map` that
only ever grows is a memory leak with no obvious owner.

---

## 5. Static blocks

```java
class Config {
    static final Map<String, String> DEFAULTS;

    static {
        DEFAULTS = new HashMap<>();
        DEFAULTS.put("host", "localhost");
        DEFAULTS.put("port", "8080");
    }
}
```

A static block runs **once**, when the class is initialised. Use it when a static field
needs more than one statement to set up.

**When does a class initialise?** On first *active use*:

- creating an instance,
- calling a static method,
- reading or writing a non-constant static field.

Critically, it does **not** initialise on:

- declaring a variable of that type (`Config c;`),
- reading a `static final` **compile-time constant** — the compiler inlined it, so the
  class is never touched.

That second exception surprises people, and it is why a `static final int` behaves
differently from a `static final Object`.

### Static initialisation is thread-safe for free

The JVM guarantees a class is initialised **exactly once**, with proper locking. This is
what makes the *initialization-on-demand holder* idiom the cleanest lazy singleton:

```java
class Singleton {
    private Singleton() {}

    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }

    static Singleton getInstance() {
        return Holder.INSTANCE;      // Holder loads on first call — lazily, safely
    }
}
```

No `synchronized`, no `volatile`, no double-checked locking. The JVM's class-initialisation
lock does all the work.

### If a static initialiser throws

You get `ExceptionInInitializerError`, and the class is marked **erroneous permanently**.
Every later attempt to use it throws `NoClassDefFoundError` — often with no hint of the
original cause. This is a genuinely confusing failure mode, so keep static initialisers
simple and never let them do risky I/O.

---

## 6. Static nested classes

```java
class Outer {
    static class Nested { }      // no reference to any Outer instance
    class Inner { }              // holds a hidden reference to an Outer
}

Outer.Nested nested = new Outer.Nested();      // no Outer needed
Outer.Inner inner = new Outer().new Inner();   // needs an Outer
```

**Default to `static` for nested classes.** A non-static inner class holds a hidden
reference to its enclosing instance, which prevents that instance from being garbage
collected and is a genuine memory-leak source. Make it non-static only when it genuinely
needs the outer object. Lesson 34 covers this.

---

## 7. Static methods cannot be overridden

```java
class Parent { static void greet() { System.out.println("Parent"); } }
class Child extends Parent { static void greet() { System.out.println("Child"); } }

Parent p = new Child();
p.greet();          // prints "Parent" — resolved by the DECLARED type
```

This is **hiding**, not overriding. Static methods are bound at compile time using the
declared type, exactly like overloading (lesson 18) and unlike overriding (lesson 27).

`@Override` on a static method is a compile error, which is the compiler telling you this
distinction is real.

**Practical rule:** never call a static method through an instance, and never hide a static
method in a subclass. Both read as polymorphism and are not.

---

## 8. When to use `static`

**Use it when:**

- The method does not use any instance state → make it `static` and say so.
- You need a genuine constant → `static final`.
- You are writing a utility class → all static, with a private constructor:

```java
public final class StringUtils {
    private StringUtils() {                       // prevent instantiation
        throw new AssertionError("no instances");
    }
    public static String reverse(String s) { ... }
}
```

- You need a factory method (lesson 22).

**Avoid it when:**

- The state is mutable and shared — that is a global variable.
- It makes testing hard — static state cannot be mocked or reset easily.
- You are reaching for it to avoid passing a dependency. That is usually a design problem,
  and it is why dependency injection frameworks exist.

---

## 9. Summary

- `static` means "belongs to the class". One copy, created at class load, usable with no
  objects.
- A static method **cannot** touch instance members — there is no `this`. The reverse is
  always fine.
- `main` is static because the JVM must call it before any object exists.
- Access statics through the **class name**. `instance.staticField` compiles and misleads;
  `nullRef.staticField` even works.
- **Mutable static state is a global variable** — not thread-safe, leaks between tests,
  never garbage collected.
- `static final` on a collection prevents reassignment, **not** mutation. Use `List.of`.
- Static blocks run **once**, on first active use. Reading a `static final` compile-time
  constant does **not** trigger initialisation.
- Class initialisation is thread-safe for free — hence the holder idiom for lazy singletons.
- A throwing static initialiser causes `ExceptionInInitializerError`, then
  `NoClassDefFoundError` forever after.
- Static methods are **hidden**, not overridden — resolved by the declared type.
- Default nested classes to `static`.

---

**Previous:** [24 — Encapsulation](24-encapsulation.md) ·
**Next:** [26 — Inheritance and `super`](26-inheritance-and-super.md)
