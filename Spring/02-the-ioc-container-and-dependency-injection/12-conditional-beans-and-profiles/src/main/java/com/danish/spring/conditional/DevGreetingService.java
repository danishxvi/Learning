package com.danish.spring.conditional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// @Profile means this bean is registered ONLY when "dev" is among the active profiles.
// With no profile active (or any profile other than "dev"), this class is never even
// instantiated - not "instantiated then ignored," genuinely never constructed at all.
@Profile("dev")
@Component
public class DevGreetingService implements GreetingService {
    @Override
    public String greet() {
        return "Hey! (verbose dev greeting, with debug details you would never want in prod)";
    }
}
