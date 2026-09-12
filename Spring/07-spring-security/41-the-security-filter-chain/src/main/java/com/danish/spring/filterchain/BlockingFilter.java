package com.danish.spring.filterchain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Runs BEFORE Spring Security's own filters (see SecurityConfig's addFilterBefore call).
// A request carrying "X-Blocked: true" never reaches authentication OR authorization at
// all - it is rejected at the very front of the chain, before Spring Security has any
// chance to even ask "who are you."
public class BlockingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("true".equals(request.getHeader("X-Blocked"))) {
            System.out.println("  [BlockingFilter] REJECTING " + request.getRequestURI() + " - never reaches Spring Security at all");
            // Writing the response DIRECTLY, not via response.sendError(...). sendError()
            // triggers the servlet container's error-page DISPATCH mechanism, which
            // re-enters the filter chain a second time for that dispatch - and since
            // OncePerRequestFilter skips ERROR dispatches by default, THIS filter would
            // not run again, letting the request fall through to Spring Security's own
            // filters unguarded on that second pass, which then challenge for
            // authentication and overwrite this 403 with a 401. Writing the response
            // body ourselves and returning avoids that pitfall entirely.
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("text/plain");
            response.getWriter().write("Blocked before authentication was even attempted");
            return; // deliberately NOT calling filterChain.doFilter() - the chain stops HERE
        }
        System.out.println("  [BlockingFilter] passing " + request.getRequestURI() + " through");
        filterChain.doFilter(request, response);
    }
}
