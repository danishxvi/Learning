/* ============================================================================
 * 23 - THE this KEYWORD
 * ----------------------------------------------------------------------------
 * Companion lesson: 23-this-keyword.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/23-this-keyword.java
 *
 * `this` is a reference to THE OBJECT WHOSE METHOD IS CURRENTLY RUNNING.
 * It is small, and it does five distinct jobs. Section 2 covers the one you
 * will use every day; Section 4 covers the one that causes real bugs.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

class ThisKeyword {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - WHAT this IS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - this IS THE CURRENT OBJECT");
        System.out.println("=".repeat(74));

        Counter first = new Counter("first");
        Counter second = new Counter("second");

        first.increment();
        first.increment();
        second.increment();

        System.out.println("  Two objects, each with its own state:");
        System.out.println("    " + first);
        System.out.println("    " + second);
        System.out.println();
        System.out.println("  Inside increment(), `this` was whichever Counter you called");
        System.out.println("  it on. Proof - each object reports its own identity:");
        first.reportIdentity();
        second.reportIdentity();

        System.out.println();
        System.out.println("  PROPERTIES OF this:");
        System.out.println("    IMPLICIT      - count++ and this.count++ are identical");
        System.out.println("    NEVER null    - you cannot be inside a method of an object");
        System.out.println("                    that does not exist");
        System.out.println("    NOT in static - a static method belongs to the CLASS, so no");
        System.out.println("                    particular object is running. Compile error.");
        System.out.println("    EFFECTIVELY final - you cannot assign to it");


        /* ====================================================================
         * SECTION 2 - JOB 1: DISAMBIGUATING A SHADOWED FIELD
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE JOB YOU WILL USE EVERY DAY");
        System.out.println("=".repeat(74));

        System.out.println("  CORRECT - `this.name` is the FIELD, `name` is the PARAMETER:");
        System.out.println("      Person(String name) { this.name = name; }");
        System.out.println("    new PersonCorrect(\"Danish\") -> " + new PersonCorrect("Danish"));

        System.out.println();
        System.out.println("  BROKEN - the parameter SHADOWS the field:");
        System.out.println("      Person(String name) { name = name; }");
        System.out.println("    new PersonBroken(\"Danish\")  -> " + new PersonBroken("Danish"));
        System.out.println();
        System.out.println("    `name = name;` assigned the parameter to ITSELF. The field");
        System.out.println("    was never touched, so it is still null.");
        System.out.println();
        System.out.println("    This COMPILES WITHOUT ERROR. Some IDEs warn ('assignment to");
        System.out.println("    itself'), javac does not by default. A classic silent bug.");

        System.out.println();
        System.out.println("  Turn the warning on and javac does tell you:");
        System.out.println("      javac -Xlint:all YourFile.java");

        System.out.println();
        System.out.println("  WHY NOT JUST RENAME THE PARAMETER to personName?");
        System.out.println("    You can. But matching the field name is the convention,");
        System.out.println("    because the parameter name appears in Javadoc and IDE hints,");
        System.out.println("    and `name` is the honest name for it. `this.` costs two");
        System.out.println("    characters and states the intent exactly.");


        /* ====================================================================
         * SECTION 3 - JOB 2: this(...) CONSTRUCTOR DELEGATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - this(...) IS A DIFFERENT FEATURE ENTIRELY");
        System.out.println("=".repeat(74));

        System.out.println("    new Point()        -> " + new Point());
        System.out.println("    new Point(5)       -> " + new Point(5));
        System.out.println("    new Point(3, 7)    -> " + new Point(3, 7));
        System.out.println();
        System.out.println("  this(...) WITH PARENTHESES delegates to another constructor.");
        System.out.println("  this.field           accesses a member.");
        System.out.println("  They share a keyword and nothing else. Lesson 22 covers");
        System.out.println("  delegation properly.");


        /* ====================================================================
         * SECTION 4 - JOB 3: PASSING THE CURRENT OBJECT
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - PASSING this, AND THE HAZARD");
        System.out.println("=".repeat(74));

        EventBus bus = new EventBus();

        System.out.println("  The SAFE order - construct first, register afterwards:");
        SafeWidget safe = new SafeWidget("save-button");
        bus.register(safe);
        bus.publish("click");

        System.out.println();
        System.out.println("  The DANGEROUS version - the constructor registers `this`");
        System.out.println("  BEFORE it has finished setting its own fields:");
        EventBus earlyBus = new EventBus();
        new UnsafeWidget(earlyBus, "cancel-button");
        earlyBus.publish("click");

        System.out.println();
        System.out.println("  Look at the name the bus saw. The object was not finished:");
        System.out.println("  the constructor published `this` and only then assigned the");
        System.out.println("  field, so the listener observed a half-built object.");
        System.out.println();
        System.out.println("  With another THREAD involved this gets worse: even final");
        System.out.println("  fields are not guaranteed visible until the constructor");
        System.out.println("  returns. This is the same family of bug as calling an");
        System.out.println("  overridable method from a constructor (lesson 22).");
        System.out.println();
        System.out.println("  RULE: never let `this` escape a constructor. Register after");
        System.out.println("  construction, or use a static factory that does both in order.");


        /* ====================================================================
         * SECTION 5 - JOB 4: RETURNING this FOR CHAINING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - RETURNING this MAKES FLUENT APIs");
        System.out.println("=".repeat(74));

        String query = new QueryBuilder()
                .select("id", "name", "email")
                .from("users")
                .where("country = 'India'")
                .where("active = true")
                .orderBy("name")
                .build();

        System.out.println("  A fluent query builder:");
        System.out.println();
        System.out.println("    " + query);
        System.out.println();
        System.out.println("  Each method ends with `return this;`, so the next call has");
        System.out.println("  something to attach to. That is the whole trick.");
        System.out.println();
        System.out.println("  StringBuilder does exactly this, which is why append chains:");
        String chained = new StringBuilder()
                .append("a").append("b").append("c").toString();
        System.out.println("    new StringBuilder().append(\"a\").append(\"b\").append(\"c\") -> "
                + chained);


        /* ====================================================================
         * SECTION 6 - JOB 5: QUALIFIED this FROM AN INNER CLASS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - Outer.this");
        System.out.println("=".repeat(74));

        Outer outer = new Outer("I am the OUTER object");
        Outer.Inner inner = outer.new Inner("I am the INNER object");
        inner.showBoth();

        System.out.println();
        System.out.println("  `Outer.this` is QUALIFIED this. It exists only for inner");
        System.out.println("  (non-static nested) classes, and it is how an inner object");
        System.out.println("  reaches the object that created it.");
        System.out.println();
        System.out.println("  THE MEMORY-LEAK CONSEQUENCE:");
        System.out.println("    A non-static inner class holds a HIDDEN reference to its");
        System.out.println("    outer instance. If the inner object outlives the outer one -");
        System.out.println("    stored in a static registry, say - the outer object can");
        System.out.println("    NEVER be garbage collected.");
        System.out.println();
        System.out.println("    Making the nested class `static` removes that reference,");
        System.out.println("    and is the right default. Lesson 34 covers this fully.");


        /* ====================================================================
         * SECTION 7 - this IN LAMBDAS VS ANONYMOUS CLASSES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - THE DIFFERENCE THAT SURPRISES PEOPLE");
        System.out.println("=".repeat(74));

        new ScopeDemonstration().compare();

        System.out.println();
        System.out.println("  An ANONYMOUS CLASS is a real class with its own instance, so");
        System.out.println("  `this` is THAT instance.");
        System.out.println();
        System.out.println("  A LAMBDA is not a class. It does not introduce a new scope for");
        System.out.println("  `this`, so `this` still means whatever it meant in the");
        System.out.println("  surrounding code. Lambdas are LEXICALLY SCOPED for `this`.");
        System.out.println();
        System.out.println("  This bites when converting anonymous classes to lambdas: the");
        System.out.println("  meaning of `this` silently changes. Lessons 34 and 51.");


        /* ====================================================================
         * SECTION 8 - WHEN TO WRITE this. EXPLICITLY
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - STYLE");
        System.out.println("=".repeat(74));

        System.out.printf("    %-42s %s%n", "SITUATION", "USE this.?");
        System.out.printf("    %-42s %s%n", "field shadowed by a parameter", "REQUIRED");
        System.out.printf("    %-42s %s%n", "constructor delegation", "REQUIRED - this(...)");
        System.out.printf("    %-42s %s%n", "passing the object to something", "REQUIRED");
        System.out.printf("    %-42s %s%n", "returning it for chaining", "REQUIRED");
        System.out.printf("    %-42s %s%n", "ordinary field access", "optional");
        System.out.printf("    %-42s %s%n", "calling another instance method", "optional");

        System.out.println();
        System.out.println("  A reasonable default: use `this.` where it is REQUIRED, and");
        System.out.println("  for field assignment in constructors and setters. Omit it");
        System.out.println("  elsewhere. Consistency within a codebase matters more than");
        System.out.println("  which convention you pick.");

        System.out.println();
        System.out.println("  And the one place you simply cannot use it:");
        System.out.println("    static void main(String[] args) { System.out.println(this); }");
        System.out.println("    -> error: non-static variable this cannot be referenced");
        System.out.println("       from a static context");
        System.out.println("    There is no current object. That is what `static` MEANS.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 23.");
        System.out.println("=".repeat(74));
    }
}

// ----------------------------------------------------------------------------
// SECTION 1
// ----------------------------------------------------------------------------

/** Shows that `this` is whichever object the method was called on. */
class Counter {

    private final String label;
    private int count;

    /** @param label a name for this counter */
    Counter(String label) {
        this.label = label;
    }

    /** Increments. `count++` and `this.count++` are identical here. */
    void increment() {
        this.count++;
    }

    /** Prints this object's own identity hash, proving `this` differs per object. */
    void reportIdentity() {
        System.out.println("    " + this.label + ": this = "
                + Integer.toHexString(System.identityHashCode(this)));
    }

    @Override
    public String toString() {
        return "Counter[" + label + ", count=" + count + "]";
    }
}

// ----------------------------------------------------------------------------
// SECTION 2 - SHADOWING
// ----------------------------------------------------------------------------

/** Assigns the field correctly, using `this` to reach past the parameter. */
class PersonCorrect {

    private final String name;

    /** @param name the person's name */
    PersonCorrect(String name) {
        this.name = name;      // this.name is the FIELD, name is the PARAMETER
    }

    @Override
    public String toString() {
        return "name = " + name;
    }
}

/** The silent bug: the parameter shadows the field, so nothing is assigned. */
class PersonBroken {

    private String name;

    /** @param name the person's name - which never reaches the field */
    PersonBroken(String name) {
        // THE BUG. This assigns the parameter to itself and does nothing else.
        // The field is untouched, so it keeps its default value of null.
        name = name;
    }

    @Override
    public String toString() {
        return "name = " + name + "   <- the field was never assigned";
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - this(...) DELEGATION
// ----------------------------------------------------------------------------

/** Three constructors chaining with this(...). */
class Point {

    private final int x;
    private final int y;

    /** The origin. */
    Point() {
        this(0, 0);
    }

    /** @param both the value used for both coordinates */
    Point(int both) {
        this(both, both);
    }

    /** @param x the x coordinate  @param y the y coordinate */
    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - PASSING this, SAFELY AND UNSAFELY
// ----------------------------------------------------------------------------

/** A minimal listener contract for the event-bus demonstration. */
interface Listener {
    /** @param event the event that occurred */
    void onEvent(String event);
}

/** Holds listeners and notifies them. */
class EventBus {

    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Registers a listener and immediately sends it a "registered" event.
     *
     * <p>Sending something on registration is completely ordinary bus
     * behaviour - a welcome message, a replay of the last event, a health
     * check. It is also exactly what turns a leaked {@code this} from a
     * theoretical hazard into an observable bug: the bus calls back into an
     * object whose constructor has not finished.
     *
     * @param listener something to notify
     */
    void register(Listener listener) {
        listeners.add(listener);
        listener.onEvent("registration");   // calls back IMMEDIATELY
    }

    /** @param event the event to broadcast */
    void publish(String event) {
        for (Listener listener : listeners) {
            listener.onEvent(event);
        }
    }
}

/** Constructed fully, then registered by the caller. This is the safe order. */
class SafeWidget implements Listener {

    private final String name;

    /** @param name this widget's name */
    SafeWidget(String name) {
        this.name = name;
        // Deliberately does NOT register itself.
    }

    @Override
    public void onEvent(String event) {
        System.out.println("    SafeWidget   \"" + name + "\" handled " + event);
    }
}

/** Publishes `this` from its own constructor. This is the mistake. */
class UnsafeWidget implements Listener {

    private String name;

    /**
     * @param bus  the bus to register with
     * @param name this widget's name
     */
    UnsafeWidget(EventBus bus, String name) {
        // THE MISTAKE: `this` escapes before the object is finished. Anything
        // the bus does with it now sees a half-built object.
        bus.register(this);

        // Only NOW is the field assigned. Too late for anyone already holding
        // a reference from the line above.
        this.name = name;
    }

    @Override
    public void onEvent(String event) {
        // On the "registration" callback, `name` is still null: the constructor
        // has not reached the assignment yet. On later events it is set. The
        // same object behaves differently depending on WHEN it is called.
        String marker = (name == null) ? "   <- NULL: the constructor had not finished!" : "";
        System.out.println("    UnsafeWidget \"" + name + "\" handled " + event + marker);
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - RETURNING this
// ----------------------------------------------------------------------------

/** A fluent builder. Every method returns `this` so calls can chain. */
class QueryBuilder {

    private String columns = "*";
    private String table = "?";
    private final List<String> conditions = new ArrayList<>();
    private String ordering;

    /** @param columns the columns to select  @return this builder */
    QueryBuilder select(String... columns) {
        this.columns = String.join(", ", columns);
        return this;
    }

    /** @param table the table name  @return this builder */
    QueryBuilder from(String table) {
        this.table = table;
        return this;
    }

    /** @param condition a WHERE clause; may be called repeatedly  @return this builder */
    QueryBuilder where(String condition) {
        this.conditions.add(condition);
        return this;
    }

    /** @param column the column to order by  @return this builder */
    QueryBuilder orderBy(String column) {
        this.ordering = column;
        return this;
    }

    /** @return the assembled SQL */
    String build() {
        StringBuilder sql = new StringBuilder("SELECT ").append(columns)
                .append(" FROM ").append(table);
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        if (ordering != null) {
            sql.append(" ORDER BY ").append(ordering);
        }
        return sql.toString();
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - QUALIFIED this
// ----------------------------------------------------------------------------

/** Contains an inner class that reaches back to it with Outer.this. */
class Outer {

    private final String description;

    /** @param description text identifying this outer object */
    Outer(String description) {
        this.description = description;
    }

    /**
     * A NON-STATIC nested class, i.e. an inner class. It holds a hidden
     * reference to the Outer instance that created it, which is what makes
     * Outer.this work - and what makes it a memory-leak risk.
     */
    class Inner {

        private final String description;

        /** @param description text identifying this inner object */
        Inner(String description) {
            this.description = description;
        }

        /** Prints all three ways of naming a `description` from in here. */
        void showBoth() {
            System.out.println("  Inside Inner, three different names:");
            System.out.println("    description        -> " + description);
            System.out.println("    this.description   -> " + this.description);
            System.out.println("    Outer.this.description -> " + Outer.this.description);
        }
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - LAMBDA VS ANONYMOUS CLASS
// ----------------------------------------------------------------------------

/** Prints what `this` means inside a lambda and inside an anonymous class. */
class ScopeDemonstration {

    @Override
    public String toString() {
        return "the enclosing ScopeDemonstration object";
    }

    /** Runs one lambda and one anonymous class, printing `this` from each. */
    void compare() {
        System.out.println("  In the enclosing method, this = " + this);
        System.out.println();

        Runnable anonymous = new Runnable() {
            @Override
            public void run() {
                // A real class with a real instance, so `this` is THAT object.
                System.out.println("  Inside an ANONYMOUS CLASS:");
                System.out.println("    this = " + this.getClass().getName());
                System.out.println("           (the $1 suffix means a real, separate class)");
            }
        };

        Runnable lambda = () -> {
            // Not a class. No new `this` scope, so `this` is unchanged.
            System.out.println("  Inside a LAMBDA:");
            System.out.println("    this = " + this + "   <- UNCHANGED");
        };

        anonymous.run();
        System.out.println();
        lambda.run();
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Compile this file with `javac -Xlint:all` and find the warning javac
 *    produces for PersonBroken. Then fix it and confirm the warning goes.
 *
 * 2. Add `System.out.println(this)` to main() and read the compile error.
 *    Explain it in one sentence using the word "static".
 *
 * 3. Give QueryBuilder a `limit(int n)` method that returns this, and use it
 *    in the chain. Then make one method return void and watch the chain break.
 *
 * 4. Make Outer.Inner static. Which line stops compiling, and why? What have
 *    you gained by making it static?
 *
 * 5. Rewrite the anonymous Runnable in ScopeDemonstration as a lambda and
 *    predict what `this` prints. Then run it. This is exactly the surprise
 *    people hit when modernising old code.
 *
 * 6. Fix UnsafeWidget so the constructor no longer leaks `this`. Then write a
 *    static factory `UnsafeWidget.createAndRegister(bus, name)` that does both
 *    steps in the safe order.
 * ============================================================================
 */
