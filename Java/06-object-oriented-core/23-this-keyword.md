# 23 · The `this` Keyword

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/23-this-keyword.java
> ```

`this` is a reference to **the object whose method is currently running**. It is small,
and it does five distinct jobs.

---

## 1. The definition

Inside any instance method or constructor, `this` refers to the current object:

```java
class Counter {
    private int count;

    void increment() {
        this.count++;        // `this` is the Counter you called increment() on
    }
}
```

`this` is:

- **Implicit** — `count++` and `this.count++` are identical.
- **Never `null`** — you cannot be inside a method of an object that does not exist.
- **Unavailable in `static` context** — `static` methods belong to the class, and no
  particular object is running. Using `this` there is a compile error.
- **Effectively `final`** — you cannot assign to it.

---

## 2. Job 1: disambiguating a shadowed field

This is the reason you will use `this` most often:

```java
class Person {
    private String name;

    Person(String name) {
        this.name = name;    // this.name = the FIELD; name = the PARAMETER
    }
}
```

Without `this`, the parameter **shadows** the field and `name = name;` assigns the
parameter to itself — a no-op that leaves the field `null`:

```java
Person(String name) {
    name = name;             // does nothing at all. Field stays null.
}
```

This compiles with no error. Some IDEs warn ("assignment to itself"), but `javac` does not
by default. It is a classic silent bug.

### Why not just rename the parameter?

You could write `Person(String personName)`. But matching the field name is the widespread
convention, because:

- The parameter name appears in Javadoc and IDE hints, and `name` is the honest name.
- It makes the constructor's intent obvious.
- `this.` is a two-character cost.

---

## 3. Job 2: calling another constructor — `this(...)`

Covered in lesson 22:

```java
Rectangle() {
    this(1, 1);          // MUST be the first statement
}
```

Note that `this(...)` with parentheses (constructor delegation) is a completely different
thing from `this.field` (member access). They share a keyword and nothing else.

---

## 4. Job 3: passing the current object

```java
class Button {
    void register(EventBus bus) {
        bus.subscribe(this);      // hand the bus a reference to me
    }
}
```

This is how callbacks, listeners and registries work. It is also where a genuine hazard
lives.

### Do not let `this` escape from a constructor

```java
class Widget {
    Widget(EventBus bus) {
        bus.register(this);       // DANGEROUS
        this.name = "widget";     // the bus may already have used a half-built object
    }
}
```

The object is not fully constructed until the constructor returns. Publishing `this`
before that point means another thread — or even the same thread via a callback — can
observe fields that are still `null`, and `final` fields whose values are not yet
guaranteed visible.

This is the same family of bug as calling an overridable method from a constructor
(lesson 22). Register **after** construction:

```java
Widget widget = new Widget();
bus.register(widget);
```

Or use a static factory that constructs first, then registers.

---

## 5. Job 4: returning `this` for method chaining

```java
class Builder {
    Builder cheese()   { this.cheese = true; return this; }
    Builder mushroom() { this.mushroom = true; return this; }
}

new Builder().cheese().mushroom().build();
```

Returning `this` is what makes **fluent interfaces** work — builders, `StringBuilder`,
stream pipelines. Each call returns the same object so the next call can be appended.

Note `StringBuilder.append` does exactly this, which is why
`sb.append("a").append("b")` works.

---

## 6. Job 5: reaching the outer instance from an inner class

```java
class Outer {
    private String name = "outer";

    class Inner {
        private String name = "inner";

        void print() {
            System.out.println(name);              // "inner"
            System.out.println(this.name);         // "inner"
            System.out.println(Outer.this.name);   // "outer"  ← qualified this
        }
    }
}
```

`Outer.this` is **qualified `this`**, and it exists only for inner (non-static nested)
classes. It is how an inner class reaches the object that created it.

This also explains a memory leak: **a non-static inner class holds a hidden reference to
its outer instance.** If the inner object outlives the outer one — stored in a static
registry, say — the outer object can never be collected. Making the nested class `static`
removes that reference. Lesson 34 covers this fully.

---

## 7. `this` in lambdas vs anonymous classes

This distinction matters and surprises people:

```java
class Example {
    void run() {
        Runnable anonymous = new Runnable() {
            public void run() {
                System.out.println(this);   // the ANONYMOUS class instance
            }
        };

        Runnable lambda = () -> {
            System.out.println(this);       // the ENCLOSING Example instance
        };
    }
}
```

- An **anonymous class** is a real class with its own instance, so `this` is that instance.
- A **lambda** is not a class. It does not introduce a new scope for `this`, so `this`
  means whatever it meant in the surrounding code.

Lambdas are said to be **lexically scoped** for `this`. This is one of the practical
differences between the two, and it is a common source of confusion when converting
anonymous classes to lambdas. Lessons 34 and 51.

---

## 8. When to write `this.` explicitly

The compiler does not need it except in the shadowing case. Style guides differ:

| Situation | Use `this.`? |
| --- | --- |
| Field shadowed by a parameter | **Required** |
| Constructor delegation | **Required** (`this(...)`) |
| Passing the object | **Required** |
| Returning for chaining | **Required** |
| Ordinary field access | Optional — team preference |
| Calling another instance method | Optional — usually omitted |

A reasonable default: **use `this.` where it is required, and for field assignment in
setters and constructors; omit it elsewhere.** Consistency within a codebase matters more
than which convention you pick.

---

## 9. Summary

- `this` is a reference to the object whose method is currently running. It is implicit,
  never `null`, and unavailable in `static` context.
- Its main job is disambiguating a **shadowed field**: `this.name = name`. Omitting it
  gives a silent self-assignment that leaves the field `null`.
- `this(...)` delegates to another constructor and must be the first statement — a
  different feature from `this.field`.
- Passing `this` enables callbacks — but **never let `this` escape a constructor**, because
  the object is not finished yet.
- Returning `this` is what makes fluent/builder APIs chain.
- `Outer.this` reaches the enclosing instance from an inner class — and that hidden
  reference is a real memory-leak source.
- In an **anonymous class** `this` is that instance; in a **lambda** `this` is the
  enclosing object. Lambdas do not introduce a new `this`.

---

**Previous:** [22 — Constructors](22-constructors.md) ·
**Next:** [24 — Encapsulation](24-encapsulation.md)
