package com.danish.spring.orderservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryService {

    private final RestTemplate restTemplate;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    public InventoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // name = "inventoryService" must match the resilience4j.circuitbreaker.instances.*
    // key in application.properties - that's how this method's calls get tracked
    // against THAT breaker's sliding window, thresholds, and state.
    //
    // fallbackMethod is called with the SAME arguments plus the Throwable that
    // triggered it - either a real exception from a failed call, or (once the breaker
    // is OPEN) CallNotPermittedException, thrown WITHOUT the real call ever happening.
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackStock")
    public String getStock(String productId) {
        return restTemplate.getForObject(inventoryServiceUrl + "/inventory/" + productId, String.class);
    }

    public String fallbackStock(String productId, Throwable throwable) {
        return "FALLBACK: stock for " + productId + " unavailable right now (" + throwable.getClass().getSimpleName() + ")";
    }
}
