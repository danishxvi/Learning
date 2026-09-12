package com.danish.spring.exceptions;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookRepository {
    private final Map<Long, Book> books = new ConcurrentHashMap<>();

    public BookRepository() {
        books.put(1L, new Book(1L, "Effective Java", "Joshua Bloch"));
    }

    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(books.get(id));
    }
}
