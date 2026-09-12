package com.danish.spring.qualifiers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

// @Qualifier names a SPECIFIC bean, overriding @Primary entirely. The default bean name
// for a @Component is its class name with a lowercase first letter - "smsNotifier" for
// SmsNotifier - unless @Component("customName") overrides it.
@Component
public class QualifiedConsumer {
    private final Notifier notifier;

    public QualifiedConsumer(@Qualifier("smsNotifier") Notifier notifier) {
        this.notifier = notifier;
    }

    public void notify(String message) {
        notifier.send(message);
    }

    public String resolvedType() {
        return notifier.getClass().getSimpleName();
    }
}
