/* ============================================================================
 * 34 - INNER, STATIC NESTED, LOCAL AND ANONYMOUS CLASSES
 * ----------------------------------------------------------------------------
 * Companion lesson: 34-inner-and-anonymous-classes.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/34-inner-and-anonymous-classes.java
 *
 * Java has FOUR kinds of nested class. They look similar and behave very
 * differently - and one of them is a genuine memory-leak source, which
 * Section 3 measures rather than merely describes.
 * ============================================================================
 */

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class InnerAndAnonymousClasses {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE FOUR KINDS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - FOUR KINDS OF NESTED CLASS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-20s %-24s %s%n", "KIND", "HOLDS OUTER REFERENCE?", "STANDALONE?");
        System.out.printf("    %-20s %-24s %s%n", "static nested", "NO", "yes");
        System.out.printf("    %-20s %-24s %s%n", "inner", "YES", "no - needs an instance");
        System.out.printf("    %-20s %-24s %s%n", "local", "yes (in an instance method)", "only in that method");
        System.out.printf("    %-20s %-24s %s%n", "anonymous", "yes (in an instance method)", "only where declared");

        System.out.println();
        System.out.println("  Creating them:");

        // A static nested class needs no enclosing instance at all.
        Outer.StaticNested nested = new Outer.StaticNested();
        System.out.println("    new Outer.StaticNested()      -> " + nested.describe());

        // An inner class REQUIRES one, hence the odd `outer.new` syntax.
        Outer outer = new Outer("the outer object");
        Outer.Inner inner = outer.new Inner();
        System.out.println("    outer.new Inner()             -> " + inner.describe());

        System.out.println();
        System.out.println("  That `outer.new Inner()` syntax exists precisely because an");
        System.out.println("  inner class CANNOT EXIST without an enclosing instance.");


        /* ====================================================================
         * SECTION 2 - THE HIDDEN FIELD
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE SYNTHETIC this$0 FIELD");
        System.out.println("=".repeat(74));

        System.out.println("  The compiler ADDS a field to every inner class. Reflection can");
        System.out.println("  see it - you never wrote it:");
        System.out.println();
        System.out.println("    Outer.Inner declares:");
        printDeclaredFields(Outer.Inner.class);
        System.out.println();
        System.out.println("    Outer.StaticNested declares:");
        printDeclaredFields(Outer.StaticNested.class);

        System.out.println();
        System.out.println("  That this$0 field is what makes Outer.this work:");
        inner.showOuterAccess();
        System.out.println();
        System.out.println("  ...and it is exactly what causes the leak in Section 3.");
        System.out.println("  You can also see it with:  javap -p Outer\\$Inner");


        /* ====================================================================
         * SECTION 3 - THE MEMORY LEAK, MEASURED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE LEAK THAT `static` PREVENTS");
        System.out.println("=".repeat(74));

        Runtime runtime = Runtime.getRuntime();
        long megabyte = 1024L * 1024L;
        int listeners = 40;

        System.out.println("  A Screen holds a 10 MB bitmap. We register " + listeners
                + " tiny listeners");
        System.out.println("  in a global registry, then DROP every reference to the Screens.");
        System.out.println();

        long baselineMb = usedMemoryMb(runtime, megabyte);

        // --- WITH a non-static inner listener: the Screens cannot be collected.
        List<Object> leakyRegistry = new ArrayList<>();
        for (int i = 0; i < listeners; i++) {
            LeakyScreen screen = new LeakyScreen();
            leakyRegistry.add(screen.new LeakyListener());
            // `screen` goes out of scope here - but the listener still holds it
        }
        long leakedMb = usedMemoryMb(runtime, megabyte);

        // Drop the registry and prove the memory really was pinned by it.
        leakyRegistry.clear();
        long afterClearingMb = usedMemoryMb(runtime, megabyte);

        // --- WITH a static nested listener: the Screens are collectible.
        List<Object> safeRegistry = new ArrayList<>();
        for (int i = 0; i < listeners; i++) {
            SafeScreen screen = new SafeScreen();
            safeRegistry.add(new SafeScreen.SafeListener("listener-" + i));
            // `screen` is genuinely unreachable now
        }
        long safeMb = usedMemoryMb(runtime, megabyte);

        System.out.printf("    %-52s %4d MB%n", "baseline, before anything", baselineMb);
        System.out.printf("    %-52s %4d MB%n",
                "holding " + listeners + " NON-STATIC inner listeners", leakedMb);
        System.out.printf("    %-52s %4d MB%n",
                "after clearing that registry", afterClearingMb);
        System.out.printf("    %-52s %4d MB%n",
                "holding " + listeners + " STATIC nested listeners", safeMb);
        System.out.println();
        System.out.println("    Both registries held " + listeners + " tiny listener objects.");
        System.out.println("    The NON-STATIC one also pinned " + listeners
                + " x 10 MB of bitmaps that");
        System.out.println("    nothing else referenced. The STATIC one pinned nothing at all -");
        System.out.println("    its line is back at the baseline.");
        System.out.println();
        System.out.println("    Clearing the registry released it, which proves the listeners");
        System.out.println("    were the only thing keeping those screens alive.");

        System.out.println();
        System.out.println("  THE CHAIN: registry -> Listener -> this$0 -> Screen -> 10 MB array.");
        System.out.println("  Nothing in that chain can EVER be collected while the registry");
        System.out.println("  holds the listener.");
        System.out.println();
        System.out.println("  This is the standard explanation for Android Activity leaks, and");
        System.out.println("  it happens in server code with callbacks and caches too.");

        /* --------------------------------------------------------------------
         * A REAL DETAIL MOST MATERIAL GETS WRONG: javac ELIDES this$0 when the
         * inner class never actually uses the enclosing instance. Worth knowing,
         * and NOT worth relying on.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  A DETAIL MOST MATERIAL GETS WRONG:");
        System.out.println("    javac does NOT add this$0 unconditionally. If an inner class");
        System.out.println("    never uses the enclosing instance, the field is ELIDED:");
        System.out.println();
        System.out.println("      Outer.Inner (uses `description`)  fields: "
                + Outer.Inner.class.getDeclaredFields().length);
        System.out.println("      NeverUsesOuter (uses nothing)     fields: "
                + Outer.NeverUsesOuter.class.getDeclaredFields().length
                + "   <- this$0 ELIDED");
        System.out.println();
        System.out.println("    So an inner class that touches nothing may not leak at all.");
        System.out.println("    DO NOT RELY ON THAT. It is a javac optimisation, not a");
        System.out.println("    language guarantee; older compilers did not do it; and one");
        System.out.println("    day someone adds a single reference to an outer field and");
        System.out.println("    silently reintroduces the leak with no visible change.");
        System.out.println();
        System.out.println("    The rule stands: DEFAULT TO static, and let the reader see");
        System.out.println("    the intent rather than deduce it from the compiler's mood.");
        System.out.println();
        System.out.println("  THE FIX IS ONE WORD: make the nested class `static` and pass it");
        System.out.println("  whatever it actually needs.");
        System.out.println();
        System.out.println("  DEFAULT NESTED CLASSES TO static. Drop it only when the class");
        System.out.println("  genuinely needs the enclosing instance.");

        // Release them so the rest of the program is not affected.
        leakyRegistry.clear();
        safeRegistry.clear();


        /* ====================================================================
         * SECTION 4 - ANONYMOUS CLASSES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - ANONYMOUS CLASSES");
        System.out.println("=".repeat(74));

        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
                System.out.println("    running, from " + getClass().getName());
            }
        };
        anonymous.run();
        String simpleName = anonymous.getClass().getSimpleName();
        System.out.println("    getSimpleName() -> \"" + simpleName + "\"   "
                + (simpleName.isEmpty() ? "<- EMPTY: it genuinely has no name" : ""));
        System.out.println("    getName()       -> " + anonymous.getClass().getName()
                + "   <- the compiler-generated name");
        System.out.println("    A real class file exists on disk with that name.");

        System.out.println();
        System.out.println("  WHAT ANONYMOUS CLASSES CAN DO THAT LAMBDAS CANNOT:");
        System.out.printf("    %-46s %-10s %s%n", "", "ANON", "LAMBDA");
        System.out.printf("    %-46s %-10s %s%n", "implement a MULTI-method interface", "YES", "no");
        System.out.printf("    %-46s %-10s %s%n", "extend a CLASS", "YES", "no");
        System.out.printf("    %-46s %-10s %s%n", "hold STATE in fields", "YES", "no");
        System.out.printf("    %-46s %-10s %s%n", "`this` means", "itself", "the enclosing object");
        System.out.printf("    %-46s %-10s %s%n", "generates a class file", "YES", "no (invokedynamic)");

        System.out.println();
        System.out.println("  1. A MULTI-METHOD interface - a lambda cannot do this at all:");
        Lifecycle lifecycle = new Lifecycle() {
            @Override public void onStart() { System.out.println("      onStart"); }
            @Override public void onStop()  { System.out.println("      onStop"); }
        };
        lifecycle.onStart();
        lifecycle.onStop();

        System.out.println();
        System.out.println("  2. EXTENDING A CLASS - also impossible with a lambda:");
        Greeter shouting = new Greeter("Danish") {
            @Override
            String greet() {
                return super.greet().toUpperCase() + "!!!";
            }
        };
        System.out.println("      " + shouting.greet());

        System.out.println();
        System.out.println("  3. HOLDING STATE between calls:");
        Runnable counting = new Runnable() {
            private int invocations = 0;      // a FIELD - lambdas cannot have these
            @Override
            public void run() {
                invocations++;
                System.out.println("      call number " + invocations);
            }
        };
        counting.run();
        counting.run();
        counting.run();

        System.out.println();
        System.out.println("  4. THE `this` DIFFERENCE, which bites when modernising code:");
        new ScopeComparison().compare();


        /* ====================================================================
         * SECTION 5 - PREFER LAMBDAS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - USE A LAMBDA WHEN YOU CAN");
        System.out.println("=".repeat(74));

        List<String> names = new ArrayList<>(List.of("Danish", "Al", "Priya", "Bo"));

        System.out.println("  The same comparator, three ways:");
        System.out.println();
        System.out.println("    ANONYMOUS CLASS (pre-Java 8):");
        List<String> viaAnonymous = new ArrayList<>(names);
        viaAnonymous.sort(new Comparator<String>() {
            @Override
            public int compare(String left, String right) {
                return Integer.compare(left.length(), right.length());
            }
        });
        System.out.println("      " + viaAnonymous + "   (5 lines)");

        System.out.println("    LAMBDA:");
        List<String> viaLambda = new ArrayList<>(names);
        viaLambda.sort((left, right) -> Integer.compare(left.length(), right.length()));
        System.out.println("      " + viaLambda + "   (1 line)");

        System.out.println("    METHOD REFERENCE via a comparator factory:");
        List<String> viaFactory = new ArrayList<>(names);
        viaFactory.sort(Comparator.comparingInt(String::length));
        System.out.println("      " + viaFactory + "   (1 short line)");

        System.out.println();
        System.out.println("  All three are identical in behaviour. The lambda produces NO");
        System.out.println("  class file - it compiles to an invokedynamic call site.");

        /* --------------------------------------------------------------------
         * THE DOUBLE-BRACE ANTI-PATTERN.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE DOUBLE-BRACE ANTI-PATTERN - never do this:");
        System.out.println();
        System.out.println("      List<String> list = new ArrayList<>() {{");
        System.out.println("          add(\"a\");");
        System.out.println("          add(\"b\");");
        System.out.println("      }};");
        System.out.println();

        List<String> doubleBrace = new ArrayList<>() {{
            add("a");
            add("b");
        }};
        List<String> plain = new ArrayList<>(List.of("a", "b"));

        System.out.println("    it 'works':            " + doubleBrace);
        System.out.println("    but its runtime class is " + doubleBrace.getClass().getName());
        System.out.println("    a plain ArrayList is     " + plain.getClass().getName());
        System.out.println("    contents equal?          " + doubleBrace.equals(plain)
                + "    (List.equals compares contents, so this still works)");
        System.out.println("    same class?              "
                + (doubleBrace.getClass() == plain.getClass()) + "   <- an ANONYMOUS SUBCLASS");
        System.out.println();
        System.out.println("    That subclass generates a class file, holds a reference to");
        System.out.println("    the enclosing instance, and breaks any code that checks");
        System.out.println("    getClass(). It also defeats serialization in some frameworks.");
        System.out.println();
        System.out.println("    Use  List.of(\"a\", \"b\")  or  new ArrayList<>(List.of(...)).");


        /* ====================================================================
         * SECTION 6 - LOCAL CLASSES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - LOCAL CLASSES");
        System.out.println("=".repeat(74));

        System.out.println("  A class declared INSIDE a method, scoped like a local variable:");
        demonstrateLocalClass(List.of("Danish", "", "  ", "Priya"));

        System.out.println();
        System.out.println("  They can capture EFFECTIVELY FINAL locals, exactly like lambdas");
        System.out.println("  (lesson 31). They are rare in modern code - a lambda, a method");
        System.out.println("  reference, or a private static method almost always reads");
        System.out.println("  better. You will mostly meet them in older codebases.");


        /* ====================================================================
         * SECTION 7 - MUTUAL PRIVATE ACCESS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - NESTED AND ENCLOSING SEE EACH OTHER'S PRIVATES");
        System.out.println("=".repeat(74));

        NestAccess.demonstrate();

        System.out.println();
        System.out.println("  They are considered the same 'top-level entity' for access.");
        System.out.println("  The JVM had no such concept, so javac used to generate");
        System.out.println("  SYNTHETIC BRIDGE METHODS to make it work - which is why javap");
        System.out.println("  on a nested class used to show methods you never wrote.");
        System.out.println();
        System.out.println("  Java 11's NEST-BASED ACCESS CONTROL (JEP 181) added real JVM");
        System.out.println("  support and removed those bridges. You can see the nest:");
        System.out.println("    NestAccess.class.getNestHost()    -> "
                + NestAccess.class.getNestHost().getSimpleName());
        System.out.println("    NestAccess.Helper.getNestHost()   -> "
                + NestAccess.Helper.class.getNestHost().getSimpleName());
        System.out.println("    members of the nest               -> "
                + NestAccess.class.getNestMembers().length);

        System.out.println();
        System.out.println("  Nested classes may use ALL FOUR access modifiers, unlike");
        System.out.println("  top-level classes which may only be public or package-private");
        System.out.println("  (lesson 30).");


        /* ====================================================================
         * SECTION 8 - WHERE THESE APPEAR IN THE JDK
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - THE JDK's OWN USES");
        System.out.println("=".repeat(74));

        System.out.println("  STATIC NESTED - Map.Entry:");
        var entry = java.util.Map.entry("key", "value");
        System.out.println("    Map.entry(\"key\", \"value\") -> " + entry);
        System.out.println("    Nested because an Entry only makes sense in a Map's context,");
        System.out.println("    but a HashMap.Node needs NO reference back to the map.");

        System.out.println();
        System.out.println("  INNER - iterators, which genuinely need the outer instance:");
        CountingBag bag = new CountingBag();
        bag.add("first");
        bag.add("second");
        bag.add("third");
        for (String item : bag) {
            System.out.println("    iterated: " + item);
        }
        System.out.println("    The iterator's whole job is to walk THAT bag, so a non-static");
        System.out.println("    inner class is CORRECT here. ArrayList.Itr works exactly so.");

        System.out.println();
        System.out.println("  STATIC NESTED - builders (lesson 22):");
        System.out.println("    new Pizza.Builder(12).cheese().build()");

        System.out.println();
        System.out.println("  THE DECISION, IN ONE LINE:");
        System.out.println("    Does this nested class need the enclosing OBJECT?");
        System.out.println("      no  -> static nested   (the common case)");
        System.out.println("      yes -> inner           (iterators, live views)");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 34.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Prints a class's declared fields, including compiler-generated ones, so
     * the synthetic this$0 becomes visible.
     *
     * @param type the class to inspect
     */
    static void printDeclaredFields(Class<?> type) {
        Field[] fields = type.getDeclaredFields();
        if (fields.length == 0) {
            System.out.println("      (no fields at all)");
            return;
        }
        for (Field field : fields) {
            System.out.println("      " + field.getType().getSimpleName() + " " + field.getName()
                    + (field.isSynthetic() ? "   <- SYNTHETIC, added by the compiler" : ""));
        }
    }

    /**
     * Reads current heap usage in megabytes, nudging the collector first so the
     * number reflects what is genuinely reachable.
     *
     * @param runtime  the runtime to ask
     * @param megabyte the divisor
     * @return megabytes currently in use
     */
    static long usedMemoryMb(Runtime runtime, long megabyte) {
        System.gc();
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return (runtime.totalMemory() - runtime.freeMemory()) / megabyte;
    }

    /**
     * Declares and uses a LOCAL class - one scoped to this method only.
     *
     * @param inputs the strings to validate
     */
    static void demonstrateLocalClass(List<String> inputs) {

        // A local class: visible only inside this method, like a local variable.
        class BlankValidator {
            /**
             * @param candidate the string to test
             * @return true if it has real content
             */
            boolean isValid(String candidate) {
                return candidate != null && !candidate.isBlank();
            }
        }

        BlankValidator validator = new BlankValidator();
        for (String input : inputs) {
            System.out.println("    \"" + input + "\" valid? " + validator.isValid(input));
        }
    }
}

// ----------------------------------------------------------------------------
// SECTIONS 1 AND 2
// ----------------------------------------------------------------------------

/** Holds one static nested class and one inner class, for comparison. */
class Outer {

    private final String description;

    /** @param description text identifying this outer object */
    Outer(String description) {
        this.description = description;
    }

    /**
     * STATIC NESTED: no reference to any Outer instance, so it can be created
     * standalone and cannot reach Outer's instance state.
     */
    static class StaticNested {

        /** @return a description of what this class can see */
        String describe() {
            // System.out.println(description);
            //   ERROR: non-static variable description cannot be referenced
            //   from a static context - there is no Outer instance.
            return "a static nested class - no outer instance, none needed";
        }
    }

    /**
     * INNER (non-static nested): holds a hidden reference to the Outer that
     * created it, which is what lets it read Outer's private state.
     */
    class Inner {

        /** @return a description of what this class can see */
        String describe() {
            return "an inner class - it can see \"" + description + "\"";
        }

        /** Prints the three ways of reaching the enclosing object. */
        void showOuterAccess() {
            System.out.println("    description         -> " + description);
            System.out.println("    Outer.this.description -> " + Outer.this.description);
            System.out.println("    Outer.this          -> a real reference to the outer object");
        }
    }

    /**
     * An inner class that never touches the enclosing instance. javac ELIDES
     * this$0 for it - see the note in Section 3. Non-static, and yet it holds
     * nothing.
     */
    class NeverUsesOuter {

        /** @return something computed from nothing outside itself */
        int compute() {
            return 42;
        }
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - THE LEAK
// ----------------------------------------------------------------------------

/** Holds a large array, and a NON-STATIC listener that accidentally pins it. */
class LeakyScreen {

    /** 10 MB that nothing else references once the Screen goes out of scope. */
    private final byte[] bitmap = new byte[10_000_000];

    /**
     * NON-STATIC, so every instance holds this$0 -> the whole LeakyScreen ->
     * the 10 MB bitmap. Registering one of these anywhere long-lived pins all
     * of it, forever.
     */
    class LeakyListener {

        /**
         * Uses ONE trivial thing from the enclosing screen - which is entirely
         * realistic for a listener, and is enough to force javac to keep the
         * this$0 field. See the note in Section 3 about elision.
         *
         * @return a harmless description
         */
        String describe() {
            return "listener for a screen of " + bitmap.length + " bytes";
        }
    }

    /** @return the bitmap size, so the field is not optimised away */
    int bitmapSize() {
        return bitmap.length;
    }
}

/** The same design, done safely. */
class SafeScreen {

    private final byte[] bitmap = new byte[10_000_000];

    /**
     * STATIC nested: no this$0, so registering one pins nothing but itself.
     * It takes exactly what it needs as a constructor parameter.
     */
    static class SafeListener {

        private final String name;

        /** @param name what this listener is for */
        SafeListener(String name) {
            this.name = name;
        }

        /** Does nothing, and holds nothing. */
        void onEvent() {
        }
    }

    /** @return the bitmap size, so the field is not optimised away */
    int bitmapSize() {
        return bitmap.length;
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - ANONYMOUS CLASSES
// ----------------------------------------------------------------------------

/** TWO abstract methods, so no lambda can implement it. */
interface Lifecycle {
    /** Called when starting. */
    void onStart();

    /** Called when stopping. */
    void onStop();
}

/** A CLASS, so only an anonymous class can extend it inline. */
class Greeter {

    private final String name;

    /** @param name who to greet */
    Greeter(String name) {
        this.name = name;
    }

    /** @return the greeting */
    String greet() {
        return "Hello, " + name;
    }
}

/** Shows that `this` means different things in the two forms. */
class ScopeComparison {

    @Override
    public String toString() {
        return "the enclosing ScopeComparison object";
    }

    /** Prints `this` from inside an anonymous class and inside a lambda. */
    void compare() {
        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
                System.out.println("      anonymous class: this = " + this.getClass().getName());
            }
        };

        Runnable lambda = () -> System.out.println("      lambda:          this = " + this);

        anonymous.run();
        lambda.run();
        System.out.println("      A lambda does NOT introduce a new scope for `this`.");
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - NEST-BASED ACCESS
// ----------------------------------------------------------------------------

/** Demonstrates that nested and enclosing classes see each other's privates. */
class NestAccess {

    private static final String OUTER_SECRET = "the outer class's private field";

    /** A nested class with its own private state. */
    static class Helper {

        private static final String NESTED_SECRET = "the nested class's private field";

        /** @return the OUTER class's private field, read from in here */
        static String readOuterPrivate() {
            return OUTER_SECRET;
        }
    }

    /** Prints both directions of access. */
    static void demonstrate() {
        // The enclosing class reading the NESTED class's private field.
        System.out.println("    Outer reads Helper.NESTED_SECRET -> " + Helper.NESTED_SECRET);
        // The nested class reading the ENCLOSING class's private field.
        System.out.println("    Helper reads OUTER_SECRET        -> " + Helper.readOuterPrivate());
    }
}

// ----------------------------------------------------------------------------
// SECTION 8 - A CORRECT USE OF AN INNER CLASS
// ----------------------------------------------------------------------------

/**
 * An iterable collection whose iterator is a NON-STATIC inner class - the one
 * case where that is genuinely correct, because the iterator's whole job is to
 * walk this particular instance. ArrayList.Itr works exactly this way.
 */
class CountingBag implements Iterable<String> {

    private final List<String> items = new ArrayList<>();

    /** @param item something to add */
    void add(String item) {
        items.add(item);
    }

    @Override
    public Iterator<String> iterator() {
        return new BagIterator();
    }

    /**
     * INNER, deliberately: it needs the enclosing bag's items, and it cannot
     * meaningfully exist without one.
     */
    private class BagIterator implements Iterator<String> {

        private int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor < items.size();      // reaches the OUTER instance's list
        }

        @Override
        public String next() {
            return items.get(cursor++);
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Compile this file with javac and run:
 *        javap -p Outer\$Inner
 *    Find the this$0 field in the output. Then do the same for
 *    Outer$StaticNested and confirm it is absent.
 *
 * 2. Change LeakyScreen.LeakyListener to static (you will have to fix the
 *    construction site). Re-run Section 3 and compare the heap numbers.
 *
 * 3. Convert the anonymous Comparator in Section 5 to a lambda, then to a
 *    method reference. Then try to convert the Lifecycle one and explain why
 *    you cannot.
 *
 * 4. Write an anonymous Runnable that prints `this`, then convert it to a
 *    lambda and predict what changes. This is the exact surprise people hit
 *    when modernising old listener code.
 *
 * 5. Make CountingBag.BagIterator static. Which line stops compiling? What
 *    would you have to pass it to make it work, and is that an improvement?
 *
 * 6. Build a double-brace-initialised HashMap and pass it to a method that
 *    does `if (map.getClass() == HashMap.class)`. Watch it fail. Then fix it.
 * ============================================================================
 */
