package com.danish.spring.orderservice;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final InventoryService inventoryService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public OrderController(InventoryService inventoryService, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.inventoryService = inventoryService;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping("/order-with-stock/{productId}")
    public String orderWithStock(@PathVariable String productId) {
        return inventoryService.getStock(productId);
    }

    // Queries the breaker's REAL current state directly from Resilience4j's own
    // registry - not inferred from HTTP behavior, the actual CircuitBreaker.State enum
    // value (CLOSED, OPEN, HALF_OPEN).
    @GetMapping("/circuit-state")
    public String circuitState() {
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("inventoryService");
        CircuitBreaker.Metrics metrics = breaker.getMetrics();
        return "state=" + breaker.getState()
                + ", failureRate=" + metrics.getFailureRate() + "%"
                + ", bufferedCalls=" + metrics.getNumberOfBufferedCalls()
                + ", failedCalls=" + metrics.getNumberOfFailedCalls();
    }
}
