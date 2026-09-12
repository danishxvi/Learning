package com.danish.spring.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessor {

    // SLF4J is a FACADE - this code never mentions Logback by name. Swapping the
    // implementation (Log4j2, java.util.logging) needs a dependency change, zero code
    // change, because every call here goes through org.slf4j.Logger only.
    private static final Logger log = LoggerFactory.getLogger(OrderProcessor.class);

    private int expensiveCallCount = 0;

    public void demonstrateLevels() {
        // Five levels, lowest to highest severity. Only levels AT OR ABOVE the configured
        // threshold actually print - application.yml set this package to DEBUG, so all
        // five print here; root stays INFO, so a class in another package would only show
        // the last three.
        log.trace("TRACE - extremely fine-grained, almost always off even in dev");
        log.debug("DEBUG - development detail, off by default, on for this package via application.yml");
        log.info("INFO - normal operational messages, the default threshold");
        log.warn("WARN - something unexpected but not yet broken");
        log.error("ERROR - something failed");
    }

    public void demonstrateParameterizedLogging() {
        // BAD: string concatenation runs UNCONDITIONALLY, even if DEBUG were disabled -
        // expensiveDescription() would be called and its result thrown away.
        log.debug("Concatenated (bad): order=" + expensiveDescription());

        // GOOD: the {} placeholder means expensiveDescription() is passed as an ARGUMENT,
        // not concatenated into a String first. SLF4J only calls toString() on it if the
        // DEBUG level is actually enabled - if disabled, the argument is never touched.
        log.debug("Parameterized (good): order={}", expensiveDescription());

        System.out.println("  expensiveDescription() was called " + expensiveCallCount + " times "
                + "(both would call it here, since DEBUG IS enabled for this package - "
                + "the difference only matters when the level is OFF, see the .md)");
    }

    private String expensiveDescription() {
        expensiveCallCount++;
        return "ORD-" + (1000 + expensiveCallCount);
    }

    public void demonstrateExceptionLogging() {
        try {
            throw new IllegalStateException("payment gateway timeout");
        } catch (IllegalStateException ex) {
            // Passing the exception as the LAST argument logs the full stack trace, not
            // just ex.getMessage() - always prefer this over log.error(ex.getMessage()).
            log.error("Order processing failed", ex);
        }
    }
}
