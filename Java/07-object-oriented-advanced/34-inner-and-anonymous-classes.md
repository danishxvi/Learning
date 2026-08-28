# 34 · Inner, Static Nested and Anonymous Classes

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/34-inner-and-anonymous-classes.java
> ```

Java has **four** kinds of nested class. They look similar and behave very differently —
and one of them is a genuine memory-leak source.

---

## 1. The four kinds

```java
class Outer {

    static class StaticNested { }      // 1. static nested
    class Inner { }                    // 2. inner (non-static nested)

    void method() {
        class LocalClass { }           // 3. local class
        Runnable r = new Runnable() {  // 4. anonymous class
            public void run() { }
        };
    }
}
```

| Kind | Holds an outer reference? | Can be created standalone? | Typical use |
| --- | :---: | :---: | --- |
| **Static nested** | **No** | Yes | Helper types, builders, nodes |
| **Inner** | **Yes** | No — needs an instance | Iterators, views |
| **Local** | Yes (if in an instance method) | Only inside that method | Rare |
| **Anonymous** | Yes (if in an instance method) | Only where declared | Listeners (pre-lambda) |

---

## 2. Static nested vs inner — the important difference

```java
class Outer {
    private String name = "outer";

    static class StaticNested {
        void show() {
            // System.out.println(name);   ERROR — no outer instance exists
        }
    }

    class Inner {
        void show() {
            System.out.println(name);      // fine — there IS an outer instance
        }
    }
}
```

Creating them differs too:

```java
Outer.StaticNested nested = new Outer.StaticNested();     // no Outer needed
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();                    // the odd `outer.new` syntax
```

That `outer.new Inner()` syntax exists precisely because an inner class **cannot exist
without an enclosing instance**.

### The hidden field

The compiler adds a synthetic field to every inner class:

```java
class Inner {
    final Outer this$0;        // synthetic — added by javac
}
```

You can see it with `javap -p`. That field is what makes `Outer.this` work — and it is what
causes the leak.

### A detail most material gets wrong

`javac` does **not** add `this$0` unconditionally. If an inner class never actually uses the
enclosing instance, the field is **elided**:

```java
class Outer {
    private String name;
    class UsesOuter    { String get() { return name; } }   // has this$0
    class NeverUsesOuter { int get() { return 42; } }      // NO this$0 — elided
}
```

The companion program proves this with reflection: one declares a field, the other declares
none.

**Do not rely on it.** It is a compiler optimisation, not a language guarantee; older
compilers did not do it; and the day someone adds a single reference to an outer field, the
leak silently reappears with no visible change at the call site. The rule stands: default to
`static`, so the intent is written down rather than deduced from the compiler's mood.

---

## 3. The memory leak

```java
class Screen {
    private byte[] bitmap = new byte[10_000_000];   // 10 MB

    class Listener {                                 // NON-static
        void onEvent() { }
    }
}

// somewhere global
static List<Screen.Listener> registry = new ArrayList<>();
registry.add(new Screen().new Listener());
```

The `Listener` is tiny, but it holds `this$0` → the whole `Screen` → the 10 MB array.
**Nothing in that chain can ever be collected** while the registry holds the listener.

This is a real and common leak — it is the standard explanation for Android `Activity`
leaks, and it happens in server code with callbacks and caches too.

**The fix is one word:** make the nested class `static` and pass what it actually needs.

> **Default nested classes to `static`.** Drop `static` only when the class genuinely needs
> the enclosing instance. *Effective Java* states this as a rule, and the reason is exactly
> this leak.

---

## 4. Anonymous classes

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("running");
    }
};
```

You are declaring a class and creating its only instance in one expression. The class has
no name — the compiler generates `Outer$1`, `Outer$2` and so on.

### What they can do that lambdas cannot

| | Anonymous class | Lambda |
| --- | --- | --- |
| Implement an interface with **many** methods | **Yes** | No — one abstract method only |
| Extend a **class** | **Yes** | No |
| Hold **state** (fields) | **Yes** | No |
| `this` refers to | **itself** | the **enclosing** object |
| Generated class file | **Yes** — `Outer$1.class` | No — `invokedynamic` |

The `this` difference is the one that bites when converting old code:

```java
class Example {
    Runnable anonymous = new Runnable() {
        public void run() { System.out.println(this); }   // the anonymous instance
    };

    Runnable lambda = () -> System.out.println(this);      // the Example instance
};
```

A lambda does **not** introduce a new scope for `this`. Covered in lesson 23.

### When to still use an anonymous class

- The interface has **more than one** abstract method.
- You need to **extend a class**, not implement an interface.
- You need a **field** to hold state between calls.
- You need `this` to mean the handler itself.

Otherwise, **use a lambda** — it is shorter, produces no class file, and reads better.

### The double-brace initialisation anti-pattern

```java
List<String> list = new ArrayList<>() {{      // DO NOT DO THIS
    add("a");
    add("b");
}};
```

This creates an **anonymous subclass of `ArrayList`** whose instance initialiser calls
`add`. It looks clever and it is a trap: it generates a class file, it holds a reference to
the enclosing instance, and it breaks `equals` with other lists because the runtime class
differs.

Use `List.of("a", "b")` or `new ArrayList<>(List.of("a", "b"))`.

---

## 5. Local classes

```java
void process(List<String> items) {
    class Validator {                 // declared inside a method
        boolean isValid(String s) { return !s.isBlank(); }
    }
    Validator validator = new Validator();
}
```

Scoped to the method, like a local variable. They can capture **effectively final** locals,
exactly like lambdas (lesson 31).

They are rare in modern code — a lambda, a method reference, or a private static method
almost always reads better. You will mostly meet them in older codebases.

---

## 6. Access rules — the surprising part

A nested class and its enclosing class can see **each other's private members**:

```java
class Outer {
    private int outerSecret = 1;

    static class Nested {
        private int nestedSecret = 2;
    }

    void peek(Nested n) {
        System.out.println(n.nestedSecret);    // legal!
    }
}
```

They are considered the same "top-level entity" for access purposes. The JVM has no such
concept, so historically javac generated **synthetic bridge methods** to make it work —
which is why `javap` on a nested class used to show methods you never wrote. Java 11's
*nest-based access control* (JEP 181) added real JVM support and removed those bridges.

Nested classes may use **all four** access modifiers, unlike top-level classes which may
only be `public` or package-private (lesson 30).

---

## 7. Where these appear in the JDK

**Static nested — `Map.Entry`:**

```java
public interface Map<K, V> {
    interface Entry<K, V> { K getKey(); V getValue(); }
}
```

Nested because an entry only makes sense in the context of a map, but a `HashMap.Node` does
not need a reference back to the map.

**Inner — iterators:**

```java
public class ArrayList<E> {
    private class Itr implements Iterator<E> {
        int cursor;
        public E next() { return elementData[cursor++]; }   // reaches the outer list
    }
}
```

This one genuinely needs the outer instance — the iterator's whole job is to walk *that*
list. A correct use of a non-static inner class.

**Static nested — builders:**

```java
new Pizza.Builder(12).cheese().build();
```

Covered in lesson 22.

---

## 8. Summary

- Four kinds: **static nested**, **inner**, **local**, **anonymous**.
- The critical difference: an **inner class holds a hidden `this$0` reference** to its
  enclosing instance; a static nested class does not.
- That hidden reference is a genuine **memory leak** when the inner object outlives the
  outer one. **Default nested classes to `static`.**
- Creating an inner class needs the odd `outer.new Inner()` syntax, because it cannot exist
  without an enclosing instance.
- Anonymous classes can implement **multi-method** interfaces, **extend classes**, and hold
  **state** — lambdas cannot. But `this` means the anonymous instance, not the enclosing
  object.
- **Prefer lambdas** for single-method interfaces.
- **Never use double-brace initialisation** — it creates an anonymous subclass with all the
  attendant problems.
- Nested and enclosing classes can access each other's `private` members; Java 11's
  nest-based access control made that a real JVM feature rather than a javac trick.
- Nested classes may use all four access modifiers.

---

**Previous:** [33 — `equals()` and `hashCode()`](33-equals-and-hashcode.md) ·
**Next:** [35 — Enums](35-enums.md)
