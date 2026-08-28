/* ============================================================================
 * 15 - StringBuilder AND StringBuffer
 * ----------------------------------------------------------------------------
 * Companion lesson: 15-stringbuilder-and-stringbuffer.md
 *
 * RUN IT:
 *     java Java/04-arrays-and-strings/15-stringbuilder-and-stringbuffer.java
 *
 * Lesson 14 established that String is immutable. That is the right default,
 * but it makes building a string piece by piece expensive. This lesson
 * MEASURES how expensive, then shows the tools that fix it.
 * ============================================================================
 */

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

class StringBuilderAndBuffer {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE PROBLEM, MEASURED
         * --------------------------------------------------------------------
         * += cannot append to an immutable String. Each iteration allocates a
         * new string and COPIES everything so far into it. At iteration n the
         * copy costs n characters, so total work is 1+2+3+...+n = n^2/2.
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WHY += IN A LOOP IS QUADRATIC");
        System.out.println("=".repeat(74));

        System.out.println("  Doubling the loop count should DOUBLE an O(n) algorithm's");
        System.out.println("  time, but QUADRUPLE an O(n^2) one. Watch which happens:");
        System.out.println();
        System.out.printf("    %-12s %14s %14s %12s%n",
                "ITERATIONS", "+= (ms)", "BUILDER (ms)", "RATIO");

        for (int count : new int[]{10_000, 20_000, 40_000, 80_000}) {
            long concatMillis = timeConcatenation(count);
            long builderMillis = timeBuilder(count);
            String ratio = builderMillis == 0
                    ? "> " + concatMillis + "x"
                    : (concatMillis / builderMillis) + "x";
            System.out.printf("    %-12s %14d %14d %12s%n",
                    String.format("%,d", count), concatMillis, builderMillis, ratio);
        }

        System.out.println();
        System.out.println("  Read the += column: each row is roughly FOUR times the one");
        System.out.println("  above, for only twice the work. That is O(n^2) in the wild.");
        System.out.println("  (Garbage collection and JIT add noise, so expect 2x-4x rather");
        System.out.println("  than a clean 4x.)");
        System.out.println();
        System.out.println("  The builder column stays at 0-1 ms throughout - it is below");
        System.out.println("  millisecond resolution at these sizes, which is exactly the");
        System.out.println("  point. Section 5 measures it at 5,000,000 appends instead.");
        System.out.println();
        System.out.println("  WHY: result += piece must");
        System.out.println("    1. allocate a NEW string big enough for old + new");
        System.out.println("    2. COPY every existing character into it");
        System.out.println("    3. append the new piece");
        System.out.println("    4. leave the old string as garbage");
        System.out.println("  StringBuilder writes into one growable buffer instead.");


        /* ====================================================================
         * SECTION 2 - THE BASICS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - StringBuilder BASICS");
        System.out.println("=".repeat(74));

        StringBuilder builder = new StringBuilder();
        builder.append("Hello");
        builder.append(", ");
        builder.append("world");
        builder.append('!');        // char
        builder.append(' ');
        builder.append(42);         // int
        builder.append(' ');
        builder.append(3.14);       // double
        builder.append(' ');
        builder.append(true);       // boolean
        builder.append(' ');
        builder.append((Object) null);   // appends the literal text "null"

        System.out.println("  after appending mixed types:");
        System.out.println("    \"" + builder + "\"");
        System.out.println("  append is overloaded for every primitive, char[], String,");
        System.out.println("  CharSequence and Object - so nothing needs converting first.");
        System.out.println("  Appending null gives the literal text \"null\", not an NPE.");

        // Every mutator returns `this`, so calls chain. This is the FLUENT
        // INTERFACE pattern.
        String chained = new StringBuilder()
                .append("Danish")
                .append(" ")
                .append("Husain")
                .toString();
        System.out.println();
        System.out.println("  Chained (every mutator returns `this`):");
        System.out.println("    \"" + chained + "\"");


        /* ====================================================================
         * SECTION 3 - THE FULL API
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE WHOLE API, DEMONSTRATED");
        System.out.println("=".repeat(74));

        StringBuilder api = new StringBuilder("Hello World");
        System.out.println("  start                        -> \"" + api + "\"");

        api.append("!");
        System.out.println("  append(\"!\")                  -> \"" + api + "\"");

        api.insert(5, ",");
        System.out.println("  insert(5, \",\")               -> \"" + api + "\"");

        api.replace(0, 5, "Howdy");
        System.out.println("  replace(0, 5, \"Howdy\")       -> \"" + api + "\"");

        api.deleteCharAt(api.length() - 1);
        System.out.println("  deleteCharAt(length - 1)     -> \"" + api + "\"");

        api.delete(5, 6);
        System.out.println("  delete(5, 6)   [end exclusive] -> \"" + api + "\"");

        api.setCharAt(0, 'h');
        System.out.println("  setCharAt(0, 'h')            -> \"" + api + "\"");

        System.out.println();
        System.out.println("  READING (these do not modify anything):");
        System.out.println("    charAt(0)        = '" + api.charAt(0) + "'");
        System.out.println("    indexOf(\"World\") = " + api.indexOf("World"));
        System.out.println("    length()         = " + api.length() + "   (a METHOD, like String)");
        System.out.println("    substring(6)     = \"" + api.substring(6) + "\"   (returns a String)");
        System.out.println("    isEmpty()        = " + api.isEmpty() + "   (Java 15+)");

        api.reverse();
        System.out.println();
        System.out.println("  reverse()  [in place]        -> \"" + api + "\"");
        api.reverse();      // put it back

        System.out.println();
        System.out.println("  String has NO reverse() method, because it is immutable.");
        System.out.println("  new StringBuilder(s).reverse().toString() is the idiom, and");
        System.out.println("  it is why StringBuilder appears in so many string puzzles.");
        System.out.println("    reverse(\"Bengaluru\") = " + reverse("Bengaluru"));


        /* ====================================================================
         * SECTION 4 - CAPACITY AND GROWTH
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - CAPACITY: THE BUFFER BEHIND THE BUILDER");
        System.out.println("=".repeat(74));

        System.out.println("  Default capacities:");
        System.out.println("    new StringBuilder()          -> capacity "
                + new StringBuilder().capacity());
        System.out.println("    new StringBuilder(100)       -> capacity "
                + new StringBuilder(100).capacity());
        System.out.println("    new StringBuilder(\"hello\")   -> capacity "
                + new StringBuilder("hello").capacity() + "   (5 + 16)");

        System.out.println();
        System.out.println("  Watching it grow. The rule is newCapacity = 2 * old + 2:");
        System.out.println();
        System.out.printf("    %-10s %-12s %s%n", "LENGTH", "CAPACITY", "EVENT");

        StringBuilder growing = new StringBuilder();
        int lastCapacity = growing.capacity();
        System.out.printf("    %-10d %-12d %s%n", growing.length(), lastCapacity, "created");

        for (int i = 1; i <= 80; i++) {
            growing.append('x');
            if (growing.capacity() != lastCapacity) {
                System.out.printf("    %-10d %-12d %s%n",
                        growing.length(), growing.capacity(),
                        "GREW from " + lastCapacity + " (2 * " + lastCapacity + " + 2)");
                lastCapacity = growing.capacity();
            }
        }

        System.out.println();
        System.out.println("  That DOUBLING is what makes appending amortised O(1): growth");
        System.out.println("  happens logarithmically often, and each character is copied a");
        System.out.println("  constant number of times on average.");

        System.out.println();
        System.out.println("  Presizing avoids every intermediate copy:");
        System.out.println("    new StringBuilder(expectedLength)");
        System.out.println("  Worth doing for large builders; irrelevant for a few appends.");

        // setLength(0) clears the CONTENT but keeps the allocated buffer.
        StringBuilder reused = new StringBuilder(1000);
        reused.append("some content");
        int capacityBefore = reused.capacity();
        reused.setLength(0);

        System.out.println();
        System.out.println("  Clearing a builder:");
        System.out.println("    setLength(0)  -> length " + reused.length()
                + ", capacity still " + reused.capacity()
                + " (was " + capacityBefore + ")");
        System.out.println("    Reuses the buffer - no allocation. In a loop that builds");
        System.out.println("    many strings this beats `sb = new StringBuilder()`.");


        /* ====================================================================
         * SECTION 5 - StringBuilder VS StringBuffer
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - StringBuilder VS StringBuffer");
        System.out.println("=".repeat(74));

        int operations = 5_000_000;

        StringBuilder unsynchronised = new StringBuilder();
        long startBuilder = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            unsynchronised.append('x');
        }
        long builderMillis = (System.nanoTime() - startBuilder) / 1_000_000;

        StringBuffer synchronised = new StringBuffer();
        long startBuffer = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            synchronised.append('x');
        }
        long bufferMillis = (System.nanoTime() - startBuffer) / 1_000_000;

        System.out.printf("  %,d appends on a single thread:%n", operations);
        System.out.println("    StringBuilder (unsynchronised) : " + builderMillis + " ms");
        System.out.println("    StringBuffer  (synchronised)   : " + bufferMillis + " ms");
        System.out.println("    same result? " + (unsynchronised.length() == synchronised.length()));

        System.out.println();
        System.out.println("  IDENTICAL APIs. The only difference is that every StringBuffer");
        System.out.println("  method is `synchronized`.");
        System.out.println();
        System.out.println("  StringBuffer came first (Java 1.0) and was made thread-safe");
        System.out.println("  'just in case'. Java 5 added StringBuilder without the locking,");
        System.out.println("  because almost all string building happens inside one method");
        System.out.println("  on one thread, where the synchronisation is pure waste.");
        System.out.println();
        System.out.println("  DEFAULT TO StringBuilder. ALWAYS.");
        System.out.println();
        System.out.println("  Note the JIT can often elide an uncontended lock, so the gap");
        System.out.println("  above may look small - but StringBuffer never WINS, and it");
        System.out.println("  solves a problem you almost certainly do not have. Even then");
        System.out.println("  it is only METHOD-level safe: a SEQUENCE of calls is still not");
        System.out.println("  atomic, so it rarely fixes a real concurrency bug anyway.");


        /* ====================================================================
         * SECTION 6 - WHEN PLAIN + IS PERFECTLY FINE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - DO NOT OVERCORRECT: + IS OFTEN RIGHT");
        System.out.println("=".repeat(74));

        String name = "Danish";
        int id = 42;

        // ONE expression. javac compiles this efficiently - historically into a
        // StringBuilder, and since Java 9 into an invokedynamic call to
        // StringConcatFactory, which the JIT optimises further.
        String good = "User " + name + " has id " + id;

        // The same thing written manually: more code, no benefit.
        String pointless = new StringBuilder()
                .append("User ").append(name).append(" has id ").append(id).toString();

        System.out.println("  With + :  " + good);
        System.out.println("  Manual :  " + pointless);
        System.out.println("  Identical? " + good.equals(pointless));
        System.out.println();
        System.out.println("  The manual version is WORSE: more code, harder to read, and");
        System.out.println("  exactly zero performance gain. The compiler already did it.");

        System.out.println();
        System.out.println("  THE RULE IS ABOUT LOOPS, NOT ABOUT +:");
        System.out.println("    fixed pieces in one expression  -> use +");
        System.out.println("    concatenation inside a LOOP     -> use StringBuilder");
        System.out.println("    building from a collection      -> use String.join / joining");
        System.out.println("    a template with formatting      -> use String.format");


        /* ====================================================================
         * SECTION 7 - JOINING A COLLECTION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - JOINING, AND THE TRAILING-SEPARATOR BUG");
        System.out.println("=".repeat(74));

        List<String> cities = List.of("Bengaluru", "Delhi", "Mumbai");
        List<String> nothing = List.of();

        System.out.println("  The classic manual approach, with its classic bug:");
        System.out.println();
        System.out.println("      StringBuilder sb = new StringBuilder();");
        System.out.println("      for (String s : list) sb.append(s).append(\", \");");
        System.out.println("      sb.setLength(sb.length() - 2);   // strip trailing \", \"");
        System.out.println();
        System.out.println("    with " + cities + ":");
        System.out.println("      -> \"" + manualJoin(cities) + "\"   works");
        System.out.println("    with an EMPTY list:");
        System.out.println("      -> " + manualJoin(nothing));

        System.out.println();
        System.out.println("  String.join - no separator logic to get wrong:");
        System.out.println("    String.join(\", \", cities)  -> \"" + String.join(", ", cities) + "\"");
        System.out.println("    String.join(\", \", empty)   -> \"" + String.join(", ", nothing)
                + "\"   (empty, no crash)");

        System.out.println();
        System.out.println("  Collectors.joining - adds a prefix and suffix too:");
        String collected = cities.stream()
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("    joining(\", \", \"[\", \"]\")     -> " + collected);

        System.out.println();
        System.out.println("  StringJoiner - incremental, with separators handled:");
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        joiner.add("Bengaluru").add("Delhi").add("Mumbai");
        System.out.println("    joiner.add(...).add(...)   -> " + joiner);

        StringJoiner emptyJoiner = new StringJoiner(", ", "[", "]");
        emptyJoiner.setEmptyValue("(none)");
        System.out.println("    with setEmptyValue(\"(none)\") and nothing added -> " + emptyJoiner);
        System.out.println();
        System.out.println("  StringJoiner is what Collectors.joining uses internally. Reach");
        System.out.println("  for it when you need incremental building WITH separators and");
        System.out.println("  a stream does not fit.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 15.");
        System.out.println("=".repeat(74));
    }

    /**
     * Builds a string of the given length using += , which is O(n^2) because
     * every iteration copies everything accumulated so far.
     *
     * @param count how many pieces to append
     * @return elapsed milliseconds
     */
    static long timeConcatenation(int count) {
        long start = System.nanoTime();
        String result = "";
        for (int i = 0; i < count; i++) {
            result += "x";
        }
        long elapsed = System.nanoTime() - start;
        if (result.length() == -1) System.out.print("");   // keep the loop alive
        return elapsed / 1_000_000;
    }

    /**
     * Builds the same string with a StringBuilder, which is O(n) because it
     * writes into one growable buffer.
     *
     * @param count how many pieces to append
     * @return elapsed milliseconds
     */
    static long timeBuilder(int count) {
        long start = System.nanoTime();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("x");
        }
        String result = builder.toString();
        long elapsed = System.nanoTime() - start;
        if (result.length() == -1) System.out.print("");
        return elapsed / 1_000_000;
    }

    /**
     * Reverses a string. String itself has no reverse() because it is
     * immutable, so this is the standard idiom.
     *
     * @param input the text to reverse
     * @return the reversed text
     */
    static String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * Joins with a manual StringBuilder, including the classic trailing-
     * separator strip - and its classic failure on an empty list. Kept
     * deliberately buggy to demonstrate why String.join exists.
     *
     * @param items the strings to join
     * @return the joined text, or a description of the failure
     */
    static String manualJoin(List<String> items) {
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            builder.append(item).append(", ");
        }
        try {
            builder.setLength(builder.length() - 2);   // strip the trailing ", "
            return builder.toString();
        } catch (StringIndexOutOfBoundsException e) {
            return "StringIndexOutOfBoundsException - length was 0, so length - 2 is -2";
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Change the iteration counts in Section 1 to 100,000 and 200,000. Confirm
 *    the += column still quadruples. Then explain the ratio column to someone.
 *
 * 2. Write `String repeat(String s, int n)` three ways: with +=, with a
 *    StringBuilder, and with s.repeat(n). Time all three at n = 50,000.
 *
 * 3. Presize a StringBuilder to exactly the length you will need, and time it
 *    against an unsized one for 10,000,000 appends. Is the gap what you
 *    expected?
 *
 * 4. Write `String toCsvRow(List<String> fields)` that quotes any field
 *    containing a comma. Do it with StringJoiner. Handle an empty list.
 *
 * 5. Fix manualJoin() so it works on an empty list. Then note that you have
 *    just reimplemented String.join badly, and delete it.
 *
 * 6. Predict, then verify:
 *        StringBuilder a = new StringBuilder("x");
 *        StringBuilder b = new StringBuilder("x");
 *        System.out.println(a.equals(b));
 *        System.out.println(a.toString().equals(b.toString()));
 *    StringBuilder does NOT override equals. Why is that arguably correct for
 *    a mutable type? (Hint: think about using one as a HashMap key.)
 * ============================================================================
 */
