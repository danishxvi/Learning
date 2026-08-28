# 11 · `break`, `continue`, Labels and Loop Design

> **Run the code for this lesson**
> ```bash
> java Java/03-control-flow/11-break-continue-and-labels.java
> ```

Three small keywords that change control flow, plus the question they force you to
answer: **when is a loop the wrong shape entirely?**

---

## 1. `break` — leave the loop now

```java
for (int i = 0; i < 10; i++) {
    if (i == 5) {
        break;          // exit the loop entirely
    }
    System.out.println(i);   // 0 1 2 3 4
}
```

`break` exits the **innermost** enclosing loop or `switch`. Execution resumes at the first
statement *after* that loop.

Its main use is the **search-and-stop** pattern: once you have found what you came for,
continuing is wasted work.

```java
String found = null;
for (String name : names) {
    if (name.startsWith("D")) {
        found = name;
        break;              // stop — we have our answer
    }
}
```

---

## 2. `continue` — skip to the next iteration

```java
for (int i = 0; i < 5; i++) {
    if (i % 2 == 0) {
        continue;       // skip the rest of THIS iteration
    }
    System.out.println(i);   // 1 3
}
```

### The critical difference between loop types

> **In a `for` loop, `continue` still runs the update clause. In a `while` loop, it does
> not — it jumps straight back to the condition.**

This is the most common cause of accidental infinite loops:

```java
int i = 0;
while (i < 5) {
    if (i == 2) {
        continue;       // INFINITE LOOP — i is never incremented
    }
    System.out.println(i);
    i++;
}
```

In the equivalent `for` loop this is perfectly safe, because `i++` lives in the header and
runs regardless. If you use `continue` in a `while`, make sure the update happens before
it.

---

## 3. Labels — breaking out of nested loops

By default `break` only escapes one level. A **label** lets it escape several:

```java
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (grid[i][j] == target) {
            break outer;        // leaves BOTH loops
        }
    }
}
```

A label is an identifier followed by a colon, placed immediately before a loop. Then:

- `break label;` exits the labelled loop entirely.
- `continue label;` skips to the next iteration **of the labelled loop**.

```java
outer:
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (j == 1) {
            continue outer;     // abandon this i, move to the next i
        }
        System.out.println(i + "," + j);   // only j == 0 ever prints
    }
}
```

### Are labels bad style?

They have a reputation as "Java's `goto`", which is unfair but not baseless. The honest
position:

- **Labels are fine** for breaking out of nested loops. That is exactly what they exist
  for, and the alternatives (a `found` flag checked in both conditions) are often worse.
- **Labels are a smell** when they are jumping around complicated logic. That usually
  means the loop body should be a method.

The cleanest alternative is almost always **extract a method and `return`**:

```java
// with a label
outer:
for (...) { for (...) { if (match) break outer; } }

// as a method — usually clearer
static int[] find(int[][] grid, int target) {
    for (int i = 0; ...) {
        for (int j = 0; ...) {
            if (grid[i][j] == target) return new int[]{i, j};
        }
    }
    return null;
}
```

`return` exits everything, needs no label, and gives the block a name that says what it
does. Reach for this first; use a label when extraction genuinely does not fit.

> Java has the `goto` keyword **reserved but unimplemented** — it is on the keyword list
> purely so it cannot be used as an identifier. Labelled `break`/`continue` are the
> deliberate, structured substitute.

---

## 4. `break` in a `switch` inside a loop

A trap worth stating explicitly:

```java
for (int i = 0; i < 5; i++) {
    switch (i) {
        case 2:
            break;      // breaks the SWITCH, not the loop!
    }
    System.out.println(i);   // still prints every i
}
```

`break` binds to the innermost `break`-able construct, and a `switch` counts. To leave the
loop from inside a `switch`, you need a label:

```java
loop:
for (int i = 0; i < 5; i++) {
    switch (i) {
        case 2 -> { break loop; }
    }
}
```

This is another reason to prefer arrow-form `switch` and switch expressions — they do not
use `break` at all.

---

## 5. `return` inside a loop

`return` exits the whole **method**, and therefore every enclosing loop:

```java
static boolean contains(int[] array, int target) {
    for (int value : array) {
        if (value == target) {
            return true;        // exits the loop AND the method
        }
    }
    return false;
}
```

This is usually the clearest way to write a search. Note there is no `break` and no result
variable — the structure carries the meaning.

### `finally` still runs

```java
try {
    for (...) {
        if (...) return value;    // finally runs BEFORE the method actually returns
    }
} finally {
    cleanup();
}
```

Lesson 39 covers this properly, including the pathological case where `finally` contains
its own `return` and silently discards the real one.

---

## 6. When the loop is the wrong shape

`break` and `continue` are often signals. Before reaching for them, ask whether the loop
should exist at all.

### `continue` as a filter

```java
// with continue
for (String name : names) {
    if (name.isEmpty()) continue;
    if (name.length() < 3) continue;
    process(name);
}

// as a stream — the intent is now explicit
names.stream()
     .filter(n -> !n.isEmpty())
     .filter(n -> n.length() >= 3)
     .forEach(this::process);
```

### `break` as a search

```java
// with break
String found = null;
for (String name : names) {
    if (name.startsWith("D")) { found = name; break; }
}

// as a stream — short-circuits identically
Optional<String> found = names.stream()
                              .filter(n -> n.startsWith("D"))
                              .findFirst();
```

`findFirst()` is lazy: it stops at the first match exactly as `break` does. You lose
nothing in performance and gain a line that states its purpose.

**This is not a rule that streams are always better.** Loops are clearer when you need an
index, when the body is long, or when you are mutating state. But if a loop body is
mostly `continue` guards, a stream usually reads better. Lesson 54 covers streams.

---

## 7. Loop design checklist

Before you finish writing a loop, check:

| Question | Why |
| --- | --- |
| Can this ever not terminate? | The update must move toward the condition |
| Is the bound `<` or `<=`? | The most common off-by-one source |
| Does `continue` skip the update in a `while`? | Classic infinite loop |
| Am I modifying the collection I am iterating? | `ConcurrentModificationException` |
| Am I building a `String` with `+=`? | O(n²); use `StringBuilder` |
| Does this need to be nested? | Two nested loops over n is n² work |
| Would a method with `return` be clearer than a label? | Usually yes |

---

## 8. Summary

- `break` exits the innermost loop **or switch**; `continue` skips to the next iteration.
- In a `for`, `continue` **still runs the update**. In a `while`, it does not — that is
  the classic accidental infinite loop.
- `break` inside a `switch` inside a loop exits the **switch only**. Use a label, or use
  arrow-form `switch`.
- Labelled `break`/`continue` escape nested loops. They are legitimate, but extracting a
  method and using `return` is usually clearer.
- `goto` is a reserved but unimplemented keyword; labels are the structured substitute.
- `return` exits the method and every loop in it — often the cleanest way to write a search.
- A loop body that is mostly `continue` guards is usually a `filter`; a `break` search is
  usually a `findFirst`.

---

**Previous:** [10 — Loops](10-loops.md) ·
**Next:** [12 — Arrays](../04-arrays-and-strings/12-arrays.md)
