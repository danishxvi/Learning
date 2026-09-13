package com.danish.spring.events;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// ============================================================================
// 57 - APPLICATION EVENTS AND @EventListener
// ============================================================================
// Run: mvn -f Spring/11-events-and-messaging/57-application-events-and-eventlistener spring-boot:run
// ============================================================================
@SpringBootApplication
public class EventsApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventsApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final OrderService orderService;
        private final OrderRecordRepository orderRecordRepository;
        private final ApplicationEventPublisher publisher;
        private final EventRecorder recorder;
        private final ConfigurableApplicationContext context;

        Demo(OrderService orderService, OrderRecordRepository orderRecordRepository, ApplicationEventPublisher publisher,
             EventRecorder recorder, ConfigurableApplicationContext context) {
            this.orderService = orderService;
            this.orderRecordRepository = orderRecordRepository;
            this.publisher = publisher;
            this.recorder = recorder;
            this.context = context;
        }

        @Override
        public void run(String... args) throws Exception {
            runSynchronousVsAsyncScenario();
            System.out.println();
            runRollbackScenario();
            System.out.println();
            runExceptionPropagationScenario();

            context.close();
            System.exit(0);
        }

        private void runSynchronousVsAsyncScenario() throws InterruptedException {
            System.out.println("=".repeat(74));
            System.out.println("SYNCHRONOUS vs @Async listeners, and @Order among synchronous ones");
            System.out.println("=".repeat(74));

            long start = System.currentTimeMillis();
            orderService.placeOrder("order-1", BigDecimal.valueOf(100), false);
            long elapsed = System.currentTimeMillis() - start;

            System.out.println("  placeOrder() returned after " + elapsed + "ms");
            System.out.println("  entries recorded so far: " + describe(recorder.getEntries()));
            System.out.println("  ('async-email' not necessarily present yet - it runs on its own thread)");

            Thread.sleep(700);
            System.out.println("  after waiting 700ms for the async listener: " + describe(recorder.getEntries()));
        }

        private void runRollbackScenario() {
            System.out.println("=".repeat(74));
            System.out.println("@TransactionalEventListener(AFTER_COMMIT) does NOT fire on rollback");
            System.out.println("=".repeat(74));

            recorder.clear();
            try {
                orderService.placeOrder("order-2", BigDecimal.valueOf(50), true);
                System.out.println("  placeOrder did not throw (unexpected)");
            } catch (RuntimeException ex) {
                System.out.println("  placeOrder threw as expected: " + ex.getMessage());
            }

            System.out.println("  plain listeners that already ran before the rollback: "
                    + describe(recorder.getEntries().stream().filter(e -> !e.listenerName().startsWith("async")).toList()));
            System.out.println("  accounting-after-commit fired? " + recorder.has("accounting-after-commit")
                    + "  (should be false - the transaction rolled back)");

            boolean persisted = orderRecordRepository.findAll().stream().anyMatch(o -> o.getOrderId().equals("order-2"));
            System.out.println("  order-2 actually persisted in the database? " + persisted
                    + "  (should be false - proves the rollback was real, not just the event)");
        }

        private void runExceptionPropagationScenario() {
            System.out.println("=".repeat(74));
            System.out.println("an exception in one synchronous listener stops the REST from running");
            System.out.println("=".repeat(74));

            recorder.clear();
            try {
                publisher.publishEvent(new RiskyEvent("risky-1"));
                System.out.println("  publishEvent did not throw (unexpected)");
            } catch (RuntimeException ex) {
                System.out.println("  publishEvent threw: " + ex.getMessage());
            }

            System.out.println("  risky-first ran? " + recorder.has("risky-first"));
            System.out.println("  risky-second ran? " + recorder.has("risky-second")
                    + "  (should be false - it never got a chance to run)");
        }

        private String describe(java.util.List<EventRecorder.Entry> entries) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < entries.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(entries.get(i).listenerName()).append("@").append(entries.get(i).threadName());
            }
            return sb.append("]").toString();
        }
    }
}
