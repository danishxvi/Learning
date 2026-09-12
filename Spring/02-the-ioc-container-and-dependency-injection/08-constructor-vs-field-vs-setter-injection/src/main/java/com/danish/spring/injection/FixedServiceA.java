package com.danish.spring.injection;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

// The SAME cycle as BrokenServiceA/BrokenServiceB - FixedServiceA needs FixedServiceB and
// FixedServiceB needs FixedServiceA - but this pair starts up fine, because of @Lazy below.
@Component
public class FixedServiceA {

    private final FixedServiceB serviceB;

    // @Lazy on a constructor parameter tells Spring: "inject a PROXY here instead of the
    // real bean." The proxy lets FixedServiceA finish constructing immediately, without
    // waiting for FixedServiceB to exist yet. The real FixedServiceB is only resolved the
    // first time a method is actually called on the proxy - by which point FixedServiceB's
    // OWN constructor (which needs FixedServiceA) can complete, because FixedServiceA
    // already finished constructing. That is the whole trick: defer the lookup, not the
    // dependency.
    public FixedServiceA(@Lazy FixedServiceB serviceB) {
        this.serviceB = serviceB;
        System.out.println("  FixedServiceA constructed (serviceB is a lazy proxy so far, not the real bean yet)");
    }

    public void ping() {
        System.out.println("  FixedServiceA.ping() calling into serviceB...");
        serviceB.pong();
    }
}
