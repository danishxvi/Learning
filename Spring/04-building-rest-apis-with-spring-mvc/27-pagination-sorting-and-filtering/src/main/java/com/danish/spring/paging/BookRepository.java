package com.danish.spring.paging;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepository {
    // 12 books, deliberately unsorted, so sorting has something real to prove.
    private final List<Book> books = List.of(
            new Book(1L, "Refactoring", "Martin Fowler"),
            new Book(2L, "Clean Code", "Robert C. Martin"),
            new Book(3L, "Effective Java", "Joshua Bloch"),
            new Book(4L, "Domain-Driven Design", "Eric Evans"),
            new Book(5L, "The Pragmatic Programmer", "Andy Hunt"),
            new Book(6L, "Working Effectively with Legacy Code", "Michael Feathers"),
            new Book(7L, "Design Patterns", "Erich Gamma"),
            new Book(8L, "Test-Driven Development", "Kent Beck"),
            new Book(9L, "Clean Architecture", "Robert C. Martin"),
            new Book(10L, "Continuous Delivery", "Jez Humble"),
            new Book(11L, "Release It!", "Michael Nygard"),
            new Book(12L, "Building Microservices", "Sam Newman")
    );

    public List<Book> findAll() {
        return books;
    }
}
