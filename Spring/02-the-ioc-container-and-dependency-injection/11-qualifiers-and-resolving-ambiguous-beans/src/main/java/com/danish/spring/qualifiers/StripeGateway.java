package com.danish.spring.qualifiers;

// NOT @Component - registered manually into a throwaway context only, alongside
// PaypalGateway, purely to reproduce a genuine NoUniqueBeanDefinitionException with
// neither @Primary nor @Qualifier present to resolve it.
public class StripeGateway implements PaymentGateway {
    @Override
    public void charge(int amountCents) {
        System.out.println("  [StripeGateway] charged " + amountCents);
    }
}
