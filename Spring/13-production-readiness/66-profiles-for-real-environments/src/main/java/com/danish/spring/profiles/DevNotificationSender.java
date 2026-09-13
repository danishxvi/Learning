package com.danish.spring.profiles;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// @Profile controls whether this bean EXISTS in the container at all - not just
// whether some property has one value or another. With no "dev" profile active, this
// class is never even instantiated.
@Profile("dev")
@Component
public class DevNotificationSender implements NotificationSender {
    @Override
    public String send(String message) {
        return "[DEV] printed to console instead of really sending: " + message;
    }
}
