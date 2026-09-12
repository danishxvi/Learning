package com.danish.spring.external;

import com.danish.spring.configbeans.Car;
import com.danish.spring.configbeans.Engine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// This lives in a SIBLING package to the application class (com.danish.spring.configbeans),
// deliberately outside its scan tree (lesson 07) - it is only ever registered manually by
// ConfigurationApplication's demo code, in its own throwaway context, so it never collides
// with ThirdPartyConfig's identically-named "engine"/"car" beans in the real application.
//
// proxyBeanMethods = true is the DEFAULT for @Configuration - "full" mode. Spring generates
// a CGLIB SUBCLASS of this class at startup and registers THAT as the actual bean, not your
// class directly. Every call to car() is intercepted by the subclass, which checks "has
// engine() already been called for this context?" before ever running the real method body.
@Configuration // proxyBeanMethods defaults to true - shown explicitly in LiteConfig for contrast
public class FullProxyConfig {

    @Bean
    public Engine engine() {
        System.out.println("  [FullProxyConfig] engine() method body actually executing");
        return new Engine();
    }

    @Bean
    public Car car() {
        // A DIRECT JAVA METHOD CALL - not dependency injection, not a parameter.
        // In full mode, this call is intercepted by the CGLIB proxy and redirected to the
        // SAME singleton Engine instance already in the context, instead of running
        // engine()'s body again.
        return new Car(engine());
    }
}
