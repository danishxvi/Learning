package com.danish.spring.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

// ============================================================================
// 60 - SERVICE DISCOVERY WITH EUREKA (the registry server)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/60-service-discovery-with-eureka/eureka-server spring-boot:run
// Dashboard: http://localhost:8761
// ============================================================================
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
