package com.danish.spring.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Two beans, SAME groupId ("group-a") - simulating two instances of the same service.
// Kafka assigns each of the topic's partitions to exactly ONE consumer within a
// group, so any given message is delivered to only ONE of GroupAConsumer1/2, never both.
@Component
public class GroupAConsumer1 {

    private final EventRecorder recorder;

    public GroupAConsumer1(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC, groupId = "group-a")
    public void onMessage(ConsumerRecord<String, OrderEvent> record) {
        recorder.record("group-a-consumer-1", record.key(), record.partition(), record.value());
    }
}
