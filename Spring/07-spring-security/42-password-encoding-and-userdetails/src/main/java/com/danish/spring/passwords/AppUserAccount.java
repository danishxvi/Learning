package com.danish.spring.passwords;

// A plain DOMAIN object - what your own persistence layer would actually store. It
// knows NOTHING about Spring Security's UserDetails interface - that separation is
// deliberate, see AppUserPrincipal for why.
public class AppUserAccount {
    private final String username;
    private final String passwordHash;
    private final String role;
    private final boolean enabled;

    public AppUserAccount(String username, String passwordHash, String role, boolean enabled) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
}
