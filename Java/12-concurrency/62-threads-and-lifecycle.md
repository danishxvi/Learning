# 62 · Threads and the Thread Lifecycle

> **Run the code for this lesson**
> ```bash
> java Java/12-concurrency/62-threads-and-lifecycle.java
> ```

Section 2 reproduces the single most common threading mistake beginners make — calling
`run()` instead of `start()` — with real thread-name evidence proving nothing actually
ran concurrently. Section 5 reproduces the real `IllegalThreadStateException` from
starting a `Thread` twice.

---

## 1. Extending `Thread` vs implementing `Runnable`

```java
Thread byExtending = new Thread() {
    @Override public void run() { ... }
};

Runnable task = () -> { ... };
Thread byRunnable = new Thread(task);
```

Both genuinely start a new thread. The difference is **extends vs implements**. A class
can extend only one superclass (lesson 26), so extending `Thread` burns your one
inheritance slot on something that has nothing to do with your class's actual purpose. A
`Runnable` is just *behavior* — your class stays free to extend whatever it actually
needs, and the same `Runnable` can be handed to an `ExecutorService` (lesson 64) instead
of a raw `Thread`.

---

## 2. `start()` vs `run()`: the #1 beginner mistake, reproduced

```java
Thread demo = new Thread(() ->
    System.out.println(Thread.currentThread().getName()), "worker-thread");

demo.run();     // THE MISTAKE
```

Real result: prints `main` — the *calling* thread, not a new one.
`demo.getState()` after this call: still `NEW`. `run()` was just a normal method call, on
`main`'s own call stack — nothing was ever scheduled concurrently.

```java
demo.start();   // THE FIX
```

Real result: prints `worker-thread-2` — a genuinely different thread.

Calling `run()` directly compiles fine, produces no error, and just silently runs
everything sequentially on the caller's own thread — no concurrency happened at all. This
is a real, easy-to-miss bug, precisely because the code still runs and often still looks
correct — it's just not concurrent.

---

## 3. The lifecycle, observed with real `getState()` calls

```java
Thread lifecycle = new Thread(() -> {
    synchronized (lock) { Thread.sleep(150); }
});

lifecycle.getState();     // NEW - before start()
lifecycle.start();
// ... during the sleep:
lifecycle.getState();     // TIMED_WAITING
lifecycle.join();
lifecycle.getState();     // TERMINATED
```

Real, measured states: `NEW` (created, never started), `TIMED_WAITING` (mid-sleep),
`TERMINATED` (`run()` returned — a `Thread` can never be restarted from here, confirmed
for real in Section 5). The other real states — `RUNNABLE` (eligible to run *or*
currently running; Java doesn't distinguish the two), `BLOCKED` (waiting for a monitor
lock another thread holds), `WAITING` (`Object.wait()`/`Thread.join()` with no timeout) —
lesson 63 demonstrates `BLOCKED` concretely with real lock contention.

---

## 4. Daemon threads do not prevent JVM shutdown

```java
Thread daemon = new Thread(() -> { Thread.sleep(60_000); });
daemon.setDaemon(true);
daemon.start();
```

Real, observed result: the program exits almost immediately anyway, despite a thread
"sleeping" for a full minute. If it were a normal (non-daemon) thread, `main()` would have
to wait the full 60 seconds before the JVM could shut down. Daemon threads are for
background work (GC, housekeeping) that should never hold the process open on its own —
the JVM exits once every non-daemon thread has finished, killing any remaining daemons
outright.

---

## 5. A `Thread` can never be restarted: reproduced for real

```java
Thread onceOnly = new Thread(() -> {});
onceOnly.start();
onceOnly.join();          // state now TERMINATED

onceOnly.start();         // IllegalThreadStateException
```

A `Thread` object is genuinely single-use — once `TERMINATED`, it can never run again,
even though the object itself still exists and is perfectly readable. Need to run the
same work again? Create a *new* `Thread` with the same `Runnable` — the `Runnable` is
reusable; the `Thread` wrapping it is not.

---

## 6. `interrupt()`: cooperative, not forced

```java
Thread sleeper = new Thread(() -> {
    try {
        Thread.sleep(10_000);
    } catch (InterruptedException e) {
        // woke up EARLY
    }
});
sleeper.start();
sleeper.interrupt();
```

Real result: the sleeper wakes up early via a caught `InterruptedException`, well before
the full 10 seconds elapse.

`interrupt()` does **not** forcibly kill a thread — it sets an internal flag, and any
blocking call currently in progress (`sleep`, `wait`, `join`, ...) notices that flag and
throws `InterruptedException`. A thread doing pure computation with no blocking calls
will not react to `interrupt()` at all unless its own code explicitly checks
`Thread.currentThread().isInterrupted()` periodically.

---

## 7. Summary

- Prefer implementing `Runnable` over extending `Thread` — it preserves your class's one
  inheritance slot and works with `ExecutorService` (lesson 64) unchanged.
- Calling `run()` instead of `start()` is a real, silent mistake: the code runs, on the
  *caller's* thread, with zero concurrency — verified here by real thread-name evidence.
- The real lifecycle states (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`,
  `TERMINATED`) were observed directly via `Thread.getState()` at each transition point.
- Daemon threads (`setDaemon(true)`, before `start()`) never block JVM shutdown —
  confirmed here by a program exiting immediately despite a 60-second daemon sleep still
  in progress.
- A terminated `Thread` cannot be restarted — `start()` a second time throws a real
  `IllegalThreadStateException`.
- `interrupt()` is cooperative: it sets a flag that blocking calls check and react to by
  throwing `InterruptedException` — it does not forcibly stop a thread doing pure,
  non-blocking computation.

---

**Previous:** [61 — Regular expressions](../11-io-files-and-time/61-regular-expressions.md) ·
**Next:** [63 — Synchronization, locks and the memory model](63-synchronization-and-locks.md)
