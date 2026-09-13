package com.danish.spring.webtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 47 - TESTING THE WEB LAYER WITH @WebMvcTest
// ============================================================================
// The lesson content lives in src/test/java - run it:
// mvn -f Spring/08-testing-spring-applications/47-testing-the-web-layer-with-webmvctest test
// ============================================================================
@SpringBootApplication
public class WebLayerTestingApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebLayerTestingApplication.class, args);
    }
}
