# 43 · Generics, Wildcards and Type Erasure

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/43-generics.java
> ```

Generics move type errors from **runtime** to **compile time**. Everything strange about
them follows from one implementation decision: **erasure**.

---

## 1. The problem they solve

Before Java 5:

```java
List names = new ArrayList();
names.add("Danish");
names.add(42);                             // compiles happily
String name = (String) names.get(1);       // ClassCastException at runtime
```

Every read needed a cast, and every cast could fail. With generics:

```java
List<String> names = new ArrayList<>();
names.add("Danish");
names.add(42);                             // COMPILE ERROR
String name = names.get(0);                // no cast needed
```

The failure moved from a customer's screen to your editor.

---

## 2. Generic classes and methods

```java
class Box<T> {                             // T is a TYPE PARAMETER
    private T content;
    void put(T content) { this.content = content; }
    T get() { return content; }
}

Box<String> box = new Box<>();             // the diamond infers <String>
```

### Generic methods

The type parameter goes **before the return type**:

```java
static <T> void printAll(List<T> items) { ... }
static <T, R> List<R> map(List<T> source, Function<T, R> mapper) { ... }
```

Usually inferred: `printAll(names)`. Occasionally explicit:
`Collections.<String>emptyList()`.

### Conventional names

`T` type · `E` element · `K` key · `V` value · `R` result · `N` number · `?` wildcard

---

## 3. Bounded type parameters

```java
static <T extends Number> double sum(List<T> numbers) {
    double total = 0;
    for (T number : numbers) {
        total += number.doubleValue();     // allowed — T IS a Number
    }
    return total;
}
```

Without the bound, `T` erases to `Object` and you could only call `Object`'s methods.

**Multiple bounds** use `&`, with the class first if there is one:

```java
<T extends Comparable<T> & Serializable>
```

---

## 4. Erasure — the fact that explains everything

> **Generics exist only at compile time.** The compiler checks types, inserts casts, and
> then **erases** the type parameters. The bytecode contains no generic information.

```java
List<String> strings = new ArrayList<>();
List<Integer> integers = new ArrayList<>();
strings.getClass() == integers.getClass();   // TRUE — both are just ArrayList
```

`List<String>` becomes `List`; `T` becomes `Object`; `T extends Number` becomes `Number`.

### Why erasure?

**Backward compatibility.** Java 5 had to let generic and pre-generic code interoperate
without breaking the millions of existing class files. C# added generics later with a
breaking runtime change; Java chose not to. It is a defensible trade, and it costs you
everything below.

### What erasure forbids

```java
new T()                          // cannot instantiate a type parameter
new T[10]                        // cannot create a generic array
x instanceof List<String>        // cannot test a parameterised type
class MyEx<T> extends Exception  // a generic class cannot extend Throwable
static T field;                  // no static members of the class's type parameter

void f(List<String> l) { }
void f(List<Integer> l) { }      // ERROR: same erasure — both are f(List)
```

That last one is worth remembering: **two methods differing only in type arguments have
the same signature after erasure**, so they cannot coexist.

### Working around `new T()`

Pass a factory or a `Class` token:

```java
static <T> T create(Supplier<T> factory) { return factory.get(); }
create(ArrayList::new);
```

---

## 5. Generics are invariant

```java
List<String> strings = new ArrayList<>();
List<Object> objects = strings;          // COMPILE ERROR
```

`List<String>` is **not** a `List<Object>`, even though `String` is an `Object`.

**Why?** Because it would break:

```java
List<Object> objects = strings;   // if this were allowed...
objects.add(42);                  // ...you could put an Integer in
String s = strings.get(0);        // ...and get a ClassCastException here
```

Arrays *are* covariant and have exactly this hole — which is why
`Object[] o = new String[1]; o[0] = 42;` throws `ArrayStoreException` at runtime
(lesson 12). Generics moved that error to compile time deliberately.

---

## 6. Wildcards, and PECS

Invariance is safe but restrictive. Wildcards restore flexibility.

### `? extends T` — a **producer** (read from it)

```java
double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number n : numbers) total += n.doubleValue();   // READ: fine
    // numbers.add(1);                                    // WRITE: compile error
    return total;
}

sum(List.of(1, 2, 3));        // List<Integer> — accepted
sum(List.of(1.5, 2.5));       // List<Double>  — accepted
```

You can **read** as `Number`, but cannot **add** — the compiler does not know whether the
real list is `List<Integer>` or `List<Double>`.

### `? super T` — a **consumer** (write to it)

```java
void addNumbers(List<? super Integer> target) {
    target.add(1);                        // WRITE: fine
    // Integer x = target.get(0);         // READ: only Object is guaranteed
}

addNumbers(new ArrayList<Integer>());
addNumbers(new ArrayList<Number>());
addNumbers(new ArrayList<Object>());
```

### PECS

> **Producer Extends, Consumer Super.**

If the parameter **produces** values you read → `? extends T`.
If it **consumes** values you write → `? super T`.
If it does both → use a plain `T`.

The JDK follows it exactly:

```java
static <T> void copy(List<? super T> dest, List<? extends T> src)
Stream<T> filter(Predicate<? super T> predicate)
<R> Stream<R> map(Function<? super T, ? extends R> mapper)
```

### The unbounded wildcard

```java
void printSize(List<?> list) { ... }      // any list; you can only read Object
```

`List<?>` is not `List<Object>`: you can pass a `List<String>` to the first, not the
second. You may not add anything to a `List<?>` except `null`.

---

## 6a. Where to use wildcards

**Use them on parameters, not return types.** A wildcard return type forces wildcards on
every caller:

```java
List<? extends Number> getNumbers()      // now the caller has a restricted list
List<Number> getNumbers()                // better
```

---

## 7. Unchecked warnings and `@SuppressWarnings`

```java
List<String> list = (List<String>) someRawList;   // unchecked cast warning
```

The compiler is telling you it **cannot verify** the cast, because the type information was
erased. The runtime check you would expect does not happen.

Only suppress when you have **proved** the cast is safe, on the **narrowest possible
scope**, with a comment saying why:

```java
@SuppressWarnings("unchecked")   // safe: we only ever put Strings in
List<String> result = (List<String>) raw;
```

Compile with `-Xlint:unchecked` to see the details.

### Heap pollution

Covered in lesson 19: a generic varargs array can escape and be typed wrongly. Annotate
genuinely safe methods with `@SafeVarargs`.

---

## 8. Raw types — never use them

```java
List raw = new ArrayList();       // a RAW type
raw.add("string");
raw.add(42);                      // no complaint
```

Raw types exist **only** for pre-Java-5 compatibility. Using one turns off generic checking
for the whole variable — including for the elements you thought were safe.

Use `List<?>` when you genuinely do not care about the element type.

---

## 9. Summary

- Generics move `ClassCastException` from **runtime** to **compile time**, and remove casts.
- Type parameters go on the class (`class Box<T>`) or before a method's return type
  (`static <T> void f(...)`).
- **Bounds** (`<T extends Number>`) are what let you call methods on `T`.
- **Erasure**: generics are compile-time only. `List<String>` and `List<Integer>` are the
  same class at runtime. This was for backward compatibility.
- Erasure forbids `new T()`, `new T[]`, `instanceof List<String>`, generic `Throwable`
  subclasses, and two overloads differing only in type arguments.
- Generics are **invariant**: `List<String>` is not a `List<Object>`. Arrays are covariant
  and have the runtime hole to prove it was the right call.
- **PECS — Producer Extends, Consumer Super.** `? extends T` to read, `? super T` to write,
  plain `T` for both.
- Put wildcards on **parameters**, not return types.
- Suppress unchecked warnings only when you have proved safety, at the narrowest scope.
- **Never use raw types.** Use `List<?>` instead.

---

**Previous:** [42 — Wrapper classes](42-wrapper-classes-and-autoboxing.md) ·
**Next:** [44 — The Collections Framework](44-collections-framework-overview.md)
