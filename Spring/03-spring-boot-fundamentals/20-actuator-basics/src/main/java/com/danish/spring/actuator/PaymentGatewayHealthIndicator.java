package com.danish.spring.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

// A CUSTOM health check. Actuator discovers any bean implementing HealthIndicator
// automatically (Spring's usual "find every bean of this type" pattern, lesson 11) and
// folds its result into the OVERALL status returned by /actuator/health - if this
// reports DOWN, the whole endpoint reports DOWN, the way a real load balancer or
// orchestrator (Kubernetes) expects.
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean reachable = checkPaymentGateway();
        if (reachable) {
            return Health.up()
                    .withDetail("gateway", "stripe-sandbox")
                    .withDetail("latencyMs", 42)
                    .build();
        }
        return Health.down()
                .withDetail("gateway", "stripe-sandbox")
                .withDetail("reason", "connection timed out")
                .build();
    }

    private boolean checkPaymentGateway() {
        return true; // pretend it is reachable - this lesson only needs the SHAPE of a real check
    }
}
