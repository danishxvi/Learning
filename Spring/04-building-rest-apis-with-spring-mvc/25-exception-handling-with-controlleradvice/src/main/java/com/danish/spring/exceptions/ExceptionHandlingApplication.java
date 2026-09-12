package com.danish.spring.exceptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 25 - EXCEPTION HANDLING WITH @ControllerAdvice
// ============================================================================
// Run: mvn -f Spring/04-building-rest-apis-with-spring-mvc/25-exception-handling-with-controlleradvice spring-boot:run
// ============================================================================
@SpringBootApplication
public class ExceptionHandlingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExceptionHandlingApplication.class, args);
    }
}
