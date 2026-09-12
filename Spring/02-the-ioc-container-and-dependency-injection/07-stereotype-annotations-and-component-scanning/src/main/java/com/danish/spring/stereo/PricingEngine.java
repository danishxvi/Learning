package com.danish.spring.stereo;

// Uses the custom @BusinessLogic meta-annotation from BusinessLogic.java instead of
// @Component or @Service directly - and is still discovered by component scanning.
@BusinessLogic
public class PricingEngine {

    public int priceInCents(String product) {
        return switch (product) {
            case "desk-lamp" -> 2499;
            case "keyboard" -> 4999;
            default -> 999;
        };
    }
}
