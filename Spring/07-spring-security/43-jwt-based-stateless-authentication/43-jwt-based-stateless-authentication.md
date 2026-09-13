# 43 · JWT-based stateless authentication

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/07-spring-security/43-jwt-based-stateless-authentication spring-boot:run
> ```

Lessons 40–42 used HTTP Basic — credentials sent on every single request. This lesson
replaces that with a **JSON Web Token (JWT)**: issued once at login, then presented on
every later request, with the server keeping no record of it at all.

---

## 1. What "stateless" actually means, configured explicitly

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

```bash
curl -i -H "Authorization: Bearer <token>" http://localhost:8080/protected/profile
```
```
HTTP/1.1 200
X-Content-Type-Options: nosniff
...
(no Set-Cookie header at all)
```

**No `JSESSIONID`, no cookie, nothing.** Every earlier lesson in this section let Spring
Security create an `HttpSession` implicitly; this one forbids it entirely. "Stateless"
here isn't a buzzword — it's a real, verifiable property: the server holds zero
memory of who logged in or when. Every single request must carry everything needed to
authenticate it, on its own.

---

## 2. Issuing a token

```java
public String generateToken(String username, String role) {
    return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(now + EXPIRATION_MILLIS))
            .signWith(key)
            .compact();
}
```

```bash
curl -X POST http://localhost:8080/login -H "Content-Type: application/json" -d '{"username":"bob","password":"bob-pass"}'
```
```json
{"token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2IiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc4OTI4MTY4MSwiZXhwIjoxNzg5MjgxNjg2fQ.5yFMHlViXM8bC8sTLCu1qohnUPImh1_ma7jBcxogb1k"}
```

`/login` is the **only** endpoint in this lesson that checks a password. Every other
endpoint trusts a valid token instead — this is the entire point: the login step never
needs to happen again for that token to keep working, and there's no server-side
session record it could even fall out of sync with.

---

## 3. A JWT is signed, not encrypted — read it with no key at all

```bash
echo "$TOKEN" | cut -d. -f1 | base64 -d   # {"alg":"HS256"}
echo "$TOKEN" | cut -d. -f2 | base64 -d   # {"sub":"bob","role":"USER","iat":1789281681,"exp":1789281686}
```

**Both the header and the payload are plain, readable JSON** — just base64-encoded, not
encrypted, decodable by anyone with the token and no key at all. This is a genuinely
common misconception worth correcting directly: a JWT proves the *issuer* signed off on
these exact claims (nobody without the secret key could produce a signature that
verifies against them) — it does **not** hide the claims from whoever holds the token.
**Never put a password, a secret, or anything genuinely confidential inside a JWT's
claims** — a username and a role, as here, are the right kind of thing to put in one.

---

## 4. The signature is what tampering breaks

```bash
curl -H "Authorization: Bearer <token with one flipped character>" http://localhost:8080/protected/profile
```
```
403
```

Console log, real and unedited:
```
[JwtAuthenticationFilter] rejected: SignatureException - JWT signature does not match locally computed signature.
JWT validity cannot be asserted and should not be trusted.
```

Changing even one character anywhere in the token — including the readable payload —
invalidates the signature, because the signature was computed over the token's exact
original bytes. `parseAndValidate` in `JwtService` cannot return claims without this
check passing first; there is no code path that reads the payload while skipping
verification.

---

## 5. Expiration, reproduced for real

This lesson's tokens deliberately last **5 seconds** — long enough to demonstrate, short
enough to actually watch expire:

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/protected/profile   # 200, immediately
sleep 6
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/protected/profile   # 403, now expired
```

```
[JwtAuthenticationFilter] rejected: ExpiredJwtException - JWT expired 1625 milliseconds ago at 2026-09-13T06:41:46.000Z.
Current time: 2026-09-13T06:41:47.625Z. Allowed clock skew: 0 milliseconds.
```

A **different exception type** than the tampered-signature case — `ExpiredJwtException`
vs `SignatureException` — both handled identically by this lesson's filter (log and
leave the request unauthenticated), but genuinely distinct failures a real system might
want to log or monitor differently (an expired token is normal and expected; a bad
signature on an otherwise well-formed token is a much more suspicious signal).

---

## 6. Why this lesson's failures are `403`, not `401`

Every failure in this lesson — no token, a tampered token, an expired token — produces
`403 Forbidden`, not the `401 Unauthorized` lesson 40 used for a bad password. **This
is a real, verifiable consequence of removing `.httpBasic(...)`** — without it, there's
no `AuthenticationEntryPoint` registered to issue a `401` challenge (the thing that,
for HTTP Basic, also sends the `WWW-Authenticate` header prompting a browser or client
to retry with credentials). With no such mechanism configured — appropriate for an API
where the client already knows to attach a JWT and doesn't need a challenge — Spring
Security's default behavior for an unauthenticated request reaching `AuthorizationFilter`
is `403`. This isn't a bug in this lesson's setup; it's what a stateless, token-only API
actually looks like without a browser-facing login challenge in the mix.

---

## 7. Summary

- **`SessionCreationPolicy.STATELESS`** genuinely stops Spring Security from creating a
  session — verified here by a real response carrying no `Set-Cookie` header at all.
- **A JWT is issued once, at login**, then presented on every later request — no
  server-side record of it exists anywhere, which is the actual meaning of "stateless."
- **A JWT's header and payload are plainly readable by anyone holding the token** — it's
  signed, not encrypted. Never put a secret inside one.
- **The signature is what verification actually checks** — flipping even one character
  anywhere in the token produces a real `SignatureException`, caught and rejected before
  any claim is trusted.
- **Expiration is checked and reported as a distinct failure** (`ExpiredJwtException`)
  from a bad signature — both result in an unauthenticated request, but they mean
  different things operationally.
- **Removing `.httpBasic(...)` changes every failure to `403`** instead of `401` — a
  direct, verifiable consequence of there being no authentication challenge mechanism
  configured, appropriate for a token-only API.

---

**Previous:** [42 — Password encoding and `UserDetails`](../42-password-encoding-and-userdetails/42-password-encoding-and-userdetails.md) ·
**Next:** [44 — Method-level security](../44-method-level-security/44-method-level-security.md)
