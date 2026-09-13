package com.danish.spring.events;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
public class SecondRiskyListener {

    private final EventRecorder recorder;

    public SecondRiskyListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    // Registered to run AFTER FirstRiskyListener (@Order(1) < @Order(2)) - the .md
    // documents whether this ever actually runs when the first listener throws.
    @Order(2)
    @EventListener
    public void onRiskyEvent(RiskyEvent event) {
        recorder.record("risky-second");
    }
}
