/* ============================================================================
 * 33 - equals() AND hashCode() - THE CONTRACT
 * ----------------------------------------------------------------------------
 * Companion lesson: 33-equals-and-hashcode.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/33-equals-and-hashcode.java
 *
 * Getting these two wrong produces NO compile error and NO exception. It
 * produces COLLECTIONS THAT SILENTLY LOSE YOUR DATA. That is why this has its
 * own lesson, and why every failure below is demonstrated rather than described.
 * ============================================================================
 */

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

class EqualsAndHashCode {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE DEFAULT IS IDENTITY
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Object.equals IS `this == other`");
        System.out.println("=".repeat(74));

        NoOverrides a = new NoOverrides(1, 2);
        NoOverrides b = new NoOverrides(1, 2);

        System.out.println("  Two objects with IDENTICAL contents, nothing overridden:");
        System.out.println("    a.equals(b)   -> " + a.equals(b));
        System.out.println("    a.hashCode()  -> " + a.hashCode());
        System.out.println("    b.hashCode()  -> " + b.hashCode() + "   <- a different number");
        System.out.println();
        System.out.println("  For objects representing a VALUE - money, a point, a date, an");
        System.out.println("  ID - identity is almost never what you want.");


        /* ====================================================================
         * SECTION 2 - THE hashCode RULE THAT BREAKS COLLECTIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - equals WITHOUT hashCode: DATA DISAPPEARS");
        System.out.println("=".repeat(74));

        System.out.println("  A class that overrides equals CORRECTLY but forgets hashCode:");
        System.out.println();

        BrokenPoint brokenA = new BrokenPoint(1, 2);
        BrokenPoint brokenB = new BrokenPoint(1, 2);

        System.out.println("    brokenA.equals(brokenB) -> " + brokenA.equals(brokenB)
                + "    equals works perfectly");
        System.out.println("    brokenA.hashCode()      -> " + brokenA.hashCode());
        System.out.println("    brokenB.hashCode()      -> " + brokenB.hashCode()
                + "   <- DIFFERENT, and that is the bug");

        Map<BrokenPoint, String> brokenMap = new HashMap<>();
        brokenMap.put(brokenA, "the value");

        System.out.println();
        System.out.println("    map.put(brokenA, \"the value\");");
        System.out.println("    map.get(brokenB)        -> " + brokenMap.get(brokenB)
                + "   <- NULL, despite an EQUAL key being present");
        System.out.println("    map.containsKey(brokenB)-> " + brokenMap.containsKey(brokenB));

        Set<BrokenPoint> brokenSet = new HashSet<>();
        brokenSet.add(brokenA);
        brokenSet.add(brokenB);
        System.out.println("    a Set given two EQUAL objects has size "
                + brokenSet.size() + "   <- should be 1");

        System.out.println();
        System.out.println("  WHY: HashMap and HashSet find an entry in TWO steps:");
        System.out.println("    1. compute hashCode() to pick a BUCKET");
        System.out.println("    2. search that bucket using equals()");
        System.out.println();
        System.out.println("  Different hash codes means different buckets, so step 2 NEVER");
        System.out.println("  HAPPENS. The map reports the key as absent while an equal key");
        System.out.println("  sits inside it.");

        System.out.println();
        System.out.println("  The SAME class with hashCode added:");

        GoodPoint goodA = new GoodPoint(1, 2);
        GoodPoint goodB = new GoodPoint(1, 2);
        Map<GoodPoint, String> goodMap = new HashMap<>();
        goodMap.put(goodA, "the value");

        System.out.println("    equal hash codes?       -> " + (goodA.hashCode() == goodB.hashCode()));
        System.out.println("    map.get(goodB)          -> " + goodMap.get(goodB));
        Set<GoodPoint> goodSet = new HashSet<>(List.of(goodA, goodB));
        System.out.println("    Set given two equal objects has size " + goodSet.size()
                + "   correct");

        System.out.println();
        System.out.println("  THE RULE: if a.equals(b), then a.hashCode() == b.hashCode().");
        System.out.println("  MANDATORY. The reverse is NOT required - collisions are fine,");
        System.out.println("  and unavoidable, since there are more objects than ints.");


        /* ====================================================================
         * SECTION 3 - THE equals CONTRACT, AND HOW TO BREAK IT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE FIVE PROPERTIES");
        System.out.println("=".repeat(74));

        System.out.printf("    %-16s %s%n", "REFLEXIVE", "x.equals(x) is true");
        System.out.printf("    %-16s %s%n", "SYMMETRIC", "x.equals(y) <=> y.equals(x)");
        System.out.printf("    %-16s %s%n", "TRANSITIVE", "x=y and y=z implies x=z");
        System.out.printf("    %-16s %s%n", "CONSISTENT", "repeated calls agree, if nothing changed");
        System.out.printf("    %-16s %s%n", "NON-NULL", "x.equals(null) is false, never throws");

        // Reflexivity broken by using == on a double field.
        System.out.println();
        System.out.println("  BREAKING REFLEXIVITY with == on a double:");

        NaiveDoubleHolder nan = new NaiveDoubleHolder(Double.NaN);
        System.out.println("    Double.NaN == Double.NaN  -> " + (Double.NaN == Double.NaN));
        System.out.println("    holder.equals(holder)     -> " + nan.equals(nan)
                + "   <- an object NOT EQUAL TO ITSELF");

        List<NaiveDoubleHolder> naiveList = new ArrayList<>(List.of(nan));
        System.out.println("    list.contains(theSameObject) -> " + naiveList.contains(nan)
                + "   <- cannot find an object it holds");

        CorrectDoubleHolder correctNan = new CorrectDoubleHolder(Double.NaN);
        System.out.println();
        System.out.println("    with Double.compare(a, b) == 0 instead:");
        System.out.println("    holder.equals(holder)     -> " + correctNan.equals(correctNan)
                + "    correct");
        System.out.println();
        System.out.println("    Double.compare also distinguishes 0.0 from -0.0:");
        System.out.println("      0.0 == -0.0                 -> " + (0.0 == -0.0));
        System.out.println("      Double.compare(0.0, -0.0)   -> " + Double.compare(0.0, -0.0)
                + "   (they ARE distinguishable)");

        // Symmetry broken by an asymmetric type check.
        System.out.println();
        System.out.println("  BREAKING SYMMETRY with a subclass that adds a field:");

        InstanceofPoint plainPoint = new InstanceofPoint(1, 2);
        ColourPoint colouredPoint = new ColourPoint(1, 2, "red");

        System.out.println("    plain.equals(coloured) -> " + plainPoint.equals(colouredPoint)
                + "    (Point sees only x and y)");
        System.out.println("    coloured.equals(plain) -> " + colouredPoint.equals(plainPoint)
                + "   (ColourPoint also wants colour)");
        System.out.println("    SYMMETRY BROKEN - and collections notice:");

        List<InstanceofPoint> symmetryList = new ArrayList<>(List.of(colouredPoint));
        System.out.println("      list.of(coloured).contains(plain) -> "
                + symmetryList.contains(plainPoint));
        List<InstanceofPoint> reversedList = new ArrayList<>(List.of(plainPoint));
        System.out.println("      list.of(plain).contains(coloured) -> "
                + reversedList.contains(colouredPoint));
        System.out.println("      The SAME PAIR gives different answers depending on which");
        System.out.println("      one is in the list. That is what asymmetry costs you.");


        /* ====================================================================
         * SECTION 4 - THE CORRECT TEMPLATE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE TEMPLATE, AND HOW TO COMPARE EACH FIELD TYPE");
        System.out.println("=".repeat(74));

        System.out.println("      @Override");
        System.out.println("      public boolean equals(Object other) {");
        System.out.println("          if (this == other) return true;                    // 1");
        System.out.println("          if (!(other instanceof Point point)) return false; // 2");
        System.out.println("          return x == point.x && y == point.y;               // 3");
        System.out.println("      }");
        System.out.println();
        System.out.println("    1. IDENTITY - a cheap early exit, and correct for self-comparison");
        System.out.println("    2. instanceof PATTERN - handles null (false), checks the type,");
        System.out.println("       and binds the cast variable, all in one line");
        System.out.println("    3. compare only the fields that define LOGICAL IDENTITY");

        System.out.println();
        System.out.println("  HOW TO COMPARE EACH FIELD TYPE:");
        System.out.printf("    %-38s %s%n", "primitives (except float/double)", "==");
        System.out.printf("    %-38s %s%n", "float", "Float.compare(a, b) == 0");
        System.out.printf("    %-38s %s%n", "double", "Double.compare(a, b) == 0");
        System.out.printf("    %-38s %s%n", "objects", "Objects.equals(a, b)  (null-safe)");
        System.out.printf("    %-38s %s%n", "arrays", "Arrays.equals / deepEquals");

        System.out.println();
        System.out.println("  A class using all of them correctly:");
        Measurement first = new Measurement("temp", 21.5, new int[]{1, 2, 3});
        Measurement second = new Measurement("temp", 21.5, new int[]{1, 2, 3});
        Measurement different = new Measurement("temp", 21.5, new int[]{9, 9, 9});

        System.out.println("    identical contents      -> " + first.equals(second));
        System.out.println("    differing array         -> " + first.equals(different));
        System.out.println("    equal hash codes?       -> " + (first.hashCode() == second.hashCode()));
        System.out.println("    null-safe               -> " + first.equals(null));
        System.out.println("    wrong type              -> " + first.equals("a string"));

        System.out.println();
        System.out.println("  ARRAYS ARE THE TRAP: Objects.equals on an array compares");
        System.out.println("  IDENTITY, because arrays do not override equals (lesson 12):");
        int[] leftArray = {1, 2, 3};
        int[] rightArray = {1, 2, 3};
        System.out.println("    Objects.equals(a, b) -> " + Objects.equals(leftArray, rightArray)
                + "   WRONG for arrays");
        System.out.println("    Arrays.equals(a, b)  -> " + Arrays.equals(leftArray, rightArray)
                + "    correct");
        System.out.println("    ...and hashCode has exactly the same trap:");
        System.out.println("    Objects.hash(a)      -> identity-based, differs each run");
        System.out.println("    Arrays.hashCode(a)   -> " + Arrays.hashCode(leftArray)
                + "   content-based");


        /* ====================================================================
         * SECTION 5 - WRITING hashCode
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - hashCode");
        System.out.println("=".repeat(74));

        System.out.println("  THE DEFAULT YOU SHOULD USE:");
        System.out.println("      return Objects.hash(x, y);   // same fields as equals");
        System.out.println();
        System.out.println("  THE MANUAL FORM, if profiling says the allocation matters:");
        System.out.println("      int result = Integer.hashCode(x);");
        System.out.println("      result = 31 * result + Integer.hashCode(y);");
        System.out.println("      return result;");
        System.out.println();
        System.out.println("    Objects.hash allocates a VARARGS ARRAY on every call (lesson");
        System.out.println("    19), which is why the JDK's own implementations are manual.");
        System.out.println("    For application code, use Objects.hash unless you measured.");

        System.out.println();
        System.out.println("  Both give a well-distributed result:");
        GoodPoint viaObjectsHash = new GoodPoint(3, 7);
        ManualHashPoint viaManual = new ManualHashPoint(3, 7);
        System.out.println("    Objects.hash(3, 7)     -> " + viaObjectsHash.hashCode());
        System.out.println("    manual 31 * r + field  -> " + viaManual.hashCode());

        System.out.println();
        System.out.println("  WHY 31? It is odd and prime, 31 * i optimises to (i << 5) - i,");
        System.out.println("  and it distributes typical field values well. Nothing magical -");
        System.out.println("  but matching the convention is worth more than any alternative.");

        /* --------------------------------------------------------------------
         * A CONSTANT hashCode is legal and catastrophic.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  A CONSTANT hashCode is LEGAL and catastrophic:");

        int entries = 30_000;

        Set<GoodPoint> distributed = new HashSet<>();
        long startGood = System.nanoTime();
        for (int i = 0; i < entries; i++) {
            distributed.add(new GoodPoint(i, i));
        }
        long goodMillis = (System.nanoTime() - startGood) / 1_000_000;

        Set<ConstantHashPoint> collided = new HashSet<>();
        long startBad = System.nanoTime();
        for (int i = 0; i < entries; i++) {
            collided.add(new ConstantHashPoint(i, i));
        }
        long badMillis = (System.nanoTime() - startBad) / 1_000_000;

        System.out.printf("    inserting %,d objects:%n", entries);
        System.out.println("      well-distributed hashCode -> " + goodMillis + " ms");
        System.out.println("      `return 1;`               -> " + badMillis + " ms");
        System.out.println("      both sets are correct: " + distributed.size()
                + " and " + collided.size() + " elements");
        System.out.println();
        System.out.println("    Every object landed in ONE bucket, turning the HashSet into");
        System.out.println("    a linear scan. O(1) became O(n), and insertion became O(n^2).");
        System.out.println("    Correct, and unusable.");


        /* ====================================================================
         * SECTION 6 - instanceof VS getClass
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - THE SUBCLASS PROBLEM HAS NO CLEAN SOLUTION");
        System.out.println("=".repeat(74));

        System.out.println("  Section 3 showed instanceof breaking SYMMETRY when a subclass");
        System.out.println("  adds a value component. getClass() fixes symmetry:");

        GetClassPoint strictPlain = new GetClassPoint(1, 2);
        StrictColourPoint strictColoured = new StrictColourPoint(1, 2, "red");

        System.out.println("    plain.equals(coloured) -> " + strictPlain.equals(strictColoured));
        System.out.println("    coloured.equals(plain) -> " + strictColoured.equals(strictPlain));
        System.out.println("    SYMMETRIC - both false, consistently.");
        System.out.println();
        System.out.println("  ...but now a subclass can NEVER be equal to its superclass,");
        System.out.println("  even when it adds nothing:");

        GetClassPoint asPlain = new GetClassPoint(1, 2);
        TaggedPoint addsNothing = new TaggedPoint(1, 2);
        System.out.println("    plain.equals(subclassThatAddsNoFields) -> "
                + asPlain.equals(addsNothing) + "   arguably wrong");

        System.out.println();
        System.out.println("  THE FUNDAMENTAL RESULT (Effective Java, stated plainly):");
        System.out.println("    THERE IS NO WAY to add a value component to a subclass while");
        System.out.println("    preserving the equals contract. Not a bug - a consequence of");
        System.out.println("    what equivalence relations are.");

        System.out.println();
        System.out.println("  THE PRACTICAL RESOLUTIONS, best first:");
        System.out.println("    1. COMPOSITION - ColourPoint HAS A Point. Recommended.");
        ComposedColourPoint composed = new ComposedColourPoint(new GoodPoint(1, 2), "red");
        ComposedColourPoint composedTwin = new ComposedColourPoint(new GoodPoint(1, 2), "red");
        System.out.println("       composed.equals(twin) -> " + composed.equals(composedTwin)
                + "   symmetric, transitive, correct");
        System.out.println("    2. Use getClass() and accept subclasses are never equal.");
        System.out.println("    3. Make the class FINAL so the problem cannot arise. This is");
        System.out.println("       why so many JDK value classes are final.");
        System.out.println("    4. Use a RECORD - implicitly final, correct equals generated.");
        System.out.println();
        System.out.println("  For a FINAL class the two versions are equivalent, so use");
        System.out.println("  instanceof with pattern matching: shorter, and null-safe free.");


        /* ====================================================================
         * SECTION 7 - THE MUTABLE-KEY DISASTER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - MUTATING A KEY MAKES IT UNREACHABLE");
        System.out.println("=".repeat(74));

        Set<MutablePoint> set = new HashSet<>();
        MutablePoint key = new MutablePoint(1, 2);
        set.add(key);

        System.out.println("    set.add(point(1, 2));");
        System.out.println("    set.contains(point) -> " + set.contains(key) + "   fine so far");
        System.out.println("    set                 -> " + set);

        key.setX(99);      // the hash changes; the object stays in the old bucket

        System.out.println();
        System.out.println("    point.setX(99);");
        System.out.println("    set.contains(point) -> " + set.contains(key)
                + "   <- FALSE, for an object that IS in the set");
        System.out.println("    set.remove(point)   -> " + set.remove(key)
                + "   <- cannot remove it either");
        System.out.println("    set.size()          -> " + set.size() + "   still there");
        System.out.println("    set                 -> " + set + "   visible when iterating!");

        System.out.println();
        System.out.println("  The object was FILED under its old hash. Changing the hash left");
        System.out.println("  it in the wrong bucket: UNREACHABLE and UNREMOVABLE. You can");
        System.out.println("  see it by iterating, and you can never get rid of it. That is a");
        System.out.println("  genuine memory leak with no way to clean it up.");
        System.out.println();
        System.out.println("  THE RULE: never use a mutable object as a HashMap key or a");
        System.out.println("  HashSet element, unless you are certain the fields used by");
        System.out.println("  hashCode will never change.");
        System.out.println();
        System.out.println("  The clean answer is to make key types IMMUTABLE (lesson 38) -");
        System.out.println("  or a record, which is shallowly immutable by construction.");


        /* ====================================================================
         * SECTION 8 - CONSISTENCY WITH compareTo
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - equals AND compareTo SHOULD AGREE");
        System.out.println("=".repeat(74));

        BigDecimal one = new BigDecimal("1.0");
        BigDecimal oneTwoDecimals = new BigDecimal("1.00");

        System.out.println("  BigDecimal is the classic inconsistency:");
        System.out.println("    new BigDecimal(\"1.0\").equals(new BigDecimal(\"1.00\"))  -> "
                + one.equals(oneTwoDecimals) + "   (different SCALE)");
        System.out.println("    ...compareTo(...) == 0                               -> "
                + (one.compareTo(oneTwoDecimals) == 0) + "    (numerically equal)");

        Set<BigDecimal> hashSet = new HashSet<>(List.of(one, oneTwoDecimals));
        Set<BigDecimal> treeSet = new TreeSet<>(List.of(one, oneTwoDecimals));

        System.out.println();
        System.out.println("    the SAME two values in two Sets:");
        System.out.println("      HashSet size " + hashSet.size() + "  -> " + hashSet
                + "   (uses equals)");
        System.out.println("      TreeSet size " + treeSet.size() + "  -> " + treeSet
                + "        (uses compareTo)");
        System.out.println();
        System.out.println("  Not required by the compiler, but SORTED COLLECTIONS ASSUME IT.");
        System.out.println("  BigDecimal documents this in its Javadoc precisely because it");
        System.out.println("  surprises people. Lesson 49 covers comparators.");


        /* ====================================================================
         * SECTION 9 - THE SHORTCUT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - JUST USE A RECORD");
        System.out.println("=".repeat(74));

        System.out.println("      record Point(int x, int y) { }");
        System.out.println();

        RecordPoint recordA = new RecordPoint(1, 2);
        RecordPoint recordB = new RecordPoint(1, 2);
        Map<RecordPoint, String> recordMap = new HashMap<>();
        recordMap.put(recordA, "works");

        System.out.println("    toString()               -> " + recordA);
        System.out.println("    equals                   -> " + recordA.equals(recordB));
        System.out.println("    consistent hashCode      -> " + (recordA.hashCode() == recordB.hashCode()));
        System.out.println("    as a HashMap key         -> " + recordMap.get(recordB));
        System.out.println("    null-safe                -> " + recordA.equals(null));
        System.out.println("    reflexive                -> " + recordA.equals(recordA));

        System.out.println();
        System.out.println("  One line generated all of it, correctly. A record is:");
        System.out.println("    - implicitly FINAL, so the symmetry problem cannot arise");
        System.out.println("    - built from final components, so the mutable-key disaster");
        System.out.println("      cannot happen either");
        System.out.println();
        System.out.println("  FOR VALUE TYPES, PREFER A RECORD. Write equals/hashCode by hand");
        System.out.println("  only when you need something a record cannot express: a SUBSET");
        System.out.println("  of fields, case-insensitive comparison, or a class that must");
        System.out.println("  extend something. Lesson 36.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 33.");
        System.out.println("=".repeat(74));
    }
}

// ----------------------------------------------------------------------------
// SECTION 1
// ----------------------------------------------------------------------------

/** Overrides nothing, so equals is identity. */
class NoOverrides {

    private final int x;
    private final int y;

    /** @param x the x  @param y the y */
    NoOverrides(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

// ----------------------------------------------------------------------------
// SECTION 2
// ----------------------------------------------------------------------------

/** equals is correct; hashCode is NOT overridden. This is the classic bug. */
class BrokenPoint {

    private final int x;
    private final int y;

    /** @param x the x  @param y the y */
    BrokenPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BrokenPoint point)) return false;
        return x == point.x && y == point.y;
    }

    // NO hashCode(). Two equal objects therefore have different hash codes,
    // land in different buckets, and are never compared to each other.

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

/** The same class, done correctly. */
class GoodPoint {

    private final int x;
    private final int y;

    /** @param x the x  @param y the y */
    GoodPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GoodPoint point)) return false;
        return x == point.x && y == point.y;
    }

    /** Uses EXACTLY the same fields as equals. That is the whole requirement. */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

/** The manual hashCode form, avoiding the varargs allocation. */
class ManualHashPoint {

    private final int x;
    private final int y;

    /** @param x the x  @param y the y */
    ManualHashPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ManualHashPoint point)) return false;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        return result;
    }
}

/** A legal hashCode that destroys HashMap and HashSet performance. */
class ConstantHashPoint {

    private final int x;
    private final int y;

    /** @param x the x  @param y the y */
    ConstantHashPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ConstantHashPoint point)) return false;
        return x == point.x && y == point.y;
    }

    /**
     * PERFECTLY LEGAL - it satisfies the contract, because equal objects do
     * have equal hash codes. It also puts every object in one bucket.
     */
    @Override
    public int hashCode() {
        return 1;
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - BREAKING THE CONTRACT
// ----------------------------------------------------------------------------

/** Uses == on a double field, which breaks reflexivity for NaN. */
class NaiveDoubleHolder {

    private final double value;

    /** @param value the measurement */
    NaiveDoubleHolder(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NaiveDoubleHolder holder)) return false;
        // THE BUG: NaN == NaN is false, so this object is not equal to itself.
        return value == holder.value;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}

/** Uses Double.compare, which handles NaN and signed zero correctly. */
class CorrectDoubleHolder {

    private final double value;

    /** @param value the measurement */
    CorrectDoubleHolder(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof CorrectDoubleHolder holder)) return false;
        return Double.compare(value, holder.value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }
}

/** Uses instanceof, which lets a subclass break symmetry. */
class InstanceofPoint {

    protected final int x;
    protected final int y;

    /** @param x the x  @param y the y */
    InstanceofPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InstanceofPoint point)) return false;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

/** Adds a value component, which is what breaks symmetry. */
class ColourPoint extends InstanceofPoint {

    private final String colour;

    /** @param x the x  @param y the y  @param colour the colour */
    ColourPoint(int x, int y, String colour) {
        super(x, y);
        this.colour = colour;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ColourPoint point)) return false;
        return super.equals(other) && colour.equals(point.colour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, colour);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + colour + ")";
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - EVERY FIELD TYPE, DONE RIGHT
// ----------------------------------------------------------------------------

/** Demonstrates the correct comparison for each kind of field. */
class Measurement {

    private final String name;         // an object -> Objects.equals
    private final double reading;      // a double  -> Double.compare
    private final int[] samples;       // an array  -> Arrays.equals

    /**
     * @param name    the measurement name
     * @param reading the value
     * @param samples the raw samples
     */
    Measurement(String name, double reading, int[] samples) {
        this.name = name;
        this.reading = reading;
        this.samples = samples;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Measurement measurement)) return false;
        return Objects.equals(name, measurement.name)              // null-safe
                && Double.compare(reading, measurement.reading) == 0 // NaN-safe
                && Arrays.equals(samples, measurement.samples);      // content-based
    }

    @Override
    public int hashCode() {
        // Note Arrays.hashCode for the array - Objects.hash would use the
        // array's IDENTITY hash and break the contract.
        return 31 * Objects.hash(name, reading) + Arrays.hashCode(samples);
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - getClass, AND COMPOSITION
// ----------------------------------------------------------------------------

/** Uses getClass(), which preserves symmetry at the cost of subclass equality. */
class GetClassPoint {

    protected final int x;
    protected final int y;

    /** @param x the x  @param y the y */
    GetClassPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        GetClassPoint point = (GetClassPoint) other;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

/** Adds a value component. getClass() keeps this symmetric with its parent. */
class StrictColourPoint extends GetClassPoint {

    private final String colour;

    /** @param x the x  @param y the y  @param colour the colour */
    StrictColourPoint(int x, int y, String colour) {
        super(x, y);
        this.colour = colour;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        StrictColourPoint point = (StrictColourPoint) other;
        return x == point.x && y == point.y && colour.equals(point.colour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, colour);
    }
}

/** Adds NO fields, yet getClass() still makes it unequal to its parent. */
class TaggedPoint extends GetClassPoint {

    /** @param x the x  @param y the y */
    TaggedPoint(int x, int y) {
        super(x, y);
    }
}

/** THE RECOMMENDED FIX: composition rather than inheritance. */
class ComposedColourPoint {

    private final GoodPoint point;      // HAS A point, rather than IS A point
    private final String colour;

    /** @param point the position  @param colour the colour */
    ComposedColourPoint(GoodPoint point, String colour) {
        this.point = point;
        this.colour = colour;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ComposedColourPoint composed)) return false;
        return point.equals(composed.point) && colour.equals(composed.colour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, colour);
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - THE MUTABLE KEY
// ----------------------------------------------------------------------------

/** A mutable object whose hashCode changes - a disaster as a hash key. */
class MutablePoint {

    private int x;
    private int y;

    /** @param x the x  @param y the y */
    MutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** @param x the new x - which silently changes this object's hashCode */
    void setX(int x) {
        this.x = x;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MutablePoint point)) return false;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

// ----------------------------------------------------------------------------
// SECTION 9 - THE SHORTCUT
// ----------------------------------------------------------------------------

/** One line generates a correct equals, hashCode and toString.
 *  @param x the x  @param y the y */
record RecordPoint(int x, int y) {}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add hashCode() to BrokenPoint and re-run Section 2. Every failure should
 *    turn into a success without touching equals at all.
 *
 * 2. Write a Person class with name and age. Put 1,000 into a HashSet. Now
 *    delete hashCode and measure how long the same insertion takes. Explain
 *    the difference using the word "bucket".
 *
 * 3. Give Measurement a `float` field and compare it with ==. Then find an
 *    input for which the object is not equal to itself.
 *
 * 4. Fix the symmetry violation in ColourPoint using composition, the way
 *    ComposedColourPoint does. Verify symmetry in both directions.
 *
 * 5. Put a MutablePoint in a HashMap as a key, mutate it, then try to iterate
 *    the map and remove the entry. You cannot. Now do the same with a record
 *    and explain why the problem disappeared.
 *
 * 6. Predict the size of each, then run it:
 *        new HashSet<>(List.of(new BigDecimal("1.0"), new BigDecimal("1.00")))
 *        new TreeSet<>(List.of(new BigDecimal("1.0"), new BigDecimal("1.00")))
 *    Then decide which one is "right", and why the question has no clean answer.
 * ============================================================================
 */
