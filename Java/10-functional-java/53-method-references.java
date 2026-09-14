/* ============================================================================
 * 53 - Method references
 * ----------------------------------------------------------------------------
 * Companion lesson: 53-method-references.md
 *
 * RUN IT:
 *     java Java/10-functional-java/53-method-references.java
 *
 * There are FOUR kinds, and the third one - "unbound instance method
 * reference" - is the one nobody's intuition gets right the first time.
 * Section 3 works through exactly what its "extra" parameter really is.
 * ============================================================================
 */

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

class MethodReferences {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - KIND 1: STATIC METHOD REFERENCE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - KIND 1: ClassName::staticMethod");
        System.out.println("=".repeat(74));

        Function<String, Integer> parseAsLambda = s -> Integer.parseInt(s);
        Function<String, Integer> parseAsRef = Integer::parseInt;
        System.out.println("    lambda:  s -> Integer.parseInt(s)   .apply(\"42\") -> "
                + parseAsLambda.apply("42"));
        System.out.println("    ref:     Integer::parseInt           .apply(\"42\") -> "
                + parseAsRef.apply("42"));
        System.out.println("    The method IS static; the reference just points at it directly -");
        System.out.println("    no instance involved anywhere.");


        /* ====================================================================
         * SECTION 2 - KIND 2: BOUND INSTANCE METHOD REFERENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - KIND 2: instance::instanceMethod  (BOUND to ONE object)");
        System.out.println("=".repeat(74));

        String greeting = "Hello, World!";
        Supplier<Integer> lengthAsLambda = () -> greeting.length();
        Supplier<Integer> lengthAsRef = greeting::length;
        System.out.println("    String greeting = \"Hello, World!\";");
        System.out.println("    lambda:  () -> greeting.length()   .get() -> " + lengthAsLambda.get());
        System.out.println("    ref:     greeting::length           .get() -> " + lengthAsRef.get());
        System.out.println("    \"greeting\" is a SPECIFIC, already-existing object - the reference");
        System.out.println("    is permanently BOUND to it, exactly like a lambda that CAPTURES it.");


        /* ====================================================================
         * SECTION 3 - KIND 3: UNBOUND INSTANCE METHOD REFERENCE (THE TRICKY ONE)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - KIND 3: ClassName::instanceMethod  (UNBOUND - no object yet)");
        System.out.println("=".repeat(74));

        Function<String, Integer> lengthUnboundAsLambda = s -> s.length();
        Function<String, Integer> lengthUnboundAsRef = String::length;
        System.out.println("    lambda:  s -> s.length()      .apply(\"hello\") -> "
                + lengthUnboundAsLambda.apply("hello"));
        System.out.println("    ref:     String::length        .apply(\"hello\") -> "
                + lengthUnboundAsRef.apply("hello"));
        System.out.println();
        System.out.println("    THE KEY DIFFERENCE FROM KIND 2: there is NO specific String yet -");
        System.out.println("    String::length is a reference to the METHOD ITSELF. The functional");
        System.out.println("    interface's FIRST parameter becomes the RECEIVER the method is");
        System.out.println("    called ON, and any REMAINING parameters become the method's OWN");
        System.out.println("    arguments. This generalizes to methods that take arguments too:");

        BiFunction<String, String, Boolean> startsWithAsLambda = (s, prefix) -> s.startsWith(prefix);
        BiFunction<String, String, Boolean> startsWithAsRef = String::startsWith;
        System.out.println();
        System.out.println("    BiFunction<String, String, Boolean>:");
        System.out.println("      lambda: (s, prefix) -> s.startsWith(prefix)");
        System.out.println("      ref:    String::startsWith");
        System.out.println("      startsWithAsRef.apply(\"hello\", \"he\") -> "
                + startsWithAsRef.apply("hello", "he"));
        System.out.println("        the FIRST arg (\"hello\") became the RECEIVER  s.startsWith(...)");
        System.out.println("        the SECOND arg (\"he\") became startsWith's OWN parameter");
        System.out.println();
        System.out.println("    This exact shape - unbound instance method reference used as a");
        System.out.println("    two-argument functional interface - is precisely how");
        System.out.println("    list.sort(String::compareTo) works as a Comparator<String>:");

        List<String> names = new java.util.ArrayList<>(List.of("delta", "alpha", "charlie"));
        names.sort(String::compareTo);
        System.out.println("      names.sort(String::compareTo) -> " + names);


        /* ====================================================================
         * SECTION 4 - KIND 4: CONSTRUCTOR REFERENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - KIND 4: ClassName::new");
        System.out.println("=".repeat(74));

        Supplier<java.util.ArrayList<String>> newListAsLambda = () -> new java.util.ArrayList<>();
        Supplier<java.util.ArrayList<String>> newListAsRef = java.util.ArrayList::new;
        System.out.println("    lambda:  () -> new ArrayList<>()");
        System.out.println("    ref:     ArrayList::new");
        System.out.println("    newListAsRef.get() creates a genuinely new instance each call -> "
                + (newListAsRef.get() != newListAsRef.get()));

        Function<String, StringBuilder> newBuilderFromString = StringBuilder::new;
        System.out.println();
        System.out.println("    Function<String, StringBuilder> = StringBuilder::new");
        System.out.println("      picks the CONSTRUCTOR OVERLOAD matching the functional");
        System.out.println("      interface's shape - here StringBuilder(String):");
        System.out.println("      newBuilderFromString.apply(\"seed\").append(\"!\") -> "
                + newBuilderFromString.apply("seed").append("!"));

        System.out.println();
        System.out.println("    ARRAY constructor references - int[]::new - exist specifically");
        System.out.println("    for Stream.toArray(IntFunction<T[]>), so a stream can allocate an");
        System.out.println("    array of exactly the right size ITSELF:");
        IntFunction<String[]> arrayMaker = String[]::new;
        String[] made = arrayMaker.apply(3);
        System.out.println("      String[]::new applied to 3 -> array of length " + made.length);


        /* ====================================================================
         * SECTION 5 - OVERLOAD RESOLUTION: THE TARGET TYPE DECIDES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - A METHOD REFERENCE TO AN OVERLOADED METHOD");
        System.out.println("=".repeat(74));

        System.out.println("    println() has MANY overloads: println(String), println(int),");
        System.out.println("    println(Object), println(boolean), ... Which one does");
        System.out.println("    System.out::println resolve to? Whichever matches the TARGET");
        System.out.println("    functional interface's parameter type - resolved at COMPILE TIME,");
        System.out.println("    exactly like ordinary overload resolution (lesson 18):");

        java.util.function.Consumer<String> printString = System.out::println;
        java.util.function.IntConsumer printInt = System.out::println;
        System.out.println();
        System.out.println("      Consumer<String> printString = System.out::println;");
        System.out.println("        -> resolves to println(String) at compile time");
        System.out.println("      IntConsumer printInt = System.out::println;");
        System.out.println("        -> resolves to println(int) at compile time");
        System.out.println();
        System.out.println("    SAME method reference expression, TWO different overloads chosen,");
        System.out.println("    purely from the assignment's declared type. If the target type is");
        System.out.println("    genuinely ambiguous (matches more than one overload equally well),");
        System.out.println("    THAT is a real compile error - \"reference to println is ambiguous\".");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 53.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. For each of the four kinds in this lesson, write ONE example of your
 *    own (not copied from here) using a class you define yourself.
 *
 * 2. Explain, in one sentence each, why greeting::length (Section 2) and
 *    String::length (Section 3) have DIFFERENT functional interface
 *    shapes (Supplier<Integer> vs Function<String, Integer>) even though
 *    both ultimately call the SAME method.
 *
 * 3. Write a BiFunction<String, Integer, String> using String::substring
 *    (unbound, Kind 3) and explain which parameter becomes the receiver.
 *
 * 4. Create a class with two constructors, one taking a String and one
 *    taking an int, then write two DIFFERENT functional interface
 *    variables both assigned ClassName::new, letting the target type pick
 *    the right constructor for each.
 *
 * 5. Compile a case where a method reference to an overloaded method IS
 *    genuinely ambiguous (hint: two overloads with reference-compatible
 *    parameter types the compiler cannot distinguish from context) and
 *    read the real "reference to X is ambiguous" error.
 * ============================================================================
 */
