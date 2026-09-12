package com.danish.spring.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

// A CUSTOM condition - this is what @Profile and @ConditionalOnProperty are themselves
// built out of underneath. Implementing Condition.matches() lets a bean's registration
// depend on ANYTHING inspectable at startup - here, the operating system.
public class OnWindowsCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
