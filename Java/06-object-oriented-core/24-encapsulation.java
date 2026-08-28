/* ============================================================================
 * 24 - ENCAPSULATION
 * ----------------------------------------------------------------------------
 * Companion lesson: 24-encapsulation.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/24-encapsulation.java
 *
 * Encapsulation is usually taught as "make fields private and add getters and
 * setters". That is the MECHANISM, and stating it that way misses the point so
 * completely that people write a public setter for every private field - which
 * protects nothing.
 *
 * THE REAL TEST IS NOT "are the fields private?" but
 *   "CAN ANY CALLER PUT THIS OBJECT INTO AN INVALID STATE?"
 * Section 3 shows code that passes the first test and fails the second.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Encapsulation {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE PROBLEM: A PUBLIC FIELD HAS NO DEFENCE
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WHY private EXISTS");
        System.out.println("=".repeat(74));

        ExposedAccount exposed = new ExposedAccount(5000);
        System.out.println("  A class with a public field:");
        System.out.println("    starting balance -> " + exposed.balance);

        // Every one of these is legal, and every one breaks the account.
        exposed.balance = -5000;
        System.out.println("    exposed.balance = -5000;      -> " + exposed.balance);
        exposed.balance += 1_000_000;
        System.out.println("    exposed.balance += 1000000;   -> " + exposed.balance);
        exposed.balance = Double.NaN;
        System.out.println("    exposed.balance = Double.NaN; -> " + exposed.balance);

        System.out.println();
        System.out.println("  With a public field, EVERY LINE IN THE PROGRAM is a place the");
        System.out.println("  invariant can break. Finding the bug means searching the whole");
        System.out.println("  codebase.");

        System.out.println();
        System.out.println("  The encapsulated version:");
        GuardedAccount guarded = new GuardedAccount(5000);
        System.out.println("    starting balance -> " + guarded.getBalance());

        guarded.deposit(2500);
        System.out.println("    deposit(2500)    -> " + guarded.getBalance());
        guarded.withdraw(1000);
        System.out.println("    withdraw(1000)   -> " + guarded.getBalance());

        System.out.println();
        System.out.println("  And every attempt to break it is refused, by the object itself:");
        tryIt("deposit(-100)", () -> guarded.deposit(-100));
        tryIt("deposit(0)", () -> guarded.deposit(0));
        tryIt("withdraw(999999)", () -> guarded.withdraw(999_999));
        tryIt("deposit(Double.NaN)", () -> guarded.deposit(Double.NaN));

        System.out.println();
        System.out.println("  There are now exactly TWO places the balance can change, and");
        System.out.println("  both enforce the rules. If the balance is ever wrong, the bug");
        System.out.println("  is in one of two methods.");
        System.out.println();
        System.out.println("  That reduction - from 'anywhere' to 'here' - IS the value of");
        System.out.println("  encapsulation. Not the keyword. The reduction.");


        /* ====================================================================
         * SECTION 2 - THE ACCESS MODIFIERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE FOUR ACCESS LEVELS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-18s %-12s %-14s %-16s %s%n",
                "MODIFIER", "same class", "same package", "subclass (other)", "anywhere");
        System.out.printf("    %-18s %-12s %-14s %-16s %s%n",
                "private", "yes", "no", "no", "no");
        System.out.printf("    %-18s %-12s %-14s %-16s %s%n",
                "(none)", "yes", "yes", "no", "no");
        System.out.printf("    %-18s %-12s %-14s %-16s %s%n",
                "protected", "yes", "yes", "yes", "no");
        System.out.printf("    %-18s %-12s %-14s %-16s %s%n",
                "public", "yes", "yes", "yes", "yes");

        System.out.println();
        System.out.println("  DEFAULT TO private. Widen only with a reason, and know what");
        System.out.println("  each widening commits you to:");
        System.out.println();
        System.out.println("    public    - a PERMANENT promise. Once published you cannot");
        System.out.println("                narrow it without breaking callers.");
        System.out.println("    protected - ALSO public in practice: anyone can subclass your");
        System.out.println("                class and read it. Same permanent commitment.");
        System.out.println("    (none)    - package-private. Underused, and often the right");
        System.out.println("                answer for classes that collaborate closely.");
        System.out.println();
        System.out.println("  Lesson 30 covers packages and modifiers in full.");


        /* ====================================================================
         * SECTION 3 - THE GETTER/SETTER MISUNDERSTANDING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - A SETTER WITH NO VALIDATION PROTECTS NOTHING");
        System.out.println("=".repeat(74));

        System.out.println("  This class LOOKS encapsulated - private field, getter, setter:");
        System.out.println();
        System.out.println("    class Person {");
        System.out.println("        private String name;");
        System.out.println("        public String getName()          { return name; }");
        System.out.println("        public void setName(String name) { this.name = name; }");
        System.out.println("    }");
        System.out.println();

        PretendEncapsulated pretend = new PretendEncapsulated("Danish");
        System.out.println("  But watch:");
        pretend.setName(null);
        System.out.println("    setName(null)                 -> " + pretend.getName());
        pretend.setName("");
        System.out.println("    setName(\"\")                   -> \"" + pretend.getName() + "\"");
        pretend.setName("   ");
        System.out.println("    setName(\"   \")                -> \"" + pretend.getName() + "\"");

        System.out.println();
        System.out.println("  Six lines written to achieve exactly what `public String name;`");
        System.out.println("  achieves - plus the ILLUSION of safety.");
        System.out.println();
        System.out.println("  A SETTER WITH NO VALIDATION IS A PUBLIC FIELD WITH EXTRA STEPS.");

        System.out.println();
        System.out.println("  FIX 1 - ask whether the setter is needed at all. Most fields");
        System.out.println("  are set once and never change. Make them final, delete it:");
        ImmutablePerson immutable = new ImmutablePerson("Danish");
        System.out.println("    new ImmutablePerson(\"Danish\") -> " + immutable);
        tryIt("new ImmutablePerson(null)", () -> new ImmutablePerson(null));
        tryIt("new ImmutablePerson(\"  \")", () -> new ImmutablePerson("  "));
        System.out.println("    There is no setter to misuse, and the constructor refuses");
        System.out.println("    to build an invalid object in the first place.");

        System.out.println();
        System.out.println("  FIX 2 - if it genuinely must change, validate:");
        ValidatingPerson validating = new ValidatingPerson("Danish", "d@example.com");
        System.out.println("    " + validating);
        tryIt("setEmail(\"not-an-email\")", () -> validating.setEmail("not-an-email"));
        tryIt("setEmail(null)", () -> validating.setEmail(null));
        validating.setEmail("danish@example.com");
        System.out.println("    setEmail(\"danish@example.com\") -> " + validating);


        /* ====================================================================
         * SECTION 4 - EXPOSE OPERATIONS, NOT FIELDS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE ANAEMIC MODEL");
        System.out.println("=".repeat(74));

        System.out.println("  ANAEMIC - the CALLER does the thinking:");
        System.out.println("      account.setBalance(account.getBalance() - 100);");
        System.out.println();
        System.out.println("    The withdrawal DECISION was made outside the object, so the");
        System.out.println("    object cannot check it, log it, fire an event about it, or");
        System.out.println("    make it thread-safe. By the time setBalance runs it is too");
        System.out.println("    late to refuse.");
        System.out.println();
        System.out.println("  ENCAPSULATED - the OBJECT does the thinking:");
        System.out.println("      account.withdraw(100);");
        System.out.println();

        AuditedAccount audited = new AuditedAccount(1000);
        audited.withdraw(300);
        audited.deposit(50);
        tryIt("withdraw(5000)", () -> audited.withdraw(5000));
        System.out.println();
        System.out.println("    The object kept its own audit log, because it was the one");
        System.out.println("    making the decisions:");
        for (String entry : audited.getAuditLog()) {
            System.out.println("      " + entry);
        }
        System.out.println();
        System.out.println("    None of that is possible with a setter. This is the");
        System.out.println("    difference between an ANAEMIC DOMAIN MODEL - data holders");
        System.out.println("    plus separate 'service' classes holding all the logic - and");
        System.out.println("    a real object model. The anaemic style is very common and");
        System.out.println("    worth learning to recognise.");


        /* ====================================================================
         * SECTION 5 - THE LEAKING-REFERENCE BUG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - private PROTECTS THE FIELD, NOT THE OBJECT");
        System.out.println("=".repeat(74));

        LeakyTeam leaky = new LeakyTeam("Platform");
        leaky.addMember("Danish");
        leaky.addMember("Aisha");
        leaky.addMember("Rahul");

        System.out.println("  A team with a PRIVATE list and a getter that returns it:");
        System.out.println("    team.getMembers() -> " + leaky.getMembers());

        // The field was private. The object it pointed at was not.
        leaky.getMembers().clear();

        System.out.println("    team.getMembers().clear();");
        System.out.println("    team.getMembers() -> " + leaky.getMembers()
                + "   <- the team was wiped from outside");
        System.out.println();
        System.out.println("  `private` protected the FIELD - the variable holding the");
        System.out.println("  reference. Handing out that reference gave away everything it");
        System.out.println("  was guarding. This is the encapsulation failure people miss most.");

        System.out.println();
        System.out.println("  THE THREE FIXES:");

        SafeTeam safeTeam = new SafeTeam("Platform");
        safeTeam.addMember("Danish");
        safeTeam.addMember("Aisha");

        System.out.println();
        System.out.println("  1. UNMODIFIABLE VIEW - cheap, but reflects later changes");
        List<String> view = safeTeam.getMembersAsView();
        System.out.println("     view -> " + view);
        tryIt("view.clear()", view::clear);
        safeTeam.addMember("Rahul");
        System.out.println("     after the team adds Rahul, the view shows -> " + view);
        System.out.println("     (a VIEW is live: it keeps tracking the underlying list)");

        System.out.println();
        System.out.println("  2. DEFENSIVE COPY - an independent snapshot");
        List<String> copy = safeTeam.getMembersAsCopy();
        copy.add("An Impostor");     // allowed - but only affects the copy
        System.out.println("     copy after adding to it -> " + copy);
        System.out.println("     the real team is still  -> " + safeTeam.getMembersAsView());

        System.out.println();
        System.out.println("  3. IMMUTABLE COPY (Java 10+) - usually the best answer");
        List<String> immutableCopy = safeTeam.getMembers();
        System.out.println("     List.copyOf(...) -> " + immutableCopy);
        tryIt("immutableCopy.add(\"X\")", () -> immutableCopy.add("X"));
        System.out.println("     independent AND unmodifiable");

        System.out.println();
        System.out.println("  THE SAME APPLIES ON THE WAY IN:");
        List<String> callerList = new ArrayList<>(List.of("Danish", "Aisha"));
        SafeTeam fromList = new SafeTeam("Imported", callerList);
        callerList.add("Injected Later");
        System.out.println("    caller's list after the constructor -> " + callerList);
        System.out.println("    the team's members                  -> " + fromList.getMembers());
        System.out.println("    The constructor COPIED, so the caller cannot reach in later.");
        System.out.println();
        System.out.println("  This applies to ANY mutable object: List, Map, arrays, Date,");
        System.out.println("  StringBuilder, and your own mutable classes. Lesson 38 covers");
        System.out.println("  defensive copying in depth.");


        /* ====================================================================
         * SECTION 6 - ENCAPSULATING DECISIONS, NOT JUST DATA
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - HIDING IMPLEMENTATION DECISIONS");
        System.out.println("=".repeat(74));

        Temperature boiling = Temperature.ofCelsius(100);
        System.out.println("  One stored number, three readings:");
        System.out.println("    getCelsius()    = " + boiling.getCelsius());
        System.out.println("    getFahrenheit() = " + boiling.getFahrenheit());
        System.out.println("    getKelvin()     = " + boiling.getKelvin());
        System.out.println();
        System.out.println("  Callers cannot tell which unit is stored internally - and");
        System.out.println("  should not care. Switch the field to Kelvin tomorrow and NO");
        System.out.println("  caller changes.");
        System.out.println();
        System.out.println("  That freedom to change your mind later IS what encapsulation");
        System.out.println("  buys you. It is not about hiding data; it is about hiding");
        System.out.println("  DECISIONS, so they stay reversible.");

        System.out.println();
        System.out.println("  THREE RELATED TERMS, OFTEN CONFUSED:");
        System.out.printf("    %-20s %s%n", "ENCAPSULATION",
                "bundling data with behaviour, controlling access");
        System.out.printf("    %-20s %s%n", "INFORMATION HIDING",
                "hiding HOW something is done");
        System.out.printf("    %-20s %s%n", "ABSTRACTION",
                "exposing WHAT it does, not how");
        System.out.println();
        System.out.println("    Encapsulation is the MECHANISM, information hiding is the");
        System.out.println("    GOAL, abstraction is the DESIGN PRINCIPLE.");


        /* ====================================================================
         * SECTION 7 - THE PRACTICAL RULES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE RULES, AND WHEN TO RELAX THEM");
        System.out.println("=".repeat(74));

        System.out.println("    1. Fields private, and final wherever possible.");
        System.out.println("    2. No setter unless something genuinely must change.");
        System.out.println("    3. Validate in the constructor - never exist invalid.");
        System.out.println("    4. Never return an internal mutable object.");
        System.out.println("    5. Copy mutable arguments on the way IN too.");
        System.out.println("    6. Expose OPERATIONS, not fields.");
        System.out.println("    7. Keep the public surface small - every public member is");
        System.out.println("       a promise you must keep forever.");
        System.out.println("    8. Use records for pure data (lesson 36) - immutable by");
        System.out.println("       construction, so most of this becomes automatic.");

        System.out.println();
        System.out.println("  WHEN IT IS FINE TO RELAX:");
        System.out.println();
        System.out.println("    A public constant is fine IF it is genuinely immutable:");
        System.out.println("      public static final int MAX_RETRIES = 3;        // fine");
        System.out.println("      public static final List<String> NAMES =");
        System.out.println("              new ArrayList<>();                      // NOT fine");
        System.out.println("    The second is a public MUTABLE GLOBAL wearing `final`.");
        System.out.println("    `final` stops REASSIGNMENT, never MUTATION (lesson 31).");
        System.out.println();
        System.out.println("    Watch it happen:");
        System.out.println("      Constants.MUTABLE_BY_ACCIDENT -> " + Constants.MUTABLE_BY_ACCIDENT);
        Constants.MUTABLE_BY_ACCIDENT.add("anyone can do this");
        System.out.println("      after .add(...)               -> " + Constants.MUTABLE_BY_ACCIDENT);
        System.out.println("      Constants.PROPERLY_IMMUTABLE  -> " + Constants.PROPERLY_IMMUTABLE);
        tryIt("PROPERLY_IMMUTABLE.add(\"x\")", () -> Constants.PROPERLY_IMMUTABLE.add("x"));

        System.out.println();
        System.out.println("    A record exposes its components by design - that is the");
        System.out.println("    point of a record, and they are immutable anyway.");
        System.out.println();
        System.out.println("    Package-private is genuinely right for classes that");
        System.out.println("    collaborate closely and are not part of your public API.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 24.");
        System.out.println("=".repeat(74));
    }

    /**
     * Runs an action and reports whether it succeeded or was refused, so the
     * lesson can show many rejected operations without a wall of try/catch.
     *
     * @param description what is being attempted
     * @param action      the attempt
     */
    static void tryIt(String description, Runnable action) {
        try {
            action.run();
            System.out.println("    " + pad(description) + " -> allowed");
        } catch (RuntimeException e) {
            System.out.println("    " + pad(description) + " -> REFUSED: "
                    + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : ": " + e.getMessage()));
        }
    }

    /**
     * Pads a label to a fixed width so the output lines up.
     *
     * @param text the label
     * @return the padded label
     */
    static String pad(String text) {
        return String.format("%-30s", text);
    }
}

// ----------------------------------------------------------------------------
// SECTION 1
// ----------------------------------------------------------------------------

/** No protection whatsoever. Every field is a public global. */
class ExposedAccount {

    public double balance;

    /** @param balance the opening balance */
    ExposedAccount(double balance) {
        this.balance = balance;
    }
}

/** The same account, with the rules enforced by the object itself. */
class GuardedAccount {

    private double balance;

    /**
     * @param openingBalance the opening balance; must not be negative
     * @throws IllegalArgumentException if the opening balance is negative
     */
    GuardedAccount(double openingBalance) {
        if (openingBalance < 0) {
            throw new IllegalArgumentException("opening balance cannot be negative");
        }
        this.balance = openingBalance;
    }

    /**
     * @param amount how much to add; must be positive and finite
     * @throws IllegalArgumentException if the amount is not a positive number
     */
    void deposit(double amount) {
        requireValidAmount(amount);
        balance += amount;
    }

    /**
     * @param amount how much to remove; must be positive, finite and available
     * @throws IllegalArgumentException if the amount is not a positive number
     * @throws IllegalStateException    if there are insufficient funds
     */
    void withdraw(double amount) {
        requireValidAmount(amount);
        if (amount > balance) {
            throw new IllegalStateException(
                    "insufficient funds: have " + balance + ", requested " + amount);
        }
        balance -= amount;
    }

    /** @return the current balance */
    double getBalance() {
        return balance;
    }

    /**
     * The shared validation. Private, so no subclass can weaken it and no
     * caller can bypass it.
     *
     * @param amount the amount to check
     * @throws IllegalArgumentException if it is not a positive finite number
     */
    private void requireValidAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            throw new IllegalArgumentException("amount must be a real number, got " + amount);
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive, got " + amount);
        }
    }
}

// ----------------------------------------------------------------------------
// SECTION 3
// ----------------------------------------------------------------------------

/** Private field, public getter, public setter - and no protection at all. */
class PretendEncapsulated {

    private String name;

    /** @param name the initial name */
    PretendEncapsulated(String name) {
        this.name = name;
    }

    /** @return the name */
    String getName() {
        return name;
    }

    /** Accepts absolutely anything. @param name the new name */
    void setName(String name) {
        this.name = name;
    }
}

/** No setter at all: the field is final and validated once, at construction. */
class ImmutablePerson {

    private final String name;

    /**
     * @param name the person's name; must not be null or blank
     * @throws IllegalArgumentException if the name is null or blank
     */
    ImmutablePerson(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name;
    }

    /** @return the name */
    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ImmutablePerson[" + name + "]";
    }
}

/** A field that genuinely changes - so the setter validates. */
class ValidatingPerson {

    private final String name;
    private String email;

    /**
     * @param name  the person's name
     * @param email the initial email
     */
    ValidatingPerson(String name, String email) {
        this.name = Objects.requireNonNull(name, "name");
        setEmail(email);      // reuse the validation rather than duplicating it
    }

    /**
     * @param email the new email; must contain '@' and a dot after it
     * @throws IllegalArgumentException if the email is not plausible
     */
    void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.substring(email.indexOf('@')).contains(".")) {
            throw new IllegalArgumentException("invalid email: " + email);
        }
        this.email = email;
    }

    @Override
    public String toString() {
        return "ValidatingPerson[" + name + ", " + email + "]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 4
// ----------------------------------------------------------------------------

/**
 * Exposes operations rather than fields, which is what lets it keep an audit
 * log. A setter could never do this, because the decision would already have
 * been made outside the object.
 */
class AuditedAccount {

    private double balance;
    private final List<String> auditLog = new ArrayList<>();

    /** @param openingBalance the opening balance */
    AuditedAccount(double openingBalance) {
        this.balance = openingBalance;
        auditLog.add("opened with " + openingBalance);
    }

    /** @param amount how much to add */
    void deposit(double amount) {
        if (amount <= 0) {
            auditLog.add("REJECTED deposit of " + amount + " (not positive)");
            throw new IllegalArgumentException("deposit must be positive");
        }
        balance += amount;
        auditLog.add("deposited " + amount + ", balance now " + balance);
    }

    /** @param amount how much to remove */
    void withdraw(double amount) {
        if (amount > balance) {
            auditLog.add("REJECTED withdrawal of " + amount + " (insufficient funds)");
            throw new IllegalStateException("insufficient funds");
        }
        balance -= amount;
        auditLog.add("withdrew " + amount + ", balance now " + balance);
    }

    /** @return an unmodifiable view of the audit log */
    List<String> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }
}

// ----------------------------------------------------------------------------
// SECTION 5
// ----------------------------------------------------------------------------

/** Private field, public getter that hands out the internal list. */
class LeakyTeam {

    private final String name;
    private final List<String> members = new ArrayList<>();

    /** @param name the team name */
    LeakyTeam(String name) {
        this.name = name;
    }

    /** @param member someone to add */
    void addMember(String member) {
        members.add(member);
    }

    /** THE LEAK: this hands the caller the object `private` was protecting.
     *  @return the internal list itself */
    List<String> getMembers() {
        return members;
    }
}

/** The same team, with every route in and out defended. */
class SafeTeam {

    private final String name;
    private final List<String> members;

    /** @param name the team name */
    SafeTeam(String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    /**
     * Copies the incoming list, so the caller cannot mutate our state later
     * through the reference they still hold.
     *
     * @param name           the team name
     * @param initialMembers the starting members; copied, not stored
     */
    SafeTeam(String name, List<String> initialMembers) {
        this.name = name;
        this.members = new ArrayList<>(initialMembers);   // DEFENSIVE COPY IN
    }

    /** @param member someone to add */
    void addMember(String member) {
        members.add(member);
    }

    /**
     * FIX 3, and usually the best: an independent, unmodifiable copy.
     *
     * @return the members, safe to hand anywhere
     */
    List<String> getMembers() {
        return List.copyOf(members);
    }

    /**
     * FIX 1: an unmodifiable VIEW. Cheap - no copying - but it stays LIVE, so
     * it reflects later changes to the underlying list.
     *
     * @return an unmodifiable view of the members
     */
    List<String> getMembersAsView() {
        return Collections.unmodifiableList(members);
    }

    /**
     * FIX 2: a mutable but independent snapshot. The caller may change their
     * copy freely; it does not affect us.
     *
     * @return a fresh list containing the members
     */
    List<String> getMembersAsCopy() {
        return new ArrayList<>(members);
    }
}

// ----------------------------------------------------------------------------
// SECTION 6
// ----------------------------------------------------------------------------

/**
 * Stores one number and exposes three readings. Callers cannot tell - and
 * should not care - which unit is stored, which is exactly what makes the
 * decision reversible.
 */
class Temperature {

    /** An internal implementation decision, hidden from every caller. */
    private final double celsius;

    private Temperature(double celsius) {
        this.celsius = celsius;
    }

    /** @param celsius the temperature in Celsius  @return a Temperature */
    static Temperature ofCelsius(double celsius) {
        return new Temperature(celsius);
    }

    /** @return the temperature in Celsius */
    double getCelsius() {
        return celsius;
    }

    /** @return the temperature in Fahrenheit, computed on demand */
    double getFahrenheit() {
        return celsius * 9 / 5 + 32;
    }

    /** @return the temperature in Kelvin, computed on demand */
    double getKelvin() {
        return celsius + 273.15;
    }
}

// ----------------------------------------------------------------------------
// SECTION 7
// ----------------------------------------------------------------------------

/** Shows that `final` on a collection stops reassignment, never mutation. */
class Constants {

    /** Genuinely immutable - an int cannot be changed. */
    public static final int MAX_RETRIES = 3;

    /**
     * A public MUTABLE GLOBAL wearing `final`. The reference cannot be
     * reassigned; the list's CONTENTS can be changed by anyone, from anywhere.
     */
    public static final List<String> MUTABLE_BY_ACCIDENT = new ArrayList<>(List.of("initial"));

    /** The fix: an immutable list, so there is nothing to mutate. */
    public static final List<String> PROPERLY_IMMUTABLE = List.of("safe", "values");
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a `setBalance(double)` to GuardedAccount. Now write the line that
 *    breaks the account. Then delete the setter and try again.
 *
 * 2. LeakyTeam has a second leak you have not seen yet: add a constructor
 *    taking a List and storing it directly. Then mutate the caller's list and
 *    watch the team change. Fix it.
 *
 * 3. Write an Inventory class holding a Map<String, Integer> of item counts.
 *    Expose addStock, removeStock and a read-only view. Prove no caller can
 *    make a count negative.
 *
 * 4. Change Temperature to store KELVIN internally instead of Celsius. Count
 *    how many lines outside the class you had to change. (It should be zero.)
 *
 * 5. Explain why `Collections.unmodifiableList(members)` is not the same as
 *    `List.copyOf(members)`. Write a five-line program that shows the
 *    difference.
 *
 * 6. Find a class in your own code with a getter returning a List, Map or
 *    array. Write the line that corrupts it from outside. Then fix it.
 * ============================================================================
 */
