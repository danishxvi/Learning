# 08 · `if` / `else` and the Ternary Operator

> **Run the code for this lesson**
> ```bash
> java Java/03-control-flow/08-if-else-and-ternary.java
> ```

Conditionals are the first thing everyone learns and the last thing anyone masters. The
syntax takes five minutes. This lesson spends its time on the mistakes that survive into
production: dangling braces, `==` on objects, unreachable branches, and conditions that
are technically correct but unreadable.

---

## 1. The forms

```java
// 1. if alone
if (condition) {
    // runs when condition is true
}

// 2. if / else
if (condition) {
    // true branch
} else {
    // false branch
}

// 3. if / else if / else — a chain
if (score >= 90) {
    grade = "A";
} else if (score >= 80) {
    grade = "B";
} else {
    grade = "F";
}
```

### The condition must be a `boolean`

This is a hard rule and a real advantage over C, JavaScript and Python:

```java
if (1) { }           // ERROR: int cannot be converted to boolean
if ("hello") { }     // ERROR
if (list) { }        // ERROR — no "truthiness" in Java
```

There is **no truthiness in Java**. You must state the comparison explicitly:

```java
if (count != 0) { }
if (!text.isEmpty()) { }
if (list != null && !list.isEmpty()) { }
```

This verbosity is deliberate. It eliminates an entire family of bugs that plague
truthy/falsy languages, where `0`, `""`, `[]` and `null` all behave differently and
inconsistently between languages.

### The one accidental-assignment case Java still allows

C programmers know the classic typo `if (x = 5)`. In Java that is a compile error for
`int`, because `5` is not a `boolean`. But with **booleans** it still compiles:

```java
boolean isReady = false;
if (isReady = true) {     // ASSIGNS true, then tests it. Always true!
    // always runs
}
```

Two defences: always compare with `==` deliberately, and never write `== true` at all —
`if (isReady)` is both shorter and immune.

---

## 2. Braces — the dangling-statement trap

Braces are optional for a single statement:

```java
if (x > 0)
    System.out.println("positive");
```

This is legal, and it is how the **goto fail** bug shipped in Apple's TLS stack in 2014:

```java
if (x > 0)
    System.out.println("positive");
    System.out.println("this ALWAYS runs");   // not part of the if!
```

The indentation lies. Only the *first* statement belongs to the `if`.

> **Rule: always use braces, even for one line.** Every serious style guide (Google,
> Oracle, Airbnb) mandates this. The two characters cost nothing; the bug costs a day.

### The empty-statement trap

```java
if (x > 0);              // note the semicolon
{
    System.out.println("always runs");
}
```

The `;` is a complete empty statement, so the `if` does nothing and the block always
executes. Compilers do not warn about this by default.

---

## 3. Dangling `else`

```java
if (a)
    if (b)
        doX();
else
    doY();          // which if does this belong to?
```

**Rule: `else` binds to the nearest unmatched `if`.** So `doY()` runs when `a` is true and
`b` is false — *not* when `a` is false, despite the indentation. Braces make the intent
unambiguous and are the only real fix.

---

## 4. Comparing correctly

### Primitives — `==` is right

```java
if (age == 18) { }
if (price > 100.0) { }
```

But **never `==` on floating point**, for the reasons in lesson 03:

```java
if (0.1 + 0.2 == 0.3) { }                       // false!
if (Math.abs(a - b) < 1e-9) { }                 // correct
```

### Objects — `.equals()` is right

```java
String input = scanner.nextLine();
if (input == "yes") { }          // almost always FALSE
if (input.equals("yes")) { }     // correct
```

Input read at runtime is not an interned literal, so `==` compares two different objects.
This bug is subtle because it *works* when you test with literals and fails with real
input.

### Null-safe comparison — Yoda conditions

```java
if (input.equals("yes")) { }     // NullPointerException if input is null
if ("yes".equals(input)) { }     // safe — a literal is never null
```

Putting the constant first is called a **Yoda condition**. It looks odd and it is the
standard defensive idiom. Alternatively:

```java
if (Objects.equals(input, "yes")) { }   // null-safe both ways
```

`Objects.equals(null, null)` is `true`, which is usually what you want.

---

## 5. Structuring conditions well

### Guard clauses beat nesting

Deeply nested conditionals — the "arrow anti-pattern" — are hard to follow:

```java
// BAD
if (user != null) {
    if (user.isActive()) {
        if (user.hasPermission("read")) {
            return user.getData();
        } else {
            throw new AccessDeniedException();
        }
    } else {
        throw new InactiveUserException();
    }
} else {
    throw new IllegalArgumentException("user is null");
}
```

Invert each test and return (or throw) immediately:

```java
// GOOD
if (user == null)                     throw new IllegalArgumentException("user is null");
if (!user.isActive())                 throw new InactiveUserException();
if (!user.hasPermission("read"))      throw new AccessDeniedException();

return user.getData();
```

Same logic, one level of indentation, and the happy path is the last line rather than
buried in the middle. This is called the **guard clause** or **early return** pattern, and
it is one of the highest-value habits you can build.

### Name complex conditions

```java
// BAD
if (age >= 18 && country.equals("IN") && !suspended && balance > 0) { }

// GOOD
boolean isAdult = age >= 18;
boolean isEligibleRegion = country.equals("IN");
boolean isAccountUsable = !suspended && balance > 0;

if (isAdult && isEligibleRegion && isAccountUsable) { }
```

A boolean variable with a good name is a comment that the compiler checks.

### Order conditions by cost

Because `&&` short-circuits, put the cheap, most-likely-to-fail test first:

```java
if (cache.containsKey(id) && database.isReachable() && validate(id)) { }
//  cheap, usually false     expensive             expensive
```

---

## 6. The ternary operator, revisited

```java
result = condition ? valueIfTrue : valueIfFalse;
```

The key distinction from `if`:

| | `if` | Ternary |
| --- | --- | --- |
| Is a… | **Statement** — does something | **Expression** — has a value |
| Can be assigned | No | Yes |
| Can hold statements | Yes | No — only expressions |
| Best for | Branching *behaviour* | Choosing a *value* |

```java
int max = (a > b) ? a : b;                      // good — choosing a value
String plural = (n == 1) ? "item" : "items";    // good

// Bad — the ternary is being used for side effects
value = (x > 0) ? doThing() : doOtherThing();   // use an if
```

### Both branches must have compatible types

```java
Object o = flag ? "text" : 42;      // legal — common type is Object
int i = flag ? "text" : 42;         // ERROR
```

And the unboxing trap from lesson 05 is worth repeating, because it produces a
`NullPointerException` from code that contains no visible dereference:

```java
Integer result = flag ? 1 : nullInteger();   // NPE when flag is false
```

The `int` literal `1` forces the whole expression to type `int`, which forces the `null`
branch to be unboxed. Keep both branches the same type.

---

## 7. Scope inside conditionals

A variable declared inside a block dies with that block:

```java
if (condition) {
    int temp = 10;
}
System.out.println(temp);      // ERROR: cannot find symbol
```

Declare it outside if you need it afterwards:

```java
int temp = 0;
if (condition) {
    temp = 10;
}
System.out.println(temp);      // fine
```

Pattern-matching `instanceof` (Java 16+) introduces a variable with **flow scope** — it
exists exactly where the compiler can prove it is valid:

```java
if (!(o instanceof String s)) {
    return;                    // s is NOT in scope here
}
System.out.println(s.length()); // s IS in scope here — the compiler reasoned it out
```

---

## 8. Summary

- The condition must be a `boolean`. Java has **no truthiness** — state the comparison.
- `if (flag = true)` compiles and is always true. Write `if (flag)`.
- **Always use braces.** The dangling-statement trap shipped a real TLS vulnerability.
- Watch for the stray semicolon: `if (x);` is a complete, empty `if`.
- `else` binds to the nearest unmatched `if`, regardless of indentation.
- `==` for primitives, `.equals()` for objects, tolerance comparison for floating point.
- Put the literal first (`"yes".equals(input)`) or use `Objects.equals` to survive `null`.
- Prefer **guard clauses** to nesting; name complex conditions; order `&&` operands
  cheapest-first.
- The ternary is an *expression* — use it to choose a value, never to run behaviour, and
  keep both branches the same type.

---

**Previous:** [07 — Output and formatting](../02-operators-and-input/07-output-and-string-formatting.md) ·
**Next:** [09 — switch statements and expressions](09-switch-statement-and-expression.md)
