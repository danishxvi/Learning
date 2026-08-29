/* ============================================================================
 * 40 - CUSTOM EXCEPTIONS AND EXCEPTION DESIGN
 * ----------------------------------------------------------------------------
 * Companion lesson: 40-custom-exceptions.md
 *
 * RUN IT:
 *     java Java/08-exception-handling/40-custom-exceptions.java
 *
 * Writing an exception class is trivial. Designing an exception HIERARCHY -
 * one that helps callers rather than burdening them - is the real skill.
 *
 * Section 3 is the difference between a useful custom exception and a
 * pointless one.
 * ============================================================================
 */

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

class CustomExceptions {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE FOUR CONSTRUCTORS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - WRITING ONE PROPERLY");
        System.out.println("=".repeat(74));

        System.out.println("  Throwable has FOUR constructors, and a well-behaved exception");
        System.out.println("  mirrors all of them:");
        System.out.println();
        System.out.println("      public MyException() { super(); }");
        System.out.println("      public MyException(String message) { super(message); }");
        System.out.println("      public MyException(String message, Throwable cause) { ... }");
        System.out.println("      public MyException(Throwable cause) { super(cause); }");
        System.out.println();

        System.out.println("  All four, on a real class:");
        for (OrderException example : List.of(
                new OrderException(),
                new OrderException("order 42 could not be processed"),
                new OrderException("order 42 failed", new SQLException("connection refused")),
                new OrderException(new SQLException("connection refused")))) {
            System.out.printf("    %-46s cause: %s%n",
                    example.getMessage() == null ? "(no message)" : example.getMessage(),
                    example.getCause() == null ? "none" : example.getCause().getMessage());
        }

        System.out.println();
        System.out.println("  THE (String, Throwable) ONE IS THE IMPORTANT ONE. Without it,");
        System.out.println("  callers CANNOT preserve the cause when wrapping - and your");
        System.out.println("  exception silently destroys stack traces.");
        System.out.println();
        System.out.println("  An exception class that omits it:");
        try {
            throwUsingCauselessException();
        } catch (CauselessException e) {
            System.out.println("    " + e.getMessage());
            System.out.println("    getCause() -> " + e.getCause()
                    + "   <- the SQLException is gone forever");
            System.out.println("    The caller WANTED to pass the cause and had no way to.");
        }


        /* ====================================================================
         * SECTION 2 - CHECKED OR UNCHECKED?
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE ONE QUESTION THAT DECIDES IT");
        System.out.println("=".repeat(74));

        System.out.println("  CAN A CALLER REALISTICALLY DO SOMETHING DIFFERENT BECAUSE OF THIS?");
        System.out.println();
        System.out.printf("    %-52s %s%n", "SITUATION", "CHOOSE");
        System.out.printf("    %-52s %s%n", "the caller can retry, fall back, or prompt", "CHECKED");
        System.out.printf("    %-52s %s%n", "it is a programming bug", "unchecked");
        System.out.printf("    %-52s %s%n", "the caller cannot meaningfully recover", "unchecked");
        System.out.printf("    %-52s %s%n", "it crosses a layer and would leak details", "unchecked + cause");

        System.out.println();
        System.out.println("  A CHECKED one, where the caller genuinely acts on it:");
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                String result = fetchWithRetryableFailure(attempt);
                System.out.println("    attempt " + attempt + " -> " + result);
            } catch (TransientFailureException e) {
                System.out.println("    attempt " + attempt + " -> " + e.getMessage()
                        + "; retryAfter=" + e.getRetryAfterSeconds() + "s, so we RETRY");
            }
        }

        System.out.println();
        System.out.println("  An UNCHECKED one, where there is nothing sensible to do:");
        try {
            loadConfiguration(null);
        } catch (ConfigurationException e) {
            System.out.println("    " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("    The application cannot start. Catching this to 'handle'");
            System.out.println("    it would only hide a fatal misconfiguration.");
        }

        System.out.println();
        System.out.println("  MODERN JAVA LEANS HEAVILY UNCHECKED. Spring, Hibernate and most");
        System.out.println("  frameworks converted their checked exceptions to unchecked,");
        System.out.println("  because checked ones leak through abstractions, force throws");
        System.out.println("  clauses up entire call stacks, and do not work with lambdas");
        System.out.println("  (lesson 39).");
        System.out.println();
        System.out.println("  But checked exceptions are NOT a mistake - they are a tool with");
        System.out.println("  a narrow, real use: a failure the caller is expected to handle");
        System.out.println("  RIGHT THERE, as the retry loop above does.");


        /* ====================================================================
         * SECTION 3 - CARRY DATA, NOT JUST A MESSAGE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - WHAT MAKES A CUSTOM EXCEPTION WORTH WRITING");
        System.out.println("=".repeat(74));

        System.out.println("  POINTLESS - a String would have done the same job:");
        try {
            withdrawPointlessly(new BigDecimal("500"), new BigDecimal("120"));
        } catch (PointlessOrderException e) {
            System.out.println("    " + e.getMessage());
            System.out.println("    To recover, the caller must PARSE THE MESSAGE STRING to");
            System.out.println("    find the numbers. That is a sign of a badly designed");
            System.out.println("    exception - and it breaks the moment you reword the text.");
        }

        System.out.println();
        System.out.println("  USEFUL - structured fields the caller can act on:");
        try {
            withdrawProperly("ACC-001", new BigDecimal("500"), new BigDecimal("120"));
        } catch (InsufficientFundsException e) {
            System.out.println("    " + e.getMessage());
            System.out.println();
            System.out.println("    ...and the caller can now do this:");
            System.out.println("      e.getAccountId()  -> " + e.getAccountId());
            System.out.println("      e.getRequested()  -> " + e.getRequested());
            System.out.println("      e.getAvailable()  -> " + e.getAvailable());
            System.out.println("      e.getShortfall()  -> " + e.getShortfall());
            System.out.println();

            BigDecimal overdraftLimit = new BigDecimal("500");
            if (e.getShortfall().compareTo(overdraftLimit) <= 0) {
                System.out.println("      shortfall " + e.getShortfall() + " is within the "
                        + overdraftLimit + " overdraft limit");
                System.out.println("      -> RECOVERED: applying an overdraft, no user prompt");
            }
        }

        System.out.println();
        System.out.println("  Note the message above is BUILT FROM THE FIELDS, so the text");
        System.out.println("  and the data can never disagree - see the constructor:");
        System.out.println();
        System.out.println("      super(\"account %s: requested %s but only %s available"
                + " (short by %s)\"");
        System.out.println("              .formatted(accountId, requested, available,");
        System.out.println("                         requested.subtract(available)));");


        /* ====================================================================
         * SECTION 4 - HIERARCHY DESIGN
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - ONE BASE EXCEPTION PER MODULE");
        System.out.println("=".repeat(74));

        System.out.println("      OrderException                  <- the base");
        System.out.println("        +-- OrderNotFoundException");
        System.out.println("        +-- InsufficientFundsException");
        System.out.println("        +-- PaymentDeclinedException");
        System.out.println();
        System.out.println("  This gives callers a genuine CHOICE OF GRANULARITY:");
        System.out.println();

        for (String scenario : new String[]{"missing", "funds", "declined"}) {
            System.out.println("    scenario \"" + scenario + "\":");
            handleAtFineGrain(scenario);
            handleAtCoarseGrain(scenario);
        }

        System.out.println();
        System.out.println("  DO NOT CREATE AN EXCEPTION PER ERROR MESSAGE. Twenty classes");
        System.out.println("  that differ only in their text is a code smell - use ONE class");
        System.out.println("  with a field, or an enum error code:");
        try {
            validatePayment("", new BigDecimal("-5"));
        } catch (PaymentDeclinedException e) {
            System.out.println("    " + e.getMessage());
            System.out.println("    e.getReason() -> " + e.getReason()
                    + "   (an enum, not twenty classes)");
            System.out.println("    e.getReason().isRetryable() -> " + e.getReason().isRetryable());
        }

        System.out.println();
        System.out.println("  NAMING - end with Exception, and name the PROBLEM, not the code");
        System.out.println("  that failed:");
        System.out.printf("    %-38s %s%n", "InsufficientFundsException", "good");
        System.out.printf("    %-38s %s%n", "WithdrawFailedException", "names the CODE, not the problem");
        System.out.printf("    %-38s %s%n", "OrderNotFoundException", "good");
        System.out.printf("    %-38s %s%n", "RepositoryException3", "tells the caller nothing");


        /* ====================================================================
         * SECTION 5 - TRANSLATING ACROSS LAYERS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - NEVER LET A LOW-LEVEL EXCEPTION ESCAPE ITS LAYER");
        System.out.println("=".repeat(74));

        System.out.println("  A repository that throws SQLException forces EVERY caller to");
        System.out.println("  know you use SQL. Change to MongoDB and the application breaks.");
        System.out.println();
        System.out.println("  THE LEAKY VERSION - the service layer must catch SQLException:");
        try {
            new LeakyUserService().displayName(42);
        } catch (SQLException e) {
            System.out.println("    the SERVICE layer had to catch " + e.getClass().getSimpleName());
            System.out.println("    ...and so does every layer above it. The database choice");
            System.out.println("    has leaked all the way to the UI.");
        }

        System.out.println();
        System.out.println("  THE TRANSLATED VERSION - the boundary absorbs it:");
        try {
            new CleanUserService().displayName(42);
        } catch (DataAccessException e) {
            System.out.println("    the service sees " + e.getClass().getSimpleName()
                    + ": " + e.getMessage());
            System.out.println("    and the cause is preserved for whoever debugs it:");
            for (Throwable current = e; current != null; current = current.getCause()) {
                System.out.println("      " + current.getClass().getSimpleName()
                        + ": " + current.getMessage());
            }
        }

        System.out.println();
        System.out.println("  Now the service depends on DataAccessException, not on JDBC.");
        System.out.println("  Spring's DataAccessException hierarchy exists for exactly this.");
        System.out.println();
        System.out.println("  TRANSLATE AT THE BOUNDARY - AND ALWAYS PASS THE CAUSE.");


        /* ====================================================================
         * SECTION 6 - USE THE JDK's STANDARD EXCEPTIONS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - DO NOT REINVENT THE STANDARD ONES");
        System.out.println("=".repeat(74));

        System.out.printf("    %-34s %s%n", "IllegalArgumentException", "a bad ARGUMENT");
        System.out.printf("    %-34s %s%n", "IllegalStateException", "a bad OBJECT STATE");
        System.out.printf("    %-34s %s%n", "NullPointerException", "an unexpected null");
        System.out.printf("    %-34s %s%n", "UnsupportedOperationException", "this operation is not available");
        System.out.printf("    %-34s %s%n", "IndexOutOfBoundsException", "an index outside the range");

        System.out.println();
        System.out.println("  Each one, thrown for the right reason:");

        record Attempt(String description, Runnable action) {}
        for (Attempt attempt : List.of(
                new Attempt("new Account(null, 100)",
                        () -> new Account(null, new BigDecimal("100"))),
                new Attempt("new Account(\"A\", -50)",
                        () -> new Account("A", new BigDecimal("-50"))),
                new Attempt("closedAccount.withdraw(10)",
                        () -> {
                            Account account = new Account("A", new BigDecimal("100"));
                            account.close();
                            account.withdraw(new BigDecimal("10"));
                        }))) {
            try {
                attempt.action().run();
                System.out.println("    " + pad(attempt.description()) + " -> no exception");
            } catch (RuntimeException e) {
                System.out.println("    " + pad(attempt.description()) + " -> "
                        + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("  DO NOT invent InvalidArgumentException or NullValueException.");
        System.out.println("  The JDK's exceptions are universally understood - a caller");
        System.out.println("  already knows what IllegalArgumentException means.");
        System.out.println();
        System.out.println("  Objects.requireNonNull throws NullPointerException WITH a");
        System.out.println("  message, which is why it beats a hand-written null check:");
        try {
            Objects.requireNonNull(null, "customerId must not be null");
        } catch (NullPointerException e) {
            System.out.println("    " + e.getMessage());
        }


        /* ====================================================================
         * SECTION 7 - MESSAGES THAT HELP
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WRITING THE MESSAGE");
        System.out.println("=".repeat(74));

        System.out.println("  A good message answers: WHAT failed, WITH WHAT VALUES, and");
        System.out.println("  what the caller might do about it.");
        System.out.println();
        System.out.println("    USELESS: \"invalid input\"");
        System.out.println("    USEFUL : \"age must be between 0 and 150, got 200\"");
        System.out.println();

        for (int age : new int[]{25, 200}) {
            try {
                validateAge(age);
                System.out.println("    validateAge(" + age + ") -> accepted");
            } catch (IllegalArgumentException e) {
                System.out.println("    validateAge(" + age + ") -> " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("  BUT NEVER INCLUDE SECRETS. Exception messages end up in logs,");
        System.out.println("  and logs end up in support tickets and screenshots (lesson 32):");
        try {
            authenticate("danish", "hunter2");
        } catch (AuthenticationException e) {
            System.out.println("    " + e.getMessage());
            System.out.println("    The username is there because it helps. The password is");
            System.out.println("    not, because it would leak into every log aggregator you");
            System.out.println("    have - and several you do not know about.");
        }


        /* ====================================================================
         * SECTION 8 - DOCUMENTING THEM
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - @throws IS NOT OPTIONAL");
        System.out.println("=".repeat(74));

        System.out.println("      /**");
        System.out.println("       * Withdraws money from this account.");
        System.out.println("       *");
        System.out.println("       * @param amount how much to withdraw; must be positive");
        System.out.println("       * @throws IllegalArgumentException   if amount is not positive");
        System.out.println("       * @throws InsufficientFundsException if the balance is too low");
        System.out.println("       * @throws IllegalStateException      if the account is closed");
        System.out.println("       */");
        System.out.println("      void withdraw(BigDecimal amount) { ... }");
        System.out.println();
        System.out.println("  DOCUMENT UNCHECKED EXCEPTIONS TOO. The compiler does not require");
        System.out.println("  it, but it is the ONLY way a caller learns what can happen - and");
        System.out.println("  it is the difference between an API that can be used correctly");
        System.out.println("  and one that cannot.");
        System.out.println();
        System.out.println("  Account.withdraw() in this file is documented that way. Run");
        System.out.println("  `javadoc` on it and the three exceptions appear in the HTML,");
        System.out.println("  which is where a caller will actually look.");


        /* ====================================================================
         * SECTION 9 - THE CHECKLIST
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 9 - THE CHECKLIST");
        System.out.println("=".repeat(74));

        System.out.println("    1. Extend RuntimeException unless the caller can act on it");
        System.out.println("    2. Provide all FOUR constructors, especially (String, Throwable)");
        System.out.println("    3. Carry STRUCTURED DATA, not just a message");
        System.out.println("    4. Build the message FROM those fields");
        System.out.println("    5. One base exception per module, specialised beneath it");
        System.out.println("    6. Name the PROBLEM; end with Exception");
        System.out.println("    7. Translate at layer boundaries, ALWAYS passing the cause");
        System.out.println("    8. Use the JDK's standard exceptions where they fit");
        System.out.println("    9. Name the failing value in the message; never a secret");
        System.out.println("   10. Document with @throws, unchecked ones included");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 40.");
        System.out.println("=".repeat(74));
    }

    /**
     * Pads a label so output lines up.
     *
     * @param text the label
     * @return the padded label
     */
    static String pad(String text) {
        return String.format("%-30s", text);
    }

    // ------------------------------------------------------------------------
    // SECTION 1 SUPPORT
    // ------------------------------------------------------------------------

    /** Tries to wrap a cause and cannot, because the class has no such constructor. */
    static void throwUsingCauselessException() {
        try {
            throw new SQLException("connection refused");
        } catch (SQLException e) {
            // We WANT to pass `e` as the cause. CauselessException offers no
            // constructor that accepts one, so the original is simply lost.
            throw new CauselessException("could not load the order");
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 2 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Fails on the first two attempts with a CHECKED exception the caller is
     * genuinely expected to handle by retrying.
     *
     * @param attempt which attempt this is
     * @return the fetched value
     * @throws TransientFailureException if this attempt failed but may succeed later
     */
    static String fetchWithRetryableFailure(int attempt) throws TransientFailureException {
        if (attempt < 3) {
            throw new TransientFailureException("upstream timed out", attempt * 2);
        }
        return "the data arrived";
    }

    /**
     * Throws an UNCHECKED exception, because a missing configuration file is
     * not something the caller can retry or work around.
     *
     * @param path where the configuration should be
     */
    static void loadConfiguration(String path) {
        if (path == null) {
            throw new ConfigurationException("no configuration path was supplied");
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 3 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * Throws an exception carrying only a message, so the caller has nothing
     * to work with but text.
     *
     * @param requested how much was asked for
     * @param available how much there is
     */
    static void withdrawPointlessly(BigDecimal requested, BigDecimal available) {
        if (requested.compareTo(available) > 0) {
            throw new PointlessOrderException(
                    "cannot withdraw " + requested + ", only " + available + " available");
        }
    }

    /**
     * Throws an exception carrying structured data the caller can act on.
     *
     * @param accountId the account
     * @param requested how much was asked for
     * @param available how much there is
     */
    static void withdrawProperly(String accountId, BigDecimal requested, BigDecimal available) {
        if (requested.compareTo(available) > 0) {
            throw new InsufficientFundsException(accountId, requested, available);
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 4 SUPPORT
    // ------------------------------------------------------------------------

    /** Catches one specific subtype. @param scenario which failure to provoke */
    static void handleAtFineGrain(String scenario) {
        try {
            failWith(scenario);
        } catch (InsufficientFundsException e) {
            System.out.println("      fine-grain catch (InsufficientFundsException) -> handled precisely");
        } catch (OrderException e) {
            System.out.println("      fine-grain catch -> fell through to OrderException ("
                    + e.getClass().getSimpleName() + ")");
        }
    }

    /** Catches the module's base type. @param scenario which failure to provoke */
    static void handleAtCoarseGrain(String scenario) {
        try {
            failWith(scenario);
        } catch (OrderException e) {
            System.out.println("      coarse-grain catch (OrderException)           -> caught "
                    + e.getClass().getSimpleName());
        }
    }

    /** @param scenario which failure to provoke */
    static void failWith(String scenario) {
        switch (scenario) {
            case "missing" -> throw new OrderNotFoundException(4242);
            case "funds" -> throw new InsufficientFundsException(
                    "ACC-001", new BigDecimal("500"), new BigDecimal("120"));
            case "declined" -> throw new PaymentDeclinedException(
                    DeclineReason.CARD_EXPIRED);
            default -> throw new OrderException("unknown scenario: " + scenario);
        }
    }

    /**
     * Uses ONE exception class with an enum reason, rather than one class per
     * failure message.
     *
     * @param cardNumber the card
     * @param amount     the amount
     */
    static void validatePayment(String cardNumber, BigDecimal amount) {
        if (cardNumber == null || cardNumber.isBlank()) {
            throw new PaymentDeclinedException(DeclineReason.INVALID_CARD);
        }
        if (amount.signum() <= 0) {
            throw new PaymentDeclinedException(DeclineReason.INVALID_AMOUNT);
        }
    }

    // ------------------------------------------------------------------------
    // SECTION 7 SUPPORT
    // ------------------------------------------------------------------------

    /**
     * @param age the age to check
     * @throws IllegalArgumentException naming the offending value
     */
    static void validateAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("age must be between 0 and 150, got " + age);
        }
    }

    /**
     * Names the username, which helps, and never the password, which would
     * leak into every log the application writes.
     *
     * @param username the user
     * @param password the password - deliberately absent from the message
     */
    static void authenticate(String username, String password) {
        throw new AuthenticationException(
                "authentication failed for user '" + username + "' (password not logged)");
    }
}

// ----------------------------------------------------------------------------
// SECTION 1 - THE FOUR CONSTRUCTORS, AND WHAT HAPPENS WITHOUT THEM
// ----------------------------------------------------------------------------

/** The module's base exception, with all four standard constructors. */
class OrderException extends RuntimeException {

    /** No message, no cause. */
    OrderException() {
        super();
    }

    /** @param message what went wrong */
    OrderException(String message) {
        super(message);
    }

    /**
     * THE IMPORTANT ONE. Without this, callers cannot preserve the cause.
     *
     * @param message what went wrong
     * @param cause   the underlying failure
     */
    OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    /** @param cause the underlying failure */
    OrderException(Throwable cause) {
        super(cause);
    }
}

/** Deliberately omits the (String, Throwable) constructor, and so destroys
 *  every stack trace that passes through it. */
class CauselessException extends RuntimeException {

    /** @param message what went wrong - and no way to say WHY */
    CauselessException(String message) {
        super(message);
    }
}

// ----------------------------------------------------------------------------
// SECTION 2 - CHECKED VERSUS UNCHECKED
// ----------------------------------------------------------------------------

/**
 * CHECKED, because the caller is genuinely expected to handle it right there -
 * and it carries the data needed to do so.
 */
class TransientFailureException extends Exception {

    private final int retryAfterSeconds;

    /**
     * @param message           what failed
     * @param retryAfterSeconds how long the caller should wait
     */
    TransientFailureException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /** @return how long to wait before retrying */
    int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

/** UNCHECKED, because a missing configuration is fatal and unrecoverable. */
class ConfigurationException extends RuntimeException {

    /** @param message what is misconfigured */
    ConfigurationException(String message) {
        super(message);
    }

    /** @param message what is misconfigured  @param cause the underlying failure */
    ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - MESSAGE-ONLY VERSUS STRUCTURED
// ----------------------------------------------------------------------------

/** Carries nothing but text, so callers must parse the message to recover. */
class PointlessOrderException extends OrderException {

    /** @param message the only thing the caller gets */
    PointlessOrderException(String message) {
        super(message);
    }
}

/**
 * Carries structured data, so a caller can decide programmatically what to do -
 * and the message is BUILT FROM those fields, so the two can never disagree.
 */
class InsufficientFundsException extends OrderException {

    private final String accountId;
    private final BigDecimal requested;
    private final BigDecimal available;

    /**
     * @param accountId the account that was short
     * @param requested how much was asked for
     * @param available how much there was
     */
    InsufficientFundsException(String accountId, BigDecimal requested, BigDecimal available) {
        super("account %s: requested %s but only %s available (short by %s)"
                .formatted(accountId, requested, available, requested.subtract(available)));
        this.accountId = accountId;
        this.requested = requested;
        this.available = available;
    }

    /** @return the account that was short */
    String getAccountId() {
        return accountId;
    }

    /** @return how much was asked for */
    BigDecimal getRequested() {
        return requested;
    }

    /** @return how much there was */
    BigDecimal getAvailable() {
        return available;
    }

    /** @return how much more was needed - the number a caller usually wants */
    BigDecimal getShortfall() {
        return requested.subtract(available);
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - THE HIERARCHY
// ----------------------------------------------------------------------------

/** A specific member of the OrderException family. */
class OrderNotFoundException extends OrderException {

    private final long orderId;

    /** @param orderId the order that was not found */
    OrderNotFoundException(long orderId) {
        super("no order with id " + orderId);
        this.orderId = orderId;
    }

    /** @return the missing order's id */
    long getOrderId() {
        return orderId;
    }
}

/** Why a payment was refused. An enum, rather than one class per reason. */
enum DeclineReason {

    INVALID_CARD("the card details are not valid", false),
    INVALID_AMOUNT("the amount is not valid", false),
    CARD_EXPIRED("the card has expired", false),
    INSUFFICIENT_FUNDS("the card has insufficient funds", false),
    ISSUER_UNAVAILABLE("the card issuer did not respond", true);

    private final String description;
    private final boolean retryable;

    /** @param description human-readable text  @param retryable whether retrying may help */
    DeclineReason(String description, boolean retryable) {
        this.description = description;
        this.retryable = retryable;
    }

    /** @return whether retrying the same payment might succeed */
    boolean isRetryable() {
        return retryable;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}

/** ONE class covering five decline reasons, via an enum field. */
class PaymentDeclinedException extends OrderException {

    private final DeclineReason reason;

    /** @param reason why the payment was declined */
    PaymentDeclinedException(DeclineReason reason) {
        super("payment declined: " + reason);
        this.reason = reason;
    }

    /** @return the structured reason, so callers need not parse the message */
    DeclineReason getReason() {
        return reason;
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - LAYER TRANSLATION
// ----------------------------------------------------------------------------

/** The application's own persistence-failure type, independent of JDBC. */
class DataAccessException extends RuntimeException {

    /** @param message what failed  @param cause the underlying failure */
    DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

/** Lets SQLException escape, forcing every caller to know about JDBC. */
class LeakyUserService {

    /**
     * @param id the user id
     * @return the user's display name
     * @throws SQLException leaking the persistence technology to every caller
     */
    String displayName(long id) throws SQLException {
        // No translation: the repository's SQLException passes straight through.
        throw new SQLException("ORA-00942: table or view does not exist");
    }
}

/** Translates at the boundary, so callers never see JDBC. */
class CleanUserService {

    /**
     * @param id the user id
     * @return the user's display name
     * @throws DataAccessException if the user cannot be loaded, cause preserved
     */
    String displayName(long id) {
        try {
            throw new SQLException("ORA-00942: table or view does not exist");
        } catch (SQLException e) {
            // Translate, and PASS THE CAUSE. Without that second argument the
            // original stack trace is destroyed.
            throw new DataAccessException("could not load user " + id, e);
        }
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 AND 8 - STANDARD EXCEPTIONS, AND DOCUMENTING THEM
// ----------------------------------------------------------------------------

/** Uses the JDK's standard exceptions for argument, state and null problems. */
class Account {

    private final String id;
    private BigDecimal balance;
    private boolean closed;

    /**
     * @param id      the account id; must not be null
     * @param balance the opening balance; must not be negative
     * @throws NullPointerException     if id is null
     * @throws IllegalArgumentException if the balance is negative
     */
    Account(String id, BigDecimal balance) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        if (balance.signum() < 0) {
            throw new IllegalArgumentException("balance cannot be negative: " + balance);
        }
        this.balance = balance;
    }

    /** Closes the account, after which no further withdrawals are permitted. */
    void close() {
        this.closed = true;
    }

    /**
     * Withdraws money from this account.
     *
     * <p>Note that all three exceptions are documented, including the unchecked
     * ones. The compiler does not require this, and it is the only way a caller
     * learns what can happen.
     *
     * @param amount how much to withdraw; must be positive
     * @throws IllegalArgumentException   if the amount is not positive
     * @throws IllegalStateException      if the account is closed
     * @throws InsufficientFundsException if the balance is too low
     */
    void withdraw(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive, got " + amount);
        }
        if (closed) {
            throw new IllegalStateException("account " + id + " is closed");
        }
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(id, amount, balance);
        }
        balance = balance.subtract(amount);
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - MESSAGES
// ----------------------------------------------------------------------------

/** Names the user but never the password. */
class AuthenticationException extends RuntimeException {

    /** @param message a message that must not contain credentials */
    AuthenticationException(String message) {
        super(message);
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add the (String, Throwable) constructor to CauselessException, then fix
 *    throwUsingCauselessException to pass the cause. Print both stack traces
 *    and count the lines of useful information you gained.
 *
 * 2. Add a RATE_LIMITED reason to DeclineReason, marked retryable. Notice you
 *    did not have to write a new exception class. Then write the caller-side
 *    code that retries only retryable declines.
 *
 * 3. Design an exception hierarchy for a file-upload feature: too large, wrong
 *    type, virus detected, storage full. Decide checked or unchecked for each,
 *    and justify every choice in one sentence.
 *
 * 4. Take LeakyUserService and translate its exception properly. Then change
 *    the "database" to throw a different low-level exception and confirm no
 *    caller had to change.
 *
 * 5. Write an exception whose message includes a credit-card number. Then find
 *    every place that message could end up. That list is why Section 7 exists.
 *
 * 6. Run `javadoc -d docs 40-custom-exceptions.java` and open Account.withdraw
 *    in the generated HTML. That is what your callers actually read.
 * ============================================================================
 */
