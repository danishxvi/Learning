# 52 · Built-in Functional Interfaces

> **Run the code for this lesson**
> ```bash
> java Java/10-functional-java/52-functional-interfaces.java
> ```

`java.util.function` ships around 40 interfaces so real code rarely needs to write its
own. Every one of them is a variation on four shapes, plus primitive specializations that
exist for a real, measured reason.

---

## 1. The core four

| Interface | Shape | Method |
| --- | --- | --- |
| `Function<T, R>` | `T -> R` | `apply` |
| `Consumer<T>` | `T -> void` | `accept` |
| `Supplier<T>` | `() -> T` | `get` |
| `Predicate<T>` | `T -> boolean` | `test` |

```java
Function<String, Integer> length = String::length;   length.apply("hello");     // 5
Consumer<String> printer = s -> System.out.println(s); printer.accept("hi");     // prints, returns nothing
Supplier<String> greeting = () -> "generated on demand"; greeting.get();          // "generated on demand"
Predicate<String> isLong = s -> s.length() > 5;        isLong.test("greetings"); // true
```

Every other interface in `java.util.function` is a variation on one of these four: more
arguments (`Bi-`), a primitive instead of a boxed type (`Int-`/`Long-`/`Double-`), or both.

---

## 2. The `Bi-` variants: two arguments in

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;    add.apply(3, 4);          // 7
BiConsumer<String, Integer> logPair = (name, v) -> ...;         logPair.accept("count", 42);
BiPredicate<String, Integer> matchesLength = (s, len) -> s.length() == len;
matchesLength.test("hello", 5);   // true
```

---

## 3. `UnaryOperator`/`BinaryOperator`: same-type special cases

```java
UnaryOperator<String> shout = s -> s.toUpperCase() + "!";     // extends Function<T, T>
BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);  // extends BiFunction<T, T, T>
```

**Why they exist separately**: `List.replaceAll(UnaryOperator<E>)` and the reduction
helpers specifically need *input type == output type* — a plain `Function<T, R>` doesn't
encode that guarantee in its signature; `UnaryOperator<T>` does.

```java
List<String> words = new ArrayList<>(List.of("delta", "alpha", "charlie"));
words.replaceAll(shout);
// [DELTA!, ALPHA!, CHARLIE!]
```

---

## 4. Primitive specializations: the real cost of not using them

Lesson 42 covered autoboxing's cost in the abstract. Here it's paid on every single call
of a hot loop, 200,000,000 times, squaring `i % 1000`:

| Interface | Time |
| --- | --- |
| `Function<Integer, Integer>` (boxed) | 348 ms |
| `IntUnaryOperator` (primitive) | 225 ms |

`Function<Integer, Integer>` boxes the `int` argument into an `Integer` to call `apply()`,
then unboxes the returned `Integer` back for the sum — real work on every call (cached for
small values per lesson 42's `Integer` cache, but the cache doesn't cover the full `int`
range, and boxing/unboxing is still genuine overhead even when cached). Over hundreds of
millions of calls in a hot path, that ~35% gap is real.

`java.util.function` ships `IntFunction`, `ToIntFunction`, `IntPredicate`, `IntSupplier`,
`IntConsumer`, `IntUnaryOperator`, `IntBinaryOperator` — and the same set for `long` and
`double` — specifically so hot code never has to pay this tax.

---

## 5. The default methods that compose these interfaces

```java
Function<Integer, Integer> timesTwo = x -> x * 2;
Function<Integer, Integer> plusThree = x -> x + 3;

timesTwo.andThen(plusThree).apply(5);   // 13 - (5*2)+3 : andThen runs THIS first, then the argument
timesTwo.compose(plusThree).apply(5);   // 16 - (5+3)*2 : compose runs the ARGUMENT first, then this
```

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> startsWithA = s -> s.startsWith("A");

notEmpty.and(startsWithA).test("Apple");    // true
notEmpty.and(startsWithA).test("Banana");   // false
startsWithA.negate().test("Apple");         // false
```

```java
Consumer<String> log = s -> System.out.println("LOG: " + s);
Consumer<String> alsoUppercase = s -> System.out.println("UPPER: " + s.toUpperCase());
log.andThen(alsoUppercase).accept("hi");   // runs BOTH, in order
```

---

## 6. `@FunctionalInterface` is a compile-time check, nothing more

An interface with **exactly one abstract method** is a valid lambda target whether or not
it's annotated — `@FunctionalInterface` adds no runtime behavior at all. What it *does* do
is make the compiler **enforce** that promise. Real, reproducible proof:

```java
@FunctionalInterface
interface Broken {
    void first();
    void second();
}
```

```
javac TwoAbstractMethods.java
error: Unexpected @FunctionalInterface annotation
@FunctionalInterface
^
  Broken is not a functional interface
    multiple non-overriding abstract methods found in interface Broken
```

**Default and static methods do not count toward the limit** — this is why `Function` can
have default `andThen()`/`compose()` *and* still be a valid lambda target: it has exactly
one abstract method (`apply`), plus as many default/static methods as it wants.

---

## 7. Summary

- Everything in `java.util.function` is a variation on `Function`/`Consumer`/`Supplier`/`Predicate`:
  more arguments (`Bi-`) or a primitive specialization (`Int-`/`Long-`/`Double-`), or both.
- `UnaryOperator<T>`/`BinaryOperator<T>` exist as narrower special cases specifically to
  encode "input type equals output type" in the signature, which `Function`/`BiFunction`
  cannot express — required by APIs like `List.replaceAll`.
- Primitive specializations exist to avoid autoboxing on hot paths — measured here at
  roughly a 35% real slowdown (`Function<Integer,Integer>` vs `IntUnaryOperator`) over
  200,000,000 calls.
- `andThen`/`compose` (on `Function`) and `and`/`or`/`negate` (on `Predicate`) and
  `andThen` (on `Consumer`) are default methods that let small lambdas compose into
  larger ones without writing new classes.
- `@FunctionalInterface` is purely a compiler check that an interface has exactly one
  abstract method — verified here with the real error a second abstract method produces.
  Default and static methods never count against that limit.

---

**Previous:** [51 — Lambda expressions](51-lambda-expressions.md) ·
**Next:** [53 — Method references](53-method-references.md)
