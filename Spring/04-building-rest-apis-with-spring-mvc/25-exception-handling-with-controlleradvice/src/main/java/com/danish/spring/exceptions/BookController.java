package com.danish.spring.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    // Throws a CUSTOM exception and lets it propagate - no try/catch here at all. The
    // controller method reads as pure "happy path" logic; GlobalExceptionHandler decides
    // what a BookNotFoundException actually means as an HTTP response.
    @GetMapping("/{id}")
    public Book findById(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    // ResponseStatusException - a LIGHTER-WEIGHT alternative for a one-off case that
    // doesn't need its own exception class or a dedicated @ExceptionHandler. Spring MVC
    // has built-in handling for this exception type already; no code in
    // GlobalExceptionHandler mentions it at all, and it still produces a real 404.
    @GetMapping("/{id}/quick-check")
    public String quickCheck(@PathVariable Long id) {
        if (repository.findById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No book with id " + id);
        }
        return "Book " + id + " exists.";
    }

    // Deliberately triggers an UNEXPECTED exception, to prove the catch-all in
    // GlobalExceptionHandler intercepts it too - not just the exceptions written with
    // error handling in mind.
    @GetMapping("/boom")
    public String boom() {
        String willBeNull = null;
        return willBeNull.toUpperCase(); // NullPointerException, thrown on purpose
    }
}
