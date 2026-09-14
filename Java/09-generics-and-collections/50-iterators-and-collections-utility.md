# 50 · Iterators and the `Collections` Utility Class

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/50-iterators-and-collections-utility.java
> ```

The last lesson of Section 09. It starts with the exception almost everyone hits at least
once — modifying a list while looping over it — and ends with a subtler trap: "unmodifiable"
does not mean "immutable."

---

## 1. `ConcurrentModificationException`, reproduced on purpose

```java
List<String> names = new ArrayList<>(List.of("Alice", "Bob", "Charlie", "Dave"));
for (String name : names) {
    if (name.equals("Bob")) names.remove(name);   // structural change DURING iteration
}
```

Real result: `ConcurrentModificationException` — thrown on the *next* call to `next()`
after the structural change, not even on the `remove()` call itself.

**Why**: the enhanced `for` is sugar for an `Iterator`. Every list tracks a `modCount`,
bumped by every structural change. Each `Iterator` remembers the `modCount` it started
with; `next()` checks it still matches before doing anything, and throws if not. This is a
**fail-fast, best-effort** check — the JDK's own Javadoc warns it is not guaranteed to
catch every case; it exists to catch bugs, not to be relied on for correctness.

**The fix** — `Iterator.remove()`, the only safe way to remove *during* iteration:

```java
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    if (it.next().equals("Bob")) it.remove();
}
// [Alice, Charlie, Dave] - no exception
```

**The other fix**, when not tied to `Iterator` directly — `removeIf()`:

```java
names.removeIf(name -> name.equals("Bob"));
```

---

## 2. `ListIterator`: everything `Iterator` can't do

```java
ListIterator<String> it = letters.listIterator();
while (it.hasNext()) {
    String value = it.next();
    if (value.equals("b")) {
        it.set("B");     // REPLACE the just-returned element
        it.add("b2");    // INSERT right after it
    }
}
// [a, b, c]  ->  [a, B, b2, c]
```

Walking backwards from the end:

```java
ListIterator<String> backwards = list.listIterator(list.size());
while (backwards.hasPrevious()) { ... backwards.previous() ... }
// real result: "c b2 B a"
```

Plain `Iterator` only goes forward and can only `remove()`. `ListIterator` can also
`set()`, `add()`, and walk either direction — only `List` offers one, because only `List`
has *positions*.

---

## 3. The `Collections` utility class

```java
List<Integer> numbers = new ArrayList<>(List.of(5, 2, 8, 2, 9, 1, 2));
Collections.max(numbers) / Collections.min(numbers)     // 9 / 1
Collections.frequency(numbers, 2)                        // 3
Collections.sort(numbers)                                 // [1, 2, 2, 2, 5, 8, 9]
Collections.reverse(numbers)                               // [9, 8, 5, 2, 2, 2, 1]
Collections.binarySearch(numbers, 8)   // on SORTED numbers -> index 5
```

### The `binarySearch` trap

`binarySearch` **requires sorted input**, and gives **no warning** if you hand it
something else:

```java
List<Integer> unsorted = new ArrayList<>(List.of(50, 10, 40, 20, 30));
Collections.binarySearch(unsorted, 20);   // returns -1
```

Real, measured result: `-1` — even though `20` genuinely **is** in the list, at index 3.
`binarySearch`'s own Javadoc says the result is **undefined** on unsorted input — not an
exception, just a wrong answer, on purpose, because checking sortedness first would cost
O(n) and defeat the entire point of an O(log n) search.

```java
Collections.shuffle(list, new Random(42));   // seeded -> deterministic, safe for tests
Collections.nCopies(3, "x");                  // [x, x, x]
```

---

## 4. `Collections.unmodifiableList` is a **view**, not a copy

```java
List<String> mutable = new ArrayList<>(List.of("x", "y", "z"));
List<String> unmodifiableView = Collections.unmodifiableList(mutable);

unmodifiableView.add("blocked");   // UnsupportedOperationException - good, the view itself is locked

mutable.add("added via the ORIGINAL list");
System.out.println(unmodifiableView);
// [x, y, z, added via the ORIGINAL list]  <- the VIEW CHANGED
```

**"Unmodifiable" only means you cannot call mutating methods on *that reference*** — it is
not a copy, and whoever still holds the original, mutable list can change what the
"unmodifiable" view shows, at any time, with no warning. This is a real, common source of
bugs: code that hands out `Collections.unmodifiableList(internalList)` believing it has
protected its internal state, while anything with a reference to `internalList` itself can
still mutate what callers see.

For genuine, independent immutability, use `List.copyOf` (Java 10+):

```java
List<String> genuinelyImmutable = List.copyOf(mutable);
mutable.add("this one cannot reach List.copyOf's result");
// genuinelyImmutable is UNCHANGED - no shared backing storage at all
```

This is the same distinction as `List.of(...)` (lesson 45) vs. a plain wrapper — a real
copy shares nothing with its source, while a view is only a thin, read-only *window* onto
storage someone else still controls.

---

## 5. Summary

- The enhanced `for` is sugar for an `Iterator`; structurally modifying a collection
  (other than through that same iterator) while iterating throws
  `ConcurrentModificationException` — a fail-fast, best-effort safety check, not a
  guarantee.
- `Iterator.remove()` (or `Collection.removeIf()`) is the only safe way to remove elements
  during iteration.
- `ListIterator` adds `set()`, `add()`, and backward traversal to plain `Iterator` —
  available only on `List`, because only `List` has positions.
- `Collections.binarySearch` requires sorted input and silently returns a **wrong** index
  on unsorted input — verified here with a real, reproducible `-1` for a value that was
  genuinely present.
- `Collections.unmodifiableList`/`Set`/`Map` wrap the original collection as a read-only
  **view** — mutations to the original are still visible through it. For real,
  independent immutability, use `List.copyOf`/`Set.copyOf`/`Map.copyOf` (or the
  `List.of`/`Set.of`/`Map.of` factories from lessons 45/46), which make genuine copies.

---

**Previous:** [49 — `Comparable` and `Comparator`](49-comparable-and-comparator.md) ·
**Next:** [51 — Lambda expressions](../10-functional-java/51-lambda-expressions.md)
