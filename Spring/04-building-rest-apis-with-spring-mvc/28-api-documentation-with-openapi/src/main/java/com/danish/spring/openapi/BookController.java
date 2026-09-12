package com.danish.spring.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// @Tag groups every endpoint in this controller under one heading in the generated
// documentation - useful once an API has many controllers.
@Tag(name = "Books", description = "Operations for managing the book catalog")
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final Map<Long, BookResponse> books = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    // @Operation documents the ENDPOINT itself - summary/description appear in the
    // generated docs exactly as written here, alongside whatever springdoc already
    // inferred from the method signature (the path, the HTTP verb, the return type).
    @Operation(summary = "Fetch a single book", description = "Returns 404 if the id does not exist.")
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(
            @Parameter(description = "The book's numeric id") @PathVariable Long id) {
        BookResponse book = books.get(id);
        return book != null ? ResponseEntity.ok(book) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Create a new book")
    @ApiResponse(responseCode = "201", description = "Book created successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed - see the response body for field errors")
    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        long id = nextId.getAndIncrement();
        BookResponse saved = new BookResponse(id, request.getTitle(), request.getAuthor(), request.getPriceCents());
        books.put(id, saved);
        return ResponseEntity.status(201).body(saved);
    }
}
