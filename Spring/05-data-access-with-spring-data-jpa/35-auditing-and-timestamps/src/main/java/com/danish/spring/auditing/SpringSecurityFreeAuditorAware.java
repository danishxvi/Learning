package com.danish.spring.auditing;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

// AuditorAware<T> is the ONE piece of auditing that MUST be supplied by hand - Spring
// Data has no way to know "who" without being told. In a real application with Spring
// Security (section 07), this would read SecurityContextHolder's authenticated principal
// instead of a thread-local set by hand.
public class SpringSecurityFreeAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(CurrentUserHolder.get());
    }
}
