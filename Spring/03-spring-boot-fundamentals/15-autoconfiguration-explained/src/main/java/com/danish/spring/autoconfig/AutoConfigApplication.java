package com.danish.spring.autoconfig;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 15 - AUTO-CONFIGURATION, EXPLAINED
// ============================================================================
// Default (our own auto-configuration supplies the bean):
//   mvn -f Spring/03-spring-boot-fundamentals/15-autoconfiguration-explained spring-boot:run
// With the app's own bean overriding it:
//   mvn -f Spring/03-spring-boot-fundamentals/15-autoconfiguration-explained spring-boot:run -Dspring-boot.run.profiles=custom
// To see Boot's own CONDITIONS EVALUATION REPORT naming GreetingAutoConfiguration:
//   mvn -f Spring/03-spring-boot-fundamentals/15-autoconfiguration-explained spring-boot:run -Dspring-boot.run.arguments=--debug
// ============================================================================
@SpringBootApplication
public class AutoConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoConfigApplication.class, args);
    }

    @Component
    static class AutoConfigDemo implements CommandLineRunner {

        private final GreetingService greetingService;

        AutoConfigDemo(GreetingService greetingService) {
            this.greetingService = greetingService;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("WHICH GreetingService BEAN WON?");
            System.out.println("=".repeat(74));
            System.out.println("  " + greetingService.greet());
            System.out.println();
            System.out.println("This GreetingService bean was NEVER found by @ComponentScan - it came from");
            System.out.println("META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports,");
            System.out.println("the exact file @EnableAutoConfiguration reads to decide what to even consider.");
        }
    }
}
