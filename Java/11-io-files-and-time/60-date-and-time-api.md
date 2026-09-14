# 60 · The `java.time` Date and Time API

> **Run the code for this lesson**
> ```bash
> java Java/11-io-files-and-time/60-date-and-time-api.java
> ```

Section 4 reproduces a real Daylight Saving Time bug using an actual DST transition date
from the JVM's real timezone database — not a simulation.

---

## 1. The three "local" types: no timezone involved at all

```java
LocalDate date = LocalDate.of(2024, 3, 15);
LocalTime time = LocalTime.of(14, 30);
LocalDateTime dateTime = LocalDateTime.of(date, time);   // 2024-03-15T14:30
```

"Local" means exactly that — a wall-clock date/time with **no** attached timezone or
offset at all. `"2024-03-15T14:30"` alone does not identify one specific instant in
universal time — it's a civil calendar reading, the same string whether you mean it in
Tokyo or New York. Use it for birthdays, store hours, anything genuinely tied to a wall
clock, not a universal instant.

---

## 2. Every `java.time` type is immutable

```java
LocalDate original = LocalDate.of(2024, 1, 15);
LocalDate later = original.plusMonths(2);
// original is STILL 2024-01-15 - plusMonths RETURNED a new LocalDate
```

The real, historical contrast: `java.util.Date` **is** mutable, and that has caused real
bugs — a `Date` handed to another class that calls `.setTime()` on your instance corrupts
state you thought was yours alone:

```java
Date legacy = new Date(0);
Date sameReference = legacy;
sameReference.setTime(999_999_999_999L);
// legacy changed too - same object, no copy ever protected it
```

---

## 3. Month-overflow arithmetic: clamped correctly

```java
LocalDate.of(2024, 1, 31).plusMonths(1);   // 2024-02-29 (2024 IS a leap year)
LocalDate.of(2023, 1, 31).plusMonths(1);   // 2023-02-28 (2023 is NOT a leap year)
```

`java.time` clamps to the target month's *last valid day* rather than overflowing into
March, the way naive day-counting arithmetic might — verified here for both a leap and
non-leap February.

---

## 4. `Duration.ofDays(1)` is not `Period.ofDays(1)` — real proof

A real US DST transition date: `America/New_York`, March 10, 2024, where clocks spring
forward from 2:00 AM to 3:00 AM — `1:30 AM` exists, but the entire `2:00–2:59 AM` hour
does **not** exist that day.

```java
ZonedDateTime start = ZonedDateTime.of(2024, 3, 10, 1, 30, 0, 0, ZoneId.of("America/New_York"));
// 2024-03-10T01:30-05:00[America/New_York]

start.plus(Duration.ofHours(24));   // 2024-03-11T02:30-04:00[America/New_York]
start.plus(Period.ofDays(1));       // 2024-03-11T01:30-04:00[America/New_York]
```

**These are different moments.** `Duration.ofHours(24)` means exactly 24 real, elapsed
hours — since one hour was skipped that day, 24 elapsed hours lands at `2:30 AM` the next
day, not `1:30 AM`. `Period.ofDays(1)` means "the same wall-clock time, one calendar day
later" — it lands at `1:30 AM` the next day, regardless of how many hours actually
elapsed.

Verified directly: `plusDuration.toLocalTime().equals(start.toLocalTime())` is `false`;
`plusPeriod.toLocalTime().equals(start.toLocalTime())` is `true`.

**The rule**: `Duration` is for machine time (elapsed seconds/nanos — timeouts, measured
intervals, `Instant` arithmetic). `Period` is for calendar time ("one month from today",
"in 2 weeks", anything a human would describe in days/months/years). Using `Duration` for
"add one day" on a `ZonedDateTime` is a real, DST-triggered bug — just demonstrated with
an actual transition date, not a hypothetical.

---

## 5. `Instant`: one unambiguous point on the universal timeline

```java
Instant now = Instant.now();   // always UTC, always unambiguous

ZonedDateTime tokyo = now.atZone(ZoneId.of("Asia/Tokyo"));
ZonedDateTime losAngeles = now.atZone(ZoneId.of("America/Los_Angeles"));

tokyo.toInstant().equals(losAngeles.toInstant());   // true
```

Two completely different wall-clock readings, the same real instant. This is exactly
what `Instant` is for: a server timestamp, a log entry, an "event happened at" record —
anything that needs to identify one real moment unambiguously, regardless of who reads it
from where.

---

## 6. `DateTimeFormatter`: the replacement for `SimpleDateFormat`

```java
someDate.format(DateTimeFormatter.ISO_LOCAL_DATE);            // 2024-12-25
someDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));  // Wednesday, December 25, 2024

LocalDate.parse("2024-06-01");                                 // ISO by default
LocalDate.parse("25/12/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy"));  // 2024-12-25
```

Unlike the old `SimpleDateFormat`, `DateTimeFormatter` is **immutable and thread-safe** —
a single formatter instance can be safely shared and reused across threads without
external synchronization, which `SimpleDateFormat` famously could not do safely (a
well-known source of real, intermittent, hard-to-reproduce bugs in older codebases that
shared one `SimpleDateFormat` instance across threads).

---

## 7. Summary

- `LocalDate`/`LocalTime`/`LocalDateTime` carry no timezone at all — a wall-clock reading,
  not a universal instant. Use them for civil, calendar-tied concepts.
- Every `java.time` type is immutable — every "mutator" method returns a new instance,
  confirmed here against `java.util.Date`'s real, historically bug-prone mutability.
- Date arithmetic across month boundaries clamps sensibly to the target month's last
  valid day (verified for both leap and non-leap Februaries), rather than overflowing.
- `Duration` (elapsed machine time) and `Period` (calendar time) are **not
  interchangeable** — measured here with a real DST transition date where
  `Duration.ofHours(24)` and `Period.ofDays(1)` landed on genuinely different moments.
  Use `Period` for calendar-day arithmetic on zoned types.
- `Instant` represents one unambiguous universal moment; the same `Instant` viewed
  through different `ZoneId`s produces different wall-clock readings that are still,
  verifiably, the same instant.
- `DateTimeFormatter` is immutable and thread-safe, unlike `SimpleDateFormat` — safe to
  share as a single, reused instance across threads.

---

**Previous:** [59 — Serialization](59-serialization.md) ·
**Next:** [61 — Regular expressions](61-regular-expressions.md)
