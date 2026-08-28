# 30 · Packages and Access Modifiers

> **Run the code for this lesson**
> ```bash
> java Java/07-object-oriented-advanced/30-packages-and-access-modifiers.java
> ```
>
> **And the real multi-package project** — this lesson ships a small project with
> three genuine packages, because access rules cannot be demonstrated inside one file:
> ```bash
> cd Java/07-object-oriented-advanced/30-packages-demo
> ```
> ```bash
> javac -d out src/com/danish/library/model/*.java src/com/danish/library/service/*.java src/com/danish/app/*.java
> ```
> ```bash
> java -cp out com.danish.app.Main
> ```

---

## 1. What a package is

A package is a **namespace** backed by a **directory**.

```java
package com.danish.library.model;    // must be the FIRST statement

public class Book { }
```

That file must live at `com/danish/library/model/Book.java`. The package name and the
directory path must match exactly, or the JVM will not find the class at runtime — which
surfaces as the confusing `Could not find or load main class`.

### What packages give you

| Benefit | Detail |
| --- | --- |
| **Namespacing** | Two `Book` classes can coexist as `com.danish.Book` and `org.other.Book` |
| **Access control** | Package-private members are visible only within the package |
| **Organisation** | Related classes live together |
| **Distribution** | JARs and modules are organised by package |

### Naming convention

Reverse your domain name, lowercase, no underscores:

```
com.danish.library.model
org.apache.commons.lang3
java.util.concurrent
```

The reverse-domain convention exists to guarantee global uniqueness — nobody else owns
`danish.com`, so nobody else will produce `com.danish.*`.

The **default package** (no `package` statement) works for single files and learning — it
is what every other lesson in this repository uses — but is unusable in real projects:
classes in the default package **cannot be imported** by any packaged class.

---

## 2. The four access levels

| Modifier | Same class | Same package | Subclass, other package | Anywhere |
| --- | :---: | :---: | :---: | :---: |
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(none)* — package-private | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

Read that table as increasing visibility, and note the one asymmetry that surprises people:

> **`protected` is *wider* than package-private.** A protected member is visible to every
> class in the package **plus** every subclass anywhere. It is not "package-private plus
> subclasses in the package" — it is a genuine widening.

### What this means in practice

**`public` is a permanent promise.** Once published, removing or narrowing it breaks every
caller. Think carefully before making anything public.

**`protected` is effectively public.** Anyone in the world can write
`class Mine extends YourClass` and gain full read/write access to every `protected` member.
So a `protected` field hands strangers the ability to corrupt state your class is supposed
to be guarding. Prefer `private` fields with `protected` *methods*, if you need the
extension point at all.

**Package-private is underused.** It is the right level for classes and members that
collaborate closely but are not part of your published API. It is also the default, which
is a nice piece of language design: you have to *ask* for wider visibility.

**Top-level classes** may only be `public` or package-private — never `private` or
`protected`. There is no enclosing scope for those to mean anything in.

---

## 3. Imports

```java
import com.danish.library.model.Book;      // single-type — preferred
import com.danish.library.model.*;         // wildcard — the whole package
import static java.lang.Math.PI;           // static import of one member
import static java.lang.Math.*;            // static import of everything
```

### What imports actually do

**Nothing at runtime.** An import is purely a compile-time convenience that lets you write
`Book` instead of `com.danish.library.model.Book`. It does not load anything, and it has
no performance cost — a wildcard import of a 500-class package costs exactly the same as a
single-type import.

### Wildcard imports

They import every **public type in that package** — not sub-packages, and not anything
non-public:

```java
import java.util.*;          // does NOT import java.util.concurrent.*
```

The argument against them is **readability and collisions**, not performance:

```java
import java.util.*;
import java.awt.*;

List list;          // ERROR: reference to List is ambiguous
                    // java.util.List and java.awt.List both match
```

Fix by importing the specific one, or by writing the fully qualified name.

### `java.lang` is automatic

`String`, `Integer`, `System`, `Math`, `Object`, `Thread` and `Exception` are all in
`java.lang`, which is imported implicitly. That is why you never write
`import java.lang.String;`.

### Static imports

```java
import static java.lang.Math.max;

int biggest = max(3, 7);        // instead of Math.max(3, 7)
```

Use sparingly. `max(3, 7)` reads fine; a file full of unqualified static calls from five
different classes does not. The legitimate cases are test assertions
(`assertEquals`, `assertThat`) and heavy maths code.

---

## 4. Real project layout

Maven and Gradle both use the same convention, and effectively every Java project follows
it:

```
project/
├── src/
│   ├── main/
│   │   ├── java/          ← production source
│   │   │   └── com/danish/app/Main.java
│   │   └── resources/     ← config files, templates
│   └── test/
│       ├── java/          ← test source, MIRRORING main's packages
│       └── resources/
├── target/  (Maven)  or  build/  (Gradle)
└── pom.xml  or  build.gradle
```

Tests mirror the production package structure deliberately: a test in the *same package* as
the class it tests can reach package-private members, which lets you test internals without
making them public. That is a genuinely good reason to keep the mirroring.

Lesson 75 covers Maven and Gradle.

---

## 5. The classpath

The classpath tells the JVM where to look for classes:

```bash
java -cp out com.danish.app.Main
java -cp "out;lib/*" com.danish.app.Main       # Windows: semicolons
java -cp "out:lib/*" com.danish.app.Main       # Unix: colons
```

You pass the **fully qualified class name**, never a file path, and never with `.class`.

The three classic errors:

| Error | Cause |
| --- | --- |
| `Could not find or load main class X` | Wrong name, wrong classpath, or the package/directory do not match |
| `ClassNotFoundException` | A class was requested at runtime and not found on the classpath |
| `NoClassDefFoundError` | It was there at compile time and is missing now — **or** its static initialiser failed earlier (lesson 25) |

The second and third are different, and the distinction matters when debugging.

---

## 6. Modules — a brief orientation (Java 9+)

The **module system** (JPMS) adds a level above packages:

```java
// module-info.java, at the source root
module com.danish.library {
    exports com.danish.library.model;      // this package is visible outside
    requires java.sql;                     // we depend on this module
}
```

Packages not listed in `exports` are invisible outside the module, **even if their classes
are `public`**. This closes the long-standing hole where `public` meant "visible to
absolutely everything, forever".

The JDK itself is modularised — which is why `sun.misc.Unsafe` and other internals became
inaccessible in Java 9 and broke a great deal of code.

**Most application projects do not use modules.** They matter for library authors and for
`jlink`, which builds a minimal custom runtime image containing only the modules you
actually use. Knowing they exist and what `module-info.java` means is enough for now.

---

## 7. Practical guidance

**Start `private`.** Widen only when something outside genuinely needs it.

**Package by feature, not by layer.**

```
com.danish.app.order.OrderService        ← everything about orders, together
com.danish.app.order.OrderRepository
com.danish.app.payment.PaymentService

// rather than
com.danish.app.service.OrderService      ← every service, from every feature
com.danish.app.service.PaymentService
com.danish.app.repository.OrderRepository
```

Feature packages let you make things package-private that layer packages force you to make
public. A change to "orders" then touches one directory.

**Avoid `protected` fields.** Use `private` fields and, if you truly need an extension
point, `protected` methods.

**Keep the public surface small.** Every public type and member is a permanent commitment.

---

## 8. Summary

- A package is a namespace backed by a matching **directory path**. The `package`
  statement must be the file's first statement.
- Convention is **reverse domain name**, all lowercase. The default package is unusable in
  real projects.
- Four levels: `private` ⊂ package-private ⊂ `protected` ⊂ `public`. **`protected` is
  wider than package-private**, because it reaches subclasses in *any* package.
- **`public` is a permanent promise; `protected` is effectively public.** Avoid
  `protected` fields.
- Top-level classes may only be `public` or package-private.
- Imports are a **compile-time convenience with no runtime cost**. Wildcards are a
  readability and collision issue, not a performance one.
- `java.lang` is imported automatically.
- The classpath takes a **fully qualified class name**, never a file path.
  `ClassNotFoundException` and `NoClassDefFoundError` mean different things.
- **Modules** (Java 9+) gate whole packages with `exports`, so `public` no longer means
  "visible everywhere". Most applications do not need them.
- **Package by feature, not by layer** — it lets you keep more things package-private.

---

**Previous:** [29 — Interfaces](../06-object-oriented-core/29-interfaces.md) ·
**Next:** [31 — The `final` keyword](31-final-keyword.md)
