/* ============================================================================
 * 63 - Synchronization, locks and the memory model
 * ----------------------------------------------------------------------------
 * Companion lesson: 63-synchronization-and-locks.md
 *
 * RUN IT:
 *     java Java/12-concurrency/63-synchronization-and-locks.java
 *
 * Sections 3 and 4 reproduce a REAL memory-visibility bug and a REAL
 * deadlock, LIVE, on this machine - both using a genuinely safe technique
 * (daemon threads + a bounded join timeout, from lesson 62) so that even a
 * true infinite hang cannot prevent this file from finishing and exiting.
 * ============================================================================
 */

class SynchronizationAndLocks {

    static int unsafeCounter = 0;
    static int safeCounter = 0;
    static final Object counterLock = new Object();

    public static void main(String[] args) throws InterruptedException {

        /* ====================================================================
         * SECTION 1 - A REAL RACE CONDITION, WITH RAW THREADS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - counter++ ACROSS 10 THREADS, NO SYNCHRONIZATION");
        System.out.println("=".repeat(74));

        int threadCount = 10;
        int incrementsEach = 100_000;
        Thread[] unsafeThreads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            unsafeThreads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    unsafeCounter++;   // read, increment, write - NOT ATOMIC
                }
            });
        }
        for (Thread t : unsafeThreads) t.start();
        for (Thread t : unsafeThreads) t.join();

        int expected = threadCount * incrementsEach;
        System.out.println("    " + threadCount + " threads x " + incrementsEach + " increments each");
        System.out.println("    expected -> " + expected);
        System.out.println("    got      -> " + unsafeCounter
                + (unsafeCounter == expected ? "" : "   <- LOST UPDATES, a real race condition"));
        System.out.println();
        System.out.println("    counter++ is THREE steps: READ the current value, ADD one,");
        System.out.println("    WRITE it back. Two threads can both READ the SAME value before");
        System.out.println("    EITHER writes - one increment is silently LOST. This is the exact");
        System.out.println("    same class of bug lesson 54 measured with a parallel STREAM; here");
        System.out.println("    it is reproduced with raw Threads directly.");


        /* ====================================================================
         * SECTION 2 - synchronized: THE FIX, AND A REAL BLOCKED STATE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - synchronized FIXES IT, AND PRODUCES A REAL BLOCKED THREAD");
        System.out.println("=".repeat(74));

        Thread[] safeThreads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            safeThreads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    synchronized (counterLock) {
                        safeCounter++;   // now ATOMIC with respect to counterLock
                    }
                }
            });
        }
        for (Thread t : safeThreads) t.start();
        for (Thread t : safeThreads) t.join();
        System.out.println("    SAME workload, wrapped in synchronized (counterLock) { }:");
        System.out.println("    expected -> " + expected);
        System.out.println("    got      -> " + safeCounter
                + (safeCounter == expected ? "   <- CORRECT, every increment survived" : ""));

        System.out.println();
        System.out.println("    A REAL BLOCKED THREAD, caught in the act:");
        Object demoLock = new Object();
        Thread holder = new Thread(() -> {
            synchronized (demoLock) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }
            }
        }, "lock-holder");
        Thread waiter = new Thread(() -> {
            synchronized (demoLock) {
            }
        }, "lock-waiter");
        holder.start();
        Thread.sleep(50);   // let holder actually acquire the lock first
        waiter.start();
        Thread.sleep(50);   // let waiter actually try, and get stuck
        System.out.println("      waiter.getState() while holder still owns the lock -> "
                + waiter.getState());
        holder.join();
        waiter.join();
        System.out.println("      waiter.getState() after holder released it        -> "
                + waiter.getState() + "   (ran and finished, once the lock was free)");


        /* ====================================================================
         * SECTION 3 - A REAL VISIBILITY BUG, LIVE, AND volatile's REAL FIX
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - A REAL MEMORY-VISIBILITY BUG, REPRODUCED LIVE");
        System.out.println("=".repeat(74));

        System.out.println("    THIS THREAD IS A DAEMON (lesson 62) SPECIFICALLY so that even if");
        System.out.println("    it genuinely never sees the update and loops forever, THIS FILE");
        System.out.println("    still exits cleanly at the end - safe to actually attempt the");
        System.out.println("    famous 'the JIT cached the read' bug for real, not just describe it:");

        NonVolatileFlag flag = new NonVolatileFlag();
        Thread visibilityWorker = new Thread(() -> {
            long iterations = 0;
            while (flag.running) {
                iterations++;
            }
            System.out.println("      [worker] noticed the change after " + iterations + " iterations");
        });
        visibilityWorker.setDaemon(true);
        visibilityWorker.start();
        Thread.sleep(500);
        System.out.println("    main: setting the (non-volatile) flag to false...");
        flag.running = false;
        visibilityWorker.join(3000);
        System.out.println("    worker noticed within 3 real seconds? -> " + !visibilityWorker.isAlive());
        if (visibilityWorker.isAlive()) {
            System.out.println("      -> IT DID NOT. This is the REAL bug: the worker thread's own");
            System.out.println("      CPU core is reading \"running\" from a REGISTER or CACHE LINE");
            System.out.println("      it loaded ONCE, before the loop started - main's write exists");
            System.out.println("      in MAIN MEMORY, but nothing forces the worker to look there");
            System.out.println("      again. No exception, no warning - it can loop like this");
            System.out.println("      FOREVER, on real hardware, on a real JVM, right now.");
        }

        VolatileFlag volatileFlag = new VolatileFlag();
        Thread volatileWorker = new Thread(() -> {
            long iterations = 0;
            while (volatileFlag.running) {
                iterations++;
            }
            System.out.println("      [volatile worker] noticed after " + iterations + " iterations");
        });
        volatileWorker.setDaemon(true);
        volatileWorker.start();
        Thread.sleep(500);
        System.out.println();
        System.out.println("    THE FIX - the ONLY difference is the word \"volatile\" on the field:");
        volatileFlag.running = false;
        volatileWorker.join(3000);
        System.out.println("    volatile worker noticed within 3 real seconds? -> " + !volatileWorker.isAlive());
        System.out.println();
        System.out.println("    volatile guarantees every READ sees the MOST RECENT WRITE from ANY");
        System.out.println("    thread - it forbids exactly the caching that broke Section 3's");
        System.out.println("    first half. It does NOT make counter++ atomic (Section 1's bug is");
        System.out.println("    UNRELATED and volatile alone would NOT fix it) - visibility and");
        System.out.println("    atomicity are TWO DIFFERENT problems with two different tools.");


        /* ====================================================================
         * SECTION 4 - A REAL DEADLOCK, REPRODUCED LIVE, THE SAME SAFE WAY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - A REAL DEADLOCK, REPRODUCED LIVE");
        System.out.println("=".repeat(74));

        Object lockA = new Object();
        Object lockB = new Object();

        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                sleepQuietly(200);
                synchronized (lockB) {
                    System.out.println("      t1 got BOTH locks (should NOT print)");
                }
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                sleepQuietly(200);
                synchronized (lockA) {
                    System.out.println("      t2 got BOTH locks (should NOT print)");
                }
            }
        }, "t2");

        System.out.println("    t1 locks A then B. t2 locks B then A. OPPOSITE ORDER - the classic");
        System.out.println("    deadlock shape. Both threads are DAEMONS, so a real deadlock here");
        System.out.println("    cannot prevent this file from finishing:");

        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();
        t1.join(3000);
        t2.join(3000);

        System.out.println();
        System.out.println("    t1 finished within 3 real seconds? -> " + !t1.isAlive());
        System.out.println("    t2 finished within 3 real seconds? -> " + !t2.isAlive());
        System.out.println("    t1.getState() -> " + t1.getState());
        System.out.println("    t2.getState() -> " + t2.getState());
        System.out.println();
        System.out.println("    BOTH threads are BLOCKED, PERMANENTLY - t1 holds A and wants B; t2");
        System.out.println("    holds B and wants A. Neither can EVER proceed. This is a REAL");
        System.out.println("    deadlock, live, right now, on this machine - not a diagram.");
        System.out.println();
        System.out.println("    THE FIX: always acquire MULTIPLE locks in the SAME GLOBAL ORDER,");
        System.out.println("    everywhere in the codebase (e.g. always lower object-identity-hash");
        System.out.println("    first, or an explicit ranking) - if both threads had locked A");
        System.out.println("    before B, this deadlock could never happen.");


        /* ====================================================================
         * SECTION 5 - wait()/notify(): COORDINATING, NOT JUST EXCLUDING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - wait()/notify(): A THREAD GIVES UP THE LOCK TO WAIT");
        System.out.println("=".repeat(74));

        SimpleBox<String> box = new SimpleBox<>();
        Thread producer = new Thread(() -> {
            sleepQuietly(300);
            box.put("a real message, produced after a real delay");
        }, "producer");
        Thread consumer = new Thread(() -> {
            String received = box.take();   // BLOCKS, via wait(), until put() calls notify()
            System.out.println("      consumer received -> \"" + received + "\"");
        }, "consumer");

        consumer.start();
        Thread.sleep(50);
        System.out.println("    consumer.getState() BEFORE producer puts anything -> " + consumer.getState()
                + "   (WAITING - inside Object.wait(), holding NO lock while it waits)");
        producer.start();
        producer.join();
        consumer.join();
        System.out.println();
        System.out.println("    wait() atomically RELEASES the monitor and suspends the thread;");
        System.out.println("    notify() wakes ONE waiting thread back up to re-ACQUIRE it. This");
        System.out.println("    is what makes wait/notify GENUINE COORDINATION, not just mutual");
        System.out.println("    exclusion - lesson 64's BlockingQueue is the modern, safer way to");
        System.out.println("    get this exact producer/consumer behavior without hand-rolling it.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 63.");
        System.out.println("=".repeat(74));
    }

    static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}

/** A flag WITHOUT volatile - Section 3's real bug. */
class NonVolatileFlag {
    boolean running = true;
}

/** The SAME flag, WITH volatile - Section 3's real fix. */
class VolatileFlag {
    volatile boolean running = true;
}

/** A minimal, hand-rolled single-slot producer/consumer box using wait/notify. */
class SimpleBox<T> {
    private T value;
    private boolean present = false;

    synchronized void put(T newValue) {
        value = newValue;
        present = true;
        notify();   // wake the (single) waiting consumer, if any
    }

    synchronized T take() {
        while (!present) {
            try {
                wait();   // releases the monitor while waiting - does NOT hold the lock idle
            } catch (InterruptedException ignored) {
            }
        }
        present = false;
        return value;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Reproduce Section 1's race condition with java.util.concurrent.atomic.
 *    AtomicInteger instead of synchronized - confirm it also produces the
 *    correct total, and explain in one sentence why it does not need a
 *    synchronized block at all (lesson 65 covers this in depth).
 *
 * 2. Section 3's demo waits 500ms before flipping the flag. Reduce it to
 *    5ms and re-run several times - does the visibility bug still
 *    reproduce as reliably? What does that suggest about WHEN the JIT
 *    actually starts caching the read?
 *
 * 3. Fix Section 4's deadlock by making BOTH threads acquire lockA before
 *    lockB, and confirm t1.getState()/t2.getState() both end up
 *    TERMINATED instead of BLOCKED.
 *
 * 4. Extend SimpleBox to hold MULTIPLE items (a real bounded queue) using
 *    two conditions (full/empty) - or look ahead to lesson 64's
 *    BlockingQueue and explain why it is almost always the better choice.
 *
 * 5. Using jstack (or Thread.getAllStackTraces()) on a REAL deadlock like
 *    Section 4's, without the join(3000) timeout, observe how a real
 *    diagnostic tool reports it - then add the timeout back.
 * ============================================================================
 */
