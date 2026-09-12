package com.danish.spring.proxies;

// An INTERFACE - the historical reason JDK dynamic proxies exist: they can only ever
// implement an interface, never subclass a concrete class.
public interface GreetingService {
    String greet(String name);
    String greetTwice(String name);
}
