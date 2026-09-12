package com.danish.spring.qualifiers;

// Also NOT @Component. Asks for a single PaymentGateway with no @Qualifier - registered
// only into the throwaway context alongside StripeGateway and PaypalGateway, where
// resolving this constructor parameter is genuinely ambiguous.
public class CheckoutNeedingUnqualifiedGateway {
    public CheckoutNeedingUnqualifiedGateway(PaymentGateway gateway) {
    }
}
