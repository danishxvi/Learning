/* ============================================================================
 * 22 - CONSTRUCTORS AND INITIALISATION ORDER
 * ----------------------------------------------------------------------------
 * Companion lesson: 22-constructors.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/22-constructors.java
 *
 * A constructor's job is to produce an object that is VALID FROM ITS FIRST
 * INSTANT. Get that right and whole categories of bug become impossible.
 *
 * Section 4 prints the full initialisation order as it happens, and Section 5
 * shows the genuinely nasty bug that order causes.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Constructors {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE BASICS, AND THE void TRAP
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WHAT A CONSTRUCTOR IS");
        System.out.println("=".repeat(74));

        System.out.println("  A constructor:");
        System.out.println("    - has EXACTLY the class's name");
        System.out.println("    - has NO return type, not even void");
        System.out.println("    - runs automatically on `new`, and cannot be called directly");
        System.out.println();
        System.out.println("    new Person(\"Danish\", 25) -> " + new Person("Danish", 25));

        System.out.println();
        System.out.println("  THE void TRAP:");
        LooksLikeAConstructor trapped = new LooksLikeAConstructor();
        System.out.println("    class LooksLikeAConstructor {");
        System.out.println("        String name;");
        System.out.println("        void LooksLikeAConstructor() { name = \"initialised\"; }");
        System.out.println("    }");
        System.out.println();
        System.out.println("    new LooksLikeAConstructor().name -> " + trapped.name);
        System.out.println();
        System.out.println("    Adding `void` silently turned it into an ordinary METHOD");
        System.out.println("    that happens to share the class's name. The class then got");
        System.out.println("    a DEFAULT constructor, your initialisation never ran, and");
        System.out.println("    nothing warned you. It compiles perfectly.");


        /* ====================================================================
         * SECTION 2 - THE DEFAULT CONSTRUCTOR
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE DEFAULT CONSTRUCTOR APPEARS AND DISAPPEARS");
        System.out.println("=".repeat(74));

        System.out.println("  A class with NO constructor gets one for free:");
        System.out.println("    new NoConstructorWritten() -> " + new NoConstructorWritten());
        System.out.println();
        System.out.println("    class Empty { }");
        System.out.println("  becomes");
        System.out.println("    class Empty { Empty() { super(); } }");

        System.out.println();
        System.out.println("  But the MOMENT you write any constructor, it disappears:");
        System.out.println("    class Person { Person(String name, int age) { ... } }");
        System.out.println("    new Person();   -> COMPILE ERROR: no suitable constructor");
        System.out.println();
        System.out.println("  This bites when you ADD a constructor to an existing class:");
        System.out.println("  every `new Person()` in the codebase stops compiling. If you");
        System.out.println("  need both, write both.");
        System.out.println();
        System.out.println("  The default constructor also inherits the CLASS's access");
        System.out.println("  modifier, which is why a public class gets a public one.");


        /* ====================================================================
         * SECTION 3 - CHAINING WITH this(...)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - this(...) DELEGATION");
        System.out.println("=".repeat(74));

        System.out.println("  Three constructors, all funnelling into one:");
        System.out.println("    new Rectangle()      -> " + new Rectangle());
        System.out.println("    new Rectangle(5)     -> " + new Rectangle(5));
        System.out.println("    new Rectangle(4, 6)  -> " + new Rectangle(4, 6));

        System.out.println();
        System.out.println("  The validation lives in ONE place, so every entry point gets it:");
        for (int[] bad : new int[][]{{-1, 5}, {0, 0}, {3, -2}}) {
            try {
                new Rectangle(bad[0], bad[1]);
            } catch (IllegalArgumentException e) {
                System.out.println("    new Rectangle(" + bad[0] + ", " + bad[1] + ") -> "
                        + e.getMessage());
            }
        }
        try {
            new Rectangle(-3);   // reaches the SAME validation through this(side, side)
        } catch (IllegalArgumentException e) {
            System.out.println("    new Rectangle(-3)    -> " + e.getMessage()
                    + "   <- via this(...)");
        }

        System.out.println();
        System.out.println("  RULES FOR this(...):");
        System.out.println("    - must be the FIRST statement in the constructor");
        System.out.println("    - therefore you can have this(...) OR super(...), never both");
        System.out.println("    - cannot be recursive: A() calling this() is a compile error");
        System.out.println();
        System.out.println("  WHY CHAIN: add a rule to the primary constructor and every");
        System.out.println("  other constructor inherits it automatically. Duplicating");
        System.out.println("  validation is how objects end up half-validated.");


        /* ====================================================================
         * SECTION 4 - THE FULL INITIALISATION ORDER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - INITIALISATION ORDER, PRINTED AS IT HAPPENS");
        System.out.println("=".repeat(74));

        System.out.println("  Creating the FIRST Child object (so classes load too):");
        System.out.println();
        new Child("first");

        System.out.println();
        System.out.println("  Creating a SECOND Child object:");
        System.out.println();
        new Child("second");

        System.out.println();
        System.out.println("  Notice the static lines did NOT repeat. THE ORDER IS:");
        System.out.println("    1. Parent's static blocks and static fields  (ONCE, at load)");
        System.out.println("    2. Child's static blocks and static fields   (ONCE, at load)");
        System.out.println("    3. Object's constructor");
        System.out.println("    4. Parent's instance initialisers and fields");
        System.out.println("    5. Parent's constructor body");
        System.out.println("    6. Child's instance initialisers and fields");
        System.out.println("    7. Child's constructor body");
        System.out.println();
        System.out.println("  STATICS ONCE, EVER. Instance members BOTTOM-UP: the superclass");
        System.out.println("  is fully built before the subclass starts.");

        System.out.println();
        System.out.println("  If you write neither this(...) nor super(...), the compiler");
        System.out.println("  inserts super() - the NO-ARGUMENT superclass constructor.");
        System.out.println("  That is why this is the most common inheritance error:");
        System.out.println();
        System.out.println("    class Animal { Animal(String name) { } }   // no no-arg!");
        System.out.println("    class Dog extends Animal { Dog() { } }");
        System.out.println("    -> error: no suitable constructor found for Animal()");
        System.out.println();
        System.out.println("  Fix it by calling super(name) explicitly, or by giving Animal");
        System.out.println("  a no-arg constructor.");


        /* ====================================================================
         * SECTION 5 - THE OVERRIDABLE-METHOD-IN-CONSTRUCTOR BUG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - NEVER CALL AN OVERRIDABLE METHOD FROM A CONSTRUCTOR");
        System.out.println("=".repeat(74));

        System.out.println("  Base's constructor calls init(). Derived overrides init() and");
        System.out.println("  reads its own field, which it initialises at declaration:");
        System.out.println();
        System.out.println("    class Derived extends Base {");
        System.out.println("        private String value = \"definitely set\";");
        System.out.println("        void init() { System.out.println(value); }");
        System.out.println("    }");
        System.out.println();
        System.out.print("  new Derived() prints -> ");
        Derived derived = new Derived();
        System.out.println();
        System.out.println("  After construction finishes, the field IS set:");
        System.out.println("    derived.getValue() = " + derived.getValue());

        System.out.println();
        System.out.println("  TRACE IT against Section 4's order:");
        System.out.println("    step 5 - Base's constructor body runs, and calls init()");
        System.out.println("    init() is OVERRIDDEN, so Derived.init() runs");
        System.out.println("    step 6 - Derived's field initialisers have NOT run yet");
        System.out.println("    -> `value` is still null");
        System.out.println();
        System.out.println("  THE RULE: never call an overridable method from a constructor.");
        System.out.println("  Make such methods private, final, or static. This is a real");
        System.out.println("  bug that has bitten every Java codebase of any size.");


        /* ====================================================================
         * SECTION 6 - STATIC VS INSTANCE INITIALISERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - INITIALISER BLOCKS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-14s %-32s %s%n", "", "STATIC BLOCK", "INSTANCE BLOCK");
        System.out.printf("    %-14s %-32s %s%n", "runs", "ONCE, when the class loads", "on EVERY object creation");
        System.out.printf("    %-14s %-32s %s%n", "order", "before any instance member", "after super(), before ctor body");
        System.out.printf("    %-14s %-32s %s%n", "can access", "static members only", "everything");
        System.out.printf("    %-14s %-32s %s%n", "use it for", "constants, lookup tables", "almost never");

        System.out.println();
        System.out.println("  A genuinely useful static block - building a lookup table:");
        System.out.println("    RomanNumerals.toRoman(1994) = " + RomanNumerals.toRoman(1994));
        System.out.println("    RomanNumerals.toRoman(2026) = " + RomanNumerals.toRoman(2026));
        System.out.println("    RomanNumerals.toRoman(4)    = " + RomanNumerals.toRoman(4));
        System.out.println();
        System.out.println("  The table was built once, at class load, before any call.");

        System.out.println();
        System.out.println("  INSTANCE initialiser blocks are almost never the right tool.");
        System.out.println("  Their one legitimate use is sharing code between constructors");
        System.out.println("  that cannot chain - and even then a private method called from");
        System.out.println("  each constructor is clearer. You will meet them mainly in");
        System.out.println("  anonymous classes (lesson 34).");


        /* ====================================================================
         * SECTION 7 - STATIC FACTORY METHODS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WHEN A FACTORY BEATS A CONSTRUCTOR");
        System.out.println("=".repeat(74));

        System.out.println("  These two constructors are IMPOSSIBLE - same signature:");
        System.out.println("    Temperature(double celsius)");
        System.out.println("    Temperature(double fahrenheit)");
        System.out.println();
        System.out.println("  Static factories have NAMES, so the problem disappears:");
        System.out.println("    Temperature.ofCelsius(100)    -> " + Temperature.ofCelsius(100));
        System.out.println("    Temperature.ofFahrenheit(212) -> " + Temperature.ofFahrenheit(212));
        System.out.println("    Temperature.freezing()        -> " + Temperature.freezing());

        System.out.println();
        System.out.println("  THREE ADVANTAGES OVER CONSTRUCTORS:");
        System.out.println("    1. THEY HAVE NAMES - the case above");
        System.out.println("    2. THEY CAN RETURN A CACHED INSTANCE - a constructor is");
        System.out.println("       obliged to create a new object every single time");
        System.out.println("       Integer.valueOf(127) == Integer.valueOf(127) -> "
                + (Integer.valueOf(127) == Integer.valueOf(127)));
        System.out.println("       new Integer(127) == new Integer(127)         -> false");
        System.out.println("    3. THEY CAN RETURN A SUBTYPE - List.of returns different");
        System.out.println("       implementations depending on the size:");
        System.out.println("         List.of()        -> " + List.of().getClass().getSimpleName());
        System.out.println("         List.of(1)       -> " + List.of(1).getClass().getSimpleName());
        System.out.println("         List.of(1,2,3)   -> " + List.of(1, 2, 3).getClass().getSimpleName());
        System.out.println();
        System.out.println("  This is why the JDK is full of valueOf, of, from and getInstance.");


        /* ====================================================================
         * SECTION 8 - TELESCOPING CONSTRUCTORS AND THE BUILDER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - THE TELESCOPING CONSTRUCTOR PROBLEM");
        System.out.println("=".repeat(74));

        System.out.println("  The problem - what does this call actually order?");
        System.out.println("    new Pizza(12, true, false, true, false)");
        System.out.println();
        System.out.println("  You cannot tell without opening the class. Swap two booleans");
        System.out.println("  and it compiles happily and orders the wrong pizza.");

        System.out.println();
        System.out.println("  The builder pattern (lesson 76) fixes it:");
        Pizza pizza = new Pizza.Builder(12)
                .cheese()
                .mushroom()
                .build();
        System.out.println("    " + pizza);

        Pizza plain = new Pizza.Builder(9).build();
        System.out.println("    " + plain);

        System.out.println();
        System.out.println("  Every option is named at the call site, order does not matter,");
        System.out.println("  and you only mention what you actually want.");


        /* ====================================================================
         * SECTION 9 - COPY CONSTRUCTORS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - COPY CONSTRUCTORS");
        System.out.println("=".repeat(74));

        Person originalPerson = new Person("Danish", 25);
        originalPerson.addAddress("Bengaluru");
        originalPerson.addAddress("Delhi");

        Person copy = new Person(originalPerson);
        copy.addAddress("Mumbai");

        System.out.println("  original -> " + originalPerson);
        System.out.println("  copy     -> " + copy);
        System.out.println();
        System.out.println("  Adding to the COPY did not affect the ORIGINAL, because the");
        System.out.println("  copy constructor DEEP-copied the address list:");
        System.out.println("      this.addresses = new ArrayList<>(other.addresses);");
        System.out.println();
        System.out.println("  A shallow copy would have left both objects sharing ONE list,");
        System.out.println("  so mutating either would change both. Lesson 38 covers this.");
        System.out.println();
        System.out.println("  Copy constructors are generally preferable to clone(), which");
        System.out.println("  has a famously awkward contract (lesson 32).");


        /* ====================================================================
         * SECTION 10 - CONSTRUCTOR DESIGN CHECKLIST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 10 - DESIGN CHECKLIST");
        System.out.println("=".repeat(74));

        System.out.println("  VALIDATE EVERYTHING.");
        System.out.println("    An object should never exist in an invalid state. Validate");
        System.out.println("    once in the constructor, and every method afterwards can");
        System.out.println("    then assume the state is sane.");
        System.out.println();
        System.out.println("  ASSIGN final FIELDS EXACTLY ONCE.");
        System.out.println("    The compiler enforces that every final field is definitely");
        System.out.println("    assigned once on every path through the constructor.");
        System.out.println();
        System.out.println("  KEEP CONSTRUCTORS CHEAP. No file I/O, no network calls, no");
        System.out.println("  threads. Because:");
        System.out.println("    - the class becomes untestable without that resource");
        System.out.println("    - failure mid-construction leaves a partly-built object");
        System.out.println("    - starting a thread can leak `this` before it is finished");
        System.out.println("    Use a static factory, or an init() the caller invokes.");
        System.out.println();
        System.out.println("  NEVER CALL AN OVERRIDABLE METHOD (Section 5).");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 22.");
        System.out.println("=".repeat(74));
    }
}

// ----------------------------------------------------------------------------
// SECTION 1 AND 9 - Person, with validation and a copy constructor
// ----------------------------------------------------------------------------

/** A person, demonstrating validation, a copy constructor and deep copying. */
class Person {

    private final String name;
    private final int age;
    private final List<String> addresses;

    /**
     * The primary constructor. Every rule about a valid Person lives here.
     *
     * @param name the person's name; must not be null
     * @param age  the person's age; must be 0..150
     * @throws NullPointerException     if name is null
     * @throws IllegalArgumentException if age is out of range
     */
    Person(String name, int age) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("age out of range: " + age);
        }
        this.age = age;
        this.addresses = new ArrayList<>();
    }

    /**
     * A COPY CONSTRUCTOR. Note the deep copy of the address list - a shallow
     * copy would leave both people sharing one list.
     *
     * @param other the person to copy
     */
    Person(Person other) {
        this.name = other.name;
        this.age = other.age;
        this.addresses = new ArrayList<>(other.addresses);   // DEEP copy
    }

    /** @param address an address to record */
    void addAddress(String address) {
        addresses.add(address);
    }

    @Override
    public String toString() {
        return "Person[" + name + ", " + age + ", addresses=" + addresses + "]";
    }
}

/** Demonstrates the `void` trap: this class has NO constructor of its own. */
class LooksLikeAConstructor {

    String name = "NEVER INITIALISED BY THE 'CONSTRUCTOR'";

    /**
     * This is NOT a constructor - the `void` makes it an ordinary method that
     * merely shares the class's name. It is never called by `new`.
     */
    void LooksLikeAConstructor() {
        name = "initialised";
    }
}

/** Has no constructor at all, so the compiler supplies a default one. */
class NoConstructorWritten {
    @Override
    public String toString() {
        return "built by the compiler-supplied default constructor";
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - CHAINING WITH this(...)
// ----------------------------------------------------------------------------

/** Three constructors chaining into one primary constructor. */
class Rectangle {

    private final int width;
    private final int height;

    /** A unit rectangle. */
    Rectangle() {
        this(1, 1);          // must be the FIRST statement
    }

    /** @param side the length of every side */
    Rectangle(int side) {
        this(side, side);
    }

    /**
     * THE PRIMARY CONSTRUCTOR - the only one that assigns fields, and therefore
     * the only one that needs to validate them.
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
        return width + " x " + height;
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - INITIALISATION ORDER, MADE VISIBLE
// ----------------------------------------------------------------------------

/** The superclass in the initialisation-order demonstration. */
class Parent {

    /** Static field initialiser - runs once, at class load. */
    static String parentStaticField = announce("    1. Parent static field initialiser");

    static {
        announce("    1. Parent static block");
    }

    /** Instance field initialiser - runs on every object, after super(). */
    String parentInstanceField = announce("    4. Parent instance field initialiser");

    {
        announce("    4. Parent instance block");
    }

    /** @param label which object is being built */
    Parent(String label) {
        announce("    5. Parent constructor body (" + label + ")");
    }

    /**
     * Prints and returns, so it can be used as a field initialiser.
     *
     * @param message what to print
     * @return the message
     */
    static String announce(String message) {
        System.out.println(message);
        return message;
    }
}

/** The subclass in the initialisation-order demonstration. */
class Child extends Parent {

    static String childStaticField = announce("    2. Child static field initialiser");

    static {
        announce("    2. Child static block");
    }

    String childInstanceField = announce("    6. Child instance field initialiser");

    {
        announce("    6. Child instance block");
    }

    /** @param label which object is being built */
    Child(String label) {
        // The compiler would insert super() here if we wrote nothing. We call
        // super(label) explicitly, which must be the FIRST statement.
        super(label);
        announce("    7. Child constructor body (" + label + ")");
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - THE OVERRIDABLE-METHOD BUG
// ----------------------------------------------------------------------------

/** Calls an overridable method from its constructor. This is the mistake. */
class Base {

    /** THE BUG: init() is overridable, and this runs before Derived's fields. */
    Base() {
        init();
    }

    /** Overridable, which is exactly the problem. */
    void init() {
        System.out.println("Base.init()");
    }
}

/** Overrides init() and reads a field that has not been initialised yet. */
class Derived extends Base {

    /** Initialised at declaration - which happens AFTER Base's constructor. */
    private String value = "definitely set";

    @Override
    void init() {
        // At this point Base's constructor is still running, so this class's
        // field initialisers have not executed. `value` is still null.
        System.out.println(value + "   <- null, despite the initialiser above");
    }

    /** @return the field, read after construction has finished */
    String getValue() {
        return value;
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - A USEFUL STATIC BLOCK
// ----------------------------------------------------------------------------

/** Converts integers to Roman numerals using a table built at class load. */
class RomanNumerals {

    private static final int[] VALUES;
    private static final String[] SYMBOLS;

    /*
     * A legitimate use of a static block: building a lookup table that is
     * awkward to express as a single field initialiser. It runs ONCE, when the
     * class is first used, before any method on it can be called.
     */
    static {
        VALUES = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        SYMBOLS = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    }

    /**
     * @param number a positive number
     * @return its Roman numeral representation
     */
    static String toRoman(int number) {
        StringBuilder result = new StringBuilder();
        int remaining = number;
        for (int i = 0; i < VALUES.length; i++) {
            while (remaining >= VALUES[i]) {
                result.append(SYMBOLS[i]);
                remaining -= VALUES[i];
            }
        }
        return result.toString();
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - STATIC FACTORY METHODS
// ----------------------------------------------------------------------------

/**
 * Two ways to build a temperature that a constructor could never express,
 * because both would have the signature Temperature(double).
 */
class Temperature {

    private final double celsius;

    /** Private, so the factories are the only way in. */
    private Temperature(double celsius) {
        this.celsius = celsius;
    }

    /** @param celsius the temperature in Celsius  @return a Temperature */
    static Temperature ofCelsius(double celsius) {
        return new Temperature(celsius);
    }

    /** @param fahrenheit the temperature in Fahrenheit  @return a Temperature */
    static Temperature ofFahrenheit(double fahrenheit) {
        return new Temperature((fahrenheit - 32) * 5 / 9);
    }

    /** @return the freezing point of water */
    static Temperature freezing() {
        return new Temperature(0);
    }

    @Override
    public String toString() {
        return String.format("%.1f C (%.1f F)", celsius, celsius * 9 / 5 + 32);
    }
}

// ----------------------------------------------------------------------------
// SECTION 8 - THE BUILDER PATTERN
// ----------------------------------------------------------------------------

/**
 * Replaces a telescoping constructor with a builder. Every option is named at
 * the call site, order does not matter, and you mention only what you want.
 */
class Pizza {

    private final int sizeInches;
    private final boolean cheese;
    private final boolean pepperoni;
    private final boolean mushroom;

    /** Private: the Builder is the only way to construct a Pizza. */
    private Pizza(Builder builder) {
        this.sizeInches = builder.sizeInches;
        this.cheese = builder.cheese;
        this.pepperoni = builder.pepperoni;
        this.mushroom = builder.mushroom;
    }

    @Override
    public String toString() {
        StringBuilder toppings = new StringBuilder();
        if (cheese) toppings.append("cheese ");
        if (pepperoni) toppings.append("pepperoni ");
        if (mushroom) toppings.append("mushroom ");
        if (toppings.isEmpty()) toppings.append("(no toppings)");
        return sizeInches + "\" pizza with " + toppings.toString().strip();
    }

    /** Collects options one named call at a time, then builds the Pizza. */
    static class Builder {

        private final int sizeInches;      // required, so it goes in the constructor
        private boolean cheese;            // optional, defaults to false
        private boolean pepperoni;
        private boolean mushroom;

        /**
         * @param sizeInches the pizza size; must be positive
         * @throws IllegalArgumentException if the size is not positive
         */
        Builder(int sizeInches) {
            if (sizeInches <= 0) {
                throw new IllegalArgumentException("size must be positive");
            }
            this.sizeInches = sizeInches;
        }

        /** @return this builder, so calls can chain */
        Builder cheese() {
            this.cheese = true;
            return this;
        }

        /** @return this builder, so calls can chain */
        Builder pepperoni() {
            this.pepperoni = true;
            return this;
        }

        /** @return this builder, so calls can chain */
        Builder mushroom() {
            this.mushroom = true;
            return this;
        }

        /** @return the finished, immutable Pizza */
        Pizza build() {
            return new Pizza(this);
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add `new Person()` to main and read the compile error. Then add a no-arg
 *    constructor that chains with this("Unknown", 0) and watch it compile.
 *
 * 2. Predict the exact output of `new Child("third")` BEFORE running it. Then
 *    add a second Parent constructor and make Child call it. Predict again.
 *
 * 3. Fix the Section 5 bug two ways: make init() private, and make it final.
 *    Explain what each one prevents.
 *
 * 4. Give Rectangle a `final` field you forget to assign in one constructor.
 *    Read the compiler error. Then assign it and confirm you cannot assign it
 *    twice.
 *
 * 5. Add a `.pepperoni()` call to the Pizza builder in main. Then try to build
 *    a Pizza with `new Pizza(...)` directly and confirm the private constructor
 *    stops you.
 *
 * 6. Write a Temperature.ofKelvin(double) factory that throws for values below
 *    absolute zero. Then argue why that validation belongs in the private
 *    constructor instead.
 * ============================================================================
 */
