package com.danish.spring.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 64 - RESILIENCE WITH CIRCUIT BREAKERS (the flaky downstream service)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/64-resilience-with-circuit-breakers/inventory-service spring-boot:run
// ============================================================================
@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
