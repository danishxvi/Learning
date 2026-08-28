# 18 · Method Overloading and Resolution Rules

> **Run the code for this lesson**
> ```bash
> java Java/05-methods/18-method-overloading.java
> ```

**Overloading** means several methods sharing one name but differing in parameters. It is
resolved entirely at **compile time**, by rules that are more intricate than most people
realise — and those rules are where the surprises live.

---

## 1. What makes a valid overload

Two methods may share a name if their **parameter lists differ** in:

- the **number** of parameters,
- the **types** of parameters,
- or the **order** of the types.

```java
void print(int x)                 // ok
void print(String s)              // ok — different type
void print(int x, int y)          // ok — different count
void print(int x, String s)       // ok
void print(String s, int x)       // ok — different ORDER
```

### What does *not* count

```java
int    process(String s)
String process(String s)          // ERROR — return type is not part of the signature

void   log(String message)
void   log(String text)           // ERROR — parameter NAMES are irrelevant

void   save(final String s)       // ERROR — `final` on a parameter is not part of it
```

The signature is **name + parameter types**, and nothing else. Not the return type, not
parameter names, not modifiers, not the `throws` clause.

---

## 2. Overloading vs overriding

These are constantly confused. They are unrelated mechanisms:

| | Overloading | Overriding |
| --- | --- | --- |
| Where | Same class (or inherited) | Subclass replaces a superclass method |
| Signature | **Must differ** | **Must be identical** |
| Return type | Must be compatible if signature same → error | Same, or a subtype (covariant) |
| Resolved | **Compile time** (static binding) | **Runtime** (dynamic binding) |
| Also called | Static / compile-time polymorphism | Dynamic / runtime polymorphism |
| Depends on | The **declared** type of the argument | The **actual** type of the object |

That last row is the one that causes real bugs, and §5 demonstrates it. Overriding is
lesson 27.

---

## 3. How the compiler picks an overload

When several overloads could apply, the compiler works in **three phases**, and stops at
the first phase that finds a match:

**Phase 1 — exact match or widening.** No boxing, no varargs.
```java
f(int)  ←  f(5)          exact
f(long) ←  f(5)          widening int → long
```

**Phase 2 — allow boxing/unboxing.** Only if phase 1 found nothing.
```java
f(Integer) ← f(5)        boxing int → Integer
f(Object)  ← f(5)        boxing then widening reference
```

**Phase 3 — allow varargs.** The last resort.
```java
f(int...) ← f(5)
```

### The consequence that surprises everyone

```java
static void show(long x)     { System.out.println("long"); }
static void show(Integer x)  { System.out.println("Integer"); }
static void show(int... x)   { System.out.println("varargs"); }

show(5);     // prints "long"
```

Not `Integer` — even though `5` is an `int` and `Integer` looks like the closer match.
**Widening beats boxing, and boxing beats varargs**, because phase 1 succeeds before
phase 2 is ever considered.

This ordering exists for backward compatibility: boxing and varargs arrived in Java 5, and
the rules had to guarantee that existing code kept choosing the same overloads it always
had.

### Most-specific wins within a phase

If several candidates match in the same phase, the **most specific** one is chosen:

```java
static void f(Object o)  { }
static void f(String s)  { }

f("hello");     // calls f(String) — String is more specific than Object
```

"More specific" means the parameter type can be passed to the other without a cast.

---

## 4. Ambiguity — when the compiler refuses

```java
static void f(int a, long b)  { }
static void f(long a, int b)  { }

f(1, 2);     // ERROR: reference to f is ambiguous
```

Neither is more specific: the first requires widening the second argument, the second
requires widening the first. Neither wins, so the compiler stops. Fix it with an explicit
cast: `f(1, (long) 2)`.

### `null` is famously ambiguous

```java
static void f(String s) { }
static void f(Integer i) { }

f(null);     // ERROR: ambiguous — null fits both
```

`null` is assignable to every reference type. When one candidate is a **subtype** of the
other it resolves fine (most specific wins), but two unrelated types are ambiguous. Cast
to disambiguate: `f((String) null)`.

---

## 5. The trap: overloading is resolved on the *declared* type

This is the most important practical point in the lesson.

```java
static void describe(Object o) { System.out.println("Object"); }
static void describe(String s) { System.out.println("String"); }

Object value = "I am really a String";
describe(value);        // prints "Object" — NOT "String"
```

The compiler sees only that `value` is *declared* `Object`. It has no idea what will
actually be in there at runtime, and overload resolution happens at compile time.

Compare with **overriding**, which uses the runtime type:

```java
Animal a = new Dog();
a.speak();              // calls Dog's speak() — the RUNTIME type wins
```

> **Overloading looks at the reference's declared type. Overriding looks at the object's
> actual type.**

This is why "overloading is compile-time polymorphism" is not just terminology — it
changes which code runs.

---

## 6. The classic gotchas

### `remove(int)` vs `remove(Object)` on a `List`

```java
List<Integer> list = new ArrayList<>(List.of(10, 20, 30));

list.remove(1);                      // remove(int index) → removes INDEX 1 (the value 20)
list.remove(Integer.valueOf(10));    // remove(Object) → removes the VALUE 10
```

`List` has both overloads, and an `int` literal matches `remove(int)` in phase 1. This is
a genuine, frequently-hit bug in production code.

### Autoboxing changes which method is called

```java
static void f(int x)     { System.out.println("int"); }
static void f(Integer x) { System.out.println("Integer"); }

f(5);                       // "int"      — exact match
f(Integer.valueOf(5));      // "Integer"  — exact match
```

Both compile, and which one runs depends on how you wrote the argument.

### Widening a `char`

```java
static void f(int x)   { }
static void f(Object o) { }

f('a');     // calls f(int) — char widens to int in phase 1
```

### `String` vs `StringBuilder`

```java
static void f(String s)       { }
static void f(CharSequence c) { }

f("hello");                        // f(String) — more specific
f(new StringBuilder("hello"));     // f(CharSequence) — StringBuilder is not a String
```

---

## 7. When to overload, and when not to

**Good uses** — the same operation on different input types:

```java
Math.max(int, int)
Math.max(long, long)
Math.max(double, double)

System.out.println(int)
System.out.println(String)
System.out.println(Object)
```

Every overload does *conceptually the same thing*. A caller never has to think about which
one runs.

**Bad uses** — overloads that behave differently:

```java
void process(String csv)     { /* parse CSV */ }
void process(String json)     // can't even compile — but the intent is the problem
void save(User u)            { /* to the database */ }
void save(User u, boolean b) { /* to a file if b */ }   // now the reader must guess
```

If two overloads do different things, give them different names. `parseCsv` and
`parseJson` are clearer than any amount of cleverness.

**A useful rule:** if you could not swap one overload for another without a caller
noticing a behavioural difference, they should not share a name.

### Prefer distinct names over boolean/`null` disambiguation

```java
// Hard to call
find(name, null);
find(null, id);

// Obvious
findByName(name);
findById(id);
```

---

## 8. Constructor overloading

Constructors overload by exactly the same rules:

```java
class Rectangle {
    Rectangle()                       { this(1, 1); }
    Rectangle(int side)               { this(side, side); }
    Rectangle(int w, int h)           { this.width = w; this.height = h; }
}
```

`this(...)` delegates to another constructor and **must be the first statement**. Chaining
to one "primary" constructor that does the real work avoids duplicating validation.
Lesson 22 covers this.

---

## 9. Summary

- An overload must differ in the **number, type, or order** of parameters. Return type,
  parameter names and modifiers are **not** part of the signature.
- Resolution happens in three phases: **widening → boxing → varargs**, stopping at the
  first that matches. That is why `show(5)` picks `show(long)` over `show(Integer)`.
- Within a phase, the **most specific** candidate wins.
- Two candidates that each require a different conversion are **ambiguous** — a compile
  error. Fix with a cast.
- `f(null)` is ambiguous between unrelated reference types.
- **Overloading uses the declared type; overriding uses the runtime type.** An
  `Object`-declared variable holding a `String` calls the `Object` overload.
- `list.remove(1)` removes by **index**; `list.remove(Integer.valueOf(1))` removes by
  **value**.
- Overload only when every version does the same thing conceptually. Otherwise use
  different names.

---

**Previous:** [17 — Methods and parameter passing](17-methods-and-parameter-passing.md) ·
**Next:** [19 — Varargs](19-varargs.md)
