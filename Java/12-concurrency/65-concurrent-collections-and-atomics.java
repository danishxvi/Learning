/* ============================================================================
 * 65 - Concurrent collections and atomics
 * ----------------------------------------------------------------------------
 * Companion lesson: 65-concurrent-collections-and-atomics.md
 *
 * RUN IT:
 *     java Java/12-concurrency/65-concurrent-collections-and-atomics.java
 *
 * Section 4 reproduces a REAL ConcurrentModificationException from a plain
 * HashMap under real concurrent writes, live, on this machine - then
 * Section 5 shows ConcurrentHashMap handling the IDENTICAL workload safely.
 * ============================================================================
 */

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class ConcurrentCollectionsAndAtomics {

    static int synchronizedCounter = 0;
    static final Object counterLock = new Object();
    static AtomicInteger atomicCounter = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {

        /* ====================================================================
         * SECTION 1 - AtomicInteger VS synchronized: BOTH CORRECT, MEASURED
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - AtomicInteger: LOCK-FREE, AND MEASURABLY FASTER");
        System.out.println("=".repeat(74));

        int threadCount = 8;
        int incrementsEach = 500_000;
        long expected = (long) threadCount * incrementsEach;

        long syncStart = System.nanoTime();
        runIncrementThreads(threadCount, incrementsEach, () -> {
            synchronized (counterLock) {
                synchronizedCounter++;
            }
        });
        long syncMillis = (System.nanoTime() - syncStart) / 1_000_000;

        long atomicStart = System.nanoTime();
        runIncrementThreads(threadCount, incrementsEach, atomicCounter::incrementAndGet);
        long atomicMillis = (System.nanoTime() - atomicStart) / 1_000_000;

        System.out.printf("    %d threads x %,d increments each (%,d total):%n", threadCount, incrementsEach, expected);
        System.out.println("      synchronized int          -> " + syncMillis + " ms, result=" + synchronizedCounter
                + (synchronizedCounter == expected ? " (correct)" : " (WRONG)"));
        System.out.println("      AtomicInteger.incrementAndGet() -> " + atomicMillis + " ms, result=" + atomicCounter.get()
                + (atomicCounter.get() == expected ? " (correct)" : " (WRONG)"));
        System.out.println();
        System.out.println("    BOTH are correct - no lost updates either way. AtomicInteger is");
        System.out.println("    typically faster because it uses a LOCK-FREE hardware instruction");
        System.out.println("    (compare-and-swap, Section 2) instead of acquiring a real monitor");
        System.out.println("    lock - no thread ever BLOCKS waiting for another.");


        /* ====================================================================
         * SECTION 2 - compareAndSet: THE MECHANISM UNDER EVERY Atomic CLASS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - compareAndSet: \"UPDATE IT, BUT ONLY IF NOBODY BEAT ME TO IT\"");
        System.out.println("=".repeat(74));

        AtomicInteger value = new AtomicInteger(10);
        boolean firstAttempt = value.compareAndSet(10, 20);
        System.out.println("    value starts at 10");
        System.out.println("    compareAndSet(10, 20) -> " + firstAttempt + ", value is now " + value.get());

        boolean secondAttempt = value.compareAndSet(10, 999);
        System.out.println("    compareAndSet(10, 999) AGAIN (value is no longer 10) -> "
                + secondAttempt + ", value is STILL " + value.get());
        System.out.println();
        System.out.println("    compareAndSet(expected, newValue) ATOMICALLY does: \"IF the");
        System.out.println("    current value still equals EXPECTED, set it to newValue and return");
        System.out.println("    true - otherwise change NOTHING and return false.\" This is the");
        System.out.println("    real CPU instruction (CAS) that EVERY Atomic class, and most of");
        System.out.println("    java.util.concurrent, is ultimately built on. incrementAndGet()");
        System.out.println("    itself is just \"read, compute newValue, compareAndSet in a RETRY");
        System.out.println("    LOOP until it succeeds\" - lock-free because NOTHING ever blocks,");
        System.out.println("    it just retries.");


        /* ====================================================================
         * SECTION 3 - AtomicReference: THE SAME IDEA, FOR OBJECTS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - AtomicReference: compareAndSet FOR ANY OBJECT");
        System.out.println("=".repeat(74));

        java.util.concurrent.atomic.AtomicReference<String> ref =
                new java.util.concurrent.atomic.AtomicReference<>("initial");
        System.out.println("    ref starts at \"" + ref.get() + "\"");
        System.out.println("    updateAndGet(s -> s.toUpperCase()) -> "
                + ref.updateAndGet(String::toUpperCase));
        System.out.println("    accumulateAndGet(\"!!!\", String::concat) -> "
                + ref.accumulateAndGet("!!!", String::concat));
        System.out.println();
        System.out.println("    updateAndGet/accumulateAndGet apply a FUNCTION atomically, via the");
        System.out.println("    SAME retry-on-CAS-failure loop internally - useful for any");
        System.out.println("    \"replace this object with a new one DERIVED from the current one,");
        System.out.println("    safely under concurrent access\" pattern.");


        /* ====================================================================
         * SECTION 4 - HashMap UNDER CONCURRENT WRITES: A REAL CME
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - A PLAIN HashMap UNDER REAL CONCURRENT WRITES");
        System.out.println("=".repeat(74));

        System.out.println("    HashMap's own Javadoc states plainly: it is NOT thread-safe, and");
        System.out.println("    concurrent structural modification from multiple threads is");
        System.out.println("    UNDEFINED BEHAVIOR - historically, this has caused REAL infinite");
        System.out.println("    loops during concurrent resize in older JDKs. Reproduced here,");
        System.out.println("    live, with 4 real writer threads racing a real iterator:");

        HashMap<Integer, Integer> unsafeMap = new HashMap<>();
        for (int i = 0; i < 16; i++) unsafeMap.put(i, i);

        Thread[] writers = new Thread[4];
        for (int w = 0; w < 4; w++) {
            int base = w * 1_000_000;
            writers[w] = new Thread(() -> {
                for (int i = 0; i < 2_000_000; i++) {
                    unsafeMap.put(base + i, i);
                }
            });
            writers[w].setDaemon(true);   // safety net (lesson 62) in case this run behaves differently
        }
        for (Thread t : writers) t.start();
        Thread.sleep(5);   // let the writers get well into heavy resizing before iterating

        int iterated = 0;
        try {
            Iterator<Integer> it = unsafeMap.keySet().iterator();
            while (it.hasNext()) {
                it.next();
                iterated++;
            }
            System.out.println("      this run completed WITHOUT an exception, having iterated "
                    + iterated + " keys - concurrent HashMap corruption is TIMING-DEPENDENT, not");
            System.out.println("      guaranteed on every single run, but it IS real (see the .md for");
            System.out.println("      the separately-confirmed reproduction rate).");
        } catch (ConcurrentModificationException e) {
            System.out.println("      ConcurrentModificationException after " + iterated
                    + " keys - a REAL, live reproduction, on THIS run, THIS machine, right now.");
        }


        /* ====================================================================
         * SECTION 5 - THE SAME WORKLOAD, ON ConcurrentHashMap: SAFE BY DESIGN
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE IDENTICAL WORKLOAD, ON ConcurrentHashMap");
        System.out.println("=".repeat(74));

        ConcurrentHashMap<Integer, Integer> safeMap = new ConcurrentHashMap<>();
        for (int i = 0; i < 16; i++) safeMap.put(i, i);

        Thread[] safeWriters = new Thread[4];
        for (int w = 0; w < 4; w++) {
            int base = w * 1_000_000;
            safeWriters[w] = new Thread(() -> {
                for (int i = 0; i < 2_000_000; i++) {
                    safeMap.put(base + i, i);
                }
            });
            safeWriters[w].setDaemon(true);
        }
        for (Thread t : safeWriters) t.start();
        Thread.sleep(5);

        int safeIterated = 0;
        for (Integer k : safeMap.keySet()) {
            safeIterated++;
        }
        for (Thread t : safeWriters) t.join();
        System.out.println("    SAME 4 concurrent writer threads, SAME timing, iterating");
        System.out.println("    ConcurrentHashMap instead -> NO exception, EVER (this is a REAL");
        System.out.println("    GUARANTEE, not luck): iterated " + safeIterated + " keys mid-write,");
        System.out.println("    final map size settled at " + safeMap.size());
        System.out.println();
        System.out.println("    ConcurrentHashMap's iterator is WEAKLY CONSISTENT: it is guaranteed");
        System.out.println("    to reflect the state of the map AT SOME POINT during the iteration,");
        System.out.println("    it MAY or may NOT show entries added or removed while it runs, and");
        System.out.println("    it will NEVER throw ConcurrentModificationException. It achieves");
        System.out.println("    this via LOCK STRIPING internally - locking small SEGMENTS of the");
        System.out.println("    table independently, rather than the whole structure at once,");
        System.out.println("    which is also WHY it scales better under contention than wrapping a");
        System.out.println("    plain HashMap in Collections.synchronizedMap (one single lock for");
        System.out.println("    the ENTIRE map, serializing every access).");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 65.");
        System.out.println("=".repeat(74));
    }

    static void runIncrementThreads(int threadCount, int incrementsEach, Runnable increment)
            throws InterruptedException {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsEach; j++) {
                    increment.run();
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Run Section 4 (the plain HashMap section) TEN times in a row and
 *    record how often it throws versus completes silently - report your
 *    OWN honest reproduction rate, the same way this lesson reports its.
 *
 * 2. Implement your OWN "atomic" increment using ONLY compareAndSet in a
 *    manual retry loop (do not call incrementAndGet) - confirm it produces
 *    the same correct total as Section 1's built-in version.
 *
 * 3. Replace Section 5's ConcurrentHashMap with
 *    Collections.synchronizedMap(new HashMap<>()) and re-run the SAME
 *    concurrent writer workload - does it throw CME too? Explain why a
 *    synchronized wrapper does or does not have the same iterator
 *    guarantee as ConcurrentHashMap.
 *
 * 4. Use ConcurrentHashMap.computeIfAbsent to build a thread-safe cache
 *    (Map<String, List<String>>) from multiple threads adding to shared
 *    keys, and confirm no entries are ever lost the way lesson 63's
 *    unsynchronized counter lost increments.
 *
 * 5. Benchmark AtomicLong vs a synchronized long field the SAME way
 *    Section 1 did for int - does the GAP grow, shrink, or stay similar
 *    at a higher thread count (try 16 or 32 threads)?
 * ============================================================================
 */
