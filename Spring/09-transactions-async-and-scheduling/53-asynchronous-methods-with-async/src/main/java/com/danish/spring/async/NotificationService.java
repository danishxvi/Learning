package com.danish.spring.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    // Returning CompletableFuture<T> lets the caller find out what happened AND when -
    // Spring's @Async proxy runs this method body on the configured Executor and
    // completes the returned future with whatever this method returns (or exceptionally,
    // with whatever it throws).
    @Async
    public CompletableFuture<String> sendEmailAsync(String to, long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        return CompletableFuture.completedFuture(to + " notified by " + Thread.currentThread().getName());
    }

    // A void @Async method is fire-and-forget: the caller gets NOTHING back, not even a
    // future to check later. If this throws, the caller's call site already returned
    // long ago - the only place that exception can go is the AsyncUncaughtExceptionHandler.
    @Async
    public void sendEmailFireAndForget(String to) {
        throw new RuntimeException("simulated failure sending to " + to);
    }
}
