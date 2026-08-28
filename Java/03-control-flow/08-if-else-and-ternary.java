/* ============================================================================
 * 08 - if / else AND THE TERNARY OPERATOR
 * ----------------------------------------------------------------------------
 * Companion lesson: 08-if-else-and-ternary.md
 *
 * RUN IT:
 *     java Java/03-control-flow/08-if-else-and-ternary.java
 *
 * The syntax takes five minutes. This file spends its time on the mistakes
 * that survive into production: dangling statements, == on objects, stray
 * semicolons, and conditions that are correct but unreadable.
 * ============================================================================
 */

import java.util.Objects;

class IfElseAndTernary {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE CONDITION MUST BE A boolean
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - NO TRUTHINESS IN JAVA");
        System.out.println("=".repeat(74));

        int count = 0;
        String text = "";

        // if (count) { }        // ERROR: int cannot be converted to boolean
        // if (text) { }         // ERROR: String cannot be converted to boolean
        // In JavaScript or Python both of these would compile and mean
        // "is it non-zero / non-empty". Java forces you to say which you mean.

        System.out.println("These are COMPILE ERRORS in Java (see the source):");
        System.out.println("  if (count)   -> int cannot be converted to boolean");
        System.out.println("  if (text)    -> String cannot be converted to boolean");
        System.out.println();
        System.out.println("You must state the comparison explicitly:");

        if (count != 0) {
            System.out.println("  count is non-zero");
        } else {
            System.out.println("  count != 0    ->  false, so count is zero");
        }

        if (!text.isEmpty()) {
            System.out.println("  text has content");
        } else {
            System.out.println("  !text.isEmpty() ->  false, so text is empty");
        }

        System.out.println();
        System.out.println("The verbosity is deliberate: there is exactly one meaning for");
        System.out.println("each test, so 0, \"\", empty list and null cannot be confused.");

        /* --------------------------------------------------------------------
         * THE ONE ACCIDENTAL-ASSIGNMENT CASE JAVA STILL ALLOWS.
         * `if (x = 5)` is a compile error because 5 is not a boolean. But with
         * a boolean variable, `=` instead of `==` compiles happily.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("THE = vs == TRAP (compiles fine with booleans):");

        boolean isReady = false;
        if (isReady = true) {          // ASSIGNS true, then tests it. Always true.
            System.out.println("  `if (isReady = true)` ran even though isReady was false");
            System.out.println("  because = ASSIGNED true and then tested the result.");
        }
        System.out.println("  isReady is now " + isReady + " - the condition changed it!");
        System.out.println();
        System.out.println("  Defence: never write `== true` at all.");
        System.out.println("  `if (isReady)` is shorter AND immune to this typo.");


        /* ====================================================================
         * SECTION 2 - BRACES AND THE DANGLING STATEMENT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - ALWAYS USE BRACES");
        System.out.println("=".repeat(74));

        int value = -5;

        System.out.println("Without braces, only the FIRST statement belongs to the if:");
        System.out.println("  value is " + value + " (negative), so nothing should print...");

        if (value > 0)
            System.out.println("    [inside the if] value is positive");
            System.out.println("    [NOT inside the if] this ALWAYS runs - the indent lied");

        System.out.println();
        System.out.println("  That is the shape of Apple's 'goto fail' TLS bug from 2014.");
        System.out.println("  RULE: always use braces, even for one line.");

        System.out.println();
        System.out.println("With braces, the behaviour matches the indentation:");
        if (value > 0) {
            System.out.println("    value is positive");
            System.out.println("    both of these are inside the if");
        } else {
            System.out.println("    value is not positive - correct, nothing above ran");
        }

        /* --------------------------------------------------------------------
         * THE STRAY SEMICOLON. `if (cond);` is a complete, EMPTY if statement.
         * The block that follows is then unconditional.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("THE STRAY SEMICOLON TRAP:");
        if (value > 0);      // <- this semicolon ends the if. It does nothing.
        {
            System.out.println("  This block ran even though value is " + value + ".");
            System.out.println("  `if (cond);` is a complete EMPTY statement; the braces");
            System.out.println("  below it are just an ordinary block. No warning by default.");
        }


        /* ====================================================================
         * SECTION 3 - DANGLING else
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - else BINDS TO THE NEAREST UNMATCHED if");
        System.out.println("=".repeat(74));

        System.out.println("The indentation below suggests else belongs to the OUTER if.");
        System.out.println("It does not. Watch:");
        System.out.println();
        System.out.println("  a = true, b = false:");
        danglingElse(true, false);
        System.out.println("  a = false, b = true:");
        danglingElse(false, true);
        System.out.println();
        System.out.println("  The else ran for (true, false), NOT for (false, true).");
        System.out.println("  It binds to `if (b)`, the nearest unmatched if.");
        System.out.println("  Braces are the only real fix.");


        /* ====================================================================
         * SECTION 4 - COMPARING CORRECTLY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - == VS .equals() IN CONDITIONS");
        System.out.println("=".repeat(74));

        // Primitives: == is exactly right.
        int age = 18;
        System.out.println("Primitives - == compares values:");
        if (age == 18) {
            System.out.println("  age == 18  ->  true. Correct and idiomatic.");
        }

        // Floating point: == is wrong, for the reasons in lesson 03.
        System.out.println();
        System.out.println("Floating point - == is wrong:");
        if (0.1 + 0.2 == 0.3) {
            System.out.println("  unreachable");
        } else {
            System.out.println("  0.1 + 0.2 == 0.3  ->  FALSE. Never == doubles.");
        }
        if (Math.abs((0.1 + 0.2) - 0.3) < 1e-9) {
            System.out.println("  Math.abs(diff) < 1e-9  ->  true. Compare with a tolerance.");
        }

        /* --------------------------------------------------------------------
         * Objects: == compares identity. The bug is subtle because it WORKS
         * with literals in your test and FAILS with input read at runtime.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("Objects - == compares identity, not content:");

        String literal = "yes";
        // Built at runtime, so it is a distinct object even though the text matches.
        String fromInput = new StringBuilder("y").append("es").toString();

        System.out.println("  Both variables hold the text \"yes\".");
        System.out.println("  literal == fromInput        ->  " + (literal == fromInput)
                + "   <- different objects");
        System.out.println("  literal.equals(fromInput)   ->  " + literal.equals(fromInput)
                + "    <- same content. This is the one you want.");
        System.out.println();
        System.out.println("  This is why == on Strings 'works' in tests with literals");
        System.out.println("  and fails on real input from a Scanner, a file or a network.");

        // Null safety: the Yoda condition and Objects.equals.
        System.out.println();
        System.out.println("Surviving null:");
        String missing = null;

        try {
            if (missing.equals("yes")) {
                System.out.println("  unreachable");
            }
        } catch (NullPointerException e) {
            System.out.println("  missing.equals(\"yes\")     ->  NullPointerException");
        }

        // Put the literal first. A literal is never null, so this cannot throw.
        System.out.println("  \"yes\".equals(missing)     ->  " + "yes".equals(missing)
                + "   <- safe (a 'Yoda condition')");
        System.out.println("  Objects.equals(missing, \"yes\") ->  " + Objects.equals(missing, "yes")
                + "   <- safe in both directions");
        System.out.println("  Objects.equals(null, null)     ->  " + Objects.equals(null, null)
                + "    <- and two nulls are equal");


        /* ====================================================================
         * SECTION 5 - GUARD CLAUSES BEAT NESTING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - GUARD CLAUSES");
        System.out.println("=".repeat(74));

        System.out.println("The same rule, written two ways. Compare them in the source.");
        System.out.println();
        System.out.println("  Nested version   : " + describeNested(25, true));
        System.out.println("  Guard version    : " + describeWithGuards(25, true));
        System.out.println("  Nested version   : " + describeNested(15, true));
        System.out.println("  Guard version    : " + describeWithGuards(15, true));
        System.out.println("  Nested version   : " + describeNested(25, false));
        System.out.println("  Guard version    : " + describeWithGuards(25, false));
        System.out.println();
        System.out.println("Identical behaviour. The guard version has ONE level of");
        System.out.println("indentation and puts the happy path on the last line, where");
        System.out.println("you can find it. This is the highest-value habit in this lesson.");

        /* --------------------------------------------------------------------
         * NAME COMPLEX CONDITIONS. A well-named boolean is a comment the
         * compiler checks.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("Naming complex conditions:");

        int userAge = 22;
        String country = "IN";
        boolean suspended = false;
        double balance = 500.0;

        System.out.println("  UNNAMED (what does this line mean at a glance?):");
        System.out.println("    if (userAge >= 18 && country.equals(\"IN\") && !suspended && balance > 0)");
        System.out.println("      -> " + (userAge >= 18 && country.equals("IN") && !suspended && balance > 0));

        boolean isAdult = userAge >= 18;
        boolean isEligibleRegion = country.equals("IN");
        boolean isAccountUsable = !suspended && balance > 0;
        boolean canPlaceOrder = isAdult && isEligibleRegion && isAccountUsable;

        System.out.println();
        System.out.println("  NAMED (each line states one idea):");
        System.out.println("    isAdult          = " + isAdult);
        System.out.println("    isEligibleRegion = " + isEligibleRegion);
        System.out.println("    isAccountUsable  = " + isAccountUsable);
        System.out.println("    canPlaceOrder    = " + canPlaceOrder);
        System.out.println();
        System.out.println("  Bonus: when this is false, you can see WHICH part failed.");


        /* ====================================================================
         * SECTION 6 - THE TERNARY AS AN EXPRESSION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - if IS A STATEMENT, ?: IS AN EXPRESSION");
        System.out.println("=".repeat(74));

        int first = 17;
        int second = 42;

        // An if STATEMENT does something. It has no value, so it cannot be
        // assigned - you must declare the variable first and write into it.
        int maxViaIf;
        if (first > second) {
            maxViaIf = first;
        } else {
            maxViaIf = second;
        }

        // A ternary EXPRESSION has a value, so it can initialise directly -
        // which means the variable can be final.
        int maxViaTernary = (first > second) ? first : second;

        System.out.println("  via if      : " + maxViaIf + "   (4 lines, variable assigned later)");
        System.out.println("  via ternary : " + maxViaTernary + "   (1 line, can be final)");

        System.out.println();
        System.out.println("Good ternary use - choosing a VALUE:");
        for (int items = 0; items <= 2; items++) {
            System.out.println("  " + items + " " + (items == 1 ? "item" : "items"));
        }

        System.out.println();
        System.out.println("Bad ternary use - branching BEHAVIOUR. Use an if for that.");
        System.out.println("  If either branch has side effects, or is longer than a few");
        System.out.println("  characters, an if/else reads better and diffs better.");

        // Type compatibility between the branches.
        System.out.println();
        System.out.println("Both branches must share a type:");
        boolean flag = true;
        Object mixed = flag ? "text" : 42;    // legal: the common type is Object
        System.out.println("  flag ? \"text\" : 42  ->  " + mixed + "   (common type is Object)");
        System.out.println("  int i = flag ? \"text\" : 42;  ->  COMPILE ERROR");

        // The unboxing trap, worth repeating: an NPE from code with no visible
        // dereference.
        System.out.println();
        System.out.println("THE UNBOXING TRAP - an NPE with nothing to dereference:");
        try {
            boolean condition = false;
            Integer result = condition ? 1 : nullInteger();
            System.out.println("  got " + result);
        } catch (NullPointerException e) {
            System.out.println("  `condition ? 1 : nullInteger()` threw NullPointerException.");
            System.out.println("  The int literal 1 makes the whole expression type int,");
            System.out.println("  which forces the null branch to be UNBOXED. Keep both");
            System.out.println("  branches the same type.");
        }


        /* ====================================================================
         * SECTION 7 - SCOPE INSIDE CONDITIONALS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - SCOPE");
        System.out.println("=".repeat(74));

        if (true) {
            int insideOnly = 10;
            System.out.println("  A variable declared in a block lives only there: " + insideOnly);
        }
        // System.out.println(insideOnly);   // ERROR: cannot find symbol

        int declaredOutside = 0;
        if (true) {
            declaredOutside = 10;   // assigning, not declaring
        }
        System.out.println("  Declare it outside if you need it afterwards: " + declaredOutside);

        // Pattern matching introduces a variable with FLOW SCOPE: the compiler
        // works out exactly where it is provably valid. Lesson 37 goes deeper.
        System.out.println();
        System.out.println("Flow scope with pattern matching (Java 16+):");
        printLengthIfString("a string");
        printLengthIfString(42);


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 08.");
        System.out.println("=".repeat(74));
    }

    /**
     * Demonstrates that `else` binds to the nearest unmatched `if`, regardless
     * of how the code is indented. Deliberately written without braces.
     *
     * @param a the outer condition
     * @param b the inner condition
     */
    static void danglingElse(boolean a, boolean b) {
        if (a)
            if (b)
                System.out.println("    -> both a and b were true");
            else
                System.out.println("    -> the else ran (a was true, b was false)");
        // Nothing prints when a is false: the whole construct is skipped.
        if (!a) {
            System.out.println("    -> a was false, so NOTHING in the construct ran");
        }
    }

    /**
     * Eligibility check written with nested conditionals - the "arrow"
     * anti-pattern. Compare with {@link #describeWithGuards(int, boolean)}.
     *
     * @param age      the applicant's age
     * @param verified whether their identity is verified
     * @return a description of the outcome
     */
    static String describeNested(int age, boolean verified) {
        if (age > 0) {
            if (age >= 18) {
                if (verified) {
                    return "approved";
                } else {
                    return "rejected: not verified";
                }
            } else {
                return "rejected: under 18";
            }
        } else {
            return "rejected: invalid age";
        }
    }

    /**
     * The same eligibility rule written with guard clauses. Each failure is
     * handled and dismissed immediately, so the happy path is the last line
     * and the whole method stays at one level of indentation.
     *
     * @param age      the applicant's age
     * @param verified whether their identity is verified
     * @return a description of the outcome
     */
    static String describeWithGuards(int age, boolean verified) {
        if (age <= 0)   return "rejected: invalid age";
        if (age < 18)   return "rejected: under 18";
        if (!verified)  return "rejected: not verified";

        return "approved";
    }

    /**
     * Shows flow scope: the pattern variable exists only where the compiler can
     * prove the test succeeded - including AFTER an early return that handles
     * the failing case.
     *
     * @param candidate any object
     */
    static void printLengthIfString(Object candidate) {
        if (!(candidate instanceof String s)) {
            // `s` is deliberately NOT in scope here - the test failed.
            System.out.println("  " + candidate + " is not a String");
            return;
        }
        // The compiler reasoned: we only reach this line if the test passed,
        // so `s` is valid here even though it was declared inside a negated if.
        System.out.println("  \"" + s + "\" is a String of length " + s.length());
    }

    /**
     * Returns a null Integer. Writing `null` inline would let the compiler
     * infer a different type and hide the unboxing trap.
     *
     * @return null
     */
    static Integer nullInteger() {
        return null;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Rewrite describeNested() for a rule with FIVE conditions, then rewrite it
 *    with guard clauses. Count the maximum indentation level in each.
 *
 * 2. Find the bug without running it:
 *        if (score > 90);
 *            grade = "A";
 *    What is grade after this runs, for any score? Why?
 *
 * 3. Write `boolean isValidEmail(String email)` using guard clauses that
 *    returns false for null, blank, missing "@", or missing "." after the "@".
 *    Never let it throw.
 *
 * 4. Predict, then verify:
 *        String a = "hello";
 *        String b = "hel" + "lo";           // both parts are literals
 *        String c = "hel"; c = c + "lo";    // built at runtime
 *        System.out.println(a == b);
 *        System.out.println(a == c);
 *    Explain the difference. (Hint: the compiler folds constant expressions.)
 *
 * 5. Convert this to a single readable ternary, then decide whether you should:
 *        String s;
 *        if (n < 0) { s = "negative"; } else if (n == 0) { s = "zero"; }
 *        else { s = "positive"; }
 *
 * 6. Add braces to danglingElse() so the else belongs to the OUTER if. Confirm
 *    the behaviour changes for (false, true).
 * ============================================================================
 */
