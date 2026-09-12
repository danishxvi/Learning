package com.danish.spring.projections;

// A CLASS (DTO) PROJECTION - a concrete type with a constructor matching a JPQL
// "constructor expression" exactly (see BookRepository.findSummaries). Unlike the
// interface projection, this one is a real class you could pass around, serialize, or
// return directly from a @RestController with no further mapping (lesson 24).
public class BookSummary {
    private final String title;
    private final String author;

    public BookSummary(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }

    @Override
    public String toString() {
        return title + " by " + author;
    }
}
