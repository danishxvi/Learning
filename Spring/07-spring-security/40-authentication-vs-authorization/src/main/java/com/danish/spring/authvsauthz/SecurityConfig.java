package com.danish.spring.authvsauthz;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// Replaces zero-config Spring Security's "one generated password, all-or-nothing" model
// with REAL users carrying REAL roles - this is what makes AUTHORIZATION (lesson 40's
// second half) a meaningful, separate decision from just being logged in at all.
@Configuration
public class SecurityConfig {

    // Two users, two different roles. bob is authenticated but has no ADMIN role -
    // watch what happens when he requests /admin/dashboard.
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("bob").password(encoder.encode("bob-pass")).roles("USER").build(),
                User.withUsername("alice").password(encoder.encode("alice-pass")).roles("USER", "ADMIN").build()
        );
    }

    // Password encoding is covered properly in lesson 42 - this is the minimum needed
    // here so passwords are not compared as plain text even in this lesson's demo.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // AUTHENTICATION alone decides who gets past the front door at all -
                // permitAll() means NO authentication is required for this path.
                .requestMatchers("/public/**").permitAll()
                // AUTHORIZATION - being authenticated is necessary but not sufficient
                // here. hasRole("ADMIN") checks the ROLE on top of identity.
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Any other request just needs to be authenticated, any role.
                .anyRequest().authenticated()
            )
            // HTTP Basic - credentials sent (base64-encoded, NOT encrypted) on every
            // request, in the Authorization header. Simple, fine for this lesson;
            // section 07 covers JWT (lesson 43) as the real alternative for a stateless API.
            .httpBasic(basic -> {});
        return http.build();
    }
}
