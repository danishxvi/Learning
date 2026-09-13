package com.danish.spring.scheduling;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ExecutionRecorder {

    private final long startNanos = System.nanoTime();
    private final Map<String, List<Long>> executionsMsSinceStart = new ConcurrentHashMap<>();
    private final Map<String, List<String>> threadNames = new ConcurrentHashMap<>();

    public void record(String taskName) {
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
        executionsMsSinceStart.computeIfAbsent(taskName, k -> new CopyOnWriteArrayList<>()).add(elapsedMs);
        threadNames.computeIfAbsent(taskName, k -> new CopyOnWriteArrayList<>()).add(Thread.currentThread().getName());
    }

    public List<Long> getExecutions(String taskName) {
        return executionsMsSinceStart.getOrDefault(taskName, List.of());
    }

    public List<String> getThreadNames(String taskName) {
        return threadNames.getOrDefault(taskName, List.of());
    }
}
