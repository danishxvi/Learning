package com.danish.spring.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// @Entity is a JPA annotation (jakarta.persistence, part of the SPECIFICATION) - it says
// "instances of this class are stored as rows in a table." Hibernate is what actually
// reads this annotation and does the work; a different JPA provider (EclipseLink) would
// read the exact same annotation and behave the same way from this class's point of view.
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // let the DATABASE assign ids (an auto-increment column)
    private Long id;

    private String title;
    private String author;

    // JPA requires a no-arg constructor - Hibernate uses it (via reflection) to build
    // instances when reading rows back from the database, before your own code ever
    // touches them.
    protected Book() {
    }

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
