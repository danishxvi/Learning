package com.danish.spring.environment;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

// ============================================================================
// 14 - THE Environment ABSTRACTION
// ============================================================================
// Default: mvn -f Spring/02-the-ioc-container-and-dependency-injection/14-the-environment-abstraction spring-boot:run
// With "dev" active: add -Dspring-boot.run.profiles=dev
// ============================================================================
@SpringBootApplication
public class EnvironmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnvironmentApplication.class, args);
    }

    @Component
    static class EnvironmentDemo implements CommandLineRunner {

        private final ConfigurableEnvironment environment;

        // Every lesson since 04 injected Environment - the READ-ONLY interface. Its
        // actual runtime type is ConfigurableEnvironment, which ADDS the ability to
        // mutate property sources - injecting that more specific type directly is
        // exactly what section 4's demo below needs.
        EnvironmentDemo(ConfigurableEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("getProperty() - THREE VARIANTS");
            System.out.println("=".repeat(74));
            System.out.println("  getProperty(\"app.max-retries\")                 = "
                    + environment.getProperty("app.max-retries"));
            System.out.println("  getProperty(\"app.max-retries\", Integer.class)  = "
                    + environment.getProperty("app.max-retries", Integer.class) + "  (converted to int)");
            System.out.println("  getProperty(\"app.missing-key\", \"fallback\")      = "
                    + environment.getProperty("app.missing-key", "fallback"));
            try {
                environment.getRequiredProperty("app.missing-key");
            } catch (IllegalStateException ex) {
                System.out.println("  getRequiredProperty(\"app.missing-key\") threw: " + ex.getClass().getSimpleName());
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("containsProperty()");
            System.out.println("=".repeat(74));
            System.out.println("  containsProperty(\"app.max-retries\") = " + environment.containsProperty("app.max-retries"));
            System.out.println("  containsProperty(\"app.missing-key\") = " + environment.containsProperty("app.missing-key"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("acceptsProfiles() - checking profile activation IN CODE");
            System.out.println("=".repeat(74));
            System.out.println("  Active profiles: " + Arrays.toString(environment.getActiveProfiles()));
            System.out.println("  acceptsProfiles(\"dev\" | \"test\") = "
                    + environment.acceptsProfiles(Profiles.of("dev | test")));
            System.out.println("  acceptsProfiles(\"!prod\")         = "
                    + environment.acceptsProfiles(Profiles.of("!prod")));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("PROPERTY SOURCES - the layers getProperty() actually searches, IN ORDER");
            System.out.println("=".repeat(74));
            MutablePropertySources sources = environment.getPropertySources();
            for (PropertySource<?> source : sources) {
                System.out.println("  - " + source.getName());
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("ADDING A PROPERTY SOURCE PROGRAMMATICALLY, AT THE HIGHEST PRECEDENCE");
            System.out.println("=".repeat(74));
            System.out.println("  Before: app.max-retries = " + environment.getProperty("app.max-retries"));
            sources.addFirst(new MapPropertySource("runtime-override", Map.of("app.max-retries", "99")));
            System.out.println("  After adding a MapPropertySource in front of everything else:");
            System.out.println("  app.max-retries = " + environment.getProperty("app.max-retries")
                    + "  (overridden - addFirst() outranks application.yml)");
        }
    }
}
