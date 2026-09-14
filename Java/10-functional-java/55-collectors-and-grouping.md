# 55 · Collectors, Grouping and Partitioning

> **Run the code for this lesson**
> ```bash
> java Java/10-functional-java/55-collectors-and-grouping.java
> ```

`Collectors.toList()` and `stream.toList()` look like the same thing and behave
differently — one is mutable, one throws. This lesson catches that, then reproduces
`toMap`'s real exception on duplicate keys, and builds a custom `Collector` from scratch.

---

## 1. `toList`, `toSet`, `joining`

```java
employees.stream().map(Employee::name).collect(Collectors.toList());
// [Alice, Bob, Carol, Dave, Eve, Frank]

employees.stream().map(Employee::department).collect(Collectors.toSet());
// [Engineering, Sales, Marketing] - deduplicated, Set semantics

employees.stream().map(Employee::name).collect(Collectors.joining(", ", "[", "]"));
// "[Alice, Bob, Carol, Dave, Eve, Frank]"
```

---

## 2. `Collectors.toList()` vs `stream.toList()` — these look the same and are not

```java
List<Integer> viaCollectors = Stream.of(1, 2, 3).collect(Collectors.toList());
List<Integer> viaStreamMethod = Stream.of(1, 2, 3).toList();   // Java 16+ instance method
```

```java
viaCollectors.add(4);   // succeeds - now [1, 2, 3, 4]
```

`Collectors.toList()`'s **contract never actually promises mutability or immutability**
— the current implementation just happens to return a plain, mutable `ArrayList`.

```java
viaStreamMethod.add(4);   // UnsupportedOperationException
```

`stream.toList()` **explicitly guarantees** an unmodifiable list — a real, contractual
difference from `Collectors.toList()`, not an accident of implementation. Relying on
`Collectors.toList()`'s result being mutable is relying on unspecified behavior that
could change.

---

## 3. `groupingBy`: one key, multiple buckets

```java
Map<String, List<Employee>> byDepartment = employees.stream()
        .collect(Collectors.groupingBy(Employee::department));
```

Real result:
```
Engineering -> [Alice(95000), Bob(82000), Eve(110000)]
Sales       -> [Carol(70000), Dave(65000)]
Marketing   -> [Frank(60000)]
```

**A downstream collector** — `groupingBy`'s second argument — processes each group
further instead of just collecting a `List`:

```java
Collectors.groupingBy(Employee::department, Collectors.counting());
// {Engineering=3, Sales=2, Marketing=1}

Collectors.groupingBy(Employee::department, Collectors.averagingInt(Employee::salary));
// {Engineering=95666.67, Sales=67500.0, Marketing=60000.0}

Collectors.groupingBy(Employee::department, Collectors.mapping(Employee::name, Collectors.toList()));
// {Engineering=[Alice, Bob, Eve], Sales=[Carol, Dave], Marketing=[Frank]}
```

**Multi-level grouping** — `groupingBy` nested inside `groupingBy`:

```java
Collectors.groupingBy(Employee::department,
        Collectors.groupingBy(e -> e.salary() > 80_000));
```

Real result:
```
Engineering -> {true=[Alice, Bob, Eve]}
Sales       -> {false=[Carol, Dave]}
Marketing   -> {false=[Frank]}
```

Notice Engineering's inner map has **only** a `true` key — every Engineering employee
earns over 80,000, so there's no `false` group, and nested `groupingBy` simply omits it.
Section 4 (`partitioningBy`) does *not* behave this way — that's the real, concrete
difference between the two.

---

## 4. `partitioningBy`: a special case of `groupingBy`

```java
Map<Boolean, List<Employee>> partitioned = employees.stream()
        .collect(Collectors.partitioningBy(e -> e.salary() > 80_000));

partitioned.get(true);    // high earners
partitioned.get(false);   // everyone else
```

**The real difference from `groupingBy(Predicate)`**: `partitioningBy`'s result **always**
has both `true` and `false` keys present, even if one side is empty — `groupingBy` would
simply omit an empty group's key entirely (as seen above with Engineering's missing
`false` key). Use `partitioningBy` specifically when the caller always needs to handle
both branches without a null/missing-key check.

---

## 5. `toMap`: the duplicate-key trap, reproduced

```java
employees.stream().collect(Collectors.toMap(Employee::name, Employee::salary));
// fine - names are unique
```

```java
employees.stream().collect(Collectors.toMap(Employee::department, Employee::salary));
```

Real error — three employees share `"Engineering"`:

```
IllegalStateException: Duplicate key Engineering (attempted merging values 95000 and 82000)
```

**The fix** — `toMap`'s third argument, a merge function for collisions:

```java
employees.stream().collect(Collectors.toMap(Employee::department, Employee::salary, Integer::max));
// {Engineering=110000, Sales=70000, Marketing=60000}
```

---

## 6. Building a custom `Collector`

`Collector.of()` takes four pieces: a **supplier** (new container), an **accumulator**
(add one element), a **combiner** (merge two containers — for parallel streams, the same
role as `reduce`'s combiner in lesson 54), and a **finisher** (container → final result):

```java
Collector<Employee, ?, String> nameSummary = Collector.of(
        StringBuilder::new,
        (sb, e) -> sb.append(sb.isEmpty() ? "" : ", ").append(e.name()),
        (sb1, sb2) -> sb1.append(sb1.isEmpty() ? "" : ", ").append(sb2),
        StringBuilder::toString
);
employees.stream().collect(nameSummary);
// "Alice, Bob, Carol, Dave, Eve, Frank"
```

---

## 7. Summary

- `Collectors.toList()` and `stream.toList()` are **not** interchangeable —
  `Collectors.toList()`'s mutability is unspecified (currently mutable), while
  `stream.toList()` (Java 16+) *guarantees* an unmodifiable result — verified here with a
  real `UnsupportedOperationException` on one and a successful `add()` on the other.
- `groupingBy`'s second argument (a downstream collector — `counting()`,
  `averagingInt()`, `mapping()`, or another `groupingBy` for multi-level grouping) lets
  each bucket be processed further instead of just collected into a `List`.
- `partitioningBy` is `groupingBy` specialized to exactly two boolean buckets, and —
  unlike `groupingBy` — always includes both `true` and `false` keys even when one side
  is empty, confirmed here by contrast with a nested `groupingBy` that silently omitted
  an empty group.
- `toMap` throws a real `IllegalStateException` on colliding keys unless given a third,
  merge-function argument to resolve the collision.
- A custom `Collector` is just four functions (`supplier`, `accumulator`, `combiner`,
  `finisher`) passed to `Collector.of()` — the same shape as `reduce`'s three-argument
  form, generalized to build any container, not just a single accumulated value.

---

**Previous:** [54 — The Stream API](54-streams-api.md) ·
**Next:** [56 — `Optional`](56-optional.md)
