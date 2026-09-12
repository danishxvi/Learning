package com.danish.spring.scopes;

import org.springframework.stereotype.Component;

// FIX #2: inject ScopedProxyCounter directly, looking EXACTLY like BrokenPrototypeHolder's
// mistake - the difference is entirely in ScopedProxyCounter's own @Scope(proxyMode=...).
// Because that bean declared a scoped proxy, Spring injects a lightweight CGLIB PROXY
// here instead of a real instance. The proxy has no state of its own - every method call
// on it looks up the "real" prototype instance for that call, fresh, then delegates.
// From this class's point of view the code is indistinguishable from injecting the real
// thing; the fix lives entirely on the bean definition, not the consumer.
@Component
public class ScopedProxyHolder {

    private final ScopedProxyCounter counter; // actually a CGLIB proxy, not a real instance

    public ScopedProxyHolder(ScopedProxyCounter counter) {
        this.counter = counter;
    }

    public void incrementAndPrint() {
        System.out.println("  instanceId=" + counter.getInstanceId() + ", count=" + counter.increment());
    }
}
