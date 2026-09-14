/* ============================================================================
 * 47 - Map: HashMap internals and friends
 * ----------------------------------------------------------------------------
 * Companion lesson: 47-map-implementations.md
 *
 * RUN IT:
 *     java Java/09-generics-and-collections/47-map-implementations.java
 *
 * Lesson 46 measured what a bad hashCode() costs. This lesson goes inside the
 * structure that cost is paid against - and Section 5 produces a real,
 * reproducible bug: a key that goes missing while it is still in the map.
 * ============================================================================
 */

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

class MapImplementations {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE BUCKET ARRAY, AND HOW A KEY FINDS ITS BUCKET
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - HOW A HashMap IS ACTUALLY BUILT");
        System.out.println("=".repeat(74));

        System.out.println("  HashMap<K, V> holds an array of buckets:");
        System.out.println();
        System.out.println("      table:  [0][1][2][3][4][5][6][7]  ...  capacity 16 by default");
        System.out.println("                       |");
        System.out.println("                       v");
        System.out.println("              Node(key, value, hash, next) -> Node -> null");
        System.out.println();
        System.out.println("  Each slot holds a Node whose hash COLLIDED into it - a chain, in");
        System.out.println("  the simple case. put(k, v) does three things:");
        System.out.println();
        System.out.println("    1. h = k.hashCode() ^ (k.hashCode() >>> 16)   <- SPREAD the bits");
        System.out.println("    2. index = h & (table.length - 1)             <- fast mod, needs a");
        System.out.println("                                                     POWER-OF-TWO capacity");
        System.out.println("    3. walk that bucket's chain: replace on an equals() match,");
        System.out.println("       otherwise append a new Node");
        System.out.println();
        System.out.println("  WHY XOR THE HIGH BITS IN? Because step 2 only looks at the LOW");
        System.out.println("  bits of the hash (capacity 16 means only 4 bits matter). Many");
        System.out.println("  real hashCode() implementations vary mostly in their HIGH bits -");
        System.out.println("  without spreading, those differences would never affect which");
        System.out.println("  bucket a key lands in, and collisions would be far more common");
        System.out.println("  than the hash function's quality suggests.");

        int h = "example".hashCode();
        int spread = h ^ (h >>> 16);
        System.out.println();
        System.out.println("    \"example\".hashCode()        -> " + h);
        System.out.println("    spread (h ^ (h >>> 16))     -> " + spread);
        System.out.println("    bucket index at capacity 16 -> " + (spread & 15));


        /* ====================================================================
         * SECTION 2 - LOAD FACTOR AND RESIZING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - LOAD FACTOR AND RESIZING");
        System.out.println("=".repeat(74));

        System.out.println("  Default capacity 16, default load factor 0.75. RESIZE THRESHOLD =");
        System.out.println("  capacity x load factor. Once size exceeds that, capacity DOUBLES");
        System.out.println("  and EVERY entry is rehashed into the new, larger table:");
        System.out.println();
        System.out.println("      capacity 16  -> resize after the 13th entry  (16 x 0.75  = 12)");
        System.out.println("      capacity 32  -> resize after the 25th entry  (32 x 0.75  = 24)");
        System.out.println("      capacity 64  -> resize after the 49th entry  (64 x 0.75  = 48)");
        System.out.println("      ... doubling, forever, unless you presize");
        System.out.println();
        System.out.println("  A LOWER load factor means MORE memory, FEWER collisions, resizes");
        System.out.println("  sooner. A HIGHER one means less memory, more collisions. 0.75 is");
        System.out.println("  Java's measured sweet spot; changing it is rarely worth it.");

        int size = 2_000_000;

        long startUnsized = System.nanoTime();
        Map<Integer, Integer> unsized = new java.util.HashMap<>();
        for (int i = 0; i < size; i++) {
            unsized.put(i, i);
        }
        long unsizedMillis = (System.nanoTime() - startUnsized) / 1_000_000;

        // The constructor takes the INITIAL CAPACITY, not the expected entry
        // count - to hold "size" entries without ANY resize, ask for
        // size / loadFactor, exactly as the real formula demands.
        int neededCapacity = (int) (size / 0.75) + 1;
        long startPresized = System.nanoTime();
        Map<Integer, Integer> presized = new java.util.HashMap<>(neededCapacity);
        for (int i = 0; i < size; i++) {
            presized.put(i, i);
        }
        long presizedMillis = (System.nanoTime() - startPresized) / 1_000_000;

        System.out.println();
        System.out.printf("    putting %,d entries:%n", size);
        System.out.println("      new HashMap<>()                    -> " + unsizedMillis + " ms   (~21 resizes)");
        System.out.println("      new HashMap<>(" + neededCapacity + ")     -> " + presizedMillis + " ms   (zero resizes)");
        System.out.println();
        System.out.println("    Every resize allocates a new, DOUBLE-SIZED array and rehashes");
        System.out.println("    every single existing entry into it. Presizing when the final");
        System.out.println("    size is known avoids all of that - same trade-off as ArrayList");
        System.out.println("    presizing in lesson 45, for the identical reason.");


        /* ====================================================================
         * SECTION 3 - TREEIFICATION: THE SAFETY NET UNDER A BAD hashCode()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - WHEN A BUCKET BECOMES A TREE, NOT A LIST");
        System.out.println("=".repeat(74));

        System.out.println("  Since Java 8, HashMap does not just accept an O(n) chain forever.");
        System.out.println("  A bucket is converted from a linked list to a RED-BLACK TREE when");
        System.out.println("  BOTH of these are true (constants straight from HashMap's own");
        System.out.println("  documented source):");
        System.out.println();
        System.out.println("      TREEIFY_THRESHOLD    = 8    <- that bucket has >= 8 nodes");
        System.out.println("      MIN_TREEIFY_CAPACITY = 64   <- AND the table itself has >= 64 slots");
        System.out.println();
        System.out.println("  If the table is still small, HashMap RESIZES instead of");
        System.out.println("  treeifying that one bucket - growing the whole table first is");
        System.out.println("  usually enough to spread a collision out on its own.");
        System.out.println();
        System.out.println("  This is precisely what softened lesson 46's BadHashKey disaster:");
        System.out.println("  20,000 keys all hashing to bucket 42 is FAR more than 8, and the");
        System.out.println("  table long since grew past 64 slots - so that bucket was almost");
        System.out.println("  certainly a TREE by the time lookups were measured, capping the");
        System.out.println("  damage at O(log n) instead of true O(n). Even so, it was still");
        System.out.println("  roughly 1,000x slower than a good hashCode(). Treeification is a");
        System.out.println("  safety net, not a fix - it turns a catastrophe into a merely bad");
        System.out.println("  outcome. Writing a real hashCode() is still the actual fix.");


        /* ====================================================================
         * SECTION 4 - THE MODERN Map METHODS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - getOrDefault, computeIfAbsent, merge, compute");
        System.out.println("=".repeat(74));

        String text = "the quick brown fox the lazy dog the fox runs";
        String[] words = text.split(" ");

        System.out.println("  Word-frequency counting, three ways over the SAME sentence:");
        System.out.println("    \"" + text + "\"");

        System.out.println();
        System.out.println("  THE OLD WAY - explicit contains-then-put:");
        Map<String, Integer> oldStyle = new java.util.HashMap<>();
        for (String word : words) {
            if (oldStyle.containsKey(word)) {
                oldStyle.put(word, oldStyle.get(word) + 1);
            } else {
                oldStyle.put(word, 1);
            }
        }
        System.out.println("    " + oldStyle + "   <- TWO map lookups per word");

        System.out.println();
        System.out.println("  getOrDefault - one lookup, still an explicit put:");
        Map<String, Integer> withDefault = new java.util.HashMap<>();
        for (String word : words) {
            withDefault.put(word, withDefault.getOrDefault(word, 0) + 1);
        }
        System.out.println("    " + withDefault);

        System.out.println();
        System.out.println("  merge - ONE call, no branching, the map's own idiom for this:");
        Map<String, Integer> withMerge = new java.util.HashMap<>();
        for (String word : words) {
            withMerge.merge(word, 1, Integer::sum);
        }
        System.out.println("    " + withMerge);
        System.out.println("    merge(key, 1, Integer::sum) means:");
        System.out.println("      absent      -> put(key, 1)");
        System.out.println("      present (v) -> put(key, Integer.sum(v, 1))");

        System.out.println();
        System.out.println("  computeIfAbsent - build-if-missing, the idiom for Map<K, List<V>>:");
        Map<Integer, java.util.List<String>> byLength = new java.util.HashMap<>();
        for (String word : words) {
            byLength.computeIfAbsent(word.length(), key -> new java.util.ArrayList<>()).add(word);
        }
        System.out.println("    grouped by word length -> " + byLength);
        System.out.println("    WITHOUT computeIfAbsent this is: if (!map.containsKey(k))");
        System.out.println("    map.put(k, new ArrayList<>()); map.get(k).add(word); - THREE");
        System.out.println("    operations and a bug waiting to happen if you forget one.");


        /* ====================================================================
         * SECTION 5 - THE MUTABLE-KEY BUG: A KEY THAT IS STILL IN THE MAP,
         *             AND UNFINDABLE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - MUTATE A KEY AFTER INSERTION AND WATCH IT VANISH");
        System.out.println("=".repeat(74));

        System.out.println("  MutablePoint (defined below) computes hashCode() from x and y,");
        System.out.println("  and both fields are mutable - deliberately, to reproduce a real,");
        System.out.println("  common bug.");

        Map<MutablePoint, String> pointNames = new java.util.HashMap<>();
        MutablePoint origin = new MutablePoint(0, 0);
        pointNames.put(origin, "origin");

        System.out.println();
        System.out.println("    put(new MutablePoint(0, 0), \"origin\")");
        System.out.println("    get(a point equal to (0, 0)) -> "
                + pointNames.get(new MutablePoint(0, 0)) + "   (found - works so far)");

        origin.setX(99);
        System.out.println();
        System.out.println("    origin.setX(99)   <- mutate the SAME object already stored as a key");
        System.out.println();
        System.out.println("    get(new MutablePoint(99, 0))  -> " + pointNames.get(new MutablePoint(99, 0))
                + "   <- NOT FOUND, even though origin IS (99, 0) now");
        System.out.println("    get(new MutablePoint(0, 0))   -> " + pointNames.get(new MutablePoint(0, 0))
                + "   <- NOT FOUND either - the OLD key no longer exists");
        System.out.println("    containsKey(origin) (the SAME object reference) -> "
                + pointNames.containsKey(origin) + "   <- still false!");
        System.out.println("    map.size()                    -> " + pointNames.size()
                + "   <- but the entry is STILL THERE");

        System.out.println();
        System.out.println("    WHY: put() computed hash(0, 0) and filed the entry in THAT");
        System.out.println("    bucket. setX(99) changed the object, but nothing re-files it -");
        System.out.println("    HashMap has no way to know a key changed out from under it. A");
        System.out.println("    later get() computes hash(99, 0), looks in a DIFFERENT bucket,");
        System.out.println("    and finds nothing. The entry is genuinely still in the map -");
        System.out.println("    iterating entrySet() below proves it - just permanently");
        System.out.println("    unreachable by key.");

        for (Map.Entry<MutablePoint, String> entry : pointNames.entrySet()) {
            System.out.println("      entrySet() still shows -> " + entry.getKey() + " = " + entry.getValue());
        }

        System.out.println();
        System.out.println("    THE RULE: never use a mutable object as a HashMap/HashSet key");
        System.out.println("    unless you can GUARANTEE it will never change while it is a key.");
        System.out.println("    Records (lesson 36, all fields final) are naturally immune.");


        /* ====================================================================
         * SECTION 6 - LinkedHashMap: INSERTION ORDER, OR A REAL LRU CACHE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - LinkedHashMap: ORDER, AND removeEldestEntry");
        System.out.println("=".repeat(74));

        Map<String, Integer> insertionOrdered = new LinkedHashMap<>();
        insertionOrdered.put("delta", 4);
        insertionOrdered.put("alpha", 1);
        insertionOrdered.put("charlie", 3);
        System.out.println("    default LinkedHashMap (insertion order) -> " + insertionOrdered);

        System.out.println();
        System.out.println("    ACCESS-ORDER MODE (the 3-arg constructor's third flag) moves an");
        System.out.println("    entry to the END every time it is get() or put() - the exact");
        System.out.println("    mechanism an LRU cache needs, and removeEldestEntry() is the");
        System.out.println("    hook that turns it into a REAL bounded cache:");

        LruCache<String, Integer> cache = new LruCache<>(3);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        System.out.println("      put a, b, c (capacity 3) -> " + cache);
        cache.get("a");
        System.out.println("      get(\"a\")                -> touches a, moving it to the end");
        cache.put("d", 4);
        System.out.println("      put(\"d\", 4)  (over capacity) -> " + cache
                + "   <- \"b\" was evicted, the LEAST recently used, not \"a\"");


        /* ====================================================================
         * SECTION 7 - TreeMap: SORTED KEYS AND NavigableMap
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - TreeMap: SORTED BY KEY, WITH NavigableMap");
        System.out.println("=".repeat(74));

        java.util.NavigableMap<Integer, String> scores = new TreeMap<>();
        scores.put(85, "Bob");
        scores.put(92, "Alice");
        scores.put(78, "Carol");
        scores.put(92, "Dave");   // same key as Alice - REPLACES her

        System.out.println("    put(85,Bob), put(92,Alice), put(78,Carol), put(92,Dave):");
        System.out.println("      " + scores + "   <- Dave REPLACED Alice at key 92, size stayed 3");
        System.out.println();
        System.out.println("    firstEntry() / lastEntry() -> " + scores.firstEntry()
                + " / " + scores.lastEntry());
        System.out.println("    floorKey(90)  (<= 90)      -> " + scores.floorKey(90));
        System.out.println("    ceilingKey(90) (>= 90)     -> " + scores.ceilingKey(90));
        System.out.println("    headMap(90)   [keys < 90)  -> " + scores.headMap(90));
        System.out.println("    tailMap(90)   [keys >= 90) -> " + scores.tailMap(90));
        System.out.println();
        System.out.println("    Exactly TreeSet's NavigableSet API (lesson 46) - because");
        System.out.println("    TreeSet literally IS a TreeMap<E, Object> underneath.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 47.");
        System.out.println("=".repeat(74));
    }
}

/**
 * A point whose {@code hashCode()} depends on mutable fields - the exact
 * shape of object that must NEVER be used as a {@code HashMap}/{@code
 * HashSet} key once it might change. See Section 5.
 */
class MutablePoint {
    private int x;
    private int y;

    MutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void setX(int x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MutablePoint other && other.x == x && other.y == y;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

/**
 * A genuinely bounded, real LRU cache - {@code LinkedHashMap} in
 * access-order mode plus {@code removeEldestEntry} is the ENTIRE
 * implementation; no manual list-splicing needed.
 *
 * @param <K> key type
 * @param <V> value type
 */
class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    LruCache(int capacity) {
        super(16, 0.75f, true);   // true = ACCESS order, not insertion order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Compute the bucket index (capacity 16) for five strings of your choice
 *    by hand, using the spreading formula in Section 1. Verify with
 *    ("string".hashCode() ^ ("string".hashCode() >>> 16)) & 15.
 *
 * 2. Section 2 presizes with size / 0.75 + 1. Presize with EXACTLY `size`
 *    instead and re-measure. Explain the (small) difference using the
 *    resize-threshold formula.
 *
 * 3. Reproduce Section 5's bug with a HashSet<MutablePoint> instead of a Map
 *    key - confirm contains() also silently fails after mutation.
 *
 * 4. Rewrite Section 4's computeIfAbsent grouping using
 *    Collectors.groupingBy from the Stream API (lesson 54/55) and compare
 *    the two versions for readability.
 *
 * 5. Change LruCache's capacity to 2 and trace through six puts and two
 *    gets by hand before running it, predicting each eviction.
 *
 * 6. Using TreeMap, implement a "closest key" lookup: given a target int not
 *    necessarily present, return whichever of floorKey/ceilingKey is
 *    numerically closer.
 * ============================================================================
 */
