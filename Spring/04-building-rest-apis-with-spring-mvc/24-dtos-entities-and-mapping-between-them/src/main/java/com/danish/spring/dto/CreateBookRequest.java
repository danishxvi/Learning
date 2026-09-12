package com.danish.spring.dto;

// A REQUEST DTO for creation specifically. No "id" field - the server assigns that, and
// giving a client the ABILITY to send one would only invite confusion about whether it's
// honoured. No "createdAt", no "internalEditorNotes" - a client creating a book has no
// business setting either. This shape exists ONLY to describe "what a create request
// looks like," and nothing else uses it.
public class CreateBookRequest {
    private String title;
    private String author;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
