package com.danish.spring.restbasics;

import org.springframework.web.bind.annotation.*;

import java.util.Collection;

// @RestController = @Controller (lesson 07's stereotype - marks this a web layer bean)
// + @ResponseBody (every method's RETURN VALUE is written directly into the HTTP
// response body, serialized to JSON by Jackson, instead of being treated as a view
// name to render - the historical behaviour @Controller alone still has).
@RestController
// @RequestMapping at the CLASS level sets a base path every method's mapping is
// relative to - every endpoint below actually lives under /api/books/...
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    // @GetMapping("/x") is shorthand for @RequestMapping(value = "/x", method = RequestMethod.GET) -
    // introduced in Spring 4.3 specifically because that combination was so common.
    @GetMapping
    public Collection<Book> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Book findById(@PathVariable Long id) {
        return repository.findById(id); // returning null here becomes a 200 with body "null" - lesson 23 fixes this
    }

    @PostMapping
    public Book create(@RequestBody Book book) {
        return repository.save(book);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book book) {
        Book toSave = new Book(id, book.getTitle(), book.getAuthor());
        return repository.save(toSave);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // The OLD, verbose way to write @GetMapping("/legacy") - shown once, here, so you
    // recognise it in older codebases. Every other method above uses the modern shorthand.
    @RequestMapping(value = "/legacy", method = RequestMethod.GET)
    public String legacyStyle() {
        return "This endpoint is mapped the pre-4.3 way - functionally identical to @GetMapping.";
    }
}
