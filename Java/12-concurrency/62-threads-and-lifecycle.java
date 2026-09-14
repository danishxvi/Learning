/* ============================================================================
 * 62 - Threads and the thread lifecycle
 * ----------------------------------------------------------------------------
 * Companion lesson: 62-threads-and-lifecycle.md
 *
 * RUN IT:
 *     java Java/12-concurrency/62-threads-and-lifecycle.java
 *
 * Section 2 reproduces the single most common threading mistake beginners
 * make - calling run() instead of start() - with REAL thread name evidence
 * proving nothing actually ran concurrently. Section 5 reproduces the real
 * IllegalThreadStateException from starting a Thread twice.
 * ============================================================================
 */

class ThreadsAndLifecycle {

    public static void main(String[] args) throws InterruptedException {

        /* ====================================================================
         * SECTION 1 - TWO WAYS TO CREATE A THREAD, AND WHY ONE IS PREFERRED
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - EXTENDING Thread VS IMPLEMENTING Runnable");
        System.out.println("=".repeat(74));

        Thread byExtending = new Thread() {
            @Override
            public void run() {
                System.out.println("      (extends Thread) running on -> " + Thread.currentThread().getName());
            }
        };
        byExtending.start();
        byExtending.join();

        Runnable task = () -> System.out.println("      (implements Runnable) running on -> "
                + Thread.currentThread().getName());
        Thread byRunnable = new Thread(task);
        byRunnable.start();
        byRunnable.join();

        System.out.println();
        System.out.println("    BOTH genuinely started a new thread - the difference is EXTENDS");
        System.out.println("    VS IMPLEMENTS. A class can extend only ONE superclass (lesson 26),");
        System.out.println("    so extending Thread BURNS your one inheritance slot on something");
        System.out.println("    that has NOTHING to do with your class's actual purpose. A");
        System.out.println("    Runnable is just BEHAVIOR - your class stays free to extend");
        System.out.println("    whatever it actually needs to, and the SAME Runnable can be handed");
        System.out.println("    to an ExecutorService (lesson 64) instead of a raw Thread.");


        /* ====================================================================
         * SECTION 2 - start() VS run(): THE #1 BEGINNER MISTAKE, REPRODUCED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - CALLING run() DIRECTLY DOES NOT START A THREAD");
        System.out.println("=".repeat(74));

        System.out.println("    main() is running on -> " + Thread.currentThread().getName());

        Thread demo = new Thread(() ->
                System.out.println("      run() executing on -> " + Thread.currentThread().getName()), "worker-thread");

        System.out.println();
        System.out.println("    calling demo.run() DIRECTLY (THE MISTAKE):");
        demo.run();
        System.out.println("    demo.getState() after run() returned -> " + demo.getState()
                + "   (still NEW - run() was just a NORMAL METHOD CALL, on main's OWN stack)");

        System.out.println();
        System.out.println("    calling demo.start() (THE FIX):");
        Thread correct = new Thread(() ->
                System.out.println("      run() executing on -> " + Thread.currentThread().getName()), "worker-thread-2");
        correct.start();
        correct.join();
        System.out.println();
        System.out.println("    run() DIRECTLY printed \"main\" - the SAME thread that called it.");
        System.out.println("    start() printed \"worker-thread-2\" - a GENUINELY DIFFERENT thread.");
        System.out.println("    Calling run() compiles fine, produces NO error, and just silently");
        System.out.println("    runs everything SEQUENTIALLY on the caller's own thread - no");
        System.out.println("    concurrency happened at all. This is real, and easy to miss,");
        System.out.println("    because the CODE still runs and often still looks correct.");


        /* ====================================================================
         * SECTION 3 - THE THREAD LIFECYCLE, OBSERVED WITH REAL getState() CALLS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE LIFECYCLE: NEW -> RUNNABLE -> ... -> TERMINATED");
        System.out.println("=".repeat(74));

        Object lock = new Object();
        Thread lifecycle = new Thread(() -> {
            synchronized (lock) {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
        }, "lifecycle-thread");

        System.out.println("    BEFORE start()          -> " + lifecycle.getState());
        lifecycle.start();
        Thread.sleep(30);   // give it time to actually enter the sleep
        System.out.println("    DURING Thread.sleep(150) -> " + lifecycle.getState()
                + "   (TIMED_WAITING - sleeping with a bound)");
        lifecycle.join();
        System.out.println("    AFTER join() returns    -> " + lifecycle.getState());
        System.out.println();
        System.out.println("    REAL states observed: NEW (created, never started), TIMED_WAITING");
        System.out.println("    (mid-sleep), TERMINATED (run() returned - a Thread can NEVER be");
        System.out.println("    restarted from here, Section 5 proves that for real). The OTHER");
        System.out.println("    real states - RUNNABLE (eligible to run OR running - Java does not");
        System.out.println("    distinguish), BLOCKED (waiting for a MONITOR LOCK another thread");
        System.out.println("    holds), WAITING (Object.wait()/Thread.join() with no timeout) -");
        System.out.println("    lesson 63 demonstrates BLOCKED concretely with real lock contention.");


        /* ====================================================================
         * SECTION 4 - DAEMON THREADS: DO NOT KEEP THE JVM ALIVE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - DAEMON THREADS DO NOT PREVENT JVM SHUTDOWN");
        System.out.println("=".repeat(74));

        Thread daemon = new Thread(() -> {
            try {
                Thread.sleep(60_000);   // WOULD run for a minute, if allowed to
            } catch (InterruptedException ignored) {
            }
        }, "daemon-worker");
        daemon.setDaemon(true);
        daemon.start();
        System.out.println("    started a DAEMON thread sleeping for 60 SECONDS - isDaemon() -> "
                + daemon.isDaemon());
        System.out.println("    this program will exit in a moment ANYWAY - if it were a NORMAL");
        System.out.println("    (non-daemon) thread, main() would have to wait the FULL 60 seconds");
        System.out.println("    for it before the JVM could shut down. Daemon threads are for");
        System.out.println("    background work (GC, housekeeping) that should NEVER hold the");
        System.out.println("    process open on its own - the JVM exits once every NON-daemon");
        System.out.println("    thread has finished, killing any remaining daemons outright.");


        /* ====================================================================
         * SECTION 5 - A Thread CAN NEVER BE RESTARTED: REPRODUCED FOR REAL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - CALLING start() TWICE: A REAL EXCEPTION");
        System.out.println("=".repeat(74));

        Thread onceOnly = new Thread(() -> {
        });
        onceOnly.start();
        onceOnly.join();
        System.out.println("    onceOnly.start() the FIRST time -> ran and finished, state now "
                + onceOnly.getState());
        try {
            onceOnly.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("    onceOnly.start() the SECOND time -> IllegalThreadStateException");
        }
        System.out.println();
        System.out.println("    A Thread object is GENUINELY single-use - once TERMINATED, it can");
        System.out.println("    NEVER run again, even though the OBJECT itself still exists and");
        System.out.println("    is perfectly readable. Need to run the same work again? Create a");
        System.out.println("    NEW Thread with the same Runnable - the Runnable IS reusable, the");
        System.out.println("    Thread wrapping it is not.");


        /* ====================================================================
         * SECTION 6 - interrupt(): A POLITE REQUEST, NOT A FORCED STOP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - Thread.interrupt(): COOPERATIVE, NOT FORCED");
        System.out.println("=".repeat(74));

        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(10_000);
                System.out.println("      slept the full 10 seconds (should NOT print)");
            } catch (InterruptedException e) {
                System.out.println("      interrupted DURING sleep -> InterruptedException caught,"
                        + " woke up EARLY as intended");
            }
        }, "sleeper");
        sleeper.start();
        Thread.sleep(100);   // let it actually get INTO the sleep first
        sleeper.interrupt();
        sleeper.join();
        System.out.println();
        System.out.println("    interrupt() does NOT forcibly kill a thread - it sets an internal");
        System.out.println("    FLAG, and any BLOCKING call currently in progress (sleep, wait,");
        System.out.println("    join, ...) notices that flag and throws InterruptedException. A");
        System.out.println("    thread doing PURE COMPUTATION with no blocking calls will not");
        System.out.println("    react to interrupt() AT ALL unless its own code explicitly checks");
        System.out.println("    Thread.currentThread().isInterrupted() periodically.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 62.");
        System.out.println("=".repeat(74));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Reproduce Section 2's mistake yourself with a DIFFERENT observable
 *    side effect (not a println of the thread name) - e.g. have run()
 *    increment a shared counter and confirm main()'s OWN stack depth grew
 *    by checking a stack trace element, proving it never left main's frame.
 *
 * 2. Create 5 threads with DIFFERENT names, start them all, then join()
 *    ALL of them in a loop before printing "all done" - confirm the
 *    message never prints before every thread has genuinely finished.
 *
 * 3. Write a loop-based worker (NOT using sleep) that checks
 *    Thread.currentThread().isInterrupted() every iteration and exits
 *    cleanly when interrupted - confirm interrupt() actually stops it,
 *    unlike a tight loop with no such check.
 *
 * 4. Set a thread's priority with setPriority() to MIN_PRIORITY and
 *    another to MAX_PRIORITY, run both doing the same busy work, and
 *    measure whether one genuinely finishes faster - report your HONEST
 *    result (priority's practical effect is famously platform-dependent).
 *
 * 5. Reproduce Section 4's daemon behavior in the OTHER direction: start a
 *    NON-daemon thread sleeping for 3 seconds, and time how long the
 *    overall program takes to exit - confirm it genuinely waits.
 * ============================================================================
 */
