# 57 - Application Events and `@EventListener`

Spring's `ApplicationEventPublisher` lets one part of the application publish
"something happened" without knowing or caring who, if anyone, is listening.
`@EventListener` is how a bean says "when that happens, run this." Every
behavior below - execution order, synchronous blocking, async dispatch,
transaction-aware delivery, and exception propagation - was measured with a
real event recorder, not assumed.

```bash
mvn -f Spring/11-events-and-messaging/57-application-events-and-eventlistener spring-boot:run
```

## The event and the publisher

[`OrderPlacedEvent.java`](src/main/java/com/danish/spring/events/OrderPlacedEvent.java)
is a plain record - Spring has allowed arbitrary objects as events since 4.2;
there's no need to extend the older `ApplicationEvent` base class. [`OrderService.java`](src/main/java/com/danish/spring/events/OrderService.java)
publishes one after saving an order:

```java
@Transactional
public void placeOrder(String orderId, BigDecimal amount, boolean failAfterPublish) {
    repository.save(new OrderRecord(orderId, amount));
    publisher.publishEvent(new OrderPlacedEvent(orderId, amount));
    if (failAfterPublish) {
        throw new RuntimeException("simulated failure after publishing event - transaction should roll back");
    }
}
```

Five separate listener beans react to `OrderPlacedEvent`, each demonstrating
one distinct piece of real behavior.

## Synchronous listeners run in `@Order`, and block the publisher

[`OrderedListenerA.java`](src/main/java/com/danish/spring/events/OrderedListenerA.java) /
[`OrderedListenerB.java`](src/main/java/com/danish/spring/events/OrderedListenerB.java) /
[`SlowSynchronousListener.java`](src/main/java/com/danish/spring/events/SlowSynchronousListener.java):

```java
@Order(1) @EventListener
public void onOrderPlaced(OrderPlacedEvent event) { recorder.record("A"); }

@Order(2) @EventListener
public void onOrderPlaced(OrderPlacedEvent event) { recorder.record("B"); }

@EventListener
public void onOrderPlaced(OrderPlacedEvent event) throws InterruptedException {
    Thread.sleep(300);
    recorder.record("slow-sync");
}
```

Real output:

```
SYNCHRONOUS vs @Async listeners, and @Order among synchronous ones
==========================================================================
  placeOrder() returned after 402ms
  entries recorded so far: [A@main, B@main, slow-sync@main, accounting-after-commit@main]
  ('async-email' not necessarily present yet - it runs on its own thread)
  after waiting 700ms for the async listener: [A@main, B@main, slow-sync@main, accounting-after-commit@main, async-email@event-async-1]
```

`A` ran before `B`, exactly matching `@Order(1)` before `@Order(2)`. All
three plain listeners ran on `main` - the same thread that called
`orderService.placeOrder(...)` - and `placeOrder()` itself took 402ms to
return, even though its own body does almost no work: the 300ms sleep inside
`slow-sync` genuinely blocked the caller, because plain `@EventListener`
methods run synchronously, inline, as part of the `publishEvent()` call.
Publishing an event is not "fire and forget" by default - it's a direct
method call to every matching listener, one after another.

## `@Async` listeners don't block the publisher

[`AsyncEmailListener.java`](src/main/java/com/danish/spring/events/AsyncEmailListener.java):

```java
@Async
@EventListener
public void onOrderPlaced(OrderPlacedEvent event) throws InterruptedException {
    Thread.sleep(500);
    recorder.record("async-email");
}
```

`@Async` on an `@EventListener` method (needs `@EnableAsync` - the same
mechanism as lesson 53) works exactly like `@Async` anywhere else: the method
is handed to an executor and `publishEvent()` moves on immediately without
waiting for it. In the output above, `async-email` was genuinely **not yet
present** in the recorded entries the moment `placeOrder()` returned - its
500ms sleep was still running on a separate thread. Only after the demo
explicitly waited 700ms did `async-email@event-async-1` show up, confirmed
running on the configured executor's own thread (`event-async-1`), not
`main`. This is the single biggest practical difference between a plain
`@EventListener` and an `@Async` one: a slow synchronous listener makes the
*publisher* slow; a slow async listener does not.

## `@TransactionalEventListener(AFTER_COMMIT)`: only fires if the transaction actually commits

[`AccountingAfterCommitListener.java`](src/main/java/com/danish/spring/events/AccountingAfterCommitListener.java):

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderPlaced(OrderPlacedEvent event) {
    recorder.record("accounting-after-commit");
}
```

In the first (successful) run above, `accounting-after-commit@main` appeared
in the recorded entries **immediately**, before even the async listener -
because `placeOrder()`'s transaction commits when the method returns
normally to its caller (`REQUIRED` propagation, no outer transaction), and
`AFTER_COMMIT` fires right at that moment, synchronously, still on `main`.

The real, valuable contrast comes from forcing a rollback. `OrderService.placeOrder`
accepts a `failAfterPublish` flag that throws *after* publishing the event:

```java
try {
    orderService.placeOrder("order-2", BigDecimal.valueOf(50), true);
} catch (RuntimeException ex) { ... }
```

Real output:

```
@TransactionalEventListener(AFTER_COMMIT) does NOT fire on rollback
==========================================================================
  placeOrder threw as expected: simulated failure after publishing event - transaction should roll back
  plain listeners that already ran before the rollback: [A@main, B@main, slow-sync@main]
  accounting-after-commit fired? false  (should be false - the transaction rolled back)
  order-2 actually persisted in the database? false  (should be false - proves the rollback was real, not just the event)
```

`A`, `B`, and `slow-sync` all ran anyway - they're plain `@EventListener`
methods, invoked synchronously the moment `publishEvent()` was called,
completely unaware of what the transaction will eventually do. But
`accounting-after-commit` never ran at all: its execution was deferred until
commit, and commit never happened - the exception thrown right after
`publishEvent()` rolled the transaction back instead, confirmed independently
by `order-2` genuinely not existing in the database afterward. **This is the
real reason `@TransactionalEventListener` exists**: a plain `@EventListener`
reacting to "order placed" has no way to know the order will actually be
rolled back a moment later - it would send an email, charge a fee, or update
an external system for an order that, from the database's point of view,
never happened. `AFTER_COMMIT` is how a listener opts into "only run if this
was real."

## An exception in one synchronous listener stops the rest from running

[`FirstRiskyListener.java`](src/main/java/com/danish/spring/events/FirstRiskyListener.java) /
[`SecondRiskyListener.java`](src/main/java/com/danish/spring/events/SecondRiskyListener.java):

```java
@Order(1) @EventListener
public void onRiskyEvent(RiskyEvent event) {
    recorder.record("risky-first");
    throw new RuntimeException("simulated failure in the first listener");
}

@Order(2) @EventListener
public void onRiskyEvent(RiskyEvent event) {
    recorder.record("risky-second");
}
```

Real output:

```
an exception in one synchronous listener stops the REST from running
==========================================================================
  publishEvent threw: simulated failure in the first listener
  risky-first ran? true
  risky-second ran? false  (should be false - it never got a chance to run)
```

Spring's default event multicaster invokes synchronous listeners one at a
time and does **not** catch exceptions between them: the exception thrown by
`FirstRiskyListener` (ordered to run first) propagated straight out of
`publisher.publishEvent(...)`, and `SecondRiskyListener` never ran at all.
This has a real practical consequence: if `OrderService.placeOrder` published
its event to several unrelated synchronous listeners (inventory, email,
shipping), a bug in the *first* one would silently prevent the *others* from
running too - not because they failed, but because they never got a turn.
This is a strong argument for keeping synchronous listeners narrow and
side-effect-safe, or moving genuinely independent reactions to `@Async`
listeners, which run on their own threads and can't block each other this way.

## Key takeaways

- `publisher.publishEvent(event)` invokes every matching plain
  `@EventListener` synchronously, in `@Order` sequence, on the calling
  thread - a slow or failing listener directly affects the code that
  published the event.
- `@Async` on an `@EventListener` method dispatches it to an executor and
  lets the publisher continue immediately - the listener's own duration no
  longer affects the publisher's.
- `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` defers
  execution until the surrounding transaction commits, and **never runs at
  all** if that transaction rolls back - the right tool for reactions that
  must not fire for work that never actually took effect, unlike a plain
  listener which fires unconditionally the moment the event is published.
- The default synchronous event multicaster does not isolate listeners from
  each other's exceptions: one listener throwing stops every listener
  registered after it (in `@Order`) from running at all, for that
  `publishEvent()` call.
