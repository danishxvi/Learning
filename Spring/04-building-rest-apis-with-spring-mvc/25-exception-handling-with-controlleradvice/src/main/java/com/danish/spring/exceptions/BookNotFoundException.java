package com.danish.spring.exceptions;

// A CUSTOM, domain-specific exception - carries structured data (the missing id) instead
// of just a message string, the same principle the Java stack's lesson 40 (custom
// exceptions) argued for. Nothing about this class knows it will become an HTTP response
// at all - that translation is entirely BookExceptionHandler's job, kept separate.
public class BookNotFoundException extends RuntimeException {
    private final Long bookId;

    public BookNotFoundException(Long bookId) {
        super("No book found with id " + bookId);
        this.bookId = bookId;
    }

    public Long getBookId() {
        return bookId;
    }
}
