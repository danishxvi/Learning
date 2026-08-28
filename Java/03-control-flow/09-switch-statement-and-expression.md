# 09 · `switch` Statements and `switch` Expressions

> **Run the code for this lesson**
> ```bash
> java Java/03-control-flow/09-switch-statement-and-expression.java
> ```

`switch` has changed more than any other part of Java syntax. There are now **two
different constructs** sharing one keyword, with different rules. Knowing which one you
are writing is the whole lesson.

---

## 1. The classic `switch` statement

```java
switch (day) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    default:
        System.out.println("Unknown");
}
```

Rules:

- Each `case` label must be a **compile-time constant** — a literal, a `final` variable
  initialised with a constant, or an enum constant. Not a variable, not a method call,
  not a range.
- `break` exits the switch. **Without it, execution falls through** to the next case.
- `default` handles everything unmatched. It is optional and may appear anywhere
  (conventionally last).

### What `switch` accepts

| Type | Since |
| --- | --- |
| `byte`, `short`, `char`, `int` | Java 1.0 |
| Their wrappers (`Byte`, `Short`, `Character`, `Integer`) | Java 5 |
| `enum` | Java 5 |
| `String` | Java 7 |
| Any object, via patterns | Java 21 |

**`long`, `float`, `double` and `boolean` are never allowed.** `long` is excluded for
historical bytecode reasons; floating point because equality on it is unreliable;
`boolean` because `if`/`else` already covers two cases.

`switch` on a `String` uses `.equals()` internally (via `hashCode()` then `equals()`), so
it compares content correctly — but it throws `NullPointerException` on a `null`
selector. Guard it.

---

## 2. Fall-through — the feature that is usually a bug

```java
switch (grade) {
    case 'A':
        System.out.println("Excellent");
        // no break!
    case 'B':
        System.out.println("Good");
        break;
}
```

With `grade == 'A'`, this prints **both** lines. Execution enters at the matching label
and continues until a `break`, a `return`, or the end of the switch.

This causes so many bugs that C# made `break` mandatory. Java kept it for one genuinely
useful case: **grouping labels**.

```java
switch (month) {
    case 1: case 3: case 5: case 7: case 8: case 10: case 12:
        days = 31;
        break;
    case 4: case 6: case 9: case 11:
        days = 30;
        break;
    case 2:
        days = isLeapYear ? 29 : 28;
        break;
}
```

That is the legitimate use. Any *other* fall-through should carry an explicit
`// falls through` comment, because the next reader will assume you forgot the `break`.

---

## 3. Arrow labels (Java 14+) — fall-through eliminated

```java
switch (day) {
    case 1 -> System.out.println("Monday");
    case 2 -> System.out.println("Tuesday");
    case 6, 7 -> System.out.println("Weekend");
    default -> System.out.println("Unknown");
}
```

- `->` instead of `:`.
- **No `break` needed, ever** — only the matching branch runs.
- Multiple labels are comma-separated: `case 6, 7 ->`.
- For more than one statement, use a block: `case 1 -> { ...; ... }`.

**Use arrow form for all new code.** It removes an entire bug class at zero cost.

---

## 4. `switch` expressions (Java 14+)

This is the bigger change: `switch` can now **produce a value**.

```java
String name = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 6, 7 -> "Weekend";
    default -> "Unknown";
};      // <-- note the semicolon: it is an expression, so this is a statement
```

Compare with the old way, which needed a mutable variable and four times the lines:

```java
String name;
switch (day) {
    case 1: name = "Monday"; break;
    case 2: name = "Tuesday"; break;
    default: name = "Unknown";
}
```

The expression form lets `name` be `final`, cannot fall through, and cannot forget a case.

### Exhaustiveness — the compiler now checks

A switch **expression** must cover every possible value. If it does not, it is a compile
error:

```java
int code = switch (status) {      // ERROR if some enum constant is unhandled
    case ACTIVE -> 1;
    case INACTIVE -> 2;
    // missing SUSPENDED
};
```

This is a genuine feature: add a new enum constant, and the compiler shows you every
switch that needs updating. A `switch` *statement* gives you no such help — it silently
does nothing.

> **Tip:** for an enum, prefer omitting `default` in a switch expression. Then adding a
> constant becomes a compile error rather than a silent fall to `default`.

### `yield` — returning a value from a block

When a branch needs multiple statements, use a block and `yield`:

```java
int result = switch (operation) {
    case "add" -> a + b;
    case "divide" -> {
        if (b == 0) {
            throw new ArithmeticException("divide by zero");
        }
        yield a / b;                 // yield, NOT return
    }
    default -> throw new IllegalArgumentException(operation);
};
```

`return` inside a switch expression is a **compile error** — `return` exits the enclosing
*method*, which is not what a value-producing expression means. `yield` supplies the
value of the switch.

You can also use colon labels with `yield`, though mixing styles in one switch is not
allowed:

```java
int x = switch (n) {
    case 1: yield 10;
    default: yield 0;
};
```

---

## 5. Pattern matching in `switch` (Java 21)

`switch` can now match on **type**, not just value:

```java
String describe(Object o) {
    return switch (o) {
        case null        -> "nothing";
        case Integer i   -> "int " + i;
        case String s    -> "string of length " + s.length();
        case int[] arr   -> "int array of " + arr.length;
        default          -> "something else";
    };
}
```

This replaces long `if`/`else if` chains of `instanceof`.

### Guarded patterns — `when`

```java
String size = switch (n) {
    case Integer i when i < 0   -> "negative";
    case Integer i when i == 0  -> "zero";
    case Integer i when i < 100 -> "small";
    case Integer i              -> "large";
    default                     -> "not a number";
};
```

The `when` clause adds a boolean condition to a pattern. Order matters: the first
matching case wins, so the most specific guards must come first.

### `null` handling changed

Historically `switch` threw `NullPointerException` on a `null` selector. Since Java 21 you
may write `case null ->` explicitly. If you do not, the NPE behaviour is preserved for
compatibility.

```java
case null, default -> "nothing or unknown";   // combine them
```

### Record patterns (Java 21)

Patterns can destructure records directly:

```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}

double area = switch (shape) {
    case Circle(double r)             -> Math.PI * r * r;
    case Rectangle(double w, double h) -> w * h;
};
```

No `default` is needed: `Shape` is `sealed`, so the compiler knows the list is complete.
Lessons 36 and 37 cover records and sealed types properly.

---

## 6. `switch` vs `if`/`else if`

| Use `switch` when | Use `if`/`else if` when |
| --- | --- |
| Testing one variable against many constants | Testing ranges or unrelated conditions |
| The values are enums, strings or small ints | The conditions involve `&&` / `\|\|` |
| You want exhaustiveness checking | You have only two or three branches |

`switch` on many `int` cases can also be **faster**: the compiler may emit a
`tableswitch` bytecode, which is an O(1) jump table rather than a chain of comparisons.
For a handful of cases the difference is irrelevant — choose on readability.

You cannot switch on ranges:

```java
switch (score) {
    case 90..100 -> "A";     // NOT valid Java
}
```

Use `if`/`else if` for ranges, or guarded patterns:

```java
String grade = switch (score / 10) {
    case 10, 9 -> "A";
    case 8     -> "B";
    default    -> "F";
};
```

---

## 7. Summary

- Case labels must be **compile-time constants**. `long`, `float`, `double` and `boolean`
  are never allowed as selectors.
- Colon labels **fall through** without `break`. Legitimate only for grouping labels;
  otherwise mark it `// falls through`.
- **Arrow labels (`->`) never fall through.** Use them for all new code.
- A `switch` **expression** produces a value, must be **exhaustive**, and ends with a
  semicolon.
- Use `yield` — not `return` — to produce a value from a block inside a switch expression.
- Omitting `default` on an enum switch expression turns a future missing case into a
  compile error. That is a feature, not an oversight.
- Java 21 adds **type patterns**, `when` guards, `case null`, and record destructuring.
- `switch` on `String` uses `.equals()` but throws on a `null` selector unless you write
  `case null`.

---

**Previous:** [08 — if/else and the ternary](08-if-else-and-ternary.md) ·
**Next:** [10 — Loops](10-loops.md)
