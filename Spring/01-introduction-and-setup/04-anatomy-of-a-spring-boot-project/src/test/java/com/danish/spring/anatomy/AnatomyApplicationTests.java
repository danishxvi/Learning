package com.danish.spring.anatomy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// This is the file Spring Initializr generates by default in src/test/java, mirroring
// the main package exactly (com.danish.spring.anatomy in both places - see the .md).
// @SpringBootTest starts the ENTIRE context, the same way `spring-boot:run` does, and
// this test simply checks that startup does not throw. It is a cheap, high-value smoke
// test: if any bean fails to wire, this fails immediately. Lesson 49 covers
// @SpringBootTest properly; this is only here to show WHERE such a test lives.
@SpringBootTest
class AnatomyApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty - SpringBootTest already did the work by starting the
        // context above without throwing an exception.
    }
}
