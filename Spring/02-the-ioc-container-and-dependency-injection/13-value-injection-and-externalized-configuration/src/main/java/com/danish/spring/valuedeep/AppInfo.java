package com.danish.spring.valuedeep;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

// @PropertySource loads an ADDITIONAL properties file that Spring Boot would never read
// on its own - only application.yml/.properties (and their profile-specific variants,
// lesson 05) are loaded automatically. This is how a library, or a config file shared
// across several modules, gets pulled in explicitly.
@PropertySource("classpath:extra.properties")
@Component
public class AppInfo {

    private final String version;
    private final String buildName;

    // CONSTRUCTOR-based @Value - the same recommendation as lesson 08's constructor
    // injection applies here too: prefer this over @Value on a field where practical,
    // for the same testability and final-field reasons.
    public AppInfo(@Value("${app.version}") String version,
                   @Value("${app.build-name}") String buildName) {
        this.version = version;
        this.buildName = buildName;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildName() {
        return buildName;
    }
}
