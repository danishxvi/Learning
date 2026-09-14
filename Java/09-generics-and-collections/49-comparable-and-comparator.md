# 49 · `Comparable` and `Comparator`

> **Run the code for this lesson**
> ```bash
> java Java/09-generics-and-collections/49-comparable-and-comparator.java
> ```

Lesson 46 showed a `Comparator` that silently dropped Bob from a `TreeSet` because it only
compared by age. This lesson explains exactly why that was legal — and reproduces the
classic `compareTo()` bug that still ships in real code today: subtraction that overflows.

---

## 1. `Comparable`: the class picks its own order

```java
class Employee implements Comparable<Employee> {
    private final int salary;
    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.salary, other.salary);
    }
}
```

```java
employees.sort(null);   // null Comparator -> use natural ordering, i.e. compareTo()
```

Real result: `[Dave(45000), Carol(58000), Alice(72000), Bob(72000)]`. `Collections.sort(list)`
and `list.sort(null)` mean the same thing: use `compareTo()`. A class can only have **one**
natural order — that's exactly why `Comparator` exists for everything else.

---

## 2. `Comparator`: external, and composable

```java
list.sort(Comparator.comparing(Employee::name));                              // by name
list.sort(Comparator.comparing(Employee::name).reversed());                   // reversed
list.sort(Comparator.comparingInt(Employee::salary).thenComparing(Employee::name));  // by salary, then name
```

Real result for the last one, `[Dave(45000), Carol(58000), Alice(72000), Bob(72000)]` —
Alice and Bob tie at 72,000, broken by name.

`comparingInt`/`comparingLong`/`comparingDouble` avoid boxing an `Integer`/`Long`/`Double`
per comparison the way plain `comparing(Employee::salary)` would if `salary` were already
boxed — a real, if usually small, allocation cost avoided.

---

## 3. `List.sort`/`Collections.sort` is stable

"Stable" means elements that compare **equal** keep their original relative order.
`Arrays.sort`/`List.sort` on objects uses **TimSort**, a stable merge sort. This is what
makes multi-key sorting *by stages* work at all — sort by the least significant key first:

```java
list.sort(Comparator.comparing(Employee::name));       // stage 1: by name
list.sort(Comparator.comparingInt(Employee::salary));   // stage 2: by salary
```

Real result, starting from `[Eve(60000), Frank(55000), Grace(60000), Heidi(55000)]`:

```
[Frank(55000), Heidi(55000), Eve(60000), Grace(60000)]
```

Within each salary, names stayed alphabetical — Frank before Heidi at 55,000, Eve before
Grace at 60,000 — because the second sort never disturbed the relative order the first
sort had already established. A non-stable sort could scramble this.

> `Arrays.sort` on **primitive** arrays (`int[]`, `double[]`, ...) uses dual-pivot
> quicksort instead — faster, but *not* stable. This isn't a contradiction: stability only
> matters when equal elements carry other, independently-orderable state, and a bare
> primitive `int` has no such "other field" to disturb.

---

## 4. The `compareTo` subtraction bug, reproduced for real

A shortcut that appears in real production code constantly:

```java
public int compareTo(Box other) {
    return this.value - other.value;   // "clever", and WRONG
}
```

It *looks* correct — negative if smaller, positive if larger, zero if equal — and passes
every casual test. It fails the moment the subtraction itself **overflows** `int`'s range.

Real, measured proof:

```java
BuggyBox(-2147483648).compareTo(BuggyBox(1))
// MIN_VALUE - 1 wraps around to  -> 2147483647
// sign says LARGER - WRONG. -2147483648 really is smaller than 1.
```

The fix, `Integer.compare(a, b)`, never overflows:

```java
CorrectBox(-2147483648).compareTo(CorrectBox(1))
// result -> -1, sign says smaller - correct
```

And this isn't just a theoretical wrong sign on one comparison — a real sort built on the
buggy version produces genuinely wrong output:

```
sorted with the subtraction bug -> [5, -2147483645, -1000000000]   (NOT actually ascending)
sorted with Integer.compare     -> [-2147483645, -1000000000, 5]   (genuinely ascending)
```

**Always use `Integer.compare(a, b)` / `Long.compare` / `Double.compare`** (or
`comparingInt`/`comparingLong`/`comparingDouble`) — never subtract by hand.

---

## 5. The contract, and the callback to lesson 46's bug

For any `a`, `b`, `c`:

```
sgn(a.compareTo(b)) == -sgn(b.compareTo(a))                          antisymmetric
a.compareTo(b) > 0 && b.compareTo(c) > 0  implies  a.compareTo(c) > 0 transitive
a.compareTo(b) == 0  implies  sgn(a.compareTo(c)) == sgn(b.compareTo(c))  consistent ties
```

**Nothing in that contract requires `compareTo() == 0` to imply `equals() == true`.** That
is legal, documented, and exactly what lesson 46 Section 5 exploited: a `TreeSet<Person>`
ordered only by age treated `Alice(30)` and `Bob(30)` as "equal" (`compareTo == 0`) even
though `equals()` correctly said they were different people — and Bob was silently
dropped. That was never a `TreeSet` bug. It was this exact, legal gap in the contract, in
action.

The JDK's own `Comparable` Javadoc says this mismatch is "strongly recommended (though not
required)" to avoid — this lesson (and lesson 46) is the concrete "why."

---

## 6. Summary

- `Comparable` gives a class exactly **one** natural order, via `compareTo()`;
  `Comparator` supplies as many external orderings as needed, without touching the class.
- `Comparator.comparing`/`thenComparing`/`reversed` compose cleanly into multi-key
  orderings; `comparingInt`/`Long`/`Double` avoid unnecessary boxing.
- `List.sort`/`Collections.sort` on objects is **stable** (TimSort) — verified here by
  sorting in two stages and confirming equal-key elements kept their prior relative
  order. Primitive-array `Arrays.sort` uses a faster, non-stable quicksort instead.
- `this.a - this.b` as a `compareTo()` body is a real, reproducible bug: it overflows for
  values far enough apart, producing a wrong sign and, measurably, a genuinely
  mis-sorted list. Use `Integer.compare`/`Long.compare`/`Double.compare` instead, always.
- The `compareTo`/`compare` contract never requires consistency with `equals()` — a
  `Comparator` (or `compareTo`) that ties on a subset of fields is legal, and, as lesson
  46 demonstrated, can silently discard elements from a `TreeSet`/`TreeMap` that
  `equals()` would have kept as distinct.

---

**Previous:** [48 — `Queue`, `Deque` and `PriorityQueue`](48-queue-and-deque.md) ·
**Next:** [50 — Iterators and the `Collections` utility class](50-iterators-and-collections-utility.md)
