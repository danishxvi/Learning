package com.danish.spring.scopes;

import org.springframework.stereotype.Component;

// No @Scope at all - singleton is the DEFAULT. Exactly one instance exists for the whole
// container, and every injection point shares it, which is why state accumulates here
// across unrelated calls - the correct, expected behaviour for shared, stateless-by-design
// services (which is what the vast majority of @Component/@Service beans are).
@Component
public class SingletonCounter {
    private int count = 0;

    public int increment() {
        return ++count;
    }
}
