package com.danish.spring.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

// ============================================================================
// 64 - RESILIENCE WITH CIRCUIT BREAKERS (calls the flaky service THROUGH a breaker)
// ============================================================================
// Requires inventory-service (8081) already running.
// Run: mvn -f Spring/12-microservices-with-spring-cloud/64-resilience-with-circuit-breakers/order-service spring-boot:run
// ============================================================================
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
