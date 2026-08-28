package com.danish.library.model;

import java.util.ArrayList;
import java.util.List;

/**
 * In the SAME package as {@link Book}, so it can reach Book's package-private
 * members. This is what package-private is for: classes that collaborate
 * closely without exposing that collaboration to the outside world.
 */
public class Catalogue {

    private final List<Book> books = new ArrayList<>();

    /**
     * @param book the book to add
     */
    public void add(Book book) {
        books.add(book);
    }

    /**
     * Reaches Book's PACKAGE-PRIVATE field and method. Legal only because
     * this class is in com.danish.library.model too.
     *
     * @param title the title to lend
     * @return true if the book was found and lent
     */
    public boolean lend(String title) {
        for (Book book : books) {
            if (book.getTitle().equals(title) && !book.isOnLoan()) {
                book.setOnLoan(true);                    // package-private METHOD
                book.internalCatalogueCode += "-OUT";    // package-private FIELD
                return true;
            }
        }
        return false;
    }

    /** @return an immutable snapshot of the catalogue */
    public List<Book> all() {
        return List.copyOf(books);
    }
}
