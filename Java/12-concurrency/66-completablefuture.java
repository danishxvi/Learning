/* ============================================================================
 * 66 - CompletableFuture and async pipelines
 * ----------------------------------------------------------------------------
 * Companion lesson: 66-completablefuture.md
 *
 * RUN IT:
 *     java Java/12-concurrency/66-completablefuture.java
 *
 * Section 2 reproduces a REAL, surprising thread-naming fact: thenApply can
 * run on the ORIGINAL CALLING thread, not a worker thread, depending on
 * TIMING. Section 3 reproduces the real DOUBLY-WRAPPED type thenApply
 * produces when the mapping function itself returns a CompletableFuture.
 * ============================================================================
 */

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

class CompletableFutureLesson {

    public static void main(String[] args) throws Exception {

        /* ====================================================================
         * SECTION 1 - supplyAsync/runAsync: WHERE THE WORK ACTUALLY RUNS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - supplyAsync: A RESULT COMPUTED SOMEWHERE ELSE");
        System.out.println("=".repeat(74));

        System.out.println("    main() is running on -> " + Thread.currentThread().getName());

        CompletableFuture<String> greeting = CompletableFuture.supplyAsync(() -> {
            System.out.println("      supplyAsync's task ran on -> " + Thread.currentThread().getName());
            return "hello";
        });
        System.out.println("    supplyAsync() RETURNED IMMEDIATELY - main() did NOT block here.");
        System.out.println("    greeting.get() (BLOCKS until ready) -> " + greeting.get());
        System.out.println();
        System.out.println("    WITH NO EXECUTOR ARGUMENT, supplyAsync uses the SHARED");
        System.out.println("    ForkJoinPool.commonPool() - the SAME pool Stream's parallelStream()");
        System.out.println("    (lesson 54) uses internally. For genuinely I/O-bound work, or to");
        System.out.println("    avoid competing with parallel streams for the SAME pool, pass an");
        System.out.println("    EXPLICIT Executor (lesson 64) instead: supplyAsync(supplier, executor).");


        /* ====================================================================
         * SECTION 2 - thenApply: WHICH THREAD RUNS IT DEPENDS ON TIMING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - thenApply's THREAD DEPENDS ON WHETHER IT WAS ALREADY DONE");
        System.out.println("=".repeat(74));

        CompletableFuture<String> alreadyDone = CompletableFuture.supplyAsync(() -> "value");
        Thread.sleep(200);   // give it PLENTY of time to actually finish first
        System.out.println("    alreadyDone.isDone() before chaining -> " + alreadyDone.isDone());
        alreadyDone.thenApply(s -> {
            System.out.println("      thenApply on an ALREADY-COMPLETE future ran on -> "
                    + Thread.currentThread().getName());
            return s.toUpperCase();
        }).get();

        CompletableFuture<String> stillRunning = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(200);
            return "value";
        });
        stillRunning.thenApply(s -> {
            System.out.println("      thenApply chained BEFORE completion ran on -> "
                    + Thread.currentThread().getName());
            return s.toUpperCase();
        }).get();
        System.out.println();
        System.out.println("    REAL, VERIFIED RESULT: chaining thenApply onto a future that has");
        System.out.println("    ALREADY completed runs the callback ON THE CALLING THREAD (\"main\"");
        System.out.println("    here) - NOT a pool thread. Chaining it BEFORE completion runs it on");
        System.out.println("    whichever pool thread ends up COMPLETING the future. This is NOT a");
        System.out.println("    bug - it is documented behavior - but it means thenApply's thread");
        System.out.println("    is genuinely UNPREDICTABLE from the code alone. thenApplyAsync (with");
        System.out.println("    no explicit executor) ALWAYS uses the common pool, REGARDLESS of");
        System.out.println("    timing - use it whenever which thread runs the callback matters.");


        /* ====================================================================
         * SECTION 3 - thenApply VS thenCompose: THE NESTED-FUTURE TRAP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - thenApply WITH AN ASYNC MAPPING FUNCTION: A REAL TRAP");
        System.out.println("=".repeat(74));

        System.out.println("    A COMMON MISTAKE: calling ANOTHER async operation inside thenApply:");
        CompletableFuture<CompletableFuture<String>> nested = CompletableFuture
                .supplyAsync(() -> "x")
                .thenApply(s -> CompletableFuture.supplyAsync(() -> s + "y"));
        System.out.println("      CompletableFuture<CompletableFuture<String>> nested = ...thenApply(s ->");
        System.out.println("          CompletableFuture.supplyAsync(() -> s + \"y\"));");
        System.out.println("      REQUIRES A DOUBLE .get().get() to reach the actual value:");
        System.out.println("      nested.get().get() -> \"" + nested.get().get() + "\"");

        System.out.println();
        System.out.println("    THE FIX - thenCompose, for when the NEXT step is ITSELF async:");
        CompletableFuture<String> flat = CompletableFuture
                .supplyAsync(() -> "x")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "y"));
        System.out.println("      CompletableFuture<String> flat = ...thenCompose(s ->");
        System.out.println("          CompletableFuture.supplyAsync(() -> s + \"y\"));");
        System.out.println("      ONE .get() reaches the value directly:");
        System.out.println("      flat.get() -> \"" + flat.get() + "\"");
        System.out.println();
        System.out.println("    THE RULE: if your mapping function returns a PLAIN value, use");
        System.out.println("    thenApply. If it returns ANOTHER CompletableFuture (because it");
        System.out.println("    starts ANOTHER async operation), use thenCompose - it FLATTENS the");
        System.out.println("    result instead of nesting it, exactly like Stream's flatMap");
        System.out.println("    (lesson 54) flattens a stream of streams.");


        /* ====================================================================
         * SECTION 4 - thenCombine: TWO INDEPENDENT FUTURES, ONE RESULT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - thenCombine: MERGING TWO INDEPENDENT ASYNC RESULTS");
        System.out.println("=".repeat(74));

        CompletableFuture<Integer> priceFuture = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(100);
            return 50;
        });
        CompletableFuture<Integer> quantityFuture = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(150);
            return 3;
        });
        CompletableFuture<Integer> total = priceFuture.thenCombine(quantityFuture, (price, qty) -> price * qty);
        System.out.println("    price and quantity computed INDEPENDENTLY and CONCURRENTLY,");
        System.out.println("    combined once BOTH are ready -> total = " + total.get());
        System.out.println();
        System.out.println("    thenCompose is for a CHAIN (step 2 needs step 1's result to even");
        System.out.println("    START). thenCombine is for a FORK/JOIN (two INDEPENDENT operations,");
        System.out.println("    running CONCURRENTLY, whose results are combined at the end).");


        /* ====================================================================
         * SECTION 5 - EXCEPTION HANDLING: exceptionally, handle
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - EXCEPTIONS PROPAGATE THROUGH THE CHAIN, REAL PROOF");
        System.out.println("=".repeat(74));

        CompletableFuture<Integer> failing = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("simulated failure");
        });

        try {
            failing.get();
        } catch (ExecutionException e) {
            System.out.println("    failing.get() -> ExecutionException, cause: \""
                    + e.getCause().getMessage() + "\"   (the ORIGINAL exception, WRAPPED)");
        }

        CompletableFuture<Integer> recovered = failing.exceptionally(ex -> {
            System.out.println("      .exceptionally() caught: " + ex.getCause().getMessage());
            return -1;
        });
        System.out.println("    recovered.get() -> " + recovered.get() + "   (the FALLBACK value)");
        System.out.println();
        System.out.println("    .exceptionally(fn) runs ONLY if an earlier stage threw - like a");
        System.out.println("    catch block for the WHOLE chain up to this point. .handle((result,");
        System.out.println("    ex) -> ...) runs EITHER way, letting ONE callback see BOTH the");
        System.out.println("    success value AND the exception (whichever is non-null).");


        /* ====================================================================
         * SECTION 6 - orTimeout: A REAL, GENUINE TIMEOUT ON THE FUTURE ITSELF
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - orTimeout: THE FUTURE FAILS ITSELF IF TOO SLOW");
        System.out.println("=".repeat(74));

        CompletableFuture<Integer> slow = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(3000);
            return 99;
        });
        CompletableFuture<Integer> bounded = slow.orTimeout(300, TimeUnit.MILLISECONDS);
        try {
            bounded.get();
        } catch (ExecutionException e) {
            System.out.println("    slow.orTimeout(300, MILLISECONDS).get() -> ExecutionException,"
                    + " cause: " + e.getCause().getClass().getSimpleName());
        }
        System.out.println();
        System.out.println("    UNLIKE lesson 64's Future.get(timeout, unit) - which only makes");
        System.out.println("    THAT ONE CALL give up waiting, leaving the task still running -");
        System.out.println("    orTimeout makes the FUTURE ITSELF complete exceptionally after the");
        System.out.println("    deadline, so EVERY downstream stage chained onto it sees the");
        System.out.println("    failure too, not just one particular caller.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 66.");
        System.out.println("=".repeat(74));
    }

    static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Build a THREE-stage pipeline with thenApply that fetches a number,
 *    doubles it, then formats it as a String - confirm the final type is
 *    CompletableFuture<String>, not nested.
 *
 * 2. Reproduce Section 2's thread-naming experiment with thenApplyAsync
 *    instead of thenApply in BOTH the "already done" and "still running"
 *    cases - confirm it uses a pool thread EVERY time, unlike thenApply.
 *
 * 3. Chain THREE independent CompletableFutures with allOf, and write the
 *    pattern for extracting all three RESULTS afterward (allOf itself
 *    returns CompletableFuture<Void> - look up how to get the individual
 *    values back out).
 *
 * 4. Use .handle((result, ex) -> ...) instead of .exceptionally() on a
 *    chain that sometimes throws and sometimes does not - confirm handle
 *    runs in BOTH cases, with result/ex correctly non-null in each.
 *
 * 5. Compare Section 6's orTimeout with completeOnTimeout(fallbackValue,
 *    time, unit) - which one lets the CHAIN continue with a DEFAULT value
 *    instead of failing outright? Demonstrate the difference for real.
 * ============================================================================
 */
