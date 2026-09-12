package com.danish.spring.scopes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

// ============================================================================
// 10 - BEAN SCOPES
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/10-bean-scopes spring-boot:run
// ============================================================================
@SpringBootApplication
public class ScopesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScopesApplication.class, args);
    }

    @Component
    static class ScopesDemo implements CommandLineRunner {

        @Autowired private ApplicationContext context;
        @Autowired private SingletonCounter singletonCounter;
        @Autowired private BrokenPrototypeHolder brokenPrototypeHolder;
        @Autowired private ObjectProviderHolder objectProviderHolder;
        @Autowired private ScopedProxyHolder scopedProxyHolder;

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("SINGLETON (the default) - EVERY getBean() call returns the SAME instance");
            System.out.println("=".repeat(74));
            for (int i = 0; i < 3; i++) {
                System.out.println("  context.getBean() count=" + context.getBean(SingletonCounter.class).increment());
            }
            System.out.println("  All three calls shared one instance's state - that is what singleton means.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("PROTOTYPE, ASKED DIRECTLY - a NEW instance every getBean() call");
            System.out.println("=".repeat(74));
            for (int i = 0; i < 3; i++) {
                Counter counter = context.getBean(Counter.class);
                System.out.println("  instanceId=" + counter.getInstanceId() + ", count=" + counter.increment());
            }
            System.out.println("  Three DIFFERENT instance IDs, every count is 1 - each was brand new.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("PROTOTYPE INJECTED INTO A SINGLETON - THE COMMON MISTAKE");
            System.out.println("=".repeat(74));
            for (int i = 0; i < 3; i++) {
                brokenPrototypeHolder.incrementAndPrint();
            }
            System.out.println("  SAME instanceId every time, count kept climbing - one Counter got frozen");
            System.out.println("  into the singleton at construction time. \"prototype\" never manifested here.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("FIX #1: ObjectProvider<Counter> - lazily ask again on every use");
            System.out.println("=".repeat(74));
            for (int i = 0; i < 3; i++) {
                objectProviderHolder.incrementAndPrint();
            }
            System.out.println("  DIFFERENT instanceId every call, count always 1 - genuinely fresh each time.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("FIX #2: a SCOPED PROXY - looks like direct injection, isn't");
            System.out.println("=".repeat(74));
            for (int i = 0; i < 3; i++) {
                scopedProxyHolder.incrementAndPrint();
            }
            System.out.println("  DIFFERENT instanceId every call too - the proxy re-resolved on every method call.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("THE WEB SCOPES - request AND session")  ;
            System.out.println("=".repeat(74));
            System.out.println("  Two more scopes exist - \"request\" (one instance per HTTP request) and");
            System.out.println("  \"session\" (one instance per user session). Both need an active web request");
            System.out.println("  to mean anything, so they cannot run in this lesson's plain console app -");
            System.out.println("  they return once section 04 introduces Spring MVC and real HTTP requests.");
        }
    }
}
