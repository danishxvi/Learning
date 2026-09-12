package com.danish.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// @ConfigurationProperties binds an entire tree of configuration keys onto one type-safe
// object, instead of one @Value per key. "app.notification" is the prefix - everything
// under it in application.yml becomes a field here.
//
// This is a Java record. Since Spring Boot 3, records are bound via CONSTRUCTOR binding
// automatically - Spring calls this canonical constructor with values pulled from
// configuration, matched by parameter name. No setters, no @ConstructorBinding needed.
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(
        String defaultSender,      // matches "default-sender" in YAML - see the .md for why
        int retryCount,            // matches "retry-count"
        List<String> allowedChannels,
        RateLimit rateLimit
) {
    // A nested record for the nested YAML block "app.notification.rate-limit".
    // Spring binds nested objects the same way, recursively.
    public record RateLimit(int maxPerMinute, int burst) {
    }
}
