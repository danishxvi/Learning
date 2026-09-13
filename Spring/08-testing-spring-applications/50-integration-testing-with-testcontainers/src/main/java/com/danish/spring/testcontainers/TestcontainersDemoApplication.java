package com.danish.spring.testcontainers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 50 - INTEGRATION TESTING WITH TESTCONTAINERS
// ============================================================================
// This lesson has no application.yml at all - it exists purely to be tested, against a
// REAL, ephemeral Postgres container. Run the test (needs Docker running):
// mvn -f Spring/08-testing-spring-applications/50-integration-testing-with-testcontainers test
// ============================================================================
@SpringBootApplication
public class TestcontainersDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestcontainersDemoApplication.class, args);
    }
}
