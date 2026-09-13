package com.danish.spring.configclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 61 - CENTRALIZED CONFIGURATION WITH CONFIG SERVER (a client)
// ============================================================================
// Requires config-server (8888) already running.
// Run: mvn -f Spring/12-microservices-with-spring-cloud/61-centralized-configuration-with-config-server/config-client spring-boot:run
// With a profile:
// mvn -f Spring/12-microservices-with-spring-cloud/61-centralized-configuration-with-config-server/config-client spring-boot:run -Dspring-boot.run.profiles=dev
// ============================================================================
@SpringBootApplication
public class ConfigClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}
