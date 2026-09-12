package com.danish.spring.qualifiers;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

// Injecting a List<Notifier> or Map<String, Notifier> sidesteps ambiguity entirely by
// asking for EVERY matching bean at once, instead of picking one.
@Component
public class BroadcastNotifier {

    private final List<Notifier> allNotifiers;      // ordered by @Order, low value first
    private final Map<String, Notifier> byBeanName;  // keyed by bean name, NOT ordered

    public BroadcastNotifier(List<Notifier> allNotifiers, Map<String, Notifier> byBeanName) {
        this.allNotifiers = allNotifiers;
        this.byBeanName = byBeanName;
    }

    public void broadcastToAll(String message) {
        allNotifiers.forEach(notifier -> notifier.send(message));
    }

    public List<String> orderedTypeNames() {
        return allNotifiers.stream().map(n -> n.getClass().getSimpleName()).toList();
    }

    public Map<String, String> beanNameToType() {
        Map<String, String> result = new java.util.TreeMap<>();
        byBeanName.forEach((name, notifier) -> result.put(name, notifier.getClass().getSimpleName()));
        return result;
    }
}
