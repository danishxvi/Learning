package com.danish.spring.datatest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 48 - TESTING THE DATA LAYER WITH @DataJpaTest
// ============================================================================
// The lesson content lives in src/test/java - run it:
// mvn -f Spring/08-testing-spring-applications/48-testing-the-data-layer-with-datajputest test
// ============================================================================
@SpringBootApplication
public class DataTestingApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataTestingApplication.class, args);
    }
}
