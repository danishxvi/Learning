package com.danish.spring.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

// @Scope("prototype") - a NEW instance every time the container is asked for this bean,
// instead of the singleton default. Each instance gets its own identity and its own count.
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Counter {

    private static final AtomicInteger instancesCreated = new AtomicInteger(0);

    private final int instanceId = instancesCreated.incrementAndGet();
    private int count = 0;

    public int increment() {
        return ++count;
    }

    public int getInstanceId() {
        return instanceId;
    }
}
