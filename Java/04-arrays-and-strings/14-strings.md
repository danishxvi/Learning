# 14 · Strings, the String Pool and Immutability

> **Run the code for this lesson**
> ```bash
> java Java/04-arrays-and-strings/14-strings.java
> ```

`String` is the most-used class in Java, and the one with the most surprising behaviour.
Two facts explain nearly all of it:

1. **Strings are immutable.** No method ever changes a string; every one returns a new one.
2. **String literals are interned** in a shared pool, so identical literals are the *same
   object*.

---

## 1. Immutability

```java
String s = "hello";
s.toUpperCase();
System.out.println(s);           // "hello" — unchanged!

s = s.toUpperCase();             // you must reassign
System.out.println(s);           // "HELLO"
```

Every `String` method — `toUpperCase`, `trim`, `replace`, `substring`, `concat` — returns
a **new** string and leaves the original untouched. Calling one and ignoring the result is
one of the most common beginner bugs, and the compiler cannot warn you about it because
the code is technically valid.

### Why immutable?

| Reason | Detail |
| --- | --- |
| **Security** | A filename or URL passed to a security check cannot be changed afterwards by another thread |
| **Thread safety** | Immutable objects need no synchronisation — they can be shared freely |
| **Caching the hash** | `hashCode()` is computed once and stored, which is why `String` is such a good `HashMap` key |
| **The string pool** | Sharing one object between many variables is only safe if nobody can modify it |

The cost is that building a string in a loop with `+=` creates a new object each time —
which is why `StringBuilder` exists (lesson 15).

---

## 2. The string pool

The JVM keeps a special area (historically "PermGen", now part of the heap) called the
**string pool** or *string constant pool*.

```java
String a = "hello";     // creates one object in the pool
String b = "hello";     // finds the existing one — SAME object
System.out.println(a == b);       // true

String c = new String("hello");   // FORCES a new object on the heap
System.out.println(a == c);       // false
System.out.println(a.equals(c));  // true
```

Diagram:

```
  Heap
  ┌─────────────────────────────────────────┐
  │  String Pool                            │
  │   ┌──────────┐                          │
  │   │ "hello"  │ ◄─── a                   │
  │   └──────────┘ ◄─── b                   │
  │                                         │
  │   ┌──────────┐                          │
  │   │ "hello"  │ ◄─── c   (new String())  │
  │   └──────────┘                          │
  └─────────────────────────────────────────┘
```

### `new String("x")` is almost always wrong

It creates a redundant object and defeats the pool. There is essentially never a reason to
write it in real code. Its only use is to *demonstrate* this behaviour, which is exactly
why it shows up in interview questions and nowhere else.

> **Interview classic:** how many objects does `String s = new String("hello");` create?
> **Two** — one in the pool for the literal `"hello"` (if not already there), and one on
> the heap for the `new`.

### `intern()`

```java
String c = new String("hello");
String pooled = c.intern();       // returns the POOLED instance
System.out.println(pooled == a);  // true
```

`intern()` is occasionally useful for deduplicating a large number of repeated strings
read from a file or database. It is also easy to overuse — modern JVMs have automatic
string deduplication (`-XX:+UseStringDeduplication` with G1), which is usually the better
answer.

### Compile-time constant folding

```java
String a = "hello";
String b = "hel" + "lo";          // folded by the COMPILER into "hello"
System.out.println(a == b);       // true!

String part = "hel";
String c = part + "lo";           // computed at RUNTIME — a new object
System.out.println(a == c);       // false

final String finalPart = "hel";
String d = finalPart + "lo";      // final + literal is a constant expression → folded
System.out.println(a == d);       // true
```

This is why `==` on strings appears to work in simple test code and then fails on real
data. **Never use `==` on strings.** Use `.equals()`.

---

## 3. How a `String` stores its characters

Before Java 9, a `String` held a `char[]` — 2 bytes per character, even for pure ASCII.

Since Java 9 (**compact strings**, JEP 254), it holds a `byte[]` plus a one-byte coder
flag. Strings that contain only Latin-1 characters use **1 byte per character**, halving
the memory for the vast majority of real-world strings. Everything else transparently uses
UTF-16 as before.

You never see this from the API — but it explains why upgrading from Java 8 to 9+ reduced
heap usage in many applications with no code changes at all.

---

## 4. The methods you need

### Inspecting

```java
s.length()                  // number of chars (NOT visible characters — see §6)
s.isEmpty()                 // length == 0
s.isBlank()                 // empty or only whitespace          (Java 11+)
s.charAt(i)                 // char at index i
s.indexOf("x")              // first index, or -1
s.lastIndexOf("x")          // last index, or -1
s.contains("x")             // boolean
s.startsWith("x") / endsWith("x")
```

### Comparing

```java
s.equals(other)                 // exact content
s.equalsIgnoreCase(other)       // case-insensitive
s.compareTo(other)              // <0, 0, >0 — lexicographic order
s.compareToIgnoreCase(other)
```

`compareTo` returns the difference in the first non-matching character, or the length
difference if one is a prefix of the other. Never rely on the exact number — only on its
sign.

### Transforming (all return a new string)

```java
s.toUpperCase() / toLowerCase()
s.trim()                    // removes chars <= U+0020
s.strip()                   // removes Unicode whitespace         (Java 11+ — prefer this)
s.stripLeading() / stripTrailing()
s.replace('a', 'b')         // ALL occurrences, literal
s.replace("ab", "cd")       // ALL occurrences, literal
s.replaceAll("regex", "x")  // REGEX — note the trap below
s.replaceFirst("regex", "x")
s.repeat(3)                 //                                    (Java 11+)
s.concat("x")               // same as + but rejects null
```

> **The `replace` / `replaceAll` trap.** `replace` takes a **literal**; `replaceAll` takes
> a **regular expression**. `s.replaceAll(".", "-")` replaces *every character*, because
> `.` means "any character" in regex. To replace literal dots use `s.replace(".", "-")`.

### Splitting and joining

```java
"a,b,c".split(",")                    // ["a", "b", "c"]
"a,b,c".split(",", 2)                 // ["a", "b,c"] — limit
String.join("-", "a", "b", "c")       // "a-b-c"
String.join(", ", listOfStrings)
```

`split` also takes a **regex**. Splitting on `.` or `|` requires escaping:
`s.split("\\.")`, `s.split("\\|")`.

> **`split` drops trailing empty strings.** `"a,b,,".split(",")` gives `["a", "b"]`, not
> four elements. Pass a negative limit to keep them: `split(",", -1)`.

### Substrings

```java
s.substring(3)        // from index 3 to the end
s.substring(2, 5)     // from 2 INCLUSIVE to 5 EXCLUSIVE — length 3
```

The end index being exclusive means `substring(a, b)` always has length `b - a`, which is
the useful property. Out-of-range indices throw `StringIndexOutOfBoundsException`.

---

## 5. `switch`, `equals` and `null`

```java
String input = null;

input.equals("yes")           // NullPointerException
"yes".equals(input)           // false — safe
Objects.equals(input, "yes")  // false — safe
switch (input) { ... }        // NullPointerException (unless you write `case null`)
```

Always put the literal first, or use `Objects.equals`.

---

## 6. Unicode — `length()` is not what you think

```java
"hello".length()      // 5
"héllo".length()      // 5
"😀".length()         // 2  (!)
"👨‍👩‍👧".length()      // 8  (!!)
```

`length()` returns the number of **UTF-16 code units**, not visible characters. Characters
outside the Basic Multilingual Plane (emoji, some historic scripts) need two `char`s — a
*surrogate pair*. Emoji built from joined sequences need even more.

```java
s.codePointCount(0, s.length())   // number of code points — closer to "characters"
s.chars()                          // IntStream of char values
s.codePoints()                     // IntStream of code points — usually what you want
```

For truly correct "visible character" counting you need grapheme cluster segmentation
(`java.text.BreakIterator`). In practice: if you are indexing into user-supplied text
character by character, be careful.

---

## 7. Common recipes

```java
// Reverse
new StringBuilder(s).reverse().toString();

// Palindrome check (ignoring case and non-letters)
String clean = s.toLowerCase().replaceAll("[^a-z0-9]", "");
clean.equals(new StringBuilder(clean).reverse().toString());

// Count occurrences of a character
s.chars().filter(c -> c == 'a').count();

// Title case one word
s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();

// Safe trim of a possibly-null string
String safe = (s == null) ? "" : s.strip();

// Check for a number without exceptions
s.matches("-?\\d+");
```

---

## 8. Summary

- Strings are **immutable**; every method returns a new one. Calling `s.toUpperCase()`
  without reassigning does nothing.
- Immutability buys thread safety, a cached `hashCode`, and the string pool.
- **Literals are pooled**, so `==` sometimes appears to work. Never use `==` on strings.
- `new String("x")` creates a redundant object — never write it in real code.
- The compiler **folds** constant expressions, so `"hel" + "lo"` is pooled but a runtime
  concatenation is not.
- Since Java 9, strings use **one byte per character** for Latin-1 text (compact strings).
- `replace` is literal; `replaceAll` and `split` take **regular expressions**.
- `split` drops trailing empty strings unless you pass a negative limit.
- `substring(a, b)` is inclusive–exclusive, so its length is `b - a`.
- `length()` counts **UTF-16 code units**, not visible characters — an emoji is 2.
- Put the literal first in `equals`, or use `Objects.equals`, to survive `null`.

---

**Previous:** [13 — Multidimensional arrays](13-multidimensional-arrays.md) ·
**Next:** [15 — StringBuilder and StringBuffer](15-stringbuilder-and-stringbuffer.md)
