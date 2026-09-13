# 52 - Optimistic and Pessimistic Locking

Two different answers to the same question: "two transactions want to modify
the same row - how do we stop one of them from silently overwriting the
other's work?"

- **Optimistic locking** assumes conflicts are rare: let both transactions
  read freely, and only check for a conflict when one tries to write. If the
  row changed underneath it, fail loudly instead of overwriting.
- **Pessimistic locking** assumes conflicts are common enough to prevent
  outright: take a real database lock the moment you read the row, so nobody
  else can even start writing to it until you're done.

Both were run against a real H2 database with real concurrent threads.

```bash
mvn -f Spring/09-transactions-async-and-scheduling/52-optimistic-and-pessimistic-locking spring-boot:run
```

## The domain

A `Product` (id, name, stock) - see
[`Product.java`](src/main/java/com/danish/spring/locking/Product.java) - with
one extra field:

```java
@Version
private Long version;
```

`@Version` tells Hibernate to maintain this column itself: every `UPDATE`
Hibernate generates for this entity includes `WHERE id = ? AND version = ?`
(the version it read), and sets `version = version + 1`. If some other
transaction already bumped the version, that `WHERE` clause matches zero rows
- and Hibernate treats "the UPDATE I issued matched 0 rows" as proof someone
else got there first.

## Optimistic locking: two transactions read the same version, both write

[`OptimisticLockingService.java`](src/main/java/com/danish/spring/locking/OptimisticLockingService.java):

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void readThenUpdateAfterSignal(Long productId, int newStock, CountDownLatch readDone, CountDownLatch okToUpdate)
        throws InterruptedException {
    Product product = productRepository.findById(productId).orElseThrow();
    readDone.countDown();
    okToUpdate.await(5, TimeUnit.SECONDS);
    product.setStock(newStock);
}
```

No explicit `save()` call - `product` is a managed entity inside this
transaction's persistence context, so Hibernate's dirty checking (lesson 29)
picks up the `setStock` change and flushes it, versioned `UPDATE` included, at
commit.

The test harness starts a `Product(stock=100)` at `version=0`, then runs two
separate `REQUIRES_NEW` transactions on two threads, using latches to force
both to read the row *before either writes*:

```java
Product product = productRepository.save(new Product("widget", 100));
// ... two threads call readThenUpdateAfterSignal, each on its own latch pair ...
readDoneA.await();
readDoneB.await();               // both have now read version=0
okToUpdateA.countDown();
futureA.get();                   // A commits: stock=90, version becomes 1
okToUpdateB.countDown();
futureB.get();                   // B tries to commit its stale version=0 read
```

Real output:

```
OPTIMISTIC LOCKING - two transactions read the same version, both write
==========================================================================
  initial: stock=100, version=0
  both transactions have now read version=0 into memory
  transaction A committed stock=90 - 1 is the new version
  transaction B failed: ObjectOptimisticLockingFailureException: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect) : [com.danish.spring.locking.Product#1]
  final: stock=90, version=1
```

Transaction A commits cleanly: it read `version=0`, its `UPDATE ... WHERE
version=0` matches the one row, and the version becomes `1`. Transaction B
still has `version=0` in its in-memory copy (it read before A committed), so
its own `UPDATE ... WHERE version=0` at commit time matches **zero** rows -
Hibernate detects that and throws `ObjectOptimisticLockingFailureException`
instead of letting a zero-row update pass silently. B's write to `stock` is
lost, which is exactly the point: it's lost *loudly*, as an exception the
caller must handle (retry the whole operation with a fresh read, surface a
"someone else changed this, please retry" error to a user, etc.) - not
silently, as `stock` quietly reverting to a stale value.

## Pessimistic locking: a second reader blocks until the first commits

[`ProductRepository.java`](src/main/java/com/danish/spring/locking/ProductRepository.java)
adds a lookup that takes a real lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from Product p where p.id = :id")
Optional<Product> findByIdForUpdate(@Param("id") Long id);
```

`PESSIMISTIC_WRITE` compiles to `SELECT ... FOR UPDATE` - a real row lock,
held for the rest of the transaction. [`PessimisticLockingService.java`](src/main/java/com/danish/spring/locking/PessimisticLockingService.java):

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void lockUpdateAndHoldUntilSignalled(Long productId, int newStock, CountDownLatch lockAcquired, CountDownLatch okToRelease)
        throws InterruptedException {
    Product product = productRepository.findByIdForUpdate(productId).orElseThrow();
    lockAcquired.countDown();
    okToRelease.await(5, TimeUnit.SECONDS);
    product.setStock(newStock);
}
```

The test harness starts transaction A, waits for it to confirm it holds the
lock, *then* submits transaction B against the same row on a second thread,
times how long B's `findByIdForUpdate` call takes to return, holds A open for
1500ms, and only then releases A:

```java
lockAcquiredA.await();                       // A holds the lock
long beforeB = System.currentTimeMillis();
Future<?> futureB = executor.submit(() -> pessimisticLockingService
        .lockUpdateAndHoldUntilSignalled(product.getId(), 300, lockAcquiredB, okToReleaseB));
Thread.sleep(1500);
okToReleaseA.countDown();
futureA.get();                               // A commits, releasing the lock
lockAcquiredB.await();
long blockedForMs = System.currentTimeMillis() - beforeB;
```

Real output:

```
PESSIMISTIC LOCKING - a second reader BLOCKS until the first commits
==========================================================================
  initial: stock=100
  [main] transaction A holds the PESSIMISTIC_WRITE lock
  [main] transaction B submitted - it wants the SAME lock and should now block
  [main] holding A's lock for 1500ms before releasing it
  [main] transaction A committed and released the lock
  [main] transaction B finally acquired the lock after 1507ms of blocking
  final: stock=300 (B's write, applied after A's)
```

B's `findByIdForUpdate` call didn't return instantly and then race A for the
write - it genuinely **blocked inside the database** for the full ~1500ms A
held the lock (1507ms measured, matching A's hold time almost exactly), and
only returned once A's transaction committed and released it. There is no
conflict to detect after the fact here, unlike the optimistic case - B simply
could not start its own write until A was completely done, so both writes
apply in strict sequence and the final `stock=300` is B's value with no
exception anywhere.

## Choosing between them

- **Optimistic** costs nothing while transactions are just reading, and only
  pays a price (a thrown exception, a retry) when a real conflict happens. Use
  it when conflicts are rare and you're comfortable handling "someone else
  already changed this" as an occasional, expected error - e.g. a user editing
  a record that's rarely edited concurrently.
- **Pessimistic** guarantees no conflict can ever happen, because it prevents
  the second writer from even starting - at the cost of making every other
  transaction on that row wait, even when they would have succeeded anyway.
  Use it when conflicts are common enough, or expensive enough to unwind,
  that blocking is cheaper than retrying - e.g. decrementing a shared
  inventory count under real contention.
- Both need `@Transactional` and a real transaction boundary to mean anything
  - `@Version` is checked at flush/commit time, and `PESSIMISTIC_WRITE` is
  held until commit/rollback; neither does anything useful outside a
  transaction.

## Key takeaways

- `@Version` adds a version column Hibernate manages itself, turning every
  `UPDATE` into a conditional one (`WHERE version = <the version I read>`) so
  a stale write fails instead of silently overwriting newer data.
- A failed optimistic write throws `ObjectOptimisticLockingFailureException`
  at flush/commit time - not at `setStock()` time, and not at read time.
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` on a repository query issues a real
  `SELECT ... FOR UPDATE`, blocking any other transaction that tries to
  acquire the same lock until this transaction ends.
- Optimistic locking fails fast and loud on conflict; pessimistic locking
  prevents the conflict from being possible at all, in exchange for making
  other transactions wait.
