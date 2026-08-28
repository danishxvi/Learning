/* ============================================================================
 * 07 - PRINTING AND STRING FORMATTING
 * ----------------------------------------------------------------------------
 * Companion lesson: 07-output-and-string-formatting.md
 *
 * RUN IT:
 *     java Java/02-operators-and-input/07-output-and-string-formatting.java
 *
 * Everything printf can do, demonstrated. The payoff is Section 6, where the
 * specifiers combine into a genuinely aligned report - the thing you actually
 * wanted formatting for.
 * ============================================================================
 */

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.IllegalFormatConversionException;
import java.util.Locale;

class OutputAndStringFormatting {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE OUTPUT METHODS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE OUTPUT METHODS");
        System.out.println("=".repeat(74));

        System.out.print("print()   adds no newline, so ");
        System.out.print("these ");
        System.out.print("join up.");
        System.out.println();

        System.out.println("println() adds a newline.");

        // printf does NOT add a newline. Forgetting %n is the most common
        // printf mistake - the next line of output runs straight into yours.
        System.out.printf("printf()  adds NO newline either");
        System.out.printf(" <- see, this continued the same line%n");

        // format() is the exact same method under a second name.
        System.out.format("format()  is identical to printf()%n");

        // String.format() RETURNS the text instead of printing it.
        String built = String.format("String.format() returns text: %d + %d = %d", 2, 3, 5);
        System.out.println(built);

        // Use %n, not \n. %n emits the platform's line separator: \n on
        // Unix/macOS, \r\n on Windows. Hard-coding \n produces files that look
        // wrong in Windows Notepad.
        System.out.println();
        System.out.println("Use %n rather than \\n:");
        System.out.printf("  This line ended with %%n - correct on every platform.%n");


        /* ====================================================================
         * SECTION 2 - THE CONVERSIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - CONVERSION CHARACTERS");
        System.out.println("=".repeat(74));

        System.out.printf("  %%s  string          -> %s%n", "Danish");
        System.out.printf("  %%S  UPPERCASED      -> %S%n", "Danish");
        System.out.printf("  %%d  integer         -> %d%n", 42);
        System.out.printf("  %%f  floating point  -> %f%n", 3.14);
        System.out.printf("  %%e  scientific      -> %e%n", 31400.0);
        System.out.printf("  %%g  general         -> %g%n", 0.00003);
        System.out.printf("  %%c  character       -> %c%n", 65);
        System.out.printf("  %%x  hexadecimal     -> %x%n", 255);
        System.out.printf("  %%X  HEX uppercase   -> %X%n", 255);
        System.out.printf("  %%o  octal           -> %o%n", 8);
        System.out.printf("  %%b  boolean         -> %b%n", true);
        System.out.printf("  %%%%  literal percent -> %d%%%n", 75);

        /* --------------------------------------------------------------------
         * TRAP: %b is NOT a boolean test. It prints "true" for ANY non-null
         * value and "false" only for null.
         * ------------------------------------------------------------------*/
        System.out.println();
        System.out.println("TRAP - %b means 'is it non-null?', not 'is it true?':");
        System.out.printf("  %%b with true    -> %b%n", true);
        System.out.printf("  %%b with false   -> %b%n", false);
        System.out.printf("  %%b with \"hello\" -> %b   <- a String is not a boolean!%n", "hello");
        System.out.printf("  %%b with 0       -> %b   <- zero is not false either%n", 0);
        System.out.printf("  %%b with null    -> %b%n", (Object) null);

        /* --------------------------------------------------------------------
         * TRAP: %d accepts INTEGER types only. Passing a double throws at
         * RUNTIME - the compiler cannot check format strings.
         * ------------------------------------------------------------------*/
        System.out.println();
        System.out.println("TRAP - %d rejects doubles, and only at RUNTIME:");
        try {
            System.out.printf("  %d%n", 3.14);
        } catch (IllegalFormatConversionException e) {
            System.out.println("  printf(\"%d\", 3.14) threw " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            System.out.println("  The compiler cannot verify format strings. Watch your types.");
        }


        /* ====================================================================
         * SECTION 3 - FLAGS, WIDTH AND PRECISION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - FLAGS, WIDTH AND PRECISION");
        System.out.println("=".repeat(74));

        System.out.println("Width - the | markers show where each field ends:");
        System.out.printf("  %%10s   -> |%10s|   right-justified (the default)%n", "hi");
        System.out.printf("  %%-10s  -> |%-10s|   LEFT-justified%n", "hi");
        System.out.printf("  %%10d   -> |%10d|%n", 42);
        System.out.printf("  %%-10d  -> |%-10d|%n", 42);

        System.out.println();
        System.out.println("Width is a MINIMUM, never a maximum:");
        System.out.printf("  %%5s with a long value -> |%5s|   <- not truncated%n",
                "far too long for five");
        System.out.printf("  %%.5s truncates though  -> |%.5s|%n", "far too long for five");

        System.out.println();
        System.out.println("Zero padding and signs:");
        System.out.printf("  %%05d   -> %05d   zero-padded%n", 42);
        System.out.printf("  %%+d    -> %+d      always show the sign%n", 42);
        System.out.printf("  %%+d    -> %+d%n", -42);
        System.out.printf("  %% d    -> % d      space for positives (aligns with negatives)%n", 42);
        System.out.printf("  %%(d    -> %(d     negatives in parentheses (accounting style)%n", -42);
        System.out.printf("  %%,d    -> %,d   group separator, locale-aware%n", 1234567);
        System.out.printf("  %%#x    -> %#x     alternate form adds the 0x prefix%n", 255);

        System.out.println();
        System.out.println("Precision on floating point:");
        System.out.printf("  %%f      -> %f    (six decimals by default)%n", 3.14159265);
        System.out.printf("  %%.2f    -> %.2f%n", 3.14159265);
        System.out.printf("  %%.0f    -> %.0f%n", 3.14159265);
        System.out.printf("  %%10.2f  -> |%10.2f|   width AND precision%n", 3.14159265);
        System.out.printf("  %%-10.2f -> |%-10.2f|%n", 3.14159265);
        System.out.printf("  %%,.2f   -> %,.2f   grouped and rounded%n", 1234567.891);

        System.out.println();
        System.out.println("%.2f ROUNDS (half up). It does not truncate like a cast:");
        System.out.printf("  %%.2f of 3.145 -> %.2f%n", 3.145);
        System.out.printf("  %%.2f of 3.144 -> %.2f%n", 3.144);
        System.out.printf("  %%.0f of 2.5   -> %.0f%n", 2.5);
        System.out.println("  (int) 3.99    -> " + (int) 3.99 + "     <- a cast truncates instead");

        System.out.println();
        System.out.println("Argument index - reuse a value without repeating it:");
        System.out.printf("  %1$s is %2$d years old. Long live %1$s!%n", "Java", 31);
        System.out.println("  Essential for translations, where word order changes.");


        /* ====================================================================
         * SECTION 4 - TEXT BLOCKS (Java 15+)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - TEXT BLOCKS");
        System.out.println("=".repeat(74));

        // The old way: every quote escaped, every newline spelled out.
        String oldStyleJson = "{\n" +
                              "  \"name\": \"Danish\",\n" +
                              "  \"age\": 25\n" +
                              "}";

        // The new way: write it as it looks.
        String textBlockJson = """
                {
                  "name": "Danish",
                  "age": 25
                }""";

        System.out.println("Old style (escaped quotes and \\n):");
        System.out.println(oldStyleJson);
        System.out.println();
        System.out.println("Text block (Java 15+) - identical result, readable source:");
        System.out.println(textBlockJson);
        System.out.println();
        System.out.println("  Are they identical? " + oldStyleJson.equals(textBlockJson));

        // INCIDENTAL INDENTATION: the compiler finds the least-indented
        // non-blank line (the closing """ counts) and strips that much from
        // every line. So the closing delimiter's position sets the left margin.
        System.out.println();
        System.out.println("Indentation is set by the CLOSING delimiter:");

        String closingAligned = """
                    left margin here
                      indented two more
                    """;
        System.out.print(closingAligned);

        // Ending a line with \ suppresses the newline after it.
        String noTrailingNewline = """
                this text block has \
                no line break in the middle, \
                and none at the end.""";
        System.out.println("Line continuation with a trailing backslash:");
        System.out.println("  " + noTrailingNewline);

        // formatted() is String.format as an instance method - it reads much
        // better chained onto a text block.
        String name = "Danish Husain";
        int age = 25;
        String report = """
                Name : %s
                Age  : %d
                Grade: %.1f
                """.formatted(name, age, 87.5);
        System.out.println();
        System.out.println("Text block + .formatted(...) (Java 15+):");
        System.out.print(report);

        // Text blocks are at their best for SQL and HTML, where escaping quotes
        // used to make the code genuinely hard to read.
        String query = """
                SELECT id, name, email
                  FROM users
                 WHERE country = 'India'
                   AND active = true
                 ORDER BY name""";
        System.out.println();
        System.out.println("SQL in a text block - no escaping at all:");
        System.out.println(query);


        /* ====================================================================
         * SECTION 5 - NUMBERS FOR HUMANS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - LOCALE-AWARE NUMBER FORMATTING");
        System.out.println("=".repeat(74));

        double amount = 1234567.891;

        // NumberFormat produces the conventions of a specific locale: currency
        // symbol, separator characters, and digit-grouping rules.
        System.out.println("The same amount, " + amount + ", in three locales:");
        System.out.println("  US       : " + NumberFormat.getCurrencyInstance(Locale.US).format(amount));
        System.out.println("  Germany  : " + NumberFormat.getCurrencyInstance(Locale.GERMANY).format(amount));
        System.out.println("  India    : "
                + NumberFormat.getCurrencyInstance(Locale.of("en", "IN")).format(amount));
        System.out.println();
        System.out.println("  Look at what changed WITHOUT you writing any logic:");
        System.out.println("    - the currency symbol");
        System.out.println("    - which character groups thousands (, vs .)");
        System.out.println("    - which character is the decimal point (. vs ,)");
        System.out.println("    - whether the symbol goes before or after the number");
        System.out.println("  (If some symbols print as ? or garbage, that is your CONSOLE's");
        System.out.println("   encoding, not Java. Try:  java -Dfile.encoding=UTF-8 ...  or");
        System.out.println("   run `chcp 65001` first on Windows.)");

        System.out.println();
        System.out.println("Percentages and compact numbers:");
        System.out.println("  getPercentInstance(US).format(0.756)      = "
                + NumberFormat.getPercentInstance(Locale.US).format(0.756));
        System.out.println("  getCompactNumberInstance(US).format(1.2m) = "
                + NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
                        .format(1_200_000) + "   (Java 12+)");
        System.out.println("  the SAME call in your default locale         = "
                + NumberFormat.getCompactNumberInstance().format(1_200_000));
        System.out.println("  (in an Indian locale that is 12L - twelve lakh - not 1.2M.");
        System.out.println("   Compact forms are locale conventions, not abbreviations.)");

        System.out.println();
        System.out.println("DecimalFormat gives you the exact pattern:");
        System.out.println("  new DecimalFormat(\"#,##0.00\").format(1234.5)  = "
                + new DecimalFormat("#,##0.00").format(1234.5));
        System.out.println("  new DecimalFormat(\"000\").format(7)            = "
                + new DecimalFormat("000").format(7));
        System.out.println("  new DecimalFormat(\"#.##%\").format(0.1234)     = "
                + new DecimalFormat("#.##%").format(0.1234));
        System.out.println("  new DecimalFormat(\"#.##\").format(0.5)         = "
                + new DecimalFormat("#.##").format(0.5) + "   ('#' hides absent digits)");

        System.out.println();
        System.out.println("WARNING: NumberFormat and DecimalFormat are NOT thread-safe.");
        System.out.println("Never share one instance across threads. Create one per use.");

        System.out.println();
        System.out.println("For money, format a BigDecimal, never a double:");
        BigDecimal exactPrice = new BigDecimal("1.10");
        BigDecimal quantity = new BigDecimal("3");
        System.out.println("  1.10 x 3 as BigDecimal = " + exactPrice.multiply(quantity)
                + "   <- exact, and it keeps the scale");
        System.out.println("  1.10 x 3 as double     = " + (1.1 * 3)
                + "   <- that tail is binary rounding noise");
        System.out.println();
        System.out.println("  Formatting can HIDE the noise but not remove it:");
        System.out.printf("    printf(\"%%.2f\", 1.1 * 3) = %.2f   <- looks right...%n", 1.1 * 3);
        System.out.println("    ...but the underlying value is still wrong. Multiply it by");
        System.out.println("    a million invoices and the error is real money.");


        /* ====================================================================
         * SECTION 6 - PUTTING IT TOGETHER: AN ALIGNED REPORT
         * --------------------------------------------------------------------
         * This is what formatting is actually for. Every column lines up
         * because the width is fixed in the format string, not guessed with
         * spaces.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - A REAL FORMATTED REPORT");
        System.out.println("=".repeat(74));

        String[] products = {"Mechanical Keyboard", "Mouse", "27\" Monitor", "USB-C Hub"};
        int[] quantities = {2, 15, 3, 7};
        double[] prices = {8999.00, 1299.50, 24999.99, 2450.75};

        // One format string, reused for the header and every row, so the
        // columns cannot drift out of alignment.
        String rowFormat = "  %-22s %8s %14s %14s%n";
        String dataFormat = "  %-22s %8d %14s %14s%n";

        System.out.printf(rowFormat, "PRODUCT", "QTY", "UNIT PRICE", "LINE TOTAL");
        System.out.println("  " + "-".repeat(60));

        NumberFormat rupees = NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
        double grandTotal = 0;

        for (int i = 0; i < products.length; i++) {
            double lineTotal = quantities[i] * prices[i];
            grandTotal += lineTotal;
            System.out.printf(dataFormat,
                    products[i],
                    quantities[i],
                    rupees.format(prices[i]),
                    rupees.format(lineTotal));
        }

        System.out.println("  " + "-".repeat(60));
        System.out.printf(rowFormat, "GRAND TOTAL", "", "", rupees.format(grandTotal));

        // Notice how %-22s guarantees the product column, however long the
        // name, and how the currency strings right-align in their %14s fields.


        /* ====================================================================
         * SECTION 7 - WHY PRODUCTION CODE DOES NOT USE println
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - println IS NOT LOGGING");
        System.out.println("=".repeat(74));

        System.out.println("System.out.println is right for learning and CLI tools.");
        System.out.println("Inside an application it is wrong, because it has:");
        System.out.println("  - no severity levels, so you cannot filter debug noise");
        System.out.println("  - no timestamp, thread name or class name");
        System.out.println("  - no way to switch off without editing and redeploying");
        System.out.println("  - no destination but the console (no file, no aggregator)");
        System.out.println("  - a synchronized lock, so it stalls hot loops");
        System.out.println();
        System.out.println("Real code uses SLF4J with Logback or Log4j2:");
        System.out.println();
        System.out.println("    private static final Logger log =");
        System.out.println("            LoggerFactory.getLogger(MyClass.class);");
        System.out.println();
        System.out.println("    log.debug(\"Cache miss for key {}\", key);");
        System.out.println("    log.info(\"Order {} placed by {}\", orderId, userId);");
        System.out.println("    log.error(\"Payment failed for {}\", orderId, exception);");
        System.out.println();
        System.out.println("The {} placeholders matter: the message is only assembled if");
        System.out.println("that level is enabled, so a disabled log.debug costs nothing.");
        System.out.println("Writing \"...\" + key would build the string every single time.");
        System.out.println();
        System.out.println("For THIS repository println is exactly right - the point is to");
        System.out.println("see values immediately. Just know what you would use at work.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 07.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Print a multiplication table for 1..9 where every column is exactly 4
 *    characters wide and perfectly aligned. One printf, one nested loop.
 *
 * 2. Format 1234567.891 five ways: two decimals; grouped with two decimals;
 *    zero-padded to 15 characters; scientific notation; and as INR currency.
 *
 * 3. Predict the output of each, then run them:
 *        System.out.printf("%b%n", "false");
 *        System.out.printf("%s%n", (Object) null);
 *        System.out.printf("%.3s%n", "abcdefg");
 *
 * 4. Write a text block containing an HTML page with quoted attributes. Notice
 *    you escape nothing. Then move the closing """ four spaces right and see
 *    how the indentation of the whole block changes.
 *
 * 5. Build a receipt with a %-20s name column, a %8.2f price column and a
 *    right-aligned total row. Make it still line up when a name is 30
 *    characters long - then explain why it cannot, and what you would do.
 *
 * 6. Time 100,000 iterations of string building three ways: "a" + i,
 *    String.format("%d", i), and a StringBuilder. Predict the ranking first.
 *    Lesson 15 explains the result.
 * ============================================================================
 */
