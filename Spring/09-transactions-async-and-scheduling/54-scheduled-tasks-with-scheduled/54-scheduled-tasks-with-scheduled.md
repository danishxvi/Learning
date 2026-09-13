# 54 - Scheduled Tasks with `@Scheduled`

`@Scheduled` runs a method on a recurring schedule with no external trigger -
no HTTP request, no message, just time. Three ways to describe "recurring,"
and one sizeable, real, measured pitfall in how Spring actually executes them
by default.

```bash
mvn -f Spring/09-transactions-async-and-scheduling/54-scheduled-tasks-with-scheduled spring-boot:run
```

## Turning it on: `@EnableScheduling`

Like `@EnableAsync` in lesson 53, `@Scheduled` is inert without
`@EnableScheduling` - it's what tells Spring to actually scan for
`@Scheduled` methods and register them with a `TaskScheduler`.

## The three schedule types

[`ScheduledTasks.java`](src/main/java/com/danish/spring/scheduling/ScheduledTasks.java):

```java
@Scheduled(fixedRate = 300)
public void fastTask() { recorder.record("fastTask"); }

@Scheduled(fixedRate = 300)
public void slowTask() throws InterruptedException {
    recorder.record("slowTask");
    Thread.sleep(800);
}

@Scheduled(fixedDelay = 300)
public void fixedDelayTask() throws InterruptedException {
    recorder.record("fixedDelayTask");
    Thread.sleep(200);
}

@Scheduled(cron = "*/1 * * * * *")
public void cronTask() { recorder.record("cronTask"); }
```

- **`fixedRate`** schedules the next run 300ms after the *start* of this one -
  in principle, executions could even overlap if one runs long enough.
- **`fixedDelay`** schedules the next run 300ms after the *end* of this one -
  the delay is measured from completion, so a slow task pushes every
  subsequent run back by however long it took.
- **`cron`** fires on a wall-clock schedule using Spring's 6-field format
  (second, minute, hour, day-of-month, month, day-of-week) -
  `*/1 * * * * *` means "every second, on the second," independent of when
  the previous execution happened to run.

## The real pitfall: Spring Boot's default scheduler is ONE thread for everything

The first real run below used no custom `TaskScheduler` bean at all -
`@EnableScheduling` with nothing else, which is what a lot of code in the
wild actually looks like. `fastTask` and `slowTask` are BOTH `fixedRate=300`,
but `slowTask` sleeps 800ms every run. Real output:

```
fastTask (fixedRate=300, no artificial work)
  execution times (ms since start): [1045, 2051, 2856, 3662]
  deltas between executions (ms):  [1006, 805, 806]

slowTask (fixedRate=300, sleeps 800ms each run - shares the scheduler thread with fastTask)
  execution times (ms since start): [232, 1246, 2051, 2856, 3662]
  deltas between executions (ms):  [1014, 805, 805, 806]

fixedDelayTask (fixedDelay=300, sleeps 200ms each run)
  execution times (ms since start): [1045]
  deltas between executions (ms):  []

cronTask (cron=*/1 * * * * * (every second on the second))
  execution times (ms since start): [1246, 3662]
  deltas between executions (ms):  [2416]

distinct threads used across ALL @Scheduled methods: [scheduling-1]
```

Every single execution ran on **one thread**, `scheduling-1`. `fastTask` was
declared `fixedRate=300` and does no work of its own, so it should fire every
~300ms - instead its actual gaps were 1006ms, 805ms, 806ms, because it had to
wait for `slowTask`'s 800ms sleep to finish before the shared thread was free
to run anything else. `fixedDelayTask` only managed to run **once** in the
whole 4-second window - it was starved, sitting in the scheduler's queue
behind `slowTask` every time it should have fired. `cronTask`, which should
fire once a second (4 times in 4 seconds), only fired twice, 2416ms apart
instead of ~1000ms. None of these tasks are slow themselves - they're all
waiting on the one thread `slowTask` keeps occupying.

**What's actually happening:** Spring Boot's autoconfiguration only creates a
`TaskScheduler` with more than one thread if you tell it to. With no
`TaskScheduler` bean defined, `@EnableScheduling`'s default execution model
funnels every `@Scheduled` method in the entire application through a single
worker thread - so one slow scheduled task doesn't just run late itself, it
delays every *other* scheduled task in the app, regardless of what those
tasks' own intervals say.

## The fix: a pooled `TaskScheduler`

[`SchedulingConfig.java`](src/main/java/com/danish/spring/scheduling/SchedulingConfig.java):

```java
@Bean
public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(4);
    scheduler.setThreadNamePrefix("scheduling-pool-");
    scheduler.initialize();
    return scheduler;
}
```

Defining a `TaskScheduler` bean overrides the single-thread default entirely
- Spring uses whatever `TaskScheduler` bean is in the context instead. With a
4-thread pool and the exact same `ScheduledTasks` code, unchanged, real
output:

```
fastTask (fixedRate=300, no artificial work)
  execution times (ms since start): [183, 476, 781, 1088, 1379, 1677, 1979, 2281, 2580, 2879, 3177, 3483, 3781, 4080]
  deltas between executions (ms):  [293, 305, 307, 291, 298, 302, 302, 299, 299, 298, 306, 298, 299]

slowTask (fixedRate=300, sleeps 800ms each run - shares the scheduler thread with fastTask)
  execution times (ms since start): [183, 990, 1796, 2601, 3406]
  deltas between executions (ms):  [807, 806, 805, 805]

fixedDelayTask (fixedDelay=300, sleeps 200ms each run)
  execution times (ms since start): [183, 691, 1198, 1705, 2212, 2726, 3233, 3747]
  deltas between executions (ms):  [508, 507, 507, 507, 514, 507, 514]

cronTask (cron=*/1 * * * * * (every second on the second))
  execution times (ms since start): [1052, 2045, 3045, 4046]
  deltas between executions (ms):  [993, 1000, 1001]

distinct threads used across ALL @Scheduled methods: [scheduling-pool-1, scheduling-pool-2, scheduling-pool-3, scheduling-pool-4]
```

Every task now behaves exactly as its own declaration says, independent of
what the others are doing:

- `fastTask` fires roughly every 300ms (293-307ms), 14 times in 4 seconds -
  `slowTask`'s 800ms sleep no longer touches it at all.
- `slowTask` fires roughly every 805-807ms, which is exactly `300ms rate +
  ~505ms` of its own 800ms sleep spilling past the next trigger - consistent
  with `fixedRate` semantics (next run scheduled from *start* time, so a task
  slower than its rate just runs back-to-back rather than overlapping, since
  a single execution of the same task can't run concurrently with itself).
- `fixedDelayTask` fires roughly every 507-514ms - almost exactly its 200ms
  of work plus its 300ms delay, exactly as `fixedDelay` promises.
- `cronTask` fires roughly every 993-1001ms - once per second, as declared,
  landing on whole-second boundaries.
- Four distinct `scheduling-pool-N` threads were used, confirming the tasks
  genuinely ran independently rather than queuing behind each other.

## Key takeaways

- `fixedRate` measures the interval from the *start* of the previous
  execution; `fixedDelay` measures it from the *end*. A task slower than its
  own `fixedRate` doesn't overlap itself - the next run still waits for the
  current one to finish - but it also doesn't wait any *extra* delay, so its
  actual interval becomes "however long the task took," not the declared
  rate.
- `cron` schedules against wall-clock time, not relative to the previous
  execution, so its actual firing interval reflects the cron expression
  itself when the thread is available.
- **The default `TaskScheduler` (with no bean of your own) is single-threaded
  for the whole application** - one slow `@Scheduled` method delays every
  other `@Scheduled` method, not just itself, measurably and severely (a
  ~300ms task effectively became a ~1000ms one, and another task nearly
  starved completely, purely from sharing a thread with an unrelated slow
  task).
- Defining a `TaskScheduler` bean (a `ThreadPoolTaskScheduler` with a pool
  size sized to the number of genuinely concurrent scheduled tasks in the
  application) fixes this completely, with no change to the `@Scheduled`
  methods themselves - the exact same code, given more threads to run on,
  stopped interfering with itself.
