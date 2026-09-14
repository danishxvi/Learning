# 48 · `Queue`, `Deque` and `PriorityQueue`

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/48-queue-and-deque.java
> ```

Lesson 45 said "use `ArrayDeque` instead of `LinkedList` for a queue or stack" and asked
you to take it on faith. This lesson measures it — and along the way, measuring honestly
turns up a surprise the first draft of this lesson got wrong.

---

## 1. `Queue` has two families of methods, on purpose

| | Throws on failure | Returns a special value |
| --- | --- | --- |
| Insert | `add(e)` | `offer(e)` |
| Remove | `remove()` | `poll()` |
| Examine | `element()` | `peek()` |

```java
Queue<String> queue = new ArrayDeque<>();
queue.offer("a");      // true
queue.poll();           // "a"
queue.poll();           // null - empty, no exception
queue.peek();           // null - empty, no exception
queue.remove();         // NoSuchElementException
queue.element();        // NoSuchElementException
```

Use `offer`/`poll`/`peek` unless an empty queue is genuinely a bug in your program — then
let `add`/`remove`/`element` throw and say so.

---

## 2. `Deque`: one interface, two shapes

```java
Deque<Integer> deque = new ArrayDeque<>();
deque.addFirst(2); deque.addFirst(1); deque.addLast(3);
// [1, 2, 3]
```

**As a stack** — `push`/`pop` are `addFirst`/`removeFirst`:

```java
Deque<String> stack = new ArrayDeque<>();
stack.push("first"); stack.push("second"); stack.push("third");
// [third, second, first]
stack.pop();   // "third" - LIFO, last pushed, first out
```

**As a queue** — `offer`/`poll` are `offerLast`/`pollFirst`:

```java
Deque<String> q = new ArrayDeque<>();
q.offer("first"); q.offer("second"); q.offer("third");
// [first, second, third]
q.poll();   // "first" - FIFO, first offered, first out
```

`java.util.Stack` (legacy, extends `Vector`, lessons 26/45) does the same job as an
`ArrayDeque` used as a stack. The JDK's own `Deque` Javadoc recommends `ArrayDeque` over
`Stack` — see Section 3 for the real, measured reasons why.

---

## 3. `ArrayDeque` vs `LinkedList` vs `Stack`, measured for real

2,000,000 push+pop pairs (stack usage) and 2,000,000 offer+poll pairs (queue usage):

| Role | `ArrayDeque` | `LinkedList` | `Stack` |
| --- | --- | --- | --- |
| Stack (push/pop) | 42 ms | 53 ms | 45 ms |
| Queue (offer/poll) | 26 ms | 48 ms | — |

`ArrayDeque` clearly beats `LinkedList` in **both** roles, for the same reason lesson 45
measured for `ArrayList`: a resizable circular array has no per-element `Node` allocation
and no pointer-chasing.

### An honest surprise

The first version of this lesson claimed `Stack` was "synchronized and slower." Running
the benchmark repeatedly showed that claim doesn't reliably hold — across several runs,
`Stack` and `ArrayDeque` traded places, both landing in the low-to-mid 40ms range. Rather
than keep an unverified claim, here's what's actually true:

`Stack`'s `synchronized` methods use **uncontended** locks in a single-threaded
benchmark, and modern JVMs make uncontended synchronization nearly free (biased/thin
locking). So "measurably slower in single-threaded code" isn't the real argument against
`Stack`. The real arguments are:

1. That same synchronization becomes **real, measurable contention overhead** the moment
   multiple threads actually share one `Stack` — this benchmark, being single-threaded,
   never exercises that cost at all.
2. `Stack` extends `Vector`, so it also exposes `get(i)`, `insertElementAt(i)`, and every
   other `List` method — nothing stops code from breaking LIFO discipline by indexing
   into the middle of what's supposed to be a stack. `ArrayDeque`'s API has no such
   escape hatch; it only exposes deque operations.

This is a genuine reminder that a microbenchmark measures exactly what it measures — a
single thread, no contention — and generalizing beyond that requires understanding *why*
a number came out the way it did, not just reading the number.

---

## 4. `PriorityQueue`: a heap, not a sorted list

```java
PriorityQueue<Integer> heap = new PriorityQueue<>();
int[] insertOrder = {50, 20, 80, 10, 30, 90, 5};
for (int v : insertOrder) heap.add(v);

System.out.println(heap);
// [5, 20, 10, 50, 30, 90, 80]  <- NOT sorted
```

**This is the #1 `PriorityQueue` mistake**: expecting iteration (or `toString()`) to come
out sorted. `PriorityQueue` only guarantees that `peek()`/`poll()` return the *smallest*
element — internally it's a **binary heap** stored in a flat array, whose only structural
rule is "every parent ≤ both its children." Iterating the array walks the heap's internal
layout, not sorted order.

Repeated `poll()`, however, comes out perfectly sorted:

```java
while (!heap.isEmpty()) System.out.print(heap.poll() + " ");
// 5 10 20 30 50 80 90
```

Min-heap (smallest first) is the default. A max-heap is one line:

```java
new PriorityQueue<>(Comparator.reverseOrder());
// peek() now returns the LARGEST element
```

---

## 5. A real use: the top-K pattern

Given an array and a small `k`, finding the k largest values **without sorting the whole
array**: keep a **min-heap of size k** — if a new value beats the heap's current
smallest, evict the smallest and insert the new one. After scanning everything, the heap
holds exactly the top k.

```java
PriorityQueue<Integer> topK = new PriorityQueue<>();
for (int value : scores) {
    topK.add(value);
    if (topK.size() > k) topK.poll();   // evict the current smallest of the k kept
}
```

Real result, `k = 3` over `{42,17,88,5,63,91,24,76,8,55,99,31}`: the heap ends up holding
`{88, 91, 99}` (order not meaningful — sorted for display: `[99, 91, 88]`).

**Cost: O(n log k)** — scanning n elements, each heap operation O(log k). Sorting the
whole array first would be O(n log n). For k much smaller than n, the difference is real.

---

## 6. Summary

- `Queue` methods come in two families: `add`/`remove`/`element` throw on failure;
  `offer`/`poll`/`peek` return a special value. Default to the safe family.
- `Deque` is both a stack (`push`/`pop`) and a FIFO queue (`offer`/`poll`) in one
  interface — `ArrayDeque` is the modern implementation of both roles.
- Measured: `ArrayDeque` clearly beats `LinkedList` for both stack and queue usage
  (26–42ms vs 48–53ms at 2,000,000 operations).
- `Stack` was **not** reliably slower than `ArrayDeque` in this single-threaded
  benchmark — uncontended synchronization is cheap on modern JVMs. The real case against
  `Stack` is contention cost under real multi-threading, plus its `Vector`-inherited API
  letting code break LIFO discipline — not raw single-threaded speed.
- `PriorityQueue` is a binary heap: iteration/`toString()` is **not** sorted order, only
  `peek()`/`poll()` are guaranteed to return the smallest (or, with a reversed
  `Comparator`, largest) remaining element.
- The top-K pattern (a bounded min-heap, evicting the smallest when oversized) finds the
  k largest values in O(n log k), without sorting the whole input.

---

**Previous:** [47 — `Map` implementations](47-map-implementations.md) ·
**Next:** [49 — `Comparable` and `Comparator`](49-comparable-and-comparator.md)
