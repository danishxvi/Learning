# 13 · Multidimensional and Jagged Arrays

> **Run the code for this lesson**
> ```bash
> java Java/04-arrays-and-strings/13-multidimensional-arrays.java
> ```

The single most important fact in this lesson:

> **Java has no true 2D arrays.** `int[][]` is an *array of references to `int[]` arrays*.

Everything surprising about multidimensional arrays follows from that one sentence.

---

## 1. What `int[][]` really is

```java
int[][] grid = new int[3][4];
```

This creates **four** objects, not one:

```
grid ──► ┌───────────────┐        (the outer array: 3 references)
         │ [0] ──────────┼──► [0][0] [0][1] [0][2] [0][3]   ← an int[4]
         │ [1] ──────────┼──► [1][0] [1][1] [1][2] [1][3]   ← an int[4]
         │ [2] ──────────┼──► [2][0] [2][1] [2][2] [2][3]   ← an int[4]
         └───────────────┘
```

- The outer array holds **references**, not numbers.
- Each row is an independent `int[]` object, allocated separately, possibly far apart in
  memory.

This is unlike C, where `int grid[3][4]` is one flat contiguous block. The consequences:

| Consequence | Why |
| --- | --- |
| Rows can have **different lengths** | Each is a separate object |
| `grid[0]` is a **valid `int[]`** you can pass around | It is a real object |
| Row-major iteration is **faster** than column-major | Cache locality |
| `grid.length` is the row count, `grid[0].length` the column count | Two different objects |

---

## 2. Creating them

```java
// Rectangular, all zeros
int[][] grid = new int[3][4];

// With values — rows inferred
int[][] grid = {
    {1, 2, 3},
    {4, 5, 6}
};

// Outer array only — rows are null until you create them
int[][] rows = new int[3][];
rows[0] = new int[5];
rows[1] = new int[2];

// Three dimensions and beyond
int[][][] cube = new int[2][3][4];
```

### `new int[3][]` leaves the rows `null`

```java
int[][] rows = new int[3][];
System.out.println(rows[0]);          // null
System.out.println(rows[0].length);   // NullPointerException
```

You must allocate each row before using it. This is the most common multidimensional
array bug.

### You cannot skip a dimension

```java
int[][] a = new int[][4];    // ERROR — cannot specify a later dimension
                             // without the earlier one
```

The outer dimension must always be given, because there is nothing to hold the rows
otherwise.

---

## 3. Jagged arrays

Because each row is a separate object, rows may have different lengths:

```java
int[][] triangle = new int[4][];
for (int i = 0; i < 4; i++) {
    triangle[i] = new int[i + 1];
}
```

```
row 0: [0]
row 1: [0][0]
row 2: [0][0][0]
row 3: [0][0][0][0]
```

Or with a literal:

```java
int[][] jagged = {
    {1},
    {2, 3},
    {4, 5, 6}
};
```

**Never assume rectangularity.** Always iterate with `grid[i].length`, not `grid[0].length`:

```java
for (int i = 0; i < grid.length; i++) {
    for (int j = 0; j < grid[i].length; j++) {   // NOT grid[0].length
        ...
    }
}
```

The enhanced `for` handles this automatically, which is a good reason to prefer it:

```java
for (int[] row : grid) {
    for (int value : row) {
        ...
    }
}
```

---

## 4. Printing and comparing

```java
int[][] grid = {{1, 2}, {3, 4}};

System.out.println(grid);                    // [[I@1b6d3586
System.out.println(Arrays.toString(grid));   // [[I@76ed5528, [I@2c7b84de]  ← still wrong!
System.out.println(Arrays.deepToString(grid)); // [[1, 2], [3, 4]]  ← correct
```

`Arrays.toString` calls `toString()` on each element — and each element is an *array*,
whose `toString()` is the useless default. You need `deepToString`.

The same applies to equality:

```java
Arrays.equals(a, b)      // compares row REFERENCES — false for equal contents
Arrays.deepEquals(a, b)  // compares recursively — correct
```

| Operation | 1D | 2D+ |
| --- | --- | --- |
| Print | `Arrays.toString` | `Arrays.deepToString` |
| Compare | `Arrays.equals` | `Arrays.deepEquals` |
| Hash | `Arrays.hashCode` | `Arrays.deepHashCode` |

---

## 5. Copying a 2D array

```java
int[][] original = {{1, 2}, {3, 4}};
int[][] shallow = original.clone();          // or Arrays.copyOf
shallow[0][0] = 99;
System.out.println(original[0][0]);          // 99 — the ROWS are shared!
```

`clone()` on a 2D array copies the outer array of references. Both outer arrays now point
at the **same row objects**. To copy properly, copy every row:

```java
int[][] deep = new int[original.length][];
for (int i = 0; i < original.length; i++) {
    deep[i] = original[i].clone();
}
```

Or with a stream:

```java
int[][] deep = Arrays.stream(original)
                     .map(int[]::clone)
                     .toArray(int[][]::new);
```

---

## 6. Row-major order and why it is faster

Iterating rows-then-columns is meaningfully faster than columns-then-rows on large arrays:

```java
// FAST — walks each row's contiguous memory in order
for (int i = 0; i < rows; i++)
    for (int j = 0; j < cols; j++)
        sum += grid[i][j];

// SLOW — jumps to a different row object on every single access
for (int j = 0; j < cols; j++)
    for (int i = 0; i < rows; i++)
        sum += grid[i][j];
```

The reason is the **CPU cache**. When you read `grid[i][0]`, the processor loads an entire
cache line (typically 64 bytes = 16 `int`s) into fast memory. Reading `grid[i][1]` through
`grid[i][15]` then costs almost nothing — the data is already there.

Column-major order reads one `int` from each cache line and then jumps away, so every
access is a cache miss. On a large array this is often a **2–10× difference** for
identical logic and identical results.

This is one of the few micro-optimisations genuinely worth knowing, because it costs
nothing to write the loops in the right order.

---

## 7. Common patterns

### Transpose

```java
int[][] transposed = new int[cols][rows];
for (int i = 0; i < rows; i++)
    for (int j = 0; j < cols; j++)
        transposed[j][i] = original[i][j];
```

### Sum each row / column

```java
for (int[] row : grid) {
    int rowSum = Arrays.stream(row).sum();
}
```

### A grid of objects

```java
String[][] board = new String[3][3];
for (String[] row : board) {
    Arrays.fill(row, ".");      // fill() works per row
}
```

Note `Arrays.fill(board, ".")` would fail to compile — the outer array holds `String[]`,
not `String`.

---

## 8. When not to use a 2D array

For most real problems, a 2D array is *not* the right model:

| Instead of | Consider |
| --- | --- |
| `String[][]` for records | A `List<Person>` with a proper class |
| `int[][]` for a lookup | A `Map<Key, Value>` |
| A sparse grid mostly zeros | A `Map<Point, Value>` |
| Matrix maths | A library, or flat `double[]` with manual indexing |

For very large numeric work, a **flat array with computed indices** is often faster,
because it is genuinely contiguous:

```java
double[] flat = new double[rows * cols];
double value = flat[i * cols + j];      // manual row-major indexing
```

That is what serious numerical libraries do internally, precisely to get the contiguity
Java's `[][]` does not give you.

---

## 9. Summary

- `int[][]` is an **array of references to arrays** — four objects for `new int[3][4]`,
  not one contiguous block.
- Rows are independent, so arrays can be **jagged**. Always use `grid[i].length`.
- `new int[3][]` leaves the rows `null` — allocate each one before use.
- Use `Arrays.deepToString`, `deepEquals` and `deepHashCode` for 2D and deeper.
- `clone()` on a 2D array is **shallow** — the rows are shared. Clone each row.
- **Row-major iteration is faster** than column-major, often several times, because of CPU
  cache lines. Write the loops in that order by default.
- For anything other than genuine grids, a class or a `Map` usually models the problem
  better; for heavy numerics, a flat array with computed indices is faster still.

---

**Previous:** [12 — Arrays](12-arrays.md) ·
**Next:** [14 — Strings](14-strings.md)
