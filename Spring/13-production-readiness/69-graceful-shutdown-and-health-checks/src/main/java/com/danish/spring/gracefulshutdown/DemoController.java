package com.danish.spring.gracefulshutdown;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    private final ApplicationEventPublisher publisher;

    public DemoController(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    // Simulates a real, slow in-flight request - the exact kind of request graceful
    // shutdown is meant to protect from being cut off mid-processing.
    @GetMapping("/slow-task")
    public String slowTask() throws InterruptedException {
        Thread.sleep(3000);
        return "completed";
    }

    // Manually flips this instance's READINESS state - the real mechanism a load
    // balancer or Kubernetes readiness probe reacts to when deciding whether to send
    // this instance traffic, independent of whether the process itself is alive
    // (liveness) or actually up (regular /health).
    @PostMapping("/readiness/{state}")
    public String setReadiness(@PathVariable String state) {
        ReadinessState newState = state.equalsIgnoreCase("up")
                ? ReadinessState.ACCEPTING_TRAFFIC
                : ReadinessState.REFUSING_TRAFFIC;
        AvailabilityChangeEvent.publish(publisher, this, newState);
        return "readiness set to " + newState;
    }
}
