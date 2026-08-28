package com.danish.library.model;

/*
 * The `package` statement MUST be the first statement in the file (comments
 * and blank lines aside), and it must match this file's directory path:
 *
 *     package com.danish.library.model;
 *     ->  src/com/danish/library/model/Book.java
 *
 * Get that wrong and javac will still compile the file, but `java` will not
 * find the class - which produces the confusing "Could not find or load main
 * class" error.
 */

import java.util.Objects;

/**
 * A book. This class is {@code public}, so it is visible from every package -
 * it is part of the library's published API.
 *
 * <p>Note carefully which members are public and which are not: that choice
 * is what {@code Access.java} in the app package demonstrates.
 */
public class Book {

    /** PUBLIC: visible everywhere. Part of the permanent API. */
    public static final int MAX_TITLE_LENGTH = 200;

    /** PRIVATE: visible only inside this class. The default for state. */
    private final String title;

    /** PRIVATE. */
    private final String author;

    /**
     * PACKAGE-PRIVATE (no modifier): visible to other classes in
     * com.danish.library.model, and nowhere else. Not even to subclasses in
     * other packages.
     */
    String internalCatalogueCode;

    /**
     * PROTECTED: visible in this package AND to subclasses in ANY package.
     * That second half is why protected is effectively public - see the
     * EBook class in the service package.
     */
    protected boolean onLoan;

    /**
     * The constructor is public, so anyone can create a Book.
     *
     * @param title  the title; must not be null or blank
     * @param author the author; must not be null
     * @throws IllegalArgumentException if the title is blank or too long
     */
    public Book(String title, String author) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("title too long");
        }
        this.title = title;
        this.author = Objects.requireNonNull(author, "author");
        this.internalCatalogueCode = "CAT-" + Math.abs(title.hashCode() % 10000);
    }

    /** @return the title */
    public String getTitle() {
        return title;
    }

    /** @return the author */
    public String getAuthor() {
        return author;
    }

    /** @return true if the book is currently lent out */
    public boolean isOnLoan() {
        return onLoan;
    }

    /**
     * PACKAGE-PRIVATE method. Only classes in this package may call it, which
     * is how the model package keeps mutation to itself.
     *
     * @param onLoan the new loan status
     */
    void setOnLoan(boolean onLoan) {
        this.onLoan = onLoan;
    }

    /**
     * A PRIVATE helper. Nothing outside this class can call it - not even
     * another class in the same package.
     *
     * @return a short display code
     */
    private String shortCode() {
        return internalCatalogueCode.substring(4);
    }

    @Override
    public String toString() {
        return "Book[" + title + " by " + author + ", code " + shortCode()
                + (onLoan ? ", ON LOAN" : ", available") + "]";
    }
}
