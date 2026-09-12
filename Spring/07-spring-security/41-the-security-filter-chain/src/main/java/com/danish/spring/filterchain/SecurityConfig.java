package com.danish.spring.filterchain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("bob").password(encoder.encode("bob-pass")).roles("USER").build());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .httpBasic(basic -> {})
            // BlockingFilter runs BEFORE SecurityContextHolderFilter - effectively the
            // very first thing in the entire chain, ahead of every Spring Security
            // filter that does anything related to authentication.
            .addFilterBefore(new BlockingFilter(), SecurityContextHolderFilter.class)
            // RequestTimingFilter runs before the filter that actually reads HTTP Basic
            // credentials and attempts authentication - so it wraps around (and its
            // "EXITING" line reports the time for) the entire authentication process.
            .addFilterBefore(new RequestTimingFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
