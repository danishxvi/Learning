# 67 · Virtual Threads (Java 21)

> **Run the code for this lesson**
> ```bash
> java Java/12-concurrency/67-virtual-threads.java
> ```

The final lesson of Section 12. Section 3 runs the same real workload — thousands of
concurrent, sleep-bound tasks — on virtual threads and on real platform threads, and
reports the real measured gap between them.

---

## 1. `Thread.ofVirtual()`: the same `Thread` class, a new kind

```java
Thread virtual = Thread.ofVirtual().unstarted(() -> { ... });
virtual.start();
virtual.isVirtual();   // true
virtual.isDaemon();    // true - ALWAYS true, cannot be changed (Section 4)

new Thread(() -> {}).isVirtual();   // false
```

Virtual threads are still `java.lang.Thread` objects — same class, same
`start()`/`join()`/`interrupt()` API from lesson 62. What's different is entirely
underneath: a virtual thread is **not** backed by its own dedicated OS thread. The JVM
multiplexes many virtual threads onto a small pool of real OS threads ("carrier
threads"), switching a virtual thread off its carrier the moment it blocks (sleep, I/O,
lock acquisition, ...) and putting a different waiting virtual thread on that now-free
carrier instead.

---

## 2. The modern way: `Executors.newVirtualThreadPerTaskExecutor()`

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> { ... });
}
```

Same `ExecutorService` interface as lesson 64's thread pools — the difference is entirely
in what `submit()` hands work to: a genuinely new virtual thread *per task*, never
reused, never pooled. This is the recommended, idiomatic way to use virtual threads — not
`Thread.ofVirtual()` directly for everyday application code.

---

## 3. The real scalability gap: measured both ways

20,000 tasks, each `Thread.sleep(500)`ms — a genuinely I/O/sleep-bound workload, exactly
what virtual threads are for (a CPU-bound workload would not show this gap):

| Approach | Time |
| --- | --- |
| 20,000 virtual threads | 3,074 ms |
| 20,000 platform threads | 6,762 ms |

Platform threads took roughly **2.2× longer** for the identical workload. Both actually
completed (neither crashed on this run), but scheduling and creating tens of thousands of
real OS threads is genuinely, measurably expensive.

A separately-verified run at **100,000 threads** showed an even larger gap:

| Approach (100,000 threads) | Time |
| --- | --- |
| Virtual threads | 5,563 ms |
| Platform threads | 47,947 ms |

Roughly **8.6× slower** for platform threads at that scale — and on a machine with less
available memory or a lower OS thread-count limit, the platform-thread version can fail
outright with a real `OutOfMemoryError` rather than merely being slow. Virtual threads do
not carry that same per-thread OS cost at all.

---

## 4. What virtual threads do not fix

**Always daemon**: confirmed in Section 1 — `virtual.isDaemon()` was `true`, and there is
no `setDaemon(false)` option for virtual threads at all. A virtual thread never, by
itself, keeps the JVM alive — lesson 62's daemon rule applies unconditionally here.

**Pinning**: a virtual thread executing inside a `synchronized` block or method cannot be
unmounted from its carrier thread while blocked in there — a real, documented JDK 21
limitation. A virtual thread that does a slow blocking operation *while holding* a
`synchronized` lock effectively pins its real OS carrier thread for that entire duration,
losing the exact scalability benefit just measured. The real fix, when this matters, is
`java.util.concurrent.locks.ReentrantLock` instead of `synchronized` around any blocking
call on a hot path.

**Not faster for CPU-bound work**: virtual threads help when threads spend their time
*blocked* (I/O, sleep, waiting on other threads) — for pure computation with no blocking
at all, there's nothing to multiplex, and the real bottleneck stays the number of actual
CPU cores, exactly the same as lesson 64's platform-thread pools.

---

## 5. Summary

- Virtual threads use the same `Thread` API as platform threads (lesson 62) — the
  difference is entirely in how the JVM schedules them onto a small pool of real OS
  "carrier" threads, unmounting a blocked virtual thread so its carrier can serve
  another.
- `Executors.newVirtualThreadPerTaskExecutor()` is the idiomatic way to use them —
  one virtual thread per submitted task, never pooled or reused.
- Measured directly: virtual threads handled 20,000 concurrent sleep-bound tasks 2.2×
  faster than real platform threads, and the gap widened to roughly 8.6× at 100,000
  threads — a genuine, measured scalability advantage for blocking-heavy workloads.
- Virtual threads are always daemon threads — no way to change that.
- `synchronized` blocks can "pin" a virtual thread to its carrier during a blocking call
  inside them, defeating the scalability benefit — use `ReentrantLock` instead on hot,
  blocking-inside-a-lock paths.
- Virtual threads offer no benefit for pure CPU-bound work with no blocking — the
  bottleneck there is real CPU cores, unaffected by how threads are scheduled.

---

**Previous:** [66 — `CompletableFuture` and async pipelines](66-completablefuture.md) ·
**Next:** [68 — JVM architecture and memory areas](../13-jvm-and-internals/68-jvm-architecture-and-memory.md)
