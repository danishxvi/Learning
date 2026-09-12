package com.danish.spring.filterchain;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("  [DemoController] handler method actually running");
        return "Hello, authenticated user.";
    }
}
