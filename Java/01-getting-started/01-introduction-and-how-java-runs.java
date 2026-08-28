/* ============================================================================
 * 01 - INTRODUCTION - WHAT JAVA IS AND HOW IT ACTUALLY RUNS
 * ----------------------------------------------------------------------------
 * Companion lesson: 01-introduction-and-how-java-runs.md
 *
 * RUN IT:
 *     java Java/01-getting-started/01-introduction-and-how-java-runs.java
 *
 * TRY IT WITH ARGUMENTS (this file reacts to them):
 *     java Java/01-getting-started/01-introduction-and-how-java-runs.java Danish 2026
 *
 * This program does not just talk about the JVM - it ASKS the JVM about itself
 * and prints the answers. Everything below is real information read from the
 * runtime you are using right now.
 * ============================================================================
 */

// Note: the class is package-private (no `public` keyword) on purpose.
// A `public` class must live in a file of exactly the same name, and
// "01-introduction-and-how-java-runs" is not a legal Java identifier.
// Dropping `public` lets the numbered filename compile with javac too.
class IntroductionToJava {

    public static void main(String[] args) {

        /* --------------------------------------------------------------------
         * SECTION 1 - THE ENTRY POINT ITSELF
         * --------------------------------------------------------------------
         * The JVM found this method by its exact signature:
         *     public static void main(String[] args)
         *
         *   public -> the JVM is outside this class and must be allowed to call it
         *   static -> it is called before any object of this class exists
         *   void   -> the JVM ignores return values (use System.exit(code) instead)
         *   main   -> the fixed name the JVM searches for
         *   String[] args -> command-line arguments; NEVER null, just empty (length 0)
         * ------------------------------------------------------------------*/

        System.out.println("=".repeat(70));
        System.out.println("  LESSON 01 - HOW JAVA RUNS");
        System.out.println("=".repeat(70));

        // `args` is guaranteed non-null, so calling .length on it is always safe.
        System.out.println();
        System.out.println("Command-line arguments received: " + args.length);
        if (args.length == 0) {
            System.out.println("  (none - try re-running with:  ... .java Danish 2026)");
        } else {
            for (int i = 0; i < args.length; i++) {
                // Every argument arrives as a String, even one that looks numeric.
                // "2026" is text here, not the number 2026. Converting it is lesson 04.
                System.out.println("  args[" + i + "] = \"" + args[i] + "\"");
            }
        }


        /* --------------------------------------------------------------------
         * SECTION 2 - ASKING THE JVM ABOUT ITSELF
         * --------------------------------------------------------------------
         * System.getProperty(key) reads a "system property" - a key/value pair
         * the JVM populates at startup describing itself and the machine.
         * This proves the JDK/JRE/JVM layering is a real, inspectable thing.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("  THE RUNTIME YOU ARE USING RIGHT NOW");
        System.out.println("-".repeat(70));

        // printf formats text: %-28s means "a string, left-aligned, 28 chars wide".
        // %n is a newline that adapts to the operating system. Full details in lesson 07.
        System.out.printf("%-28s %s%n", "Java version:",   System.getProperty("java.version"));
        System.out.printf("%-28s %s%n", "Java vendor:",    System.getProperty("java.vendor"));
        System.out.printf("%-28s %s%n", "JVM name:",       System.getProperty("java.vm.name"));
        System.out.printf("%-28s %s%n", "JVM version:",    System.getProperty("java.vm.version"));
        System.out.printf("%-28s %s%n", "Operating system:",
                System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        System.out.printf("%-28s %s%n", "JDK install location:", System.getProperty("java.home"));

        // WORA in one line: the SAME bytecode printed different OS values on
        // whatever machine you just ran it on. Nothing was recompiled.


        /* --------------------------------------------------------------------
         * SECTION 3 - THE MEMORY THE JVM GAVE YOU
         * --------------------------------------------------------------------
         * Runtime.getRuntime() hands you a live handle on the JVM process.
         * These numbers change while the program runs - that is the garbage
         * collector and the heap manager doing their job. Full detail: lesson 68.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("  MEMORY AND CPU (managed for you: no malloc, no free)");
        System.out.println("-".repeat(70));

        Runtime runtime = Runtime.getRuntime();

        // Bytes are unreadable, so convert to megabytes. 1 MB = 1024 * 1024 bytes.
        long megabyte = 1024L * 1024L;

        long maxMemory   = runtime.maxMemory();    // ceiling the heap may grow to
        long totalMemory = runtime.totalMemory();  // currently reserved from the OS
        long freeMemory  = runtime.freeMemory();   // unused portion of totalMemory
        long usedMemory  = totalMemory - freeMemory;

        System.out.printf("%-28s %d%n",    "Processors available:", runtime.availableProcessors());
        System.out.printf("%-28s %d MB%n", "Max heap (-Xmx):",      maxMemory   / megabyte);
        System.out.printf("%-28s %d MB%n", "Heap reserved now:",    totalMemory / megabyte);
        System.out.printf("%-28s %d MB%n", "Heap actually in use:", usedMemory  / megabyte);


        /* --------------------------------------------------------------------
         * SECTION 4 - WATCHING GARBAGE COLLECTION HAPPEN
         * --------------------------------------------------------------------
         * We deliberately create a million short-lived objects. In C you would
         * now be responsible for freeing every one of them. In Java, the moment
         * nothing references them they become eligible for collection.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(70));
        System.out.println("  GARBAGE COLLECTION - CREATING 1,000,000 THROWAWAY OBJECTS");
        System.out.println("-".repeat(70));

        long usedBefore = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < 1_000_000; i++) {
            // Underscores in 1_000_000 are a readability feature (Java 7+).
            // The compiler ignores them entirely; the value is one million.

            // Each iteration allocates a new object on the heap...
            String throwaway = "object number " + i;

            // ...and at the end of the iteration `throwaway` goes out of scope,
            // so nothing references that object any more. It is now GARBAGE.
            // We never free it. We never can. The GC will handle it.
            if (throwaway.isEmpty()) {
                System.out.println("unreachable - exists only so the loop body is not optimised away");
            }
        }

        long usedAfter = runtime.totalMemory() - runtime.freeMemory();
        System.out.printf("%-28s %d MB%n", "Heap in use before loop:", usedBefore / megabyte);
        System.out.printf("%-28s %d MB%n", "Heap in use after loop:",  usedAfter  / megabyte);
        System.out.println();
        System.out.println("  Notice the heap did NOT grow by ~1,000,000 objects' worth.");
        System.out.println("  The collector reclaimed them mid-loop, invisibly, while you");
        System.out.println("  wrote zero lines of memory-management code.");


        /* --------------------------------------------------------------------
         * SECTION 5 - STATICALLY TYPED - PROVEN BY WHAT WILL NOT COMPILE
         * --------------------------------------------------------------------
         * The lines below are commented out because they are COMPILE ERRORS.
         * Uncomment one at a time and re-run to meet the compiler. Reading these
         * errors is the fastest way to learn Java's type rules.
         * ------------------------------------------------------------------*/

        int wholeNumber = 42;
        System.out.println();
        System.out.println("A statically typed variable: int wholeNumber = " + wholeNumber);

        // wholeNumber = "forty-two";
        // ERROR: incompatible types: String cannot be converted to int
        // In JavaScript or Python this line would be perfectly legal and would
        // fail much later, at runtime, probably in production. Java refuses to
        // even produce a program. That refusal is the feature.

        // undeclaredVariable = 10;
        // ERROR: cannot find symbol
        // Java has no implicit variable creation. Everything must be declared.

        // int missingSemicolon = 5
        // ERROR: ';' expected


        /* --------------------------------------------------------------------
         * SECTION 6 - EXIT STATUS
         * --------------------------------------------------------------------
         * `main` returns void, so a program signals success or failure to the
         * operating system with an exit code: 0 means success, anything else
         * means failure. Reaching the end of main normally exits with 0.
         *
         * Check it yourself after running:  echo $?   (Bash)   or   $LASTEXITCODE (PowerShell)
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("  Finished. Exiting with status 0 (success).");
        System.out.println("=".repeat(70));

        // System.exit(0);  // implicit - reaching the end of main does this for you.
        // System.exit(1);  // uncomment to exit with a FAILURE status instead.
    }
}

/* ============================================================================
 * EXERCISES - do these before moving on
 * ----------------------------------------------------------------------------
 * 1. Run the file, then run it again with arguments. Watch Section 1 change.
 *
 * 2. Uncomment the line `wholeNumber = "forty-two";` in Section 5, re-run, and
 *    read the compiler error carefully. Then comment it out again. Repeat for
 *    each commented error. Getting comfortable reading javac output now will
 *    save you hours later.
 *
 * 3. Cap the heap and watch the numbers change:
 *        java -Xmx64m Java/01-getting-started/01-introduction-and-how-java-runs.java
 *
 * 4. See garbage collection narrate itself:
 *        java -verbose:gc Java/01-getting-started/01-introduction-and-how-java-runs.java
 *    Every line it prints is a collection cycle you never had to write.
 *
 * 5. Prove bytecode is real. Compile, then disassemble:
 *        javac -d out Java/01-getting-started/01-introduction-and-how-java-runs.java
 *        javap -c -p -cp out IntroductionToJava
 *    What you see is the instruction set the JVM actually executes.
 * ============================================================================
 */
