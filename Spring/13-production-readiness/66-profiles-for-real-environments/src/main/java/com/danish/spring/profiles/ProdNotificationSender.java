package com.danish.spring.profiles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
public class ProdNotificationSender implements NotificationSender {
    @Override
    public String send(String message) {
        return "[PROD] actually dispatched via the real paid gateway: " + message;
    }
}
