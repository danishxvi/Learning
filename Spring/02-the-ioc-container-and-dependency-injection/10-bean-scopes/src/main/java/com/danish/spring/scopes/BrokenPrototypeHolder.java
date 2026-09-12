package com.danish.spring.scopes;

import org.springframework.stereotype.Component;

// A singleton (the default, no @Scope here) that constructor-injects a PROTOTYPE bean.
// This is the single most common bean-scope mistake. Spring builds this singleton exactly
// ONCE, which means Counter's constructor is called exactly ONCE too - whichever Counter
// instance happens to exist at THAT moment gets frozen into this field forever. Every
// later call to incrementAndPrint() reuses that same frozen instance - "prototype" scope
// never actually manifests from this consumer's point of view.
@Component
public class BrokenPrototypeHolder {

    private final Counter counter; // looks fresh every time - is NOT

    public BrokenPrototypeHolder(Counter counter) {
        this.counter = counter;
    }

    public void incrementAndPrint() {
        System.out.println("  instanceId=" + counter.getInstanceId() + ", count=" + counter.increment());
    }
}
