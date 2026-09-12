package com.danish.spring.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

// MDC (Mapped Diagnostic Context) - a per-THREAD map SLF4J exposes, whose contents this
// lesson's logback-spring.xml pattern prints via %X{requestId}. In a real web application,
// a Filter would put a unique request ID into MDC at the start of every request and clear
// it at the end, so every log line printed while handling that request - across every
// class, with no parameter threading required - carries the same ID for correlation.
@Component
public class RequestSimulator {

    private static final Logger log = LoggerFactory.getLogger(RequestSimulator.class);

    public void handle(String requestId, String action) {
        MDC.put("requestId", requestId);
        try {
            log.info("Handling request: {}", action);
            new OrderProcessor().demonstrateLevels();
            log.info("Request complete");
        } finally {
            // ALWAYS remove in a finally block - MDC is thread-local, and thread pools
            // reuse threads, so a forgotten entry leaks into the NEXT unrelated request
            // handled by the same thread.
            MDC.remove("requestId");
        }
    }
}
