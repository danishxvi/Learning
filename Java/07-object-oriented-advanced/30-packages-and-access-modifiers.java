/* ============================================================================
 * 30 - PACKAGES AND ACCESS MODIFIERS
 * ----------------------------------------------------------------------------
 * Companion lesson: 30-packages-and-access-modifiers.md
 *
 * RUN IT:
 *     java Java/07-object-oriented-advanced/30-packages-and-access-modifiers.java
 *
 * AND RUN THE REAL MULTI-PACKAGE PROJECT - access rules across package
 * boundaries CANNOT be demonstrated inside one file, so this lesson ships a
 * small three-package project alongside it:
 *
 *     cd Java/07-object-oriented-advanced/30-packages-demo
 *     javac -d out src/com/danish/library/model/*.java \
 *                  src/com/danish/library/service/*.java \
 *                  src/com/danish/app/*.java
 *     java -cp out com.danish.app.Main
 *
 * This file covers what CAN be shown in one place: what each modifier means,
 * what imports actually do, and how the classpath works.
 * ============================================================================
 */

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

// A STATIC import: brings in one member so it can be used unqualified.
import static java.lang.Math.PI;

class PackagesAndAccessModifiers {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - WHAT PACKAGE ARE WE IN?
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE DEFAULT PACKAGE");
        System.out.println("=".repeat(74));

        Package thisPackage = PackagesAndAccessModifiers.class.getPackage();
        System.out.println("  This class's package -> "
                + (thisPackage == null ? "(the DEFAULT package - no package statement)"
                                       : thisPackage.getName()));
        System.out.println("  Fully qualified name -> "
                + PackagesAndAccessModifiers.class.getName());
        System.out.println();
        System.out.println("  Compare with a packaged class:");
        System.out.println("    String   -> " + String.class.getName());
        System.out.println("    List     -> " + List.class.getName());
        System.out.println("    Field    -> " + Field.class.getName());

        System.out.println();
        System.out.println("  Every lesson in this repository uses the DEFAULT package, so");
        System.out.println("  each file runs with a single command. That is fine for");
        System.out.println("  learning and unusable in a real project, because classes in");
        System.out.println("  the default package CANNOT BE IMPORTED by any packaged class.");
        System.out.println();
        System.out.println("  A real declaration looks like this, and must be the file's");
        System.out.println("  first statement:");
        System.out.println();
        System.out.println("      package com.danish.library.model;");
        System.out.println();
        System.out.println("  and the file must live at:");
        System.out.println();
        System.out.println("      com/danish/library/model/Book.java");
        System.out.println();
        System.out.println("  Mismatch the two and javac still compiles it, but `java`");
        System.out.println("  cannot find the class - which surfaces as the confusing");
        System.out.println("  'Could not find or load main class'.");


        /* ====================================================================
         * SECTION 2 - THE FOUR ACCESS LEVELS
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE FOUR LEVELS");
        System.out.println("=".repeat(74));

        System.out.printf("    %-20s %-12s %-14s %-18s %s%n",
                "MODIFIER", "same class", "same package", "subclass elsewhere", "anywhere");
        System.out.printf("    %-20s %-12s %-14s %-18s %s%n",
                "private", "yes", "no", "no", "no");
        System.out.printf("    %-20s %-12s %-14s %-18s %s%n",
                "(none)", "yes", "yes", "no", "no");
        System.out.printf("    %-20s %-12s %-14s %-18s %s%n",
                "protected", "yes", "yes", "YES", "no");
        System.out.printf("    %-20s %-12s %-14s %-18s %s%n",
                "public", "yes", "yes", "yes", "yes");

        System.out.println();
        System.out.println("  THE ASYMMETRY THAT SURPRISES PEOPLE:");
        System.out.println("    protected is WIDER than package-private. It is not");
        System.out.println("    'package-private plus subclasses in the package' - it reaches");
        System.out.println("    every class in the package PLUS every subclass ANYWHERE.");

        // Everything in this file is in the same (default) package, so all
        // four levels are visible from here except private.
        AccessDemo demo = new AccessDemo();
        System.out.println();
        System.out.println("  From this class, in the SAME (default) package:");
        System.out.println("    publicField          -> " + demo.publicField);
        System.out.println("    protectedField       -> " + demo.protectedField);
        System.out.println("    packagePrivateField  -> " + demo.packagePrivateField);
        System.out.println("    privateField         -> NOT ACCESSIBLE (compile error)");
        // System.out.println(demo.privateField);
        //   ERROR: privateField has private access in AccessDemo
        System.out.println();
        System.out.println("    ...but the class can expose it itself:");
        System.out.println("    demo.readOwnPrivateField() -> " + demo.readOwnPrivateField());

        // Reflection can prove the private field is really there.
        System.out.println();
        System.out.println("  The private field DOES exist - reflection can see it:");
        for (Field field : AccessDemo.class.getDeclaredFields()) {
            String level = describeModifier(field.getModifiers());
            System.out.printf("    %-22s %s%n", field.getName(), level);
        }
        System.out.println();
        System.out.println("  Access control is a COMPILE-TIME and link-time rule, not an");
        System.out.println("  encryption scheme. Reflection can bypass it (lesson 70), which");
        System.out.println("  is why `private` protects against MISTAKES, not against");
        System.out.println("  attackers with code running in your JVM.");

        System.out.println();
        System.out.println("  TOP-LEVEL CLASSES may only be public or package-private:");
        System.out.println("    private class Foo { }    -> COMPILE ERROR");
        System.out.println("    protected class Foo { }  -> COMPILE ERROR");
        System.out.println("  There is no enclosing scope for those to mean anything in.");
        System.out.println("  (NESTED classes may use all four - lesson 34.)");


        /* ====================================================================
         * SECTION 3 - WHAT public AND protected REALLY COMMIT YOU TO
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - THE COST OF WIDENING");
        System.out.println("=".repeat(74));

        System.out.println("  public IS A PERMANENT PROMISE.");
        System.out.println("    Once published, removing or narrowing it breaks every caller");
        System.out.println("    you have never met. Think before making anything public.");

        System.out.println();
        System.out.println("  protected IS EFFECTIVELY public.");
        System.out.println("    Anyone in the world can write:");
        System.out.println("        class Mine extends YourClass { }");
        System.out.println("    and gain full READ AND WRITE access to every protected member.");
        System.out.println();
        System.out.println("    Watch it happen:");

        AccessDemo target = new AccessDemo();
        System.out.println("      before -> protectedField = " + target.protectedField);
        Subverter subverter = new Subverter();
        subverter.corruptInheritedState();
        System.out.println("      a subclass wrote to its own inherited copy -> "
                + subverter.reportState());
        System.out.println();
        System.out.println("    A protected FIELD hands strangers the ability to corrupt");
        System.out.println("    state your class is supposed to be guarding. Prefer private");
        System.out.println("    fields with protected METHODS, if you need the extension");
        System.out.println("    point at all.");

        System.out.println();
        System.out.println("  PACKAGE-PRIVATE IS UNDERUSED.");
        System.out.println("    It is the right level for classes and members that");
        System.out.println("    collaborate closely but are not part of your published API.");
        System.out.println("    It is also the DEFAULT, which is good language design: you");
        System.out.println("    have to ASK for wider visibility.");


        /* ====================================================================
         * SECTION 4 - WHAT IMPORTS ACTUALLY DO
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - IMPORTS ARE PURELY A COMPILE-TIME CONVENIENCE");
        System.out.println("=".repeat(74));

        System.out.println("  An import does NOTHING at runtime. It loads nothing, costs");
        System.out.println("  nothing, and has no performance implication whatsoever. It");
        System.out.println("  only lets you write a short name instead of a long one.");
        System.out.println();
        System.out.println("  These two lines are IDENTICAL after compilation:");

        List<String> viaImport = List.of("a", "b");
        java.util.List<String> viaFullName = java.util.List.of("a", "b");

        System.out.println("    List<String> x            = " + viaImport);
        System.out.println("    java.util.List<String> y  = " + viaFullName);
        System.out.println("    same class? "
                + (viaImport.getClass() == viaFullName.getClass()));

        System.out.println();
        System.out.println("  WILDCARD IMPORTS import every PUBLIC type in that package -");
        System.out.println("  and NOT sub-packages:");
        System.out.println("      import java.util.*;   does NOT import java.util.concurrent.*");
        System.out.println();
        System.out.println("  The argument against them is COLLISIONS and readability, not");
        System.out.println("  performance:");
        System.out.println("      import java.util.*;");
        System.out.println("      import java.awt.*;");
        System.out.println("      List list;   -> error: reference to List is ambiguous");
        System.out.println();
        System.out.println("  Fix by importing the specific one, or writing the full name.");

        System.out.println();
        System.out.println("  java.lang IS IMPORTED AUTOMATICALLY:");
        System.out.println("    String, Integer, System, Math, Object, Thread, Exception");
        System.out.println("    all live in java.lang, which is why you never import them.");

        System.out.println();
        System.out.println("  STATIC IMPORTS bring in a MEMBER rather than a type:");
        System.out.println("      import static java.lang.Math.PI;");
        System.out.println("    PI        -> " + PI + "   (unqualified, thanks to the import)");
        System.out.println("    Math.PI   -> " + Math.PI + "   (qualified, always works)");
        System.out.println();
        System.out.println("  Use them SPARINGLY. max(3, 7) reads fine; a file full of");
        System.out.println("  unqualified static calls from five different classes does not.");
        System.out.println("  The legitimate cases are test assertions (assertEquals,");
        System.out.println("  assertThat) and heavy maths code.");


        /* ====================================================================
         * SECTION 5 - PROJECT LAYOUT AND THE CLASSPATH
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - LAYOUT AND CLASSPATH");
        System.out.println("=".repeat(74));

        System.out.println("  THE LAYOUT EVERY JAVA PROJECT USES (Maven and Gradle both):");
        System.out.println();
        System.out.println("    project/");
        System.out.println("    +-- src/");
        System.out.println("    |   +-- main/");
        System.out.println("    |   |   +-- java/        <- production source");
        System.out.println("    |   |   +-- resources/   <- config, templates");
        System.out.println("    |   +-- test/");
        System.out.println("    |       +-- java/        <- tests, MIRRORING main's packages");
        System.out.println("    |       +-- resources/");
        System.out.println("    +-- target/ or build/");
        System.out.println("    +-- pom.xml or build.gradle");
        System.out.println();
        System.out.println("  Tests mirror the production packages DELIBERATELY: a test in");
        System.out.println("  the SAME package as the class it tests can reach");
        System.out.println("  package-private members, which lets you test internals without");
        System.out.println("  making them public. Lesson 75.");

        System.out.println();
        System.out.println("  THE CLASSPATH tells the JVM where to look:");
        System.out.println("      java -cp out com.danish.app.Main");
        System.out.println("      java -cp \"out;lib/*\" com.danish.app.Main    (Windows: ;)");
        System.out.println("      java -cp \"out:lib/*\" com.danish.app.Main    (Unix: :)");
        System.out.println();
        System.out.println("  You pass the FULLY QUALIFIED CLASS NAME - never a file path,");
        System.out.println("  never with .class.");
        System.out.println();
        System.out.println("  Your current classpath is: " + System.getProperty("java.class.path"));

        System.out.println();
        System.out.println("  THE THREE CLASSIC ERRORS, AND THEIR DIFFERENT MEANINGS:");
        System.out.printf("    %-34s %s%n", "Could not find or load main class",
                "wrong name, or package/dir mismatch");
        System.out.printf("    %-34s %s%n", "ClassNotFoundException",
                "asked for at runtime, not on the classpath");
        System.out.printf("    %-34s %s%n", "NoClassDefFoundError",
                "present at compile time, missing now -");
        System.out.printf("    %-34s %s%n", "",
                "OR its static initialiser failed (lesson 25)");


        /* ====================================================================
         * SECTION 6 - MODULES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - MODULES (Java 9+), BRIEFLY");
        System.out.println("=".repeat(74));

        System.out.println("  The module system adds a level ABOVE packages:");
        System.out.println();
        System.out.println("      // module-info.java, at the source root");
        System.out.println("      module com.danish.library {");
        System.out.println("          exports com.danish.library.model;   // visible outside");
        System.out.println("          requires java.sql;                  // we depend on this");
        System.out.println("      }");
        System.out.println();
        System.out.println("  A package NOT listed in `exports` is invisible outside the");
        System.out.println("  module EVEN IF ITS CLASSES ARE public. That closes the");
        System.out.println("  long-standing hole where public meant 'visible to absolutely");
        System.out.println("  everything, forever'.");

        System.out.println();
        System.out.println("  The JDK itself is modularised. Your classes are in:");
        System.out.println("    String's module   -> " + String.class.getModule().getName());
        System.out.println("    List's module     -> " + List.class.getModule().getName());
        System.out.println("    this class's module -> "
                + PackagesAndAccessModifiers.class.getModule().getName()
                + "   (the unnamed module - classpath code)");
        System.out.println();
        System.out.println("  That modularisation is why sun.misc.Unsafe and other internals");
        System.out.println("  became inaccessible in Java 9 and broke a great deal of code.");
        System.out.println();
        System.out.println("  MOST APPLICATION PROJECTS DO NOT USE MODULES. They matter for");
        System.out.println("  library authors and for jlink, which builds a minimal custom");
        System.out.println("  runtime containing only the modules you actually use. Knowing");
        System.out.println("  they exist and what module-info.java means is enough for now.");


        /* ====================================================================
         * SECTION 7 - PACKAGE BY FEATURE, NOT BY LAYER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - HOW TO ORGANISE PACKAGES");
        System.out.println("=".repeat(74));

        System.out.println("  BY LAYER - the common default, and usually worse:");
        System.out.println("      com.danish.app.service.OrderService");
        System.out.println("      com.danish.app.service.PaymentService");
        System.out.println("      com.danish.app.repository.OrderRepository");
        System.out.println("      com.danish.app.repository.PaymentRepository");
        System.out.println();
        System.out.println("  BY FEATURE - usually better:");
        System.out.println("      com.danish.app.order.OrderService");
        System.out.println("      com.danish.app.order.OrderRepository");
        System.out.println("      com.danish.app.payment.PaymentService");
        System.out.println("      com.danish.app.payment.PaymentRepository");
        System.out.println();
        System.out.println("  WHY: OrderRepository only ever needs to be visible to");
        System.out.println("  OrderService. In the by-layer version they are in DIFFERENT");
        System.out.println("  packages, so the repository must be public - visible to the");
        System.out.println("  whole application. In the by-feature version it can be");
        System.out.println("  PACKAGE-PRIVATE.");
        System.out.println();
        System.out.println("  Feature packages let you keep more things hidden, and a change");
        System.out.println("  to 'orders' then touches exactly one directory.");

        System.out.println();
        System.out.println("  THE GUIDANCE, IN FOUR LINES:");
        System.out.println("    1. Start private. Widen only when something needs it.");
        System.out.println("    2. Package by feature, not by layer.");
        System.out.println("    3. Avoid protected fields; use private fields and, if you");
        System.out.println("       truly need an extension point, protected methods.");
        System.out.println("    4. Keep the public surface small - every public member is a");
        System.out.println("       permanent commitment.");


        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  Now run the real multi-package project:");
        System.out.println("    cd Java/07-object-oriented-advanced/30-packages-demo");
        System.out.println("    javac -d out src/com/danish/library/model/*.java \\");
        System.out.println("                 src/com/danish/library/service/*.java \\");
        System.out.println("                 src/com/danish/app/*.java");
        System.out.println("    java -cp out com.danish.app.Main");
        System.out.println();
        System.out.println("  It has three genuine packages and shows exactly what each");
        System.out.println("  modifier permits across real package boundaries - which no");
        System.out.println("  single file can demonstrate.");
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 30.");
        System.out.println("=".repeat(74));
    }

    /**
     * Renders a field's access level as readable text, using the reflection
     * Modifier helpers.
     *
     * @param modifiers the raw modifier bits from Field.getModifiers()
     * @return a description such as "private (and it really is there)"
     */
    static String describeModifier(int modifiers) {
        if (Modifier.isPrivate(modifiers))   return "private     <- invisible to this class, but it EXISTS";
        if (Modifier.isProtected(modifiers)) return "protected";
        if (Modifier.isPublic(modifiers))    return "public";
        return "package-private (no modifier)";
    }
}

/**
 * One field at each access level, so Section 2 can show what is reachable
 * from a different class in the same (default) package.
 */
class AccessDemo {

    /** Visible everywhere. */
    public String publicField = "public - visible everywhere";

    /** Visible in this package and to subclasses in ANY package. */
    protected String protectedField = "protected - package + all subclasses";

    /** Visible only in this package. The default, and underused. */
    String packagePrivateField = "package-private - this package only";

    /** Visible only inside this class. */
    private String privateField = "private - this class only";

    /**
     * The class can always expose its own private state deliberately. That is
     * the difference between hiding and forbidding.
     *
     * @return the private field's value
     */
    String readOwnPrivateField() {
        return privateField;
    }
}

/**
 * A subclass, demonstrating that {@code protected} grants full read AND write
 * access to anyone willing to extend the class. In this file it is the same
 * package too, but the demo project proves it works across packages.
 */
class Subverter extends AccessDemo {

    /** Writes to inherited protected state, which nothing can prevent. */
    void corruptInheritedState() {
        this.protectedField = "!!! OVERWRITTEN BY A SUBCLASS !!!";
    }

    /** @return this object's view of the inherited field */
    String reportState() {
        return protectedField;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Run the 30-packages-demo project. Then uncomment one of the commented-out
 *    access attempts in Main.java or EBook.java and read the exact compiler
 *    error. Do all five.
 *
 * 2. In the demo, move EBook from com.danish.library.service into
 *    com.danish.library.model. Which access attempt now compiles that did not
 *    before? Explain why.
 *
 * 3. Add a fourth package com.danish.app.report with a class that tries to
 *    read Book.internalCatalogueCode. Then make it work WITHOUT making the
 *    field public. (Hint: add a public accessor, and think about whether you
 *    should.)
 *
 * 4. Create two classes both named `Config`, in different packages, and use
 *    both in one file. You will have to fully qualify one of them.
 *
 * 5. Compile the demo and inspect the output directory. Note that the .class
 *    files reproduce the package directory structure exactly. Then try running
 *    `java -cp out Main` and read the error.
 *
 * 6. Take a project of your own organised BY LAYER and sketch what it would
 *    look like BY FEATURE. Count how many public classes could become
 *    package-private.
 * ============================================================================
 */
