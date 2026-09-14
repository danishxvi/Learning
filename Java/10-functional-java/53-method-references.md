# 53 · Method References

> **Run the code for this lesson**
> ```bash
> java Java/10-functional-java/53-method-references.java
> ```

There are four kinds of method reference, and the third — "unbound instance method
reference" — is the one nobody's intuition gets right the first time. This lesson works
through exactly what its "extra" parameter really is.

---

## 1. Kind 1: `ClassName::staticMethod`

```java
Function<String, Integer> parse = Integer::parseInt;
parse.apply("42");   // 42
```

Equivalent lambda: `s -> Integer.parseInt(s)`. The method is static; the reference just
points at it directly — no instance involved anywhere.

---

## 2. Kind 2: `instance::instanceMethod` — bound to one object

```java
String greeting = "Hello, World!";
Supplier<Integer> length = greeting::length;
length.get();   // 13
```

Equivalent lambda: `() -> greeting.length()`. `greeting` is a specific, already-existing
object — the reference is permanently **bound** to it, exactly like a lambda that
*captures* it.

---

## 3. Kind 3: `ClassName::instanceMethod` — unbound, the tricky one

```java
Function<String, Integer> length = String::length;
length.apply("hello");   // 5
```

Equivalent lambda: `s -> s.length()`. The key difference from Kind 2: there is **no
specific `String` yet**. `String::length` is a reference to the *method itself*. The
functional interface's **first parameter becomes the receiver** the method is called on,
and any remaining parameters become the method's own arguments. This generalizes to
methods that take arguments too:

```java
BiFunction<String, String, Boolean> startsWith = String::startsWith;
startsWith.apply("hello", "he");   // true
//                ^^^^^^^  ^^
//                receiver  startsWith's OWN argument
```

The first argument (`"hello"`) became the receiver — `"hello".startsWith(...)` — and the
second (`"he"`) became `startsWith`'s own parameter. This exact shape — an unbound
instance method reference used as a two-argument functional interface — is precisely how
`list.sort(String::compareTo)` works as a `Comparator<String>`:

```java
names.sort(String::compareTo);   // [alpha, charlie, delta]
```

`compareTo`'s receiver becomes the `Comparator`'s first argument, and its own parameter
becomes the second.

---

## 4. Kind 4: `ClassName::new`

```java
Supplier<ArrayList<String>> newList = ArrayList::new;
newList.get() != newList.get();   // true - a genuinely NEW instance every call
```

The constructor reference picks whichever **overload** matches the functional
interface's shape:

```java
Function<String, StringBuilder> newBuilder = StringBuilder::new;   // picks StringBuilder(String)
newBuilder.apply("seed").append("!");   // "seed!"
```

**Array constructor references** — `int[]::new`, `String[]::new` — exist specifically for
`Stream.toArray(IntFunction<T[]>)`, so a stream can allocate an array of exactly the right
size itself:

```java
IntFunction<String[]> arrayMaker = String[]::new;
arrayMaker.apply(3).length;   // 3
```

---

## 5. Overload resolution: the target type decides

`println()` has many overloads — `println(String)`, `println(int)`, `println(Object)`,
`println(boolean)`, and more. Which one does `System.out::println` resolve to? Whichever
matches the **target functional interface's** parameter type, resolved at **compile
time**, exactly like ordinary overload resolution (lesson 18):

```java
Consumer<String> printString = System.out::println;   // resolves to println(String)
IntConsumer printInt = System.out::println;             // resolves to println(int)
```

Same method reference expression, two different overloads chosen, purely from the
assignment's declared type. If the target type is genuinely ambiguous (matches more than
one overload equally well), that is a real compile error: `reference to println is ambiguous`.

---

## 6. Summary

| Kind | Form | Example |
| --- | --- | --- |
| 1. Static | `ClassName::staticMethod` | `Integer::parseInt` |
| 2. Bound instance | `instance::instanceMethod` | `greeting::length` |
| 3. Unbound instance | `ClassName::instanceMethod` | `String::length`, `String::compareTo` |
| 4. Constructor | `ClassName::new` | `ArrayList::new`, `String[]::new` |

- Kind 2 is bound to a *specific* object that already exists — like a lambda that
  captures a variable.
- Kind 3 has no object yet — the functional interface's first parameter supplies the
  receiver, and any remaining parameters become the referenced method's own arguments.
  This is exactly the mechanism behind `list.sort(String::compareTo)`.
- Kind 4 picks whichever constructor overload matches the functional interface's shape,
  including array constructors (`T[]::new`) for `Stream.toArray`.
- A method reference to an overloaded method resolves at compile time based on the
  target functional interface's declared signature — the same reference expression can
  resolve to different overloads in different contexts, and can be a genuine compile
  error if the target type is ambiguous.
- Method references compile via the same `invokedynamic` mechanism as lambdas (lesson
  51) — they are not anonymous classes either.

---

**Previous:** [52 — Built-in functional interfaces](52-functional-interfaces.md) ·
**Next:** [54 — The Stream API](54-streams-api.md)
