package com.danish.spring.valuedeep;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 13 - @Value INJECTION AND EXTERNALIZED CONFIGURATION, DEEPER
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/13-value-injection-and-externalized-configuration spring-boot:run
// ============================================================================
@SpringBootApplication
public class ValueDeepDiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValueDeepDiveApplication.class, args);
    }

    @Component
    static class ValueDeepDiveDemo implements CommandLineRunner {

        private final AppInfo appInfo;
        private final SpelShowcase spelShowcase;

        ValueDeepDiveDemo(AppInfo appInfo, SpelShowcase spelShowcase) {
            this.appInfo = appInfo;
            this.spelShowcase = spelShowcase;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("@PropertySource - a SECOND file, loaded alongside application.yml");
            System.out.println("=".repeat(74));
            System.out.println("  version=" + appInfo.getVersion() + ", buildName=" + appInfo.getBuildName()
                    + "  (from extra.properties, via constructor @Value)");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("SpEL EXPRESSIONS - everything #{...} can do that ${...} can't");
            System.out.println("=".repeat(74));
            spelShowcase.printAll();
        }
    }
}
