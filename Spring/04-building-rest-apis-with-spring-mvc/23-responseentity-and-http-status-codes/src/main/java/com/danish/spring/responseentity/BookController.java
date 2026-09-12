package com.danish.spring.responseentity;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// The lesson-21 version of this controller returned plain Book (or void) and let Spring
// MVC invent a status code by default - always 200, even for "not found." Every method
// here returns ResponseEntity<T> instead, making the status code an EXPLICIT decision.
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    // Optional.map/orElseGet turns "found" into 200 + body, and "not found" into a
    // GENUINE 404 - fixing lesson 21's null-body bug at the source.
    @GetMapping("/{id}")
    public ResponseEntity<Book> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)                     // 200, with the book as the body
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404, with NO body at all
    }

    // 201 Created, not 200 - the resource did not exist before this call. The Location
    // header points at WHERE the new resource can now be fetched from - a REST
    // convention most clients (and API tooling) actually rely on.
    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = repository.save(book);
        URI location = URI.create("/api/books/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // 204 No Content for a successful delete - there is genuinely nothing meaningful to
    // send back, and 204 says exactly that, distinct from 200 (which implies a body).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = repository.deleteById(id);
        return deleted ? ResponseEntity.noContent().build()
                        : ResponseEntity.notFound().build();
    }

    // ResponseEntity.status(...) - the general-purpose escape hatch for any status the
    // convenience methods (ok/created/notFound/noContent) don't have a name for.
    @GetMapping("/{id}/reserve")
    public ResponseEntity<String> reserve(@PathVariable Long id) {
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No book with id " + id);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Reservation for book " + id + " is being processed");
    }
}
