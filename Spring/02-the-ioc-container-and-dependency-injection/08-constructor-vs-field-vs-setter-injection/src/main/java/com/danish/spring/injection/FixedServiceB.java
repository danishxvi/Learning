package com.danish.spring.injection;

import org.springframework.stereotype.Component;

@Component
public class FixedServiceB {

    private final FixedServiceA serviceA; // ordinary, eager constructor injection - only ONE side needs @Lazy

    public FixedServiceB(FixedServiceA serviceA) {
        this.serviceA = serviceA;
        System.out.println("  FixedServiceB constructed (holds the REAL, already-built FixedServiceA)");
    }

    public void pong() {
        System.out.println("  FixedServiceB.pong() - reached the other side of the cycle successfully");
    }
}
