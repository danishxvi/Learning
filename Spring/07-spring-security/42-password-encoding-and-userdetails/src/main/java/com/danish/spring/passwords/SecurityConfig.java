package com.danish.spring.passwords;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // A PLAIN BCryptPasswordEncoder - not the DelegatingPasswordEncoder lessons 40-41
    // used - specifically so this lesson's demo can inspect a raw BCrypt hash directly,
    // without a "{bcrypt}" prefix in the way. The .md covers what that prefix is for.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Wires our OWN UserDetailsService and PasswordEncoder into an AuthenticationManager
    // we can call directly (see PasswordDemo) - this is exactly what HTTP Basic itself
    // calls internally on every request; the demo just calls it by hand to inspect the
    // result instead of only seeing an HTTP status code.
    @Bean
    public AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        return provider::authenticate;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(basic -> {});
        return http.build();
    }
}
