# 66 · `CompletableFuture` and Async Pipelines

> **Run the code for this lesson**
> ```bash
> java Java/12-concurrency/66-completablefuture.java
> ```

Section 2 reproduces a real, surprising thread-naming fact: `thenApply` can run on the
*original calling thread*, not a worker thread, depending purely on timing. Section 3
reproduces the real doubly-wrapped type `thenApply` produces when the mapping function
itself returns a `CompletableFuture`.

---

## 1. `supplyAsync`: a result computed somewhere else

```java
CompletableFuture<String> greeting = CompletableFuture.supplyAsync(() -> {
    return "hello";
});
// returns IMMEDIATELY - main() did not block
greeting.get();   // NOW it blocks, until the value is ready
```

Real result: the supplier ran on `ForkJoinPool.commonPool-worker-1`, while `main` moved
on immediately. With no executor argument, `supplyAsync` uses the shared
`ForkJoinPool.commonPool()` — the same pool `parallelStream()` (lesson 54) uses
internally. For genuinely I/O-bound work, or to avoid competing with parallel streams for
the same pool, pass an explicit `Executor` (lesson 64) instead:
`supplyAsync(supplier, executor)`.

---

## 2. `thenApply`'s thread depends on whether it was already done

```java
CompletableFuture<String> alreadyDone = CompletableFuture.supplyAsync(() -> "value");
Thread.sleep(200);   // give it plenty of time to finish first
alreadyDone.thenApply(s -> { /* which thread am I on? */ return s.toUpperCase(); });
```

Real result: ran on `main` — the *calling* thread, not a pool thread.

```java
CompletableFuture<String> stillRunning = CompletableFuture.supplyAsync(() -> { sleep(200); return "value"; });
stillRunning.thenApply(s -> { /* which thread now? */ return s.toUpperCase(); });
```

Real result: ran on `ForkJoinPool.commonPool-worker-1`.

**Chaining `thenApply` onto a future that has already completed runs the callback on the
calling thread — not a pool thread.** Chaining it *before* completion runs it on whichever
pool thread ends up completing the future. This is documented behavior, not a bug — but
it means `thenApply`'s thread is genuinely unpredictable from the code alone.
`thenApplyAsync` (with no explicit executor) always uses the common pool, regardless of
timing — use it whenever which thread runs the callback actually matters.

---

## 3. `thenApply` with an async mapping function: a real trap

A common mistake: calling another async operation inside `thenApply`:

```java
CompletableFuture<CompletableFuture<String>> nested = CompletableFuture
        .supplyAsync(() -> "x")
        .thenApply(s -> CompletableFuture.supplyAsync(() -> s + "y"));

nested.get().get();   // REQUIRES a double .get() - real, confirmed: "xy"
```

**The fix** — `thenCompose`, for when the next step is itself async:

```java
CompletableFuture<String> flat = CompletableFuture
        .supplyAsync(() -> "x")
        .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "y"));

flat.get();   // ONE .get() reaches the value directly - "xy"
```

**The rule**: if the mapping function returns a plain value, use `thenApply`. If it
returns *another* `CompletableFuture` (because it starts another async operation), use
`thenCompose` — it flattens the result instead of nesting it, exactly like `Stream`'s
`flatMap` (lesson 54) flattens a stream of streams.

---

## 4. `thenCombine`: merging two independent async results

```java
CompletableFuture<Integer> priceFuture = CompletableFuture.supplyAsync(() -> { sleep(100); return 50; });
CompletableFuture<Integer> quantityFuture = CompletableFuture.supplyAsync(() -> { sleep(150); return 3; });
CompletableFuture<Integer> total = priceFuture.thenCombine(quantityFuture, (price, qty) -> price * qty);
```

Real result: `150` — price and quantity computed independently and concurrently, combined
once both are ready. `thenCompose` is for a *chain* (step 2 needs step 1's result to even
start). `thenCombine` is for a *fork/join* (two independent operations, running
concurrently, combined at the end).

---

## 5. Exceptions propagate through the chain, real proof

```java
CompletableFuture<Integer> failing = CompletableFuture.supplyAsync(() -> {
    throw new RuntimeException("simulated failure");
});
failing.get();   // ExecutionException, cause: "simulated failure" - the ORIGINAL exception, wrapped
```

```java
CompletableFuture<Integer> recovered = failing.exceptionally(ex -> {
    return -1;   // the fallback value
});
recovered.get();   // -1
```

`.exceptionally(fn)` runs only if an earlier stage threw — like a catch block for the
whole chain up to this point. `.handle((result, ex) -> ...)` runs *either* way, letting
one callback see both the success value and the exception (whichever is non-null).

---

## 6. `orTimeout`: the future fails itself if too slow

```java
CompletableFuture<Integer> slow = CompletableFuture.supplyAsync(() -> { sleep(3000); return 99; });
CompletableFuture<Integer> bounded = slow.orTimeout(300, TimeUnit.MILLISECONDS);
bounded.get();   // ExecutionException, cause: TimeoutException
```

Unlike lesson 64's `Future.get(timeout, unit)` — which only makes *that one call* give up
waiting, leaving the task still running in the background — `orTimeout` makes the future
*itself* complete exceptionally after the deadline, so every downstream stage chained onto
it sees the failure too, not just one particular caller.

---

## 7. Summary

- `supplyAsync`/`runAsync` compute work off the calling thread, by default on the shared
  `ForkJoinPool.commonPool()` — pass an explicit `Executor` to use a dedicated pool.
- `thenApply`'s execution thread is genuinely timing-dependent: the calling thread if the
  future was already done, a pool thread otherwise — verified here directly.
  `thenApplyAsync` always uses a pool thread, regardless.
- A mapping function that itself returns a `CompletableFuture`, used with `thenApply`,
  produces a nested `CompletableFuture<CompletableFuture<T>>` requiring a double `get()` —
  `thenCompose` flattens it, exactly like `Stream.flatMap`.
- `thenCombine` merges two independent, concurrently-running futures; `thenCompose`
  chains a dependent one.
- `.exceptionally()` recovers from a failure with a fallback value; `.handle()` observes
  both success and failure in one callback.
- `orTimeout` fails the future itself on a deadline, propagating to every downstream
  stage — a genuinely different guarantee than a single `Future.get(timeout, unit)` call.

---

**Previous:** [65 — Concurrent collections and atomics](65-concurrent-collections-and-atomics.md) ·
**Next:** [67 — Virtual threads (Java 21)](67-virtual-threads.md)
