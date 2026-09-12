package com.danish.spring.qualifiers;

public class PaypalGateway implements PaymentGateway {
    @Override
    public void charge(int amountCents) {
        System.out.println("  [PaypalGateway] charged " + amountCents);
    }
}
