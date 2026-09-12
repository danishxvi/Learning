package com.danish.spring.actuator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 20 - ACTUATOR BASICS
// ============================================================================
// Run: mvn -f Spring/03-spring-boot-fundamentals/20-actuator-basics spring-boot:run
// Then, in another terminal:
//   curl http://localhost:8081/actuator/health
//   curl http://localhost:8081/actuator/info
//   curl http://localhost:8081/actuator/metrics/jvm.memory.used
// Note the PORT - 8081, not 8080 - see the .md for why.
// ============================================================================
@SpringBootApplication
public class ActuatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActuatorApplication.class, args);
    }
}
