package com.danish.spring.queries;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    // DERIVED QUERY - Spring Data PARSES this method name at startup, recognises
    // "findBy" + "Title" + "ContainingIgnoreCase", and builds a JPQL query from it.
    // No annotation, no query string - the method NAME is the entire specification.
    List<Book> findByTitleContainingIgnoreCase(String titleFragment);

    // Traverses a NESTED property - "AuthorName" walks Book.author.name, generating a
    // JOIN automatically, because "Author" alone isn't a property of Book but
    // "author.name" is a reachable path from it.
    List<Book> findByAuthorName(String name);

    // Comparison keywords - GreaterThan, LessThan, Between, and many others (see the .md)
    // are recognised the same way "ContainingIgnoreCase" was above.
    List<Book> findByPriceCentsGreaterThan(int amountCents);

    // Multiple conditions AND an ORDER BY - all still derived purely from the name.
    List<Book> findByAuthorNameOrderByPriceCentsDesc(String name);

    // Derived queries are not limited to returning a List - count/exists derive their
    // own SQL shape (COUNT/EXISTS) instead of fetching full rows.
    long countByAuthorName(String name);

    boolean existsByTitle(String title);

    // A CUSTOM query, written in JPQL (Java Persistence Query Language) - operates on
    // ENTITY names and FIELD names (Book, priceCents), not table/column names. Needed
    // the moment a query is more than a derived method name can comfortably express.
    @Query("SELECT b FROM Book b WHERE b.priceCents > :minPriceCents")
    List<Book> expensiveBooks(@Param("minPriceCents") int minPriceCents);

    // JOIN FETCH - this is the fix lesson 30 promised for LazyInitializationException.
    // Instead of loading a Book and touching its lazy author later (risking the
    // exception), this query loads BOTH in one round trip, eagerly, for this specific
    // use case only - the entity mapping itself (Book.author, still LAZY) is untouched.
    @Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.author.name = :authorName")
    List<Book> findByAuthorNameWithAuthorFetched(@Param("authorName") String authorName);

    // A NATIVE query - real SQL, not JPQL - for the rare case JPQL genuinely cannot
    // express (a database-specific function, a hand-tuned query). Operates on TABLE and
    // COLUMN names (book, price_cents), not entity/field names - notice the difference
    // from expensiveBooks() above, which is JPQL.
    @Query(value = "SELECT * FROM book WHERE price_cents > ?1", nativeQuery = true)
    List<Book> expensiveBooksNative(int minPriceCents);
}
