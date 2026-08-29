# 44 · The Collections Framework, Mapped Out

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/44-collections-framework-overview.java
> ```

Before the detail of lessons 45–50, here is the whole map: what the interfaces are, what
implements them, and how to choose.

---

## 1. The shape of it

```
                    Iterable<E>
                        |
                   Collection<E>
        ________________|________________
       |                |                |
    List<E>          Set<E>          Queue<E>
       |                |                |
 ArrayList        HashSet           ArrayDeque
 LinkedList       LinkedHashSet     PriorityQueue
 Vector (legacy)  TreeSet           LinkedList


    Map<K,V>          ← NOT a Collection
       |
  HashMap
  LinkedHashMap
  TreeMap
  Hashtable (legacy)
```

Two things to notice immediately:

**`Map` is not a `Collection`.** It holds *pairs*, not elements, so `add(E)` makes no
sense for it. `Map` is a separate root, and that surprises people. You can reach a
collection view of it with `keySet()`, `values()` or `entrySet()`.

**`LinkedList` implements both `List` and `Deque`.** That is why you sometimes see it used
as a queue.

---

## 2. The four core interfaces

| Interface | Guarantees | Duplicates? | Ordered? |
| --- | --- | :---: | --- |
| **`List`** | Indexed, positional access | **Yes** | Insertion order |
| **`Set`** | No duplicates | No | Depends on implementation |
| **`Queue`** | Ordered for processing | Yes | FIFO, or by priority |
| **`Map`** | Key → value | Keys no, values yes | Depends on implementation |

### `Collection`'s methods

```java
add(E)  remove(Object)  contains(Object)  size()  isEmpty()  clear()
addAll  removeAll  retainAll  containsAll
iterator()  stream()  forEach(Consumer)
removeIf(Predicate)                    // Java 8 — the right way to filter in place
toArray()
```

---

## 3. Choosing an implementation

This table is the practical core of the whole framework:

| I need | Use | Why |
| --- | --- | --- |
| An ordered list, indexed access | **`ArrayList`** | O(1) get; the default |
| Lots of insert/remove at the ends | **`ArrayDeque`** | Faster than `LinkedList` |
| No duplicates, order irrelevant | **`HashSet`** | O(1) operations |
| No duplicates, insertion order kept | **`LinkedHashSet`** | O(1) plus ordering |
| No duplicates, sorted | **`TreeSet`** | O(log n), sorted |
| Key→value, order irrelevant | **`HashMap`** | O(1); the default |
| Key→value, insertion order kept | **`LinkedHashMap`** | Also does LRU caches |
| Key→value, sorted by key | **`TreeMap`** | O(log n), plus range queries |
| A queue | **`ArrayDeque`** | Faster than `LinkedList` |
| A priority queue | **`PriorityQueue`** | Heap-ordered |
| A stack | **`ArrayDeque`** | **Not** `Stack` — see below |
| Enum keys | **`EnumMap`/`EnumSet`** | Array/bitset backed (lesson 35) |
| Thread-safe map | **`ConcurrentHashMap`** | Not `Hashtable` (lesson 65) |

**Default to `ArrayList` and `HashMap`.** Change only when you have a reason.

---

## 4. Complexity, honestly

| Operation | `ArrayList` | `LinkedList` | `HashSet` | `TreeSet` |
| --- | --- | --- | --- | --- |
| `get(i)` | **O(1)** | O(n) | — | — |
| `add` (end) | O(1)* | O(1) | O(1) | O(log n) |
| `add` (start) | O(n) | **O(1)** | — | — |
| `remove(i)` | O(n) | O(n)† | — | — |
| `contains` | O(n) | O(n) | **O(1)** | O(log n) |

\* amortised — it doubles and copies occasionally
† O(1) once you have the node, but O(n) to *find* it

> **The table lies about `LinkedList`.** Its O(1) insertion assumes you already hold the
> node. Traversal is a pointer-chase across scattered heap objects, which destroys CPU
> cache locality. In practice `ArrayList` beats `LinkedList` at almost everything —
> lesson 45 measures it.

---

## 5. The legacy classes

| Legacy | Use instead | Why |
| --- | --- | --- |
| `Vector` | `ArrayList` | Synchronised on every method, for no benefit |
| `Hashtable` | `HashMap` | Same; also rejects `null` |
| `Stack` | `ArrayDeque` | Extends `Vector`, so it exposes `add(int, E)` (lesson 26) |
| `Enumeration` | `Iterator` | Older, weaker interface |

These are still in the JDK for compatibility and will never be removed. That is not an
endorsement.

---

## 6. Immutable and unmodifiable collections

```java
List.of(1, 2, 3)                  // immutable, rejects null      (Java 9+)
Map.of("a", 1, "b", 2)            // immutable, up to 10 pairs
Map.ofEntries(entry(...), ...)    // more than 10
List.copyOf(existing)             // immutable copy

Collections.unmodifiableList(l)   // a VIEW — reflects later changes
Arrays.asList(array)              // fixed-size, MUTABLE view over the array
```

Three distinct things people conflate (lesson 38):

- **`List.of`** — genuinely immutable, and rejects `null` elements.
- **`Collections.unmodifiableList`** — a read-only *view*; the underlying list can still
  change beneath it.
- **`Arrays.asList`** — fixed size but mutable, and writes through to the array.

---

## 7. `null` handling differs

| Collection | `null` element | `null` key | `null` value |
| --- | --- | --- | --- |
| `ArrayList` | Yes | — | — |
| `HashSet` | Yes (one) | — | — |
| `TreeSet` | **No** — needs to compare | — | — |
| `HashMap` | — | **Yes (one)** | Yes |
| `TreeMap` | — | No | Yes |
| `List.of` / `Map.of` | **No** | **No** | **No** |
| `ConcurrentHashMap` | — | **No** | **No** |

`HashMap` allowing one `null` key is a genuine oddity. `ConcurrentHashMap` forbids it
because `get` returning `null` would be ambiguous under concurrency.

---

## 8. Iteration order — what is guaranteed

| Type | Order |
| --- | --- |
| `ArrayList`, `LinkedList` | **Insertion order** |
| `HashSet`, `HashMap` | **No guarantee** — do not rely on it |
| `LinkedHashSet`, `LinkedHashMap` | **Insertion order** |
| `TreeSet`, `TreeMap` | **Sorted** |
| `PriorityQueue` | **Not sorted on iteration** — only `poll()` is ordered |

`HashMap`'s order is stable within a run but is not part of the contract; it changes with
capacity, and has changed between Java versions. Code that depends on it is broken code
that has not failed yet.

`PriorityQueue` is the surprising one: iterating it gives heap order, not sorted order.
Only `poll()` returns elements in priority sequence.

---

## 9. Sequenced collections (Java 21)

New interfaces giving every ordered collection a uniform first/last API:

```java
SequencedCollection<E>   getFirst()  getLast()  addFirst()  addLast()
                         removeFirst()  removeLast()  reversed()
SequencedSet<E>
SequencedMap<K,V>        firstEntry()  lastEntry()  reversed()
```

```java
list.getFirst();                  // instead of list.get(0)
linkedHashMap.firstEntry();       // previously required an iterator
list.reversed();                  // a reversed VIEW, not a copy
```

This filled a long-standing gap: `List` had `get(0)`, `Deque` had `getFirst()`,
`LinkedHashSet` had *no* way to get the first element without an iterator.

---

## 10. Summary

- `Iterable` → `Collection` → `List`, `Set`, `Queue`. **`Map` is separate** — it holds
  pairs, not elements.
- `LinkedList` implements both `List` and `Deque`.
- **Default to `ArrayList` and `HashMap`.** Change only with a reason.
- `ArrayList` beats `LinkedList` in practice because of **cache locality**, whatever the
  complexity table suggests.
- Avoid the legacy `Vector`, `Hashtable`, `Stack` and `Enumeration`.
- `List.of` is immutable; `Collections.unmodifiableList` is a **view**; `Arrays.asList` is
  a **fixed-size mutable view**.
- `null` rules differ: `TreeSet` forbids it, `HashMap` allows one `null` key, `List.of`
  and `ConcurrentHashMap` forbid it entirely.
- **`HashMap` iteration order is not guaranteed.** Use `LinkedHashMap` when order matters.
- `PriorityQueue` iterates in heap order, not sorted order — only `poll()` is ordered.
- Java 21's **sequenced collections** give a uniform `getFirst`/`getLast`/`reversed` API.

---

**Previous:** [43 — Generics](43-generics.md) ·
**Next:** [45 — `List` implementations](45-list-implementations.md)
