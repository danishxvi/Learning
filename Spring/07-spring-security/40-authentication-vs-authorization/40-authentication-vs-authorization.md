# 40 · Authentication vs authorization

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/07-spring-security/40-authentication-vs-authorization spring-boot:run
> ```

These two words get used almost interchangeably in casual conversation, and they mean
genuinely different things. This lesson makes the difference impossible to confuse
again: two different HTTP status codes, produced by two different failures.

---

## 1. Adding the starter alone locks down everything

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

With **no** `SecurityConfig` class at all, starting the app prints a real, unedited log
line:

```
Using generated security password: 5ecbbdbc-6996-48b4-b7ec-a93e391049af
```

A random password, generated fresh on every startup, for a single default user named
`user`. Every endpoint now requires it:

```bash
curl http://localhost:8080/public/hello                              # 401
curl -u user:5ecbbdbc-6996-48b4-b7ec-a93e391049af http://localhost:8080/public/hello  # 200
curl -u user:5ecbbdbc-6996-48b4-b7ec-a93e391049af http://localhost:8080/admin/dashboard # 200 - ALSO!
```

**Zero-config Spring Security is all-or-nothing** — the same `user` reaches
`/admin/dashboard` just as easily as `/public/hello`. This default has no concept of
roles or permissions at all; it only answers one question — "is this request
authenticated" — which is exactly **authentication**, and exactly why it isn't enough on
its own for anything beyond a quick local demo.

---

## 2. Authentication: "who are you"

```java
.requestMatchers("/public/**").permitAll()
```

Adding real configuration and two real users:

```java
User.withUsername("bob").password(encoder.encode("bob-pass")).roles("USER").build()
User.withUsername("alice").password(encoder.encode("alice-pass")).roles("USER", "ADMIN").build()
```

```bash
curl http://localhost:8080/user/profile                    # 401 - no credentials at all
curl -u bob:bob-pass http://localhost:8080/user/profile     # 200 - identity confirmed
curl -u alice:wrong-pass http://localhost:8080/admin/dashboard  # 401 - identity NOT confirmed
```

**`401 Unauthorized`** (a genuinely confusing HTTP status name — it really means
"unauthenticated") is what authentication failing looks like: no credentials, or
credentials that don't match any known identity. The request never gets far enough to
ask what that identity is *allowed* to do.

---

## 3. Authorization: "what are you allowed to do"

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

```bash
curl -u bob:bob-pass http://localhost:8080/admin/dashboard    # 403 - authenticated, WRONG role
curl -u alice:alice-pass http://localhost:8080/admin/dashboard # 200 - authenticated, RIGHT role
```

**`bob` successfully authenticates** — his password is correct, Spring Security knows
exactly who he is — **and is still refused**, with a different status entirely:
**`403 Forbidden`**. This is authorization failing on its own, independent of
authentication: bob proved who he is; who he is just isn't allowed here. `alice`, with
the same mechanism and the same endpoint, succeeds — the only difference between her
request and bob's is the `ADMIN` role attached to her account, not anything about
whether either of them is "logged in."

---

## 4. The distinction, side by side

| Status | Question it answers | This lesson's example |
| --- | --- | --- |
| `401 Unauthorized` | Do we know who you are? | No credentials, or wrong password |
| `403 Forbidden` | Do we know who you are, AND are you allowed here? | Correct password, wrong role |
| `200 OK` | Both — a real identity, with sufficient permission | `alice` on `/admin/dashboard` |

A client receiving `401` should prompt for different or corrected credentials; a client
receiving `403` should not — the credentials are fine, the account simply lacks
permission, and asking for a *different password* fixes nothing. Conflating the two
(returning `403` for a login failure, or `401` for a permissions failure) is a real,
common API design mistake this distinction exists specifically to prevent.

---

## 5. Summary

- **Adding `spring-boot-starter-security` alone** locks every endpoint behind a single
  generated password, printed at startup — an all-or-nothing default with no concept of
  roles.
- **Authentication** answers "who are you" — failing it is `401 Unauthorized`, whether
  from missing or incorrect credentials.
- **Authorization** answers "what are you allowed to do," and only matters *after*
  authentication succeeds — failing it is `403 Forbidden`.
- **The same user, same password, same endpoint mechanism can produce both outcomes** —
  the only variable in this lesson's `403` case was `bob`'s role, not his identity.
- Never conflate the two status codes: `401` invites the client to try different
  credentials; `403` means the credentials were fine and trying again won't help.

---

**Previous:** [39 — Aspects, pointcuts and advice](../../06-aspect-oriented-programming/39-aspects-pointcuts-and-advice/39-aspects-pointcuts-and-advice.md) ·
**Next:** [41 — The security filter chain](../41-the-security-filter-chain/41-the-security-filter-chain.md)
