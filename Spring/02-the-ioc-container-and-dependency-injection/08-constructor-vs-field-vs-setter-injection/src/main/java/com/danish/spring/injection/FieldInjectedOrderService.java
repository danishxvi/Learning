package com.danish.spring.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// FIELD INJECTION - looks convenient, and is generally discouraged. Two real problems,
// both demonstrated by this lesson's demo code:
//   1. The field CANNOT be final, so nothing stops later code from reassigning it.
//   2. Plain `new FieldInjectedOrderService()` leaves `notifier` NULL - there is no
//      constructor parameter to pass a fake into, so testing this class without
//      dragging in a Spring context (or reflection) is not possible.
@Component
public class FieldInjectedOrderService {

    @Autowired
    private Notifier notifier; // not final - Spring sets this via reflection AFTER construction

    public void placeOrder(String product) {
        if (notifier == null) {
            System.out.println("  notifier is NULL - this object was built with plain `new`, not by Spring");
            return;
        }
        notifier.send("Order placed for " + product);
    }
}
