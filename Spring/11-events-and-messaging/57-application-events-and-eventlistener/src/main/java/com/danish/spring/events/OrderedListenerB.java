package com.danish.spring.events;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderedListenerB {

    private final EventRecorder recorder;

    public OrderedListenerB(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @Order(2)
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        recorder.record("B");
    }
}
