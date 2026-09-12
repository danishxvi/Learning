package com.danish.spring.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// A class with MORE THAN ONE constructor. With exactly one constructor (every other
// class in this lesson), Spring uses it automatically - no annotation needed. The moment
// there is more than one, Spring cannot guess which one you want called, and throws
// at startup unless exactly one constructor is marked @Autowired.
@Component
public class GreetingService {

    private final Notifier notifier;
    private final String greetingPrefix;

    // This is the constructor Spring will call - the ONLY one allowed to carry @Autowired.
    @Autowired
    public GreetingService(Notifier notifier) {
        this(notifier, "Hello");
    }

    // A second constructor for tests or manual construction with a custom prefix -
    // never called by Spring, only by code that calls it directly with `new`.
    public GreetingService(Notifier notifier, String greetingPrefix) {
        this.notifier = notifier;
        this.greetingPrefix = greetingPrefix;
    }

    public void greet(String name) {
        notifier.send(greetingPrefix + ", " + name + "!");
    }
}
