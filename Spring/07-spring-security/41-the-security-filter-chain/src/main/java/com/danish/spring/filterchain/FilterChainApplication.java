package com.danish.spring.filterchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ============================================================================
// 41 - THE SECURITY FILTER CHAIN
// ============================================================================
// Run: mvn -f Spring/07-spring-security/41-the-security-filter-chain spring-boot:run
// Then, in another terminal, see the .md for the exact curl commands.
// ============================================================================
@SpringBootApplication
public class FilterChainApplication {
    public static void main(String[] args) {
        SpringApplication.run(FilterChainApplication.class, args);
    }
}
