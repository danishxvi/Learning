package com.danish.spring.fulltest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest with NO webEnvironment argument would use MOCK (like lesson 47) - but
// RANDOM_PORT starts a REAL embedded Tomcat, on a REAL (randomly chosen, so parallel
// test runs never collide) port. This is the full stack: real HTTP, real Spring MVC
// dispatching, a real (in-memory) database via the real BookRepository - nothing about
// this test is a slice. It is, deliberately, the most expensive and most realistic test
// this whole section covers.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {

    // The actual port the embedded server bound to - injected because it's chosen at
    // random specifically to avoid colliding with anything else on the machine.
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void createThenFetchGoesThroughTheRealHttpStack() {
        System.out.println("  Embedded server actually running on port: " + port);

        // A REAL HTTP POST - TestRestTemplate makes an actual network call to
        // localhost:<port>, through the actual embedded Tomcat, through actual Spring
        // MVC dispatching, into the actual BookController, into the actual
        // BookRepository, into the actual (in-memory) database.
        ResponseEntity<Book> createResponse = restTemplate.postForEntity(
                "/api/books", new Book("Effective Java", "Joshua Bloch"), Book.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = createResponse.getBody().getId();
        assertThat(id).isNotNull();

        // Confirms the row is REALLY in the database - not just that the controller
        // returned something that looked right.
        assertThat(bookRepository.findById(id)).isPresent();

        // A SECOND real HTTP call, fetching what the FIRST call created.
        ResponseEntity<Book> getResponse = restTemplate.getForEntity("/api/books/" + id, Book.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Effective Java");
    }

    @Test
    void fetchingAMissingBookReturns404OverRealHttp() {
        ResponseEntity<Book> response = restTemplate.getForEntity("/api/books/999999", Book.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
