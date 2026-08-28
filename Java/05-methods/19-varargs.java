/* ============================================================================
 * 19 - VARARGS (VARIABLE-LENGTH ARGUMENT LISTS)
 * ----------------------------------------------------------------------------
 * Companion lesson: 19-varargs.md
 *
 * RUN IT:
 *     java Java/05-methods/19-varargs.java
 *
 * ONE FACT EXPLAINS EVERY RULE IN THIS LESSON:
 *   Varargs is syntactic sugar. The COMPILER builds an array at the CALL SITE.
 *   Inside the method the parameter IS an ordinary array, with every property
 *   an array has - it allocates, it is mutable, and it can be null.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Varargs {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE SYNTAX, AND WHAT IT COMPILES TO
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - VARARGS IS AN ARRAY IN DISGUISE");
        System.out.println("=".repeat(74));

        System.out.println("  static int sum(int... numbers)");
        System.out.println();
        System.out.println("    sum()                    = " + sum());
        System.out.println("    sum(1)                   = " + sum(1));
        System.out.println("    sum(1, 2, 3)             = " + sum(1, 2, 3));
        System.out.println("    sum(1, 2, 3, 4, 5, 6)    = " + sum(1, 2, 3, 4, 5, 6));
        System.out.println("    sum(new int[]{1, 2, 3})  = " + sum(new int[]{1, 2, 3})
                + "   <- an array works directly");

        System.out.println();
        System.out.println("  Inside the method the parameter IS an array:");
        inspect("a", "b", "c");

        System.out.println();
        System.out.println("  WHAT THE COMPILER DOES:");
        System.out.println("    you write   sum(1, 2, 3)");
        System.out.println("    it compiles sum(new int[]{1, 2, 3})");
        System.out.println();
        System.out.println("  There is NO special runtime support for varargs. The array is");
        System.out.println("  created at the CALL SITE. Every rule below follows from that.");


        /* ====================================================================
         * SECTION 2 - THE RULES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE THREE RULES");
        System.out.println("=".repeat(74));

        System.out.println("  1. ONLY ONE varargs parameter per method.");
        System.out.println("       void f(int... a, int... b)   -> COMPILE ERROR");
        System.out.println("       There is no way to know where the first list ends.");
        System.out.println();
        System.out.println("  2. It must be the LAST parameter.");
        System.out.println("       void f(int... a, String b)   -> COMPILE ERROR");
        System.out.println("       void f(String b, int... a)   -> fine");
        System.out.println();
        System.out.println("  3. Fixed parameters may PRECEDE it - the useful shape:");
        log("INFO", "server started", "port 8080 bound");
        log("ERROR", "connection refused");
        log("DEBUG");


        /* ====================================================================
         * SECTION 3 - IT IS A REAL ARRAY, WITH REAL CONSEQUENCES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - CONSEQUENCES OF BEING A REAL ARRAY");
        System.out.println("=".repeat(74));

        // It is mutable, and if the caller passed an existing array, the caller
        // sees the change - the reference was copied, not the contents.
        System.out.println("  MUTABLE - and the caller can see it:");
        String[] callerArray = {"original", "values"};
        System.out.println("    before  : " + Arrays.toString(callerArray));
        mutateFirst(callerArray);
        System.out.println("    after   : " + Arrays.toString(callerArray)
                + "   <- the caller's array changed");
        System.out.println("    This is lesson 17 again: the REFERENCE was copied, so the");
        System.out.println("    method and the caller share one array object.");

        System.out.println();
        System.out.println("    Spreading arguments instead is safe, because the compiler");
        System.out.println("    built a fresh array that nobody else holds:");
        mutateFirst("original", "values");
        System.out.println("    (nothing of the caller's could be affected)");

        /* --------------------------------------------------------------------
         * THE null TRAP. f(null) sets the whole ARRAY reference to null - it
         * does not create an array containing one null.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE null TRAP:");
        System.out.println("    sum(new int[0])   = " + sum(new int[0]) + "   an EMPTY array: fine");

        try {
            System.out.println("    sum((int[]) null) = " + sumUnguarded((int[]) null));
        } catch (NullPointerException e) {
            System.out.println("    sum((int[]) null) -> NullPointerException");
            System.out.println("      null set the whole ARRAY reference to null. It did NOT");
            System.out.println("      create an array containing one null element.");
        }

        System.out.println("    A defensive version just checks for it:");
        System.out.println("      sum((int[]) null) = " + sum((int[]) null) + "   (guarded, returns 0)");

        System.out.println();
        System.out.println("    For OBJECT varargs, f(null) is genuinely ambiguous between");
        System.out.println("    'a null array' and 'an array holding one null', and javac");
        System.out.println("    warns about it. Be explicit:");
        System.out.println("      describe((String[]) null)  -> " + describe((String[]) null));
        System.out.println("      describe((String) null)    -> " + describe((String) null));
        System.out.println("      describe()                 -> " + describe());


        /* ====================================================================
         * SECTION 4 - VARARGS IS THE LAST RESORT IN OVERLOAD RESOLUTION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - VARARGS LOSES TO EVERYTHING (PHASE 3)");
        System.out.println("=".repeat(74));

        System.out.println("  Two overloads available:");
        System.out.println("    void choose(int x)       <- fixed arity");
        System.out.println("    void choose(int... x)    <- varargs");
        System.out.println();
        System.out.print("    choose(5)      -> ");
        choose(5);
        System.out.print("    choose(5, 6)   -> ");
        choose(5, 6);
        System.out.print("    choose()       -> ");
        choose();

        System.out.println();
        System.out.println("  The fixed-arity method wins whenever it CAN match, because");
        System.out.println("  varargs is phase 3 and phase 1 succeeds first (lesson 18).");
        System.out.println();
        System.out.println("  This is genuinely useful: you can ADD a varargs overload to an");
        System.out.println("  existing API and every existing call site keeps binding to the");
        System.out.println("  method it always did. Nothing silently changes behaviour.");

        System.out.println();
        System.out.println("  But two VARARGS overloads are ambiguous:");
        System.out.println("    void f(int... x)     /  void f(Integer... x)");
        System.out.println("    f(1, 2);  -> ERROR: reference to f is ambiguous");
        System.out.println("    Both need phase 3, and neither is more specific.");
        System.out.println("    RULE: do not overload varargs methods at all.");


        /* ====================================================================
         * SECTION 5 - GENERIC VARARGS AND HEAP POLLUTION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - HEAP POLLUTION WITH T...");
        System.out.println("=".repeat(74));

        System.out.println("  Generics are ERASED (lesson 43), so T... becomes Object[] at");
        System.out.println("  runtime. That array's real element type is decided at the CALL");
        System.out.println("  SITE - and when the caller is itself generic, the compiler has");
        System.out.println("  nothing better to create than an Object[].");
        System.out.println();
        System.out.println("  Watching it happen. pickTwo(\"a\", \"b\") looks like it returns a");
        System.out.println("  String[], and its signature says so:");

        try {
            String[] pair = pickTwo("a", "b");
            System.out.println("    got " + Arrays.toString(pair));   // never reached
        } catch (ClassCastException e) {
            System.out.println("    -> ClassCastException: " + e.getMessage());
            System.out.println();
            System.out.println("    NOTHING was cast in your source. There is no visible cast");
            System.out.println("    anywhere in pickTwo. Here is what actually happened:");
            System.out.println();
            System.out.println("      1. pickTwo(T a, T b) calls toArray(a, b)");
            System.out.println("      2. inside pickTwo, T is not yet known, so the compiler");
            System.out.println("         creates an Object[] for the varargs array");
            System.out.println("      3. toArray returns that Object[]");
            System.out.println("      4. pickTwo returns it as T[], which the CALLER believes");
            System.out.println("         is a String[]");
            System.out.println("      5. the caller's implicit cast to String[] fails");
            System.out.println();
            System.out.println("    An Object[] is now referenced by a String[] variable's");
            System.out.println("    type. THAT is heap pollution: the heap holds an object");
            System.out.println("    whose real type contradicts its declared type.");
        }

        System.out.println();
        System.out.println("  The lesson: NEVER let a generic varargs array escape the");
        System.out.println("  method - do not return it, and do not pass it on. Read it and");
        System.out.println("  copy what you need out of it.");
        System.out.println();
        System.out.println("  javac warns on BOTH the declaration and the call site.");
        System.out.println();
        System.out.println("  If your method really is safe - it only READS the array and");
        System.out.println("  never stores into it - declare that with @SafeVarargs:");
        System.out.println("    safeListOf(\"a\", \"b\", \"c\") = " + safeListOf("a", "b", "c"));
        System.out.println("    safeListOf(1, 2, 3)       = " + safeListOf(1, 2, 3));
        System.out.println();
        System.out.println("  @SafeVarargs is allowed ONLY on methods that cannot be");
        System.out.println("  overridden: static, final, or private (Java 9+). A subclass");
        System.out.println("  could otherwise override the method unsafely and inherit the");
        System.out.println("  promise it does not keep.");
        System.out.println();
        System.out.println("  List.of, Arrays.asList and EnumSet.of are all annotated so.");


        /* ====================================================================
         * SECTION 6 - THE ALLOCATION COST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - EVERY CALL ALLOCATES AN ARRAY");
        System.out.println("=".repeat(74));

        int iterations = 50_000_000;

        long startFixed = System.nanoTime();
        long fixedTotal = 0;
        for (int i = 0; i < iterations; i++) {
            fixedTotal += addFixed(i, 1);
        }
        long fixedMillis = (System.nanoTime() - startFixed) / 1_000_000;

        long startVarargs = System.nanoTime();
        long varargsTotal = 0;
        for (int i = 0; i < iterations; i++) {
            varargsTotal += addVarargs(i, 1);
        }
        long varargsMillis = (System.nanoTime() - startVarargs) / 1_000_000;

        System.out.printf("  %,d calls:%n", iterations);
        System.out.println("    add(int, int)     : " + fixedMillis + " ms");
        System.out.println("    add(int...)       : " + varargsMillis + " ms");
        System.out.println("    same results? " + (fixedTotal == varargsTotal));
        System.out.println();
        System.out.println("  (The JIT can often eliminate the allocation entirely when it");
        System.out.println("  proves the array never escapes, so the gap may be small here.");
        System.out.println("  It is not always able to.)");

        System.out.println();
        System.out.println("  This is exactly why the JDK writes:");
        System.out.println("    static <E> List<E> of()               // 0 elements");
        System.out.println("    static <E> List<E> of(E e1)           // 1");
        System.out.println("    static <E> List<E> of(E e1, E e2)     // 2");
        System.out.println("    ...  ten fixed-arity overloads  ...");
        System.out.println("    static <E> List<E> of(E... elements)  // 11 or more");
        System.out.println();
        System.out.println("  Ten hand-written overloads purely to avoid allocating an array");
        System.out.println("  in the common cases. Map.of and EnumSet.of do the same.");
        System.out.println();
        System.out.println("  DO NOT COPY THIS unless profiling proves you need it. It is a");
        System.out.println("  JDK-scale optimisation for code called billions of times.");


        /* ====================================================================
         * SECTION 7 - PASSING AN ARRAY: SPREAD OR WRAP?
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - SPREADING VERSUS WRAPPING");
        System.out.println("=".repeat(74));

        Object[] threeValues = {"a", "b", "c"};

        System.out.println("  Object[] values = {\"a\", \"b\", \"c\"};");
        System.out.println();
        System.out.print("    printf(\"%s %s %s%n\", values)         -> ");
        System.out.printf("%s %s %s%n", threeValues);
        System.out.println("      the array SPREAD into three arguments");

        System.out.print("    printf(\"%s%n\", (Object) values)      -> ");
        System.out.printf("%s%n", (Object) threeValues);
        System.out.println("      the cast forced it to WRAP: one argument, the array itself");
        System.out.println();
        System.out.println("  Casting to (Object) is the standard trick when you need to");
        System.out.println("  pass an array as a SINGLE argument rather than spreading it.");

        // The classic Arrays.asList puzzle, which is the same rule.
        System.out.println();
        System.out.println("  The classic puzzle, which is this same rule:");

        Integer[] boxed = {1, 2, 3};
        int[] primitives = {1, 2, 3};

        System.out.println("    Arrays.asList(Integer[]{1,2,3}).size() = "
                + Arrays.asList(boxed).size() + "   SPREAD into three elements");
        System.out.println("    Arrays.asList(int[]{1,2,3}).size()     = "
                + Arrays.asList(primitives).size() + "   WRAPPED as one element");
        System.out.println();
        System.out.println("  Why: asList takes T..., generics cannot hold primitives, so an");
        System.out.println("  int[] cannot spread. The whole array becomes a single T.");
        System.out.println("  An Integer[] spreads happily. Lesson 16 covered this too.");


        /* ====================================================================
         * SECTION 8 - VARARGS YOU ALREADY USE EVERY DAY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - WHERE YOU ALREADY USE IT");
        System.out.println("=".repeat(74));

        System.out.println("    String.format(\"%s is %d\", \"Java\", 31)");
        System.out.println("      -> " + String.format("%s is %d", "Java", 31));
        System.out.println("    List.of(1, 2, 3)");
        System.out.println("      -> " + List.of(1, 2, 3));
        System.out.println("    String.join(\", \", \"a\", \"b\", \"c\")");
        System.out.println("      -> " + String.join(", ", "a", "b", "c"));
        System.out.println("    Arrays.asList(1, 2, 3)");
        System.out.println("      -> " + Arrays.asList(1, 2, 3));
        System.out.println("    Objects.hash(1, \"a\", true)");
        System.out.println("      -> " + java.util.Objects.hash(1, "a", true));
        System.out.println();
        System.out.println("  Objects.hash is a good illustration of the trade-off: very");
        System.out.println("  convenient, allocates an array on every call, and the JDK");
        System.out.println("  documents that a manual computation is preferable inside a");
        System.out.println("  hot hashCode() implementation.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 19.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 1 AND 3 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Sums any number of ints, guarding against a null array.
     *
     * @param numbers the values to add; may be empty or null
     * @return the total, or 0 if null
     */
    static int sum(int... numbers) {
        if (numbers == null) {
            return 0;      // f(null) hands us a null ARRAY, not an empty one
        }
        int total = 0;
        for (int number : numbers) {
            total += number;
        }
        return total;
    }

    /**
     * The same sum without the null guard, used to demonstrate what happens.
     *
     * @param numbers the values to add
     * @return the total
     */
    static int sumUnguarded(int... numbers) {
        int total = 0;
        for (int number : numbers) {   // NullPointerException if numbers is null
            total += number;
        }
        return total;
    }

    /**
     * Proves the varargs parameter is an ordinary array by interrogating it.
     *
     * @param items any number of strings
     */
    static void inspect(String... items) {
        System.out.println("    items.length      = " + items.length);
        System.out.println("    items.getClass()  = " + items.getClass().getName()
                + "   ([Ljava.lang.String; means String[])");
        System.out.println("    items[0]          = " + items[0] + "   (indexable)");
        System.out.println("    Arrays.toString   = " + Arrays.toString(items));
    }

    /**
     * The common and useful shape: fixed parameters first, varargs last.
     *
     * @param level    the log level
     * @param messages any number of message lines
     */
    static void log(String level, String... messages) {
        if (messages.length == 0) {
            System.out.println("       [" + level + "] (no messages)");
            return;
        }
        for (String message : messages) {
            System.out.println("       [" + level + "] " + message);
        }
    }

    /**
     * Mutates the first element. When the caller passed an existing array, the
     * caller sees this - the varargs parameter is that same array object.
     *
     * @param items any number of strings
     */
    static void mutateFirst(String... items) {
        if (items.length > 0) {
            items[0] = "MUTATED";
        }
    }

    /**
     * Distinguishes the three ways a caller can supply "nothing".
     *
     * @param items a null array, an array of one null, or no arguments
     * @return a description of what actually arrived
     */
    static String describe(String... items) {
        if (items == null) {
            return "the ARRAY itself is null";
        }
        if (items.length == 0) {
            return "an empty array, length 0";
        }
        return "an array of length " + items.length + ", first element is " + items[0];
    }

    // ------------------------------------------------------------------------
    // SECTION 4 SUPPORT
    // ------------------------------------------------------------------------

    /** Fixed arity - wins whenever it can match. @param x the value */
    static void choose(int x) {
        System.out.println("choose(int)     [fixed arity wins]");
    }

    /** Varargs - phase 3, the last resort. @param x the values */
    static void choose(int... x) {
        System.out.println("choose(int...)  [varargs, " + x.length + " argument(s)]");
    }

    // ------------------------------------------------------------------------
    // SECTION 5 SUPPORT - HEAP POLLUTION
    // ------------------------------------------------------------------------

    /**
     * Returns the varargs array itself - which is the unsafe act. The array's
     * real runtime type was decided by whoever called this method, and letting
     * it escape hands that array to code with a different idea of its type.
     *
     * <p>This is deliberately NOT annotated {@code @SafeVarargs}, because it is
     * not safe. javac warns about it.
     *
     * @param <T>   the element type
     * @param items the elements
     * @return the varargs array itself
     */
    @SuppressWarnings("varargs")
    static <T> T[] toArray(T... items) {
        return items;      // THE MISTAKE: letting the generic varargs array escape
    }

    /**
     * Looks completely safe and is not. Inside this method T is still unknown,
     * so the compiler must create an {@code Object[]} for the call to
     * {@link #toArray}. That {@code Object[]} is then returned as {@code T[]},
     * and the caller's implicit cast to its own concrete type fails.
     *
     * <p>This is the canonical heap-pollution example from Effective Java.
     *
     * @param <T>    the element type
     * @param first  the first element
     * @param second the second element
     * @return an array that claims to be T[] but is really an Object[]
     */
    static <T> T[] pickTwo(T first, T second) {
        return toArray(first, second);
    }

    /**
     * Genuinely safe: it only READS the varargs array and never stores anything
     * into it, so the @SafeVarargs promise is real. Note it is static, which is
     * one of the three places the annotation is permitted.
     *
     * @param <T>   the element type
     * @param items the elements to collect
     * @return a new mutable list holding them
     */
    @SafeVarargs
    static <T> List<T> safeListOf(T... items) {
        List<T> result = new ArrayList<>();
        for (T item : items) {
            result.add(item);      // reading only - nothing is written into `items`
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // SECTION 6 SUPPORT - ALLOCATION COST
    // ------------------------------------------------------------------------

    /** @param a first  @param b second  @return their sum */
    static int addFixed(int a, int b) {
        return a + b;
    }

    /** @param values the numbers to add  @return their sum */
    static int addVarargs(int... values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write `String joinWith(String separator, String... parts)` that returns
 *    "" for no parts and never leaves a trailing separator. Then compare it
 *    with String.join and delete yours.
 *
 * 2. Predict each, then run it:
 *        static void f(Object... o) { print(o.length); }
 *        f();  f(null);  f((Object) null);  f(new Object[0]);  f(1, 2);
 *    Two of these behave differently from what most people expect.
 *
 * 3. Add `static void choose(long... x)` next to the two in Section 4. Does
 *    choose(5) still compile? Does choose(5, 6)? Explain using the phases.
 *
 * 4. Write `static int max(int first, int... rest)`. Why is that signature
 *    better than `static int max(int... values)`? (Hint: what does the second
 *    one do when called with no arguments?)
 *
 * 5. Remove @SafeVarargs from safeListOf and compile with -Xlint:all. Read the
 *    warning. Then try to put @SafeVarargs on a non-static, non-final instance
 *    method and read that error.
 *
 * 6. Time Objects.hash(a, b, c) against a manual
 *    `31 * (31 * a.hashCode() + b.hashCode()) + c.hashCode()` over 50,000,000
 *    iterations. Then decide whether you would ever bother.
 * ============================================================================
 */
