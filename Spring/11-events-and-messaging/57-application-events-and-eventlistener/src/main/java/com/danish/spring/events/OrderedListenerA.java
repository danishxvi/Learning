package com.danish.spring.events;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderedListenerA {

    private final EventRecorder recorder;

    public OrderedListenerA(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @Order(1)
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) {
        recorder.record("A");
    }
}
