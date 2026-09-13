package com.danish.spring.testcontainers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // A native query (lesson 32) using ILIKE - a REAL Postgres-specific operator
    // (case-insensitive LIKE) that H2's Postgres-compatibility mode also happens to
    // support, but plenty of other Postgres-specific syntax does NOT. This is exactly
    // the class of query where "it passed against H2" and "it actually works against
    // Postgres" can quietly diverge - the entire reason this lesson exists.
    @Query(value = "SELECT * FROM book WHERE title ILIKE %:fragment%", nativeQuery = true)
    List<Book> searchTitleCaseInsensitive(String fragment);
}
