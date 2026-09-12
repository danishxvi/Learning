package com.danish.spring.dto;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch", Instant.now(),
                "Editor note: check for a 4th edition next year"));
    }

    public Book save(Book book) {
        long id = book.getId() != null ? book.getId() : nextId.getAndIncrement();
        Book saved = new Book(id, book.getTitle(), book.getAuthor(), book.getCreatedAt(), book.getInternalEditorNotes());
        books.put(id, saved);
        return saved;
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public java.util.Collection<Book> findAll() {
        return books.values();
    }
}
