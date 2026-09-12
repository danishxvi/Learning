package com.danish.spring.configbeans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// A @Configuration class exists specifically for cases @Component can't cover: registering
// a bean out of a class you did not write (and cannot add @Component to), or one that
// needs constructor arguments computed by code rather than injected. This is the same job
// lesson 02's AppConfig did with @ComponentScan - here it's individual @Bean methods.
@Configuration
public class ThirdPartyConfig {

    // The method NAME becomes the bean name by default ("engine", not "engineBean" or
    // anything else) unless @Bean(name = "...") overrides it.
    @Bean
    public Engine engine() {
        return new Engine();
    }

    @Bean
    public Car car(Engine engine) {          // Spring injects the "engine" bean as a plain method parameter
        return new Car(engine);
    }

    // initMethod/destroyMethod bridge a third-party class's own lifecycle method names onto
    // Spring's lifecycle (lesson 06) - connect() runs like @PostConstruct would, shutdown()
    // like @PreDestroy, without editing ThirdPartyConnectionPool at all.
    @Bean(initMethod = "connect", destroyMethod = "shutdown")
    public ThirdPartyConnectionPool connectionPool() {
        return new ThirdPartyConnectionPool();
    }
}
