# 46 · `Set` — `HashSet`, `LinkedHashSet`, `TreeSet`

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/46-set-implementations.java
> ```

A `Set` is "no duplicates" — but the three implementations disagree on what "duplicate"
even means, and on what order (if any) you get back. One of them can lose data silently.

---

## 1. Same elements, three different iteration orders

```
inserted in this order  -> [delta, alpha, charlie, bravo, echo]
HashSet iterates as     -> [bravo, alpha, delta, echo, charlie]
LinkedHashSet iterates  -> [delta, alpha, charlie, bravo, echo]
TreeSet iterates as     -> [alpha, bravo, charlie, delta, echo]
```

- **`HashSet`**'s order is whatever hash buckets the elements happen to land in — an
  implementation detail, not a contract. It can change between JVM versions. Never rely
  on it.
- **`LinkedHashSet`** keeps a doubly-linked list threaded through the same hash table, so
  iteration is **insertion order**, at the cost of two extra pointers per element.
- **`TreeSet`** keeps no insertion information at all — it iterates in **sorted order**
  because that is literally how the tree is built.

---

## 2. What's actually underneath

`HashSet<E>` is, literally, a `HashMap<E, Object>` in disguise:

```java
private transient HashMap<E, Object> map;
private static final Object PRESENT = new Object();
add(e) { return map.put(e, PRESENT) == null; }
```

Every `HashSet` method is one line delegating to the map — its performance and its bugs
*are* `HashMap`'s performance and bugs (lesson 47 goes inside the map itself: buckets,
treeification, load factor, resizing).

`LinkedHashSet` extends `HashSet` — same map underneath, constructed with the map's
linked-order constructor. `TreeSet<E>` wraps a `TreeMap<E, Object>` — a **red-black
tree**, a self-balancing binary search tree guaranteeing O(log n) for add/remove/contains,
in exchange for giving up O(1).

---

## 3. `contains()` — the whole reason `Set` exists

Measured directly: 200,000 elements, 200,000 `contains()` calls each:

| Structure | Time | Complexity |
| --- | --- | --- |
| `List.contains(o)` | 25,365 ms | O(n) per call |
| `HashSet.contains(o)` | 7 ms | O(1) average |
| `TreeSet.contains(o)` | 27 ms | O(log n) |

`HashSet` beat the `List` by roughly **3,600×**. That gap is the entire reason `Set` is a
separate abstraction from `List` — checking membership is what it is *for*.

---

## 4. What a broken `hashCode()` actually costs

`HashSet`'s O(1) promise is a promise about a **good** hash function — one that spreads
elements across buckets. The lesson's `.java` file defines two key classes: `GoodHashKey`
(a normal `hashCode()`) and `BadHashKey`, whose `hashCode()` **always returns 42** while
`equals()` still compares the real `id` field correctly. This is *not* a violation of the
equals/hashCode contract (lesson 33: equal objects must hash equal) — it's just terrible,
and terrible is legal.

Measured at 20,000 keys, 20,000 `contains()` calls:

```
good hashCode() (spread across buckets) -> 2 ms
hashCode() always 42 (one giant bucket) -> 1990 ms
```

The bad key was roughly **1,000× slower** — and it gets *worse* as the set grows, because
every key funnels into one bucket, which becomes a plain linked list (or, since Java 8, a
red-black tree once that one bucket gets large enough and the keys are `Comparable` — this
caps the damage at O(log n) instead of O(n), covered fully in lesson 47).

**The lesson**: `hashCode()` is not optional boilerplate. A bad one silently turns every
`HashSet`/`HashMap` using that key into a near-linear-scan structure — no exception, no
warning, just a service that mysteriously gets slower as it grows.

---

## 5. The `TreeSet` trap: `compareTo()` decides duplicates, not `equals()`

```java
record Person(String name, int age) {}

Set<Person> hashOfPeople = new HashSet<>();
hashOfPeople.add(new Person("Alice", 30));
hashOfPeople.add(new Person("Bob", 30));
// size = 2 - equals() correctly says these differ

Set<Person> treeOfPeople = new TreeSet<>(Comparator.comparingInt(Person::age));
treeOfPeople.add(new Person("Alice", 30));
treeOfPeople.add(new Person("Bob", 30));   // returns false!
// size = 1 - Bob is GONE
```

Real, measured result: the `HashSet` correctly kept both people (`size = 2`). The
`TreeSet`, ordered only by `age`, silently dropped Bob — `add()` returned `false`, and the
final set contained only Alice.

`TreeSet` considers two elements duplicates when `compareTo()` (or the `Comparator`)
returns `0` — it **never calls `equals()` at all**. A `Comparator` that only orders by one
field is a silent **data-loss bug** the moment two elements tie on that field. If ties
matter, break them explicitly:

```java
Comparator.comparingInt(Person::age).thenComparing(Person::name)
```

With that tie-break added, both people are kept (`size = 2`), confirmed in the same run.

---

## 6. `TreeSet`'s extra powers: `NavigableSet`

```java
NavigableSet<Integer> numbers = new TreeSet<>(List.of(10, 20, 30, 40, 50));

numbers.first() / numbers.last()      // 10 / 50
numbers.floor(25)    // <= 25         -> 20
numbers.ceiling(25)  // >= 25         -> 30
numbers.lower(30)    // < 30          -> 20
numbers.higher(30)   // > 30          -> 40
numbers.headSet(30)  // [elements < 30)     -> [10, 20]
numbers.tailSet(30)  // [elements >= 30)    -> [30, 40, 50]
numbers.subSet(20, 40)  // [20, 40)         -> [20, 30]
numbers.descendingSet()                     -> [50, 40, 30, 20, 10]
numbers.pollFirst()   // removes AND returns 10
```

None of this exists on `HashSet` or `LinkedHashSet` — it needs a total order, which only
`TreeSet` maintains. `headSet`/`tailSet`/`subSet` are **live views**, exactly like
`List.subList` (lesson 45).

---

## 7. Immutable sets and `add()`'s return value

```java
Set<String> immutable = Set.of("a", "b", "c");
immutable.add("d");        // UnsupportedOperationException
Set.of("a", "a");          // IllegalArgumentException - rejects duplicates AT CONSTRUCTION
```

Unlike `new HashSet<>(List.of("a", "a"))` (which silently keeps one "a"), `Set.of` refuses
outright if you hand it duplicates.

`add()` tells you whether it actually changed anything:

```java
set.add("x");   // true  - was added
set.add("x");   // false - already present, no-op
```

which is what makes the standard **set algebra** idioms work:

```java
Set<Integer> union        = new HashSet<>(a); union.addAll(b);
Set<Integer> intersection = new HashSet<>(a); intersection.retainAll(b);
Set<Integer> difference   = new HashSet<>(a); difference.removeAll(b);
```

With `a = {1,2,3,4}` and `b = {3,4,5,6}`, real output: union `{1,2,3,4,5,6}`, intersection
`{3,4}`, difference `{1,2}`.

---

## 8. Summary

- `HashSet` is a `HashMap` in disguise — no order guarantee, O(1) average operations.
- `LinkedHashSet` adds insertion-order iteration at the cost of two pointers per element.
- `TreeSet` is a red-black tree — sorted iteration, O(log n) operations, and the
  `NavigableSet` API (`floor`/`ceiling`/`headSet`/`tailSet`/`subSet`).
- `contains()` is the whole point: measured here at ~3,600× faster than `List.contains()`
  at 200,000 elements.
- A bad `hashCode()` (legal, but terrible) measurably collapses `HashSet` performance —
  ~1,000× slower in this lesson's measurement, and it gets worse as the set grows.
- **`TreeSet` decides duplicates via `compareTo()`/`Comparator`, never `equals()`.** A
  `Comparator` ordering by one field alone will silently drop elements that tie on that
  field — always break ties explicitly with `thenComparing(...)` if every element should
  survive.
- `Set.of(...)` is immutable and rejects duplicates at construction time, unlike
  `new HashSet<>(...)`.

---

**Previous:** [45 — `List` implementations](45-list-implementations.md) ·
**Next:** [47 — `Map` implementations](47-map-implementations.md)
