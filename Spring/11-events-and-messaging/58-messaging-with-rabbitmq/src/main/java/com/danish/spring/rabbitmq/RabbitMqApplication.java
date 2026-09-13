package com.danish.spring.rabbitmq;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

// ============================================================================
// 58 - MESSAGING WITH RABBITMQ
// ============================================================================
// Requires a real RabbitMQ broker. Start one with:
//   docker run -d --name learning-rabbitmq -p 5673:5672 -p 15673:15672 rabbitmq:3-management-alpine
// Run: mvn -f Spring/11-events-and-messaging/58-messaging-with-rabbitmq spring-boot:run
// ============================================================================
@SpringBootApplication
public class RabbitMqApplication {
    public static void main(String[] args) {
        SpringApplication.run(RabbitMqApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final OrderEventPublisher publisher;
        private final EventRecorder recorder;
        private final ConfigurableApplicationContext context;

        Demo(OrderEventPublisher publisher, EventRecorder recorder, ConfigurableApplicationContext context) {
            this.publisher = publisher;
            this.recorder = recorder;
            this.context = context;
        }

        @Override
        public void run(String... args) throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("publishing order.created and order.shipped to a topic exchange");
            System.out.println("=".repeat(74));

            publisher.publish("order.created", new OrderEvent("order-1", "CREATED"));
            publisher.publish("order.shipped", new OrderEvent("order-1", "SHIPPED"));

            Thread.sleep(1000);

            System.out.println("  entries received by listeners:");
            recorder.getEntries().forEach(e -> System.out.println("    " + e));

            context.close();
            System.exit(0);
        }
    }
}
