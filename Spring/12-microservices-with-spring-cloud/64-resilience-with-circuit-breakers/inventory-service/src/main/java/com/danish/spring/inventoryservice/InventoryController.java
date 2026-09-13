package com.danish.spring.inventoryservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class InventoryController {

    // Starts in FAILURE mode on purpose - the whole lesson is about how order-service
    // reacts while this dependency is broken.
    private final AtomicBoolean failing = new AtomicBoolean(true);
    private final AtomicInteger callCount = new AtomicInteger();

    @GetMapping("/inventory/{productId}")
    public String getStock(@PathVariable String productId) {
        callCount.incrementAndGet();
        if (failing.get()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "inventory-service is currently failing (simulated)");
        }
        return "product " + productId + " has 42 units in stock";
    }

    // Lets the demo flip this service between "broken" and "healthy" WITHOUT
    // restarting it - used to demonstrate the circuit breaker recovering once the real
    // dependency recovers.
    @PostMapping("/toggle-failure")
    public String toggleFailure() {
        boolean newState = !failing.get();
        failing.set(newState);
        return "failing=" + newState;
    }

    // The real, independent proof of whether a request actually reached this service -
    // used to confirm the circuit breaker genuinely stops calling this service while OPEN.
    @GetMapping("/call-count")
    public int getCallCount() {
        return callCount.get();
    }
}
