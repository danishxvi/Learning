package com.danish.spring.kafka;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventRecorder {

    public record Entry(String consumerLabel, String key, int partition, OrderEvent event) {
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public void record(String consumerLabel, String key, int partition, OrderEvent event) {
        entries.add(new Entry(consumerLabel, key, partition, event));
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
