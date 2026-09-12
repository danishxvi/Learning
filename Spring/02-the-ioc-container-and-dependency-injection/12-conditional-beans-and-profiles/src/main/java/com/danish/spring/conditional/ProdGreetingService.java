package com.danish.spring.conditional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
public class ProdGreetingService implements GreetingService {
    @Override
    public String greet() {
        return "Welcome.";
    }
}
