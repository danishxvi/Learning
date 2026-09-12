package com.danish.spring.queries;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// ============================================================================
// 32 - DERIVED AND CUSTOM QUERIES
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/32-derived-and-custom-queries spring-boot:run
// ============================================================================
@SpringBootApplication
public class DerivedQueriesApplication {
    public static void main(String[] args) {
        SpringApplication.run(DerivedQueriesApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final AuthorRepository authorRepository;
        private final BookRepository bookRepository;

        Demo(AuthorRepository authorRepository, BookRepository bookRepository) {
            this.authorRepository = authorRepository;
            this.bookRepository = bookRepository;
        }

        @Override
        @Transactional
        public void run(String... args) {
            Author bloch = authorRepository.save(new Author("Joshua Bloch"));
            Author martin = authorRepository.save(new Author("Robert C. Martin"));
            bookRepository.save(new Book("Effective Java", 3499, bloch));
            bookRepository.save(new Book("Clean Code", 2999, martin));
            bookRepository.save(new Book("Clean Architecture", 3999, martin));
            bookRepository.save(new Book("Clean Coder", 2599, martin));

            System.out.println("=".repeat(74));
            System.out.println("findByTitleContainingIgnoreCase(\"clean\")");
            System.out.println("=".repeat(74));
            print(bookRepository.findByTitleContainingIgnoreCase("clean"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("findByAuthorName(\"Robert C. Martin\") - nested property, generates a JOIN");
            System.out.println("=".repeat(74));
            print(bookRepository.findByAuthorName("Robert C. Martin"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("findByAuthorNameOrderByPriceCentsDesc(\"Robert C. Martin\")");
            System.out.println("=".repeat(74));
            print(bookRepository.findByAuthorNameOrderByPriceCentsDesc("Robert C. Martin"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("countByAuthorName(...) and existsByTitle(...) - shaped SQL, not full rows");
            System.out.println("=".repeat(74));
            System.out.println("  countByAuthorName(\"Robert C. Martin\") = " + bookRepository.countByAuthorName("Robert C. Martin"));
            System.out.println("  existsByTitle(\"Effective Java\")       = " + bookRepository.existsByTitle("Effective Java"));
            System.out.println("  existsByTitle(\"Nonexistent Book\")     = " + bookRepository.existsByTitle("Nonexistent Book"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Query JPQL - expensiveBooks(3000)");
            System.out.println("=".repeat(74));
            print(bookRepository.expensiveBooks(3000));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Query with JOIN FETCH - loads the Author eagerly, in ONE query");
            System.out.println("=".repeat(74));
            List<Book> withAuthor = bookRepository.findByAuthorNameWithAuthorFetched("Robert C. Martin");
            System.out.println("  Accessing .getAuthor().getName() with NO transaction issue:");
            withAuthor.forEach(b -> System.out.println("    - " + b.getTitle() + " by " + b.getAuthor().getName()));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Query NATIVE SQL - expensiveBooksNative(3000)");
            System.out.println("=".repeat(74));
            print(bookRepository.expensiveBooksNative(3000));
        }

        private void print(List<Book> books) {
            System.out.println("  " + books.stream().map(Book::getTitle).collect(Collectors.joining(", ")));
        }
    }
}
