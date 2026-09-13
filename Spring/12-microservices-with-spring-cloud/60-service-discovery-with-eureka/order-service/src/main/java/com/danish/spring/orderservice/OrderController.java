package com.danish.spring.orderservice;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    public OrderController(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    // Asks Eureka (via the DiscoveryClient abstraction) which real instances are
    // currently registered under the logical name "inventory-service" - no hardcoded
    // host or port anywhere in this method.
    @GetMapping("/discover/inventory-service")
    public String discoverInventoryService() {
        List<ServiceInstance> instances = discoveryClient.getInstances("inventory-service");
        return instances.stream()
                .map(i -> i.getServiceId() + " @ " + i.getUri())
                .collect(Collectors.joining(", "));
    }

    // Discovers inventory-service's address at CALL TIME and forwards the request to
    // it - the URL is never hardcoded; if inventory-service moved to a different port
    // (or a different machine), this code would need no change at all.
    @GetMapping("/order-with-stock/{productId}")
    public String orderWithStock(@PathVariable String productId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("inventory-service");
        if (instances.isEmpty()) {
            return "inventory-service is not currently registered with Eureka";
        }
        ServiceInstance instance = instances.get(0);
        String url = instance.getUri() + "/inventory/" + productId;
        String inventoryResponse = restTemplate.getForObject(url, String.class);
        return "order-service discovered inventory-service at " + instance.getUri() + " and got: " + inventoryResponse;
    }
}
