/* ============================================================================
 * 38 - IMMUTABILITY AND DEFENSIVE COPYING
 * ----------------------------------------------------------------------------
 * Companion lesson: 38-immutability-and-defensive-copying.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/38-immutability-and-defensive-copying.java
 *
 * An IMMUTABLE object cannot change after construction. This is the single
 * highest-value design habit in Java, and it ties together final (31),
 * equals/hashCode (33), encapsulation (24) and records (36).
 *
 * Section 3 is the part people get wrong: `private final` protects the FIELD,
 * not the OBJECT it points at.
 * ============================================================================
 */

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

class ImmutabilityAndDefensiveCopying {

    public static void main(String[] args) throws InterruptedException {

        /* ====================================================================
         * SECTION 1 - WHAT IT BUYS YOU
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WHY BOTHER");
        System.out.println("=".repeat(74));

        System.out.printf("    %-26s %s%n", "BENEFIT", "WHY");
        System.out.printf("    %-26s %s%n", "thread safety, free", "nothing changes, nothing to synchronise");
        System.out.printf("    %-26s %s%n", "safe as a hash key", "the hashCode can never drift");
        System.out.printf("    %-26s %s%n", "safe to share", "hand out the same instance to anyone");
        System.out.printf("    %-26s %s%n", "safe to cache", "the value is the value, forever");
        System.out.printf("    %-26s %s%n", "simpler reasoning", "assigned once, trustworthy thereafter");
        System.out.printf("    %-26s %s%n", "failure atomicity", "a failed op cannot half-update it");

        /* --------------------------------------------------------------------
         * THREAD SAFETY, DEMONSTRATED. A mutable counter loses updates under
         * concurrency; an immutable value cannot, because nothing writes to it.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THREAD SAFETY IS NOT A SMALL POINT. Eight threads reading:");

        MutableCounter mutable = new MutableCounter();
        ImmutableValue immutable = new ImmutableValue(100);

        int threads = 8;
        int operationsEach = 200_000;
        runConcurrently(threads, () -> {
            for (int i = 0; i < operationsEach; i++) {
                mutable.increment();
            }
        });

        int[] observed = new int[threads];
        runConcurrentlyIndexed(threads, index -> {
            int total = 0;
            for (int i = 0; i < operationsEach; i++) {
                total += immutable.value();      // always 100, from every thread
            }
            observed[index] = total;
        });

        System.out.printf("    mutable counter, expected %,d -> %,d   %s%n",
                threads * operationsEach, mutable.get(),
                mutable.get() == threads * operationsEach ? "(lucky)" : "<- UPDATES LOST");
        System.out.println("    immutable value, every thread agreed? "
                + Arrays.stream(observed).allMatch(t -> t == operationsEach * 100));
        System.out.println();
        System.out.println("  The immutable object needed NO synchronized, NO volatile, NO");
        System.out.println("  locks. The final-field memory-model guarantee (lesson 31) makes");
        System.out.println("  it real: once the constructor returns, every thread sees the");
        System.out.println("  fully-built object.");


        /* ====================================================================
         * SECTION 2 - THE FIVE RULES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE FIVE RULES");
        System.out.println("=".repeat(74));

        System.out.println("    1. Make the CLASS final (or private ctor + static factories)");
        System.out.println("       so a subclass cannot add mutability");
        System.out.println("    2. Make every FIELD private final");
        System.out.println("    3. Provide NO SETTERS, and no method that changes state");
        System.out.println("    4. DEFENSIVELY COPY mutable objects on the way IN");
        System.out.println("    5. DEFENSIVELY COPY mutable objects on the way OUT");
        System.out.println();
        System.out.println("  Rules 4 and 5 are the ones people forget, and they are what the");
        System.out.println("  rest of this lesson is about.");


        /* ====================================================================
         * SECTION 3 - THE TWO LEAKS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - private final PROTECTS THE FIELD, NOT THE OBJECT");
        System.out.println("=".repeat(74));

        System.out.println("  LEAK 1 - storing the caller's mutable object:");

        Date callerStart = new Date(1_000_000_000_000L);
        Date callerEnd = new Date(2_000_000_000_000L);
        LeakyPeriod leaky = new LeakyPeriod(callerStart, callerEnd);

        System.out.println("    created  -> " + leaky);
        callerStart.setTime(0);                       // the caller still holds it
        System.out.println("    caller did start.setTime(0)");
        System.out.println("    now      -> " + leaky + "   <- the 'immutable' object CHANGED");

        System.out.println();
        System.out.println("  LEAK 2 - handing out your own mutable object:");

        LeakyPeriod second = new LeakyPeriod(new Date(1_000_000_000_000L),
                new Date(2_000_000_000_000L));
        System.out.println("    created  -> " + second);
        second.getStart().setTime(0);                 // mutating what the getter returned
        System.out.println("    caller did period.getStart().setTime(0)");
        System.out.println("    now      -> " + second + "   <- CHANGED again");

        System.out.println();
        System.out.println("  BOTH leaks must be closed. Closing one and not the other leaves");
        System.out.println("  the object mutable - as the two examples above show.");

        System.out.println();
        System.out.println("  THE FIX - copy IN and copy OUT:");

        Date safeStart = new Date(1_000_000_000_000L);
        Date safeEnd = new Date(2_000_000_000_000L);
        SafePeriod safe = new SafePeriod(safeStart, safeEnd);

        System.out.println("    created  -> " + safe);
        safeStart.setTime(0);
        safe.getStart().setTime(0);
        System.out.println("    caller mutated BOTH the original AND the getter's result");
        System.out.println("    now      -> " + safe + "   <- untouched");

        /* --------------------------------------------------------------------
         * THE VALIDATION ORDERING TRAP.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE VALIDATION ORDERING TRAP:");
        System.out.println();
        System.out.println("      // WRONG - checks the ORIGINAL, then copies");
        System.out.println("      if (start.after(end)) throw ...;");
        System.out.println("      this.start = new Date(start.getTime());");
        System.out.println();
        System.out.println("    Between the check and the copy, another thread could mutate");
        System.out.println("    `start`. That is a TIME-OF-CHECK TO TIME-OF-USE bug, and it is");
        System.out.println("    the mechanism behind several historical JDK vulnerabilities.");
        System.out.println();
        System.out.println("      // RIGHT - copy FIRST, then validate the COPY");
        System.out.println("      this.start = new Date(start.getTime());");
        System.out.println("      this.end   = new Date(end.getTime());");
        System.out.println("      if (this.start.after(this.end)) throw ...;");
        System.out.println();
        System.out.println("    SafePeriod does it in the right order - see the source.");
        try {
            new SafePeriod(new Date(2_000_000_000_000L), new Date(1_000_000_000_000L));
        } catch (IllegalArgumentException e) {
            System.out.println("    new SafePeriod(later, earlier) -> " + e.getMessage());
        }


        /* ====================================================================
         * SECTION 4 - COPY, VIEW, OR IMMUTABLE TYPE?
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THREE WAYS TO HAND OUT A COLLECTION");
        System.out.println("=".repeat(74));

        List<String> backing = new ArrayList<>(List.of("Danish", "Aisha"));

        List<String> mutableCopy = new ArrayList<>(backing);
        List<String> unmodifiableView = Collections.unmodifiableList(backing);
        List<String> immutableCopy = List.copyOf(backing);

        System.out.printf("    %-38s %-10s %-16s %s%n",
                "APPROACH", "COST", "CALLER CAN EDIT", "SEES LATER CHANGES");
        System.out.printf("    %-38s %-10s %-16s %s%n",
                "new ArrayList<>(list)", "O(n)", "yes, their copy", "no");
        System.out.printf("    %-38s %-10s %-16s %s%n",
                "Collections.unmodifiableList(list)", "O(1)", "no", "YES - a live view");
        System.out.printf("    %-38s %-10s %-16s %s%n",
                "List.copyOf(list)", "O(n)", "no", "no");

        System.out.println();
        System.out.println("    all three initially -> " + mutableCopy + " / "
                + unmodifiableView + " / " + immutableCopy);

        backing.add("Rahul");
        System.out.println("    after backing.add(\"Rahul\"):");
        System.out.println("      mutable copy       -> " + mutableCopy + "   independent");
        System.out.println("      unmodifiable VIEW  -> " + unmodifiableView
                + "   <- SAW the change");
        System.out.println("      immutable copy     -> " + immutableCopy + "   independent");

        System.out.println();
        System.out.println("    Collections.unmodifiableList is a VIEW: the caller cannot");
        System.out.println("    modify it, but WILL see changes you make afterwards. For a");
        System.out.println("    truly immutable object either is fine; for a MUTABLE class");
        System.out.println("    exposing a read-only view, the distinction matters.");

        System.out.println();
        System.out.println("    List.copyOf is usually right - independent AND unmodifiable.");
        System.out.println("    It also REJECTS null elements:");
        List<String> withNull = new ArrayList<>();
        withNull.add("ok");
        withNull.add(null);
        try {
            List.copyOf(withNull);
        } catch (NullPointerException e) {
            System.out.println("      List.copyOf(listContainingNull) -> NullPointerException");
        }

        System.out.println();
        System.out.println("  NOT THE SAME THING (lesson 16):");
        System.out.println("    List.of(...)        -> genuinely immutable, rejects null");
        System.out.println("    Arrays.asList(...)  -> a FIXED-SIZE MUTABLE VIEW over the array");
        String[] array = {"a", "b"};
        List<String> asListView = Arrays.asList(array);
        asListView.set(0, "CHANGED");
        System.out.println("      asList.set(0, \"CHANGED\") -> the ARRAY is now "
                + Arrays.toString(array));


        /* ====================================================================
         * SECTION 5 - WHICH TYPES ARE ALREADY IMMUTABLE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - KNOW WHICH TYPES NEED COPYING");
        System.out.println("=".repeat(74));

        System.out.println("  IMMUTABLE - share freely, no copying needed:");
        System.out.println("    String, all primitive wrappers, BigInteger, BigDecimal,");
        System.out.println("    LocalDate, LocalDateTime, Instant, Duration, UUID, enums,");
        System.out.println("    records of immutable components, List.of/Map.of/Set.of, Optional");

        System.out.println();
        System.out.println("  MUTABLE - must be copied:");
        System.out.println("    Date, Calendar, StringBuilder, ARRAYS (always), ArrayList and");
        System.out.println("    every standard collection, SimpleDateFormat, most of your own");
        System.out.println("    classes");

        System.out.println();
        System.out.println("  ARRAYS ARE ALWAYS MUTABLE - there is no immutable array in Java:");
        final int[] finalArray = {1, 2, 3};
        System.out.println("    final int[] a = {1, 2, 3};");
        finalArray[0] = 99;
        System.out.println("    a[0] = 99;   -> " + Arrays.toString(finalArray)
                + "   `final` did not stop it");
        System.out.println("    To expose one safely: return a.clone(), or List.copyOf.");

        LeakyScores leakyScores = new LeakyScores(new int[]{10, 20, 30});
        System.out.println();
        System.out.println("    a class returning its internal array:");
        System.out.println("      " + leakyScores);
        leakyScores.getScores()[0] = 999;
        System.out.println("      caller did getScores()[0] = 999");
        System.out.println("      " + leakyScores + "   <- mutated");

        SafeScores safeScores = new SafeScores(new int[]{10, 20, 30});
        System.out.println();
        System.out.println("    the same class returning a clone:");
        System.out.println("      " + safeScores);
        safeScores.getScores()[0] = 999;
        System.out.println("      caller did getScores()[0] = 999");
        System.out.println("      " + safeScores + "   <- untouched");

        System.out.println();
        System.out.println("  java.util.Date being mutable is a genuine historical mistake,");
        System.out.println("  and the whole reason java.time was designed immutable:");
        LocalDate today = LocalDate.of(2026, 8, 28);
        LocalDate later = today.plusDays(10);
        System.out.println("    LocalDate today = " + today);
        System.out.println("    today.plusDays(10) -> " + later);
        System.out.println("    today is still     -> " + today + "   (lesson 60)");


        /* ====================================================================
         * SECTION 6 - CHANGING BY RETURNING A NEW OBJECT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - IMMUTABLE DOES NOT MEAN INERT");
        System.out.println("=".repeat(74));

        Money price = new Money(1999, "INR");
        Money tax = new Money(360, "INR");
        Money total = price.plus(tax);
        Money doubled = total.times(2);

        System.out.println("    Money price = " + price);
        System.out.println("    price.plus(tax)    -> " + total);
        System.out.println("    total.times(2)     -> " + doubled);
        System.out.println("    price is still     -> " + price + "   unchanged throughout");

        try {
            price.plus(new Money(100, "USD"));
        } catch (IllegalArgumentException e) {
            System.out.println("    price.plus(USD)    -> " + e.getMessage());
            System.out.println("    FAILURE ATOMICITY: the operation failed and `price` is");
            System.out.println("    still " + price + " - it could not be half-updated.");
        }

        System.out.println();
        System.out.println("  THE NAMING CONVENTION MATTERS:");
        System.out.println("    plusX, withX, toX  -> returns a NEW object");
        System.out.println("    setX, addX         -> MUTATES this one");
        System.out.println("    String.replace returning a new String is exactly this");
        System.out.println("    convention (lesson 14).");

        System.out.println();
        System.out.println("  THE COST, AND WHY IT USUALLY DOES NOT MATTER:");

        int iterations = 5_000_000;

        long startImmutable = System.nanoTime();
        Money running = new Money(0, "INR");
        for (int i = 0; i < iterations; i++) {
            running = running.plus(new Money(1, "INR"));    // TWO objects per iteration
        }
        long immutableMillis = (System.nanoTime() - startImmutable) / 1_000_000;

        long startMutable = System.nanoTime();
        MutableMoney mutableMoney = new MutableMoney(0, "INR");
        for (int i = 0; i < iterations; i++) {
            mutableMoney.add(1);
        }
        long mutableMillis = (System.nanoTime() - startMutable) / 1_000_000;

        System.out.printf("    %,d additions:%n", iterations);
        System.out.println("      immutable (2 new objects per step) -> " + immutableMillis + " ms");
        System.out.println("      mutable   (in place)               -> " + mutableMillis + " ms");
        System.out.println("      same answer? " + (running.amount() == mutableMoney.getAmount()));
        System.out.println();
        System.out.printf("    BE HONEST ABOUT THAT: the ratio is real, but so are the%n");
        System.out.printf("    ABSOLUTE numbers - %d ms for %,d operations is about %d ns%n",
                immutableMillis, iterations,
                immutableMillis * 1_000_000 / iterations);
        System.out.println("    each. If your code does five million money additions in a hot");
        System.out.println("    loop, optimise it. If it handles a few thousand requests a");
        System.out.println("    second, this is invisible next to one database call.");
        System.out.println();
        System.out.println("    Allocation is CHEAP on the JVM: young-generation allocation is");
        System.out.println("    close to a pointer bump, short-lived objects are collected");
        System.out.println("    almost for free, and escape analysis can remove it entirely.");
        System.out.println();
        System.out.println("    The genuine exception is building a VALUE IN A LOOP, which is");
        System.out.println("    exactly why StringBuilder exists (lesson 15). MEASURE before");
        System.out.println("    trading away immutability.");


        /* ====================================================================
         * SECTION 7 - RECORDS GET MOST OF THIS FREE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WHAT A RECORD GIVES YOU, AND WHAT IT DOES NOT");
        System.out.println("=".repeat(74));

        System.out.printf("    %-42s %s%n", "1. final class", "AUTOMATIC");
        System.out.printf("    %-42s %s%n", "2. private final fields", "AUTOMATIC");
        System.out.printf("    %-42s %s%n", "3. no setters", "AUTOMATIC");
        System.out.printf("    %-42s %s%n", "4. copy IN", "YOU must write it");
        System.out.printf("    %-42s %s%n", "5. copy OUT", "YOU must write it");

        System.out.println();
        System.out.println("  A record with a validating compact constructor and a copy:");
        PeriodRecord periodRecord = new PeriodRecord(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), new ArrayList<>(List.of("Q1")));
        System.out.println("    " + periodRecord);
        try {
            periodRecord.tags().add("injected");
        } catch (UnsupportedOperationException e) {
            System.out.println("    record.tags().add(...) -> UnsupportedOperationException");
        }
        try {
            new PeriodRecord(LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1), List.of());
        } catch (IllegalArgumentException e) {
            System.out.println("    new PeriodRecord(later, earlier) -> " + e.getMessage());
        }

        System.out.println();
        System.out.println("  The LocalDate components need NO copying - java.time is already");
        System.out.println("  immutable. Only the List did. Knowing which types are which");
        System.out.println("  (Section 5) is what tells you where to copy.");


        /* ====================================================================
         * SECTION 8 - AND AS A HASH KEY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - WHY IMMUTABLE TYPES MAKE SAFE KEYS");
        System.out.println("=".repeat(74));

        Set<Money> moneySet = new HashSet<>();
        Money key = new Money(500, "INR");
        moneySet.add(key);

        System.out.println("    an IMMUTABLE key:");
        System.out.println("      set.contains(key) -> " + moneySet.contains(key));
        System.out.println("      There is no setter, so its hashCode can NEVER drift.");
        System.out.println("      Lesson 33's unreachable-entry disaster is impossible.");

        Set<MutableMoney> mutableSet = new HashSet<>();
        MutableMoney mutableKey = new MutableMoney(500, "INR");
        mutableSet.add(mutableKey);
        System.out.println();
        System.out.println("    a MUTABLE key:");
        System.out.println("      set.contains(key) -> " + mutableSet.contains(mutableKey));
        mutableKey.add(1);
        System.out.println("      after key.add(1):");
        System.out.println("      set.contains(key) -> " + mutableSet.contains(mutableKey)
                + "   <- unreachable and unremovable");


        /* ====================================================================
         * SECTION 9 - WHEN NOT TO
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - WHEN IMMUTABILITY IS THE WRONG CHOICE");
        System.out.println("=".repeat(74));

        System.out.println("    - LARGE objects with FREQUENT SMALL CHANGES. Copying a");
        System.out.println("      10,000-element structure per edit is real cost. Consider a");
        System.out.println("      builder, or a persistent data structure.");
        System.out.println();
        System.out.println("    - Objects with genuine IDENTITY and a LIFECYCLE. A database");
        System.out.println("      entity that is UPDATED is naturally mutable.");
        System.out.println();
        System.out.println("    - Performance-critical inner loops - AFTER PROFILING.");
        System.out.println();
        System.out.println("    - Framework requirements. Some older frameworks need a no-arg");
        System.out.println("      constructor and setters.");
        System.out.println();
        System.out.println("  Even then, prefer IMMUTABLE VALUE OBJECTS inside mutable");
        System.out.println("  entities. The rule is not 'never mutate'. It is:");
        System.out.println();
        System.out.println("      DEFAULT TO IMMUTABLE, AND MUTATE DELIBERATELY.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 38.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Runs an action on several threads and waits for all of them.
     *
     * @param threadCount how many threads
     * @param action      the work
     * @throws InterruptedException if interrupted
     */
    static void runConcurrently(int threadCount, Runnable action) throws InterruptedException {
        CountDownLatch finished = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    action.run();
                } finally {
                    finished.countDown();
                }
            }).start();
        }
        finished.await();
    }

    /**
     * The same, but each thread knows its own index.
     *
     * @param threadCount how many threads
     * @param action      the work, given the thread index
     * @throws InterruptedException if interrupted
     */
    static void runConcurrentlyIndexed(int threadCount, java.util.function.IntConsumer action)
            throws InterruptedException {
        CountDownLatch finished = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    action.accept(index);
                } finally {
                    finished.countDown();
                }
            }).start();
        }
        finished.await();
    }
}

// ----------------------------------------------------------------------------
// SECTION 1 - THREAD SAFETY
// ----------------------------------------------------------------------------

/** Mutable, and therefore needs synchronisation it does not have. */
class MutableCounter {

    private int count = 0;

    /** NOT atomic: read, increment, write. */
    void increment() {
        count++;
    }

    /** @return the current count */
    int get() {
        return count;
    }
}

/** Immutable, and therefore thread-safe with no synchronisation at all. */
final class ImmutableValue {

    private final int value;

    /** @param value the fixed value */
    ImmutableValue(int value) {
        this.value = value;
    }

    /** @return the value - identical from every thread, forever */
    int value() {
        return value;
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - THE TWO LEAKS
// ----------------------------------------------------------------------------

/**
 * Looks immutable - final class, private final fields, no setters - and is
 * not, because it stores and returns a mutable Date.
 */
final class LeakyPeriod {

    private final Date start;
    private final Date end;

    /** @param start the start  @param end the end */
    LeakyPeriod(Date start, Date end) {
        // LEAK 1: stores the caller's object. They still hold a reference.
        this.start = start;
        this.end = end;
    }

    /** LEAK 2: hands the caller our internal object. @return the start */
    Date getStart() {
        return start;
    }

    @Override
    public String toString() {
        return "Period[" + start.getTime() + " .. " + end.getTime() + "]";
    }
}

/** The same class with both leaks closed, and validation in the right order. */
final class SafePeriod {

    private final Date start;
    private final Date end;

    /**
     * @param start the start; copied
     * @param end   the end; copied
     * @throws IllegalArgumentException if the start is after the end
     */
    SafePeriod(Date start, Date end) {
        // COPY FIRST...
        this.start = new Date(start.getTime());
        this.end = new Date(end.getTime());

        // ...THEN VALIDATE THE COPY. Validating the ORIGINAL would be a
        // time-of-check to time-of-use bug: another thread could mutate it
        // between the check and the copy.
        if (this.start.after(this.end)) {
            throw new IllegalArgumentException("start must not be after end");
        }
    }

    /** @return a COPY, so the caller cannot reach our field */
    Date getStart() {
        return new Date(start.getTime());
    }

    /** @return a COPY */
    Date getEnd() {
        return new Date(end.getTime());
    }

    @Override
    public String toString() {
        return "Period[" + start.getTime() + " .. " + end.getTime() + "]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - ARRAYS
// ----------------------------------------------------------------------------

/** Returns its internal array, which is always a leak. */
final class LeakyScores {

    private final int[] scores;

    /** @param scores the scores */
    LeakyScores(int[] scores) {
        this.scores = scores;
    }

    /** @return the internal array itself */
    int[] getScores() {
        return scores;
    }

    @Override
    public String toString() {
        return "Scores" + Arrays.toString(scores);
    }
}

/** Copies in and clones out - the only safe way to hold an array. */
final class SafeScores {

    private final int[] scores;

    /** @param scores the scores; copied */
    SafeScores(int[] scores) {
        this.scores = scores.clone();
    }

    /** @return a clone, so the caller cannot reach our array */
    int[] getScores() {
        return scores.clone();
    }

    @Override
    public String toString() {
        return "Scores" + Arrays.toString(scores);
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 AND 8 - VALUE OBJECTS
// ----------------------------------------------------------------------------

/** An immutable money value. Every operation returns a NEW object. */
final class Money {

    private final long amount;
    private final String currency;

    /** @param amount the amount in the smallest unit  @param currency the code */
    Money(long amount, String currency) {
        this.amount = amount;
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    /** @return the amount */
    long amount() {
        return amount;
    }

    /**
     * Named plusX, not addX, to signal that it returns a new object.
     *
     * @param other the money to add; must be the same currency
     * @return a NEW Money
     * @throws IllegalArgumentException on a currency mismatch
     */
    Money plus(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "currency mismatch: " + currency + " and " + other.currency);
        }
        return new Money(amount + other.amount, currency);
    }

    /** @param factor the multiplier  @return a NEW Money */
    Money times(int factor) {
        return new Money(amount * factor, currency);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Money money)) return false;
        return amount == money.amount && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency + " " + (amount / 100.0);
    }
}

/** The mutable equivalent, for the benchmark and the hash-key demonstration. */
final class MutableMoney {

    private long amount;
    private final String currency;

    /** @param amount the starting amount  @param currency the code */
    MutableMoney(long amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    /** @param delta how much to add, in place */
    void add(long delta) {
        this.amount += delta;
    }

    /** @return the current amount */
    long getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof MutableMoney money)) return false;
        return amount == money.amount && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - A RECORD DOING IT PROPERLY
// ----------------------------------------------------------------------------

/**
 * A record gets rules 1-3 free. Rules 4 and 5 still need writing - here the
 * List needs copying, while the LocalDates do not because java.time is
 * already immutable.
 *
 * @param start the start date
 * @param end   the end date
 * @param tags  descriptive tags; copied defensively
 */
record PeriodRecord(LocalDate start, LocalDate end, List<String> tags) {

    /** Copies the mutable component, then validates. */
    PeriodRecord {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        tags = List.copyOf(tags);          // rule 4: copy IN
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must not be after end");
        }
    }

    // Rule 5 comes free here: the generated accessor returns the immutable
    // copy made above, so there is nothing for a caller to mutate.
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Fix LeakyPeriod one leak at a time. After fixing only the constructor,
 *    show the object can still be mutated. Then fix the getter too.
 *
 * 2. Write an immutable Matrix class holding a double[][]. Remember that a
 *    shallow clone() of a 2D array shares the rows (lesson 13).
 *
 * 3. Take SafePeriod and change it to validate BEFORE copying. Then write a
 *    two-thread test that exploits the window. It is harder than it looks -
 *    which is why the bug survives code review.
 *
 * 4. Replace Money's `long amount` with a BigDecimal. Does anything about the
 *    immutability change? Why not?
 *
 * 5. Add a `List<String> notes` to Money and make it immutable properly.
 *    Then convert Money to a record and confirm you still need the copy.
 *
 * 6. Find a class in your own code with a getter returning a List, Map, array
 *    or Date. Write the line that corrupts it from outside. Then fix it, and
 *    decide whether the class should have been immutable all along.
 * ============================================================================
 */
