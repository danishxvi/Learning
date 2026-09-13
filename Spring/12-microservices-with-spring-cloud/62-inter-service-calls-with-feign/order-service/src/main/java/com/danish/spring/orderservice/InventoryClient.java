package com.danish.spring.orderservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// A declarative HTTP client - no implementation body anywhere. "name" is the Eureka
// service name to discover and load-balance against, exactly like DiscoveryClient did
// manually in lesson 60 - Feign does the lookup and the HTTP call for you.
//
// @PathVariable is NOT optional here the way it can be on a Spring MVC @Controller
// method with -parameters compiled in - Feign's contract needs an explicit annotation
// to know a parameter fills a URI template variable. See the .md for the real 404 this
// produces without it.
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/inventory/{productId}")
    String getStock(@PathVariable("productId") String productId);
}
