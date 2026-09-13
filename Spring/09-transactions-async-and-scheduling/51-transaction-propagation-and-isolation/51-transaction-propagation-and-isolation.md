# 51 - Transaction Propagation and Isolation

Two independent questions `@Transactional` answers, and this lesson keeps them
separate on purpose:

- **Propagation** - when this method is called, should it join an existing
  transaction, start a new one, require one to already exist, forbid one from
  existing, or run inside a savepoint of the current one?
- **Isolation** - while a transaction is running, how much of the concurrent
  work of *other* transactions is it allowed to see?

Everything below was actually compiled and run against a real H2 database with
real concurrent threads (`CountDownLatch` + `ExecutorService`, not mocks or
sleeps). Run it yourself:

```bash
mvn -f Spring/09-transactions-async-and-scheduling/51-transaction-propagation-and-isolation spring-boot:run
```

## The domain

An `Account` (id, owner, balance in cents) and an `AuditLogEntry` (id,
message) - two plain JPA entities, two plain `JpaRepository` interfaces. See
[`Account.java`](src/main/java/com/danish/spring/txpropagation/Account.java),
[`AuditLogEntry.java`](src/main/java/com/danish/spring/txpropagation/AuditLogEntry.java).

## Propagation: MANDATORY and NEVER (these work exactly as documented)

[`AuditService.java`](src/main/java/com/danish/spring/txpropagation/AuditService.java)
declares two propagation extremes:

```java
@Transactional(propagation = Propagation.MANDATORY)
public void mustRunInsideAnExistingTransaction() { ... }

@Transactional(propagation = Propagation.NEVER)
public void mustNeverRunInsideATransaction() { ... }
```

- `MANDATORY` refuses to run unless a transaction is *already* active. Unlike
  the default `REQUIRED` (join one, or start one), this is a way of saying
  "calling this with no transaction in progress is a caller bug, not
  something to silently paper over."
- `NEVER` is the mirror image: it refuses to run *inside* an active
  transaction. Genuinely rare, but real - some DDL and locking statements on
  some databases are only valid outside a transaction.

[`PlainCaller.java`](src/main/java/com/danish/spring/txpropagation/PlainCaller.java)
has **no** `@Transactional` anywhere, so it calls `mustRunInsideAnExistingTransaction()`
with genuinely no transaction active. Real result:

```
MANDATORY - called with NO transaction active
==========================================================================
  IllegalTransactionStateException: No existing transaction found for transaction marked with propagation 'mandatory'
```

[`PropagationService.callNeverPropagationFromInsideATransaction()`](src/main/java/com/danish/spring/txpropagation/PropagationService.java)
is itself `@Transactional`, so it calls `mustNeverRunInsideATransaction()`
from inside an active transaction. Real result:

```
NEVER - called FROM WITHIN an active transaction
==========================================================================
  IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'
```

Both are the exact same exception type, `IllegalTransactionStateException`,
thrown for opposite reasons - Spring checks "is there a transaction?" against
each method's declared expectation before the method body ever runs.

## Propagation: NESTED (this one does NOT work here - and that is the lesson)

The plan was to show the textbook NESTED behavior: a savepoint taken before
the nested method runs, so that if the nested method fails, only *its* work
rolls back while the outer transaction's work survives and can still commit.

```java
@Transactional(propagation = Propagation.NESTED)
public void recordNested(String message, boolean shouldFail) {
    auditLogRepository.save(new AuditLogEntry(message));
    if (shouldFail) {
        throw new RuntimeException("simulated audit failure - only THIS savepoint's work should roll back");
    }
}
```

called from an outer `@Transactional` method that updates an `Account`
balance first, then attempts the nested audit write, catching whatever it
throws:

```java
@Transactional
public void updateBalanceWithNestedAudit(Long accountId, int newBalance, boolean failAudit) {
    Account account = accountRepository.findById(accountId).orElseThrow();
    account.setBalanceCents(newBalance);
    try {
        auditService.recordNested("Balance changed to " + newBalance, failAudit);
    } catch (RuntimeException ex) {
        System.out.println("  Caught nested failure: " + ex.getMessage() + " - outer transaction continues");
    }
}
```

Running this against both `failAudit = true` and `failAudit = false` produced
the **same** real output both times:

```
NESTED - intended: a failing savepoint should NOT undo the outer transaction
==========================================================================
  Caught nested failure: JpaDialect does not support savepoints - check your JPA provider's capabilities - outer transaction continues
  Balance after (outer survives, but not via a savepoint - see below): 9000
  Audit log entries after (NESTED never ran at all): 0

NESTED - intended: a succeeding savepoint should commit with the outer tx
==========================================================================
  Caught nested failure: JpaDialect does not support savepoints - check your JPA provider's capabilities - outer transaction continues
  Balance after: 8000
  Audit log entries after (still never ran - same rejection either way): 0
```

**What actually happened, and why:** `recordNested` never even got to run its
body. Spring's `AbstractPlatformTransactionManager` checks, before starting a
`NESTED` transaction, whether the underlying `PlatformTransactionManager` can
actually provide a savepoint. This app uses `JpaTransactionManager` (the
default when you have `spring-boot-starter-data-jpa` and no separate
`DataSource`-based transaction manager configured), and `JpaTransactionManager`
delegates that capability check to its `JpaDialect`
(`HibernateJpaDialect` here) via `supportsSavepoints()` - which returns
`false`. JPA itself has no standard savepoint API; `EntityManager` has no
`setSavepoint()` method the way `java.sql.Connection` does. So the *exact same*
`RuntimeException` message - `JpaDialect does not support savepoints` - fires
regardless of `shouldFail`, before the method body's `save()` call or the
`throw` ever execute. That is why the audit log count is `0` in **both** runs,
not just the failing one: neither call ever got to insert anything.

The balance survives in both cases (`9000` then `8000`), which is real, but
for a boring reason that has nothing to do with savepoints: the caught
`RuntimeException` just never propagates up past the `try/catch` in
`updateBalanceWithNestedAudit`, so the outer `@Transactional` method finishes
normally and commits like any other successful method call would.

**The actual, useful takeaway:** `Propagation.NESTED` is not a JPA-transaction
feature. It only does anything with a transaction manager that manages a raw
JDBC `Connection` directly and can call `Connection.setSavepoint()` - i.e.
`DataSourceTransactionManager`, not `JpaTransactionManager`. A Spring Data JPA
application (the overwhelmingly common case, and everything built in this
curriculum since lesson 07) almost never has real access to `NESTED`. If you
see `@Transactional(propagation = Propagation.NESTED)` in JPA code, either the
author hasn't hit this exception yet, or the project has deliberately wired a
JDBC-based transaction manager alongside JPA specifically to get savepoint
support (uncommon, and outside this lesson's scope). In practice, `REQUIRES_NEW`
(lesson 33) - a genuinely separate transaction and connection, not a savepoint
- is almost always the right tool when you want partial-failure isolation
inside JPA code.

## Isolation: does a second read see another transaction's concurrent commit?

[`IsolationService.java`](src/main/java/com/danish/spring/txpropagation/IsolationService.java)
reads an account's balance, waits for a signal, then reads it again -
comparing that under `READ_COMMITTED` vs. `REPEATABLE_READ`. `@Transactional`'s
`isolation` attribute must be a compile-time constant, so it can't be a method
parameter; the fix is two thin methods with fixed isolation levels delegating
to one shared private method:

```java
@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
public int[] readTwiceUnderReadCommitted(Long accountId, CountDownLatch firstReadDone, CountDownLatch okToReadAgain)
        throws InterruptedException {
    return readTwice(accountId, firstReadDone, okToReadAgain);
}

@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
public int[] readTwiceUnderRepeatableRead(Long accountId, CountDownLatch firstReadDone, CountDownLatch okToReadAgain)
        throws InterruptedException {
    return readTwice(accountId, firstReadDone, okToReadAgain);
}

private int[] readTwice(Long accountId, CountDownLatch firstReadDone, CountDownLatch okToReadAgain)
        throws InterruptedException {
    int first = accountRepository.findById(accountId).orElseThrow().getBalanceCents();
    firstReadDone.countDown();
    okToReadAgain.await(5, TimeUnit.SECONDS);
    entityManager.clear(); // bypass the first-level cache (lesson 29) - force a real re-query
    int second = accountRepository.findById(accountId).orElseThrow().getBalanceCents();
    return new int[]{first, second};
}
```

`entityManager.clear()` matters: without it, the second `findById` would just
return the same cached Java object from the first read - the persistence
context (lesson 29's first-level cache) would short-circuit the whole
experiment regardless of isolation level.

The test harness runs this on a background thread while the main thread
updates and commits the balance from a **separate** `REQUIRES_NEW` transaction
in between the two reads, using two `CountDownLatch`es to guarantee ordering
deterministically (no `Thread.sleep` guessing):

```java
Future<int[]> future = executor.submit(() -> readCommitted
        ? isolationService.readTwiceUnderReadCommitted(accountId, firstReadDone, okToReadAgain)
        : isolationService.readTwiceUnderRepeatableRead(accountId, firstReadDone, okToReadAgain));
firstReadDone.await();
isolationService.updateBalanceInOwnTransaction(accountId, updatedTo);
okToReadAgain.countDown();
int[] results = future.get();
```

Real output:

```
ISOLATION - READ_COMMITTED: does a second read see a concurrent commit?
==========================================================================
  [main thread] first read finished - now updating and committing from ANOTHER transaction
  [main thread] update committed - releasing the reading transaction to read again
  first read=8000, second read=7500  (DIFFERENT - non-repeatable read)

ISOLATION - REPEATABLE_READ: does a second read see a concurrent commit?
==========================================================================
  [main thread] first read finished - now updating and committing from ANOTHER transaction
  [main thread] update committed - releasing the reading transaction to read again
  first read=7500, second read=7500  (SAME - isolation held)
```

Under `READ_COMMITTED`, the second read sees the value the other transaction
just committed - a **non-repeatable read**: the same query, run twice in the
same transaction, gives two different answers because a *different*
transaction's commit landed in between. Under `REPEATABLE_READ`, the database
guarantees the second read matches the first, even though the other
transaction's commit genuinely happened and genuinely succeeded - H2 achieves
this by holding the row's earlier snapshot (or a read lock, depending on
engine) for the duration of the reading transaction.

## Running it yourself

```bash
mvn -f Spring/09-transactions-async-and-scheduling/51-transaction-propagation-and-isolation spring-boot:run
```

## Key takeaways

- `MANDATORY` and `NEVER` are opposite guardrails, both enforced by a simple
  "is a transaction currently active?" check before the method body runs, and
  both throw the same `IllegalTransactionStateException` type for their
  respective violations.
- `NESTED` requires real JDBC savepoint support from the transaction manager.
  `JpaTransactionManager` (the default for Spring Data JPA apps) reports
  `supportsSavepoints() == false` via its `JpaDialect`, so `NESTED` fails
  before the method body runs, for both success and failure paths alike. Use
  `REQUIRES_NEW` for partial-failure isolation in JPA code instead.
- `@Transactional`'s `isolation` (and `propagation`) attributes are
  compile-time constants - they cannot be parameterized at runtime. Split into
  separate annotated methods delegating to shared logic when you need to
  compare levels.
- `READ_COMMITTED` allows non-repeatable reads: a second read in the same
  transaction can see another transaction's commit that landed in between.
  `REPEATABLE_READ` prevents this, at the cost of holding more state (locks or
  snapshots) for the duration of the transaction.
- `entityManager.clear()` is required to make a "read again" test meaningful
  at all - otherwise the first-level cache (lesson 29) returns the same cached
  object without ever asking the database a second time.
