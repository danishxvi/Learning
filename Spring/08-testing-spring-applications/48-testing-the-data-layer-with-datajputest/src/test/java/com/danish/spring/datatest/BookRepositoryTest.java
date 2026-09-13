package com.danish.spring.datatest;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest loads ONLY JPA-related infrastructure: entities, repositories, the
// EntityManager, and (since H2 is on the TEST classpath) an automatically-configured
// in-memory database - no web layer, no @Service beans, none of lesson 47's
// TestDispatcherServlet, no real Postgres from lesson 37. A genuinely narrower slice,
// for a genuinely different purpose: proving repository queries actually work.
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookRepositoryTest {

    // TestEntityManager is a test-specific wrapper around a real EntityManager
    // (section 05) - persistAndFlush() writes AND sends the SQL immediately, instead of
    // waiting for a transaction boundary, so the very next line can safely query for it.
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @Order(1)
    void savesAndFindsABook() {
        Book saved = entityManager.persistAndFlush(new Book("Effective Java", "Joshua Bloch"));

        Optional<Book> found = bookRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Effective Java");

        // Proves the database has exactly one row - the ASSERTION this lesson's
        // rollback test (below) depends on NOT being true when it runs.
        assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void eachTestRollsBackAutomatically() {
        // If test isolation were broken, this would see the "Effective Java" row
        // savesAndFindsABook() inserted above (guaranteed to run FIRST via @Order).
        // @DataJpaTest wraps every test method in a transaction that is rolled back at
        // the end of that method - by default, with no configuration - so this always
        // starts from a genuinely empty table.
        assertThat(bookRepository.count()).isEqualTo(0);
    }

    @Test
    @Order(3)
    void derivedQueryFindsBooksByAuthor() {
        entityManager.persistAndFlush(new Book("Clean Code", "Robert C. Martin"));
        entityManager.persistAndFlush(new Book("Clean Architecture", "Robert C. Martin"));
        entityManager.persistAndFlush(new Book("Effective Java", "Joshua Bloch"));

        List<Book> byMartin = bookRepository.findByAuthor("Robert C. Martin");

        assertThat(byMartin).hasSize(2);
        assertThat(byMartin).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
    }
}
