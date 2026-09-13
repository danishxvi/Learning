package com.danish.spring.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
            // STATELESS - Spring Security will NEVER create an HttpSession for this
            // application, and never reads one either. Every request must carry
            // everything needed to authenticate it, on its own - the actual meaning of
            // "stateless" here, not just a buzzword.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // CSRF protection exists to protect COOKIE-based session auth from a
            // forged cross-site request riding on an existing session cookie. With no
            // session and no cookie, there is nothing for CSRF to protect - disabling
            // it here is correct for this architecture, not a shortcut. Lesson 45
            // covers exactly when CSRF still matters.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/login", "/public/**").permitAll()
                    .anyRequest().authenticated())
            // Our filter runs where UsernamePasswordAuthenticationFilter normally would -
            // it IS this lesson's authentication mechanism, replacing form login entirely.
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
