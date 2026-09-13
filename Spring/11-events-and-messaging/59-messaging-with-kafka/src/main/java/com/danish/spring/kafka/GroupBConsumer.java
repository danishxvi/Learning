package com.danish.spring.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// A DIFFERENT groupId ("group-b") gets its OWN full copy of every message, entirely
// independent of group-a's load-sharing - Kafka tracks consumer offsets PER GROUP, so
// group-a and group-b each read the whole topic from their own position.
@Component
public class GroupBConsumer {

    private final EventRecorder recorder;

    public GroupBConsumer(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC, groupId = "group-b")
    public void onMessage(ConsumerRecord<String, OrderEvent> record) {
        recorder.record("group-b-consumer", record.key(), record.partition(), record.value());
    }
}
