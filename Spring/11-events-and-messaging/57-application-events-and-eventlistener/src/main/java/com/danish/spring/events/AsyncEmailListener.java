package com.danish.spring.events;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncEmailListener {

    private final EventRecorder recorder;

    public AsyncEmailListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    // @Async on an @EventListener (needs @EnableAsync, same proxy mechanism as lesson
    // 53) hands this method to the executor immediately and lets publishEvent() move on
    // to the next listener without waiting - unlike SlowSynchronousListener, this one's
    // 500ms sleep does NOT delay OrderService.placeOrder() at all.
    @Async
    @EventListener
    public void onOrderPlaced(OrderPlacedEvent event) throws InterruptedException {
        Thread.sleep(500);
        recorder.record("async-email");
    }
}
