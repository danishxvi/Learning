package com.danish.spring.runners;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// Runs LAST (highest @Order value) so the two runners above always get to print first.
// Throws only when explicitly asked to (--fail), to demonstrate a real consequence: an
// exception thrown from ANY runner aborts the whole application - SpringApplication.run
// never returns normally, and the process exits with a non-zero status.
@Order(3)
@Component
public class FailingRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("fail")) {
            throw new IllegalStateException("FailingRunner: --fail was passed, aborting startup on purpose.");
        }
        System.out.println("  [3] FailingRunner - no --fail flag, finishing normally.");
    }
}
