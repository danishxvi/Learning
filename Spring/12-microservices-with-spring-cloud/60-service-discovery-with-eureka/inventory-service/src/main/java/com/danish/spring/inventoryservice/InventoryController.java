package com.danish.spring.inventoryservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InventoryController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/inventory/{productId}")
    public String getStock(@PathVariable String productId) {
        return "product " + productId + " has 42 units in stock (answered by inventory-service on port " + port + ")";
    }
}
