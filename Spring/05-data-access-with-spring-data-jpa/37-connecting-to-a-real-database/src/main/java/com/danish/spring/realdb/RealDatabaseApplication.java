package com.danish.spring.realdb;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// ============================================================================
// 37 - CONNECTING TO A REAL DATABASE
// ============================================================================
// Needs a real Postgres server reachable at the URL in application.yml. The quickest
// way to get one, with no local install: Docker.
//   docker run -d --name learning-postgres -e POSTGRES_DB=learningdb ^
//     -e POSTGRES_USER=learning -e POSTGRES_PASSWORD=learning -p 55432:5432 postgres:16-alpine
// Then: mvn -f Spring/05-data-access-with-spring-data-jpa/37-connecting-to-a-real-database spring-boot:run
// ============================================================================
@SpringBootApplication
public class RealDatabaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(RealDatabaseApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {

        @PersistenceContext
        private EntityManager entityManager;

        private final BookRepository repository;

        Demo(BookRepository repository) {
            this.repository = repository;
        }

        @Override
        @Transactional
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("PROVING THIS IS REAL POSTGRES, NOT H2's COMPATIBILITY MODE");
            System.out.println("=".repeat(74));
            Object version = entityManager.createNativeQuery("SELECT version()").getSingleResult();
            System.out.println("  " + version);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("A NORMAL save() - the EXACT SAME JpaRepository method as every H2 lesson");
            System.out.println("=".repeat(74));
            Book saved = repository.save(new Book("Designing Data-Intensive Applications", "Martin Kleppmann"));
            System.out.println("  Saved with id=" + saved.getId());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("findAll() - reading it back");
            System.out.println("=".repeat(74));
            repository.findAll().forEach(b -> System.out.println("  " + b.getId() + ": " + b.getTitle() + " by " + b.getAuthor()));
        }
    }
}
