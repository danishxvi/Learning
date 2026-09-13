package com.danish.spring.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 62 - INTER-SERVICE CALLS WITH FEIGN (the service being called)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/62-inter-service-calls-with-feign/inventory-service spring-boot:run
// ============================================================================
@SpringBootApplication
public class InventoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
