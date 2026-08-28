/* ============================================================================
 * 36 - RECORDS
 * ----------------------------------------------------------------------------
 * Companion lesson: 36-records.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/36-records.java
 *
 * A record is a TRANSPARENT CARRIER FOR IMMUTABLE DATA. One line replaces
 * about fifty of boilerplate - and, more importantly, fifty lines you would
 * probably get subtly wrong (see lesson 33).
 *
 * Standard since Java 16.
 * ============================================================================
 */

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Records {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - WHAT ONE LINE GENERATES
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - ONE LINE, FIFTY LINES OF BEHAVIOUR");
        System.out.println("=".repeat(74));

        System.out.println("      record Point(int x, int y) { }");
        System.out.println();

        Point point = new Point(3, 4);
        Point sameValues = new Point(3, 4);

        System.out.println("    canonical constructor -> new Point(3, 4)");
        System.out.println("    accessors             -> x() = " + point.x()
                + ", y() = " + point.y());
        System.out.println("    toString()            -> " + point);
        System.out.println("    equals()              -> " + point.equals(sameValues));
        System.out.println("    hashCode() consistent -> "
                + (point.hashCode() == sameValues.hashCode()));
        System.out.println("    works as a Map key    -> "
                + Map.of(point, "value").get(sameValues));

        System.out.println();
        System.out.println("  Reflection can list the components the compiler recorded:");
        for (RecordComponent component : Point.class.getRecordComponents()) {
            System.out.println("      " + component.getType().getSimpleName()
                    + " " + component.getName()
                    + "   accessor: " + component.getAccessor().getName() + "()");
        }

        System.out.println();
        System.out.println("  Compare with the hand-written equivalent in this file");
        System.out.println("  (HandWrittenPoint) - it is 50 lines and every one is a chance");
        System.out.println("  to get equals or hashCode subtly wrong:");
        HandWrittenPoint handWritten = new HandWrittenPoint(3, 4);
        System.out.println("      " + handWritten);
        System.out.println("      identical behaviour, fifty times the code");

        System.out.println();
        System.out.println("  WHAT YOU CANNOT CHANGE:");
        System.out.printf("    %-38s %s%n", "implicitly final", "cannot be extended");
        System.out.printf("    %-38s %s%n", "implicitly extends Record",
                Point.class.getSuperclass().getSimpleName() + " - so no other superclass");
        System.out.printf("    %-38s %s%n", "components are private final", "no setters, ever");
        System.out.printf("    %-38s %s%n", "cannot add INSTANCE fields", "state IS the components");
        System.out.printf("    %-38s %s%n", "CAN implement interfaces", "any number");
        System.out.printf("    %-38s %s%n", "CAN have static members", "fields and methods");
        System.out.printf("    %-38s %s%n", "CAN have extra methods", "yes");

        System.out.println();
        System.out.println("  That 'no instance fields' rule is the one that catches people -");
        System.out.println("  and it is EXACTLY what makes equals and toString trustworthy.");
        System.out.println("  There is no hidden state they could miss.");


        /* ====================================================================
         * SECTION 2 - ACCESSORS ARE x(), NOT getX()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE NAMING CONVENTION");
        System.out.println("=".repeat(74));

        System.out.println("    point.x()    -> " + point.x() + "   correct");
        System.out.println("    point.getX() -> does not exist (compile error)");
        System.out.println();
        System.out.println("  Deliberate: records follow the COMPONENT ACCESSOR convention,");
        System.out.println("  not JavaBeans. Very old tooling that scans for getX() may need");
        System.out.println("  configuration - Jackson and Spring have supported records");
        System.out.println("  natively for years.");


        /* ====================================================================
         * SECTION 3 - COMPACT CONSTRUCTORS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - VALIDATION AND NORMALISATION");
        System.out.println("=".repeat(74));

        System.out.println("      record Range(int low, int high) {");
        System.out.println("          Range {                      // NO parameter list");
        System.out.println("              if (low > high) throw ...;");
        System.out.println("          }                            // NO field assignments");
        System.out.println("      }");
        System.out.println();
        System.out.println("    new Range(1, 10)  -> " + new Range(1, 10));
        try {
            new Range(10, 1);
        } catch (IllegalArgumentException e) {
            System.out.println("    new Range(10, 1)  -> IllegalArgumentException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  The parameters are implicitly in scope, and the compiler assigns");
        System.out.println("  them to the fields AFTER your code runs. Which means you can");
        System.out.println("  also NORMALISE by reassigning the PARAMETER:");
        System.out.println();
        System.out.println("      Email {  address = address.strip().toLowerCase();  }");
        System.out.println();
        System.out.println("    new Email(\"  Danish@Example.COM  \")");
        System.out.println("      -> " + new Email("  Danish@Example.COM  "));
        System.out.println();
        System.out.println("    Writing `this.address = ...` in a compact constructor is a");
        System.out.println("    COMPILE ERROR - the compiler does that for you at the end.");

        /* --------------------------------------------------------------------
         * SHALLOW IMMUTABILITY - the trap.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  A RECORD IS ONLY SHALLOWLY IMMUTABLE:");

        List<String> callerList = new ArrayList<>(List.of("Danish", "Aisha"));
        LeakyTeam leaky = new LeakyTeam("Platform", callerList);

        System.out.println("    record LeakyTeam(String name, List<String> members) { }");
        System.out.println("      created with " + leaky);
        callerList.add("Injected From Outside");
        System.out.println("      caller then did list.add(...)");
        System.out.println("      the record now reads " + leaky + "   <- MUTATED");
        leaky.members().add("And Again");
        System.out.println("      and record.members().add(...) works too -> " + leaky);

        System.out.println();
        System.out.println("    The FIX - copy in the compact constructor:");
        List<String> anotherList = new ArrayList<>(List.of("Danish", "Aisha"));
        SafeTeam safe = new SafeTeam("Platform", anotherList);
        System.out.println("      created with " + safe);
        anotherList.add("Injected From Outside");
        System.out.println("      caller then did list.add(...)");
        System.out.println("      the record still reads " + safe + "   <- untouched");
        try {
            safe.members().add("nope");
        } catch (UnsupportedOperationException e) {
            System.out.println("      record.members().add(...) -> UnsupportedOperationException");
        }
        System.out.println();
        System.out.println("      List.copyOf also REJECTS null elements, which is a bonus.");
        System.out.println("      Lesson 38 covers defensive copying properly.");


        /* ====================================================================
         * SECTION 4 - WHAT ELSE A RECORD CAN HAVE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - RECORDS ARE STILL CLASSES");
        System.out.println("=".repeat(74));

        System.out.println("    static constant       -> RichPoint.ORIGIN = " + RichPoint.ORIGIN);
        System.out.println("    static factory        -> RichPoint.of(3, 4) = " + RichPoint.of(3, 4));
        System.out.println("    extra constructor     -> new RichPoint(5) = " + new RichPoint(5));
        System.out.println("    instance method       -> new RichPoint(3, 4).distanceFromOrigin() = "
                + new RichPoint(3, 4).distanceFromOrigin());
        System.out.println("    overridden toString   -> " + new RichPoint(3, 4));
        System.out.println("    implements Comparable -> "
                + RichPoint.of(1, 1).compareTo(RichPoint.of(5, 5)));

        System.out.println();
        System.out.println("  Any ADDITIONAL constructor MUST delegate to the canonical one");
        System.out.println("  with this(...), so validation can never be bypassed:");
        try {
            new RichPoint(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("    new RichPoint(-5) -> " + e.getMessage());
            System.out.println("    ...and that validation lives ONLY in the compact constructor.");
        }

        System.out.println();
        System.out.println("  You MAY override equals, hashCode or an accessor - but think");
        System.out.println("  hard first. The generated versions are correct, and overriding");
        System.out.println("  them usually means the type is not really a record.");


        /* ====================================================================
         * SECTION 5 - RECORD PATTERNS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - WHERE RECORDS BECOME MORE THAN BOILERPLATE");
        System.out.println("=".repeat(74));

        List<Shape> shapes = List.of(
                new Circle(3),
                new Rectangle(4, 5),
                new Line(new Point(0, 0), new Point(3, 4))
        );

        System.out.println("  A record PATTERN tests the type AND destructures in one step:");
        System.out.println();
        for (Shape shape : shapes) {
            System.out.printf("    %-46s -> %s%n", shape, describe(shape));
        }

        System.out.println();
        System.out.println("  Patterns NEST, which is where they really pay off:");
        System.out.println("      case Line(Point(var x1, var y1), Point(var x2, var y2)) -> ...");
        System.out.println("    Four variables bound from one nested structure, with the");
        System.out.println("    compiler checking every type.");

        System.out.println();
        System.out.println("  It works with instanceof too:");
        Object something = new Circle(2);
        if (something instanceof Circle(double radius)) {
            System.out.println("    if (o instanceof Circle(double radius)) -> radius = " + radius);
        }

        System.out.println();
        System.out.println("  SEALED INTERFACE + RECORDS + SWITCH is the combination that");
        System.out.println("  makes Java's data modelling genuinely pleasant. Note describe()");
        System.out.println("  has NO default branch - Shape is sealed, so the compiler");
        System.out.println("  verifies every case. Lesson 37.");


        /* ====================================================================
         * SECTION 6 - LOCAL RECORDS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - RECORDS DECLARED INSIDE A METHOD");
        System.out.println("=".repeat(74));

        summariseConfig(List.of("host=localhost", "port=8080", "timeout=30", "bad-line"));

        System.out.println();
        System.out.println("  A record scoped to ONE METHOD. Ideal for intermediate results");
        System.out.println("  in a stream pipeline, where you would previously have used an");
        System.out.println("  Object[], a Map.Entry, or an awkward two-element list.");
        System.out.println();
        System.out.println("  Local records are implicitly STATIC, so they capture nothing -");
        System.out.println("  no this$0, no leak (lesson 34).");


        /* ====================================================================
         * SECTION 7 - WHEN NOT TO USE A RECORD
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE DECISION");
        System.out.println("=".repeat(74));

        System.out.println("  USE A RECORD WHEN:");
        System.out.println("    - the type is a TRANSPARENT DATA CARRIER: its state IS its");
        System.out.println("      components");
        System.out.println("    - it should be IMMUTABLE");
        System.out.println("    - you want equals/hashCode/toString for free AND correct");
        System.out.println();
        System.out.println("    Typical: DTOs, API request/response types, coordinates, money,");
        System.out.println("    database rows, event payloads, map keys, multi-value returns");
        System.out.println("    (lesson 17), and the variants of a sealed hierarchy.");

        System.out.println();
        System.out.println("  DO NOT USE ONE WHEN:");
        System.out.println("    - the object needs MUTABLE state");
        System.out.println("    - it must EXTEND a class");
        System.out.println("    - the internal representation should be HIDDEN");
        System.out.println("    - IDENTITY matters more than value - an entity with a");
        System.out.println("      lifecycle, not a value");

        System.out.println();
        System.out.println("  THE ENCAPSULATION TRADE-OFF, made concrete:");
        System.out.println();
        System.out.println("    A record PUBLISHES its components by design:");
        TemperatureRecord asRecord = new TemperatureRecord(100);
        System.out.println("      " + asRecord + "   - callers can see it stores Celsius");
        System.out.println("      change the field to Kelvin and EVERY caller of celsius() breaks");
        System.out.println();
        System.out.println("    A class HIDES its representation (lesson 24):");
        TemperatureClass asClass = TemperatureClass.ofCelsius(100);
        System.out.printf("      %.1f C / %.1f F / %.1f K - callers cannot tell which is stored%n",
                asClass.celsius(), asClass.fahrenheit(), asClass.kelvin());
        System.out.println("      change the field to Kelvin and NOTHING outside changes");
        System.out.println();
        System.out.println("  That is a real trade-off, not an oversight. A record declares");
        System.out.println("  'this type IS these values, and that will not change'. When you");
        System.out.println("  might want to swap the representation later, write a class.");


        /* ====================================================================
         * SECTION 8 - SERIALIZATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - RECORDS DESERIALIZE SAFELY");
        System.out.println("=".repeat(74));

        System.out.println("  An ordinary Serializable class BYPASSES its constructors on");
        System.out.println("  deserialization - so validation never runs, and an attacker (or");
        System.out.println("  a corrupt file) can produce an object your constructor would");
        System.out.println("  have rejected. A long-standing source of invariant-breaking bugs.");
        System.out.println();
        System.out.println("  A RECORD deserializes THROUGH THE CANONICAL CONSTRUCTOR, so");
        System.out.println("  every validation and normalisation in the compact constructor");
        System.out.println("  runs exactly as it does for `new`.");
        System.out.println();
        System.out.println("  That means the Range record can NEVER exist with low > high -");
        System.out.println("  not from new, not from deserialization, not from reflection on");
        System.out.println("  the canonical constructor. The invariant is genuinely total.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 36.");
        System.out.println("=".repeat(74));
    }

    /**
     * Uses RECORD PATTERNS to destructure each shape. No default branch is
     * needed because Shape is sealed.
     *
     * @param shape the shape to describe
     * @return a description computed from its destructured components
     */
    static String describe(Shape shape) {
        return switch (shape) {
            case Circle(double radius) ->
                    String.format("a circle, area %.2f", Math.PI * radius * radius);
            case Rectangle(double width, double height) ->
                    String.format("a %.0f x %.0f rectangle, area %.2f", width, height, width * height);
            // NESTED record patterns: destructure the Points inside the Line.
            case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
                    String.format("a line from (%d,%d) to (%d,%d), length %.2f",
                            x1, y1, x2, y2, Math.hypot(x2 - x1, y2 - y1));
        };
    }

    /**
     * Declares and uses a LOCAL record - one scoped to this method only.
     *
     * @param lines raw key=value lines, possibly malformed
     */
    static void summariseConfig(List<String> lines) {

        // A record declared inside a method. Perfect for an intermediate shape
        // that has no meaning outside this pipeline.
        record Setting(String key, String value) {
            /** @return true if the value looks numeric */
            boolean isNumeric() {
                return value.matches("-?\\d+");
            }
        }

        List<Setting> settings = lines.stream()
                .map(line -> line.split("=", 2))
                .filter(parts -> parts.length == 2)
                .map(parts -> new Setting(parts[0], parts[1]))
                .toList();

        System.out.println("  Parsed " + settings.size() + " of " + lines.size() + " lines:");
        for (Setting setting : settings) {
            System.out.printf("    %-10s = %-12s %s%n", setting.key(), setting.value(),
                    setting.isNumeric() ? "(numeric)" : "");
        }
        System.out.println("    the malformed line was filtered out");
    }
}

// ----------------------------------------------------------------------------
// SECTION 1
// ----------------------------------------------------------------------------

/** The whole class. @param x the x coordinate  @param y the y coordinate */
record Point(int x, int y) {}

/**
 * The hand-written equivalent of the one-line record above. Roughly fifty
 * lines, and every one of them is a chance to get equals or hashCode subtly
 * wrong - which is exactly what lesson 33 was about.
 */
final class HandWrittenPoint {

    private final int x;
    private final int y;

    /** @param x the x coordinate  @param y the y coordinate */
    HandWrittenPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** @return the x coordinate */
    int x() {
        return x;
    }

    /** @return the y coordinate */
    int y() {
        return y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HandWrittenPoint point)) return false;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "HandWrittenPoint[x=" + x + ", y=" + y + "]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - COMPACT CONSTRUCTORS
// ----------------------------------------------------------------------------

/** Validates in a compact constructor. @param low the lower bound
 *  @param high the upper bound */
record Range(int low, int high) {

    /**
     * COMPACT constructor: no parameter list, no field assignments. The
     * compiler assigns the fields after this body runs.
     */
    Range {
        if (low > high) {
            throw new IllegalArgumentException("low (" + low + ") must not exceed high (" + high + ")");
        }
        // this.low = low;
        //   ERROR: cannot assign a value to final variable low - the compiler
        //   does this for you at the end of the compact constructor.
    }
}

/** Normalises by reassigning the PARAMETER. @param address the email address */
record Email(String address) {

    /** Trims and lowercases before the compiler assigns the field. */
    Email {
        Objects.requireNonNull(address, "address");
        if (!address.contains("@")) {
            throw new IllegalArgumentException("not an email address: " + address);
        }
        address = address.strip().toLowerCase();   // reassigns the PARAMETER
    }
}

/** Shallowly immutable, and therefore leaky. @param name the team name
 *  @param members the members - stored by reference */
record LeakyTeam(String name, List<String> members) {}

/** The same record with a defensive copy. @param name the team name
 *  @param members the members - copied on the way in */
record SafeTeam(String name, List<String> members) {

    /** Copies the incoming list, so neither side can mutate the other's view. */
    SafeTeam {
        members = List.copyOf(members);
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - EVERYTHING ELSE A RECORD CAN HAVE
// ----------------------------------------------------------------------------

/**
 * A record with a static constant, a static factory, an extra constructor,
 * instance methods, an implemented interface and an overridden toString.
 *
 * @param x the x coordinate
 * @param y the y coordinate
 */
record RichPoint(int x, int y) implements Comparable<RichPoint> {

    /** A static field - allowed. Instance fields are not. */
    static final RichPoint ORIGIN = new RichPoint(0, 0);

    /** The single place validation lives. */
    RichPoint {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("coordinates must be non-negative: " + x + ", " + y);
        }
    }

    /**
     * An extra constructor. It MUST delegate to the canonical one, so the
     * validation above cannot be bypassed.
     *
     * @param both the value for both coordinates
     */
    RichPoint(int both) {
        this(both, both);
    }

    /** @param x the x  @param y the y  @return a new point */
    static RichPoint of(int x, int y) {
        return new RichPoint(x, y);
    }

    /** @return the distance from the origin */
    double distanceFromOrigin() {
        return Math.hypot(x, y);
    }

    @Override
    public int compareTo(RichPoint other) {
        return Double.compare(distanceFromOrigin(), other.distanceFromOrigin());
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - RECORD PATTERNS
// ----------------------------------------------------------------------------

/** A closed set of shapes, so switch over it can be exhaustive. */
sealed interface Shape permits Circle, Rectangle, Line {}

/** @param radius the radius */
record Circle(double radius) implements Shape {}

/** @param width the width  @param height the height */
record Rectangle(double width, double height) implements Shape {}

/** Holds two records, so patterns over it NEST. @param from the start
 *  @param to the end */
record Line(Point from, Point to) implements Shape {}

// ----------------------------------------------------------------------------
// SECTION 7 - THE ENCAPSULATION TRADE-OFF
// ----------------------------------------------------------------------------

/**
 * A record PUBLISHES its representation. Change the component to Kelvin and
 * every caller of celsius() breaks.
 *
 * @param celsius the temperature in Celsius
 */
record TemperatureRecord(double celsius) {

    /** @return the temperature in Fahrenheit */
    double fahrenheit() {
        return celsius * 9 / 5 + 32;
    }
}

/**
 * A class HIDES its representation. Callers cannot tell which unit is stored,
 * so the decision stays reversible (lesson 24).
 */
final class TemperatureClass {

    /** An internal decision, invisible to every caller. */
    private final double storedCelsius;

    private TemperatureClass(double storedCelsius) {
        this.storedCelsius = storedCelsius;
    }

    /** @param celsius the temperature in Celsius  @return a Temperature */
    static TemperatureClass ofCelsius(double celsius) {
        return new TemperatureClass(celsius);
    }

    /** @return the temperature in Celsius */
    double celsius() {
        return storedCelsius;
    }

    /** @return the temperature in Fahrenheit */
    double fahrenheit() {
        return storedCelsius * 9 / 5 + 32;
    }

    /** @return the temperature in Kelvin */
    double kelvin() {
        return storedCelsius + 273.15;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Try to add `private int cachedHash;` as an instance field to Point. Read
 *    the error. Then work out where you WOULD cache such a thing.
 *
 * 2. Write `this.low = low;` inside Range's compact constructor. Read that
 *    error too, and explain what the compiler is telling you.
 *
 * 3. Add a `Money(BigDecimal amount, String currency)` record that rejects
 *    null, rejects negative amounts, and normalises the currency to uppercase.
 *    All in the compact constructor.
 *
 * 4. Add a Triangle to the Shape hierarchy. Which method stops compiling, and
 *    why is that a good thing?
 *
 * 5. Take LeakyTeam and demonstrate the mutation two ways: through the
 *    caller's original list, and through record.members(). Then fix both with
 *    one line.
 *
 * 6. Convert a class from your own code into a record. Count the lines you
 *    deleted. Then find one you should NOT convert, and write down why.
 * ============================================================================
 */
