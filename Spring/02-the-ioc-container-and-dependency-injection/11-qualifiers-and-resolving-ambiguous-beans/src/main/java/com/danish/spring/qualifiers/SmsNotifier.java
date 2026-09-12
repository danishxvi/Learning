package com.danish.spring.qualifiers;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class SmsNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("  [SmsNotifier] " + message);
    }
}
