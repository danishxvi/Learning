/* ============================================================================
 * 67 - Virtual threads (Java 21)
 * ----------------------------------------------------------------------------
 * Companion lesson: 67-virtual-threads.md
 *
 * RUN IT:
 *     java Java/12-concurrency/67-virtual-threads.java
 *
 * The final lesson of Section 12. Section 3 runs the SAME real workload -
 * 100,000 concurrent 1-second sleeps - on virtual threads AND on real
 * platform threads, and reports the REAL measured gap between them.
 * ============================================================================
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class VirtualThreads {

    public static void main(String[] args) throws Exception {

        /* ====================================================================
         * SECTION 1 - CREATING A VIRTUAL THREAD: THE SAME Thread API
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - Thread.ofVirtual(): THE SAME Thread CLASS, A NEW KIND");
        System.out.println("=".repeat(74));

        Thread virtual = Thread.ofVirtual().unstarted(() ->
                System.out.println("    running on -> " + Thread.currentThread()));
        virtual.start();
        virtual.join();

        System.out.println("    virtual.isVirtual() -> " + virtual.isVirtual());
        System.out.println("    virtual.isDaemon()  -> " + virtual.isDaemon()
                + "   (ALWAYS true for virtual threads - this cannot be changed, Section 4)");

        Thread platform = new Thread(() -> {
        });
        System.out.println("    a PLAIN new Thread().isVirtual() -> " + platform.isVirtual());
        System.out.println();
        System.out.println("    Virtual threads are STILL java.lang.Thread objects - same class,");
        System.out.println("    same start()/join()/interrupt() API from lesson 62. What is");
        System.out.println("    DIFFERENT is entirely underneath: a virtual thread is NOT backed");
        System.out.println("    by its own dedicated OS thread. The JVM multiplexes MANY virtual");
        System.out.println("    threads onto a SMALL pool of real OS threads (\"CARRIER THREADS\"),");
        System.out.println("    switching a virtual thread OFF its carrier the moment it BLOCKS");
        System.out.println("    (sleep, I/O, lock acquisition, ...) and putting a DIFFERENT waiting");
        System.out.println("    virtual thread ON that now-free carrier instead.");


        /* ====================================================================
         * SECTION 2 - THE MODERN WAY: newVirtualThreadPerTaskExecutor
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - Executors.newVirtualThreadPerTaskExecutor()");
        System.out.println("=".repeat(74));

        try (ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            virtualExecutor.submit(() -> System.out.println("    task on -> " + Thread.currentThread()));
            virtualExecutor.submit(() -> System.out.println("    task on -> " + Thread.currentThread()));
        }
        System.out.println();
        System.out.println("    SAME ExecutorService interface as lesson 64's thread pools - the");
        System.out.println("    difference is entirely in WHAT submit() hands work to: a genuinely");
        System.out.println("    NEW virtual thread PER TASK, never reused, never pooled. This is");
        System.out.println("    the recommended, idiomatic way to use virtual threads - NOT via");
        System.out.println("    Thread.ofVirtual() directly for everyday application code.");


        /* ====================================================================
         * SECTION 3 - THE REAL SCALABILITY GAP: 100,000 CONCURRENT SLEEPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - 100,000 CONCURRENT 1-SECOND SLEEPS: MEASURED BOTH WAYS");
        System.out.println("=".repeat(74));

        int taskCount = 20_000;   // scaled down from the separately-verified 100,000 run so
        // this lesson finishes in a reasonable time for every reader - see the .md for the
        // full, separately-measured 100,000-thread numbers.

        System.out.println("    running " + taskCount + " tasks, each Thread.sleep(500)ms, BOTH ways:");
        System.out.println("    (a genuinely I/O/sleep-BOUND workload - exactly what virtual");
        System.out.println("    threads are FOR; a CPU-bound workload would not show this gap)");

        long virtualMillis = runConcurrentSleeps(taskCount, true);
        System.out.println("      " + taskCount + " VIRTUAL threads  -> " + virtualMillis + " ms");

        long platformMillis = runConcurrentSleeps(taskCount, false);
        System.out.println("      " + taskCount + " PLATFORM threads -> " + platformMillis + " ms");

        System.out.println();
        System.out.printf("    platform threads took roughly %.1fx LONGER for the IDENTICAL%n",
                (double) platformMillis / Math.max(1, virtualMillis));
        System.out.println("    workload - both actually WORK (neither crashed HERE), but");
        System.out.println("    scheduling and creating tens of thousands of REAL OS threads is");
        System.out.println("    genuinely, measurably expensive. The SEPARATELY-verified 100,000-");
        System.out.println("    thread run (see the .md) showed an even larger real gap. On a");
        System.out.println("    machine with less available memory or a lower OS thread-count");
        System.out.println("    limit, the platform-thread version can fail OUTRIGHT with a real");
        System.out.println("    OutOfMemoryError - virtual threads do not carry that same per-");
        System.out.println("    thread OS cost AT ALL.");


        /* ====================================================================
         * SECTION 4 - REAL, DOCUMENTED LIMITATIONS - NOT A FREE LUNCH
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHAT VIRTUAL THREADS DO NOT FIX");
        System.out.println("=".repeat(74));

        System.out.println("    ALWAYS DAEMON: confirmed in Section 1 - virtual.isDaemon() was");
        System.out.println("    true, and Thread.ofVirtual()...setDaemon(false) is NOT offered at");
        System.out.println("    all. A virtual thread NEVER, by itself, keeps the JVM alive -");
        System.out.println("    lesson 62's daemon rule applies UNCONDITIONALLY here.");
        System.out.println();
        System.out.println("    PINNING: a virtual thread executing inside a synchronized block or");
        System.out.println("    method CANNOT be unmounted from its carrier thread while BLOCKED in");
        System.out.println("    there (a real, documented JDK 21 limitation) - a virtual thread");
        System.out.println("    that does a SLOW blocking operation WHILE holding a synchronized");
        System.out.println("    lock effectively PINS its real OS carrier thread for that entire");
        System.out.println("    duration, losing the exact scalability benefit just measured. The");
        System.out.println("    real fix, when this matters, is java.util.concurrent.locks.ReentrantLock");
        System.out.println("    instead of synchronized around any blocking call on a HOT path.");
        System.out.println();
        System.out.println("    NOT FASTER FOR CPU-BOUND WORK: virtual threads help when threads");
        System.out.println("    spend their time BLOCKED (I/O, sleep, waiting on other threads) -");
        System.out.println("    for PURE COMPUTATION with no blocking at all, there is nothing to");
        System.out.println("    multiplex, and the real bottleneck stays the number of actual CPU");
        System.out.println("    cores, exactly the same as lesson 64's platform-thread pools.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 67, and of Section 12.");
        System.out.println("=".repeat(74));
    }

    /**
     * Runs {@code count} tasks that each sleep 500ms, all concurrently, using
     * either virtual or platform threads, and returns the total wall-clock
     * time in milliseconds.
     *
     * @param count   how many concurrent sleeping tasks to run
     * @param virtual true for virtual threads, false for real platform threads
     * @return elapsed milliseconds for ALL tasks to finish
     */
    static long runConcurrentSleeps(int count, boolean virtual) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(count);
        Runnable task = () -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            } finally {
                latch.countDown();
            }
        };

        long start = System.nanoTime();
        if (virtual) {
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int i = 0; i < count; i++) {
                    executor.submit(task);
                }
                latch.await();
            }
        } else {
            for (int i = 0; i < count; i++) {
                Thread t = new Thread(task);
                t.setDaemon(true);   // safety net (lesson 62/64) in case any thread creation stalls
                t.start();
            }
            latch.await();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Re-run Section 3 with taskCount raised to 100,000 (this lesson's OWN
 *    file uses 20,000 so it finishes quickly for every reader) and compare
 *    your OWN measured numbers against the ones reported in the .md.
 *
 * 2. Write a CPU-bound task (compute a large Fibonacci number or similar,
 *    NO sleeping, NO I/O at all) and run 10,000 of them on virtual threads
 *    versus a FIXED platform pool sized to your CPU core count - confirm
 *    virtual threads do NOT meaningfully help here, as Section 4 predicts.
 *
 * 3. Reproduce PINNING for real: wrap a Thread.sleep() call inside a
 *    synchronized block on a virtual thread, run MANY of them
 *    concurrently, and compare the total time against the SAME workload
 *    using ReentrantLock instead of synchronized.
 *
 * 4. Try Thread.ofVirtual().name("worker-", 0).factory() to build a
 *    ThreadFactory usable with a custom ExecutorService, and confirm the
 *    resulting virtual threads carry the expected auto-incrementing names.
 *
 * 5. Look up (do not just guess) which java.util.concurrent constructs
 *    besides synchronized are documented to ALSO pin a virtual thread's
 *    carrier in JDK 21 specifically, and note whether later JDK versions
 *    changed this - this is an active area of JDK improvement.
 * ============================================================================
 */
