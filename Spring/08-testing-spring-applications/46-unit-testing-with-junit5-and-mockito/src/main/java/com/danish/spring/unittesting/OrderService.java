package com.danish.spring.unittesting;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final int MINIMUM_ORDER_CENTS = 500;

    private final OrderRepository orderRepository;
    private final PricingService pricingService;

    public OrderService(OrderRepository orderRepository, PricingService pricingService) {
        this.orderRepository = orderRepository;
        this.pricingService = pricingService;
    }

    public Order placeOrder(String product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive, was " + quantity);
        }
        int unitPrice = pricingService.unitPriceCents(product);
        int total = unitPrice * quantity;
        if (total < MINIMUM_ORDER_CENTS) {
            throw new MinimumOrderException("Order total " + total + " is below the minimum of " + MINIMUM_ORDER_CENTS);
        }
        return orderRepository.save(new Order(product, quantity, total));
    }
}
