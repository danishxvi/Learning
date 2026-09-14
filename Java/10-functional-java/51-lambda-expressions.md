# 51 · Lambda Expressions

> **Run the code for this lesson**
> ```bash
> java Java/10-functional-java/51-lambda-expressions.java
> ```

"A lambda is just shorthand for an anonymous class" is the most common wrong intuition
about this feature. This lesson proves, with real compiled bytecode, that it isn't — and
that difference explains why lambdas were worth adding to Java 8 as a real language
feature instead of just being sugar.

---

## 1. Every shape

```java
() -> System.out.println("no parameters")

x -> x * 2                 // parens OPTIONAL for exactly one inferred-type parameter
(x) -> x * 2                // parens allowed too - same thing

(a, b) -> a + b              // multiple params NEED the parens

x -> {                        // block body - explicit `return` REQUIRED
    int doubled = x * 2;
    return doubled + 1;
}

(int a, int b) -> a * b        // explicit parameter types allowed, rarely needed
```

---

## 2. What a lambda actually is — not an anonymous class

Real, reproducible proof, compiled with plain `javac` (not the single-file launcher, which
compiles in memory and never writes `.class` files to disk):

```java
// ClassFileCheck.java
Runnable anon   = new Runnable() { public void run() { System.out.println("anonymous class"); } };
Runnable lambda = () -> System.out.println("lambda");
```

```bash
javac ClassFileCheck.java && ls
```
```
ClassFileCheck.class      <- the outer class
ClassFileCheck$1.class    <- the ANONYMOUS CLASS, a real .class file
```

**Nothing else.** No separate `.class` file for the lambda at all. And looking at the
bytecode:

```bash
javap -c ClassFileCheck.class | grep invokedynamic
```
```
invokedynamic #10, 0   // InvokeDynamic #0:run:()Ljava/lang/Runnable;
```

An anonymous class compiles to a real, separate, named `.class` file — loaded and
instantiated as ordinary bytecode, exactly like any other class. A lambda compiles to a
**single `invokedynamic` instruction**. The JVM defers the actual decision of *how* to
implement it to a runtime bootstrap method (`LambdaMetafactory`), which generates the
implementation class on the fly, the first time that call site is reached — and can cache
or reuse it.

This is the real "why": no per-lambda `.class` file bloat (a codebase with thousands of
lambdas doesn't ship thousands of extra class files), and the JVM has more freedom to
optimize lambda dispatch than it does with a fixed, pre-compiled anonymous class.

---

## 3. Captured variables must be effectively final

```java
int base = 10;   // never reassigned after this -> "effectively final"
Supplier<Integer> addsToBase = () -> base + 5;   // compiles fine, captures the VALUE
addsToBase.get();   // 15
```

The real compile error the moment a captured variable **is** reassigned:

```java
int counter = 0;
Runnable r = () -> { counter++; System.out.println(counter); };
```

```
javac CaptureTest.java
error: local variables referenced from a lambda expression must be final or effectively final
        counter++;
        ^
(and a second, identical error for the println(counter) line — one per USE)
```

**Why**: the lambda may be handed to another thread, or simply called long after this
method returns and its local variables are gone from the stack. A lambda captures the
*value* at creation time (or a reference, for objects) — not a live connection to the
variable's storage — so the compiler forbids anything that would make that distinction
observable.

**The workaround**, when a running total is genuinely needed — an effectively-final
*reference* to a *mutable* object:

```java
int[] mutableBox = {0};             // the ARRAY REFERENCE is effectively final
Runnable incrementer = () -> mutableBox[0]++;   // its CONTENTS are not
```

Real result after three calls: `mutableBox[0]` is `3`. The array reference itself never
changed — only what it points to.

---

## 4. A lambda body shares its enclosing scope

An anonymous class body is a **new scope** — it can freely declare a local variable with
the same name as one in the enclosing method. A lambda body **cannot**; it shares the
enclosing scope, so reusing a name is a real, documented compile error:

```java
int value = 5;
IntUnaryOperator op = (value2) -> {
    int value = value2 * 2;   // SAME name as the enclosing local
    return value;
};
```

```
javac ShadowTest.java
error: variable value is already defined in method main(String[])
        int value = value2 * 2;
            ^
```

This is a real, useful safety property: it's impossible to accidentally shadow an
enclosing variable inside a lambda the way you can inside a nested class (or even a
nested block, in some other languages). The compiler catches the name collision
immediately.

---

## 5. `this` means something different in each

```java
class LambdaExpressions {
    private int instanceCounter = 100;

    void demonstrateThis() {
        Runnable anonymousClass = new Runnable() {
            public void run() {
                // this.getClass().getSimpleName() -> ""  (anonymous classes have no simple name)
            }
        };

        Runnable lambda = () -> {
            // this.instanceCounter is DIRECTLY readable - 100
            // this.getClass().getSimpleName() -> "LambdaExpressions" (the ENCLOSING class)
        };
    }
}
```

Real, measured confirmation: inside the lambda, `this.getClass().getSimpleName()` printed
`"LambdaExpressions"` — the *enclosing* class — and `this.instanceCounter` was directly
readable without any qualification. Inside the anonymous class, `this.getClass()` names
the (nameless) anonymous class itself.

**A lambda has no `this` of its own** — it captures the enclosing instance's `this`,
exactly like a regular nested block would. An anonymous class is a real class with its
own identity, so its `this` refers to *itself* — which is also why an anonymous class
needs `OuterClass.this.field` to reach an enclosing field with a name collision, something
a lambda never needs.

---

## 6. Summary

- Lambda syntax has several shapes — no-parens for exactly one inferred parameter,
  required parens for zero or multiple, an expression body or a `{ }` block body with an
  explicit `return`, and optional explicit parameter types.
- A lambda is **not** compiled the way an anonymous class is: real, verified proof shows
  an anonymous class produces its own `.class` file, while a lambda compiles to a single
  `invokedynamic` instruction resolved at runtime via `LambdaMetafactory`.
- Captured local variables must be effectively final — reassigning one inside a lambda is
  a real compile error, verified with the exact `javac` message. A mutable box (an
  array, or a small holder object) is the standard workaround when a running value is
  genuinely needed.
- A lambda body shares its enclosing scope; redeclaring a name already in scope is a
  compile error — unlike an anonymous class body, which is a genuinely new scope.
- `this` inside a lambda refers to the *enclosing* instance; `this` inside an anonymous
  class refers to the anonymous class instance itself — confirmed here by reading
  `this.getClass().getSimpleName()` from both.

---

**Previous:** [50 — Iterators and the `Collections` utility class](../09-generics-and-collections/50-iterators-and-collections-utility.md) ·
**Next:** [52 — Built-in functional interfaces](52-functional-interfaces.md)
