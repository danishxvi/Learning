package com.danish.spring.actuatordeepdive;

import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class OrderController {

    private final OrderMetrics orderMetrics;
    private final DownstreamHealthIndicator downstreamHealthIndicator;

    public OrderController(OrderMetrics orderMetrics, DownstreamHealthIndicator downstreamHealthIndicator) {
        this.orderMetrics = orderMetrics;
        this.downstreamHealthIndicator = downstreamHealthIndicator;
    }

    @PostMapping("/orders")
    public String placeOrder() throws InterruptedException {
        orderMetrics.incrementPending();
        Timer.Sample sample = orderMetrics.startTimer();
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
            orderMetrics.recordOrderPlaced();
            return "order placed";
        } finally {
            orderMetrics.stopTimer(sample);
            orderMetrics.decrementPending();
        }
    }

    @PostMapping("/toggle-downstream-health/{healthy}")
    public String toggleDownstreamHealth(@PathVariable boolean healthy) {
        downstreamHealthIndicator.setHealthy(healthy);
        return "downstream health set to " + healthy;
    }
}
