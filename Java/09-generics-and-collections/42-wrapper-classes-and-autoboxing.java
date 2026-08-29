/* ============================================================================
 * 42 - WRAPPER CLASSES, AUTOBOXING AND CACHING
 * ----------------------------------------------------------------------------
 * Companion lesson: 42-wrapper-classes-and-autoboxing.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/42-wrapper-classes-and-autoboxing.java
 *
 * Collections and generics cannot hold primitives. Wrapper classes bridge that
 * gap - and the AUTOMATIC conversion between the two hides several genuinely
 * expensive traps.
 *
 * Section 3 is the one that reaches production: code that passes its tests
 * with small numbers and fails with large ones.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

class WrapperClassesAndAutoboxing {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE EIGHT WRAPPERS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE EIGHT WRAPPERS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-12s %-14s %-12s %-10s %s%n",
                "PRIMITIVE", "WRAPPER", "final?", "Number?", "CACHED RANGE");
        printWrapper("byte", Byte.class, "all 256 values");
        printWrapper("short", Short.class, "-128..127");
        printWrapper("int", Integer.class, "-128..127 (configurable)");
        printWrapper("long", Long.class, "-128..127");
        printWrapper("float", Float.class, "NONE");
        printWrapper("double", Double.class, "NONE");
        printWrapper("char", Character.class, "0..127");
        printWrapper("boolean", Boolean.class, "both values");

        System.out.println();
        System.out.println("  Every wrapper is IMMUTABLE and final. Six extend Number;");
        System.out.println("  Character and Boolean do not.");

        System.out.println();
        System.out.println("  WHY THEY EXIST:");
        System.out.println("      List<int> numbers;      -> does not compile");
        System.out.println("      List<Integer> numbers;  -> fine");
        System.out.println();
        System.out.println("  Generics work by ERASURE (lesson 43) and erase to Object, which");
        System.out.println("  a primitive can never be. Wrappers also give you null for");
        System.out.println("  'absent', plus useful statics:");
        System.out.println("    Integer.MAX_VALUE          -> " + Integer.MAX_VALUE);
        System.out.println("    Integer.parseInt(\"42\")     -> " + Integer.parseInt("42"));
        System.out.println("    Integer.toBinaryString(10) -> " + Integer.toBinaryString(10));


        /* ====================================================================
         * SECTION 2 - AUTOBOXING, AND THE null TRAP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - AUTOBOXING IS A COMPILER INSERTION");
        System.out.println("=".repeat(74));

        Integer boxed = 42;          // the compiler writes Integer.valueOf(42)
        int unboxed = boxed;         // the compiler writes boxed.intValue()

        System.out.println("      Integer boxed = 42;    // becomes Integer.valueOf(42)");
        System.out.println("      int unboxed = boxed;   // becomes boxed.intValue()");
        System.out.println();
        System.out.println("    boxed   -> " + boxed);
        System.out.println("    unboxed -> " + unboxed);
        System.out.println();
        System.out.println("  Convenient - and that convenience is exactly why the traps");
        System.out.println("  below are so easy to miss.");

        System.out.println();
        System.out.println("  THE null UNBOXING TRAP:");

        Map<String, Integer> counts = new HashMap<>();
        counts.put("present", 5);

        try {
            int count = counts.get("missing");
            System.out.println("    unreachable: " + count);
        } catch (NullPointerException e) {
            System.out.println("    int count = counts.get(\"missing\");");
            System.out.println("      -> NullPointerException");
            System.out.println("      " + e.getMessage());
            System.out.println();
            System.out.println("    get() returned null, and unboxing called null.intValue().");
            System.out.println("    There is NO VISIBLE DEREFERENCE on that line, which makes");
            System.out.println("    it a genuinely confusing NPE.");
        }

        System.out.println();
        System.out.println("    An even quieter version - a comparison:");
        try {
            Integer value = null;
            if (value == 0) {
                System.out.println("    unreachable");
            }
        } catch (NullPointerException e) {
            System.out.println("      if (value == 0)  ->  NullPointerException");
            System.out.println("      `value` was UNBOXED in order to compare with the int 0.");
        }

        System.out.println();
        System.out.println("    THE FIXES:");
        System.out.println("      counts.getOrDefault(\"missing\", 0) -> "
                + counts.getOrDefault("missing", 0));
        Integer maybe = counts.get("missing");
        System.out.println("      guard first: (maybe != null && maybe == 0) -> "
                + (maybe != null && maybe == 0));

        System.out.println();
        System.out.println("  THE TERNARY UNBOXING TRAP (lessons 05 and 08):");
        try {
            boolean condition = false;
            Integer result = condition ? 1 : nullInteger();
            System.out.println("    unreachable: " + result);
        } catch (NullPointerException e) {
            System.out.println("      condition ? 1 : nullInteger()  ->  NullPointerException");
            System.out.println("      The int literal 1 forces the whole expression to type");
            System.out.println("      int, which UNBOXES the null branch. Keep both branches");
            System.out.println("      the same type.");
        }


        /* ====================================================================
         * SECTION 3 - THE Integer CACHE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE TRAP THAT REACHES PRODUCTION");
        System.out.println("=".repeat(74));

        Integer smallA = 127;
        Integer smallB = 127;
        Integer largeA = 128;
        Integer largeB = 128;

        System.out.println("    Integer a = 127, b = 127;   a == b -> " + (smallA == smallB));
        System.out.println("    Integer c = 128, d = 128;   c == d -> " + (largeA == largeB)
                + "   <- ONE apart, opposite answer");
        System.out.println();
        System.out.println("    a.equals(b) -> " + smallA.equals(smallB));
        System.out.println("    c.equals(d) -> " + largeA.equals(largeB) + "   always correct");

        System.out.println();
        System.out.println("  Finding the exact boundary by testing every value:");
        int lowest = findCacheLowerBound();
        int highest = findCacheUpperBound();
        System.out.println("    the cache on THIS JVM covers " + lowest + " .. " + highest);

        System.out.println();
        System.out.println("  Integer.valueOf returns a CACHED instance in that range and a");
        System.out.println("  NEW object outside it. So == compares identity and appears to");
        System.out.println("  work for small numbers.");
        System.out.println();
        System.out.println("  WHY THIS REACHES PRODUCTION: code passes its tests with small");
        System.out.println("  values and fails with large ones. The bug survives review");
        System.out.println("  because the TEST DATA is unrepresentative.");

        System.out.println();
        System.out.println("  Demonstrating exactly that:");
        System.out.println("    matchesUsingIdentity(orderId, 100)   -> "
                + matchesUsingIdentity(100, 100) + "   passes in the test");
        System.out.println("    matchesUsingIdentity(orderId, 90210) -> "
                + matchesUsingIdentity(90210, 90210) + "  fails in production");
        System.out.println("    matchesUsingEquals(orderId, 90210)   -> "
                + matchesUsingEquals(90210, 90210) + "   correct at any size");

        System.out.println();
        System.out.println("  The upper bound is configurable:");
        System.out.println("      java -XX:AutoBoxCacheMax=1000 ...");
        System.out.println("  which is worth knowing mainly because it means you cannot rely");
        System.out.println("  on 127 either.");

        System.out.println();
        System.out.println("  new Integer(5) always bypassed the cache and is DEPRECATED FOR");
        System.out.println("  REMOVAL. Use Integer.valueOf(5), or just 5.");


        /* ====================================================================
         * SECTION 4 - THE == RULES, EXHAUSTIVELY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHEN == COMPARES VALUES AND WHEN IT COMPARES OBJECTS");
        System.out.println("=".repeat(74));

        Integer wrapperA = 1000;
        Integer wrapperB = 1000;
        int primitive = 1000;

        System.out.println("    Integer a = 1000, b = 1000;  int p = 1000;");
        System.out.println();
        System.out.println("      a == b        -> " + (wrapperA == wrapperB)
                + "   two WRAPPERS: compares REFERENCES");
        System.out.println("      a == p        -> " + (wrapperA == primitive)
                + "    one PRIMITIVE: `a` is UNBOXED, compares VALUES");
        System.out.println("      a.equals(b)   -> " + wrapperA.equals(wrapperB)
                + "    content, always correct");
        System.out.println();
        System.out.println("  THE RULE: if EITHER operand is a primitive, == unboxes and");
        System.out.println("  compares values. If BOTH are wrappers, it compares references.");
        System.out.println();
        System.out.println("  That asymmetry is why `a == p` behaves differently from `a == b`,");
        System.out.println("  and it is worth knowing precisely.");

        System.out.println();
        System.out.println("  MIXING TYPES BREAKS equals:");
        Integer asInteger = 1;
        Long asLong = 1L;
        System.out.println("    Integer 1 .equals( Long 1L )  -> " + asInteger.equals(asLong)
                + "   <- FALSE, despite equal values");
        System.out.println("    Integer.equals checks the TYPE first, and returns false for");
        System.out.println("    anything that is not an Integer, whatever its value.");
        System.out.println("    (`asInteger == asLong` does not even compile.)");
        System.out.println();
        System.out.println("    The fix, when you must compare across types:");
        System.out.println("      asInteger.longValue() == asLong  -> "
                + (asInteger.longValue() == asLong));


        /* ====================================================================
         * SECTION 5 - THE PERFORMANCE COST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - ONE CHARACTER, A MILLION ALLOCATIONS");
        System.out.println("=".repeat(74));

        int iterations = 20_000_000;

        long startPrimitive = System.nanoTime();
        long primitiveSum = 0L;                    // long - no boxing
        for (int i = 0; i < iterations; i++) {
            primitiveSum += i;
        }
        long primitiveMillis = (System.nanoTime() - startPrimitive) / 1_000_000;

        long startBoxed = System.nanoTime();
        Long boxedSum = 0L;                        // Long - boxes EVERY iteration
        for (int i = 0; i < iterations; i++) {
            boxedSum += i;                         // unbox, add, box
        }
        long boxedMillis = (System.nanoTime() - startBoxed) / 1_000_000;

        System.out.println("      long sum = 0L;   vs   Long sum = 0L;");
        System.out.println("                            ^ one character");
        System.out.println();
        System.out.printf("    %,d additions:%n", iterations);
        System.out.println("      long (primitive) -> " + primitiveMillis + " ms");
        System.out.println("      Long (boxed)     -> " + boxedMillis + " ms");
        System.out.println("      same answer? " + (primitiveSum == boxedSum));
        System.out.println();
        System.out.println("    The boxed version allocated " + String.format("%,d", iterations)
                + " Long objects,");
        System.out.println("    every one of them immediately garbage. This is the classic");
        System.out.println("    accidental-boxing bug, and it is invisible on review.");

        System.out.println();
        System.out.println("  THE MEMORY COST IS REAL TOO:");
        System.out.println("    an int      -> 4 bytes");
        System.out.println("    an Integer  -> typically 16 bytes + an 8-byte reference");
        System.out.println("                   roughly 6x, before counting cache pressure");

        Runtime runtime = Runtime.getRuntime();
        long megabyte = 1024L * 1024L;
        int elements = 3_000_000;

        System.gc();
        long beforeArray = runtime.totalMemory() - runtime.freeMemory();
        int[] primitiveArray = new int[elements];
        for (int i = 0; i < elements; i++) {
            primitiveArray[i] = i;
        }
        long arrayMb = (runtime.totalMemory() - runtime.freeMemory() - beforeArray) / megabyte;

        System.gc();
        long beforeList = runtime.totalMemory() - runtime.freeMemory();
        List<Integer> boxedList = new ArrayList<>(elements);
        for (int i = 0; i < elements; i++) {
            boxedList.add(i);
        }
        long listMb = (runtime.totalMemory() - runtime.freeMemory() - beforeList) / megabyte;

        System.out.println();
        System.out.printf("    %,d elements:%n", elements);
        System.out.println("      int[]          -> about " + arrayMb + " MB");
        System.out.println("      List<Integer>  -> about " + listMb + " MB");
        System.out.println("      (checksums " + primitiveArray[elements - 1] + " and "
                + boxedList.get(elements - 1) + ")");


        /* ====================================================================
         * SECTION 6 - THE PRIMITIVE-SPECIALISED TYPES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - THE JDK's ANSWER: SPECIALISED TYPES");
        System.out.println("=".repeat(74));

        int streamIterations = 5_000_000;

        long startIntStream = System.nanoTime();
        long intStreamSum = IntStream.range(0, streamIterations).asLongStream().sum();
        long intStreamMillis = (System.nanoTime() - startIntStream) / 1_000_000;

        long startBoxedStream = System.nanoTime();
        long boxedStreamSum = IntStream.range(0, streamIterations)
                .boxed()                       // forces boxing of every element
                .mapToLong(Integer::longValue)
                .sum();
        long boxedStreamMillis = (System.nanoTime() - startBoxedStream) / 1_000_000;

        System.out.printf("    summing %,d values:%n", streamIterations);
        System.out.println("      IntStream (no boxing) -> " + intStreamMillis + " ms");
        System.out.println("      .boxed() first        -> " + boxedStreamMillis + " ms");
        System.out.println("      same answer? " + (intStreamSum == boxedStreamSum));

        System.out.println();
        System.out.println("  THE SPECIALISED TYPES THAT EXIST TO AVOID BOXING:");
        System.out.printf("    %-30s %s%n", "IntStream, LongStream, DoubleStream", "lesson 54");
        System.out.printf("    %-30s %s%n", "IntPredicate, ToIntFunction", "lesson 52");
        System.out.printf("    %-30s %s%n", "IntUnaryOperator, IntBinaryOperator", "lesson 52");
        System.out.printf("    %-30s %s%n", "OptionalInt, OptionalLong", "lesson 56");
        System.out.printf("    %-30s %s%n", "AtomicInteger, AtomicLong", "lesson 65");
        System.out.printf("    %-30s %s%n", "int[], long[], double[]", "lesson 12");

        System.out.println();
        System.out.println("  When the choice exists, take the primitive one.");


        /* ====================================================================
         * SECTION 7 - USEFUL STATICS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE STATICS WORTH KNOWING");
        System.out.println("=".repeat(74));

        System.out.println("  Integer:");
        System.out.println("    parseInt(\"42\")        -> " + Integer.parseInt("42") + "   (an int)");
        System.out.println("    valueOf(\"42\")         -> " + Integer.valueOf("42") + "   (an Integer, cached)");
        System.out.println("    toBinaryString(10)    -> " + Integer.toBinaryString(10));
        System.out.println("    toHexString(255)      -> " + Integer.toHexString(255));
        System.out.println("    bitCount(7)           -> " + Integer.bitCount(7));
        System.out.println("    sum(3, 4)             -> " + Integer.sum(3, 4)
                + "    (for method references)");
        System.out.println("    max(3, 4)             -> " + Integer.max(3, 4));

        System.out.println();
        System.out.println("  Integer.compare MATTERS - the old `a - b` trick OVERFLOWS:");
        int big = Integer.MAX_VALUE;
        int small = -10;
        System.out.println("    a = " + big + ", b = " + small);
        System.out.println("      a - b                 -> " + (big - small)
                + "   NEGATIVE, so 'a < b'. WRONG.");
        System.out.println("      Integer.compare(a, b) -> " + Integer.compare(big, small)
                + "             positive, correct");
        System.out.println("    That overflow silently reverses a comparator's ordering, and");
        System.out.println("    it is a real source of broken sorts (lesson 49).");

        System.out.println();
        System.out.println("  Character:");
        System.out.println("    isDigit('7')          -> " + Character.isDigit('7'));
        System.out.println("    isLetter('a')         -> " + Character.isLetter('a'));
        System.out.println("    isWhitespace(' ')     -> " + Character.isWhitespace(' '));
        System.out.println("    toUpperCase('a')      -> " + Character.toUpperCase('a'));

        System.out.println();
        System.out.println("  Boolean.parseBoolean is LENIENT - anything not \"true\" is false,");
        System.out.println("  and it never throws:");
        for (String input : new String[]{"true", "TRUE", "yes", "1", "nonsense", null}) {
            System.out.println("    parseBoolean(" + (input == null ? "null" : "\"" + input + "\"")
                    + ") -> " + Boolean.parseBoolean(input));
        }
        System.out.println("    Note \"yes\" and \"1\" are FALSE. That surprises people parsing");
        System.out.println("    configuration files.");

        System.out.println();
        System.out.println("  Double:");
        System.out.println("    isNaN(0.0 / 0)        -> " + Double.isNaN(0.0 / 0));
        System.out.println("    isInfinite(1.0 / 0)   -> " + Double.isInfinite(1.0 / 0));
        System.out.println("    compare(0.0, -0.0)    -> " + Double.compare(0.0, -0.0)
                + "    (they ARE distinguishable - lesson 33)");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 42.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Prints one row of the wrapper table, reading the facts from the class.
     *
     * @param primitive   the primitive's name
     * @param wrapper     the wrapper class
     * @param cachedRange a description of its cache
     */
    static void printWrapper(String primitive, Class<?> wrapper, String cachedRange) {
        System.out.printf("    %-12s %-14s %-12s %-10s %s%n",
                primitive,
                wrapper.getSimpleName(),
                java.lang.reflect.Modifier.isFinal(wrapper.getModifiers()) ? "yes" : "no",
                Number.class.isAssignableFrom(wrapper) ? "yes" : "no",
                cachedRange);
    }

    /**
     * Returns a null Integer. Written as a method so the compiler cannot infer
     * a different type and hide the ternary trap.
     *
     * @return null
     */
    static Integer nullInteger() {
        return null;
    }

    /**
     * Finds the lowest value for which Integer.valueOf still returns a cached
     * instance, by testing rather than assuming.
     *
     * @return the cache's lower bound
     */
    static int findCacheLowerBound() {
        int lowest = 0;
        for (int candidate = 0; candidate > -2000; candidate--) {
            if (Integer.valueOf(candidate) != Integer.valueOf(candidate)) {
                break;
            }
            lowest = candidate;
        }
        return lowest;
    }

    /**
     * Finds the highest cached value the same way.
     *
     * @return the cache's upper bound
     */
    static int findCacheUpperBound() {
        int highest = 0;
        for (int candidate = 0; candidate < 100_000; candidate++) {
            if (Integer.valueOf(candidate) != Integer.valueOf(candidate)) {
                break;
            }
            highest = candidate;
        }
        return highest;
    }

    /**
     * Compares boxed ids with ==. Works for small ids, silently fails for
     * large ones - the exact shape of the production bug.
     *
     * @param left  one id
     * @param right the other
     * @return whether == considered them equal
     */
    static boolean matchesUsingIdentity(Integer left, Integer right) {
        return left == right;
    }

    /**
     * The correct version.
     *
     * @param left  one id
     * @param right the other
     * @return whether they are equal
     */
    static boolean matchesUsingEquals(Integer left, Integer right) {
        return left.equals(right);
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Run the program with -XX:AutoBoxCacheMax=1000 and watch Section 3's
 *    measured boundary change. Then explain why that makes == even less
 *    trustworthy, not more.
 *
 * 2. Write a method that counts word frequencies into a Map<String, Integer>
 *    using `map.put(word, map.get(word) + 1)`. Find the input that makes it
 *    throw, then fix it three ways: getOrDefault, merge, and computeIfAbsent.
 *
 * 3. Predict each, then run it:
 *        Integer a = 127, b = 127;   Long c = 127L;
 *        System.out.println(a == b);
 *        System.out.println(a.equals(c));
 *        System.out.println(a.longValue() == c);
 *
 * 4. Time summing 1..10,000,000 four ways: a long loop, a Long loop, an
 *    IntStream, and a Stream<Integer>. Rank them before you measure.
 *
 * 5. Write a Comparator using `a - b` and find two inputs for which it sorts
 *    wrongly. Then fix it with Integer.compare.
 *
 * 6. Parse the strings "true", "yes", "1", "on" as booleans the way a config
 *    file would want. Boolean.parseBoolean handles exactly one of them - write
 *    the method that handles all four.
 * ============================================================================
 */
