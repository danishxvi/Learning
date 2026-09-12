package com.danish.spring.aspects.service;

import com.danish.spring.aspects.Timed;
import org.springframework.stereotype.Service;

// This class knows NOTHING about logging or timing - no @Aspect, no manual
// System.out.println anywhere in it. Every cross-cutting behaviour it ends up with
// comes entirely from LoggingAspect and TimingAspect, applied from outside.
@Service
public class OrderService {

    public String placeOrder(String product) {
        return "Order placed for " + product;
    }

    public String cancelOrder(String orderId) {
        if (orderId.equals("missing")) {
            throw new IllegalArgumentException("No such order: " + orderId);
        }
        return "Order " + orderId + " cancelled";
    }

    // The ONLY thing on this method that hints at anything cross-cutting - @Timed
    // carries no code of its own. TimingAspect decides what it means.
    @Timed
    public String slowReport() {
        try {
            Thread.sleep(80); // pretend this does real, slow work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Report generated";
    }
}
