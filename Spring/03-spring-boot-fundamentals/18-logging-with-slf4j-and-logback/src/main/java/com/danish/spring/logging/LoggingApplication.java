package com.danish.spring.logging;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 18 - LOGGING WITH SLF4J AND LOGBACK
// ============================================================================
// Run: mvn -f Spring/03-spring-boot-fundamentals/18-logging-with-slf4j-and-logback spring-boot:run
// With the file appender active: add -Dspring-boot.run.profiles=verbose, then check
// target/verbose-demo.log afterward.
// ============================================================================
@SpringBootApplication
public class LoggingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoggingApplication.class, args);
    }

    @Component
    static class LoggingDemo implements CommandLineRunner {
        private final OrderProcessor orderProcessor;
        private final RequestSimulator requestSimulator;

        LoggingDemo(OrderProcessor orderProcessor, RequestSimulator requestSimulator) {
            this.orderProcessor = orderProcessor;
            this.requestSimulator = requestSimulator;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("FIVE LOG LEVELS - all print, because application.yml raised THIS package to DEBUG");
            System.out.println("=".repeat(74));
            orderProcessor.demonstrateLevels();

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("PARAMETERIZED VS CONCATENATED LOGGING");
            System.out.println("=".repeat(74));
            orderProcessor.demonstrateParameterizedLogging();

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("LOGGING AN EXCEPTION WITH ITS FULL STACK TRACE");
            System.out.println("=".repeat(74));
            orderProcessor.demonstrateExceptionLogging();

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("MDC - every line below carries the SAME requestId, from logback-spring.xml's pattern");
            System.out.println("=".repeat(74));
            requestSimulator.handle("req-8841", "checkout");
        }
    }
}
