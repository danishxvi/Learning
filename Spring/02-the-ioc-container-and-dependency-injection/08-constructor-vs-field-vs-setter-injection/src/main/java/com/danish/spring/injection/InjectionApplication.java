package com.danish.spring.injection;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

// ============================================================================
// 08 - CONSTRUCTOR VS FIELD VS SETTER INJECTION
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/08-constructor-vs-field-vs-setter-injection spring-boot:run
// ============================================================================
@SpringBootApplication
public class InjectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(InjectionApplication.class, args);
    }

    @Component
    static class InjectionDemo implements CommandLineRunner {

        private final ConstructorInjectedOrderService constructorInjected;
        private final ReportGenerator reportGenerator;
        private final GreetingService greetingService;
        private final FixedServiceA fixedServiceA;

        InjectionDemo(ConstructorInjectedOrderService constructorInjected,
                      ReportGenerator reportGenerator,
                      GreetingService greetingService,
                      FixedServiceA fixedServiceA) {
            this.constructorInjected = constructorInjected;
            this.reportGenerator = reportGenerator;
            this.greetingService = greetingService;
            this.fixedServiceA = fixedServiceA;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("CONSTRUCTOR INJECTION - works fine built by Spring...");
            System.out.println("=".repeat(74));
            constructorInjected.placeOrder("keyboard");

            System.out.println();
            System.out.println("...AND works fine built by hand, with a fake Notifier - no Spring needed:");
            new ConstructorInjectedOrderService(msg -> System.out.println("  [fake in a test] " + msg))
                    .placeOrder("test-widget");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("FIELD INJECTION - fine via Spring, BROKEN built by hand");
            System.out.println("=".repeat(74));
            FieldInjectedOrderService byHand = new FieldInjectedOrderService();
            byHand.placeOrder("mouse"); // notifier is null - there was no constructor to pass one into

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("SETTER INJECTION - the right tool for an OPTIONAL dependency");
            System.out.println("=".repeat(74));
            reportGenerator.generateReport(); // no AuditLogger bean exists anywhere in this context

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("MULTIPLE CONSTRUCTORS - @Autowired picks which one Spring calls");
            System.out.println("=".repeat(74));
            greetingService.greet("Danish");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("CIRCULAR DEPENDENCY - constructor injection FAILS FAST");
            System.out.println("=".repeat(74));
            try (AnnotationConfigApplicationContext brokenContext =
                         new AnnotationConfigApplicationContext(BrokenServiceA.class, BrokenServiceB.class)) {
                System.out.println("  This line never runs.");
            } catch (BeanCreationException ex) {
                System.out.println("  Context refused to start: " + ex.getClass().getSimpleName());
                System.out.println("  Root cause: " + rootCauseMessage(ex));
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("THE SAME CYCLE, FIXED WITH @Lazy on one side");
            System.out.println("=".repeat(74));
            fixedServiceA.ping();
        }

        private static String rootCauseMessage(Throwable t) {
            Throwable current = t;
            while (current.getCause() != null) {
                current = current.getCause();
            }
            return current.getClass().getSimpleName() + ": " + current.getMessage();
        }
    }
}
