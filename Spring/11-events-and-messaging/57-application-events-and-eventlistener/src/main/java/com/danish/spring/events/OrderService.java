package com.danish.spring.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRecordRepository repository;
    private final ApplicationEventPublisher publisher;

    public OrderService(OrderRecordRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    // publishEvent() dispatches to every @EventListener SYNCHRONOUSLY, right here,
    // mid-transaction - long before this method returns and the surrounding
    // @Transactional proxy commits. A @TransactionalEventListener registered for
    // AFTER_COMMIT is different: it's deferred until the transaction actually commits,
    // which for this REQUIRED-propagation transaction happens when this method returns
    // normally to its caller - or never, if this method throws and the transaction
    // rolls back instead.
    @Transactional
    public void placeOrder(String orderId, BigDecimal amount, boolean failAfterPublish) {
        repository.save(new OrderRecord(orderId, amount));
        publisher.publishEvent(new OrderPlacedEvent(orderId, amount));
        if (failAfterPublish) {
            throw new RuntimeException("simulated failure after publishing event - transaction should roll back");
        }
    }
}
