package com.danish.spring.jwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 43 - JWT-BASED STATELESS AUTHENTICATION
// ============================================================================
// Run: mvn -f Spring/07-spring-security/43-jwt-based-stateless-authentication spring-boot:run
// See the .md for the exact curl commands this lesson runs, including the tampered-token
// and expired-token demonstrations.
// ============================================================================
@SpringBootApplication
public class JwtApplication {
    public static void main(String[] args) {
        SpringApplication.run(JwtApplication.class, args);
    }
}
