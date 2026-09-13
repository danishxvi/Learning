package com.danish.spring.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 63 - API GATEWAY (a service the gateway routes to)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/63-api-gateway/inventory-service spring-boot:run
// ============================================================================
@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
