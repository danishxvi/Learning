package com.danish.spring.aspects;

import com.danish.spring.aspects.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 39 - ASPECTS, POINTCUTS AND ADVICE
// ============================================================================
// Run: mvn -f Spring/06-aspect-oriented-programming/39-aspects-pointcuts-and-advice spring-boot:run
// ============================================================================
@SpringBootApplication
public class AspectsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AspectsApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final OrderService orderService;

        Demo(OrderService orderService) {
            this.orderService = orderService;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("THE HAPPY PATH - @Before, @AfterReturning, @After all fire");
            System.out.println("=".repeat(74));
            String result = orderService.placeOrder("desk-lamp");
            System.out.println("  Method actually returned: " + result);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("THE FAILURE PATH - @Before, @AfterThrowing, @After fire; NOT @AfterReturning");
            System.out.println("=".repeat(74));
            try {
                orderService.cancelOrder("missing");
            } catch (IllegalArgumentException ex) {
                System.out.println("  Exception reached the caller anyway: " + ex.getMessage());
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Around WITH A CUSTOM @Timed ANNOTATION - a different matching style entirely");
            System.out.println("=".repeat(74));
            orderService.slowReport();
        }
    }
}
