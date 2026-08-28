/* ============================================================================
 * 16 - THE java.util.Arrays UTILITY CLASS
 * ----------------------------------------------------------------------------
 * Companion lesson: 16-arrays-utility-class.md
 *
 * RUN IT:
 *     java Java/04-arrays-and-strings/16-arrays-utility-class.java
 *
 * An array has almost no methods of its own - just the `length` field and
 * clone(). Everything else lives here as static helpers. Knowing what is in
 * this class saves you writing loops the JDK already wrote correctly.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

class ArraysUtilityClass {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - SORTING, AND THE TWO ALGORITHMS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - SORTING");
        System.out.println("=".repeat(74));

        int[] numbers = {5, 2, 9, 1, 7, 3};
        System.out.println("  before          -> " + Arrays.toString(numbers));

        Arrays.sort(numbers);       // sorts IN PLACE and returns void
        System.out.println("  Arrays.sort(a)  -> " + Arrays.toString(numbers));

        int[] partial = {5, 2, 9, 1, 7, 3};
        Arrays.sort(partial, 1, 4);     // [1, 4) - end exclusive
        System.out.println("  sort(a, 1, 4)   -> " + Arrays.toString(partial)
                + "   only indices 1..3 moved");

        System.out.println();
        System.out.println("  Note sort() returns VOID and mutates the array. It does not");
        System.out.println("  hand you a sorted copy. If you need the original, copy first:");
        int[] keepOriginal = {3, 1, 2};
        int[] sortedCopy = keepOriginal.clone();
        Arrays.sort(sortedCopy);
        System.out.println("    original " + Arrays.toString(keepOriginal)
                + "  sorted copy " + Arrays.toString(sortedCopy));

        /* --------------------------------------------------------------------
         * TWO DIFFERENT ALGORITHMS, AND WHY.
         *   primitives -> dual-pivot quicksort, UNSTABLE, less memory
         *   objects    -> TimSort, STABLE
         * Stability means equal elements keep their original relative order.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("STABILITY - why objects and primitives use different algorithms:");

        // Two people with the same age but different names compare "equal" when
        // sorting by age. A STABLE sort keeps them in their original order.
        String[][] people = {
                {"Danish", "25"},
                {"Aisha", "22"},
                {"Rahul", "25"},
                {"Priya", "22"}
        };

        System.out.println("  original order:");
        for (String[] person : people) {
            System.out.println("    " + person[0] + " (" + person[1] + ")");
        }

        Arrays.sort(people, Comparator.comparing(person -> Integer.parseInt(person[1])));

        System.out.println("  after a STABLE sort by age only:");
        for (String[] person : people) {
            System.out.println("    " + person[0] + " (" + person[1] + ")");
        }
        System.out.println("  Aisha still precedes Priya, and Danish still precedes Rahul.");
        System.out.println("  That is stability, and it is what lets you sort by one key");
        System.out.println("  and then another to get a sensible combined ordering.");

        System.out.println();
        System.out.println("  Primitives get dual-pivot QUICKSORT (unstable, less memory):");
        System.out.println("    two equal ints are indistinguishable, so stability is");
        System.out.println("    meaningless and the JDK takes the faster algorithm.");
        System.out.println("  Objects get TIMSORT (stable, a merge sort variant):");
        System.out.println("    two objects can compare equal yet differ, so order matters.");

        /* --------------------------------------------------------------------
         * SORTING OBJECTS WITH COMPARATORS. Lesson 49 covers these properly.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("Sorting objects with comparators:");

        String[] names = {"Rahul", "aisha", "Danish", "bob", "Priya"};
        System.out.println("  original                       -> " + Arrays.toString(names));

        String[] natural = names.clone();
        Arrays.sort(natural);
        System.out.println("  natural order                  -> " + Arrays.toString(natural));
        System.out.println("    ^ ALL uppercase before ALL lowercase: natural String order");
        System.out.println("      is by Unicode code point, and 'Z' (90) < 'a' (97).");

        String[] caseInsensitive = names.clone();
        Arrays.sort(caseInsensitive, String.CASE_INSENSITIVE_ORDER);
        System.out.println("  CASE_INSENSITIVE_ORDER         -> " + Arrays.toString(caseInsensitive));

        String[] byLength = names.clone();
        Arrays.sort(byLength, Comparator.comparing(String::length));
        System.out.println("  by length                      -> " + Arrays.toString(byLength));

        String[] byLengthThenAlpha = names.clone();
        Arrays.sort(byLengthThenAlpha, Comparator.comparing(String::length)
                .thenComparing(String.CASE_INSENSITIVE_ORDER));
        System.out.println("  by length, then alphabetically -> " + Arrays.toString(byLengthThenAlpha));

        String[] reversed = names.clone();
        Arrays.sort(reversed, Comparator.reverseOrder());
        System.out.println("  reverse order                  -> " + Arrays.toString(reversed));

        System.out.println();
        System.out.println("  A PRIMITIVE array cannot take a comparator - there is no");
        System.out.println("  Comparator<int>. Box it to Integer[], or sort ascending and");
        System.out.println("  reverse manually:");
        int[] descending = {5, 2, 9, 1};
        Arrays.sort(descending);
        for (int i = 0, j = descending.length - 1; i < j; i++, j--) {
            int swap = descending[i];
            descending[i] = descending[j];
            descending[j] = swap;
        }
        System.out.println("    sorted then reversed -> " + Arrays.toString(descending));

        System.out.println();
        System.out.println("  parallelSort() splits the work across threads. Worth it only");
        System.out.println("  for large arrays - roughly tens of thousands of elements up.");
        System.out.println("  Below that the coordination costs more than it saves.");


        /* ====================================================================
         * SECTION 2 - binarySearch AND ITS SILENT PRECONDITION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - binarySearch");
        System.out.println("=".repeat(74));

        int[] sorted = {10, 20, 30, 40, 50};
        System.out.println("  sorted array -> " + Arrays.toString(sorted));
        System.out.println("    binarySearch(30) = " + Arrays.binarySearch(sorted, 30)
                + "    <- found at index 2");
        System.out.println("    binarySearch(10) = " + Arrays.binarySearch(sorted, 10));

        // The negative return encodes WHERE the key would go.
        int missing = Arrays.binarySearch(sorted, 35);
        System.out.println("    binarySearch(35) = " + missing + "   <- not found");
        System.out.println("      the formula is -(insertionPoint) - 1, so:");
        System.out.println("      insertion point = -(" + missing + ") - 1 = " + (-missing - 1));
        System.out.println("      i.e. 35 belongs at index " + (-missing - 1) + ", between 30 and 40.");
        System.out.println("      That lets you find a nearest neighbour or insert while");
        System.out.println("      keeping the array sorted, in a single call.");

        /* --------------------------------------------------------------------
         * THE PRECONDITION. On an unsorted array binarySearch returns garbage
         * and DOES NOT THROW. One of the quieter bugs in the standard library.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE SILENT PRECONDITION - the array MUST be sorted:");

        int[] unsorted = {50, 40, 30, 20, 10};   // sorted DESCENDING - still "unsorted"
        System.out.println("    array (descending) -> " + Arrays.toString(unsorted));
        System.out.println("    binarySearch(50)   -> " + Arrays.binarySearch(unsorted, 50)
                + "   claims NOT FOUND");
        System.out.println("    but 50 is right there at index 0:");
        System.out.println("    indexOf(unsorted, 50) -> " + indexOf(unsorted, 50));
        System.out.println();
        System.out.println("    No exception. No warning. Just a confidently wrong answer,");
        System.out.println("    because binarySearch assumed it could halve the range using");
        System.out.println("    ASCENDING order and threw away the half containing the value.");
        System.out.println();
        System.out.println("    Worse: sometimes it is accidentally RIGHT. Searching this");
        System.out.println("    same array for 30 gives " + Arrays.binarySearch(unsorted, 30)
                + ", which happens to be correct");
        System.out.println("    because 30 sits at the midpoint. A bug that is right half");
        System.out.println("    the time is far harder to find than one that always fails.");


        /* ====================================================================
         * SECTION 3 - FILLING AND POPULATING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - fill AND setAll");
        System.out.println("=".repeat(74));

        int[] filled = new int[8];
        Arrays.fill(filled, 7);
        System.out.println("  fill(a, 7)            -> " + Arrays.toString(filled));

        Arrays.fill(filled, 2, 5, 0);
        System.out.println("  fill(a, 2, 5, 0)      -> " + Arrays.toString(filled)
                + "   [2, 5) - end exclusive");

        // setAll computes each element FROM ITS INDEX. It replaces the whole
        // "allocate, then loop to populate" pattern with one line.
        int[] squares = new int[8];
        Arrays.setAll(squares, index -> index * index);
        System.out.println("  setAll(a, i -> i * i) -> " + Arrays.toString(squares) + "   (Java 8+)");

        String[] labels = new String[5];
        Arrays.setAll(labels, index -> "item-" + index);
        System.out.println("  setAll on String[]    -> " + Arrays.toString(labels));

        /* --------------------------------------------------------------------
         * THE 2D fill TRAP. Filling the OUTER array puts the SAME row object
         * into every slot.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE 2D fill TRAP:");

        int[][] shared = new int[3][];
        Arrays.fill(shared, new int[]{1, 1, 1});     // ONE row object, three references
        shared[0][0] = 99;

        System.out.println("    Arrays.fill(grid, new int[]{1,1,1}); grid[0][0] = 99;");
        System.out.println("      -> " + Arrays.deepToString(shared));
        System.out.println("      All three rows changed: they are the SAME object.");
        System.out.println("      grid[0] == grid[1] -> " + (shared[0] == shared[1]));

        int[][] correct = new int[3][3];
        for (int[] row : correct) {
            Arrays.fill(row, 1);        // fill each row separately
        }
        correct[0][0] = 99;
        System.out.println();
        System.out.println("    Filling each ROW instead:");
        System.out.println("      -> " + Arrays.deepToString(correct) + "   correct");


        /* ====================================================================
         * SECTION 4 - COPYING AND COMPARING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - COPYING AND COMPARING");
        System.out.println("=".repeat(74));

        int[] source = {1, 2, 3, 4, 5};
        System.out.println("  source                       -> " + Arrays.toString(source));
        System.out.println("  copyOf(a, 3)                 -> " + Arrays.toString(Arrays.copyOf(source, 3))
                + "         truncated");
        System.out.println("  copyOf(a, 8)                 -> " + Arrays.toString(Arrays.copyOf(source, 8))
                + "   padded with defaults");
        System.out.println("  copyOfRange(a, 1, 4)         -> "
                + Arrays.toString(Arrays.copyOfRange(source, 1, 4)) + "         [1, 4)");

        System.out.println();
        System.out.println("  Comparing:");
        int[] left = {1, 2, 3};
        int[] right = {1, 2, 3};
        int[] different = {1, 2, 4};

        System.out.println("    equals({1,2,3}, {1,2,3})   -> " + Arrays.equals(left, right));
        System.out.println("    equals({1,2,3}, {1,2,4})   -> " + Arrays.equals(left, different));
        System.out.println("    compare({1,2,3}, {1,2,4})  -> " + Arrays.compare(left, different)
                + "   lexicographic, Java 9+");
        System.out.println("    mismatch({1,2,3}, {1,2,4}) -> " + Arrays.mismatch(left, different)
                + "    <- the INDEX of the first difference");
        System.out.println("    mismatch({1,2,3}, {1,2,3}) -> " + Arrays.mismatch(left, right)
                + "   -1 means identical");
        System.out.println();
        System.out.println("    mismatch is excellent for diagnostics: instead of learning");
        System.out.println("    THAT two arrays differ, you learn WHERE.");

        System.out.println();
        System.out.println("    hashCode({1,2,3})          -> " + Arrays.hashCode(left));
        System.out.println("    hashCode({1,2,3}) again    -> " + Arrays.hashCode(right)
                + "   equal contents, equal hash");


        /* ====================================================================
         * SECTION 5 - Arrays.asList AND ITS TWO TRAPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - Arrays.asList: TWO TRAPS");
        System.out.println("=".repeat(74));

        String[] backing = {"a", "b", "c"};
        List<String> view = Arrays.asList(backing);

        System.out.println("  TRAP 1 - it is a fixed-size VIEW, not a copy.");
        System.out.println("    list          -> " + view);

        view.set(0, "z");   // allowed: writes THROUGH to the array
        System.out.println("    list.set(0, \"z\")  -> list " + view);
        System.out.println("                       -> array " + Arrays.toString(backing)
                + "   <- the ARRAY changed too");

        try {
            view.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("    list.add(\"d\")     -> UnsupportedOperationException");
        }
        try {
            view.remove("a");
        } catch (UnsupportedOperationException e) {
            System.out.println("    list.remove(\"a\")  -> UnsupportedOperationException");
        }
        System.out.println("    The size is fixed because the backing ARRAY's size is fixed.");

        List<String> realList = new ArrayList<>(Arrays.asList(backing));
        realList.add("d");
        System.out.println();
        System.out.println("    For a real, modifiable list, wrap it:");
        System.out.println("      new ArrayList<>(Arrays.asList(array)) -> " + realList);

        /* --------------------------------------------------------------------
         * TRAP 2 - primitives. Generics cannot hold primitives, so an int[]
         * matches the single-varargs-element overload.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  TRAP 2 - it does not do what you expect on a primitive array:");

        int[] primitives = {1, 2, 3};
        List<int[]> wrong = Arrays.asList(primitives);
        System.out.println("    Arrays.asList(int[]{1,2,3}).size()     -> " + wrong.size()
                + "   <- ONE element: the array itself!");

        Integer[] boxedArray = {1, 2, 3};
        List<Integer> right2 = Arrays.asList(boxedArray);
        System.out.println("    Arrays.asList(Integer[]{1,2,3}).size() -> " + right2.size()
                + "   correct");

        List<Integer> viaStream = Arrays.stream(primitives).boxed().toList();
        System.out.println("    Arrays.stream(int[]).boxed().toList()  -> " + viaStream
                + "   <- the right way");
        System.out.println();
        System.out.println("    WHY: generics cannot hold primitives, so int[] matches the");
        System.out.println("    varargs overload as a SINGLE element of type int[].");


        /* ====================================================================
         * SECTION 6 - STREAMS OVER ARRAYS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - Arrays.stream");
        System.out.println("=".repeat(74));

        int[] data = {4, 8, 15, 16, 23, 42};
        System.out.println("  data -> " + Arrays.toString(data));
        System.out.println();
        System.out.println("    sum()                    = " + Arrays.stream(data).sum());
        System.out.println("    max()                    = " + Arrays.stream(data).max().getAsInt());
        System.out.println("    min()                    = " + Arrays.stream(data).min().getAsInt());
        System.out.println("    average()                = " + Arrays.stream(data).average().orElse(0));
        System.out.println("    count()                  = " + Arrays.stream(data).count());
        System.out.println("    filter(n > 15).toArray() = "
                + Arrays.toString(Arrays.stream(data).filter(n -> n > 15).toArray()));
        System.out.println("    map(n -> n * 2)          = "
                + Arrays.toString(Arrays.stream(data).map(n -> n * 2).toArray()));
        System.out.println("    boxed().toList()         = " + Arrays.stream(data).boxed().toList());
        System.out.println("    summaryStatistics()      = " + Arrays.stream(data).summaryStatistics());

        System.out.println();
        System.out.println("  Arrays.stream(int[]) gives an IntStream, which has sum(),");
        System.out.println("  average() and max() directly. Arrays.stream(Object[]) gives a");
        System.out.println("  Stream<T>, which does not - you would mapToInt() first.");
        System.out.println("  Lesson 54 covers streams properly.");


        /* ====================================================================
         * SECTION 7 - QUICK REFERENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE WHOLE CLASS, ON ONE SCREEN");
        System.out.println("=".repeat(74));

        System.out.printf("    %-28s %s%n", "TASK", "CALL");
        System.out.printf("    %-28s %s%n", "Print", "Arrays.toString / deepToString");
        System.out.printf("    %-28s %s%n", "Sort", "Arrays.sort (+ Comparator for objects)");
        System.out.printf("    %-28s %s%n", "Sort in parallel", "Arrays.parallelSort");
        System.out.printf("    %-28s %s%n", "Search a SORTED array", "Arrays.binarySearch");
        System.out.printf("    %-28s %s%n", "Fill", "Arrays.fill");
        System.out.printf("    %-28s %s%n", "Populate from the index", "Arrays.setAll");
        System.out.printf("    %-28s %s%n", "Copy or resize", "Arrays.copyOf");
        System.out.printf("    %-28s %s%n", "Slice", "Arrays.copyOfRange");
        System.out.printf("    %-28s %s%n", "Compare", "Arrays.equals / deepEquals");
        System.out.printf("    %-28s %s%n", "Find first difference", "Arrays.mismatch");
        System.out.printf("    %-28s %s%n", "Hash", "Arrays.hashCode / deepHashCode");
        System.out.printf("    %-28s %s%n", "To a modifiable List", "new ArrayList<>(Arrays.asList(a))");
        System.out.printf("    %-28s %s%n", "To a Stream", "Arrays.stream(a)");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 16.");
        System.out.println("=".repeat(74));
    }

    /**
     * A plain linear search, shown alongside binarySearch to make the trade-off
     * concrete: O(n) but with no precondition at all.
     *
     * @param array  the array to scan
     * @param target the value to find
     * @return the index of the first match, or -1
     */
    static int indexOf(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Use Arrays.setAll to build an int[20] of Fibonacci numbers. You will find
 *    you cannot - the lambda only receives the index. Explain why, then do it
 *    with a plain loop.
 *
 * 2. Write `int insertSorted(int[] sorted, int size, int value)` that uses the
 *    negative return of binarySearch to find the insertion point, shifts the
 *    tail with System.arraycopy, and returns the new size.
 *
 * 3. Sort a String[] by length descending, then alphabetically ascending for
 *    ties. One Comparator chain, no loops.
 *
 * 4. Prove TimSort is stable and dual-pivot quicksort is not: sort an
 *    Integer[] of duplicates and confirm order is preserved, then explain why
 *    you cannot even ASK the question of an int[].
 *
 * 5. Take a 2D array, use Arrays.fill wrongly (sharing rows), and find the bug
 *    by printing grid[0] == grid[1]. Then fix it.
 *
 * 6. Given int[] a and int[] b, print the index of their first difference and
 *    both values there. Use Arrays.mismatch, and handle the -1 case.
 * ============================================================================
 */
