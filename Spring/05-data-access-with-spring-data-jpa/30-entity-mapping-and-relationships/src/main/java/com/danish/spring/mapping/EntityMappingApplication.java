package com.danish.spring.mapping;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 30 - ENTITY MAPPING AND RELATIONSHIPS
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/30-entity-mapping-and-relationships spring-boot:run
// ============================================================================
@SpringBootApplication
public class EntityMappingApplication {
    public static void main(String[] args) {
        SpringApplication.run(EntityMappingApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final MappingService service;

        Demo(MappingService service) {
            this.service = service;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("CREATING AN AUTHOR WITH TWO BOOKS - cascade = ALL persists both");
            System.out.println("=".repeat(74));
            Long authorId = service.createAuthorWithBooks();

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("LAZY COLLECTION, ACCESSED INSIDE A TRANSACTION - works fine");
            System.out.println("=".repeat(74));
            service.demonstrateLazyAccessWithinTransaction(authorId);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("LAZY ASSOCIATION, ACCESSED OUTSIDE A TRANSACTION - fails");
            System.out.println("=".repeat(74));
            Book book = service.fetchBookWithoutTransaction(1L);
            service.demonstrateLazyInitializationException(book);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("orphanRemoval = true - removing a Book from Author's list deletes it");
            System.out.println("=".repeat(74));
            service.removeOneBook(authorId, 2L);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("cascade = ALL - deleting the Author deletes every remaining Book too");
            System.out.println("=".repeat(74));
            service.deleteAuthorAndAllBooks(authorId);
        }
    }
}
