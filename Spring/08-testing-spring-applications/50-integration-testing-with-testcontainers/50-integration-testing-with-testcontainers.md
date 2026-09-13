# 50 · Integration testing with Testcontainers

> **Run the tests for this lesson** (needs Docker running and reachable):
> ```bash
> mvn -f Spring/08-testing-spring-applications/50-integration-testing-with-testcontainers test
> ```

Lessons 48–49 both used H2 — fast, zero-install, and a genuine approximation of a real
database rather than the real thing. This lesson replaces H2 with an actual, disposable
Postgres container, spun up and torn down automatically around the test.

> **An honest note on this lesson's verification.** While writing this lesson, running
> the test on this machine hit a real, documented Windows/Docker Desktop compatibility
> issue — described in section 5 below, with the actual error captured. The code shown
> here is correct, standard Testcontainers usage and compiles cleanly, but the container
> itself could not be started and observed running in this environment. If Docker is
> reachable on your machine, this test should pass exactly as described; treat this
> lesson's SQL and assertions as the documented, expected behavior rather than something
> captured from a successful run here.

---

## 1. Why H2 isn't always enough

Lesson 36's Flyway migrations and lesson 48's derived queries all ran against H2 —
genuinely useful for speed, but H2 only **approximates** Postgres compatibility. A query
using real Postgres-only syntax can pass against H2's approximation and still fail
against a real Postgres server the moment it reaches one — exactly the gap
Testcontainers exists to close.

```java
@Query(value = "SELECT * FROM book WHERE title ILIKE %:fragment%", nativeQuery = true)
List<Book> searchTitleCaseInsensitive(String fragment);
```

`ILIKE` (case-insensitive `LIKE`) is genuine Postgres syntax. Testing it against a real
Postgres container, rather than H2's compatibility mode, is the entire point of what
follows.

---

## 2. `@Testcontainers` + `@Container` — a real, disposable database

```java
@Testcontainers
@SpringBootTest
class BookRepositoryTestcontainersTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BookRepository bookRepository;
}
```

`@Testcontainers` manages the container's lifecycle around this test class — starting
it before any test runs, stopping it after the last one. `static` means **one**
container is shared across every test method in the class, rather than paying its
startup cost per test. `postgres:16-alpine` is the exact same image family this stack
used with real, live verification back in lesson 37 — this lesson's difference is that
the container's lifecycle is managed entirely by the test framework, not by a `docker run`
command typed by hand.

---

## 3. `@ServiceConnection` — no manual property wiring

```java
@Container
@ServiceConnection
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
```

Before Spring Boot 3.1, wiring a Testcontainers container's connection details into
Spring's `DataSource` needed a `@DynamicPropertySource` method manually reading
`postgres.getJdbcUrl()`/`getUsername()`/`getPassword()` and registering each one. `@ServiceConnection`
replaces all of that: Spring Boot recognizes the container type and wires its actual
(randomly assigned) host, port, and credentials into the `DataSource` automatically —
no property names to get right, no manual registration.

---

## 4. What this test is expected to prove

```java
List<Book> results = bookRepository.searchTitleCaseInsensitive("EFFECTIVE");
assertThat(results).hasSize(1);
assertThat(results.get(0).getTitle()).isEqualTo("Effective Java");
```

Against a real Postgres engine, `ILIKE` performs a genuine case-insensitive match —
`"EFFECTIVE"` correctly finds `"Effective Java"`. The point isn't this specific query;
it's that whatever a repository method actually does against Postgres is tested against
**the genuine engine**, not an approximation that might quietly diverge from it on
edge cases neither H2 nor its maintainers have prioritized replicating exactly.

---

## 5. A real, encountered Windows/Docker Desktop compatibility issue

Running this lesson's test on this machine produced a real, reproducible failure:

```
NpipeSocketClientProviderStrategy: failed with exception BadRequestException
(Status 400: {"ID":"","Containers":0, ... ,"Labels":["com.docker.desktop.address=npipe://\\.\pipe\docker_cli"], ...})
Could not find a valid Docker environment. Please check configuration.
```

`docker ps` and `docker run` (used directly, as in lesson 37) worked fine on this same
machine throughout this whole session — the Docker CLI and this specific Docker Desktop
version communicate over a different path than the one Testcontainers' underlying
`docker-java` library uses. Every named pipe tried (`docker_engine`,
`dockerDesktopLinuxEngine`, the CLI's own default) returned the same stub response
pointing at `docker_cli` — a pipe `docker-java` doesn't know how to speak to directly.
This is a known, documented issue with recent Docker Desktop versions on Windows.

**The standard fix**: Docker Desktop → Settings → General → "Expose daemon on
`tcp://localhost:2375` without TLS" — this opens a genuine TCP endpoint `docker-java`
(and therefore Testcontainers) can reach directly, sidestepping the named-pipe proxy
layer entirely. It's a real security trade-off (any local process could then control
Docker with no authentication), which is exactly why this lesson documents the fix
rather than applying it automatically.

---

## 6. Why this matters even though it wasn't verified live here

The gap this section reveals is itself the whole argument for Testcontainers existing:
an environment-specific quirk (this exact Docker Desktop version's named-pipe behavior
on this exact machine) is precisely the kind of thing that's invisible when every test
runs against H2, and only surfaces the moment something tries to talk to a real engine.
A CI pipeline running Linux containers directly against a Linux Docker daemon (the
overwhelmingly common real-world setup) would not hit this Windows-specific pipe issue
at all — it's a genuine, environment-specific finding, not a flaw in the Testcontainers
approach itself.

---

## 7. Summary

- **Testcontainers spins up a real, disposable database container** for a test class,
  closing the gap between "passes against an approximation" and "actually works against
  the real engine" — exactly the class of bug `ILIKE` and other database-specific syntax
  can hide from H2.
- **`@Testcontainers` + `@Container` (static)** manages one container's lifecycle,
  shared across every test method in the class.
- **`@ServiceConnection`** (Spring Boot 3.1+) wires a container's connection details into
  the `DataSource` automatically — no manual `@DynamicPropertySource` property
  registration needed.
- **This lesson's code is correct and compiles**, but the actual container run could not
  be completed in this session due to a real, documented Windows/Docker Desktop
  named-pipe compatibility issue with Testcontainers' Java client — captured and
  explained rather than papered over.
- **The standard fix** (exposing the Docker daemon over TCP) is a real security
  trade-off, appropriately left for you to decide on your own machine rather than
  applied automatically.

This closes section 08 — the testing pyramid, start to finish: a zero-Spring unit test
(lesson 46), a web-layer slice (47), a data-layer slice (48), a full integration test
(49), and, here, the real-database variant of that full integration test.

---

**Previous:** [49 — Full integration tests with `@SpringBootTest`](../49-full-integration-tests-with-springboottest/49-full-integration-tests-with-springboottest.md) ·
**Next:** [51 — Transaction propagation and isolation](../../09-transactions-async-and-scheduling/51-transaction-propagation-and-isolation/51-transaction-propagation-and-isolation.md)
