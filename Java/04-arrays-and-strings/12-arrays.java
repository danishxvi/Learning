/* ============================================================================
 * 12 - ARRAYS: MEMORY LAYOUT, CREATION AND ITERATION
 * ----------------------------------------------------------------------------
 * Companion lesson: 12-arrays.md
 *
 * RUN IT:
 *     java Java/04-arrays-and-strings/12-arrays.java
 *
 * The array is Java's ONLY built-in data structure. ArrayList, HashMap,
 * StringBuilder and String are all classes built on top of one. Understanding
 * arrays properly explains a great deal of later behaviour.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ArraysLesson {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - DECLARING AND CREATING
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - CREATING ARRAYS");
        System.out.println("=".repeat(74));

        // Declaration alone creates NO array. The variable is a null reference.
        int[] notYetCreated = null;
        System.out.println("  int[] declared but not created -> " + notYetCreated);

        // new int[5] allocates 5 slots, each set to the type's DEFAULT value.
        int[] sized = new int[5];
        System.out.println("  new int[5]                     -> " + Arrays.toString(sized));

        // The shorthand initialiser. Only legal in a DECLARATION.
        int[] shorthand = {10, 20, 30, 40, 50};
        System.out.println("  {10, 20, 30, 40, 50}           -> " + Arrays.toString(shorthand));

        // The explicit form works anywhere, including as a method argument.
        int[] explicit = new int[]{10, 20, 30};
        System.out.println("  new int[]{10, 20, 30}          -> " + Arrays.toString(explicit));

        System.out.println();
        System.out.println("  int[] b; b = {1, 2, 3};        -> COMPILE ERROR");
        System.out.println("  int[] b; b = new int[]{1,2,3}; -> fine");
        System.out.println("  which is why passing a literal array to a method needs");
        System.out.println("  the long form: sum(new int[]{1, 2, 3})");
        System.out.println("  sum(new int[]{1, 2, 3}) = " + sum(new int[]{1, 2, 3}));

        // Where the brackets go. Put them on the TYPE, always.
        System.out.println();
        System.out.println("Bracket placement - a real trap:");
        int[] bothArrays1, bothArrays2;       // BOTH are int[]
        int oneArray[], plainInt;             // oneArray is int[], plainInt is int
        bothArrays1 = new int[]{1};
        bothArrays2 = new int[]{2};
        oneArray = new int[]{3};
        plainInt = 4;
        System.out.println("  int[] a, b;  -> both are arrays: "
                + Arrays.toString(bothArrays1) + " " + Arrays.toString(bothArrays2));
        System.out.println("  int a[], b;  -> a is an array " + Arrays.toString(oneArray)
                + ", but b is a plain int " + plainInt + "  <- trap");


        /* ====================================================================
         * SECTION 2 - DEFAULT VALUES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - EVERY ELEMENT IS INITIALISED");
        System.out.println("=".repeat(74));

        System.out.println("  new int[3]      -> " + Arrays.toString(new int[3]));
        System.out.println("  new double[3]   -> " + Arrays.toString(new double[3]));
        System.out.println("  new boolean[3]  -> " + Arrays.toString(new boolean[3]));
        System.out.println("  new String[3]   -> " + Arrays.toString(new String[3]));

        char[] chars = new char[3];
        System.out.println("  new char[3]     -> three null characters, codes "
                + (int) chars[0] + " " + (int) chars[1] + " " + (int) chars[2]);

        System.out.println();
        System.out.println("Note what new String[3] is NOT: it is three NULLS, not three");
        System.out.println("empty strings. This is a very common source of NPEs:");

        String[] names = new String[3];
        try {
            System.out.println(names[0].length());
        } catch (NullPointerException e) {
            System.out.println("  names[0].length() -> NullPointerException");
            System.out.println("  You must populate a reference array before using it.");
        }


        /* ====================================================================
         * SECTION 3 - length IS A FIELD, AND BOUNDS ARE CHECKED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - length AND BOUNDS CHECKING");
        System.out.println("=".repeat(74));

        int[] data = {10, 20, 30, 40, 50};
        String text = "hello";
        List<Integer> list = List.of(1, 2, 3);

        System.out.println("Three spellings for the same idea - simply learn them:");
        System.out.println("  array.length    (a FIELD, no parentheses) = " + data.length);
        System.out.println("  string.length() (a METHOD)                = " + text.length());
        System.out.println("  list.size()     (a METHOD)                = " + list.size());
        System.out.println();
        System.out.println("  array.length() and string.length are both compile errors.");
        System.out.println("  `length` is also final - you cannot assign to it to resize.");

        System.out.println();
        System.out.println("Valid indices for a length-" + data.length + " array are 0.."
                + (data.length - 1) + ":");
        System.out.println("  data[0]                 = " + data[0]);
        System.out.println("  data[data.length - 1]   = " + data[data.length - 1] + "   <- the LAST one");

        try {
            System.out.println(data[data.length]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("  data[data.length]       -> " + e.getMessage());
            System.out.println("  Writing `i <= array.length` is THE most common array bug.");
        }

        try {
            System.out.println(data[-1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("  data[-1]                -> " + e.getMessage());
        }

        System.out.println();
        System.out.println("Java checks EVERY access. C does not - it would write past the");
        System.out.println("end and corrupt whatever memory was there. That difference is");
        System.out.println("the mechanism behind decades of buffer-overflow vulnerabilities.");


        /* ====================================================================
         * SECTION 4 - CONTIGUOUS MEMORY MEANS O(1) ACCESS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHY ACCESS IS O(1)");
        System.out.println("=".repeat(74));

        int[] large = new int[10_000_000];
        Arrays.fill(large, 7);

        // Reaching element 0 and element 9,999,999 cost the same, because the
        // JVM computes  base + index * elementSize  rather than walking a chain.
        //
        // MEASURING THIS HONESTLY IS HARDER THAN IT LOOKS. A single nanoTime()
        // reading is meaningless on the JVM: whichever index you measure FIRST
        // pays for JIT compilation and a cold CPU cache, and looks slower for
        // reasons that have nothing to do with the array.
        //
        // So we run the whole comparison THREE times. Watch round 0 be noisy
        // and rounds 1 and 2 settle - that settling IS the JIT warming up.
        int repetitions = 20_000_000;

        System.out.println("  Reading three different indices of a 10,000,000-element array,");
        System.out.println("  " + String.format("%,d", repetitions) + " times each, repeated three rounds:");
        System.out.println();
        System.out.printf("    %-8s %14s %14s %14s%n", "ROUND", "INDEX 0", "INDEX 5m", "INDEX 9,999,999");

        for (int round = 0; round < 3; round++) {
            double firstNanos  = timeReads(large, 0, repetitions);
            double middleNanos = timeReads(large, large.length / 2, repetitions);
            double lastNanos   = timeReads(large, large.length - 1, repetitions);

            System.out.printf("    %-8d %11.3f ns %11.3f ns %11.3f ns%n",
                    round, firstNanos, middleNanos, lastNanos);
        }

        System.out.println();
        System.out.println("  Once warm, all three indices cost the same. There is no");
        System.out.println("  distance to travel - the JVM computes an address.");
        System.out.println();
        System.out.println("  Each read is a single address calculation:");
        System.out.println("    address = base + index * elementSize");
        System.out.println("  That is what O(1) means, and it is also WHY an array cannot");
        System.out.println("  grow: the memory just past the end belongs to something else.");
        System.out.println();
        System.out.println("  (Sub-nanosecond averages simply mean your CPU completes several");
        System.out.println("  of these per clock cycle. For real benchmarking use JMH, which");
        System.out.println("  handles warm-up and dead-code elimination properly - hand-rolled");
        System.out.println("  timing like this is for illustration only.)");


        /* ====================================================================
         * SECTION 5 - COPYING, AND THE REFERENCE TRAP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - ASSIGNMENT DOES NOT COPY");
        System.out.println("=".repeat(74));

        int[] original = {1, 2, 3};
        int[] alias = original;         // copies the REFERENCE, not the contents

        alias[0] = 99;
        System.out.println("  int[] alias = original;  alias[0] = 99;");
        System.out.println("    original -> " + Arrays.toString(original) + "   <- also changed!");
        System.out.println("    alias    -> " + Arrays.toString(alias));
        System.out.println("    original == alias -> " + (original == alias)
                + "   (one array, two names)");

        System.out.println();
        System.out.println("The four real ways to copy:");

        int[] source = {1, 2, 3};

        int[] viaCopyOf = Arrays.copyOf(source, source.length);
        int[] viaRange = Arrays.copyOfRange(source, 1, 3);       // [1, 3) - end exclusive
        int[] viaClone = source.clone();
        int[] viaArraycopy = new int[source.length];
        System.arraycopy(source, 0, viaArraycopy, 0, source.length);

        System.out.println("  Arrays.copyOf(a, a.length)   -> " + Arrays.toString(viaCopyOf));
        System.out.println("  Arrays.copyOfRange(a, 1, 3)  -> " + Arrays.toString(viaRange)
                + "      (end index is EXCLUSIVE)");
        System.out.println("  a.clone()                    -> " + Arrays.toString(viaClone));
        System.out.println("  System.arraycopy(...)        -> " + Arrays.toString(viaArraycopy)
                + "   (fastest, ugliest)");

        viaCopyOf[0] = 99;
        System.out.println();
        System.out.println("  After changing the copy, the source is untouched: "
                + Arrays.toString(source));

        // copyOf can also RESIZE. This is exactly how ArrayList grows.
        System.out.println();
        System.out.println("copyOf can resize, which is how ArrayList grows internally:");
        System.out.println("  Arrays.copyOf(a, 6) -> " + Arrays.toString(Arrays.copyOf(source, 6))
                + "   (padded with defaults)");
        System.out.println("  Arrays.copyOf(a, 2) -> " + Arrays.toString(Arrays.copyOf(source, 2))
                + "            (truncated)");

        /* --------------------------------------------------------------------
         * SHALLOW VS DEEP. Every copy method above copies the REFERENCES held
         * in the array, not the objects they point to.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("EVERY copy method above is SHALLOW:");

        StringBuilder[] builders = {new StringBuilder("a"), new StringBuilder("b")};
        StringBuilder[] shallowCopy = builders.clone();

        shallowCopy[0].append("!");    // mutating the OBJECT, not the array slot

        System.out.println("  after shallowCopy[0].append(\"!\"):");
        System.out.println("    original    -> " + Arrays.toString(builders) + "   <- changed too");
        System.out.println("    shallowCopy -> " + Arrays.toString(shallowCopy));
        System.out.println("    The arrays are different objects, but the ELEMENTS are shared.");
        System.out.println("    A deep copy means copying each element yourself. Lesson 38.");


        /* ====================================================================
         * SECTION 6 - COMPARING AND PRINTING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - COMPARING AND PRINTING");
        System.out.println("=".repeat(74));

        int[] left = {1, 2, 3};
        int[] right = {1, 2, 3};

        System.out.println("Two arrays with identical contents:");
        System.out.println("  left == right          -> " + (left == right)
                + "   (different objects)");
        System.out.println("  left.equals(right)     -> " + left.equals(right)
                + "   (arrays do NOT override equals - this is identity again)");
        System.out.println("  Arrays.equals(l, r)    -> " + Arrays.equals(left, right)
                + "    <- the one you want");

        System.out.println();
        System.out.println("Printing:");
        System.out.println("  System.out.println(array)      -> " + left);
        System.out.println("    decode that: [ = array, I = of int, @ = identity hash in hex.");
        System.out.println("    Seeing it always means you forgot Arrays.toString.");
        System.out.println("  Arrays.toString(array)         -> " + Arrays.toString(left));


        /* ====================================================================
         * SECTION 7 - ITERATING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE FOUR WAYS TO ITERATE");
        System.out.println("=".repeat(74));

        int[] values = {5, 10, 15, 20};

        System.out.print("  indexed for   : ");
        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i] + " ");
        }
        System.out.println("  <- use when you need the index");

        System.out.print("  enhanced for  : ");
        for (int value : values) {
            System.out.print(value + " ");
        }
        System.out.println("  <- use when you just need the values");

        System.out.print("  backwards     : ");
        for (int i = values.length - 1; i >= 0; i--) {
            System.out.print(values[i] + " ");
        }
        System.out.println("  <- the enhanced for cannot do this");

        System.out.print("  stream        : ");
        Arrays.stream(values).forEach(value -> System.out.print(value + " "));
        System.out.println("  <- composes with filter, map, sum (lesson 54)");

        System.out.println();
        System.out.println("  Arrays.stream(values).sum()     = " + Arrays.stream(values).sum());
        System.out.println("  Arrays.stream(values).max()     = " + Arrays.stream(values).max().getAsInt());
        System.out.println("  Arrays.stream(values).average() = "
                + Arrays.stream(values).average().getAsDouble());


        /* ====================================================================
         * SECTION 8 - ARRAY VS ArrayList, AND COVARIANCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - ARRAY VS ArrayList");
        System.out.println("=".repeat(74));

        int[] fixed = new int[3];
        List<Integer> growable = new ArrayList<>();
        growable.add(1);
        growable.add(2);
        growable.add(3);
        growable.add(4);        // an array could never do this

        System.out.println("  array   : fixed at " + fixed.length + ", holds primitives, uses .length");
        System.out.println("  ArrayList: grew to " + growable.size()
                + ", holds objects only, uses .size()");
        System.out.println();
        System.out.println("  USE AN ARRAY when the size is genuinely fixed, you are storing");
        System.out.println("  primitives in bulk and memory matters, or an API demands one.");
        System.out.println("  USE ArrayList for everything else - which is most code.");

        /* --------------------------------------------------------------------
         * ARRAY COVARIANCE - a deliberate hole in the type system.
         * String[] is treated as a SUBTYPE of Object[], which lets you write
         * code the compiler cannot verify. The JVM re-checks on every store.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("ARRAY COVARIANCE - a hole in the type system:");

        Object[] objects = new String[3];    // legal: arrays are covariant
        System.out.println("  Object[] objects = new String[3];  -> compiles fine");

        objects[0] = "this is a String";     // fine - it really is a String[]
        System.out.println("  objects[0] = \"a String\";           -> fine");

        try {
            objects[1] = 42;                 // compiles! but the real type is String[]
        } catch (ArrayStoreException e) {
            System.out.println("  objects[1] = 42;                   -> compiles, then throws");
            System.out.println("      ArrayStoreException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  The JVM re-checks the type on EVERY array store to catch this.");
        System.out.println("  Generics were deliberately designed NOT to work this way:");
        System.out.println("    List<String> is NOT a List<Object>");
        System.out.println("  so the equivalent generic mistake is a COMPILE error instead.");
        System.out.println("  Lesson 43 explains the trade-off.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 12.");
        System.out.println("=".repeat(74));
    }

    /**
     * Sums an int array. Exists mainly to show that passing a literal array as
     * an argument requires the explicit `new int[]{...}` form.
     *
     * @param values the numbers to add up
     * @return their total
     */
    static int sum(int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    /**
     * Reads one element. Kept as a separate method so the timing loop in
     * Section 4 measures an actual array access rather than letting the JIT
     * hoist a constant read out of the loop entirely.
     *
     * @param array the array to read from
     * @param index the index to read
     * @return the value at that index
     */
    static int readAt(int[] array, int index) {
        return array[index];
    }

    /**
     * Times repeated reads of one index and returns the average in nanoseconds.
     * The running total is accumulated and folded into the result so the JIT
     * cannot delete the loop as having no effect - a real hazard when writing
     * microbenchmarks by hand.
     *
     * @param array       the array to read
     * @param index       the index to read repeatedly
     * @param repetitions how many reads to perform
     * @return the average nanoseconds per read
     */
    static double timeReads(int[] array, int index, int repetitions) {
        long start = System.nanoTime();
        long checksum = 0;
        for (int i = 0; i < repetitions; i++) {
            checksum += readAt(array, index);
        }
        long elapsed = System.nanoTime() - start;

        // Consume the checksum so the loop cannot be optimised away entirely.
        if (checksum == Long.MIN_VALUE) {
            System.out.print("");
        }
        return (double) elapsed / repetitions;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write `int[] reverse(int[] a)` that returns a NEW reversed array without
 *    modifying the input. Then write `void reverseInPlace(int[] a)` that
 *    swaps ends inward. Which one can the caller misuse?
 *
 * 2. Find the second-largest value in an int[] in ONE pass. Handle arrays of
 *    length 0 and 1 sensibly - decide what "sensibly" means and document it.
 *
 * 3. Write `int[] grow(int[] a)` that returns an array twice the length with
 *    the original contents at the front. You have now written the core of
 *    ArrayList.
 *
 * 4. Predict, then verify:
 *        int[][] grid = new int[2][3];
 *        System.out.println(grid.length);
 *        System.out.println(grid[0].length);
 *        System.out.println(grid[0]);
 *        System.out.println(Arrays.toString(grid));
 *        System.out.println(Arrays.deepToString(grid));
 *
 * 5. Write `boolean isSorted(int[] a)` returning true for empty and
 *    single-element arrays. Then use it to prove that Arrays.binarySearch on
 *    an UNSORTED array returns a wrong answer without throwing.
 *
 * 6. Time filling an int[1_000_000] against an ArrayList<Integer> of the same
 *    size. Then check the memory difference with `-verbose:gc`. The gap is
 *    boxing, and lesson 42 explains it.
 * ============================================================================
 */
