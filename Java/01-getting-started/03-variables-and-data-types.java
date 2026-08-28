/* ============================================================================
 * 03 - VARIABLES, PRIMITIVE TYPES AND LITERALS
 * ----------------------------------------------------------------------------
 * Companion lesson: 03-variables-and-data-types.md
 *
 * RUN IT:
 *     java Java/01-getting-started/03-variables-and-data-types.java
 *
 * Every claim in the lesson is PROVEN below by printing the real value. Do not
 * take the table's word for it - watch the overflow happen, watch 0.1 + 0.2
 * fail, watch a char behave like a number.
 * ============================================================================
 */

import java.math.BigDecimal;

class VariablesAndDataTypes {

    /* ------------------------------------------------------------------------
     * FIELDS - declared outside any method, so they belong to the class.
     * Notice we NEVER assign these. Fields are given default values
     * automatically. Local variables are not. Section 7 proves the difference.
     * --------------------------------------------------------------------- */
    static byte    defaultByte;
    static short   defaultShort;
    static int     defaultInt;
    static long    defaultLong;
    static float   defaultFloat;
    static double  defaultDouble;
    static char    defaultChar;
    static boolean defaultBoolean;
    static String  defaultReference;   // a reference type, not a primitive

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE EIGHT PRIMITIVES, WITH THEIR REAL LIMITS
         * --------------------------------------------------------------------
         * Never memorise the ranges. Every wrapper class publishes them as
         * constants, and asking the JDK is always right.
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE EIGHT PRIMITIVE TYPES");
        System.out.println("=".repeat(74));

        byte    smallWhole   = 100;
        short   mediumWhole  = 30_000;        // underscores are ignored by the compiler
        int     normalWhole  = 2_000_000_000;
        long    hugeWhole    = 9_000_000_000L; // L suffix REQUIRED: the literal itself
                                               // would overflow int without it
        float   smallDecimal = 3.14f;          // f suffix REQUIRED: literals are double
        double  bigDecimal   = 3.141592653589793;
        char    singleLetter = 'A';            // SINGLE quotes, exactly one character
        boolean isLearning   = true;           // only true/false - never 0 or 1

        System.out.printf("%-9s %-8s %-24s %s%n", "TYPE", "BITS", "MIN", "MAX");
        System.out.printf("%-9s %-8d %-24d %d%n", "byte",   Byte.SIZE,    Byte.MIN_VALUE,    Byte.MAX_VALUE);
        System.out.printf("%-9s %-8d %-24d %d%n", "short",  Short.SIZE,   Short.MIN_VALUE,   Short.MAX_VALUE);
        System.out.printf("%-9s %-8d %-24d %d%n", "int",    Integer.SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.printf("%-9s %-8d %-24d %d%n", "long",   Long.SIZE,    Long.MIN_VALUE,    Long.MAX_VALUE);
        System.out.printf("%-9s %-8d %-24s %s%n", "float",  Float.SIZE,   Float.MIN_VALUE,   Float.MAX_VALUE);
        System.out.printf("%-9s %-8d %-24s %s%n", "double", Double.SIZE,  Double.MIN_VALUE,  Double.MAX_VALUE);
        System.out.printf("%-9s %-8d %-24d %d%n", "char",   Character.SIZE,
                (int) Character.MIN_VALUE, (int) Character.MAX_VALUE);
        System.out.println("boolean   (size is deliberately unspecified by the JVM spec)");

        System.out.println();
        System.out.println("The values we declared:");
        System.out.println("  byte    = " + smallWhole);
        System.out.println("  short   = " + mediumWhole);
        System.out.println("  int     = " + normalWhole);
        System.out.println("  long    = " + hugeWhole);
        System.out.println("  float   = " + smallDecimal);
        System.out.println("  double  = " + bigDecimal);
        System.out.println("  char    = " + singleLetter);
        System.out.println("  boolean = " + isLearning);

        // NOTE on float vs double precision: float keeps roughly 7 significant
        // digits, double roughly 15. Watch the same constant lose its tail.
        float  piAsFloat  = 3.141592653589793f;
        double piAsDouble = 3.141592653589793;
        System.out.println();
        System.out.println("Same constant, two types (precision loss is visible):");
        System.out.println("  as float  = " + piAsFloat);
        System.out.println("  as double = " + piAsDouble);


        /* ====================================================================
         * SECTION 2 - OVERFLOW: JAVA WRAPS AROUND SILENTLY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - INTEGER OVERFLOW (silent, no exception)");
        System.out.println("=".repeat(74));

        int atTheTop = Integer.MAX_VALUE;
        System.out.println("Integer.MAX_VALUE     = " + atTheTop);
        System.out.println("Integer.MAX_VALUE + 1 = " + (atTheTop + 1) + "   <- wrapped to the MINIMUM");

        int atTheBottom = Integer.MIN_VALUE;
        System.out.println("Integer.MIN_VALUE     = " + atTheBottom);
        System.out.println("Integer.MIN_VALUE - 1 = " + (atTheBottom - 1) + "    <- wrapped to the MAXIMUM");

        // The famous real-world version of this bug: computing a midpoint.
        int low  = 2_000_000_000;
        int high = 2_100_000_000;
        System.out.println();
        System.out.println("Midpoint of " + low + " and " + high + ":");
        System.out.println("  (low + high) / 2        = " + ((low + high) / 2) + "   <- NEGATIVE. Broken.");
        System.out.println("  low + (high - low) / 2  = " + (low + (high - low) / 2) + "   <- correct");
        System.out.println("  This exact bug sat in the JDK's own binarySearch for nine years.");

        // Math.*Exact throws instead of wrapping - use it when silent
        // wraparound would be a correctness bug rather than a design choice.
        System.out.println();
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
        } catch (ArithmeticException e) {
            System.out.println("Math.addExact(MAX_VALUE, 1) threw: " + e.getMessage());
        }

        // Promoting to long before the arithmetic also avoids overflow.
        System.out.println("Widening first: (long) MAX_VALUE + 1 = " + ((long) Integer.MAX_VALUE + 1));


        /* ====================================================================
         * SECTION 3 - FLOATING POINT IS NOT EXACT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - FLOATING POINT (IEEE 754)");
        System.out.println("=".repeat(74));

        System.out.println("0.1 + 0.2          = " + (0.1 + 0.2));
        System.out.println("0.1 + 0.2 == 0.3   = " + (0.1 + 0.2 == 0.3) + "   <- FALSE");
        System.out.println("Reason: 1/10 has no exact binary representation, exactly as");
        System.out.println("1/3 has no exact decimal representation.");

        // The correct way to compare doubles: within a tolerance (epsilon).
        double left    = 0.1 + 0.2;
        double right   = 0.3;
        double epsilon = 1e-9;
        System.out.println();
        System.out.println("Comparing within a tolerance of 1e-9:");
        System.out.println("  Math.abs(left - right) < epsilon = " + (Math.abs(left - right) < epsilon));

        // The correct way to handle MONEY: BigDecimal, built from Strings.
        System.out.println();
        System.out.println("Money must never be a double. Use BigDecimal:");
        BigDecimal priceCorrect = new BigDecimal("0.10");
        BigDecimal taxCorrect   = new BigDecimal("0.20");
        System.out.println("  new BigDecimal(\"0.10\").add(new BigDecimal(\"0.20\")) = "
                + priceCorrect.add(taxCorrect) + "   <- exact");

        // The WRONG BigDecimal constructor: the double is already inaccurate
        // before BigDecimal ever receives it, so BigDecimal faithfully records
        // the inaccuracy.
        System.out.println("  new BigDecimal(0.10) = " + new BigDecimal(0.10));
        System.out.println("  ^ that is why you always use the String constructor.");

        // Special floating-point values. Note the asymmetry with integers.
        System.out.println();
        System.out.println("Special values:");
        System.out.println("   1.0 / 0            = " + (1.0 / 0));
        System.out.println("  -1.0 / 0            = " + (-1.0 / 0));
        System.out.println("   0.0 / 0            = " + (0.0 / 0));
        System.out.println("  Double.NaN == Double.NaN = " + (Double.NaN == Double.NaN)
                + "   <- NaN equals nothing, not even itself");
        System.out.println("  Double.isNaN(0.0/0)      = " + Double.isNaN(0.0 / 0)
                + "    <- the correct test");

        try {
            System.out.println(1 / 0);   // INTEGER division by zero
        } catch (ArithmeticException e) {
            System.out.println("  1 / 0 (integers) threw: " + e.getMessage()
                    + "   <- integers throw, doubles do not");
        }


        /* ====================================================================
         * SECTION 4 - char IS A NUMBER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - char IS A 16-BIT UNSIGNED NUMBER");
        System.out.println("=".repeat(74));

        char letterA = 'A';
        int  codeOfA = letterA;   // widening: char -> int needs no cast

        System.out.println("char letterA      = " + letterA);
        System.out.println("int  codeOfA      = " + codeOfA + "   <- 'A' is code point 65");
        System.out.println("'A' + 1           = " + ('A' + 1) + "   <- + promotes both operands to int");
        System.out.println("(char)('A' + 1)   = " + (char) ('A' + 1) + "    <- cast back to see the letter");

        char fromNumber = 66;               // assigning a number directly
        char fromEscape = 'C';         // Unicode escape for 'C'
        char rupeeSign  = '₹';    // any Basic Multilingual Plane character
        System.out.println("char fromNumber   = " + fromNumber + "    (assigned the number 66)");
        System.out.println("char fromEscape   = " + fromEscape + "    (assigned a Unicode escape)");
        System.out.println("rupee sign code   = " + (int) rupeeSign + " (prints as a symbol your console may not show)");

        // Because char is a number, you can loop over an alphabet arithmetically.
        System.out.print("Alphabet by arithmetic: ");
        for (char c = 'a'; c <= 'e'; c++) {
            System.out.print(c + " ");
        }
        System.out.println();

        // A 16-bit char cannot hold characters above the Basic Multilingual
        // Plane. Emoji need TWO chars (a surrogate pair) - which is why
        // String.length() on an emoji surprises people.
        String emoji = "😀";      // a grinning face, stored as a surrogate pair
        System.out.println("Emoji String.length()      = " + emoji.length()
                + "   <- two chars, one visible character");
        System.out.println("Emoji codePointCount()     = " + emoji.codePointCount(0, emoji.length())
                + "   <- the count you probably wanted");


        /* ====================================================================
         * SECTION 5 - LITERALS: THE WAYS TO WRITE A VALUE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - LITERAL FORMS (all of these equal 255)");
        System.out.println("=".repeat(74));

        int decimal     = 255;
        int hexadecimal = 0xFF;          // 0x prefix
        int octal       = 0377;          // LEADING ZERO means octal - a real trap
        int binary      = 0b1111_1111;   // 0b prefix, Java 7+

        System.out.println("  255          = " + decimal);
        System.out.println("  0xFF         = " + hexadecimal);
        System.out.println("  0377         = " + octal);
        System.out.println("  0b1111_1111  = " + binary);

        System.out.println();
        System.out.println("THE OCTAL TRAP - a leading zero silently changes the value:");
        int looksLikeOneTwoThree = 0123;
        System.out.println("  int x = 0123;  ->  " + looksLikeOneTwoThree
                + "   <- NOT 123. Never zero-pad numeric literals.");

        System.out.println();
        System.out.println("Scientific notation and underscores:");
        System.out.println("  1.5e3        = " + 1.5e3);
        System.out.println("  1_000_000    = " + 1_000_000 + "   (underscores are ignored by the compiler)");


        /* ====================================================================
         * SECTION 6 - PRIMITIVE VS REFERENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - PRIMITIVES VS REFERENCES");
        System.out.println("=".repeat(74));

        int    primitiveValue = 42;        // the variable HOLDS 42
        String referenceValue = "hello";   // the variable holds an ADDRESS

        System.out.println("int    primitiveValue = " + primitiveValue);
        System.out.println("String referenceValue = " + referenceValue);
        System.out.println();
        System.out.println("A reference can be null; a primitive can never be:");
        String nullable = null;
        System.out.println("  String nullable = null;   -> legal, value is " + nullable);
        // int impossible = null;   // ERROR: incompatible types
        System.out.println("  int impossible = null;    -> COMPILE ERROR (see the source)");
        System.out.println();
        System.out.println("References have methods; primitives do not:");
        System.out.println("  referenceValue.toUpperCase() = " + referenceValue.toUpperCase());
        System.out.println("  primitiveValue.anything()    -> COMPILE ERROR: int cannot be dereferenced");


        /* ====================================================================
         * SECTION 7 - DEFAULT VALUES: FIELDS YES, LOCALS NO
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - DEFAULT VALUES");
        System.out.println("=".repeat(74));

        System.out.println("FIELDS are zeroed automatically when the object/class is created:");
        System.out.println("  byte    -> " + defaultByte);
        System.out.println("  short   -> " + defaultShort);
        System.out.println("  int     -> " + defaultInt);
        System.out.println("  long    -> " + defaultLong);
        System.out.println("  float   -> " + defaultFloat);
        System.out.println("  double  -> " + defaultDouble);
        System.out.println("  char    -> [" + defaultChar + "] (the null character, code "
                + (int) defaultChar + ")");
        System.out.println("  boolean -> " + defaultBoolean);
        System.out.println("  String  -> " + defaultReference + "  (references default to null)");

        System.out.println();
        System.out.println("LOCAL variables get NO default. This is a compile error:");
        System.out.println("    int uninitialised;");
        System.out.println("    System.out.println(uninitialised);");
        System.out.println("    -> error: variable uninitialised might not have been initialized");
        // int uninitialised;
        // System.out.println(uninitialised);   // uncomment to see it fail

        System.out.println();
        System.out.println("WHY the difference: fields live in heap memory the JVM zeroes on");
        System.out.println("allocation. Locals live on the stack, in memory that may contain");
        System.out.println("anything, so the compiler forces you to assign before reading.");


        /* ====================================================================
         * SECTION 8 - var: LOCAL TYPE INFERENCE (Java 10+)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - var IS STILL STATIC TYPING");
        System.out.println("=".repeat(74));

        var inferredText   = "I am a String";   // inferred as String
        var inferredNumber = 42;                // inferred as int
        var inferredDouble = 42.0;              // inferred as double

        // getClass() proves the compiler picked a real, fixed type.
        System.out.println("var inferredText   -> actual type " + inferredText.getClass().getSimpleName());
        System.out.println("var inferredNumber -> value " + inferredNumber + " (an int, fixed forever)");
        System.out.println("var inferredDouble -> value " + inferredDouble);

        // inferredNumber = "now a string";
        // ERROR: incompatible types: String cannot be converted to int
        // The type was INFERRED, not made dynamic. It is still locked in.

        System.out.println();
        System.out.println("Where var is NOT allowed (all compile errors - see the source):");
        System.out.println("  var x;                  -> no initialiser to infer from");
        System.out.println("  var y = null;           -> cannot infer a type from null");
        System.out.println("  var[] arr = {1, 2};     -> not allowed on array declarations");
        System.out.println("  class C { var f = 1; }  -> fields cannot use var");
        System.out.println("  void m(var p) { }       -> parameters cannot use var");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 03.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Make a byte overflow. Declare `byte b = 127;` then print `(byte)(b + 1)`.
 *    Predict the answer before you run it.
 *
 * 2. Uncomment `int uninitialised;` and its println in Section 7. Read the
 *    compiler's exact wording; you will see it many times in your career.
 *
 * 3. Uncomment `inferredNumber = "now a string";` in Section 8 and confirm for
 *    yourself that var is not dynamic typing.
 *
 * 4. Loop 10 times adding 0.1 to a double starting at 0.0, printing each step.
 *    Watch the error accumulate. Then do the same with BigDecimal and watch it
 *    stay exact.
 *
 * 5. Print the code point of every character in your own name:
 *        for (char c : "Danish".toCharArray()) System.out.println(c + " = " + (int) c);
 *
 * 6. Work out on paper why `Math.abs(Integer.MIN_VALUE)` returns a NEGATIVE
 *    number, then run it and confirm. (Hint: the range is asymmetric.)
 * ============================================================================
 */
