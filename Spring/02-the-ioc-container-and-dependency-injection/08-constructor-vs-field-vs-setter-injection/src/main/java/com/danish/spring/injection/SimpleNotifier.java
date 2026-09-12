package com.danish.spring.injection;

import org.springframework.stereotype.Component;

@Component
public class SimpleNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("  [SimpleNotifier] " + message);
    }
}
