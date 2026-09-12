package com.danish.spring.runners;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// ApplicationRunner gets an ApplicationArguments WRAPPER instead of a raw array. It
// understands Spring Boot's own "--name=value" option convention and separates those
// from plain positional arguments automatically - no manual parsing needed.
@Order(2)
@Component
public class ParsedArgsRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        System.out.println("  [2] ApplicationRunner - option names:  " + args.getOptionNames());
        for (String name : args.getOptionNames()) {
            System.out.println("      --" + name + " = " + args.getOptionValues(name));
        }
        System.out.println("  [2] ApplicationRunner - non-option args: " + args.getNonOptionArgs());
        System.out.println("  [2] ApplicationRunner - raw source args: " + java.util.Arrays.toString(args.getSourceArgs()));
    }
}
