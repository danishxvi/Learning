package com.danish.spring.restbasics;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// An in-memory stand-in for a real database - just enough to give the controller
// something to call. Real persistence starts in section 05.
@Repository
public class BookRepository {
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch"));
        save(new Book(null, "Clean Code", "Robert C. Martin"));
    }

    public Book save(Book book) {
        long id = book.getId() != null ? book.getId() : nextId.getAndIncrement();
        Book saved = new Book(id, book.getTitle(), book.getAuthor());
        books.put(id, saved);
        return saved;
    }

    public java.util.Collection<Book> findAll() {
        return books.values();
    }

    public Book findById(Long id) {
        return books.get(id);
    }

    public boolean deleteById(Long id) {
        return books.remove(id) != null;
    }
}
