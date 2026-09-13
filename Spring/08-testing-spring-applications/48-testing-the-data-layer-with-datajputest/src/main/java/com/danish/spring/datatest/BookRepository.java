package com.danish.spring.datatest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    // A derived query (lesson 32) - exactly what this lesson's test proves actually
    // generates correct SQL, against a real (in-memory) database, not just compiles.
    List<Book> findByAuthor(String author);
}
