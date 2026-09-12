package com.danish.spring.injection;

public class BrokenServiceB {
    private final BrokenServiceA serviceA;

    public BrokenServiceB(BrokenServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
