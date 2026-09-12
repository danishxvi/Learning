package com.danish.spring.qualifiers;

import org.springframework.stereotype.Component;

// Asks for a plain Notifier with no further hint. With THREE Notifier beans in the
// context, this would normally be ambiguous - it resolves cleanly only because
// EmailNotifier is marked @Primary.
@Component
public class PrimaryConsumer {
    private final Notifier notifier;

    public PrimaryConsumer(Notifier notifier) {
        this.notifier = notifier;
    }

    public void notify(String message) {
        notifier.send(message);
    }

    public String resolvedType() {
        return notifier.getClass().getSimpleName();
    }
}
