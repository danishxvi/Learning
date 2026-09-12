package com.danish.spring.projections;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

// ============================================================================
// 34 - PAGINATION AND PROJECTIONS
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/34-pagination-and-projections spring-boot:run
// ============================================================================
@SpringBootApplication
public class PaginationApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaginationApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final BookRepository repository;

        Demo(BookRepository repository) {
            this.repository = repository;
        }

        @Override
        public void run(String... args) {
            for (int i = 1; i <= 7; i++) {
                repository.save(new Book("Clean Code Vol. " + i, "Robert C. Martin", 2000 + i * 100));
            }
            repository.save(new Book("Effective Java", "Joshua Bloch", 3499));

            System.out.println("=".repeat(74));
            System.out.println("Page<Book> - findAll(Pageable), page 0, size 3, sorted by title");
            System.out.println("=".repeat(74));
            Page<Book> page0 = repository.findAll(PageRequest.of(0, 3, Sort.by("title")));
            printPage(page0);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("Page<Book> - page 1 of the SAME query");
            System.out.println("=".repeat(74));
            Page<Book> page1 = repository.findAll(PageRequest.of(1, 3, Sort.by("title")));
            printPage(page1);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("A DERIVED query WITH Pageable - findByAuthor(name, pageable)");
            System.out.println("=".repeat(74));
            Page<Book> byAuthor = repository.findByAuthor("Robert C. Martin", PageRequest.of(0, 4));
            printPage(byAuthor);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("INTERFACE PROJECTION - findByAuthorOrderByTitle returns BookTitleOnly, not Book");
            System.out.println("=".repeat(74));
            repository.findByAuthorOrderByTitle("Robert C. Martin")
                    .forEach(b -> System.out.println("  " + b.getTitle()));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("DTO PROJECTION - findSummaries returns BookSummary via a constructor expression");
            System.out.println("=".repeat(74));
            repository.findSummaries(2500).forEach(s -> System.out.println("  " + s));
        }

        private void printPage(Page<Book> page) {
            System.out.println("  content         = " + page.getContent().stream().map(Book::getTitle).toList());
            System.out.println("  number (0-based) = " + page.getNumber() + ", size = " + page.getSize());
            System.out.println("  totalElements    = " + page.getTotalElements() + ", totalPages = " + page.getTotalPages());
            System.out.println("  isFirst=" + page.isFirst() + ", isLast=" + page.isLast() + ", hasNext=" + page.hasNext());
        }
    }
}
