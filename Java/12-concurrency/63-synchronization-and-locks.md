# 63 · Synchronization, Locks and the Memory Model

> **Run the code for this lesson**
> ```bash
> java Java/12-concurrency/63-synchronization-and-locks.java
> ```

Sections 3 and 4 reproduce a real memory-visibility bug and a real deadlock, **live**, on
this machine — both using a genuinely safe technique (daemon threads + a bounded `join`
timeout, from lesson 62) so that even a true infinite hang cannot prevent this file from
finishing and exiting.

---

## 1. A real race condition, with raw threads

```java
static int unsafeCounter = 0;
// 10 threads, each: for (100,000 times) unsafeCounter++;
```

Real, measured result: expected `1,000,000`, got `161,609` — lost updates, a real race
condition. `counter++` is three steps: read the current value, add one, write it back.
Two threads can both read the same value before either writes — one increment is silently
lost. This is the exact same class of bug lesson 54 measured with a parallel *stream*;
here it's reproduced with raw `Thread`s directly.

---

## 2. `synchronized` fixes it, and produces a real `BLOCKED` thread

```java
synchronized (counterLock) {
    safeCounter++;   // now atomic with respect to counterLock
}
```

Real result: `1,000,000` — correct, every increment survived.

A real `BLOCKED` thread, caught in the act:

```java
Thread holder = new Thread(() -> { synchronized (demoLock) { Thread.sleep(300); } });
Thread waiter = new Thread(() -> { synchronized (demoLock) { } });
```

Real, observed sequence:
```
waiter.getState() while holder still owns the lock -> BLOCKED
waiter.getState() after holder released it          -> TERMINATED
```

---

## 3. A real memory-visibility bug, reproduced live

The worker thread is a **daemon** (lesson 62) specifically so that even if it genuinely
never sees the update and loops forever, this file still exits cleanly — safe to actually
attempt the famous "the JIT cached the read" bug for real, not just describe it:

```java
class NonVolatileFlag { boolean running = true; }

Thread worker = new Thread(() -> { while (flag.running) { /* spin */ } });
worker.setDaemon(true);
worker.start();
// ... later:
flag.running = false;
worker.join(3000);
```

Real, measured result: **the worker did not notice within 3 real seconds.** The worker
thread's own CPU core is reading `running` from a register or cache line it loaded once,
before the loop started — main's write exists in main memory, but nothing forces the
worker to look there again. No exception, no warning — it can loop like this forever, on
real hardware, on a real JVM, right now.

**The fix** — the only difference is the word `volatile`:

```java
class VolatileFlag { volatile boolean running = true; }
```

Real result: `[volatile worker] noticed after 624476259 iterations` — it stopped within
the 3-second window every time.

`volatile` guarantees every read sees the most recent write from *any* thread — it
forbids exactly the caching that broke the non-volatile version. It does **not** make
`counter++` atomic (Section 1's bug is unrelated, and `volatile` alone would not fix it)
— visibility and atomicity are two different problems with two different tools.

---

## 4. A real deadlock, reproduced live

```java
Thread t1 = new Thread(() -> {
    synchronized (lockA) { sleep(200); synchronized (lockB) { ... } }
});
Thread t2 = new Thread(() -> {
    synchronized (lockB) { sleep(200); synchronized (lockA) { ... } }
});
```

`t1` locks A then B. `t2` locks B then A — opposite order, the classic deadlock shape.
Both threads are daemons, so a real deadlock here cannot prevent the file from finishing:

```
t1 finished within 3 real seconds? -> false
t2 finished within 3 real seconds? -> false
t1.getState() -> BLOCKED
t2.getState() -> BLOCKED
```

Both threads are `BLOCKED`, permanently — `t1` holds A and wants B; `t2` holds B and wants
A. Neither can ever proceed. This is a real deadlock, live, right now, on this machine —
not a diagram.

**The fix**: always acquire multiple locks in the same global order, everywhere in the
codebase (e.g. always the lower object-identity-hash first, or an explicit ranking) — if
both threads had locked A before B, this deadlock could never happen.

---

## 5. `wait()`/`notify()`: coordinating, not just excluding

```java
class SimpleBox<T> {
    private T value;
    private boolean present = false;

    synchronized void put(T v) { value = v; present = true; notify(); }
    synchronized T take() {
        while (!present) { wait(); }   // releases the monitor while waiting
        present = false;
        return value;
    }
}
```

Real, observed state before the producer runs: `consumer.getState()` is `WAITING` — inside
`Object.wait()`, holding *no* lock while it waits. Once the producer calls `put()`, the
consumer receives the real message.

`wait()` atomically releases the monitor and suspends the thread; `notify()` wakes one
waiting thread back up to re-acquire it. This is what makes wait/notify genuine
*coordination*, not just mutual exclusion — lesson 64's `BlockingQueue` is the modern,
safer way to get this exact producer/consumer behavior without hand-rolling it.

---

## 6. Summary

- `counter++` is not atomic — a real, measured race condition across 10 threads lost over
  80% of increments. `synchronized` around the operation fixed it completely.
- A thread waiting for a `synchronized` block another thread holds is genuinely
  `BLOCKED` — confirmed directly via `Thread.getState()`.
- A field read in a tight loop by one thread, written by another, without `volatile`, can
  genuinely never observe the update — reproduced live here as a real, indefinite hang
  (safely contained with a daemon thread), fixed completely by adding `volatile`.
- `volatile` fixes visibility, not atomicity — the two are separate problems requiring
  separate tools (`volatile` vs `synchronized`/atomics).
- Two threads acquiring the same two locks in opposite order can deadlock permanently —
  reproduced live here, both threads genuinely and forever `BLOCKED`. The fix is a
  consistent global lock-acquisition order.
- `wait()`/`notify()` provide real coordination: a waiting thread releases its lock while
  waiting (confirmed via `WAITING` state) and is woken by another thread's `notify()`.

---

**Previous:** [62 — Threads and the thread lifecycle](62-threads-and-lifecycle.md) ·
**Next:** [64 — The Executor framework](64-executor-framework.md)
