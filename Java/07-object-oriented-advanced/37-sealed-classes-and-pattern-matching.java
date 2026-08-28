/* ============================================================================
 * 37 - SEALED CLASSES AND PATTERN MATCHING
 * ----------------------------------------------------------------------------
 * Companion lesson: 37-sealed-classes-and-pattern-matching.md
 *
 * RUN IT (needs Java 21+):
 *     java Java/07-object-oriented-advanced/37-sealed-classes-and-pattern-matching.java
 *
 * Sealed types let you say "THESE ARE THE ONLY SUBTYPES THERE WILL EVER BE".
 * Combined with records and pattern matching they give Java a genuine
 * algebraic data type - and change how you model data (Section 3).
 * ============================================================================
 */

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class SealedClassesAndPatternMatching {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - sealed AND permits
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - DECLARING A CLOSED HIERARCHY");
        System.out.println("=".repeat(74));

        System.out.println("      sealed interface Shape permits Circle, Rectangle, Triangle { }");
        System.out.println();
        System.out.println("  The compiler records that list, and reflection can read it back:");
        for (Class<?> permitted : Shape.class.getPermittedSubclasses()) {
            System.out.println("      permits " + permitted.getSimpleName());
        }

        System.out.println();
        System.out.println("  EVERY permitted subtype must be exactly one of:");
        System.out.printf("    %-14s %s%n", "final", "no further extension (records are implicitly final)");
        System.out.printf("    %-14s %s%n", "sealed", "continues the closed hierarchy with its own permits");
        System.out.printf("    %-14s %s%n", "non-sealed", "DELIBERATELY reopens the hierarchy at that branch");

        System.out.println();
        System.out.println("  All three, in one hierarchy:");
        System.out.println("      sealed interface Vehicle permits Car, CommercialVehicle, Experimental");
        for (Class<?> permitted : Vehicle.class.getPermittedSubclasses()) {
            String kind = permitted.isSealed() ? "sealed"
                    : java.lang.reflect.Modifier.isFinal(permitted.getModifiers()) ? "final"
                    : "non-sealed";
            System.out.printf("        %-22s %s%n", permitted.getSimpleName(), kind);
        }
        System.out.println();
        System.out.println("    `non-sealed` is Java's ONLY hyphenated keyword. It exists so");
        System.out.println("    you can close MOST of a hierarchy while leaving one branch");
        System.out.println("    genuinely extensible.");

        System.out.println();
        System.out.println("  THE OTHER RULES:");
        System.out.println("    - permitted subtypes must be in the SAME MODULE, or the same");
        System.out.println("      PACKAGE if the module is unnamed");
        System.out.println("    - `permits` may be OMITTED when all subtypes are in the same");
        System.out.println("      file - the compiler infers the list:");
        System.out.println("        Payment has no explicit permits clause, and the compiler");
        System.out.println("        still knows: " + Arrays.toString(
                Arrays.stream(Payment.class.getPermittedSubclasses())
                        .map(Class::getSimpleName).toArray()));


        /* ====================================================================
         * SECTION 2 - THE POINT: EXHAUSTIVENESS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE COMPILER CHECKS YOU HANDLED EVERY CASE");
        System.out.println("=".repeat(74));

        List<Shape> shapes = List.of(
                new Circle(3),
                new Rectangle(4, 5),
                new Triangle(6, 2)
        );

        System.out.printf("    %-36s %10s %10s%n", "SHAPE", "AREA", "PERIMETER");
        for (Shape shape : shapes) {
            System.out.printf("    %-36s %10.2f %10.2f%n", shape, area(shape), perimeter(shape));
        }

        System.out.println();
        System.out.println("  NEITHER method has a `default` branch. The compiler knows the");
        System.out.println("  list of subtypes is COMPLETE, so it can verify exhaustiveness.");
        System.out.println();
        System.out.println("  Add a fourth Shape and BOTH methods stop compiling, with the");
        System.out.println("  compiler naming exactly what is missing:");
        System.out.println("      error: the switch expression does not cover all possible");
        System.out.println("             input values");
        System.out.println();
        System.out.println("  COMPARE WITH AN OPEN HIERARCHY: you would need a `default`,");
        System.out.println("  which SILENTLY SWALLOWS the new case and produces a wrong");
        System.out.println("  answer at RUNTIME instead of an error at COMPILE time.");
        System.out.println();
        System.out.println("  OMIT `default` ON A SEALED SWITCH. Adding one throws away the");
        System.out.println("  entire benefit.");


        /* ====================================================================
         * SECTION 3 - THE EXPRESSION PROBLEM
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - TWO WAYS TO MODEL DATA, AND WHEN TO PICK WHICH");
        System.out.println("=".repeat(74));

        System.out.println("  POLYMORPHISM - open types, closed operations:");
        System.out.println("      interface Shape { double area(); }   // each subtype implements");
        System.out.println();
        System.out.println("    EASY to add a TYPE     - write a class, nothing else changes");
        System.out.println("    HARD to add an OPERATION - perimeter() means editing EVERY subclass");

        System.out.println();
        System.out.println("  SEALED + SWITCH - closed types, open operations:");
        System.out.println("      sealed interface Shape permits Circle, Rectangle, Triangle");
        System.out.println("      double area(Shape s)      { return switch (s) { ... }; }");
        System.out.println("      double perimeter(Shape s) { return switch (s) { ... }; }");
        System.out.println();
        System.out.println("    EASY to add an OPERATION - one new method, in one place");
        System.out.println("    HARD to add a TYPE       - every switch must change, BUT the");
        System.out.println("                               compiler tells you exactly where");

        System.out.println();
        System.out.println("  Proof that adding an operation is trivial here - describe() and");
        System.out.println("  boundingBox() were both added without touching Circle, Rectangle");
        System.out.println("  or Triangle at all:");
        for (Shape shape : shapes) {
            System.out.printf("    %-36s %s%n", describe(shape), boundingBox(shape));
        }

        System.out.println();
        System.out.println("  THIS IS THE EXPRESSION PROBLEM, and neither answer is");
        System.out.println("  universally right:");
        System.out.println();
        System.out.printf("    %-46s %s%n", "YOUR SITUATION", "CHOOSE");
        System.out.printf("    %-46s %s%n", "types change often, operations rarely", "polymorphism");
        System.out.printf("    %-46s %s%n", "operations change often, types rarely", "sealed + switch");
        System.out.printf("    %-46s %s%n", "operations do not belong on the type", "sealed + switch");
        System.out.printf("    %-46s %s%n", "types come from outside your control", "polymorphism");
        System.out.println();
        System.out.println("  A JSON node, an arithmetic expression, a protocol message, a");
        System.out.println("  result type - these have a FIXED set of shapes and a GROWING set");
        System.out.println("  of things you do to them. Sealed types fit perfectly.");
        System.out.println("  A plugin system does not.");


        /* ====================================================================
         * SECTION 4 - PATTERN MATCHING IN FULL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - EVERY FORM OF PATTERN");
        System.out.println("=".repeat(74));

        System.out.println("  TYPE PATTERNS - test and bind in one step:");
        for (Object value : new Object[]{42, "text", 3.14, List.of(1, 2), null}) {
            System.out.println("    " + classify(value));
        }

        System.out.println();
        System.out.println("  RECORD PATTERNS - destructure, and NEST:");
        Line line = new Line(new Point(0, 0), new Point(3, 4));
        System.out.println("    " + line);
        System.out.println("    " + describeLine(line));
        System.out.println("      case Line(Point(var x1, var y1), Point(var x2, var y2))");
        System.out.println("      binds four variables from one nested structure, with the");
        System.out.println("      compiler checking every type.");

        System.out.println();
        System.out.println("  GUARDED PATTERNS - `when` adds a condition:");
        List<Shape> guardedExamples = List.of(
                new Rectangle(5, 5),
                new Rectangle(4, 6),
                new Circle(0.5),
                new Circle(10)
        );
        for (Shape shape : guardedExamples) {
            System.out.printf("    %-36s -> %s%n", shape, classifyBySize(shape));
        }
        System.out.println();
        System.out.println("    ORDER MATTERS: the FIRST matching case wins, so the more");
        System.out.println("    specific guard must come first. A guarded case never counts");
        System.out.println("    toward exhaustiveness - the compiler still requires an");
        System.out.println("    UNGUARDED case for that type.");

        System.out.println();
        System.out.println("  null HANDLING:");
        System.out.println("    " + classify(null));
        System.out.println("    Historically switch threw NullPointerException on a null");
        System.out.println("    selector. Since Java 21 you may write `case null ->`. If you");
        System.out.println("    do NOT, the old NPE behaviour is preserved for compatibility:");
        try {
            switchWithoutNullCase(null);
        } catch (NullPointerException e) {
            System.out.println("      a switch with no `case null` -> NullPointerException");
        }


        /* ====================================================================
         * SECTION 5 - THE RESULT TYPE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - MAKING FAILURE IMPOSSIBLE TO IGNORE");
        System.out.println("=".repeat(74));

        System.out.println("      sealed interface Result<T> permits Success, Failure { }");
        System.out.println();

        for (String input : new String[]{"42", "-1", "abc", null}) {
            Result<Integer> result = parsePositive(input);
            System.out.printf("    parsePositive(%-6s) -> %-38s %s%n",
                    input == null ? "null" : "\"" + input + "\"",
                    result,
                    render(result));
        }

        System.out.println();
        System.out.println("  The compiler FORCES the caller to handle failure. There is:");
        System.out.println("    - no way to forget - the switch would not compile");
        System.out.println("    - no null to check");
        System.out.println("    - no exception to catch (or silently swallow)");
        System.out.println();
        System.out.println("  This is how Rust's Result and Kotlin's sealed classes work, and");
        System.out.println("  it is now expressible in plain Java.");
        System.out.println();
        System.out.println("  Other natural fits: JSON trees, expression evaluators, state");
        System.out.println("  machines, protocol messages, and the states of a UI.");

        /* --------------------------------------------------------------------
         * A REAL EXPRESSION EVALUATOR - the textbook case for sealed types.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  A complete arithmetic evaluator in one recursive switch:");
        System.out.println();

        // (2 + 3) * (10 - 4)
        Expression expression = new Multiply(
                new Add(new Literal(2), new Literal(3)),
                new Subtract(new Literal(10), new Literal(4)));

        System.out.println("    " + format(expression));
        System.out.println("    evaluates to " + evaluate(expression));
        System.out.println();
        System.out.println("    Two operations - evaluate() and format() - over four node");
        System.out.println("    types, and neither node type knows either operation exists.");
        System.out.println("    Adding a Divide node breaks both switches until handled.");


        /* ====================================================================
         * SECTION 6 - SEALED VERSUS ENUM
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - SEALED HIERARCHY OR ENUM?");
        System.out.println("=".repeat(74));

        System.out.printf("    %-32s %-24s %s%n", "", "ENUM", "SEALED HIERARCHY");
        System.out.printf("    %-32s %-24s %s%n", "instances", "a FIXED NUMBER", "UNLIMITED");
        System.out.printf("    %-32s %-24s %s%n", "each variant carries", "the same fields", "DIFFERENT fields");
        System.out.printf("    %-32s %-24s %s%n", "best for", "fixed constants", "fixed SHAPES");

        System.out.println();
        System.out.println("  Status.ACTIVE is ONE value. Circle is a KIND of value with");
        System.out.println("  infinitely many instances:");
        System.out.println("    every Circle ever made -> unlimited: "
                + new Circle(1) + ", " + new Circle(2.5) + ", ...");
        System.out.println("    every Status ever made -> exactly three, forever");
        System.out.println();
        System.out.println("  When your variants need DIFFERENT DATA, you want a sealed");
        System.out.println("  hierarchy. When they are just named constants, you want an enum.");


        /* ====================================================================
         * SECTION 7 - PRACTICAL NOTES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WHAT TO KNOW BEFORE YOU SEAL SOMETHING");
        System.out.println("=".repeat(74));

        System.out.println("  A SEALED HIERARCHY IS AN API COMMITMENT.");
        System.out.println("    Adding a permitted subtype is source-compatible but");
        System.out.println("    BEHAVIOURALLY breaking for every downstream switch. That is");
        System.out.println("    by design, and it is why you seal deliberately.");
        System.out.println();
        System.out.println("  RECORDS + SEALED IS THE NATURAL PAIRING.");
        System.out.println("    Records are implicitly final, so they satisfy the");
        System.out.println("    permitted-subtype rule with no extra keyword:");
        System.out.println("      Circle is final? "
                + java.lang.reflect.Modifier.isFinal(Circle.class.getModifiers())
                + "   (and nobody wrote `final`)");
        System.out.println();
        System.out.println("  `permits` CAN BE OMITTED when everything is in one file.");
        System.out.println();
        System.out.println("  REFLECTION CAN ENUMERATE THEM:");
        System.out.println("    Shape.class.isSealed()                -> " + Shape.class.isSealed());
        System.out.println("    Shape.class.getPermittedSubclasses()  -> "
                + Shape.class.getPermittedSubclasses().length + " subtypes");
        System.out.println();
        System.out.println("  EXHAUSTIVENESS applies to switch EXPRESSIONS, and since Java 21");
        System.out.println("  to switch STATEMENTS over sealed types too. A pre-21 switch");
        System.out.println("  statement gets no such check.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 37.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 2 AND 3 - OPERATIONS OVER A SEALED HIERARCHY
    // ------------------------------------------------------------------------

    /**
     * No default branch: the compiler verifies every Shape is handled.
     *
     * @param shape the shape to measure
     * @return its area
     */
    static double area(Shape shape) {
        return switch (shape) {
            case Circle(double radius) -> Math.PI * radius * radius;
            case Rectangle(double width, double height) -> width * height;
            case Triangle(double base, double height) -> 0.5 * base * height;
        };
    }

    /**
     * A SECOND operation, added without touching any of the three record types.
     *
     * @param shape the shape to measure
     * @return its perimeter
     */
    static double perimeter(Shape shape) {
        return switch (shape) {
            case Circle(double radius) -> 2 * Math.PI * radius;
            case Rectangle(double width, double height) -> 2 * (width + height);
            // A rough approximation: this triangle only knows base and height.
            case Triangle(double base, double height) ->
                    base + 2 * Math.hypot(base / 2, height);
        };
    }

    /**
     * A THIRD operation. Still nothing changed in the record types.
     *
     * @param shape the shape to describe
     * @return a human-readable description
     */
    static String describe(Shape shape) {
        return switch (shape) {
            case Circle(double radius) -> "a circle of radius " + radius;
            case Rectangle(double width, double height) when width == height ->
                    "a square of side " + width;
            case Rectangle(double width, double height) ->
                    "a " + width + " by " + height + " rectangle";
            case Triangle(double base, double height) ->
                    "a triangle, base " + base;
        };
    }

    /**
     * A FOURTH operation, returning the bounding box dimensions.
     *
     * @param shape the shape to bound
     * @return a description of the bounding box
     */
    static String boundingBox(Shape shape) {
        return switch (shape) {
            case Circle(double radius) ->
                    String.format("bounding box %.1f x %.1f", radius * 2, radius * 2);
            case Rectangle(double width, double height) ->
                    String.format("bounding box %.1f x %.1f", width, height);
            case Triangle(double base, double height) ->
                    String.format("bounding box %.1f x %.1f", base, height);
        };
    }

    // ------------------------------------------------------------------------
    // SECTION 4 - PATTERN FORMS
    // ------------------------------------------------------------------------

    /**
     * Type patterns over an open type, including an explicit null case.
     *
     * @param value any object, or null
     * @return a description of what it is
     */
    static String classify(Object value) {
        return switch (value) {
            case null            -> "null           -> handled by `case null`, no exception";
            case Integer i       -> "Integer " + i + "     -> doubled is " + (i * 2);
            case String s        -> "String \"" + s + "\"  -> length " + s.length();
            case Double d        -> "Double " + d + "   -> rounded is " + Math.round(d);
            case List<?> list    -> "List           -> " + list.size() + " elements";
            default              -> "something else -> " + value.getClass().getSimpleName();
        };
    }

    /**
     * A switch with NO `case null`, to show the historical behaviour survives.
     *
     * @param value the value to switch on
     * @return a description
     */
    static String switchWithoutNullCase(Object value) {
        return switch (value) {
            case Integer i -> "an integer";
            default        -> "something else";
        };
    }

    /**
     * NESTED record patterns: destructures the Points inside the Line.
     *
     * @param line the line to describe
     * @return a description built from four destructured values
     */
    static String describeLine(Line line) {
        return switch (line) {
            case Line(Point(var x1, var y1), Point(var x2, var y2)) ->
                    String.format("from (%d,%d) to (%d,%d), length %.2f",
                            x1, y1, x2, y2, Math.hypot(x2 - x1, y2 - y1));
        };
    }

    /**
     * Guarded patterns. The first match wins, so the specific guards come
     * first; the unguarded case for each type is what satisfies exhaustiveness.
     *
     * @param shape the shape to classify
     * @return a size description
     */
    static String classifyBySize(Shape shape) {
        return switch (shape) {
            case Rectangle(double width, double height) when width == height ->
                    "a SQUARE (guarded: width == height)";
            case Rectangle rectangle -> "a rectangle (unguarded fallback)";
            case Circle(double radius) when radius < 1 -> "a TINY circle (guarded: radius < 1)";
            case Circle circle -> "a circle (unguarded fallback)";
            case Triangle triangle -> "a triangle";
        };
    }

    // ------------------------------------------------------------------------
    // SECTION 5 - THE RESULT TYPE
    // ------------------------------------------------------------------------

    /**
     * Returns a Result rather than throwing or returning null, so the caller
     * cannot ignore the failure case.
     *
     * @param input the text to parse; may be null
     * @return a Success holding the number, or a Failure explaining why not
     */
    static Result<Integer> parsePositive(String input) {
        if (input == null) {
            return new Failure<>("input was null");
        }
        try {
            int parsed = Integer.parseInt(input.strip());
            return parsed > 0
                    ? new Success<>(parsed)
                    : new Failure<>("not positive: " + parsed);
        } catch (NumberFormatException e) {
            return new Failure<>("not a number: \"" + input + "\"");
        }
    }

    /**
     * Handles both cases, because the compiler will not accept anything less.
     *
     * @param result the result to render
     * @return a message for either outcome
     */
    static String render(Result<Integer> result) {
        return switch (result) {
            case Success<Integer>(Integer value) -> "OK: " + value;
            case Failure<Integer>(String error)  -> "FAILED: " + error;
        };
    }

    /**
     * Evaluates an expression tree with one recursive switch.
     *
     * @param expression the tree to evaluate
     * @return its numeric value
     */
    static int evaluate(Expression expression) {
        return switch (expression) {
            case Literal(int value) -> value;
            case Add(Expression left, Expression right) -> evaluate(left) + evaluate(right);
            case Subtract(Expression left, Expression right) -> evaluate(left) - evaluate(right);
            case Multiply(Expression left, Expression right) -> evaluate(left) * evaluate(right);
        };
    }

    /**
     * A SECOND operation over the same tree, added without touching any node
     * type - the whole argument for sealed types.
     *
     * @param expression the tree to render
     * @return the expression written out with brackets
     */
    static String format(Expression expression) {
        return switch (expression) {
            case Literal(int value) -> String.valueOf(value);
            case Add(Expression left, Expression right) ->
                    "(" + format(left) + " + " + format(right) + ")";
            case Subtract(Expression left, Expression right) ->
                    "(" + format(left) + " - " + format(right) + ")";
            case Multiply(Expression left, Expression right) ->
                    "(" + format(left) + " * " + format(right) + ")";
        };
    }
}

// ----------------------------------------------------------------------------
// THE SEALED HIERARCHIES
// ----------------------------------------------------------------------------

/** A closed set of shapes. Nothing outside this list can ever implement it. */
sealed interface Shape permits Circle, Rectangle, Triangle {}

/** @param radius the radius */
record Circle(double radius) implements Shape {}

/** @param width the width  @param height the height */
record Rectangle(double width, double height) implements Shape {}

/** @param base the base  @param height the height */
record Triangle(double base, double height) implements Shape {}

/** No `permits` clause - the compiler infers it from this file. */
sealed interface Payment {}

/** @param amount the amount */
record CardPayment(double amount) implements Payment {}

/** @param amount the amount */
record CashPayment(double amount) implements Payment {}

/** Demonstrates all three permitted-subtype modifiers in one hierarchy. */
sealed interface Vehicle permits Car, CommercialVehicle, Experimental {}

/** FINAL: this branch is closed. @param model the model name */
record Car(String model) implements Vehicle {}

/** SEALED: this branch continues the closed hierarchy. */
sealed interface CommercialVehicle extends Vehicle permits Truck {}

/** @param tonnes the payload capacity */
record Truck(double tonnes) implements CommercialVehicle {}

/**
 * NON-SEALED: this branch is deliberately REOPENED, so anyone may extend it.
 * The only hyphenated keyword in Java.
 */
non-sealed interface Experimental extends Vehicle {}

// ----------------------------------------------------------------------------
// SECTION 4 - NESTED PATTERNS
// ----------------------------------------------------------------------------

/** @param x the x coordinate  @param y the y coordinate */
record Point(int x, int y) {}

/** Holds two records, so patterns over it nest. @param from the start
 *  @param to the end */
record Line(Point from, Point to) {}

// ----------------------------------------------------------------------------
// SECTION 5 - RESULT AND EXPRESSIONS
// ----------------------------------------------------------------------------

/**
 * A result that is either a success or a failure - and the compiler forces
 * callers to handle both.
 *
 * @param <T> the success value type
 */
sealed interface Result<T> permits Success, Failure {}

/** @param value the successful value  @param <T> the value type */
record Success<T>(T value) implements Result<T> {}

/** @param error what went wrong  @param <T> the value type that did not arrive */
record Failure<T>(String error) implements Result<T> {}

/** An arithmetic expression tree - the textbook case for sealed types. */
sealed interface Expression permits Literal, Add, Subtract, Multiply {}

/** @param value the constant */
record Literal(int value) implements Expression {}

/** @param left the left operand  @param right the right operand */
record Add(Expression left, Expression right) implements Expression {}

/** @param left the left operand  @param right the right operand */
record Subtract(Expression left, Expression right) implements Expression {}

/** @param left the left operand  @param right the right operand */
record Multiply(Expression left, Expression right) implements Expression {}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a `record Divide(Expression left, Expression right)` to the
 *    Expression hierarchy. Count how many methods stop compiling, and note
 *    that the compiler names every one.
 *
 * 2. Add a `default` branch to area(). Now add a fourth Shape. Confirm it
 *    compiles silently and returns a wrong answer. Then delete the default.
 *
 * 3. Write `sealed interface Json permits JsonNull, JsonBoolean, JsonNumber,
 *    JsonString, JsonArray, JsonObject` and a `String render(Json)` that
 *    serialises any tree. One recursive switch.
 *
 * 4. Reorder classifyBySize() so the unguarded `case Rectangle rectangle`
 *    comes FIRST. Read the error, and explain it in terms of "first match wins".
 *
 * 5. Model a traffic light two ways: as an enum, and as a sealed interface
 *    with records. Which is better, and what would change your mind?
 *
 * 6. Take an if/else-if chain of instanceof checks from your own code and
 *    convert it to a sealed hierarchy plus a switch. What did the compiler
 *    catch that you had missed?
 * ============================================================================
 */
