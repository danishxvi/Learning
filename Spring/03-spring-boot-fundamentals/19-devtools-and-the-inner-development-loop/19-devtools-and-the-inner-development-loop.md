# 19 · DevTools and the inner development loop

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/03-spring-boot-fundamentals/19-devtools-and-the-inner-development-loop spring-boot:run
> ```
> While it's running, edit `application.yml`'s `app.greeting-suffix`, save, and watch the
> console — it restarts on its own, in a fraction of a second, with no manual stop.

Every earlier lesson meant stopping the process, rerunning `mvn spring-boot:run`, and
waiting through a full cold start to see one change. DevTools removes that wait.

---

## 1. Adding it: `optional=true`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

`optional=true` marks this dependency as **not transitively inherited** by anything that
depends on this module — appropriate for a development-time convenience nobody
downstream should be forced to pull in. It's also specifically designed to exclude
itself from a packaged executable jar (lesson 04's `BOOT-INF/lib`) when built for
production — DevTools is meant to never ship by accident.

---

## 2. What starting up with DevTools actually prints

Real log lines from running this lesson, unedited:

```
DevToolsPropertyDefaultsPostProcessor : Devtools property defaults active!
OptionalLiveReloadServer              : LiveReload server is running on port 35729
```

Two things happened here that a plain `spring-boot-starter` project never does:

- **Property defaults** — DevTools silently disables template caching and a handful of
  other production-oriented defaults, so the "did my HTML template change take effect"
  question never comes up in development.
- **A LiveReload server** starts on port 35729. Installing a LiveReload browser
  extension makes an open browser tab auto-refresh the instant the server restarts —
  useful the moment section 04 introduces web pages to look at.

---

## 3. Editing a file while the app is running

This lesson's `main` deliberately blocks after `SpringApplication.run` (a real web
application stays alive on its own, via its embedded server — this console app has
nothing else keeping the JVM up, so it blocks explicitly, purely so DevTools has
something to keep watching). With the app running, editing `application.yml` and saving
produces this — the exact, real console output from doing it:

```
File Watcher : Restarting due to 1 class path change (0 additions, 0 deletions, 1 modification)

  .   ____          _            __ _ _
 ...
 :: Spring Boot ::                (v3.3.4)

DevToolsApplication : Starting DevToolsApplication using Java 21.0.11 ...
DevToolsApplication : Started DevToolsApplication in 0.18 seconds (process running for 16.387)
==========================================================================
READY - Hello from DevTools demo (version 2 - EDITED WHILE RUNNING)
==========================================================================
```

**`0.18 seconds`** — against roughly `1.2` seconds for the original cold start of this
same tiny project. No manual stop, no manual rerun: a background **File Watcher** thread
noticed the change and restarted the application on its own.

---

## 4. Why the restart is fast: two classloaders

A normal JVM restart reloads every class — your code and every library it depends on —
from scratch. DevTools splits the classpath across **two classloaders** instead:

- **Base classloader**: loads every jar dependency — Spring itself, Logback, everything
  under `BOOT-INF/lib` conceptually. These almost never change during a coding session,
  so this classloader is built once and kept alive across restarts.
- **Restart classloader**: loads only *your* compiled classes, from `target/classes`.
  This is the one thrown away and rebuilt on every restart.

Because only your (usually small) set of classes gets reloaded, and every dependency jar
stays loaded in memory throughout, a restart costs a small fraction of what a full JVM
cold start does — exactly the `0.18s` vs `1.2s` difference captured above. This is a
restart, not a *reload*: the whole `ApplicationContext` is torn down and rebuilt (every
bean re-constructed, lesson 06's lifecycle running again in full) — it's fast because of
*what* gets reloaded, not because Spring skips any of its normal startup work.

---

## 5. What triggers a restart, and what doesn't

DevTools watches `target/classes` (and equivalent output directories) by default —
editing a `.java` file only triggers a restart once your build recompiles it into
`target/classes` (an IDE with "build automatically" enabled does this on save; from the
command line, a separate `mvn compile` while `spring-boot:run` is active does the same).
Editing a resource file directly under `target/classes` — as this lesson's demo did —
triggers it immediately, with no compile step needed, since resources don't need
compiling in the first place.

`spring.devtools.restart.exclude` lets you exclude paths from triggering a restart at
all — useful for static assets meant to be picked up by LiveReload without tearing down
the whole `ApplicationContext` for a CSS tweak.

---

## 6. Summary

- **`spring-boot-devtools`**, added with `optional=true`, is a development-only
  dependency that excludes itself from a packaged production jar.
- It relaxes development-oriented defaults (disabling caching) and starts a
  **LiveReload server** for auto-refreshing an open browser tab.
- Saving a change under `target/classes` (recompiled `.java`, or an edited resource
  file directly) triggers an **automatic restart** — no manual stop/rerun.
- The restart is fast because DevTools splits the classpath into a **base classloader**
  (your dependencies, loaded once) and a **restart classloader** (your own classes,
  rebuilt every time) — this lesson measured a real `0.18s` restart against a `1.2s`
  cold start.
- It is still a full restart — every bean is rebuilt, lesson 06's lifecycle runs again —
  just scoped to a much smaller set of classes than a genuine process relaunch.
- **`spring.devtools.restart.exclude`** excludes specific paths from triggering a
  restart at all.

---

**Previous:** [18 — Logging with SLF4J and Logback](../18-logging-with-slf4j-and-logback/18-logging-with-slf4j-and-logback.md) ·
**Next:** [20 — Actuator basics](../20-actuator-basics/20-actuator-basics.md)
