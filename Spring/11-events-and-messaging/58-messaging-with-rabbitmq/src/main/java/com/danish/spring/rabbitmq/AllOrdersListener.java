package com.danish.spring.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AllOrdersListener {

    private final EventRecorder recorder;

    public AllOrdersListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @RabbitListener(queues = RabbitConfig.ALL_QUEUE)
    public void onMessage(OrderEvent event) {
        recorder.record("all-queue received " + event);
    }
}
