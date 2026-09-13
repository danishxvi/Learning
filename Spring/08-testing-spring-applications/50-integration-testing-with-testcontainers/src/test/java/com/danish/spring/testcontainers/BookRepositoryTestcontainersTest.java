package com.danish.spring.testcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @Testcontainers + @Container manage a REAL Docker container's lifecycle around this
// test class - started once before any test runs, stopped after the last one, no
// manual docker commands anywhere in this file.
@Testcontainers
@SpringBootTest
class BookRepositoryTestcontainersTest {

    // A REAL PostgreSQL server, in a REAL Docker container - not H2's
    // Postgres-compatibility mode. "static" means ONE container is shared across every
    // test method in this class, rather than paying startup cost per test.
    @Container
    @ServiceConnection // Spring Boot 3.1+ - reads the container's JDBC URL, username and
                        // password and wires them into the DataSource automatically. No
                        // manual @DynamicPropertySource property registration needed.
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BookRepository bookRepository;

    @Test
    void isActuallyBackedByARealPostgresContainer() {
        System.out.println("  Container image:    " + postgres.getDockerImageName());
        System.out.println("  Container JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("  Container running?  " + postgres.isRunning());

        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void caseInsensitiveSearchWorksAgainstRealPostgres() {
        bookRepository.save(new Book("Effective Java", "Joshua Bloch"));
        bookRepository.save(new Book("Clean Code", "Robert C. Martin"));

        // ILIKE is genuinely Postgres syntax - this proves it against the real engine,
        // not an approximation of it.
        List<Book> results = bookRepository.searchTitleCaseInsensitive("EFFECTIVE");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Effective Java");
    }
}
