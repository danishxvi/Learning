/* ============================================================================
 * 28 - ABSTRACT CLASSES
 * ----------------------------------------------------------------------------
 * Companion lesson: 28-abstract-classes.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/28-abstract-classes.java
 *
 * An abstract class CANNOT BE INSTANTIATED and may leave methods unimplemented
 * for subclasses to fill in. It sits between a concrete class and an interface.
 *
 * Its real purpose is Section 3: define an ALGORITHM and leave HOLES.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.List;

class AbstractClasses {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE MECHANICS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - abstract MEANS 'CANNOT BE INSTANTIATED'");
        System.out.println("=".repeat(74));

        // Shape shape = new Shape("x");
        //   ERROR: Shape is abstract; cannot be instantiated

        System.out.println("    new Shape(\"anything\")  ->  COMPILE ERROR");
        System.out.println("      'Shape is abstract; cannot be instantiated'");
        System.out.println();
        System.out.println("  But you can hold one, and every concrete subclass fits:");

        List<Shape> shapes = List.of(
                new Circle(5),
                new Rectangle(4, 6),
                new Triangle(3, 4, 5)
        );

        for (Shape shape : shapes) {
            System.out.println("    " + shape.describe());
        }

        System.out.println();
        System.out.println("  THE RULES:");
        System.out.printf("    %-46s %s%n", "cannot be instantiated", "but CAN have constructors");
        System.out.printf("    %-46s %s%n", "can mix abstract and concrete methods", "any proportion");
        System.out.printf("    %-46s %s%n", "can have fields and static members", "unlike an interface");
        System.out.printf("    %-46s %s%n", "a class with an abstract method", "MUST be abstract");
        System.out.printf("    %-46s %s%n", "an abstract class with no abstract methods", "is legal");
        System.out.printf("    %-46s %s%n", "a subclass must implement all of them", "or be abstract too");
        System.out.printf("    %-46s %s%n", "abstract + final", "COMPILE ERROR");
        System.out.printf("    %-46s %s%n", "abstract + private", "COMPILE ERROR");
        System.out.printf("    %-46s %s%n", "abstract + static", "COMPILE ERROR");

        System.out.println();
        System.out.println("  Why those three are contradictory:");
        System.out.println("    final   - abstract MUST be overridden, final FORBIDS it");
        System.out.println("    private - a private method is invisible to subclasses");
        System.out.println("    static  - static methods are hidden, never overridden");

        System.out.println();
        System.out.println("  An abstract class CAN have a constructor - it runs on every");
        System.out.println("  subclass instantiation, it just cannot be called with `new`:");
        try {
            new Circle(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("    new Circle(-1) -> " + e.getMessage());
            System.out.println("    That validation lives in Shape's constructor, which every");
            System.out.println("    subclass calls via super(...). Written once.");
        }

        System.out.println();
        System.out.println("  A subclass that does NOT implement everything must itself be");
        System.out.println("  abstract - and it can add new abstract methods too:");
        System.out.println("    " + new Square(4).describe());
        System.out.println("    Square extends AbstractQuadrilateral extends Shape");


        /* ====================================================================
         * SECTION 2 - A PARTIAL IMPLEMENTATION
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - CONCRETE CODE THAT CALLS METHODS THAT DO NOT EXIST YET");
        System.out.println("=".repeat(74));

        System.out.println("  Shape.summary() was written ONCE, in the abstract class:");
        System.out.println();
        System.out.println("      String summary() {");
        System.out.println("          return format(\"area=%.2f, perimeter=%.2f, ratio=%.2f\",");
        System.out.println("                        area(), perimeter(), area() / perimeter());");
        System.out.println("      }");
        System.out.println();
        System.out.println("  It calls area() and perimeter(), which did not exist when it");
        System.out.println("  was written. Every subclass gets it correctly, for free:");
        System.out.println();
        for (Shape shape : shapes) {
            System.out.printf("    %-12s %s%n", shape.getName() + ":", shape.summary());
        }
        System.out.println();
        System.out.println("  THAT is what an abstract class is for: define the ALGORITHM,");
        System.out.println("  leave HOLES for the parts that vary.");


        /* ====================================================================
         * SECTION 3 - THE TEMPLATE METHOD PATTERN
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE TEMPLATE METHOD PATTERN");
        System.out.println("=".repeat(74));

        System.out.println("  Two importers. They differ in TWO methods and share everything");
        System.out.println("  else - including the ORDER the steps happen in.");
        System.out.println();

        DataImporter csv = new CsvImporter();
        csv.importData("employees.csv");

        System.out.println();
        DataImporter json = new JsonImporter();
        json.importData("employees.json");

        System.out.println();
        System.out.println("  LOOK AT WHAT MAKES IT WORK:");
        System.out.println("    importData() is FINAL");
        System.out.println("      -> subclasses cannot reorder or skip steps");
        System.out.println("    the hooks are PROTECTED ABSTRACT");
        System.out.println("      -> visible to subclasses, invisible to callers");
        System.out.println("    the shared steps are PRIVATE");
        System.out.println("      -> subclasses cannot break validation or logging");
        System.out.println();
        System.out.println("  The base class owns the SEQUENCE; subclasses own the STEPS.");
        System.out.println();
        System.out.println("  You already use this: AbstractList, AbstractMap and InputStream");
        System.out.println("  in the JDK are all built exactly this way.");

        System.out.println();
        System.out.println("  Proof that a subclass cannot break the sequence:");
        System.out.println("    @Override void importData(String s) { ... }");
        System.out.println("    -> error: importData(String) in CsvImporter cannot override");
        System.out.println("       importData(String) in DataImporter; overridden method is final");


        /* ====================================================================
         * SECTION 4 - ABSTRACT CLASS VS INTERFACE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - THE COMPARISON THAT ACTUALLY MATTERS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-34s %-16s %s%n", "", "ABSTRACT CLASS", "INTERFACE");
        System.out.printf("    %-34s %-16s %s%n", "instance fields (state)", "YES", "NO");
        System.out.printf("    %-34s %-16s %s%n", "constructors", "yes", "no");
        System.out.printf("    %-34s %-16s %s%n", "how many can you have", "ONE", "MANY");
        System.out.printf("    %-34s %-16s %s%n", "method bodies", "yes", "yes, since Java 8");
        System.out.printf("    %-34s %-16s %s%n", "private/protected members", "yes", "private only (9+)");

        System.out.println();
        System.out.println("  'Interfaces cannot have implementations' has been FALSE since");
        System.out.println("  Java 8 (2014). Interfaces have default and static methods:");
        System.out.println();
        Greeter greeter = Greeter.of("Danish");
        System.out.println("    Greeter.of(\"Danish\").greet() -> " + greeter.greet());
        System.out.println("    ...and greet() is a DEFAULT method with a body, in an interface.");

        System.out.println();
        System.out.println("  SO THE REAL DIFFERENCES ARE ONLY TWO:");
        System.out.println("    1. an abstract class can hold STATE; an interface cannot");
        System.out.println("    2. you get ONE superclass but MANY interfaces");

        System.out.println();
        System.out.println("  WHICH TO CHOOSE:");
        System.out.println();
        System.out.println("    INTERFACE - and this is the DEFAULT - when:");
        System.out.println("      - you are defining a CAPABILITY (Comparable, Runnable)");
        System.out.println("      - unrelated classes might implement it");
        System.out.println("      - you want implementers free to extend something else");
        System.out.println();
        System.out.println("    ABSTRACT CLASS when:");
        System.out.println("      - subclasses genuinely share STATE");
        System.out.println("      - you want a TEMPLATE METHOD controlling an algorithm");
        System.out.println("      - you need non-public members");
        System.out.println("      - the subtypes are closely related and you control them all");

        System.out.println();
        System.out.println("  OFTEN THE BEST ANSWER IS BOTH - the JDK's own pattern:");
        System.out.println("      interface List<E>");
        System.out.println("      abstract class AbstractList<E> implements List<E>");
        System.out.println("      class ArrayList<E> extends AbstractList<E>");
        System.out.println();
        System.out.println("    Callers depend on the INTERFACE. Implementers may extend the");
        System.out.println("    skeletal class for a head start, or implement the interface");
        System.out.println("    directly if they need to extend something else.");
        System.out.println();
        System.out.println("  Demonstrated here with a Repository interface and a skeletal base:");
        UserRepository users = new UserRepository();
        users.save("Danish");
        users.save("Aisha");
        System.out.println("    users.count()        -> " + users.count());
        System.out.println("    users.findAll()      -> " + users.findAll());
        System.out.println("    users.isEmpty()      -> " + users.isEmpty()
                + "   <- a DEFAULT method from the interface");
        System.out.println("    users.describe()     -> " + users.describe()
                + "   <- from the skeletal abstract class");


        /* ====================================================================
         * SECTION 5 - COMMON MISTAKES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - THE MISTAKES");
        System.out.println("=".repeat(74));

        System.out.println("  MISTAKE 1 - making everything abstract.");
        System.out.println("    If every method is abstract and there are no fields, you have");
        System.out.println("    written an INTERFACE - and one that costs your users their");
        System.out.println("    single inheritance slot for nothing. Use an interface.");

        System.out.println();
        System.out.println("  MISTAKE 2 - too much state in the base class.");
        System.out.println("    The more state a base holds, the more tightly subclasses are");
        System.out.println("    coupled to it, and the worse the FRAGILE BASE CLASS problem");
        System.out.println("    gets (lesson 26). Keep abstract classes thin.");

        System.out.println();
        System.out.println("  MISTAKE 3 - calling an abstract method from the constructor.");
        System.out.print("    new BrokenSubclass() prints -> ");
        BrokenSubclass broken = new BrokenSubclass();
        System.out.println("    after construction, the field IS set: " + broken.getValue());
        System.out.println();
        System.out.println("    Exactly the bug from lesson 22 - and abstract classes make it");
        System.out.println("    MORE tempting, because the hole is right there asking to be");
        System.out.println("    called. The base constructor runs BEFORE the subclass's field");
        System.out.println("    initialisers, so the subclass sees null.");

        System.out.println();
        System.out.println("  MISTAKE 4 - forgetting the constructor still runs.");
        System.out.println("    An abstract class's constructor executes on every subclass");
        System.out.println("    instantiation. It just cannot be reached with `new`.");
        System.out.println("    Validation there still applies - as Section 1 showed.");


        /* ====================================================================
         * SECTION 6 - WHEN NOT TO USE EITHER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - BEFORE YOU BUILD A HIERARCHY AT ALL");
        System.out.println("=".repeat(74));

        System.out.println("  TWO CASES?");
        System.out.println("    A boolean or an enum is usually clearer than two subclasses.");
        System.out.println();
        System.out.println("  SHARING CODE ONLY?");
        System.out.println("    Composition or a static helper (lesson 26).");
        System.out.println();
        System.out.println("  A FIXED SET OF VARIANTS?");
        System.out.println("    A SEALED interface with records, plus an exhaustive switch");
        System.out.println("    (lesson 37). Often better than an abstract class for a closed");
        System.out.println("    hierarchy, because the COMPILER verifies you handled every");
        System.out.println("    case - an abstract class gives you no such check.");
        System.out.println();
        System.out.println("  JUST DATA?");
        System.out.println("    A record (lesson 36).");
        System.out.println();
        System.out.println("  Abstract classes are a good tool with a NARROW purpose:");
        System.out.println("  SHARED STATE plus a CONTROLLED ALGORITHM. Reach for an");
        System.out.println("  interface first, and use an abstract class when you actually");
        System.out.println("  need what it uniquely offers.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 28.");
        System.out.println("=".repeat(74));
    }
}

// ----------------------------------------------------------------------------
// SECTIONS 1 AND 2 - THE SHAPE HIERARCHY
// ----------------------------------------------------------------------------

/**
 * An abstract class with state, a validating constructor, abstract holes, and
 * concrete methods written in terms of those holes.
 */
abstract class Shape {

    /** State - an interface could not have this. */
    private final String name;

    /**
     * A constructor in an abstract class. It runs on every subclass
     * instantiation; it simply cannot be reached with `new`.
     *
     * @param name the shape's name; must not be blank
     * @throws IllegalArgumentException if the name is blank
     */
    protected Shape(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("shape name must not be blank");
        }
        this.name = name;
    }

    /** A HOLE. Every concrete subclass must fill it. @return the area */
    abstract double area();

    /** A HOLE. @return the perimeter */
    abstract double perimeter();

    /**
     * CONCRETE, and written entirely in terms of methods that did not exist
     * when this was written. Every subclass inherits it correctly for free.
     *
     * @return a formatted summary
     */
    String summary() {
        return String.format("area=%.2f, perimeter=%.2f, ratio=%.2f",
                area(), perimeter(), area() / perimeter());
    }

    /** @return a one-line description */
    String describe() {
        return String.format("%-12s area %8.2f, perimeter %8.2f", name, area(), perimeter());
    }

    /** @return the shape's name */
    String getName() {
        return name;
    }
}

/** A concrete shape: implements every abstract method. */
class Circle extends Shape {

    private final double radius;

    /**
     * @param radius the radius; must be positive
     * @throws IllegalArgumentException if the radius is not positive
     */
    Circle(double radius) {
        super("Circle");       // reaches Shape's validation
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be positive, got " + radius);
        }
        this.radius = radius;
    }

    @Override
    double area() {
        return Math.PI * radius * radius;
    }

    @Override
    double perimeter() {
        return 2 * Math.PI * radius;
    }
}

/**
 * An ABSTRACT subclass. It implements one hole, leaves the other open, and
 * adds a new abstract method of its own. Perfectly legal.
 */
abstract class AbstractQuadrilateral extends Shape {

    /** @param name the shape's name */
    protected AbstractQuadrilateral(String name) {
        super(name);
    }

    /** A new hole, added by this level of the hierarchy. @return the side count */
    abstract int sideCount();

    /** Implemented here, so concrete subclasses do not have to. @return 4 */
    int cornerCount() {
        return sideCount();
    }
}

/** A concrete quadrilateral. */
class Rectangle extends AbstractQuadrilateral {

    private final double width;
    private final double height;

    /** @param width the width  @param height the height */
    Rectangle(double width, double height) {
        super("Rectangle");
        this.width = width;
        this.height = height;
    }

    @Override
    double area() {
        return width * height;
    }

    @Override
    double perimeter() {
        return 2 * (width + height);
    }

    @Override
    int sideCount() {
        return 4;
    }
}

/** A concrete quadrilateral two levels down from Shape. */
class Square extends AbstractQuadrilateral {

    private final double side;

    /** @param side the length of every side */
    Square(double side) {
        super("Square");
        this.side = side;
    }

    @Override
    double area() {
        return side * side;
    }

    @Override
    double perimeter() {
        return 4 * side;
    }

    @Override
    int sideCount() {
        return 4;
    }
}

/** A third concrete shape. */
class Triangle extends Shape {

    private final double a;
    private final double b;
    private final double c;

    /** @param a first side  @param b second side  @param c third side */
    Triangle(double a, double b, double c) {
        super("Triangle");
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    double area() {
        double s = perimeter() / 2;                    // Heron's formula
        return Math.sqrt(s * (s - a) * (s - b) * (s - c));
    }

    @Override
    double perimeter() {
        return a + b + c;
    }
}

// ----------------------------------------------------------------------------
// SECTION 3 - THE TEMPLATE METHOD PATTERN
// ----------------------------------------------------------------------------

/** A parsed record, kept trivial so the pattern stays visible. */
record ImportedRecord(String id, String value) {}

/**
 * The template. The base class owns the SEQUENCE of steps; subclasses own the
 * two steps that actually vary.
 */
abstract class DataImporter {

    /**
     * THE TEMPLATE METHOD. It is final, so no subclass can reorder or skip the
     * steps - which is the whole point of the pattern.
     *
     * @param source where to import from
     */
    final void importData(String source) {
        validate(source);
        List<String> raw = read(source);
        List<ImportedRecord> parsed = parse(raw);
        save(parsed);
        log(source, parsed.size());
    }

    /**
     * A HOOK. protected, so subclasses can implement it and callers cannot see it.
     *
     * @param source where to read from
     * @return the raw lines
     */
    protected abstract List<String> read(String source);

    /**
     * A HOOK.
     *
     * @param raw the raw lines
     * @return the parsed records
     */
    protected abstract List<ImportedRecord> parse(List<String> raw);

    /** Shared and PRIVATE, so no subclass can weaken it.
     *  @param source the source to check */
    private void validate(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        System.out.println("    [shared]   validated " + source);
    }

    /** Shared and private. @param records the records to save */
    private void save(List<ImportedRecord> records) {
        System.out.println("    [shared]   saved " + records.size() + " records");
    }

    /** Shared and private. @param source the source  @param count how many records */
    private void log(String source, int count) {
        System.out.println("    [shared]   logged: imported " + count + " from " + source);
    }
}

/** Fills the two holes for comma-separated data. */
class CsvImporter extends DataImporter {

    @Override
    protected List<String> read(String source) {
        System.out.println("  CsvImporter importing " + source);
        System.out.println("    [subclass] reading as comma-separated text");
        return List.of("1,Danish", "2,Aisha", "3,Rahul");
    }

    @Override
    protected List<ImportedRecord> parse(List<String> raw) {
        System.out.println("    [subclass] splitting on commas");
        List<ImportedRecord> records = new ArrayList<>();
        for (String line : raw) {
            String[] parts = line.split(",");
            records.add(new ImportedRecord(parts[0], parts[1]));
        }
        return records;
    }

    // @Override void importData(String source) { }
    //   ERROR: importData(String) in CsvImporter cannot override
    //   importData(String) in DataImporter; overridden method is final
}

/** Fills the same two holes differently. Everything else is identical. */
class JsonImporter extends DataImporter {

    @Override
    protected List<String> read(String source) {
        System.out.println("  JsonImporter importing " + source);
        System.out.println("    [subclass] reading as JSON objects");
        return List.of("{\"id\":\"1\",\"name\":\"Danish\"}", "{\"id\":\"2\",\"name\":\"Aisha\"}");
    }

    @Override
    protected List<ImportedRecord> parse(List<String> raw) {
        System.out.println("    [subclass] parsing JSON fields");
        List<ImportedRecord> records = new ArrayList<>();
        for (String line : raw) {
            String id = between(line, "\"id\":\"", "\"");
            String name = between(line, "\"name\":\"", "\"");
            records.add(new ImportedRecord(id, name));
        }
        return records;
    }

    /**
     * A deliberately naive extractor - real JSON parsing is not this lesson's
     * subject.
     *
     * @param text  the text to search
     * @param start the marker before the value
     * @param end   the marker after it
     * @return the extracted value
     */
    private String between(String text, String start, String end) {
        int from = text.indexOf(start) + start.length();
        int to = text.indexOf(end, from);
        return text.substring(from, to);
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - INTERFACES WITH BODIES, AND THE SKELETAL PATTERN
// ----------------------------------------------------------------------------

/** An interface with an abstract method, a default method and a static factory. */
interface Greeter {

    /** @return the name to greet */
    String name();

    /** A DEFAULT method - a real body, in an interface. @return the greeting */
    default String greet() {
        return "Hello, " + name() + "!";
    }

    /** A STATIC factory, in an interface. @param name the name @return a Greeter */
    static Greeter of(String name) {
        return () -> name;
    }
}

/**
 * The JDK's own pattern: an interface for the TYPE.
 *
 * @param <T> the stored type
 */
interface Repository<T> {

    /** @param item the item to store */
    void save(T item);

    /** @return everything stored */
    List<T> findAll();

    /** @return how many items are stored */
    int count();

    /** A default method, derived from the others. @return true if empty */
    default boolean isEmpty() {
        return count() == 0;
    }
}

/**
 * ...and a SKELETAL abstract class for convenience. Implementers may extend
 * this for a head start, or implement Repository directly if they need to
 * extend something else.
 *
 * @param <T> the stored type
 */
abstract class AbstractRepository<T> implements Repository<T> {

    /** Shared STATE - which is exactly what an interface could not provide. */
    protected final List<T> items = new ArrayList<>();

    @Override
    public void save(T item) {
        items.add(item);
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(items);
    }

    @Override
    public int count() {
        return items.size();
    }

    /** @return a description, using the subclass's own name */
    String describe() {
        return getClass().getSimpleName() + " holding " + count() + " item(s)";
    }
}

/** A concrete repository that gets everything from the skeletal class. */
class UserRepository extends AbstractRepository<String> {
    // Nothing to write. All of it came from the interface and the skeleton.
}

// ----------------------------------------------------------------------------
// SECTION 5 - THE CONSTRUCTOR BUG
// ----------------------------------------------------------------------------

/** Calls its own abstract method from the constructor. This is the mistake. */
abstract class BrokenBase {

    /** THE BUG: this runs before any subclass field initialiser. */
    BrokenBase() {
        init();
    }

    /** The hole, tempting to call from the constructor above. */
    abstract void init();
}

/** Overrides init() and reads a field that has not been initialised yet. */
class BrokenSubclass extends BrokenBase {

    /** Initialised at declaration - which happens AFTER BrokenBase's constructor. */
    private String value = "definitely set";

    @Override
    void init() {
        System.out.println(value + "   <- null, despite the initialiser");
    }

    /** @return the field, read after construction has finished */
    String getValue() {
        return value;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a Pentagon to the Shape hierarchy. Note that summary() and describe()
 *    work immediately, with no extra code. Then count how many places you
 *    would have edited without the abstract base.
 *
 * 2. Try to write `abstract final class X`. Then `abstract private void y()`.
 *    Then `abstract static void z()`. Read all three errors and explain each
 *    in one sentence.
 *
 * 3. Add an XmlImporter. You should write exactly two methods. If you find
 *    yourself wanting to change importData(), the design is telling you
 *    something - what?
 *
 * 4. Remove `final` from importData() and override it in CsvImporter to skip
 *    validation. This is why the final is there. Put it back.
 *
 * 5. Convert Shape to an interface. Which member cannot survive the change,
 *    and why? Now decide whether Shape should have been an interface all along.
 *
 * 6. Fix BrokenBase two ways: pass the value through the constructor, and use
 *    a separate init() the caller invokes. Which do you prefer, and why?
 * ============================================================================
 */
