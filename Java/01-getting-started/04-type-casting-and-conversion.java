/* ============================================================================
 * 04 - TYPE CASTING, PROMOTION AND CONVERSION
 * ----------------------------------------------------------------------------
 * Companion lesson: 04-type-casting-and-conversion.md
 *
 * RUN IT:
 *     java Java/01-getting-started/04-type-casting-and-conversion.java
 *
 * Three separate mechanisms are demonstrated, in order:
 *   1. WIDENING  - automatic, but not always lossless
 *   2. NARROWING - explicit cast, truncates or discards bits
 *   3. PARSING   - String to number and back, using library methods
 * ============================================================================
 */

class TypeCastingAndConversion {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - WIDENING: byte -> short -> int -> long -> float -> double
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WIDENING (automatic, no cast needed)");
        System.out.println("=".repeat(74));

        byte   startingByte = 42;
        short  asShort      = startingByte;   // each of these assignments is
        int    asInt        = asShort;        // automatic: the target type is
        long   asLong       = asInt;          // strictly larger, so nothing can
        float  asFloat      = asLong;         // overflow
        double asDouble     = asFloat;

        System.out.println("byte   42 climbs the widening ladder with no casts:");
        System.out.println("  short  -> " + asShort);
        System.out.println("  int    -> " + asInt);
        System.out.println("  long   -> " + asLong);
        System.out.println("  float  -> " + asFloat);
        System.out.println("  double -> " + asDouble);

        /* --------------------------------------------------------------------
         * TRAP 1: widening is NOT always lossless.
         * A long has 64 bits of integer precision. A float has ~24 bits of
         * mantissa. The conversion is automatic, so nothing warns you - but
         * digits ARE lost. Widening preserves MAGNITUDE, not every digit.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("TRAP - automatic does not mean lossless:");
        long   preciseLong = 123_456_789_123_456_789L;
        float  lossyFloat  = preciseLong;     // automatic, and wrong
        double saferDouble = preciseLong;     // automatic, still not exact

        System.out.println("  original long      = " + preciseLong);
        System.out.println("  widened to float   = " + lossyFloat  + "   <- digits gone");
        System.out.println("  widened to double  = " + saferDouble + "   <- better, still not exact");
        System.out.println("  back from float    = " + (long) lossyFloat);

        /* --------------------------------------------------------------------
         * TRAP 2: char and short do NOT convert to each other automatically.
         * Both are 16 bits, but char is UNSIGNED (0..65535) and short is
         * SIGNED (-32768..32767). Neither range fits inside the other.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("TRAP - char and short are both 16 bits but do NOT interconvert:");
        char sampleChar = 'A';
        // short fromChar = sampleChar;   // ERROR: possible lossy conversion
        short fromChar = (short) sampleChar;   // explicit cast required
        System.out.println("  char 'A' -> short needs a cast -> " + fromChar);
        System.out.println("  Reason: char is unsigned 0..65535, short is signed -32768..32767.");
        System.out.println("  char joins the widening ladder at int, not at short.");


        /* ====================================================================
         * SECTION 2 - NARROWING: THE EXPLICIT CAST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - NARROWING (explicit cast, data can be lost)");
        System.out.println("=".repeat(74));

        // 2a. double -> int TRUNCATES toward zero. It does NOT round.
        System.out.println("Truncation, not rounding:");
        System.out.println("  (int)  9.99  = " + (int) 9.99   + "    <- not 10");
        System.out.println("  (int)  9.01  = " + (int) 9.01);
        System.out.println("  (int) -9.99  = " + (int) -9.99  + "   <- toward zero, not toward -infinity");

        System.out.println();
        System.out.println("To round, use Math - and note what each one returns:");
        System.out.println("  Math.round(9.99)  = " + Math.round(9.99)  + "    (returns long)");
        System.out.println("  Math.round(9.49)  = " + Math.round(9.49));
        System.out.println("  Math.round(-9.5)  = " + Math.round(-9.5)  + "    (rounds toward +infinity on .5)");
        System.out.println("  Math.floor(9.99)  = " + Math.floor(9.99)  + "   (returns double)");
        System.out.println("  Math.ceil(9.01)   = " + Math.ceil(9.01)   + "  (returns double)");

        // 2b. int -> byte DISCARDS the high bits. This is the dangerous one.
        System.out.println();
        System.out.println("Integer narrowing keeps only the low bits:");
        int threeHundred = 300;
        byte narrowed = (byte) threeHundred;
        System.out.println("  int 300 in binary        = " + Integer.toBinaryString(threeHundred));
        System.out.println("  (byte) 300               = " + narrowed);
        System.out.println("  why: a byte keeps only the low 8 bits, 00101100 = 44");
        System.out.println("  NOTHING warns you. The cast made it look deliberate.");

        // 2c. Floating point to integer SATURATES rather than wrapping.
        System.out.println();
        System.out.println("double -> int saturates (clamps) instead of wrapping:");
        System.out.println("  (int) 1e20            = " + (int) 1e20 + "   <- clamped to Integer.MAX_VALUE");
        System.out.println("  (int) -1e20           = " + (int) -1e20);
        System.out.println("  (int) Double.NaN      = " + (int) Double.NaN + "            <- NaN becomes zero");


        /* ====================================================================
         * SECTION 3 - NUMERIC PROMOTION IN EXPRESSIONS
         * --------------------------------------------------------------------
         * THE RULE: operands smaller than int are promoted to int before any
         * arithmetic. If either operand is larger, both promote to the largest
         * type present. Order: int -> long -> float -> double.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - NUMERIC PROMOTION");
        System.out.println("=".repeat(74));

        byte firstByte = 10;
        byte secondByte = 20;

        // byte sum = firstByte + secondByte;
        // ERROR: incompatible types: possible lossy conversion from int to byte
        // Both operands are byte, yet the RESULT is an int.

        int promotedSum = firstByte + secondByte;      // fine: int holds an int
        byte castBackSum = (byte) (firstByte + secondByte);  // fine: explicit cast

        System.out.println("byte + byte produces an INT, not a byte:");
        System.out.println("  10 + 20 as int          = " + promotedSum);
        System.out.println("  10 + 20 cast to byte    = " + castBackSum);

        // 3a. Integer division truncates. This is THE classic bug.
        System.out.println();
        System.out.println("Integer division truncates - cast an OPERAND, not the result:");
        System.out.println("  5 / 2              = " + (5 / 2)              + "      both operands are int");
        System.out.println("  5.0 / 2            = " + (5.0 / 2)            + "    one is double, both promote");
        System.out.println("  5 / 2.0            = " + (5 / 2.0)            + "    same thing");
        System.out.println("  (double) 5 / 2     = " + ((double) 5 / 2)     + "    cast an OPERAND: correct");
        System.out.println("  (double) (5 / 2)   = " + ((double) (5 / 2))   + "    cast the RESULT: TOO LATE");

        // 3b. The percentage bug, which this rule causes constantly.
        System.out.println();
        int tasksDone = 3;
        int tasksTotal = 10;
        double wrongPercent = tasksDone / tasksTotal * 100;
        double rightPercent = (double) tasksDone / tasksTotal * 100;
        System.out.println("Percentage of " + tasksDone + " out of " + tasksTotal + ":");
        System.out.println("  done / total * 100            = " + wrongPercent + "     <- WRONG (3/10 is 0)");
        System.out.println("  (double) done / total * 100   = " + rightPercent + "    <- correct");

        // 3c. Modulo follows the same promotion rules and keeps the sign of the
        //     LEFT operand - which surprises people coming from Python.
        System.out.println();
        System.out.println("Modulo keeps the sign of the left operand:");
        System.out.println("   7 %  3 = " + (7 % 3));
        System.out.println("  -7 %  3 = " + (-7 % 3) + "   <- negative, unlike Python");
        System.out.println("   7 % -3 = " + (7 % -3));
        System.out.println("   7.5 % 2 = " + (7.5 % 2) + "  <- modulo works on doubles too");


        /* ====================================================================
         * SECTION 4 - THE COMPOUND-ASSIGNMENT LOOPHOLE
         * --------------------------------------------------------------------
         * +=, -=, *=, /=, %= all contain a HIDDEN implicit cast.
         *     b += 5    is defined as    b = (byte)(b + 5)
         * Convenient, and a silent-overflow trap.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - COMPOUND ASSIGNMENT HIDES A CAST");
        System.out.println("=".repeat(74));

        byte counter = 10;
        // counter = counter + 5;   // ERROR: possible lossy conversion from int to byte
        counter += 5;               // FINE - the hidden cast makes it legal
        System.out.println("byte counter = 10; counter += 5;  ->  " + counter);
        System.out.println("  but  counter = counter + 5;  is a COMPILE ERROR.");
        System.out.println("  Reason: += is defined as  counter = (byte)(counter + 5)");

        System.out.println();
        System.out.println("Which means += can overflow silently:");
        byte atLimit = 127;
        atLimit += 1;
        System.out.println("  byte atLimit = 127; atLimit += 1;  ->  " + atLimit + "   <- wrapped, no warning");

        System.out.println();
        System.out.println("And it silently truncates doubles into ints:");
        int wholeNumber = 5;
        wholeNumber += 3.9;         // legal! means wholeNumber = (int)(5 + 3.9)
        System.out.println("  int wholeNumber = 5; wholeNumber += 3.9;  ->  " + wholeNumber
                + "   <- 8.9 truncated to 8");


        /* ====================================================================
         * SECTION 5 - char ARITHMETIC AND STRING CONCATENATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - char ARITHMETIC AND THE + OPERATOR");
        System.out.println("=".repeat(74));

        char letter = 'A';
        System.out.println("char letter = 'A';");
        System.out.println("  letter + 1            = " + (letter + 1)         + "     <- int, because + promotes");
        System.out.println("  (char)(letter + 1)    = " + (char) (letter + 1)  + "      <- cast back to see it");
        System.out.println("  \"\" + letter + 1       = " + "" + letter + 1
                + "     <- a String appeared, so + means CONCATENATE");

        System.out.println();
        System.out.println("Once a String is involved, + is left-to-right concatenation:");
        System.out.println("  1 + 2 + \"3\"    = " + (1 + 2 + "3")   + "    (1+2 first, then concat)");
        System.out.println("  \"1\" + 2 + 3    = " + ("1" + 2 + 3)   + "   (concat, then concat)");
        System.out.println("  \"1\" + (2 + 3)  = " + ("1" + (2 + 3)) + "    (parentheses force arithmetic first)");


        /* ====================================================================
         * SECTION 6 - REFERENCE CASTING: UPCAST AND DOWNCAST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - OBJECT CASTING");
        System.out.println("=".repeat(74));

        // UPCAST: subclass -> superclass. Implicit, always safe.
        Object asObject = "I am really a String";
        System.out.println("Upcast (String -> Object): implicit, can never fail.");

        // DOWNCAST: superclass -> subclass. Explicit, checked at RUNTIME.
        String backToString = (String) asObject;
        System.out.println("Downcast (Object -> String): explicit, verified at runtime.");
        System.out.println("  recovered value = " + backToString);

        // A downcast that is wrong compiles fine and fails at runtime.
        Object actuallyAnInteger = Integer.valueOf(42);
        try {
            String impossible = (String) actuallyAnInteger;
            System.out.println(impossible);   // never reached
        } catch (ClassCastException e) {
            System.out.println();
            System.out.println("A wrong downcast COMPILES but throws at runtime:");
            System.out.println("  " + e.getMessage());
        }

        // The safe modern pattern: pattern matching for instanceof (Java 16+).
        // It tests AND declares the variable in one step, and it is null-safe.
        System.out.println();
        System.out.println("The safe way - pattern matching for instanceof (Java 16+):");
        describe("a piece of text");
        describe(42);
        describe(3.14);
        describe(null);


        /* ====================================================================
         * SECTION 7 - STRING AND NUMBER CONVERSION
         * --------------------------------------------------------------------
         * Casting does NOT work here. (int) "42" is a compile error.
         * You need library methods.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - STRING <-> NUMBER");
        System.out.println("=".repeat(74));

        // String -> number (PARSING)
        int     parsedInt     = Integer.parseInt("42");
        long    parsedLong    = Long.parseLong("9000000000");
        double  parsedDouble  = Double.parseDouble("3.14");
        boolean parsedBoolean = Boolean.parseBoolean("true");
        int     parsedHex     = Integer.parseInt("FF", 16);   // with a radix

        System.out.println("Parsing text into numbers:");
        System.out.println("  Integer.parseInt(\"42\")        = " + parsedInt);
        System.out.println("  Long.parseLong(\"9000000000\")  = " + parsedLong);
        System.out.println("  Double.parseDouble(\"3.14\")    = " + parsedDouble);
        System.out.println("  Boolean.parseBoolean(\"true\")  = " + parsedBoolean);
        System.out.println("  Integer.parseInt(\"FF\", 16)    = " + parsedHex + "   (radix 16)");

        // Every one of these throws NumberFormatException - an UNCHECKED
        // exception, so the compiler will never remind you to handle it.
        System.out.println();
        System.out.println("Bad input throws NumberFormatException (unchecked - no compiler help):");
        tryParse("abc");
        tryParse("");
        tryParse(" 42 ");     // whitespace is NOT trimmed for you
        tryParse("42.0");     // a decimal point is not valid for parseInt
        tryParse(null);       // NumberFormatException, NOT NullPointerException

        // number -> String
        System.out.println();
        System.out.println("Numbers into text:");
        System.out.println("  String.valueOf(42)             = " + String.valueOf(42));
        System.out.println("  Integer.toString(42)           = " + Integer.toString(42));
        System.out.println("  42 + \"\"                        = " + (42 + ""));
        System.out.println("  String.format(\"%.2f\", 3.14159) = " + String.format("%.2f", 3.14159));
        System.out.println("  Integer.toBinaryString(255)    = " + Integer.toBinaryString(255));
        System.out.println("  Integer.toHexString(255)       = " + Integer.toHexString(255));

        System.out.println();
        System.out.println("Prefer String.valueOf - it is null-safe:");
        Object nothing = null;
        System.out.println("  String.valueOf(null reference) = " + String.valueOf(nothing));
        System.out.println("  nullReference.toString()       -> NullPointerException");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 04.");
        System.out.println("=".repeat(74));
    }

    /**
     * Demonstrates pattern matching for instanceof (Java 16+). The test and the
     * variable declaration happen together, and instanceof is false for null,
     * so this is null-safe without an extra check.
     *
     * @param value any object, including null
     */
    static void describe(Object value) {
        if (value instanceof String text) {
            // `text` is already a String here - no separate cast line needed.
            System.out.println("  String of length " + text.length() + ": \"" + text + "\"");
        } else if (value instanceof Integer number) {
            System.out.println("  Integer: " + number + ", doubled is " + (number * 2));
        } else if (value instanceof Double decimal) {
            System.out.println("  Double: " + decimal);
        } else {
            System.out.println("  null or an unrecognised type (instanceof is false for null)");
        }
    }

    /**
     * Attempts to parse text as an int and reports what happened, so the
     * failure modes of parseInt are visible rather than fatal.
     *
     * @param input the text to parse; may be null
     */
    static void tryParse(String input) {
        String shown = (input == null) ? "null" : "\"" + input + "\"";
        try {
            int result = Integer.parseInt(input);
            System.out.println("  parseInt(" + shown + ") = " + result);
        } catch (NumberFormatException e) {
            System.out.println("  parseInt(" + shown + ") threw NumberFormatException: " + e.getMessage());
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Predict, then verify: what does `(byte) 128` print? And `(byte) 256`?
 *
 * 2. Fix this so it prints 33.33 rather than 0.0:
 *        int hits = 1, attempts = 3;
 *        System.out.println(hits / attempts * 100);
 *
 * 3. Explain why `Math.round(-9.5)` is -9 but `Math.round(9.5)` is 10.
 *    (Hint: Math.round is defined as floor(x + 0.5).)
 *
 * 4. Write a method `safeParse(String s, int fallback)` that returns the parsed
 *    int, or `fallback` if the text is null, blank or not a number. Trim first.
 *
 * 5. Uncomment `short fromChar = sampleChar;` in Section 1 and read the error.
 *    Then work out why `int fromChar = sampleChar;` needs no cast at all.
 *
 * 6. Start with `byte b = 0;` and run `b += 1;` inside a loop 200 times,
 *    printing b each time. Watch it wrap at 127. Then try `b = b + 1;` and see
 *    the compiler stop you instead.
 * ============================================================================
 */
