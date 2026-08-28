/* ============================================================================
 * 29 - INTERFACES
 * ----------------------------------------------------------------------------
 * Companion lesson: 29-interfaces.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/29-interfaces.java
 *
 * An interface is a CONTRACT: a set of methods a class promises to provide.
 * It is Java's most important tool for decoupling code, and it has changed
 * substantially since Java 8 - Section 2 covers what changed and why.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Interfaces {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE BASICS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - A CONTRACT, NOT AN IMPLEMENTATION");
        System.out.println("=".repeat(74));

        List<Drawable> drawables = List.of(
                new Circle(5),
                new Square(4),
                new TextLabel("Hello")
        );

        System.out.println("  Three unrelated classes, one contract:");
        for (Drawable drawable : drawables) {
            System.out.print("    ");
            drawable.draw();
        }

        System.out.println();
        System.out.println("  IMPLICIT MODIFIERS - these are all redundant:");
        System.out.println("      public abstract void draw();     // in an interface");
        System.out.println("      public static final int MAX = 1; // in an interface");
        System.out.println("    Interface methods are ALWAYS public abstract.");
        System.out.println("    Interface fields are ALWAYS public static final.");
        System.out.println("    Most style guides say omit them - they add nothing.");
        System.out.println();
        System.out.println("    Config.MAX_ITEMS = " + Config.MAX_ITEMS
                + "   (a constant, not instance state)");
        System.out.println("    An interface CANNOT have instance fields. No state, ever.");

        System.out.println();
        System.out.println("  Implementing methods MUST be public - you cannot narrow access:");
        System.out.println("      class Circle implements Drawable {");
        System.out.println("          void draw() { }        // ERROR: weaker access");
        System.out.println("          public void draw() { } // correct");
        System.out.println("      }");

        System.out.println();
        System.out.println("  A class may implement MANY interfaces - which is the central");
        System.out.println("  advantage over abstract classes:");
        Circle circle = new Circle(5);
        System.out.println("    circle instanceof Drawable   -> " + (circle instanceof Drawable));
        System.out.println("    circle instanceof Resizable  -> " + (circle instanceof Resizable));
        System.out.println("    circle instanceof Comparable -> " + (circle instanceof Comparable));
        System.out.println("    Interfaces do not consume the single inheritance slot.");


        /* ====================================================================
         * SECTION 2 - WHAT CHANGED IN JAVA 8 AND 9
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - default, static AND private METHODS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-24s %-10s %s%n", "FEATURE", "VERSION", "PURPOSE");
        System.out.printf("    %-24s %-10s %s%n", "abstract methods", "1.0", "the contract");
        System.out.printf("    %-24s %-10s %s%n", "constants", "1.0", "public static final");
        System.out.printf("    %-24s %-10s %s%n", "default methods", "8", "evolve without breaking");
        System.out.printf("    %-24s %-10s %s%n", "static methods", "8", "factories and utilities");
        System.out.printf("    %-24s %-10s %s%n", "private methods", "9", "share code between defaults");
        System.out.printf("    %-24s %-10s %s%n", "sealed", "17", "restrict who may implement");

        System.out.println();
        System.out.println("  WHY default EXISTS - and it is not what people assume.");
        System.out.println();
        System.out.println("    Before Java 8, adding a method to a published interface BROKE");
        System.out.println("    EVERY IMPLEMENTATION IN THE WORLD. That is why Collection");
        System.out.println("    could not gain stream() - millions of classes implement it.");
        System.out.println();
        System.out.println("      interface Collection<E> {");
        System.out.println("          default Stream<E> stream() { ... }   // free for everyone");
        System.out.println("      }");
        System.out.println();
        System.out.println("    That is INTERFACE EVOLUTION, and it is the entire reason");
        System.out.println("    default was added. It was NOT meant to turn interfaces into");
        System.out.println("    abstract classes.");

        System.out.println();
        System.out.println("  A default method in action - Report gets summary() for free:");
        Report sales = new SalesReport();
        Report audit = new AuditReport();
        System.out.println("    " + sales.summary());
        System.out.println("    " + audit.summary());
        System.out.println("    Neither class wrote summary(). Both can override it:");
        System.out.println("    " + new CustomReport().summary());

        System.out.println();
        System.out.println("  STATIC methods on interfaces replaced companion classes:");
        System.out.println("    before Java 8: Collection -> Collections, array -> Arrays");
        System.out.println("    now:           List.of, Map.entry, Comparator.comparing");
        System.out.println();
        Validator notBlank = Validator.notBlank();
        Validator maxLength = Validator.maxLength(5);
        System.out.println("    Validator.notBlank().test(\"  \")     -> " + notBlank.test("  "));
        System.out.println("    Validator.notBlank().test(\"hi\")     -> " + notBlank.test("hi"));
        System.out.println("    Validator.maxLength(5).test(\"hello\") -> " + maxLength.test("hello"));
        System.out.println("    Validator.maxLength(5).test(\"toolong\") -> " + maxLength.test("toolong"));
        System.out.println();
        System.out.println("    Static interface methods are NOT inherited by implementers.");
        System.out.println("    You call Validator.notBlank(), never someValidator.notBlank().");

        System.out.println();
        System.out.println("  PRIVATE methods (Java 9) share code between defaults:");
        Logger logger = message -> System.out.println("      " + message);
        logger.info("server started");
        logger.error("connection refused");
        System.out.println();
        System.out.println("    Both defaults call a PRIVATE log() helper. Without private,");
        System.out.println("    that helper would have to be a default method - and therefore");
        System.out.println("    part of the public contract, visible to every caller.");


        /* ====================================================================
         * SECTION 3 - THE DIAMOND PROBLEM
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - CONFLICTING DEFAULTS");
        System.out.println("=".repeat(74));

        System.out.println("    interface A { default void greet() { print(\"A\"); } }");
        System.out.println("    interface B { default void greet() { print(\"B\"); } }");
        System.out.println("    class C implements A, B { }");
        System.out.println();
        System.out.println("  -> error: class C inherits unrelated defaults for greet()");
        System.out.println("     from types A and B");
        System.out.println();
        System.out.println("  Java refuses to guess. You MUST override, and you can delegate:");

        System.out.print("    ChoosesA.greet()  -> ");
        new ChoosesA().greet();
        System.out.print("    ChoosesB.greet()  -> ");
        new ChoosesB().greet();
        System.out.print("    ChoosesBoth.greet() -> ");
        new ChoosesBoth().greet();
        System.out.println();
        System.out.println("    `A.super.greet()` is the syntax for 'the default from A'.");

        System.out.println();
        System.out.println("  THE RESOLUTION RULES, IN ORDER:");
        System.out.println("    1. A CLASS WINS over an interface.");
        System.out.print("       ClassWins.greet() -> ");
        new ClassWins().greet();
        System.out.println("       The superclass's concrete method beat the interface default,");
        System.out.println("       with no ambiguity error at all.");
        System.out.println();
        System.out.println("    2. THE MOST SPECIFIC INTERFACE WINS. If B extends A, B's");
        System.out.println("       default beats A's:");
        System.out.print("       MostSpecific.greet() -> ");
        new MostSpecific().greet();
        System.out.println();
        System.out.println("    3. OTHERWISE you must override. Ambiguity is a compile error,");
        System.out.println("       never a silent choice.");
        System.out.println();
        System.out.println("  Rule 1 exists to guarantee that adding a `default` to an");
        System.out.println("  interface can NEVER change the behaviour of existing code.");


        /* ====================================================================
         * SECTION 4 - FUNCTIONAL INTERFACES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - ONE ABSTRACT METHOD MEANS A LAMBDA CAN IMPLEMENT IT");
        System.out.println("=".repeat(74));

        Calculator add = (a, b) -> a + b;
        Calculator multiply = (a, b) -> a * b;
        Calculator max = Math::max;                 // a method reference

        System.out.println("    Calculator add      = (a, b) -> a + b;   add.apply(3, 4)      = "
                + add.apply(3, 4));
        System.out.println("    Calculator multiply = (a, b) -> a * b;   multiply.apply(3, 4) = "
                + multiply.apply(3, 4));
        System.out.println("    Calculator max      = Math::max;         max.apply(3, 4)      = "
                + max.apply(3, 4));

        System.out.println();
        System.out.println("    Calculator still has default and static methods:");
        System.out.println("      add.describe()          -> " + add.describe());
        System.out.println("      Calculator.identity()   -> " + Calculator.identity().apply(7, 99));
        System.out.println();
        System.out.println("    default and static methods do NOT count toward the");
        System.out.println("    one-abstract-method limit. An interface can be functional");
        System.out.println("    and still have many methods.");

        System.out.println();
        System.out.println("  @FunctionalInterface is optional but recommended:");
        System.out.println("    it makes the COMPILER enforce exactly one abstract method,");
        System.out.println("    so someone adding a second gets an error rather than");
        System.out.println("    silently breaking every lambda in the codebase.");
        System.out.println();
        System.out.println("  The JDK ships the common shapes in java.util.function -");
        System.out.println("  Function, Predicate, Consumer, Supplier. Lessons 51 and 52.");


        /* ====================================================================
         * SECTION 5 - SEALED INTERFACES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - sealed CLOSES THE HIERARCHY (Java 17)");
        System.out.println("=".repeat(74));

        List<Payment> payments = List.of(
                new CardPayment("4111", 250.00),
                new UpiPayment("danish@upi", 99.50),
                new CashPayment(500.00)
        );

        System.out.println("    sealed interface Payment permits CardPayment, UpiPayment, CashPayment");
        System.out.println();
        for (Payment payment : payments) {
            System.out.printf("    %-42s fee %.2f%n", payment, processingFee(payment));
        }

        System.out.println();
        System.out.println("  processingFee() has NO default branch. The compiler knows the");
        System.out.println("  list of implementers is COMPLETE, so it can verify the switch");
        System.out.println("  is exhaustive.");
        System.out.println();
        System.out.println("  Add a fourth payment type and EVERY such switch stops compiling");
        System.out.println("  until you handle it. That is a genuinely different guarantee");
        System.out.println("  from an open interface.");
        System.out.println();
        System.out.println("  Every permitted type must be final, sealed, or non-sealed.");
        System.out.println();
        System.out.println("  SEALED INTERFACE + RECORDS + SWITCH is a strong alternative to");
        System.out.println("  polymorphism when the VARIANTS are fixed but the OPERATIONS keep");
        System.out.println("  changing - you add a new operation in one place instead of");
        System.out.println("  editing every subclass. Lesson 37.");


        /* ====================================================================
         * SECTION 6 - MARKER INTERFACES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - MARKER INTERFACES");
        System.out.println("=".repeat(74));

        System.out.println("  An interface with NO members at all, used purely to tag a type:");
        System.out.println("      interface Serializable { }");
        System.out.println("      interface Cloneable { }");
        System.out.println();

        Object[] items = {new AuditedDocument("contract"), new PlainNote("shopping list")};
        for (Object item : items) {
            String tag = (item instanceof Auditable) ? "auditable" : "not auditable";
            System.out.println("    " + item + " -> " + tag);
        }

        System.out.println();
        System.out.println("  Annotations largely replaced this pattern, but a marker");
        System.out.println("  interface has one real advantage: it creates a TYPE, so the");
        System.out.println("  COMPILER can enforce it:");
        System.out.println();
        System.out.println("      void archive(Auditable data) { }   // compile-time check");
        System.out.println();
        System.out.println("  An annotation could only be checked at runtime, by reflection.");


        /* ====================================================================
         * SECTION 7 - PROGRAMMING TO INTERFACES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - DECLARE THE INTERFACE, NOT THE IMPLEMENTATION");
        System.out.println("=".repeat(74));

        System.out.println("  BAD:");
        System.out.println("      ArrayList<String> names = new ArrayList<>();");
        System.out.println("      void process(ArrayList<String> items) { }");
        System.out.println();
        System.out.println("  GOOD:");
        System.out.println("      List<String> names = new ArrayList<>();");
        System.out.println("      void process(List<String> items) { }");
        System.out.println();
        System.out.println("  The second version accepts anything a caller already has:");
        System.out.println("    process(new ArrayList<>())  -> " + describeList(new ArrayList<String>()));
        System.out.println("    process(List.of(\"a\", \"b\"))  -> " + describeList(List.of("a", "b")));
        System.out.println("    process(new LinkedList<>()) -> "
                + describeList(new java.util.LinkedList<String>()));
        System.out.println();
        System.out.println("  RULE: declare the most general type that supplies what you use.");
        System.out.println();
        System.out.println("  DO NOT OVERDO IT, though. If your method genuinely needs O(1)");
        System.out.println("  random access, saying `List` and then calling get(i) in a loop");
        System.out.println("  is a lie that a LinkedList will punish (lesson 45).");

        /* --------------------------------------------------------------------
         * WHERE INTERFACES PAY FOR THEMSELVES: testing.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  WHERE INTERFACES ACTUALLY PAY FOR THEMSELVES - testing:");
        System.out.println();

        OrderService production = new OrderService(new RealPaymentGateway());
        System.out.println("    production: " + production.placeOrder("ORD-1", 250.00));

        RecordingPaymentGateway fake = new RecordingPaymentGateway();
        OrderService underTest = new OrderService(fake);
        System.out.println("    test:       " + underTest.placeOrder("ORD-2", 99.00));
        System.out.println("    the fake recorded: " + fake.getCharges());
        System.out.println();
        System.out.println("    Without the interface, OrderService would be untestable");
        System.out.println("    without a network. This is DEPENDENCY INVERSION - the 'D' in");
        System.out.println("    SOLID - and it is the practical reason interfaces matter.");


        /* ====================================================================
         * SECTION 8 - INTERFACE DESIGN
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - DESIGNING GOOD INTERFACES");
        System.out.println("=".repeat(74));

        System.out.println("  KEEP THEM SMALL - the INTERFACE SEGREGATION PRINCIPLE ('I'):");
        System.out.println();
        System.out.println("    BAD - a printer that cannot fax must still implement fax():");
        System.out.println("      interface Machine { void print(); void scan(); void fax(); }");
        System.out.println();
        System.out.println("    GOOD:");
        System.out.println("      interface Printer { void print(); }");
        System.out.println("      interface Scanner { void scan(); }");
        System.out.println("      class AllInOne implements Printer, Scanner { }");
        System.out.println();
        System.out.println("    A class should never be forced to implement methods it does");
        System.out.println("    not need. Many small interfaces beat one large one.");

        System.out.println();
        System.out.println("  NAME THEM FOR CAPABILITY.");
        System.out.println("    Comparable, Runnable, Closeable, Iterable - the -able suffix");
        System.out.println("    reads well. Avoid IUserService; that is a C# convention.");

        System.out.println();
        System.out.println("  DO NOT ADD default METHODS CASUALLY.");
        System.out.println("    They exist to EVOLVE published interfaces, not to avoid");
        System.out.println("    writing an abstract class. Every default is a decision you");
        System.out.println("    impose on every implementer forever.");

        System.out.println();
        System.out.println("  PREFER AN INTERFACE TO AN ABSTRACT CLASS unless you need");
        System.out.println("  state (lesson 28).");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 29.");
        System.out.println("=".repeat(74));
    }

    /**
     * Accepts any List, so callers may pass whatever they already hold.
     *
     * @param items any list
     * @return a description of what actually arrived
     */
    static String describeList(List<String> items) {
        return "accepted a " + items.getClass().getSimpleName()
                + " with " + items.size() + " element(s)";
    }

    /**
     * Computes a fee with an exhaustive switch and NO default branch, which is
     * only possible because Payment is sealed.
     *
     * @param payment the payment to charge for
     * @return the processing fee
     */
    static double processingFee(Payment payment) {
        return switch (payment) {
            case CardPayment card -> card.amount() * 0.02;
            case UpiPayment upi   -> 0.0;
            case CashPayment cash -> 5.0;
            // No default. The compiler knows the list is complete.
        };
    }
}

// ----------------------------------------------------------------------------
// SECTION 1
// ----------------------------------------------------------------------------

/** A capability. Note there is no `public abstract` - it is implicit. */
interface Drawable {
    /** Renders this object. */
    void draw();
}

/** A second capability, to show a class implementing several. */
interface Resizable {
    /** @param factor how much to scale by  @return the new size */
    double resize(double factor);
}

/** Interface fields are implicitly public static final - constants only. */
interface Config {
    /** The maximum number of items. Implicitly public static final. */
    int MAX_ITEMS = 100;

    // int counter;
    //   ERROR: = expected. An interface field needs an initialiser, because
    //   it is a constant. There is no instance state in an interface.
}

/** Implements three interfaces, which no abstract class could allow. */
class Circle implements Drawable, Resizable, Comparable<Circle> {

    private double radius;

    /** @param radius the radius */
    Circle(double radius) {
        this.radius = radius;
    }

    // MUST be public: you cannot narrow access when implementing (lesson 27).
    @Override
    public void draw() {
        System.out.println("Circle with radius " + radius);
    }

    @Override
    public double resize(double factor) {
        radius *= factor;
        return radius;
    }

    @Override
    public int compareTo(Circle other) {
        return Double.compare(this.radius, other.radius);
    }
}

/** A second implementer, unrelated to Circle by inheritance. */
class Square implements Drawable {

    private final double side;

    /** @param side the side length */
    Square(double side) {
        this.side = side;
    }

    @Override
    public void draw() {
        System.out.println("Square with side " + side);
    }
}

/** A third implementer with nothing structurally in common with the others. */
class TextLabel implements Drawable {

    private final String text;

    /** @param text the label text */
    TextLabel(String text) {
        this.text = text;
    }

    @Override
    public void draw() {
        System.out.println("Text label reading \"" + text + "\"");
    }
}

// ----------------------------------------------------------------------------
// SECTION 2 - default, static AND private
// ----------------------------------------------------------------------------

/** Demonstrates a default method inherited free by every implementer. */
interface Report {

    /** @return the report's title */
    String title();

    /** @return how many rows it contains */
    int rowCount();

    /**
     * A DEFAULT method: every implementer gets this without writing it, and
     * may override it if the default is wrong for them.
     *
     * @return a one-line summary
     */
    default String summary() {
        return title() + ": " + rowCount() + " rows";
    }
}

/** Gets summary() for free. */
class SalesReport implements Report {
    @Override public String title() { return "Sales"; }
    @Override public int rowCount() { return 1_204; }
}

/** Also gets summary() for free. */
class AuditReport implements Report {
    @Override public String title() { return "Audit"; }
    @Override public int rowCount() { return 87; }
}

/** Overrides the default, which implementers are always free to do. */
class CustomReport implements Report {
    @Override public String title() { return "Custom"; }
    @Override public int rowCount() { return 3; }

    @Override
    public String summary() {
        return "Custom report (overrode the default): " + rowCount() + " row(s)";
    }
}

/** A functional interface with STATIC factory methods on the interface itself. */
@FunctionalInterface
interface Validator {

    /** @param input the text to check  @return true if it passes */
    boolean test(String input);

    /** @return a validator rejecting null and blank text */
    static Validator notBlank() {
        return input -> input != null && !input.isBlank();
    }

    /** @param limit the maximum length  @return a validator enforcing it */
    static Validator maxLength(int limit) {
        return input -> input != null && input.length() <= limit;
    }
}

/** Uses a PRIVATE method (Java 9+) to share code between two defaults. */
interface Logger {

    /** @param message the fully formatted line to emit */
    void write(String message);

    /** @param message the message to log at INFO */
    default void info(String message) {
        log("INFO", message);
    }

    /** @param message the message to log at ERROR */
    default void error(String message) {
        log("ERROR", message);
    }

    /**
     * PRIVATE, so it is shared between the defaults WITHOUT becoming part of
     * the public contract. Before Java 9 this had to be a default method,
     * visible to every caller.
     *
     * @param level   the severity label
     * @param message the message
     */
    private void log(String level, String message) {
        write("[" + level + "] " + message);
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - THE DIAMOND PROBLEM
// ----------------------------------------------------------------------------

/** One half of the conflict. */
interface GreeterA {
    /** Prints a greeting. */
    default void greet() {
        System.out.println("GreeterA's default");
    }
}

/** The other half. */
interface GreeterB {
    /** Prints a greeting. */
    default void greet() {
        System.out.println("GreeterB's default");
    }
}

// class Broken implements GreeterA, GreeterB { }
//   ERROR: class Broken inherits unrelated defaults for greet() from types
//   GreeterA and GreeterB. Java refuses to guess.

/** Resolves the conflict by delegating to A. */
class ChoosesA implements GreeterA, GreeterB {
    @Override
    public void greet() {
        GreeterA.super.greet();
    }
}

/** Resolves it by delegating to B. */
class ChoosesB implements GreeterA, GreeterB {
    @Override
    public void greet() {
        GreeterB.super.greet();
    }
}

/** Resolves it by using both. */
class ChoosesBoth implements GreeterA, GreeterB {
    @Override
    public void greet() {
        System.out.print("both -> ");
        GreeterA.super.greet();
        System.out.print("            ");
        GreeterB.super.greet();
    }
}

/** A concrete superclass, whose method beats any interface default. */
class ConcreteGreeter {
    /** Prints a greeting. */
    public void greet() {
        System.out.println("the CLASS's method  <- rule 1: a class always wins");
    }
}

/** RULE 1: the class's concrete method wins, with no ambiguity error at all. */
class ClassWins extends ConcreteGreeter implements GreeterA, GreeterB {
    // Nothing to write. The inherited class method resolves the conflict.
}

/** A more specific interface, extending GreeterA and overriding its default. */
interface SpecificGreeter extends GreeterA {
    @Override
    default void greet() {
        System.out.println("SpecificGreeter's default  <- rule 2: most specific wins");
    }
}

/** RULE 2: SpecificGreeter extends GreeterA, so its default wins. */
class MostSpecific implements GreeterA, SpecificGreeter {
    // Nothing to write. SpecificGreeter is more specific than GreeterA.
}

// ----------------------------------------------------------------------------
// SECTION 4 - FUNCTIONAL INTERFACES
// ----------------------------------------------------------------------------

/**
 * Exactly ONE abstract method, so a lambda can implement it. The annotation is
 * optional but makes the compiler enforce that, so nobody can add a second
 * abstract method and silently break every lambda.
 */
@FunctionalInterface
interface Calculator {

    /** THE one abstract method. @param a first  @param b second  @return the result */
    int apply(int a, int b);

    /** A default method - does NOT count toward the limit. @return a description */
    default String describe() {
        return "a Calculator (a lambda implementing one abstract method)";
    }

    /** A static factory - also does not count. @return a calculator returning its first argument */
    static Calculator identity() {
        return (a, b) -> a;
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - SEALED INTERFACES
// ----------------------------------------------------------------------------

/** A CLOSED hierarchy: these three types are the complete list, forever. */
sealed interface Payment permits CardPayment, UpiPayment, CashPayment {
    /** @return the amount being paid */
    double amount();
}

/** @param cardNumber the masked card number  @param amount the amount */
record CardPayment(String cardNumber, double amount) implements Payment {}

/** @param upiId the UPI identifier  @param amount the amount */
record UpiPayment(String upiId, double amount) implements Payment {}

/** @param amount the amount */
record CashPayment(double amount) implements Payment {}

// ----------------------------------------------------------------------------
// SECTION 6 - MARKER INTERFACES
// ----------------------------------------------------------------------------

/** A MARKER interface: no members at all, used purely to tag a type. */
interface Auditable {
}

/** Tagged, so `instanceof Auditable` is true and the compiler can check it. */
class AuditedDocument implements Auditable {

    private final String name;

    /** @param name the document name */
    AuditedDocument(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AuditedDocument[" + name + "]";
    }
}

/** Not tagged. */
class PlainNote {

    private final String text;

    /** @param text the note text */
    PlainNote(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "PlainNote[" + text + "]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - DEPENDENCY INVERSION
// ----------------------------------------------------------------------------

/** The abstraction OrderService depends on, rather than a concrete class. */
interface PaymentGateway {
    /**
     * @param orderId the order being paid for
     * @param amount  how much to charge
     * @return a confirmation reference
     */
    String charge(String orderId, double amount);
}

/** What production uses - it would really hit the network. */
class RealPaymentGateway implements PaymentGateway {
    @Override
    public String charge(String orderId, double amount) {
        return "REAL-TXN-" + orderId;      // pretend network call
    }
}

/** What a test uses: records calls instead of making them. */
class RecordingPaymentGateway implements PaymentGateway {

    private final List<String> charges = new ArrayList<>();

    @Override
    public String charge(String orderId, double amount) {
        charges.add(orderId + " for " + amount);
        return "FAKE-TXN-" + orderId;
    }

    /** @return what this fake was asked to do */
    List<String> getCharges() {
        return List.copyOf(charges);
    }
}

/** Depends on the INTERFACE, so either gateway can be supplied. */
class OrderService {

    private final PaymentGateway gateway;

    /** @param gateway whichever gateway the caller supplies */
    OrderService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    /**
     * @param orderId the order id
     * @param amount  the amount
     * @return the confirmation reference
     */
    String placeOrder(String orderId, double amount) {
        return gateway.charge(orderId, amount);
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Uncomment `class Broken implements GreeterA, GreeterB { }` and read the
 *    error. Then fix it three different ways.
 *
 * 2. Add a fourth Payment type. Count how many places stop compiling. Now
 *    imagine the interface was NOT sealed - how would you have found them?
 *
 * 3. Add a second abstract method to Calculator. Read the @FunctionalInterface
 *    error, and note it appears on the INTERFACE, not on the lambdas.
 *
 * 4. Write a Cache interface with get/put, then a default `getOrCompute` that
 *    uses them. Implement it two ways and confirm neither wrote getOrCompute.
 *
 * 5. Add a `void fax()` to a Machine interface and implement it in a
 *    printer-only class. Notice what you are forced to write. Then split the
 *    interface and delete that code.
 *
 * 6. Replace the Auditable marker interface with an annotation. What can you
 *    no longer do at compile time? That difference is the whole argument for
 *    marker interfaces.
 * ============================================================================
 */
