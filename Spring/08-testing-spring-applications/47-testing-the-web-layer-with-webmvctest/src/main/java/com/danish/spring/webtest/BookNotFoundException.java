package com.danish.spring.webtest;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("No book with id " + id);
    }
}
