package com.danish.spring.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// A CUSTOM filter (lesson 41's exact mechanism) - this is the ENTIRE authentication
// mechanism for this lesson. There is no UsernamePasswordAuthenticationFilter doing
// form-login work, no BasicAuthenticationFilter checking a header the way lesson 40
// did - this filter reads the Authorization header itself, validates the token itself,
// and populates the SecurityContext itself, by hand.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                Claims claims = jwtService.parseAndValidate(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                // Building an Authentication object BY HAND and placing it into the
                // SecurityContext - this is exactly what BasicAuthenticationFilter and
                // UsernamePasswordAuthenticationFilter do internally after their own
                // checks succeed. From here on, the rest of the filter chain (and
                // @PreAuthorize, lesson 44) sees a fully authenticated request.
                var authentication = new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException ex) {
                // Deliberately does NOT reject the request here - leaves the
                // SecurityContext empty and lets AuthorizationFilter (lesson 41's chain)
                // decide what an unauthenticated request should get, exactly the way an
                // invalid Basic Auth header would be handled.
                System.out.println("  [JwtAuthenticationFilter] rejected: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
