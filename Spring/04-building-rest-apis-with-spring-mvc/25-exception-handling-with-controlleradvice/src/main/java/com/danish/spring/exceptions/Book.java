package com.danish.spring.exceptions;

public class Book {
    private final Long id;
    private final String title;
    private final String author;

    public Book(Long id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
}
