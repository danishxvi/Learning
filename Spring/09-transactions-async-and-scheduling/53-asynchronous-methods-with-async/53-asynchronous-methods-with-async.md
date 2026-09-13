# 53 - Asynchronous Methods with `@Async`

`@Async` moves a method's execution off the calling thread and onto a thread
pool, so the caller can keep going without waiting for it to finish. Every
scenario below was actually run - real threads, real timing, real exceptions.

```bash
mvn -f Spring/09-transactions-async-and-scheduling/53-asynchronous-methods-with-async spring-boot:run
```

## Turning it on: `@EnableAsync` and a real executor

[`AsyncConfig.java`](src/main/java/com/danish/spring/async/AsyncConfig.java):

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setThreadNamePrefix("async-demo-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return capturingAsyncExceptionHandler;
    }
}
```

`@Async` is inert without `@EnableAsync` - that annotation is what makes
Spring wrap `@Async`-annotated beans in a proxy (the same proxy mechanism
`@Transactional` uses - lesson 38's cross-cutting-concerns proxy problem)
that intercepts the call and hands the actual method body to an `Executor`
instead of running it inline. Implementing `AsyncConfigurer` lets this app
supply its own thread pool (named `async-demo-N` so the demo output can prove
which thread ran what) instead of Spring's plain, unbounded default.

## `CompletableFuture<T>`: calls genuinely overlap in time

[`NotificationService.java`](src/main/java/com/danish/spring/async/NotificationService.java):

```java
@Async
public CompletableFuture<String> sendEmailAsync(String to, long delayMs) throws InterruptedException {
    Thread.sleep(delayMs);
    return CompletableFuture.completedFuture(to + " notified by " + Thread.currentThread().getName());
}
```

Three calls, each simulating 1000ms of work, fired without waiting between
them:

```java
CompletableFuture<String> f1 = notificationService.sendEmailAsync("alice@example.com", 1000);
CompletableFuture<String> f2 = notificationService.sendEmailAsync("bob@example.com", 1000);
CompletableFuture<String> f3 = notificationService.sendEmailAsync("carol@example.com", 1000);
CompletableFuture.allOf(f1, f2, f3).get(5, TimeUnit.SECONDS);
```

Real output:

```
@Async runs on a DIFFERENT thread, and calls genuinely overlap in time
==========================================================================
  [main] calling thread is main
  [main] all three calls returned immediately - elapsed so far: 15ms
  alice@example.com notified by async-demo-1
  bob@example.com notified by async-demo-2
  carol@example.com notified by async-demo-3
  total elapsed: 1032ms for three 1000ms tasks  (ran IN PARALLEL, not sequentially)
```

All three calls returned to the caller in 15ms - they didn't block for even
one of the 1000ms sleeps. Each ran on its own `async-demo-N` thread from the
pool, not on `main`. And the total wall-clock time for all three to finish was
~1032ms, not ~3000ms: if these had run sequentially on one thread, three
1000ms sleeps would take three seconds. They ran in parallel because the pool
has 4 core threads and only 3 tasks were submitted, so all three started
essentially at once.

## A `void @Async` method's exception has nowhere to go but the handler

```java
@Async
public void sendEmailFireAndForget(String to) {
    throw new RuntimeException("simulated failure sending to " + to);
}
```

A `void` async method gives the caller nothing back - no `Future`, no return
value, nothing to call `.get()` on and catch an exception from. By the time
this method's body starts running on a pool thread, the caller's stack frame
that invoked it is long gone. Real output:

```
a void @Async method's exception has nowhere to go but the handler
==========================================================================
  [main] the call above already returned - no exception was thrown here
  handler captured 1 exception(s):
    sendEmailFireAndForget -> simulated failure sending to dave@example.com
```

The exception did NOT surface at the call site - `notificationService.sendEmailFireAndForget(...)`
returned normally, with nothing thrown, because it had already returned
before the method body even ran. The only place Spring can hand this
exception to is the `AsyncUncaughtExceptionHandler` configured in
`AsyncConfig` - [`CapturingAsyncExceptionHandler.java`](src/main/java/com/danish/spring/async/CapturingAsyncExceptionHandler.java)
recorded it, proving the exception genuinely happened and genuinely was
caught somewhere, just never at the call site. Without a handler like this,
Spring's default just logs the exception and moves on - a fire-and-forget
`@Async void` method's failure is easy to lose entirely if nothing is set up
to catch it.

## Self-invocation silently bypasses `@Async`

[`SelfInvokingService.java`](src/main/java/com/danish/spring/async/SelfInvokingService.java):

```java
@Async
public CompletableFuture<String> asyncMethod() {
    return CompletableFuture.completedFuture(Thread.currentThread().getName());
}

public String callAsyncMethodViaThis() throws ExecutionException, InterruptedException {
    return asyncMethod().get();
}
```

`callAsyncMethodViaThis()` calls `asyncMethod()` as a plain, unqualified
method call on `this` - not through the Spring-managed proxy that wraps this
bean everywhere else in the application. Real output:

```
self-invocation bypasses the @Async proxy entirely
==========================================================================
  [main] calling thread:              main
  "async" method actually ran on:     main
  SAME thread - @Async was silently ignored due to self-invocation
```

Both lines say `main`. `asyncMethod()`'s body ran on the exact same thread
that called `callAsyncMethodViaThis()` - `@Async` had zero effect. This is
the same proxy limitation covered for `@Transactional` in lesson 38: any
Spring AOP annotation (`@Async`, `@Transactional`, `@Cacheable`, custom
aspects) only takes effect when the call arrives through the proxy Spring
wraps around the bean. A call from *outside* the bean (through the injected
proxy reference) goes through the proxy and works. A call from *inside* the
bean to one of its own `@Async` methods is a plain Java virtual method call
that never touches the proxy, so it silently runs synchronously instead -
with no exception, no warning, nothing to indicate `@Async` was ignored. The
usual fix is to move the async method to a separate bean and inject that bean
(exactly like `AuditService` was a separate bean from `PropagationService` in
lesson 51, for the same underlying proxy reason).

## Key takeaways

- `@Async` needs `@EnableAsync` to do anything; without it, the annotation is
  inert and the method runs synchronously like any other.
- An `@Async` method returning `CompletableFuture<T>` lets the caller find out
  both when it finished and what it returned - and multiple such calls issued
  back-to-back genuinely run in parallel, bounded by the executor's pool size.
- An `@Async void` method is fire-and-forget: the caller has no way to catch
  its exceptions, because the caller's code already moved on before the
  method body runs. The only hook is a configured
  `AsyncUncaughtExceptionHandler` - without one, failures are easy to lose.
- Self-invocation (`this.asyncMethod()` from inside the same class) bypasses
  the Spring AOP proxy entirely and runs synchronously, with no error or
  warning - the same proxy limitation `@Transactional` has (lesson 38). Move
  `@Async` methods to a separate bean when they need to be called from
  sibling methods in the same class.
