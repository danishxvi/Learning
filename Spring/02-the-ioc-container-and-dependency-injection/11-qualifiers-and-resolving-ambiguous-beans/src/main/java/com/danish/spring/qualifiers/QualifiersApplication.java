package com.danish.spring.qualifiers;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

// ============================================================================
// 11 - QUALIFIERS AND RESOLVING AMBIGUOUS BEANS
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/11-qualifiers-and-resolving-ambiguous-beans spring-boot:run
// ============================================================================
@SpringBootApplication
public class QualifiersApplication {

    public static void main(String[] args) {
        SpringApplication.run(QualifiersApplication.class, args);
    }

    @Component
    static class QualifiersDemo implements CommandLineRunner {

        private final PrimaryConsumer primaryConsumer;
        private final QualifiedConsumer qualifiedConsumer;
        private final BroadcastNotifier broadcastNotifier;

        QualifiersDemo(PrimaryConsumer primaryConsumer, QualifiedConsumer qualifiedConsumer,
                       BroadcastNotifier broadcastNotifier) {
            this.primaryConsumer = primaryConsumer;
            this.qualifiedConsumer = qualifiedConsumer;
            this.broadcastNotifier = broadcastNotifier;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("THREE Notifier BEANS EXIST: EmailNotifier, SmsNotifier, PushNotifier");
            System.out.println("=".repeat(74));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("THE AMBIGUITY, REPRODUCED: no @Primary, no @Qualifier anywhere");
            System.out.println("=".repeat(74));
            try (AnnotationConfigApplicationContext ambiguousContext = new AnnotationConfigApplicationContext(
                    StripeGateway.class, PaypalGateway.class, CheckoutNeedingUnqualifiedGateway.class)) {
                System.out.println("  This line never runs.");
            } catch (BeanCreationException ex) {
                Throwable rootCause = ex;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                System.out.println("  " + rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage());
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Primary RESOLVES THE SAME AMBIGUITY, SILENTLY, FOR AN UNQUALIFIED INJECTION");
            System.out.println("=".repeat(74));
            System.out.println("  Plain `Notifier notifier` resolved to: " + primaryConsumer.resolvedType());
            primaryConsumer.notify("Your order shipped.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Qualifier NAMES A SPECIFIC BEAN, OVERRIDING @Primary");
            System.out.println("=".repeat(74));
            System.out.println("  @Qualifier(\"smsNotifier\") resolved to: " + qualifiedConsumer.resolvedType());
            qualifiedConsumer.notify("Your OTP is 482913.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("List<Notifier> - ALL THREE, ORDERED BY @Order");
            System.out.println("=".repeat(74));
            System.out.println("  " + broadcastNotifier.orderedTypeNames());
            broadcastNotifier.broadcastToAll("System maintenance at midnight.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("Map<String, Notifier> - KEYED BY BEAN NAME");
            System.out.println("=".repeat(74));
            broadcastNotifier.beanNameToType().forEach((name, type) -> System.out.println("  " + name + " -> " + type));
        }
    }
}
