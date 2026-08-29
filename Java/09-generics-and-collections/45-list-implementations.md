# 45 · `List` — `ArrayList` vs `LinkedList`

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/45-list-implementations.java
> ```

The textbook says "use `LinkedList` for frequent insertions." The textbook is, in practice,
wrong. This lesson measures why.

---

## 1. How each one stores its elements

**`ArrayList`** wraps a single `Object[]`:

```
elementData:  [ a ][ b ][ c ][ d ][   ][   ][   ]
              contiguous, size=4, capacity=7
```

**`LinkedList`** is a doubly-linked chain of `Node` objects:

```
first → [prev|a|next] ⇄ [prev|b|next] ⇄ [prev|c|next] ← last
        scattered anywhere on the heap
```

Every consequence below follows from that picture.

---

## 2. `ArrayList` growth

Starts with capacity 10 (lazily — an empty `ArrayList` allocates no array until the first
`add`). When full it grows by **1.5×**:

```
10 → 15 → 22 → 33 → 49 → 73 → 109 → ...
```

Growth means allocating a new array and copying. That is why `add` is **amortised** O(1),
not strictly O(1) — most adds are free; occasionally one copies everything.

**Presize when you know the size:**

```java
new ArrayList<>(10_000);      // no reallocation at all
```

`trimToSize()` releases unused capacity, which matters for a long-lived list that was
briefly large.

> `ArrayList` never shrinks automatically. A list that held a million elements still holds
> a million-element array after `clear()`.

---

## 3. Complexity, and why it misleads

| Operation | `ArrayList` | `LinkedList` |
| --- | --- | --- |
| `get(i)` | **O(1)** | O(n) |
| `add(e)` at end | O(1) amortised | O(1) |
| `add(0, e)` at start | O(n) | **O(1)** |
| `remove(i)` | O(n) | O(n) to find |
| `contains(o)` | O(n) | O(n) |
| Memory per element | ~4–8 bytes | ~40 bytes |

`LinkedList` looks better in two rows. In practice it loses almost everywhere, for one
reason: **cache locality**.

`ArrayList` walks contiguous memory, so the CPU prefetches the next elements before you ask
for them. `LinkedList` follows pointers to objects scattered across the heap — every step
is a potential cache miss, and a cache miss costs roughly **100× an L1 hit**.

The complexity table counts *operations*. The CPU cares about *memory access patterns*.

### Even where `LinkedList` should win

`add(0, e)` really is O(1) for `LinkedList` and O(n) for `ArrayList` — but `ArrayList`'s
O(n) is `System.arraycopy`, a highly optimised bulk memory move, while `LinkedList`'s O(1)
allocates a `Node` object. For small and medium lists the array copy often still wins.

### The `get(i)` disaster

```java
for (int i = 0; i < list.size(); i++) {
    sum += list.get(i);        // O(n) per call on a LinkedList → O(n²) total
}
```

On a 100,000-element `LinkedList` this is ten billion pointer hops. Always use the enhanced
`for` or an iterator on a `LinkedList` (lesson 10).

---

## 4. When `LinkedList` is genuinely right

Almost never. The honest list:

- You need a `Deque` **and** a `List` in the same object — but `ArrayDeque` is faster if you
  only need the `Deque`.
- You are removing via an `Iterator` while traversing, at genuinely large scale.
- You need constant-time splice at a node you already hold — and Java's API does not
  actually expose that.

**Java's own documentation and Joshua Bloch both recommend `ArrayList` by default.** If you
are considering `LinkedList` for a queue, use `ArrayDeque` instead.

---

## 5. The other `List` implementations

| Class | Notes |
| --- | --- |
| `ArrayList` | The default |
| `LinkedList` | Rarely the right answer |
| `Vector` | Legacy — synchronised, no benefit |
| `Stack` | Legacy — extends `Vector` (lesson 26) |
| `CopyOnWriteArrayList` | Thread-safe; **every write copies the whole array**. Good for many reads and very few writes — listener lists |
| `List.of(...)` | Immutable, compact, rejects `null` |
| `Arrays.asList(...)` | Fixed-size **view** over an array |

---

## 6. The methods worth knowing

```java
list.get(i) / set(i, e) / add(e) / add(i, e) / remove(i) / remove(Object)
list.indexOf(o) / lastIndexOf(o) / contains(o) / subList(from, to)
list.sort(comparator)                      // in place — Java 8
list.replaceAll(UnaryOperator)             // map in place
list.removeIf(Predicate)                   // filter in place
list.getFirst() / getLast() / reversed()   // Java 21
```

### The `remove` overload trap

```java
list.remove(1);                    // remove(int index)   — by POSITION
list.remove(Integer.valueOf(1));   // remove(Object)      — by VALUE
```

On a `List<Integer>` these read almost identically and do different things. Covered in
lesson 18; it is a real production bug.

### `subList` is a view

```java
List<String> part = list.subList(1, 3);
part.set(0, "changed");            // writes THROUGH to the original list
list.add("x");                     // now `part` throws ConcurrentModificationException
```

`subList` returns a **live view**, not a copy. Structurally modifying the backing list
invalidates it. For a copy: `new ArrayList<>(list.subList(1, 3))`.

---

## 7. Summary

- `ArrayList` is a contiguous array; `LinkedList` is a chain of scattered `Node` objects.
- `ArrayList` grows by **1.5×** and **never shrinks** — presize when you can, `trimToSize()`
  to reclaim.
- The complexity table favours `LinkedList` in two rows and **misleads**: cache locality
  makes `ArrayList` faster in practice almost everywhere.
- A `LinkedList` costs roughly **40 bytes per element** against `ArrayList`'s 4–8.
- `get(i)` in an indexed loop over a `LinkedList` is **O(n²)**. Use the enhanced `for`.
- **Default to `ArrayList`.** For a queue or stack use `ArrayDeque`, not `LinkedList`.
- `CopyOnWriteArrayList` suits many-read/few-write cases such as listener lists.
- `list.remove(1)` removes by **index**; `list.remove(Integer.valueOf(1))` by **value**.
- `subList` is a **live view** that writes through and can be invalidated.

---

**Previous:** [44 — Collections Framework](44-collections-framework-overview.md) ·
**Next:** [46 — `Set` implementations](46-set-implementations.md)
