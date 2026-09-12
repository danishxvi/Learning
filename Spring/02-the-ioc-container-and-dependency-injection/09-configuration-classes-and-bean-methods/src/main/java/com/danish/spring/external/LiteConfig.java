package com.danish.spring.external;

import com.danish.spring.configbeans.Car;
import com.danish.spring.configbeans.Engine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// IDENTICAL to FullProxyConfig except for one attribute: proxyBeanMethods = false. This is
// "lite" mode - no CGLIB subclass is generated. car() calling engine() is now an ORDINARY
// Java method call with no interception at all, which means it runs engine()'s body again,
// producing a SECOND Engine instance that the container never registered as a bean.
@Configuration(proxyBeanMethods = false)
public class LiteConfig {

    @Bean
    public Engine engine() {
        System.out.println("  [LiteConfig] engine() method body actually executing");
        return new Engine();
    }

    @Bean
    public Car car() {
        // No proxy is intercepting this call - it is exactly as if `engine()` were a
        // regular private method. It runs again, in full, producing a DIFFERENT Engine.
        return new Car(engine());
    }
}
