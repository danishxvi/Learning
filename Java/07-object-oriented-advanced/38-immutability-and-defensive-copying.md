# 38 · Immutability and Defensive Copying

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/38-immutability-and-defensive-copying.java
> ```

An **immutable** object cannot change after construction. This is the single highest-value
design habit in Java, and it ties together `final` (31), `equals`/`hashCode` (33),
encapsulation (24) and records (36).

---

## 1. What immutability buys you

| Benefit | Why |
| --- | --- |
| **Thread safety, free** | Nothing changes, so there is nothing to synchronise |
| **Safe as a hash key** | The `hashCode` can never drift (lesson 33's disaster) |
| **Safe to share** | Hand the same instance to anyone; no defensive copy needed |
| **Safe to cache** | The value is the value, forever |
| **Simpler reasoning** | A value assigned once is a value you can trust for the rest of the method |
| **Failure atomicity** | A failed operation cannot leave the object half-updated |

The thread-safety point is not a small one. An immutable object is **automatically**
thread-safe with no `synchronized`, no `volatile`, no locks. The `final`-field memory-model
guarantee (lesson 31) makes it real: once the constructor returns, every thread sees the
fully-built object.

---

## 2. The five rules

1. **Make the class `final`** — or use a private constructor with static factories — so a
   subclass cannot add mutability.
2. **Make every field `private final`.**
3. **Provide no setters**, and no method that changes state.
4. **Defensively copy mutable objects on the way in** (constructor).
5. **Defensively copy mutable objects on the way out** (getters), or return unmodifiable
   views.

Rules 4 and 5 are the ones people forget, and they are what this lesson is really about.

---

## 3. The two leaks

`private final` protects the **field**, not the **object it points at**. There are exactly
two ways state escapes:

### Leak 1 — storing a caller's mutable object

```java
final class Period {
    private final Date start;

    Period(Date start) {
        this.start = start;          // LEAK — the caller still holds it
    }
}

Date start = new Date();
Period period = new Period(start);
start.setTime(0);                    // the "immutable" Period just changed
```

**Fix: copy in the constructor.**

```java
this.start = new Date(start.getTime());
```

### Leak 2 — handing out an internal mutable object

```java
Date getStart() {
    return start;                    // LEAK — the caller can mutate it
}

period.getStart().setTime(0);        // the "immutable" Period changed again
```

**Fix: copy on the way out**, or return an unmodifiable view, or return an immutable type.

Both leaks must be closed. Closing one and not the other leaves the object mutable.

### The validation ordering trap

```java
Period(Date start, Date end) {
    if (start.after(end)) throw new IllegalArgumentException();   // check the ORIGINAL
    this.start = new Date(start.getTime());                        // then copy
}
```

This has a **time-of-check to time-of-use** bug: between the check and the copy, another
thread could mutate `start`. Always **copy first, then validate the copy**:

```java
this.start = new Date(start.getTime());
this.end = new Date(end.getTime());
if (this.start.after(this.end)) throw new IllegalArgumentException();
```

This is a real security consideration, not a theoretical one — it is the mechanism behind
several historical JDK vulnerabilities.

---

## 4. Copy, view, or immutable type?

| Approach | Cost | Caller can modify? | Reflects later changes? |
| --- | --- | --- | --- |
| `new ArrayList<>(list)` | O(n) copy | Yes, their copy | No |
| `Collections.unmodifiableList(list)` | O(1) wrapper | No | **Yes** — it is a live view |
| `List.copyOf(list)` | O(n) copy | No | No |

`Collections.unmodifiableList` is a **view**: the caller cannot modify it, but they will see
changes you make to the underlying list afterwards. For a truly immutable object that never
changes, either is fine; for a mutable class exposing a read-only view, the distinction
matters.

**`List.copyOf` is usually the right answer** — independent and unmodifiable. Note it also
rejects `null` elements.

> **`List.of()`, `Map.of()`, `Set.of()` are genuinely immutable** and reject `null`.
> `Arrays.asList()` is a **fixed-size mutable view** over the array — a different thing
> entirely (lesson 16).

---

## 5. Which types are already immutable?

**Immutable — safe to share, no copying needed:**

`String`, all primitive wrappers, `BigInteger`, `BigDecimal`, `LocalDate`, `LocalDateTime`,
`Instant`, `Duration`, `Period` (the `java.time` one), `UUID`, enums, records *of immutable
components*, `List.of`/`Map.of`/`Set.of` results, `Optional`.

**Mutable — must be copied:**

`Date`, `Calendar`, `StringBuilder`, arrays (**always**), `ArrayList` and every standard
collection, `SimpleDateFormat`, most of your own classes.

> **Arrays are always mutable.** There is no immutable array in Java. `final int[] a` still
> allows `a[0] = 99`. To expose one safely, return `a.clone()` or wrap it in a
> `List.copyOf`.

`java.util.Date` being mutable is a genuine historical mistake, and the whole reason
`java.time` was designed to be immutable (lesson 60).

---

## 6. Making changes: return a new object

Immutable does not mean "cannot compute new values":

```java
LocalDate today = LocalDate.now();
LocalDate tomorrow = today.plusDays(1);      // a NEW object; `today` is unchanged
```

Your own types follow the same shape:

```java
Money plus(Money other) {
    return new Money(this.amount + other.amount, currency);
}
```

The naming convention matters: **`plusX`, `withX`, `toX`** signal a new object;
**`setX`, `addX`** signal mutation. `String.replace` returning a new string rather than
mutating is exactly this convention (lesson 14).

### The cost, and why it usually does not matter

Creating objects is cheap on the JVM — young-generation allocation is close to a pointer
bump, and short-lived objects are collected almost for free. Escape analysis can eliminate
the allocation entirely.

The genuine exception is building a value in a loop, which is why `StringBuilder` exists
(lesson 15). **Measure before trading away immutability.**

---

## 7. Records get most of this free

```java
record Period(LocalDate start, LocalDate end) {
    Period {
        if (start.isAfter(end)) throw new IllegalArgumentException();
    }
}
```

A record is implicitly `final`, its components are `private final`, and it has no setters —
rules 1, 2 and 3 are automatic. But **rules 4 and 5 are not**: a record is only *shallowly*
immutable, so a `List` or array component still needs copying in the compact constructor
(lesson 36).

---

## 8. When immutability is the wrong choice

- **Large objects with frequent small changes** — copying a 10,000-element structure per
  edit is real cost. Consider a builder, or a persistent data structure.
- **Objects with genuine identity and a lifecycle** — a database entity that is *updated* is
  naturally mutable.
- **Performance-critical inner loops**, *after profiling*.
- **Framework requirements** — some older frameworks need a no-arg constructor and setters.

Even then, prefer **immutable value objects** inside mutable entities. The rule is not
"never mutate"; it is **"default to immutable, and mutate deliberately."**

---

## 9. Summary

- Immutable = cannot change after construction. It gives thread safety, hash-key safety,
  free sharing, and simpler reasoning.
- The five rules: `final` class, `private final` fields, no setters, **copy in**, **copy
  out**.
- `private final` protects the **field, not the object**. Both leaks — storing a caller's
  object and returning your own — must be closed.
- **Copy first, then validate the copy.** Validating the original is a
  time-of-check/time-of-use bug.
- `List.copyOf` = independent and unmodifiable (usual best);
  `Collections.unmodifiableList` = a **live view**; `new ArrayList<>(...)` = an independent
  mutable copy.
- **Arrays are always mutable** — there is no immutable array. Return `clone()`.
- `String`, wrappers, `BigDecimal`, `java.time`, `UUID` and enums are immutable.
  `Date`, `Calendar`, `StringBuilder`, arrays and all standard collections are not.
- Change by **returning a new object**; name such methods `plusX`/`withX`, not `setX`.
- Allocation is cheap on the JVM — measure before trading immutability for speed.
- Records give you rules 1–3 free, but **not** the defensive copies.
- **Default to immutable; mutate deliberately.**

---

**Previous:** [37 — Sealed classes and pattern matching](37-sealed-classes-and-pattern-matching.md) ·
**Next:** [39 — Exception handling basics](../08-exception-handling/39-exception-handling-basics.md)
