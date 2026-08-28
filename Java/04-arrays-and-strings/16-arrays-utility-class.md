# 16 · The `Arrays` Utility Class

> **Run the code for this lesson**
> ```bash
> java Java/04-arrays-and-strings/16-arrays-utility-class.java
> ```

Arrays are objects, but they have almost no methods — just the `length` field and
`clone()`. Everything else lives in **`java.util.Arrays`**, a class of `static` helpers.

Knowing what is in it saves you writing loops that already exist, correctly, in the JDK.

---

## 1. Printing

```java
Arrays.toString(array)        // 1D:  [1, 2, 3]
Arrays.deepToString(array)    // 2D+: [[1, 2], [3, 4]]
```

Without these you get `[I@1b6d3586`. Covered in lessons 12 and 13.

---

## 2. Sorting

```java
Arrays.sort(array)                       // whole array, ascending, in place
Arrays.sort(array, fromIndex, toIndex)   // a range, end exclusive
Arrays.sort(array, comparator)           // objects, custom order
Arrays.parallelSort(array)               // multi-threaded, for large arrays
```

### Two different algorithms

This is a real detail worth knowing:

| Array type | Algorithm | Stable? |
| --- | --- | --- |
| **Primitives** (`int[]`, `double[]`…) | Dual-pivot quicksort | No |
| **Objects** (`String[]`, `Integer[]`…) | TimSort (merge sort variant) | **Yes** |

**Stable** means equal elements keep their original relative order. Primitives do not need
stability — two equal `int`s are indistinguishable — so the JDK uses the faster,
lower-memory quicksort. Objects *can* differ while comparing equal (two `Person`s with the
same age but different names), so stability matters and TimSort is used.

This is why sorting a `Person[]` by age and then by name gives a sensible combined
ordering, but you cannot rely on any such thing for primitives.

### Sorting objects

```java
String[] names = {"Rahul", "aisha", "Danish"};

Arrays.sort(names);                                   // natural order — case-sensitive!
Arrays.sort(names, String.CASE_INSENSITIVE_ORDER);
Arrays.sort(names, Comparator.comparing(String::length));
Arrays.sort(names, Comparator.comparing(String::length)
                             .thenComparing(Comparator.naturalOrder()));
Arrays.sort(names, Comparator.reverseOrder());
```

Natural `String` order is by Unicode code point, so **all uppercase letters sort before
all lowercase ones** — `"Zebra"` comes before `"apple"`. Use
`String.CASE_INSENSITIVE_ORDER` when that is not what you want. Lesson 49 covers
comparators fully.

Sorting a primitive array **cannot** take a comparator — there is no `Comparator<int>`.
Box it (`Integer[]`) or sort ascending and reverse manually.

---

## 3. Searching

```java
int index = Arrays.binarySearch(array, key);
```

**O(log n)** instead of O(n) — but with a hard precondition:

> **The array must already be sorted.** On an unsorted array `binarySearch` returns a
> meaningless value and **does not throw**.

That silence makes it one of the quieter bugs in the standard library.

### The negative return value is useful

If the key is absent, the return is `-(insertionPoint) - 1`. So:

```java
int result = Arrays.binarySearch(sorted, key);
if (result < 0) {
    int insertionPoint = -result - 1;    // where it WOULD go
}
```

That lets you find the nearest neighbour, or insert while keeping the array sorted, in one
call.

---

## 4. Filling and setting

```java
Arrays.fill(array, value)                     // every element
Arrays.fill(array, from, to, value)           // a range, end exclusive
Arrays.setAll(array, i -> i * i)              // compute from the index (Java 8+)
Arrays.parallelSetAll(array, i -> compute(i)) // the same, multi-threaded
```

`setAll` is often overlooked and is genuinely useful — it replaces the whole
"allocate then loop to populate" pattern with one line.

Note `Arrays.fill` on a **2D** array fills the outer array with the same *row reference*,
which is almost never what you want:

```java
int[][] grid = new int[3][3];
Arrays.fill(grid, new int[]{1, 1, 1});     // all three rows are the SAME object!
grid[0][0] = 99;
System.out.println(grid[1][0]);            // 99 (!)
```

Fill each row instead: `for (int[] row : grid) Arrays.fill(row, 1);`

---

## 5. Copying

```java
Arrays.copyOf(array, newLength)              // copy, padding or truncating
Arrays.copyOfRange(array, from, to)          // a slice, end exclusive
System.arraycopy(src, srcPos, dst, dstPos, length)   // fastest, most manual
```

All are **shallow** for object arrays. `System.arraycopy` is a native method and the
fastest of the three — the other two call it internally.

---

## 6. Comparing

```java
Arrays.equals(a, b)          // element-wise, 1D
Arrays.deepEquals(a, b)      // recursive, 2D+
Arrays.compare(a, b)         // lexicographic ordering        (Java 9+)
Arrays.mismatch(a, b)        // index of the first difference, or -1  (Java 9+)
Arrays.hashCode(a)           // content-based hash
Arrays.deepHashCode(a)       // recursive
```

`mismatch` is excellent for diagnostics — instead of "these arrays differ", you get
*where*.

---

## 7. Converting to collections and streams

```java
Arrays.asList(array)              // fixed-size List VIEW — see the trap
Arrays.stream(array)              // a Stream
List.of(1, 2, 3)                  // immutable list        (Java 9+)
```

### The `Arrays.asList` trap — two of them

**Trap 1: the list is fixed-size and backed by the array.**

```java
List<String> list = Arrays.asList("a", "b", "c");
list.set(0, "z");     // OK — writes THROUGH to the array
list.add("d");        // UnsupportedOperationException
list.remove("a");     // UnsupportedOperationException
```

It is a *view*, not a copy. To get a real, modifiable list:

```java
List<String> real = new ArrayList<>(Arrays.asList(array));
```

**Trap 2: it does not work on primitive arrays the way you expect.**

```java
int[] numbers = {1, 2, 3};
List<int[]> wrong = Arrays.asList(numbers);      // a list of ONE element: the array!
System.out.println(wrong.size());                // 1

Integer[] boxed = {1, 2, 3};
List<Integer> right = Arrays.asList(boxed);      // size 3
```

Generics cannot hold primitives, so `Arrays.asList(int[])` matches the single-varargs-
element overload. To get a `List<Integer>` from an `int[]`:

```java
List<Integer> list = Arrays.stream(numbers).boxed().toList();
```

---

## 8. Stream operations on arrays

```java
Arrays.stream(intArray).sum()
Arrays.stream(intArray).max().getAsInt()
Arrays.stream(intArray).average().orElse(0)
Arrays.stream(intArray).filter(n -> n > 5).toArray()
Arrays.stream(intArray).boxed().toList()
Arrays.stream(objectArray).map(...).collect(...)
```

For an `int[]`, `Arrays.stream` returns an `IntStream`, which has `sum()`, `average()` and
`max()` directly. For an object array it returns a `Stream<T>`, which does not — you would
use `mapToInt` first. Lesson 54 covers streams properly.

---

## 9. Quick reference

| Task | Call |
| --- | --- |
| Print | `Arrays.toString` / `deepToString` |
| Sort | `Arrays.sort` (+ `Comparator` for objects) |
| Sort in parallel | `Arrays.parallelSort` |
| Search a sorted array | `Arrays.binarySearch` |
| Fill | `Arrays.fill` |
| Populate from index | `Arrays.setAll` |
| Copy / resize | `Arrays.copyOf` |
| Slice | `Arrays.copyOfRange` |
| Compare | `Arrays.equals` / `deepEquals` |
| Find first difference | `Arrays.mismatch` |
| Hash | `Arrays.hashCode` / `deepHashCode` |
| To a `List` | `new ArrayList<>(Arrays.asList(a))` |
| To a `Stream` | `Arrays.stream(a)` |

---

## 10. Summary

- Arrays have almost no methods; `java.util.Arrays` supplies them all as `static` helpers.
- Primitives sort with **dual-pivot quicksort** (unstable); objects with **TimSort**
  (stable). The difference matters when you sort by one key then another.
- `binarySearch` requires a **sorted** array and returns nonsense — silently — otherwise.
  Its negative return encodes the insertion point as `-(point) - 1`.
- `Arrays.fill` on a 2D array shares one row object across every row. Fill each row.
- `Arrays.setAll(a, i -> f(i))` replaces the allocate-then-loop pattern.
- `Arrays.asList` returns a **fixed-size view** backed by the array — `set` works, `add`
  and `remove` throw. Wrap it in `new ArrayList<>(...)` for a real list.
- `Arrays.asList(intArray)` gives a **one-element** list. Use
  `Arrays.stream(a).boxed().toList()`.
- `Arrays.mismatch` tells you *where* two arrays differ, not just *that* they do.

---

**Previous:** [15 — StringBuilder](15-stringbuilder-and-stringbuffer.md) ·
**Next:** [17 — Methods and parameter passing](../05-methods/17-methods-and-parameter-passing.md)
