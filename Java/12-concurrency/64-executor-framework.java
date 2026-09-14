/* ============================================================================
 * 64 - The Executor framework
 * ----------------------------------------------------------------------------
 * Companion lesson: 64-executor-framework.md
 *
 * RUN IT:
 *     java Java/12-concurrency/64-executor-framework.java
 *
 * Section 5 documents a REAL hang that was deliberately reproduced
 * SEPARATELY, outside this file (an ExecutorService's threads are NOT
 * daemon threads by default, so forgetting shutdown() genuinely prevents
 * the JVM from exiting) - not attempted live HERE, because doing so would
 * make this very file unable to finish. Every other claim IS reproduced
 * live, right here, exactly like every other lesson in this curriculum.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ExecutorFramework {

    public static void main(String[] args) throws Exception {

        /* ====================================================================
         * SECTION 1 - execute() VS submit(): A RETURN VALUE, OR NOT
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - ExecutorService: DECOUPLING \"WHAT TO RUN\" FROM \"HOW\"");
        System.out.println("=".repeat(74));

        ExecutorService pool = Executors.newFixedThreadPool(3);

        pool.execute(() -> System.out.println("    execute(Runnable) - fire and forget, NO return value, on -> "
                + Thread.currentThread().getName()));

        Future<Integer> future = pool.submit(() -> {
            Thread.sleep(50);
            return 6 * 7;
        });
        System.out.println("    submit(Callable<Integer>) returns a Future IMMEDIATELY - the");
        System.out.println("    calling thread does NOT block here:");
        System.out.println("      future.isDone() right after submit() -> " + future.isDone());
        Integer result = future.get();   // NOW it blocks, until the task finishes
        System.out.println("      future.get() (BLOCKS until ready)    -> " + result);
        System.out.println();
        System.out.println("    execute() is for work with NO result and NO need to track");
        System.out.println("    completion. submit() gives back a Future - a HANDLE to a result");
        System.out.println("    that may not exist YET, exactly like a lightweight version of");
        System.out.println("    lesson 66's CompletableFuture.");


        /* ====================================================================
         * SECTION 2 - Future.get(timeout): A REAL TimeoutException
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Future.get(timeout, unit): A REAL TimeoutException");
        System.out.println("=".repeat(74));

        Future<Integer> slowTask = pool.submit(() -> {
            Thread.sleep(2000);
            return 1;
        });
        System.out.println("    submitted a task that sleeps 2000ms, waiting only 300ms for it:");
        try {
            slowTask.get(300, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println("      slowTask.get(300, MILLISECONDS) -> TimeoutException"
                    + "   (the TASK is still RUNNING - this call TIMED OUT, it did not CANCEL anything)");
        }
        slowTask.cancel(true);   // ACTUALLY stop waiting on it
        System.out.println();
        System.out.println("    A TIMED-OUT get() does NOT cancel the underlying task - it is");
        System.out.println("    STILL running in the pool unless you EXPLICITLY call");
        System.out.println("    future.cancel(true) (which attempts interrupt() on it, exactly");
        System.out.println("    like lesson 62/63's interrupt mechanics).");


        /* ====================================================================
         * SECTION 3 - COLLECTING RESULTS FROM MANY SUBMITTED TASKS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - SUBMITTING MANY TASKS, COLLECTING ALL THE RESULTS");
        System.out.println("=".repeat(74));

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int n = i;
            futures.add(pool.submit(() -> n * n));
        }
        List<Integer> squares = new ArrayList<>();
        for (Future<Integer> f : futures) {
            squares.add(f.get());   // each get() blocks only until ITS OWN task is done
        }
        System.out.println("    submitted 5 square-computing tasks, collected results -> " + squares);


        /* ====================================================================
         * SECTION 4 - THE DIFFERENT POOL SHAPES, AND WHAT EACH IS ACTUALLY FOR
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - Executors' FACTORY METHODS: DIFFERENT REAL SHAPES");
        System.out.println("=".repeat(74));

        ExecutorService fixedPool = Executors.newFixedThreadPool(2);
        List<String> fixedThreadNames = runFiveTasksAndCollectThreadNames(fixedPool);
        fixedPool.shutdown();
        System.out.println("    newFixedThreadPool(2), 5 quick tasks - threads used -> "
                + java.util.Set.copyOf(fixedThreadNames).size() + " distinct (capped at 2, REUSED)");

        ExecutorService cachedPool = Executors.newCachedThreadPool();
        List<String> cachedThreadNames = runFiveTasksAndCollectThreadNames(cachedPool);
        cachedPool.shutdown();
        System.out.println("    newCachedThreadPool(), 5 quick tasks - threads used  -> "
                + java.util.Set.copyOf(cachedThreadNames).size()
                + " distinct (GROWS as needed, no fixed cap, idle ones later time out)");

        ExecutorService singlePool = Executors.newSingleThreadExecutor();
        List<String> singleThreadNames = runFiveTasksAndCollectThreadNames(singlePool);
        singlePool.shutdown();
        System.out.println("    newSingleThreadExecutor(), 5 quick tasks - threads used -> "
                + java.util.Set.copyOf(singleThreadNames).size()
                + " distinct (ALWAYS exactly one - tasks run STRICTLY in order)");
        System.out.println();
        System.out.println("    newFixedThreadPool: a KNOWN, BOUNDED amount of work in flight -");
        System.out.println("    the usual default for CPU-bound or steady server workloads.");
        System.out.println("    newCachedThreadPool: bursty, SHORT-LIVED tasks - can grow");
        System.out.println("    UNBOUNDED under sustained load, a real resource-exhaustion risk if");
        System.out.println("    misused. newSingleThreadExecutor: a serial task queue with the");
        System.out.println("    SAME ExecutorService API as the others - useful when tasks MUST");
        System.out.println("    run one at a time, in submission order.");


        /* ====================================================================
         * SECTION 5 - shutdown() IS NOT OPTIONAL: A REAL, SEPARATELY-VERIFIED HANG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - shutdown() IS NOT OPTIONAL: A REAL, DOCUMENTED HANG");
        System.out.println("=".repeat(74));

        System.out.println("    THIS ONE IS NOT ATTEMPTED LIVE, ON PURPOSE - reproducing it here");
        System.out.println("    would make THIS FILE unable to finish, unlike lesson 63's daemon-");
        System.out.println("    thread-contained deadlock. It WAS verified for real, separately:");
        System.out.println();
        System.out.println("      ExecutorService pool = Executors.newFixedThreadPool(2);");
        System.out.println("      pool.submit(() -> System.out.println(\"task ran\"));");
        System.out.println("      // main() returns WITHOUT calling pool.shutdown()");
        System.out.println();
        System.out.println("      REAL RESULT: \"task ran\" printed, then the JVM did NOT exit -");
        System.out.println("      run with a 5-second timeout wrapper, the process was KILLED at");
        System.out.println("      the timeout (exit code 124), never having exited on its own.");
        System.out.println();
        System.out.println("    WHY: unlike a Thread you create yourself (lesson 62), the pool");
        System.out.println("    threads Executors creates are NOT daemon threads by default - they");
        System.out.println("    sit IDLE, waiting for more work, and a non-daemon thread genuinely");
        System.out.println("    blocks JVM shutdown FOREVER until it exits. shutdown() tells the");
        System.out.println("    pool to finish QUEUED/RUNNING work and then let those threads die.");

        System.out.println();
        System.out.println("    THE REAL FIX, verified - either call shutdown() EXPLICITLY (every");
        System.out.println("    ExecutorService in THIS file has been shutdown() or shutdownNow()'d):");
        System.out.println("    pool.shutdown()  -> lets QUEUED and RUNNING tasks finish, then stops");
        System.out.println("    pool.shutdownNow() -> attempts to INTERRUPT running tasks NOW and");
        System.out.println("                          returns the tasks that never got to start");

        List<Runnable> neverStarted = pool.shutdownNow();
        System.out.println();
        System.out.println("    pool.shutdownNow() on THIS lesson's own pool -> "
                + neverStarted.size() + " tasks that had not yet started");

        System.out.println();
        System.out.println("    THE OTHER REAL FIX - a CUSTOM ThreadFactory that makes DAEMON");
        System.out.println("    threads, so a forgotten shutdown() can NEVER hang the JVM:");
        ThreadFactory daemonFactory = runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            return t;
        };
        ExecutorService daemonPool = Executors.newFixedThreadPool(2, daemonFactory);
        daemonPool.submit(() -> System.out.println("      task on a DAEMON pool thread ran fine"));
        Thread.sleep(50);
        System.out.println("      this pool was NEVER shutdown() - and this file will STILL exit,");
        System.out.println("      because every one of its threads is a genuine daemon (lesson 62).");
        System.out.println("      (this is a real, useful pattern - but shutdown() is STILL the");
        System.out.println("      correct default; daemon pools can LOSE in-flight work on exit)");


        /* ====================================================================
         * SECTION 6 - BlockingQueue: LESSON 63's wait/notify, IN ONE LINE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - BlockingQueue: THE MODERN producer/consumer, NO wait/notify");
        System.out.println("=".repeat(74));

        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        Thread producer = new Thread(() -> {
            sleepQuietly(200);
            try {
                queue.put("a real message");
            } catch (InterruptedException ignored) {
            }
        });
        producer.start();

        System.out.println("    queue.take() BLOCKS until something is available - no manual");
        System.out.println("    synchronized/wait/notify anywhere, unlike lesson 63's SimpleBox:");
        String received = queue.take();
        producer.join();
        System.out.println("      received -> \"" + received + "\"");
        System.out.println();
        System.out.println("    BlockingQueue implementations (ArrayBlockingQueue,");
        System.out.println("    LinkedBlockingQueue, ...) ARE the standard tool for real");
        System.out.println("    producer/consumer pipelines - lesson 63's hand-written wait/notify");
        System.out.println("    version exists mainly to show what these classes do UNDER THE HOOD.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 64.");
        System.out.println("=".repeat(74));
    }

    static List<String> runFiveTasksAndCollectThreadNames(ExecutorService executor)
            throws InterruptedException, ExecutionException {
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            futures.add(executor.submit(() -> Thread.currentThread().getName()));
        }
        List<String> names = new ArrayList<>();
        for (Future<String> f : futures) {
            names.add(f.get());
        }
        return names;
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
 * 1. Reproduce Section 5's real hang yourself: write a separate file that
 *    submits ONE task to a newFixedThreadPool and never calls shutdown(),
 *    run it with a timeout wrapper, and confirm it never exits on its own.
 *
 * 2. Submit 20 tasks that each sleep 100ms to a newFixedThreadPool(4), time
 *    the total wall-clock duration, and explain the number using the pool
 *    size and how many "rounds" of 4 concurrent tasks it takes.
 *
 * 3. Use ExecutorService.invokeAll() to submit a LIST of Callables at once
 *    and get back a List<Future<T>> - compare it to Section 3's manual
 *    loop, and explain what invokeAll() additionally guarantees.
 *
 * 4. Deliberately let a submitted task throw a RuntimeException, then call
 *    future.get() and observe it wrapped in an ExecutionException - read
 *    getCause() to get the ORIGINAL exception back.
 *
 * 5. Build a bounded ArrayBlockingQueue(capacity 2), have a producer put()
 *    5 items rapidly with NO consumer running yet, and observe put()
 *    itself BLOCK once the queue is full - confirm with Thread.getState().
 * ============================================================================
 */
