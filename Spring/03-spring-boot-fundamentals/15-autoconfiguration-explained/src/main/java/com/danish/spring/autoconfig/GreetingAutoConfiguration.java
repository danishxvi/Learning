package com.danish.spring.autoconfig;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

// THIS is what a "starter" actually looks like on the inside - the exact same shape as
// every one of lesson 04's 52 mystery beans. @AutoConfiguration is a specialised
// @Configuration, meant specifically to be discovered by Boot's auto-configuration
// import mechanism rather than found by @ComponentScan.
//
// This class is NEVER found by component scanning - it isn't annotated @Component, and
// even if it were, scanning alone would register it unconditionally, defeating the whole
// point. It is found ONLY because its fully-qualified name is listed in
// META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports,
// which is the file @EnableAutoConfiguration (part of @SpringBootApplication, lesson 03)
// actually reads at startup.
@AutoConfiguration
@ConditionalOnClass(GreetingService.class) // only makes sense if this API is even on the classpath
public class GreetingAutoConfiguration {

    // @ConditionalOnMissingBean - the EXACT mechanism from lesson 12. If the application
    // (or another auto-configuration) already registered a GreetingService, this method
    // is skipped entirely - never even evaluated far enough to run its body.
    @Bean
    @ConditionalOnMissingBean(GreetingService.class)
    public GreetingService greetingService() {
        return () -> "Hello from GreetingAutoConfiguration's DEFAULT bean - nobody overrode it.";
    }
}
