package com.danish.spring.actuatordeepdive;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

// A custom HealthIndicator becomes a named component under /actuator/health -
// "downstreamService" here, because Spring strips the "HealthIndicator" suffix from
// the bean name. Spring Boot's default aggregation rule is simple and real: if ANY
// registered indicator reports DOWN, the application's OVERALL status is DOWN too -
// this is what makes /actuator/health a meaningful signal for a load balancer or
// orchestrator to act on, not just "is the JVM process alive."
@Component("downstreamService")
public class DownstreamHealthIndicator implements HealthIndicator {

    private final AtomicBoolean healthy = new AtomicBoolean(true);

    @Override
    public Health health() {
        if (healthy.get()) {
            return Health.up().withDetail("checkedAt", System.currentTimeMillis()).build();
        }
        return Health.down().withDetail("reason", "simulated downstream outage").build();
    }

    public void setHealthy(boolean healthy) {
        this.healthy.set(healthy);
    }
}
