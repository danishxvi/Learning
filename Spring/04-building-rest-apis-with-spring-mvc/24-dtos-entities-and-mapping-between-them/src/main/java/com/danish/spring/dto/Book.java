package com.danish.spring.dto;

import java.time.Instant;

// The ENTITY - the internal, persistence-shaped representation (section 05 makes this a
// real JPA @Entity; for now it is a plain class standing in for one). It carries fields
// that have NOTHING to do with what an API consumer should ever see.
public class Book {
    private final Long id;
    private String title;
    private String author;
    private final Instant createdAt;
    // Internal-only: an editorial note, never meant to leave this service. If a
    // controller returned this class directly, this field would appear in the JSON
    // response with zero code deciding that on purpose.
    private String internalEditorNotes;

    public Book(Long id, String title, String author, Instant createdAt, String internalEditorNotes) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
        this.internalEditorNotes = internalEditorNotes;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Instant getCreatedAt() { return createdAt; }
    public String getInternalEditorNotes() { return internalEditorNotes; }
    public void setInternalEditorNotes(String notes) { this.internalEditorNotes = notes; }
}
