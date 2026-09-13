package com.danish.spring.fulltest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 49 - FULL INTEGRATION TESTS WITH @SpringBootTest
// ============================================================================
// The lesson content lives in src/test/java - run it:
// mvn -f Spring/08-testing-spring-applications/49-full-integration-tests-with-springboottest test
// ============================================================================
@SpringBootApplication
public class FullIntegrationTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(FullIntegrationTestApplication.class, args);
    }
}
