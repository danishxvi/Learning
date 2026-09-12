# 41 · The security filter chain

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/07-spring-security/41-the-security-filter-chain spring-boot:run
> ```

Lesson 40 treated "Spring Security" as one thing that decides 401 vs 403. It's actually
a **chain of servlet filters**, each with one job, running in a specific order before a
request ever reaches a `@RestController`. This lesson inserts two custom filters into
that real chain and watches the actual order Spring prints at startup.

---

## 1. A filter is not an aspect

```java
public class RequestTimingFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ...
        filterChain.doFilter(request, response);   // hands off to the NEXT filter
        ...
    }
}
```

A **servlet filter** operates at the servlet container level — before Spring MVC's own
dispatching begins at all. This is a lower-level interception point than section 06's
`@Aspect`s, which only wrap Spring-managed bean method calls. `filterChain.doFilter(...)`
is what passes the request to the next filter in line; **not** calling it stops the
request from going any further — which is exactly what the next filter does on purpose.

---

## 2. The real chain, printed by Spring Security itself

With `logging.level.org.springframework.security: DEBUG`, starting this lesson prints
the actual, complete filter chain, unedited:

```
Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter,
BlockingFilter, SecurityContextHolderFilter, HeaderWriterFilter, CsrfFilter, LogoutFilter,
RequestTimingFilter, BasicAuthenticationFilter, RequestCacheAwareFilter,
SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter,
ExceptionTranslationFilter, AuthorizationFilter
```

This single log line answers questions that are otherwise opaque: **CSRF checking
happens before authentication is attempted; authorization (`AuthorizationFilter`) is
the very last filter, after authentication has already resolved.** Two custom filters
sit exactly where they were told to:

```java
.addFilterBefore(new BlockingFilter(), SecurityContextHolderFilter.class)
.addFilterBefore(new RequestTimingFilter(), UsernamePasswordAuthenticationFilter.class)
```

`BlockingFilter` landed second in the whole chain — ahead of everything Spring Security
itself does. `RequestTimingFilter` landed right before `BasicAuthenticationFilter` (the
filter that actually reads and checks HTTP Basic credentials) — `addFilterBefore` places
relative to `UsernamePasswordAuthenticationFilter`, and Spring inserted it directly
ahead of the credential-checking filter that matters for this configuration.

---

## 3. Short-circuiting the chain, for real

```java
if ("true".equals(request.getHeader("X-Blocked"))) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.getWriter().write("Blocked before authentication was even attempted");
    return;   // filterChain.doFilter() is NEVER called
}
```

```bash
curl -i -H "X-Blocked: true" http://localhost:8080/hello
```
```
HTTP/1.1 403
Blocked before authentication was even attempted
```

Console output confirms exactly what didn't happen:

```
[BlockingFilter] REJECTING /hello - never reaches Spring Security at all
```

**No `RequestTimingFilter` line, no `DemoController` line.** Because `BlockingFilter`
sits ahead of every Spring Security filter and never calls `doFilter`, the request never
reaches authentication, authorization, or the controller at all — it's rejected at the
very front of the pipeline, before anyone even asks "who are you."

---

## 4. A real pitfall: `sendError()` re-enters the chain

The first version of `BlockingFilter` used `response.sendError(403, ...)` instead of
writing the response directly — and it produced the **wrong** status: a real `401`, with
a `WWW-Authenticate` header, instead of the intended `403`. The reason:
`sendError()` triggers the servlet container's **error-page dispatch** mechanism, which
re-enters the entire filter chain a second time, tagged as an `ERROR` dispatch.
`OncePerRequestFilter` skips `ERROR` dispatches by default — so on that second pass,
`BlockingFilter` doesn't run again, and the request falls through to Spring Security's
own filters unguarded, which then challenge for authentication (no credentials were
sent) and produce their own `401`, silently overwriting the intended `403`. Writing the
response body directly and returning avoids the container's error-dispatch machinery
entirely — a genuine, reproducible gotcha worth knowing before it produces a confusing
wrong status code in a real filter.

---

## 5. Why order matters this much

Every filter in the printed chain runs for **every** request, in that fixed order,
regardless of which controller method eventually handles it (or doesn't). Placing a
custom filter matters enormously: a logging filter placed *after* authentication can log
who made the request; placed *before*, it can't. A rate-limiting or IP-blocking filter
placed *before* Spring Security's own filters can reject a request without Spring
Security doing any work at all — exactly the efficiency reason such filters are
typically placed as early as possible.

---

## 6. Summary

- **A servlet filter** intercepts a request before Spring MVC dispatching begins — a
  lower-level, different interception point than an `@Aspect` (section 06).
- **`logging.level.org.springframework.security: DEBUG`** prints the real, complete
  filter chain at startup — the actual source of truth for "what runs, in what order,"
  not a diagram to trust blindly.
- **`addFilterBefore(filter, SomeFilter.class)`** places a custom filter at an exact,
  verifiable position in that chain.
- **Not calling `filterChain.doFilter(...)`** stops the request from going any further —
  the mechanism behind rejecting a request before Spring Security even attempts
  authentication.
- **`response.sendError()` re-enters the filter chain** via the container's error-page
  dispatch, which `OncePerRequestFilter` skips by default on that second pass — write
  the response directly instead when a filter needs to short-circuit cleanly.

---

**Previous:** [40 — Authentication vs authorization](../40-authentication-vs-authorization/40-authentication-vs-authorization.md) ·
**Next:** [42 — Password encoding and `UserDetails`](../42-password-encoding-and-userdetails/42-password-encoding-and-userdetails.md)
