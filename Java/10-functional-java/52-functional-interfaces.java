/* ============================================================================
 * 52 - Built-in functional interfaces
 * ----------------------------------------------------------------------------
 * Companion lesson: 52-functional-interfaces.md
 *
 * RUN IT:
 *     java Java/10-functional-java/52-functional-interfaces.java
 *
 * java.util.function ships about 40 interfaces so you rarely need to write
 * your own. Section 4 measures the real cost of NOT using the primitive
 * specializations - the autoboxing tax from lesson 42, paid on every call.
 * ============================================================================
 */

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

class FunctionalInterfaces {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE CORE FOUR
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE CORE FOUR: Function, Consumer, Supplier, Predicate");
        System.out.println("=".repeat(74));

        Function<String, Integer> length = String::length;
        System.out.println("    Function<String, Integer>   T -> R    length.apply(\"hello\") -> "
                + length.apply("hello"));

        Consumer<String> printer = s -> System.out.println("      consumed: " + s);
        System.out.println("    Consumer<String>             T -> void");
        printer.accept("a value with nothing returned");

        Supplier<String> greeting = () -> "generated on demand";
        System.out.println("    Supplier<String>             () -> T    supplier.get() -> "
                + greeting.get());

        Predicate<String> isLong = s -> s.length() > 5;
        System.out.println("    Predicate<String>            T -> boolean");
        System.out.println("      isLong.test(\"hi\")     -> " + isLong.test("hi"));
        System.out.println("      isLong.test(\"greetings\") -> " + isLong.test("greetings"));

        System.out.println();
        System.out.println("    EVERY OTHER interface in java.util.function is a VARIATION on");
        System.out.println("    one of these four: more arguments (Bi-), a primitive instead of");
        System.out.println("    a boxed type (Int-/Long-/Double-), or both.");


        /* ====================================================================
         * SECTION 2 - THE Bi- VARIANTS: TWO ARGUMENTS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Bi- VARIANTS: TWO ARGUMENTS IN");
        System.out.println("=".repeat(74));

        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
        System.out.println("    BiFunction<A, B, R>   (A, B) -> R    add.apply(3, 4) -> "
                + add.apply(3, 4));

        BiConsumer<String, Integer> logPair = (name, value) ->
                System.out.println("      " + name + " = " + value);
        System.out.println("    BiConsumer<A, B>      (A, B) -> void");
        logPair.accept("count", 42);

        BiPredicate<String, Integer> matchesLength = (s, len) -> s.length() == len;
        System.out.println("    BiPredicate<A, B>     (A, B) -> boolean");
        System.out.println("      matchesLength.test(\"hello\", 5) -> " + matchesLength.test("hello", 5));


        /* ====================================================================
         * SECTION 3 - UnaryOperator AND BinaryOperator: SAME-TYPE SPECIAL CASES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - UnaryOperator/BinaryOperator: Function WHERE R == T");
        System.out.println("=".repeat(74));

        UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
        System.out.println("    UnaryOperator<T> extends Function<T, T>");
        System.out.println("      shout.apply(\"hello\") -> " + shout.apply("hello"));

        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);
        System.out.println("    BinaryOperator<T> extends BiFunction<T, T, T>");
        System.out.println("      BinaryOperator.maxBy(...).apply(3, 9) -> " + max.apply(3, 9));

        System.out.println();
        System.out.println("    WHY THEY EXIST SEPARATELY: List.replaceAll(UnaryOperator<E>) and");
        System.out.println("    Collections' reduction helpers specifically need INPUT TYPE ==");
        System.out.println("    OUTPUT TYPE - a plain Function<T, R> does not encode that");
        System.out.println("    guarantee in its signature, UnaryOperator<T> does.");

        java.util.List<String> words = new java.util.ArrayList<>(
                java.util.List.of("delta", "alpha", "charlie"));
        words.replaceAll(shout);
        System.out.println("      List.replaceAll(shout) -> " + words);


        /* ====================================================================
         * SECTION 4 - PRIMITIVE SPECIALIZATIONS: THE REAL COST OF NOT USING THEM
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - IntUnaryOperator VS Function<Integer, Integer>: MEASURED");
        System.out.println("=".repeat(74));

        System.out.println("  Lesson 42 covered autoboxing's cost in the abstract. Here it is");
        System.out.println("  paid on every single call of a HOT loop:");

        int iterations = 200_000_000;

        Function<Integer, Integer> boxedSquare = x -> x * x;
        IntUnaryOperator primitiveSquare = x -> x * x;

        long boxedStart = System.nanoTime();
        long boxedSum = 0;
        for (int i = 0; i < iterations; i++) {
            boxedSum += boxedSquare.apply(i % 1000);   // int -> Integer -> int, EVERY call
        }
        long boxedMillis = (System.nanoTime() - boxedStart) / 1_000_000;

        long primitiveStart = System.nanoTime();
        long primitiveSum = 0;
        for (int i = 0; i < iterations; i++) {
            primitiveSum += primitiveSquare.applyAsInt(i % 1000);   // pure int, no boxing
        }
        long primitiveMillis = (System.nanoTime() - primitiveStart) / 1_000_000;

        System.out.printf("    %,d calls:%n", iterations);
        System.out.println("      Function<Integer,Integer> (boxed) -> " + boxedMillis + " ms");
        System.out.println("      IntUnaryOperator (primitive)      -> " + primitiveMillis + " ms");
        System.out.println("      (checksums " + boxedSum + " / " + primitiveSum + " match: "
                + (boxedSum == primitiveSum) + ")");
        System.out.println();
        System.out.println("    Function<Integer, Integer> boxes the int argument into an Integer");
        System.out.println("    to call apply(), then unboxes the returned Integer back to a long");
        System.out.println("    for the sum - TWO allocations' worth of work per call (cached for");
        System.out.println("    small values per lesson 42's Integer cache, but still real work,");
        System.out.println("    and the cache does not cover the full int range). Over hundreds of");
        System.out.println("    millions of calls in a hot path, that difference is real money.");
        System.out.println();
        System.out.println("    java.util.function ships IntFunction, ToIntFunction, IntPredicate,");
        System.out.println("    IntSupplier, IntConsumer, IntUnaryOperator, IntBinaryOperator -");
        System.out.println("    and the same set for long and double - specifically so hot code");
        System.out.println("    never has to pay this tax.");


        /* ====================================================================
         * SECTION 5 - COMPOSING FUNCTIONS: andThen, compose, and, or, negate
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE DEFAULT METHODS THAT COMPOSE THESE INTERFACES");
        System.out.println("=".repeat(74));

        Function<Integer, Integer> timesTwo = x -> x * 2;
        Function<Integer, Integer> plusThree = x -> x + 3;

        Function<Integer, Integer> twoThenThree = timesTwo.andThen(plusThree);
        Function<Integer, Integer> threeThenTwo = timesTwo.compose(plusThree);
        System.out.println("    timesTwo.andThen(plusThree).apply(5) -> " + twoThenThree.apply(5)
                + "   ((5*2)+3 - andThen runs THIS first, then the argument)");
        System.out.println("    timesTwo.compose(plusThree).apply(5) -> " + threeThenTwo.apply(5)
                + "   ((5+3)*2 - compose runs the ARGUMENT first, then this)");

        Predicate<String> notEmpty = s -> !s.isEmpty();
        Predicate<String> startsWithA = s -> s.startsWith("A");
        Predicate<String> combined = notEmpty.and(startsWithA);
        Predicate<String> negated = startsWithA.negate();
        System.out.println();
        System.out.println("    notEmpty.and(startsWithA).test(\"Apple\") -> " + combined.test("Apple"));
        System.out.println("    notEmpty.and(startsWithA).test(\"Banana\") -> " + combined.test("Banana"));
        System.out.println("    startsWithA.negate().test(\"Apple\")      -> " + negated.test("Apple"));

        Consumer<String> log = s -> System.out.println("      LOG: " + s);
        Consumer<String> alsoUppercase = s -> System.out.println("      UPPER: " + s.toUpperCase());
        Consumer<String> both = log.andThen(alsoUppercase);
        System.out.println();
        System.out.println("    log.andThen(alsoUppercase).accept(\"hi\") runs BOTH, in order:");
        both.accept("hi");


        /* ====================================================================
         * SECTION 6 - @FunctionalInterface: A COMPILE-TIME PROMISE, NOT MAGIC
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - @FunctionalInterface IS A COMPILE-TIME CHECK, NOTHING MORE");
        System.out.println("=".repeat(74));

        System.out.println("    An interface with EXACTLY ONE abstract method is a valid lambda");
        System.out.println("    target whether or not it is annotated - @FunctionalInterface adds");
        System.out.println("    NO runtime behavior at all. What it DOES do is make the compiler");
        System.out.println("    ENFORCE that promise. Add a second abstract method and watch it");
        System.out.println("    refuse to compile:");
        System.out.println();
        System.out.println("      @FunctionalInterface");
        System.out.println("      interface Broken {");
        System.out.println("          void first();");
        System.out.println("          void second();");
        System.out.println("      }");
        System.out.println();
        System.out.println("      javac TwoAbstractMethods.java");
        System.out.println("        error: Unexpected @FunctionalInterface annotation");
        System.out.println("        @FunctionalInterface");
        System.out.println("        ^");
        System.out.println("          Broken is not a functional interface");
        System.out.println("            multiple non-overriding abstract methods found in interface Broken");
        System.out.println();
        System.out.println("    DEFAULT and STATIC methods do NOT count toward the limit - THIS");
        System.out.println("    is why Function can have default andThen()/compose() AND still be");
        System.out.println("    a valid lambda target: it has exactly one ABSTRACT method (apply),");
        System.out.println("    plus as many default/static methods as it wants.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 52.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write TwoAbstractMethods.java yourself (Section 6), compile it, and
 *    read the exact error on your JDK. Then remove @FunctionalInterface -
 *    does it still fail to compile? Why or why not?
 *
 * 2. Section 4 measures Integer boxing cost for squares. Repeat the
 *    measurement for BiFunction<Integer,Integer,Integer> (boxed) versus
 *    IntBinaryOperator (primitive) doing addition instead.
 *
 * 3. Chain FOUR Function<Integer,Integer> transformations with andThen and
 *    predict the final result on input 1 before running it.
 *
 * 4. Write a Predicate<Integer> isEven and isPositive, combine them with
 *    .and(), then use it to filter a List<Integer> with removeIf
 *    (lesson 50) combined with .negate().
 *
 * 5. IntPredicate is used below only in the imports. Write one that checks
 *    "is a perfect square" and test it against 0 through 20, printing
 *    which pass.
 * ============================================================================
 */
