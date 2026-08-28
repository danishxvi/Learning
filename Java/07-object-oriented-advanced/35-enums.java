/* ============================================================================
 * 35 - ENUMS
 * ----------------------------------------------------------------------------
 * Companion lesson: 35-enums.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/35-enums.java
 *
 * An enum is a CLASS WITH A FIXED, KNOWN SET OF INSTANCES. Most people use
 * them as named constants and stop there, which misses almost everything they
 * can do - Sections 4, 5 and 7 in particular.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntBinaryOperator;

class Enums {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - WHY THEY EXIST
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE int-CONSTANT ALTERNATIVE IS GENUINELY BAD");
        System.out.println("=".repeat(74));

        System.out.println("  With int constants:");
        System.out.println("      public static final int STATUS_ACTIVE = 0;");
        System.out.println("      public static final int DAY_MONDAY    = 0;");
        System.out.println();
        System.out.println("    setStatus(47);           -> compiles. Nothing stops it.");
        System.out.println("    setStatus(DAY_MONDAY);   -> compiles. A completely unrelated");
        System.out.println("                                constant that happens to be 0.");
        System.out.println("    println(status);         -> prints \"0\". Good luck debugging.");

        System.out.println();
        System.out.println("  With an enum:");
        Status status = Status.ACTIVE;
        System.out.println("    Status status = Status.ACTIVE;");
        System.out.println("    println(status)          -> " + status + "   (its own NAME)");
        System.out.println("    setStatus(47)            -> COMPILE ERROR");
        System.out.println("    setStatus(Day.MONDAY)    -> COMPILE ERROR");

        System.out.println();
        System.out.printf("    %-40s %s%n", "PROBLEM WITH int CONSTANTS", "ENUM");
        System.out.printf("    %-40s %s%n", "any int is accepted", "type-safe");
        System.out.printf("    %-40s %s%n", "prints a meaningless number", "prints its name");
        System.out.printf("    %-40s %s%n", "no namespace, STATUS_ prefixes", "Status.ACTIVE");
        System.out.printf("    %-40s %s%n", "cannot carry behaviour", "fields, methods, constructors");
        System.out.printf("    %-40s %s%n", "inlined by javac (lesson 31 hazard)", "real objects");
        System.out.printf("    %-40s %s%n", "no exhaustiveness checking", "switch is verified");


        /* ====================================================================
         * SECTION 2 - ENUMS ARE REAL CLASSES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - FIELDS, CONSTRUCTORS AND METHODS");
        System.out.println("=".repeat(74));

        System.out.println("  A Planet enum carrying real physical data:");
        System.out.println();
        System.out.printf("    %-10s %-14s %-14s %s%n", "PLANET", "MASS (kg)", "RADIUS (m)", "SURFACE GRAVITY");
        for (Planet planet : Planet.values()) {
            System.out.printf("    %-10s %-14.3e %-14.4e %.2f m/s^2%n",
                    planet, planet.getMass(), planet.getRadius(), planet.surfaceGravity());
        }

        System.out.println();
        double earthWeightNewtons = 75 * Planet.EARTH.surfaceGravity();
        System.out.println("  A 75 kg person weighs " + Math.round(earthWeightNewtons)
                + " N on Earth. Elsewhere:");
        for (Planet planet : Planet.values()) {
            System.out.printf("    %-10s %6.1f kg-equivalent%n",
                    planet, planet.weighFrom(earthWeightNewtons));
        }

        System.out.println();
        System.out.println("  THE SYNTAX RULES:");
        System.out.println("    - constants come FIRST, then a SEMICOLON, then everything else");
        System.out.println("    - the constructor is implicitly PRIVATE - you cannot write");
        System.out.println("      `new Planet(...)`");
        System.out.println("    - enums implicitly extend java.lang.Enum:");
        System.out.println("        Planet's superclass -> " + Planet.class.getSuperclass().getName());
        System.out.println("      so they CANNOT extend anything else - but they CAN implement");
        System.out.println("      interfaces (Section 6).");


        /* ====================================================================
         * SECTION 3 - THE BUILT-IN METHODS, AND TWO TRAPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE BUILT-IN METHODS");
        System.out.println("=".repeat(74));

        System.out.println("    values()          -> " + java.util.Arrays.toString(Status.values()));
        System.out.println("    valueOf(\"ACTIVE\") -> " + Status.valueOf("ACTIVE"));
        System.out.println("    ACTIVE.name()     -> " + Status.ACTIVE.name());
        System.out.println("    ACTIVE.ordinal()  -> " + Status.ACTIVE.ordinal());
        System.out.println("    SUSPENDED.ordinal() -> " + Status.SUSPENDED.ordinal());
        System.out.println("    ACTIVE.compareTo(SUSPENDED) -> "
                + Status.ACTIVE.compareTo(Status.SUSPENDED) + "   (compares by ordinal)");
        System.out.println("    ACTIVE.toString() -> " + Status.ACTIVE.toString()
                + "   (name() by default, and overridable)");
        System.out.println("    ACTIVE.getDeclaringClass() -> "
                + Status.ACTIVE.getDeclaringClass().getSimpleName());

        /* --------------------------------------------------------------------
         * TRAP 1: values() allocates a new array EVERY call.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  TRAP 1 - values() returns a FRESH COPY every call:");

        Status[] first = Status.values();
        Status[] second = Status.values();
        System.out.println("    Status.values() == Status.values() -> " + (first == second)
                + "   two different arrays");
        System.out.println("    It MUST copy: arrays are mutable, so the JDK cannot hand out");
        System.out.println("    its internal one. Watch what would happen if it did:");
        first[0] = Status.SUSPENDED;
        System.out.println("      after modifying the returned array: "
                + java.util.Arrays.toString(Status.values())
                + "   <- unaffected, correctly");

        int iterations = 5_000_000;

        long startFresh = System.nanoTime();
        int freshCount = 0;
        for (int i = 0; i < iterations; i++) {
            freshCount += Status.values().length;
        }
        long freshMillis = (System.nanoTime() - startFresh) / 1_000_000;

        long startCached = System.nanoTime();
        int cachedCount = 0;
        for (int i = 0; i < iterations; i++) {
            cachedCount += Status.CACHED_VALUES.length;
        }
        long cachedMillis = (System.nanoTime() - startCached) / 1_000_000;

        System.out.println();
        System.out.printf("    %,d calls:%n", iterations);
        System.out.println("      Status.values()      -> " + freshMillis + " ms");
        System.out.println("      a cached static array-> " + cachedMillis + " ms");
        System.out.println("      (checksums " + freshCount + " and " + cachedCount + ")");
        System.out.println("    In a hot loop, cache it in a private static final array.");

        /* --------------------------------------------------------------------
         * TRAP 2: NEVER persist ordinal().
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  TRAP 2 - NEVER PERSIST ordinal():");
        System.out.println();
        System.out.println("      enum Status { ACTIVE, INACTIVE }");
        System.out.println("        ACTIVE = 0, INACTIVE = 1");
        System.out.println();
        System.out.println("      enum Status { ACTIVE, PENDING, INACTIVE }   // one insertion");
        System.out.println("        ACTIVE = 0, PENDING = 1, INACTIVE = 2");
        System.out.println();
        System.out.println("    Every row in your database that stored 1 now means PENDING");
        System.out.println("    instead of INACTIVE. Silently. Across the whole table.");
        System.out.println();
        System.out.println("    PERSIST name(), or an explicit code field YOU control:");
        for (Status value : Status.values()) {
            System.out.printf("      %-10s name()=%-10s stableCode()=%s%n",
                    value, value.name(), value.getStableCode());
        }
        System.out.println("      A stable code survives reordering AND renaming.");
        System.out.println();
        System.out.println("    ordinal() exists for EnumMap/EnumSet internals, not for you.");

        /* --------------------------------------------------------------------
         * TRAP 3: valueOf throws.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  TRAP 3 - valueOf THROWS on anything unexpected:");
        tryValueOf("ACTIVE");
        tryValueOf("active");
        tryValueOf("UNKNOWN");
        tryValueOf(null);
        System.out.println();
        System.out.println("    For USER INPUT, write a lenient lookup returning Optional:");
        for (String input : new String[]{"active", " SUSPENDED ", "nonsense", null}) {
            System.out.println("      lookup(" + (input == null ? "null" : "\"" + input + "\"")
                    + ") -> " + Status.lookup(input));
        }


        /* ====================================================================
         * SECTION 4 - CONSTANT-SPECIFIC BEHAVIOUR
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - EACH CONSTANT CAN HAVE ITS OWN BEHAVIOUR");
        System.out.println("=".repeat(74));

        System.out.println("  CONSTANT-SPECIFIC METHOD BODIES:");
        for (Operation operation : Operation.values()) {
            System.out.printf("    12 %-6s 4 = %s%n", operation.getSymbol(),
                    operation.apply(12, 4));
        }

        System.out.println();
        System.out.println("  This is STRICTLY BETTER than a switch inside the enum:");
        System.out.println("    adding a constant WITHOUT implementing apply() is a COMPILE");
        System.out.println("    ERROR, because apply() is abstract. A switch would silently");
        System.out.println("    fall through to its default.");

        System.out.println();
        System.out.println("  THE MODERN ALTERNATIVE - a lambda in a constructor field:");
        for (LambdaOperation operation : LambdaOperation.values()) {
            System.out.printf("    12 %-6s 4 = %s%n", operation.getSymbol(),
                    operation.apply(12, 4));
        }
        System.out.println();
        System.out.println("    Both are good. The lambda form is more compact; the");
        System.out.println("    constant-body form allows SEVERAL methods per constant.");

        System.out.println();
        System.out.println("  Enums can also carry ordinary shared behaviour:");
        for (Status value : Status.values()) {
            System.out.printf("    %-10s isTerminal=%-6s canTransitionTo(ACTIVE)=%s%n",
                    value, value.isTerminal(), value.canTransitionTo(Status.ACTIVE));
        }


        /* ====================================================================
         * SECTION 5 - EnumMap AND EnumSet
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE COLLECTIONS MOST PEOPLE NEVER USE");
        System.out.println("=".repeat(74));

        Map<Day, String> schedule = new EnumMap<>(Day.class);
        schedule.put(Day.WEDNESDAY, "gym");
        schedule.put(Day.MONDAY, "standup");
        schedule.put(Day.FRIDAY, "retro");

        System.out.println("  EnumMap - inserted Wednesday, Monday, Friday IN THAT ORDER:");
        System.out.println("    " + schedule);
        System.out.println("    Iterates in DECLARATION order, for free. No comparator, no");
        System.out.println("    LinkedHashMap, no sorting.");

        Map<Day, String> hashVersion = new HashMap<>();
        hashVersion.put(Day.WEDNESDAY, "gym");
        hashVersion.put(Day.MONDAY, "standup");
        hashVersion.put(Day.FRIDAY, "retro");
        System.out.println("    the same data in a HashMap: " + hashVersion
                + "   (arbitrary order)");

        System.out.println();
        System.out.println("  EnumSet:");
        Set<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
        Set<Day> weekdays = EnumSet.complementOf(EnumSet.copyOf(weekend));
        Set<Day> everything = EnumSet.allOf(Day.class);
        Set<Day> nothing = EnumSet.noneOf(Day.class);
        Set<Day> midweek = EnumSet.range(Day.TUESDAY, Day.THURSDAY);

        System.out.println("    EnumSet.of(SAT, SUN)              -> " + weekend);
        System.out.println("    EnumSet.complementOf(weekend)     -> " + weekdays);
        System.out.println("    EnumSet.allOf(Day.class)          -> " + everything);
        System.out.println("    EnumSet.noneOf(Day.class)         -> " + nothing);
        System.out.println("    EnumSet.range(TUESDAY, THURSDAY)  -> " + midweek);

        System.out.println();
        System.out.println("  WHY THEY ARE FASTER:");
        System.out.println("    EnumMap is backed by a plain ARRAY INDEXED BY ordinal. No");
        System.out.println("    hashing, no collisions, no boxing - a direct array access.");
        System.out.println();
        System.out.println("    EnumSet is a BIT VECTOR. An enum with 64 or fewer constants");
        System.out.println("    fits in a single long, so contains() is one bitwise AND, and");
        System.out.println("    union/intersection are single machine instructions.");

        int lookups = 20_000_000;

        Map<Day, String> enumMap = new EnumMap<>(Day.class);
        Map<Day, String> plainHashMap = new HashMap<>();
        for (Day day : Day.values()) {
            enumMap.put(day, day.name());
            plainHashMap.put(day, day.name());
        }

        long startEnumMap = System.nanoTime();
        int enumMapHits = 0;
        for (int i = 0; i < lookups; i++) {
            if (enumMap.get(Day.WEDNESDAY) != null) enumMapHits++;
        }
        long enumMapMillis = (System.nanoTime() - startEnumMap) / 1_000_000;

        long startHashMap = System.nanoTime();
        int hashMapHits = 0;
        for (int i = 0; i < lookups; i++) {
            if (plainHashMap.get(Day.WEDNESDAY) != null) hashMapHits++;
        }
        long hashMapMillis = (System.nanoTime() - startHashMap) / 1_000_000;

        System.out.println();
        System.out.printf("    %,d lookups:%n", lookups);
        System.out.println("      EnumMap -> " + enumMapMillis + " ms");
        System.out.println("      HashMap -> " + hashMapMillis + " ms");
        System.out.println("      (checksums " + enumMapHits + " and " + hashMapHits + ")");

        System.out.println();
        System.out.println("    Be honest about that margin: on a warm JIT with a tiny enum,");
        System.out.println("    HashMap is already fast, so the gap is modest. EnumMap's real");
        System.out.println("    wins are MEMORY (no Node objects, no hash array) and ORDERING");
        System.out.println("    (free, and guaranteed) rather than raw lookup speed.");

        System.out.println();
        System.out.println("  IF YOUR KEY OR ELEMENT TYPE IS AN ENUM, USE EnumMap/EnumSet.");
        System.out.println("  Faster, smaller, and sensibly ordered. HashMap works, but there");
        System.out.println("  is no reason to accept it.");


        /* ====================================================================
         * SECTION 6 - IMPLEMENTING INTERFACES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - ENUMS CAN IMPLEMENT INTERFACES");
        System.out.println("=".repeat(74));

        List<Describable> describables = new ArrayList<>();
        for (Priority priority : Priority.values()) {
            describables.add(priority);
        }
        describables.add(new CustomPriority("bespoke"));

        System.out.println("  A fixed set of enum strategies alongside a non-enum one,");
        System.out.println("  sharing one interface:");
        for (Describable describable : describables) {
            System.out.println("    " + describable.getClass().getSimpleName()
                    + " -> " + describable.describe());
        }

        System.out.println();
        System.out.println("  An enum cannot extend a CLASS (it already extends Enum) but may");
        System.out.println("  implement any number of INTERFACES.");


        /* ====================================================================
         * SECTION 7 - THE SINGLETON ENUM
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE BEST SINGLETON IN JAVA");
        System.out.println("=".repeat(74));

        Configuration.INSTANCE.set("host", "localhost");
        Configuration.INSTANCE.set("port", "8080");

        System.out.println("    Configuration.INSTANCE.get(\"host\") -> "
                + Configuration.INSTANCE.get("host"));
        System.out.println("    the same instance, always?          -> "
                + (Configuration.INSTANCE == Configuration.valueOf("INSTANCE")));

        System.out.println();
        System.out.println("  Effective Java calls this the BEST way to implement a singleton,");
        System.out.println("  because the JVM guarantees all three of these:");
        System.out.println();
        System.out.println("    1. EXACTLY ONE instance, created safely at class init");
        System.out.println("    2. SERIALIZATION cannot create a second one");
        System.out.println("       (a real hole in classic singletons - readObject makes a new");
        System.out.println("        object unless you write readResolve correctly)");
        System.out.println("    3. REFLECTION cannot construct one:");

        try {
            var constructor = Configuration.class.getDeclaredConstructors()[0];
            // setAccessible(true) is exactly what defeats a private constructor
            // in an ORDINARY class. Do it here, so the refusal below cannot be
            // dismissed as mere access control.
            constructor.setAccessible(true);
            constructor.newInstance();
            System.out.println("       unreachable");
        } catch (Exception e) {
            System.out.println("       even AFTER setAccessible(true):");
            System.out.println("         " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("       The JDK refuses at a level access control cannot reach.");
            System.out.println("       A private constructor in an ORDINARY class falls to");
            System.out.println("       exactly the call above.");
        }

        System.out.println();
        System.out.println("  The classic `private static Instance instance` singleton is");
        System.out.println("  vulnerable to all three.");


        /* ====================================================================
         * SECTION 8 - ENUMS AND switch
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - switch OVER AN ENUM");
        System.out.println("=".repeat(74));

        for (Status value : Status.values()) {
            System.out.printf("    %-10s -> %s%n", value, describeStatus(value));
        }

        System.out.println();
        System.out.println("  Inside a case label you write the BARE constant name:");
        System.out.println("      case ACTIVE ->     correct");
        System.out.println("      case Status.ACTIVE -> unnecessary; the type is inferred");
        System.out.println();
        System.out.println("  OMIT default IN A SWITCH EXPRESSION OVER AN ENUM.");
        System.out.println("  describeStatus() above has no default, so adding a constant to");
        System.out.println("  Status makes it STOP COMPILING - exactly the reminder you want.");
        System.out.println("  Adding a default would silently swallow the new case. Lesson 09.");


        /* ====================================================================
         * SECTION 9 - THE MISTAKES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - THE MISTAKE LIST");
        System.out.println("=".repeat(74));

        System.out.printf("    %-38s %s%n", "MISTAKE", "WHY IT HURTS");
        System.out.printf("    %-38s %s%n", "persisting ordinal()", "reordering corrupts stored data");
        System.out.printf("    %-38s %s%n", "values() in a hot loop", "allocates an array every call");
        System.out.printf("    %-38s %s%n", "HashMap<MyEnum, V>", "EnumMap is strictly better");
        System.out.printf("    %-38s %s%n", "default in a switch over an enum", "hides missing cases");
        System.out.printf("    %-38s %s%n", "mutable state in an enum", "a global variable in disguise");
        System.out.printf("    %-38s %s%n", "valueOf on user input", "throws instead of returning");

        System.out.println();
        System.out.println("  The mutable-state one is worth a moment: enum constants are");
        System.out.println("  effectively GLOBAL SINGLETONS. A mutable field on one is a");
        System.out.println("  global variable, shared by every part of your program, with");
        System.out.println("  every problem lesson 25 listed.");
        System.out.println();
        System.out.println("  (Configuration.INSTANCE in Section 7 is deliberately mutable,");
        System.out.println("   because that is what a configuration singleton IS - but it is");
        System.out.println("   the exception, not the pattern.)");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 35.");
        System.out.println("=".repeat(74));
    }

    /**
     * Attempts valueOf and reports what happened, so the failure modes are
     * visible rather than fatal.
     *
     * @param input the name to look up; may be null
     */
    static void tryValueOf(String input) {
        String shown = input == null ? "null" : "\"" + input + "\"";
        try {
            System.out.println("    valueOf(" + shown + ") -> " + Status.valueOf(input));
        } catch (IllegalArgumentException | NullPointerException e) {
            System.out.println("    valueOf(" + shown + ") -> " + e.getClass().getSimpleName());
        }
    }

    /**
     * A switch EXPRESSION over an enum with NO default, so the compiler
     * verifies every constant is handled.
     *
     * @param status the status to describe
     * @return a human-readable description
     */
    static String describeStatus(Status status) {
        return switch (status) {
            case ACTIVE    -> "running normally";
            case SUSPENDED -> "temporarily halted";
            case CLOSED    -> "finished, cannot reopen";
            // Deliberately NO default. Add a constant to Status and this stops
            // compiling until you handle it.
        };
    }
}

// ----------------------------------------------------------------------------
// SECTIONS 1, 3, 4 AND 8
// ----------------------------------------------------------------------------

/** An account status, with a stable persistence code and real behaviour. */
enum Status {

    ACTIVE("ACT"),
    SUSPENDED("SUS"),
    CLOSED("CLS");                 // <- semicolon before the other members

    /**
     * Cached once, because values() allocates a new array on every call.
     * Package-private so the lesson can measure the difference.
     */
    static final Status[] CACHED_VALUES = values();

    /** A code YOU control, safe to store - unlike ordinal() or even name(). */
    private final String stableCode;

    /**
     * Implicitly private. You cannot write `new Status(...)`.
     *
     * @param stableCode the persistence code
     */
    Status(String stableCode) {
        this.stableCode = stableCode;
    }

    /** @return the stable code, safe to persist */
    String getStableCode() {
        return stableCode;
    }

    /** @return true if no further transition is possible */
    boolean isTerminal() {
        return this == CLOSED;
    }

    /**
     * @param target the status to move to
     * @return true if that transition is allowed
     */
    boolean canTransitionTo(Status target) {
        return !isTerminal() && this != target;
    }

    /**
     * A lenient lookup for user input: trims, ignores case, and returns empty
     * rather than throwing.
     *
     * @param input the raw text; may be null
     * @return the matching status, or empty
     */
    static Optional<Status> lookup(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String cleaned = input.strip().toUpperCase();
        for (Status candidate : CACHED_VALUES) {
            if (candidate.name().equals(cleaned) || candidate.stableCode.equals(cleaned)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}

/** Used only to show that two unrelated enums cannot be confused. */
enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

/** An enum carrying real data and computing from it. */
enum Planet {

    MERCURY(3.303e+23, 2.4397e6),
    EARTH(5.976e+24, 6.37814e6),
    JUPITER(1.9e+27, 7.1492e7);

    /** The universal gravitational constant. */
    private static final double GRAVITATIONAL_CONSTANT = 6.67300E-11;

    private final double mass;
    private final double radius;

    /** @param mass in kilograms  @param radius in metres */
    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    /** @return the mass in kilograms */
    double getMass() {
        return mass;
    }

    /** @return the radius in metres */
    double getRadius() {
        return radius;
    }

    /** @return surface gravity in m/s^2 */
    double surfaceGravity() {
        return GRAVITATIONAL_CONSTANT * mass / (radius * radius);
    }

    /**
     * @param earthWeightNewtons the object's weight on Earth
     * @return its mass-equivalent reading on this planet
     */
    double weighFrom(double earthWeightNewtons) {
        double mass = earthWeightNewtons / EARTH.surfaceGravity();
        return mass * surfaceGravity() / EARTH.surfaceGravity();
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - CONSTANT-SPECIFIC BEHAVIOUR
// ----------------------------------------------------------------------------

/**
 * Constant-specific method bodies. Each constant is effectively an anonymous
 * subclass. Adding a constant without implementing apply() is a COMPILE ERROR,
 * which a switch would never give you.
 */
enum Operation {

    PLUS("+") {
        @Override
        int apply(int left, int right) {
            return left + right;
        }
    },
    MINUS("-") {
        @Override
        int apply(int left, int right) {
            return left - right;
        }
    },
    TIMES("*") {
        @Override
        int apply(int left, int right) {
            return left * right;
        }
    },
    DIVIDE("/") {
        @Override
        int apply(int left, int right) {
            if (right == 0) {
                throw new ArithmeticException("cannot divide by zero");
            }
            return left / right;
        }
    };

    private final String symbol;

    /** @param symbol the operator symbol */
    Operation(String symbol) {
        this.symbol = symbol;
    }

    /** @return the operator symbol */
    String getSymbol() {
        return symbol;
    }

    /**
     * ABSTRACT, so every constant MUST implement it.
     *
     * @param left  the left operand
     * @param right the right operand
     * @return the result
     */
    abstract int apply(int left, int right);
}

/** The same idea with a lambda held in a constructor field - more compact. */
enum LambdaOperation {

    PLUS("+", (left, right) -> left + right),
    MINUS("-", (left, right) -> left - right),
    TIMES("*", (left, right) -> left * right);

    private final String symbol;
    private final IntBinaryOperator operation;

    /** @param symbol the operator symbol  @param operation what it does */
    LambdaOperation(String symbol, IntBinaryOperator operation) {
        this.symbol = symbol;
        this.operation = operation;
    }

    /** @return the operator symbol */
    String getSymbol() {
        return symbol;
    }

    /** @param left the left operand  @param right the right operand  @return the result */
    int apply(int left, int right) {
        return operation.applyAsInt(left, right);
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - INTERFACES
// ----------------------------------------------------------------------------

/** A contract that both enum and non-enum types can satisfy. */
interface Describable {
    /** @return a human-readable description */
    String describe();
}

/** An enum implementing an interface. */
enum Priority implements Describable {

    LOW(1), MEDIUM(5), HIGH(10);

    private final int weight;

    /** @param weight the numeric priority */
    Priority(int weight) {
        this.weight = weight;
    }

    @Override
    public String describe() {
        return "priority " + name() + " (weight " + weight + ")";
    }
}

/** A non-enum implementation of the same interface. */
class CustomPriority implements Describable {

    private final String label;

    /** @param label the priority label */
    CustomPriority(String label) {
        this.label = label;
    }

    @Override
    public String describe() {
        return "a custom priority called " + label;
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - THE SINGLETON
// ----------------------------------------------------------------------------

/**
 * The enum singleton. Deliberately mutable, because that is what a
 * configuration holder is - but note Section 9's warning that mutable enum
 * state is the exception, not the pattern.
 */
enum Configuration {

    INSTANCE;

    private final Map<String, String> settings = new HashMap<>();

    /** @param key the setting name  @param value the setting value */
    void set(String key, String value) {
        settings.put(key, value);
    }

    /** @param key the setting name  @return the value, or null */
    String get(String key) {
        return settings.get(key);
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a PENDING constant to Status, between ACTIVE and SUSPENDED. Which
 *    method stops compiling? Now imagine you had persisted ordinal() - what
 *    would every stored 1 mean now?
 *
 * 2. Add a MODULO constant to Operation WITHOUT implementing apply(). Read the
 *    error. Then do the same to LambdaOperation and compare the two errors.
 *
 * 3. Write an enum HttpMethod with GET, POST, PUT, DELETE, each knowing whether
 *    it is idempotent and whether it may carry a body. No switch statements.
 *
 * 4. Replace the EnumMap in Section 5 with a HashMap and re-run the benchmark
 *    at 100,000,000 lookups. Then explain the gap using the word "ordinal".
 *
 * 5. Build a state machine: give Status a `Set<Status> allowedTransitions`
 *    using EnumSet, and rewrite canTransitionTo() to use it.
 *
 * 6. Write a classic singleton with a private constructor, then defeat it with
 *    setAccessible(true) and reflection. Then try the same on Configuration
 *    and confirm you cannot.
 * ============================================================================
 */
