# 17 · Methods and How Arguments Are Really Passed

> **Run the code for this lesson**
> ```bash
> java Java/05-methods/17-methods-and-parameter-passing.java
> ```

Methods are how you give a name to a piece of behaviour. The syntax is small. The part
that causes real confusion — and appears in almost every interview — is **how arguments
are passed**, so that gets half this lesson.

---

## 1. Anatomy of a method

```java
public static int add(int a, int b) {
    return a + b;
}
//  ^      ^     ^    ^       ^        ^
//  |      |     |    |       |        └── body
//  |      |     |    |       └── parameter list
//  |      |     |    └── method name
//  |      |     └── return type
//  |      └── static: belongs to the class, not an object
//  └── access modifier
```

| Part | Notes |
| --- | --- |
| Access modifier | `public`, `private`, `protected`, or none (package-private) |
| `static` | Optional. Belongs to the class rather than an instance |
| Return type | A type, or `void` for "returns nothing". **Mandatory** |
| Name | `camelCase`, conventionally a verb: `calculateTotal`, `isValid` |
| Parameters | Zero or more `type name` pairs, comma-separated |
| Body | Braces. Required unless the method is `abstract` or `native` |

### The signature

A method's **signature** is its **name plus parameter types** — and nothing else:

```java
int    process(String s)      // signature: process(String)
String process(String s)      // SAME signature — will not compile alongside the first
```

The **return type is not part of the signature**. Two methods differing only in return
type are a compile error. This matters for overloading (lesson 18).

---

## 2. Parameters vs arguments

```java
static int square(int number) { ... }    // `number` is a PARAMETER
square(5);                               // `5` is an ARGUMENT
```

The distinction is worth keeping straight because the pass-by-value discussion below
depends on it: the argument's *value* is copied into the parameter.

---

## 3. Java is pass-by-value. Always.

This is the single most misunderstood thing in Java. Say it precisely:

> **Java passes everything by value. For a reference type, the *value being copied* is
> the reference — not the object.**

There is no pass-by-reference in Java, unlike C++ (`int&`) or C# (`ref`).

### Primitives — obviously copied

```java
static void change(int x) {
    x = 99;
}

int value = 5;
change(value);
System.out.println(value);     // 5 — unchanged
```

`x` is a separate variable holding a copy of `5`. Reassigning it does nothing to `value`.

### Objects — the confusing case

```java
static void modify(StringBuilder sb) {
    sb.append(" world");       // MUTATES the shared object
}

StringBuilder text = new StringBuilder("hello");
modify(text);
System.out.println(text);      // "hello world" — it DID change!
```

This is what makes people say "objects are passed by reference". They are not. What
happened is:

1. `text` holds a *reference* (an address) to a `StringBuilder` on the heap.
2. That **reference is copied** into the parameter `sb`.
3. Both `text` and `sb` now point at the **same object**.
4. `sb.append(...)` mutates the object both of them see.

The proof that it is still pass-by-value is **reassignment**:

```java
static void reassign(StringBuilder sb) {
    sb = new StringBuilder("something else");   // repoints the LOCAL copy only
}

StringBuilder text = new StringBuilder("hello");
reassign(text);
System.out.println(text);      // "hello" — unchanged!
```

If Java were pass-by-reference, `text` would now point at the new object. It does not.

### The mental model

```
BEFORE THE CALL                    INSIDE THE METHOD
┌──────────┐                       ┌──────────┐   ┌──────────┐
│  text    │──────┐                │  text    │──►│  sb      │──┐
└──────────┘      ▼                └──────────┘   └──────────┘  │
             ┌─────────┐                                        ▼
             │ "hello" │◄───────────────────────────────┌─────────┐
             └─────────┘                                │ "hello" │
                                                        └─────────┘
                                        two references, ONE object
```

Mutating the object → both see it. Reassigning a reference → only the local one moves.

### The one-line rule

> **A method can change what an object *contains*, but never what the caller's variable
> *points at*.**

### Why `String` seems different

```java
static void change(String s) { s = "changed"; }
```

`String` is immutable, so there is no mutating method to call. Every operation reassigns
the local copy. That is why strings *look* pass-by-value in a way that `StringBuilder`
does not — but the passing mechanism is identical.

---

## 4. Returning values

```java
static int add(int a, int b) {
    return a + b;
}

static void log(String message) {
    System.out.println(message);
    return;              // optional in a void method — bare `return` exits early
}
```

Rules:

- A non-`void` method **must** return on every path, or it is a compile error
  (*"missing return statement"*).
- `return` immediately exits the method, skipping everything after it.
- Code after an unconditional `return` is a compile error (*"unreachable statement"*).

### Returning multiple values

Java has no tuples. Four options, roughly in order of preference:

```java
// 1. A record — best since Java 16
record MinMax(int min, int max) {}
static MinMax findRange(int[] a) { return new MinMax(lo, hi); }

// 2. A small class — the same idea, pre-16
// 3. An array — only when the values share a type and meaning
static int[] findRange(int[] a) { return new int[]{lo, hi}; }

// 4. A Map or List — usually a design smell
```

Records make this so cheap that "I need to return two things" is no longer a reason to
reach for an array. Lesson 36 covers them.

---

## 5. Method design

### Do one thing

A method that validates, transforms, saves and emails is four methods. The strongest
signal is the name: if you need "and" to describe it, split it.

### Keep the parameter list short

| Parameters | Verdict |
| --- | --- |
| 0–2 | Ideal |
| 3 | Acceptable |
| 4+ | Usually means a parameter object is missing |

```java
// Hard to call correctly — which boolean is which?
createUser("Danish", "d@x.com", 25, true, false, true);

// Self-documenting
createUser(new UserDetails("Danish", "d@x.com", 25));
```

Long parameter lists of the same type are especially dangerous, because swapping two
arguments compiles fine and fails silently.

### Avoid boolean parameters

```java
render(true);                  // true what?
renderExpanded();              // clear
```

A boolean parameter almost always means the method does two things.

### Return early

Covered in lesson 08 — guard clauses beat nesting.

### Do not return `null` for collections

```java
// BAD — forces every caller to null-check
static List<Item> findItems() { return null; }

// GOOD
static List<Item> findItems() { return List.of(); }
```

An empty collection lets the caller loop unconditionally. For a single value that may be
absent, return `Optional<T>` (lesson 56).

---

## 6. `static` vs instance methods — a preview

```java
static int add(int a, int b) { ... }     // called on the CLASS: Math.add(1, 2)
int getBalance() { ... }                 // called on an OBJECT: account.getBalance()
```

- A `static` method belongs to the class and **cannot** access instance fields or `this`.
- An instance method belongs to an object and can access both.
- `main` is `static` because the JVM calls it before any object exists.

Rule of thumb: if a method does not use any instance state, it can be `static` — and
making it so documents that fact. Lesson 25 covers this properly.

---

## 7. Recursion — a preview

A method may call itself:

```java
static int factorial(int n) {
    if (n <= 1) return 1;          // base case — MANDATORY
    return n * factorial(n - 1);   // recursive case
}
```

Without a base case you get `StackOverflowError`. Lesson 20 covers recursion in full.

---

## 8. Summary

- A method's **signature** is its name plus parameter types. The **return type is not
  part of it**.
- **Java is always pass-by-value.** For objects, the *reference* is what gets copied.
- A method **can** mutate the object a parameter points at; it **cannot** change what the
  caller's variable points at.
- `String` only *seems* different because it is immutable — the mechanism is identical.
- A non-`void` method must return on every path; code after an unconditional `return` is
  unreachable and a compile error.
- Java has no tuples — return a **record** when you need several values.
- Keep methods to one job, parameter lists short, and avoid boolean parameters.
- Return an **empty collection**, never `null`; return `Optional` for a maybe-absent value.

---

**Previous:** [16 — The `Arrays` class](../04-arrays-and-strings/16-arrays-utility-class.md) ·
**Next:** [18 — Method overloading](18-method-overloading.md)
