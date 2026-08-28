/* ============================================================================
 * 09 - switch STATEMENTS AND switch EXPRESSIONS
 * ----------------------------------------------------------------------------
 * Companion lesson: 09-switch-statement-and-expression.md
 *
 * RUN IT (needs Java 21+ for the pattern matching in Sections 6 and 7):
 *     java Java/03-control-flow/09-switch-statement-and-expression.java
 *
 * There are now TWO constructs sharing one keyword:
 *   - the switch STATEMENT, which does something
 *   - the switch EXPRESSION, which produces a value
 * Knowing which one you are writing is the whole lesson.
 * ============================================================================
 */

class SwitchStatementAndExpression {

    /** Used to demonstrate switching on an enum and exhaustiveness checking. */
    enum OrderStatus { PLACED, SHIPPED, DELIVERED, CANCELLED }

    /* ------------------------------------------------------------------------
     * A sealed hierarchy for the record-pattern demonstration in Section 7.
     * `sealed ... permits` tells the compiler the COMPLETE list of subtypes,
     * which is what lets a switch over them be exhaustive with no default.
     * Lessons 36 and 37 cover records and sealed types properly.
     * --------------------------------------------------------------------- */
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE CLASSIC STATEMENT, AND WHY break MATTERS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE CLASSIC switch STATEMENT");
        System.out.println("=".repeat(74));

        int day = 3;
        System.out.println("Switching on day = " + day + ":");

        switch (day) {
            case 1:
                System.out.println("  Monday");
                break;          // exits the switch
            case 2:
                System.out.println("  Tuesday");
                break;
            case 3:
                System.out.println("  Wednesday");
                break;
            default:
                System.out.println("  Some other day");
        }

        System.out.println();
        System.out.println("What a case label may be: a compile-time CONSTANT only.");
        System.out.println("  a literal, a final variable holding a constant, or an enum");
        System.out.println("  constant. Never a variable, a method call, or a range.");
        System.out.println();
        System.out.println("What you may switch ON:");
        System.out.println("  byte, short, char, int and their wrappers, enum, String,");
        System.out.println("  and (Java 21) any object via patterns.");
        System.out.println("  NEVER long, float, double or boolean:");
        System.out.println("    long   - historical bytecode limitation");
        System.out.println("    float/double - equality on them is unreliable (lesson 03)");
        System.out.println("    boolean - if/else already covers exactly two cases");


        /* ====================================================================
         * SECTION 2 - FALL-THROUGH
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - FALL-THROUGH: USUALLY A BUG, SOMETIMES A FEATURE");
        System.out.println("=".repeat(74));

        char grade = 'A';
        System.out.println("Grade '" + grade + "' with MISSING breaks:");

        switch (grade) {
            case 'A':
                System.out.println("  Excellent");
                // no break - execution CONTINUES into the next case
            case 'B':
                System.out.println("  Good");
                // no break again
            case 'C':
                System.out.println("  Pass");
                break;
            default:
                System.out.println("  Fail");
        }

        System.out.println();
        System.out.println("  All three printed. Execution ENTERS at the matching label");
        System.out.println("  and runs on until a break, a return, or the end of the switch.");
        System.out.println("  C# made break mandatory because of exactly this.");

        System.out.println();
        System.out.println("The ONE legitimate use - grouping labels:");

        int month = 2;
        boolean isLeapYear = true;
        int daysInMonth;

        switch (month) {
            case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                daysInMonth = 31;
                break;
            case 4: case 6: case 9: case 11:
                daysInMonth = 30;
                break;
            case 2:
                daysInMonth = isLeapYear ? 29 : 28;
                break;
            default:
                daysInMonth = -1;
        }
        System.out.println("  month " + month + " (leap year) has " + daysInMonth + " days");
        System.out.println();
        System.out.println("  Any OTHER fall-through deserves an explicit");
        System.out.println("  // falls through   comment, because the next reader will");
        System.out.println("  assume you simply forgot the break.");


        /* ====================================================================
         * SECTION 3 - ARROW LABELS (Java 14+): FALL-THROUGH ELIMINATED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - ARROW LABELS");
        System.out.println("=".repeat(74));

        System.out.println("Same grade '" + grade + "', arrow form, no breaks written:");

        switch (grade) {
            case 'A' -> System.out.println("  Excellent");
            case 'B' -> System.out.println("  Good");
            case 'C' -> System.out.println("  Pass");
            default  -> System.out.println("  Fail");
        }

        System.out.println("  Only the matching branch ran. Fall-through is impossible.");

        System.out.println();
        System.out.println("Multiple labels are comma-separated, and a block holds many statements:");

        int weekday = 7;
        switch (weekday) {
            case 1, 2, 3, 4, 5 -> System.out.println("  " + weekday + " is a working day");
            case 6, 7 -> {
                System.out.println("  " + weekday + " is the weekend");
                System.out.println("  (a block lets a branch have several statements)");
            }
            default -> System.out.println("  not a valid day number");
        }

        System.out.println();
        System.out.println("USE ARROW FORM FOR ALL NEW CODE. It removes a whole bug class");
        System.out.println("at zero cost in readability.");


        /* ====================================================================
         * SECTION 4 - switch AS AN EXPRESSION (Java 14+)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - switch EXPRESSIONS PRODUCE A VALUE");
        System.out.println("=".repeat(74));

        // THE OLD WAY: a mutable variable, four lines, and a break you can forget.
        String oldStyleName;
        switch (day) {
            case 1: oldStyleName = "Monday"; break;
            case 2: oldStyleName = "Tuesday"; break;
            case 3: oldStyleName = "Wednesday"; break;
            default: oldStyleName = "Unknown";
        }

        // THE NEW WAY: an expression. Note the semicolon after the closing brace -
        // this whole construct is a value being assigned.
        final String newStyleName = switch (day) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            default -> "Unknown";
        };

        System.out.println("  statement form : " + oldStyleName);
        System.out.println("  expression form: " + newStyleName);
        System.out.println();
        System.out.println("  The expression form lets the variable be FINAL, cannot fall");
        System.out.println("  through, and cannot silently leave the variable unassigned.");

        /* --------------------------------------------------------------------
         * yield - producing a value from a multi-statement branch.
         * `return` inside a switch EXPRESSION is a compile error: return exits
         * the enclosing METHOD, which is not what a value-producing expression
         * can mean.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("yield - a value from a block branch:");
        System.out.println("  10 add 3      = " + calculate("add", 10, 3));
        System.out.println("  10 divide 3   = " + calculate("divide", 10, 3));
        System.out.println("  10 divide 0   = " + calculate("divide", 10, 0));
        System.out.println("  10 modulo 3   = " + calculate("modulo", 10, 3));
        System.out.println();
        System.out.println("  Inside a switch expression use `yield`, never `return`.");


        /* ====================================================================
         * SECTION 5 - EXHAUSTIVENESS: THE COMPILER CHECKS FOR YOU
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - EXHAUSTIVENESS CHECKING");
        System.out.println("=".repeat(74));

        for (OrderStatus status : OrderStatus.values()) {
            System.out.printf("  %-10s -> %s%n", status, describeStatus(status));
        }

        System.out.println();
        System.out.println("Note describeStatus() has NO default branch. That is deliberate:");
        System.out.println("  - the compiler verifies every enum constant is handled");
        System.out.println("  - add a REFUNDED constant tomorrow, and this stops compiling");
        System.out.println("  - with a default, it would silently fall through instead");
        System.out.println();
        System.out.println("A switch STATEMENT gives you none of this protection. This is");
        System.out.println("one of the strongest reasons to prefer the expression form.");


        /* ====================================================================
         * SECTION 6 - PATTERN MATCHING (Java 21)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - TYPE PATTERNS AND GUARDS");
        System.out.println("=".repeat(74));

        Object[] things = {42, "hello", 3.14, 'x', true, new int[]{1, 2, 3}, null, 100L};

        System.out.println("Matching on TYPE, not just value:");
        for (Object thing : things) {
            System.out.println("  " + describe(thing));
        }

        System.out.println();
        System.out.println("Guarded patterns with `when` - order matters, first match wins:");
        int[] samples = {-5, 0, 42, 5000};
        for (int sample : samples) {
            System.out.println("  " + sample + " -> " + classify(sample));
        }

        System.out.println();
        System.out.println("null handling changed in Java 21:");
        System.out.println("  Historically a null selector always threw NullPointerException.");
        System.out.println("  You may now write `case null ->` explicitly. If you do NOT,");
        System.out.println("  the old NPE behaviour is kept for compatibility.");


        /* ====================================================================
         * SECTION 7 - RECORD PATTERNS (Java 21)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - RECORD PATTERNS AND SEALED TYPES");
        System.out.println("=".repeat(74));

        Shape[] shapes = {
                new Circle(3),
                new Rectangle(4, 5),
                new Triangle(6, 2)
        };

        System.out.println("A pattern can DESTRUCTURE a record straight into variables:");
        for (Shape shape : shapes) {
            System.out.printf("  %-28s area = %.2f%n", shape, area(shape));
        }

        System.out.println();
        System.out.println("area() has no default branch either. Shape is `sealed`, so the");
        System.out.println("compiler knows Circle, Rectangle and Triangle are the COMPLETE");
        System.out.println("list. Add a fourth shape and area() stops compiling until you");
        System.out.println("handle it - which is exactly the reminder you want.");


        /* ====================================================================
         * SECTION 8 - switch VS if/else, AND THE RANGE LIMITATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - CHOOSING BETWEEN switch AND if/else");
        System.out.println("=".repeat(74));

        System.out.println("You CANNOT switch on a range. This is not valid Java:");
        System.out.println("    case 90..100 -> \"A\";");
        System.out.println();
        System.out.println("Workaround 1 - switch on a derived value:");
        int[] scores = {95, 83, 71, 42};
        for (int score : scores) {
            String letter = switch (score / 10) {
                case 10, 9 -> "A";
                case 8     -> "B";
                case 7     -> "C";
                case 6     -> "D";
                default    -> "F";
            };
            System.out.println("    score " + score + " -> grade " + letter);
        }

        System.out.println();
        System.out.println("Workaround 2 - if/else, which is honestly clearer for ranges:");
        for (int score : scores) {
            String letter;
            if (score >= 90)      letter = "A";
            else if (score >= 80) letter = "B";
            else if (score >= 70) letter = "C";
            else if (score >= 60) letter = "D";
            else                  letter = "F";
            System.out.println("    score " + score + " -> grade " + letter);
        }

        System.out.println();
        System.out.println("RULE OF THUMB:");
        System.out.println("  switch   - one variable against many CONSTANTS (enum, String, int)");
        System.out.println("  if/else  - ranges, or unrelated conditions joined by && and ||");
        System.out.println();
        System.out.println("Performance is rarely the deciding factor, but for many int cases");
        System.out.println("the compiler can emit a `tableswitch` bytecode - an O(1) jump");
        System.out.println("table rather than a chain of comparisons. Choose on readability.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 09.");
        System.out.println("=".repeat(74));
    }

    /**
     * Shows `yield` producing a value from a multi-statement branch, and a
     * branch that throws instead of yielding.
     *
     * @param operation the operation name
     * @param a         the left operand
     * @param b         the right operand
     * @return a description of the result, or of the failure
     */
    static String calculate(String operation, int a, int b) {
        try {
            int result = switch (operation) {
                case "add" -> a + b;
                case "subtract" -> a - b;
                case "multiply" -> a * b;
                case "divide" -> {
                    // A block branch needs `yield` to supply the value.
                    if (b == 0) {
                        // Throwing is allowed - it simply never yields.
                        throw new ArithmeticException("cannot divide by zero");
                    }
                    yield a / b;
                }
                // A branch may also be a throw expression on its own.
                default -> throw new IllegalArgumentException("unknown operation: " + operation);
            };
            return String.valueOf(result);
        } catch (ArithmeticException | IllegalArgumentException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    /**
     * Switches over an enum with NO default branch, so the compiler enforces
     * that every constant is handled. Adding a constant to OrderStatus makes
     * this method stop compiling - which is the point.
     *
     * @param status the order status
     * @return a human-readable description
     */
    static String describeStatus(OrderStatus status) {
        return switch (status) {
            case PLACED    -> "waiting to be picked";
            case SHIPPED   -> "on its way";
            case DELIVERED -> "arrived";
            case CANCELLED -> "cancelled by the customer";
            // deliberately NO default - see the note in Section 5
        };
    }

    /**
     * Matches on the runtime type of a value, replacing a chain of instanceof
     * tests. Note `case null` - without it this would throw on a null value.
     *
     * @param value any object, including null
     * @return a description of what it is
     */
    static String describe(Object value) {
        return switch (value) {
            case null           -> "null            -> nothing at all";
            case Integer i      -> "Integer " + i + "      -> doubled is " + (i * 2);
            case Long l         -> "Long " + l + "        -> a 64-bit whole number";
            case Double d       -> "Double " + d + "     -> a decimal";
            case Character c    -> "Character '" + c + "'   -> code point " + (int) c;
            case Boolean b      -> "Boolean " + b + "     -> negated is " + !b;
            case String s       -> "String \"" + s + "\"  -> length " + s.length();
            case int[] arr      -> "int[]           -> " + arr.length + " elements";
            default             -> "something else  -> " + value.getClass().getSimpleName();
        };
    }

    /**
     * Demonstrates guarded patterns. The `when` clause adds a boolean test to a
     * pattern; the FIRST matching case wins, so the most specific guards must
     * be written first.
     *
     * @param value the number to classify
     * @return a size description
     */
    static String classify(Object value) {
        return switch (value) {
            case Integer i when i < 0    -> "negative";
            case Integer i when i == 0   -> "zero";
            case Integer i when i < 100  -> "small positive";
            case Integer i               -> "large positive";
            case null, default           -> "not a number";
        };
    }

    /**
     * Destructures records directly in the case labels. No default branch is
     * needed because Shape is sealed, so the compiler knows the list of
     * subtypes is complete.
     *
     * @param shape the shape to measure
     * @return its area
     */
    static double area(Shape shape) {
        return switch (shape) {
            case Circle(double radius)             -> Math.PI * radius * radius;
            case Rectangle(double width, double h) -> width * h;
            case Triangle(double base, double h)   -> 0.5 * base * h;
        };
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a REFUNDED constant to OrderStatus. Compile. Read the error from
 *    describeStatus(). Now add a `default` branch instead and note that the
 *    compiler goes quiet - and that this is WORSE.
 *
 * 2. Rewrite the month/days switch in Section 2 using arrow labels and a
 *    switch expression that returns the day count. Compare the line counts.
 *
 * 3. Write a calculator that takes an operator char ('+', '-', '*', '/', '%')
 *    as a switch expression, throwing IllegalArgumentException by default and
 *    ArithmeticException on divide-by-zero.
 *
 * 4. Add a Square record to the Shape hierarchy (remember `permits`). Compile
 *    and watch area() fail until you handle it.
 *
 * 5. Write `String httpMessage(int code)` covering 200, 201, 301, 302, 400,
 *    401, 403, 404 and 500, grouping labels where the messages match.
 *
 * 6. Try `switch (someDouble)` and `switch (someBoolean)`. Read both compiler
 *    errors, then explain to yourself why each type is excluded.
 * ============================================================================
 */
