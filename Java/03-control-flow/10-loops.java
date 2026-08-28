/* ============================================================================
 * 10 - LOOPS: for, while, do-while AND THE ENHANCED for
 * ----------------------------------------------------------------------------
 * Companion lesson: 10-loops.md
 *
 * RUN IT:
 *     java Java/03-control-flow/10-loops.java
 *
 * Four constructs, one question: what do you know before the loop starts?
 * The bugs are almost all off-by-one errors and accidental infinite loops,
 * so those get the most space here.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

class Loops {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE CLASSIC for, AND ITS EXACT EXECUTION ORDER
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE CLASSIC for LOOP");
        System.out.println("=".repeat(74));

        System.out.print("  for (int i = 0; i < 5; i++)  ->  ");
        for (int i = 0; i < 5; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        // Tracing the order explicitly makes off-by-one errors obvious.
        System.out.println();
        System.out.println("The exact order, traced:");
        System.out.println("  1. init      - once, before anything else");
        System.out.println("  2. condition - if false, the loop ends IMMEDIATELY");
        System.out.println("  3. body");
        System.out.println("  4. update");
        System.out.println("  5. back to step 2");
        System.out.println();

        for (int i = 0; trace("condition i<2", i < 2); i++) {
            System.out.println("    body with i = " + i);
        }
        System.out.println("    (the loop ended on the condition check, before any body)");

        // Every clause is optional.
        System.out.println();
        System.out.println("Every clause is optional:");
        int external = 0;
        for (; external < 3; external++) {
            // initialisation happened outside
        }
        System.out.println("  init outside the header    -> external ended at " + external);

        int updatedInBody = 0;
        for (; updatedInBody < 3;) {
            updatedInBody++;    // update inside the body
        }
        System.out.println("  update inside the body     -> reached " + updatedInBody);
        System.out.println("  for (;;) is an infinite loop, identical to while (true)");

        // Multiple variables. They must all be the SAME type - the comma here
        // is a for-loop separator, not the comma operator from C.
        System.out.println();
        System.out.println("Two counters moving toward each other:");
        for (int low = 0, high = 6; low < high; low++, high--) {
            System.out.println("    low = " + low + ", high = " + high);
        }

        // Scope: the loop variable dies with the loop.
        System.out.println();
        System.out.println("Scope - `i` does not exist after the loop:");
        System.out.println("  System.out.println(i);  ->  cannot find symbol (see the source)");


        /* ====================================================================
         * SECTION 2 - while
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - while: LOOP UNTIL SOMETHING CHANGES");
        System.out.println("=".repeat(74));

        // Use while when the iteration count is not known up front.
        int remaining = 100;
        int withdrawals = 0;
        System.out.println("Draining a balance of 100 in variable-sized chunks:");
        while (remaining > 0) {
            int chunk = Math.min(remaining, 30);
            remaining -= chunk;
            withdrawals++;
            System.out.println("    took " + chunk + ", " + remaining + " left");
        }
        System.out.println("  took " + withdrawals + " withdrawals - a count we did not know in advance");

        /* --------------------------------------------------------------------
         * THE THREE WAYS TO WRITE AN INFINITE LOOP BY ACCIDENT.
         * Each is shown with a safety counter so this file still terminates.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("INFINITE LOOP CAUSE 1 - forgetting the update:");
        System.out.println("    int i = 0;");
        System.out.println("    while (i < 5) { System.out.println(i); }   // no i++");
        System.out.println("  i never changes, so the condition never becomes false.");

        System.out.println();
        System.out.println("INFINITE LOOP CAUSE 2 - != on floating point:");
        double x = 0.0;
        int guard = 0;
        // The loop below would run forever with != because 0.1 cannot be
        // represented exactly, so x skips straight past 1.0.
        while (x != 1.0 && guard < 20) {
            x += 0.1;
            guard++;
        }
        System.out.println("    while (x != 1.0) { x += 0.1; }");
        System.out.println("  after 20 additions x = " + x + ", which is NOT 1.0");
        System.out.println("  x jumped from " + (x - 0.1) + " past 1.0 without ever equalling it.");
        System.out.println("  RULE: use < or <= with floating point, never != or ==.");

        System.out.println();
        System.out.println("INFINITE LOOP CAUSE 3 - the stray semicolon:");
        System.out.println("    while (x > 0);        // <- empty body");
        System.out.println("    { x--; }              // <- an ordinary block, never reached");
        System.out.println("  The semicolon IS the loop body. Nothing decrements x.");


        /* ====================================================================
         * SECTION 3 - do-while: THE BODY ALWAYS RUNS ONCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - do-while");
        System.out.println("=".repeat(74));

        // Same false condition, two loops, two different outcomes.
        int neverTrue = 100;

        System.out.println("With a condition that is false from the start:");

        System.out.print("  while  -> ");
        while (neverTrue < 5) {
            System.out.print("ran ");
        }
        System.out.println("(body never ran)");

        System.out.print("  do-while -> ");
        do {
            System.out.print("ran ");
        } while (neverTrue < 5);
        System.out.println("(body ran ONCE before the test)");

        System.out.println();
        System.out.println("Note the SEMICOLON after the while(...) of a do-while.");
        System.out.println("Leaving it out is a compile error.");

        System.out.println();
        System.out.println("Where do-while is the natural fit - a menu or retry loop:");
        int[] simulatedChoices = {3, 1, 0};   // stands in for user input
        int index = 0;
        int choice;
        do {
            choice = simulatedChoices[index++];
            System.out.println("    showing the menu, user chose " + choice);
        } while (choice != 0);
        System.out.println("  With a plain while you would have to duplicate the prompt");
        System.out.println("  before the loop to get the first choice.");


        /* ====================================================================
         * SECTION 4 - THE ENHANCED for
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE ENHANCED for (FOR-EACH)");
        System.out.println("=".repeat(74));

        String[] names = {"Danish", "Aisha", "Rahul", "Priya"};

        System.out.println("Indexed for - you manage the counter and the bound:");
        for (int i = 0; i < names.length; i++) {
            System.out.println("    " + i + ": " + names[i]);
        }

        System.out.println();
        System.out.println("Enhanced for - read the colon as 'in':");
        for (String name : names) {
            System.out.println("    " + name);
        }
        System.out.println("  No counter, no bound, so no off-by-one error is possible.");

        // It works on anything Iterable, which is every collection.
        List<Integer> scores = List.of(88, 92, 79);
        int total = 0;
        for (int score : scores) {     // note: int, not Integer - auto-unboxed
            total += score;
        }
        System.out.println();
        System.out.println("  Works on any Iterable too. Sum of " + scores + " = " + total);

        /* --------------------------------------------------------------------
         * WHAT YOU GIVE UP. Assigning to the loop variable changes only the
         * local copy - the source is untouched.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("LIMITATION 1 - assigning to the loop variable does nothing:");
        int[] numbers = {1, 2, 3};
        for (int n : numbers) {
            n = 99;                 // changes only this local copy
        }
        System.out.println("    after `for (int n : numbers) n = 99;`  ->  "
                + Arrays.toString(numbers) + "   <- unchanged");

        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = 99;        // writing through the INDEX does work
        }
        System.out.println("    after `numbers[i] = 99;` in an indexed for  ->  "
                + Arrays.toString(numbers));

        System.out.println();
        System.out.println("LIMITATION 2 - no index, no reverse, no two-at-a-time:");
        System.out.println("    use an indexed for when you need any of those.");

        /* --------------------------------------------------------------------
         * LIMITATION 3 - ConcurrentModificationException. The most important
         * one, because it is a runtime crash rather than a compile error.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("LIMITATION 3 - modifying the collection while iterating:");

        List<String> letters = new ArrayList<>(List.of("a", "b", "c", "d"));
        try {
            for (String letter : letters) {
                if (letter.equals("b")) {
                    letters.remove(letter);     // breaks the iterator
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("    removing inside a for-each threw ConcurrentModificationException");
            System.out.println("    (the iterator tracks a modification count and notices)");
        }

        System.out.println();
        System.out.println("  THE THREE FIXES:");

        List<String> fixOne = new ArrayList<>(List.of("a", "b", "c", "d"));
        fixOne.removeIf(letter -> letter.equals("b"));
        System.out.println("    1. removeIf (Java 8+, best)   -> " + fixOne);

        List<String> fixTwo = new ArrayList<>(List.of("a", "b", "c", "d"));
        Iterator<String> iterator = fixTwo.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals("b")) {
                iterator.remove();      // the ITERATOR's remove is safe
            }
        }
        System.out.println("    2. explicit Iterator.remove() -> " + fixTwo);

        List<String> fixThree = new ArrayList<>(List.of("a", "b", "c", "d"));
        for (String letter : new ArrayList<>(fixThree)) {    // iterate a COPY
            if (letter.equals("b")) {
                fixThree.remove(letter);
            }
        }
        System.out.println("    3. iterate a copy             -> " + fixThree);


        /* ====================================================================
         * SECTION 5 - NESTED LOOPS AND WHY THEY GET EXPENSIVE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - NESTED LOOPS");
        System.out.println("=".repeat(74));

        System.out.println("A multiplication table - the inner loop completes fully for");
        System.out.println("each SINGLE iteration of the outer loop:");
        System.out.println();
        System.out.print("      ");
        for (int col = 1; col <= 5; col++) {
            System.out.printf("%5d", col);
        }
        System.out.println();
        System.out.println("      " + "-".repeat(25));

        for (int row = 1; row <= 5; row++) {
            System.out.printf("  %2d |", row);
            for (int col = 1; col <= 5; col++) {
                System.out.printf("%5d", row * col);
            }
            System.out.println();
        }

        // Counting the actual iterations makes the cost concrete.
        int singleLoopCount = 0;
        for (int i = 0; i < 1000; i++) {
            singleLoopCount++;
        }

        int nestedLoopCount = 0;
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                nestedLoopCount++;
            }
        }

        System.out.println();
        System.out.println("Cost, measured by counting iterations at n = 1,000:");
        System.out.printf("    one loop      : %,15d%n", singleLoopCount);
        System.out.printf("    two nested    : %,15d%n", nestedLoopCount);
        System.out.printf("    three nested  : %,15d  (not run - it would take a while)%n",
                1_000_000_000L);
        System.out.println();
        System.out.println("  At n = 10,000 the nested version is 100,000,000 iterations.");
        System.out.println("  If you are nesting loops to SEARCH a collection, a HashMap");
        System.out.println("  usually turns O(n^2) into O(n). Lesson 47 covers that.");


        /* ====================================================================
         * SECTION 6 - LOOP PERFORMANCE THAT ACTUALLY MATTERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - THE ONE PERFORMANCE MISTAKE THAT REALLY COSTS");
        System.out.println("=".repeat(74));

        int iterations = 20_000;

        // Strings are immutable, so += builds a brand-new String every time,
        // copying everything accumulated so far. That is O(n^2) total work.
        long startConcat = System.nanoTime();
        String concatenated = "";
        for (int i = 0; i < iterations; i++) {
            concatenated += "x";
        }
        long concatMillis = (System.nanoTime() - startConcat) / 1_000_000;

        // StringBuilder mutates one growable buffer. O(n) total work.
        long startBuilder = System.nanoTime();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            builder.append("x");
        }
        String built = builder.toString();
        long builderMillis = (System.nanoTime() - startBuilder) / 1_000_000;

        System.out.printf("  Building a %,d character string:%n", iterations);
        System.out.println("    with  result += \"x\"     : " + concatMillis + " ms");
        System.out.println("    with  builder.append() : " + builderMillis + " ms");
        System.out.println("    same result? " + concatenated.equals(built));
        System.out.println();
        System.out.println("  += creates a NEW String each iteration and copies everything");
        System.out.println("  so far into it. StringBuilder mutates one growable buffer.");
        System.out.println("  Lesson 15 measures this properly.");

        System.out.println();
        System.out.println("Smaller point - hoist invariant work out of the condition:");
        System.out.println("    for (int i = 0; i < list.size(); i++)   // size() every iteration");
        System.out.println("    int size = list.size();");
        System.out.println("    for (int i = 0; i < size; i++)          // called once");
        System.out.println("  The JIT usually handles this, but the second states the intent.");

        System.out.println();
        System.out.println("And one that really matters - LinkedList with an indexed for:");
        System.out.println("    for (int i = 0; i < linkedList.size(); i++) linkedList.get(i);");
        System.out.println("  get(i) walks from the head each time, making this O(n^2).");
        System.out.println("  Always use the enhanced for or an iterator on a LinkedList.");
        System.out.println("  Lesson 45 covers the difference.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 10.");
        System.out.println("=".repeat(74));
    }

    /**
     * Prints each condition evaluation so the execution order of a for loop is
     * visible rather than merely described.
     *
     * @param label  what is being checked
     * @param result the condition's value
     * @return the result, unchanged, so it can be used as the loop condition
     */
    static boolean trace(String label, boolean result) {
        System.out.println("    check " + label + " -> " + result);
        return result;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Print the numbers 1 to 10 four ways: for, while, do-while, and an
 *    enhanced for over an array you build first. Then print them backwards -
 *    and note which loop cannot do it.
 *
 * 2. Find the off-by-one error in each, and say what it prints:
 *        for (int i = 0; i <= array.length; i++) { use(array[i]); }
 *        for (int i = 1; i < array.length; i++)  { use(array[i]); }
 *
 * 3. Print this triangle with nested loops:
 *        *
 *        **
 *        ***
 *        ****
 *    Then print it right-aligned. The second version needs two inner loops.
 *
 * 4. Write a loop that sums only the even numbers in an int[]. Do it once with
 *    an indexed for and once with an enhanced for. Which reads better?
 *
 * 5. Reproduce ConcurrentModificationException yourself, then fix it all three
 *    ways from Section 4. Time all three on a 100,000-element list.
 *
 * 6. Predict which is faster on a 100,000-element LinkedList, then measure:
 *        for (int i = 0; i < list.size(); i++) sum += list.get(i);
 *        for (int n : list) sum += n;
 *    The gap is larger than most people expect.
 * ============================================================================
 */
