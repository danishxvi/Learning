# 10 · Loops — `for`, `while`, `do-while` and the Enhanced `for`

> **Run the code for this lesson**
> ```bash
> java Java/03-control-flow/10-loops.java
> ```

Java has four looping constructs. Choosing the right one is mostly about **what you know
before the loop starts**, and the bugs are almost all off-by-one errors and accidental
infinite loops.

---

## 1. The four loops at a glance

| Loop | Use when | Runs at least once? |
| --- | --- | --- |
| `for` | You know the iteration count, or need the index | No |
| `while` | You loop until a condition changes | No |
| `do-while` | The body must run before the test | **Yes** |
| Enhanced `for` | You just need each element, in order | No |

---

## 2. The classic `for`

```java
for (initialisation; condition; update) {
    // body
}
```

```java
for (int i = 0; i < 5; i++) {
    System.out.println(i);       // 0 1 2 3 4
}
```

### The exact execution order

This trips people up, so learn it precisely:

1. **Initialisation** — once, before anything else.
2. **Condition** — checked. If `false`, the loop ends immediately.
3. **Body** — runs.
4. **Update** — runs.
5. Go to step 2.

Note the update runs **after** the body, not before the next condition check — which is
the same thing, but stating it this way explains why `continue` still runs the update
(see lesson 11).

### Every part is optional

```java
for (;;) { }              // infinite loop — the idiomatic form
int i = 0;
for (; i < 5; i++) { }    // initialisation done outside
for (int j = 0; j < 5;) { j++; }   // update inside the body
```

`for (;;)` and `while (true)` compile to identical bytecode. Use whichever your team
prefers; `while (true)` reads more clearly to most people.

### Multiple variables, and the comma

```java
for (int i = 0, j = 10; i < j; i++, j--) {
    System.out.println(i + " " + j);
}
```

Both variables must be the **same type** — `int i = 0, String s = ""` is not allowed. The
comma here is a special `for`-loop separator, not an operator.

### Scope

```java
for (int i = 0; i < 5; i++) { }
System.out.println(i);        // ERROR: cannot find symbol
```

`i` exists only inside the loop. That is usually what you want; declare it outside if you
need the final value.

---

## 3. `while`

```java
while (condition) {
    // body
}
```

Use it when the number of iterations is not known up front:

```java
while (scanner.hasNextLine()) {
    process(scanner.nextLine());
}

while (remaining > 0) {
    remaining -= take();
}
```

### The infinite-loop causes

```java
int i = 0;
while (i < 5) {
    System.out.println(i);     // forgot i++ — runs forever
}
```

```java
double x = 0.0;
while (x != 1.0) {             // may NEVER be exactly 1.0
    x += 0.1;
}
```

That second one is the floating-point trap from lesson 03 wearing a different hat. Use
`<` or `<=`, never `!=` or `==`, with floating-point loop conditions.

```java
while (x > 0);                 // stray semicolon = empty body = infinite loop
{
    x--;
}
```

---

## 4. `do-while`

```java
do {
    // body
} while (condition);           // note the semicolon
```

**The body always runs at least once**, because the condition is tested afterwards. That
makes it the right choice for menus, retry loops, and anything that must attempt before
it can decide:

```java
int choice;
do {
    showMenu();
    choice = readChoice();
} while (choice != 0);
```

Writing that with `while` would force you to duplicate the prompt before the loop. In
practice `do-while` is the least-used loop in Java — but when it fits, nothing else is as
clean.

---

## 5. The enhanced `for` (for-each)

```java
for (String name : names) {
    System.out.println(name);
}
```

Read `:` as **"in"**. It works on **arrays** and anything implementing `Iterable`, which
is every collection.

### What you gain

- No index variable, so no off-by-one errors.
- No `i < array.length` to get wrong.
- Reads like the intent: "for each name in names".

### What you give up

| You cannot | Because |
| --- | --- |
| Know the index | There is no index variable |
| Modify the array/collection **structurally** | Throws `ConcurrentModificationException` |
| Iterate backwards | It always goes forwards |
| Skip elements by index | No index to skip with |
| Iterate two collections in step | One `Iterable` per loop |

### The `ConcurrentModificationException` trap

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);        // ConcurrentModificationException
    }
}
```

Removing while iterating breaks the iterator's internal modification count. The three
fixes:

```java
list.removeIf(s -> s.equals("b"));              // best — Java 8+

Iterator<String> it = list.iterator();          // explicit iterator
while (it.hasNext()) {
    if (it.next().equals("b")) it.remove();     // the iterator's own remove
}

for (String s : new ArrayList<>(list)) {        // iterate a copy
    if (s.equals("b")) list.remove(s);
}
```

> **Assigning to the loop variable does nothing useful.** `for (int n : numbers) { n = 0; }`
> changes only the local copy. For primitives and immutable objects you cannot modify the
> source this way — use an indexed `for`.

---

## 6. Nested loops and complexity

```java
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        System.out.print(i * j + "\t");
    }
    System.out.println();
}
```

The inner loop completes fully for each single iteration of the outer loop. Two nested
loops over `n` items do `n²` work; three do `n³`. This is where performance problems are
born:

| Nesting | Iterations at n=1,000 | At n=10,000 |
| --- | --- | --- |
| One loop | 1,000 | 10,000 |
| Two nested | 1,000,000 | 100,000,000 |
| Three nested | 1,000,000,000 | 10¹² (hours) |

If you find yourself writing a nested loop to search a collection, a `HashMap` usually
turns `O(n²)` into `O(n)`. Lesson 47 covers that.

---

## 7. Loop performance notes

### Hoist invariant work out of the loop

```java
for (int i = 0; i < list.size(); i++) { }        // size() called every iteration
int size = list.size();
for (int i = 0; i < size; i++) { }               // called once
```

The JIT often optimises this anyway, but it is free to write it correctly and it makes
the intent clearer.

### String concatenation in a loop is genuinely slow

```java
String result = "";
for (int i = 0; i < 10000; i++) {
    result += i;                  // creates a NEW String every iteration — O(n²)
}
```

Strings are immutable, so `+=` builds an entirely new string each time. Use
`StringBuilder`:

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 10000; i++) {
    sb.append(i);
}
String result = sb.toString();
```

For 10,000 iterations this is the difference between milliseconds and a noticeable pause.
Lesson 15 measures it.

### Enhanced `for` vs indexed `for` on an `ArrayList`

Effectively identical — both are O(1) per element. On a `LinkedList`, however, an indexed
`for` is **O(n²)** because `get(i)` walks the list from the start each time. Always use
the enhanced `for` (or an iterator) on a `LinkedList`. Lesson 45 covers this.

---

## 8. Summary

- `for` when you know the count or need the index; `while` when you loop until something
  changes; `do-while` when the body must run at least once; enhanced `for` when you just
  need each element.
- `for` execution order: init once, then condition → body → update → condition …
- The loop variable declared in a `for` header is scoped to the loop.
- `for (;;)` and `while (true)` are identical.
- Infinite-loop causes: forgotten update, `!=` on floating point, and the stray semicolon.
- The enhanced `for` cannot give you an index, cannot go backwards, and throws
  `ConcurrentModificationException` if you modify the collection structurally — use
  `removeIf`, an explicit `Iterator`, or iterate a copy.
- Assigning to the enhanced-`for` variable changes only the local copy.
- Nested loops multiply: two over `n` items is `n²`. A `HashMap` often removes the nesting.
- Never build strings with `+=` inside a loop; use `StringBuilder`.

---

**Previous:** [09 — switch](09-switch-statement-and-expression.md) ·
**Next:** [11 — break, continue and labels](11-break-continue-and-labels.md)
