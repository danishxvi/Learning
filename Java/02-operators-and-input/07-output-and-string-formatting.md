# 07 · Printing and String Formatting

> **Run the code for this lesson**
> ```bash
> java Java/02-operators-and-input/07-output-and-string-formatting.java
> ```

`System.out.println` gets you through week one. This lesson covers everything after that:
`printf` format specifiers in full, `String.format`, text blocks, number and currency
formatting, and why `System.out.println` is the wrong tool in production code.

---

## 1. The output methods

| Method | Behaviour |
| --- | --- |
| `System.out.print(x)` | Print, no newline |
| `System.out.println(x)` | Print, then newline |
| `System.out.printf(fmt, args...)` | Print with formatting, **no automatic newline** |
| `System.out.format(fmt, args...)` | Identical to `printf` — same method, two names |
| `String.format(fmt, args...)` | Returns the formatted `String` instead of printing it |
| `System.err.println(x)` | Print to the **error** stream |

`printf` does **not** add a newline. You must include `%n` yourself. Forgetting is the
most common `printf` mistake.

---

## 2. Format specifiers — the complete reference

A specifier looks like this:

```
%[argument_index$][flags][width][.precision]conversion
```

Only the `%` and the conversion character are required.

### Conversions

| Conversion | Accepts | Example | Output |
| --- | --- | --- | --- |
| `%s` | Anything (calls `toString()`) | `%s` with `"hi"` | `hi` |
| `%S` | Anything | `%S` with `"hi"` | `HI` |
| `%d` | Integer types only | `%d` with `42` | `42` |
| `%f` | Floating point | `%f` with `3.14` | `3.140000` |
| `%e` | Scientific notation | `%e` with `31400.0` | `3.140000e+04` |
| `%g` | General (picks `%f` or `%e`) | `%g` with `0.00003` | `3.00000e-05` |
| `%b` | Boolean (**or "is it non-null?"**) | `%b` with `"hi"` | `true` |
| `%c` | Character | `%c` with `65` | `A` |
| `%x` / `%X` | Hexadecimal | `%x` with `255` | `ff` / `FF` |
| `%o` | Octal | `%o` with `8` | `10` |
| `%n` | Platform newline | — | `\n` or `\r\n` |
| `%%` | A literal percent sign | — | `%` |

> **`%b` is not a boolean test.** It prints `true` for *any non-null* value and `false`
> only for `null`. `String.format("%b", "hello")` is `true`. This surprises people.

> **`%d` accepts integer types only.** `String.format("%d", 3.14)` throws
> `IllegalFormatConversionException` at runtime — not a compile error.

### Flags

| Flag | Meaning | Example | Output |
| --- | --- | --- | --- |
| `-` | Left-justify | `%-10s\|` | `hello     \|` |
| `0` | Zero-pad numbers | `%05d` | `00042` |
| `+` | Always show the sign | `%+d` | `+42` |
| `,` | Locale group separator | `%,d` with `1234567` | `1,234,567` |
| `(` | Negatives in parentheses | `%(d` with `-42` | `(42)` |
| ` ` (space) | Space for positive numbers | `% d` | ` 42` |
| `#` | Alternate form (`0x` for hex, `0` for octal) | `%#x` | `0xff` |

### Width and precision

```java
"%10s"    // right-justified in 10 columns
"%-10s"   // LEFT-justified in 10 columns
"%.2f"    // exactly 2 decimal places
"%10.2f"  // 10 columns wide, 2 decimals
"%.3s"    // TRUNCATE a string to 3 characters
```

Width is a **minimum**, never a maximum — content longer than the width is never
truncated (except by `.precision` on a string). That is why a formatted table can still
break its alignment on unusually long values.

### Argument index

```java
String.format("%1$s is %2$d. Again: %1$s", "Java", 31);
// "Java is 31. Again: Java"
```

`%1$s` means "the first argument". Useful when the same value appears twice, and
essential for translated messages where word order differs between languages.

---

## 3. `%f` rounds; it does not truncate

```java
String.format("%.2f", 3.145)    // "3.15"  (rounds HALF_UP)
String.format("%.2f", 3.144)    // "3.14"
String.format("%.0f", 2.5)      // "3"
```

This is different from `(int)` casting, which truncates. And because the underlying value
is binary floating point, edge cases can still surprise you:

```java
String.format("%.2f", 1.005)    // "1.01" on most JDKs — but 1.005 is really
                                // 1.00499999999999989... in binary
```

For money, format a `BigDecimal`, not a `double`.

---

## 4. `String.format` vs `printf` vs concatenation

```java
// 1. Concatenation — fine for one or two values
System.out.println("Name: " + name + ", age: " + age);

// 2. printf — best when you need alignment or decimal control
System.out.printf("Name: %-10s Age: %3d%n", name, age);

// 3. String.format — when you need the string, not the output
String message = String.format("Name: %s, age: %d", name, age);

// 4. Text block + formatted (Java 15+) — best for multi-line
String report = """
        Name: %s
        Age:  %d
        """.formatted(name, age);
```

`"...".formatted(args)` is an instance method added in Java 15. It is exactly
`String.format(this, args)` but reads better when chained onto a text block.

---

## 5. Text blocks (Java 15+)

Before text blocks, multi-line strings were painful:

```java
String json = "{\n" +
              "  \"name\": \"Danish\",\n" +
              "  \"age\": 25\n" +
              "}";
```

Now:

```java
String json = """
        {
          "name": "Danish",
          "age": 25
        }
        """;
```

### The rules

- Opening `"""` **must** be followed by a line break. `"""hello"""` is a compile error.
- **Incidental indentation is stripped.** The compiler finds the least-indented
  non-blank line (including the closing `"""`) and removes that much from every line.
- The position of the **closing** `"""` therefore controls indentation. Put it on its own
  line, aligned where you want the left margin.
- A trailing newline is included unless you end the last content line with `\`.
- Double quotes need no escaping. Backslashes still do.

```java
String noTrailingNewline = """
        line one
        line two\
        """;               // the \ suppresses the final newline
```

Text blocks are ideal for JSON, SQL, HTML and multi-line help text — anywhere escaping
quotes used to make the code unreadable.

---

## 6. Formatting numbers for humans

`printf` handles layout. For locale-aware currency, percentages and compact numbers, use
`java.text.NumberFormat`:

```java
NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.US);
currency.format(1234567.891);     // "$1,234,567.89"

NumberFormat german = NumberFormat.getCurrencyInstance(Locale.GERMANY);
german.format(1234567.891);       // "1.234.567,89 €"   — separators swapped, symbol trails

NumberFormat inr = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
inr.format(1234567.891);          // "₹1,234,567.89"

NumberFormat percent = NumberFormat.getPercentInstance(Locale.US);
percent.format(0.756);            // "76%"

NumberFormat compact = NumberFormat.getCompactNumberInstance(
        Locale.US, NumberFormat.Style.SHORT);          // Java 12+
compact.format(1_200_000);        // "1M"
```

> **Always pass the `Locale` explicitly** when the output format matters. The no-argument
> overloads use the *default* locale, so the same line produces different text on
> different machines. `getCompactNumberInstance().format(1_200_000)` is `"1M"` in a US
> locale and `"12L"` — twelve lakh — in an Indian one. Both are correct; only one is what
> your test asserted.
>
> Note also that `new Locale("en", "IN")` is deprecated since Java 19. Use
> `Locale.of("en", "IN")`.

For full control over the pattern, use `DecimalFormat`:

```java
new DecimalFormat("#,##0.00").format(1234.5);    // "1,234.50"
new DecimalFormat("000").format(7);              // "007"
new DecimalFormat("#.##%").format(0.1234);       // "12.34%"
```

> **`NumberFormat` and `DecimalFormat` are not thread-safe.** Do not share one instance
> across threads. Create one per use, or use a `ThreadLocal`.

---

## 7. Why production code does not use `System.out.println`

`System.out.println` is right for learning and for command-line tools. It is wrong inside
an application, because it:

- has **no severity levels** — you cannot filter out debug noise in production;
- has **no timestamps**, no thread name, no class name;
- **cannot be turned off** without editing and redeploying code;
- writes to the console only — not to a file, not to a log aggregator;
- is **synchronised**, so it becomes a genuine bottleneck in hot loops;
- is invisible in most server deployments where stdout is discarded.

Real code uses a logging framework (SLF4J with Logback or Log4j2):

```java
private static final Logger log = LoggerFactory.getLogger(MyClass.class);

log.debug("Cache miss for key {}", key);      // filtered out in production
log.info("Order {} placed by {}", orderId, userId);
log.error("Payment failed for order {}", orderId, exception);
```

Note the `{}` placeholders: the string is only assembled if that level is actually
enabled, so a disabled `log.debug` costs almost nothing. `"..." + key` would build the
string every time regardless.

**For this repository, `System.out.println` is exactly right** — the whole point is to see
values immediately. Just know what you would use instead at work.

---

## 8. Summary

- `printf` does **not** add a newline. Use `%n`, not `\n` — `%n` is platform-correct.
- Specifier shape: `%[index$][flags][width][.precision]conversion`.
- `%s` anything, `%d` integers only, `%f` floating point, `%%` a literal percent.
- `%b` prints `true` for any non-null value — it is not a boolean test.
- `%d` with a `double` throws `IllegalFormatConversionException` at **runtime**.
- Width is a minimum, not a maximum; only `.precision` truncates strings.
- `%.2f` **rounds**; `(int)` truncates.
- `String.format` returns a string; `"...".formatted(...)` (Java 15+) reads better on
  text blocks.
- Text blocks strip incidental indentation based on the closing `"""`; end a line with
  `\` to suppress the final newline.
- Use `NumberFormat` / `DecimalFormat` for locale-aware currency and percentages — and
  never share an instance across threads.
- Use a logging framework, not `println`, in real applications.

---

**Previous:** [06 — Reading input with Scanner](06-reading-input-with-scanner.md) ·
**Next:** [08 — `if` / `else` and the ternary](../03-control-flow/08-if-else-and-ternary.md)
