package com.danish.spring.scopes;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

// Same idea as Counter, but with proxyMode = TARGET_CLASS added. This is what makes a
// prototype bean SAFE to inject directly into a singleton's constructor - see
// ScopedProxyHolder for what that buys you.
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ScopedProxyCounter {

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
