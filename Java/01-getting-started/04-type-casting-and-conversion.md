# 04 · Type Casting, Promotion and Conversion

> **Run the code for this lesson**
> ```bash
> java Java/01-getting-started/04-type-casting-and-conversion.java
> ```

Java is statically typed, but values still need to move between types constantly. There
are three separate mechanisms, and confusing them causes silent data loss:

1. **Widening** — automatic, safe, no syntax needed.
2. **Narrowing** — manual, lossy, requires an explicit cast.
3. **Parsing / conversion** — turning text into numbers and back, using library methods.

---

## 1. The widening ladder

Java automatically converts a value to a **larger** type, because nothing can be lost:

```
byte → short → int → long → float → double
                ↑
             char
```

```java
int  small = 42;
long big   = small;      // automatic. No cast. No loss.
double d   = big;        // automatic.
```

This is called **widening primitive conversion** or *implicit conversion*. It happens
silently in assignments, method calls, and arithmetic.

### Two surprises in that ladder

**`char` joins at `int`, not at `short`.** Both are 16 bits, but `char` is unsigned
(0…65,535) and `short` is signed (−32,768…32,767). Neither fits inside the other, so
there is **no automatic conversion in either direction**:

```java
char c = 'A';
short s = c;    // ERROR: possible lossy conversion
short x = 5;
char y = x;     // ERROR: possible lossy conversion
```

**`long → float` is widening but still loses precision.** A `long` has 64 bits of
integer precision; a `float` has only about 24 bits of mantissa. So:

```java
long precise = 123456789123456789L;
float lossy  = precise;              // legal, automatic... and wrong
System.out.println(lossy);           // 1.23456791E17  <- digits gone
```

Widening guarantees the *magnitude* survives, **not** every digit. This trips up people
who assume "automatic means safe".

---

## 2. Narrowing — the explicit cast

Going the other way (large → small) can lose data, so Java forces you to say so:

```java
double d = 9.99;
int i = (int) d;      // 9   <- the cast is mandatory
```

Syntax: `(targetType) value`.

### Narrowing truncates, it does not round

```java
(int) 9.99    // 9    not 10
(int) 9.01    // 9
(int) -9.99   // -9   toward zero, not toward negative infinity
```

To round, use `Math.round()`:

```java
Math.round(9.99)     // 10  (returns long for a double argument)
Math.round(9.5f)     // 10  (returns int for a float argument)
Math.floor(9.99)     // 9.0  (returns double)
Math.ceil(9.01)      // 10.0 (returns double)
```

### Narrowing integers throws away high bits

```java
int big = 300;
byte b = (byte) big;      // 44   (!!)
```

Why 44? `300` in binary is `1_0010_1100`. A `byte` keeps only the low 8 bits:
`0010_1100` = 44. Nothing warns you. Nothing throws. **The bits are simply discarded.**

This is the single most dangerous conversion in Java, because the cast makes it look
deliberate even when the programmer had no idea the value could exceed 127.

### Casting `double` beyond `int` range saturates

```java
(int) 1e20        // 2147483647   (clamped to Integer.MAX_VALUE, not wrapped)
(int) Double.NaN  // 0
```

Floating-point → integer conversion *saturates* rather than truncating bits. Yet another
inconsistency worth knowing.

---

## 3. Numeric promotion in expressions

This rule explains a whole family of "why is my answer wrong?" bugs.

> **In any arithmetic expression, operands smaller than `int` are promoted to `int`
> first. If either operand is larger, both are promoted to the largest type present.**

The promotion order is: `int` → `long` → `float` → `double`.

```java
byte a = 10, b = 20;
byte c = a + b;        // ERROR: possible lossy conversion from int to byte
byte c = (byte)(a + b);  // fine
```

`a + b` produced an `int` even though both operands were `byte`. Assigning that `int`
back to a `byte` needs a cast.

### Integer division truncates

```java
System.out.println(5 / 2);        // 2      both operands are int
System.out.println(5.0 / 2);      // 2.5    one is double, so both promote
System.out.println(5 / 2.0);      // 2.5
System.out.println((double) 5 / 2); // 2.5
System.out.println((double)(5 / 2)); // 2.0   TOO LATE - division already happened
```

That last line is the classic mistake: casting the *result* instead of an *operand*.

### Percentage bug

```java
int done = 3, total = 10;
double percent = done / total * 100;        // 0.0   (!!)
double percent = (double) done / total * 100; // 30.0
```

`3 / 10` is integer division, giving `0`, before the multiplication ever happens.

### The compound-assignment loophole

```java
byte b = 10;
b = b + 5;    // ERROR: possible lossy conversion
b += 5;       // FINE  (!!)
```

Compound operators (`+=`, `-=`, `*=`, `/=`, `%=`) contain a **hidden implicit cast**.
`b += 5` is defined as `b = (byte)(b + 5)`. Convenient — and a trap:

```java
byte b = 127;
b += 1;              // no error, silently becomes -128
```

Similarly:

```java
int i = 5;
i += 3.9;            // legal! becomes 8, because it means i = (int)(i + 3.9)
```

---

## 4. `char` arithmetic

Because `char` is a number, arithmetic on it promotes to `int`:

```java
char a = 'A';
System.out.println(a + 1);          // 66  (an int)
System.out.println((char)(a + 1));  // B
System.out.println("" + a + 1);     // A1  <- string concatenation, not arithmetic!
```

That last line matters: once a `String` is involved, `+` means *concatenate*, and
evaluation is strictly left to right.

```java
System.out.println(1 + 2 + "3");    // "33"   (1+2=3, then concat)
System.out.println("1" + 2 + 3);    // "123"  (concat, then concat)
```

---

## 5. Reference casting — upcast and downcast

Casting also applies to objects, with completely different rules.

```java
Object o = "hello";           // UPCAST: automatic, always safe
String s = (String) o;        // DOWNCAST: explicit, checked at runtime
```

- **Upcasting** (subclass → superclass) is implicit and can never fail.
- **Downcasting** (superclass → subclass) needs an explicit cast and **is verified at
  runtime**. If the object is not really that type, you get `ClassCastException`.

```java
Object o = Integer.valueOf(42);
String s = (String) o;        // compiles fine, throws ClassCastException at runtime
```

The safe pattern, since Java 16, is **pattern matching for `instanceof`**:

```java
if (o instanceof String text) {
    System.out.println(text.toUpperCase());   // `text` is already a String here
}
```

Before Java 16 you wrote the same thing in three lines:

```java
if (o instanceof String) {
    String text = (String) o;
    System.out.println(text.toUpperCase());
}
```

`instanceof` returns `false` for `null`, so it also guards against
`NullPointerException` for free. Lesson 37 covers pattern matching in depth.

---

## 6. String ⇄ number conversion

Casting does **not** work between `String` and numbers. `(int) "42"` is a compile error.
You need library methods.

### String → number (parsing)

```java
int    i = Integer.parseInt("42");
long   l = Long.parseLong("42");
double d = Double.parseDouble("3.14");
boolean b = Boolean.parseBoolean("true");

int hex = Integer.parseInt("FF", 16);    // 255 - with a radix
```

Bad input throws `NumberFormatException` — an *unchecked* exception, so the compiler
will not remind you to handle it:

```java
Integer.parseInt("abc");   // NumberFormatException: For input string: "abc"
Integer.parseInt("");      // NumberFormatException
Integer.parseInt(null);    // NumberFormatException (not NPE)
Integer.parseInt(" 42 ");  // NumberFormatException - whitespace is NOT trimmed
```

Always wrap parsing of user input in a `try`/`catch`, and `trim()` first.

### `parseInt` vs `valueOf`

```java
int     primitive = Integer.parseInt("42");   // returns int
Integer object    = Integer.valueOf("42");    // returns Integer (an object)
```

Use `parseInt` when you want a primitive — it avoids creating an object. Use `valueOf`
when you need the wrapper (for a collection, for example). Lesson 42 goes deeper.

### number → String

```java
String a = String.valueOf(42);     // preferred - handles null safely
String b = Integer.toString(42);
String c = 42 + "";                // works, but wasteful and unclear
String d = String.format("%.2f", 3.14159);   // "3.14"
```

`String.valueOf(null)` is safe; `someObject.toString()` on a null reference is a
`NullPointerException`. That is why `String.valueOf` is the recommended default.

---

## 7. Quick reference

| From → To | Mechanism | Loses data? |
| --- | --- | --- |
| `int` → `long` | Automatic (widening) | No |
| `long` → `float` | Automatic (widening) | **Yes** — precision |
| `double` → `int` | `(int)` cast | Yes — truncates toward zero |
| `int` → `byte` | `(byte)` cast | Yes — keeps low 8 bits only |
| `char` ↔ `short` | Cast required both ways | Possibly |
| `String` → `int` | `Integer.parseInt()` | Throws on bad input |
| `int` → `String` | `String.valueOf()` | No |
| `Object` → `String` | `(String)` cast | Throws `ClassCastException` if wrong |
| subclass → superclass | Automatic (upcast) | No |

---

## 8. Summary

- Widening (`byte`→…→`double`) is automatic; narrowing always needs an explicit cast.
- "Automatic" does not mean "lossless": `long`→`float` and `int`→`float` lose precision.
- Narrowing **truncates** (`(int) 9.99` is `9`); use `Math.round()` to round.
- `int`→`byte` discards high bits silently. `(byte) 300` is `44`.
- Any arithmetic on types smaller than `int` promotes to `int` first.
- `5 / 2` is `2`. Cast an **operand**, never the result.
- Compound assignment (`+=`) hides an implicit cast — convenient, and a silent-overflow trap.
- Once a `String` appears in a `+` expression, `+` means concatenation, left to right.
- Object upcasts are free; downcasts are runtime-checked. Prefer
  `if (o instanceof String s)`.
- `String` ⇄ number needs `Integer.parseInt` / `String.valueOf`, not casts.

---

**Previous:** [03 — Variables and data types](03-variables-and-data-types.md) ·
**Next:** [05 — Operators](../02-operators-and-input/05-operators.md)
