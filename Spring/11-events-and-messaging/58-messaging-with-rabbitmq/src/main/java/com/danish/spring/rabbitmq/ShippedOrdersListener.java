package com.danish.spring.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ShippedOrdersListener {

    private final EventRecorder recorder;

    public ShippedOrdersListener(EventRecorder recorder) {
        this.recorder = recorder;
    }

    @RabbitListener(queues = RabbitConfig.SHIPPED_QUEUE)
    public void onMessage(OrderEvent event) {
        recorder.record("shipped-queue received " + event);
    }
}
