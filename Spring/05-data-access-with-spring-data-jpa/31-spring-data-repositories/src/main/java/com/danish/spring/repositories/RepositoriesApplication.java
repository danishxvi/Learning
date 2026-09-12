package com.danish.spring.repositories;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.Optional;

// ============================================================================
// 31 - SPRING DATA REPOSITORIES
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/31-spring-data-repositories spring-boot:run
// ============================================================================
@SpringBootApplication
public class RepositoriesApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepositoriesApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final BookRepository repository;

        Demo(BookRepository repository) {
            this.repository = repository;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("THIS INTERFACE HAS NO IMPLEMENTATION - WHAT DID SPRING ACTUALLY INJECT?");
            System.out.println("=".repeat(74));
            System.out.println("  repository.getClass()      = " + repository.getClass());
            System.out.println("  repository.getClass().getSuperclass() = " + repository.getClass().getSuperclass());
            System.out.println("  This is a runtime-generated PROXY, built by Spring Data at startup -");
            System.out.println("  nobody wrote a BookRepositoryImpl class anywhere in this project.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("save() ON A NEW ENTITY (id == null) - an INSERT");
            System.out.println("=".repeat(74));
            Book saved = repository.save(new Book("Effective Java", "Joshua Bloch"));
            System.out.println("  Assigned id: " + saved.getId());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("save() ON AN ENTITY WITH AN EXISTING id - an UPDATE, not a second row");
            System.out.println("=".repeat(74));
            Book updated = repository.save(new Book(saved.getId(), "Effective Java (3rd Edition)", "Joshua Bloch"));
            System.out.println("  Same id? " + updated.getId().equals(saved.getId()));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("findById() RETURNS Optional<Book> - lesson 23's exact pattern");
            System.out.println("=".repeat(74));
            Optional<Book> found = repository.findById(saved.getId());
            System.out.println("  found.isPresent() = " + found.isPresent() + ", title = " + found.map(Book::getTitle).orElse("n/a"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("findAll(), count(), existsById() - all generated, all free");
            System.out.println("=".repeat(74));
            repository.save(new Book("Clean Code", "Robert C. Martin"));
            System.out.println("  findAll().size() = " + repository.findAll().size());
            System.out.println("  count()          = " + repository.count());
            System.out.println("  existsById(999)  = " + repository.existsById(999L));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("deleteById()");
            System.out.println("=".repeat(74));
            repository.deleteById(saved.getId());
            System.out.println("  count() after delete = " + repository.count());
        }
    }
}
