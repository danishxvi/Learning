package com.danish.spring.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // The key determines the partition: Kafka hashes the key to pick one of the
    // topic's partitions, and EVERY message with the same key always lands on the SAME
    // partition - that's what gives per-key ordering within a topic.
    public void send(String key, OrderEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC, key, event);
    }
}
