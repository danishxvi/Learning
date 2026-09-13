package com.danish.spring.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 63 - API GATEWAY
// ============================================================================
// Requires eureka-server (8761) and inventory-service (8081) already running.
// Run: mvn -f Spring/12-microservices-with-spring-cloud/63-api-gateway/api-gateway spring-boot:run
// ============================================================================
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
