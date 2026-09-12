package com.danish.spring.conditional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlatformConfig {

    // @Profile and @ConditionalOnProperty are themselves meta-annotated with @Conditional,
    // pointing at their OWN Condition implementations - this is the exact same mechanism,
    // spelled out directly instead of hidden behind a more specific annotation name.
    @Bean
    @Conditional(OnWindowsCondition.class)
    public String windowsPathTip() {
        return "Tip: on Windows, prefer forward slashes in paths passed to Maven - both work, but forward slashes avoid escaping surprises.";
    }
}
