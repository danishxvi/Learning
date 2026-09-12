package com.danish.spring.dto;

import java.time.Instant;

// A RESPONSE DTO. Deliberately narrower than the Book entity: id, title, author and
// createdAt are fine for a client to see - internalEditorNotes is NOT, and this class
// simply has no field for it, so there is no way to accidentally serialize it, ever,
// no matter what changes happen inside Book later.
public class BookResponse {
    private final Long id;
    private final String title;
    private final String author;
    private final Instant createdAt;

    public BookResponse(Long id, String title, String author, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
}
