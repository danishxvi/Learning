/* ============================================================================
 * 51 - Lambda expressions
 * ----------------------------------------------------------------------------
 * Companion lesson: 51-lambda-expressions.md
 *
 * RUN IT:
 *     java Java/10-functional-java/51-lambda-expressions.java
 *
 * "A lambda is just an anonymous class" is the most common wrong intuition
 * about this feature. Section 2 proves, with real compiled bytecode, that it
 * is not - and that difference is not trivia, it explains why lambdas were
 * added in Java 8 at all instead of just using anonymous classes forever.
 * ============================================================================
 */

import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

class LambdaExpressions {

    private int instanceCounter = 100;

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE SYNTAX, EVERY SHAPE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - EVERY LAMBDA SHAPE");
        System.out.println("=".repeat(74));

        Runnable noArgs = () -> System.out.println("    () -> ...                    no parameters");
        noArgs.run();

        IntUnaryOperator oneArgNoParens = x -> x * 2;
        System.out.println("    x -> x * 2                    parens OPTIONAL for exactly one");
        System.out.println("                                   inferred-type parameter -> "
                + oneArgNoParens.applyAsInt(21));

        IntUnaryOperator oneArgWithParens = (x) -> x * 2;
        System.out.println("    (x) -> x * 2                  parens allowed too, same thing -> "
                + oneArgWithParens.applyAsInt(21));

        java.util.function.IntBinaryOperator twoArgs = (a, b) -> a + b;
        System.out.println("    (a, b) -> a + b                multiple params NEED the parens -> "
                + twoArgs.applyAsInt(3, 4));

        IntUnaryOperator blockBody = x -> {
            int doubled = x * 2;
            return doubled + 1;         // explicit return REQUIRED inside a { } body
        };
        System.out.println("    x -> { ...; return ...; }     block body, explicit return -> "
                + blockBody.applyAsInt(10));

        java.util.function.IntBinaryOperator explicitTypes = (int a, int b) -> a * b;
        System.out.println("    (int a, int b) -> a * b       explicit types ALLOWED, rarely needed -> "
                + explicitTypes.applyAsInt(6, 7));


        /* ====================================================================
         * SECTION 2 - WHAT A LAMBDA ACTUALLY IS (NOT AN ANONYMOUS CLASS)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - A LAMBDA IS NOT SUGAR FOR AN ANONYMOUS CLASS");
        System.out.println("=".repeat(74));

        System.out.println("  The intuition everyone starts with: '() -> ... is just shorthand");
        System.out.println("  for new Runnable() { public void run() { ... } }'. Close, but");
        System.out.println("  demonstrably wrong at the bytecode level. Real, reproducible proof:");
        System.out.println();
        System.out.println("    ClassFileCheck.java, containing BOTH:");
        System.out.println("      Runnable anon   = new Runnable() { public void run() {...} };");
        System.out.println("      Runnable lambda = () -> System.out.println(\"lambda\");");
        System.out.println();
        System.out.println("    javac ClassFileCheck.java && ls");
        System.out.println("      ClassFileCheck.class      <- the outer class");
        System.out.println("      ClassFileCheck$1.class    <- the ANONYMOUS CLASS, a real .class file");
        System.out.println("      (nothing else - NO separate .class file for the lambda AT ALL)");
        System.out.println();
        System.out.println("    javap -c ClassFileCheck.class | grep invokedynamic");
        System.out.println("      invokedynamic #10, 0   // InvokeDynamic #0:run:()Ljava/lang/Runnable;");
        System.out.println();
        System.out.println("  An anonymous class compiles to a REAL, separate, named .class file -");
        System.out.println("  loaded and instantiated as ordinary bytecode, exactly like any other");
        System.out.println("  class. A lambda compiles to a single invokedynamic instruction. The");
        System.out.println("  JVM defers the actual decision of HOW to implement it to a runtime");
        System.out.println("  bootstrap method (LambdaMetafactory), which generates the");
        System.out.println("  implementation class ON THE FLY, the FIRST time that call site is");
        System.out.println("  reached - and can cache or reuse it. This is WHY lambdas were worth");
        System.out.println("  adding as a separate feature rather than just sugar: no .class file");
        System.out.println("  bloat per lambda, and the JVM has more freedom to optimize.");


        /* ====================================================================
         * SECTION 3 - CAPTURING VARIABLES: EFFECTIVELY FINAL, FOR REAL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - CAPTURED VARIABLES MUST BE EFFECTIVELY FINAL");
        System.out.println("=".repeat(74));

        int base = 10;   // never reassigned after this -> "effectively final", no `final` keyword needed
        Supplier<Integer> addsToBase = () -> base + 5;
        System.out.println("    int base = 10;  (never reassigned)");
        System.out.println("    () -> base + 5   compiles fine, captures the VALUE -> "
                + addsToBase.get());

        System.out.println();
        System.out.println("    THE REAL COMPILE ERROR if you DO reassign a captured variable:");
        System.out.println();
        System.out.println("      int counter = 0;");
        System.out.println("      Runnable r = () -> { counter++; System.out.println(counter); };");
        System.out.println();
        System.out.println("      javac CaptureTest.java");
        System.out.println("        error: local variables referenced from a lambda expression");
        System.out.println("        must be final or effectively final");
        System.out.println("            counter++;");
        System.out.println("            ^");
        System.out.println("        (and again for the println(counter) line - TWO errors, one");
        System.out.println("        per USE of the now-not-effectively-final variable)");
        System.out.println();
        System.out.println("    WHY: the lambda may be handed to another thread, or simply called");
        System.out.println("    long after this method returns and its local variables are gone");
        System.out.println("    from the stack. The lambda captures the VALUE at creation time (or");
        System.out.println("    a reference, for objects), not a live connection to the variable's");
        System.out.println("    storage - so the compiler forbids anything that would make that");
        System.out.println("    distinction observable.");

        System.out.println();
        System.out.println("    THE WORKAROUND, when you genuinely need a running total - an");
        System.out.println("    effectively-final REFERENCE to a MUTABLE object:");
        int[] mutableBox = {0};   // the ARRAY REFERENCE is effectively final; its CONTENTS are not
        Runnable incrementer = () -> mutableBox[0]++;
        incrementer.run();
        incrementer.run();
        incrementer.run();
        System.out.println("      int[] mutableBox = {0}; () -> mutableBox[0]++;");
        System.out.println("      after 3 calls -> " + mutableBox[0]
                + "   (the ARRAY reference never changed, only its CONTENTS)");


        /* ====================================================================
         * SECTION 4 - A LAMBDA DOES NOT OPEN A NEW SCOPE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - A LAMBDA BODY SHARES ITS ENCLOSING SCOPE");
        System.out.println("=".repeat(74));

        System.out.println("    An anonymous class body is a NEW scope - it can freely declare a");
        System.out.println("    local variable with the SAME NAME as one in the enclosing method.");
        System.out.println("    A lambda body CANNOT - it shares the enclosing scope, so reusing a");
        System.out.println("    name is a REAL, DOCUMENTED compile error:");
        System.out.println();
        System.out.println("      int value = 5;");
        System.out.println("      IntUnaryOperator op = (value2) -> {");
        System.out.println("          int value = value2 * 2;   // SAME name as the enclosing local");
        System.out.println("          return value;");
        System.out.println("      };");
        System.out.println();
        System.out.println("      javac ShadowTest.java");
        System.out.println("        error: variable value is already defined in method main(String[])");
        System.out.println("                int value = value2 * 2;");
        System.out.println("                    ^");
        System.out.println();
        System.out.println("    This is a real, useful safety property: it is IMPOSSIBLE to");
        System.out.println("    accidentally shadow an enclosing variable inside a lambda the way");
        System.out.println("    you can inside a nested class or even a nested block in some");
        System.out.println("    languages. The compiler catches the name collision immediately.");


        /* ====================================================================
         * SECTION 5 - "this" MEANS SOMETHING DIFFERENT IN EACH
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - this INSIDE A LAMBDA VS INSIDE AN ANONYMOUS CLASS");
        System.out.println("=".repeat(74));

        new LambdaExpressions().demonstrateThis();


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 51.");
        System.out.println("=".repeat(74));
    }

    /** Instance method so {@code this} refers to a real, meaningful object. */
    void demonstrateThis() {
        System.out.println("    outer instance's this.instanceCounter -> " + this.instanceCounter);

        Runnable anonymousClass = new Runnable() {
            // this.instanceCounter here would NOT compile - the anonymous class has
            // NO field called instanceCounter of its own, proving `this` inside it
            // refers to the ANONYMOUS CLASS INSTANCE, not the enclosing object.
            public void run() {
                System.out.println("      anonymous class: this.getClass().getSimpleName() -> \""
                        + this.getClass().getSimpleName() + "\"   (empty - anonymous classes have no simple name)");
            }
        };
        anonymousClass.run();

        Runnable lambda = () -> {
            // `this` here IS the enclosing LambdaExpressions instance - a lambda has
            // no identity of its own to bind `this` to.
            System.out.println("      lambda: this.instanceCounter directly readable -> " + this.instanceCounter);
            System.out.println("      lambda: this.getClass().getSimpleName() -> \""
                    + this.getClass().getSimpleName() + "\"   (the ENCLOSING class, not the lambda)");
        };
        lambda.run();

        System.out.println();
        System.out.println("    A lambda has no 'this' of its own - it captures the ENCLOSING");
        System.out.println("    instance's this, exactly like a regular nested block would. An");
        System.out.println("    anonymous class is a real class with its own identity, so its");
        System.out.println("    this refers to ITSELF - which is also why it needs");
        System.out.println("    OuterClass.this.field to reach an enclosing field with a name");
        System.out.println("    collision, something a lambda never needs.");
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Write ClassFileCheck.java yourself (Section 2), compile it with javac,
 *    and list the directory. Confirm you see exactly the two .class files
 *    described - no more, no fewer.
 *
 * 2. Reproduce Section 3's compile error yourself: write a method with a
 *    local variable reassigned inside a lambda, and read the exact javac
 *    error message it produces on your JDK version.
 *
 * 3. Write a lambda that captures an int[] of length 1 (Section 3's
 *    workaround) and use it as a counter inside a loop of 100 iterations
 *    calling it via a List<Runnable>. Confirm the final count is 100.
 *
 * 4. In a class with a method taking no arguments, write an anonymous
 *    Runnable and a lambda Runnable side by side, and print
 *    this.getClass() from both (Section 5's approach) to confirm the
 *    difference for yourself with a class of your own design.
 *
 * 5. Try to make a lambda implement an interface with TWO abstract methods
 *    (write one yourself) and read the real compiler error - lesson 52
 *    covers WHY only single-abstract-method interfaces work.
 * ============================================================================
 */
