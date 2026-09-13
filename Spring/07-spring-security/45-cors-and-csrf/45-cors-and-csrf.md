# 45 · CORS and CSRF

> **Run the code for this lesson** — this one genuinely needs a browser, not curl (CORS
> is a *browser* restriction; curl never enforces it):
> ```bash
> mvn -f Spring/07-spring-security/45-cors-and-csrf spring-boot:run
> ```
> Then serve `test-page/` on a different port (any static server works):
> ```bash
> npx http-server Spring/07-spring-security/45-cors-and-csrf/test-page -p 5500
> ```
> Open `http://localhost:5500` in an actual browser and click the buttons.

Two different browser security mechanisms, both commonly confused with each other.
This lesson keeps them apart — and along the way, hits two real, documented Spring
Security gotchas that only show up when you actually try this in a browser instead of
reading about it.

---

## 1. CORS: a real, browser-enforced block

The test page is served from `http://localhost:5500`; the API is on
`http://localhost:8080` — genuinely different origins. With the server's allow-list set
to the wrong origin on purpose (`http://localhost:9999`), clicking "fetch" in a real
browser produces this, in the DevTools console, unedited:

```
Access to fetch at 'http://localhost:8080/api/data' from origin 'http://localhost:5500'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on
the requested resource.
```

**This is the browser refusing to hand the response to the page's JavaScript** — the
HTTP request actually completed; Spring Boot answered it; the browser simply won't let
the calling script read the result, because the response didn't carry permission for
this specific origin. `curl` would show a perfectly normal `200` for the exact same
request — CORS is enforced entirely client-side, by the browser, which is why this
lesson needs one running for real instead of a `curl` command.

Fixing the allow-list to the correct origin and reloading:

```java
configuration.setAllowedOrigins(List.of("http://localhost:5500"));
```
```json
SUCCESS (200): {"message":"This is cross-origin data.","notesCount":0}
```

Same request, same two ports, now genuinely allowed.

---

## 2. CSRF: what it protects, and a real 403 without a token

```java
.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
```

POSTing to `/api/notes` with no CSRF token at all — no login required, `permitAll` is
in effect, and it's still rejected:

```
Status 403 (no token sent): {"timestamp":"...","status":403,"error":"Forbidden","path":"/api/notes"}
```

CSRF protects **state-changing requests** (`POST`/`PUT`/`PATCH`/`DELETE`) specifically
because a malicious site could otherwise silently trigger one using a victim's existing
session cookie, just by getting their browser to submit a form or make a request to
your site — the browser attaches cookies automatically, regardless of which site's page
initiated the request. A CSRF token, sent by a legitimate client that actually read it
from a cookie or page, is something a forged cross-site request has no way to supply.

---

## 3. A real gotcha: the token cookie was never actually being written

The first version of this lesson's `SecurityConfig` used only
`CookieCsrfTokenRepository.withHttpOnlyFalse()` and still failed every POST — even with
a token read from the cookie and sent back correctly. The reason: **Spring Security 6
resolves the CSRF token lazily** — it's only actually generated (and its cookie written)
if something during the request reads it. A plain `@RestController` that never touches
`request.getAttribute("_csrf")` never triggers that read, so the cookie is never
reliably refreshed. The documented fix is a filter that forces the read on every
request:

```java
public class CsrfCookieFilter extends OncePerRequestFilter {
    protected void doFilterInternal(...) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) csrfToken.getToken();   // the read that forces resolution
        filterChain.doFilter(request, response);
    }
}
```
```java
.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
```

---

## 4. A second real gotcha: the default handler masks the token

Even after adding `CsrfCookieFilter`, the exact same cookie value sent back as the
`X-XSRF-TOKEN` header **still** failed — verified directly with `curl`, isolating the
browser entirely out of the picture:

```bash
curl -c cookies.txt http://localhost:8080/api/data
curl -b cookies.txt -H "X-XSRF-TOKEN: <the exact cookie value>" -X POST http://localhost:8080/api/notes -d '...'
# HTTP/1.1 403
```

The cause: Spring's **default** `CsrfTokenRequestHandler` is
`XorCsrfTokenRequestAttributeHandler`, which XOR-masks the token's value with a random
pad every time it's read, specifically to protect the raw value against BREACH-style
compression attacks in server-rendered HTML forms. That's the right default for a
Thymeleaf form (the mask is applied and removed transparently within one request). A
JavaScript client that just reads the cookie and echoes it back — the standard SPA
pattern this lesson uses — sends the **unmasked** value, which the XOR handler then
fails to match. The fix is switching to the plain handler:

```java
.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
```

With both fixes in place, the exact same `curl` sequence — and the real browser test
page — succeed:

```bash
curl -b cookies.txt -H "X-XSRF-TOKEN: <cookie value>" -X POST http://localhost:8080/api/notes -d '...'
# HTTP/1.1 200
```
```
Status 200 (token=77ba684e-...): {"text":"a note WITH a valid CSRF token","id":2}
```

---

## 5. Why lesson 43's JWT API disabled CSRF, and this one doesn't

```java
// lesson 43
.csrf(csrf -> csrf.disable())   // correct there - no cookies, no session, nothing to forge
```
```java
// this lesson
.csrf(csrf -> csrf.csrfTokenRepository(...))  // correct here - cookies ARE in play
```

CSRF protection exists specifically to guard **cookie-based** authentication — a
malicious page can make your browser send a request with your session cookie attached,
but it cannot read or forge a CSRF token it was never given. A stateless JWT API
(lesson 43) sends its token in an `Authorization` header the client controls explicitly
and a browser never attaches automatically to a cross-site request — there's nothing
for CSRF to protect there, which is *why* lesson 43 disabling it was correct, not a
shortcut. **The right call depends entirely on the authentication mechanism** — CORS and
CSRF are two separate decisions, and neither one implies anything about the other.

---

## 6. Summary

- **CORS is enforced by the browser**, not the server — the request completes either
  way; CORS only decides whether the calling page's JavaScript is allowed to read the
  response. `curl` never sees a CORS error because `curl` isn't a browser.
- **CSRF protects state-changing requests specifically because browsers attach cookies
  automatically**, regardless of which site's page triggered the request — a token the
  attacker's page never had access to closes that gap.
- **The CSRF token is resolved lazily by default** — a controller that never reads it
  never causes its cookie to be reliably written; a filter forcing
  `csrfToken.getToken()` on every request fixes this.
- **The default `XorCsrfTokenRequestAttributeHandler` masks the token value** — correct
  for server-rendered forms, wrong for a JavaScript client that reads a cookie and
  echoes it back verbatim; switch to the plain `CsrfTokenRequestAttributeHandler` for
  that pattern.
- **CORS and CSRF are independent decisions** — a stateless, header-based API (lesson
  43) correctly disables CSRF; a cookie-based one (this lesson) correctly keeps it
  enabled and configures CORS separately for its own cross-origin needs.

This closes section 07. Every mechanism from zero-config lockdown (lesson 40) through
CORS and CSRF here builds on the same filter chain (lesson 41) and the same proxy-based
enforcement (lesson 38) established across the whole section.

---

**Previous:** [44 — Method-level security](../44-method-level-security/44-method-level-security.md) ·
**Next:** [46 — Unit testing with JUnit 5 and Mockito](../../08-testing-spring-applications/46-unit-testing-with-junit5-and-mockito/46-unit-testing-with-junit5-and-mockito.md)
