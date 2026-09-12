package com.danish.spring.runners;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// CommandLineRunner gets the RAW String[] - exactly what main(String[] args) would have
// received, completely unparsed. "--name=Danish" and "hello" are indistinguishable from
// each other here; both are just strings in an array.
@Order(1)
@Component
public class RawArgsRunner implements CommandLineRunner {
    @Override
    public void run(String... args) {
        System.out.println("  [1] CommandLineRunner - raw args: " + java.util.Arrays.toString(args));
    }
}
