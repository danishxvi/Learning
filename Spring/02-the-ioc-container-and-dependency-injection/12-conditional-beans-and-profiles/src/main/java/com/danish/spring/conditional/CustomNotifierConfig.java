package com.danish.spring.conditional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Only registers a Notifier when "dev" is active - standing in for "a real, more
// specific implementation a team configured for their own environment."
@Configuration
@Profile("dev")
public class CustomNotifierConfig {
    @Bean
    public Notifier notifier() {
        return () -> "CustomNotifier (registered explicitly because the \"dev\" profile is active)";
    }
}
