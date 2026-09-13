package com.danish.spring.corscsrf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 45 - CORS AND CSRF
// ============================================================================
// Run: mvn -f Spring/07-spring-security/45-cors-and-csrf spring-boot:run
// See the .md for the paired static test page and the exact browser-verified steps.
// ============================================================================
@SpringBootApplication
public class CorsCsrfApplication {
    public static void main(String[] args) {
        SpringApplication.run(CorsCsrfApplication.class, args);
    }
}
