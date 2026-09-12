package com.danish.spring.runners;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 17 - CommandLineRunner AND ApplicationRunner
// ============================================================================
// Plain run:
//   mvn -f Spring/03-spring-boot-fundamentals/17-commandlinerunner-and-applicationrunner spring-boot:run
// With arguments (options AND a positional argument):
//   mvn -f Spring/03-spring-boot-fundamentals/17-commandlinerunner-and-applicationrunner spring-boot:run "-Dspring-boot.run.arguments=--name=Danish --retries=3 hello"
// Triggering the abort:
//   mvn -f Spring/03-spring-boot-fundamentals/17-commandlinerunner-and-applicationrunner spring-boot:run -Dspring-boot.run.arguments=--fail
// ============================================================================
@SpringBootApplication
public class RunnersApplication {
    public static void main(String[] args) {
        System.out.println("=".repeat(74));
        System.out.println("RUNNER EXECUTION ORDER, CONTROLLED BY @Order");
        System.out.println("=".repeat(74));
        SpringApplication.run(RunnersApplication.class, args);
        System.out.println();
        System.out.println("This line only prints if EVERY runner completed without throwing.");
    }
}
