package com.danish.spring.projections;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private int priceCents;

    protected Book() { }

    public Book(String title, String author, int priceCents) {
        this.title = title;
        this.author = author;
        this.priceCents = priceCents;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getPriceCents() { return priceCents; }
}
