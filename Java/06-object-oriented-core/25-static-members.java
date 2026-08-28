/* ============================================================================
 * 25 - static MEMBERS AND STATIC BLOCKS
 * ----------------------------------------------------------------------------
 * Companion lesson: 25-static-members.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/25-static-members.java
 *
 * `static` means "BELONGS TO THE CLASS, NOT TO ANY OBJECT". One word, and it
 * changes where the data lives, when it is initialised, and what code can see
 * it. Section 5 covers the dangers, which are the part usually left out.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

class StaticMembers {

    public static void main(String[] args) throws InterruptedException {

        /* ====================================================================
         * SECTION 1 - ONE COPY VERSUS ONE PER OBJECT
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - static IS SHARED, INSTANCE IS PER-OBJECT");
        System.out.println("=".repeat(74));

        System.out.println("  Before creating anything:");
        System.out.println("    Robot.totalBuilt = " + Robot.totalBuilt
                + "   <- readable with NO objects in existence");

        Robot first = new Robot("R2");
        Robot second = new Robot("C3");
        Robot third = new Robot("K9");

        System.out.println();
        System.out.println("  After building three robots:");
        System.out.println("    " + first);
        System.out.println("    " + second);
        System.out.println("    " + third);
        System.out.println();
        System.out.println("    Robot.totalBuilt = " + Robot.totalBuilt
                + "   <- ONE shared counter");
        System.out.println("    each robot's serialNumber differs  <- one field PER object");

        System.out.println();
        System.out.printf("    %-22s %-24s %s%n", "", "static MEMBER", "INSTANCE MEMBER");
        System.out.printf("    %-22s %-24s %s%n", "belongs to", "the CLASS", "each OBJECT");
        System.out.printf("    %-22s %-24s %s%n", "copies in memory", "exactly one", "one per object");
        System.out.printf("    %-22s %-24s %s%n", "created when", "the class loads", "the object is created");
        System.out.printf("    %-22s %-24s %s%n", "can access", "static members only", "static AND instance");
        System.out.printf("    %-22s %-24s %s%n", "exists with no objects", "yes", "no");


        /* ====================================================================
         * SECTION 2 - THE RULE THAT CAUSES MOST COMPILE ERRORS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - WHY A STATIC METHOD CANNOT SEE INSTANCE FIELDS");
        System.out.println("=".repeat(74));

        System.out.println("    class Example {");
        System.out.println("        int instanceField;");
        System.out.println("        static void staticMethod() {");
        System.out.println("            System.out.println(instanceField);   // ERROR");
        System.out.println("        }");
        System.out.println("    }");
        System.out.println();
        System.out.println("  error: non-static variable instanceField cannot be referenced");
        System.out.println("         from a static context");
        System.out.println();
        System.out.println("  THE REASON is not arbitrary. A static method can be called");
        System.out.println("  before ANY object exists, so there is no `this`. The question");
        System.out.println("  'which object's instanceField?' has no answer.");
        System.out.println();
        System.out.println("  The reverse is ALWAYS fine - an instance method can read static");
        System.out.println("  members, because if an object exists the class is certainly loaded:");
        System.out.println("    first.describeUsingBoth() -> " + first.describeUsingBoth());

        System.out.println();
        System.out.println("  This is also exactly why main is static: the JVM must call it");
        System.out.println("  BEFORE any object of your class exists. If main were an instance");
        System.out.println("  method, the JVM would have to construct your class first - and it");
        System.out.println("  has no idea which constructor to use or what to pass it.");


        /* ====================================================================
         * SECTION 3 - HOW TO ACCESS STATIC MEMBERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - ALWAYS USE THE CLASS NAME");
        System.out.println("=".repeat(74));

        System.out.println("    Robot.totalBuilt  -> " + Robot.totalBuilt + "   CORRECT");
        System.out.println("    first.totalBuilt  -> " + first.totalBuilt
                + "   legal, but reads as if it were per-object");

        // The genuinely surprising one.
        Robot nothing = null;
        System.out.println();
        System.out.println("  And the piece of trivia that convinces people to stop:");
        System.out.println("    Robot nothing = null;");
        System.out.println("    nothing.totalBuilt -> " + nothing.totalBuilt + "   NO NullPointerException!");
        System.out.println();
        System.out.println("  The reference is never dereferenced. The compiler resolved it");
        System.out.println("  to Robot.totalBuilt at COMPILE time and discarded the variable");
        System.out.println("  entirely. Which is a good reason never to write it.");


        /* ====================================================================
         * SECTION 4 - LEGITIMATE USES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHAT static IS ACTUALLY FOR");
        System.out.println("=".repeat(74));

        System.out.println("  1. CONSTANTS");
        System.out.println("       MathConstants.PI            = " + MathConstants.PI);
        System.out.println("       MathConstants.MAX_RETRIES   = " + MathConstants.MAX_RETRIES);
        System.out.println("     A static final COMPILE-TIME constant is inlined by the");
        System.out.println("     compiler - there is no runtime lookup at all.");

        System.out.println();
        System.out.println("  2. UTILITY METHODS WITH NO STATE");
        System.out.println("       StringUtils.reverse(\"Danish\")     = " + StringUtils.reverse("Danish"));
        System.out.println("       StringUtils.isPalindrome(\"level\") = " + StringUtils.isPalindrome("level"));
        System.out.println("       Math.max(3, 7)                    = " + Math.max(3, 7));
        System.out.println();
        System.out.println("     A utility class should be final with a PRIVATE constructor,");
        System.out.println("     so nobody can instantiate something with no instance state:");
        System.out.println("       new StringUtils()  ->  COMPILE ERROR (private constructor)");

        System.out.println();
        System.out.println("  3. COUNTERS AND CACHES SHARED BY ALL INSTANCES");
        System.out.println("       Robot.totalBuilt = " + Robot.totalBuilt);

        System.out.println();
        System.out.println("  4. FACTORY METHODS (lesson 22)");


        /* ====================================================================
         * SECTION 5 - THE DANGERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - MUTABLE STATIC STATE IS A GLOBAL VARIABLE");
        System.out.println("=".repeat(74));

        System.out.println("  DANGER 1 - anyone, anywhere, can change it.");
        System.out.println("    GlobalState.messages -> " + GlobalState.messages);
        GlobalState.messages.add("added from main");
        addFromSomewhereElse();
        System.out.println("    after two unrelated pieces of code touched it -> "
                + GlobalState.messages);
        System.out.println();
        System.out.println("    This makes TESTS ORDER-DEPENDENT: state leaks from one test");
        System.out.println("    into the next. It is exactly what object orientation was");
        System.out.println("    invented to avoid.");

        System.out.println();
        System.out.println("  DANGER 2 - it is NOT thread-safe. counter++ is THREE operations:");
        System.out.println("    read, increment, write. Two threads can collide.");
        System.out.println();

        int threads = 8;
        int incrementsEach = 100_000;
        int expected = threads * incrementsEach;

        UnsafeCounter.reset();
        runConcurrently(threads, incrementsEach, UnsafeCounter::increment);
        AtomicCounter.reset();
        runConcurrently(threads, incrementsEach, AtomicCounter::increment);

        System.out.printf("    %d threads x %,d increments, expected %,d:%n",
                threads, incrementsEach, expected);
        System.out.printf("      plain  static int counter++ -> %,d   %s%n",
                UnsafeCounter.get(),
                UnsafeCounter.get() == expected ? "(got lucky this run)" : "<- INCREMENTS LOST");
        System.out.printf("      static AtomicInteger        -> %,d   correct%n",
                AtomicCounter.get());
        System.out.println();
        System.out.println("    Run it a few times - the unsafe number changes every time.");
        System.out.println("    Lesson 63 covers this properly.");

        System.out.println();
        System.out.println("  DANGER 3 - static final on a collection is NOT immutability.");
        System.out.println("    GlobalState.NAMES -> " + GlobalState.NAMES);
        GlobalState.NAMES.add("still mutable");
        System.out.println("    after .add(...)   -> " + GlobalState.NAMES);
        System.out.println("    `final` stops REASSIGNMENT, never MUTATION (lesson 31).");
        System.out.println("    Use List.of(...) for a genuinely immutable constant.");

        System.out.println();
        System.out.println("  DANGER 4 - statics are NEVER garbage collected while the class");
        System.out.println("    is loaded. A static Map that only ever grows is a memory leak");
        System.out.println("    with no obvious owner, and nothing will ever reclaim it.");


        /* ====================================================================
         * SECTION 6 - STATIC BLOCKS AND CLASS INITIALISATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - WHEN DOES A CLASS INITIALISE?");
        System.out.println("=".repeat(74));

        System.out.println("  Declaring a variable of a type does NOT load it:");
        LazyClass notYetLoaded;                     // no initialisation
        System.out.println("    LazyClass notYetLoaded;   -> (nothing printed above)");

        System.out.println();
        System.out.println("  Reading a static final COMPILE-TIME CONSTANT does NOT either:");
        System.out.println("    LazyClass.COMPILE_TIME_CONSTANT = " + LazyClass.COMPILE_TIME_CONSTANT);
        System.out.println("    (still nothing - the compiler INLINED the value, so the");
        System.out.println("     class was never touched at runtime)");

        System.out.println();
        System.out.println("  Touching a real static member DOES initialise it:");
        System.out.println("    LazyClass.RUNTIME_VALUE = " + LazyClass.RUNTIME_VALUE);

        System.out.println();
        System.out.println("  A class initialises on FIRST ACTIVE USE:");
        System.out.println("    - creating an instance");
        System.out.println("    - calling a static method");
        System.out.println("    - reading or writing a NON-CONSTANT static field");
        System.out.println("  ...and exactly once, ever.");

        System.out.println();
        System.out.println("  A useful static block - building a lookup table:");
        System.out.println("    HttpStatus.describe(200) = " + HttpStatus.describe(200));
        System.out.println("    HttpStatus.describe(404) = " + HttpStatus.describe(404));
        System.out.println("    HttpStatus.describe(999) = " + HttpStatus.describe(999));

        /* --------------------------------------------------------------------
         * CLASS INITIALISATION IS THREAD-SAFE FOR FREE. The JVM guarantees a
         * class is initialised exactly once, with proper locking. That is what
         * makes the holder idiom the cleanest lazy singleton.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE JVM GUARANTEES A CLASS INITIALISES EXACTLY ONCE, with");
        System.out.println("  proper locking. That gives you a lazy singleton for free:");
        System.out.println();
        System.out.println("    Singleton.getInstance() -> " + Singleton.getInstance());
        System.out.println("    Singleton.getInstance() -> " + Singleton.getInstance());
        System.out.println("    same object? "
                + (Singleton.getInstance() == Singleton.getInstance()));
        System.out.println();
        System.out.println("  The Holder class loads on the FIRST call to getInstance() and");
        System.out.println("  never again. No synchronized, no volatile, no double-checked");
        System.out.println("  locking - the JVM's class-initialisation lock does all of it.");

        System.out.println();
        System.out.println("  IF A STATIC INITIALISER THROWS:");
        try {
            BrokenInitialiser.use();
        } catch (Throwable t) {
            System.out.println("    first use  -> " + t.getClass().getSimpleName()
                    + ": caused by " + t.getCause().getClass().getSimpleName());
        }
        try {
            BrokenInitialiser.use();
        } catch (Throwable t) {
            System.out.println("    second use -> " + t.getClass().getSimpleName()
                    + "   <- and now the original cause is GONE");
        }
        System.out.println();
        System.out.println("    The class is marked erroneous PERMANENTLY. Every later use");
        System.out.println("    throws NoClassDefFoundError, usually with no hint of what");
        System.out.println("    actually went wrong. Keep static initialisers simple, and");
        System.out.println("    never let them do risky I/O.");


        /* ====================================================================
         * SECTION 7 - STATIC METHODS ARE HIDDEN, NOT OVERRIDDEN
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - STATIC METHODS CANNOT BE OVERRIDDEN");
        System.out.println("=".repeat(74));

        Parent viaParentReference = new Child();

        System.out.print("  STATIC   - Parent p = new Child(); p.greet()    -> ");
        viaParentReference.greet();
        System.out.print("  INSTANCE - Parent p = new Child(); p.describe() -> ");
        viaParentReference.describe();

        System.out.println();
        System.out.println("  The static method printed \"Parent\" and the instance method");
        System.out.println("  printed \"Child\", from the SAME reference.");
        System.out.println();
        System.out.println("  That is HIDING, not overriding. Static methods bind at COMPILE");
        System.out.println("  time using the DECLARED type - exactly like overloading");
        System.out.println("  (lesson 18) and unlike overriding (lesson 27).");
        System.out.println();
        System.out.println("  Putting @Override on a static method is a compile error, which");
        System.out.println("  is the compiler telling you the distinction is real.");
        System.out.println();
        System.out.println("  RULE: never call a static method through an instance, and never");
        System.out.println("  hide one in a subclass. Both read as polymorphism and are not.");


        /* ====================================================================
         * SECTION 8 - WHEN TO USE static
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - THE DECISION");
        System.out.println("=".repeat(74));

        System.out.println("  USE static WHEN:");
        System.out.println("    - the method uses no instance state (make it static and SAY so)");
        System.out.println("    - you need a genuine constant  -> static final");
        System.out.println("    - you are writing a utility class (all static, private ctor)");
        System.out.println("    - you need a factory method (lesson 22)");
        System.out.println("    - a nested class does not need its outer instance");
        System.out.println();
        System.out.println("  AVOID static WHEN:");
        System.out.println("    - the state is mutable and shared -> that is a global variable");
        System.out.println("    - it makes testing hard: static state cannot be mocked or");
        System.out.println("      reset easily, and leaks between test methods");
        System.out.println("    - you are reaching for it to avoid passing a dependency.");
        System.out.println("      That is a design problem, and it is precisely why");
        System.out.println("      dependency injection frameworks exist.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 25.");
        System.out.println("=".repeat(74));
    }

    /** Mutates the global list from a completely unrelated place, to make the
     *  point that static state has no owner. */
    static void addFromSomewhereElse() {
        GlobalState.messages.add("added from an unrelated method");
    }

    /**
     * Runs an action concurrently on several threads, so a race condition has a
     * real chance to appear.
     *
     * @param threadCount    how many threads to run
     * @param iterationsEach how many times each thread runs the action
     * @param action         the work to repeat
     * @throws InterruptedException if interrupted while waiting
     */
    static void runConcurrently(int threadCount, int iterationsEach, Runnable action)
            throws InterruptedException {

        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startGate.await();            // all threads start together
                    for (int j = 0; j < iterationsEach; j++) {
                        action.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finished.countDown();
                }
            }).start();
        }

        startGate.countDown();
        finished.await();
    }
}

// ----------------------------------------------------------------------------
// SECTION 1 AND 2
// ----------------------------------------------------------------------------

/** One shared counter, one serial number per object. */
class Robot {

    /** ONE copy, shared by every Robot ever made. */
    static int totalBuilt = 0;

    /** One copy PER robot. */
    private final int serialNumber;
    private final String name;

    /** @param name this robot's name */
    Robot(String name) {
        totalBuilt++;                 // touches the shared counter
        this.serialNumber = totalBuilt;
        this.name = name;
    }

    /**
     * An INSTANCE method may freely read static members - if an object exists,
     * the class is certainly loaded.
     *
     * @return a description using both kinds of member
     */
    String describeUsingBoth() {
        return name + " is robot " + serialNumber + " of " + totalBuilt + " built so far";
    }

    // static void broken() { System.out.println(name); }
    //   ERROR: non-static variable name cannot be referenced from a static
    //   context. There is no `this`, so there is no name to print.

    @Override
    public String toString() {
        return "Robot[" + name + ", serial=" + serialNumber + "]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - LEGITIMATE USES
// ----------------------------------------------------------------------------

/** Compile-time constants. These are inlined by the compiler. */
final class MathConstants {

    /** @see Math#PI for the real one; this is here to be a constant. */
    public static final double PI = 3.14159;

    /** A genuine constant: an int cannot be mutated. */
    public static final int MAX_RETRIES = 3;

    private MathConstants() {
        throw new AssertionError("no instances");
    }
}

/**
 * A utility class: final, with a private constructor, holding only static
 * methods. There is no instance state, so there should be no instances.
 */
final class StringUtils {

    /** Prevents instantiation, and prevents reflection from doing it either. */
    private StringUtils() {
        throw new AssertionError("no instances of StringUtils");
    }

    /**
     * @param input the text to reverse
     * @return the reversed text
     */
    static String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * @param input the text to test
     * @return true if it reads the same both ways, ignoring case and non-letters
     */
    static boolean isPalindrome(String input) {
        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9]", "");
        return cleaned.equals(reverse(cleaned));
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - THE DANGERS
// ----------------------------------------------------------------------------

/** Mutable static state, i.e. global variables with a class name in front. */
class GlobalState {

    /** Any code anywhere can read or modify this. */
    public static List<String> messages = new ArrayList<>(List.of("initial"));

    /** `final` stops reassignment; the CONTENTS are still wide open. */
    public static final List<String> NAMES = new ArrayList<>(List.of("Danish"));
}

/** A static counter incremented without synchronisation. */
class UnsafeCounter {

    private static int counter = 0;

    /** NOT atomic: read, increment, write. Two threads can lose an update. */
    static void increment() {
        counter++;
    }

    /** @return the current value */
    static int get() {
        return counter;
    }

    /** Resets between runs. */
    static void reset() {
        counter = 0;
    }
}

/** The same counter done correctly. */
class AtomicCounter {

    private static final AtomicInteger counter = new AtomicInteger();

    /** Atomic: the read-modify-write happens as one indivisible operation. */
    static void increment() {
        counter.incrementAndGet();
    }

    /** @return the current value */
    static int get() {
        return counter.get();
    }

    /** Resets between runs. */
    static void reset() {
        counter.set(0);
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - CLASS INITIALISATION
// ----------------------------------------------------------------------------

/** Prints when it initialises, so you can see exactly what triggers it. */
class LazyClass {

    /**
     * A COMPILE-TIME CONSTANT. Reading it does NOT initialise the class,
     * because javac inlines the literal value into the caller's bytecode.
     */
    static final int COMPILE_TIME_CONSTANT = 42;

    /**
     * Not a compile-time constant - the value comes from a method call, so it
     * must be computed at runtime, which forces initialisation.
     */
    static final int RUNTIME_VALUE = computeValue();

    static {
        System.out.println("    >>> LazyClass static block ran (the class just initialised)");
    }

    private static int computeValue() {
        System.out.println("    >>> LazyClass.computeValue() ran");
        return 99;
    }
}

/** Builds a lookup table in a static block - a genuinely good use of one. */
class HttpStatus {

    private static final Map<Integer, String> DESCRIPTIONS;

    static {
        // Several statements are needed, which is exactly when a static block
        // beats a field initialiser. This runs ONCE, before any call below.
        DESCRIPTIONS = new HashMap<>();
        DESCRIPTIONS.put(200, "OK");
        DESCRIPTIONS.put(201, "Created");
        DESCRIPTIONS.put(301, "Moved Permanently");
        DESCRIPTIONS.put(400, "Bad Request");
        DESCRIPTIONS.put(401, "Unauthorized");
        DESCRIPTIONS.put(404, "Not Found");
        DESCRIPTIONS.put(500, "Internal Server Error");
    }

    /**
     * @param code an HTTP status code
     * @return its description, or "Unknown" if not recognised
     */
    static String describe(int code) {
        return DESCRIPTIONS.getOrDefault(code, "Unknown");
    }
}

/**
 * The initialization-on-demand holder idiom: a lazy, thread-safe singleton
 * with no synchronisation anywhere, relying entirely on the JVM's guarantee
 * that a class initialises exactly once.
 */
class Singleton {

    private Singleton() {
    }

    /**
     * A private static nested class. It is not loaded until it is first USED,
     * which happens on the first call to getInstance().
     */
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }

    /** @return the single instance, created lazily on first call */
    static Singleton getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public String toString() {
        return "Singleton@" + Integer.toHexString(System.identityHashCode(this));
    }
}

/** A class whose static initialiser fails, to show the permanent damage. */
class BrokenInitialiser {

    static final int VALUE;

    static {
        // Something that fails during class initialisation - a missing config
        // file, a bad system property, a null in a lookup table.
        if (true) {
            throw new IllegalStateException("configuration missing");
        }
        VALUE = 1;
    }

    /** Touching the class triggers initialisation, and therefore the failure. */
    static void use() {
        System.out.println(VALUE);
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - HIDING VS OVERRIDING
// ----------------------------------------------------------------------------

/** The superclass, with one static and one instance method. */
class Parent {

    /** Static: bound at COMPILE time by the declared type. */
    static void greet() {
        System.out.println("Parent.greet()   [static - chosen by the DECLARED type]");
    }

    /** Instance: bound at RUNTIME by the actual object. */
    void describe() {
        System.out.println("Parent.describe() [instance]");
    }
}

/** HIDES the static method and OVERRIDES the instance method. */
class Child extends Parent {

    /**
     * This HIDES Parent.greet(); it does not override it. Adding @Override
     * here would be a compile error.
     */
    static void greet() {
        System.out.println("Child.greet()    [never reached through a Parent reference]");
    }

    @Override
    void describe() {
        System.out.println("Child.describe()  [instance - chosen by the RUNTIME type]");
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Run Section 5's thread test five times. Record the unsafe counter each
 *    time. Then explain why the number is different but always <= expected,
 *    never greater.
 *
 * 2. Add a static method to Robot that tries to print `name`. Read the exact
 *    compiler error, then fix it two ways: make it an instance method, or pass
 *    a Robot as a parameter.
 *
 * 3. Change LazyClass.RUNTIME_VALUE to a plain literal (`= 99`). Re-run and
 *    note that the class no longer initialises when you read it. Explain why.
 *
 * 4. Make GlobalState.NAMES genuinely immutable. Then find every line in this
 *    file that stops compiling, and decide whether that is a good thing.
 *
 * 5. Write a Registry class with a static Map that never removes entries. Add
 *    a million entries and watch the heap with -verbose:gc. You have written a
 *    memory leak with no owner.
 *
 * 6. Try to add @Override to Child.greet(). Read the error. Then explain to
 *    someone why Java allows hiding at all, given how confusing it is.
 * ============================================================================
 */
