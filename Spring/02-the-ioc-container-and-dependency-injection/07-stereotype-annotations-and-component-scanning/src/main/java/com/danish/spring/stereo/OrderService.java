package com.danish.spring.stereo;

import org.springframework.stereotype.Service;

// @Service is ALSO functionally identical to @Component for scanning purposes - there is
// no PersistenceExceptionTranslationPostProcessor, no extra proxying, nothing technical
// that @Component doesn't already do. @Service exists purely to communicate INTENT: "this
// class holds business logic," the way @Repository communicates "this class talks to
// storage." A code reviewer, or a tool generating an architecture diagram, can tell the
// class's role from its annotation alone - that is the entire value of the distinction.
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void printRecentOrders() {
        orderRepository.findRecentOrderIds().forEach(id -> System.out.println("  - " + id));
    }
}
