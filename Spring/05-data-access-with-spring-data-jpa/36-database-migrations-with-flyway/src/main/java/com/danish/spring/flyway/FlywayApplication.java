package com.danish.spring.flyway;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// ============================================================================
// 36 - DATABASE MIGRATIONS WITH FLYWAY
// ============================================================================
// Run this lesson TWICE, in order, editing files between runs exactly as the .md
// describes:
//   mvn -f Spring/05-data-access-with-spring-data-jpa/36-database-migrations-with-flyway spring-boot:run
// ============================================================================
@SpringBootApplication
public class FlywayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlywayApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {

        @PersistenceContext
        private EntityManager entityManager;

        private final NoteRepository repository;

        Demo(NoteRepository repository) {
            this.repository = repository;
        }

        @Override
        @Transactional
        public void run(String... args) {
            if (repository.count() == 0) {
                repository.save(new Note("First note - inserted on the very first run."));
            }

            System.out.println("=".repeat(74));
            System.out.println("flyway_schema_history - EVERY migration Flyway has ever applied here");
            System.out.println("=".repeat(74));
            @SuppressWarnings("unchecked")
            List<Object[]> rows = entityManager.createNativeQuery(
                    "SELECT \"installed_rank\", \"version\", \"description\", \"checksum\", \"success\" FROM \"flyway_schema_history\""
            ).getResultList();
            rows.forEach(row -> System.out.println("  rank=" + row[0] + " version=" + row[1]
                    + " description=\"" + row[2] + "\" checksum=" + row[3] + " success=" + row[4]));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("ALL NOTES CURRENTLY IN THE DATABASE");
            System.out.println("=".repeat(74));
            repository.findAll().forEach(n -> System.out.println("  id=" + n.getId() + " content=\"" + n.getContent()
                    + "\" archived=" + n.isArchived()));
        }
    }
}
