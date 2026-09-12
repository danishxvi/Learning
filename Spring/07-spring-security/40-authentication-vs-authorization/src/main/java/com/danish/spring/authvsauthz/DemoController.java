package com.danish.spring.authvsauthz;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello, anyone.";
    }

    @GetMapping("/user/profile")
    public String userProfile() {
        return "Any authenticated user can see this.";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "Only an ADMIN can see this.";
    }
}
