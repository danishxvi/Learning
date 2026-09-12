# 33 · Transactions and `@Transactional`

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/33-transactions-and-transactional spring-boot:run
> ```

Every service method in this section so far has been `@Transactional` with no
explanation of what that annotation actually decides. This lesson answers that with
five real scenarios — including two genuine surprises this exact annotation can produce
if its defaults aren't understood.

---

## 1. The default rollback rule: unchecked exceptions roll back

```java
@Transactional
public void transferAndCrash(Long fromId, Long toId, int amountCents) {
    move(fromId, toId, amountCents);              // debit + credit, via dirty checking
    throw new RuntimeException("simulated failure");
}
```

```
Before: Alice=10000, Bob=5000
Caught: simulated failure - watch both balances stay unchanged
After:  Alice=10000, Bob=5000  <-- unchanged, rollback happened
```

Both writes vanish. Spring's default rule: **roll back on any unchecked exception
(`RuntimeException` or `Error`)**. This is the behavior every earlier `@Transactional`
method in this stack has relied on implicitly.

---

## 2. The real surprise: checked exceptions do *not* roll back by default

```java
@Transactional
public void transferWithCheckedFailureDefaultRollback(Long fromId, Long toId, int amountCents)
        throws InsufficientFundsException {
    move(fromId, toId, amountCents);
    throw new InsufficientFundsException("simulated checked failure");
}
```

```
Before: Alice=10000, Bob=5000
Caught: simulated checked failure - watch balances CHANGE anyway
After:  Alice=8000, Bob=7000  <-- CHANGED, despite the exception! (the default rollback surprise)
```

**The transaction committed anyway.** `InsufficientFundsException extends Exception`,
not `RuntimeException` — and Spring's default rule only covers unchecked exceptions. The
reasoning behind this default: a checked exception is often modeled as an *expected*,
recoverable business outcome (declared right there in the method signature,
`throws InsufficientFundsException`), not a failure the transaction should undo. Whether
that's the right call for a given method is a judgment call the code has to make
explicitly — the annotation won't make it for you.

---

## 3. The fix: `rollbackFor`

```java
@Transactional(rollbackFor = InsufficientFundsException.class)
public void transferWithCheckedFailureExplicitRollback(...) throws InsufficientFundsException {
    move(fromId, toId, amountCents);
    throw new InsufficientFundsException("simulated checked failure");
}
```

```
Before: Alice=8000, Bob=7000
Caught: simulated checked failure - watch balances stay unchanged now
After:  Alice=8000, Bob=7000  <-- unchanged, rollbackFor fixed it
```

Same method body, one attribute added. Spring's own transaction manager log confirms
the rule was actually registered — the transaction's internal name includes it
explicitly:

```
Creating new transaction with name [...transferWithCheckedFailureExplicitRollback]:
PROPAGATION_REQUIRED,ISOLATION_DEFAULT,-com.danish.spring.transactions.InsufficientFundsException
```

**Any method that can throw a checked exception representing a genuine failure needs
`rollbackFor` named explicitly** — the default silently commits otherwise, exactly as
demonstrated above.

---

## 4. `REQUIRES_NEW` — a genuinely independent transaction, through a proper bean

```java
// AuditService - a SEPARATE bean
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(String message) { auditLogRepository.save(new AuditLogEntry(message)); }
```

```java
// TransferService
@Transactional
public void transferWithAuditViaProperBean(...) {
    move(fromId, toId, amountCents);
    auditService.log("Attempted transfer of " + amountCents + " from " + fromId + " to " + toId);
    throw new RuntimeException("simulated failure - the audit entry should SURVIVE this");
}
```

```
Audit entries before: 0
Caught: simulated failure - the audit entry should SURVIVE this
Audit entries after:  1  <-- the audit entry survived
Balances still unchanged: Alice=8000, Bob=7000
```

Spring's transaction manager log shows exactly what happened underneath:

```
Suspending current transaction, creating new transaction with name [...AuditService.log]
...
Resuming suspended transaction after completion of inner transaction
Initiating transaction rollback
```

`REQUIRES_NEW` genuinely **suspends** the caller's transaction, runs `log()` in a brand
new one that commits independently, then resumes the original — which then rolls back
on its own, from the `RuntimeException` thrown afterward. The audit entry survives
because it was never part of the transaction that failed.

---

## 5. The self-invocation trap — `REQUIRES_NEW` silently ignored

```java
@Transactional
public void transferWithAuditViaSelfInvocation(...) {
    move(fromId, toId, amountCents);
    this.recordAttemptViaSelf("Attempted transfer of ...");   // SAME class, plain method call
    throw new RuntimeException("simulated failure - the 'REQUIRES_NEW' audit entry will NOT survive this");
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordAttemptViaSelf(String message) { auditLogRepository.save(new AuditLogEntry(message)); }
```

```
Audit entries before: 1
Caught: simulated failure - the 'REQUIRES_NEW' audit entry will NOT survive this
Audit entries after:  1  <-- unchanged - self-invocation defeated REQUIRES_NEW
```

**No new entry.** The transaction manager's own log confirms why — compare this call to
`AuditService.log()` above:

```
Participating in existing transaction
```

**No "Suspending current transaction" line at all.** `this.recordAttemptViaSelf(...)` is
a plain Java method call on the same object — it never goes through the Spring proxy
that makes `@Transactional` (or `@Cacheable`, or any other AOP-based annotation) work in
the first place. This is the exact same proxy mechanism lesson 09 demonstrated for
`@Configuration` and lesson 31 demonstrated for repositories — and here, bypassing it
means the `@Transactional(REQUIRES_NEW)` annotation on `recordAttemptViaSelf` is simply
never seen; the call just executes as ordinary code inside the caller's existing
transaction, and gets rolled back with everything else.

**The fix**: call through a separate bean (as `AuditService.log()` does), or inject the
class into itself (the `@Lazy` self-injection pattern from lesson 08) so the call goes
through the proxy. Self-invocation silently defeating `@Transactional` is one of the
most common real bugs involving this annotation — it produces no error, no warning, just
a transaction boundary that quietly isn't where the code implies it is.

---

## 6. Summary

- **`@Transactional`** with no arguments defaults to `Propagation.REQUIRED` and rolls
  back on unchecked exceptions (`RuntimeException`/`Error`) only.
- **Checked exceptions do not trigger rollback by default** — a real, easy-to-miss
  surprise that silently commits partial work unless `rollbackFor` names that exception
  type explicitly.
- **`Propagation.REQUIRES_NEW`** suspends the caller's transaction and runs the method in
  a genuinely independent one that commits or rolls back on its own — the standard shape
  for an audit trail that should survive the operation it's recording failing.
- **`REQUIRES_NEW` (or any `@Transactional` attribute) is silently ignored on
  self-invocation** — calling `this.method()` within the same class bypasses the Spring
  proxy entirely, so the call just runs inside whatever transaction is already active.
  Call through a separate bean, or a `@Lazy` self-injected reference, to make it real.

---

**Previous:** [32 — Derived and custom queries](../32-derived-and-custom-queries/32-derived-and-custom-queries.md) ·
**Next:** [34 — Pagination and projections](../34-pagination-and-projections/34-pagination-and-projections.md)
