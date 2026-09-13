package com.danish.spring.kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ============================================================================
// 59 - MESSAGING WITH KAFKA
// ============================================================================
// Requires a real Kafka broker (KRaft mode, no Zookeeper needed). Start one with:
//   docker run -d --name learning-kafka -p 9094:9094 \
//     -e KAFKA_NODE_ID=1 -e KAFKA_PROCESS_ROLES=broker,controller \
//     -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094 \
//     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092,EXTERNAL://localhost:9094 \
//     -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
//     -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
//     -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,EXTERNAL:PLAINTEXT \
//     -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
//     -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
//     apache/kafka:3.7.0
// Run: mvn -f Spring/11-events-and-messaging/59-messaging-with-kafka spring-boot:run
// ============================================================================
@SpringBootApplication
public class KafkaMessagingApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaMessagingApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final OrderEventProducer producer;
        private final EventRecorder recorder;
        private final ConfigurableApplicationContext context;

        Demo(OrderEventProducer producer, EventRecorder recorder, ConfigurableApplicationContext context) {
            this.producer = producer;
            this.recorder = recorder;
            this.context = context;
        }

        @Override
        public void run(String... args) throws Exception {
            // Give the consumer groups time to finish their initial coordinator
            // discovery and rebalance before sending anything - a fresh single-node
            // KRaft broker takes a few seconds to settle group coordination on startup.
            Thread.sleep(5000);

            System.out.println("=".repeat(74));
            System.out.println("sending 6 events across 3 keys to a 3-partition topic");
            System.out.println("=".repeat(74));

            String[] keys = {"order-1", "order-2", "order-1", "order-3", "order-2", "order-1"};
            for (int i = 0; i < keys.length; i++) {
                producer.send(keys[i], new OrderEvent(keys[i], "STATUS-" + i));
            }

            Thread.sleep(10000);

            System.out.println("  raw entries received:");
            recorder.getEntries().forEach(e -> System.out.println("    " + e.consumerLabel() + " | key=" + e.key()
                    + " partition=" + e.partition() + " | " + e.event()));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("partition affinity: does every message for the SAME key land on the SAME partition?");
            System.out.println("=".repeat(74));
            Map<String, java.util.Set<Integer>> partitionsPerKey = new HashMap<>();
            for (EventRecorder.Entry e : recorder.getEntries()) {
                if (e.consumerLabel().startsWith("group-a")) {
                    partitionsPerKey.computeIfAbsent(e.key(), k -> new java.util.TreeSet<>()).add(e.partition());
                }
            }
            partitionsPerKey.forEach((key, partitions) -> System.out.println("  " + key + " -> partitions " + partitions
                    + (partitions.size() == 1 ? "  (consistent - always the same partition)" : "  (INCONSISTENT - unexpected)")));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("consumer groups: group-a SHARES messages, group-b gets its OWN full copy");
            System.out.println("=".repeat(74));
            long groupATotal = recorder.getEntries().stream().filter(e -> e.consumerLabel().startsWith("group-a")).count();
            long groupA1 = recorder.getEntries().stream().filter(e -> e.consumerLabel().equals("group-a-consumer-1")).count();
            long groupA2 = recorder.getEntries().stream().filter(e -> e.consumerLabel().equals("group-a-consumer-2")).count();
            long groupBTotal = recorder.getEntries().stream().filter(e -> e.consumerLabel().equals("group-b-consumer")).count();

            System.out.println("  messages sent: " + keys.length);
            System.out.println("  group-a total received (consumer-1=" + groupA1 + " + consumer-2=" + groupA2 + "): " + groupATotal
                    + (groupATotal == keys.length ? "  (matches - shared across the group, no duplicates)" : "  (unexpected)"));
            System.out.println("  group-b total received: " + groupBTotal
                    + (groupBTotal == keys.length ? "  (matches - its OWN full copy, independent of group-a)" : "  (unexpected)"));

            context.close();
            System.exit(0);
        }
    }
}
