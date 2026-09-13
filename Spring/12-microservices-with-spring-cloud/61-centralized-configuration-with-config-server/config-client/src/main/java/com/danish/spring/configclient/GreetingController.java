package com.danish.spring.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// @RefreshScope makes this bean eligible to be recreated (picking up new @Value
// bindings) when POST /actuator/refresh is called - WITHOUT restarting the
// application. Without this annotation, @Value would only ever be bound once, at
// startup, and would keep serving the ORIGINAL value forever regardless of what
// changes on the Config Server afterward.
@RefreshScope
@RestController
public class GreetingController {

    @Value("${greeting.message}")
    private String greetingMessage;

    @GetMapping("/greeting")
    public String greeting() {
        return greetingMessage;
    }
}
