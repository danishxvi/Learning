/* ============================================================================
 * 44 - THE COLLECTIONS FRAMEWORK, MAPPED OUT
 * ----------------------------------------------------------------------------
 * Companion lesson: 44-collections-framework-overview.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/44-collections-framework-overview.java
 *
 * Before the detail of lessons 45-50, here is the whole map: what the
 * interfaces are, what implements them, and how to choose between them.
 * ============================================================================
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

class CollectionsFrameworkOverview {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE SHAPE OF IT
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE HIERARCHY");
        System.out.println("=".repeat(74));

        System.out.println("                      Iterable<E>");
        System.out.println("                          |");
        System.out.println("                     Collection<E>");
        System.out.println("          ________________|________________");
        System.out.println("         |                |                |");
        System.out.println("      List<E>          Set<E>          Queue<E>");
        System.out.println("         |                |                |");
        System.out.println("   ArrayList        HashSet           ArrayDeque");
        System.out.println("   LinkedList       LinkedHashSet     PriorityQueue");
        System.out.println("   Vector (legacy)  TreeSet           LinkedList");
        System.out.println();
        System.out.println("      Map<K,V>          <- NOT a Collection");
        System.out.println("         |");
        System.out.println("    HashMap");
        System.out.println("    LinkedHashMap");
        System.out.println("    TreeMap");
        System.out.println("    Hashtable (legacy)");

        System.out.println();
        System.out.println("  TWO THINGS TO NOTICE IMMEDIATELY:");
        System.out.println();
        System.out.println("  1. Map is NOT a Collection. Proven from the classes themselves:");
        System.out.println("       Collection.isAssignableFrom(Map.class) -> "
                + Collection.class.isAssignableFrom(Map.class));
        System.out.println("       Collection.isAssignableFrom(List.class) -> "
                + Collection.class.isAssignableFrom(List.class));
        System.out.println("     A Map holds PAIRS, not elements, so add(E) makes no sense.");
        System.out.println("     Reach a collection view with keySet(), values() or entrySet():");
        Map<String, Integer> ages = new LinkedHashMap<>();
        ages.put("Danish", 25);
        ages.put("Aisha", 30);
        System.out.println("       map.keySet()   -> " + ages.keySet());
        System.out.println("       map.values()   -> " + ages.values());
        System.out.println("       map.entrySet() -> " + ages.entrySet());

        System.out.println();
        System.out.println("  2. LinkedList implements BOTH List and Deque:");
        System.out.println("       LinkedList is a List  -> " + (new LinkedList<>() instanceof List));
        System.out.println("       LinkedList is a Deque -> " + (new LinkedList<>() instanceof Deque));
        System.out.println("     Which is why you sometimes see it used as a queue.");

        System.out.println();
        System.out.println("  And everything is Iterable, which is what makes the enhanced");
        System.out.println("  for loop work on all of them (lesson 10):");
        for (Class<?> type : new Class<?>[]{List.class, Set.class, Queue.class, Map.class}) {
            System.out.printf("       %-10s Iterable? %s%n",
                    type.getSimpleName(), Iterable.class.isAssignableFrom(type));
        }


        /* ====================================================================
         * SECTION 2 - THE FOUR CORE INTERFACES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - WHAT EACH ONE GUARANTEES");
        System.out.println("=".repeat(74));

        System.out.printf("    %-10s %-34s %-14s %s%n",
                "INTERFACE", "GUARANTEES", "DUPLICATES?", "ORDERED?");
        System.out.printf("    %-10s %-34s %-14s %s%n",
                "List", "indexed, positional access", "YES", "insertion order");
        System.out.printf("    %-10s %-34s %-14s %s%n",
                "Set", "no duplicates", "no", "depends");
        System.out.printf("    %-10s %-34s %-14s %s%n",
                "Queue", "ordered for processing", "yes", "FIFO or priority");
        System.out.printf("    %-10s %-34s %-14s %s%n",
                "Map", "key -> value", "keys no", "depends");

        System.out.println();
        System.out.println("  Seeing the difference in one line each:");

        List<String> list = new ArrayList<>(List.of("a", "b", "a"));
        Set<String> set = new HashSet<>(List.of("a", "b", "a"));
        Queue<String> queue = new ArrayDeque<>(List.of("a", "b", "a"));

        System.out.println("    from [a, b, a]:");
        System.out.println("      List  -> " + list + "   duplicates kept, order kept");
        System.out.println("      Set   -> " + set + "      duplicate dropped");
        System.out.println("      Queue -> " + queue + "   kept, but you poll() from the front: "
                + queue.peek());

        System.out.println();
        System.out.println("  Collection's own methods, which everything above inherits:");
        System.out.println("      add  remove  contains  size  isEmpty  clear");
        System.out.println("      addAll  removeAll  retainAll  containsAll");
        System.out.println("      iterator  stream  forEach  removeIf  toArray");
        System.out.println();
        System.out.println("    removeIf (Java 8) is the RIGHT way to filter in place -");
        System.out.println("    it avoids the ConcurrentModificationException from lesson 10:");
        List<String> filtered = new ArrayList<>(List.of("a", "bb", "ccc", "dddd"));
        filtered.removeIf(item -> item.length() < 3);
        System.out.println("      removeIf(length < 3) -> " + filtered);

        System.out.println();
        System.out.println("    retainAll and removeAll are set operations on any collection:");
        List<String> left = new ArrayList<>(List.of("a", "b", "c", "d"));
        left.retainAll(List.of("b", "c", "z"));
        System.out.println("      [a,b,c,d].retainAll([b,c,z]) -> " + left + "   (intersection)");


        /* ====================================================================
         * SECTION 3 - CHOOSING AN IMPLEMENTATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE PRACTICAL CORE OF THE WHOLE FRAMEWORK");
        System.out.println("=".repeat(74));

        System.out.printf("    %-42s %-20s %s%n", "I NEED", "USE", "WHY");
        printChoice("an ordered list, indexed access", "ArrayList", "O(1) get; the default");
        printChoice("lots of insert/remove at the ends", "ArrayDeque", "beats LinkedList");
        printChoice("no duplicates, order irrelevant", "HashSet", "O(1) operations");
        printChoice("no duplicates, insertion order kept", "LinkedHashSet", "O(1) plus order");
        printChoice("no duplicates, sorted", "TreeSet", "O(log n), sorted");
        printChoice("key->value, order irrelevant", "HashMap", "O(1); the default");
        printChoice("key->value, insertion order kept", "LinkedHashMap", "also LRU caches");
        printChoice("key->value, sorted by key", "TreeMap", "O(log n), range queries");
        printChoice("a queue", "ArrayDeque", "beats LinkedList");
        printChoice("a priority queue", "PriorityQueue", "heap-ordered");
        printChoice("a stack", "ArrayDeque", "NOT Stack - see Section 5");
        printChoice("enum keys", "EnumMap / EnumSet", "array/bitset (lesson 35)");
        printChoice("a thread-safe map", "ConcurrentHashMap", "not Hashtable (lesson 65)");

        System.out.println();
        System.out.println("  DEFAULT TO ArrayList AND HashMap. Change only with a reason.");


        /* ====================================================================
         * SECTION 4 - COMPLEXITY, AND WHERE THE TABLE LIES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - COMPLEXITY");
        System.out.println("=".repeat(74));

        System.out.printf("    %-16s %-14s %-14s %-12s %s%n",
                "OPERATION", "ArrayList", "LinkedList", "HashSet", "TreeSet");
        System.out.printf("    %-16s %-14s %-14s %-12s %s%n",
                "get(i)", "O(1)", "O(n)", "-", "-");
        System.out.printf("    %-16s %-14s %-14s %-12s %s%n",
                "add (end)", "O(1)*", "O(1)", "O(1)", "O(log n)");
        System.out.printf("    %-16s %-14s %-14s %-12s %s%n",
                "add (start)", "O(n)", "O(1)", "-", "-");
        System.out.printf("    %-16s %-14s %-14s %-12s %s%n",
                "remove(i)", "O(n)", "O(n)+", "-", "-");
        System.out.printf("    %-16s %-14s %-14s %-12s %s%n",
                "contains", "O(n)", "O(n)", "O(1)", "O(log n)");
        System.out.println();
        System.out.println("    * amortised - it doubles and copies occasionally");
        System.out.println("    + O(1) once you HOLD the node, O(n) to find it");

        System.out.println();
        System.out.println("  THE TABLE LIES ABOUT LinkedList. Watch a lookup-heavy workload:");

        int lookupSize = 40_000;
        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();
        for (int i = 0; i < lookupSize; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }

        long startArray = System.nanoTime();
        long arraySum = 0;
        for (int i = 0; i < lookupSize; i++) {
            arraySum += arrayList.get(i);
        }
        long arrayMillis = (System.nanoTime() - startArray) / 1_000_000;

        long startLinked = System.nanoTime();
        long linkedSum = 0;
        for (int i = 0; i < lookupSize; i++) {
            linkedSum += linkedList.get(i);
        }
        long linkedMillis = (System.nanoTime() - startLinked) / 1_000_000;

        System.out.printf("    %,d indexed reads:%n", lookupSize);
        System.out.println("      ArrayList  -> " + arrayMillis + " ms");
        System.out.println("      LinkedList -> " + linkedMillis + " ms");
        System.out.println("      (checksums " + arraySum + " and " + linkedSum + ")");
        System.out.println();
        System.out.println("    LinkedList's O(1) insertion assumes you ALREADY HOLD the node.");
        System.out.println("    Traversal is a pointer-chase across scattered heap objects,");
        System.out.println("    which destroys CPU cache locality (lesson 13's lesson again).");
        System.out.println();
        System.out.println("    In practice ArrayList beats LinkedList at almost everything.");
        System.out.println("    Lesson 45 measures it properly.");


        /* ====================================================================
         * SECTION 5 - THE LEGACY CLASSES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - WHAT NOT TO USE");
        System.out.println("=".repeat(74));

        System.out.printf("    %-16s %-18s %s%n", "LEGACY", "USE INSTEAD", "WHY");
        System.out.printf("    %-16s %-18s %s%n", "Vector", "ArrayList", "synchronized for no benefit");
        System.out.printf("    %-16s %-18s %s%n", "Hashtable", "HashMap", "same; also rejects null");
        System.out.printf("    %-16s %-18s %s%n", "Stack", "ArrayDeque", "extends Vector (lesson 26)");
        System.out.printf("    %-16s %-18s %s%n", "Enumeration", "Iterator", "older, weaker interface");

        System.out.println();
        System.out.println("  Stack's problem, demonstrated - it inherited methods that break");
        System.out.println("  the very guarantee a stack is supposed to make:");
        java.util.Stack<Integer> brokenStack = new java.util.Stack<>();
        brokenStack.push(1);
        brokenStack.push(2);
        brokenStack.push(3);
        System.out.println("      after three pushes  -> " + brokenStack);
        brokenStack.add(0, 99);
        System.out.println("      stack.add(0, 99)    -> " + brokenStack
                + "   <- inserted at the BOTTOM");
        System.out.println();
        System.out.println("  ArrayDeque as a stack - only push/pop/peek, as intended:");
        Deque<Integer> properStack = new ArrayDeque<>();
        properStack.push(1);
        properStack.push(2);
        properStack.push(3);
        System.out.println("      push 1,2,3 -> " + properStack + "   (top first)");
        System.out.println("      pop()      -> " + properStack.pop() + ", leaving " + properStack);

        System.out.println();
        System.out.println("  These stay in the JDK for compatibility and will never be");
        System.out.println("  removed. That is not an endorsement.");


        /* ====================================================================
         * SECTION 6 - IMMUTABLE, UNMODIFIABLE AND FIXED-SIZE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - THREE THINGS PEOPLE CONFLATE");
        System.out.println("=".repeat(74));

        List<String> backing = new ArrayList<>(List.of("a", "b"));
        List<String> immutable = List.of("a", "b");
        List<String> unmodifiableView = Collections.unmodifiableList(backing);
        String[] array = {"a", "b"};
        List<String> asListView = Arrays.asList(array);

        System.out.printf("    %-34s %-16s %s%n", "", "CAN YOU ADD?", "SEES LATER CHANGES?");
        System.out.printf("    %-34s %-16s %s%n", "List.of(...)", "no", "n/a - immutable");
        System.out.printf("    %-34s %-16s %s%n", "Collections.unmodifiableList(l)", "no", "YES - a live view");
        System.out.printf("    %-34s %-16s %s%n", "Arrays.asList(array)", "no (fixed size)", "YES - writes through");
        System.out.printf("    %-34s %-16s %s%n", "new ArrayList<>(...)", "yes", "no");

        System.out.println();
        backing.add("c");
        System.out.println("    after backing.add(\"c\"):");
        System.out.println("      the unmodifiable VIEW now reads -> " + unmodifiableView);
        System.out.println("      List.of(...) is unaffected      -> " + immutable);

        System.out.println();
        asListView.set(0, "CHANGED");
        System.out.println("    Arrays.asList is FIXED-SIZE but MUTABLE, and writes through:");
        System.out.println("      asList.set(0, \"CHANGED\") -> the ARRAY is now "
                + Arrays.toString(array));
        tryIt("List.of(...).add(\"x\")", () -> immutable.add("x"));
        tryIt("Arrays.asList(a).add(\"x\")", () -> asListView.add("x"));

        System.out.println();
        System.out.println("    List.of also REJECTS null; the others do not:");
        tryIt("List.of(\"a\", null)", () -> List.of("a", null));


        /* ====================================================================
         * SECTION 7 - null HANDLING DIFFERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WHICH COLLECTIONS ACCEPT null");
        System.out.println("=".repeat(74));

        System.out.printf("    %-24s %s%n", "COLLECTION", "RESULT OF ADDING null");
        tryNullElement("ArrayList", new ArrayList<>());
        tryNullElement("HashSet", new HashSet<>());
        tryNullElement("LinkedHashSet", new LinkedHashSet<>());
        tryNullElement("TreeSet", new TreeSet<>());
        tryNullElement("ArrayDeque", new ArrayDeque<>());
        tryNullElement("PriorityQueue", new PriorityQueue<>());

        System.out.println();
        System.out.printf("    %-24s %s%n", "MAP", "RESULT OF A null KEY");
        tryNullKey("HashMap", new HashMap<>());
        tryNullKey("LinkedHashMap", new LinkedHashMap<>());
        tryNullKey("TreeMap", new TreeMap<>());
        tryNullKey("ConcurrentHashMap", new ConcurrentHashMap<>());

        System.out.println();
        System.out.println("  HashMap allowing ONE null key is a genuine oddity - it special-");
        System.out.println("  cases it into bucket 0, because null.hashCode() would throw.");
        System.out.println();
        System.out.println("  ConcurrentHashMap FORBIDS it, because under concurrency a null");
        System.out.println("  return from get() would be ambiguous: 'absent' or 'present with");
        System.out.println("  a null value'? Without a lock you cannot follow up with");
        System.out.println("  containsKey() and trust the answer.");
        System.out.println();
        System.out.println("  TreeSet and TreeMap forbid it because they must COMPARE keys,");
        System.out.println("  and null has no ordering.");


        /* ====================================================================
         * SECTION 8 - ITERATION ORDER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - WHAT ORDER IS ACTUALLY GUARANTEED");
        System.out.println("=".repeat(74));

        List<String> input = List.of("zebra", "apple", "mango", "banana");

        System.out.println("  Inserting " + input + " into each:");
        System.out.println();
        System.out.println("    ArrayList     -> " + new ArrayList<>(input)
                + "   INSERTION order");
        System.out.println("    HashSet       -> " + new HashSet<>(input)
                + "   NO GUARANTEE");
        System.out.println("    LinkedHashSet -> " + new LinkedHashSet<>(input)
                + "   INSERTION order");
        System.out.println("    TreeSet       -> " + new TreeSet<>(input)
                + "   SORTED");

        // Needs enough elements for the heap shape to differ visibly from
        // sorted order - with only three or four it often coincides.
        List<Integer> numbers = List.of(5, 1, 4, 2, 8, 3, 9, 7);
        PriorityQueue<Integer> priorityQueue = new PriorityQueue<>(numbers);

        System.out.println();
        System.out.println("  PriorityQueue is the surprising one. Inserting " + numbers + ":");
        System.out.println("    iterating it  -> " + priorityQueue + "   <- HEAP order, NOT sorted");

        List<Integer> polled = new ArrayList<>();
        PriorityQueue<Integer> draining = new PriorityQueue<>(numbers);
        while (!draining.isEmpty()) {
            polled.add(draining.poll());
        }
        System.out.println("    poll()ing it  -> " + polled + "   sorted, as promised");
        System.out.println();
        System.out.println("    Only poll() returns elements in priority sequence. A heap");
        System.out.println("    guarantees the SMALLEST is at the root and nothing more -");
        System.out.println("    the rest is merely heap-ordered. Iterating a PriorityQueue");
        System.out.println("    expecting sorted output is a real and common bug.");

        System.out.println();
        System.out.println("  HashMap's order is STABLE WITHIN A RUN but is not part of the");
        System.out.println("  contract. It changes with capacity, and has changed between Java");
        System.out.println("  versions. Watch it change as the map grows:");
        for (int entryCount : new int[]{6, 20, 40}) {
            Map<String, String> hashMap = new HashMap<>();
            for (int i = 1; i <= entryCount; i++) {
                hashMap.put("key-" + i, "v");
            }
            List<String> keys = new ArrayList<>(hashMap.keySet());
            List<String> firstFew = keys.subList(0, Math.min(5, keys.size()));
            System.out.printf("    %2d entries -> first keys are %s%n", entryCount, firstFew);
        }
        System.out.println();
        System.out.println("  Code that depends on HashMap order is broken code that has not");
        System.out.println("  failed yet. Use LinkedHashMap when order matters.");


        /* ====================================================================
         * SECTION 9 - SEQUENCED COLLECTIONS (Java 21)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - SEQUENCED COLLECTIONS (Java 21)");
        System.out.println("=".repeat(74));

        System.out.println("  New interfaces giving every ORDERED collection a uniform");
        System.out.println("  first/last API:");
        System.out.println();
        System.out.println("      SequencedCollection<E>  getFirst getLast addFirst addLast");
        System.out.println("                              removeFirst removeLast reversed");
        System.out.println("      SequencedSet<E>");
        System.out.println("      SequencedMap<K,V>       firstEntry lastEntry reversed");

        System.out.println();
        List<String> sequenced = new ArrayList<>(List.of("first", "middle", "last"));
        System.out.println("    list                -> " + sequenced);
        System.out.println("    list.getFirst()     -> " + sequenced.getFirst()
                + "   (instead of get(0))");
        System.out.println("    list.getLast()      -> " + sequenced.getLast()
                + "    (instead of get(size() - 1))");
        System.out.println("    list.reversed()     -> " + sequenced.reversed()
                + "   a VIEW, not a copy");

        LinkedHashSet<String> orderedSet = new LinkedHashSet<>(List.of("a", "b", "c"));
        System.out.println();
        System.out.println("    LinkedHashSet.getFirst() -> " + orderedSet.getFirst());
        System.out.println("      Before Java 21 there was NO way to get a LinkedHashSet's");
        System.out.println("      first element without creating an iterator.");

        LinkedHashMap<String, Integer> orderedMap = new LinkedHashMap<>();
        orderedMap.put("first", 1);
        orderedMap.put("last", 2);
        System.out.println();
        System.out.println("    LinkedHashMap.firstEntry() -> " + orderedMap.firstEntry());
        System.out.println("    LinkedHashMap.lastEntry()  -> " + orderedMap.lastEntry());

        System.out.println();
        System.out.println("  This filled a long-standing gap: List had get(0), Deque had");
        System.out.println("  getFirst(), and LinkedHashSet had nothing at all.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 44. Lessons 45-48 take each family in turn.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Prints one row of the choosing table.
     *
     * @param need the requirement
     * @param use  the recommended implementation
     * @param why  the reason
     */
    static void printChoice(String need, String use, String why) {
        System.out.printf("    %-42s %-20s %s%n", need, use, why);
    }

    /**
     * Runs an action and reports whether it was allowed or refused.
     *
     * @param description what is being attempted
     * @param action      the attempt
     */
    static void tryIt(String description, Runnable action) {
        try {
            action.run();
            System.out.printf("      %-30s -> allowed%n", description);
        } catch (RuntimeException e) {
            System.out.printf("      %-30s -> %s%n", description, e.getClass().getSimpleName());
        }
    }

    /**
     * Attempts to add null to a collection and reports the outcome.
     *
     * @param name       the collection's name
     * @param collection the collection to test
     */
    static void tryNullElement(String name, Collection<String> collection) {
        try {
            collection.add(null);
            System.out.printf("    %-24s accepted%n", name);
        } catch (RuntimeException e) {
            System.out.printf("    %-24s %s%n", name, e.getClass().getSimpleName());
        }
    }

    /**
     * Attempts to use null as a map key and reports the outcome.
     *
     * @param name the map's name
     * @param map  the map to test
     */
    static void tryNullKey(String name, Map<String, String> map) {
        try {
            map.put(null, "value");
            System.out.printf("    %-24s accepted (one null key)%n", name);
        } catch (RuntimeException e) {
            System.out.printf("    %-24s %s%n", name, e.getClass().getSimpleName());
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. For each of these, name the collection you would use and why:
 *    - the last 100 log lines, oldest evicted first
 *    - unique visitor IDs seen today
 *    - words in a document, in alphabetical order
 *    - a task list processed by deadline
 *    - HTTP headers, preserving the order they arrived
 *
 * 2. Run Section 8's HashMap ordering demonstration a few times. Then add
 *    entries one at a time, printing the key order after each, and find the
 *    exact insertion that reorders everything. That is a resize.
 *
 * 3. Take Section 4's benchmark to 200,000 elements. Does the ratio grow
 *    linearly? Explain why using the word "quadratic".
 *
 * 4. Write the same "count word frequencies" program with HashMap,
 *    LinkedHashMap and TreeMap. Print each. When would you ship each one?
 *
 * 5. Prove Collections.unmodifiableList is a VIEW and List.copyOf is not,
 *    in five lines.
 *
 * 6. Use Java 21's reversed() on a List, then modify the ORIGINAL list and
 *    print the reversed view again. It is a view - confirm it updated.
 * ============================================================================
 */
