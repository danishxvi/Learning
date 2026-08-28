/* ============================================================================
 * 05 - OPERATORS, PRECEDENCE AND THE TRAPS
 * ----------------------------------------------------------------------------
 * Companion lesson: 05-operators.md
 *
 * RUN IT:
 *     java Java/02-operators-and-input/05-operators.java
 *
 * The obvious operators get one line each. The surprising ones get a whole
 * section, because those are the ones that cost you an afternoon.
 * ============================================================================
 */

class Operators {

    // Permission flags for the bitwise section. Each is a distinct single bit,
    // which is what lets them be combined into one integer without colliding.
    static final int PERMISSION_READ    = 1;   // 0b001
    static final int PERMISSION_WRITE   = 2;   // 0b010
    static final int PERMISSION_EXECUTE = 4;   // 0b100

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - ARITHMETIC, AND THE TWO THAT SURPRISE PEOPLE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - ARITHMETIC");
        System.out.println("=".repeat(74));

        int left = 17;
        int right = 5;

        System.out.println(left + " + " + right + " = " + (left + right));
        System.out.println(left + " - " + right + " = " + (left - right));
        System.out.println(left + " * " + right + " = " + (left * right));
        System.out.println(left + " / " + right + " = " + (left / right) + "    <- INTEGER division truncates");
        System.out.println(left + " % " + right + " = " + (left % right) + "    <- remainder");

        System.out.println();
        System.out.println("Force real division by making one operand a double:");
        System.out.println("  (double) 17 / 5 = " + ((double) left / right));

        // Modulo takes the sign of the LEFT operand. This differs from Python,
        // and it breaks the common "x % 2 == 1" oddness test for negatives.
        System.out.println();
        System.out.println("Modulo takes the sign of the LEFT operand:");
        System.out.println("   7 %  3 = " + (7 % 3));
        System.out.println("  -7 %  3 = " + (-7 % 3) + "   <- NOT 2. Python would say 2.");
        System.out.println("   7 % -3 = " + (7 % -3));
        System.out.println("  -7 % -3 = " + (-7 % -3));
        System.out.println("  7.5 % 2 = " + (7.5 % 2) + "  <- modulo works on doubles too");

        System.out.println();
        System.out.println("Which is why this oddness test is BROKEN for negatives:");
        int negativeOdd = -7;
        System.out.println("  " + negativeOdd + " % 2 == 1  ->  " + (negativeOdd % 2 == 1) + "   <- wrong");
        System.out.println("  " + negativeOdd + " % 2 != 0  ->  " + (negativeOdd % 2 != 0) + "    <- correct");

        // Java has no ** operator. Use Math.pow, which returns a double.
        System.out.println();
        System.out.println("Java has no power operator. Use Math.pow (returns double):");
        System.out.println("  Math.pow(2, 10) = " + Math.pow(2, 10));
        System.out.println("  as an int       = " + (int) Math.pow(2, 10));


        /* ====================================================================
         * SECTION 2 - ++ AND --, PREFIX VS POSTFIX
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - INCREMENT AND DECREMENT");
        System.out.println("=".repeat(74));

        int postfix = 5;
        System.out.println("int postfix = 5;");
        System.out.println("  System.out.println(postfix++)  prints " + postfix++
                + "   <- OLD value used, THEN incremented");
        System.out.println("  postfix is now                  " + postfix);

        int prefix = 5;
        System.out.println();
        System.out.println("int prefix = 5;");
        System.out.println("  System.out.println(++prefix)   prints " + ++prefix
                + "   <- incremented FIRST, then used");
        System.out.println("  prefix is now                   " + prefix);

        System.out.println();
        System.out.println("As a statement on its own line they are IDENTICAL:");
        int standalone = 5;
        standalone++;
        System.out.println("  standalone++;  ->  " + standalone);
        standalone = 5;
        ++standalone;
        System.out.println("  ++standalone;  ->  " + standalone);

        /* --------------------------------------------------------------------
         * THE CLASSIC TRAP. i = i++ leaves i unchanged. Order of evaluation:
         *   1. i++ produces the CURRENT value (0) as its result
         *   2. i is incremented to 1
         *   3. the assignment writes the saved 0 back over it
         * The assignment happens LAST and wipes out the increment.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("THE TRAP - i = i++ does nothing:");
        int trapped = 0;
        trapped = trapped++;
        System.out.println("  int i = 0; i = i++;  ->  i is " + trapped + "   <- still zero!");
        System.out.println("  Step 1: i++ yields 0.  Step 2: i becomes 1.");
        System.out.println("  Step 3: the assignment writes the saved 0 back. Increment lost.");

        System.out.println();
        System.out.println("Mixing them in one expression - evaluate strictly left to right:");
        int mixed = 5;
        int mixedResult = mixed++ + ++mixed;
        System.out.println("  int x = 5; int r = x++ + ++x;");
        System.out.println("    x++ yields 5, x becomes 6");
        System.out.println("    ++x makes x 7 and yields 7");
        System.out.println("    r = 5 + 7 = " + mixedResult + ", and x ends at " + mixed);
        System.out.println("  ADVICE: never put ++ or -- inside a bigger expression.");


        /* ====================================================================
         * SECTION 3 - SHORT-CIRCUIT EVALUATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - && AND || SHORT-CIRCUIT; & AND | DO NOT");
        System.out.println("=".repeat(74));

        System.out.println("Using && - the right side is skipped when the left is false:");
        boolean andResult = returnsFalse() && returnsTrue();
        System.out.println("  result = " + andResult);

        System.out.println();
        System.out.println("Using & - BOTH sides always run:");
        boolean nonShortCircuitAnd = returnsFalse() & returnsTrue();
        System.out.println("  result = " + nonShortCircuitAnd);

        System.out.println();
        System.out.println("Using || - the right side is skipped when the left is true:");
        boolean orResult = returnsTrue() || returnsFalse();
        System.out.println("  result = " + orResult);

        /* --------------------------------------------------------------------
         * Short-circuiting is a CORRECTNESS tool, not just an optimisation.
         * The standard null-guard idiom only works because of it.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("Short-circuiting is what makes the null-guard idiom work:");
        String nothing = null;

        // SAFE: if nothing == null is false... wait, it is TRUE, so && stops
        // and length() is never called.
        if (nothing != null && nothing.length() > 0) {
            System.out.println("  never reached");
        } else {
            System.out.println("  (nothing != null && nothing.length() > 0)  ->  safe, no NPE");
        }

        // The same test with a single & would evaluate nothing.length() and
        // throw. Proving it without crashing the lesson:
        try {
            boolean unsafe = (nothing != null) & (nothing.length() > 0);
            System.out.println("  unreachable: " + unsafe);
        } catch (NullPointerException e) {
            System.out.println("  (nothing != null &  nothing.length() > 0)  ->  NullPointerException");
        }

        // Side effects can silently not happen. Worth knowing before you put
        // a method call with consequences on the right-hand side.
        int sideEffectCounter = 0;
        boolean skipped = false && (sideEffectCounter++ > 0);
        System.out.println();
        System.out.println("Side effects on the skipped side never run:");
        System.out.println("  after `false && (counter++ > 0)`, counter is still " + sideEffectCounter
                + "   (result was " + skipped + ")");


        /* ====================================================================
         * SECTION 4 - == COMPARES REFERENCES, NOT CONTENTS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - == VS .equals()");
        System.out.println("=".repeat(74));

        // For PRIMITIVES, == compares values and is exactly right.
        int primitiveOne = 100;
        int primitiveTwo = 100;
        System.out.println("Primitives - == compares VALUES, which is correct:");
        System.out.println("  int 100 == int 100  ->  " + (primitiveOne == primitiveTwo));

        // For OBJECTS, == compares identity: "the same object?"
        String literalOne = "hello";
        String literalTwo = "hello";                 // reuses the pooled object
        String constructed = new String("hello");    // forces a NEW heap object

        System.out.println();
        System.out.println("Strings - == compares IDENTITY, which is usually not what you want:");
        System.out.println("  \"hello\" == \"hello\"                 ->  " + (literalOne == literalTwo)
                + "    <- both point at the same POOLED object");
        System.out.println("  \"hello\" == new String(\"hello\")     ->  " + (literalOne == constructed)
                + "   <- different objects, equal text");
        System.out.println("  \"hello\".equals(new String(\"hello\")) ->  " + literalOne.equals(constructed)
                + "    <- .equals() compares CONTENT");

        // intern() puts a string into the pool, making == work again - but
        // relying on this is fragile. Just use .equals().
        System.out.println("  constructed.intern() == \"hello\"     ->  " + (constructed.intern() == literalOne));

        /* --------------------------------------------------------------------
         * THE INTEGER CACHE TRAP. Java caches Integer objects for -128..127.
         * Inside that range autoboxing reuses one object, so == appears to
         * work. Outside it, new objects are made and == fails. Code passes
         * testing with small numbers and breaks in production with large ones.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("THE INTEGER CACHE TRAP (-128 to 127 are cached):");
        Integer smallOne = 127;
        Integer smallTwo = 127;
        Integer bigOne = 128;
        Integer bigTwo = 128;

        System.out.println("  Integer 127 == Integer 127  ->  " + (smallOne == smallTwo)
                + "    <- same cached object");
        System.out.println("  Integer 128 == Integer 128  ->  " + (bigOne == bigTwo)
                + "   <- OUTSIDE the cache: different objects");
        System.out.println("  Integer 128 .equals( 128 )  ->  " + bigOne.equals(bigTwo)
                + "    <- always use .equals() on wrappers");


        /* ====================================================================
         * SECTION 5 - THE TERNARY OPERATOR
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE TERNARY OPERATOR");
        System.out.println("=".repeat(74));

        int scoreA = 87;
        int scoreB = 92;

        // Read as: "if (condition) value-if-true else value-if-false"
        int higher = (scoreA > scoreB) ? scoreA : scoreB;
        System.out.println("  higher of " + scoreA + " and " + scoreB + " = " + higher);

        // Its best use: choosing between two values inline, especially for text.
        int itemCount = 1;
        System.out.println("  " + itemCount + " " + ((itemCount == 1) ? "item" : "items"));
        itemCount = 3;
        System.out.println("  " + itemCount + " " + ((itemCount == 1) ? "item" : "items"));

        // Nesting works because ternaries are RIGHT-associative, but it reads
        // badly. An if/else-if chain or a switch is almost always clearer.
        int examScore = 85;
        String grade = examScore >= 90 ? "A"
                     : examScore >= 80 ? "B"
                     : examScore >= 70 ? "C"
                     : "F";
        System.out.println();
        System.out.println("  Nested ternary for score " + examScore + " gives grade " + grade);
        System.out.println("  It works - but use if/else or switch for anything this long.");

        // THE AUTOBOXING TRAP: if the branches have different types, Java finds
        // a common type and may UNBOX in the process, turning a harmless null
        // into a NullPointerException.
        System.out.println();
        System.out.println("THE TERNARY AUTOBOXING TRAP:");
        try {
            boolean condition = false;
            Integer boxedResult = condition ? 1 : nullInteger();
            System.out.println("  got " + boxedResult);
        } catch (NullPointerException e) {
            System.out.println("  `condition ? 1 : someNullInteger` threw NullPointerException");
            System.out.println("  Reason: the int branch forces UNBOXING of the null branch.");
            System.out.println("  Fix: make both branches the same type.");
        }


        /* ====================================================================
         * SECTION 6 - BITWISE AND SHIFT OPERATORS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - BITWISE OPERATORS");
        System.out.println("=".repeat(74));

        int bitsA = 0b1100;   // 12
        int bitsB = 0b1010;   // 10

        System.out.printf("  a        = %2d  = %4s%n", bitsA, Integer.toBinaryString(bitsA));
        System.out.printf("  b        = %2d  = %4s%n", bitsB, Integer.toBinaryString(bitsB));
        System.out.printf("  a & b    = %2d  = %4s   (1 only where BOTH are 1)%n",
                bitsA & bitsB, Integer.toBinaryString(bitsA & bitsB));
        System.out.printf("  a | b    = %2d  = %4s   (1 where EITHER is 1)%n",
                bitsA | bitsB, Integer.toBinaryString(bitsA | bitsB));
        System.out.printf("  a ^ b    = %2d  = %4s   (1 where they DIFFER)%n",
                bitsA ^ bitsB, Integer.toBinaryString(bitsA ^ bitsB));
        System.out.println("  ~a       = " + (~bitsA) + "        (flips all 32 bits)");
        System.out.println("  Note: ~x is always -x - 1, because of two's complement.");

        System.out.println();
        System.out.println("Shifts:");
        System.out.println("   5 << 1  = " + (5 << 1)   + "            multiply by 2");
        System.out.println("   5 << 3  = " + (5 << 3)   + "            multiply by 8 (2^3)");
        System.out.println("  20 >> 2  = " + (20 >> 2)  + "             divide by 4");
        System.out.println(" -20 >> 2  = " + (-20 >> 2) + "            SIGNED shift fills with the sign bit");
        System.out.println(" -20 >>> 2 = " + (-20 >>> 2) + "   UNSIGNED shift fills with zeros");
        System.out.println("  There is no <<< because left shift always fills with zeros anyway.");

        System.out.println();
        System.out.println("Shift distance wraps (mod 32 for int, mod 64 for long):");
        System.out.println("  1 << 32  = " + (1 << 32) + "   <- NOT 0. 32 mod 32 is 0, so this is 1 << 0.");
        System.out.println("  1 << 33  = " + (1 << 33) + "   <- same as 1 << 1");

        /* --------------------------------------------------------------------
         * WHERE BITWISE OPERATORS ACTUALLY EARN THEIR KEEP: flag sets.
         * This is how Unix file permissions and java.lang.reflect.Modifier work.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("Real use - packing many booleans into one int (a flag set):");

        int permissions = PERMISSION_READ | PERMISSION_WRITE;   // combine with OR
        System.out.println("  permissions = READ | WRITE      = " + describePermissions(permissions));

        // Test a flag with AND. The result is non-zero if the bit is set.
        System.out.println("  can write?  (perms & WRITE) != 0 = " + ((permissions & PERMISSION_WRITE) != 0));
        System.out.println("  can exec?   (perms & EXEC)  != 0 = " + ((permissions & PERMISSION_EXECUTE) != 0));

        permissions |= PERMISSION_EXECUTE;    // ADD a flag
        System.out.println("  after |= EXECUTE                = " + describePermissions(permissions));

        permissions &= ~PERMISSION_WRITE;     // REMOVE a flag (AND with the complement)
        System.out.println("  after &= ~WRITE                 = " + describePermissions(permissions));

        permissions ^= PERMISSION_READ;       // TOGGLE a flag
        System.out.println("  after ^= READ (toggle)          = " + describePermissions(permissions));

        System.out.println();
        System.out.println("Bit tricks (write the readable version unless you have measured):");
        int number = 42;
        System.out.println("  " + number + " is even?  (n & 1) == 0  ->  " + ((number & 1) == 0));
        System.out.println("  " + number + " halved:   n >> 1        ->  " + (number >> 1));
        System.out.println("  Integer.bitCount(" + number + ")            ->  "
                + Integer.bitCount(number) + "   (how many 1 bits)");


        /* ====================================================================
         * SECTION 7 - PRECEDENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - PRECEDENCE");
        System.out.println("=".repeat(74));

        System.out.println("  2 + 3 * 4       = " + (2 + 3 * 4)   + "   (* binds tighter than +)");
        System.out.println("  (2 + 3) * 4     = " + ((2 + 3) * 4) + "   (parentheses win)");

        System.out.println();
        System.out.println("The one that genuinely catches people - & binds TIGHTER than ==:");
        int flags = 0b0110;
        int mask  = 0b0100;
        // if (flags & mask == 0)     // parses as flags & (mask == 0) -> COMPILE ERROR
        System.out.println("  if (flags & mask == 0)    -> parses as flags & (mask == 0): COMPILE ERROR");
        System.out.println("  if ((flags & mask) == 0)  -> what you meant: " + ((flags & mask) == 0));

        System.out.println();
        System.out.println("Assignment is RIGHT-associative, so this chains:");
        int chainA, chainB, chainC;
        chainA = chainB = chainC = 7;
        System.out.println("  a = b = c = 7;  ->  a=" + chainA + " b=" + chainB + " c=" + chainC);

        System.out.println();
        System.out.println("ADVICE: do not memorise the precedence table. Memorise that");
        System.out.println("* / % beat + -, and that parentheses cost nothing.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 05.");
        System.out.println("=".repeat(74));
    }

    /**
     * Prints a marker when called, so short-circuit evaluation becomes visible.
     *
     * @return always false
     */
    static boolean returnsFalse() {
        System.out.println("    [returnsFalse() was called]");
        return false;
    }

    /**
     * Prints a marker when called, so short-circuit evaluation becomes visible.
     *
     * @return always true
     */
    static boolean returnsTrue() {
        System.out.println("    [returnsTrue() was called]");
        return true;
    }

    /**
     * Returns a null Integer. Used to demonstrate the ternary unboxing trap;
     * writing `null` inline would let the compiler infer a different type.
     *
     * @return null
     */
    static Integer nullInteger() {
        return null;
    }

    /**
     * Renders a permission bit set as readable text.
     *
     * @param permissions a combination of the PERMISSION_* flags
     * @return a string such as "READ|WRITE (3)"
     */
    static String describePermissions(int permissions) {
        StringBuilder result = new StringBuilder();
        if ((permissions & PERMISSION_READ) != 0)    result.append("READ|");
        if ((permissions & PERMISSION_WRITE) != 0)   result.append("WRITE|");
        if ((permissions & PERMISSION_EXECUTE) != 0) result.append("EXECUTE|");
        if (result.length() == 0) result.append("NONE|");
        result.setLength(result.length() - 1);   // drop the trailing separator
        return result + " (" + permissions + ")";
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Predict each, then run it:
 *        int i = 1; System.out.println(i++ + i++ + i++);
 *        int j = 1; System.out.println(++j + ++j + ++j);
 *
 * 2. Write `isOdd(int n)` that is correct for negative numbers too. Test it
 *    with -7, -1, 0, 1, 7.
 *
 * 3. Add a PERMISSION_DELETE = 8 flag. Then write `hasAll(int perms, int required)`
 *    that returns true only when EVERY required bit is present.
 *    (Hint: (perms & required) == required)
 *
 * 4. Explain why `Integer a = 1000, b = 1000; a == b` is false, but the same
 *    code with 100 is true. Then fix the comparison.
 *
 * 5. Swap two ints without a temporary variable using ^=. Then explain why you
 *    would never ship that code.
 *
 * 6. Uncomment `if (flags & mask == 0)` in Section 7 and read the compiler
 *    error. It is one of the least obvious messages javac produces.
 * ============================================================================
 */
