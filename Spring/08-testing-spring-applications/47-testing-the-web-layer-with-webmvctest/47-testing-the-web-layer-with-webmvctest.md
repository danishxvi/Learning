# 47 · Testing the web layer with `@WebMvcTest`

> **Run the tests for this lesson**
> ```bash
> mvn -f Spring/08-testing-spring-applications/47-testing-the-web-layer-with-webmvctest test
> ```

Lesson 46 tested a service class with zero Spring involved. This lesson sits between
that and a full `@SpringBootTest` (lesson 49): real Spring MVC request handling —
routing, JSON conversion, validation, exception translation — without starting the
whole application.

---

## 1. `@WebMvcTest` — one narrow slice of the application

```java
@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private BookService bookService;
}
```

Real startup log, unedited:
```
Initializing Spring TestDispatcherServlet ''
Started BookControllerTest in 3.444 seconds
```

**`TestDispatcherServlet`, not a real embedded Tomcat.** `@WebMvcTest(BookController.class)`
loads only what the web layer needs — this controller, `@RestControllerAdvice` beans,
Jackson's message converters, Bean Validation — and registers `BookService` as a
`@MockBean` instead of requiring a real implementation anywhere on the classpath. No
`@Repository`, no database, no other controller. `3.444` seconds is slower than lesson
46's zero-Spring test (`1.5` seconds for the *whole suite*) but meaningfully narrower
than a full application context — lesson 49 measures that comparison directly.

---

## 2. `MockMvc` — real Spring MVC dispatching, no network involved

```java
mockMvc.perform(get("/api/books/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.title").value("Effective Java"));
```

`MockMvc` sends a request through Spring MVC's **real** dispatching machinery — the same
path-matching, argument-binding, and JSON-serialization code that runs in production —
without opening a socket or starting a server. `jsonPath("$.title")` reads the actual
JSON response body Jackson produced, not a hand-inspected object.

---

## 3. `@MockBean` vs lesson 46's `@Mock`

```java
@MockBean
private BookService bookService;
```

Lesson 46's `@Mock` created a plain object nothing else knew about — the test wired it
in by hand. `@MockBean` does something different: it registers the mock **into the
Spring context itself**, replacing whatever real `BookService` bean would otherwise
exist, so `BookController` receives it through completely ordinary dependency
injection — indistinguishable, from the controller's point of view, from a real
implementation.

---

## 4. `@RestControllerAdvice` is part of the slice — verified

```java
when(bookService.findById(999L)).thenThrow(new BookNotFoundException(999L));

mockMvc.perform(get("/api/books/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("No book with id 999"));
```

The mocked service throws; `GlobalExceptionHandler` (lesson 25's exact pattern) catches
it and produces the real JSON error body — proving the controller advice genuinely
intercepts an exception coming from a mocked dependency, exactly as it would from a real
one. This is something lesson 46's plain unit test structurally could not test at all —
there's no `@RestControllerAdvice` mechanism to invoke without Spring MVC's real
dispatching in the loop.

---

## 5. Bean Validation runs for real, inside the slice

```java
mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"author\":\"Robert C. Martin\"}"))
        .andExpect(status().isBadRequest());
```

Real, unedited log line from this test run:
```
Resolved [MethodArgumentNotValidException: Validation failed for argument [0] ...
Field error in object 'createBookRequest' on field 'title': rejected value [];
... default message [must not be blank]]
```

`@Valid` on `BookController.create`'s parameter (lesson 26) runs exactly as it would in
production — the blank `title` is rejected before `bookService.create(...)` is ever
called, confirmed by the mock never needing a stub for this case at all.

---

## 6. Summary

- **`@WebMvcTest(Controller.class)`** loads only the web layer for that controller —
  `@RestControllerAdvice`, JSON conversion, and Bean Validation are real; everything
  else (services, repositories, the database) is absent unless explicitly mocked.
- **`MockMvc`** dispatches a request through Spring MVC's real routing and conversion
  code, with no network call and no running server.
- **`@MockBean`** registers a mock *into the Spring context*, replacing a real bean —
  different from lesson 46's plain `@Mock`, which the test wired in by hand.
- **`@RestControllerAdvice` exception translation is testable in this slice** — a mocked
  service throwing an exception genuinely exercises the same handler a real failure
  would.
- **Startup here (`~3.4s`) sits between a zero-Spring unit test (`~1.5s` total, lesson
  46) and a full `@SpringBootTest`** — narrower than the whole application, but not free.

---

**Previous:** [46 — Unit testing with JUnit 5 and Mockito](../46-unit-testing-with-junit5-and-mockito/46-unit-testing-with-junit5-and-mockito.md) ·
**Next:** [48 — Testing the data layer with `@DataJpaTest`](../48-testing-the-data-layer-with-datajputest/48-testing-the-data-layer-with-datajputest.md)
