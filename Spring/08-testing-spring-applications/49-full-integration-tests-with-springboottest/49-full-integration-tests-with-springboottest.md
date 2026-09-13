# 49 · Full integration tests with `@SpringBootTest`

> **Run the tests for this lesson**
> ```bash
> mvn -f Spring/08-testing-spring-applications/49-full-integration-tests-with-springboottest test
> ```

Lessons 47–48 tested narrow slices. This lesson uses the full application — a real
embedded server on a real (randomly chosen) port, real HTTP calls, and a real database —
the most expensive and most realistic test this whole section covers.

---

## 1. `RANDOM_PORT` — an actual embedded server, not a mock

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookIntegrationTest {
    @LocalServerPort
    private int port;
}
```

Real, unedited startup log:
```
Tomcat initialized with port 0 (http)
...
Tomcat started on port 58169 (http) with context path '/'
Started BookIntegrationTest in 5.63 seconds
```
```
Embedded server actually running on port: 58169
```

**A genuine embedded Tomcat, bound to a real (if temporary) port** — `port 0` tells the
OS "pick any free port," which is exactly why `RANDOM_PORT` exists: parallel test runs
on the same machine, or a CI server running many builds at once, never collide over a
fixed port the way `8080` might. `@LocalServerPort` injects whatever the OS actually
chose. This is unlike lesson 47's `@WebMvcTest`, which used a `TestDispatcherServlet` —
nothing there ever opened a real network socket at all.

---

## 2. `TestRestTemplate` — real HTTP, not simulated dispatching

```java
ResponseEntity<Book> createResponse = restTemplate.postForEntity(
        "/api/books", new Book("Effective Java", "Joshua Bloch"), Book.class);

assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
Long id = createResponse.getBody().getId();

assertThat(bookRepository.findById(id)).isPresent();   // really in the database

ResponseEntity<Book> getResponse = restTemplate.getForEntity("/api/books/" + id, Book.class);
assertThat(getResponse.getBody().getTitle()).isEqualTo("Effective Java");
```

Both tests pass. Unlike `MockMvc` (lesson 47), `TestRestTemplate` makes an **actual
network call** to `localhost:<port>` — through the real embedded Tomcat, real Spring
MVC dispatching, the real `BookController`, the real `BookRepository`, into a real (if
in-memory) database. The test even checks the repository directly after the `POST` to
confirm the row genuinely exists — not just that the controller *returned* something
that looked plausible.

---

## 3. The real cost of each testing slice, measured across this section

| Lesson | Mechanism | Measured startup |
| --- | --- | --- |
| 46 | Plain `@Mock`, no Spring | `~1.5s` for the whole 6-test suite |
| 47 | `@WebMvcTest` (`TestDispatcherServlet`) | `~3.4s` |
| **49** | **`@SpringBootTest(RANDOM_PORT)`** (real Tomcat + real JPA + HikariCP) | **`~5.6s`** |

This isn't a hypothetical trade-off — it's the same three-lesson progression in this
section, each one genuinely measured. Every layer this lesson adds back (a real
`DataSource`, a real connection pool, a real embedded server) costs real startup time,
which is exactly why the testing pyramid convention (many fast unit tests, fewer slice
tests, still fewer full integration tests) exists: a full `@SpringBootTest` proves the
most, at the highest cost per test, so a project runs relatively few of them and relies
on lessons 46–48's cheaper, narrower tests for everything a slice can already confirm.

---

## 4. A real 404, over real HTTP

```java
ResponseEntity<Book> response = restTemplate.getForEntity("/api/books/999999", Book.class);
assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
```

`BookController.findById`'s `ResponseEntity`-based not-found handling (lesson 23's
exact pattern) is exercised here through a real HTTP round trip, not a direct method
call — proving the actual serialized response a real client would receive, not just
that the Java method returns the right object internally.

---

## 5. When each slice is the right tool

- **Lesson 46** (plain unit test): business logic in one class, with dependencies
  mocked by hand — the fast, default choice for most test cases.
- **Lesson 47** (`@WebMvcTest`): does this controller route, validate, and translate
  exceptions correctly — without paying for a database or a real server.
- **Lesson 48** (`@DataJpaTest`): does this repository query actually work against a
  real (if in-memory) database — without paying for the web layer.
- **This lesson** (`@SpringBootTest`, `RANDOM_PORT`): does the *whole* stack work
  together, end to end, exactly as a real client would experience it — reserved for the
  handful of critical paths worth the real cost of proving completely.

---

## 6. Summary

- **`@SpringBootTest(webEnvironment = RANDOM_PORT)`** starts a genuine embedded server on
  an OS-assigned port — verified here by a real `Tomcat started on port 58169` log line,
  not a simulated dispatcher.
- **`TestRestTemplate`** makes real HTTP calls over that real port, exercising the
  complete stack: real Spring MVC dispatching, a real repository, a real database.
- **This is measurably the most expensive test type** — `~5.6s` here, against `~3.4s`
  for a `@WebMvcTest` slice and `~1.5s` for a suite of plain unit tests with no Spring
  at all — the real cost behind the testing-pyramid convention of favoring narrower
  tests where they can prove the same thing.
- **A full integration test proves the whole chain works together** — something no
  slice, by definition, can confirm on its own.

---

**Previous:** [48 — Testing the data layer with `@DataJpaTest`](../48-testing-the-data-layer-with-datajputest/48-testing-the-data-layer-with-datajputest.md) ·
**Next:** [50 — Integration testing with Testcontainers](../50-integration-testing-with-testcontainers/50-integration-testing-with-testcontainers.md)
