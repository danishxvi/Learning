# 56 · `Optional`

> **Run the code for this lesson**
> ```bash
> java Java/10-functional-java/56-optional.java
> ```

The last lesson of Section 10. It catches the single most common `Optional` performance
mistake — `orElse`'s argument is *always* evaluated, even when the `Optional` is present —
and shows why `Optional` as a field defeats its own purpose.

---

## 1. Creating an `Optional`, three ways

```java
Optional.of("hello")     // Optional[hello]
Optional.empty()          // Optional.empty
```

**The real difference**: `Optional.of` rejects `null` immediately.

```java
Optional.of(null);          // NullPointerException, RIGHT HERE - fail fast
Optional.ofNullable(null);  // Optional.empty - safe
```

Use `Optional.ofNullable` whenever the value genuinely might be `null` — most commonly,
wrapping the result of a `Map.get(key)` on a possibly-missing key:

```java
String value = config.get("port");   // null if "port" isn't a key
Optional.of(value);           // would THROW if value is null
Optional.ofNullable(value);   // always safe
```

---

## 2. `.get()` on an empty `Optional`: a real exception

```java
present.isPresent() / present.isEmpty()   // true / false
empty.get();                               // NoSuchElementException: "No value present"
```

`if (opt.isPresent()) { opt.get() }` works, but it's exactly the if-null-check pattern
`Optional` exists to get code *away* from. The idioms below are the actual point of this
type.

---

## 3. `orElse`, `orElseThrow`, `ifPresent`, `ifPresentOrElse`

```java
present.orElse("default");    // "hello" (present's own value)
empty.orElse("default");      // "default"

empty.orElseThrow(() -> new IllegalStateException("config value required"));
// throws the custom exception

present.ifPresent(v -> ...);                       // runs the consumer with the value
empty.ifPresentOrElse(v -> ..., () -> ...);          // runs the SECOND (empty) branch
```

---

## 4. `orElse` vs `orElseGet`: the real, measurable difference

A helper method that just prints when it actually runs:

```java
static String computeExpensiveDefault() {
    System.out.println("*** computeExpensiveDefault() ACTUALLY RAN ***");
    return "default";
}
```

```java
present.orElse(computeExpensiveDefault());              // "default" IS COMPUTED, even though unused
present.orElseGet(OptionalLesson::computeExpensiveDefault);   // NOT computed at all
```

Real, measured proof: with `present` already holding a value, `orElse`'s call printed
`*** computeExpensiveDefault() ACTUALLY RAN ***` — the method genuinely ran. The identical
situation with `orElseGet` produced **no such print at all**.

**Why**: `orElse`'s argument is an already-evaluated *value* — Java evaluates method
arguments before the call, unconditionally, the same as any other method call.
`orElseGet` takes a *supplier*, which is only invoked if actually needed — genuinely lazy.
For a cheap literal default this difference is invisible. For an expensive default (a
database call, a network request, a heavy computation), `orElse` pays that cost **every
time**, even when the `Optional` was already present and the default was never needed.
Always prefer `orElseGet` for anything beyond a trivial constant.

---

## 5. `map`/`flatMap`/`filter`: functional chaining

```java
Optional.of("alice").map(String::length);          // Optional[5]

Optional.of("alice").filter(n -> n.length() > 3).map(String::toUpperCase);
// Optional[ALICE]

Optional.of("al").filter(n -> n.length() > 3).map(String::toUpperCase);
// Optional.empty - the filter failed, so map never even ran
```

`map()` on an empty `Optional` short-circuits safely — no null check needed anywhere in
the chain:

```java
Optional.<String>empty().map(String::length);   // Optional.empty
```

`flatMap` chains through a nested `Optional`-returning field, with no nested `if`:

```java
record Address(String city) {
    Optional<String> cityOptional() { return Optional.ofNullable(city); }
}
record Person(Optional<Address> address) {}

personWithAddress.address().flatMap(Address::cityOptional);      // Optional[Springfield]
personWithoutAddress.address().flatMap(Address::cityOptional);   // Optional.empty
```

---

## 6. `Optional` is for return types — not fields, not parameters

`Optional`'s entire point is: a method's **return type** tells callers "this might not
have a value" without them needing to remember to null-check. That guarantee lives in the
type signature at the *call site*.

Using `Optional<T>` as a **field** does not extend that guarantee — it just moves the
same null-check problem one level, because the `Optional` reference itself can still be
`null`:

```java
class BrokenPerson {
    Optional<Address> address;   // defaults to null, like ANY other reference field
}

BrokenPerson p = new BrokenPerson();
p.address.isPresent();   // NullPointerException!
```

Real, reproduced result: a genuine `NullPointerException` — the `Optional<Address>`
*field itself* was `null`. `Optional` promised nothing here, because nothing forced it to
be set; declaring a field `Optional<T>` doesn't stop the field from simply never being
assigned.

`Optional` is also not `Serializable` and carries real per-instance allocation cost — the
JDK's own design intent (stated directly by Brian Goetz, its architect) is **return types
only**. For a field that might be absent, use `null` with real discipline, or a proper
default value — not `Optional`.

---

## 7. Summary

- `Optional.of` throws immediately on `null`; `Optional.ofNullable` is the safe wrapper
  for a value that might genuinely be absent (like a `Map.get` result).
- `.get()` on an empty `Optional` throws a real `NoSuchElementException` — prefer
  `orElse`/`orElseGet`/`orElseThrow`/`map`/`ifPresent` over manual `isPresent()` checks.
- `orElse`'s argument is evaluated unconditionally, even when unused — measured here with
  a method that visibly ran under `orElse` and did not run at all under `orElseGet`.
  Always prefer `orElseGet` for anything beyond a cheap constant.
- `map`/`filter`/`flatMap` let an `Optional` chain be built without any explicit null or
  presence checks anywhere in the chain — an empty or filtered-out `Optional` just flows
  through as empty.
- `Optional` is designed for **method return types**, not fields or parameters — its
  guarantee lives entirely in the type signature at the call site. As a field, the
  `Optional` reference itself can be `null`, reproducing exactly the problem it was meant
  to solve, confirmed here with a genuine `NullPointerException`.

---

**Previous:** [55 — Collectors, grouping and partitioning](55-collectors-and-grouping.md) ·
**Next:** [57 — File I/O with streams and readers](../11-io-files-and-time/57-file-io-basics.md)
