package com.danish.spring.intro;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// ============================================================================
// 02 - SPRING VS SPRING BOOT, AND THE ECOSYSTEM AROUND THEM
// ============================================================================
// This is the FIRST real Spring code in this stack. No Spring Boot yet - this
// is "plain" Spring Framework, started by hand, so you see exactly what Boot
// will later do FOR you automatically in lesson 03.
//
// Run: mvn -f Spring/01-introduction-and-setup/02-spring-vs-spring-boot-and-the-ecosystem compile exec:java
// ============================================================================
public class SpringVsSpringBootDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(74));
        System.out.println("STARTING THE CONTAINER (this line runs BEFORE any bean exists)");
        System.out.println("=".repeat(74));

        long start = System.nanoTime();

        // This one line replaces every `container.register(...)` call from lesson 01's
        // MiniContainer. AnnotationConfigApplicationContext reads AppConfig, finds
        // @ComponentScan, walks com.danish.spring.intro with reflection, finds every
        // @Component class, works out each one's constructor dependencies, and builds
        // the whole graph - all before this line returns.
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        long startupMillis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Container ready in " + startupMillis + " ms.");

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("ASKING THE CONTAINER FOR A FULLY-WIRED BEAN");
        System.out.println("=".repeat(74));

        // We never wrote `new OrderService(new SmtpEmailNotifier())` anywhere. The
        // container built OrderService, saw it needed a Notifier, found the ONE
        // @Component that implements Notifier, and injected it - automatically.
        OrderService orderService = context.getBean(OrderService.class);
        orderService.placeOrder("desk-lamp");

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("BEANS ARE SINGLETONS BY DEFAULT");
        System.out.println("=".repeat(74));

        OrderService again = context.getBean(OrderService.class);
        System.out.println("Same instance returned on second getBean() call: " + (orderService == again));
        System.out.println("(Exactly like lesson 01's MiniContainer cache - covered properly in lesson 10.)");

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("WHAT THIS PROJECT DOES NOT HAVE, THAT SPRING BOOT ADDS");
        System.out.println("=".repeat(74));
        System.out.println("  - No embedded web server. This program starts, does its work, and exits.");
        System.out.println("  - No auto-configuration. We wrote AppConfig ourselves, by hand.");
        System.out.println("  - No 'starter' dependency. We hand-picked spring-context's exact version.");
        System.out.println("  - No externalized application.properties/yml (lesson 05).");
        System.out.println("Lesson 03 introduces Spring Boot, which adds all four - on top of the exact");
        System.out.println("same IoC container you just watched start above.");

        // Plain Spring applications must close their own context; Spring Boot does
        // this for you via a shutdown hook, covered when we get there.
        context.close();
    }
}
