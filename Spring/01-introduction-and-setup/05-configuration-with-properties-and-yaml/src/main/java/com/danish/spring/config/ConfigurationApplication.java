package com.danish.spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

// ============================================================================
// 05 - CONFIGURATION WITH application.properties AND YAML
// ============================================================================
// Run with the default profile:
//   mvn -f Spring/01-introduction-and-setup/05-configuration-with-properties-and-yaml spring-boot:run
//
// Run with the "dev" profile active, to see application-dev.yml override values:
//   mvn -f Spring/01-introduction-and-setup/05-configuration-with-properties-and-yaml spring-boot:run -Dspring-boot.run.profiles=dev
//
// Override a single value from the command line, no file involved at all:
//   mvn -f Spring/01-introduction-and-setup/05-configuration-with-properties-and-yaml spring-boot:run -Dspring-boot.run.arguments=--app.notification.retry-count=99
// ============================================================================

// @ConfigurationPropertiesScan tells Spring Boot to find every @ConfigurationProperties
// class in this package tree and register it as a bean automatically - the
// @ConfigurationProperties equivalent of @ComponentScan for @Component classes.
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationApplication.class, args);
    }

    // @Value pulls ONE property directly, with Spring Expression Language (SpEL) syntax.
    // The ":default-value" part after the colon is used only if the key is missing -
    // "app.notification.timeout-seconds" does not exist anywhere in this lesson's YAML,
    // so this always falls back to 30.
    @Component
    static class ConfigurationPrinter implements CommandLineRunner {

        private final Environment environment;
        private final NotificationProperties notificationProperties;

        @Value("${app.notification.timeout-seconds:30}")
        private int timeoutSeconds;

        ConfigurationPrinter(Environment environment, NotificationProperties notificationProperties) {
            this.environment = environment;
            this.notificationProperties = notificationProperties;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("ACTIVE PROFILES");
            System.out.println("=".repeat(74));
            String[] profiles = environment.getActiveProfiles();
            System.out.println(profiles.length == 0
                    ? "(none) - only application.yml was loaded"
                    : String.join(", ", profiles) + " - application-" + profiles[0] + ".yml was ALSO loaded and merged");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@ConfigurationProperties - the WHOLE tree, bound to one typed record");
            System.out.println("=".repeat(74));
            System.out.println("defaultSender     = " + notificationProperties.defaultSender());
            System.out.println("retryCount        = " + notificationProperties.retryCount());
            System.out.println("allowedChannels   = " + notificationProperties.allowedChannels());
            System.out.println("rateLimit.max/min = " + notificationProperties.rateLimit().maxPerMinute());
            System.out.println("rateLimit.burst   = " + notificationProperties.rateLimit().burst());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Value - ONE property at a time, with a default");
            System.out.println("=".repeat(74));
            System.out.println("timeoutSeconds = " + timeoutSeconds + " (key is absent from every YAML file - this is the default)");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("PRECEDENCE - environment.getProperty() sees the WINNING value,");
            System.out.println("regardless of which source it came from");
            System.out.println("=".repeat(74));
            System.out.println("app.notification.retry-count resolved to: "
                    + environment.getProperty("app.notification.retry-count"));
            System.out.println("Try the command-line override in this file's header comment and");
            System.out.println("watch this same line print 99 instead - a command-line argument");
            System.out.println("always outranks any YAML or properties file.");
        }
    }

    // @Bean methods can themselves be configured by properties - this one exists only
    // to show that NotificationProperties, once bound, is an ordinary injectable bean
    // like any other, usable anywhere in the application via constructor injection.
    @Bean
    CommandLineRunner secondConsumer(NotificationProperties props) {
        return args -> System.out.println(
                "\nA second, unrelated bean also received the SAME NotificationProperties instance: "
                        + "defaultSender=" + props.defaultSender());
    }
}
