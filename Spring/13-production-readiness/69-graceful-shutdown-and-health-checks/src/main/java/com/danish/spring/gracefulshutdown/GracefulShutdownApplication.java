package com.danish.spring.gracefulshutdown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 69 - GRACEFUL SHUTDOWN AND HEALTH CHECKS
// ============================================================================
// Run: mvn -f Spring/13-production-readiness/69-graceful-shutdown-and-health-checks spring-boot:run
// ============================================================================
@SpringBootApplication
public class GracefulShutdownApplication {
    public static void main(String[] args) {
        SpringApplication.run(GracefulShutdownApplication.class, args);
    }
}
