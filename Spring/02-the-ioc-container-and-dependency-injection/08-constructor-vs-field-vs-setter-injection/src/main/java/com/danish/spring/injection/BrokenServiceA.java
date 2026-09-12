package com.danish.spring.injection;

// NOT annotated @Component - this class and BrokenServiceB are never picked up by
// component scanning. They are only ever registered manually, in a throwaway
// AnnotationConfigApplicationContext built purely to demonstrate the exception a
// genuine constructor-injection cycle produces (see CircularDependencyDemo).
public class BrokenServiceA {
    private final BrokenServiceB serviceB;

    public BrokenServiceA(BrokenServiceB serviceB) {
        this.serviceB = serviceB;
    }
}
