package com.danish.spring.filterchain;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// A Servlet FILTER - not Spring AOP (section 06). Filters operate at the SERVLET
// container level, before Spring MVC's own dispatching even begins - this is a
// completely different, lower-level interception point than @Aspect ever reaches.
// OncePerRequestFilter guarantees this runs exactly once per request, even if the
// servlet container would otherwise dispatch the same request internally more than once.
public class RequestTimingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        System.out.println("  [RequestTimingFilter] ENTERING - " + request.getMethod() + " " + request.getRequestURI());

        // filterChain.doFilter(...) is what hands the request to the NEXT filter in the
        // chain - Spring Security's own authentication filters, in this lesson's setup.
        // Skipping this call entirely would stop the request from going any further at
        // all - see BlockingFilter for exactly that, used deliberately.
        filterChain.doFilter(request, response);

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  [RequestTimingFilter] EXITING  - " + request.getRequestURI() + " (" + elapsedMillis + " ms total, including auth)");
    }
}
