package com.danish.spring.stereo;

import org.springframework.stereotype.Component;

// This IS annotated @Component, so it WOULD normally be picked up by component scanning.
// It also carries @Legacy - which StereotypeApplication's explicit @ComponentScan
// excludeFilters uses to keep it OUT of the context, even though it lives in the same
// package as everything else that gets scanned in.
@Component
@Legacy
public class OldPaymentGateway {

    public void charge(int amountCents) {
        System.out.println("  [OldPaymentGateway] this should never run - it was excluded from scanning");
    }
}
