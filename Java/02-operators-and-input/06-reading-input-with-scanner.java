/* ============================================================================
 * 06 - READING USER INPUT WITH Scanner
 * ----------------------------------------------------------------------------
 * Companion lesson: 06-reading-input-with-scanner.md
 *
 * RUN IT (demonstration mode - no typing required, nothing blocks):
 *     java Java/02-operators-and-input/06-reading-input-with-scanner.java
 *
 * RUN IT INTERACTIVELY (it will actually ask you to type):
 *     java Java/02-operators-and-input/06-reading-input-with-scanner.java --interactive
 *
 * WHY TWO MODES:
 * Every trap in this lesson can be reproduced exactly by pointing a Scanner at
 * a String instead of at the keyboard. A Scanner does not care where its
 * characters come from, so the demonstrations are identical - but repeatable,
 * and they never leave you staring at a blinking cursor. Section 8 then does
 * the real thing if you ask for it.
 * ============================================================================
 */

import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

class ReadingInputWithScanner {

    public static void main(String[] args) {

        /* ====================================================================
         * SECTION 1 - A Scanner READS FROM ANY SOURCE
         * --------------------------------------------------------------------
         * new Scanner(System.in)  -> the keyboard
         * new Scanner("some text") -> a String, which is what makes these
         *                             demonstrations repeatable and testable
         * ==================================================================*/

        System.out.println("=".repeat(74));
        System.out.println("  SECTION 1 - THE READING METHODS");
        System.out.println("=".repeat(74));

        // This String stands in for something a user typed. The \n characters
        // are exactly what pressing Enter produces.
        Scanner tokens = new Scanner("Danish 25 3.14 true Bengaluru India");

        System.out.println("Input: \"Danish 25 3.14 true Bengaluru India\"");
        System.out.println();
        System.out.println("  next()          -> " + tokens.next()
                + "        (one token, up to whitespace)");
        System.out.println("  nextInt()       -> " + tokens.nextInt()
                + "             (token parsed as int)");
        System.out.println("  nextDouble()    -> " + tokens.nextDouble()
                + "           (token parsed as double)");
        System.out.println("  nextBoolean()   -> " + tokens.nextBoolean()
                + "           (accepts TRUE/true/True)");
        System.out.println("  nextLine()      -> \"" + tokens.nextLine()
                + "\"   (the REST of the line, including the leading space)");

        // There is deliberately no nextChar(). Read a token and take character 0.
        Scanner singleChar = new Scanner("Y");
        System.out.println();
        System.out.println("There is no nextChar(). To read one character:");
        System.out.println("  scanner.next().charAt(0)  ->  " + singleChar.next().charAt(0));


        /* ====================================================================
         * SECTION 2 - THE nextInt() / nextLine() TRAP
         * --------------------------------------------------------------------
         * The most famous Scanner bug. Reproduced here exactly as it happens
         * with a keyboard, because the String below contains the same \n
         * characters that pressing Enter produces.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 2 - THE nextInt() / nextLine() TRAP");
        System.out.println("=".repeat(74));

        // Imagine the user typed:  25 <Enter>  Danish Husain <Enter>
        String typedByUser = "25\nDanish Husain\n";

        System.out.println("The user typed:  25 <Enter>  then  Danish Husain <Enter>");
        System.out.println();

        System.out.println("BROKEN version:");
        Scanner broken = new Scanner(typedByUser);
        int brokenAge = broken.nextInt();          // reads 25, LEAVES the \n behind
        String brokenName = broken.nextLine();     // reads up to that leftover \n: empty!
        System.out.println("  int age      = " + brokenAge);
        System.out.println("  String name  = \"" + brokenName + "\"   <- EMPTY. It never waited.");

        System.out.println();
        System.out.println("WHY: the buffer holds  2 5 \\n D a n i s h ...");
        System.out.println("  nextInt() consumes '25' and STOPS, leaving \\n in the buffer.");
        System.out.println("  nextLine() reads 'up to the next newline' - and the very next");
        System.out.println("  character IS a newline, so it returns an empty string.");
        System.out.println("  Every nextX() except nextLine() leaves the trailing newline.");

        System.out.println();
        System.out.println("FIX 1 - consume the leftover newline:");
        Scanner fixedOne = new Scanner(typedByUser);
        int ageOne = fixedOne.nextInt();
        fixedOne.nextLine();                       // throw away the rest of that line
        String nameOne = fixedOne.nextLine();
        System.out.println("  int age     = " + ageOne);
        System.out.println("  String name = \"" + nameOne + "\"   <- correct");

        System.out.println();
        System.out.println("FIX 2 - read everything as lines and parse yourself (preferred):");
        Scanner fixedTwo = new Scanner(typedByUser);
        int ageTwo = Integer.parseInt(fixedTwo.nextLine().trim());
        String nameTwo = fixedTwo.nextLine();
        System.out.println("  int age     = " + ageTwo);
        System.out.println("  String name = \"" + nameTwo + "\"   <- correct");
        System.out.println("  One method, one mental model, no leftovers. This is what");
        System.out.println("  professional code usually does.");


        /* ====================================================================
         * SECTION 3 - VALIDATING INPUT WITH hasNextX()
         * --------------------------------------------------------------------
         * hasNextInt() asks "could the NEXT token be read as an int?" without
         * consuming it. This is the correct way to validate.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 3 - VALIDATING WITH hasNextX()");
        System.out.println("=".repeat(74));

        Scanner mixedTypes = new Scanner("42 hello 3.14 true -7");
        System.out.println("Input: \"42 hello 3.14 true -7\"");
        System.out.println("Classifying each token WITHOUT ever throwing:");

        while (mixedTypes.hasNext()) {
            // Order matters: check the most specific type first. Every int is
            // also a valid double, so testing hasNextDouble() first would
            // classify 42 as a double.
            if (mixedTypes.hasNextInt()) {
                System.out.println("  int     -> " + mixedTypes.nextInt());
            } else if (mixedTypes.hasNextDouble()) {
                System.out.println("  double  -> " + mixedTypes.nextDouble());
            } else if (mixedTypes.hasNextBoolean()) {
                System.out.println("  boolean -> " + mixedTypes.nextBoolean());
            } else {
                System.out.println("  String  -> " + mixedTypes.next());
            }
        }


        /* ====================================================================
         * SECTION 4 - THE INFINITE-LOOP TRAP WHEN CATCHING InputMismatchException
         * --------------------------------------------------------------------
         * nextInt() on bad input throws - and DOES NOT CONSUME the bad token.
         * A naive retry loop therefore spins forever on the same token.
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 4 - WHY NAIVE RETRY LOOPS SPIN FOREVER");
        System.out.println("=".repeat(74));

        Scanner badInput = new Scanner("abc 42");
        System.out.println("Input: \"abc 42\"");
        System.out.println();
        System.out.println("Attempting nextInt() on 'abc' three times WITHOUT consuming it:");

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                int value = badInput.nextInt();
                System.out.println("  attempt " + attempt + ": got " + value);
            } catch (InputMismatchException e) {
                // NOTE: we deliberately do NOT consume the bad token here.
                System.out.println("  attempt " + attempt
                        + ": InputMismatchException - and 'abc' is STILL in the buffer");
            }
        }

        System.out.println();
        System.out.println("In a `while (true)` loop that would never terminate.");
        System.out.println("THE FIX - call scanner.next() in the catch block to discard it:");

        badInput.next();   // finally consume the offending token
        System.out.println("  after scanner.next() discards 'abc', nextInt() gives "
                + badInput.nextInt());

        System.out.println();
        System.out.println("The other Scanner exceptions:");
        Scanner empty = new Scanner("");
        try {
            empty.next();
        } catch (NoSuchElementException e) {
            System.out.println("  NoSuchElementException - no input left at all");
            System.out.println("    (this is what Ctrl+D / Ctrl+Z produces)");
        }

        Scanner closed = new Scanner("data");
        closed.close();
        try {
            closed.next();
        } catch (IllegalStateException e) {
            System.out.println("  IllegalStateException - the scanner was already closed");
        }


        /* ====================================================================
         * SECTION 5 - LOCALE: THE BUG THAT ONLY APPEARS ON OTHER MACHINES
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 5 - LOCALE AFFECTS NUMBER PARSING");
        System.out.println("=".repeat(74));

        System.out.println("Your default locale is: " + Locale.getDefault());
        System.out.println();

        // In US/UK convention the decimal separator is a dot.
        Scanner usStyle = new Scanner("3.14").useLocale(Locale.US);
        System.out.println("  Locale.US    parsing \"3.14\"  ->  " + usStyle.nextDouble());

        // In German convention it is a comma, and the dot groups thousands.
        Scanner germanStyle = new Scanner("3,14").useLocale(Locale.GERMANY);
        System.out.println("  Locale.GERMANY parsing \"3,14\" ->  " + germanStyle.nextDouble());

        // And the same text fails under the other locale.
        Scanner mismatched = new Scanner("3.14").useLocale(Locale.GERMANY);
        System.out.print("  Locale.GERMANY parsing \"3.14\" ->  ");
        if (mismatched.hasNextDouble()) {
            System.out.println(mismatched.nextDouble() + "   (read as 314, the dot grouped thousands)");
        } else {
            System.out.println("cannot parse it as a double");
        }

        System.out.println();
        System.out.println("RULE: for MACHINE input (files, protocols, test data) pin the");
        System.out.println("locale with useLocale(). For genuine USER input, the default");
        System.out.println("locale is correct - it is their number format, not yours.");


        /* ====================================================================
         * SECTION 6 - DELIMITERS: Scanner AS A SIMPLE CSV READER
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 6 - CUSTOM DELIMITERS");
        System.out.println("=".repeat(74));

        // By default the delimiter is whitespace. Change it and Scanner becomes
        // a serviceable parser for simple delimited data.
        Scanner csv = new Scanner("Danish,25,Bengaluru").useDelimiter(",");
        System.out.println("Input: \"Danish,25,Bengaluru\" with delimiter \",\"");
        System.out.println("  name = " + csv.next());
        System.out.println("  age  = " + csv.nextInt());
        System.out.println("  city = " + csv.next());

        // The delimiter is a REGULAR EXPRESSION, so it can be more flexible.
        Scanner messy = new Scanner("a1b22c333d").useDelimiter("\\d+");
        System.out.print("Input: \"a1b22c333d\" with delimiter \"\\d+\" (runs of digits): ");
        while (messy.hasNext()) {
            System.out.print(messy.next() + " ");
        }
        System.out.println();

        // Java 9+ gives you the remaining tokens as a Stream. Lesson 54.
        Scanner streamed = new Scanner("one two three four");
        System.out.println("Java 9+ tokens() as a Stream, uppercased: "
                + streamed.tokens().map(String::toUpperCase).toList());


        /* ====================================================================
         * SECTION 7 - CLOSING: THE System.in RULE
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 7 - WHEN TO CLOSE A Scanner");
        System.out.println("=".repeat(74));

        System.out.println("NEVER close a Scanner that wraps System.in.");
        System.out.println("  Closing it closes System.in itself, for the WHOLE JVM.");
        System.out.println("  Every later `new Scanner(System.in)` then throws");
        System.out.println("  NoSuchElementException, and nothing can reopen it.");
        System.out.println();
        System.out.println("  -> Create ONE Scanner for System.in and pass it around.");
        System.out.println("  -> Do close file-based scanners, using try-with-resources:");
        System.out.println();
        System.out.println("       try (Scanner s = new Scanner(new File(\"data.txt\"))) {");
        System.out.println("           while (s.hasNextLine()) System.out.println(s.nextLine());");
        System.out.println("       }   // closed automatically, even if an exception is thrown");
        System.out.println();
        System.out.println("  Lesson 41 covers try-with-resources properly.");


        /* ====================================================================
         * SECTION 8 - THE REAL THING (only with --interactive)
         * ==================================================================*/

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  SECTION 8 - LIVE KEYBOARD INPUT");
        System.out.println("=".repeat(74));

        boolean interactive = args.length > 0 && args[0].equals("--interactive");

        if (!interactive) {
            System.out.println("Skipped, so this program never blocks waiting for you.");
            System.out.println();
            System.out.println("To run it for real:");
            System.out.println("  java Java/02-operators-and-input/06-reading-input-with-scanner.java --interactive");
        } else {
            runInteractiveDemo();
        }

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of lesson 06.");
        System.out.println("=".repeat(74));
    }

    /**
     * Reads real keyboard input, applying every rule from the lesson: one
     * Scanner for the whole program, line-based reading to avoid the newline
     * trap, validation before parsing, and no close() on System.in.
     */
    static void runInteractiveDemo() {

        // ONE Scanner for System.in, created once. Never closed.
        Scanner keyboard = new Scanner(System.in);

        System.out.println("Type your answers and press Enter after each.");
        System.out.println();

        // Reading a line is always safe - nextLine() consumes its own newline.
        System.out.print("  Your name: ");
        String name = readLineSafely(keyboard);
        if (name == null) {
            System.out.println();
            System.out.println("  (input ended - nothing more to read)");
            return;
        }

        // For a number, read the LINE and parse it. This avoids the nextInt()
        // newline trap completely, and lets us re-prompt on bad input.
        int age = -1;
        while (age < 0) {
            System.out.print("  Your age (a whole number): ");
            String line = readLineSafely(keyboard);
            if (line == null) {
                System.out.println();
                System.out.println("  (input ended)");
                return;
            }
            try {
                age = Integer.parseInt(line.trim());
                if (age < 0) {
                    System.out.println("  Age cannot be negative. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  \"" + line.trim() + "\" is not a whole number. Try again.");
            }
        }

        System.out.println();
        System.out.println("  Hello, " + name.trim() + ".");
        System.out.println("  In ten years you will be " + (age + 10) + ".");
        System.out.println();
        System.out.println("  Notice what this method did NOT do:");
        System.out.println("    - it never mixed nextInt() with nextLine()");
        System.out.println("    - it never let bad input throw uncaught");
        System.out.println("    - it never called keyboard.close()");

        // Deliberately no keyboard.close() here. See Section 7.
    }

    /**
     * Reads one line, returning null instead of throwing when input has ended.
     * Guarding with hasNextLine() is what stops a read loop from crashing when
     * the user presses Ctrl+D (Unix) or Ctrl+Z (Windows).
     *
     * @param scanner the scanner to read from
     * @return the line read, or null if there is no more input
     */
    static String readLineSafely(Scanner scanner) {
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }
}

/* ============================================================================
 * EXERCISES
 * ----------------------------------------------------------------------------
 * 1. Run with --interactive and try to break it: type letters for the age,
 *    type nothing and press Enter, press Ctrl+C. Then read runInteractiveDemo()
 *    again and identify which guard handled each case.
 *
 * 2. Reproduce the trap yourself. Write a tiny program that uses nextInt()
 *    followed by nextLine() with a real keyboard, and watch the name prompt
 *    fly past. Then fix it both ways.
 *
 * 3. Write `int readIntInRange(Scanner s, String prompt, int min, int max)`
 *    that keeps prompting until it gets a whole number in range. Use
 *    hasNextInt() rather than catching exceptions.
 *
 * 4. Point a Scanner at the String "10 20 abc 30" and sum only the integers,
 *    skipping anything else, without letting an exception escape.
 *
 * 5. Use useDelimiter to parse "name=Danish;age=25;city=Bengaluru" into three
 *    key/value pairs. (Hint: you can split twice, or use a regex delimiter.)
 *
 * 6. Rewrite the interactive section with BufferedReader instead of Scanner.
 *    Note what you gain (speed) and what you lose (nextInt and friends), and
 *    that readLine() forces you to handle a checked IOException.
 * ============================================================================
 */
