package com.danish.spring.passwords;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AppUserDetailsService implements UserDetailsService {

    // In-memory, standing in for a real repository (section 05). Real hashes, produced
    // by encoding a real plaintext password at startup - exactly what a signup endpoint
    // would do before ever writing a row to a database.
    private final Map<String, AppUserAccount> accounts;

    public AppUserDetailsService(PasswordEncoder encoder) {
        accounts = Map.of(
                "bob", new AppUserAccount("bob", encoder.encode("bob-pass"), "USER", true),
                "eve", new AppUserAccount("eve", encoder.encode("eve-pass"), "USER", false) // enabled = false
        );
    }

    // Loads by username, wraps the domain object in the UserDetails adapter, or throws
    // UsernameNotFoundException - Spring Security treats a missing user and a wrong
    // password IDENTICALLY from the client's point of view (both become a generic
    // authentication failure), specifically so a client can never tell "the username
    // doesn't exist" from "the password was wrong" - that distinction would help an
    // attacker enumerate valid usernames.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUserAccount account = accounts.get(username);
        if (account == null) {
            throw new UsernameNotFoundException("No account for " + username);
        }
        return new AppUserPrincipal(account);
    }
}
