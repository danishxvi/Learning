/* ============================================================================
 * 14 - STRINGS, THE STRING POOL AND IMMUTABILITY
 * ----------------------------------------------------------------------------
 * Companion lesson: 14-strings.md
 *
 * RUN IT:
 *     java Java/04-arrays-and-strings/14-strings.java
 *
 * TWO FACTS EXPLAIN ALMOST EVERYTHING SURPRISING ABOUT String:
 *   1. Strings are IMMUTABLE - every method returns a new one.
 *   2. String LITERALS are INTERNED - identical literals are the same object.
 * ============================================================================
 */

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class Strings {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - IMMUTABILITY
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - STRINGS ARE IMMUTABLE");
        System.out.println("=".repeat(74));

        String greeting = "hello";

        // This looks like it changes `greeting`. It does not. It computes a new
        // String and throws it away, because nothing captures the return value.
        greeting.toUpperCase();
        System.out.println("  String s = \"hello\";");
        System.out.println("  s.toUpperCase();          -> s is still \"" + greeting + "\"");

        greeting = greeting.toUpperCase();   // you must REASSIGN
        System.out.println("  s = s.toUpperCase();      -> s is now \"" + greeting + "\"");

        System.out.println();
        System.out.println("  Every String method returns a NEW string:");
        String original = "  Danish Husain  ";
        System.out.println("    original           = \"" + original + "\"");
        System.out.println("    original.strip()   = \"" + original.strip() + "\"");
        System.out.println("    original           = \"" + original + "\"   <- untouched");

        System.out.println();
        System.out.println("  The compiler CANNOT warn you about this - the code is valid,");
        System.out.println("  it just discards the result. It is the classic beginner bug.");

        System.out.println();
        System.out.println("WHY immutable? Four reasons that all matter:");
        System.out.println("  1. SECURITY   - a filename or URL checked by a security layer");
        System.out.println("                  cannot be swapped afterwards by another thread");
        System.out.println("  2. THREAD SAFETY - immutable objects need no synchronisation");
        System.out.println("  3. CACHED HASH - hashCode() is computed once and stored, which");
        System.out.println("                  is why String is such a good HashMap key");
        System.out.println("  4. THE POOL   - sharing one object between many variables is");
        System.out.println("                  only safe if nobody can modify it");


        /* ====================================================================
         * SECTION 2 - THE STRING POOL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE STRING POOL");
        System.out.println("=".repeat(74));

        String literalOne = "hello";
        String literalTwo = "hello";                 // finds the POOLED object
        String constructed = new String("hello");    // FORCES a new heap object

        System.out.println("  String a = \"hello\";");
        System.out.println("  String b = \"hello\";");
        System.out.println("  String c = new String(\"hello\");");
        System.out.println();
        System.out.println("    a == b        -> " + (literalOne == literalTwo)
                + "    both found the SAME pooled object");
        System.out.println("    a == c        -> " + (literalOne == constructed)
                + "   new String() deliberately made a second object");
        System.out.println("    a.equals(c)   -> " + literalOne.equals(constructed)
                + "    equal CONTENT, which is what you actually care about");

        System.out.println();
        System.out.println("  INTERVIEW CLASSIC: how many objects does");
        System.out.println("      String s = new String(\"hello\");");
        System.out.println("  create? TWO - one pooled for the literal (if not already");
        System.out.println("  there), and one on the heap for the `new`.");
        System.out.println();
        System.out.println("  new String(\"x\") is essentially NEVER right in real code.");
        System.out.println("  It exists in tutorials only to demonstrate this behaviour.");

        // intern() returns the pooled instance for an equal string.
        String interned = constructed.intern();
        System.out.println();
        System.out.println("  c.intern() == a  -> " + (interned == literalOne)
                + "    intern() hands back the POOLED instance");
        System.out.println("  Occasionally useful for deduplicating many repeated strings");
        System.out.println("  read from a file. Modern JVMs offer automatic deduplication");
        System.out.println("  (-XX:+UseStringDeduplication with G1), usually a better answer.");

        /* --------------------------------------------------------------------
         * COMPILE-TIME CONSTANT FOLDING. This is what makes == on strings
         * "work" in test code and fail on real data.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("COMPILE-TIME CONSTANT FOLDING:");

        String target = "hello";
        String foldedByCompiler = "hel" + "lo";     // both parts are literals
        String builtAtRuntime = buildAtRuntime();   // computed while running

        final String finalPrefix = "hel";
        String foldedViaFinal = finalPrefix + "lo"; // final + literal is still constant

        String nonFinalPrefix = "hel";
        String notFolded = nonFinalPrefix + "lo";   // a variable: runtime concatenation

        System.out.println("  \"hel\" + \"lo\"              == \"hello\"  -> "
                + (foldedByCompiler == target) + "    folded by the COMPILER");
        System.out.println("  final String p = \"hel\"; p + \"lo\"      -> "
                + (foldedViaFinal == target) + "    final makes it a constant expression");
        System.out.println("  String p = \"hel\"; p + \"lo\"            -> "
                + (notFolded == target) + "   a variable: computed at RUNTIME");
        System.out.println("  built with a StringBuilder             -> "
                + (builtAtRuntime == target) + "   definitely a new object");

        System.out.println();
        System.out.println("  ALL FOUR have equal content:");
        System.out.println("    .equals()  -> " + target.equals(foldedByCompiler) + " "
                + target.equals(foldedViaFinal) + " " + target.equals(notFolded) + " "
                + target.equals(builtAtRuntime));
        System.out.println();
        System.out.println("  THIS is why == on Strings passes your tests and fails in");
        System.out.println("  production. NEVER use == on strings. Use .equals().");


        /* ====================================================================
         * SECTION 3 - COMPACT STRINGS (Java 9+)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - HOW A String STORES ITS CHARACTERS");
        System.out.println("=".repeat(74));

        System.out.println("  Before Java 9: a char[] - 2 bytes per character, even for");
        System.out.println("  pure ASCII, which wasted half the memory of most strings.");
        System.out.println();
        System.out.println("  Since Java 9 (compact strings, JEP 254): a byte[] plus a");
        System.out.println("  one-byte coder flag. Latin-1-only strings use 1 BYTE per");
        System.out.println("  character; anything else transparently uses UTF-16.");
        System.out.println();
        System.out.println("  You never see this through the API. It is simply why moving");
        System.out.println("  from Java 8 to 9+ cut heap usage in many applications with");
        System.out.println("  no code changes at all.");


        /* ====================================================================
         * SECTION 4 - THE METHODS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE METHODS YOU ACTUALLY NEED");
        System.out.println("=".repeat(74));

        String sample = "  Java Programming  ";

        System.out.println("  sample = \"" + sample + "\"");
        System.out.println();
        System.out.println("  INSPECTING");
        System.out.println("    length()                 = " + sample.length());
        System.out.println("    isEmpty()                = " + sample.isEmpty());
        System.out.println("    isBlank()                = " + sample.isBlank() + "   (Java 11+)");
        System.out.println("    \"   \".isBlank()          = " + "   ".isBlank()
                + "    <- whitespace counts as blank");
        System.out.println("    charAt(2)                = '" + sample.charAt(2) + "'");
        System.out.println("    indexOf(\"a\")             = " + sample.indexOf("a"));
        System.out.println("    lastIndexOf(\"a\")         = " + sample.lastIndexOf("a"));
        System.out.println("    indexOf(\"zzz\")           = " + sample.indexOf("zzz")
                + "   <- -1 means NOT FOUND, not an exception");
        System.out.println("    contains(\"Program\")      = " + sample.contains("Program"));
        System.out.println("    strip().startsWith(\"Java\") = " + sample.strip().startsWith("Java"));

        System.out.println();
        System.out.println("  TRIMMING - prefer strip() over trim()");
        System.out.println("    trim()   = \"" + sample.trim() + "\"");
        System.out.println("    strip()  = \"" + sample.strip() + "\"   (Java 11+)");
        System.out.println("    trim() removes chars <= U+0020; strip() removes anything");
        System.out.println("    Unicode calls whitespace. strip() is the correct one.");

        System.out.println();
        System.out.println("  COMPARING");
        System.out.println("    \"apple\".equals(\"Apple\")            = " + "apple".equals("Apple"));
        System.out.println("    \"apple\".equalsIgnoreCase(\"Apple\")  = " + "apple".equalsIgnoreCase("Apple"));
        System.out.println("    \"apple\".compareTo(\"banana\")        = " + "apple".compareTo("banana")
                + "   (negative: apple sorts first)");
        System.out.println("    \"banana\".compareTo(\"apple\")        = " + "banana".compareTo("apple")
                + "    (positive)");
        System.out.println("    \"apple\".compareTo(\"apple\")         = " + "apple".compareTo("apple")
                + "    (zero: equal)");
        System.out.println("    \"app\".compareTo(\"apple\")           = " + "app".compareTo("apple")
                + "   (prefix: the LENGTH difference)");
        System.out.println("    Rely only on the SIGN, never the exact number.");

        System.out.println();
        System.out.println("  SUBSTRINGS - start inclusive, end EXCLUSIVE");
        String word = "Programming";
        System.out.println("    \"" + word + "\".substring(3)     = \"" + word.substring(3) + "\"");
        System.out.println("    \"" + word + "\".substring(0, 4)  = \"" + word.substring(0, 4)
                + "\"      length is 4 - 0 = 4");
        System.out.println("    That is WHY the end is exclusive: substring(a, b) always");
        System.out.println("    has length b - a, which is the property you want.");

        try {
            word.substring(5, 99);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println("    substring(5, 99) -> StringIndexOutOfBoundsException");
        }


        /* ====================================================================
         * SECTION 5 - THE replace / replaceAll AND split TRAPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - LITERAL VS REGEX: THE BIGGEST String TRAP");
        System.out.println("=".repeat(74));

        String version = "1.2.3";

        System.out.println("  version = \"" + version + "\"");
        System.out.println();
        System.out.println("    replace(\".\", \"-\")     = \"" + version.replace(".", "-")
                + "\"   <- LITERAL. Correct.");
        System.out.println("    replaceAll(\".\", \"-\")  = \"" + version.replaceAll(".", "-")
                + "\"   <- REGEX! '.' means ANY character.");
        System.out.println();
        System.out.println("    replace     takes a LITERAL string");
        System.out.println("    replaceAll  takes a REGULAR EXPRESSION");
        System.out.println("    To replace literal dots with replaceAll you must escape:");
        System.out.println("    replaceAll(\"\\\\.\", \"-\") = \"" + version.replaceAll("\\.", "-") + "\"");

        System.out.println();
        System.out.println("  split() is also a regex:");
        System.out.println("    \"1.2.3\".split(\".\")    -> " + Arrays.toString(version.split("."))
                + "   <- empty! every char was a delimiter");
        System.out.println("    \"1.2.3\".split(\"\\\\.\")   -> " + Arrays.toString(version.split("\\."))
                + "   <- correct");

        System.out.println();
        System.out.println("  split() DROPS trailing empty strings:");
        String csv = "a,b,,";
        System.out.println("    \"a,b,,\".split(\",\")     -> " + Arrays.toString(csv.split(","))
                + "     length " + csv.split(",").length);
        System.out.println("    \"a,b,,\".split(\",\", -1) -> " + Arrays.toString(csv.split(",", -1))
                + "  length " + csv.split(",", -1).length + "   <- negative limit keeps them");
        System.out.println("    This bites hard when parsing CSV with empty trailing fields.");

        System.out.println();
        System.out.println("  split() with a positive limit stops splitting:");
        System.out.println("    \"a,b,c\".split(\",\", 2)  -> " + Arrays.toString("a,b,c".split(",", 2)));

        System.out.println();
        System.out.println("  JOINING is the inverse and has no traps:");
        System.out.println("    String.join(\"-\", \"a\", \"b\", \"c\")  -> " + String.join("-", "a", "b", "c"));
        List<String> parts = List.of("Java", "is", "verbose");
        System.out.println("    String.join(\" \", aList)          -> " + String.join(" ", parts));


        /* ====================================================================
         * SECTION 6 - null SAFETY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - SURVIVING null");
        System.out.println("=".repeat(74));

        String missing = null;

        try {
            missing.equals("yes");
        } catch (NullPointerException e) {
            System.out.println("  missing.equals(\"yes\")          -> NullPointerException");
        }

        System.out.println("  \"yes\".equals(missing)          -> " + "yes".equals(missing)
                + "   <- literal first: safe");
        System.out.println("  Objects.equals(missing, \"yes\") -> " + Objects.equals(missing, "yes")
                + "   <- safe both ways");
        System.out.println("  String.valueOf(missing)        -> \"" + String.valueOf(missing)
                + "\"  <- null-safe conversion");
        System.out.println("  \"prefix \" + missing            -> \"" + ("prefix " + missing)
                + "\"   <- concatenation is null-safe too");

        try {
            switch (missing) {
                case "yes" -> System.out.println("yes");
                default -> System.out.println("other");
            }
        } catch (NullPointerException e) {
            System.out.println("  switch (missing)               -> NullPointerException");
            System.out.println("    (unless you write `case null ->`, Java 21+)");
        }


        /* ====================================================================
         * SECTION 7 - UNICODE: length() IS NOT CHARACTER COUNT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - length() COUNTS UTF-16 CODE UNITS");
        System.out.println("=".repeat(74));

        String plain = "hello";
        String accented = "héllo";
        String emoji = "😀";
        String family = "👨‍👩‍👧";

        System.out.printf("    %-22s length() = %d, codePointCount = %d%n",
                "\"hello\"", plain.length(), plain.codePointCount(0, plain.length()));
        System.out.printf("    %-22s length() = %d, codePointCount = %d%n",
                "\"héllo\" (accented)", accented.length(),
                accented.codePointCount(0, accented.length()));
        System.out.printf("    %-22s length() = %d, codePointCount = %d%n",
                "one emoji", emoji.length(), emoji.codePointCount(0, emoji.length()));
        System.out.printf("    %-22s length() = %d, codePointCount = %d%n",
                "a family emoji", family.length(), family.codePointCount(0, family.length()));

        System.out.println();
        System.out.println("  An emoji needs TWO chars - a SURROGATE PAIR - because a char");
        System.out.println("  is 16 bits and the emoji lives above the Basic Multilingual");
        System.out.println("  Plane. A joined family emoji needs several code points plus");
        System.out.println("  zero-width joiners between them.");
        System.out.println();
        System.out.println("  s.chars()      -> IntStream of char values (surrogates split!)");
        System.out.println("  s.codePoints() -> IntStream of code points (usually correct)");
        System.out.println();
        System.out.println("  For true visible-character counting you need grapheme cluster");
        System.out.println("  segmentation via java.text.BreakIterator. In practice: be");
        System.out.println("  careful whenever you index into user-supplied text.");


        /* ====================================================================
         * SECTION 8 - RECIPES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - RECIPES WORTH MEMORISING");
        System.out.println("=".repeat(74));

        System.out.println("  reverse(\"Danish\")            = " + reverse("Danish"));
        System.out.println("  isPalindrome(\"A man, a plan, a canal: Panama\") = "
                + isPalindrome("A man, a plan, a canal: Panama"));
        System.out.println("  isPalindrome(\"hello\")        = " + isPalindrome("hello"));
        System.out.println("  countChar(\"banana\", 'a')     = " + countChar("banana", 'a'));
        System.out.println("  titleCase(\"dANISH\")          = " + titleCase("dANISH"));
        System.out.println("  \"42\".matches(\"-?\\\\d+\")       = " + "42".matches("-?\\d+"));
        System.out.println("  \"4x2\".matches(\"-?\\\\d+\")      = " + "4x2".matches("-?\\d+"));
        System.out.println("  \"ab\".repeat(3)               = " + "ab".repeat(3) + "   (Java 11+)");
        System.out.println("  \"a b c\".chars().count()      = " + "a b c".chars().count());


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 14.");
        System.out.println("=".repeat(74));
    }

    /**
     * Builds "hello" at runtime so the compiler cannot fold it into a pooled
     * constant. Used to show that runtime concatenation produces a new object.
     *
     * @return a new String equal to "hello"
     */
    static String buildAtRuntime() {
        return new StringBuilder("hel").append("lo").toString();
    }

    /**
     * Reverses a string. StringBuilder is the idiomatic way - String itself has
     * no reverse method, because it is immutable.
     *
     * @param input the text to reverse
     * @return the reversed text
     */
    static String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * Tests for a palindrome, ignoring case, spaces and punctuation.
     *
     * @param input the text to test
     * @return true if it reads the same both ways
     */
    static boolean isPalindrome(String input) {
        // [^a-z0-9] is a regex meaning "anything that is NOT a lowercase letter
        // or digit". replaceAll takes a regex, which is correct here.
        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9]", "");
        return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
    }

    /**
     * Counts occurrences of one character using a stream over the char values.
     *
     * @param input  the text to search
     * @param target the character to count
     * @return how many times it appears
     */
    static long countChar(String input, char target) {
        return input.chars().filter(c -> c == target).count();
    }

    /**
     * Capitalises the first character and lowercases the rest.
     *
     * @param input the word to convert; may be null or empty
     * @return the title-cased word
     */
    static String titleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Predict each, then run it:
 *        String a = "java";
 *        String b = "ja" + "va";
 *        String c = "ja"; c = c + "va";
 *        System.out.println(a == b);
 *        System.out.println(a == c);
 *        System.out.println(a == c.intern());
 *
 * 2. Write `String removeVowels(String s)` two ways: with replaceAll and with
 *    a loop plus StringBuilder. Time both on a 1,000,000-character string.
 *
 * 3. Parse "name=Danish;age=25;city=Bengaluru" into key/value pairs. Handle a
 *    malformed entry without throwing.
 *
 * 4. Explain why `"a,b,,".split(",")` has length 2 but `"a,,b".split(",")` has
 *    length 3. Then write a split that never drops anything.
 *
 * 5. Write `int countWords(String s)` that returns 0 for null, empty and
 *    whitespace-only input, and handles multiple spaces between words.
 *
 * 6. Take the emoji from Section 7 and try s.charAt(0) and s.substring(0, 1).
 *    Print what you get. You have just split a surrogate pair - this is how
 *    text corruption happens in real applications.
 * ============================================================================
 */
