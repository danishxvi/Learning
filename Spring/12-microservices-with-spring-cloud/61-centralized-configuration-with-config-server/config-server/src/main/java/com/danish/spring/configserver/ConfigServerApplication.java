package com.danish.spring.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

// ============================================================================
// 61 - CENTRALIZED CONFIGURATION WITH CONFIG SERVER (the server)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/61-centralized-configuration-with-config-server/config-server spring-boot:run
// ============================================================================
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
