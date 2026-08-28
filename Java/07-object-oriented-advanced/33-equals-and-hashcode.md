# 33 · `equals()` and `hashCode()` — The Contract

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/33-equals-and-hashcode.java
> ```

Getting these two wrong does not produce a compile error or an exception. It produces
**collections that silently lose your data**. That is why this has its own lesson.

---

## 1. The default behaviour

```java
class Point { int x, y; }

Point a = new Point(1, 2);
Point b = new Point(1, 2);

a.equals(b);      // false — Object.equals is `this == other`
a.hashCode();     // some identity-derived number
b.hashCode();     // a DIFFERENT number
```

`Object.equals` is **identity**. For objects that represent a *value* — money, a point, a
date, an ID — that is almost never what you want.

---

## 2. The `equals` contract

For any non-null references `x`, `y`, `z`:

| Property | Requirement |
| --- | --- |
| **Reflexive** | `x.equals(x)` is `true` |
| **Symmetric** | `x.equals(y)` ⟺ `y.equals(x)` |
| **Transitive** | If `x.equals(y)` and `y.equals(z)`, then `x.equals(z)` |
| **Consistent** | Repeated calls give the same answer, if nothing changed |
| **Non-null** | `x.equals(null)` is `false` — never throws |

These are not academic. Collections rely on every one of them, and breaking any produces
behaviour that looks like a JDK bug.

---

## 3. The `hashCode` contract

| Rule | Requirement |
| --- | --- |
| **1** | If `a.equals(b)`, then `a.hashCode() == b.hashCode()` — **mandatory** |
| **2** | If `a.hashCode() == b.hashCode()`, they *need not* be equal — collisions are fine |
| **3** | Repeated calls give the same value, if nothing changed |

> **Rule 1 is the one that breaks things.** Equal objects **must** have equal hash codes.
> The reverse is not required.

### Why rule 1 matters

`HashMap` and `HashSet` find an entry in two steps:

1. Compute `hashCode()` to pick a **bucket**.
2. Search that bucket using `equals()`.

If two equal objects have different hash codes, they land in **different buckets** — and
step 2 never happens. The map reports the key as absent even though an equal key is
sitting inside it.

```java
Map<Point, String> map = new HashMap<>();
map.put(new Point(1, 2), "origin-ish");
map.get(new Point(1, 2));      // null, if hashCode was not overridden
```

You will search for the bug in your own code for an hour before suspecting `hashCode`.

---

## 4. Writing `equals` correctly

```java
@Override
public boolean equals(Object other) {
    if (this == other) return true;                  // 1. identity fast path
    if (!(other instanceof Point point)) return false; // 2. type check + null check + cast
    return x == point.x && y == point.y;             // 3. compare significant fields
}
```

Three lines, and each does a specific job:

1. **Identity check** — a cheap early exit, and it also handles self-comparison correctly.
2. **`instanceof` with pattern matching** — handles `null` (returns `false`), checks the
   type, and binds the cast variable in one step.
3. **Field comparison** — compare only the fields that define logical identity.

### Comparing fields correctly

| Field type | Use |
| --- | --- |
| Primitives (except `float`/`double`) | `==` |
| `float` | `Float.compare(a, b) == 0` |
| `double` | `Double.compare(a, b) == 0` |
| Objects | `Objects.equals(a, b)` — null-safe |
| Arrays | `Arrays.equals(a, b)`, or `deepEquals` for nested |

`Float.compare` and `Double.compare` matter because of `NaN` and `±0.0`:

```java
Double.NaN == Double.NaN                  // false — breaks reflexivity!
Double.compare(NaN, NaN) == 0             // true  — correct for equals
0.0 == -0.0                               // true
Double.compare(0.0, -0.0) == 0            // false — they ARE distinguishable
```

Using `==` on a `double` field means an object containing `NaN` is not equal to *itself*,
which violates reflexivity and breaks collections.

---

## 5. Writing `hashCode` correctly

The easy way, and the right default:

```java
@Override
public int hashCode() {
    return Objects.hash(x, y);      // same fields as equals, in the same order
}
```

The manual way, if you are in a hot path and have measured a problem:

```java
@Override
public int hashCode() {
    int result = Integer.hashCode(x);
    result = 31 * result + Integer.hashCode(y);
    return result;
}
```

`Objects.hash` allocates a varargs array on every call (lesson 19), which is why the JDK's
own `hashCode` implementations are written manually. For application code, use
`Objects.hash` unless profiling says otherwise.

### Why 31?

It is odd and prime, `31 * i` optimises to `(i << 5) - i`, and it gives good distribution
for typical field values. There is nothing magical about it — but it is the convention, and
matching the convention is worth more than any alternative.

### The absolute rules

- Use **exactly the same fields** as `equals`, or rule 1 breaks.
- **Never** include a mutable field that could change while the object is a key (§7).
- Returning a constant (`return 1;`) is *legal* but turns every `HashMap` into a linked
  list — O(1) becomes O(n).

---

## 6. `instanceof` vs `getClass()` — the symmetry problem

```java
// Version A — instanceof
if (!(other instanceof Point)) return false;

// Version B — getClass
if (other == null || getClass() != other.getClass()) return false;
```

They differ when subclasses exist:

```java
class Point { int x, y; }
class ColorPoint extends Point { String color; }

Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, "red");

// With instanceof, if ColorPoint also compares color:
p.equals(cp);      // true  — Point sees only x and y
cp.equals(p);      // false — ColorPoint also wants color
// SYMMETRY BROKEN
```

`getClass()` fixes symmetry by declaring a `Point` and a `ColorPoint` never equal — but
then **a subclass can never be equal to its superclass**, which violates the Liskov
principle in spirit.

> **There is no way to add a value component to a subclass while preserving the `equals`
> contract.** This is a known, fundamental limitation, stated plainly in *Effective Java*.

### The practical resolutions

1. **Use composition instead of inheritance.** `ColorPoint` *has a* `Point`. This is the
   recommended fix.
2. **Use `getClass()`** and accept that subclasses are never equal.
3. **Make the class `final`**, so the problem cannot arise. This is why so many value
   classes in the JDK are `final`.
4. **Use a `record`**, which is implicitly final and generates a correct `equals`.

For a `final` class the two versions are equivalent, so use `instanceof` with pattern
matching — it is shorter and handles `null` for free.

---

## 7. The mutable-key disaster

```java
Set<Point> set = new HashSet<>();
Point p = new Point(1, 2);
set.add(p);

p.setX(99);                  // the object is now in the WRONG bucket

set.contains(p);             // false — even though it IS in the set!
set.remove(p);               // fails — you cannot remove it
```

The object was filed under its old hash and its hash has changed. It is now **unreachable
and unremovable** — a genuine memory leak with no way to clean it up.

**The rule: never use a mutable object as a `HashMap` key or a `HashSet` element, unless
you are certain the fields used by `hashCode` will never change.**

The clean answer is to make key types **immutable** (lesson 38) — or a `record`, which is
shallowly immutable by construction.

---

## 8. `equals` and `compareTo` consistency

If a class implements `Comparable`, its `compareTo` should be **consistent with `equals`**:

```java
x.compareTo(y) == 0   ⟺   x.equals(y)
```

Not required by the compiler, but sorted collections assume it:

```java
BigDecimal a = new BigDecimal("1.0");
BigDecimal b = new BigDecimal("1.00");

a.equals(b);           // false — different SCALE
a.compareTo(b) == 0;   // true  — numerically equal

Set<BigDecimal> hashSet = new HashSet<>(List.of(a, b));   // size 2 — uses equals
Set<BigDecimal> treeSet = new TreeSet<>(List.of(a, b));   // size 1 — uses compareTo
```

`BigDecimal` is the classic example of this inconsistency, and it is documented in its
Javadoc precisely because it surprises people. Lesson 49 covers comparators.

---

## 9. The shortcut: records

```java
record Point(int x, int y) { }
```

That single line generates a correct `equals`, a consistent `hashCode`, and a readable
`toString`. It is implicitly `final`, so the symmetry problem cannot arise, and its
components are `final`, so the mutable-key disaster cannot happen either.

**For value types, prefer a record.** Write `equals`/`hashCode` by hand only when you need
something a record cannot express — a subset of fields, case-insensitive comparison, or a
class that must extend something. Lesson 36.

---

## 10. Summary

- `Object.equals` is **identity**. Override it for value types.
- The `equals` contract: **reflexive, symmetric, transitive, consistent, and `false` for
  `null`**.
- The `hashCode` contract's key rule: **equal objects must have equal hash codes.** The
  reverse is not required.
- Violating that makes `HashMap`/`HashSet` look up the **wrong bucket**, so entries vanish.
- The `equals` template: identity check → `instanceof` pattern → compare significant fields.
- Use `Objects.equals` for object fields, `Double.compare`/`Float.compare` for floating
  point (because `NaN != NaN` breaks reflexivity), and `Arrays.equals` for arrays.
- `Objects.hash(...)` is the right default; the manual `31 * result + ...` form avoids a
  varargs allocation in hot paths.
- **You cannot add a value component to a subclass and keep the contract.** Use
  composition, `getClass()`, `final`, or a record.
- **Never use a mutable object as a hash key** — changing it makes the entry unreachable
  and unremovable.
- `compareTo` should be consistent with `equals`; `BigDecimal` famously is not.
- **Prefer records for value types** — they generate all three correctly.

---

**Previous:** [32 — The `Object` class](32-object-class-methods.md) ·
**Next:** [34 — Inner and anonymous classes](34-inner-and-anonymous-classes.md)
