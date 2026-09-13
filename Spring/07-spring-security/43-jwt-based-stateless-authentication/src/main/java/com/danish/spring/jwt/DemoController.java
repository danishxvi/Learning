package com.danish.spring.jwt;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/public/info")
    public String publicInfo() {
        return "No token needed.";
    }

    @GetMapping("/protected/profile")
    public String protectedProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return "Hello, " + username + " - this required a valid JWT.";
    }
}
