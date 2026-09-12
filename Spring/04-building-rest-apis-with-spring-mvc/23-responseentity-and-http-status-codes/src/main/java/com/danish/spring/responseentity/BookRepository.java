package com.danish.spring.responseentity;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookRepository {
    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public BookRepository() {
        save(new Book(null, "Effective Java", "Joshua Bloch"));
    }

    public Book save(Book book) {
        long id = book.getId() != null ? book.getId() : nextId.getAndIncrement();
        Book saved = new Book(id, book.getTitle(), book.getAuthor());
        books.put(id, saved);
        return saved;
    }

    // Optional<Book> instead of a possibly-null Book - the return type ITSELF now says
    // "this might not exist," which is what lets the controller decide explicitly what
    // "not found" means instead of accidentally returning 200 with nothing in it.
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }

    public boolean deleteById(Long id) {
        return books.remove(id) != null;
    }
}
