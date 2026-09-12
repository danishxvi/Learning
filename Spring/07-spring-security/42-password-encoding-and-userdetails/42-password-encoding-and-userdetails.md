# 42 · Password encoding and `UserDetails`

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/07-spring-security/42-password-encoding-and-userdetails spring-boot:run
> ```

Lessons 40–41 called `encoder.encode(...)` without asking what it actually does or why
plain-text comparison would be a serious mistake. This lesson answers both, with real
measured numbers, and separates the *domain* concept of a user from what Spring
Security actually requires.

---

## 1. The same password, encoded twice — two different hashes

```java
String hash1 = encoder.encode("bob-pass");
String hash2 = encoder.encode("bob-pass");
```
```
hash1 = $2a$10$naQN9nlg.AK0q.S5PMV.XOW2NBFkOWU5ZAvUzsoZBrDB/j.ydhqVi
hash2 = $2a$10$jGznN66wTu7kg4pRR86n2uaaQjKtQpaNFoP4zSp19MsNjJeYpH4x2
hash1.equals(hash2)? false
encoder.matches("bob-pass", hash1) = true
encoder.matches("bob-pass", hash2) = true
```

**Two completely different strings, from the identical input — and both correctly
verify.** BCrypt embeds a random **salt** directly inside every hash it produces (the
`$2a$10$...` — the algorithm identifier, the work factor, then the salt, then the hash
itself, all in one string). Two users with the same password get unrelated hashes,
which is exactly why a leaked password database doesn't reveal "these five accounts
share a password" for free, and why `matches()` doesn't need the original salt passed
back in separately — it reads it back out of the stored hash itself before comparing.

This is also why a plain `String.equals()` on hashes, or a fast, unsalted algorithm
(MD5, SHA-256 used directly), is the wrong tool entirely: identical passwords would
produce identical hashes, immediately revealing which accounts share one, and — being
fast by design — would be crackable by brute force at billions of attempts per second on
modern hardware.

---

## 2. The work factor — a real, measured cost

```java
new BCryptPasswordEncoder(4).encode("bob-pass");    // strength 4
new BCryptPasswordEncoder(10).encode("bob-pass");   // strength 10 - Spring's default
new BCryptPasswordEncoder(12).encode("bob-pass");   // strength 12
```
```
strength 4  (weak, fast): 1 ms
strength 10 (Spring's default): 80 ms
strength 12 (stronger, slower): 326 ms
```

**BCrypt is deliberately slow, and the "strength" parameter controls exactly how
slow.** Each increment roughly doubles the cost (`2^strength` internal rounds) —
measured here, strength 10 takes **80x** longer than strength 4, and strength 12 over
**4x** longer again than strength 10. This is a genuine security property, not a
performance defect: a login endpoint only ever hashes one password per request, so 80ms
is invisible to a real user — but an attacker trying millions of guesses per second
against a leaked hash database is now limited to a few hundred *thousand* guesses per
second at best, purely because of this deliberate cost. Raising the work factor over
time (as hardware gets faster) is a real, standard practice — lowering it never is.

---

## 3. `DelegatingPasswordEncoder` — the `{bcrypt}` prefix, explained

```java
PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("bob-pass");
```
```
{bcrypt}$2a$10$ekeAM5kP9hXzXdg3FvNUw.jZFyJRQBbHs2jDkZwDEDlBNxReYStAe
```

Lessons 40–41 used exactly this encoder without explaining the prefix. `matches()`
reads `{bcrypt}` (or `{noop}`, `{sha256}`, and others) off the front of a stored hash
and dispatches to the matching algorithm's own `matches()` implementation — which is
what lets one system store some accounts hashed with an older algorithm and new ones
with BCrypt, correctly verifying either kind without knowing in advance which is which.
This is the standard, safe way to migrate a password hashing scheme over an
application's lifetime without a disruptive one-time rehash of every account.

---

## 4. Separating the domain object from `UserDetails`

```java
public class AppUserAccount {           // a plain domain object - no Spring Security import at all
    private final String username, passwordHash, role;
    private final boolean enabled;
}

public class AppUserPrincipal implements UserDetails {   // the ADAPTER
    private final AppUserAccount account;
    public String getUsername() { return account.getUsername(); }
    public boolean isEnabled() { return account.isEnabled(); }
    ...
}
```

`AppUserAccount` — what a real repository (section 05) would actually persist — has no
dependency on Spring Security's `UserDetails` interface at all. `AppUserPrincipal` is
the thin adapter translating between them. This separation matters the same way lesson
24's DTOs mattered: the domain layer stays framework-agnostic, and only one small class
would need to change if the security framework itself ever did.

---

## 5. `isEnabled()` and friends — authentication has more than one gate

```java
authenticate("bob", "bob-pass");        // -> SUCCESS
authenticate("bob", "wrong-password");  // -> BadCredentialsException: Bad credentials
authenticate("eve", "eve-pass");        // -> DisabledException: User is disabled
```

**`eve`'s password was correct.** `DisabledException`, not `BadCredentialsException` —
a genuinely different failure, checked separately from whether the password matches.
`UserDetails.isEnabled()` (along with `isAccountNonExpired()`, `isAccountNonLocked()`,
`isCredentialsNonExpired()`) is exactly the hook Spring Security calls to enforce this,
automatically, on every authentication attempt — no code in this lesson's controller or
security config checks any of these explicitly.

**Both failures still become the same generic `401` to an HTTP client**, deliberately —
distinguishing "wrong password" from "disabled account" in the actual HTTP response
would tell an attacker (or a curious user) more than a login endpoint should reveal. The
distinct exception types exist for server-side logging and internal handling, not for
the client to see.

---

## 6. Summary

- **BCrypt embeds a random salt in every hash it produces** — encoding the same password
  twice produces two different, unrelated strings, and `matches()` reads the salt back
  out of the stored hash to verify correctly.
- **The work factor is a real, measurable cost** — raising it is a legitimate response to
  faster hardware over time; this lesson measured `1ms` → `80ms` → `326ms` across
  strengths 4, 10, and 12.
- **`DelegatingPasswordEncoder`'s `{bcrypt}` prefix** lets `matches()` dispatch to the
  correct algorithm per stored hash — the mechanism behind migrating hash schemes
  without a disruptive mass rehash.
- **Keep the domain object free of `UserDetails`** — a thin adapter class translates
  between them, the same separation lesson 24 argued for entities and DTOs.
- **`isEnabled()`/`isAccountNonExpired()`/etc.** are separate gates Spring Security
  checks automatically, distinct from password correctness — both failure kinds
  deliberately collapse to the same generic `401` at the HTTP layer, even though the
  underlying exception types differ.

---

**Previous:** [41 — The security filter chain](../41-the-security-filter-chain/41-the-security-filter-chain.md) ·
**Next:** [43 — JWT-based stateless authentication](../43-jwt-based-stateless-authentication/43-jwt-based-stateless-authentication.md)
