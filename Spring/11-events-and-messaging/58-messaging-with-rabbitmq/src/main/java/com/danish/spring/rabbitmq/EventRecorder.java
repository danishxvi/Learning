package com.danish.spring.rabbitmq;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventRecorder {

    private final List<String> entries = new CopyOnWriteArrayList<>();

    public void record(String entry) {
        entries.add(entry);
    }

    public List<String> getEntries() {
        return entries;
    }
}
