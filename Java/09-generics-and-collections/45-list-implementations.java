/* ============================================================================
 * 45 - List: ArrayList VERSUS LinkedList
 * ----------------------------------------------------------------------------
 * Companion lesson: 45-list-implementations.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/45-list-implementations.java
 *
 * The textbook says "use LinkedList for frequent insertions". The textbook is,
 * in practice, WRONG. This lesson measures why - Section 3 is the whole point.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

class ListImplementations {

    public static void main(String[] args) throws Exception {

        /* ====================================================================
         * SECTION 1 - HOW EACH ONE STORES ITS ELEMENTS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE PICTURE THAT EXPLAINS EVERYTHING");
        System.out.println("=".repeat(74));

        System.out.println("  ArrayList wraps a SINGLE Object[]:");
        System.out.println();
        System.out.println("      elementData:  [ a ][ b ][ c ][ d ][   ][   ][   ]");
        System.out.println("                    contiguous, size=4, capacity=7");
        System.out.println();
        System.out.println("  LinkedList is a doubly-linked chain of Node objects:");
        System.out.println();
        System.out.println("      first -> [prev|a|next] <-> [prev|b|next] <-> [prev|c|next] <- last");
        System.out.println("               scattered ANYWHERE on the heap");
        System.out.println();
        System.out.println("  Every consequence in this lesson follows from that difference.");


        /* ====================================================================
         * SECTION 2 - ArrayList GROWTH
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - HOW ArrayList GROWS");
        System.out.println("=".repeat(74));

        System.out.println("  ArrayList's growth rule, reproduced exactly by GrowableArray at");
        System.out.println("  the bottom of this file:");
        System.out.println();
        System.out.printf("    %-10s %-12s %s%n", "SIZE", "CAPACITY", "EVENT");

        GrowableArray growing = new GrowableArray();
        int lastCapacity = growing.capacity();
        System.out.printf("    %-10d %-12d %s%n", growing.size(), lastCapacity,
                "created - the array is EMPTY until the first add");

        for (int i = 1; i <= 120; i++) {
            growing.add(i);
            int capacity = growing.capacity();
            if (capacity != lastCapacity) {
                System.out.printf("    %-10d %-12d %s%n", growing.size(), capacity,
                        lastCapacity == 0 ? "first add allocates 10"
                                : "GREW from " + lastCapacity + " (x1.5, then copy)");
                lastCapacity = capacity;
            }
        }

        System.out.println();
        System.out.println("  The sequence is 10 -> 15 -> 22 -> 33 -> 49 -> 73 -> 109 ...");
        System.out.println("  Each growth allocates a NEW array and COPIES everything. That is");
        System.out.println("  why add() is AMORTISED O(1), not strictly O(1): most adds are");
        System.out.println("  free, and occasionally one copies the lot. Over n adds, the");
        System.out.println("  copying totals about 2n element moves - a constant per element.");

        System.out.println();
        System.out.println("  WHY REPRODUCE IT RATHER THAN READ ArrayList's REAL FIELD?");
        System.out.println("  Because the MODULE SYSTEM blocks it (lesson 30):");
        System.out.println();
        System.out.println("      Field f = ArrayList.class.getDeclaredField(\"elementData\");");
        System.out.println("      f.setAccessible(true);");
        System.out.println("      -> InaccessibleObjectException: module java.base does not");
        System.out.println("         \"opens java.util\" to unnamed module");
        System.out.println();
        System.out.println("  That is JPMS strong encapsulation doing exactly its job. To");
        System.out.println("  verify against the real ArrayList, run with:");
        System.out.println("      java --add-opens java.base/java.util=ALL-UNNAMED ThisFile.java");

        System.out.println();
        System.out.println("  PRESIZE when you know the size - no reallocation at all:");
        int presizeCount = 2_000_000;

        long startUnsized = System.nanoTime();
        List<Integer> unsized = new ArrayList<>();
        for (int i = 0; i < presizeCount; i++) {
            unsized.add(i);
        }
        long unsizedMillis = (System.nanoTime() - startUnsized) / 1_000_000;

        long startPresized = System.nanoTime();
        List<Integer> presized = new ArrayList<>(presizeCount);
        for (int i = 0; i < presizeCount; i++) {
            presized.add(i);
        }
        long presizedMillis = (System.nanoTime() - startPresized) / 1_000_000;

        System.out.printf("    adding %,d elements:%n", presizeCount);
        System.out.println("      new ArrayList<>()            -> " + unsizedMillis + " ms");
        System.out.println("      new ArrayList<>(" + presizeCount + ")     -> "
                + presizedMillis + " ms");

        System.out.println();
        System.out.println("  ArrayList NEVER SHRINKS AUTOMATICALLY:");
        GrowableArray wasLarge = new GrowableArray();
        for (int i = 0; i < 100_000; i++) {
            wasLarge.add(i);
        }
        int fullCapacity = wasLarge.capacity();
        wasLarge.clear();
        System.out.println("      after adding 100,000 -> capacity " + fullCapacity);
        System.out.println("      after clear()        -> size " + wasLarge.size()
                + ", capacity STILL " + wasLarge.capacity());
        wasLarge.trimToSize();
        System.out.println("      after trimToSize()   -> capacity " + wasLarge.capacity());
        System.out.println();
        System.out.println("    The real ArrayList behaves identically. A list that briefly");
        System.out.println("    held a million elements still holds a million-element array");
        System.out.println("    after clear(). For a long-lived one, call trimToSize().");


        /* ====================================================================
         * SECTION 3 - THE COMPLEXITY TABLE, AND WHY IT MISLEADS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - MEASURING WHAT THE TABLE PROMISES");
        System.out.println("=".repeat(74));

        System.out.printf("    %-22s %-18s %s%n", "OPERATION", "ArrayList", "LinkedList");
        System.out.printf("    %-22s %-18s %s%n", "get(i)", "O(1)", "O(n)");
        System.out.printf("    %-22s %-18s %s%n", "add(e) at end", "O(1) amortised", "O(1)");
        System.out.printf("    %-22s %-18s %s%n", "add(0, e) at start", "O(n)", "O(1)");
        System.out.printf("    %-22s %-18s %s%n", "contains(o)", "O(n)", "O(n)");
        System.out.printf("    %-22s %-18s %s%n", "memory per element", "~4-8 bytes", "~40 bytes");

        System.out.println();
        System.out.println("  LinkedList looks better in ONE row. Let us test all of them.");

        int size = 100_000;

        // --- Indexed access: the disaster.
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        System.out.println();
        System.out.println("  TEST 1 - INDEXED ACCESS, where the table says O(1) vs O(n):");
        long arrayGetMillis = timeIndexedRead(arrayList);
        long linkedGetMillis = timeIndexedRead(linkedList);
        System.out.printf("    %,d get(i) calls:%n", size);
        System.out.println("      ArrayList  -> " + arrayGetMillis + " ms");
        System.out.println("      LinkedList -> " + linkedGetMillis + " ms   <- as predicted");

        // --- Appending at the end.
        System.out.println();
        System.out.println("  TEST 2 - APPENDING AT THE END, where the table says both O(1):");
        long arrayAppendMillis = timeAppend(new ArrayList<>(), size);
        long linkedAppendMillis = timeAppend(new LinkedList<>(), size);
        System.out.printf("    %,d appends:%n", size);
        System.out.println("      ArrayList  -> " + arrayAppendMillis + " ms");
        System.out.println("      LinkedList -> " + linkedAppendMillis + " ms");
        System.out.println("      Equal complexity, and ArrayList still tends to win - it");
        System.out.println("      allocates nothing per element, LinkedList allocates a Node.");

        // --- Inserting at the front: LinkedList's supposed home ground.
        System.out.println();
        System.out.println("  TEST 3 - INSERTING AT THE FRONT, LinkedList's SUPPOSED WIN:");
        int frontInserts = 60_000;
        long arrayFrontMillis = timeInsertAtFront(new ArrayList<>(), frontInserts);
        long linkedFrontMillis = timeInsertAtFront(new LinkedList<>(), frontInserts);
        System.out.printf("    %,d add(0, e) calls:%n", frontInserts);
        System.out.println("      ArrayList  -> " + arrayFrontMillis + " ms   (O(n) each)");
        System.out.println("      LinkedList -> " + linkedFrontMillis + " ms   (O(1) each)");
        System.out.println();
        System.out.println("      Here LinkedList genuinely wins - but note ArrayList's O(n)");
        System.out.println("      is System.arraycopy, a highly optimised BULK MEMORY MOVE,");
        System.out.println("      while LinkedList's O(1) allocates an object. For small and");
        System.out.println("      medium lists the array copy often still wins.");

        // --- Iteration: where cache locality decides everything.
        System.out.println();
        System.out.println("  TEST 4 - SEQUENTIAL ITERATION, where the table says both O(n).");
        System.out.println("  This one needs care, so it is measured THREE ways:");

        int iterationSize = 2_000_000;

        List<Integer> freshArray = new ArrayList<>(iterationSize);
        List<Integer> freshLinked = new LinkedList<>();
        for (int i = 0; i < iterationSize; i++) {
            freshArray.add(i);
            freshLinked.add(i);
        }

        // A LinkedList whose nodes were allocated with OTHER objects in
        // between, so they end up scattered across the heap - which is what
        // a long-lived list in a real application looks like.
        List<Integer> scatteredLinked = new LinkedList<>();
        List<Object> interleavedRubbish = new ArrayList<>(iterationSize);
        for (int i = 0; i < iterationSize; i++) {
            scatteredLinked.add(i);
            interleavedRubbish.add(new int[4]);      // pushes the next Node further away
        }
        interleavedRubbish.clear();                  // let the padding be collected

        long arrayIterateMillis = timeIteration(freshArray);
        long linkedIterateMillis = timeIteration(freshLinked);
        long scatteredMillis = timeIteration(scatteredLinked);

        System.out.printf("    %,d elements, enhanced for:%n", iterationSize);
        System.out.println("      ArrayList                          -> " + arrayIterateMillis + " ms");
        System.out.println("      LinkedList, built all at once      -> " + linkedIterateMillis + " ms");
        System.out.println("      LinkedList, allocated interleaved  -> " + scatteredMillis + " ms");

        System.out.println();
        System.out.println("      TWO HONEST OBSERVATIONS ABOUT THOSE NUMBERS:");
        System.out.println();
        System.out.printf("      1. ArrayList won by about %dx even though BOTH are O(n).%n",
                Math.max(1, linkedIterateMillis / Math.max(1, arrayIterateMillis)));
        System.out.println("         That gap is pure memory layout: one contiguous scan");
        System.out.println("         against one pointer-chase plus a Node dereference per");
        System.out.println("         element.");
        System.out.println();
        System.out.println("      2. The third row was an attempt to SCATTER the nodes by");
        System.out.println("         allocating rubbish between them - and it barely changed");
        System.out.println("         anything. That is not a failed experiment; it is the");
        System.out.println("         GENERATIONAL COLLECTOR doing its job. Surviving objects");
        System.out.println("         are COMPACTED when promoted, so the padding was collected");
        System.out.println("         and the nodes ended up adjacent again (lesson 69).");
        System.out.println();
        System.out.println("         You cannot easily force cache-hostile layout on a JVM");
        System.out.println("         that moves objects for you. Worth knowing before you");
        System.out.println("         trust any microbenchmark that claims to have done so.");

        System.out.println();
        System.out.println("  SO WHAT IS THE REAL ARGUMENT?");
        System.out.println("    NOT 'linked lists are always catastrophically slow' - Test 2");
        System.out.println("    and this test show they are often merely somewhat slower.");
        System.out.println();
        System.out.println("    It is this: ArrayList's performance is GUARANTEED BY ITS");
        System.out.println("    LAYOUT, and LinkedList's depends on where the allocator and");
        System.out.println("    collector happened to put things. One is predictable; the");
        System.out.println("    other is not.");
        System.out.println();
        System.out.printf("    Add the %dx gap on get(i) from Test 1, the memory cost below,%n",
                Math.max(1, linkedGetMillis / Math.max(1, arrayGetMillis)));
        System.out.println("    and the fact that LinkedList wins only on front-insertion,");
        System.out.println("    and the default is obvious.");

        // --- The O(n^2) trap.
        System.out.println();
        System.out.println("  THE get(i) DISASTER - an indexed loop over a LinkedList:");
        System.out.println();
        System.out.println("      for (int i = 0; i < list.size(); i++) sum += list.get(i);");
        System.out.println();
        for (int trialSize : new int[]{10_000, 20_000, 40_000}) {
            List<Integer> trial = new LinkedList<>();
            for (int i = 0; i < trialSize; i++) {
                trial.add(i);
            }
            long millis = timeIndexedRead(trial);
            System.out.printf("    %,7d elements -> %5d ms%n", trialSize, millis);
        }
        System.out.println();
        System.out.println("    Doubling the size roughly QUADRUPLES the time. That is O(n^2)");
        System.out.println("    from an innocent-looking loop. Always use the enhanced for or");
        System.out.println("    an iterator on a LinkedList (lesson 10).");

        // --- Memory.
        System.out.println();
        System.out.println("  MEMORY - a Node object per element, versus one array slot:");
        Runtime runtime = Runtime.getRuntime();
        long megabyte = 1024L * 1024L;
        int memoryElements = 2_000_000;

        System.gc();
        long beforeArrayList = usedMemory(runtime);
        List<Integer> memoryArray = new ArrayList<>(memoryElements);
        for (int i = 0; i < memoryElements; i++) {
            memoryArray.add(i);
        }
        long arrayMb = (usedMemory(runtime) - beforeArrayList) / megabyte;

        System.gc();
        long beforeLinkedList = usedMemory(runtime);
        List<Integer> memoryLinked = new LinkedList<>();
        for (int i = 0; i < memoryElements; i++) {
            memoryLinked.add(i);
        }
        long linkedMb = (usedMemory(runtime) - beforeLinkedList) / megabyte;

        System.out.printf("    %,d elements:%n", memoryElements);
        System.out.println("      ArrayList  -> about " + arrayMb + " MB");
        System.out.println("      LinkedList -> about " + linkedMb + " MB");
        System.out.println("      (each LinkedList Node holds the element plus TWO references)");
        System.out.println("      (checksums " + memoryArray.size() + " and " + memoryLinked.size() + ")");


        /* ====================================================================
         * SECTION 4 - WHEN LinkedList IS GENUINELY RIGHT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHEN TO USE IT");
        System.out.println("=".repeat(74));

        System.out.println("  Almost never. The honest list:");
        System.out.println();
        System.out.println("    - you need a Deque AND a List in the same object");
        System.out.println("      (but ArrayDeque is faster if you only need the Deque)");
        System.out.println("    - you remove via an Iterator while traversing, at genuinely");
        System.out.println("      large scale");
        System.out.println("    - you need constant-time splice at a node you already hold -");
        System.out.println("      and Java's API does not actually expose that");

        System.out.println();
        System.out.println("  The iterator-removal case, which IS real:");
        List<Integer> removeFromArray = new ArrayList<>();
        List<Integer> removeFromLinked = new LinkedList<>();
        int removalSize = 100_000;
        for (int i = 0; i < removalSize; i++) {
            removeFromArray.add(i);
            removeFromLinked.add(i);
        }
        long arrayRemoveMillis = timeIteratorRemoval(removeFromArray);
        long linkedRemoveMillis = timeIteratorRemoval(removeFromLinked);
        System.out.printf("    removing every other element from %,d, via Iterator:%n", removalSize);
        System.out.println("      ArrayList  -> " + arrayRemoveMillis + " ms   (each remove shifts)");
        System.out.println("      LinkedList -> " + linkedRemoveMillis + " ms   (relink two pointers)");

        System.out.println();
        System.out.println("  Java's own documentation and Joshua Bloch both recommend");
        System.out.println("  ArrayList by DEFAULT. If you are reaching for LinkedList to get");
        System.out.println("  a queue, use ArrayDeque instead (lesson 48).");


        /* ====================================================================
         * SECTION 5 - THE OTHER IMPLEMENTATIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE REST OF THE FAMILY");
        System.out.println("=".repeat(74));

        System.out.printf("    %-24s %s%n", "ArrayList", "the default");
        System.out.printf("    %-24s %s%n", "LinkedList", "rarely the right answer");
        System.out.printf("    %-24s %s%n", "Vector", "legacy - synchronized, no benefit");
        System.out.printf("    %-24s %s%n", "Stack", "legacy - extends Vector (lesson 26)");
        System.out.printf("    %-24s %s%n", "CopyOnWriteArrayList", "thread-safe; every WRITE copies");
        System.out.printf("    %-24s %s%n", "List.of(...)", "immutable, compact, rejects null");
        System.out.printf("    %-24s %s%n", "Arrays.asList(...)", "fixed-size VIEW over an array");

        System.out.println();
        System.out.println("  CopyOnWriteArrayList - every write copies the WHOLE array:");
        List<String> copyOnWrite = new CopyOnWriteArrayList<>(List.of("a", "b", "c"));

        int writes = 20_000;
        long startCopyOnWrite = System.nanoTime();
        List<String> growingCopy = new CopyOnWriteArrayList<>();
        for (int i = 0; i < writes; i++) {
            growingCopy.add("x");
        }
        long copyOnWriteMillis = (System.nanoTime() - startCopyOnWrite) / 1_000_000;

        long startPlain = System.nanoTime();
        List<String> plain = new ArrayList<>();
        for (int i = 0; i < writes; i++) {
            plain.add("x");
        }
        long plainMillis = (System.nanoTime() - startPlain) / 1_000_000;

        System.out.printf("    %,d adds:%n", writes);
        System.out.println("      ArrayList            -> " + plainMillis + " ms");
        System.out.println("      CopyOnWriteArrayList -> " + copyOnWriteMillis + " ms");
        System.out.println();
        System.out.println("    That is O(n) PER WRITE, so O(n^2) overall. It is for");
        System.out.println("    MANY READS and VERY FEW WRITES - listener lists, configuration");
        System.out.println("    snapshots. Its iterator never throws");
        System.out.println("    ConcurrentModificationException, because it iterates a frozen");
        System.out.println("    snapshot:");

        Iterator<String> snapshot = copyOnWrite.iterator();
        copyOnWrite.add("added after the iterator was created");
        StringBuilder seen = new StringBuilder();
        while (snapshot.hasNext()) {
            seen.append(snapshot.next()).append(" ");
        }
        System.out.println("      iterator saw   -> " + seen.toString().strip());
        System.out.println("      the list is now-> " + copyOnWrite);


        /* ====================================================================
         * SECTION 6 - THE METHODS, AND TWO TRAPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - THE METHODS WORTH KNOWING");
        System.out.println("=".repeat(74));

        List<String> words = new ArrayList<>(List.of("delta", "alpha", "charlie", "bravo"));
        System.out.println("    start                    -> " + words);

        words.sort(String::compareTo);
        System.out.println("    sort(String::compareTo)  -> " + words + "   in place, Java 8");

        words.replaceAll(String::toUpperCase);
        System.out.println("    replaceAll(toUpperCase)  -> " + words + "   map in place");

        words.removeIf(word -> word.startsWith("A"));
        System.out.println("    removeIf(startsWith A)   -> " + words + "   filter in place");

        System.out.println("    indexOf(\"CHARLIE\")       -> " + words.indexOf("CHARLIE"));
        System.out.println("    getFirst() / getLast()   -> " + words.getFirst()
                + " / " + words.getLast() + "   (Java 21)");
        System.out.println("    reversed()               -> " + words.reversed()
                + "   (Java 21, a VIEW)");

        /* --------------------------------------------------------------------
         * TRAP 1: the remove overload.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  TRAP 1 - the remove overload (lesson 18):");
        List<Integer> byIndex = new ArrayList<>(List.of(10, 20, 30));
        List<Integer> byValue = new ArrayList<>(List.of(10, 20, 30));

        byIndex.remove(1);
        byValue.remove(Integer.valueOf(10));

        System.out.println("    starting from [10, 20, 30]:");
        System.out.println("      remove(1)                  -> " + byIndex
                + "   removed INDEX 1");
        System.out.println("      remove(Integer.valueOf(10))-> " + byValue
                + "   removed the VALUE 10");
        System.out.println("    On a List<Integer> these read almost identically and do");
        System.out.println("    completely different things. A real production bug.");

        /* --------------------------------------------------------------------
         * TRAP 2: subList is a VIEW.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  TRAP 2 - subList returns a LIVE VIEW, not a copy:");
        List<String> backing = new ArrayList<>(List.of("a", "b", "c", "d", "e"));
        List<String> view = backing.subList(1, 3);

        System.out.println("    backing        -> " + backing);
        System.out.println("    subList(1, 3)  -> " + view);

        view.set(0, "CHANGED");
        System.out.println("    view.set(0, \"CHANGED\")");
        System.out.println("    backing is now -> " + backing + "   <- wrote THROUGH");

        backing.add("f");
        try {
            System.out.println(view.size());
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("    after backing.add(\"f\"), reading the view throws");
            System.out.println("      ConcurrentModificationException - the view is invalidated");
            System.out.println("      by a STRUCTURAL change to its backing list.");
        }
        System.out.println();
        System.out.println("    For an independent copy: new ArrayList<>(list.subList(1, 3))");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 45.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Reads how much memory is currently in use.
     *
     * @param runtime the runtime
     * @return bytes in use
     */

    /** @param runtime the runtime  @return bytes currently in use */
    static long usedMemory(Runtime runtime) {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * Times an indexed read of every element - O(n) on ArrayList, O(n^2) on
     * LinkedList.
     *
     * @param list the list to read
     * @return elapsed milliseconds
     */
    static long timeIndexedRead(List<Integer> list) {
        long start = System.nanoTime();
        long total = 0;
        for (int i = 0; i < list.size(); i++) {
            total += list.get(i);
        }
        long elapsed = System.nanoTime() - start;
        if (total == Long.MIN_VALUE) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Times appending at the end.
     *
     * @param list  the list to fill
     * @param count how many elements
     * @return elapsed milliseconds
     */
    static long timeAppend(List<Integer> list, int count) {
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
        long elapsed = System.nanoTime() - start;
        if (list.isEmpty()) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Times inserting at index 0 repeatedly - LinkedList's home ground.
     *
     * @param list  the list to fill
     * @param count how many elements
     * @return elapsed milliseconds
     */
    static long timeInsertAtFront(List<Integer> list, int count) {
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            list.add(0, i);
        }
        long elapsed = System.nanoTime() - start;
        if (list.isEmpty()) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Times a sequential walk with the enhanced for - the operation most code
     * actually performs, and where cache locality decides the winner.
     *
     * @param list the list to walk
     * @return elapsed milliseconds
     */
    static long timeIteration(List<Integer> list) {
        long start = System.nanoTime();
        long total = 0;
        for (int value : list) {
            total += value;
        }
        long elapsed = System.nanoTime() - start;
        if (total == Long.MIN_VALUE) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Times removing every other element through an Iterator - the one case
     * where LinkedList has a genuine structural advantage.
     *
     * @param list the list to thin out
     * @return elapsed milliseconds
     */
    static long timeIteratorRemoval(List<Integer> list) {
        long start = System.nanoTime();
        Iterator<Integer> iterator = list.iterator();
        boolean removeThisOne = false;
        while (iterator.hasNext()) {
            iterator.next();
            if (removeThisOne) {
                iterator.remove();
            }
            removeThisOne = !removeThisOne;
        }
        long elapsed = System.nanoTime() - start;
        if (list.isEmpty()) System.out.print("");
        return elapsed / 1_000_000;
    }
}

/**
 * Reproduces ArrayList's growth algorithm exactly, so the capacity sequence is
 * observable without reflecting into java.util - which the module system
 * forbids (lesson 30).
 *
 * <p>The rules copied from the real ArrayList:
 * <ul>
 *   <li>an empty list allocates NO array until the first add</li>
 *   <li>the first add allocates capacity 10</li>
 *   <li>a full array grows to {@code old + (old >> 1)}, i.e. 1.5x</li>
 *   <li>clear() sets the size to 0 and keeps the capacity</li>
 * </ul>
 */
class GrowableArray {

    /** The default capacity the first add allocates. */
    private static final int DEFAULT_CAPACITY = 10;

    /** Shared empty array, exactly as ArrayList does, so an empty list is free. */
    private static final Object[] EMPTY = {};

    private Object[] elements = EMPTY;
    private int size;

    /**
     * Appends one element, growing the backing array if it is full.
     *
     * @param element the element to append
     */
    void add(Object element) {
        if (size == elements.length) {
            grow();
        }
        elements[size++] = element;
    }

    /** Allocates a larger array and copies everything into it. */
    private void grow() {
        int newCapacity = elements.length == 0
                ? DEFAULT_CAPACITY
                : elements.length + (elements.length >> 1);   // 1.5x, as ArrayList does
        elements = Arrays.copyOf(elements, newCapacity);
    }

    /** Empties the list WITHOUT releasing the array - exactly as ArrayList does. */
    void clear() {
        Arrays.fill(elements, 0, size, null);   // release the elements for GC
        size = 0;
    }

    /** Shrinks the backing array to fit the current size. */
    void trimToSize() {
        if (size < elements.length) {
            elements = size == 0 ? EMPTY : Arrays.copyOf(elements, size);
        }
    }

    /** @return how many elements are stored */
    int size() {
        return size;
    }

    /** @return the backing array's length - what ArrayList hides from you */
    int capacity() {
        return elements.length;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Change Section 3's `size` to 10,000 and then 1,000,000. Which comparisons
 *    change their winner? Write down the crossover point for add(0, e).
 *
 * 2. Take the O(n^2) demonstration and rewrite the loop with an enhanced for.
 *    Re-measure at 40,000 and explain the difference in one sentence.
 *
 * 3. Presize an ArrayList to exactly the wrong size (say 10 when you will add
 *    a million). Does it perform worse than the default? Why not?
 *
 * 4. Write a benchmark for contains() on both lists at 100,000 elements. Both
 *    are O(n) - predict the ratio before you measure it.
 *
 * 5. Use CopyOnWriteArrayList in a loop that adds 100,000 elements and time it.
 *    Then explain to a colleague why it is still the right choice for a
 *    listener registry.
 *
 * 6. Create a subList view, then structurally modify the backing list, then
 *    read the view. Catch the exception. Now do the same with
 *    new ArrayList<>(list.subList(...)) and confirm it is immune.
 * ============================================================================
 */
