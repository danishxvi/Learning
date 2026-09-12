package com.danish.spring.mapping;

import jakarta.persistence.Embeddable;

// @Embeddable - NOT its own table and NOT its own @Id. Its fields are folded directly
// into whichever entity @Embeds it, as plain columns on THAT entity's table. Use this
// for a value that logically belongs together (an amount and its currency) but has no
// independent identity of its own - it can never be looked up by its own id, because it
// doesn't have one.
@Embeddable
public class Price {
    private int amountCents;
    private String currency;

    protected Price() { }

    public Price(int amountCents, String currency) {
        this.amountCents = amountCents;
        this.currency = currency;
    }

    public int getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }

    @Override
    public String toString() {
        return amountCents + " " + currency;
    }
}
