package com.danish.spring.queries;

import jakarta.persistence.*;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int priceCents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    protected Book() { }

    public Book(String title, int priceCents, Author author) {
        this.title = title;
        this.priceCents = priceCents;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getPriceCents() { return priceCents; }
    public Author getAuthor() { return author; }
}
