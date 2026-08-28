# 05 · Operators, Precedence and the Traps

> **Run the code for this lesson**
> ```bash
> java Java/02-operators-and-input/05-operators.java
> ```

Java has around 40 operators. Most are obvious. This lesson covers all of them, then
spends its time on the handful that produce genuinely surprising results.

---

## 1. The complete operator table

### Arithmetic

| Operator | Meaning | Example | Result |
| --- | --- | --- | --- |
| `+` | Addition (or string concatenation) | `5 + 3` | `8` |
| `-` | Subtraction | `5 - 3` | `2` |
| `*` | Multiplication | `5 * 3` | `15` |
| `/` | Division (**integer division truncates**) | `5 / 3` | `1` |
| `%` | Remainder / modulo | `5 % 3` | `2` |

### Unary

| Operator | Meaning |
| --- | --- |
| `+x` | Unary plus (does nothing; promotes to `int`) |
| `-x` | Negation |
| `++x` / `x++` | Increment by one (prefix / postfix) |
| `--x` / `x--` | Decrement by one |
| `!x` | Logical NOT (booleans only) |
| `~x` | Bitwise NOT (flips every bit) |

### Relational

| Operator | Meaning |
| --- | --- |
| `==` | Equal to |
| `!=` | Not equal to |
| `>` `<` `>=` `<=` | Greater / less than, and the "or equal" versions |
| `instanceof` | Type test |

### Logical

| Operator | Meaning | Short-circuits? |
| --- | --- | --- |
| `&&` | Conditional AND | **Yes** |
| `\|\|` | Conditional OR | **Yes** |
| `&` | Boolean AND | No — always evaluates both |
| `\|` | Boolean OR | No |
| `^` | Boolean XOR (exactly one is true) | No |
| `!` | NOT | — |

### Bitwise and shift

| Operator | Meaning |
| --- | --- |
| `&` | Bitwise AND |
| `\|` | Bitwise OR |
| `^` | Bitwise XOR |
| `~` | Bitwise complement |
| `<<` | Left shift (multiply by 2ⁿ) |
| `>>` | Signed right shift (keeps the sign bit) |
| `>>>` | Unsigned right shift (fills with zeros) |

### Assignment

`=` `+=` `-=` `*=` `/=` `%=` `&=` `|=` `^=` `<<=` `>>=` `>>>=`

### Other

| Operator | Meaning |
| --- | --- |
| `? :` | Ternary conditional |
| `(type)` | Cast |
| `.` | Member access |
| `->` | Lambda arrow (lesson 51) |
| `::` | Method reference (lesson 53) |

Java deliberately has **no** `**` power operator (use `Math.pow`), and **no** operator
overloading — `+` on `String` is the single hard-coded exception.

---

## 2. Precedence, highest to lowest

| Level | Operators | Associativity |
| --- | --- | --- |
| 1 | `x++` `x--` (postfix) | Left |
| 2 | `++x` `--x` `+x` `-x` `!` `~` | Right |
| 3 | `(cast)` `new` | Right |
| 4 | `*` `/` `%` | Left |
| 5 | `+` `-` | Left |
| 6 | `<<` `>>` `>>>` | Left |
| 7 | `<` `<=` `>` `>=` `instanceof` | Left |
| 8 | `==` `!=` | Left |
| 9 | `&` | Left |
| 10 | `^` | Left |
| 11 | `\|` | Left |
| 12 | `&&` | Left |
| 13 | `\|\|` | Left |
| 14 | `? :` | **Right** |
| 15 | `=` `+=` `-=` … | **Right** |

**Do not memorise this.** Memorise two things instead:

1. `*` `/` `%` bind tighter than `+` `-`.
2. **When in doubt, add parentheses.** Nobody has ever been criticised in code review for
   parenthesising too clearly.

The one that genuinely catches people is that **`&` `^` `|` bind tighter than `==`**:

```java
if (flags & MASK == 0)     // parses as flags & (MASK == 0)  -> compile error
if ((flags & MASK) == 0)   // what you meant
```

This is inherited from C, where it is a famous source of bugs.

---

## 3. `%` — the sign follows the left operand

```java
 7 %  3   //  1
-7 %  3   // -1   (not 2 — different from Python!)
 7 % -3   //  1
-7 % -3   // -1
7.5 % 2   //  1.5  (modulo works on doubles too)
```

Rule: **the result takes the sign of the dividend (the left side).** If you are coming
from Python, this will bite you when checking `x % 2 == 1` for oddness — that is `false`
for negative odd numbers in Java. Use `x % 2 != 0` instead.

---

## 4. `++` and `--` — prefix vs postfix

```java
int a = 5;
System.out.println(a++);   // prints 5, then a becomes 6
System.out.println(a);     // 6

int b = 5;
System.out.println(++b);   // b becomes 6, then prints 6
```

- **Postfix `a++`** — *use the old value*, then increment.
- **Prefix `++a`** — increment, then *use the new value*.

As a standalone statement (`a++;` on its own line) they are identical. The difference only
matters when the expression's value is used.

### The classic interview trap

```java
int i = 0;
i = i++;
System.out.println(i);   // 0  (!!)
```

Why zero? Java evaluates it in this order:

1. Save the current value of `i` (which is `0`) as the result of `i++`.
2. Increment `i` to `1`.
3. **Assign the saved value `0` back to `i`**, overwriting the increment.

The assignment happens last and wipes out the increment. Never write this. In C this is
*undefined behaviour*; in Java it is well-defined and still useless.

```java
int x = 5;
int y = x++ + ++x;   // 5 + 7 = 12, and x ends at 7
```

Evaluate strictly left to right: `x++` yields `5` (x becomes 6), then `++x` makes x `7`
and yields `7`.

**Practical advice:** never use `++`/`--` inside a larger expression. Put it on its own
line. Clever one-liners here cost more in review time than they save in typing.

---

## 5. Short-circuit evaluation — `&&` vs `&`

```java
if (list != null && list.size() > 0)   // safe
if (list != null &  list.size() > 0)   // NullPointerException if list is null
```

- `&&` stops as soon as the left side is `false` — the right side is **never evaluated**.
- `||` stops as soon as the left side is `true`.
- `&` and `|` always evaluate **both** sides.

This is not just an optimisation; it is a **correctness tool**. The null-check idiom above
only works because of short-circuiting.

It also means side effects can silently not happen:

```java
int count = 0;
boolean result = false && (count++ > 0);
System.out.println(count);   // 0 — count++ never ran
```

**Rule:** use `&&` and `||` for boolean logic, always. Reserve `&`, `|`, `^` for bit
manipulation. The only time you want the non-short-circuit boolean versions is when you
genuinely need both side effects to happen, which is rare and deserves a comment.

---

## 6. `==` on objects compares references, not contents

This is the single most common Java bug for newcomers.

```java
String a = "hello";
String b = "hello";
System.out.println(a == b);        // true   (!)

String c = new String("hello");
System.out.println(a == c);        // false  (!)
System.out.println(a.equals(c));   // true   <- what you actually wanted
```

Why the difference? String **literals** are interned in a shared pool, so `a` and `b`
point at the *same* object. `new String(...)` explicitly forces a new object on the heap,
so `a` and `c` are different objects holding equal text.

**Rule: `==` on references asks "are these the same object?" `.equals()` asks "do these
have the same value?"** For everything except primitives and enums, you want `.equals()`.

### The `Integer` cache trap

```java
Integer a = 127, b = 127;
System.out.println(a == b);   // true

Integer c = 128, d = 128;
System.out.println(c == d);   // false  (!!)
```

Java caches `Integer` objects for −128…127. Inside that range autoboxing reuses the same
object; outside it, new objects are created. This produces code that works in testing
with small numbers and fails in production with large ones. Lesson 42 covers it fully.

**Always use `.equals()` on wrapper types.** Or better, use primitives where you can.

---

## 7. The ternary operator

```java
int max = (a > b) ? a : b;
```

Read as: *"if `a > b`, the value is `a`, otherwise `b`."* It is an **expression** (it has
a value), unlike `if`, which is a statement.

Good use — a simple choice between two values:

```java
String label = (count == 1) ? "item" : "items";
```

Bad use — nesting:

```java
String grade = s >= 90 ? "A" : s >= 80 ? "B" : s >= 70 ? "C" : "F";   // hard to read
```

Ternaries are **right-associative**, so that chain parses as nested `else` branches. It
works, but an `if`/`else if` chain or a `switch` reads far better.

### The ternary autoboxing trap

```java
Integer value = someCondition ? 1 : null;   // can throw NullPointerException
```

If the two branches have different types, Java finds a common type and may unbox in the
process — turning a harmless `null` into an NPE. Keep both branches the same type.

---

## 8. Bitwise operators — what they are actually for

```java
int a = 0b1100;   // 12
int b = 0b1010;   // 10

a & b   // 0b1000 =  8   (1 only where BOTH are 1)
a | b   // 0b1110 = 14   (1 where EITHER is 1)
a ^ b   // 0b0110 =  6   (1 where they DIFFER)
~a      //         -13   (flips all 32 bits)
```

`~a` is `-13` because of two's complement: `~x == -x - 1`, always.

### Shifts

```java
5 << 1    // 10   multiply by 2
5 << 3    // 40   multiply by 8  (2^3)
20 >> 2   // 5    divide by 4
-20 >> 2  // -5   signed shift: fills with the SIGN bit
-20 >>> 2 // 1073741819   unsigned shift: fills with ZEROS
```

- `>>` preserves the sign (arithmetic shift) — correct for signed division by powers of 2.
- `>>>` always fills with zeros — used when treating an `int` as raw bits.
- There is **no `<<<`**, because left-shifting always fills with zeros anyway.

> **Shift-distance gotcha.** The distance is taken modulo 32 for `int` and modulo 64 for
> `long`. So `1 << 32` is `1`, not `0`.

### Where bitwise operators actually show up

**Flag sets** — packing many booleans into one integer:

```java
static final int READ    = 1;   // 0b001
static final int WRITE   = 2;   // 0b010
static final int EXECUTE = 4;   // 0b100

int permissions = READ | WRITE;                 // 0b011
boolean canWrite = (permissions & WRITE) != 0;  // true
permissions &= ~WRITE;                          // remove the WRITE flag
```

This is exactly how Unix file permissions and `java.lang.reflect.Modifier` work.

**Fast arithmetic:**

```java
boolean isEven = (n & 1) == 0;      // faster than n % 2 == 0
int half = n >> 1;                  // n / 2 for non-negative n
```

Modern JITs optimise the plain versions anyway, so write the readable one unless you have
measured a difference.

**Swap without a temporary** (a party trick, not production code):

```java
a ^= b;  b ^= a;  a ^= b;
```

---

## 9. Summary

- Integer `/` truncates; `%` takes the sign of the **left** operand.
- `*` `/` `%` bind tighter than `+` `-`; `&` `^` `|` bind tighter than `==`. Parenthesise.
- Postfix `a++` yields the old value; prefix `++a` yields the new one. Never use either
  inside a larger expression — `i = i++` leaves `i` unchanged.
- `&&` and `||` short-circuit; `&` and `|` do not. Short-circuiting is a correctness tool,
  not just an optimisation.
- `==` on references compares identity. Use `.equals()` for value comparison — always for
  `String` and wrapper types.
- `Integer` caches −128…127, so `==` on boxed integers works for small values and breaks
  for large ones.
- The ternary is an expression; keep both branches the same type and do not nest it.
- Bitwise operators are for flags and raw bits. `~x == -x - 1`. `>>` keeps the sign,
  `>>>` fills with zeros, and shift distances wrap modulo 32 (or 64).

---

**Previous:** [04 — Type casting](../01-getting-started/04-type-casting-and-conversion.md) ·
**Next:** [06 — Reading input with Scanner](06-reading-input-with-scanner.md)
