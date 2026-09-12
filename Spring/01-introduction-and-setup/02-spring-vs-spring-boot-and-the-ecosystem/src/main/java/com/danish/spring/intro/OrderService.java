package com.danish.spring.intro;

import org.springframework.stereotype.Component;

// This class is IDENTICAL in spirit to lesson 01's manually-written OrderService: it
// declares a Notifier constructor parameter and has no idea which implementation it
// will receive. The only new thing is @Component, which tells Spring "manage this
// class for me" instead of us calling `new OrderService(...)` ourselves.
@Component
public class OrderService {

    private final Notifier notifier;

    // With exactly one constructor, Spring injects its parameters automatically -
    // no @Autowired needed on the constructor itself (that annotation becomes
    // required, and gets its own explanation, once a class has more than one
    // constructor - see lesson 08).
    public OrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    public void placeOrder(String product) {
        System.out.println("  Order placed for: " + product);
        notifier.send("Your order for " + product + " has shipped.");
    }
}
