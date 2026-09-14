/* ============================================================================
 * 50 - Iterators and the Collections utility class
 * ----------------------------------------------------------------------------
 * Companion lesson: 50-iterators-and-collections-utility.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/50-iterators-and-collections-utility.java
 *
 * The last lesson of Section 09. Section 1 reproduces the exception almost
 * everyone hits at least once: modifying a list while looping over it.
 * Section 4 catches a subtler one - "unmodifiable" does not mean "immutable".
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class IteratorsAndCollectionsUtility {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - ConcurrentModificationException, REPRODUCED ON PURPOSE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - MODIFYING A LIST WHILE LOOPING OVER IT");
        System.out.println("=".repeat(74));

        List<String> names = new ArrayList<>(List.of("Alice", "Bob", "Charlie", "Dave"));
        System.out.println("    list -> " + names);
        System.out.println();
        System.out.println("    THE MISTAKE - remove() through an enhanced for:");
        System.out.println("      for (String name : names) {");
        System.out.println("          if (name.equals(\"Bob\")) names.remove(name);");
        System.out.println("      }");
        try {
            for (String name : names) {
                if (name.equals("Bob")) {
                    names.remove(name);
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("      -> ConcurrentModificationException, thrown on the NEXT");
            System.out.println("         call to next() after the structural change - not even");
            System.out.println("         on the remove() call itself.");
        }

        System.out.println();
        System.out.println("    WHY: the enhanced for is sugar for an Iterator. Every list");
        System.out.println("    tracks a modCount, bumped by every structural change. Each");
        System.out.println("    Iterator remembers the modCount it started with; next() checks");
        System.out.println("    it still matches before doing anything, and throws if not. This");
        System.out.println("    is a FAIL-FAST, BEST-EFFORT check - the JDK's own Javadoc warns");
        System.out.println("    it is not guaranteed to catch every case, only meant to catch");
        System.out.println("    bugs, not to be relied on for correctness.");

        System.out.println();
        System.out.println("    THE FIX - Iterator.remove(), the ONLY safe way to remove");
        System.out.println("    DURING iteration:");
        List<String> viaIterator = new ArrayList<>(List.of("Alice", "Bob", "Charlie", "Dave"));
        Iterator<String> it = viaIterator.iterator();
        while (it.hasNext()) {
            if (it.next().equals("Bob")) {
                it.remove();   // tells the list's modCount AND the iterator's expected count together
            }
        }
        System.out.println("      " + viaIterator + "   <- worked, no exception");

        System.out.println();
        System.out.println("    THE OTHER FIX, when you are not tied to Iterator - removeIf():");
        List<String> viaRemoveIf = new ArrayList<>(List.of("Alice", "Bob", "Charlie", "Dave"));
        viaRemoveIf.removeIf(name -> name.equals("Bob"));
        System.out.println("      " + viaRemoveIf);


        /* ====================================================================
         * SECTION 2 - ListIterator: BIDIRECTIONAL, AND CAN INSERT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - ListIterator: EVERYTHING Iterator CAN'T DO");
        System.out.println("=".repeat(74));

        List<String> letters = new ArrayList<>(List.of("a", "b", "c"));
        ListIterator<String> listIt = letters.listIterator();
        System.out.println("    start -> " + letters);
        while (listIt.hasNext()) {
            String value = listIt.next();
            if (value.equals("b")) {
                listIt.set("B");           // REPLACE the just-returned element
                listIt.add("b2");          // INSERT right after it
            }
        }
        System.out.println("    after set(\"B\") and add(\"b2\") mid-walk -> " + letters);

        System.out.println();
        System.out.println("    walking BACKWARDS from the end:");
        ListIterator<String> backwards = letters.listIterator(letters.size());
        StringBuilder reversed = new StringBuilder();
        while (backwards.hasPrevious()) {
            reversed.append(backwards.previous()).append(" ");
        }
        System.out.println("      " + reversed.toString().strip());
        System.out.println();
        System.out.println("    Plain Iterator only goes forward and can only remove(). A");
        System.out.println("    ListIterator can also set(), add(), and walk either direction -");
        System.out.println("    only List offers one, because only List has POSITIONS.");


        /* ====================================================================
         * SECTION 3 - Collections: SORT, SEARCH, SHUFFLE, min/max, frequency
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE Collections UTILITY CLASS, THE PARTS WORTH KNOWING");
        System.out.println("=".repeat(74));

        List<Integer> numbers = new ArrayList<>(List.of(5, 2, 8, 2, 9, 1, 2));
        System.out.println("    numbers                    -> " + numbers);
        System.out.println("    Collections.max/min         -> "
                + Collections.max(numbers) + " / " + Collections.min(numbers));
        System.out.println("    Collections.frequency(_, 2) -> " + Collections.frequency(numbers, 2));

        Collections.sort(numbers);
        System.out.println("    Collections.sort(numbers)   -> " + numbers);

        Collections.reverse(numbers);
        System.out.println("    Collections.reverse(numbers)-> " + numbers);

        Collections.sort(numbers);   // binarySearch REQUIRES sorted input
        int found = Collections.binarySearch(numbers, 8);
        System.out.println("    (re-sorted) binarySearch(8) -> index " + found);

        System.out.println();
        System.out.println("    THE binarySearch TRAP - it REQUIRES sorted input and gives NO");
        System.out.println("    warning if you hand it something else:");
        List<Integer> unsorted = new ArrayList<>(List.of(50, 10, 40, 20, 30));
        int wrongIndex = Collections.binarySearch(unsorted, 20);
        System.out.println("      unsorted list -> " + unsorted);
        System.out.println("      binarySearch(20) on it -> " + wrongIndex
                + "   <- 20 IS in the list, at index 3, but the search says " + wrongIndex);
        System.out.println("      binarySearch's Javadoc says the result is UNDEFINED on");
        System.out.println("      unsorted input - not an exception, just a WRONG ANSWER, on");
        System.out.println("      purpose, because checking sortedness would cost O(n) and");
        System.out.println("      defeat the point of an O(log n) search.");

        System.out.println();
        List<String> shuffleMe = new ArrayList<>(List.of("a", "b", "c", "d", "e"));
        Collections.shuffle(shuffleMe, new java.util.Random(42));   // seeded, so REPRODUCIBLE
        System.out.println("    Collections.shuffle(_, new Random(42)) -> " + shuffleMe
                + "   <- a SEEDED Random makes this deterministic across runs, for tests");

        System.out.println();
        System.out.println("    Collections.emptyList()/singletonList(x)/nCopies(n, x) - handy");
        System.out.println("    constants and generators:");
        System.out.println("      nCopies(3, \"x\") -> " + Collections.nCopies(3, "x"));


        /* ====================================================================
         * SECTION 4 - "UNMODIFIABLE" IS NOT "IMMUTABLE": THE REAL TRAP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - Collections.unmodifiableList IS A VIEW, NOT A COPY");
        System.out.println("=".repeat(74));

        List<String> mutable = new ArrayList<>(List.of("x", "y", "z"));
        List<String> unmodifiableView = Collections.unmodifiableList(mutable);

        System.out.println("    mutable            -> " + mutable);
        System.out.println("    unmodifiableView    -> " + unmodifiableView);

        try {
            unmodifiableView.add("blocked");
        } catch (UnsupportedOperationException e) {
            System.out.println("    unmodifiableView.add(\"blocked\") -> UnsupportedOperationException");
            System.out.println("      (good - the VIEW itself cannot be written to directly)");
        }

        mutable.add("added via the ORIGINAL list");
        System.out.println();
        System.out.println("    mutable.add(\"added via the ORIGINAL list\")");
        System.out.println("    unmodifiableView is now -> " + unmodifiableView);
        System.out.println();
        System.out.println("    THE VIEW CHANGED. \"Unmodifiable\" only means YOU cannot call");
        System.out.println("    mutating methods ON THAT REFERENCE - it is not a copy, and");
        System.out.println("    whoever still holds the ORIGINAL, mutable list can change what");
        System.out.println("    the \"unmodifiable\" view shows, at any time, with no warning.");

        List<String> genuinelyImmutable = List.copyOf(mutable);
        mutable.add("this one cannot reach List.copyOf's result");
        System.out.println();
        System.out.println("    List.copyOf(mutable) makes a REAL, independent COPY (Java 10+):");
        System.out.println("      genuinelyImmutable -> " + genuinelyImmutable);
        System.out.println("      mutable is now      -> " + mutable);
        System.out.println("      genuinelyImmutable is UNCHANGED - because it does not share");
        System.out.println("      backing storage with mutable at all. THIS is what you want");
        System.out.println("      when you mean 'this list must never change, from anywhere.'");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 50, and of Section 09.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Reproduce Section 1's ConcurrentModificationException using a HashMap's
 *    keySet() instead of a List - confirm the same modCount mechanism
 *    applies to Map iteration too.
 *
 * 2. Use a ListIterator to insert "separator" between every pair of
 *    elements in a List<String> of size 5, ending with 9 elements.
 *
 * 3. Write your own binarySearch(List<Integer> sorted, int target) that
 *    returns the index or -1, without using Collections.binarySearch -
 *    then compare behavior on unsorted input against the real one.
 *
 * 4. Section 4 shows unmodifiableList exposing a live mutation. Now do the
 *    OPPOSITE experiment: modify the unmodifiableView's underlying list
 *    STRUCTURALLY (add/remove) WHILE iterating the view with an enhanced
 *    for - does it throw ConcurrentModificationException too? Why would
 *    it, given the view shares the same backing list?
 *
 * 5. Collections.synchronizedList(list) makes individual method calls
 *    thread-safe, but NOT iteration. Look up why, and write the correct
 *    pattern (synchronizing manually on the returned list) for iterating
 *    a synchronizedList safely from multiple threads.
 * ============================================================================
 */
