package com.danish.spring.unittesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// ============================================================================
// 46 - UNIT TESTING WITH JUnit 5 AND MOCKITO
// ============================================================================
// This lesson is unusual: the interesting code lives in src/test/java, not here.
// Run it: mvn -f Spring/08-testing-spring-applications/46-unit-testing-with-junit5-and-mockito test
// (Run the APP itself only to prove it's wired correctly - see OrderServiceTest for the
// actual lesson content.)
// ============================================================================
@SpringBootApplication
public class UnitTestingApplication {
    public static void main(String[] args) {
        SpringApplication.run(UnitTestingApplication.class, args);
    }

    // A trivial real PricingService, just so the app context has SOMETHING to wire
    // OrderService's second dependency to if it were ever started for real.
    @Bean
    public PricingService pricingService() {
        return product -> switch (product) {
            case "desk-lamp" -> 2499;
            case "keyboard" -> 4999;
            default -> 999;
        };
    }

    @Bean
    public OrderRepository orderRepository() {
        return order -> { order.setId(1L); return order; };
    }
}
