package com.danish.spring.restbasics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 21 - @RestController AND THE @RequestMapping FAMILY
// ============================================================================
// Run: mvn -f Spring/04-building-rest-apis-with-spring-mvc/21-restcontroller-and-request-mapping spring-boot:run
// Then, in another terminal, see the .md for the exact curl commands this lesson runs.
// ============================================================================
@SpringBootApplication
public class RestBasicsApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestBasicsApplication.class, args);
    }
}
