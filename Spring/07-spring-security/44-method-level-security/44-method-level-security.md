# 44 · Method-level security

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/07-spring-security/44-method-level-security spring-boot:run
> ```

Every authorization rule so far (lessons 40–41) matched a **URL pattern**. This lesson
moves authorization onto the method itself — enforced identically whether the call
comes from a controller, a scheduled job, or another service with no HTTP request in
sight at all.

---

## 1. `@EnableMethodSecurity` — the switch

```java
@EnableMethodSecurity
@SpringBootApplication
public class MethodSecurityApplication { ... }
```

Without this, every `@PreAuthorize`/`@PostAuthorize`/`@PreFilter`/`@PostFilter`
annotation in `DocumentService` would be silently ignored — the same "declared but
never wired in" trap lesson 39's `@Aspect` and lesson 15's un-listed auto-configuration
both demonstrated. This annotation is what makes Spring wrap annotated beans in a proxy
(lesson 38's exact mechanism) that checks these expressions on every call.

---

## 2. `@PreAuthorize` — checked before the method runs

```java
@PreAuthorize("hasRole('ADMIN')")
public String deleteAllDocuments() { ... }
```

```
bob (USER) calling deleteAllDocuments()   -> AccessDeniedException: Access Denied
alice (ADMIN) calling deleteAllDocuments() -> All documents deleted (pretend).
```

**No HTTP request was involved anywhere in this call** — the demo sets an
`Authentication` directly on `SecurityContextHolder` and calls the method as plain
Java. `@PreAuthorize` doesn't care where the call came from; it evaluates its SpEL
expression against whatever `Authentication` is present and either lets the method run
or throws `AccessDeniedException` before a single line of the method body executes.

---

## 3. Referencing method parameters in the expression

```java
@PreAuthorize("#username == authentication.name")
public String getOwnProfile(String username) { ... }
```

```
bob requesting his OWN profile   -> Profile data for bob
bob requesting alice's profile   -> AccessDeniedException: Access Denied
```

`#username` binds to the method's own parameter by name; `authentication` is a built-in
SpEL variable referring to the current `Authentication`. This one line implements "you
may only look up your own data" — a genuinely common real-world rule — with no `if`
statement inside the method at all.

---

## 4. `@PostAuthorize` — checked on the *result*

```java
@PostAuthorize("returnObject.owner == authentication.name")
public Document getDocumentById(Long id) { ... }
```

```
bob fetching document #1 (his own)  -> Document{id=1, owner=bob, ...}
bob fetching document #3 (alice's)  -> AccessDeniedException: Access Denied
```

**The method body ran completely before this check happened** — `returnObject` is only
available *after* a `Document` has actually been fetched, because whether `bob` is
allowed to see it depends on data (`document.getOwner()`) that doesn't exist until the
fetch completes. `@PreAuthorize` could not express this rule at all; this is exactly
what `@PostAuthorize` exists for.

---

## 5. `@PostFilter` — and a genuine, reproduced pitfall

```java
@PostFilter("filterObject.owner == authentication.name")
public List<Document> getAllDocumentsThenFilter() {
    return allDocuments;   // an immutable List.of(...) - see what happened next
}
```

The first version of this method returned the shared `List.of(...)` field directly, and
running it crashed immediately:

```
UnsupportedOperationException
    at java.util.ImmutableCollections.uoe(...)
    at org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler.filterCollection(...)
```

**`@PostFilter` does not build a new filtered list — it calls `remove()`/`clear()` on
the returned collection itself, in place.** Passing back an immutable list throws the
instant Spring Security tries to remove a non-matching element from it. The fix is
returning a fresh, mutable copy every call:

```java
return new ArrayList<>(allDocuments);
```

```
bob sees: [Document{id=1, owner=bob, ...}, Document{id=2, owner=bob, ...}]
alice sees: [Document{id=3, owner=alice, ...}]
```

Each caller gets back only the documents they own — filtered *after* the method fetched
everything, which is fine for a small in-memory list. A real repository method
(section 05) would prefer pushing this filter into the query itself (a derived query's
`WHERE` clause, lesson 32) rather than fetching every row and discarding most of them.

---

## 6. Self-invocation defeats method security too

```java
public String deleteAllDocumentsViaSelfInvocation() {
    return this.deleteAllDocuments();   // plain Java call, same class
}
```

```
bob (USER) calling deleteAllDocumentsViaSelfInvocation() -> All documents deleted (pretend).
```

**`bob` — an ordinary `USER` — successfully invoked an admin-only operation.**
`@PreAuthorize` is enforced by the exact same AOP proxy mechanism lesson 38 built by
hand and lesson 33 showed defeating `@Transactional(REQUIRES_NEW)`: a call made via
`this.` from inside the same class never passes through the proxy wrapping the bean
from outside, so the annotation on the target method is never even consulted. Any
method that calls another `@PreAuthorize`-protected method internally must do so
through a real bean reference (a separate `@Service`, or `@Lazy` self-injection, lesson
08) — never through `this.` — or the check silently never happens.

---

## 7. Summary

- **`@EnableMethodSecurity`** turns on method-level annotation processing — without it,
  every annotation below is inert.
- **`@PreAuthorize`** checks before the method runs, and can reference the method's own
  parameters by name (`#paramName`) alongside the current `authentication`.
- **`@PostAuthorize`** checks after the method runs, against `returnObject` — the only
  option when the authorization decision depends on data the method itself produces.
- **`@PostFilter`** removes non-matching elements from a returned collection **by
  mutating it in place** — an immutable collection throws `UnsupportedOperationException`
  the moment it tries; always return a fresh, mutable copy.
- **Method security is completely independent of HTTP** — it applies to any call on an
  annotated bean, from anywhere, not just requests that arrived through
  `authorizeHttpRequests` (lessons 40–41).
- **Self-invocation bypasses `@PreAuthorize`/`@PostAuthorize` exactly as it bypasses
  `@Transactional`** — the same underlying proxy limitation, verified here by a plain
  `USER` account successfully calling an admin-only method through `this.`.

---

**Previous:** [43 — JWT-based stateless authentication](../43-jwt-based-stateless-authentication/43-jwt-based-stateless-authentication.md) ·
**Next:** [45 — CORS and CSRF](../45-cors-and-csrf/45-cors-and-csrf.md)
