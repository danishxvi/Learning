# 24 · Encapsulation

> **Run the code for this lesson**
> ```bash
> java Java/06-object-oriented-core/24-encapsulation.java
> ```

Encapsulation is usually taught as "make fields private and add getters and setters". That
is the *mechanism*, and stating it that way misses the point so completely that people
write `private` fields with a public setter for every one — which protects nothing at all.

The actual definition:

> **Encapsulation is bundling data with the code that operates on it, and controlling
> access so the object can guarantee its own correctness.**

The test is not "are the fields private?" It is **"can any caller put this object into an
invalid state?"**

---

## 1. The problem it solves

```java
class BankAccount {
    public double balance;      // no protection at all
}

account.balance = -5000;        // legal. The account is now broken.
account.balance += 1_000_000;   // also legal.
```

With a public field, **every line of code in the program is a place the invariant can
break**. Debugging means searching the whole codebase.

```java
class BankAccount {
    private double balance;

    public void withdraw(double amount) {
        if (amount <= 0)      throw new IllegalArgumentException("must be positive");
        if (amount > balance) throw new IllegalStateException("insufficient funds");
        balance -= amount;
    }
}
```

Now there are exactly **two** places the balance can change, both of which enforce the
rules. If the balance is ever wrong, the bug is in one of two methods.

That reduction — from "anywhere" to "here" — is the entire value of encapsulation.

---

## 2. Access modifiers

| Modifier | Same class | Same package | Subclass (other package) | Anywhere |
| --- | :---: | :---: | :---: | :---: |
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(none)* — package-private | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

**Default to `private`.** Widen only when you have a reason, and know what each widening
commits you to:

- `public` is a **permanent promise**. Once published, you cannot narrow it without
  breaking callers.
- `protected` is **also public** in practice — anyone can subclass your class and read it.
  It commits you to that field's existence forever.
- **Package-private** is underused and often the right answer for collaborating classes.

Lesson 30 covers packages and modifiers in full.

---

## 3. The getter/setter misunderstanding

```java
class Person {
    private String name;
    public String getName()            { return name; }
    public void setName(String name)   { this.name = name; }   // protects NOTHING
}
```

This is `private` in spelling only. A caller can still set `name` to `null`, `""`, or a
10 MB string. You have written six lines to achieve exactly what `public String name;`
achieves — and added the illusion of safety.

**A setter with no validation is a public field with extra steps.**

### What to do instead

**1. Ask whether the setter is needed at all.**

Most fields are set once at construction and never change. Make them `final` and delete
the setter:

```java
class Person {
    private final String name;
    Person(String name) { this.name = Objects.requireNonNull(name); }
    public String getName() { return name; }
}
```

**2. If it must change, validate:**

```java
public void setEmail(String email) {
    if (email == null || !email.contains("@")) {
        throw new IllegalArgumentException("invalid email: " + email);
    }
    this.email = email;
}
```

**3. Better: expose the *operation*, not the field.**

```java
// Anaemic — the caller does the thinking
account.setBalance(account.getBalance() - 100);

// Encapsulated — the object does the thinking
account.withdraw(100);
```

The second version can enforce rules, log, fire events, and be made thread-safe. The first
cannot, because by the time you call `setBalance` the decision has already been made
elsewhere.

This is the difference between an **anaemic domain model** (data holders plus separate
"service" classes containing all the logic) and a real object model. The anaemic style is
extremely common and is worth recognising.

---

## 4. The leaking-reference bug

This is the encapsulation failure people miss most often:

```java
class Team {
    private final List<String> members = new ArrayList<>();

    public List<String> getMembers() {
        return members;              // LEAK — the caller now has your list
    }
}

team.getMembers().clear();           // wipes the team's internal state
```

`private` protected the *field*, not the *object it points at*. Handing out the reference
gives away everything `private` was guarding.

### Three fixes

```java
// 1. Unmodifiable VIEW — cheap, but reflects later changes
return Collections.unmodifiableList(members);

// 2. Defensive COPY — fully independent snapshot
return new ArrayList<>(members);

// 3. Immutable COPY (Java 10+) — the usual best answer
return List.copyOf(members);
```

Note the difference between 1 and 2: an unmodifiable *view* still shows changes made to
the underlying list afterwards; a *copy* does not.

The same applies **on the way in**:

```java
Team(List<String> members) {
    this.members = new ArrayList<>(members);   // copy — caller cannot mutate ours later
}
```

And to any mutable object: `Date`, arrays, `StringBuilder`, and your own mutable classes.
Lesson 38 covers defensive copying in depth.

---

## 5. Encapsulating behaviour, not just data

Encapsulation hides **implementation decisions**, not only fields:

```java
class Temperature {
    private final double celsius;               // an internal decision

    public double getCelsius()    { return celsius; }
    public double getFahrenheit() { return celsius * 9 / 5 + 32; }
}
```

The class stores one number and exposes two readings. Callers cannot tell — and should not
care — which unit is stored. Switch the internal representation to Kelvin tomorrow and no
caller changes.

That freedom to change your mind later **is** what encapsulation buys.

---

## 6. Encapsulation, abstraction, information hiding

These three get used interchangeably. They are related but distinct:

| Term | Means | Example |
| --- | --- | --- |
| **Encapsulation** | Bundling data with its behaviour, controlling access | `private` field + validating method |
| **Information hiding** | Hiding *how* something is done | Storing Celsius, exposing both units |
| **Abstraction** | Exposing *what* something does, not how | A `List` interface with many implementations |

Encapsulation is the *mechanism*; information hiding is the *goal*; abstraction is the
*design principle*.

---

## 7. Practical rules

1. **Fields `private`, and `final` wherever possible.**
2. **No setter unless something genuinely needs to change it.**
3. **Validate in the constructor** — an object should never exist invalid.
4. **Never return an internal mutable object** — return a copy or an unmodifiable view.
5. **Copy mutable arguments on the way in** too.
6. **Expose operations, not fields.** `withdraw(100)`, not `setBalance(...)`.
7. **Keep the public surface small.** Every public member is a promise you must keep.
8. **`records` for pure data** (lesson 36) — they are immutable by construction, so much of
   this becomes automatic.

### When it is fine to relax

- A `private static final` constant can safely be `public static final` if it is
  **immutable** — `public static final int MAX = 100;` is fine, but
  `public static final List<String> NAMES = new ArrayList<>();` is a public mutable global.
- A `record` exposes its components by design. That is the point of a record.
- Package-private is genuinely appropriate for classes that collaborate closely.

---

## 8. Summary

- Encapsulation is **controlling access so the object guarantees its own correctness** —
  not "add getters and setters".
- The test is: **can any caller put this object into an invalid state?**
- A setter with no validation is a public field with extra steps.
- Prefer `final` fields set in a validating constructor, and delete the setter entirely.
- **Expose operations, not fields.** `withdraw(100)` beats
  `setBalance(getBalance() - 100)`.
- Returning an internal mutable object (list, array, `Date`) **leaks** everything `private`
  was protecting. Return `List.copyOf(...)` or an unmodifiable view — and copy mutable
  arguments on the way in.
- Encapsulation also hides *implementation decisions*, which is what lets you change them.
- Keep the public surface small: every `public` member is a permanent promise.

---

**Previous:** [23 — The `this` keyword](23-this-keyword.md) ·
**Next:** [25 — `static` members](25-static-members.md)
