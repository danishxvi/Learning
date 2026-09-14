# 54 · The Stream API

> **Run the code for this lesson**
> ```bash
> java Java/10-functional-java/54-streams-api.java
> ```

This lesson proves laziness with actual print-ordering evidence rather than a diagram,
proves a stream is single-use with a real exception, and breaks a parallel stream on
purpose to show why "no side effects" in stream operations is a real rule, not a style
preference.

---

## 1. Creating streams

```java
List.of(1, 2, 3).stream()
Stream.of("a", "b", "c")
Arrays.stream(new int[]{4, 5, 6})
IntStream.range(0, 5)          // EXCLUSIVE upper bound: [0,1,2,3,4]
IntStream.rangeClosed(0, 5)    // INCLUSIVE upper bound: [0,1,2,3,4,5]
Stream.iterate(1, x -> x * 2).limit(5)   // [1, 2, 4, 8, 16]
```

---

## 2. Laziness, proven with real print order

A pipeline with `filter().map()` does **nothing** until a terminal operation is called.
Watching real execution order via `peek()` (which prints every time an element genuinely
flows through a stage):

```java
Stream<Integer> pipeline = Stream.of(1, 2, 3, 4, 5)
        .filter(n -> { System.out.println("filter examining " + n); return n % 2 == 0; })
        .peek(n -> System.out.println("passed filter: " + n))
        .map(n -> n * 10);
// NOTHING printed yet - the pipeline is only a description
```

```java
List<Integer> result = pipeline.toList();   // NOW it runs
```

Real output:

```
filter examining 1
filter examining 2
passed filter: 2
filter examining 3
filter examining 4
passed filter: 4
filter examining 5
result -> [20, 40]
```

Two things confirmed at once: nothing ran before the terminal operation, and processing
is **element-at-a-time** — element `1` was examined, failed the filter, and the pipeline
moved straight to examining `2`, rather than filtering everything first and then mapping
everything. Each element flows through the *entire* pipeline before the next one starts.

---

## 3. The core intermediate operations

```java
List<Integer> nums = List.of(5, 3, 8, 3, 1, 9, 5, 2);

nums.stream().filter(n -> n > 3).toList();     // [5, 8, 9, 5]
nums.stream().map(n -> n * n).toList();         // [25, 9, 64, 9, 1, 81, 25, 4]
nums.stream().distinct().toList();              // [5, 3, 8, 1, 9, 2]
nums.stream().sorted().toList();                // [1, 2, 3, 3, 5, 5, 8, 9]
nums.stream().limit(3).toList();                // [5, 3, 8]
nums.stream().skip(3).toList();                 // [3, 1, 9, 5, 2]
```

`flatMap` flattens a stream of collections into one stream:

```java
List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
nested.stream().map(List::stream);       // Stream<Stream<Integer>> - not flat
nested.stream().flatMap(List::stream);   // [1, 2, 3, 4, 5] - genuinely flat
```

---

## 4. The core terminal operations

```java
nums.stream().filter(n -> n > 3).count();    // 4
nums.stream().anyMatch(n -> n > 8);           // true
nums.stream().allMatch(n -> n > 0);           // true
nums.stream().noneMatch(n -> n > 100);        // true
nums.stream().filter(n -> n > 3).findFirst(); // Optional[5]
nums.stream().max(Integer::compareTo);        // Optional[9]
nums.stream().reduce(0, Integer::sum);        // 36
```

`reduce`'s three-argument form (identity, accumulator, combiner) exists for **parallel**
streams — the combiner merges partial results computed on different threads. On a
sequential stream it's never even called — confirmed here by a combiner that prints when
invoked, which never printed anything during a sequential `reduce`.

---

## 5. A stream is single-use, for real

```java
Stream<Integer> onceOnly = Stream.of(1, 2, 3);
onceOnly.count();   // 3
onceOnly.count();   // IllegalStateException
```

Real error: `IllegalStateException: stream has already been operated upon or closed`.

A `Stream` is a one-time pipeline *description*, not a reusable collection — unlike a
`List`, which can be iterated as many times as needed. To run the same operations twice,
build a fresh stream from the source collection each time (`list.stream()` again).

---

## 6. Short-circuiting terminal operations, measured

```java
AtomicInteger examined = new AtomicInteger();
Stream.iterate(1, x -> x + 1)         // an INFINITE stream
        .peek(x -> examined.incrementAndGet())
        .filter(x -> x > 1_000_000)
        .findFirst();
```

Real result: the answer was `1000001`, and **exactly `1000001` elements were examined** —
not the whole (infinite) stream. `findFirst()` is **short-circuiting**: it stops pulling
new elements through the pipeline the moment it has its answer. `count()` is *not*
short-circuiting — it must see every element to count them, which is exactly why
`count()` on an infinite stream never returns.

---

## 7. Why stream operations must not have side effects

**The mistake** — a plain, non-thread-safe counter incremented from inside a *parallel*
stream's `forEach`:

```java
List<Integer> million = IntStream.rangeClosed(1, 1_000_000).boxed().toList();
int[] brokenCounter = {0};
million.parallelStream().forEach(n -> brokenCounter[0]++);   // DATA RACE
```

Real, measured result: **724,644** — not 1,000,000. Multiple threads ran
`brokenCounter[0]++` concurrently. That's read-increment-write, not atomic — two threads
can both read the same value before either writes back, and one increment is **lost**.
This doesn't necessarily reproduce every run, which is worse than a reliable crash: it can
pass casual testing and fail unpredictably in production.

**The fix** — an `AtomicInteger` (thread-safe by construction):

```java
AtomicInteger safeCounter = new AtomicInteger();
million.parallelStream().forEach(n -> safeCounter.incrementAndGet());
// always 1,000,000
```

**Or, far better** — don't count via a side effect at all; use the stream's own `count()`,
which parallelizes internally and correctly:

```java
million.parallelStream().count();   // 1,000,000, always
```

**The actual rule**: lambdas passed to stream operations (`map`, `filter`, `forEach`, ...)
should be stateless and free of side effects on shared mutable state. Sequential streams
often get away with breaking this rule by accident (there's only one thread); a
`parallelStream()` turns it into a real, unpredictable bug the moment multiple threads are
actually involved.

---

## 8. A preview of `collect()`

```java
nums.stream().map(n -> "n" + n).collect(Collectors.toList());
// [n5, n3, n8, n3, n1, n9, n5, n2]

nums.stream().map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
// "[5, 3, 8, 3, 1, 9, 5, 2]"
```

Lesson 55 covers `groupingBy`, `partitioningBy`, and building a custom `Collector` from
scratch.

---

## 9. Summary

- Streams are built from collections, arrays, varargs, or generators
  (`Stream.iterate`/`Stream.generate`), and are lazy: nothing runs until a **terminal**
  operation is called — confirmed here with real print-order evidence and element-at-a-time
  processing.
- `flatMap` turns a stream of collections into one flat stream — different from `map`,
  which would leave it nested.
- `reduce`'s three-arg combiner exists for parallel streams and is skipped entirely on a
  sequential one.
- A `Stream` can only be consumed **once** — a second terminal operation on the same
  stream throws a real `IllegalStateException`.
- `findFirst`/`anyMatch`/etc. are short-circuiting and can terminate an infinite stream
  early — measured here at exactly `1,000,001` elements examined to find one over a
  million. `count()` is not short-circuiting and will never return on an infinite stream.
- Stream operations must be stateless and side-effect-free — measured here with a real,
  reproducible lost-update bug (724,644 instead of 1,000,000) from a shared, unsynchronized
  counter under `parallelStream()`. Use `AtomicInteger`, or better, the stream's own
  aggregate operations (`count()`, `sum()`, `collect()`) instead of side effects.

---

**Previous:** [53 — Method references](53-method-references.md) ·
**Next:** [55 — Collectors, grouping and partitioning](55-collectors-and-grouping.md)
