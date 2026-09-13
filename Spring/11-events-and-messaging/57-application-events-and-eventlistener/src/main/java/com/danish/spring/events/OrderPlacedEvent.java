package com.danish.spring.events;

import java.math.BigDecimal;

// A plain POJO (record), not extending ApplicationEvent - Spring has allowed arbitrary
// objects as events since 4.2, and there's no reason to inherit the older base class
// for a new event type today.
public record OrderPlacedEvent(String orderId, BigDecimal amount) {
}
