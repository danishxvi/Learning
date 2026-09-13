package com.danish.spring.events;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class FirstRiskyListener {

    private final EventRecorder recorder;

    public FirstRiskyListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @Order(1)
    @EventListener
    public void onRiskyEvent(RiskyEvent event) {
        recorder.record("risky-first");
        throw new RuntimeException("simulated failure in the first listener");
    }
}
