package com.danish.spring.devtools;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// ============================================================================
// 19 - DEVTOOLS AND THE INNER DEVELOPMENT LOOP
// ============================================================================
// Run: mvn -f Spring/03-spring-boot-fundamentals/19-devtools-and-the-inner-development-loop spring-boot:run
// Then, while it is running, edit application.yml's app.greeting-suffix and save -
// watch the console: it restarts on its own, in well under a second, with NO manual stop.
// ============================================================================
@SpringBootApplication
public class DevToolsApplication {
    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(DevToolsApplication.class, args);

        // A real web application naturally stays alive on its own - the embedded server
        // keeps a non-daemon thread running (section 04). This lesson has no web server,
        // so without this line the JVM would exit the instant main() returns, taking
        // DevTools' file watcher down with it before it ever got a chance to see a
        // change. Blocking here is ONLY to give DevTools something to keep watching.
        new java.util.concurrent.CountDownLatch(1).await();
    }

    @Component
    static class ReadyPrinter {
        @Value("${app.greeting-suffix}")
        private String greetingSuffix;

        // ApplicationReadyEvent fires once, every time the context finishes starting -
        // including every DevTools restart, not just the very first startup. Watching
        // this line reprint (with an unchanged suffix) or change (after an edit) is how
        // this lesson proves a restart actually happened, from plain console output.
        @EventListener(ApplicationReadyEvent.class)
        public void onReady() {
            System.out.println("=".repeat(74));
            System.out.println("READY - Hello from DevTools demo " + greetingSuffix);
            System.out.println("=".repeat(74));
        }
    }
}
