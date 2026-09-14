# 64 · The Executor Framework

> **Run the code for this lesson**
> ```bash
> java Java/12-concurrency/64-executor-framework.java
> ```

Section 5 documents a real hang that was deliberately reproduced *separately*, outside
this file — an `ExecutorService`'s threads are not daemon threads by default, so
forgetting `shutdown()` genuinely prevents the JVM from exiting. It wasn't attempted live
in the main file, because doing so would make this very lesson unable to finish. Every
other claim here is reproduced live, exactly like every other lesson in this curriculum.

---

## 1. `execute()` vs `submit()`: a return value, or not

```java
ExecutorService pool = Executors.newFixedThreadPool(3);

pool.execute(() -> System.out.println("fire and forget"));   // no return value

Future<Integer> future = pool.submit(() -> { Thread.sleep(50); return 6 * 7; });
future.isDone();   // false - returns IMMEDIATELY, does not block
future.get();       // 42 - NOW it blocks, until the task finishes
```

`execute()` is for work with no result and no need to track completion. `submit()` gives
back a `Future` — a handle to a result that may not exist yet, a lightweight version of
lesson 66's `CompletableFuture`.

---

## 2. `Future.get(timeout, unit)`: a real `TimeoutException`

```java
Future<Integer> slowTask = pool.submit(() -> { Thread.sleep(2000); return 1; });
slowTask.get(300, TimeUnit.MILLISECONDS);
```

Real result: `TimeoutException` — the task is still *running*; this call timed out, it
did not cancel anything.

A timed-out `get()` does **not** cancel the underlying task — it's still running in the
pool unless explicitly cancelled:

```java
slowTask.cancel(true);   // attempts interrupt() on it, same mechanics as lessons 62/63
```

---

## 3. Collecting results from many submitted tasks

```java
List<Future<Integer>> futures = new ArrayList<>();
for (int i = 1; i <= 5; i++) {
    int n = i;
    futures.add(pool.submit(() -> n * n));
}
List<Integer> squares = new ArrayList<>();
for (Future<Integer> f : futures) squares.add(f.get());
```

Real result: `[1, 4, 9, 16, 25]` — each `get()` blocks only until *its own* task is done.

---

## 4. The different pool shapes, and what each is actually for

Five quick tasks, submitted to each pool type, thread names collected:

| Factory | Distinct threads used |
| --- | --- |
| `newFixedThreadPool(2)` | 2 (capped, reused) |
| `newCachedThreadPool()` | 5 (grows as needed, no fixed cap) |
| `newSingleThreadExecutor()` | 1 (always exactly one) |

- `newFixedThreadPool`: a known, bounded amount of work in flight — the usual default for
  CPU-bound or steady server workloads.
- `newCachedThreadPool`: bursty, short-lived tasks — can grow unbounded under sustained
  load, a real resource-exhaustion risk if misused.
- `newSingleThreadExecutor`: a serial task queue with the same `ExecutorService` API as
  the others — useful when tasks *must* run one at a time, in submission order.

---

## 5. `shutdown()` is not optional: a real, documented hang

Not attempted live in this file, on purpose — reproducing it here would make this very
file unable to finish, unlike lesson 63's daemon-thread-contained deadlock. It *was*
verified for real, separately:

```java
ExecutorService pool = Executors.newFixedThreadPool(2);
pool.submit(() -> System.out.println("task ran"));
// main() returns WITHOUT calling pool.shutdown()
```

Real result: `"task ran"` printed, then the JVM did **not** exit. Run with a 5-second
timeout wrapper, the process was killed at the timeout (`exit code 124`), never having
exited on its own.

**Why**: unlike a `Thread` created directly (lesson 62), the pool threads `Executors`
creates are not daemon threads by default — they sit idle, waiting for more work, and a
non-daemon thread genuinely blocks JVM shutdown forever until it exits. `shutdown()` tells
the pool to finish queued/running work and then let those threads die.

**The real fix** — call `shutdown()` (lets queued/running work finish) or `shutdownNow()`
(attempts to interrupt running tasks immediately and returns the tasks that never
started) — every `ExecutorService` in this lesson was shut down explicitly.

```java
List<Runnable> neverStarted = pool.shutdownNow();
```

**A related, real pattern** — a custom `ThreadFactory` that makes daemon threads, so a
forgotten `shutdown()` can never hang the JVM:

```java
ThreadFactory daemonFactory = runnable -> {
    Thread t = new Thread(runnable);
    t.setDaemon(true);
    return t;
};
ExecutorService daemonPool = Executors.newFixedThreadPool(2, daemonFactory);
// never shutdown() - and this file STILL exits, because every thread is a real daemon
```

Real, confirmed behavior: this pool was never shut down, and the lesson still exited
cleanly. This is a real, useful pattern — but `shutdown()` remains the correct default;
daemon pools can silently lose in-flight work on JVM exit.

---

## 6. `BlockingQueue`: lesson 63's wait/notify, in one line

```java
BlockingQueue<String> queue = new LinkedBlockingQueue<>();
// producer: queue.put("a real message");
String received = queue.take();   // BLOCKS until something is available
```

No manual `synchronized`/`wait`/`notify` anywhere, unlike lesson 63's hand-rolled
`SimpleBox`. `BlockingQueue` implementations (`ArrayBlockingQueue`, `LinkedBlockingQueue`,
...) are the standard tool for real producer/consumer pipelines — lesson 63's version
exists mainly to show what these classes do under the hood.

---

## 7. Summary

- `execute()` is fire-and-forget; `submit()` returns a `Future` handle to a
  not-yet-available result, checkable with `isDone()` and blocking on `get()`.
- `Future.get(timeout, unit)` throws a real `TimeoutException` without cancelling the
  underlying task — call `cancel(true)` explicitly if the task should actually stop.
- The three main pool factories have measurably different threading shapes: fixed
  (capped and reused), cached (grows per task, unbounded risk), single (always exactly
  one, strictly ordered).
- Forgetting `shutdown()` genuinely hangs the JVM forever — confirmed with a real,
  externally-verified `exit code 124`. Pool threads are not daemon threads by default,
  unlike threads created directly.
- A custom `ThreadFactory` producing daemon threads is a real, working alternative when a
  forgotten `shutdown()` must never hang the process — at the cost of possibly losing
  in-flight work on exit.
- `BlockingQueue` is the modern, correct tool for producer/consumer coordination,
  replacing hand-rolled `wait`/`notify` for the common case.

---

**Previous:** [63 — Synchronization, locks and the memory model](63-synchronization-and-locks.md) ·
**Next:** [65 — Concurrent collections and atomics](65-concurrent-collections-and-atomics.md)
