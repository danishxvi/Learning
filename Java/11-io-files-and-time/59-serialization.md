# 59 · Serialization

> **Run the code for this lesson**
> ```bash
> java Java/11-io-files-and-time/59-serialization.java
> ```

Every exception in this lesson except one was reproduced live inside the file. The
exception is `serialVersionUID` mismatch — it genuinely needs two different compiled
versions of the same class, so Section 4 presents a real, separately-captured error
instead of a fabricated one.

---

## 1. `Serializable` is a marker interface — no methods at all

```java
class Point implements Serializable {
    final int x; final int y;
}
```

```java
try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
    out.writeObject(new Point(3, 4));
}
try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
    Point restored = (Point) in.readObject();
}
```

Real result: a genuine 42-byte file on disk, and a restored `Point(3, 4)` that
`.equals()` the original but is a **different object** (`original == restored` is
`false`) — deserialization builds a fresh instance from the bytes.

`Serializable` declares nothing — it exists purely so `ObjectOutputStream` can check "is
this class opted in to being turned into bytes" before it starts walking the object's
fields via reflection. A class without it is refused outright.

---

## 2. A field referencing a non-`Serializable` class: real failure

```java
class Meeting implements Serializable {
    String title;
    Thread convener;   // Thread is genuinely NOT Serializable
}
```

```java
out.writeObject(new Meeting("Standup", Thread.currentThread()));
```

Real result: `NotSerializableException: java.lang.Thread`.

**The rule**: every field's class, transitively, must also be `Serializable` (or `null`
at write time, or marked `transient` — Section 3). The check happens depth-first at write
time, and the exception message names the exact class that broke the chain.

---

## 3. `transient`: a field that does not travel

```java
class UserAccount implements Serializable {
    String username;
    transient String password;
}
```

Real, measured round trip:

```
before serializing   -> username="alice", password="super-secret-password"
after the round trip -> username="alice", password=null
```

`password` comes back as `null`, regardless of what it held before. `transient` fields
are the standard way to keep genuinely sensitive or non-reconstructable state (passwords,
open handles, caches) out of the serialized bytes entirely.

---

## 4. `serialVersionUID`: a real, separately-captured `InvalidClassException`

This one couldn't be reproduced inside a single running file — it genuinely needs two
different compiled versions of the same class. It was reproduced for real, separately:

1. Compile `PersonV1` (`String name, int age`), serialize an instance to `person.ser`.
2. Add a third field (`String email`) to `PersonV1`, recompile — this changes the class
   shape.
3. Try to deserialize the *original* `person.ser` bytes with the *new* class definition.

Real, captured result:

```
InvalidClassException: PersonV1; local class incompatible:
stream classdesc serialVersionUID = 5932621617977229989,
local class serialVersionUID = -3141651860587056291
```

**Why**: with no explicit `serialVersionUID` field, the JVM computes one from the class's
shape (field names, types, method signatures...) at compile time. Adding a field changed
that computed value, so the old bytes (tagged with the old UID) no longer match the new
class (expecting a different UID) — and deserialization refuses outright rather than
guess at compatibility.

**The fix** real code uses:

```java
private static final long serialVersionUID = 1L;
```

...bumped only *deliberately*, on a genuine format break — this decouples version
compatibility from incidental field additions.

---

## 5. A shared object stays shared after deserialization

```java
Employee shared = new Employee("Shared Lead");
team.members.add(shared);
team.members.add(shared);   // the SAME object, added TWICE
```

Real, measured result:

```
members.get(0) == members.get(1) BEFORE serializing -> true
members.get(0) == members.get(1) AFTER deserializing -> true
```

Still the same object, on the *other side* of a real byte stream. `ObjectOutputStream`
tracks every object it has already written by identity — writing the same reference
twice writes a handle the second time, not a duplicate copy. This is what lets
serialization correctly round-trip an object **graph** (including cycles) rather than
just a tree, without infinite-looping or duplicating shared nodes.

---

## 6. Never deserialize untrusted data

`readObject()` does not just rebuild innocent data — it invokes real constructors and
methods (`readObject`/`readResolve` overrides, finalizers) *during* reconstruction,
driven entirely by bytes an attacker may control. Real, documented, historical CVEs (the
Apache Commons Collections "gadget chain" being the most famous) chained ordinary,
individually-harmless `Serializable` classes already present on the classpath into remote
code execution, purely by crafting the input bytes — no bug in any single class was
required, just a chain of legitimate side effects triggered in an attacker-chosen order.

**The rule, with no real exception**: never call `readObject()` on data from a source you
do not fully trust (network input, uploaded files, ...). For data crossing any trust
boundary, use a format with no code-execution capability at all — JSON, Protocol Buffers,
or similar — which is exactly why most real network APIs use those instead of Java's
native serialization, not merely for cross-language portability.

---

## 7. Summary

- `Serializable` is a marker interface with no methods — it only signals opt-in;
  `ObjectOutputStream` does the actual work via reflection.
- Every field's class must transitively be `Serializable` too, or the write fails with a
  real, precisely-targeted `NotSerializableException`.
- `transient` fields are silently excluded and come back as their type's default value
  (`null`, `0`, `false`) after deserialization — the standard tool for sensitive or
  non-reconstructable state.
- `serialVersionUID`, left implicit, is computed from the class's shape and changes
  whenever that shape changes — this is what produced a real `InvalidClassException`
  when a field was added and the class recompiled. Declare it explicitly and bump it only
  deliberately to decouple compatibility from incidental changes.
- Shared references are preserved by identity across a serialization round trip, verified
  here with `==` before and after — this is what makes correctly round-tripping an object
  graph (not just a tree) possible.
- Deserializing untrusted data is a real, historically-exploited security risk — never
  do it; use a non-code-executing format like JSON for anything crossing a trust
  boundary.

---

**Previous:** [58 — NIO.2: `Path`, `Files` and directory walking](58-nio-files-and-paths.md) ·
**Next:** [60 — The `java.time` date and time API](60-date-and-time-api.md)
