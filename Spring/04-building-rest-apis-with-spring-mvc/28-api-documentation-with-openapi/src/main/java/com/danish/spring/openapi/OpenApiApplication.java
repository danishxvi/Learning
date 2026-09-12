package com.danish.spring.openapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 28 - API DOCUMENTATION WITH OpenAPI
// ============================================================================
// Run: mvn -f Spring/04-building-rest-apis-with-spring-mvc/28-api-documentation-with-openapi spring-boot:run
// Raw spec:  curl http://localhost:8080/v3/api-docs
// Human UI:  open http://localhost:8080/swagger-ui.html in a browser
// ============================================================================
@SpringBootApplication
public class OpenApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiApplication.class, args);
    }
}
