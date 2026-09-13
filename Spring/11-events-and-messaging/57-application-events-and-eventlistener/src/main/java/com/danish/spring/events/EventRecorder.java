package com.danish.spring.events;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventRecorder {

    public record Entry(String listenerName, String threadName) {
    }

    private final List<Entry> entries = new CopyOnWriteArrayList<>();

    public void record(String listenerName) {
        entries.add(new Entry(listenerName, Thread.currentThread().getName()));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public boolean has(String listenerName) {
        return entries.stream().anyMatch(e -> e.listenerName().equals(listenerName));
    }

    public void clear() {
        entries.clear();
    }
}
