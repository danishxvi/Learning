package com.danish.spring.qualifiers;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class PushNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("  [PushNotifier] " + message);
    }
}
