/* ============================================================================
 * 21 - CLASSES AND OBJECTS
 * ----------------------------------------------------------------------------
 * Companion lesson: 21-classes-and-objects.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/21-classes-and-objects.java
 *
 * This is where Java stops being "C with a JVM". Everything from here to
 * lesson 38 builds on the ideas in this file, so it is worth going slowly.
 *
 * A CLASS is a blueprint. An OBJECT is a thing built from it. A blueprint for
 * a house is not a house.
 * ============================================================================
 */

import java.util.Objects;

class ClassesAndObjects {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - ONE CLASS, MANY INDEPENDENT OBJECTS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - CLASS VS OBJECT");
        System.out.println("=".repeat(74));

        // The Car CLASS was written once, at the bottom of this file.
        // These are two OBJECTS built from it, created at runtime.
        Car myCar = new Car("Tesla Model 3");
        Car yourCar = new Car("Tata Nexon");

        System.out.println("  Two objects from one blueprint:");
        System.out.println("    myCar   -> " + myCar);
        System.out.println("    yourCar -> " + yourCar);

        myCar.accelerate();
        myCar.accelerate();
        myCar.accelerate();

        System.out.println();
        System.out.println("  After accelerating ONLY myCar three times:");
        System.out.println("    myCar   -> " + myCar);
        System.out.println("    yourCar -> " + yourCar + "   <- untouched");
        System.out.println();
        System.out.println("  Each object has its OWN copy of every instance field.");
        System.out.println("  They SHARE one copy of the method code - there is exactly one");
        System.out.println("  accelerate() in memory no matter how many Cars exist.");

        System.out.println();
        System.out.println("  Both really are the same class:");
        System.out.println("    myCar.getClass()   = " + myCar.getClass().getSimpleName());
        System.out.println("    yourCar.getClass() = " + yourCar.getClass().getSimpleName());
        System.out.println("    same class object? " + (myCar.getClass() == yourCar.getClass())
                + "   (one Class object, loaded once)");


        /* ====================================================================
         * SECTION 2 - WHAT new ACTUALLY DOES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE FIVE STEPS OF new");
        System.out.println("=".repeat(74));

        System.out.println("  Watch the order of the printed lines below. They prove the");
        System.out.println("  sequence: field initialisers and instance blocks run BEFORE");
        System.out.println("  the constructor body.");
        System.out.println();

        new ConstructionOrder("from the caller");

        System.out.println();
        System.out.println("  THE FIVE STEPS:");
        System.out.println("    1. memory allocated on the HEAP for all instance fields");
        System.out.println("    2. fields ZEROED - 0, false, null by type");
        System.out.println("    3. field initialisers and instance blocks run, top to bottom");
        System.out.println("    4. the constructor body runs");
        System.out.println("    5. the reference is returned");
        System.out.println();
        System.out.println("  Step 2 is exactly why FIELDS have default values but LOCAL");
        System.out.println("  variables do not (lesson 03): the JVM zeroes the object's");
        System.out.println("  memory as part of allocating it. Nothing zeroes the stack.");

        // Proving step 2 directly.
        Defaults defaults = new Defaults();
        System.out.println();
        System.out.println("  Proof - a class whose fields are never assigned:");
        System.out.println("    " + defaults);


        /* ====================================================================
         * SECTION 3 - STACK, HEAP AND REFERENCES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE VARIABLE IS NOT THE OBJECT");
        System.out.println("=".repeat(74));

        Car original = new Car("Honda City");
        Car alias = original;            // copies the REFERENCE, not the object
        Car separate = new Car("Honda City");   // a genuinely different object

        alias.accelerate();

        System.out.println("  Car original = new Car(\"Honda City\");");
        System.out.println("  Car alias    = original;          // a second NAME");
        System.out.println("  Car separate = new Car(\"Honda City\");  // a second OBJECT");
        System.out.println();
        System.out.println("  After alias.accelerate():");
        System.out.println("    original -> " + original + "   <- changed");
        System.out.println("    alias    -> " + alias);
        System.out.println("    separate -> " + separate + "   <- untouched");
        System.out.println();
        System.out.println("    original == alias    -> " + (original == alias)
                + "    ONE object, two names");
        System.out.println("    original == separate -> " + (original == separate)
                + "   TWO objects that happen to match");

        System.out.println();
        System.out.println("      STACK                        HEAP");
        System.out.println("  +-------------+          +---------------------+");
        System.out.println("  | original    |--------->| Car \"Honda City\"    |");
        System.out.println("  | alias       |--------->| speed = 10          |");
        System.out.println("  +-------------+          +---------------------+");
        System.out.println("  | separate    |--------->| Car \"Honda City\"    |");
        System.out.println("  +-------------+          | speed = 0           |");
        System.out.println("                           +---------------------+");
        System.out.println();
        System.out.println("  The VARIABLE lives on the stack and holds a reference.");
        System.out.println("  The OBJECT always lives on the heap.");
        System.out.println("  This is the picture that makes lesson 17's pass-by-value");
        System.out.println("  discussion make sense.");


        /* ====================================================================
         * SECTION 4 - null AND NullPointerException
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - null");
        System.out.println("=".repeat(74));

        Car nothing = null;    // perfectly legal: a reference pointing at nothing
        System.out.println("  Car nothing = null;   -> legal. Printing it: " + nothing);

        try {
            nothing.accelerate();
        } catch (NullPointerException e) {
            System.out.println("  nothing.accelerate() -> NullPointerException");
            System.out.println("    message: " + e.getMessage());
            System.out.println();
            System.out.println("  Since Java 14 that message names the EXACT expression that");
            System.out.println("  was null. Before Java 14 you got a line number and had to");
            System.out.println("  guess which of five dots on that line was the problem.");
        }

        System.out.println();
        System.out.println("  DEFENCES, best first:");
        System.out.println("    1. Do not produce null - return empty collections and");
        System.out.println("       Optional (lessons 17 and 56)");
        System.out.println("    2. Fail fast - Objects.requireNonNull in constructors");
        System.out.println("    3. Guard - null checks, or safe ordering like");
        System.out.println("       \"literal\".equals(maybeNull)");

        System.out.println();
        System.out.println("  Fail-fast in action:");
        try {
            new Car(null);
        } catch (NullPointerException e) {
            System.out.println("    new Car(null) -> NullPointerException: " + e.getMessage());
            System.out.println("    The object refuses to exist in an invalid state, and the");
            System.out.println("    error names the real cause instead of surfacing later in");
            System.out.println("    some unrelated method.");
        }


        /* ====================================================================
         * SECTION 5 - ENCAPSULATION: THE OBJECT PROTECTS ITSELF
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - WHY FIELDS ARE private");
        System.out.println("=".repeat(74));

        BankAccount account = new BankAccount("ACC-001", 5000);
        System.out.println("  " + account);

        account.deposit(2500);
        System.out.println("  after deposit(2500)  -> " + account);

        account.withdraw(1000);
        System.out.println("  after withdraw(1000) -> " + account);

        System.out.println();
        System.out.println("  Every rule the account has is enforced by the account itself:");

        try {
            account.deposit(-5000);
        } catch (IllegalArgumentException e) {
            System.out.println("    deposit(-5000)   -> IllegalArgumentException: " + e.getMessage());
        }
        try {
            account.withdraw(999_999);
        } catch (IllegalStateException e) {
            System.out.println("    withdraw(999999) -> IllegalStateException: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  If `balance` were public, this would compile:");
        System.out.println("      account.balance = -5000;");
        System.out.println("  and there would be nothing the class could do about it.");
        System.out.println();
        System.out.println("  THAT is the point of encapsulation: the object protects its");
        System.out.println("  own invariants. Not \"getters and setters are good style\" -");
        System.out.println("  a setter with no validation gives away exactly what private");
        System.out.println("  was protecting. Lesson 24 covers this properly.");

        System.out.println();
        System.out.println("  Balance is still readable, through a method:");
        System.out.println("    account.getBalance() = " + account.getBalance());


        /* ====================================================================
         * SECTION 6 - ALWAYS OVERRIDE toString()
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - toString()");
        System.out.println("=".repeat(74));

        WithoutToString useless = new WithoutToString("Danish", 25);
        Car withToString = new Car("Mahindra Thar");

        System.out.println("  WITHOUT an override:");
        System.out.println("    println(object)  -> " + useless);
        System.out.println("    That is the class name, an @, and the identity hash in hex.");
        System.out.println("    It tells you nothing about the object's actual state.");

        System.out.println();
        System.out.println("  WITH an override:");
        System.out.println("    println(object)  -> " + withToString);

        System.out.println();
        System.out.println("  toString() is called AUTOMATICALLY by:");
        System.out.println("    System.out.println(obj)   -> " + withToString);
        System.out.println("    \"\" + obj                  -> " + ("" + withToString));
        System.out.println("    String.valueOf(obj)       -> " + String.valueOf(withToString));
        System.out.println("    a collection printing it  -> " + java.util.List.of(withToString));
        System.out.println("    ...and your debugger, and every log line.");
        System.out.println();
        System.out.println("  OVERRIDE toString() ON EVERY CLASS YOU WRITE. Three lines, and");
        System.out.println("  it pays for itself the first time you debug. Records (lesson");
        System.out.println("  36) generate it for you, which is one of their main draws.");


        /* ====================================================================
         * SECTION 7 - OBJECT LIFECYCLE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - LIFECYCLE AND GARBAGE COLLECTION");
        System.out.println("=".repeat(74));

        Runtime runtime = Runtime.getRuntime();
        long megabyte = 1024L * 1024L;

        long before = (runtime.totalMemory() - runtime.freeMemory()) / megabyte;

        // Create a large number of objects, then drop every reference to them.
        Car[] fleet = new Car[500_000];
        for (int i = 0; i < fleet.length; i++) {
            fleet[i] = new Car("car-" + i);
        }
        long withFleet = (runtime.totalMemory() - runtime.freeMemory()) / megabyte;

        fleet = null;      // every one of those 500,000 objects is now UNREACHABLE

        System.gc();       // a SUGGESTION, not a command - the JVM may ignore it
        try {
            Thread.sleep(100);   // give the collector a moment, if it chose to run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long afterCollection = (runtime.totalMemory() - runtime.freeMemory()) / megabyte;

        System.out.println("  Heap in use before creating 500,000 Cars : " + before + " MB");
        System.out.println("  Heap in use with the fleet alive         : " + withFleet + " MB");
        System.out.println("  Heap in use after dropping the reference : " + afterCollection + " MB");
        System.out.println();
        System.out.println("  THE LIFECYCLE:");
        System.out.println("    1. CREATION      - new allocates and initialises");
        System.out.println("    2. IN USE        - reachable from a live reference");
        System.out.println("    3. UNREACHABLE   - nothing refers to it any more");
        System.out.println("    4. COLLECTED     - the JVM reclaims it, WHEN IT CHOOSES");
        System.out.println();
        System.out.println("  You cannot force step 4. System.gc() is a suggestion the JVM");
        System.out.println("  is free to ignore, which is why the numbers above may or may");
        System.out.println("  not have dropped. Run it twice and you may see different results.");
        System.out.println();
        System.out.println("  finalize() - a method that once ran before collection - is");
        System.out.println("  DEPRECATED FOR REMOVAL and must not be used. It was");
        System.out.println("  unpredictable, could resurrect objects, and delayed collection.");
        System.out.println("  Use try-with-resources for cleanup instead (lesson 41).");


        /* ====================================================================
         * SECTION 8 - DESIGN PRINCIPLES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - FIRST PRINCIPLES OF CLASS DESIGN");
        System.out.println("=".repeat(74));

        System.out.println("  ONE CLASS, ONE RESPONSIBILITY.");
        System.out.println("    If describing the class needs the word 'and', it is two.");
        System.out.println();
        System.out.println("  FIELDS private, METHODS public - by default.");
        System.out.println("    Widen access only when you have a reason.");
        System.out.println();
        System.out.println("  PREFER IMMUTABILITY.");
        System.out.println("    Fields final where possible, no setters unless required.");
        System.out.println("    An immutable object is automatically thread-safe and cannot");
        System.out.println("    be corrupted by a caller. Lesson 38.");
        System.out.println();
        System.out.println("  NAME CLASSES AS NOUNS, METHODS AS VERBS.");
        System.out.println("    Invoice.calculateTotal()   not   InvoiceManager.doStuff()");
        System.out.println();
        System.out.println("  BE SUSPICIOUS OF -Manager, -Helper, -Util, -Processor.");
        System.out.println("    They usually mark a class with no clear responsibility that");
        System.out.println("    has quietly become a dumping ground.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 21.");
        System.out.println("=".repeat(74));
    }
}

/**
 * The blueprint used throughout Section 1. Note the four things every class
 * you write should have: private fields, a validating constructor, behaviour
 * that operates on the state, and a toString().
 */
class Car {

    /** Immutable identity - set once in the constructor and never changed. */
    private final String model;

    /** Mutable state, changed only through accelerate() and brake(). */
    private int speed;

    /**
     * @param model the car's model name; must not be null
     * @throws NullPointerException if model is null
     */
    Car(String model) {
        // FAIL FAST: refuse to create an object that is already broken.
        // The exception names the real cause here, rather than surfacing
        // later as a mysterious NPE inside toString().
        this.model = Objects.requireNonNull(model, "model must not be null");
        this.speed = 0;
    }

    /** Increases speed by 10. */
    void accelerate() {
        speed += 10;
    }

    /** Decreases speed by 10, never below zero. */
    void brake() {
        speed = Math.max(0, speed - 10);
    }

    /** @return the current speed */
    int getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return "Car[" + model + ", " + speed + " km/h]";
    }
}

/**
 * Makes the object-construction order visible by printing at each stage.
 * The output order is the proof that field initialisers and instance blocks
 * run BEFORE the constructor body.
 */
class ConstructionOrder {

    /** A field initialiser - runs at step 3, before the constructor body. */
    private String fromInitialiser = report("  3a. field initialiser ran");

    /** An instance initialiser block - also step 3, in source order. */
    {
        report("  3b. instance initialiser block ran");
    }

    /** A second field initialiser, to show these run strictly top to bottom. */
    private String second = report("  3c. the second field initialiser ran");

    /**
     * @param label text supplied by the caller, proving the constructor body
     *              runs last
     */
    ConstructionOrder(String label) {
        report("  4.  constructor body ran, with argument \"" + label + "\"");
    }

    /**
     * Prints and returns, so it can be used as a field initialiser.
     *
     * @param message what to print
     * @return the message
     */
    private static String report(String message) {
        System.out.println(message);
        return message;
    }
}

/**
 * Every field is left unassigned, to prove that object fields are zeroed as
 * part of allocation. Local variables get no such treatment.
 */
class Defaults {

    int number;
    double decimal;
    boolean flag;
    char character;
    String reference;

    @Override
    public String toString() {
        return "int=" + number
                + ", double=" + decimal
                + ", boolean=" + flag
                + ", char=(code " + (int) character + ")"
                + ", String=" + reference;
    }
}

/**
 * Demonstrates encapsulation: the fields are private, and every way to change
 * them enforces the account's rules.
 */
class BankAccount {

    private final String accountNumber;
    private double balance;

    /**
     * @param accountNumber the account identifier; must not be null
     * @param initialBalance the opening balance; must not be negative
     * @throws IllegalArgumentException if the opening balance is negative
     */
    BankAccount(String accountNumber, double initialBalance) {
        this.accountNumber = Objects.requireNonNull(accountNumber, "accountNumber");
        if (initialBalance < 0) {
            throw new IllegalArgumentException("opening balance cannot be negative");
        }
        this.balance = initialBalance;
    }

    /**
     * Adds money to the account.
     *
     * @param amount how much to add; must be positive
     * @throws IllegalArgumentException if the amount is not positive
     */
    void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("deposit must be positive, got " + amount);
        }
        balance += amount;
    }

    /**
     * Removes money from the account.
     *
     * @param amount how much to remove; must be positive and available
     * @throws IllegalArgumentException if the amount is not positive
     * @throws IllegalStateException    if there is not enough money
     */
    void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("withdrawal must be positive, got " + amount);
        }
        if (amount > balance) {
            throw new IllegalStateException(
                    "insufficient funds: balance is " + balance + ", requested " + amount);
        }
        balance -= amount;
    }

    /**
     * A controlled READ. There is deliberately no setBalance() - the only ways
     * to change the balance are deposit() and withdraw(), both of which
     * enforce the rules.
     *
     * @return the current balance
     */
    double getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return "BankAccount[" + accountNumber + ", balance=" + balance + "]";
    }
}

/**
 * Deliberately has no toString(), to show what the default gives you.
 */
class WithoutToString {

    private final String name;
    private final int age;

    /**
     * @param name the person's name
     * @param age  the person's age
     */
    WithoutToString(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a `brake()` demonstration to Section 1 and confirm speed never goes
 *    below zero. Then try to break that rule from outside the class - and
 *    notice you cannot.
 *
 * 2. Write a Book class with private title, author and an int copiesAvailable.
 *    Add borrow() and returnCopy() that keep copiesAvailable between 0 and the
 *    total. Prove from main that no caller can push it out of range.
 *
 * 3. Add a public field to BankAccount and write the line of code that
 *    corrupts it. Then make it private again and try to write that line.
 *
 * 4. Remove the Objects.requireNonNull from Car's constructor. Create a
 *    `new Car(null)` and call toString() on it. Compare where the exception
 *    surfaces with where it surfaced before. This is what "fail fast" buys.
 *
 * 5. Predict the exact output order of ConstructionOrder before running it.
 *    Then add a second constructor that calls this(...) and predict again.
 *
 * 6. Create two Car objects with the same model. Print `a == b` and
 *    `a.equals(b)`. Both are false. Lesson 33 explains why, and how to fix it.
 * ============================================================================
 */
