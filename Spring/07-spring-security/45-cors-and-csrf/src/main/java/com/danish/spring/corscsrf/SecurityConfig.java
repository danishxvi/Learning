package com.danish.spring.corscsrf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // CORS allow-list - ONLY this exact origin may read cross-origin responses from
    // /api/**. Change this value and restart to see the browser's own behaviour flip -
    // see the .md for the exact before/after.
    private static final String ALLOWED_ORIGIN = "http://localhost:5500";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // CSRF is left ENABLED (the default) on purpose - unlike lesson 43's
            // stateless JWT API, this app uses cookies, so CSRF protection is exactly
            // the right call here, not something to reflexively disable.
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    // The DEFAULT handler (XorCsrfTokenRequestAttributeHandler) XOR-masks
                    // the token's value every time it's read via getToken(), specifically
                    // to defend against BREACH attacks on the raw value. That's the RIGHT
                    // choice for a server-rendered HTML form (the mask is applied and
                    // removed transparently within one request/response cycle) - but it
                    // means the cookie's raw value and the masked value a client is
                    // expected to echo back are DIFFERENT strings. A JavaScript client
                    // that simply reads the cookie and sends it back unmodified (the
                    // standard SPA pattern) needs the PLAIN handler instead, so the
                    // cookie value and the expected header value are the identical string.
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            // Forces the CsrfToken to be resolved (and its cookie written) on EVERY
            // request - see CsrfCookieFilter's own comment for the real bug this fixes.
            .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(ALLOWED_ORIGIN));
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // required for the browser to send/receive the CSRF cookie cross-origin

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
