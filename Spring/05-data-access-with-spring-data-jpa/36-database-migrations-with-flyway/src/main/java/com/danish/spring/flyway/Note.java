package com.danish.spring.flyway;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// With ddl-auto: validate, this class must match the table V1 already created EXACTLY -
// Hibernate checks column names and (compatible) types at startup and fails loudly if
// they disagree, rather than silently altering anything.
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private boolean archived;

    protected Note() { }

    public Note(String content) {
        this.content = content;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public boolean isArchived() { return archived; }
}
