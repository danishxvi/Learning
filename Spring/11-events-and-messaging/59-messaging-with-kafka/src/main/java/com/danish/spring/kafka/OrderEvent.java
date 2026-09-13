package com.danish.spring.kafka;

public record OrderEvent(String orderId, String status) {
}
