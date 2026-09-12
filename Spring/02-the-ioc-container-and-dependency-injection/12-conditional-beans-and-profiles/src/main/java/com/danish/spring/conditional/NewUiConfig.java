package com.danish.spring.conditional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @ConditionalOnProperty: register this bean only if a specific property has a specific
// value. "feature.new-ui" defaults to false in application.yml - flip it from the command
// line (see the .md) and this bean starts existing, with no code change and no redeploy
// of anything except configuration.
@Configuration
public class NewUiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "feature", name = "new-ui", havingValue = "true")
    public NewUiFeature newUiFeature() {
        return new NewUiFeature();
    }

    public static class NewUiFeature {
        public String describe() {
            return "New UI feature flag is ON - this bean only exists because of that.";
        }
    }
}
