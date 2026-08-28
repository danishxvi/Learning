# 20 · Recursion and the Call Stack

> **Run the code for this lesson**
> ```bash
> java Java/05-methods/20-recursion.java
> ```

Recursion is a method calling itself. The idea takes a minute; using it well takes
understanding what the **call stack** is actually doing, and knowing when recursion is the
wrong tool.

---

## 1. The shape of every recursive method

```java
static int factorial(int n) {
    if (n <= 1) {
        return 1;                    // BASE CASE — stops the recursion
    }
    return n * factorial(n - 1);     // RECURSIVE CASE — moves TOWARD the base
}
```

Two parts, both mandatory:

| Part | Job | If you omit it |
| --- | --- | --- |
| **Base case** | A condition that returns without recursing | Infinite recursion → `StackOverflowError` |
| **Recursive case** | Calls itself with an argument **closer to the base case** | Same |

The second half of that sentence matters as much as the first. `factorial(n)` calling
`factorial(n)` has a base case and still never terminates.

### The three questions

Before writing any recursive method, answer these:

1. **What is the smallest input I can answer immediately?** → the base case.
2. **How do I make the problem smaller?** → the recursive step.
3. **Does every path reach the base case?** → termination.

---

## 2. The call stack

Every method call pushes a **stack frame** holding that call's parameters, local variables
and return address. `factorial(4)` builds this:

```
       CALL                          RETURN
  factorial(4)                    24  ← 4 * 6
    factorial(3)                   6  ← 3 * 2
      factorial(2)                 2  ← 2 * 1
        factorial(1) → 1           1  ← base case
```

Frames pile up on the way **down**, then unwind on the way **back up**. Nothing is
computed until the base case is reached — `4 * factorial(3)` cannot multiply until
`factorial(3)` returns.

### `StackOverflowError`

The stack has a fixed size (typically ~512 KB to 1 MB, roughly 10,000–20,000 frames for a
simple method). Exceed it and you get:

```
Exception in thread "main" java.lang.StackOverflowError
```

Note it is an **`Error`**, not an `Exception`. You can technically catch it, but you
should not — it means a programming mistake, and the stack may be in an unusable state.

You can raise the limit with `-Xss2m`, but if you need to, the algorithm is usually wrong.

---

## 3. Java does not have tail-call optimisation

```java
static int sumTo(int n, int accumulator) {
    if (n == 0) return accumulator;
    return sumTo(n - 1, accumulator + n);   // TAIL call — the last thing done
}
```

In Scala, Kotlin (with `tailrec`), Haskell or Scheme, a compiler recognises that this
call's frame is no longer needed and **reuses it**, making the recursion effectively a
loop with constant stack usage.

**Java does not do this.** `sumTo(100000, 0)` still overflows the stack. The reasons are
partly historical and partly that Java's security model has relied on walking real stack
frames.

**The practical consequence:** in Java, deep recursion must be converted to iteration
manually. There is no annotation that saves you.

---

## 4. Recursion vs iteration

Everything recursive can be written iteratively, and vice versa.

| | Recursion | Iteration |
| --- | --- | --- |
| Memory | O(depth) stack frames | O(1) |
| Speed | Slower — call overhead per level | Faster |
| Risk | `StackOverflowError` | Infinite loop (no crash) |
| Readability | **Better** for tree/graph/divide-and-conquer | Better for linear work |

**Use recursion when the *data* is recursive:** trees, file systems, JSON, nested
expressions, graph traversal, divide-and-conquer sorts. The code then mirrors the
structure, and an iterative version needs an explicit stack that is strictly harder to
read.

**Use iteration for linear work.** `factorial` and `fibonacci` are taught with recursion
because they are simple to explain, not because recursion is the right implementation.

```java
// Recursive — elegant, O(n) stack
static int factorial(int n) {
    return n <= 1 ? 1 : n * factorial(n - 1);
}

// Iterative — the one you would ship
static int factorial(int n) {
    int result = 1;
    for (int i = 2; i <= n; i++) result *= i;
    return result;
}
```

---

## 5. The Fibonacci disaster

The textbook example of recursion is also the textbook example of getting it wrong:

```java
static long fib(int n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);
}
```

This is **O(2ⁿ)**. `fib(40)` makes over 300 million calls, because the same subproblems
are recomputed constantly:

```
                 fib(5)
            /            \
        fib(4)            fib(3)
       /      \          /      \
   fib(3)    fib(2)   fib(2)   fib(1)
   /    \     /   \    /   \
fib(2) fib(1) ...     ...          ← fib(2) computed three times already
```

### Fix 1 — memoisation (top-down)

Cache each result the first time it is computed:

```java
static long fib(int n, Map<Integer, Long> cache) {
    if (n <= 1) return n;
    return cache.computeIfAbsent(n, k -> fib(k - 1, cache) + fib(k - 2, cache));
}
```

O(2ⁿ) → **O(n)**. `fib(50)` becomes instant.

### Fix 2 — iteration (bottom-up)

```java
static long fib(int n) {
    long previous = 0, current = 1;
    for (int i = 2; i <= n; i++) {
        long next = previous + current;
        previous = current;
        current = next;
    }
    return n == 0 ? 0 : current;
}
```

O(n) time, **O(1) space**, no stack risk. This is the version to ship.

The lesson generalises: **a recursive solution that recomputes overlapping subproblems
needs memoisation or a bottom-up rewrite.** That observation is the whole of dynamic
programming.

---

## 6. Where recursion genuinely wins

### Tree traversal

```java
static void printTree(Node node) {
    if (node == null) return;         // base case
    printTree(node.left);
    System.out.println(node.value);
    printTree(node.right);
}
```

The iterative version needs an explicit `Deque` and is much harder to follow. The tree is
recursive, so the code should be.

### Directory walking

```java
static long size(File file) {
    if (file.isFile()) return file.length();
    long total = 0;
    for (File child : file.listFiles()) total += size(child);
    return total;
}
```

Depth is bounded by the directory depth — a few dozen at most — so the stack is safe.

### Divide and conquer

Merge sort, quicksort and binary search are all naturally recursive, and their depth is
**O(log n)**: for a billion elements, only about 30 frames. Depth being logarithmic is
exactly why these are safe.

### Backtracking

N-queens, sudoku, permutations, maze solving — try a choice, recurse, undo if it fails.
The undo step is what makes an iterative version painful.

---

## 7. Mutual and indirect recursion

```java
static boolean isEven(int n) {
    return n == 0 ? true : isOdd(n - 1);
}
static boolean isOdd(int n) {
    return n == 0 ? false : isEven(n - 1);
}
```

Methods that call each other in a cycle. Legal and occasionally natural — recursive-descent
parsers are built this way (`parseExpression` calls `parseTerm` calls `parseFactor` calls
`parseExpression`). Just as prone to stack overflow.

---

## 8. Converting recursion to iteration

Any recursion can be made iterative with an **explicit stack**, because that is exactly
what the call stack was doing for you:

```java
// Recursive
static void traverse(Node node) {
    if (node == null) return;
    visit(node);
    traverse(node.left);
    traverse(node.right);
}

// Iterative — you manage the stack yourself
static void traverse(Node root) {
    Deque<Node> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
        Node node = stack.pop();
        if (node == null) continue;
        visit(node);
        stack.push(node.right);    // pushed first, so popped last
        stack.push(node.left);
    }
}
```

Use `ArrayDeque`, not the legacy `Stack` class (lesson 48). Do this conversion when depth
could exceed a few thousand — parsing untrusted input, walking user-supplied structures, or
processing linked lists of unknown length.

---

## 9. Summary

- Every recursive method needs a **base case** and a recursive case that moves **toward**
  it.
- Each call pushes a **stack frame**; frames unwind only after the base case is reached.
- Exceeding the stack throws **`StackOverflowError`** — an `Error`, not an `Exception`.
  Do not catch it.
- **Java has no tail-call optimisation.** Deep recursion must be converted to iteration by
  hand.
- Recursion costs O(depth) memory and is slower; iteration is O(1) and faster.
- Use recursion when the **data** is recursive — trees, files, JSON, divide-and-conquer,
  backtracking. Depth there is usually shallow or logarithmic.
- Naive `fib` is **O(2ⁿ)**. Memoise it or write it bottom-up — that observation is the
  foundation of dynamic programming.
- Any recursion converts to iteration with an explicit `ArrayDeque`.

---

**Previous:** [19 — Varargs](19-varargs.md) ·
**Next:** [21 — Classes and objects](../06-object-oriented-core/21-classes-and-objects.md)
