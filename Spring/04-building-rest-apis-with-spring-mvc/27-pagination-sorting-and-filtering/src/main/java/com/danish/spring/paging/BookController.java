package com.danish.spring.paging;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
public class BookController {

    private final BookRepository repository;

    public BookController(BookRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/books")
    public PageResponse<Book> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String author) {

        // FILTER first - narrowing the working set before sorting or paging it matters
        // for correctness: paging BEFORE filtering would slice a page out of the WRONG,
        // unfiltered list.
        List<Book> filtered = repository.findAll().stream()
                .filter(book -> author == null || book.author().toLowerCase().contains(author.toLowerCase()))
                .toList();

        // SORT second - a Comparator chosen by the "sortBy" request param, direction
        // applied on top. Real APIs typically allow-list which fields can be sorted by,
        // exactly like the switch below - never sort by an arbitrary, client-supplied
        // field name reflectively, which would let a client probe internal field names.
        Comparator<Book> comparator = switch (sortBy) {
            case "author" -> Comparator.comparing(Book::author);
            default -> Comparator.comparing(Book::title);
        };
        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }
        List<Book> sorted = filtered.stream().sorted(comparator).toList();

        // PAGE last - skip() to the start of the requested page, limit() to its size.
        List<Book> pageContent = sorted.stream()
                .skip((long) page * size)
                .limit(size)
                .toList();

        return PageResponse.of(pageContent, page, size, sorted.size());
    }
}
