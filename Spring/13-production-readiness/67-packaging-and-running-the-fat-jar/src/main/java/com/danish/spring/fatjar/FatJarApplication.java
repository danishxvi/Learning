package com.danish.spring.fatjar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// ============================================================================
// 67 - PACKAGING AND RUNNING THE FAT JAR
// ============================================================================
// Build: mvn -f Spring/13-production-readiness/67-packaging-and-running-the-fat-jar package
// Run:   java -jar Spring/13-production-readiness/67-packaging-and-running-the-fat-jar/target/67-packaging-and-running-the-fat-jar-1.0.0.jar
// ============================================================================
@SpringBootApplication
public class FatJarApplication {
    public static void main(String[] args) {
        SpringApplication.run(FatJarApplication.class, args);
    }

    @RestController
    static class PingController {
        @GetMapping("/ping")
        public String ping() {
            return "pong from the fat jar, running with no IDE, no mvn, no target/classes on the classpath";
        }
    }
}
