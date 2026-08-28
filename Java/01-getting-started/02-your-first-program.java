/* ============================================================================
 * 02 - YOUR FIRST PROGRAM, LINE BY LINE
 * ----------------------------------------------------------------------------
 * Companion lesson: 02-your-first-program.md
 *
 * RUN IT:
 *     java Java/01-getting-started/02-your-first-program.java
 *
 * COMPILE IT THE CLASSIC WAY (to see the .class files appear):
 *     javac -d out Java/01-getting-started/02-your-first-program.java
 *     java -cp out FirstProgram
 *
 * This file demonstrates the anatomy of a Java source file: the class, the
 * main method, printing, comments, multiple classes in one file, and the
 * errors you will meet in your first week.
 * ============================================================================
 */

/**
 * This is a JAVADOC comment - note the TWO asterisks on the opening line.
 * <p>
 * The `javadoc` tool reads these and generates a browsable HTML documentation
 * site. Try it yourself:
 * <pre>
 *     javadoc -d docs Java/01-getting-started/02-your-first-program.java
 * </pre>
 * Then open docs/index.html in a browser. This is exactly how the official
 * Java API documentation is produced.
 *
 * @author Danish Husain
 * @version 1.0
 */
class FirstProgram {

    /**
     * The entry point. The JVM looks for this exact signature.
     *
     * @param args command-line arguments supplied after the class name
     */
    public static void main(String[] args) {

        /* --------------------------------------------------------------------
         * SECTION 1 - THE CLASSIC HELLO WORLD, DECONSTRUCTED
         * --------------------------------------------------------------------
         * Read the line below as a sentence:
         *   "In the System class, take the `out` stream, and call `println`
         *    on it, passing the text Hello, World!"
         *
         *   System   -> a built-in class from java.lang (auto-imported)
         *   .out     -> a static field of System; a PrintStream to your console
         *   .println -> a method: print the argument, then a newline
         *   "..."    -> a String literal. DOUBLE quotes. Single quotes are chars.
         *   ;        -> statement terminator. Mandatory, never optional.
         * ------------------------------------------------------------------*/

        System.out.println("Hello, World!");


        /* --------------------------------------------------------------------
         * SECTION 2 - print VS println VS printf
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("--- print vs println ---");

        // print() does NOT add a newline, so these three land on one line.
        System.out.print("These ");
        System.out.print("words ");
        System.out.print("share a line.");
        System.out.println();   // an empty println() just ends the line

        // println() always ends the line, so these get a line each.
        System.out.println("This one gets its own line.");
        System.out.println("So does this one.");

        // printf() formats. %s = string, %d = whole number, %n = newline.
        // Lesson 07 covers the full format-specifier language.
        System.out.printf("Formatted: %s is %d years old.%n", "Java", 31);


        /* --------------------------------------------------------------------
         * SECTION 3 - THE OUTPUT STREAM VS THE ERROR STREAM
         * --------------------------------------------------------------------
         * System.out and System.err are DIFFERENT streams. On your terminal
         * they look identical, but they can be redirected separately:
         *
         *     java ThisFile.java > results.txt 2> problems.txt
         *
         * Everything from System.out lands in results.txt, everything from
         * System.err lands in problems.txt. Rule of thumb: results go to `out`,
         * diagnostics and failures go to `err`.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("--- out vs err ---");
        System.out.println("[out] This is a normal result.");
        System.err.println("[err] This is a diagnostic message.");


        /* --------------------------------------------------------------------
         * SECTION 4 - WHITESPACE IS FREE-FORM (BUT DISCIPLINE IS NOT)
         * --------------------------------------------------------------------
         * All three statements below are IDENTICAL to the compiler. Only the
         * semicolons and braces matter; the layout is for humans.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("--- three identical statements ---");

        System.out.println("Written normally");

            System.out.println(   "Written with silly spacing"   )   ;

        System.out
                .println("Written across multiple lines");

        // The compiler accepts all three. Your code reviewer accepts one.


        /* --------------------------------------------------------------------
         * SECTION 5 - CALLING ANOTHER CLASS DEFINED IN THIS SAME FILE
         * --------------------------------------------------------------------
         * A single .java file may hold MANY classes, but at most ONE `public`
         * class. Here we use the Greeter class defined at the bottom of the
         * file - no import needed, because it is in the same package.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("--- using another class from this file ---");

        // `new Greeter(...)` creates an OBJECT (an instance) of the class.
        // Objects and constructors are lessons 21 and 22; this is a preview.
        Greeter greeter = new Greeter("Danish");

        // Calling an INSTANCE method: it belongs to this particular object.
        System.out.println(greeter.buildGreeting());

        // Calling a STATIC method: it belongs to the class itself, so no
        // object is needed. Note we call it on the class name, not a variable.
        System.out.println(Greeter.describeJava());


        /* --------------------------------------------------------------------
         * SECTION 6 - CASE SENSITIVITY IS ABSOLUTE
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("--- case sensitivity ---");

        int score = 10;
        int Score = 20;   // A COMPLETELY DIFFERENT VARIABLE. Legal, but terrible style.

        System.out.println("score (lowercase s) = " + score);
        System.out.println("Score (uppercase S) = " + Score);
        System.out.println("Java sees these as two unrelated variables.");


        /* --------------------------------------------------------------------
         * SECTION 7 - THE ERRORS YOU WILL MEET IN WEEK ONE
         * --------------------------------------------------------------------
         * Every line below is a COMPILE ERROR. Uncomment ONE at a time, run
         * the file, and read what javac says. Learning to read these messages
         * is worth more than memorising syntax.
         * ------------------------------------------------------------------*/

        // System.out.println("no semicolon")
        //   ERROR: ';' expected
        //   NOTE: javac often reports this on the NEXT line. Always check the
        //   line ABOVE the one in the error message.

        // System.out.println(undefinedName);
        //   ERROR: cannot find symbol
        //   You used a name that was never declared. Usually a typo or wrong case.

        // system.out.println("lowercase s");
        //   ERROR: package system does not exist
        //   `System` is capitalised. Case is never negotiable.

        // System.out.println('Hello with single quotes');
        //   ERROR: unclosed character literal
        //   Single quotes make a `char`, which holds exactly ONE character.
        //   Text needs double quotes.

        // int count = "five";
        //   ERROR: incompatible types: String cannot be converted to int

        System.out.println();
        System.out.println("--- reached the end of main; exiting with status 0 ---");
    }
}


/* ============================================================================
 * A SECOND CLASS IN THE SAME FILE
 * ----------------------------------------------------------------------------
 * Perfectly legal. The rules are:
 *   - at most ONE class in a file may be `public`
 *   - only a `public` class forces the filename to match
 *   - each class still gets its own .class file when compiled:
 *         FirstProgram.class  AND  Greeter.class
 *
 * Compile with `javac -d out` and look inside the `out` folder to see both.
 * ============================================================================
 */
class Greeter {

    // A FIELD: a piece of data every Greeter object carries.
    // `private` means only code inside this class can touch it (lesson 24).
    private final String name;

    /**
     * A CONSTRUCTOR. It has no return type and its name matches the class
     * exactly. It runs when you write `new Greeter("...")`. Lesson 22.
     *
     * @param name the person this greeter will greet
     */
    Greeter(String name) {
        // `this.name` is the field; `name` alone is the parameter.
        // Without `this.`, you would be assigning the parameter to itself.
        this.name = name;
    }

    /**
     * An INSTANCE method - it uses `name`, which only exists on an object,
     * so you must have a Greeter object to call it.
     *
     * @return a personalised greeting
     */
    String buildGreeting() {
        // The + operator joins strings together (string concatenation).
        return "Hello, " + name + "! Welcome to Java.";
    }

    /**
     * A STATIC method - it belongs to the CLASS, not to any object, because
     * it needs no per-object data. That is why `main` is static too: the JVM
     * must call it before any object exists. Lesson 25.
     *
     * @return a one-line description of Java
     */
    static String describeJava() {
        // Trying to use `name` here would be a compile error:
        //   non-static variable name cannot be referenced from a static context
        return "Java: statically typed, class-based, runs on the JVM.";
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Change the name passed to `new Greeter(...)` to your own, and re-run.
 *
 * 2. Uncomment each error line in Section 7, one at a time. Read the message,
 *    fix it mentally, then comment it out again. Do all five.
 *
 * 3. Prove that one file produces multiple class files:
 *        javac -d out Java/01-getting-started/02-your-first-program.java
 *        ls out            (Windows PowerShell: dir out)
 *    You should see FirstProgram.class AND Greeter.class.
 *
 * 4. Prove out and err are separate streams:
 *        java Java/01-getting-started/02-your-first-program.java > results.txt
 *    The [err] line still appears on your screen; everything else went to the
 *    file. Open results.txt and confirm the [err] line is absent.
 *
 * 5. Generate real documentation from the Javadoc comments in this file:
 *        javadoc -d docs Java/01-getting-started/02-your-first-program.java
 *    Open docs/index.html. You just produced the same kind of site as the
 *    official Java API docs.
 *
 * 6. Add a third class to this file with its own `main` method. Compile with
 *    javac and run THAT class instead. Notice the JVM runs whichever main you
 *    name on the command line.
 * ============================================================================
 */
