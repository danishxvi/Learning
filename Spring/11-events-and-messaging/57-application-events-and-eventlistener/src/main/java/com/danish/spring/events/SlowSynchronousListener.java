package com.danish.spring.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SlowSynchronousListener {

    private final EventRecorder recorder;

    public SlowSynchronousListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    // Plain @EventListener methods run SYNCHRONOUSLY, on the publisher's own thread, as
    // part of the publishEvent() call - a slow listener here makes publishEvent() itself
    // slow, blocking whatever called it (OrderService.placeOrder, in this lesson).
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) throws InterruptedException {
        Thread.sleep(300);
        recorder.record("slow-sync");
    }
}
