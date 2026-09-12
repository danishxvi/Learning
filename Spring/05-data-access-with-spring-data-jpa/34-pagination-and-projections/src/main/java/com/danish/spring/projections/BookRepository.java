package com.danish.spring.projections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // JpaRepository ALREADY declares findAll(Pageable) - inherited, not written here.
    // Overriding it isn't necessary; it's listed only so this lesson's demo can call it
    // by name easily. Passing a Pageable is enough to get a real Page<Book> back.

    // A DERIVED query (lesson 32) that ALSO takes a Pageable - Spring Data adds the
    // LIMIT/OFFSET and a companion COUNT query automatically, on top of the WHERE
    // clause it already knew how to build from the method name.
    Page<Book> findByAuthor(String author, Pageable pageable);

    // INTERFACE PROJECTION - the return type is BookTitleOnly, not Book. Spring Data
    // selects ONLY the title column, not every column on the table.
    List<BookTitleOnly> findByAuthorOrderByTitle(String author);

    // CLASS (DTO) PROJECTION via a JPQL constructor expression - "new package.Class(args)"
    // inside the query itself tells Hibernate to select exactly those columns and
    // construct BookSummary objects directly, never materializing a Book entity at all.
    @Query("SELECT new com.danish.spring.projections.BookSummary(b.title, b.author) FROM Book b WHERE b.priceCents > :minPriceCents")
    List<BookSummary> findSummaries(@Param("minPriceCents") int minPriceCents);
}
