/* ============================================================================
 * 41 - try-with-resources AND BEST PRACTICES
 * ----------------------------------------------------------------------------
 * Companion lesson: 41-try-with-resources-and-best-practices.md
 *
 * RUN IT:
 *     java Java/08-exception-handling/41-try-with-resources-and-best-practices.java
 *
 * Anything you open, you must close. Doing that correctly BY HAND is
 * startlingly hard - which is why Java 7 added syntax for it.
 *
 * Section 3 is the feature that makes try-with-resources genuinely BETTER
 * rather than merely shorter: suppressed exceptions.
 * ============================================================================
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class TryWithResourcesAndBestPractices {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE PROBLEM WITH finally
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE PRE-JAVA-7 IDIOM, WRITTEN CORRECTLY");
        System.out.println("=".repeat(74));

        System.out.println("      BufferedReader reader = null;");
        System.out.println("      try {");
        System.out.println("          reader = new BufferedReader(new FileReader(path));");
        System.out.println("          return reader.readLine();");
        System.out.println("      } finally {");
        System.out.println("          if (reader != null) {          // the constructor may have thrown");
        System.out.println("              try {");
        System.out.println("                  reader.close();        // close() itself throws");
        System.out.println("              } catch (IOException ignored) {");
        System.out.println("                  // and letting this out would MASK the real exception");
        System.out.println("              }");
        System.out.println("          }");
        System.out.println("      }");
        System.out.println();
        System.out.println("  Eight lines of ceremony for one line of work, with THREE");
        System.out.println("  separate ways to get it wrong:");
        System.out.println("    1. forgetting the null check - the resource may never have");
        System.out.println("       been assigned, because the constructor threw");
        System.out.println("    2. letting close()'s exception propagate - it MASKS the");
        System.out.println("       original (lesson 39, trap 3)");
        System.out.println("    3. two resources means NESTED try/finally, and it compounds");

        System.out.println();
        System.out.println("  Watching the manual version lose an exception:");
        try {
            manualCleanupLosesException();
        } catch (Exception e) {
            System.out.println("    caught -> " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            System.out.println("    suppressed -> " + e.getSuppressed().length
                    + "   <- the close() failure was DISCARDED");
        }


        /* ====================================================================
         * SECTION 2 - try-with-resources
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE WHOLE THING, IN ONE LINE");
        System.out.println("=".repeat(74));

        System.out.println("      try (var reader = new BufferedReader(new FileReader(path))) {");
        System.out.println("          return reader.readLine();");
        System.out.println("      }");
        System.out.println();
        System.out.println("  The compiler generates the null check, the finally, the nested");
        System.out.println("  try, and the suppression handling.");

        System.out.println();
        System.out.println("  A single resource, closing on the normal path:");
        try (ManagedResource resource = new ManagedResource("database")) {
            resource.use();
        }

        System.out.println();
        System.out.println("  ...and closing when the body THROWS:");
        try (ManagedResource resource = new ManagedResource("file-handle")) {
            resource.use();
            throw new IllegalStateException("something went wrong mid-operation");
        } catch (IllegalStateException e) {
            System.out.println("    caught after close: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  MULTIPLE RESOURCES close in REVERSE ORDER of declaration -");
        System.out.println("  which is exactly what you want when one wraps another:");
        try (ManagedResource outer = new ManagedResource("outer-stream");
             ManagedResource inner = new ManagedResource("inner-buffer")) {
            inner.use();
        }
        System.out.println("    Declared outer then inner; closed inner then outer.");

        System.out.println();
        System.out.println("  close() RUNS BEFORE your catch or finally block:");
        try (ManagedResource resource = new ManagedResource("ordering-demo")) {
            throw new IllegalStateException("thrown from the body");
        } catch (IllegalStateException e) {
            System.out.println("    ...THEN the catch block runs");
        } finally {
            System.out.println("    ...THEN the finally block runs");
        }

        System.out.println();
        System.out.println("  EFFECTIVELY-FINAL RESOURCES (Java 9+) - use one created earlier:");
        ManagedResource existing = new ManagedResource("created-elsewhere");
        try (existing) {
            existing.use();
        }
        System.out.println("    Before Java 9 you needed a redundant `try (var r = existing)`.");

        System.out.println();
        System.out.println("  The resource variable is implicitly FINAL:");
        System.out.println("      try (var r = open()) { r = open(); }");
        System.out.println("      -> error: auto-closeable resource r may not be assigned");


        /* ====================================================================
         * SECTION 3 - SUPPRESSED EXCEPTIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE FEATURE THAT MAKES IT GENUINELY BETTER");
        System.out.println("=".repeat(74));

        System.out.println("  When the BODY throws AND close() throws, the body's exception");
        System.out.println("  wins and close()'s is attached as SUPPRESSED - not discarded:");
        System.out.println();

        try (FailingResource resource = new FailingResource("audit-log")) {
            throw new IllegalStateException("the real problem");
        } catch (Exception e) {
            System.out.println("    primary   -> " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("    SUPPRESSED-> " + suppressed.getClass().getSimpleName()
                        + ": " + suppressed.getMessage());
            }
            System.out.println();
            System.out.println("    BOTH exceptions survived. The hand-written finally version");
            System.out.println("    in Section 1 lost one of them entirely.");
        }

        System.out.println();
        System.out.println("  With MULTIPLE failing resources, every close() failure is kept:");
        try (FailingResource first = new FailingResource("first");
             FailingResource second = new FailingResource("second")) {
            throw new IllegalStateException("the body failed");
        } catch (Exception e) {
            System.out.println("    primary   -> " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("    SUPPRESSED-> " + suppressed.getMessage());
            }
        }

        System.out.println();
        System.out.println("  If ONLY close() fails, it becomes the primary exception:");
        try (FailingResource resource = new FailingResource("only-close-fails")) {
            // body succeeds
        } catch (Exception e) {
            System.out.println("    primary   -> " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            System.out.println("    suppressed-> " + e.getSuppressed().length);
        }

        System.out.println();
        System.out.println("  Stack traces print suppressed exceptions automatically, marked");
        System.out.println("  `Suppressed:`. You do not have to do anything to see them.");


        /* ====================================================================
         * SECTION 4 - WRITING YOUR OWN RESOURCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - IMPLEMENTING AutoCloseable");
        System.out.println("=".repeat(74));

        System.out.println("  AutoCloseable.close() declares `throws Exception`, but you");
        System.out.println("  should NARROW IT - a close() that throws Exception forces every");
        System.out.println("  caller to catch Exception:");
        System.out.println();
        System.out.println("      class Connection implements AutoCloseable {");
        System.out.println("          @Override public void close() { ... }   // no throws at all");
        System.out.println("      }");
        System.out.println();
        System.out.println("  ManagedResource in this file does exactly that, which is why");
        System.out.println("  Section 2 needed no try/catch around it.");

        System.out.println();
        System.out.println("  THREE RULES FOR close():");
        System.out.println();
        System.out.println("    1. MAKE IT IDEMPOTENT - calling it twice must be safe:");
        ManagedResource idempotent = new ManagedResource("idempotency-test");
        idempotent.close();
        idempotent.close();
        System.out.println("       (the second close printed nothing - it returned early)");

        System.out.println();
        System.out.println("    2. NEVER THROW InterruptedException from close(). It would be");
        System.out.println("       SUPPRESSED, silently losing an interrupt request - and");
        System.out.println("       nothing above you would ever learn the thread was asked");
        System.out.println("       to stop.");
        System.out.println();
        System.out.println("    3. PREFER NOT THROWING AT ALL if the cleanup cannot");
        System.out.println("       meaningfully fail.");

        System.out.println();
        System.out.println("  Closeable (java.io) narrows close() to `throws IOException` and");
        System.out.println("  requires idempotency. Prefer it for I/O types:");
        System.out.println("    Closeable extends AutoCloseable -> "
                + AutoCloseable.class.isAssignableFrom(java.io.Closeable.class));


        /* ====================================================================
         * SECTION 5 - THE PRACTICES, GATHERED
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE PRACTICES");
        System.out.println("=".repeat(74));

        System.out.println("  CATCH NARROWLY:");
        System.out.println("      catch (IOException e)   yes");
        System.out.println("      catch (Exception e)     catches every BUG too");
        System.out.println("      catch (Throwable t)     now you caught OutOfMemoryError");

        System.out.println();
        System.out.println("  NEVER SWALLOW. Compare what a caller learns:");
        System.out.println("    swallowing  -> " + readSwallowing("missing.txt"));
        System.out.println("    honest      -> " + readHonestly("missing.txt"));
        System.out.println();
        System.out.println("    printStackTrace() IS NOT LOGGING: it bypasses your log");
        System.out.println("    configuration, carries no severity or timestamp, and on a");
        System.out.println("    server is frequently discarded entirely.");

        System.out.println();
        System.out.println("  RESTORE THE INTERRUPT FLAG:");
        demonstrateInterruptHandling();

        System.out.println();
        System.out.println("  DO NOT USE EXCEPTIONS FOR CONTROL FLOW:");
        Map<String, String> settings = Map.of("host", " localhost ");
        System.out.println("    via exception -> \"" + lookupViaException(settings, "missing") + "\"");
        System.out.println("    via a check   -> \"" + lookupViaCheck(settings, "missing") + "\"");
        System.out.println("    Same answer. The first constructs and throws a");
        System.out.println("    NullPointerException to express 'the key was absent'.");

        System.out.println();
        System.out.println("  LOG OR RETHROW, NEVER BOTH:");
        System.out.println("      catch (IOException e) {");
        System.out.println("          log.error(\"failed\", e);      // logged here...");
        System.out.println("          throw new DataException(e);  // ...and again upstairs");
        System.out.println("      }");
        System.out.println("    The same failure appears twice in the log, from two places,");
        System.out.println("    which makes an incident HARDER to read, not easier.");
        System.out.println("    Rethrow and let the top-level handler log it once.");

        System.out.println();
        System.out.println("  FAIL FAST at the boundary, so the exception names the real");
        System.out.println("  cause rather than surfacing three layers later as an NPE:");
        try {
            new Report(null);
        } catch (NullPointerException e) {
            System.out.println("    new Report(null) -> " + e.getMessage());
            System.out.println("    ...rather than a mysterious NPE inside render() later.");
        }


        /* ====================================================================
         * SECTION 6 - EXCEPTIONS AND LAMBDAS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - WHERE CHECKED EXCEPTIONS AND LAMBDAS COLLIDE");
        System.out.println("=".repeat(74));

        List<String> paths = List.of("missing-a.txt", "missing-b.txt");

        System.out.println("      paths.stream().map(p -> Files.readString(Path.of(p)))");
        System.out.println("      -> error: unhandled exception type IOException");
        System.out.println();
        System.out.println("    A lambda cannot throw a checked exception unless the");
        System.out.println("    functional interface declares it - and Function does not.");

        System.out.println();
        System.out.println("  WORKAROUND 1 - a helper method (usually clearest):");
        paths.stream()
                .map(TryWithResourcesAndBestPractices::readOrDescribe)
                .forEach(line -> System.out.println("    " + line));

        System.out.println();
        System.out.println("  WORKAROUND 2 - a try INSIDE the lambda (verbose but explicit):");
        paths.stream()
                .map(path -> {
                    try {
                        return Files.readString(Path.of(path));
                    } catch (IOException e) {
                        return path + " -> " + e.getClass().getSimpleName();
                    }
                })
                .forEach(line -> System.out.println("    " + line));

        System.out.println();
        System.out.println("  WORKAROUND 3 - a functional interface that DECLARES throws,");
        System.out.println("  plus an adapter that wraps into an unchecked exception:");
        try {
            paths.stream()
                    .map(unchecked(path -> Files.readString(Path.of(path))))
                    .forEach(line -> System.out.println("    " + line));
        } catch (RuntimeException e) {
            System.out.println("    threw " + e.getClass().getSimpleName()
                    + ", cause " + e.getCause().getClass().getSimpleName());
            System.out.println("    The checked exception became unchecked, cause preserved.");
        }

        System.out.println();
        System.out.println("  This friction is one of the strongest practical arguments for");
        System.out.println("  UNCHECKED exceptions in modern Java (lessons 39 and 40).");


        /* ====================================================================
         * SECTION 7 - THE CHECKLIST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE CHECKLIST");
        System.out.println("=".repeat(74));

        System.out.println("    1. try-with-resources for ANYTHING you open. Never a manual");
        System.out.println("       finally - it loses an exception and you will forget the");
        System.out.println("       null check");
        System.out.println("    2. Narrow close() to a specific exception, or none at all");
        System.out.println("    3. Make close() idempotent");
        System.out.println("    4. Catch the narrowest type that fits");
        System.out.println("    5. Never swallow; printStackTrace() is not logging");
        System.out.println("    6. Restore the interrupt flag");
        System.out.println("    7. Log OR rethrow, never both");
        System.out.println("    8. Fail fast at the boundary");
        System.out.println("    9. Never use exceptions for control flow");
        System.out.println("   10. Always pass the cause when wrapping (lesson 40)");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 41. That completes section 08.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SECTION 1 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * The hand-written idiom, showing that one of the two exceptions is lost
     * no matter which way you write the inner catch.
     *
     * @throws Exception the body's exception, with close()'s discarded
     */
    static void manualCleanupLosesException() throws Exception {
        FailingResource resource = null;
        try {
            resource = new FailingResource("manual");
            throw new IllegalStateException("the real problem");
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                    // Swallowing here loses the close() failure. Letting it out
                    // would lose the REAL exception instead. There is no way to
                    // keep both without writing the suppression logic by hand.
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 5 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Swallows the failure and returns something plausible, so the caller
     * cannot tell an empty file from a missing one.
     *
     * @param path the file to read
     * @return the content, or a misleading empty string
     */
    static String readSwallowing(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return "\"\"  <- an empty string. Was the file empty, or missing? Unknowable.";
        }
    }

    /**
     * Returns an explicit "no value" the caller must handle.
     *
     * @param path the file to read
     * @return the content, or empty
     */
    static String readHonestly(String path) {
        try {
            return "Optional[" + Files.readString(Path.of(path)) + "]";
        } catch (IOException e) {
            return Optional.empty() + "  <- unambiguous: there is no content";
        }
    }

    /** Shows the interrupt flag being cleared by the catch, then restored. */
    static void demonstrateInterruptHandling() {
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                System.out.println("      flag immediately after catching: "
                        + Thread.currentThread().isInterrupted() + "   <- CLEARED by the catch");
                Thread.currentThread().interrupt();
                System.out.println("      after interrupt() restores it:  "
                        + Thread.currentThread().isInterrupted() + "    code above can now see it");
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

    /**
     * Uses an exception to express "the key was absent" - slow, and it hides
     * the intent.
     *
     * @param settings the map
     * @param key      the key
     * @return the trimmed value, or ""
     */
    static String lookupViaException(Map<String, String> settings, String key) {
        try {
            return settings.get(key).trim();
        } catch (NullPointerException e) {
            return "";
        }
    }

    /**
     * The same result with a plain check.
     *
     * @param settings the map
     * @param key      the key
     * @return the trimmed value, or ""
     */
    static String lookupViaCheck(Map<String, String> settings, String key) {
        String value = settings.get(key);
        return value == null ? "" : value.trim();
    }

    // ------------------------------------------------------------------------
    // SECTION 6 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Workaround 1: a helper that catches the checked exception and returns a
     * value, so it can be used as a method reference.
     *
     * @param path the file to read
     * @return the content, or a description of the failure
     */
    static String readOrDescribe(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            return path + " -> " + e.getClass().getSimpleName() + " (handled in a helper)";
        }
    }

    /**
     * Workaround 3: adapts a throwing function into an ordinary one by wrapping
     * any checked exception in an unchecked one, preserving the cause.
     *
     * @param <T>      the input type
     * @param <R>      the result type
     * @param function the throwing function
     * @return a plain Function usable in a stream
     */
    static <T, R> java.util.function.Function<T, R> unchecked(ThrowingFunction<T, R> function) {
        return input -> {
            try {
                return function.apply(input);
            } catch (Exception e) {
                // Wrap, ALWAYS passing the cause (lesson 40).
                throw new RuntimeException("wrapped checked exception", e);
            }
        };
    }
}

/**
 * A functional interface that DECLARES a checked exception, which
 * java.util.function.Function cannot.
 *
 * @param <T> the input type
 * @param <R> the result type
 */
@FunctionalInterface
interface ThrowingFunction<T, R> {

    /**
     * @param input the input
     * @return the result
     * @throws Exception if the operation fails
     */
    R apply(T input) throws Exception;
}

// ----------------------------------------------------------------------------
// SECTION 2 AND 4 - A WELL-BEHAVED RESOURCE
// ----------------------------------------------------------------------------

/**
 * A resource whose close() is NARROWED to throw nothing, and which is
 * IDEMPOTENT - the two most important properties of a close() method.
 */
class ManagedResource implements AutoCloseable {

    private final String name;
    private boolean closed;

    /** @param name what this resource represents */
    ManagedResource(String name) {
        this.name = name;
        System.out.println("    opened  " + name);
    }

    /** Does whatever the resource is for. */
    void use() {
        if (closed) {
            throw new IllegalStateException(name + " is already closed");
        }
        System.out.println("    using   " + name);
    }

    /**
     * Note the absence of `throws` - AutoCloseable permits `throws Exception`,
     * and narrowing it means callers need no try/catch at all.
     *
     * <p>Also IDEMPOTENT: calling it twice is safe and silent.
     */
    @Override
    public void close() {
        if (closed) {
            return;                    // idempotent - the second call does nothing
        }
        closed = true;
        System.out.println("    closed  " + name);
    }
}

/**
 * A resource whose close() always fails, so suppressed exceptions become
 * visible.
 */
class FailingResource implements AutoCloseable {

    private final String name;

    /** @param name what this resource represents */
    FailingResource(String name) {
        this.name = name;
    }

    /**
     * @throws IOException always, to demonstrate suppression
     */
    @Override
    public void close() throws IOException {
        throw new IOException("could not flush " + name);
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - FAIL FAST
// ----------------------------------------------------------------------------

/** Validates at construction, so failures name the real cause immediately. */
class Report {

    private final String title;

    /**
     * @param title the report title; must not be null
     * @throws NullPointerException immediately, rather than later inside render()
     */
    Report(String title) {
        this.title = java.util.Objects.requireNonNull(title,
                "title must not be null (checked at construction, not at render time)");
    }

    /** @return the rendered report */
    String render() {
        return "== " + title.toUpperCase() + " ==";
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Rewrite manualCleanupLosesException so it keeps BOTH exceptions, using
 *    addSuppressed() by hand. Then compare your version with the one-line
 *    try-with-resources equivalent.
 *
 * 2. Add a third resource to the multi-resource example and confirm the close
 *    order is exactly reversed. Then make the MIDDLE one fail and check which
 *    exceptions survive.
 *
 * 3. Write a Transaction class implementing AutoCloseable that rolls back if
 *    close() is reached without commit() having been called. This is a real
 *    and useful pattern.
 *
 * 4. Remove the idempotency guard from ManagedResource.close() and call it
 *    twice. Then find a case where try-with-resources itself would call it
 *    twice.
 *
 * 5. Take the Section 6 `unchecked` adapter and add a variant that wraps into
 *    a specific domain exception rather than RuntimeException. Which reads
 *    better at the call site?
 *
 * 6. Write a method that logs AND rethrows. Run it inside a program with a
 *    top-level handler that also logs. Count how many times one failure
 *    appears in the output.
 * ============================================================================
 */
