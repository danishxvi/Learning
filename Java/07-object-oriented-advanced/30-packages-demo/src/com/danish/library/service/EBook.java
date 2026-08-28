package com.danish.library.service;

// Book lives in a DIFFERENT package, so it must be imported by name.
// Without this line, `Book` is an unknown symbol here.
import com.danish.library.model.Book;

/**
 * A subclass of Book in a DIFFERENT package. This class exists to demonstrate
 * exactly what {@code protected} grants and what it does not.
 *
 * <p>Anyone in the world can write a class like this. That is why a
 * {@code protected} member is, in practice, part of your public API.
 */
public class EBook extends Book {

    private final String downloadUrl;

    /**
     * @param title       the title
     * @param author      the author
     * @param downloadUrl where to download it
     */
    public EBook(String title, String author, String downloadUrl) {
        super(title, author);
        this.downloadUrl = downloadUrl;
    }

    /**
     * Demonstrates what this subclass can and cannot reach across the package
     * boundary.
     *
     * @return a description of what was accessible
     */
    public String probeAccess() {
        StringBuilder report = new StringBuilder();

        // PUBLIC: fine from anywhere.
        report.append("public getTitle()            -> ").append(getTitle()).append('\n');
        report.append("    public MAX_TITLE_LENGTH      -> ").append(MAX_TITLE_LENGTH).append('\n');

        // PROTECTED: accessible because we are a SUBCLASS, even though we are
        // in a different package. And we can WRITE it, not just read it.
        this.onLoan = true;
        report.append("    protected onLoan (WRITTEN)   -> ").append(this.onLoan)
                .append("   <- a subclass in ANY package can do this\n");

        // PACKAGE-PRIVATE: NOT accessible. Being a subclass does not help.
        // report.append(internalCatalogueCode);
        //   ERROR: internalCatalogueCode is not public in
        //   com.danish.library.model.Book; cannot be accessed from outside package
        report.append("    package-private field        -> NOT ACCESSIBLE (compile error)\n");

        // PRIVATE: not accessible either, and never will be.
        // report.append(shortCode());
        //   ERROR: shortCode() has private access in Book
        report.append("    private shortCode()          -> NOT ACCESSIBLE (compile error)");

        return report.toString();
    }

    /** @return where to download this ebook */
    public String getDownloadUrl() {
        return downloadUrl;
    }
}
