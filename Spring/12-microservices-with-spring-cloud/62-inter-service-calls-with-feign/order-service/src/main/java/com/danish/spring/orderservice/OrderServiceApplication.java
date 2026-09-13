package com.danish.spring.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// ============================================================================
// 62 - INTER-SERVICE CALLS WITH FEIGN (a client calling another service declaratively)
// ============================================================================
// Requires eureka-server (8761) and inventory-service (8081) already running.
// Run: mvn -f Spring/12-microservices-with-spring-cloud/62-inter-service-calls-with-feign/order-service spring-boot:run
// ============================================================================
@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
