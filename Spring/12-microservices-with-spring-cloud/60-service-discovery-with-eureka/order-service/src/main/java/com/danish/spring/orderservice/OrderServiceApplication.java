package com.danish.spring.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

// ============================================================================
// 60 - SERVICE DISCOVERY WITH EUREKA (a client that DISCOVERS another service)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/60-service-discovery-with-eureka/order-service spring-boot:run
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
