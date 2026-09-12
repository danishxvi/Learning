package com.danish.spring.configbeans;

// A plain class with no Spring annotations at all - standing in for a class you did not
// write and cannot add @Component to (a third-party library type, or a class from
// another team's JAR). The only way to make Spring manage it is a @Bean method.
public class Engine {
    private final String label = "Engine@" + Integer.toHexString(System.identityHashCode(this));

    @Override
    public String toString() {
        return label;
    }
}
