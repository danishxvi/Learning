package com.danish.spring.auditing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

// ============================================================================
// 35 - AUDITING AND TIMESTAMPS
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/35-auditing-and-timestamps spring-boot:run
// ============================================================================
// @EnableJpaAuditing turns the whole mechanism on - without it, @CreatedDate etc. are
// inert annotations that Spring Data never looks at.
@EnableJpaAuditing
@SpringBootApplication
public class AuditingApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuditingApplication.class, args);
    }

    // Registers the AuditorAware bean - Spring Data looks it up by type to answer
    // "who is @CreatedBy/@LastModifiedBy for this operation?"
    @Bean
    public AuditorAware<String> auditorAware() {
        return new SpringSecurityFreeAuditorAware();
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final NoteRepository repository;
        private final NoteService service;

        Demo(NoteRepository repository, NoteService service) {
            this.repository = repository;
            this.service = service;
        }

        @Override
        public void run(String... args) throws InterruptedException {
            System.out.println("=".repeat(74));
            System.out.println("CREATING A Note AS \"danish\" - watch createdDate/createdBy fill in");
            System.out.println("=".repeat(74));
            CurrentUserHolder.set("danish");
            Long id = service.create("Remember to write lesson 36 next.");
            print(id);

            // A small real pause, so createdDate and lastModifiedDate are VISIBLY
            // different Instants below, not just equal by coincidence of clock resolution.
            Thread.sleep(50);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("UPDATING THE SAME Note AS \"editor-bot\" - createdDate/createdBy stay THE SAME");
            System.out.println("=".repeat(74));
            CurrentUserHolder.set("editor-bot");
            service.updateContent(id, "Remember to write lesson 36 next. [edited]");
            print(id);
        }

        private void print(Long id) {
            Note note = repository.findById(id).orElseThrow();
            System.out.println("  content          = " + note.getContent());
            System.out.println("  createdDate      = " + note.getCreatedDate());
            System.out.println("  lastModifiedDate = " + note.getLastModifiedDate());
            System.out.println("  createdBy        = " + note.getCreatedBy());
            System.out.println("  lastModifiedBy   = " + note.getLastModifiedBy());
        }
    }
}
