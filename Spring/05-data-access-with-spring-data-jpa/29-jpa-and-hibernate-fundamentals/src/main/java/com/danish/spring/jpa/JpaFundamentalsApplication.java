package com.danish.spring.jpa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 29 - JPA AND HIBERNATE FUNDAMENTALS
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/29-jpa-and-hibernate-fundamentals spring-boot:run
// Watch the SQL logged between each section header - that log IS the lesson.
// ============================================================================
@SpringBootApplication
public class JpaFundamentalsApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaFundamentalsApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final BookService bookService;

        Demo(BookService bookService) {
            this.bookService = bookService;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("CREATING A BOOK - watch for the INSERT below");
            System.out.println("=".repeat(74));
            Long id = bookService.createBook("Effective Java", "Joshua Bloch");
            System.out.println("  Assigned id: " + id);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("FIRST-LEVEL CACHE - fetching the SAME id twice in one transaction");
            System.out.println("=".repeat(74));
            bookService.demonstrateFirstLevelCache(id);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("DIRTY CHECKING - changing a field with NO explicit save() call");
            System.out.println("=".repeat(74));
            bookService.demonstrateDirtyChecking(id);
            System.out.println("  (the transaction has now committed - an UPDATE should have appeared above)");
        }
    }
}
