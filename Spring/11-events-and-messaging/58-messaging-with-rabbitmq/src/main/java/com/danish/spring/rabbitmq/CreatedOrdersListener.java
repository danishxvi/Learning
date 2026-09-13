package com.danish.spring.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CreatedOrdersListener {

    private final EventRecorder recorder;

    public CreatedOrdersListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @RabbitListener(queues = RabbitConfig.CREATED_QUEUE)
    public void onMessage(OrderEvent event) {
        recorder.record("created-queue received " + event);
    }
}
