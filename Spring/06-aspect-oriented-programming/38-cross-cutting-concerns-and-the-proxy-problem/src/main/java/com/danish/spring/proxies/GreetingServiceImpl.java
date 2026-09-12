package com.danish.spring.proxies;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Implements an interface - AND is annotated @Transactional, which is what makes Spring
// wrap it in a proxy AT ALL. A plain @Service with no advice applying to it (no
// @Transactional, no @Aspect matching it) is registered as-is, with NO proxy - proxies
// exist only where some cross-cutting behaviour genuinely needs to intercept calls.
@Service
@Transactional
public class GreetingServiceImpl implements GreetingService {

    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }

    // Calls greet() via `this.` - PLAIN Java, not through whatever proxy wraps this
    // bean from outside. See GreetingServiceImpl's use in the demo for what that means.
    @Override
    public String greetTwice(String name) {
        return greet(name) + " " + greet(name);
    }
}
