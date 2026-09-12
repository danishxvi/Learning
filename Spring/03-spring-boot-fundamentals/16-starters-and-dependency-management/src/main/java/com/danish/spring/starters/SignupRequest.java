package com.danish.spring.starters;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// jakarta.validation.constraints.* comes from jakarta.validation:jakarta.validation-api -
// one of the jars spring-boot-starter-validation pulled in with no version typed anywhere
// in this project's pom.xml. Bean Validation itself is covered fully in lesson 26; this
// class exists only to PROVE the starter's dependencies actually work at runtime, not
// just resolve at build time.
public class SignupRequest {

    @NotBlank
    private final String username;

    @Email
    private final String email;

    public SignupRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
