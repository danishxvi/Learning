/* ============================================================================
 * 54 - The Stream API
 * ----------------------------------------------------------------------------
 * Companion lesson: 54-streams-api.md
 *
 * RUN IT:
 *     java Java/10-functional-java/54-streams-api.java
 *
 * Section 2 proves laziness with actual print-ordering evidence, not a
 * diagram. Section 5 proves a stream is single-use with a real exception.
 * Section 7 breaks a parallel stream on purpose to show why "no side
 * effects" in stream operations is a real rule, not a style preference.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class StreamsApi {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - CREATING A STREAM, FIVE WAYS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - CREATING STREAMS");
        System.out.println("=".repeat(74));

        System.out.println("    List.of(1,2,3).stream()            -> "
                + List.of(1, 2, 3).stream().toList());
        System.out.println("    Stream.of(\"a\", \"b\", \"c\")           -> "
                + Stream.of("a", "b", "c").toList());
        System.out.println("    Arrays.stream(new int[]{4,5,6})     -> "
                + java.util.Arrays.toString(java.util.Arrays.stream(new int[]{4, 5, 6}).toArray()));
        System.out.println("    IntStream.range(0, 5)  (EXCLUSIVE)  -> "
                + IntStream.range(0, 5).boxed().toList());
        System.out.println("    IntStream.rangeClosed(0, 5)(INCLUSIVE)-> "
                + IntStream.rangeClosed(0, 5).boxed().toList());
        System.out.println("    Stream.iterate(1, x -> x * 2).limit(5)-> "
                + Stream.iterate(1, x -> x * 2).limit(5).toList());


        /* ====================================================================
         * SECTION 2 - LAZINESS, PROVEN WITH REAL PRINT ORDER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - INTERMEDIATE OPERATIONS ARE LAZY - PROVEN, NOT ASSERTED");
        System.out.println("=".repeat(74));

        System.out.println("  A stream pipeline with filter().map() does NOTHING until a TERMINAL");
        System.out.println("  operation is called. Watch the ACTUAL execution order using peek(),");
        System.out.println("  which prints every time an element genuinely flows through a stage:");

        System.out.println();
        System.out.println("    building the pipeline (filter -> peek -> map -> peek)...");
        Stream<Integer> pipeline = Stream.of(1, 2, 3, 4, 5)
                .filter(n -> {
                    System.out.println("      filter examining " + n);
                    return n % 2 == 0;
                })
                .peek(n -> System.out.println("      passed filter: " + n))
                .map(n -> n * 10);
        System.out.println("    ...pipeline BUILT. Nothing printed above THIS line - NOTHING has");
        System.out.println("    run yet, because there is no TERMINAL operation.");
        System.out.println();
        System.out.println("    NOW calling .toList() (a terminal operation):");
        List<Integer> result = pipeline.toList();
        System.out.println("    result -> " + result);
        System.out.println();
        System.out.println("    Notice ELEMENT-AT-A-TIME processing too: filter examined 1, THEN");
        System.out.println("    (since 1 failed) went straight to examining 2 - it did NOT filter");
        System.out.println("    everything, then peek everything, then map everything. Each element");
        System.out.println("    flows through the WHOLE pipeline before the next one starts.");


        /* ====================================================================
         * SECTION 3 - THE CORE INTERMEDIATE OPERATIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - filter, map, flatMap, sorted, distinct, limit, skip");
        System.out.println("=".repeat(74));

        List<Integer> nums = List.of(5, 3, 8, 3, 1, 9, 5, 2);
        System.out.println("    source                           -> " + nums);
        System.out.println("    .filter(n -> n > 3)               -> "
                + nums.stream().filter(n -> n > 3).toList());
        System.out.println("    .map(n -> n * n)                  -> "
                + nums.stream().map(n -> n * n).toList());
        System.out.println("    .distinct()                       -> "
                + nums.stream().distinct().toList());
        System.out.println("    .sorted()                         -> "
                + nums.stream().sorted().toList());
        System.out.println("    .limit(3)                         -> "
                + nums.stream().limit(3).toList());
        System.out.println("    .skip(3)                          -> "
                + nums.stream().skip(3).toList());

        System.out.println();
        System.out.println("    flatMap - FLATTENING a stream of collections into ONE stream:");
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        System.out.println("      nested          -> " + nested);
        System.out.println("      .map(List::stream)     would give a Stream<Stream<Integer>>");
        System.out.println("      .flatMap(List::stream) -> "
                + nested.stream().flatMap(List::stream).toList());


        /* ====================================================================
         * SECTION 4 - THE CORE TERMINAL OPERATIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - forEach, reduce, collect, count, anyMatch, findFirst");
        System.out.println("=".repeat(74));

        System.out.println("    .count()                          -> " + nums.stream().filter(n -> n > 3).count());
        System.out.println("    .anyMatch(n -> n > 8)              -> " + nums.stream().anyMatch(n -> n > 8));
        System.out.println("    .allMatch(n -> n > 0)              -> " + nums.stream().allMatch(n -> n > 0));
        System.out.println("    .noneMatch(n -> n > 100)           -> " + nums.stream().noneMatch(n -> n > 100));
        System.out.println("    .findFirst()                       -> " + nums.stream().filter(n -> n > 3).findFirst());
        System.out.println("    .max(Integer::compareTo)           -> " + nums.stream().max(Integer::compareTo));

        int sum = nums.stream().reduce(0, Integer::sum);
        System.out.println("    .reduce(0, Integer::sum)           -> " + sum);
        System.out.println();
        System.out.println("    reduce's THREE-arg form (identity, accumulator, combiner) exists");
        System.out.println("    for PARALLEL streams - the combiner merges partial results from");
        System.out.println("    different threads. On a sequential stream it is never even called:");
        int viaReduceThreeArg = nums.stream().reduce(0, (partial, n) -> partial + n, (a, b) -> {
            System.out.println("      combiner called with " + a + ", " + b + " - sequential streams never reach this");
            return a + b;
        });
        System.out.println("    result -> " + viaReduceThreeArg + "  (combiner line above never printed)");


        /* ====================================================================
         * SECTION 5 - A STREAM IS SINGLE-USE, FOR REAL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - A STREAM CAN ONLY BE CONSUMED ONCE");
        System.out.println("=".repeat(74));

        Stream<Integer> onceOnly = Stream.of(1, 2, 3);
        long firstCount = onceOnly.count();
        System.out.println("    first terminal op, count() -> " + firstCount);
        try {
            onceOnly.count();   // the SAME stream object, a SECOND terminal op
        } catch (IllegalStateException e) {
            System.out.println("    second terminal op on the SAME stream -> IllegalStateException");
            System.out.println("      \"" + e.getMessage() + "\"");
        }
        System.out.println();
        System.out.println("    A Stream is a ONE-TIME PIPELINE DESCRIPTION, not a reusable");
        System.out.println("    collection - unlike a List, which you can iterate as many times");
        System.out.println("    as you like. Need to run the same operations twice? Build a fresh");
        System.out.println("    stream from the SOURCE collection each time (list.stream() again).");


        /* ====================================================================
         * SECTION 6 - SHORT-CIRCUITING: findFirst/anyMatch STOP EARLY, MEASURED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - SHORT-CIRCUITING TERMINAL OPERATIONS, MEASURED");
        System.out.println("=".repeat(74));

        AtomicInteger examined = new AtomicInteger();
        java.util.Optional<Integer> firstOverAMillion = Stream.iterate(1, x -> x + 1)
                .peek(x -> examined.incrementAndGet())
                .filter(x -> x > 1_000_000)
                .findFirst();
        System.out.println("    Stream.iterate(1, x -> x + 1) is an INFINITE stream. Asking for");
        System.out.println("    findFirst() > 1,000,000:");
        System.out.println("      result            -> " + firstOverAMillion.get());
        System.out.println("      elements EXAMINED -> " + examined.get());
        System.out.println();
        System.out.println("    Exactly 1,000,001 elements were examined, not the whole (infinite)");
        System.out.println("    stream - findFirst() is SHORT-CIRCUITING: it stops pulling new");
        System.out.println("    elements through the pipeline the moment it has its answer. count()");
        System.out.println("    is NOT short-circuiting - it must see EVERY element to count them,");
        System.out.println("    which is exactly why count() on an infinite stream never returns.");


        /* ====================================================================
         * SECTION 7 - PARALLEL STREAMS: A REAL RACE CONDITION, ON PURPOSE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WHY STREAM OPERATIONS MUST NOT HAVE SIDE EFFECTS");
        System.out.println("=".repeat(74));

        List<Integer> million = IntStream.rangeClosed(1, 1_000_000).boxed().toList();

        System.out.println("  THE MISTAKE - a plain (non-thread-safe) counter incremented from");
        System.out.println("  inside a PARALLEL stream's forEach:");
        int[] brokenCounter = {0};
        million.parallelStream().forEach(n -> brokenCounter[0]++);   // data race - NOT synchronized
        System.out.println("    expected 1,000,000, got -> " + brokenCounter[0]
                + (brokenCounter[0] == 1_000_000 ? "   (correct this run - but NOT guaranteed)" : "   <- WRONG, a lost update"));
        System.out.println();
        System.out.println("    Multiple threads run n -> brokenCounter[0]++ CONCURRENTLY.");
        System.out.println("    That is READ, INCREMENT, WRITE - not atomic. Two threads can both");
        System.out.println("    read the same value before either writes back, and one increment");
        System.out.println("    is LOST. This may not reproduce every run - which is WORSE than a");
        System.out.println("    reliable crash, because it can pass testing and fail in production.");

        System.out.println();
        System.out.println("    THE FIX - either an AtomicInteger (thread-safe by construction):");
        AtomicInteger safeCounter = new AtomicInteger();
        million.parallelStream().forEach(n -> safeCounter.incrementAndGet());
        System.out.println("      AtomicInteger result -> " + safeCounter.get() + "   (always correct)");

        System.out.println();
        System.out.println("    ...or, far better, do not count via a side effect AT ALL - use the");
        System.out.println("    STREAM'S OWN count(), which parallelizes internally and correctly:");
        long realCount = million.parallelStream().count();
        System.out.println("      million.parallelStream().count() -> " + realCount);
        System.out.println();
        System.out.println("    THE ACTUAL RULE: lambdas passed to stream operations (map, filter,");
        System.out.println("    forEach, ...) should be STATELESS and free of side effects on");
        System.out.println("    shared mutable state. Sequential streams often get away with");
        System.out.println("    breaking this rule by accident; parallelStream() turns it into a");
        System.out.println("    real, unpredictable bug the moment multiple threads are involved.");


        /* ====================================================================
         * SECTION 8 - COLLECT: FROM A STREAM BACK TO A DATA STRUCTURE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - A PREVIEW OF collect() - LESSON 55 GOES DEEP HERE");
        System.out.println("=".repeat(74));

        List<String> upper = nums.stream()
                .map(n -> "n" + n)
                .collect(Collectors.toList());
        System.out.println("    .collect(Collectors.toList())   -> " + upper);

        String joined = nums.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("    .collect(Collectors.joining)    -> " + joined);

        System.out.println();
        System.out.println("    Lesson 55 covers groupingBy, partitioningBy and building your own");
        System.out.println("    Collector from scratch.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 54.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Take Section 2's pipeline and add a SECOND filter stage before map().
 *    Trace, by hand, the exact print order for the input [1,2,3,4,5]
 *    before running it, then verify.
 *
 * 2. Write a flatMap pipeline that takes a List<String> of sentences and
 *    produces a single flat List<String> of every WORD across all of them.
 *
 * 3. Reproduce Section 5's IllegalStateException with a DIFFERENT pair of
 *    terminal operations (e.g. forEach() then count()) on the same stream.
 *
 * 4. Change Section 6's threshold from 1,000,000 to 10,000,000 and predict
 *    how the "elements examined" count changes before running it.
 *
 * 5. Section 7's broken counter sometimes happens to print the CORRECT
 *    number anyway. Run it five times in a row and record how often it is
 *    wrong - explain why "it worked when I tested it" is not proof a
 *    parallel stream's side effects are safe.
 * ============================================================================
 */
