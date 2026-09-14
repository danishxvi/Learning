# 57 · File I/O with Streams and Readers

> **Run the code for this lesson**
> ```bash
> java Java/11-io-files-and-time/57-file-io-basics.java
> ```

This lesson touches the real file system — every file it creates is a real temp file, and
every one is deleted when it finishes. It reproduces a real, classic encoding bug, and
measures buffering's real cost.

---

## 1. Byte streams: raw bytes, no encoding involved at all

```java
try (FileOutputStream out = new FileOutputStream(file)) {
    out.write(65);                        // ONE byte - 'A'
    out.write(new byte[]{66, 67, 68});    // 'B', 'C', 'D'
}
try (FileInputStream in = new FileInputStream(file)) {
    int b;
    while ((b = in.read()) != -1) { ... }
}
```

Real result: `file.length()` on disk is exactly `4` bytes — one byte per ASCII character,
no encoding overhead at all. `InputStream`/`OutputStream` work in raw bytes — they know
nothing about characters, text, or encoding. Writing text through them means encoding it
to bytes yourself (`String.getBytes(charset)`).

---

## 2. Character streams, and the real default-encoding bug

```java
try (FileWriter writer = new FileWriter(charFile)) { writer.write("Hello, World!"); }
try (FileReader reader = new FileReader(charFile)) { ... }
// round-trips fine - "Hello, World!"
```

**The real problem**: `FileWriter`/`FileReader` use `Charset.defaultCharset()` —
`UTF-8` on this machine — **silently**, with no way to override it on the constructors
themselves. A different machine, a different default JVM charset (this varies by OS and
locale), and the *same code* can read back different characters for the same file.

### Reproducing the bug, verified by codepoint

A terminal's own encoding is a third, independent variable that has nothing to do with
whether a read actually matched a write — it can make correct output *look* wrong, or
corrupted output *look* right, depending entirely on how that one terminal happens to
decode bytes. So this reproduction is verified programmatically (length and codepoints),
not by how anything looks printed:

```java
String original = "café";
// write it as UTF-8, then read it back two ways:
```

Real, measured result:

```
original:                    length=4, codepoints=[U+0063, U+0061, U+0066, U+00E9]
read as ISO-8859-1 (WRONG):  length=5, codepoints=[U+0063, U+0061, U+0066, U+00C3, U+00A9], equals original? false
read as UTF-8       (right): length=4, codepoints=[U+0063, U+0061, U+0066, U+00E9], equals original? true
```

The wrong-charset read came back as a **different length** (5 characters, not 4) and
different codepoints — the single 2-byte UTF-8 character `é` (U+00E9) was misread as two
separate Latin-1 characters (U+00C3, U+00A9). That mismatch is real and unambiguous,
independent of how any particular terminal chooses to *display* either string.

**The fix**: never rely on the platform default. Use `InputStreamReader`/
`OutputStreamWriter` with an explicit `java.nio.charset.StandardCharsets` constant
(almost always `UTF_8`), not the bare `FileReader`/`FileWriter` constructors.

---

## 3. Buffering: measured, not asserted

A generated real file, 50,000 lines, read two ways:

```java
// UNBUFFERED - one character at a time
try (FileReader reader = new FileReader(file)) {
    int c;
    while ((c = reader.read()) != -1) { ... }
}

// BUFFERED
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    String line;
    while ((line = reader.readLine()) != null) { ... }
}
```

Real, measured result:

| Approach | Time |
| --- | --- |
| Unbuffered (`read()` per character) | 265 ms |
| Buffered (`BufferedReader.readLine()`) | 27 ms |

Every unbuffered `reader.read()` call is a potential real disk/OS read — the OS and
`FileReader` do some of their own buffering, but crossing the Java-to-native boundary one
character at a time still costs real, measurable overhead per call.
`BufferedReader` reads a large chunk into memory once, then serves reads from that chunk —
always wrap a raw `FileReader`/`FileWriter` in a `Buffered-` version for anything beyond
a trivial, tiny file.

---

## 4. `PrintWriter`: the convenient way to write text

```java
try (PrintWriter writer = new PrintWriter(
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))) {
    writer.println("plain line");
    writer.printf("formatted: %d items, %.2f%% done%n", 42, 87.5);
    writer.write("write() still works too, no newline");
}
```

`PrintWriter` wraps *any* `Writer` and adds `println`/`print`/`printf` on top. This exact
stack — `PrintWriter` → `BufferedWriter` → `OutputStreamWriter` → `FileOutputStream`, with
an explicit charset — is the standard, correct way to write text to a file: buffered,
encoding-explicit, and convenient to call. Lesson 58 shows NIO.2's
`Files.newBufferedWriter` shortcut for the same underlying stack.

---

## 5. Summary

- `InputStream`/`OutputStream` move raw bytes with no concept of characters or encoding;
  `Reader`/`Writer` move characters, which means an encoding is always involved, whether
  stated explicitly or not.
- `FileReader`/`FileWriter` silently use `Charset.defaultCharset()` — a real, portable
  bug waiting to happen the moment code runs on a machine with a different default.
  Always use `InputStreamReader`/`OutputStreamWriter` with an explicit
  `StandardCharsets` constant instead.
- A real charset mismatch was reproduced and verified by codepoint and length — never by
  how the output happened to render on one particular terminal, which is an independent,
  unrelated variable.
- Buffering is a measured, large real difference — 265ms vs 27ms reading the same 50,000
  lines one way vs the other. Always wrap raw file streams in their `Buffered-` versions.
- `PrintWriter` layered over a `BufferedWriter`/`OutputStreamWriter`/`FileOutputStream`
  stack, with an explicit charset, is the standard idiom for writing text files correctly.

---

**Previous:** [56 — `Optional`](../10-functional-java/56-optional.md) ·
**Next:** [58 — NIO.2: `Path`, `Files` and directory walking](58-nio-files-and-paths.md)
