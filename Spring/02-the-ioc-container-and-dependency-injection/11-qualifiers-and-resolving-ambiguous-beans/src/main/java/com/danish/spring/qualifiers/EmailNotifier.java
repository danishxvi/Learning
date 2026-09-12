package com.danish.spring.qualifiers;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// @Primary: when multiple beans implement Notifier and an injection point asks for just
// ONE Notifier with no further hint, Spring picks the @Primary one instead of failing.
// There can be at most one @Primary candidate per type - two would just move the
// ambiguity error from "no primary" to "two primaries," which is still an error.
@Primary
@Order(2)
@Component
public class EmailNotifier implements Notifier {
    @Override
    public void send(String message) {
        System.out.println("  [EmailNotifier] " + message);
    }
}
