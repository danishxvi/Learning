package com.danish.spring.orderservice;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final InventoryClient inventoryClient;

    public OrderController(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @GetMapping("/order-with-stock/{productId}")
    public String orderWithStock(@PathVariable String productId) {
        String stockResponse = inventoryClient.getStock(productId);
        return "order-service called inventory-service via Feign and got: " + stockResponse;
    }
}
