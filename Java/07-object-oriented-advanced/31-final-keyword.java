/* ============================================================================
 * 31 - THE final KEYWORD
 * ----------------------------------------------------------------------------
 * Companion lesson: 31-final-keyword.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/31-final-keyword.java
 *
 * `final` means "cannot be changed after initialisation" - but WHAT cannot be
 * changed depends entirely on where you write it. It does three different jobs.
 *
 * THE ONE THING THAT MATTERS MOST is in Section 2: final locks the REFERENCE,
 * not the OBJECT.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class FinalKeyword {

    /** A compile-time constant. javac INLINES this into every calling class. */
    static final int MAX_RETRIES = 3;

    /** NOT a compile-time constant - the value comes from a method call. */
    static final int COMPUTED_LIMIT = computeLimit();

    /** A field used to show that lambdas may capture fields freely. */
    private int instanceCounter = 0;

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE THREE USES
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THREE DIFFERENT JOBS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-22s %s%n", "final VARIABLE", "cannot be REASSIGNED");
        System.out.printf("    %-22s %s%n", "final METHOD", "cannot be OVERRIDDEN");
        System.out.printf("    %-22s %s%n", "final CLASS", "cannot be EXTENDED");

        final int limit = 100;
        System.out.println();
        System.out.println("    final int limit = 100;");
        System.out.println("    limit = 200;   -> ERROR: cannot assign a value to final variable limit");
        System.out.println("    limit is still " + limit);


        /* ====================================================================
         * SECTION 2 - THE ONE THAT MATTERS: REFERENCE, NOT OBJECT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - final LOCKS THE REFERENCE, NOT THE OBJECT");
        System.out.println("=".repeat(74));

        final List<String> names = new ArrayList<>();

        System.out.println("    final List<String> names = new ArrayList<>();");
        System.out.println("    names -> " + names);

        names.add("Danish");
        names.add("Aisha");
        System.out.println("    names.add(\"Danish\"); names.add(\"Aisha\");  -> " + names
                + "   ALLOWED");

        names.clear();
        System.out.println("    names.clear();                            -> " + names
                + "        ALLOWED");

        names.add("started over");
        System.out.println("    names.add(\"started over\");                -> " + names);

        // names = new ArrayList<>();
        //   ERROR: cannot assign a value to final variable names
        System.out.println();
        System.out.println("    names = new ArrayList<>();  -> COMPILE ERROR");
        System.out.println();
        System.out.println("  Everything about the CONTENTS was allowed. Only REPOINTING the");
        System.out.println("  variable was forbidden. That is the whole rule.");

        /* --------------------------------------------------------------------
         * THE CONSEQUENCE: the "immutable constant" that is not.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE CONSEQUENCE - a public constant that is a mutable global:");
        System.out.println("    Settings.MUTABLE_ALLOWED  -> " + Settings.MUTABLE_ALLOWED);
        Settings.MUTABLE_ALLOWED.add("injected by anyone, from anywhere");
        System.out.println("    after .add(...)           -> " + Settings.MUTABLE_ALLOWED);
        System.out.println();
        System.out.println("    public static final List<String> ALLOWED = new ArrayList<>();");
        System.out.println("    ...is a public MUTABLE GLOBAL wearing `final`.");

        System.out.println();
        System.out.println("  A genuinely immutable constant:");
        System.out.println("    Settings.REALLY_IMMUTABLE -> " + Settings.REALLY_IMMUTABLE);
        try {
            Settings.REALLY_IMMUTABLE.add("nope");
        } catch (UnsupportedOperationException e) {
            System.out.println("    .add(...) -> UnsupportedOperationException");
        }
        System.out.println("    List.of(...) has nothing to mutate. That is the fix.");

        System.out.println();
        System.out.println("  For PRIMITIVES and String, final DOES give a real constant -");
        System.out.println("  because there is nothing to mutate in the first place:");
        System.out.println("    MAX_RETRIES  = " + MAX_RETRIES + "   (an int: genuinely constant)");
        System.out.println("    a final String is constant too, because String is immutable");


        /* ====================================================================
         * SECTION 3 - final FIELDS AND DEFINITE ASSIGNMENT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - ASSIGNED EXACTLY ONCE, ON EVERY PATH");
        System.out.println("=".repeat(74));

        System.out.println("    new ImmutablePoint(3, 4)     -> " + new ImmutablePoint(3, 4));
        System.out.println("    new ImmutablePoint(5)        -> " + new ImmutablePoint(5));
        System.out.println("    ImmutablePoint.origin()      -> " + ImmutablePoint.origin());

        System.out.println();
        System.out.println("  The compiler enforces DEFINITE ASSIGNMENT:");
        System.out.println("    - assign a final field twice        -> 'might already have been assigned'");
        System.out.println("    - leave a path where it is not set  -> 'might not have been initialized'");
        System.out.println();
        System.out.println("  A final field with NO initialiser is a BLANK FINAL, and it is");
        System.out.println("  how you build an immutable object whose values come from the");
        System.out.println("  constructor. That is the foundation of lesson 38.");

        System.out.println();
        System.out.println("  final PARAMETERS prevent reassignment inside the method:");
        System.out.println("    void process(final String input) { input = \"x\"; }  -> ERROR");
        System.out.println("  Purely stylistic. It has NO effect on the caller, because Java");
        System.out.println("  is pass-by-value (lesson 17).");


        /* ====================================================================
         * SECTION 4 - COMPILE-TIME CONSTANT INLINING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE INLINING HAZARD");
        System.out.println("=".repeat(74));

        System.out.println("    static final int MAX_RETRIES  = 3;              <- COMPILE-TIME constant");
        System.out.println("    static final int COMPUTED_LIMIT = computeLimit(); <- not");
        System.out.println();
        System.out.println("    MAX_RETRIES    = " + MAX_RETRIES);
        System.out.println("    COMPUTED_LIMIT = " + COMPUTED_LIMIT);

        System.out.println();
        System.out.println("  A static final initialised with a COMPILE-TIME CONSTANT");
        System.out.println("  EXPRESSION is INLINED BY javac - the literal value is copied");
        System.out.println("  into every calling class's bytecode. The constant's own class");
        System.out.println("  is never even loaded at runtime (lesson 25 proved this).");
        System.out.println();
        System.out.println("  THE DEPLOYMENT HAZARD:");
        System.out.println("    Change  public static final int MAX = 3;  to 4,");
        System.out.println("    recompile ONLY that class, and every class compiled against");
        System.out.println("    the old value STILL USES 3 until it is recompiled too.");
        System.out.println();
        System.out.println("    This is a genuine problem with library constants, and it is");
        System.out.println("    invisible - no error, no warning, just a stale value. It does");
        System.out.println("    NOT apply to non-constant expressions like computeLimit().");


        /* ====================================================================
         * SECTION 5 - EFFECTIVELY FINAL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - EFFECTIVELY FINAL (Java 8+)");
        System.out.println("=".repeat(74));

        // Never reassigned, so it is EFFECTIVELY final even without the keyword.
        int captured = 42;
        Runnable printsCaptured = () -> System.out.println("    lambda sees captured = " + captured);
        printsCaptured.run();
        System.out.println("    `captured` has no `final` keyword, but is never reassigned,");
        System.out.println("    so it is EFFECTIVELY FINAL and the lambda may capture it.");

        System.out.println();
        System.out.println("  Reassign it anywhere in the method and the lambda stops compiling:");
        System.out.println("      int total = 0;");
        System.out.println("      Runnable r = () -> print(total);");
        System.out.println("      total = 5;      // <- this line breaks the LAMBDA above");
        System.out.println("      -> error: local variables referenced from a lambda");
        System.out.println("         expression must be final or effectively final");

        System.out.println();
        System.out.println("  WHY THE RESTRICTION EXISTS:");
        System.out.println("    A lambda captures the VALUE of a local, not the variable,");
        System.out.println("    because the local lives on the STACK and the lambda may");
        System.out.println("    outlive the method. Allowing reassignment would mean two");
        System.out.println("    copies silently diverging.");

        System.out.println();
        System.out.println("  FIELDS have NO such restriction - they live on the HEAP, so the");
        System.out.println("  lambda captures a reference to the object and always sees the");
        System.out.println("  current value:");
        new FinalKeyword().demonstrateFieldCapture();

        System.out.println();
        System.out.println("  THE WORKAROUNDS when you genuinely need mutable capture:");

        // 1. A single-element array: the ARRAY reference is effectively final.
        int[] arrayCounter = {0};
        List.of("a", "b", "c").forEach(item -> arrayCounter[0]++);
        System.out.println("    1. int[] counter = {0};  counter[0]++  -> " + arrayCounter[0]);
        System.out.println("       The ARRAY reference never changes; its contents do.");

        // 2. AtomicInteger - the correct choice if threads are involved.
        AtomicInteger atomicCounter = new AtomicInteger();
        List.of("a", "b", "c").forEach(item -> atomicCounter.incrementAndGet());
        System.out.println("    2. AtomicInteger.incrementAndGet()     -> " + atomicCounter.get());
        System.out.println("       The right choice if more than one thread is involved.");

        System.out.println("    3. A field, as demonstrated above.");


        /* ====================================================================
         * SECTION 6 - final METHODS AND CLASSES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - final METHODS AND CLASSES");
        System.out.println("=".repeat(74));

        SecureOperation operation = new AuditedOperation();
        operation.execute("transfer funds");

        System.out.println();
        System.out.println("  execute() is FINAL, so no subclass can change the sequence:");
        System.out.println("    @Override void execute(String s) { }");
        System.out.println("    -> error: execute(String) in AuditedOperation cannot override");
        System.out.println("       execute(String) in SecureOperation; overridden method is final");
        System.out.println();
        System.out.println("  USE final ON A METHOD WHEN OVERRIDING WOULD BREAK A GUARANTEE:");
        System.out.println("    - a template method whose sequence must not change (lesson 28)");
        System.out.println("    - a method called from a constructor - final makes that SAFE,");
        System.out.println("      because there is no overridden version to dispatch to");
        System.out.println("    - a security or invariant check");
        System.out.println();
        System.out.println("  private methods are IMPLICITLY final - invisible to subclasses,");
        System.out.println("  so there is nothing to override. static methods are HIDDEN, not");
        System.out.println("  overridden (lesson 25).");

        System.out.println();
        System.out.println("  final CLASSES - nothing can extend them:");
        System.out.println("    String, Integer, Long, Double, LocalDate are all final.");
        System.out.println();
        System.out.println("    class MyString extends String { }");
        System.out.println("    -> error: cannot inherit from final java.lang.String");
        System.out.println();
        System.out.println("  String is final for concrete reasons: a mutable String subclass");
        System.out.println("  would break the string POOL, break HashMap keys, and break");
        System.out.println("  every security check that validates a filename or URL before");
        System.out.println("  using it.");

        System.out.println();
        System.out.println("  THE TRADE-OFF:");
        System.out.println("    final classes cannot be mocked by frameworks that work");
        System.out.println("    through subclassing (older Mockito, EasyMock). Modern Mockito");
        System.out.println("    mocks them via an inline agent, so this matters much less");
        System.out.println("    than it used to - but it is why some teams avoid it.");
        System.out.println();
        System.out.println("    The counter-argument, from Effective Java:");
        System.out.println("      'Design and document for inheritance, or else prohibit it.'");
        System.out.println("    A class that is NEITHER designed for extension NOR final is a");
        System.out.println("    hazard - subclasses will depend on internals you never meant");
        System.out.println("    to freeze (lesson 26's fragile base class problem).");


        /* ====================================================================
         * SECTION 7 - final AND PERFORMANCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE PERFORMANCE CLAIM, EXAMINED");
        System.out.println("=".repeat(74));

        System.out.println("  THE OLD ADVICE: 'final helps the JIT inline'.");
        System.out.println("  THIS IS LARGELY OBSOLETE. Modern JITs perform CLASS HIERARCHY");
        System.out.println("  ANALYSIS: if only one implementation is loaded, they inline it");
        System.out.println("  anyway, and de-optimise later if another appears.");

        System.out.println();
        System.out.println("  final STILL HAS TWO NARROW PERFORMANCE RELEVANCES:");
        System.out.println();
        System.out.println("    1. static final COMPILE-TIME CONSTANTS are inlined by JAVAC,");
        System.out.println("       not the JIT. That is a real effect, and Section 4's hazard.");
        System.out.println();
        System.out.println("    2. final FIELDS have MEMORY-MODEL GUARANTEES: values assigned");
        System.out.println("       in a constructor are guaranteed VISIBLE to other threads");
        System.out.println("       once the constructor completes, with NO synchronisation.");
        System.out.println();
        System.out.println("       That second one is genuinely important. It is a large part");
        System.out.println("       of WHY immutable objects are thread-safe, and it is why");
        System.out.println("       leaking `this` from a constructor is dangerous (lesson 23)");
        System.out.println("       - the guarantee only holds once the constructor RETURNS.");
        System.out.println("       Lesson 63 covers the memory model.");

        System.out.println();
        System.out.println("  USE final FOR DESIGN AND CLARITY, NOT FOR SPEED.");


        /* ====================================================================
         * SECTION 8 - HOW MUCH final TO WRITE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - STYLE");
        System.out.println("=".repeat(74));

        System.out.println("  Two defensible positions:");
        System.out.println();
        System.out.println("    LIBERAL - final on everything that never changes: locals,");
        System.out.println("      parameters, fields, classes. Intent is explicit.");
        System.out.println();
        System.out.println("    MINIMAL - final on fields and constants only. Argues that");
        System.out.println("      final on every local is visual noise, and effectively-final");
        System.out.println("      already gives you the lambda benefit.");
        System.out.println();
        System.out.println("  THE MIDDLE GROUND THIS REPOSITORY USES:");
        System.out.printf("    %-44s %s%n", "fields", "YES, wherever possible");
        System.out.printf("    %-44s %s%n", "constants", "YES, always static final");
        System.out.printf("    %-44s %s%n", "classes not designed for inheritance", "YES");
        System.out.printf("    %-44s %s%n", "methods called from constructors", "YES");
        System.out.printf("    %-44s %s%n", "local variables", "only when it clarifies");
        System.out.printf("    %-44s %s%n", "parameters", "usually not");

        System.out.println();
        System.out.println("  THE IMPORTANT ONE IS THE FIRST. Default your FIELDS to final");
        System.out.println("  and make them mutable only when something genuinely has to");
        System.out.println("  change. That single habit gets you most of the way to lesson 38.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 31.");
        System.out.println("=".repeat(74));
    }

    /**
     * Not a compile-time constant, because the value comes from a method call.
     * Such fields are NOT inlined by javac.
     *
     * @return a limit
     */
    static int computeLimit() {
        return 10 * 5;
    }

    /**
     * Shows that a lambda may capture and MUTATE a field, because the field
     * lives on the heap rather than the stack.
     */
    void demonstrateFieldCapture() {
        List.of("a", "b", "c").forEach(item -> instanceCounter++);
        System.out.println("    a lambda incremented an instance FIELD -> instanceCounter = "
                + instanceCounter);
        System.out.println("    No `final` needed, and no error - fields are not restricted.");
    }
}

// ----------------------------------------------------------------------------
// SECTION 2 - THE CONSTANT THAT IS NOT
// ----------------------------------------------------------------------------

/** Contrasts a final-but-mutable constant with a genuinely immutable one. */
class Settings {

    /** `final` stops reassignment; the CONTENTS are wide open to everyone. */
    public static final List<String> MUTABLE_ALLOWED =
            new ArrayList<>(List.of("initial"));

    /** Nothing to mutate, so this really is a constant. */
    public static final List<String> REALLY_IMMUTABLE = List.of("safe", "values");
}

// ----------------------------------------------------------------------------
// SECTION 3 - BLANK FINALS
// ----------------------------------------------------------------------------

/**
 * Every field is a BLANK FINAL, assigned exactly once in each constructor.
 * The compiler verifies that on every path.
 */
class ImmutablePoint {

    private final int x;
    private final int y;

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     */
    ImmutablePoint(int x, int y) {
        this.x = x;
        this.y = y;
        // this.x = 99;
        //   ERROR: variable x might already have been assigned
    }

    /**
     * A second constructor. It must ALSO assign both fields - here by
     * delegating, which is the tidiest way to guarantee it.
     *
     * @param both the value for both coordinates
     */
    ImmutablePoint(int both) {
        this(both, both);
    }

    // ImmutablePoint(boolean unused) { }
    //   ERROR: variable x might not have been initialized

    /** @return the origin */
    static ImmutablePoint origin() {
        return new ImmutablePoint(0, 0);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - final METHODS
// ----------------------------------------------------------------------------

/** A template whose sequence is protected by `final`. */
class SecureOperation {

    /**
     * FINAL: no subclass may reorder or skip these steps. This is the same
     * pattern as lesson 28's template method.
     *
     * @param description what is being done
     */
    final void execute(String description) {
        authorise(description);
        perform(description);
        audit(description);
    }

    /** Private, so it is implicitly final too. @param description the action */
    private void authorise(String description) {
        System.out.println("    [fixed]    authorised: " + description);
    }

    /** The overridable hook. @param description the action */
    void perform(String description) {
        System.out.println("    [hook]     default implementation");
    }

    /** Private and unbreakable. @param description the action */
    private void audit(String description) {
        System.out.println("    [fixed]    audited: " + description);
    }
}

/** Fills the hook. It cannot touch the sequence. */
class AuditedOperation extends SecureOperation {

    @Override
    void perform(String description) {
        System.out.println("    [hook]     AuditedOperation performing " + description);
    }

    // @Override final void execute(String description) { }
    //   ERROR: execute(String) in AuditedOperation cannot override
    //   execute(String) in SecureOperation; overridden method is final
}

/**
 * A final class. Nothing can extend it, which is the right default for a
 * class that was not designed for inheritance.
 */
final class ImmutableMoney {

    private final long amountInPaise;
    private final String currency;

    /**
     * @param amountInPaise the amount in the smallest unit
     * @param currency      the currency code
     */
    ImmutableMoney(long amountInPaise, String currency) {
        this.amountInPaise = amountInPaise;
        this.currency = currency;
    }

    /**
     * Returns a NEW object rather than mutating this one - the standard shape
     * for an immutable type.
     *
     * @param other the money to add; must be the same currency
     * @return a new ImmutableMoney
     */
    ImmutableMoney plus(ImmutableMoney other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch");
        }
        return new ImmutableMoney(amountInPaise + other.amountInPaise, currency);
    }

    @Override
    public String toString() {
        return currency + " " + (amountInPaise / 100.0);
    }
}

// class Subverted extends ImmutableMoney { }
//   ERROR: cannot inherit from final ImmutableMoney

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a `final Map<String, Integer> scores = new HashMap<>();` and then
 *    write three lines that modify it and one that does not compile. Explain
 *    the difference in one sentence.
 *
 * 2. Give ImmutablePoint a constructor that assigns x twice. Read the error.
 *    Then one that assigns neither. Read that error too.
 *
 * 3. Write a loop that builds lambdas capturing the loop variable:
 *        for (int i = 0; i < 3; i++) runnables.add(() -> print(i));
 *    It does not compile. Fix it two ways, and explain what the enhanced-for
 *    version does differently.
 *
 * 4. Make ImmutableMoney non-final and write a subclass that breaks its
 *    immutability. Then put final back and confirm you cannot.
 *
 * 5. Create two classes in separate files: one with a public static final int
 *    constant, one that prints it. Compile both. Change the constant, recompile
 *    ONLY its own class, and run again. The old value is still printed - you
 *    have just reproduced Section 4's hazard.
 *
 * 6. Take a class of your own and make every field final. For each field you
 *    cannot, write down why. Those reasons are your mutable state.
 * ============================================================================
 */
