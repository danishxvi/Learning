package com.danish.spring.conditional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// ============================================================================
// 12 - CONDITIONAL BEANS AND PROFILES
// ============================================================================
// Default (no profile active):
//   mvn -f Spring/02-the-ioc-container-and-dependency-injection/12-conditional-beans-and-profiles spring-boot:run
// With "dev" active:
//   mvn -f Spring/02-the-ioc-container-and-dependency-injection/12-conditional-beans-and-profiles spring-boot:run -Dspring-boot.run.profiles=dev
// With "prod" active:
//   mvn -f Spring/02-the-ioc-container-and-dependency-injection/12-conditional-beans-and-profiles spring-boot:run -Dspring-boot.run.profiles=prod
// With the feature flag on:
//   mvn -f Spring/02-the-ioc-container-and-dependency-injection/12-conditional-beans-and-profiles spring-boot:run -Dspring-boot.run.arguments=--feature.new-ui=true
// ============================================================================
@SpringBootApplication
public class ConditionalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConditionalApplication.class, args);
    }

    @Component
    static class ConditionalDemo implements CommandLineRunner {

        private final Environment environment;
        private final ObjectProvider<GreetingService> greetingService;
        private final ObjectProvider<DebugToolbar> debugToolbar;
        private final Notifier notifier;
        private final ObjectProvider<NewUiConfig.NewUiFeature> newUiFeature;
        private final ObjectProvider<String> windowsPathTip;

        ConditionalDemo(Environment environment,
                        ObjectProvider<GreetingService> greetingService,
                        ObjectProvider<DebugToolbar> debugToolbar,
                        Notifier notifier,
                        ObjectProvider<NewUiConfig.NewUiFeature> newUiFeature,
                        ObjectProvider<String> windowsPathTip) {
            this.environment = environment;
            this.greetingService = greetingService;
            this.debugToolbar = debugToolbar;
            this.notifier = notifier;
            this.newUiFeature = newUiFeature;
            this.windowsPathTip = windowsPathTip;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("ACTIVE PROFILES: "
                    + (environment.getActiveProfiles().length == 0
                    ? "(none - the \"default\" profile)"
                    : Arrays.toString(environment.getActiveProfiles())));
            System.out.println("=".repeat(74));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Profile(\"dev\") / @Profile(\"prod\") - GreetingService");
            System.out.println("=".repeat(74));
            GreetingService service = greetingService.getIfAvailable();
            System.out.println(service == null
                    ? "  No GreetingService bean exists - neither \"dev\" nor \"prod\" is active."
                    : "  " + service.getClass().getSimpleName() + ": " + service.greet());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Profile(\"!prod\") - DebugToolbar (negation)");
            System.out.println("=".repeat(74));
            DebugToolbar toolbar = debugToolbar.getIfAvailable();
            System.out.println(toolbar == null
                    ? "  DebugToolbar does NOT exist - \"prod\" is active."
                    : "  " + toolbar.info());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@ConditionalOnMissingBean - Notifier fallback pattern");
            System.out.println("=".repeat(74));
            System.out.println("  " + notifier.describe());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@ConditionalOnProperty - feature.new-ui");
            System.out.println("=".repeat(74));
            NewUiConfig.NewUiFeature feature = newUiFeature.getIfAvailable();
            System.out.println(feature == null
                    ? "  NewUiFeature bean does NOT exist - feature.new-ui is false (the default)."
                    : "  " + feature.describe());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Conditional(OnWindowsCondition.class) - a fully custom condition");
            System.out.println("=".repeat(74));
            String tip = windowsPathTip.getIfAvailable();
            System.out.println(tip == null
                    ? "  windowsPathTip bean does NOT exist - os.name did not contain \"win\"."
                    : "  " + tip);
        }
    }
}
