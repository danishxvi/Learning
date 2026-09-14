/* ============================================================================
 * 46 - Set: HashSet, LinkedHashSet, TreeSet
 * ----------------------------------------------------------------------------
 * Companion lesson: 46-set-implementations.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/46-set-implementations.java
 *
 * A Set is "no duplicates" - but the three implementations disagree on what
 * "duplicate" even means, and on what order (if any) you get back. Section 4
 * is the one that produces a real, silent data-loss bug.
 * ============================================================================
 */

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

class SetImplementations {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE THREE, SIDE BY SIDE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - SAME ELEMENTS, THREE DIFFERENT ITERATION ORDERS");
        System.out.println("=".repeat(74));

        List<String> insertOrder = List.of("delta", "alpha", "charlie", "bravo", "echo");

        Set<String> hash = new HashSet<>(insertOrder);
        Set<String> linkedHash = new LinkedHashSet<>(insertOrder);
        Set<String> tree = new TreeSet<>(insertOrder);

        System.out.println("    inserted in this order  -> " + insertOrder);
        System.out.println("    HashSet iterates as     -> " + hash);
        System.out.println("    LinkedHashSet iterates   -> " + linkedHash);
        System.out.println("    TreeSet iterates as      -> " + tree);
        System.out.println();
        System.out.println("    HashSet's order is whatever the HASH BUCKETS happen to land");
        System.out.println("    in - an implementation detail, not a contract. It can change");
        System.out.println("    between JVM versions. Never rely on it.");
        System.out.println();
        System.out.println("    LinkedHashSet keeps a doubly-linked list threaded through the");
        System.out.println("    same hash table, so iteration is INSERTION order, at the cost");
        System.out.println("    of two extra pointers per element.");
        System.out.println();
        System.out.println("    TreeSet keeps no insertion information at all - it iterates in");
        System.out.println("    SORTED order because that is literally how the tree is built.");


        /* ====================================================================
         * SECTION 2 - HOW EACH ONE ACTUALLY WORKS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - WHAT'S UNDER EACH ONE");
        System.out.println("=".repeat(74));

        System.out.println("  HashSet<E> is, literally, a HashMap<E, Object> in disguise:");
        System.out.println();
        System.out.println("      private transient HashMap<E, Object> map;");
        System.out.println("      private static final Object PRESENT = new Object();");
        System.out.println("      add(e) { return map.put(e, PRESENT) == null; }");
        System.out.println();
        System.out.println("    Every HashSet method is one line delegating to the map. Its");
        System.out.println("    performance and its bugs are HashMap's performance and bugs");
        System.out.println("    (lesson 47 goes inside the map itself: buckets, treeification,");
        System.out.println("    load factor, resizing).");
        System.out.println();
        System.out.println("  LinkedHashSet extends HashSet - same map underneath, just");
        System.out.println("  constructed with the map's linked-order constructor.");
        System.out.println();
        System.out.println("  TreeSet<E> wraps a TreeMap<E, Object> - a RED-BLACK TREE, a");
        System.out.println("  self-balancing binary search tree that guarantees O(log n) for");
        System.out.println("  add/remove/contains, in exchange for giving up O(1).");


        /* ====================================================================
         * SECTION 3 - MEASURING contains(), WHERE THE POINT OF Set LIVES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - contains(): THE WHOLE REASON Set EXISTS");
        System.out.println("=".repeat(74));

        int size = 200_000;
        List<Integer> arrayBacked = new java.util.ArrayList<>();
        Set<Integer> hashBacked = new HashSet<>();
        Set<Integer> treeBacked = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            arrayBacked.add(i);
            hashBacked.add(i);
            treeBacked.add(i);
        }

        int lookups = 200_000;
        long arrayMillis = timeContains(arrayBacked, lookups, size);
        long hashMillis = timeContains(hashBacked, lookups, size);
        long treeMillis = timeContains(treeBacked, lookups, size);

        System.out.printf("    %,d elements, %,d contains() calls:%n", size, lookups);
        System.out.println("      List.contains(o)     -> " + arrayMillis + " ms   O(n) per call");
        System.out.println("      HashSet.contains(o)  -> " + hashMillis + " ms   O(1) average");
        System.out.println("      TreeSet.contains(o)  -> " + treeMillis + " ms   O(log n)");
        System.out.println();
        System.out.printf("    HashSet beat the List by roughly %dx.%n",
                Math.max(1, arrayMillis / Math.max(1, hashMillis)));
        System.out.println("    That gap is the entire reason Set is a separate abstraction");
        System.out.println("    from List - checking membership is what it is FOR.");


        /* ====================================================================
         * SECTION 4 - THE hashCode() CLIFF: A REAL, SILENT BUG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHAT A BROKEN hashCode() ACTUALLY COSTS");
        System.out.println("=".repeat(74));

        System.out.println("  HashSet's O(1) promise is a promise about a GOOD hash function -");
        System.out.println("  one that spreads elements across buckets. BadHashKey below");
        System.out.println("  (defined at the bottom of this file) violates that on purpose:");
        System.out.println("  its hashCode() ALWAYS returns 42, while equals() still compares");
        System.out.println("  the real id field correctly. It is not broken per the CONTRACT");
        System.out.println("  (lesson 33) - equal objects still hash equal - it is just");
        System.out.println("  terrible, and terrible is legal.");

        int keyCount = 20_000;
        Set<GoodHashKey> goodKeys = new HashSet<>();
        Set<BadHashKey> badKeys = new HashSet<>();
        for (int i = 0; i < keyCount; i++) {
            goodKeys.add(new GoodHashKey(i));
            badKeys.add(new BadHashKey(i));
        }

        long goodLookupMillis = timeContainsKey(goodKeys, keyCount);
        long badLookupMillis = timeContainsKey(badKeys, keyCount);

        System.out.printf("    %,d keys, %,d contains() calls:%n", keyCount, keyCount);
        System.out.println("      good hashCode() (spread across buckets) -> " + goodLookupMillis + " ms");
        System.out.println("      hashCode() always 42 (one giant bucket) -> " + badLookupMillis + " ms");
        System.out.println();
        System.out.printf("    The bad key was roughly %dx slower - and it gets WORSE as the%n",
                Math.max(1, badLookupMillis / Math.max(1, goodLookupMillis)));
        System.out.println("    set grows, because every key funnels into ONE bucket, which");
        System.out.println("    becomes a plain linked list (or a red-black tree since Java 8 -");
        System.out.println("    lesson 47 - which caps the damage at O(log n) instead of O(n),");
        System.out.println("    but only once a bucket gets large AND the keys are Comparable).");
        System.out.println();
        System.out.println("    THE LESSON: hashCode() is not optional boilerplate. A bad one");
        System.out.println("    silently turns every HashSet/HashMap using that key into a");
        System.out.println("    near-linear-scan structure, with no exception, no warning -");
        System.out.println("    just a service that mysteriously gets slower as it grows.");


        /* ====================================================================
         * SECTION 5 - THE TreeSet TRAP: compareTo DECIDES DUPLICATES,
         *             NOT equals()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - TreeSet USES compareTo() FOR EQUALITY, NOT equals()");
        System.out.println("=".repeat(74));

        System.out.println("  Person records two different people, deliberately with the SAME");
        System.out.println("  age but different names - equals() (records get it for free,");
        System.out.println("  lesson 36) says they are NOT equal.");

        record Person(String name, int age) {
        }

        Set<Person> hashOfPeople = new HashSet<>();
        hashOfPeople.add(new Person("Alice", 30));
        hashOfPeople.add(new Person("Bob", 30));
        System.out.println();
        System.out.println("    HashSet, using equals()/hashCode():");
        System.out.println("      added Alice(30) and Bob(30) -> size = " + hashOfPeople.size()
                + "   (both kept - equals() says they differ)");

        Set<Person> treeOfPeople = new TreeSet<>(Comparator.comparingInt(Person::age));
        treeOfPeople.add(new Person("Alice", 30));
        boolean bobAdded = treeOfPeople.add(new Person("Bob", 30));
        System.out.println();
        System.out.println("    TreeSet, with a Comparator that only looks at age:");
        System.out.println("      added Alice(30)             -> size = " + treeOfPeople.size());
        System.out.println("      add(Bob(30)) returned        -> " + bobAdded);
        System.out.println("      final set                    -> " + treeOfPeople);
        System.out.println();
        System.out.println("    Bob VANISHED. TreeSet considers two elements duplicates when");
        System.out.println("    compareTo() (or the Comparator) returns 0 - it never calls");
        System.out.println("    equals() at all. A Comparator that only orders by one field is");
        System.out.println("    a silent DATA-LOSS bug the moment two elements tie on that");
        System.out.println("    field. If ties matter, break them explicitly:");
        System.out.println("      Comparator.comparingInt(Person::age).thenComparing(Person::name)");

        Set<Person> fixedTree = new TreeSet<>(
                Comparator.comparingInt(Person::age).thenComparing(Person::name));
        fixedTree.add(new Person("Alice", 30));
        fixedTree.add(new Person("Bob", 30));
        System.out.println();
        System.out.println("    with the tie-break added        -> size = " + fixedTree.size()
                + "   (both kept)");


        /* ====================================================================
         * SECTION 6 - NavigableSet: WHAT TreeSet BUYS YOU BEYOND Set
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - TreeSet's EXTRA POWERS (NavigableSet)");
        System.out.println("=".repeat(74));

        NavigableSet<Integer> numbers = new TreeSet<>(List.of(10, 20, 30, 40, 50));
        System.out.println("    set                    -> " + numbers);
        System.out.println("    first() / last()       -> " + numbers.first() + " / " + numbers.last());
        System.out.println("    floor(25)  (<= 25)     -> " + numbers.floor(25));
        System.out.println("    ceiling(25) (>= 25)    -> " + numbers.ceiling(25));
        System.out.println("    lower(30)   (< 30)     -> " + numbers.lower(30));
        System.out.println("    higher(30)  (> 30)     -> " + numbers.higher(30));
        System.out.println("    headSet(30)  [< 30)    -> " + numbers.headSet(30));
        System.out.println("    tailSet(30)  [>= 30)   -> " + numbers.tailSet(30));
        System.out.println("    subSet(20, 40) [20,40) -> " + numbers.subSet(20, 40));
        System.out.println("    descendingSet()         -> " + numbers.descendingSet());
        System.out.println();
        System.out.println("    None of this exists on HashSet or LinkedHashSet - it needs a");
        System.out.println("    total order, which only TreeSet maintains. headSet/tailSet/");
        System.out.println("    subSet are LIVE VIEWS, exactly like List.subList (lesson 45).");

        Integer polled = numbers.pollFirst();
        System.out.println();
        System.out.println("    pollFirst() removed     -> " + polled + "   set is now " + numbers);


        /* ====================================================================
         * SECTION 7 - Set.of(), DUPLICATES, AND THE METHODS WORTH KNOWING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - IMMUTABLE SETS AND add()'S RETURN VALUE");
        System.out.println("=".repeat(74));

        Set<String> immutable = Set.of("a", "b", "c");
        System.out.println("    Set.of(\"a\", \"b\", \"c\") -> " + immutable);
        try {
            immutable.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("    .add(\"d\")              -> UnsupportedOperationException");
        }
        try {
            Set.of("a", "a");
        } catch (IllegalArgumentException e) {
            System.out.println("    Set.of(\"a\", \"a\")       -> IllegalArgumentException"
                    + "   (rejects duplicates at construction, unlike new HashSet<>(...))");
        }

        System.out.println();
        Set<String> mutable = new HashSet<>();
        boolean firstAdd = mutable.add("x");
        boolean secondAdd = mutable.add("x");
        System.out.println("    add() TELLS you whether it changed the set:");
        System.out.println("      first add(\"x\")  -> " + firstAdd);
        System.out.println("      second add(\"x\") -> " + secondAdd + "   (already present - no-op)");

        Set<Integer> a = new HashSet<>(List.of(1, 2, 3, 4));
        Set<Integer> b = new HashSet<>(List.of(3, 4, 5, 6));
        Set<Integer> union = new HashSet<>(a);
        union.addAll(b);
        Set<Integer> intersection = new HashSet<>(a);
        intersection.retainAll(b);
        Set<Integer> difference = new HashSet<>(a);
        difference.removeAll(b);
        System.out.println();
        System.out.println("    SET ALGEBRA via the bulk methods:");
        System.out.println("      a = " + a + ", b = " + b);
        System.out.println("      union         a.addAll(b)   -> " + union);
        System.out.println("      intersection  a.retainAll(b)-> " + intersection);
        System.out.println("      difference    a.removeAll(b)-> " + difference);


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 46.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Times {@code lookups} calls to {@code contains()}, half hits and half
     * misses, over a {@code List} or {@code Set}.
     *
     * @param collection the collection under test
     * @param lookups    how many contains() calls to make
     * @param bound      the range of values present in the collection
     * @return elapsed milliseconds
     */
    static long timeContains(java.util.Collection<Integer> collection, int lookups, int bound) {
        long start = System.nanoTime();
        int hits = 0;
        for (int i = 0; i < lookups; i++) {
            if (collection.contains(i % (bound * 2))) {
                hits++;
            }
        }
        long elapsed = System.nanoTime() - start;
        if (hits == Integer.MIN_VALUE) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Times {@code count} contains() calls over a {@code Set} of keys whose
     * {@code hashCode()} quality is under test.
     *
     * @param set   the set to query
     * @param count how many contains() calls to make
     * @return elapsed milliseconds
     */
    static <T> long timeContainsKey(Set<T> set, int count) {
        long start = System.nanoTime();
        int hits = 0;
        for (T key : set) {
            if (set.contains(key)) {
                hits++;
            }
        }
        long elapsed = System.nanoTime() - start;
        if (hits == Integer.MIN_VALUE) System.out.print("");
        return elapsed / 1_000_000;
    }
}

/**
 * A key with a GOOD {@code hashCode()} - spreads across buckets the way
 * {@code Integer}'s own does.
 */
class GoodHashKey {
    private final int id;

    GoodHashKey(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GoodHashKey other && other.id == id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

/**
 * A key with a DELIBERATELY BAD {@code hashCode()} - every instance hashes
 * to the same bucket, while {@code equals()} still compares correctly. Legal
 * per the {@code equals}/{@code hashCode} contract (lesson 33: equal objects
 * must hash equal) and catastrophic for performance anyway.
 */
class BadHashKey {
    private final int id;

    BadHashKey(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BadHashKey other && other.id == id;
    }

    @Override
    public int hashCode() {
        return 42;   // every instance collides - the one deliberate bug in this file
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Change BadHashKey's hashCode() to "return id % 10;" (10 buckets instead
 *    of 1). Re-measure at 20,000 keys. How much does spreading across even a
 *    SMALL number of buckets help?
 *
 * 2. Section 4 uses 20,000 keys. Try 100,000. Does the gap between good and
 *    bad hashCode() grow, shrink, or stay the same ratio? Explain why using
 *    what lesson 47 will cover about treeified buckets.
 *
 * 3. Build a TreeSet<String> with Comparator.comparingInt(String::length).
 *    Add "cat", "dog", "bird". Predict the size before running it.
 *
 * 4. Implement a Money class with amount and currency fields. Write a BAD
 *    equals()/hashCode() pair that only considers amount. Show a HashSet
 *    silently merging $100 and 100 (a different currency).
 *
 * 5. Using headSet/tailSet/subSet on a TreeSet<Integer>, implement a method
 *    that returns "how many elements fall in a range [lo, hi)" WITHOUT
 *    iterating - using only the NavigableSet API.
 * ============================================================================
 */
