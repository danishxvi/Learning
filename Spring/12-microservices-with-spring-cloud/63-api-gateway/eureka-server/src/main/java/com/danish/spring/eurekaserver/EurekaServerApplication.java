package com.danish.spring.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

// ============================================================================
// 63 - API GATEWAY (the registry server, same role as lessons 60 and 62)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/63-api-gateway/eureka-server spring-boot:run
// Dashboard: http://localhost:8761
// ============================================================================
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
