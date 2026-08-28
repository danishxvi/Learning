/* ============================================================================
 * 11 - break, continue, LABELS AND LOOP DESIGN
 * ----------------------------------------------------------------------------
 * Companion lesson: 11-break-continue-and-labels.md
 *
 * RUN IT:
 *     java Java/03-control-flow/11-break-continue-and-labels.java
 *
 * Three small keywords, plus the question they force you to ask: is this loop
 * the right shape at all?
 * ============================================================================
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class BreakContinueAndLabels {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - break: LEAVE THE LOOP NOW
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - break");
        System.out.println("=".repeat(74));

        System.out.print("  for i in 0..9, break at 5  ->  ");
        for (int i = 0; i < 10; i++) {
            if (i == 5) {
                break;      // leaves the loop; execution resumes after it
            }
            System.out.print(i + " ");
        }
        System.out.println();

        // The main use: search and stop. Once you have the answer, continuing
        // is wasted work - and on a large collection that waste is real.
        String[] names = {"Aisha", "Rahul", "Danish", "Priya", "Deepa"};

        String firstDName = null;
        int comparisons = 0;
        for (String name : names) {
            comparisons++;
            if (name.startsWith("D")) {
                firstDName = name;
                break;      // stop - we have what we came for
            }
        }
        System.out.println();
        System.out.println("  Searching " + Arrays.toString(names) + " for a name starting with D:");
        System.out.println("    found \"" + firstDName + "\" after " + comparisons
                + " comparisons out of " + names.length);
        System.out.println("    without break it would have checked all " + names.length
                + " and found \"Deepa\" instead");


        /* ====================================================================
         * SECTION 2 - continue: SKIP THIS ITERATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - continue");
        System.out.println("=".repeat(74));

        System.out.print("  odd numbers under 10 via continue  ->  ");
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                continue;   // skip the REST of this iteration
            }
            System.out.print(i + " ");
        }
        System.out.println();

        /* --------------------------------------------------------------------
         * THE CRITICAL DIFFERENCE BETWEEN LOOP TYPES.
         * In a for loop, continue STILL RUNS THE UPDATE clause.
         * In a while loop it does NOT - it jumps straight to the condition.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("THE DIFFERENCE THAT CAUSES INFINITE LOOPS:");
        System.out.println();
        System.out.println("  In a FOR loop, continue still runs the update (i++):");
        System.out.print("    ");
        for (int i = 0; i < 5; i++) {
            if (i == 2) {
                continue;   // i++ in the header still runs - safe
            }
            System.out.print(i + " ");
        }
        System.out.println("  <- terminated normally");

        System.out.println();
        System.out.println("  In a WHILE loop it does NOT. This shape hangs forever:");
        System.out.println("      int i = 0;");
        System.out.println("      while (i < 5) {");
        System.out.println("          if (i == 2) continue;   // i is never incremented!");
        System.out.println("          System.out.println(i);");
        System.out.println("          i++;                    // never reached when i == 2");
        System.out.println("      }");

        // The same loop written safely: update BEFORE the continue.
        System.out.print("  Written safely (update before the continue):  ");
        int i = 0;
        while (i < 5) {
            int current = i;
            i++;                        // update first, so continue cannot skip it
            if (current == 2) {
                continue;
            }
            System.out.print(current + " ");
        }
        System.out.println("  <- terminated");


        /* ====================================================================
         * SECTION 3 - LABELS: ESCAPING NESTED LOOPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - LABELLED break AND continue");
        System.out.println("=".repeat(74));

        int[][] grid = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        int target = 5;

        // WITHOUT a label, break only escapes the INNER loop.
        System.out.println("Plain break only escapes the INNER loop:");
        int visitedWithoutLabel = 0;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                visitedWithoutLabel++;
                if (grid[row][col] == target) {
                    break;      // leaves the inner loop; the outer one continues
                }
            }
        }
        System.out.println("    visited " + visitedWithoutLabel
                + " cells - the outer loop kept going after the match");

        // WITH a label, break escapes both.
        System.out.println();
        System.out.println("Labelled break escapes BOTH loops:");
        int visitedWithLabel = 0;
        int foundRow = -1, foundCol = -1;

        search:                                     // the label
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                visitedWithLabel++;
                if (grid[row][col] == target) {
                    foundRow = row;
                    foundCol = col;
                    break search;                   // leaves the LABELLED loop
                }
            }
        }
        System.out.println("    found " + target + " at [" + foundRow + "][" + foundCol
                + "] after visiting " + visitedWithLabel + " cells");

        // continue with a label abandons the current outer iteration.
        System.out.println();
        System.out.println("Labelled continue - abandon this row, move to the next:");
        outer:
        for (int row = 0; row < grid.length; row++) {
            System.out.print("    row " + row + ": ");
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] % 2 == 0) {
                    System.out.println("hit an even number, skipping the rest of this row");
                    continue outer;                 // next ROW, not next column
                }
                System.out.print(grid[row][col] + " ");
            }
            System.out.println("(row completed)");
        }

        System.out.println();
        System.out.println("Note: `goto` is a RESERVED but unimplemented Java keyword. It");
        System.out.println("exists on the keyword list only so nobody can use it as a name.");
        System.out.println("Labelled break/continue are the deliberate structured substitute.");


        /* ====================================================================
         * SECTION 4 - break INSIDE A switch INSIDE A LOOP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - break BINDS TO THE INNERMOST BREAKABLE THING");
        System.out.println("=".repeat(74));

        System.out.print("  break inside a switch  ->  ");
        for (int n = 0; n < 5; n++) {
            switch (n) {
                case 2:
                    break;      // breaks the SWITCH, not the loop
            }
            System.out.print(n + " ");
        }
        System.out.println("  <- every number still printed");

        System.out.print("  labelled break        ->  ");
        loop:
        for (int n = 0; n < 5; n++) {
            switch (n) {
                case 2:
                    break loop;     // now it leaves the LOOP
            }
            System.out.print(n + " ");
        }
        System.out.println("  <- stopped at 2");

        System.out.println();
        System.out.println("  A switch is a breakable construct, so a plain break inside");
        System.out.println("  one never escapes the surrounding loop. This is another");
        System.out.println("  reason to prefer arrow-form switch, which uses no break.");


        /* ====================================================================
         * SECTION 5 - return: THE CLEANEST EXIT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - return EXITS EVERYTHING");
        System.out.println("=".repeat(74));

        System.out.println("The same 2D search, written as a method with return:");
        int[] location = findInGrid(grid, target);
        System.out.println("    findInGrid(grid, " + target + ") -> " + Arrays.toString(location));
        System.out.println("    findInGrid(grid, 99) -> " + Arrays.toString(findInGrid(grid, 99)));
        System.out.println();
        System.out.println("  No label, no flag variable, and the method NAME says what the");
        System.out.println("  loop is for. Reach for this before reaching for a label.");

        System.out.println();
        System.out.println("A linear search reads best the same way:");
        System.out.println("    contains(names, \"Danish\") -> " + contains(names, "Danish"));
        System.out.println("    contains(names, \"Zara\")   -> " + contains(names, "Zara"));
        System.out.println("  Note: no break, no result variable. The structure carries it.");


        /* ====================================================================
         * SECTION 6 - WHEN THE LOOP IS THE WRONG SHAPE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - continue GUARDS ARE OFTEN A FILTER");
        System.out.println("=".repeat(74));

        List<String> rawNames = Arrays.asList("Danish", "", "Al", "Priya", "  ", "Rahul");

        System.out.println("Input: " + rawNames);
        System.out.println();
        System.out.println("With continue guards:");
        for (String name : rawNames) {
            if (name.isBlank()) continue;
            if (name.length() < 3) continue;
            System.out.println("    processing " + name);
        }

        System.out.println();
        System.out.println("As a stream - the same logic, now stated rather than implied:");
        rawNames.stream()
                .filter(name -> !name.isBlank())
                .filter(name -> name.length() >= 3)
                .forEach(name -> System.out.println("    processing " + name));

        System.out.println();
        System.out.println("A break search is usually a findFirst:");
        Optional<String> firstLongName = rawNames.stream()
                .filter(name -> name.length() > 4)
                .findFirst();
        System.out.println("    findFirst(length > 4) -> " + firstLongName.orElse("none"));
        System.out.println("  findFirst is LAZY - it stops at the first match, exactly as");
        System.out.println("  break does. You lose no performance and gain a clear intent.");

        System.out.println();
        System.out.println("This is NOT a rule that streams always win. Loops are clearer");
        System.out.println("when you need an index, when the body is long, or when you are");
        System.out.println("mutating state. But a body that is mostly continue guards is a");
        System.out.println("filter wearing a disguise. Lesson 54 covers streams properly.");


        /* ====================================================================
         * SECTION 7 - THE LOOP DESIGN CHECKLIST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - CHECKLIST BEFORE YOU FINISH A LOOP");
        System.out.println("=".repeat(74));

        System.out.println("  1. Can this ever fail to terminate?");
        System.out.println("       The update must move TOWARD the condition.");
        System.out.println("  2. Should the bound be < or <=?");
        System.out.println("       The single most common off-by-one source.");
        System.out.println("  3. Does a continue skip the update in a while loop?");
        System.out.println("       Classic infinite loop. Update before the continue.");
        System.out.println("  4. Am I modifying the collection I am iterating?");
        System.out.println("       ConcurrentModificationException. Use removeIf.");
        System.out.println("  5. Am I building a String with += ?");
        System.out.println("       O(n^2). Use StringBuilder.");
        System.out.println("  6. Does this really need to be nested?");
        System.out.println("       Two nested loops over n items is n^2 work.");
        System.out.println("  7. Would a method with return beat this label?");
        System.out.println("       Usually yes.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 11.");
        System.out.println("=".repeat(74));
    }

    /**
     * Searches a 2D array, returning as soon as it finds the target. This is
     * the labelled-break version rewritten as a method: `return` exits both
     * loops with no label, and the method name documents the intent.
     *
     * @param grid   the array to search
     * @param target the value to find
     * @return a two-element array of {row, column}, or {-1, -1} if not found
     */
    static int[] findInGrid(int[][] grid, int target) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] == target) {
                    return new int[]{row, col};   // exits BOTH loops and the method
                }
            }
        }
        return new int[]{-1, -1};
    }

    /**
     * A linear search written with return rather than break. Note there is no
     * result variable and no break - the structure carries the meaning.
     *
     * @param values the array to search
     * @param target the value to look for
     * @return true if the array contains the target
     */
    static boolean contains(String[] values, String target) {
        for (String value : values) {
            if (value.equals(target)) {
                return true;
            }
        }
        return false;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write a loop that prints numbers 1 to 100 but stops at the first multiple
 *    of 7 that is also a multiple of 3. Then rewrite it as a method with return
 *    and decide which you prefer.
 *
 * 2. Reproduce the while-loop infinite loop from Section 2 in a scratch file.
 *    Let it run, press Ctrl+C, then fix it two different ways.
 *
 * 3. Given a 2D int array, find the first row whose values sum to more than
 *    100. Write it twice: once with a labelled break, once as a method with
 *    return.
 *
 * 4. Print all pairs (i, j) from 1..5 where i + j is prime, skipping any pair
 *    where i == j. Use continue for the skip.
 *
 * 5. Predict the output, then run it:
 *        for (int i = 0; i < 3; i++) {
 *            for (int j = 0; j < 3; j++) {
 *                if (j == 1) break;
 *                System.out.println(i + "," + j);
 *            }
 *        }
 *    Now add a label to the outer loop and change it to `break outer;`.
 *
 * 6. Take a loop of yours that is mostly continue guards and rewrite it as a
 *    stream with filter. Then write down one reason the loop version might
 *    still be the better choice.
 * ============================================================================
 */
