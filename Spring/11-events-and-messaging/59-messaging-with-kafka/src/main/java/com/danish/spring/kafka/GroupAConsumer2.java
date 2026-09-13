package com.danish.spring.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class GroupAConsumer2 {

    private final EventRecorder recorder;

    public GroupAConsumer2(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC, groupId = "group-a")
    public void onMessage(ConsumerRecord<String, OrderEvent> record) {
        recorder.record("group-a-consumer-2", record.key(), record.partition(), record.value());
    }
}
