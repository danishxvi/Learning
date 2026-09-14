# 58 · NIO.2: `Path`, `Files` and Directory Walking

> **Run the code for this lesson**
> ```bash
> java Java/11-io-files-and-time/58-nio-files-and-paths.java
> ```

This lesson builds and tears down a real small directory tree on disk. It also reproduces
a real OS-level permission error — Windows genuinely refuses to create a symbolic link
without elevated privileges, and NIO.2 surfaces that faithfully rather than hiding it.

---

## 1. `Path`: composition, not string concatenation

```java
Path child = root.resolve("data").resolve("2024").resolve("report.txt");
child.getFileName();    // report.txt
child.getParent();      // .../data/2024
child.getNameCount();   // segment count from the root
```

```java
Path messy = root.resolve("a").resolve("..").resolve("b").resolve(".").resolve("c");
messy.normalize();      // resolves .. and . WITHOUT touching the file system
```

```java
Path absolute = child.toAbsolutePath();
absolute.getParent().relativize(child);   // "report.txt"
```

None of this touches the file system — `Path` is pure string/segment manipulation.
`java.io.File` mixed path logic and actual I/O in one class; NIO.2 splits them: `Path`
for the *structure*, `Files` for the *operations*.

---

## 2. `Files`: one-line operations, lesson 57's whole stack in one call

```java
Files.writeString(file, "Hello, NIO.2!", StandardCharsets.UTF_8);
```

That one call replaces lesson 57's entire `PrintWriter`/`BufferedWriter`/
`OutputStreamWriter`/`FileOutputStream` stack.

```java
Files.readString(file, StandardCharsets.UTF_8);   // "Hello, NIO.2!"
Files.readAllLines(file);                           // [line one, line two, line three]

Files.exists(file);         // true
Files.notExists(file);      // false
Files.isDirectory(root);    // true
Files.size(file);           // bytes, as a long

Files.copy(file, copy);
Files.move(copy, moved);
Files.delete(moved);
```

All real, all measured against an actual temp directory created for this run.

---

## 3. Walking a directory tree: a real tree, walked for real

A real tree was built:

```
tree/
├── src/main/java/App.java
├── src/test/java/AppTest.java
├── README.md
└── target/App.class
```

**`Files.walk()` returns a `Stream<Path>` that holds a real OS directory handle open** —
it must be closed, exactly like the `FileInputStream` in lesson 57, or that handle leaks.
`try`-with-resources on the stream itself is the correct pattern:

```java
try (Stream<Path> walk = Files.walk(tree)) {
    long javaFiles = walk.filter(p -> p.toString().endsWith(".java")).count();
}
```

Real result: `2` `.java` files found. Filtering out build output (`target/`) — a real,
common need:

```java
try (Stream<Path> walk = Files.walk(tree)) {
    walk.filter(Files::isRegularFile)
        .filter(p -> !p.toString().contains("target"))
        .map(tree::relativize)
        .sorted()
        .forEach(System.out::println);
}
```

Real output: `README.md`, `src\main\java\App.java`, `src\test\java\AppTest.java` — `target/App.class`
correctly excluded.

---

## 4. `BasicFileAttributes`: one system call, many answers

```java
BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
attrs.size();           // bytes
attrs.isRegularFile();  // true
attrs.isDirectory();    // false
attrs.creationTime();   // a real, measured FileTime
```

All of that came from **one** underlying OS call. The old `java.io.File` API needs a
separate system call per question (`file.length()`, `file.lastModified()`,
`file.isDirectory()`, ...) — `readAttributes` batches them, which matters for code that
checks many files (a directory scanner, a build tool).

---

## 5. NIO.2 surfaces real OS errors directly

```java
Files.createSymbolicLink(link, target);
```

Real, reproduced result on this (unprivileged) Windows process:

```
FileSystemException: A required privilege is not held by the client
```

This is not a Java limitation — it's the real Windows `SeCreateSymbolicLinkPrivilege`
requirement, surfaced faithfully by NIO.2 as a real, catchable, typed exception rather
than silently failing or masking the OS's actual reason. (On Unix-like systems, or with
Developer Mode/admin privileges on Windows, this same call succeeds normally.)

### The related, documented gotcha

`Files.exists(path)` and `Files.notExists(path)` can **both** return `false` for the same
path — when the check itself cannot be *performed* (a permission problem on a parent
directory, for example), neither method can honestly answer yes or no, and the JDK's own
Javadoc documents exactly this three-way possibility. Code that assumes
`!exists(p) == notExists(p)` is assuming away a real edge case that genuine OS-level
permission errors (like the symlink failure just reproduced) make concrete.

---

## 6. Summary

- `Path` is pure structural manipulation (`resolve`, `normalize`, `relativize`,
  `getParent`) with no file system access; `Files` performs the actual operations —
  NIO.2's deliberate split from `java.io.File`'s mixed responsibilities.
- `Files.writeString`/`readString`/`readAllLines`/`copy`/`move`/`delete` collapse what
  used to be multi-class stream stacks (lesson 57) into single calls.
- `Files.walk()` returns a `Stream<Path>` backed by a real, open OS directory handle —
  it must be used inside `try`-with-resources, exactly like any other closeable I/O
  resource.
- `Files.readAttributes` fetches size, type, and timestamps in a single OS call, instead
  of one system call per `java.io.File` method.
- NIO.2 surfaces real, OS-specific errors as typed exceptions (`FileSystemException` and
  its subtypes) rather than hiding them — reproduced here with Windows's genuine
  privilege requirement for symbolic link creation.
- `Files.exists`/`Files.notExists` are not perfect complements — both can return `false`
  when the underlying check itself cannot be performed, a real, documented edge case.

---

**Previous:** [57 — File I/O with streams and readers](57-file-io-basics.md) ·
**Next:** [59 — Serialization](59-serialization.md)
