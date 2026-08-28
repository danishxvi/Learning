/* ============================================================================
 * 26 - INHERITANCE AND super
 * ----------------------------------------------------------------------------
 * Companion lesson: 26-inheritance-and-super.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/26-inheritance-and-super.java
 *
 * Inheritance lets one class build on another. It is also the most OVERUSED
 * feature in object-oriented programming, so Sections 6 and 7 - when NOT to
 * use it - matter at least as much as the mechanics in Sections 1 to 5.
 * ============================================================================
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class InheritanceAndSuper {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - THE MECHANICS
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - extends CREATES AN 'IS-A' RELATIONSHIP");
        System.out.println("=".repeat(74));

        Dog dog = new Dog("Bruno", "Labrador");

        System.out.println("  A Dog can do everything an Animal can:");
        dog.eat();          // inherited from Animal
        dog.sleep();        // inherited from Animal
        dog.bark();         // Dog's own

        System.out.println();
        System.out.println("  And a Dog IS AN Animal, so it fits anywhere an Animal fits:");
        Animal asAnimal = dog;              // upcast - implicit, always safe
        feedAnyAnimal(asAnimal);
        feedAnyAnimal(new Cat("Misty"));

        System.out.println();
        System.out.println("    dog instanceof Dog    -> " + (dog instanceof Dog));
        System.out.println("    dog instanceof Animal -> " + (dog instanceof Animal));
        System.out.println("    dog instanceof Object -> " + (dog instanceof Object)
                + "   (everything extends Object)");

        System.out.println();
        System.out.println("  WHAT IS INHERITED:");
        System.out.printf("    %-42s %s%n", "public / protected members", "YES");
        System.out.printf("    %-42s %s%n", "package-private members", "only in the same package");
        System.out.printf("    %-42s %s%n", "private members", "NO - see below");
        System.out.printf("    %-42s %s%n", "constructors", "NO - but CALLED via super(...)");
        System.out.printf("    %-42s %s%n", "static members", "yes, but HIDDEN not overridden");

        System.out.println();
        System.out.println("  private members are NOT inherited - but they DO exist in the");
        System.out.println("  subclass object. A Dog contains Animal's private fields; Dog");
        System.out.println("  code simply cannot name them. An inherited public getter can:");
        System.out.println("    dog.getRegistrationId() -> " + dog.getRegistrationId()
                + "   (reads a field Dog cannot see)");


        /* ====================================================================
         * SECTION 2 - SINGLE INHERITANCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - ONE SUPERCLASS, MANY INTERFACES");
        System.out.println("=".repeat(74));

        System.out.println("    class Dog extends Animal                      -> fine");
        System.out.println("    class Dog extends Animal, Pet                 -> COMPILE ERROR");
        System.out.println("    class Dog extends Animal implements Pet, ...  -> fine");
        System.out.println();
        System.out.println("  WHY: the DIAMOND PROBLEM. If C extended both A and B and both");
        System.out.println("  defined greet(), which one does C get? C++ allows this and");
        System.out.println("  needs virtual inheritance to untangle it. Java sidestepped the");
        System.out.println("  question entirely.");
        System.out.println();
        System.out.println("  Interfaces avoided it because they carried no implementation.");
        System.out.println("  Java 8's default methods reintroduced the possibility, so Java");
        System.out.println("  added a rule: if two interfaces give conflicting defaults, the");
        System.out.println("  implementing class MUST override the method. Lesson 29.");

        System.out.println();
        System.out.println("  A Dog implementing an interface as well as extending a class:");
        System.out.println("    dog.getName() + \" fetches: \" -> " + dog.fetch());
        System.out.println("    dog instanceof Pet -> " + (dog instanceof Pet));


        /* ====================================================================
         * SECTION 3 - super, JOB 1 AND 2
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - super: CONSTRUCTOR AND METHOD");
        System.out.println("=".repeat(74));

        System.out.println("  JOB 1 - calling the superclass constructor:");
        System.out.println("      Dog(String name, String breed) {");
        System.out.println("          super(name);      // MUST be the first statement");
        System.out.println("          this.breed = breed;");
        System.out.println("      }");
        System.out.println();
        System.out.println("    If you write neither super(...) nor this(...), the compiler");
        System.out.println("    inserts super(). That fails if the superclass has no no-arg");
        System.out.println("    constructor - the most common inheritance error (lesson 22).");

        System.out.println();
        System.out.println("  JOB 2 - EXTENDING an overridden method rather than replacing it:");
        System.out.println();
        System.out.print("    Animal.describe()      -> ");
        new Cat("Misty").describe();
        System.out.print("    Dog.describe()         -> ");
        dog.describe();
        System.out.println();
        System.out.println("    Dog.describe() calls super.describe() first, then adds to it.");
        System.out.println("    Without that call, the Animal version would never run.");
        System.out.println();
        System.out.println("    Note: super.super.method() is NOT legal. You can reach one");
        System.out.println("    level up, never two.");


        /* ====================================================================
         * SECTION 4 - super, JOB 3: SHADOWED FIELDS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - FIELDS ARE SHADOWED, NOT OVERRIDDEN");
        System.out.println("=".repeat(74));

        ShadowChild child = new ShadowChild();
        child.showAll();

        System.out.println();
        System.out.println("  BOTH fields exist in the object at the same time. Which one you");
        System.out.println("  get depends on the DECLARED TYPE of the reference, not the");
        System.out.println("  runtime type - the opposite of how methods work:");

        ShadowParent viaParent = child;      // same object, different declared type

        System.out.println();
        System.out.println("    ShadowChild  reference .label -> " + child.label);
        System.out.println("    ShadowParent reference .label -> " + viaParent.label
                + "   <- SAME OBJECT, different answer");
        System.out.print("    method call on either         -> ");
        viaParent.describe();
        System.out.println();
        System.out.println("  The METHOD used the runtime type. The FIELD used the declared");
        System.out.println("  type. This is a genuine trap, and it is one more reason to keep");
        System.out.println("  fields private and never shadow them.");


        /* ====================================================================
         * SECTION 5 - protected IS MORE PUBLIC THAN IT LOOKS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - protected IS PART OF YOUR PUBLIC API");
        System.out.println("=".repeat(74));

        System.out.println("  protected means: same class, same package, OR ANY SUBCLASS");
        System.out.println("  ANYWHERE. That last part is the problem.");
        System.out.println();
        System.out.println("  Animal declares `protected String name`. Anyone can write:");

        MaliciousAnimal malicious = new MaliciousAnimal("Innocent");
        System.out.println("    before -> " + malicious.getName());
        malicious.corrupt();
        System.out.println("    after  -> " + malicious.getName()
                + "   <- a subclass reached in and broke it");
        System.out.println();
        System.out.println("  A protected member is a PERMANENT commitment: once published");
        System.out.println("  you can never remove or narrow it without breaking someone.");
        System.out.println();
        System.out.println("  Prefer private fields with protected accessor METHODS, or just");
        System.out.println("  private with a public getter. protected FIELDS hand subclasses");
        System.out.println("  the ability to corrupt state the superclass is guarding.");


        /* ====================================================================
         * SECTION 6 - WHEN INHERITANCE IS WRONG
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - THE IMPORTANT HALF OF THIS LESSON");
        System.out.println("=".repeat(74));

        System.out.println("  THE RULE: inherit for IS-A, compose for HAS-A.");
        System.out.println("    class Car extends Engine   -> WRONG. A car is not an engine.");
        System.out.println("    class Car { Engine engine; } -> RIGHT. A car HAS an engine.");
        System.out.println();
        System.out.println("  If the sentence 'a Subclass is a Superclass' sounds wrong, it is.");

        /* --------------------------------------------------------------------
         * THE JDK's OWN MISTAKE: Stack extends Vector.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE JDK's OWN FAMOUS MISTAKE - java.util.Stack extends Vector:");

        java.util.Stack<Integer> stack = new java.util.Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println("    after three pushes        -> " + stack);

        // Every one of these is inherited from Vector and breaks the stack
        // contract, which is supposed to allow only push and pop.
        stack.add(0, 99);
        System.out.println("    stack.add(0, 99)          -> " + stack
                + "   <- inserted at the BOTTOM");
        stack.remove(1);
        System.out.println("    stack.remove(1)           -> " + stack
                + "   <- removed from the MIDDLE");
        stack.set(0, 42);
        System.out.println("    stack.set(0, 42)          -> " + stack
                + "   <- overwrote an element");

        System.out.println();
        System.out.println("    A stack should allow only push and pop. By INHERITING, it");
        System.out.println("    inherited every method that breaks that guarantee. Stack is");
        System.out.println("    now effectively deprecated - use ArrayDeque (lesson 48).");
        System.out.println();
        System.out.println("    The same mistake: Properties extends Hashtable, which lets");
        System.out.println("    you store non-String values in a String-only container.");

        /* --------------------------------------------------------------------
         * THE LISKOV SUBSTITUTION PRINCIPLE, demonstrated.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  SQUARE extends RECTANGLE - mathematically true, and broken:");
        System.out.println();
        System.out.println("    A caller writes a test that should pass for any Rectangle:");
        System.out.println("      r.setWidth(5); r.setHeight(4); assert r.getArea() == 20;");
        System.out.println();

        checkRectangleContract(new MutableRectangle(1, 1));
        checkRectangleContract(new MutableSquare(1));

        System.out.println();
        System.out.println("    A mutable Square cannot substitute for a mutable Rectangle");
        System.out.println("    without the caller noticing. That violates the LISKOV");
        System.out.println("    SUBSTITUTION PRINCIPLE - the 'L' in SOLID, and the single");
        System.out.println("    most useful test for whether inheritance is legitimate:");
        System.out.println();
        System.out.println("      'Subtypes must be usable anywhere their supertype is,");
        System.out.println("       without the caller noticing.'");
        System.out.println();
        System.out.println("    Note the problem VANISHES if both are immutable - with no");
        System.out.println("    setters there is nothing to break. One more argument for");
        System.out.println("    immutability (lesson 38).");

        /* --------------------------------------------------------------------
         * THE FRAGILE BASE CLASS PROBLEM.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE FRAGILE BASE CLASS PROBLEM:");
        System.out.println();
        System.out.println("    A set that counts how many elements were ever added.");
        System.out.println("    Adding three elements should give a count of 3:");

        InheritedCountingSet<String> inherited = new InheritedCountingSet<>();
        inherited.addAll(List.of("a", "b", "c"));
        System.out.println("      via INHERITANCE  -> addCount = " + inherited.getAddCount()
                + "   <- WRONG, double counted");

        ComposedCountingSet<String> composed = new ComposedCountingSet<>();
        composed.addAll(List.of("a", "b", "c"));
        System.out.println("      via COMPOSITION  -> addCount = " + composed.getAddCount()
                + "   correct");

        System.out.println();
        System.out.println("    The inherited version double-counts because HashSet.addAll");
        System.out.println("    internally calls add() - an IMPLEMENTATION DETAIL the");
        System.out.println("    subclass accidentally depended on. Change that detail in a");
        System.out.println("    future JDK and the subclass breaks, without touching it.");
        System.out.println();
        System.out.println("    The composed version cannot break, because whatever HashSet");
        System.out.println("    does internally is invisible to it.");


        /* ====================================================================
         * SECTION 7 - COMPOSITION OVER INHERITANCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - PREFER COMPOSITION");
        System.out.println("=".repeat(74));

        System.out.printf("    %-30s %-22s %s%n", "", "INHERITANCE", "COMPOSITION");
        System.out.printf("    %-30s %-22s %s%n", "coupling", "tight (internals)", "loose (public API)");
        System.out.printf("    %-30s %-22s %s%n", "changeable at runtime", "no", "YES - swap it");
        System.out.printf("    %-30s %-22s %s%n", "how many sources", "one superclass", "any number");
        System.out.printf("    %-30s %-22s %s%n", "breaks when base changes", "often", "rarely");
        System.out.printf("    %-30s %-22s %s%n", "exposes base's API", "ALL of it", "only what you choose");

        System.out.println();
        System.out.println("  Composition also lets you change behaviour at RUNTIME:");
        Robot robot = new Robot(new LoudVoice());
        robot.speak();
        robot.setVoice(new QuietVoice());
        robot.speak();
        System.out.println("    You cannot change a superclass at runtime. Ever.");

        System.out.println();
        System.out.println("  THE GUIDANCE (from Effective Java) - inherit only when:");
        System.out.println("    1. there is a genuine IS-A relationship, AND");
        System.out.println("    2. the superclass was DESIGNED and DOCUMENTED for");
        System.out.println("       inheritance, or you control both classes.");
        System.out.println("  Otherwise: hold an instance and delegate.");
        System.out.println();
        System.out.println("  `final` on a class prevents inheritance entirely, and is the");
        System.out.println("  right DEFAULT for classes not designed to be extended.");
        System.out.println("  Lesson 31 covers final.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 26.");
        System.out.println("=".repeat(74));
    }

    /**
     * Accepts any Animal - which is the whole point of an is-a relationship.
     *
     * @param animal any animal, including subclasses that did not exist when
     *               this method was written
     */
    static void feedAnyAnimal(Animal animal) {
        System.out.print("    feeding: ");
        animal.eat();
    }

    /**
     * A contract test written against Rectangle. It should pass for every
     * legitimate subtype; a mutable Square fails it.
     *
     * @param rectangle the rectangle to test
     */
    static void checkRectangleContract(MutableRectangle rectangle) {
        rectangle.setWidth(5);
        rectangle.setHeight(4);
        int area = rectangle.getArea();
        String verdict = (area == 20) ? "PASSES" : "FAILS - Liskov violated";
        System.out.printf("      %-18s width=5, height=4, area=%-3d %s%n",
                rectangle.getClass().getSimpleName(), area, verdict);
    }
}

// ----------------------------------------------------------------------------
// SECTIONS 1-3 - THE ANIMAL HIERARCHY
// ----------------------------------------------------------------------------

/** A simple interface, to show that a class may extend one class and
 *  implement many interfaces. */
interface Pet {
    /** @return a description of the pet fetching something */
    String fetch();
}

/** The superclass. Note the deliberate mix of private and protected. */
class Animal {

    /**
     * PROTECTED, so any subclass anywhere can read AND WRITE it. Section 5
     * shows why this is usually a mistake.
     */
    protected String name;

    /** PRIVATE: not inherited, but it still EXISTS inside every subclass object. */
    private final int registrationId;

    private static int nextRegistrationId = 1000;

    /** @param name the animal's name */
    Animal(String name) {
        this.name = name;
        this.registrationId = nextRegistrationId++;
    }

    /** Prints what this animal does when eating. */
    void eat() {
        System.out.println(name + " is eating");
    }

    /** Prints what this animal does when sleeping. */
    void sleep() {
        System.out.println(name + " is sleeping");
    }

    /** Prints a description. Subclasses extend this with super.describe(). */
    void describe() {
        System.out.println("an animal called " + name);
    }

    /** @return the name */
    String getName() {
        return name;
    }

    /**
     * A public getter that reads a PRIVATE field. Subclasses inherit this
     * method and can therefore read the field they cannot name.
     *
     * @return the registration id
     */
    int getRegistrationId() {
        return registrationId;
    }
}

/** Extends Animal and implements Pet - one superclass, any number of interfaces. */
class Dog extends Animal implements Pet {

    private final String breed;

    /**
     * @param name  the dog's name
     * @param breed the dog's breed
     */
    Dog(String name, String breed) {
        super(name);            // MUST be the first statement
        this.breed = breed;
    }

    /** Dog's own behaviour, not present on Animal. */
    void bark() {
        System.out.println(name + " says woof");
    }

    /**
     * EXTENDS the superclass version rather than replacing it. Without the
     * super.describe() call, Animal's version would never run.
     */
    @Override
    void describe() {
        super.describe();
        System.out.println("      ...and specifically a " + breed);
    }

    @Override
    public String fetch() {
        return name + " brings back the ball";
    }
}

/** A second subclass, to show one method accepting the whole family. */
class Cat extends Animal {

    /** @param name the cat's name */
    Cat(String name) {
        super(name);
    }

    /** REPLACES the superclass version entirely - no super call. */
    @Override
    void describe() {
        System.out.println("a cat called " + name + " (Animal's version never ran)");
    }
}

// ----------------------------------------------------------------------------
// SECTION 4 - FIELD SHADOWING
// ----------------------------------------------------------------------------

/** Declares a field that the subclass will shadow. */
class ShadowParent {

    /** Public purely so the demonstration can read it from main. */
    public String label = "PARENT's field";

    /** Overridden by the subclass - methods behave differently from fields. */
    void describe() {
        System.out.println("ShadowParent.describe()");
    }
}

/** Shadows the field and overrides the method, so the two can be compared. */
class ShadowChild extends ShadowParent {

    /**
     * This SHADOWS ShadowParent.label - it does not override it. Both fields
     * exist in this object simultaneously.
     */
    public String label = "CHILD's field";

    @Override
    void describe() {
        System.out.println("ShadowChild.describe()   <- the RUNTIME type won");
    }

    /** Prints every way of naming a `label` from inside the subclass. */
    void showAll() {
        System.out.println("  From inside ShadowChild:");
        System.out.println("    label            -> " + label);
        System.out.println("    this.label       -> " + this.label);
        System.out.println("    super.label      -> " + super.label);
        System.out.println("    ((ShadowParent) this).label -> " + ((ShadowParent) this).label);
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - protected ABUSE
// ----------------------------------------------------------------------------

/** Any subclass anywhere can do this to any protected field. */
class MaliciousAnimal extends Animal {

    /** @param name the animal's name */
    MaliciousAnimal(String name) {
        super(name);
    }

    /** Reaches into the superclass's protected state and corrupts it. */
    void corrupt() {
        name = null;                       // no validation, no way to stop it
        name = "!!! CORRUPTED BY A SUBCLASS !!!";
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - LISKOV
// ----------------------------------------------------------------------------

/** A mutable rectangle with independent width and height. */
class MutableRectangle {

    protected int width;
    protected int height;

    /** @param width the width  @param height the height */
    MutableRectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /** @param width the new width */
    void setWidth(int width) {
        this.width = width;
    }

    /** @param height the new height */
    void setHeight(int height) {
        this.height = height;
    }

    /** @return width times height */
    int getArea() {
        return width * height;
    }
}

/**
 * Mathematically a square IS a rectangle. In code, a mutable square cannot
 * substitute for a mutable rectangle: keeping the sides equal is exactly the
 * behaviour that breaks the superclass's contract.
 */
class MutableSquare extends MutableRectangle {

    /** @param side the length of every side */
    MutableSquare(int side) {
        super(side, side);
    }

    @Override
    void setWidth(int width) {
        this.width = width;
        this.height = width;      // must stay square - and that is the problem
    }

    @Override
    void setHeight(int height) {
        this.width = height;
        this.height = height;
    }
}

// ----------------------------------------------------------------------------
// SECTION 6 - THE FRAGILE BASE CLASS
// ----------------------------------------------------------------------------

/**
 * Counts additions by extending HashSet. It double-counts, because it depended
 * on an implementation detail: that HashSet.addAll does not call add().
 *
 * @param <E> the element type
 */
class InheritedCountingSet<E> extends HashSet<E> {

    private int addCount = 0;

    @Override
    public boolean add(E element) {
        addCount++;
        return super.add(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        addCount += collection.size();
        // HashSet.addAll internally calls add() for each element, and add() is
        // OVERRIDDEN, so our counter is incremented a second time per element.
        return super.addAll(collection);
    }

    /** @return how many additions were counted */
    int getAddCount() {
        return addCount;
    }
}

/**
 * The same feature by composition. It holds a Set rather than being one, so
 * whatever HashSet does internally is completely invisible to it.
 *
 * @param <E> the element type
 */
class ComposedCountingSet<E> {

    private final Set<E> delegate = new HashSet<>();
    private int addCount = 0;

    /**
     * @param element the element to add
     * @return true if the set changed
     */
    boolean add(E element) {
        addCount++;
        return delegate.add(element);
    }

    /**
     * @param collection the elements to add
     * @return true if the set changed
     */
    boolean addAll(Collection<? extends E> collection) {
        addCount += collection.size();
        return delegate.addAll(collection);   // internals are invisible to us
    }

    /** @return how many additions were counted */
    int getAddCount() {
        return addCount;
    }

    /** @return the current size */
    int size() {
        return delegate.size();
    }
}

// ----------------------------------------------------------------------------
// SECTION 7 - COMPOSITION SWAPPABLE AT RUNTIME
// ----------------------------------------------------------------------------

/** A behaviour that can be swapped at runtime - impossible with inheritance. */
interface Voice {
    /** @param message what to say  @return how it comes out */
    String say(String message);
}

/** One implementation of Voice. */
class LoudVoice implements Voice {
    @Override
    public String say(String message) {
        return message.toUpperCase() + "!!!";
    }
}

/** Another implementation of Voice. */
class QuietVoice implements Voice {
    @Override
    public String say(String message) {
        return "(" + message.toLowerCase() + ")";
    }
}

/** HAS-A voice rather than IS-A voice, so the voice can be replaced. */
class Robot {

    private Voice voice;

    /** @param voice the initial voice */
    Robot(Voice voice) {
        this.voice = voice;
    }

    /** @param voice the replacement voice - impossible with inheritance */
    void setVoice(Voice voice) {
        this.voice = voice;
    }

    /** Speaks using whichever voice is currently installed. */
    void speak() {
        System.out.println("    " + voice.getClass().getSimpleName() + ": "
                + voice.say("hello"));
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add a Bird class extending Animal with a fly() method. Then add a Penguin
 *    extending Bird. Now explain the problem, and fix it without inheritance.
 *
 * 2. Remove `super(name)` from Dog's constructor and read the compile error.
 *    Then give Animal a no-arg constructor and see the error disappear - and
 *    the name become null. Which failure would you rather have?
 *
 * 3. Make MutableRectangle and MutableSquare immutable (final fields, no
 *    setters, methods returning new instances). Re-run
 *    checkRectangleContract. Explain why the Liskov violation vanished.
 *
 * 4. Add a `removeCount` to both counting sets. Does the inherited version
 *    have the same double-counting problem for remove? Find out by reading
 *    HashSet's source, then by testing. Note which was faster.
 *
 * 5. Change Animal's `protected String name` to private with a protected
 *    getter. Which classes in this file stop compiling? Is that an improvement?
 *
 * 6. Write a Playlist class two ways: extending ArrayList<Song>, and holding
 *    one. Then try to add a rule "no duplicate songs" to each. Notice how many
 *    inherited methods you have to override in the first version.
 * ============================================================================
 */
