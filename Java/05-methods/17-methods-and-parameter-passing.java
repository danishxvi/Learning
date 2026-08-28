/* ============================================================================
 * 17 - METHODS AND HOW ARGUMENTS ARE REALLY PASSED
 * ----------------------------------------------------------------------------
 * Companion lesson: 17-methods-and-parameter-passing.md
 *
 * RUN IT:
 *     java Java/05-methods/17-methods-and-parameter-passing.java
 *
 * THE ONE SENTENCE TO TAKE AWAY:
 *   Java passes everything BY VALUE. For a reference type, the value being
 *   copied is the REFERENCE - not the object.
 * Sections 2 and 3 prove it in both directions.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MethodsAndParameterPassing {

    /**
     * A record used to return two values at once. Java has no tuples, and since
     * Java 16 a record makes "return several things" almost free. Lesson 36.
     *
     * @param min the smallest value found
     * @param max the largest value found
     */
    record MinMax(int min, int max) {}

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - ANATOMY AND SIGNATURES
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - ANATOMY OF A METHOD");
        System.out.println("=".repeat(74));

        System.out.println("  static int add(int a, int b) { return a + b; }");
        System.out.println("    add(2, 3) = " + add(2, 3));
        System.out.println();
        System.out.println("  A method's SIGNATURE is its NAME plus PARAMETER TYPES only:");
        System.out.println("    add(int, int)");
        System.out.println();
        System.out.println("  The RETURN TYPE is NOT part of the signature. These two");
        System.out.println("  cannot coexist - it is a compile error, not an overload:");
        System.out.println("    int    process(String s)");
        System.out.println("    String process(String s)");
        System.out.println("  That rule is why lesson 18 on overloading works the way it does.");

        System.out.println();
        System.out.println("  PARAMETER vs ARGUMENT:");
        System.out.println("    static int square(int number)   <- `number` is a PARAMETER");
        System.out.println("    square(5)                       <- `5` is an ARGUMENT");
        System.out.println("    square(5) = " + square(5));
        System.out.println("  The argument's VALUE is copied into the parameter. Hold on to");
        System.out.println("  that sentence for the next two sections.");


        /* ====================================================================
         * SECTION 2 - PRIMITIVES ARE OBVIOUSLY COPIED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - PASSING A PRIMITIVE");
        System.out.println("=".repeat(74));

        int number = 5;
        System.out.println("  before: number = " + number);
        tryToChangePrimitive(number);
        System.out.println("  after : number = " + number + "   <- UNCHANGED");
        System.out.println();
        System.out.println("  The parameter was a separate variable holding a COPY of 5.");
        System.out.println("  Reassigning it could never touch the caller's variable.");
        System.out.println("  Nobody finds this surprising. Section 3 is where it gets hard.");


        /* ====================================================================
         * SECTION 3 - OBJECTS: THE CASE EVERYONE GETS WRONG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - PASSING AN OBJECT REFERENCE");
        System.out.println("=".repeat(74));

        // CASE A: MUTATING the object. Both variables see the change, which is
        // what makes people say "objects are passed by reference".
        StringBuilder text = new StringBuilder("hello");
        System.out.println("  CASE A - the method MUTATES the object:");
        System.out.println("    before: text = \"" + text + "\"");
        mutateTheObject(text);
        System.out.println("    after : text = \"" + text + "\"   <- IT CHANGED");
        System.out.println();
        System.out.println("    This is the observation that makes people say Java passes");
        System.out.println("    objects by reference. It does not. Here is the proof:");

        // CASE B: REASSIGNING the parameter. If Java were pass-by-reference,
        // the caller's variable would now point at the new object. It does not.
        StringBuilder original = new StringBuilder("hello");
        System.out.println();
        System.out.println("  CASE B - the method REASSIGNS the parameter:");
        System.out.println("    before: text = \"" + original + "\"");
        reassignTheParameter(original);
        System.out.println("    after : text = \"" + original + "\"   <- UNCHANGED");
        System.out.println();
        System.out.println("    In a pass-by-REFERENCE language (C++ int&, C# ref) the");
        System.out.println("    caller's variable WOULD now point at the new object.");
        System.out.println("    In Java it does not. Therefore: pass by value.");

        System.out.println();
        System.out.println("  WHAT ACTUALLY HAPPENS:");
        System.out.println("    1. `text` holds a REFERENCE (an address) to a heap object");
        System.out.println("    2. that REFERENCE is COPIED into the parameter");
        System.out.println("    3. two references now point at ONE object");
        System.out.println("    4. mutating the object -> both see it");
        System.out.println("       repointing a reference -> only the local copy moves");

        System.out.println();
        System.out.println("  THE ONE-LINE RULE:");
        System.out.println("    A method can change what an object CONTAINS,");
        System.out.println("    but never what the caller's variable POINTS AT.");

        // The same demonstration with a collection, because this is where it
        // bites in real code.
        System.out.println();
        System.out.println("  The same thing with a List, where this bites in real code:");
        List<String> names = new ArrayList<>(List.of("Danish"));
        System.out.println("    before      : " + names);
        addToList(names);
        System.out.println("    after add   : " + names + "   <- mutated, visible to the caller");
        replaceList(names);
        System.out.println("    after replace: " + names + "   <- unchanged, the local copy moved");

        // Arrays behave identically, because an array IS an object.
        System.out.println();
        System.out.println("  And with an array, which is also an object:");
        int[] values = {1, 2, 3};
        System.out.println("    before        : " + Arrays.toString(values));
        mutateArray(values);
        System.out.println("    after mutate  : " + Arrays.toString(values) + "   <- changed");
        reassignArray(values);
        System.out.println("    after reassign: " + Arrays.toString(values) + "   <- unchanged");

        /* --------------------------------------------------------------------
         * WHY String SEEMS DIFFERENT. It is immutable, so there is no mutating
         * method to call - every operation reassigns the local copy. The
         * PASSING MECHANISM is identical; only the object's nature differs.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  WHY String SEEMS TO BEHAVE DIFFERENTLY:");
        String immutable = "hello";
        System.out.println("    before: \"" + immutable + "\"");
        tryToChangeString(immutable);
        System.out.println("    after : \"" + immutable + "\"   <- unchanged");
        System.out.println();
        System.out.println("    String is IMMUTABLE, so there is no mutating method to");
        System.out.println("    call - every operation reassigns the local copy, which is");
        System.out.println("    Case B. The passing mechanism is identical to StringBuilder;");
        System.out.println("    only the object's nature differs.");

        System.out.println();
        System.out.println("  THE SWAP TEST - the classic proof:");
        int firstNumber = 1, secondNumber = 2;
        System.out.println("    before swap attempt: " + firstNumber + ", " + secondNumber);
        trySwap(firstNumber, secondNumber);
        System.out.println("    after  swap attempt: " + firstNumber + ", " + secondNumber
                + "   <- unchanged");
        System.out.println("    You CANNOT write a working swap(a, b) in Java. In C++ you");
        System.out.println("    can, with references. That single fact settles the argument.");


        /* ====================================================================
         * SECTION 4 - RETURNING VALUES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - RETURNING");
        System.out.println("=".repeat(74));

        System.out.println("  A non-void method must return on EVERY path:");
        System.out.println("    classify(5)  = " + classify(5));
        System.out.println("    classify(0)  = " + classify(0));
        System.out.println("    classify(-5) = " + classify(-5));
        System.out.println();
        System.out.println("  Omitting a path is a compile error: 'missing return statement'.");
        System.out.println("  Code after an unconditional return is 'unreachable statement'.");

        System.out.println();
        System.out.println("  RETURNING SEVERAL VALUES - Java has no tuples.");

        int[] data = {5, 2, 9, 1, 7};
        System.out.println("    data = " + Arrays.toString(data));

        MinMax range = findRange(data);
        System.out.println("    with a RECORD  -> " + range);
        System.out.println("      range.min() = " + range.min() + ", range.max() = " + range.max());
        System.out.println("      self-documenting, immutable, and you cannot mix up the order");

        int[] asArray = findRangeAsArray(data);
        System.out.println("    with an ARRAY  -> " + Arrays.toString(asArray));
        System.out.println("      which one is the min? You have to read the docs or the code.");
        System.out.println();
        System.out.println("  Records made this so cheap that 'I need to return two things'");
        System.out.println("  is no longer a reason to reach for an array. Lesson 36.");


        /* ====================================================================
         * SECTION 5 - METHOD DESIGN
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - DESIGNING A GOOD METHOD");
        System.out.println("=".repeat(74));

        System.out.println("  DO ONE THING.");
        System.out.println("    If you need the word 'and' to describe a method, split it.");
        System.out.println("    validateAndSaveAndEmail() is three methods wearing a coat.");

        System.out.println();
        System.out.println("  KEEP THE PARAMETER LIST SHORT.");
        System.out.println("    0-2 parameters : ideal");
        System.out.println("    3              : acceptable");
        System.out.println("    4+             : a parameter object is probably missing");
        System.out.println();
        System.out.println("    Hard to call correctly - which boolean is which?");
        System.out.println("      createUser(\"Danish\", \"d@x.com\", 25, true, false, true)");
        System.out.println("    Self-documenting:");
        System.out.println("      createUser(new UserDetails(\"Danish\", \"d@x.com\", 25))");
        System.out.println();
        System.out.println("    Long lists of the SAME type are the dangerous ones: swapping");
        System.out.println("    two arguments compiles fine and fails silently at runtime.");

        System.out.println();
        System.out.println("  AVOID BOOLEAN PARAMETERS.");
        System.out.println("    render(true)      <- true WHAT?");
        System.out.println("    renderExpanded()  <- clear");
        System.out.println("    A boolean parameter almost always means two methods in one.");

        System.out.println();
        System.out.println("  NEVER RETURN null FOR A COLLECTION.");
        System.out.println("    findItems() returning null    -> " + describeResult(findItemsBadly()));
        System.out.println("    findItems() returning List.of() -> " + describeResult(findItemsWell()));
        System.out.println();
        System.out.println("    An empty collection lets every caller loop unconditionally.");
        System.out.println("    Returning null forces a null check into every call site, and");
        System.out.println("    the one that forgets is a production NullPointerException.");
        System.out.println("    For a single maybe-absent value, return Optional (lesson 56).");


        /* ====================================================================
         * SECTION 6 - static VS INSTANCE (A PREVIEW)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - static VS INSTANCE METHODS");
        System.out.println("=".repeat(74));

        System.out.println("  A STATIC method belongs to the CLASS:");
        System.out.println("    MethodsAndParameterPassing.add(2, 3) = " + add(2, 3));
        System.out.println("    It cannot touch instance fields or `this`, because it can be");
        System.out.println("    called before any object exists. That is why main is static.");

        System.out.println();
        System.out.println("  An INSTANCE method belongs to an OBJECT:");
        Counter counter = new Counter();
        counter.increment();
        counter.increment();
        counter.increment();
        System.out.println("    counter.increment() x3, counter.getCount() = " + counter.getCount());
        System.out.println("    It reads and writes per-object state, so it needs an object.");

        System.out.println();
        System.out.println("  RULE OF THUMB: if a method uses no instance state, it CAN be");
        System.out.println("  static - and making it static documents that fact. Lesson 25.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 17.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 1 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Adds two numbers. The canonical example of a pure method: same inputs
     * always give the same output, and nothing outside it changes.
     *
     * @param a the first number
     * @param b the second number
     * @return their sum
     */
    static int add(int a, int b) {
        return a + b;
    }

    /**
     * Squares a number.
     *
     * @param number the value to square
     * @return number * number
     */
    static int square(int number) {
        return number * number;
    }

    // ------------------------------------------------------------------------
    // SECTION 2 AND 3 SUPPORT - THE PASS-BY-VALUE DEMONSTRATIONS
    // ------------------------------------------------------------------------

    /**
     * Attempts to change a primitive. The parameter is a separate variable
     * holding a copy, so the caller sees nothing.
     *
     * @param value a copy of the caller's number
     */
    static void tryToChangePrimitive(int value) {
        value = 99;
    }

    /**
     * CASE A: mutates the object the reference points at. The caller sees this,
     * because both references point at the same object.
     *
     * @param builder a copy of the caller's reference
     */
    static void mutateTheObject(StringBuilder builder) {
        builder.append(" world");
    }

    /**
     * CASE B: repoints the local parameter at a brand-new object. The caller's
     * variable is untouched - which is the proof that Java is pass-by-value.
     *
     * @param builder a copy of the caller's reference
     */
    static void reassignTheParameter(StringBuilder builder) {
        builder = new StringBuilder("something completely different");
        builder.append("!");   // mutating the NEW object, which nobody else can see
    }

    /**
     * Mutates the caller's list. Visible to the caller.
     *
     * @param list a copy of the caller's reference
     */
    static void addToList(List<String> list) {
        list.add("Aisha");
    }

    /**
     * Repoints the local reference at a new list. Invisible to the caller.
     *
     * @param list a copy of the caller's reference
     */
    static void replaceList(List<String> list) {
        list = new ArrayList<>(List.of("completely", "different"));
        list.add("ignored");
    }

    /**
     * Mutates the caller's array. Visible, because an array is an object.
     *
     * @param array a copy of the caller's reference
     */
    static void mutateArray(int[] array) {
        array[0] = 99;
    }

    /**
     * Repoints the local reference at a new array. Invisible to the caller.
     *
     * @param array a copy of the caller's reference
     */
    static void reassignArray(int[] array) {
        array = new int[]{7, 7, 7};
        array[1] = 8;
    }

    /**
     * Attempts to change a String. Because String is immutable there is no
     * mutating method to call, so this is always Case B.
     *
     * @param text a copy of the caller's reference
     */
    static void tryToChangeString(String text) {
        text = text.toUpperCase() + " CHANGED";
    }

    /**
     * Attempts to swap two numbers. This is impossible in Java, and that
     * impossibility is the clearest proof of pass-by-value there is.
     *
     * @param first  a copy of the caller's first number
     * @param second a copy of the caller's second number
     */
    static void trySwap(int first, int second) {
        int temporary = first;
        first = second;
        second = temporary;
        // Both locals swapped. Neither caller variable moved.
    }

    // ------------------------------------------------------------------------
    // SECTION 4 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Returns on every path, which a non-void method must do.
     *
     * @param number the value to classify
     * @return "positive", "zero" or "negative"
     */
    static String classify(int number) {
        if (number > 0) return "positive";
        if (number < 0) return "negative";
        return "zero";
        // System.out.println("x");   // 'unreachable statement' - a compile error
    }

    /**
     * Returns two values as a record: self-documenting, immutable, and
     * impossible to read in the wrong order.
     *
     * @param values the numbers to scan; must not be empty
     * @return the smallest and largest values
     */
    static MinMax findRange(int[] values) {
        int min = values[0];
        int max = values[0];
        for (int value : values) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        return new MinMax(min, max);
    }

    /**
     * The same result as an array. Works, but the caller has to know which
     * index means what - a small unforced source of bugs.
     *
     * @param values the numbers to scan; must not be empty
     * @return a two-element array of {min, max}
     */
    static int[] findRangeAsArray(int[] values) {
        MinMax range = findRange(values);
        return new int[]{range.min(), range.max()};
    }

    // ------------------------------------------------------------------------
    // SECTION 5 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Returns null when there is nothing to report - the habit that forces a
     * null check into every call site.
     *
     * @return always null, deliberately
     */
    static List<String> findItemsBadly() {
        return null;
    }

    /**
     * Returns an empty list when there is nothing to report, so every caller
     * can loop unconditionally.
     *
     * @return an empty list
     */
    static List<String> findItemsWell() {
        return List.of();
    }

    /**
     * Reports how a caller fares with each style of "nothing found".
     *
     * @param items the returned collection, possibly null
     * @return a description of what the caller has to do
     */
    static String describeResult(List<String> items) {
        if (items == null) {
            return "null - every caller must null-check, and one of them will forget";
        }
        int count = 0;
        for (String ignored : items) {
            count++;
        }
        return "empty list - the caller just loops, " + count + " iterations, no check needed";
    }
}

/**
 * A tiny class with instance state, used to contrast instance methods with
 * static ones. Classes and objects are covered properly in lesson 21.
 */
class Counter {

    /** Per-object state. A static method could not touch this. */
    private int count = 0;

    /** Increments this object's counter. */
    void increment() {
        count++;
    }

    /**
     * Reads this object's counter.
     *
     * @return the current count
     */
    int getCount() {
        return count;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write a method that DOES swap two values, given the constraint that Java
 *    is pass-by-value. (Hint: you cannot swap two ints - so change what you
 *    pass. An int[] of length 2, or a mutable holder object, both work.)
 *
 * 2. Predict each, then run it:
 *        static void f(int[] a)      { a[0] = 99; }
 *        static void g(int[] a)      { a = new int[]{99}; }
 *        static void h(String s)     { s += "!"; }
 *        static void i(List<Integer> l) { l.clear(); }
 *    Which of the four are visible to the caller? Why?
 *
 * 3. Write `boolean isValidEmail(String email)` that does ONE thing. Then try
 *    to write `boolean validateAndNormalise(String email)` and notice why the
 *    name itself tells you it is wrong.
 *
 * 4. Refactor this to use a parameter object:
 *        void book(String from, String to, String date, boolean window,
 *                  boolean meal, boolean insurance)
 *    Then explain what bug the original invites.
 *
 * 5. Write `Optional<String> findLongest(List<String> words)` returning empty
 *    for an empty list. Compare with a version that returns null, and count
 *    the lines each forces on the caller.
 *
 * 6. Make classify() fail to compile by deleting its final `return zero`. Read
 *    the exact error. Then add unreachable code after a return and read that
 *    one too.
 * ============================================================================
 */
