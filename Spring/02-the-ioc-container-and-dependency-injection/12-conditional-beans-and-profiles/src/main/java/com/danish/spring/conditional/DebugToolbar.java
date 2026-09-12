package com.danish.spring.conditional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// @Profile("!prod") is NEGATION - active whenever "prod" is NOT among the active profiles.
// That includes "dev", any other profile, AND no active profile at all (the "default"
// profile lesson 05 introduced). Only an explicit "prod" activation turns this bean off.
@Profile("!prod")
@Component
public class DebugToolbar {
    public String info() {
        return "Debug toolbar ACTIVE - this must never appear in a production log.";
    }
}
