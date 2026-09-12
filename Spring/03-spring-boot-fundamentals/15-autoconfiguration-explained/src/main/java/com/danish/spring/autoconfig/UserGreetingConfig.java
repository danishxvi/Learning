package com.danish.spring.autoconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Stands in for "application code that supplies its own bean" - gated behind a profile
// purely so this lesson can show BOTH outcomes by running the app twice. In a real
// project this would just be an ordinary @Configuration, no @Profile needed - the
// override happens simply by this bean existing at all.
@Configuration
@Profile("custom")
public class UserGreetingConfig {
    @Bean
    public GreetingService greetingService() {
        return () -> "Hello from the APPLICATION'S OWN bean - GreetingAutoConfiguration backed off.";
    }
}
