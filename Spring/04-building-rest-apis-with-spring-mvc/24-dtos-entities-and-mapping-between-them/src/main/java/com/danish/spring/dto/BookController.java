package com.danish.spring.dto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository repository;
    private final BookMapper mapper;

    public BookController(BookRepository repository, BookMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // THE MISTAKE, kept here on purpose for comparison - returns the ENTITY directly.
    // Nothing about @RestController or Jackson stops this from compiling and running;
    // it just leaks whatever fields Book happens to have, including ones that were
    // never meant to leave this service.
    @GetMapping("/{id}/leaky")
    public ResponseEntity<Book> findByIdLeaky(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // THE FIX - maps to BookResponse before returning. internalEditorNotes has no field
    // to be copied into, so it is IMPOSSIBLE for this endpoint to leak it, regardless of
    // what fields get added to Book in the future.
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<BookResponse> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    // Accepts CreateBookRequest - a client CANNOT set id, createdAt, or
    // internalEditorNotes, because CreateBookRequest simply has no fields for them.
    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody CreateBookRequest request) {
        Book entity = mapper.toEntity(request);
        Book saved = repository.save(entity);
        BookResponse response = mapper.toResponse(saved);
        return ResponseEntity.created(URI.create("/api/books/" + saved.getId())).body(response);
    }
}
