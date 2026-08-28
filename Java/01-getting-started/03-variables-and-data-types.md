# 03 · Variables, Primitive Types and Literals

> **Run the code for this lesson**
> ```bash
> java Java/01-getting-started/03-variables-and-data-types.java
> ```

Java is statically typed, which means **every variable's type is decided at compile time
and can never change**. This lesson covers every type Java has, exactly how much memory
each one uses, what values it can hold, and the specific ways each one will surprise you.

---

## 1. Declaring a variable

```java
int age = 25;
//  ^    ^    ^
//  |    |    └── the value (an "initialiser")
//  |    └─────── the identifier (the name)
//  └──────────── the type
```

You can split declaration from assignment:

```java
int age;        // declared, but not yet initialised
age = 25;       // now assigned
```

But you **cannot read a local variable before assigning it**:

```java
int age;
System.out.println(age);   // ERROR: variable age might not have been initialized
```

This is a compile-time check called **definite assignment**, and it single-handedly
eliminates a whole class of bugs that C programmers spend careers debugging.

> **Important exception.** *Fields* (variables belonging to an object or class) get
> automatic default values; *local variables* (inside a method) do not. See §7.

---

## 2. The eight primitive types — the complete table

Java has exactly **eight** primitive types. Not seven, not nine. Memorise this table; it
appears in interviews verbatim.

| Type | Size | Range | Default | Literal example |
| --- | --- | --- | --- | --- |
| `byte` | 8 bits | −128 to 127 | `0` | `byte b = 100;` |
| `short` | 16 bits | −32,768 to 32,767 | `0` | `short s = 30000;` |
| `int` | 32 bits | −2,147,483,648 to 2,147,483,647 | `0` | `int i = 42;` |
| `long` | 64 bits | ±9.22 × 10¹⁸ | `0L` | `long l = 42L;` |
| `float` | 32 bits | ~±3.4 × 10³⁸, ~7 digits precision | `0.0f` | `float f = 3.14f;` |
| `double` | 64 bits | ~±1.8 × 10³⁰⁸, ~15 digits precision | `0.0d` | `double d = 3.14;` |
| `char` | 16 bits | 0 to 65,535 (**unsigned**) | the null char (code 0) | `char c = 'A';` |
| `boolean` | JVM-dependent (~1 byte in practice) | `true` or `false` | `false` | `boolean b = true;` |

### Things this table is telling you

- **`char` is the only unsigned type.** Every numeric type in Java is signed except `char`.
- **`int` is the default** for whole-number literals. `long x = 10000000000;` is a
  *compile error* — the literal itself overflows `int` before it is ever assigned. You
  need `10000000000L`.
- **`double` is the default** for decimal literals. `float f = 3.14;` is a compile error;
  you need `3.14f`.
- **`boolean` has no defined size.** The JVM spec deliberately does not specify it.
- Sizes are **fixed on every platform**. A Java `int` is 32 bits on a watch and on a
  mainframe. C makes no such promise, and that difference is a big part of WORA.

### The ranges are not arbitrary

An `n`-bit signed type holds −2ⁿ⁻¹ to 2ⁿ⁻¹−1. The range is asymmetric because zero takes
one of the "positive" slots. That is why `int` bottoms out at −2,147,483,648 but tops out
one lower in magnitude, at 2,147,483,647.

You never need to memorise the numbers — ask the JDK:

```java
System.out.println(Integer.MAX_VALUE);   // 2147483647
System.out.println(Integer.MIN_VALUE);   // -2147483648
System.out.println(Long.MAX_VALUE);
System.out.println(Double.MAX_VALUE);
```

---

## 3. Overflow — Java wraps around silently

This is the number-one arithmetic trap in Java:

```java
int max = Integer.MAX_VALUE;   //  2147483647
System.out.println(max + 1);   // -2147483648   (!!)
```

**No exception. No warning. It silently wraps.** Adding one to the largest `int` gives
you the smallest `int`, because the bit pattern rolls over from `0111...1` to `1000...0`.

This causes real production bugs. The classic:

```java
int mid = (low + high) / 2;   // BROKEN for large arrays: low + high can overflow
int mid = low + (high - low) / 2;   // correct
```

That exact bug lived undetected in the JDK's own binary search for nine years.

### Detecting it

Since Java 8, `Math` has exact-arithmetic methods that throw instead of wrapping:

```java
Math.addExact(Integer.MAX_VALUE, 1);   // throws ArithmeticException: integer overflow
Math.multiplyExact(a, b);
Math.subtractExact(a, b);
```

Use these whenever silent wraparound would be a correctness bug.

---

## 4. Floating point — why `0.1 + 0.2 != 0.3`

```java
System.out.println(0.1 + 0.2);            // 0.30000000000000004
System.out.println(0.1 + 0.2 == 0.3);     // false
```

This is not a Java bug. `float` and `double` follow the **IEEE 754** standard and store
numbers in *binary* fractions. Just as 1/3 cannot be written exactly in decimal
(0.333…), 1/10 cannot be written exactly in binary. The stored value is the nearest
representable one, and the tiny errors accumulate.

### The three rules

**1. Never compare floating-point numbers with `==`.** Compare within a tolerance:

```java
double epsilon = 1e-9;
if (Math.abs(a - b) < epsilon) { /* equal enough */ }
```

**2. Never use `float` or `double` for money.** Use `BigDecimal`:

```java
BigDecimal price = new BigDecimal("0.10");   // String constructor, NOT the double one
BigDecimal tax   = new BigDecimal("0.20");
System.out.println(price.add(tax));          // exactly 0.30
```

Note `new BigDecimal("0.1")` (correct) versus `new BigDecimal(0.1)` (wrong — the `double`
is already inaccurate before `BigDecimal` ever sees it).

**3. `float` is almost never the right choice.** It has about 7 significant digits, which
runs out fast. Use `double` unless you are storing millions of values and memory is
genuinely the constraint.

### Floating point has special values

```java
System.out.println(1.0 / 0);      // Infinity      (no exception!)
System.out.println(-1.0 / 0);     // -Infinity
System.out.println(0.0 / 0);      // NaN  (Not a Number)
System.out.println(1 / 0);        // ArithmeticException: / by zero  <- INTEGER division
System.out.println(Double.NaN == Double.NaN);   // false (!) - NaN equals nothing, not even itself
```

Note the asymmetry: **integer** division by zero throws; **floating-point** division by
zero returns infinity. To test for `NaN`, use `Double.isNaN(x)`.

---

## 5. `char` is a number

A `char` is a 16-bit **unsigned integer** that Java happens to print as a character.

```java
char letter = 'A';
int  code   = letter;          // 65 - no cast needed, char widens to int
char next   = (char) (letter + 1);   // 'B'
System.out.println('A' + 1);   // 66  (!) - the + promotes both to int
System.out.println((char)('A' + 1)); // B
```

That `'A' + 1` giving `66` rather than `"B"` surprises everyone once. Arithmetic on
`char` promotes to `int`; you must cast back explicitly.

### Ways to write a char literal

```java
char a = 'A';         // the character itself
char b = 65;          // its numeric code
char c = '\u0041';  // Unicode escape - also 'A'
char tab = '\t';      // escape sequence
char rupee = '₹';  // any Unicode character in the Basic Multilingual Plane
```

| Escape | Meaning |
| --- | --- |
| `\n` | newline |
| `\t` | tab |
| `\\` | backslash |
| `\'` | single quote |
| `\"` | double quote |
| `\uXXXX` | Unicode code point (4 hex digits) |

> **Limitation worth knowing.** A `char` is 16 bits, which covers Unicode's Basic
> Multilingual Plane. Emoji and some scripts live above that and need **two** chars
> (a "surrogate pair"). That is why `"😀".length()` returns `2`, not `1`.

---

## 6. Literals — writing values in source

```java
int decimal     = 255;
int hexadecimal = 0xFF;        // 0x prefix - 255
int octal       = 0377;        // leading 0 - 255  (a trap: 0123 is NOT 123!)
int binary      = 0b1111_1111; // 0b prefix (Java 7+) - 255

long big        = 9_000_000_000L;   // L suffix required
float f         = 3.14f;            // f suffix required
double d        = 3.14;             // default, no suffix needed
double sci      = 1.5e3;            // scientific notation - 1500.0

boolean flag    = true;             // only `true` or `false`, never 0 or 1
String  text    = "double quotes";
char    letter  = 'x';              // single quotes, exactly one character
```

### Underscores in numeric literals (Java 7+)

`1_000_000` is far more readable than `1000000`, and the compiler ignores the
underscores entirely. Rules: they may not start or end the literal, and may not sit
adjacent to the decimal point or a suffix.

### The octal trap

A leading zero means **octal**, not decimal:

```java
int a = 0123;   // 83, not 123!
```

This bites people writing zero-padded values like dates or IDs. Do not zero-pad numeric
literals.

---

## 7. Default values — fields yes, locals no

```java
class Example {
    int fieldValue;              // automatically 0
    boolean fieldFlag;           // automatically false
    String fieldText;            // automatically null

    void method() {
        int localValue;
        System.out.println(localValue);  // COMPILE ERROR
    }
}
```

| | Fields (instance/static) | Local variables |
| --- | --- | --- |
| Default value | Yes - `0`, `false`, the null char, `null` | **No** |
| Reading before assignment | Allowed | Compile error |

The reasoning: fields live in objects that the JVM zeroes out on allocation. Locals live
on the stack in memory that may hold anything, so the compiler forces you to assign first.

---

## 8. Primitives vs reference types

Everything in Java is either a **primitive** or a **reference**.

```java
int number = 42;                    // the variable HOLDS the value 42
String text = "hello";              // the variable holds a REFERENCE (an address)
```

| | Primitive | Reference |
| --- | --- | --- |
| Stores | The actual value | A pointer to an object on the heap |
| Lives on | Stack (if local) | Variable on stack, object on heap |
| Can be `null` | **No** | Yes |
| Has methods | No | Yes |
| Examples | `int`, `double`, `char`, `boolean` | `String`, arrays, every class you write |

The eight primitives are the *only* non-objects in Java. This is why Java is often called
"not purely object-oriented" — and it is a deliberate performance decision, because
boxing every `int` into an object would be catastrophically slow in a loop.

Every primitive has a **wrapper class** (`int` → `Integer`, `char` → `Character`) for
when you need an object — for example inside collections, which cannot hold primitives.
Lesson 42 covers wrappers and autoboxing in full.

---

## 9. `var` — local type inference (Java 10+)

```java
var message = "Hello";        // inferred as String
var count = 42;               // inferred as int
var list = new ArrayList<String>();   // inferred as ArrayList<String>
```

`var` is **not** dynamic typing. The variable still has one fixed type forever; the
compiler just works it out from the initialiser. This still fails:

```java
var x = "hello";
x = 42;    // ERROR: incompatible types
```

### Where `var` is not allowed

```java
var x;                        // ERROR: no initialiser to infer from
var y = null;                 // ERROR: cannot infer from null
var[] arr = {1, 2};           // ERROR: not allowed on array declarations
class C { var field = 1; }    // ERROR: fields cannot use var
void method(var p) { }        // ERROR: parameters cannot use var
```

`var` works for **local variables only**, and only with an initialiser.

### When to use it

Good: when the type is obvious from the right-hand side and long to write.

```java
var reader = new BufferedReader(new FileReader("data.txt"));   // clear
```

Bad: when it hides information the reader needs.

```java
var result = process();   // what type is this? now I have to go look.
```

---

## 10. Naming rules and conventions

**Rules (enforced by the compiler):**

- Must start with a letter, `$`, or `_`. Never a digit.
- May then contain letters, digits, `$`, `_`.
- Cannot be a Java keyword (`class`, `int`, `for`, …).
- Case-sensitive.
- Unicode letters are legal, so `int नाम = 5;` compiles. Do not do this.

**Conventions (enforced by every reviewer):**

```java
int accountBalance;               // camelCase for variables
final int MAX_RETRY_COUNT = 3;    // SCREAMING_SNAKE_CASE for constants
class BankAccount { }             // PascalCase for classes
```

Avoid `$` entirely — the compiler uses it for generated names. Avoid a bare `_`; it has
been a reserved keyword since Java 9 and is a compile error on its own.

---

## 11. Summary

- Eight primitives: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean`.
  Sizes are identical on every platform.
- `int` is the default whole-number type; `double` the default decimal. `long` literals
  need `L`, `float` literals need `f`.
- Integer arithmetic **overflows silently** — use `Math.addExact` when that would be a bug.
- Floating point is inexact by design. Never `==` them; never use them for money
  (use `BigDecimal` with the `String` constructor).
- `char` is a 16-bit **unsigned** number; arithmetic on it promotes to `int`.
- Integer `/ 0` throws; floating-point `/ 0` gives `Infinity`. `NaN != NaN`.
- Fields get default values; **local variables do not** and must be assigned before use.
- Primitives hold values and can never be `null`; references hold addresses and can.
- `var` infers a **fixed** type for local variables only — it is not dynamic typing.

---

**Previous:** [02 — Your first program](02-your-first-program.md) ·
**Next:** [04 — Type casting and conversion](04-type-casting-and-conversion.md)
