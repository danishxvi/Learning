/* ============================================================================
 * 32 - THE Object CLASS AND ITS METHODS
 * ----------------------------------------------------------------------------
 * Companion lesson: 32-object-class-methods.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/32-object-class-methods.java
 *
 * Every class extends Object, whether you say so or not. That gives every
 * object eleven inherited methods. Knowing which to override, which to leave
 * alone, and which to avoid entirely is a large part of writing good Java.
 * ============================================================================
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class ObjectClassMethods {

    public static void main(String[] args) throws Exception {

        /* ====================================================================
         * SECTION 1 - THE ROOT OF EVERYTHING
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - EVERYTHING EXTENDS Object");
        System.out.println("=".repeat(74));

        System.out.println("    class Book { }        is exactly");
        System.out.println("    class Book extends Object { }");
        System.out.println();

        Object[] everything = {
                "a String",
                42,
                new int[]{1, 2, 3},
                List.of("a"),
                java.time.DayOfWeek.MONDAY,
                new Book("Effective Java", "Bloch")
        };

        System.out.println("  All of these are Objects:");
        for (Object item : everything) {
            System.out.printf("    %-28s superclass chain: %s%n",
                    item.getClass().getSimpleName(), chainToObject(item.getClass()));
        }

        System.out.println();
        System.out.println("  Even arrays and enums extend Object. The ONLY things that do");
        System.out.println("  not are the EIGHT PRIMITIVES - which is why `Object o = 5;`");
        System.out.println("  works only through autoboxing:");
        Object boxed = 5;
        System.out.println("    Object o = 5;  ->  actually a " + boxed.getClass().getSimpleName());


        /* ====================================================================
         * SECTION 2 - THE ELEVEN METHODS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - WHAT YOU INHERIT, AND WHAT YOU MAY CHANGE");
        System.out.println("=".repeat(74));

        // Sort so the output is stable, and skip the JVM's internal helpers
        // (registerNatives, wait0) which are not part of Object's API.
        List<Method> apiMethods = new ArrayList<>();
        for (Method method : Object.class.getDeclaredMethods()) {
            if (!method.getName().startsWith("registerNatives")
                    && !method.getName().equals("wait0")) {
                apiMethods.add(method);
            }
        }
        apiMethods.sort((left, right) -> {
            int byName = left.getName().compareTo(right.getName());
            return byName != 0 ? byName
                    : Integer.compare(left.getParameterCount(), right.getParameterCount());
        });

        int finalCount = 0;
        System.out.printf("    %-26s %-10s %s%n", "METHOD", "final?", "OVERRIDE IT?");
        for (Method method : apiMethods) {
            boolean isFinal = Modifier.isFinal(method.getModifiers());
            if (isFinal) {
                finalCount++;
            }
            System.out.printf("    %-26s %-10s %s%n",
                    signatureOf(method),
                    isFinal ? "FINAL" : "",
                    adviceFor(method.getName(), isFinal));
        }

        System.out.println();
        System.out.printf("  Counted from the class itself: %d methods, of which %d are FINAL%n",
                apiMethods.size(), finalCount);
        System.out.printf("  and %d are overridable.%n", apiMethods.size() - finalCount);
        System.out.println();
        System.out.println("  The finals are getClass, notify, notifyAll and the three wait");
        System.out.println("  overloads - all of them JVM machinery you must not redefine.");
        System.out.println("  Of the five you CAN override, toString, equals and hashCode are");
        System.out.println("  the ones you will actually write; clone and finalize you should");
        System.out.println("  not touch at all (Sections 6 and 7).");


        /* ====================================================================
         * SECTION 3 - toString()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - ALWAYS OVERRIDE toString()");
        System.out.println("=".repeat(74));

        NoToString useless = new NoToString("Danish", 25);
        Book good = new Book("Effective Java", "Bloch");

        System.out.println("  THE DEFAULT is defined as:");
        System.out.println("      getClass().getName() + \"@\" + Integer.toHexString(hashCode())");
        System.out.println();
        System.out.println("    println(objectWithoutToString) -> " + useless);
        System.out.println("      the type, an @, and an identity hash. Nothing about state.");
        System.out.println();
        System.out.println("    println(objectWithToString)    -> " + good);

        System.out.println();
        System.out.println("  It is called AUTOMATICALLY by all of these:");
        System.out.println("    System.out.println(obj)   -> " + good);
        System.out.println("    \"\" + obj                  -> " + ("" + good));
        System.out.println("    String.valueOf(obj)       -> " + String.valueOf(good));
        System.out.println("    a collection printing it  -> " + List.of(good));
        System.out.println("    String.format(\"%s\", obj)  -> " + String.format("%s", good));
        System.out.println("    ...and your debugger, and every log line you write.");

        System.out.println();
        System.out.println("  GUIDANCE:");
        System.out.println("    - include the fields that IDENTIFY the object, not all of them");
        System.out.println("    - keep it SHORT and single-line: it often appears inside a");
        System.out.println("      collection's output, as above");
        System.out.println("    - NEVER include secrets. toString ends up in logs, and logs");
        System.out.println("      end up in places you did not plan for:");
        System.out.println("        " + new SafeCredentials("danish", "hunter2"));
        System.out.println("      (the password is deliberately masked)");
        System.out.println("    - NEVER let it throw. The DEBUGGER calls toString, so a");
        System.out.println("      throwing one makes debugging dramatically harder:");

        Book withNulls = new Book(null, null);
        System.out.println("        a Book with null fields -> " + withNulls);
        System.out.println("      (guarded, so it degrades instead of exploding)");

        System.out.println();
        System.out.println("    - RECORDS generate a sensible one for free (lesson 36):");
        System.out.println("        " + new BookRecord("Clean Code", "Martin"));


        /* ====================================================================
         * SECTION 4 - equals AND hashCode, IN BRIEF
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - equals AND hashCode (SUMMARY - LESSON 33 IS FULL)");
        System.out.println("=".repeat(74));

        NoToString a = new NoToString("Danish", 25);
        NoToString b = new NoToString("Danish", 25);

        System.out.println("  The DEFAULT equals is IDENTITY - `this == other`:");
        System.out.println("    two objects with identical contents:");
        System.out.println("      a.equals(b) -> " + a.equals(b) + "   <- unequal, despite matching");

        Book bookA = new Book("Effective Java", "Bloch");
        Book bookB = new Book("Effective Java", "Bloch");
        System.out.println();
        System.out.println("  With equals AND hashCode overridden:");
        System.out.println("    bookA.equals(bookB)      -> " + bookA.equals(bookB));
        System.out.println("    same hashCode?           -> " + (bookA.hashCode() == bookB.hashCode()));
        System.out.println("    List.of(bookA).contains(bookB) -> " + List.of(bookA).contains(bookB));

        System.out.println();
        System.out.println("  THE CONTRACT: if you override one, you MUST override the other.");
        System.out.println("  Breaking that makes HashMap and HashSet behave incorrectly in");
        System.out.println("  ways that are genuinely hard to debug. Lesson 33 covers it in");
        System.out.println("  full - it is important enough to have its own lesson.");


        /* ====================================================================
         * SECTION 5 - getClass()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - getClass() ALWAYS TELLS THE TRUTH");
        System.out.println("=".repeat(74));

        Object asObject = "hello";

        System.out.println("    Object o = \"hello\";");
        System.out.println("    o.getClass()                 -> " + asObject.getClass());
        System.out.println("    o.getClass().getSimpleName() -> " + asObject.getClass().getSimpleName());
        System.out.println("    o.getClass().getName()       -> " + asObject.getClass().getName());
        System.out.println();
        System.out.println("  getClass() is FINAL. You cannot override it, and it always");
        System.out.println("  reports the RUNTIME type regardless of the declared type -");
        System.out.println("  which makes it reliable in a way instanceof deliberately is not.");

        System.out.println();
        System.out.println("  Every class has exactly ONE Class object, loaded once:");
        System.out.println("    \"a\".getClass() == \"b\".getClass() -> "
                + ("a".getClass() == "b".getClass()));
        System.out.println("    String.class == \"a\".getClass()   -> "
                + (String.class == "a".getClass()));

        System.out.println();
        System.out.println("  Arrays report their type in JVM notation:");
        System.out.println("    new int[0].getClass().getName()    -> " + new int[0].getClass().getName()
                + "     ([ = array, I = int)");
        System.out.println("    new String[0].getClass().getName() -> " + new String[0].getClass().getName());

        System.out.println();
        System.out.println("  getClass() is the entry point to REFLECTION (lesson 70), and");
        System.out.println("  the difference between it and instanceof matters when writing");
        System.out.println("  equals (lesson 33):");
        System.out.println("    \"x\" instanceof Object          -> " + ("x" instanceof Object)
                + "   accepts SUBTYPES");
        System.out.println("    \"x\".getClass() == String.class -> " + ("x".getClass() == String.class)
                + "   demands EXACTNESS");


        /* ====================================================================
         * SECTION 6 - clone() AND WHY TO AVOID IT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - clone() IS A DESIGN MISTAKE");
        System.out.println("=".repeat(74));

        ClonablePoint original = new ClonablePoint(3, 4, new ArrayList<>(List.of("tag1")));
        ClonablePoint cloned = original.clone();

        System.out.println("  It does work:");
        System.out.println("    original -> " + original);
        System.out.println("    cloned   -> " + cloned);
        System.out.println("    same object? " + (original == cloned));

        // The shallow-copy problem, demonstrated.
        cloned.getTags().add("added to the clone");
        System.out.println();
        System.out.println("  ...but Object.clone() is SHALLOW:");
        System.out.println("    cloned.getTags().add(\"added to the clone\");");
        System.out.println("    original -> " + original + "   <- changed too!");
        System.out.println("    cloned   -> " + cloned);

        System.out.println();
        System.out.println("  SIX THINGS WRONG WITH clone():");
        System.out.println("    1. clone() is PROTECTED on Object, so you must override it");
        System.out.println("       just to make it callable at all");
        System.out.println("    2. Cloneable is a MARKER INTERFACE WITH NO clone() METHOD.");
        System.out.println("       It does not give you the method - it only stops");
        System.out.println("       Object.clone() from throwing. A genuinely strange design:");
        System.out.println("         Cloneable declares " + Arrays.toString(Cloneable.class.getDeclaredMethods())
                + " methods");
        System.out.println("    3. CloneNotSupportedException is CHECKED and cannot actually");
        System.out.println("       happen once you implement Cloneable - so every");
        System.out.println("       implementation carries a pointless catch block");
        System.out.println("    4. The copy is SHALLOW, as just demonstrated");
        System.out.println("    5. It BYPASSES CONSTRUCTORS, so any validation your");
        System.out.println("       constructor enforces is simply skipped");
        System.out.println("    6. final fields cannot be reassigned inside clone()");

        System.out.println();
        System.out.println("  USE A COPY CONSTRUCTOR OR STATIC FACTORY INSTEAD:");

        SafePoint safeOriginal = new SafePoint(3, 4, new ArrayList<>(List.of("tag1")));
        SafePoint safeCopy = new SafePoint(safeOriginal);
        safeCopy.getTags().add("added to the copy");

        System.out.println("    original -> " + safeOriginal + "   <- untouched");
        System.out.println("    copy     -> " + safeCopy);
        System.out.println("    SafePoint.copyOf(p) -> " + SafePoint.copyOf(safeOriginal));
        System.out.println();
        System.out.println("    A copy constructor runs constructors, is final-friendly, has");
        System.out.println("    a clear signature, and deep-copies explicitly. Effective Java");
        System.out.println("    is unambiguous: avoid clone().");
        System.out.println();
        System.out.println("  You will still MEET it: arrays have a working clone() (lesson");
        System.out.println("  12), and some old APIs use it.");
        int[] numbers = {1, 2, 3};
        System.out.println("    new int[]{1,2,3}.clone() -> " + Arrays.toString(numbers.clone()));


        /* ====================================================================
         * SECTION 7 - finalize() - NEVER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - finalize() IS DEPRECATED FOR REMOVAL");
        System.out.println("=".repeat(74));

        System.out.println("  The idea was a destructor that ran before garbage collection.");
        System.out.println("  The reality:");
        System.out.println();
        System.out.println("    - NO GUARANTEE IT EVER RUNS. If the JVM exits first, it does not.");
        System.out.println("    - NO GUARANTEE OF WHEN. Minutes later, or never.");
        System.out.println("    - It DELAYS COLLECTION: a finalizable object survives at");
        System.out.println("      least one extra GC cycle.");
        System.out.println("    - It can RESURRECT the object by storing `this` somewhere.");
        System.out.println("    - An exception thrown inside it is SILENTLY SWALLOWED.");
        System.out.println("    - It has caused real SECURITY VULNERABILITIES - finalizer");
        System.out.println("      attacks on partially constructed objects.");
        System.out.println();
        System.out.println("  Deprecated in Java 9, and being removed.");
        System.out.println();
        System.out.println("  USE INSTEAD:");
        System.out.println("    - try-with-resources and AutoCloseable, for deterministic");
        System.out.println("      cleanup (lesson 41). This is the answer 99% of the time.");
        System.out.println("    - java.lang.ref.Cleaner, for the rare case where you need a");
        System.out.println("      safety net behind a native resource.");
        System.out.println();
        System.out.println("  Deterministic cleanup, done properly:");
        try (ManagedResource resource = new ManagedResource("database-connection")) {
            resource.use();
        }
        System.out.println("    The close() ran at a KNOWN point - the end of the try block.");


        /* ====================================================================
         * SECTION 8 - wait, notify, notifyAll
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - WHY THREAD METHODS LIVE ON Object");
        System.out.println("=".repeat(74));

        System.out.println("  They are on Object because in Java's original design");
        System.out.println("  EVERY OBJECT IS A LOCK. Any object can be waited on.");
        System.out.println();
        System.out.println("      synchronized (lock) {");
        System.out.println("          while (!condition) {      // a LOOP, never an if");
        System.out.println("              lock.wait();          // release the lock and sleep");
        System.out.println("          }");
        System.out.println("      }");
        System.out.println();
        System.out.println("      synchronized (lock) {");
        System.out.println("          condition = true;");
        System.out.println("          lock.notifyAll();         // wake the waiters");
        System.out.println("      }");

        System.out.println();
        System.out.println("  TWO RULES YOU MUST KNOW EVEN IF YOU NEVER USE THEM:");
        System.out.println();
        System.out.println("    1. They MUST be called while holding that object's monitor.");
        System.out.println("       Otherwise:");
        Object lock = new Object();
        try {
            lock.wait();       // not inside synchronized (lock)
        } catch (IllegalMonitorStateException e) {
            System.out.println("         lock.wait() outside synchronized -> "
                    + e.getClass().getSimpleName());
        }
        System.out.println();
        System.out.println("    2. wait() MUST be called in a LOOP, never an if, because of");
        System.out.println("       SPURIOUS WAKEUPS - a thread can wake without being");
        System.out.println("       notified. An `if` would then proceed on a false condition.");

        System.out.println();
        System.out.println("  MODERN CODE SHOULD NOT USE THESE. Use java.util.concurrent -");
        System.out.println("  BlockingQueue, CountDownLatch, Condition - which are safer and");
        System.out.println("  much clearer. Lesson 63.");


        /* ====================================================================
         * SECTION 9 - THE PRACTICAL SUMMARY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - WHAT TO ACTUALLY DO");
        System.out.println("=".repeat(74));

        System.out.printf("    %-16s %s%n", "toString()", "ALWAYS override. Short, no secrets, never throws.");
        System.out.printf("    %-16s %s%n", "equals()", "override when value equality matters (lesson 33)");
        System.out.printf("    %-16s %s%n", "hashCode()", "ALWAYS together with equals");
        System.out.printf("    %-16s %s%n", "getClass()", "cannot override - it is final");
        System.out.printf("    %-16s %s%n", "clone()", "NO. Write a copy constructor.");
        System.out.printf("    %-16s %s%n", "finalize()", "NEVER. Use try-with-resources.");
        System.out.printf("    %-16s %s%n", "wait/notify", "cannot override - prefer java.util.concurrent");

        System.out.println();
        System.out.println("  AND THE SHORTCUT: a RECORD generates toString, equals and");
        System.out.println("  hashCode for you, correctly, for free:");
        BookRecord recordA = new BookRecord("Effective Java", "Bloch");
        BookRecord recordB = new BookRecord("Effective Java", "Bloch");
        System.out.println("    toString  -> " + recordA);
        System.out.println("    equals    -> " + recordA.equals(recordB));
        System.out.println("    hashCode  -> " + (recordA.hashCode() == recordB.hashCode()));
        System.out.println();
        System.out.println("  For pure data carriers that is a strong argument for records");
        System.out.println("  over hand-written classes. Lesson 36.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 32.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Walks a class's superclass chain up to Object, so Section 1 can show
     * that everything really does end there.
     *
     * @param type the class to walk from
     * @return the chain, joined with arrows
     */
    static String chainToObject(Class<?> type) {
        StringBuilder chain = new StringBuilder();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (!chain.isEmpty()) {
                chain.append(" -> ");
            }
            chain.append(current.getSimpleName());
        }
        return chain.toString();
    }

    /**
     * Renders a method as name plus parameter count, for the Section 2 table.
     *
     * @param method the method
     * @return a short signature
     */
    static String signatureOf(Method method) {
        int count = method.getParameterCount();
        return method.getName() + "(" + (count == 0 ? "" : count + " arg"
                + (count == 1 ? "" : "s")) + ")";
    }

    /**
     * Gives the practical advice for each inherited method.
     *
     * @param name    the method name
     * @param isFinal whether it is final
     * @return the advice
     */
    static String adviceFor(String name, boolean isFinal) {
        if (isFinal) {
            return "cannot - it is final";
        }
        return switch (name) {
            case "toString" -> "ALMOST ALWAYS";
            case "equals"   -> "when value equality matters";
            case "hashCode" -> "ALWAYS, together with equals";
            case "clone"    -> "rarely - prefer a copy constructor";
            case "finalize" -> "NEVER - deprecated for removal";
            default         -> "";
        };
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 AND 4
// ----------------------------------------------------------------------------

/** Deliberately overrides nothing, to show the defaults. */
class NoToString {

    private final String name;
    private final int age;

    /** @param name the name  @param age the age */
    NoToString(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

/** Overrides toString, equals and hashCode - the three that matter. */
class Book {

    private final String title;
    private final String author;

    /** @param title the title  @param author the author */
    Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    /**
     * Short, single-line, identifying fields only - and GUARDED, so a null
     * field degrades instead of throwing. The debugger calls this method, so a
     * throwing toString makes debugging dramatically harder.
     */
    @Override
    public String toString() {
        return "Book[" + (title == null ? "<no title>" : title)
                + " by " + (author == null ? "<unknown>" : author) + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Book book)) return false;
        return Objects.equals(title, book.title) && Objects.equals(author, book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author);
    }
}

/** A record: toString, equals and hashCode are generated. @param title the title
 *  @param author the author */
record BookRecord(String title, String author) {}

/** Shows that toString must never leak secrets, because logs travel. */
class SafeCredentials {

    private final String username;
    private final String password;

    /** @param username the username  @param password the password */
    SafeCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String toString() {
        // NEVER include the real password. toString ends up in logs, and logs
        // end up in support tickets, aggregators and screenshots.
        return "SafeCredentials[username=" + username + ", password=********]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - clone() VERSUS A COPY CONSTRUCTOR
// ----------------------------------------------------------------------------

/**
 * Implements clone() the conventional way, which requires all six of the
 * awkward things Section 6 lists.
 */
class ClonablePoint implements Cloneable {

    private int x;
    private int y;
    private List<String> tags;

    /** @param x the x  @param y the y  @param tags mutable tags */
    ClonablePoint(int x, int y, List<String> tags) {
        this.x = x;
        this.y = y;
        this.tags = tags;
    }

    /**
     * Note everything this needs: a public override (Object's is protected),
     * a cast, and a catch for an exception that cannot happen.
     *
     * @return a SHALLOW copy - the tags list is shared
     */
    @Override
    public ClonablePoint clone() {
        try {
            // Object.clone() copies field-by-field. The `tags` REFERENCE is
            // copied, so both objects end up sharing one list.
            return (ClonablePoint) super.clone();
        } catch (CloneNotSupportedException e) {
            // Cannot happen: we implement Cloneable. Every clone() carries
            // this pointless block.
            throw new AssertionError(e);
        }
    }

    /** @return the shared tag list */
    List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ") tags=" + tags;
    }
}

/** The same data with a copy constructor instead - and a deep copy. */
class SafePoint {

    private final int x;
    private final int y;
    private final List<String> tags;

    /**
     * @param x    the x
     * @param y    the y
     * @param tags the tags; copied, not stored
     */
    SafePoint(int x, int y, List<String> tags) {
        this.x = x;
        this.y = y;
        this.tags = new ArrayList<>(tags);      // defensive copy IN
    }

    /**
     * A COPY CONSTRUCTOR. It runs the real constructor, works with final
     * fields, and deep-copies explicitly.
     *
     * @param other the point to copy
     */
    SafePoint(SafePoint other) {
        this(other.x, other.y, other.tags);
    }

    /**
     * A copy FACTORY - the same thing with a name.
     *
     * @param other the point to copy
     * @return an independent copy
     */
    static SafePoint copyOf(SafePoint other) {
        return new SafePoint(other);
    }

    /** @return the mutable tag list belonging to THIS object only */
    List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ") tags=" + tags;
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - DETERMINISTIC CLEANUP
// ----------------------------------------------------------------------------

/**
 * The replacement for finalize(): AutoCloseable plus try-with-resources, which
 * cleans up at a KNOWN point rather than whenever the collector feels like it.
 * Lesson 41 covers this properly.
 */
class ManagedResource implements AutoCloseable {

    private final String name;

    /** @param name what this resource represents */
    ManagedResource(String name) {
        this.name = name;
        System.out.println("    opened " + name);
    }

    /** Does whatever the resource is for. */
    void use() {
        System.out.println("    using " + name);
    }

    @Override
    public void close() {
        System.out.println("    closed " + name + "   <- deterministic, at the end of try");
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a toString() to NoToString. Then print a List.of(three of them) and
 *    compare the readability with the default.
 *
 * 2. Write a class with a toString() that dereferences a possibly-null field
 *    without guarding. Put one in a list, print the list, and watch a single
 *    null break the whole line.
 *
 * 3. Give ClonablePoint a deep clone() that copies the tags list too. Then
 *    count the lines and compare with SafePoint's copy constructor.
 *
 * 4. Add validation to SafePoint's constructor (x and y must be non-negative).
 *    Then try to construct an invalid one via clone() on ClonablePoint. Notice
 *    that clone() would have skipped it entirely.
 *
 * 5. Call `new Object().notify()` outside a synchronized block and read the
 *    exception. Then wrap it correctly and confirm it does not throw.
 *
 * 6. Convert Book into a record. Delete toString, equals and hashCode. Confirm
 *    every test in Section 4 still passes.
 * ============================================================================
 */
