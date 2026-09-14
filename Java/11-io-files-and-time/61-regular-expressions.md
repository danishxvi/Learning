# 61 · Regular Expressions

> **Run the code for this lesson**
> ```bash
> java Java/11-io-files-and-time/61-regular-expressions.java
> ```

The final lesson of Section 11. It measures a real, common performance mistake, and
reports an honest result from trying to reproduce catastrophic backtracking — including
where it did *not* blow up, rather than fabricating a scarier number than what was
actually measured.

---

## 1. `matches()` vs `find()` vs `lookingAt()`: three different questions

```java
String text = "The price is $42.50 today";
Pattern digits = Pattern.compile("[0-9]+");

digits.matcher(text).matches();    // false - must match the WHOLE string
digits.matcher(text).find();        // true  - matches ANYWHERE inside
digits.matcher(text).lookingAt();  // false - must match starting at INDEX 0
```

**The common confusion**: `String.matches(regex)` uses `.matches()` semantics — the
entire string must match. A regex written to find a pattern *somewhere* in text almost
always needs `.find()`, not `String.matches()`, or it will incorrectly return `false` on
any real sentence containing that pattern amid other text.

---

## 2. Capturing groups: numbered and named

```java
Pattern datePattern = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");
Matcher m = datePattern.matcher("Event date: 2024-03-15, confirmed");
m.find();
m.group(0);   // "2024-03-15" - the WHOLE match
m.group(1);   // "2024" - year
m.group(2);   // "03"   - month
m.group(3);   // "15"   - day
```

**Named groups** — self-documenting, and immune to renumbering when someone inserts a
group earlier in the pattern later:

```java
Pattern named = Pattern.compile("(?<year>\\d{4})-(?<month>\\d{2})-(?<day>\\d{2})");
Matcher nm = named.matcher("2024-03-15");
nm.matches();
nm.group("year");    // "2024"
nm.group("month");   // "03"
```

---

## 3. `replaceAll` backreferences, and the `split()` trap

```java
"John Smith".replaceAll("(\\w+) (\\w+)", "$2, $1");   // "Smith, John"
```

`$1`/`$2` in the replacement refer back to the captured groups.

```java
String csv = "a,b,c,,,";
csv.split(",");       // [a, b, c]         - TRAILING empties SILENTLY DROPPED
csv.split(",", -1);   // [a, b, c, , , ]   - negative limit KEEPS them all
```

**The real trap**: `split(regex)` with no limit argument (or limit `0`) silently discards
*trailing* empty strings. Parsing a CSV row where the last fields are legitimately blank
will silently produce fewer columns than expected — a real, common source of off-by-N
bugs in hand-rolled CSV parsing. Use `split(regex, -1)` whenever trailing empty fields
are meaningful.

---

## 4. `String.matches()` recompiles the pattern every call

500,000 validations of the same email against the same regex:

| Approach | Time |
| --- | --- |
| `email.matches(regex)` (each call) | 690 ms |
| Precompiled `Pattern`, reused | 169 ms |

`String.matches(regex)` is convenient sugar that calls
`Pattern.compile(regex).matcher(this).matches()` **every single time** — parsing and
compiling the regex from scratch on every call. In a loop or a hot validation path, that
compilation cost is paid over and over for no reason. Compile the `Pattern` once (a
`static final` field is the usual place) and reuse it — a real, measured ~4× difference.

---

## 5. Catastrophic backtracking: what was actually measured

Nested quantifiers like `(a+)+` or `(a|aa)+` are the textbook example of a regex whose
backtracking search can blow up exponentially on a crafted, non-matching input — this is
a real, well-documented vulnerability class ("ReDoS"), responsible for real production
outages (Cloudflare's July 2019 global outage was traced to exactly this, in a WAF rule's
regex).

**An honest result**: the pattern `(a|aa)+$` was tested here, live, against growing input
sizes, on this JDK:

```
n=20 -> 0 ms
n=22 -> 0 ms
n=24 -> 0 ms
n=26 -> 0 ms
n=28 -> 0 ms
n=30 -> 0 ms
n=32 -> 0 ms
n=34 -> 0 ms
```

**No exponential blowup appeared** within this range, on this JDK. Modern
`java.util.regex` has real optimizations for some of these classic patterns. This is a
genuine, measured result — not a claim that Java's regex engine is *immune* to
catastrophic backtracking in general. More complex nested/alternating patterns, or larger
inputs, can still exhibit it.

The engineering takeaway survives either way: never build a regex from untrusted input
structure, and be suspicious of nested quantifiers (`(x+)+`, `(x*)*`, `(x+)*`) applied to
attacker-influenced strings, regardless of which engine or JDK version is running it. The
absence of a blowup in one specific test is not proof of safety in general — it's a
report of exactly what was measured, no more and no less.

---

## 6. Summary

- `.matches()` requires the whole string to match; `.find()` searches anywhere;
  `.lookingAt()` requires a match starting at index 0. `String.matches()` uses
  `.matches()` semantics — a frequent source of "why doesn't my regex match" confusion.
- Named groups (`(?<name>...)`) are self-documenting and immune to renumbering when new
  groups are inserted earlier in the pattern.
- `replaceAll` replacement strings can reference captured groups with `$1`, `$2`, etc.
- `split(regex)` with no limit silently drops trailing empty strings — a real, common
  parsing bug. `split(regex, -1)` preserves them.
- `String.matches()`/`replaceAll()`/etc. recompile the pattern on every call — measured
  here at roughly 4× slower than a precompiled, reused `Pattern` over 500,000 calls.
  Precompile any regex used in a loop or hot path.
- Catastrophic backtracking (ReDoS) is a real, documented risk with nested quantifiers —
  reported honestly here as a negative result for the specific pattern and JDK tested,
  not a blanket claim of immunity. Never build a regex pattern from untrusted structure.

---

**Previous:** [60 — The `java.time` date and time API](60-date-and-time-api.md) ·
**Next:** [62 — Threads and the thread lifecycle](../12-concurrency/62-threads-and-lifecycle.md)
