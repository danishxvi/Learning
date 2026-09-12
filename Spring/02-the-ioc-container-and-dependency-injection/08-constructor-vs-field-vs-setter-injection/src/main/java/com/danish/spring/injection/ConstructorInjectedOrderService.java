package com.danish.spring.injection;

import org.springframework.stereotype.Component;

// CONSTRUCTOR INJECTION - the recommended default for required dependencies.
//   - `notifier` can be `final` - it is provably set exactly once and never reassigned.
//   - Plain Java `new ConstructorInjectedOrderService(fakeNotifier)` works with NO
//     Spring involved at all - this is the entire class of tests lesson 46 relies on.
//   - Forgetting to provide a Notifier is a COMPILE ERROR, not a runtime NullPointerException
//     discovered later - the constructor simply will not compile without the argument.
@Component
public class ConstructorInjectedOrderService {

    private final Notifier notifier;

    public ConstructorInjectedOrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    public void placeOrder(String product) {
        notifier.send("Order placed for " + product);
    }
}
