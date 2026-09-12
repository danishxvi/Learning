package com.danish.spring.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @ConditionalOnMissingBean(Notifier.class) - the exact pattern Spring Boot's OWN
// auto-configuration uses everywhere (lesson 04's 52 auto-registered beans lean on this
// heavily): "register this bean ONLY if nobody else already provided one of this type."
// This lets application code override a sensible default just by defining its own bean -
// no flag, no exclusion list, just supplying a competing bean is enough.
@Configuration
public class DefaultNotifierConfig {
    @Bean
    @ConditionalOnMissingBean(Notifier.class)
    public Notifier notifier() {
        return () -> "DefaultNotifier (registered because no other Notifier bean was found)";
    }
}
