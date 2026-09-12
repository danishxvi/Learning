package com.danish.spring.starters;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.Set;

// ============================================================================
// 16 - STARTERS AND DEPENDENCY MANAGEMENT
// ============================================================================
// Run: mvn -f Spring/03-spring-boot-fundamentals/16-starters-and-dependency-management spring-boot:run
// See the dependency tree: mvn -f Spring/03-spring-boot-fundamentals/16-starters-and-dependency-management dependency:tree
// ============================================================================
@SpringBootApplication
public class StartersApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartersApplication.class, args);
    }

    @Component
    static class StartersDemo implements CommandLineRunner {

        // Auto-configured by spring-boot-starter-validation being on the classpath -
        // nothing in THIS project registered it. Proof the starter's jars don't just
        // resolve at build time; they actually wire up a working bean at runtime.
        private final Validator validator;

        StartersDemo(Validator validator) {
            this.validator = validator;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("A VALID SignupRequest - zero violations");
            System.out.println("=".repeat(74));
            print(validator.validate(new SignupRequest("danish", "danish@example.com")));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("AN INVALID SignupRequest - real violations from Hibernate Validator");
            System.out.println("=".repeat(74));
            print(validator.validate(new SignupRequest("", "not-an-email")));
        }

        private void print(Set<ConstraintViolation<SignupRequest>> violations) {
            if (violations.isEmpty()) {
                System.out.println("  No violations.");
                return;
            }
            violations.forEach(v -> System.out.println(
                    "  " + v.getPropertyPath() + " " + v.getMessage() + " (was: \"" + v.getInvalidValue() + "\")"));
        }
    }
}
