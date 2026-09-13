package com.danish.spring.corscsrf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// A real, documented Spring Security 6 gotcha, hit while building this lesson: by
// default, the CsrfToken is resolved LAZILY - Spring only actually generates and writes
// the XSRF-TOKEN cookie if something reads the token during the request. A plain
// @RestController that never touches the CSRF token never triggers that read, so the
// cookie value silently changes on every single request and never matches what a
// client sends back - every POST fails with 403 even with a seemingly valid token.
//
// The fix: force the token to be read (and therefore fixed, and therefore written to
// the cookie consistently) on every request, via this filter.
public class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            csrfToken.getToken(); // the read that forces resolution - the fix is this one line
        }
        filterChain.doFilter(request, response);
    }
}
