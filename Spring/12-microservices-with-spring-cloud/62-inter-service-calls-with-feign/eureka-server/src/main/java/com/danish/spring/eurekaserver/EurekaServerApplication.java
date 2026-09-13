package com.danish.spring.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

// ============================================================================
// 62 - INTER-SERVICE CALLS WITH FEIGN (the registry server, same role as lesson 60)
// ============================================================================
// Run: mvn -f Spring/12-microservices-with-spring-cloud/62-inter-service-calls-with-feign/eureka-server spring-boot:run
// Dashboard: http://localhost:8761
// ============================================================================
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
