package com.danish.spring.actuatordeepdive;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderMetrics {

    private final Counter ordersPlaced;
    private final Timer orderPlacementTimer;
    private final AtomicInteger pendingOrders = new AtomicInteger();

    public OrderMetrics(MeterRegistry registry) {
        // A Counter only ever goes up - total orders EVER placed since this JVM started.
        this.ordersPlaced = Counter.builder("orders.placed")
                .description("Total number of orders placed")
                .register(registry);

        // A Timer records both a COUNT (how many times) and a total/max DURATION in one
        // meter - Micrometer computes both from every recorded sample.
        this.orderPlacementTimer = Timer.builder("orders.placement.duration")
                .description("Time taken to place an order")
                .register(registry);

        // A Gauge reads a live value at scrape time - it does NOT accumulate like a
        // Counter. Registering it against the AtomicInteger itself (not a snapshot)
        // means every scrape reads whatever the CURRENT value is, right now.
        Gauge.builder("orders.pending", pendingOrders, AtomicInteger::get)
                .description("Orders currently being processed")
                .register(registry);
    }

    public void recordOrderPlaced() {
        ordersPlaced.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(orderPlacementTimer);
    }

    public void incrementPending() {
        pendingOrders.incrementAndGet();
    }

    public void decrementPending() {
        pendingOrders.decrementAndGet();
    }
}
