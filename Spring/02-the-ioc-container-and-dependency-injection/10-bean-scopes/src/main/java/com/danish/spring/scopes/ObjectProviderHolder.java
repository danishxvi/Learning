package com.danish.spring.scopes;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

// FIX #1: inject an ObjectProvider<Counter> instead of a Counter directly. ObjectProvider
// is a lazy handle - constructing THIS singleton does not create a Counter at all.
// Calling counterProvider.getObject() asks the container for a Counter FRESH, at that
// exact moment, respecting Counter's real prototype scope. This is the recommended,
// general-purpose fix - it works for ANY scope, needs no proxyMode, and makes the
// "ask again later" intent visible right in the type.
@Component
public class ObjectProviderHolder {

    private final ObjectProvider<Counter> counterProvider;

    public ObjectProviderHolder(ObjectProvider<Counter> counterProvider) {
        this.counterProvider = counterProvider;
    }

    public void incrementAndPrint() {
        Counter counter = counterProvider.getObject(); // a NEW prototype instance, every call
        System.out.println("  instanceId=" + counter.getInstanceId() + ", count=" + counter.increment());
    }
}
