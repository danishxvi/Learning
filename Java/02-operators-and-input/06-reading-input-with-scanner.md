# 06 · Reading User Input with `Scanner`

> **Run the code for this lesson**
> ```bash
> java Java/02-operators-and-input/06-reading-input-with-scanner.java
> ```
> This program **waits for you to type**. It also runs in a non-interactive mode
> automatically if it detects no console, so it never hangs.

Reading input looks trivial and is full of traps. This lesson covers `Scanner`
thoroughly, then shows the two alternatives you will meet in real code.

---

## 1. Creating a `Scanner`

```java
import java.util.Scanner;          // Scanner is in java.util, so it needs an import

Scanner scanner = new Scanner(System.in);
```

`System.in` is the standard input stream — usually your keyboard. `Scanner` wraps it and
adds methods that parse the raw bytes into useful types.

A `Scanner` can read from many sources, not just the keyboard:

```java
new Scanner(System.in)                     // the keyboard
new Scanner("42 hello true")               // a String
new Scanner(new File("data.txt"))          // a file (throws FileNotFoundException)
new Scanner(Path.of("data.txt"))           // a path (Java 10+)
```

Being able to point a `Scanner` at a `String` is enormously useful for testing — you can
exercise input-parsing logic without a human typing anything.

---

## 2. The reading methods

| Method | Reads | Returns |
| --- | --- | --- |
| `next()` | One **token** (up to whitespace) | `String` |
| `nextLine()` | The **rest of the current line** | `String` |
| `nextInt()` | One token, parsed | `int` |
| `nextLong()` | One token, parsed | `long` |
| `nextDouble()` | One token, parsed | `double` |
| `nextFloat()` | One token, parsed | `float` |
| `nextBoolean()` | One token (`true`/`false`, case-insensitive) | `boolean` |
| `nextByte()` / `nextShort()` | One token, parsed | `byte` / `short` |

There is deliberately **no `nextChar()`**. To read a single character:

```java
char c = scanner.next().charAt(0);
```

### The corresponding `hasNextX()` methods

Every `nextX()` has a `hasNextX()` partner that returns `true` if the *next* token can be
read as that type — **without consuming it**:

```java
if (scanner.hasNextInt()) {
    int value = scanner.nextInt();
} else {
    System.out.println("That was not a number");
    scanner.next();     // consume and discard the bad token
}
```

This is the correct way to validate input. It is far better than catching exceptions,
because it never puts the scanner into a broken state.

---

## 3. The `nextInt()` / `nextLine()` trap

**This is the single most famous `Scanner` bug, and everyone hits it once.**

```java
System.out.print("Age: ");
int age = scanner.nextInt();

System.out.print("Name: ");
String name = scanner.nextLine();     // returns "" immediately, never waits!
```

You type `25`, press Enter, and the program prints `Name:` and then instantly moves on
without letting you type.

### Why it happens

When you type `25` and press Enter, the input buffer contains:

```
2 5 \n
```

- `nextInt()` reads the token `25` and **stops**. It leaves the `\n` in the buffer.
- `nextLine()` then reads "everything up to the next newline" — and the very next
  character *is* a newline. So it returns an empty string immediately.

**Every `nextX()` except `nextLine()` leaves the trailing newline behind.**

### The three fixes

**Fix 1 — consume the leftover newline (most common):**

```java
int age = scanner.nextInt();
scanner.nextLine();               // throw away the rest of the line
String name = scanner.nextLine();
```

**Fix 2 — read everything as lines and parse yourself (most robust):**

```java
int age = Integer.parseInt(scanner.nextLine().trim());
String name = scanner.nextLine();
```

This is the approach professionals prefer: one method, one mental model, no leftovers.

**Fix 3 — never mix token methods and line methods in the same program.** Pick one style
and stay with it.

---

## 4. Handling bad input

`nextInt()` on non-numeric input throws `InputMismatchException` — and, crucially, **it
does not consume the bad token**. Naive retry loops therefore spin forever:

```java
while (true) {
    try {
        return scanner.nextInt();     // INFINITE LOOP on bad input
    } catch (InputMismatchException e) {
        System.out.println("Try again");
        // the bad token is still sitting there!
    }
}
```

Fix it by consuming the offending token:

```java
} catch (InputMismatchException e) {
    scanner.next();     // discard the bad token
    System.out.println("Try again");
}
```

Better still, avoid the exception entirely with `hasNextInt()`:

```java
while (!scanner.hasNextInt()) {
    System.out.println("Numbers only, please.");
    scanner.next();
}
int value = scanner.nextInt();
```

### The other exceptions

| Exception | When |
| --- | --- |
| `InputMismatchException` | Token exists but is the wrong type |
| `NoSuchElementException` | No input left at all (end of stream / Ctrl+D / Ctrl+Z) |
| `IllegalStateException` | You called a method after `close()` |

`NoSuchElementException` is why a program that reads in a loop must check `hasNext()`
before every read when input might run out.

---

## 5. Locale — the trap that only appears on other people's machines

`Scanner` parses numbers using the **default locale**. In much of Europe the decimal
separator is a comma:

```java
scanner.nextDouble()   // "3.14" fails in a German locale; "3,14" is expected there
```

Your code works on your laptop and fails on a colleague's. Pin the locale when the input
format is fixed (files, protocols, test data):

```java
Scanner scanner = new Scanner(System.in).useLocale(Locale.US);
```

For *user* input, honouring the user's locale is correct. For *machine* input, always pin
it.

---

## 6. Closing a `Scanner` — and why you usually should not

`Scanner` implements `Closeable`, and IDEs warn you about the "resource leak". But:

> **Closing a `Scanner` wrapped around `System.in` closes `System.in` itself — permanently,
> for the whole JVM.** Any later `new Scanner(System.in)` will throw
> `NoSuchElementException`.

Guidance:

- **`System.in`** — do not close it. It is closed by the JVM at exit. Create **one**
  `Scanner` for your whole program and pass it around.
- **Files and other streams** — always close, ideally with try-with-resources:

```java
try (Scanner fileScanner = new Scanner(new File("data.txt"))) {
    while (fileScanner.hasNextLine()) {
        System.out.println(fileScanner.nextLine());
    }
}   // closed automatically, even if an exception is thrown
```

Lesson 41 covers try-with-resources properly.

---

## 7. The alternatives

### `BufferedReader` — the fast one

```java
BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
String line = reader.readLine();
int number = Integer.parseInt(line.trim());
```

| | `Scanner` | `BufferedReader` |
| --- | --- | --- |
| Parses types | Yes (`nextInt`, …) | No — you parse manually |
| Speed | Slower | **Much faster** (bigger buffer, no regex) |
| Thread-safe | No | Yes (synchronized) |
| Exceptions | Unchecked | **Checked** `IOException` |
| Best for | Learning, small input | Large input, competitive programming |

`Scanner` uses regular expressions internally, which is why it is slow. For reading
100,000 lines, `BufferedReader` can be several times faster.

### `System.console()` — for passwords

```java
Console console = System.console();
char[] password = console.readPassword("Password: ");   // input is NOT echoed
```

Returns `char[]`, not `String`, so you can zero it out after use — a `String` would sit
in the string pool until garbage collected. **`System.console()` returns `null` when
output is redirected or inside most IDEs**, so always null-check it.

---

## 8. Useful `Scanner` extras

```java
scanner.useDelimiter(",");        // parse CSV: tokens split on commas
scanner.hasNext()                 // any token left at all?
scanner.hasNextLine()             // any line left?
scanner.skip("\\s*");             // skip matching input without returning it
scanner.tokens()                  // Stream<String> of remaining tokens (Java 9+)
scanner.findInLine("\\d+")        // regex search within the current line
```

`useDelimiter` turns `Scanner` into a serviceable CSV reader for simple cases:

```java
Scanner csv = new Scanner("Danish,25,Bengaluru").useDelimiter(",");
String name = csv.next();
int age = csv.nextInt();
String city = csv.next();
```

---

## 9. Summary

- `new Scanner(System.in)` needs `import java.util.Scanner;`.
- `next()` reads one token; `nextLine()` reads the rest of the line.
- **`nextInt()` leaves the newline behind, so the following `nextLine()` returns `""`.**
  Fix with an extra `nextLine()`, or read everything as lines and parse yourself.
- Validate with `hasNextInt()` rather than catching `InputMismatchException`; a naive
  catch loop spins forever because the bad token is never consumed.
- `Scanner` parses numbers using the default **locale** — pin it with `useLocale` for
  machine-format input.
- **Never close a `Scanner` wrapped around `System.in`** — it closes `System.in` for the
  entire JVM. Do close file-based scanners, with try-with-resources.
- Use `BufferedReader` when input is large; use `System.console().readPassword()` for
  secrets, and null-check the console.

---

**Previous:** [05 — Operators](05-operators.md) ·
**Next:** [07 — Output and string formatting](07-output-and-string-formatting.md)
