# 32 · The `Object` Class and Its Methods

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/32-object-class-methods.java
> ```

Every class in Java extends `Object`, whether you say so or not. That gives every object
eleven inherited methods — and knowing which to override, which to leave alone, and which
to avoid entirely is a large part of writing well-behaved Java.

---

## 1. The root of everything

```java
class Book { }
// is exactly
class Book extends Object { }
```

Even arrays and enums extend `Object`. The only things that do not are the **eight
primitives**, which is why `Object o = 5;` works only through autoboxing.

This is why `Object[]` can hold anything, why `List<Object>` accepts any reference, and why
`Object` is the universal fallback in pre-generics APIs.

---

## 2. The eleven methods

| Method | Override? | Purpose |
| --- | --- | --- |
| `toString()` | **Almost always** | Human-readable representation |
| `equals(Object)` | **When value equality matters** | Logical equality |
| `hashCode()` | **Always with `equals`** | Hash bucket placement |
| `getClass()` | **Cannot** — `final` | Runtime type |
| `clone()` | Rarely — prefer a copy constructor | Shallow copy |
| `finalize()` | **Never** — deprecated for removal | Legacy cleanup hook |
| `wait()` × 3 | **Cannot** — `final` | Thread coordination |
| `notify()`, `notifyAll()` | **Cannot** — `final` | Thread coordination |

**Six of the eleven are `final`** — `getClass`, `notify`, `notifyAll` and the three `wait`
overloads — and cannot be touched. Of the five you *can* override, `toString`, `equals` and
`hashCode` are the ones you will actually write; `clone` and `finalize` you should not touch
at all.

(The companion program counts these from `Object.class` itself rather than trusting this
table.)

---

## 3. `toString()`

The default is useless:

```java
public String toString() {
    return getClass().getName() + "@" + Integer.toHexString(hashCode());
}
```

`Book@1b6d3586` tells you the type and an identity hash, and nothing about the object.

**Override it on every class you write.** It costs three lines and improves every
`println`, string concatenation, debugger view, log line and collection dump — because all
of them call it automatically.

```java
@Override
public String toString() {
    return "Book[title=" + title + ", author=" + author + "]";
}
```

### Guidance

- Include the **fields that identify the object**, not every field.
- Keep it **short and single-line** — it often appears inside a collection's output.
- **Never** put sensitive data in it: passwords, tokens, full card numbers. `toString`
  ends up in logs, and logs end up in places you did not plan for.
- **Never let it throw.** A `toString` that throws makes debugging dramatically harder,
  because the debugger itself calls it. Guard against `null` fields.
- Records generate a sensible one automatically (lesson 36).

---

## 4. `equals()` and `hashCode()` — the summary

The default `equals` is **identity**: `this == other`. Two objects with identical contents
are unequal unless you override it.

**The contract is: if you override one, you must override the other.** Breaking that makes
`HashMap` and `HashSet` behave incorrectly in ways that are genuinely hard to debug.

Lesson 33 covers this in full — it is important enough to have its own lesson.

---

## 5. `getClass()`

```java
Object o = "hello";
o.getClass()                    // class java.lang.String
o.getClass().getSimpleName()    // "String"
o.getClass().getName()          // "java.lang.String"
```

`getClass()` is `final` — you cannot override it, and it always reports the **runtime**
type regardless of the declared type. That makes it reliable in a way `instanceof`
deliberately is not.

Every class has exactly **one** `Class` object, loaded once:

```java
"a".getClass() == "b".getClass()      // true — the same Class object
```

`getClass()` is the entry point to reflection (lesson 70), and the difference between it
and `instanceof` matters when writing `equals` (lesson 33).

---

## 6. `clone()` — and why to avoid it

```java
class Point implements Cloneable {
    @Override
    public Point clone() {
        try {
            return (Point) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);      // cannot happen: we are Cloneable
        }
    }
}
```

Look at how much is wrong with that:

1. **`clone()` is `protected` on `Object`**, so you must override it just to make it
   callable.
2. **`Cloneable` is a marker interface with no `clone()` method.** It does not give you
   the method — it only stops `Object.clone()` from throwing. That is a genuinely strange
   design.
3. **`CloneNotSupportedException` is checked**, and cannot actually happen once you
   implement `Cloneable`, so every implementation has a pointless `catch`.
4. **`Object.clone()` is shallow.** Mutable fields are shared between the original and the
   copy, so you must deep-copy them by hand.
5. **It bypasses constructors**, so any validation or invariant your constructor enforces
   is simply skipped.
6. **`final` fields cannot be reassigned** in `clone()`, which makes cloning an immutable
   object with computed state awkward.

### Use a copy constructor or static factory instead

```java
Point(Point other) {                      // copy constructor
    this(other.x, other.y);
}

static Point copyOf(Point other) {        // copy factory
    return new Point(other.x, other.y);
}
```

These have none of those problems: they run constructors, they can be `final`-friendly,
they have a clear signature, and they can deep-copy explicitly. *Effective Java* is
unambiguous — avoid `clone()`.

You will still meet it: arrays have a working `clone()` (lesson 12), and some old APIs use
it.

---

## 7. `finalize()` — never use it

```java
@Override
protected void finalize() { }      // deprecated for removal since Java 9
```

The idea was a destructor that ran before garbage collection. The reality:

- **No guarantee it ever runs.** If the JVM exits first, it does not.
- **No guarantee of when.** It could be minutes later, or never.
- It **delays collection** — a finalizable object survives at least one extra GC cycle.
- It can **resurrect** the object by storing `this` somewhere.
- An exception thrown inside it is **silently swallowed**.
- It has caused real **security vulnerabilities** (finalizer attacks on partially
  constructed objects).

Deprecated in Java 9, and being removed. Use **try-with-resources** and `AutoCloseable`
(lesson 41) for deterministic cleanup, or `java.lang.ref.Cleaner` for the rare case where
you need a safety net for a native resource.

---

## 8. `wait()`, `notify()`, `notifyAll()`

These are `final` methods on `Object` for thread coordination:

```java
synchronized (lock) {
    while (!condition) {
        lock.wait();            // release the lock and sleep
    }
}

synchronized (lock) {
    condition = true;
    lock.notifyAll();           // wake the waiters
}
```

Two rules you must know even if you never use them:

- They **must** be called while holding that object's monitor (inside `synchronized` on
  the same object), or you get `IllegalMonitorStateException`.
- `wait()` **must** be called in a loop, never an `if`, because of *spurious wakeups* —
  a thread can wake without being notified.

They are on `Object` because in Java's original design **every object is a lock**. Modern
code should use `java.util.concurrent` — `BlockingQueue`, `CountDownLatch`, `Condition` —
which are safer and clearer. Lesson 63.

---

## 9. Which to override, in practice

For a class you write:

```java
class Money {
    private final long amount;
    private final String currency;

    @Override public String toString() { ... }      // yes — always
    @Override public boolean equals(Object o) { ... } // yes — value type
    @Override public int hashCode() { ... }           // yes — with equals
    // clone()    — no. Copy constructor instead.
    // finalize() — never.
}
```

A **record** generates `toString`, `equals` and `hashCode` for you, correctly. For pure
data carriers that is a strong argument for records over hand-written classes (lesson 36).

---

## 10. Summary

- Every class extends `Object`; only the eight primitives do not.
- Of the eleven inherited methods, **five are `final`** (`getClass`, `wait` × 3, `notify`,
  `notifyAll`) and cannot be overridden.
- **Always override `toString()`.** Keep it short, never include secrets, never let it
  throw.
- Override `equals` and `hashCode` **together**, or not at all (lesson 33).
- `getClass()` always reports the **runtime** type and is the entry point to reflection.
- **Avoid `clone()`**: it is `protected`, `Cloneable` has no `clone` method, the checked
  exception cannot happen, the copy is shallow, and it bypasses constructors. Use a **copy
  constructor**.
- **Never use `finalize()`** — deprecated for removal, unpredictable, and a security
  hazard. Use try-with-resources.
- `wait`/`notify` require the object's monitor and must be used in a loop. Prefer
  `java.util.concurrent`.
- **Records** generate `toString`, `equals` and `hashCode` correctly, for free.

---

**Previous:** [31 — The `final` keyword](31-final-keyword.md) ·
**Next:** [33 — `equals()` and `hashCode()`](33-equals-and-hashcode.md)
