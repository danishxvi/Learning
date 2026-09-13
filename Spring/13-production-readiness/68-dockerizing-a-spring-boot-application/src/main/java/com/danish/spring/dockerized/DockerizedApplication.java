package com.danish.spring.dockerized;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// ============================================================================
// 68 - DOCKERIZING A SPRING BOOT APPLICATION
// ============================================================================
// Build: mvn -f Spring/13-production-readiness/68-dockerizing-a-spring-boot-application package
// Then:  docker build -t learning-dockerized-demo Spring/13-production-readiness/68-dockerizing-a-spring-boot-application
//        docker run --rm -p 8080:8080 learning-dockerized-demo
// ============================================================================
@SpringBootApplication
public class DockerizedApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerizedApplication.class, args);
    }

    @RestController
    static class PingController {
        @GetMapping("/ping")
        public String ping() {
            return "pong from inside a Docker container - version 2 (only this line changed)";
        }
    }
}
