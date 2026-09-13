package com.danish.spring.jwt;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

// A tiny stand-in for a real user repository (section 05) - just enough to check a
// login attempt and know a role, so this lesson can stay focused on the TOKEN mechanism
// rather than re-covering lesson 42's UserDetails machinery.
@Component
public class UserStore {
    private final Map<String, String> passwordHashesByUsername;
    private final Map<String, String> rolesByUsername = Map.of("bob", "USER");

    public UserStore(PasswordEncoder encoder) {
        passwordHashesByUsername = Map.of("bob", encoder.encode("bob-pass"));
    }

    public boolean checkPassword(String username, String rawPassword, PasswordEncoder encoder) {
        String hash = passwordHashesByUsername.get(username);
        return hash != null && encoder.matches(rawPassword, hash);
    }

    public String roleOf(String username) {
        return rolesByUsername.get(username);
    }
}
