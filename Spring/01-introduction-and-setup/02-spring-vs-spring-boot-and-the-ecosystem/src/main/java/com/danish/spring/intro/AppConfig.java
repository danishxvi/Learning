package com.danish.spring.intro;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

// @Configuration marks this as a source of bean definitions - Spring's replacement for
// the block of `container.register(...)` calls in lesson 01's MiniContainer.
// @ComponentScan tells Spring which package to search for @Component-annotated classes;
// it does not need to be told about SmtpEmailNotifier or OrderService individually.
@Configuration
@ComponentScan(basePackages = "com.danish.spring.intro")
public class AppConfig {
}
