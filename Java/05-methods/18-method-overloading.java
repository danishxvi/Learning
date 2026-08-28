/* ============================================================================
 * 18 - METHOD OVERLOADING AND RESOLUTION RULES
 * ----------------------------------------------------------------------------
 * Companion lesson: 18-method-overloading.md
 *
 * RUN IT:
 *     java Java/05-methods/18-method-overloading.java
 *
 * Overloading is resolved entirely at COMPILE TIME, by a three-phase algorithm
 * that is more intricate than most people realise. Section 3 makes the phases
 * visible; Section 5 shows the bug they cause.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

class MethodOverloading {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - WHAT MAKES A VALID OVERLOAD
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - VALID AND INVALID OVERLOADS");
        System.out.println("=".repeat(74));

        System.out.println("  Overloads may differ in NUMBER, TYPE or ORDER of parameters:");
        print(42);
        print("hello");
        print(42, 7);
        print(42, "hello");
        print("hello", 42);

        System.out.println();
        System.out.println("  These are all COMPILE ERRORS (see the source):");
        System.out.println("    int process(String s)  /  String process(String s)");
        System.out.println("      -> return type is NOT part of the signature");
        System.out.println("    void log(String message)  /  void log(String text)");
        System.out.println("      -> parameter NAMES are irrelevant");
        System.out.println("    void save(String s)  /  void save(final String s)");
        System.out.println("      -> `final` on a parameter is not part of it either");
        System.out.println();
        System.out.println("  The signature is NAME + PARAMETER TYPES. Nothing else.");
        System.out.println("  Not the return type, not names, not modifiers, not throws.");


        /* ====================================================================
         * SECTION 2 - THE THREE RESOLUTION PHASES
         * --------------------------------------------------------------------
         * The compiler tries, IN ORDER, and stops at the first phase that
         * finds a match:
         *   1. exact match or WIDENING   (no boxing, no varargs)
         *   2. allow BOXING / UNBOXING
         *   3. allow VARARGS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - WIDENING BEATS BOXING BEATS VARARGS");
        System.out.println("=".repeat(74));

        System.out.println("  Three overloads are available:");
        System.out.println("    show(long x)      <- reachable by WIDENING   (phase 1)");
        System.out.println("    show(Integer x)   <- reachable by BOXING     (phase 2)");
        System.out.println("    show(int... x)    <- reachable by VARARGS    (phase 3)");
        System.out.println();
        System.out.print("  show(5) calls -> ");
        show(5);
        System.out.println();
        System.out.println("  NOT show(Integer), even though 5 is an int and Integer looks");
        System.out.println("  like the closer match. Phase 1 succeeded, so phases 2 and 3");
        System.out.println("  were never considered.");

        System.out.println();
        System.out.println("  Removing the widening candidate moves it to phase 2:");
        System.out.print("    showNoLong(5) calls -> ");
        showNoLong(5);

        System.out.println("  Removing the boxing candidate too moves it to phase 3:");
        System.out.print("    showVarargsOnly(5) calls -> ");
        showVarargsOnly(5);

        System.out.println();
        System.out.println("  WHY THIS ORDER: boxing and varargs both arrived in Java 5.");
        System.out.println("  The rules had to guarantee that code written before Java 5");
        System.out.println("  kept choosing exactly the overloads it always had. Phase 1");
        System.out.println("  is 'what Java 1.4 would have done'.");

        /* --------------------------------------------------------------------
         * MOST SPECIFIC WINS WITHIN A PHASE.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  Within one phase, the MOST SPECIFIC candidate wins:");
        System.out.print("    describe(\"hello\")            -> ");
        describe("hello");
        System.out.print("    describe(new Object())       -> ");
        describe(new Object());
        System.out.print("    describe(Integer.valueOf(1)) -> ");
        describe(Integer.valueOf(1));
        System.out.println();
        System.out.println("  'More specific' means the parameter type can be passed to the");
        System.out.println("  other candidate without a cast. String fits into Object, so");
        System.out.println("  String is the more specific of the two.");

        // char widens to int in phase 1, before any boxing to Character.
        System.out.println();
        System.out.print("  A char argument with (int) and (Object) available -> ");
        widenOrBox('a');
        System.out.println("    char widens to int in phase 1, so Object never gets a look.");


        /* ====================================================================
         * SECTION 3 - AMBIGUITY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - WHEN THE COMPILER REFUSES TO CHOOSE");
        System.out.println("=".repeat(74));

        System.out.println("  Given:");
        System.out.println("    void ambiguous(int a, long b)");
        System.out.println("    void ambiguous(long a, int b)");
        System.out.println();
        System.out.println("  ambiguous(1, 2);   -> ERROR: reference to ambiguous is ambiguous");
        System.out.println("    the first needs the SECOND argument widened,");
        System.out.println("    the second needs the FIRST argument widened.");
        System.out.println("    Neither is more specific, so the compiler stops.");
        System.out.println();
        System.out.println("  Fix it with an explicit cast:");
        System.out.print("    ambiguous(1, (long) 2)  -> ");
        ambiguous(1, (long) 2);
        System.out.print("    ambiguous((long) 1, 2)  -> ");
        ambiguous((long) 1, 2);

        System.out.println();
        System.out.println("  null is famously ambiguous, because it fits EVERY reference type:");
        System.out.println("    nullable(null)  -> ERROR when String and Integer both match");
        System.out.print("    nullable((String) null)  -> ");
        nullable((String) null);
        System.out.print("    nullable((Integer) null) -> ");
        nullable((Integer) null);
        System.out.println();
        System.out.println("  When one candidate is a SUBTYPE of the other it resolves fine,");
        System.out.println("  because most-specific still applies:");
        System.out.print("    related(null) with (Object) and (String) -> ");
        related(null);


        /* ====================================================================
         * SECTION 4 - THE BIG ONE: DECLARED TYPE, NOT RUNTIME TYPE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - OVERLOADING USES THE *DECLARED* TYPE");
        System.out.println("=".repeat(74));

        String asString = "I am really a String";
        Object asObject = asString;          // the SAME object, a different declared type

        System.out.println("  One object, two variables with different DECLARED types:");
        System.out.print("    describe(asString)  [declared String] -> ");
        describe(asString);
        System.out.print("    describe(asObject)  [declared Object] -> ");
        describe(asObject);
        System.out.println();
        System.out.println("    asString == asObject -> " + (asString == asObject)
                + "   THE SAME OBJECT.");
        System.out.println("    asObject.getClass()  -> " + asObject.getClass().getSimpleName()
                + "   really is a String at runtime.");
        System.out.println();
        System.out.println("  Yet the Object overload ran. The compiler only sees the");
        System.out.println("  DECLARED type, and overload resolution finished at compile");
        System.out.println("  time, long before any object existed.");

        // Contrast with OVERRIDING, which uses the runtime type.
        System.out.println();
        System.out.println("  Contrast with OVERRIDING, which uses the RUNTIME type:");
        Animal declaredAsAnimal = new Dog();     // declared Animal, actually a Dog
        System.out.print("    Animal a = new Dog(); a.speak() -> ");
        declaredAsAnimal.speak();
        System.out.println("    The Dog version ran, despite the declared type Animal.");

        System.out.println();
        System.out.println("  THE DISTINCTION, IN ONE LINE:");
        System.out.println("    OVERLOADING looks at the reference's DECLARED type.");
        System.out.println("    OVERRIDING  looks at the object's ACTUAL type.");
        System.out.println();
        System.out.println("  This is why 'overloading is compile-time polymorphism' is not");
        System.out.println("  just terminology - it changes which code runs. Lesson 27");
        System.out.println("  covers overriding properly.");


        /* ====================================================================
         * SECTION 5 - THE List.remove GOTCHA
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE MOST-HIT OVERLOADING BUG IN JAVA");
        System.out.println("=".repeat(74));

        System.out.println("  List has BOTH of these:");
        System.out.println("    E       remove(int index)     <- remove by POSITION");
        System.out.println("    boolean remove(Object o)      <- remove by VALUE");
        System.out.println();

        List<Integer> byIndex = new ArrayList<>(List.of(10, 20, 30));
        List<Integer> byValue = new ArrayList<>(List.of(10, 20, 30));

        System.out.println("  Starting list: " + byIndex);
        System.out.println();

        byIndex.remove(1);
        System.out.println("    list.remove(1)                   -> " + byIndex);
        System.out.println("      an int literal matches remove(int) in PHASE 1,");
        System.out.println("      so this removed INDEX 1, which held the value 20.");

        byValue.remove(Integer.valueOf(10));
        System.out.println();
        System.out.println("    list.remove(Integer.valueOf(10)) -> " + byValue);
        System.out.println("      an Integer matches remove(Object), so this removed the");
        System.out.println("      VALUE 10, which happened to be at index 0.");

        System.out.println();
        System.out.println("  On a List<Integer> these read almost identically and do");
        System.out.println("  completely different things. It is a real production bug,");
        System.out.println("  and it is caused entirely by the phase-1-before-phase-2 rule.");

        // Autoboxing changing the chosen method.
        System.out.println();
        System.out.println("  The same effect in miniature:");
        System.out.print("    boxOrPrimitive(5)                  -> ");
        boxOrPrimitive(5);
        System.out.print("    boxOrPrimitive(Integer.valueOf(5)) -> ");
        boxOrPrimitive(Integer.valueOf(5));
        System.out.println("    Same value. Different method. Decided by how you TYPED it.");


        /* ====================================================================
         * SECTION 6 - GOOD AND BAD OVERLOADING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - WHEN TO OVERLOAD");
        System.out.println("=".repeat(74));

        System.out.println("  GOOD - every overload does conceptually the SAME thing:");
        System.out.println("    Math.max(int, int) / (long, long) / (double, double)");
        System.out.println("    System.out.println(int) / (String) / (Object) / (char[])");
        System.out.println("      -> Math.max(3, 7)     = " + Math.max(3, 7));
        System.out.println("      -> Math.max(3.5, 7.1) = " + Math.max(3.5, 7.1));
        System.out.println("    A caller never has to think about which one runs.");

        System.out.println();
        System.out.println("  BAD - overloads that BEHAVE differently:");
        System.out.println("    save(User u)                  // to the database");
        System.out.println("    save(User u, boolean toFile)  // to a file if true");
        System.out.println("    The reader now has to look up what the boolean means.");
        System.out.println();
        System.out.println("  THE TEST: if you could not swap one overload for another");
        System.out.println("  without a caller noticing a BEHAVIOURAL difference, they");
        System.out.println("  should not share a name.");

        System.out.println();
        System.out.println("  Prefer distinct names over null-disambiguation:");
        System.out.println("    find(name, null); find(null, id);   <- which is which?");
        System.out.println("    findByName(name); findById(id);     <- obvious");


        /* ====================================================================
         * SECTION 7 - CONSTRUCTOR OVERLOADING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - CONSTRUCTORS OVERLOAD THE SAME WAY");
        System.out.println("=".repeat(74));

        System.out.println("  new Rectangle()      -> " + new Rectangle());
        System.out.println("  new Rectangle(5)     -> " + new Rectangle(5));
        System.out.println("  new Rectangle(4, 6)  -> " + new Rectangle(4, 6));
        System.out.println();
        System.out.println("  All three delegate with this(...) to ONE primary constructor,");
        System.out.println("  so the validation lives in exactly one place. this(...) must");
        System.out.println("  be the FIRST statement in a constructor. Lesson 22 covers this.");

        try {
            new Rectangle(-1, 5);
        } catch (IllegalArgumentException e) {
            System.out.println();
            System.out.println("  new Rectangle(-1, 5) -> IllegalArgumentException: " + e.getMessage());
            System.out.println("  Validated once, in the primary constructor, and every other");
            System.out.println("  constructor inherits that guarantee for free.");
        }


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 18.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 1 - VALID OVERLOADS
    // ------------------------------------------------------------------------

    /** @param x a number to print */
    static void print(int x) {
        System.out.println("    print(int)            -> " + x);
    }

    /** @param s text to print */
    static void print(String s) {
        System.out.println("    print(String)         -> " + s);
    }

    /** @param x first number  @param y second number */
    static void print(int x, int y) {
        System.out.println("    print(int, int)       -> " + x + ", " + y);
    }

    /** @param x a number  @param s some text */
    static void print(int x, String s) {
        System.out.println("    print(int, String)    -> " + x + ", " + s);
    }

    /** @param s some text  @param x a number */
    static void print(String s, int x) {
        System.out.println("    print(String, int)    -> " + s + ", " + x);
    }

    // These would NOT compile alongside the above:
    //
    // static int print(int x) { return x; }
    //   ERROR: method print(int) is already defined - return type is not part
    //   of the signature.
    //
    // static void print(int value) { }
    //   ERROR: same reason - parameter NAMES are irrelevant.
    //
    // static void print(final int x) { }
    //   ERROR: `final` on a parameter is not part of the signature either.

    // ------------------------------------------------------------------------
    // SECTION 2 - THE THREE PHASES
    // ------------------------------------------------------------------------

    /** Reachable from an int by WIDENING - phase 1. @param x the value */
    static void show(long x) {
        System.out.println("show(long)    [phase 1: widening]");
    }

    /** Reachable from an int by BOXING - phase 2. @param x the value */
    static void show(Integer x) {
        System.out.println("show(Integer) [phase 2: boxing]");
    }

    /** Reachable from an int by VARARGS - phase 3. @param x the values */
    static void show(int... x) {
        System.out.println("show(int...)  [phase 3: varargs]");
    }

    /** No widening candidate here, so boxing wins. @param x the value */
    static void showNoLong(Integer x) {
        System.out.println("showNoLong(Integer) [phase 2: boxing]");
    }

    /** ...and the varargs version is never reached. @param x the values */
    static void showNoLong(int... x) {
        System.out.println("showNoLong(int...)  [phase 3: varargs]");
    }

    /** Only varargs available, so phase 3 it is. @param x the values */
    static void showVarargsOnly(int... x) {
        System.out.println("showVarargsOnly(int...) [phase 3: varargs]");
    }

    /** @param o any object */
    static void describe(Object o) {
        System.out.println("describe(Object)");
    }

    /** @param s any string - more specific than Object */
    static void describe(String s) {
        System.out.println("describe(String)");
    }

    /** @param x a number */
    static void widenOrBox(int x) {
        System.out.println("widenOrBox(int)");
    }

    /** @param o any object */
    static void widenOrBox(Object o) {
        System.out.println("widenOrBox(Object)");
    }

    // ------------------------------------------------------------------------
    // SECTION 3 - AMBIGUITY
    // ------------------------------------------------------------------------

    /** @param a first  @param b second */
    static void ambiguous(int a, long b) {
        System.out.println("ambiguous(int, long)");
    }

    /** @param a first  @param b second */
    static void ambiguous(long a, int b) {
        System.out.println("ambiguous(long, int)");
    }

    // ambiguous(1, 2);
    //   ERROR: reference to ambiguous is ambiguous. Neither candidate is more
    //   specific: each needs a DIFFERENT argument widened.

    /** @param s text, or null */
    static void nullable(String s) {
        System.out.println("nullable(String)");
    }

    /** @param i a number, or null */
    static void nullable(Integer i) {
        System.out.println("nullable(Integer)");
    }

    // nullable(null);
    //   ERROR: ambiguous. null is assignable to EVERY reference type, and
    //   String and Integer are unrelated, so neither is more specific.

    /** @param o any object */
    static void related(Object o) {
        System.out.println("related(Object)");
    }

    /** @param s any string - a SUBTYPE of Object, so most-specific resolves it */
    static void related(String s) {
        System.out.println("related(String)  <- resolved, because String is a SUBTYPE of Object");
    }

    // ------------------------------------------------------------------------
    // SECTION 5 - AUTOBOXING CHANGING THE CHOSEN METHOD
    // ------------------------------------------------------------------------

    /** @param x a primitive int */
    static void boxOrPrimitive(int x) {
        System.out.println("boxOrPrimitive(int)");
    }

    /** @param x a boxed Integer */
    static void boxOrPrimitive(Integer x) {
        System.out.println("boxOrPrimitive(Integer)");
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 SUPPORT - OVERRIDING, FOR CONTRAST
// ----------------------------------------------------------------------------

/** A base type whose speak() is overridden by Dog. Lesson 27 covers this. */
class Animal {
    /** Prints what this animal says. */
    void speak() {
        System.out.println("Animal.speak()");
    }
}

/** Overrides speak(). The RUNTIME type decides which version runs. */
class Dog extends Animal {
    @Override
    void speak() {
        System.out.println("Dog.speak()   <- chosen by the RUNTIME type");
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 SUPPORT - CONSTRUCTOR OVERLOADING
// ----------------------------------------------------------------------------

/**
 * Demonstrates constructor overloading with this(...) delegation. Every
 * constructor funnels into one primary constructor, so validation is written
 * once and every entry point inherits it.
 */
class Rectangle {

    private final int width;
    private final int height;

    /** A unit rectangle. Delegates to the primary constructor. */
    Rectangle() {
        this(1, 1);
    }

    /**
     * A square. Delegates to the primary constructor.
     *
     * @param side the length of every side
     */
    Rectangle(int side) {
        this(side, side);
    }

    /**
     * THE PRIMARY CONSTRUCTOR - the only one that assigns fields, and therefore
     * the only one that needs validation.
     *
     * @param width  the width; must be positive
     * @param height the height; must be positive
     * @throws IllegalArgumentException if either dimension is not positive
     */
    Rectangle(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "dimensions must be positive, got " + width + " x " + height);
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return width + " x " + height + " (area " + (width * height) + ")";
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Predict each, then run it:
 *        static void f(Object o)  { print("Object"); }
 *        static void f(String s)  { print("String"); }
 *        static void f(Integer i) { print("Integer"); }
 *        f("hi"); f(1); f(1.0); f(null); f((Object) "hi");
 *    One of these will not compile. Which, and why?
 *
 * 2. Add `static void show(Object o)` to Section 2 and predict what show(5)
 *    prints now. Then run it. Did the phase change?
 *
 * 3. Write a List<Integer> containing 0..5. Call remove(2) and
 *    remove(Integer.valueOf(2)) on separate copies and print both. Then write
 *    the version you would actually ship, with a comment explaining why.
 *
 * 4. Create a genuinely ambiguous overload pair of your own and read the
 *    compiler error. Then resolve it two different ways: with a cast at the
 *    call site, and by renaming one method.
 *
 * 5. Add a Cat class extending Animal. Put an Animal[] holding a Dog and a Cat,
 *    loop over it calling speak(), and confirm overriding picks by runtime type
 *    while a `describe(Animal)` overload would not.
 *
 * 6. Add `Rectangle(double w, double h)` to the Rectangle class. Now call
 *    `new Rectangle(4, 6)`. Which constructor runs, and why? Now try
 *    `new Rectangle(4, 6.0)`.
 * ============================================================================
 */
