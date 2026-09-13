package com.danish.spring.profiles;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// ============================================================================
// 66 - PROFILES FOR REAL ENVIRONMENTS
// ============================================================================
// Run with no profile (see the .md for what this does on purpose):
//   mvn spring-boot:run
// Run with a profile:
//   mvn spring-boot:run -Dspring-boot.run.profiles=dev
//   mvn spring-boot:run -Dspring-boot.run.profiles=prod
// ============================================================================
@SpringBootApplication
public class ProfilesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProfilesApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final Environment environment;
        private final NotificationSender notificationSender;

        @Value("${app.environment}")
        private String appEnvironment;

        Demo(Environment environment, NotificationSender notificationSender) {
            this.environment = environment;
            this.notificationSender = notificationSender;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("active profiles: " + Arrays.toString(environment.getActiveProfiles()));
            System.out.println("app.environment: " + appEnvironment);
            System.out.println("notificationSender bean: " + notificationSender.getClass().getSimpleName());
            System.out.println(notificationSender.send("order #123 shipped"));
        }
    }
}
