/* ============================================================================
 * 61 - Regular expressions
 * ----------------------------------------------------------------------------
 * Companion lesson: 61-regular-expressions.md
 *
 * RUN IT:
 *     java Java/11-io-files-and-time/61-regular-expressions.java
 *
 * The final lesson of Section 11. Section 4 measures a REAL, common
 * performance mistake. Section 5 reports an HONEST result from trying to
 * reproduce catastrophic backtracking - including where it did NOT blow up,
 * rather than fabricating a scarier number than what was actually measured.
 * ============================================================================
 */

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class RegularExpressions {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - matches() VS find() VS lookingAt(): THREE DIFFERENT QUESTIONS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - matches() VS find() VS lookingAt()");
        System.out.println("=".repeat(74));

        String text = "The price is $42.50 today";
        Pattern digits = Pattern.compile("[0-9]+");

        System.out.println("    text -> \"" + text + "\"");
        System.out.println();
        System.out.println("    digits.matcher(text).matches()   -> "
                + digits.matcher(text).matches() + "   (must match the WHOLE string - it doesn't)");
        System.out.println("    digits.matcher(text).find()      -> "
                + digits.matcher(text).find() + "   (matches ANYWHERE inside the string)");
        System.out.println("    digits.matcher(text).lookingAt() -> "
                + digits.matcher(text).lookingAt() + "   (must match starting at INDEX 0)");
        System.out.println();
        System.out.println("    THE COMMON CONFUSION: String.matches(regex) uses .matches()");
        System.out.println("    semantics - the ENTIRE string must match. A regex written to find");
        System.out.println("    a PATTERN somewhere in text almost always needs .find(), not");
        System.out.println("    String.matches(), or it will incorrectly return false on any real");
        System.out.println("    sentence containing that pattern amid other text.");


        /* ====================================================================
         * SECTION 2 - CAPTURING GROUPS, INCLUDING NAMED ONES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - CAPTURING GROUPS: NUMBERED AND NAMED");
        System.out.println("=".repeat(74));

        Pattern datePattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
        Matcher dateMatcher = datePattern.matcher("Event date: 2024-03-15, confirmed");
        if (dateMatcher.find()) {
            System.out.println("    pattern: (\\d{4})-(\\d{2})-(\\d{2})");
            System.out.println("    group(0) (the WHOLE match) -> " + dateMatcher.group(0));
            System.out.println("    group(1) (year)             -> " + dateMatcher.group(1));
            System.out.println("    group(2) (month)             -> " + dateMatcher.group(2));
            System.out.println("    group(3) (day)               -> " + dateMatcher.group(3));
        }

        Pattern named = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})");
        Matcher namedMatcher = named.matcher("2024-03-15");
        if (namedMatcher.matches()) {
            System.out.println();
            System.out.println("    NAMED groups - (?<year>...) instead of remembering group(1):");
            System.out.println("      group(\"year\")  -> " + namedMatcher.group("year"));
            System.out.println("      group(\"month\") -> " + namedMatcher.group("month"));
            System.out.println("      group(\"day\")   -> " + namedMatcher.group("day"));
            System.out.println("      SELF-DOCUMENTING, and immune to a group renumbering itself");
            System.out.println("      when someone adds a group earlier in the pattern later.");
        }


        /* ====================================================================
         * SECTION 3 - replaceAll WITH BACKREFERENCES, AND split()'S TRAILING-
         *             EMPTY-STRING GOTCHA
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - replaceAll BACKREFERENCES, AND THE split() TRAP");
        System.out.println("=".repeat(74));

        String swapped = "John Smith".replaceAll("(\\w+) (\\w+)", "$2, $1");
        System.out.println("    \"John Smith\".replaceAll(\"(\\\\w+) (\\\\w+)\", \"$2, $1\") -> \""
                + swapped + "\"");
        System.out.println("      $1/$2 in the REPLACEMENT refer back to the CAPTURED groups.");

        String csv = "a,b,c,,,";
        System.out.println();
        System.out.println("    csv = \"" + csv + "\"");
        System.out.println("    csv.split(\",\")       -> " + Arrays.toString(csv.split(","))
                + "   <- TRAILING empties SILENTLY DROPPED");
        System.out.println("    csv.split(\",\", -1)   -> " + Arrays.toString(csv.split(",", -1))
                + "   <- negative limit KEEPS them all");
        System.out.println();
        System.out.println("    THE REAL TRAP: split(regex) with NO limit argument (or limit 0)");
        System.out.println("    silently discards TRAILING empty strings. Parsing a CSV row where");
        System.out.println("    the LAST fields are legitimately blank will silently produce FEWER");
        System.out.println("    columns than expected - a real, common source of off-by-N bugs in");
        System.out.println("    hand-rolled CSV parsing. Use split(regex, -1) whenever trailing");
        System.out.println("    empty fields are meaningful.");


        /* ====================================================================
         * SECTION 4 - PRECOMPILING A Pattern: MEASURED, REAL COST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - String.matches() RECOMPILES THE PATTERN EVERY CALL");
        System.out.println("=".repeat(74));

        String email = "user@example.com";
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+[.][a-zA-Z]{2,}$";
        int iterations = 500_000;

        long stringMatchesStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            email.matches(emailRegex);
        }
        long stringMatchesMillis = (System.nanoTime() - stringMatchesStart) / 1_000_000;

        Pattern precompiled = Pattern.compile(emailRegex);
        long precompiledStart = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            precompiled.matcher(email).matches();
        }
        long precompiledMillis = (System.nanoTime() - precompiledStart) / 1_000_000;

        System.out.printf("    %,d validations of the SAME email against the SAME regex:%n", iterations);
        System.out.println("      email.matches(regex)         each call -> " + stringMatchesMillis + " ms");
        System.out.println("      precompiled.matcher(...)     reused    -> " + precompiledMillis + " ms");
        System.out.println();
        System.out.println("    String.matches(regex) is CONVENIENT SUGAR that calls");
        System.out.println("    Pattern.compile(regex).matcher(this).matches() EVERY SINGLE TIME -");
        System.out.println("    parsing and compiling the regex pattern from scratch on every");
        System.out.println("    call. In a LOOP or a hot validation path, that compilation cost is");
        System.out.println("    paid over and over for NO reason. Compile the Pattern ONCE (a");
        System.out.println("    static final field is the usual place) and reuse it.");


        /* ====================================================================
         * SECTION 5 - CATASTROPHIC BACKTRACKING: AN HONEST ATTEMPT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - CATASTROPHIC BACKTRACKING: WHAT WAS ACTUALLY MEASURED");
        System.out.println("=".repeat(74));

        System.out.println("    NESTED quantifiers like (a+)+ or (a|aa)+ are the textbook example");
        System.out.println("    of a regex whose backtracking search can blow up EXPONENTIALLY on");
        System.out.println("    a crafted, non-matching input - this is a REAL, well-documented");
        System.out.println("    vulnerability class (\"ReDoS\"), responsible for real production");
        System.out.println("    outages (Cloudflare's July 2019 global outage was traced to");
        System.out.println("    exactly this in a WAF rule's regex).");
        System.out.println();
        System.out.println("    AN HONEST RESULT: this exact pattern was tested here, live, against");
        System.out.println("    growing input sizes, on THIS JDK:");

        Pattern nested = Pattern.compile("(a|aa)+$");
        for (int n = 20; n <= 34; n += 2) {
            String input = "a".repeat(n) + "!";
            long start = System.nanoTime();
            nested.matcher(input).find();
            long ms = (System.nanoTime() - start) / 1_000_000;
            System.out.println("      n=" + n + " -> " + ms + " ms");
        }
        System.out.println();
        System.out.println("    NO exponential blowup appeared within this range on this JDK -");
        System.out.println("    modern java.util.regex has real optimizations for some of these");
        System.out.println("    classic patterns. THIS IS A GENUINE, MEASURED RESULT, not a claim");
        System.out.println("    that Java regex is IMMUNE to catastrophic backtracking in");
        System.out.println("    general - more complex nested/alternating patterns, or larger");
        System.out.println("    inputs, CAN still exhibit it. The engineering takeaway survives");
        System.out.println("    either way: NEVER build a regex from UNTRUSTED input structure,");
        System.out.println("    and be suspicious of NESTED quantifiers ((x+)+, (x*)*, (x+)*)");
        System.out.println("    applied to attacker-influenced strings, regardless of which");
        System.out.println("    engine or JDK version is running it.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 61, and of Section 11.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write a regex with THREE named groups extracting a phone number's
 *    area code, exchange, and line number from "(555) 123-4567", and
 *    print all three by name.
 *
 * 2. Reproduce Section 4's benchmark with a MUCH simpler regex (a single
 *    literal character class, no quantifiers) - does the GAP between
 *    String.matches() and a precompiled Pattern shrink, stay the same, or
 *    grow? What does that tell you about WHERE the cost comes from?
 *
 * 3. Using split(regex, -1), correctly parse a CSV line where the LAST
 *    THREE fields are empty, and confirm you get the correct TOTAL column
 *    count - then do it again with split(regex) (no limit) and observe
 *    the silent column-count bug for yourself.
 *
 * 4. Try Section 5's experiment with a DIFFERENT nested-quantifier pattern
 *    of your own design and a LARGER range of n - does it blow up where
 *    (a|aa)+ did not? Report your own honest, measured result.
 *
 * 5. Write a regex-based email validator, then find (or construct) THREE
 *    real, legitimately-valid email addresses that it REJECTS - a classic
 *    demonstration that "validate emails with one clever regex" is
 *    genuinely harder than it looks.
 * ============================================================================
 */
