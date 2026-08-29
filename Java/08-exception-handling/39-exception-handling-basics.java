/* ============================================================================
 * 39 - EXCEPTIONS: HIERARCHY, try / catch / finally
 * ----------------------------------------------------------------------------
 * Companion lesson: 39-exception-handling-basics.md
 *
 * RUN IT:
 *     java Java/08-exception-handling/39-exception-handling-basics.java
 *
 * An exception is an object describing something that went wrong. Java's
 * exception system is one of its most distinctive features - and the one most
 * consistently misused.
 *
 * Section 3 is the part that costs people days: what `finally` silently does
 * to your return values and your exceptions.
 * ============================================================================
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class ExceptionHandlingBasics {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE HIERARCHY
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THREE BRANCHES, THREE DIFFERENT MEANINGS");
        System.out.println("=".repeat(74));

        System.out.println("                      Throwable");
        System.out.println("                     /         \\");
        System.out.println("                Error           Exception");
        System.out.println("               /     \\         /         \\");
        System.out.println("  StackOverflow  OutOfMemory  RuntimeException   IOException");
        System.out.println("                              /      |       \\    SQLException");
        System.out.println("                NullPointer  Illegal  IndexOutOf  (CHECKED)");
        System.out.println("                (UNCHECKED)  Argument  Bounds");

        System.out.println();
        System.out.printf("    %-24s %-10s %-30s %s%n",
                "BRANCH", "CHECKED?", "MEANING", "YOU SHOULD");
        System.out.printf("    %-24s %-10s %-30s %s%n",
                "Error", "no", "the JVM is in trouble", "NEVER catch");
        System.out.printf("    %-24s %-10s %-30s %s%n",
                "RuntimeException", "no", "a PROGRAMMING BUG", "fix the bug");
        System.out.printf("    %-24s %-10s %-30s %s%n",
                "other Exception", "YES", "an expected external failure", "handle or declare");

        System.out.println();
        System.out.println("  Proving the hierarchy from the classes themselves:");
        for (Class<?> type : new Class<?>[]{
                NullPointerException.class, IllegalArgumentException.class,
                IOException.class, FileNotFoundException.class,
                SQLException.class, OutOfMemoryError.class}) {
            System.out.printf("    %-28s %-12s %s%n",
                    type.getSimpleName(),
                    isChecked(type) ? "CHECKED" : "unchecked",
                    chainToThrowable(type));
        }

        System.out.println();
        System.out.println("  THE ONE THAT TRIPS PEOPLE UP:");
        System.out.println("    RuntimeException EXTENDS Exception -> "
                + Exception.class.isAssignableFrom(RuntimeException.class));
        System.out.println("    So `catch (Exception e)` catches UNCHECKED exceptions too,");
        System.out.println("    including every NullPointerException in your code. That is");
        System.out.println("    exactly why it is so dangerous - Section 7.");

        System.out.println();
        System.out.println("  UNCHECKED exceptions are BUGS, not conditions. Each of these");
        System.out.println("  signals a DEFECT IN CODE, and the fix is to correct the code:");
        demonstrateUnchecked();


        /* ====================================================================
         * SECTION 2 - try / catch / finally
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE MECHANICS");
        System.out.println("=".repeat(74));

        System.out.println("  Catch blocks are tested TOP TO BOTTOM, first match wins:");
        for (String path : new String[]{"does-not-exist.txt", null}) {
            System.out.println("    reading " + path + ":");
            readWithOrderedCatches(path);
        }

        System.out.println();
        System.out.println("  A BROADER type before a narrower one is a COMPILE ERROR:");
        System.out.println("      catch (Exception e) { }");
        System.out.println("      catch (IOException e) { }");
        System.out.println("      -> error: exception IOException has already been caught");
        System.out.println("    The second block would be unreachable, so javac refuses.");

        System.out.println();
        System.out.println("  MULTI-CATCH (Java 7+) when the handling is identical:");
        for (int scenario = 0; scenario < 3; scenario++) {
            multiCatchExample(scenario);
        }
        System.out.println("    The variable is implicitly FINAL, and its static type is the");
        System.out.println("    NEAREST COMMON SUPERTYPE - so you can only call methods");
        System.out.println("    available on that type.");

        System.out.println();
        System.out.println("  finally ALWAYS runs - normal completion, exception, or return:");
        System.out.println("    normal path  -> " + finallyOnNormalPath());
        System.out.println("    return path  -> " + finallyOnReturnPath());
        System.out.print("    exception path -> ");
        try {
            finallyOnExceptionPath();
        } catch (IllegalStateException e) {
            System.out.println("the exception still propagated: " + e.getMessage());
        }
        System.out.println();
        System.out.println("    The only things that prevent it: System.exit(), a JVM crash,");
        System.out.println("    or the thread being killed.");


        /* ====================================================================
         * SECTION 3 - THE finally TRAPS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - WHAT finally SILENTLY DOES TO YOU");
        System.out.println("=".repeat(74));

        System.out.println("  TRAP 1 - a `return` in finally SWALLOWS the exception:");
        System.out.println();
        System.out.println("      try    { throw new RuntimeException(\"real problem\"); }");
        System.out.println("      finally { return \"everything is fine\"; }");
        System.out.println();
        System.out.println("    result -> " + returnInFinallySwallowsException());
        System.out.println("    THE EXCEPTION VANISHED. No log, no trace, no evidence.");
        System.out.println();
        System.out.println("    NEVER put return, break or continue in a finally block. Most");
        System.out.println("    linters flag it, and it is one of the nastiest bugs in Java");
        System.out.println("    precisely because the evidence disappears.");

        System.out.println();
        System.out.println("  TRAP 2 - finally CANNOT change an already-evaluated return:");
        System.out.println();
        System.out.println("      int value = 1;");
        System.out.println("      try     { return value; }   // the VALUE 1 is captured here");
        System.out.println("      finally { value = 99; }     // too late");
        System.out.println();
        System.out.println("    returns -> " + finallyCannotChangeReturnValue() + ", not 99");
        System.out.println("    The return value is evaluated BEFORE finally runs, so mutating");
        System.out.println("    the variable afterwards has no effect. Surprises everyone once.");

        System.out.println();
        System.out.println("  TRAP 3 - an exception in finally MASKS the original:");
        try {
            exceptionInFinallyMasksOriginal();
        } catch (RuntimeException e) {
            System.out.println("    caught -> " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            System.out.println("    cause  -> " + e.getCause());
            System.out.println();
            System.out.println("    The REAL cause - IllegalStateException(\"the real cause\") -");
            System.out.println("    was discarded entirely. You are now debugging the cleanup");
            System.out.println("    code instead of the actual failure.");
            System.out.println();
            System.out.println("    This is EXACTLY the problem try-with-resources solves with");
            System.out.println("    SUPPRESSED exceptions. Lesson 41.");
        }


        /* ====================================================================
         * SECTION 4 - throw VERSUS throws
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - throw AND throws ARE DIFFERENT THINGS");
        System.out.println("=".repeat(74));

        System.out.println("      void readFile(String path) throws IOException {   // DECLARES");
        System.out.println("          if (path == null) {");
        System.out.println("              throw new IllegalArgumentException(\"path\");  // RAISES");
        System.out.println("          }");
        System.out.println("      }");
        System.out.println();
        System.out.println("    throw  - a STATEMENT that raises an exception now");
        System.out.println("    throws - a CLAUSE declaring what may escape this method");
        System.out.println();
        System.out.println("  Only CHECKED exceptions MUST be declared. Unchecked ones may be");
        System.out.println("  declared for documentation, but the compiler does not require it");
        System.out.println("  and it is usually noise.");

        System.out.println();
        System.out.println("  The compiler enforces the checked ones. This does not compile:");
        System.out.println("      Files.readString(Path.of(\"x\"));   // unhandled IOException");
        System.out.println("      -> error: unreported exception IOException; must be caught");
        System.out.println("         or declared to be thrown");

        System.out.println();
        System.out.println("  THE OVERRIDE RULE (lesson 27): a subclass may throw the SAME,");
        System.out.println("  NARROWER, or FEWER checked exceptions - never more:");
        System.out.println("    base:     throws IOException");
        System.out.println("    ok:       throws FileNotFoundException   (narrower)");
        System.out.println("    ok:       throws nothing                 (fewer)");
        System.out.println("    ERROR:    throws SQLException            (a NEW checked type)");
        System.out.println();
        System.out.println("  Otherwise a caller holding a supertype reference, catching only");
        System.out.println("  IOException, would be blindsided.");


        /* ====================================================================
         * SECTION 5 - READING A STACK TRACE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - READING A STACK TRACE");
        System.out.println("=".repeat(74));

        try {
            topLevelOperation();
        } catch (RuntimeException e) {
            System.out.println("  The exception, printed the way a log would show it:");
            System.out.println();
            printTrace(e);

            System.out.println();
            System.out.println("  READ FROM THE TOP:");
            System.out.println("    line 1        - the exception TYPE and MESSAGE");
            System.out.println("    first `at`    - where it was THROWN");
            System.out.println("    each `at` below - the caller, going outwards");
            System.out.println("    `Caused by`   - the ORIGINAL exception in the chain,");
            System.out.println("                    and usually the interesting part");
        }

        System.out.println();
        System.out.println("  HELPFUL NULLPOINTER MESSAGES (Java 14+) name the exact");
        System.out.println("  expression that was null:");
        try {
            String text = null;
            System.out.println(text.length());
        } catch (NullPointerException e) {
            System.out.println("    " + e.getMessage());
            System.out.println("    Before Java 14 you got a line number and had to guess");
            System.out.println("    which of five dots on that line was the problem.");
        }


        /* ====================================================================
         * SECTION 6 - EXCEPTION CHAINING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - ALWAYS PRESERVE THE CAUSE");
        System.out.println("=".repeat(74));

        System.out.println("  WITHOUT the cause - the original is thrown away:");
        try {
            loadUserLosingCause(42);
        } catch (RuntimeException e) {
            System.out.println("    " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("    getCause() -> " + e.getCause());
            System.out.println("    The person debugging this at 3 a.m. has NOTHING. Which");
            System.out.println("    query failed? Which table? Which connection? Gone.");
        }

        System.out.println();
        System.out.println("  WITH the cause - one extra argument:");
        try {
            loadUserPreservingCause(42);
        } catch (RuntimeException e) {
            System.out.println("    " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("    getCause() -> " + e.getCause());
            System.out.println();
            System.out.println("    ...and the full chain is walkable:");
            for (Throwable current = e; current != null; current = current.getCause()) {
                System.out.println("      " + current.getClass().getSimpleName()
                        + ": " + current.getMessage());
            }
        }

        System.out.println();
        System.out.println("      throw new DataAccessException(\"failed to load user \" + id, e);");
        System.out.println("                                                                ^");
        System.out.println("      That second argument is the whole difference.");


        /* ====================================================================
         * SECTION 7 - THE ANTI-PATTERNS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - HOW TO GET THIS WRONG");
        System.out.println("=".repeat(74));

        System.out.printf("    %-44s %s%n", "ANTI-PATTERN", "WHY IT IS WRONG");
        System.out.printf("    %-44s %s%n", "catch (Exception e) { }", "the failure becomes invisible");
        System.out.printf("    %-44s %s%n", "catch (...) { e.printStackTrace(); }", "prints and continues regardless");
        System.out.printf("    %-44s %s%n", "catch (Exception e) as a catch-all", "also catches every BUG");
        System.out.printf("    %-44s %s%n", "exceptions for control flow", "slow, and hides intent");
        System.out.printf("    %-44s %s%n", "losing the cause on rethrow", "throws away the stack trace");
        System.out.printf("    %-44s %s%n", "throws Exception on every method", "tells the caller nothing");
        System.out.printf("    %-44s %s%n", "catch (Throwable t)", "now you caught OutOfMemoryError");

        System.out.println();
        System.out.println("  THE SWALLOWED EXCEPTION, demonstrated:");
        System.out.println("    swallowing version  -> " + parseSwallowing("not a number"));
        System.out.println("      Returned a plausible-looking 0. The caller has no idea");
        System.out.println("      anything went wrong, and will carry that 0 into a report.");
        System.out.println("    honest version      -> " + parseHonestly("not a number"));
        System.out.println("      Returned an explicit 'no value', which the caller must handle.");

        System.out.println();
        System.out.println("  IF YOU MUST IGNORE ONE, SAY SO AND SAY WHY:");
        System.out.println("      } catch (InterruptedException e) {");
        System.out.println("          Thread.currentThread().interrupt();  // restore the flag");
        System.out.println("      }");
        System.out.println();
        System.out.println("  InterruptedException deserves special mention: CATCHING IT");
        System.out.println("  CLEARS THE INTERRUPT FLAG. If you do not restore it, code above");
        System.out.println("  you can NEVER learn the thread was asked to stop.");
        System.out.println();
        demonstrateInterruptFlag();


        /* ====================================================================
         * SECTION 8 - CHECKED VERSUS UNCHECKED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - THE DEBATE, AND THE PRAGMATIC ANSWER");
        System.out.println("=".repeat(74));

        System.out.println("  Java is the ONLY mainstream language with checked exceptions,");
        System.out.println("  and opinion is genuinely divided.");
        System.out.println();
        System.out.println("  FOR: the compiler documents and enforces failure handling. You");
        System.out.println("       cannot forget that a file read might fail.");
        System.out.println();
        System.out.println("  AGAINST:");
        System.out.println("    - they leak through abstractions");
        System.out.println("    - they force throws clauses up entire call stacks");
        System.out.println("    - they DO NOT COMPOSE WITH LAMBDAS - a Function cannot throw");
        System.out.println("      a checked exception, which is why this does not compile:");
        System.out.println("        list.stream().map(f -> Files.readString(f))   // ERROR");
        System.out.println("    - in practice they cause people to write");
        System.out.println("      `catch (Exception e) { }` just to quiet the compiler");

        System.out.println();
        System.out.println("  The lambda problem, and the standard workaround:");
        List<String> paths = List.of("missing-1.txt", "missing-2.txt");
        System.out.println("    reading " + paths + " inside a stream:");
        List<String> results = paths.stream()
                .map(ExceptionHandlingBasics::readOrDescribe)   // wraps internally
                .toList();
        results.forEach(line -> System.out.println("      " + line));
        System.out.println("    The helper catches the checked exception and returns a value,");
        System.out.println("    because the lambda itself is not allowed to throw one.");

        System.out.println();
        System.out.println("  THE PRAGMATIC POSITION MOST MODERN JAVA TAKES:");
        System.out.println("    - UNCHECKED for programming errors, and for failures the");
        System.out.println("      caller cannot meaningfully recover from");
        System.out.println("    - CHECKED only when the caller can realistically act on it");
        System.out.println("    - WRAP low-level checked exceptions in domain-specific");
        System.out.println("      unchecked ones at your layer boundary, preserving the cause");
        System.out.println();
        System.out.println("  Spring, Hibernate and most modern frameworks converted their");
        System.out.println("  checked exceptions to unchecked for exactly these reasons.");


        /* ====================================================================
         * SECTION 9 - WHAT AN EXCEPTION COSTS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - THE COST IS IN CREATION, NOT THROWING");
        System.out.println("=".repeat(74));

        int iterations = 1_000_000;

        long startNormal = System.nanoTime();
        long normalTotal = 0;
        for (int i = 0; i < iterations; i++) {
            normalTotal += parseWithCheck("123");
        }
        long normalMillis = (System.nanoTime() - startNormal) / 1_000_000;

        long startExceptional = System.nanoTime();
        long exceptionalTotal = 0;
        for (int i = 0; i < iterations; i++) {
            exceptionalTotal += parseWithException("nope");
        }
        long exceptionalMillis = (System.nanoTime() - startExceptional) / 1_000_000;

        long startStackless = System.nanoTime();
        long stacklessTotal = 0;
        for (int i = 0; i < iterations; i++) {
            stacklessTotal += parseWithStacklessException("nope");
        }
        long stacklessMillis = (System.nanoTime() - startStackless) / 1_000_000;

        System.out.printf("    %,d operations:%n", iterations);
        System.out.println("      a plain check, no exception     -> " + normalMillis + " ms");
        System.out.println("      throwing a normal exception     -> " + exceptionalMillis + " ms");
        System.out.println("      throwing a STACKLESS exception  -> " + stacklessMillis + " ms");
        System.out.println("      (checksums " + normalTotal + ", " + exceptionalTotal
                + ", " + stacklessTotal + ")");

        System.out.println();
        System.out.println("  Compare lines 2 and 3. The ONLY difference between them is");
        System.out.println("  overriding fillInStackTrace() to return `this` - so the cost is");
        System.out.println("  almost entirely WALKING THE STACK to build the trace, not the");
        System.out.println("  throw itself.");
        System.out.println();
        System.out.println("  (Line 1 is not a clean baseline: it validates with a REGEX,");
        System.out.println("  which is itself slow. That is why it can lose to the stackless");
        System.out.println("  throw. The comparison that matters is line 2 against line 3.)");
        System.out.println();
        System.out.println("  THAT is why exceptions are wrong for control flow. A stackless");
        System.out.println("  exception is a specialised optimisation for a genuinely hot");
        System.out.println("  path - and you lose the stack trace, which is usually a bad");
        System.out.println("  trade. Use a plain check instead.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 39.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 1 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * @param type a throwable type
     * @return true if it is a checked exception
     */
    static boolean isChecked(Class<?> type) {
        return Exception.class.isAssignableFrom(type)
                && !RuntimeException.class.isAssignableFrom(type);
    }

    /**
     * Walks a throwable's superclass chain up to Throwable.
     *
     * @param type the class to walk from
     * @return the chain, joined with arrows
     */
    static String chainToThrowable(Class<?> type) {
        StringBuilder chain = new StringBuilder();
        for (Class<?> current = type; current != null && !current.equals(Object.class);
                current = current.getSuperclass()) {
            if (!chain.isEmpty()) {
                chain.append(" -> ");
            }
            chain.append(current.getSimpleName());
        }
        return chain.toString();
    }

    /** Triggers several unchecked exceptions, each of which is a code defect. */
    static void demonstrateUnchecked() {
        record Case(String description, Runnable action) {}

        List<Case> cases = List.of(
                new Case("String text = null; text.length()",
                        () -> { String text = null; text.length(); }),
                new Case("new int[3][5]",
                        () -> { int[] array = new int[3]; int ignored = array[5]; }),
                new Case("1 / 0",
                        () -> { int ignored = 1 / 0; }),
                new Case("Integer.parseInt(\"abc\")",
                        () -> Integer.parseInt("abc")),
                new Case("(String) (Object) 42",
                        () -> { Object value = 42; String ignored = (String) value; }),
                new Case("List.of().get(0)",
                        () -> List.of().get(0))
        );

        for (Case testCase : cases) {
            try {
                testCase.action().run();
                System.out.println("    " + pad(testCase.description()) + " -> no exception");
            } catch (RuntimeException e) {
                System.out.println("    " + pad(testCase.description()) + " -> "
                        + e.getClass().getSimpleName());
            }
        }
    }

    /**
     * Pads a label so the output lines up.
     *
     * @param text the label
     * @return the padded label
     */
    static String pad(String text) {
        return String.format("%-34s", text);
    }

    // ------------------------------------------------------------------------
    // SECTION 2 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Catches from most specific to broadest. Reversing the order would be a
     * compile error, because the later block would be unreachable.
     *
     * @param path the file to read; may be null
     */
    static void readWithOrderedCatches(String path) {
        try {
            Files.readString(Path.of(path));
            System.out.println("      read successfully");
        } catch (NoSuchFileException e) {
            System.out.println("      NoSuchFileException  - the most specific catch matched");
        } catch (IOException e) {
            System.out.println("      IOException          - a broader catch matched");
        } catch (NullPointerException e) {
            System.out.println("      NullPointerException - an unchecked one, caught separately");
        }
    }

    /**
     * Multi-catch, where two unrelated failures need identical handling.
     *
     * @param scenario which failure to provoke
     */
    static void multiCatchExample(int scenario) {
        try {
            switch (scenario) {
                case 0 -> throw new IOException("disk unavailable");
                case 1 -> throw new SQLException("connection refused");
                default -> System.out.println("    scenario 2 -> succeeded, no exception");
            }
        } catch (IOException | SQLException e) {
            // `e` is implicitly final, and its static type is Exception - the
            // nearest common supertype of IOException and SQLException.
            System.out.println("    scenario " + scenario + " -> multi-catch handled "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** @return a value, having run finally on the way */
    static String finallyOnNormalPath() {
        StringBuilder log = new StringBuilder();
        try {
            log.append("try ran, ");
        } finally {
            log.append("finally ran");
        }
        return log.toString();
    }

    /** @return a value returned from inside try, with finally still running */
    static String finallyOnReturnPath() {
        try {
            return "returned from try (and finally still ran before this was delivered)";
        } finally {
            // This executes BEFORE the method actually returns.
            int ignored = 0;
        }
    }

    /** Throws, and finally still runs on the way out. */
    static void finallyOnExceptionPath() {
        try {
            throw new IllegalStateException("something failed");
        } finally {
            // Runs, then the exception continues propagating.
            int ignored = 0;
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 3 SUPPORT - THE finally TRAPS
    // ------------------------------------------------------------------------

    /**
     * TRAP 1. The return in finally discards the in-flight exception entirely.
     * Never write this.
     *
     * @return a misleading success message
     */
    @SuppressWarnings("finally")
    static String returnInFinallySwallowsException() {
        try {
            throw new RuntimeException("real problem");
        } finally {
            // THE BUG: this return discards the exception above. Silently.
            return "everything is fine";
        }
    }

    /**
     * TRAP 2. The return value is captured before finally runs, so mutating
     * the variable afterwards changes nothing.
     *
     * @return 1, despite the assignment in finally
     */
    static int finallyCannotChangeReturnValue() {
        int value = 1;
        try {
            return value;          // the VALUE 1 is captured right here
        } finally {
            value = 99;            // too late - the return value is already fixed
        }
    }

    /**
     * TRAP 3. An exception thrown in finally replaces the original, which is
     * lost with no record at all.
     */
    static void exceptionInFinallyMasksOriginal() {
        try {
            throw new IllegalStateException("the real cause");
        } finally {
            // THE BUG: this replaces the exception above. The real cause is
            // gone - not chained, not suppressed, just gone.
            throw new RuntimeException("from cleanup");
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 5 SUPPORT - STACK TRACES
    // ------------------------------------------------------------------------

    /** Entry point of a three-deep call chain that fails at the bottom. */
    static void topLevelOperation() {
        middleLayer();
    }

    /** Wraps the low-level failure in a domain exception, preserving the cause. */
    static void middleLayer() {
        try {
            lowLevelOperation();
        } catch (SQLException e) {
            throw new IllegalStateException("could not complete the operation", e);
        }
    }

    /**
     * The original failure.
     *
     * @throws SQLException always
     */
    static void lowLevelOperation() throws SQLException {
        throw new SQLException("connection refused");
    }

    /**
     * Prints a stack trace the way a log file would show it, trimmed so the
     * lesson output stays readable.
     *
     * @param throwable the exception to render
     */
    static void printTrace(Throwable throwable) {
        System.out.println("    " + throwable.getClass().getName() + ": " + throwable.getMessage());
        StackTraceElement[] frames = throwable.getStackTrace();
        for (int i = 0; i < Math.min(3, frames.length); i++) {
            System.out.println("        at " + frames[i]
                    + (i == 0 ? "     <- where it was THROWN" : ""));
        }
        System.out.println("        ... " + Math.max(0, frames.length - 3) + " more");

        Throwable cause = throwable.getCause();
        if (cause != null) {
            System.out.println("    Caused by: " + cause.getClass().getName()
                    + ": " + cause.getMessage() + "     <- THE ORIGINAL");
            StackTraceElement[] causeFrames = cause.getStackTrace();
            if (causeFrames.length > 0) {
                System.out.println("        at " + causeFrames[0]);
            }
            System.out.println("        ... " + Math.max(0, causeFrames.length - 1) + " more");
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 6 SUPPORT - CHAINING
    // ------------------------------------------------------------------------

    /** Rethrows without the cause. The original stack trace is destroyed.
     *  @param id the user id */
    static void loadUserLosingCause(int id) {
        try {
            throw new SQLException("ORA-00942: table or view does not exist");
        } catch (SQLException e) {
            // THE MISTAKE: no second argument.
            throw new IllegalStateException("failed to load user " + id);
        }
    }

    /** Rethrows WITH the cause. One extra argument, and the trail survives.
     *  @param id the user id */
    static void loadUserPreservingCause(int id) {
        try {
            throw new SQLException("ORA-00942: table or view does not exist");
        } catch (SQLException e) {
            throw new IllegalStateException("failed to load user " + id, e);
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 7 SUPPORT - SWALLOWING
    // ------------------------------------------------------------------------

    /**
     * Swallows the failure and returns a plausible-looking zero. The caller
     * cannot distinguish "the input was 0" from "the input was garbage".
     *
     * @param input the text to parse
     * @return the parsed value, or a misleading 0
     */
    static int parseSwallowing(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            // THE ANTI-PATTERN: the failure is now invisible.
            return 0;
        }
    }

    /**
     * Returns an explicit "no value" the caller is forced to handle.
     *
     * @param input the text to parse
     * @return the parsed value, or empty
     */
    static java.util.Optional<Integer> parseHonestly(String input) {
        try {
            return java.util.Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return java.util.Optional.empty();
        }
    }

    /** Shows that catching InterruptedException clears the interrupt flag. */
    static void demonstrateInterruptFlag() {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                // Catching it CLEARED the flag. Prove it, then restore it.
                System.out.println("      caught InterruptedException");
                System.out.println("      interrupt flag immediately after catching: "
                        + Thread.currentThread().isInterrupted() + "   <- CLEARED");
                Thread.currentThread().interrupt();
                System.out.println("      after Thread.currentThread().interrupt(): "
                        + Thread.currentThread().isInterrupted() + "    restored");
            }
        });

        worker.start();
        worker.interrupt();
        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 8 SUPPORT - THE LAMBDA PROBLEM
    // ------------------------------------------------------------------------

    /**
     * Wraps a checked exception so this method can be used as a lambda. The
     * lambda itself could not declare `throws IOException`.
     *
     * @param path the file to read
     * @return the content, or a description of the failure
     */
    static String readOrDescribe(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return path + " -> could not read (" + e.getClass().getSimpleName() + ")";
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 9 SUPPORT - COST
    // ------------------------------------------------------------------------

    /**
     * The cheap way: check first, never construct an exception.
     *
     * @param input the text to parse
     * @return the value, or -1
     */
    static int parseWithCheck(String input) {
        if (input == null || !input.matches("-?\\d+")) {
            return -1;
        }
        return Integer.parseInt(input);
    }

    /**
     * The expensive way: let a normal exception be constructed and thrown.
     *
     * @param input the text to parse
     * @return the value, or -1
     */
    static int parseWithException(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * The same throw, but with a stackless exception - isolating how much of
     * the cost is building the stack trace.
     *
     * @param input the text to parse
     * @return the value, or -1
     */
    static int parseWithStacklessException(String input) {
        try {
            if (!input.matches("-?\\d+")) {
                throw StacklessParseException.INSTANCE;
            }
            return Integer.parseInt(input);
        } catch (StacklessParseException e) {
            return -1;
        }
    }
}

/**
 * An exception that does not capture a stack trace. Overriding
 * fillInStackTrace to return {@code this} removes almost the entire cost of
 * throwing - and removes the stack trace, which is usually a bad trade.
 *
 * <p>A specialised optimisation for genuinely hot paths, not a default.
 */
final class StacklessParseException extends RuntimeException {

    /** A single reusable instance - there is no per-throw state to keep. */
    static final StacklessParseException INSTANCE = new StacklessParseException();

    private StacklessParseException() {
        // Also disable suppression and writable stack traces.
        super("not a number", null, false, false);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;      // the entire optimisation, in one line
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Reverse the catch order in readWithOrderedCatches so IOException comes
 *    first. Read the exact compiler error, and explain it using the word
 *    "unreachable".
 *
 * 2. Predict the output of each, then run them:
 *        static int a() { try { return 1; } finally { return 2; } }
 *        static int b() { int x = 1; try { return x; } finally { x = 2; } }
 *        static int c() { try { throw new RuntimeException(); } finally { return 3; } }
 *
 * 3. Write a method that reads a file and wraps IOException in a custom
 *    unchecked exception. Do it once WITHOUT the cause and once WITH, then
 *    print both stack traces and count the lines of useful information.
 *
 * 4. Take demonstrateInterruptFlag and delete the restoring interrupt() call.
 *    Then write a loop in the worker that checks isInterrupted() and confirm
 *    it never stops.
 *
 * 5. Try to write `list.stream().map(p -> Files.readString(p))`. Read the
 *    error. Then solve it three ways: a helper method, a try inside the
 *    lambda, and a custom functional interface that declares throws.
 *
 * 6. Run the Section 9 benchmark with 10,000,000 iterations. Then remove the
 *    fillInStackTrace override from StacklessParseException and run again.
 *    That difference is the entire cost of a stack trace.
 * ============================================================================
 */
