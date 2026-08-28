/* ============================================================================
 * 27 - POLYMORPHISM AND METHOD OVERRIDING
 * ----------------------------------------------------------------------------
 * Companion lesson: 27-polymorphism-and-overriding.md
 *
 * RUN IT:
 *     java Java/06-object-oriented-core/27-polymorphism-and-overriding.java
 *
 * THE ONE SENTENCE:
 *   The method that runs is chosen by the object's RUNTIME type, not by the
 *   reference's DECLARED type.
 *
 * Overriding is the ONLY mechanism in Java that works this way. Section 2
 * puts all four side by side, from a single reference, to prove it.
 * ============================================================================
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

class PolymorphismAndOverriding {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - DYNAMIC DISPATCH
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE RUNTIME TYPE DECIDES");
        System.out.println("=".repeat(74));

        // Every one of these is declared Animal. Every one behaves differently.
        List<Animal> animals = List.of(
                new Dog("Bruno"),
                new Cat("Misty"),
                new Cow("Ganga"),
                new Animal("Generic")
        );

        System.out.println("  One loop, one method call, four different behaviours:");
        System.out.println();
        System.out.println("      for (Animal animal : animals) animal.speak();");
        System.out.println();
        for (Animal animal : animals) {
            System.out.print("    ");
            animal.speak();
        }

        System.out.println();
        System.out.println("  The variable's DECLARED type is Animal every single time.");
        System.out.println("  The JVM looked at the ACTUAL object at the moment of the call.");
        System.out.println("  That is DYNAMIC DISPATCH, also called late binding or runtime");
        System.out.println("  polymorphism.");


        /* ====================================================================
         * SECTION 2 - THE FOUR MECHANISMS, FROM ONE REFERENCE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - ONLY OVERRIDING USES THE RUNTIME TYPE");
        System.out.println("=".repeat(74));

        Dog dog = new Dog("Bruno");
        Animal asAnimal = dog;          // THE SAME OBJECT, declared as Animal

        System.out.println("  Dog dog = new Dog(\"Bruno\");");
        System.out.println("  Animal asAnimal = dog;        // same object, different declared type");
        System.out.println("  asAnimal == dog -> " + (asAnimal == dog));
        System.out.println();

        System.out.print("    OVERRIDING     asAnimal.speak()      -> ");
        asAnimal.speak();
        System.out.println("      ^ RUNTIME type won: Dog's version ran");

        System.out.println("    FIELD ACCESS   asAnimal.category    -> " + asAnimal.category);
        System.out.println("                   dog.category         -> " + dog.category);
        System.out.println("      ^ DECLARED type won: two different answers, one object");

        System.out.print("    STATIC METHOD  ");
        Animal.classify();
        System.out.println("      ^ DECLARED type won: Animal's version, though it is a Dog");

        System.out.println("    OVERLOADING    describe(asAnimal)   -> " + describe(asAnimal));
        System.out.println("                   describe(dog)        -> " + describe(dog));
        System.out.println("      ^ DECLARED type won: the compiler chose before runtime");

        System.out.println();
        System.out.printf("    %-22s %-22s %s%n", "MECHANISM", "CHOSEN BY", "WHEN");
        System.out.printf("    %-22s %-22s %s%n", "overriding", "RUNTIME type", "runtime");
        System.out.printf("    %-22s %-22s %s%n", "overloading", "declared type", "compile time");
        System.out.printf("    %-22s %-22s %s%n", "field access", "declared type", "compile time");
        System.out.printf("    %-22s %-22s %s%n", "static methods", "declared type", "compile time");
        System.out.println();
        System.out.println("  Every confusing behaviour in this area comes from expecting one");
        System.out.println("  of the bottom three to behave like the top one.");


        /* ====================================================================
         * SECTION 3 - WHY POLYMORPHISM EXISTS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE OPEN/CLOSED PRINCIPLE");
        System.out.println("=".repeat(74));

        System.out.println("  WITHOUT polymorphism - an instanceof chain:");
        System.out.println();
        for (Animal animal : animals) {
            System.out.println("    " + soundWithoutPolymorphism(animal));
        }
        System.out.println();
        System.out.println("    Add a new animal and you must EDIT this method - and every");
        System.out.println("    other method shaped like it, scattered across the codebase.");

        System.out.println();
        System.out.println("  WITH polymorphism:");
        System.out.println();
        System.out.println("      void makeSound(Animal a) { a.speak(); }");
        System.out.println();
        System.out.println("    Add a new Animal subclass and this method DOES NOT CHANGE.");

        // Proving it: a class that did not exist when the loop was written.
        System.out.println();
        System.out.println("  Adding a Duck, written after the loop above:");
        System.out.print("    ");
        new Duck("Donald").speak();
        System.out.print("    ");
        soundWithPolymorphism(new Duck("Donald"));
        System.out.println();
        System.out.println("    soundWithPolymorphism() needed no changes at all. That is");
        System.out.println("    the OPEN/CLOSED PRINCIPLE - open for extension, closed for");
        System.out.println("    modification - and it is the 'O' in SOLID.");
        System.out.println();
        System.out.println("    An instanceof chain is a genuine code smell. When you see");
        System.out.println("    one, ask whether the behaviour belongs on the objects.");


        /* ====================================================================
         * SECTION 4 - THE RULES OF OVERRIDING
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHAT COUNTS AS AN OVERRIDE");
        System.out.println("=".repeat(74));

        System.out.printf("    %-28s %s%n", "same name", "exactly");
        System.out.printf("    %-28s %s%n", "same parameter list", "exactly - different params = OVERLOAD");
        System.out.printf("    %-28s %s%n", "return type", "same, or a SUBTYPE (covariant)");
        System.out.printf("    %-28s %s%n", "access", "same or WIDER - never narrower");
        System.out.printf("    %-28s %s%n", "checked exceptions", "same, narrower, or fewer");
        System.out.printf("    %-28s %s%n", "not static", "statics are HIDDEN, not overridden");
        System.out.printf("    %-28s %s%n", "not final", "final methods cannot be overridden");
        System.out.printf("    %-28s %s%n", "not private", "invisible to subclasses");

        System.out.println();
        System.out.println("  COVARIANT RETURN TYPES - returning something more specific:");
        Animal animalOffspring = new Animal("parent").reproduce();
        Dog dogOffspring = new Dog("parent").reproduce();     // no cast needed!
        System.out.println("    new Animal().reproduce() -> " + animalOffspring.getClass().getSimpleName());
        System.out.println("    new Dog().reproduce()    -> " + dogOffspring.getClass().getSimpleName()
                + "   returned as a Dog, no cast");
        System.out.println("    Legal since Java 5, and genuinely useful for builders.");

        System.out.println();
        System.out.println("  ACCESS CAN WIDEN, NEVER NARROW:");
        System.out.println("    class Animal { protected void eat() { } }");
        System.out.println("    class Dog    { public    void eat() { } }   // widening: FINE");
        System.out.println("    class Cat    { private   void eat() { } }   // ERROR");
        System.out.println("      'attempting to assign weaker access privileges'");
        System.out.println();
        System.out.println("    Narrowing would break substitutability: code holding an");
        System.out.println("    Animal reference expects to be able to call eat().");

        System.out.println();
        System.out.println("  CHECKED EXCEPTIONS CAN ONLY SHRINK:");
        System.out.println("    class Animal { void eat() throws IOException { } }");
        System.out.println("    Dog:  throws FileNotFoundException  // narrower: FINE");
        System.out.println("    Cow:  throws nothing                // fewer:    FINE");
        System.out.println("    Cat:  throws SQLException           // ERROR: new checked type");
        System.out.println();
        System.out.println("    Same reasoning: a caller holding an Animal catches");
        System.out.println("    IOException and would be blindsided by anything else.");
        System.out.println("    UNCHECKED exceptions are unrestricted - any method may throw");
        System.out.println("    any RuntimeException without declaring it.");

        System.out.println();
        System.out.println("  Demonstrating the narrowing that IS allowed:");
        try {
            new Dog("Bruno").feedFromFile("dinner.txt");
        } catch (IOException e) {
            System.out.println("    Dog.feedFromFile threw " + e.getClass().getSimpleName()
                    + " - a SUBTYPE of the declared IOException");
        }


        /* ====================================================================
         * SECTION 5 - @Override IS NOT OPTIONAL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - ALWAYS WRITE @Override");
        System.out.println("=".repeat(74));

        System.out.println("  A typo with no annotation creates a NEW method, silently:");
        System.out.println();
        System.out.println("      class Animal { void speak() { ... } }");
        System.out.println("      class Sheep  { void speek() { ... } }   // typo!");
        System.out.println();

        Animal sheep = new Sheep("Dolly");
        System.out.print("    Animal sheep = new Sheep(); sheep.speak() -> ");
        sheep.speak();
        System.out.println("      ^ Animal's version ran. The Sheep 'override' is a");
        System.out.println("        completely unrelated method nobody calls.");
        System.out.println();
        System.out.println("    Adding @Override turns that into an immediate compile error:");
        System.out.println("      'method does not override or implement a method from a");
        System.out.println("       supertype'");

        System.out.println();
        System.out.println("  @Override has NO runtime effect whatsoever. Its entire value is");
        System.out.println("  that the COMPILER CHECKS YOUR CLAIM. Without it, these all fail");
        System.out.println("  silently:");
        System.out.println("    - a typo in the method name");
        System.out.println("    - a wrong parameter type");
        System.out.println("    - a superclass method being renamed later");
        System.out.println();
        System.out.println("  One line, and it catches a whole class of invisible bugs.");

        /* --------------------------------------------------------------------
         * THE CLASSIC equals MISTAKE.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("  THE CLASSIC VERSION OF THIS BUG - equals:");

        BadPoint badA = new BadPoint(1, 2);
        BadPoint badB = new BadPoint(1, 2);
        Object badBAsObject = badB;

        System.out.println("    badA.equals(badB)                    -> " + badA.equals(badB)
                + "    (the OVERLOAD ran)");
        System.out.println("    badA.equals((Object) badB)           -> " + badA.equals(badBAsObject)
                + "   (Object.equals ran: identity)");
        System.out.println("    List.of(badA).contains(badB)         -> "
                + List.of(badA).contains(badB) + "   <- collections use equals(Object)");

        GoodPoint goodA = new GoodPoint(1, 2);
        GoodPoint goodB = new GoodPoint(1, 2);
        System.out.println();
        System.out.println("    With a correct equals(Object):");
        System.out.println("    goodA.equals(goodB)                  -> " + goodA.equals(goodB));
        System.out.println("    List.of(goodA).contains(goodB)       -> "
                + List.of(goodA).contains(goodB) + "    correct");
        System.out.println();
        System.out.println("    `public boolean equals(BadPoint other)` OVERLOADS rather than");
        System.out.println("    overrides, because Object.equals takes an Object. @Override");
        System.out.println("    catches it instantly. Lesson 33 covers the full contract.");


        /* ====================================================================
         * SECTION 6 - HOW DISPATCH WORKS, AND WHAT IT COSTS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - VTABLES AND PERFORMANCE");
        System.out.println("=".repeat(74));

        System.out.println("  Each class has a VIRTUAL METHOD TABLE - an array of pointers to");
        System.out.println("  its method implementations. Every object header points at its");
        System.out.println("  class, and therefore at its vtable:");
        System.out.println();
        System.out.println("    Dog object --> Dog class --> vtable");
        System.out.println("                                  [0] speak() -> Dog.speak");
        System.out.println("                                  [1] eat()   -> Animal.eat");
        System.out.println("                                  [2] fetch() -> Dog.fetch");
        System.out.println();
        System.out.println("  animal.speak() compiles to `invokevirtual`, which reads a fixed");
        System.out.println("  slot in whatever vtable the object points at. A couple of memory");
        System.out.println("  reads - fast, though not free.");

        int iterations = 50_000_000;

        // Monomorphic: only ever ONE type at this call site. The JIT can inline.
        Animal monomorphic = new Dog("Bruno");
        long startMono = System.nanoTime();
        long monoTotal = 0;
        for (int i = 0; i < iterations; i++) {
            monoTotal += monomorphic.legCount();
        }
        long monoMillis = (System.nanoTime() - startMono) / 1_000_000;

        // Megamorphic: the call site sees many types, so the JIT cannot inline.
        Animal[] many = {new Dog("a"), new Cat("b"), new Cow("c"), new Duck("d"), new Sheep("e")};
        long startMega = System.nanoTime();
        long megaTotal = 0;
        for (int i = 0; i < iterations; i++) {
            megaTotal += many[i % many.length].legCount();
        }
        long megaMillis = (System.nanoTime() - startMega) / 1_000_000;

        System.out.println();
        System.out.printf("  %,d virtual calls:%n", iterations);
        System.out.println("    MONOMORPHIC (one type at the call site) : " + monoMillis + " ms");
        System.out.println("    MEGAMORPHIC (five types)                : " + megaMillis + " ms");
        System.out.println("    (checksums " + monoTotal + " and " + megaTotal + ")");

        System.out.println();
        System.out.println("  BE HONEST ABOUT THAT NUMBER: the megamorphic loop also does an");
        System.out.println("  array index and a modulo that the monomorphic one does not, so");
        System.out.println("  the gap is NOT purely dispatch cost. It is still the right");
        System.out.println("  shape - inlining is what makes the difference - but a real");
        System.out.println("  measurement needs JMH, which controls for exactly this.");
        System.out.println();
        System.out.println("  THE JIT OPTIMISES BY CALL-SITE SHAPE:");
        System.out.println("    MONOMORPHIC - one type ever seen: the method is INLINED and");
        System.out.println("                  the virtual call disappears completely");
        System.out.println("    BIMORPHIC   - two types: a cheap check plus two inlined bodies");
        System.out.println("    MEGAMORPHIC - many types: a real vtable lookup, hard to inline");
        System.out.println();
        System.out.println("  DO NOT AVOID POLYMORPHISM FOR PERFORMANCE. Write it clearly and");
        System.out.println("  let the JIT work. `final` lets the JIT skip the lookup, but");
        System.out.println("  modern JITs already detect this via class-hierarchy analysis.");
        System.out.println("  Use final for DESIGN reasons, not speed.");


        /* ====================================================================
         * SECTION 7 - CASTING AND instanceof
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - UPCAST, DOWNCAST, PATTERN MATCHING");
        System.out.println("=".repeat(74));

        Dog realDog = new Dog("Bruno");
        Animal upcast = realDog;                 // implicit, always safe

        System.out.println("    Animal a = dog;        UPCAST   - implicit, cannot fail");
        System.out.println("    Dog d = (Dog) a;       DOWNCAST - explicit, checked at runtime");

        Dog downcast = (Dog) upcast;
        System.out.println("      recovered: " + downcast.getName());

        try {
            Cat wrong = (Cat) upcast;
            System.out.println(wrong);
        } catch (ClassCastException e) {
            System.out.println("    Cat c = (Cat) a;       -> ClassCastException at runtime");
        }

        System.out.println();
        System.out.println("  The modern safe pattern (Java 16+):");
        for (Animal animal : animals) {
            describeWithPatternMatching(animal);
        }
        describeWithPatternMatching(null);
        System.out.println("    instanceof is FALSE for null, so it guards against NPE too.");

        System.out.println();
        System.out.println("  instanceof VS getClass():");
        System.out.println("    dog instanceof Animal            -> " + (realDog instanceof Animal)
                + "    accepts SUBTYPES");
        System.out.println("    dog.getClass() == Dog.class      -> " + (realDog.getClass() == Dog.class));
        System.out.println("    upcast.getClass() == Animal.class -> " + (upcast.getClass() == Animal.class)
                + "   demands EXACTNESS");
        System.out.println("    upcast.getClass()                -> "
                + upcast.getClass().getSimpleName() + "   (the runtime type, always)");
        System.out.println();
        System.out.println("  A BONUS COMPILER LESSON: writing");
        System.out.println("      realDog.getClass() == Animal.class");
        System.out.println("  where realDog is DECLARED Dog does not even compile:");
        System.out.println("      error: incomparable types: Class<CAP#1> and Class<Animal>");
        System.out.println("  getClass() returns Class<? extends Dog>, so the compiler can");
        System.out.println("  PROVE the comparison is always false and refuses it. Generics");
        System.out.println("  caught a bug that would otherwise be a silent `false`.");
        System.out.println();
        System.out.println("    The instanceof/getClass distinction matters enormously when");
        System.out.println("    writing equals(). Lesson 33.");


        /* ====================================================================
         * SECTION 8 - WHEN POLYMORPHISM IS THE WRONG TOOL
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - WHEN NOT TO REACH FOR IT");
        System.out.println("=".repeat(74));

        System.out.println("  DO NOT build a hierarchy for two cases.");
        System.out.println("    A boolean or an enum is usually clearer than two subclasses.");
        System.out.println();
        System.out.println("  DO NOT inherit purely to share code.");
        System.out.println("    That is what composition and static helpers are for (lesson 26).");
        System.out.println();
        System.out.println("  PREFER INTERFACES to abstract classes for polymorphism.");
        System.out.println("    A class implements many interfaces but extends one class, so");
        System.out.println("    interfaces leave your callers more room. Lessons 28 and 29.");
        System.out.println();
        System.out.println("  SEALED TYPES (Java 17+) make switch a real alternative.");
        System.out.println("    When the set of subtypes is FIXED and known, a sealed");
        System.out.println("    hierarchy plus an exhaustive switch is type-checked, keeps");
        System.out.println("    related logic in ONE place, and the compiler tells you when");
        System.out.println("    you miss a case. Lesson 37.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 27.");
        System.out.println("=".repeat(74));
    }

    // ------------------------------------------------------------------------
    // SUPPORT METHODS
    // ------------------------------------------------------------------------

    /** Overload chosen by the DECLARED type. @param animal any animal @return a label */
    static String describe(Animal animal) {
        return "describe(Animal)";
    }

    /** Overload chosen by the DECLARED type. @param dog a dog @return a label */
    static String describe(Dog dog) {
        return "describe(Dog)";
    }

    /**
     * The version everyone writes before they understand polymorphism. Every
     * new subclass forces an edit here.
     *
     * @param animal the animal to describe
     * @return the sound it makes
     */
    static String soundWithoutPolymorphism(Animal animal) {
        if (animal instanceof Dog) return "instanceof chain says: woof";
        if (animal instanceof Cat) return "instanceof chain says: meow";
        if (animal instanceof Cow) return "instanceof chain says: moo";
        return "instanceof chain says: ...I do not know this one";
    }

    /**
     * The version that never needs editing again.
     *
     * @param animal any animal, including ones written after this method
     */
    static void soundWithPolymorphism(Animal animal) {
        animal.speak();
    }

    /**
     * Pattern matching for instanceof: tests and declares in one step, and is
     * null-safe because instanceof is false for null.
     *
     * @param animal any animal, or null
     */
    static void describeWithPatternMatching(Animal animal) {
        if (animal instanceof Dog dog) {
            System.out.println("    a Dog: " + dog.fetch());
        } else if (animal instanceof Cat cat) {
            System.out.println("    a Cat: " + cat.getName() + " ignores you");
        } else if (animal instanceof Animal other) {
            System.out.println("    some other Animal: " + other.getName());
        } else {
            System.out.println("    null - and instanceof handled it without an NPE");
        }
    }
}

// ----------------------------------------------------------------------------
// THE HIERARCHY
// ----------------------------------------------------------------------------

/** The base type. Every method here is overridable. */
class Animal {

    /** Public so Section 2 can demonstrate field access by declared type. */
    public String category = "Animal's field";

    private final String name;

    /** @param name the animal's name */
    Animal(String name) {
        this.name = name;
    }

    /** Prints this animal's sound. The method Section 1 dispatches on. */
    void speak() {
        System.out.println("Animal.speak()  - some generic noise");
    }

    /**
     * A COVARIANT RETURN demonstration: subclasses narrow this to their own type.
     *
     * @return a new animal of the same kind
     */
    Animal reproduce() {
        return new Animal("offspring");
    }

    /**
     * Declares a broad checked exception, which subclasses may narrow.
     *
     * @param filename where the food is
     * @throws IOException if reading fails
     */
    void feedFromFile(String filename) throws IOException {
        throw new IOException("generic read failure");
    }

    /** @return how many legs this animal has */
    int legCount() {
        return 4;
    }

    /** @return the name */
    String getName() {
        return name;
    }

    /** A STATIC method - hidden by subclasses, never overridden. */
    static void classify() {
        System.out.println("Animal.classify() [static]");
    }
}

/** Overrides speak, narrows the return type and the checked exception. */
class Dog extends Animal {

    /** SHADOWS Animal.category - resolved by the declared type. */
    public String category = "Dog's field";

    /** @param name the dog's name */
    Dog(String name) {
        super(name);
    }

    @Override
    void speak() {
        System.out.println("Dog.speak()     - woof");
    }

    /** COVARIANT RETURN: narrower than Animal, so callers need no cast.
     *  @return a new Dog */
    @Override
    Dog reproduce() {
        return new Dog("puppy");
    }

    /**
     * Narrows the checked exception from IOException to a subtype. Legal.
     *
     * @param filename where the food is
     * @throws FileNotFoundException if the file is missing
     */
    @Override
    void feedFromFile(String filename) throws FileNotFoundException {
        throw new FileNotFoundException(filename + " not found");
    }

    /** @return something only a Dog can do */
    String fetch() {
        return getName() + " brings back the ball";
    }

    /** HIDES Animal.classify(); it does not override it. */
    static void classify() {
        System.out.println("Dog.classify() [static - never reached via an Animal reference]");
    }
}

/** Overrides speak and drops the checked exception entirely. */
class Cat extends Animal {

    /** @param name the cat's name */
    Cat(String name) {
        super(name);
    }

    @Override
    void speak() {
        System.out.println("Cat.speak()     - meow");
    }

    /** Throws NO checked exception - fewer is always allowed.
     *  @param filename where the food is */
    @Override
    void feedFromFile(String filename) {
        // Widening the access from package-private is also allowed; narrowing
        // it would be a compile error.
    }
}

/** A third subclass, to make the loop in Section 1 worth looking at. */
class Cow extends Animal {

    /** @param name the cow's name */
    Cow(String name) {
        super(name);
    }

    @Override
    void speak() {
        System.out.println("Cow.speak()     - moo");
    }
}

/** Written "after" the dispatching methods, to prove they need no changes. */
class Duck extends Animal {

    /** @param name the duck's name */
    Duck(String name) {
        super(name);
    }

    @Override
    void speak() {
        System.out.println("Duck.speak()    - quack   <- a class the loop never knew about");
    }

    @Override
    int legCount() {
        return 2;
    }
}

/** Demonstrates the typo bug: speek() does not override speak(). */
class Sheep extends Animal {

    /** @param name the sheep's name */
    Sheep(String name) {
        super(name);
    }

    /**
     * THE TYPO. This does NOT override Animal.speak(). It is a brand-new
     * method that nothing ever calls. Adding @Override would make it a
     * compile error immediately.
     */
    void speek() {
        System.out.println("Sheep.speek()   - baa   <- never called by anything");
    }

    @Override
    int legCount() {
        return 4;
    }
}

// ----------------------------------------------------------------------------
// SECTION 5 - THE equals MISTAKE
// ----------------------------------------------------------------------------

/** equals takes the WRONG parameter type, so it overloads instead of overriding. */
class BadPoint {

    private final int x;
    private final int y;

    /** @param x the x coordinate  @param y the y coordinate */
    BadPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * THE BUG: Object.equals takes an Object, not a BadPoint. This creates a
     * SECOND method and leaves the inherited identity comparison in place - so
     * collections, which call equals(Object), get the wrong one.
     *
     * @param other another point
     * @return true if the coordinates match
     */
    public boolean equals(BadPoint other) {
        return other != null && x == other.x && y == other.y;
    }
}

/** The same class with a correct override. */
class GoodPoint {

    private final int x;
    private final int y;

    /** @param x the x coordinate  @param y the y coordinate */
    GoodPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof GoodPoint point)) return false;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;      // must be consistent with equals - lesson 33
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Add @Override to Sheep.speek(). Read the error, then fix the typo and
 *    confirm the loop in Section 1 picks it up.
 *
 * 2. Try to override Animal.speak() in Dog with `private void speak()`. Read
 *    the error. Then explain it using the word "substitutability".
 *
 * 3. Add `void feedFromFile(String f) throws Exception` to a new subclass.
 *    Read the error. Why is Exception not allowed where IOException is?
 *
 * 4. Add a Horse to the `animals` list without editing soundWithPolymorphism.
 *    Then add it to soundWithoutPolymorphism and count the edits.
 *
 * 5. Fix BadPoint by changing the parameter to Object and adding @Override.
 *    Then check List.contains again - and note you also had to add hashCode.
 *
 * 6. Rerun Section 6's benchmark with `final` on Dog. Does the monomorphic
 *    number change? Now you know whether to use final for performance.
 * ============================================================================
 */
