package com.danish.app;

// SINGLE-TYPE IMPORTS - preferred. Each one names exactly what it brings in.
import com.danish.library.model.Book;
import com.danish.library.model.Catalogue;
import com.danish.library.service.EBook;

// A WILDCARD import would be:  import com.danish.library.model.*;
// It imports every PUBLIC type in that package - but NOT sub-packages, and
// NOT anything non-public. Most style guides prefer explicit imports so the
// reader can see where each name comes from.

import java.util.List;

/**
 * The application entry point, in a package that is a CONSUMER of the library.
 * It can see only what the library made {@code public}.
 *
 * <p>Compile and run from the {@code 30-packages-demo} directory:
 * <pre>
 *     javac -d out src/com/danish/library/model/*.java \
 *                  src/com/danish/library/service/*.java \
 *                  src/com/danish/app/*.java
 *     java -cp out com.danish.app.Main
 * </pre>
 */
public class Main {

    /**
     * @param args unused
     */
    public static void main(String[] args) {

        System.out.println("=".repeat(74));
        System.out.println("  PACKAGES AND ACCESS MODIFIERS - A REAL MULTI-PACKAGE PROJECT");
        System.out.println("=".repeat(74));

        System.out.println();
        System.out.println("  Three packages:");
        System.out.println("    com.danish.library.model    Book, Catalogue");
        System.out.println("    com.danish.library.service  EBook (a subclass of Book)");
        System.out.println("    com.danish.app              Main (this class)");

        /* --------------------------------------------------------------------
         * WHAT A CONSUMER IN ANOTHER PACKAGE CAN SEE.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(74));
        System.out.println("  FROM com.danish.app - AN UNRELATED PACKAGE");
        System.out.println("-".repeat(74));

        Book book = new Book("Effective Java", "Joshua Bloch");

        System.out.println("    public constructor           -> " + book.getTitle());
        System.out.println("    public getTitle()            -> " + book.getTitle());
        System.out.println("    public MAX_TITLE_LENGTH      -> " + Book.MAX_TITLE_LENGTH);

        // book.internalCatalogueCode;
        //   ERROR: internalCatalogueCode is not public in Book;
        //          cannot be accessed from outside package
        System.out.println("    package-private field        -> NOT ACCESSIBLE");

        // book.onLoan = true;
        //   ERROR: onLoan has protected access in Book
        //   We are NOT a subclass and NOT in the same package, so protected
        //   gives us nothing.
        System.out.println("    protected onLoan             -> NOT ACCESSIBLE (we are not a subclass)");

        // book.setOnLoan(true);
        //   ERROR: setOnLoan(boolean) is not public in Book
        System.out.println("    package-private setOnLoan()  -> NOT ACCESSIBLE");

        /* --------------------------------------------------------------------
         * WHAT A CLASS IN THE SAME PACKAGE CAN SEE.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(74));
        System.out.println("  FROM com.danish.library.model - THE SAME PACKAGE AS Book");
        System.out.println("-".repeat(74));

        Catalogue catalogue = new Catalogue();
        catalogue.add(book);
        catalogue.add(new Book("Clean Code", "Robert Martin"));

        System.out.println("    before lending:");
        for (Book b : catalogue.all()) {
            System.out.println("      " + b);
        }

        boolean lent = catalogue.lend("Effective Java");

        System.out.println("    catalogue.lend(\"Effective Java\") -> " + lent);
        System.out.println("    after lending:");
        for (Book b : catalogue.all()) {
            System.out.println("      " + b);
        }
        System.out.println();
        System.out.println("    Catalogue reached Book's PACKAGE-PRIVATE field and method.");
        System.out.println("    That is what package-private is for: closely collaborating");
        System.out.println("    classes, without exposing the collaboration to the world.");

        /* --------------------------------------------------------------------
         * WHAT A SUBCLASS IN ANOTHER PACKAGE CAN SEE.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(74));
        System.out.println("  FROM com.danish.library.service - A SUBCLASS IN ANOTHER PACKAGE");
        System.out.println("-".repeat(74));

        EBook ebook = new EBook("Java Concurrency in Practice", "Brian Goetz",
                "https://example.com/jcip");
        System.out.println("    " + ebook.probeAccess());

        System.out.println();
        System.out.println("    Anyone in the world can write a subclass like EBook. That is");
        System.out.println("    why a PROTECTED member is, in practice, part of your public");
        System.out.println("    API - and why protected FIELDS are almost always a mistake.");

        /* --------------------------------------------------------------------
         * NAME COLLISIONS AND FULLY QUALIFIED NAMES.
         * ------------------------------------------------------------------*/

        System.out.println();
        System.out.println("-".repeat(74));
        System.out.println("  NAME COLLISIONS");
        System.out.println("-".repeat(74));

        // java.util.List is imported above. java.awt.List also exists.
        // You cannot import both - so one must be fully qualified.
        List<String> imported = List.of("uses the imported java.util.List");
        System.out.println("    imported List        -> " + imported);
        System.out.println("    fully qualified name -> java.util.List<String> also works");
        System.out.println();
        System.out.println("    java.util.List and java.awt.List both exist. You cannot");
        System.out.println("    import both, so one must be written out in full. This is the");
        System.out.println("    main practical argument against wildcard imports.");
        System.out.println();
        System.out.println("    java.lang.* is imported automatically - which is why you");
        System.out.println("    never import String, Integer, System or Math.");

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("  End of the package demo.");
        System.out.println("=".repeat(74));
    }
}
