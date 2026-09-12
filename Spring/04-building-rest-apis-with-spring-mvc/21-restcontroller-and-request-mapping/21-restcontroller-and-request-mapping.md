# 21 · `@RestController` and the `@RequestMapping` family

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/21-restcontroller-and-request-mapping spring-boot:run
> ```
> Then, in another terminal:
> ```bash
> curl http://localhost:8080/api/books
> ```

Lesson 20 needed `spring-boot-starter-web` just to give Actuator an HTTP surface. This
lesson uses that same starter for what it's actually for: a real REST API, backed by an
in-memory `BookRepository` so every endpoint has something real to do.

---

## 1. `@RestController` = `@Controller` + `@ResponseBody`

```java
@RestController
@RequestMapping("/api/books")
public class BookController { ... }
```

Lesson 07 introduced `@Controller` as one of the four stereotypes, noting it does nothing
special without Spring MVC. `@RestController` is `@Controller` (still the same
stereotype, still found by component scanning) **meta-annotated with `@ResponseBody`** —
which changes what a method's return value means. Without it, `@Controller` treats a
returned `String` as the *name of a view template* to render (server-side HTML — outside
this lesson's scope). With `@ResponseBody`, a method's return value is instead serialized
directly into the HTTP response body — a `Book` object becomes JSON, automatically, via
Jackson (already on the classpath through `spring-boot-starter-web`).

`@RequestMapping("/api/books")` at the **class** level sets a base path every method
below is relative to — `findAll()`'s `@GetMapping` (no path) actually maps to
`GET /api/books`, and `findById`'s `@GetMapping("/{id}")` maps to `GET /api/books/{id}`.

---

## 2. The `@RequestMapping` shorthand family

```java
@GetMapping           public Collection<Book> findAll() { ... }
@GetMapping("/{id}")  public Book findById(@PathVariable Long id) { ... }
@PostMapping          public Book create(@RequestBody Book book) { ... }
@PutMapping("/{id}")  public Book update(@PathVariable Long id, @RequestBody Book book) { ... }
@DeleteMapping("/{id}") public void delete(@PathVariable Long id) { ... }
```

Each of these is shorthand, introduced in Spring 4.3, for the older, more verbose form
still shown once in this lesson:

```java
@RequestMapping(value = "/legacy", method = RequestMethod.GET)
public String legacyStyle() { ... }
```

`@GetMapping("/legacy")` and this are **functionally identical** — running both confirms
it: `curl http://localhost:8080/api/books/legacy` returns the same 200 either way. Use
the shorthand forms; the verbose one exists in this lesson only so it's recognisable when
it turns up in an older codebase.

---

## 3. Exercising every verb, with real responses

```bash
curl http://localhost:8080/api/books
```
```json
[{"id":1,"title":"Effective Java","author":"Joshua Bloch"},{"id":2,"title":"Clean Code","author":"Robert C. Martin"}]
```

```bash
curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" \
     -d '{"title":"Domain-Driven Design","author":"Eric Evans"}'
```
```json
{"id":3,"title":"Domain-Driven Design","author":"Eric Evans"}
```

`@RequestBody` on `create`'s parameter tells Spring to deserialize the incoming JSON
request body into a `Book`, the mirror image of `@ResponseBody` serializing one back out.
A repeated `GET /api/books` afterward includes the new book at `id: 3` — the repository
really persisted it (in memory, for this lesson).

---

## 4. A real gap this lesson deliberately leaves open

```bash
curl -i http://localhost:8080/api/books/999
```
```
HTTP/1.1 200
Content-Length: 0
```

**Status `200`, with an empty body — not `404`.** `findById` returns `repository.findById(id)`,
which is `null` for an ID that doesn't exist; `@RestController` happily serializes "no
value" as an empty response body and reports success anyway, because nothing about
Spring MVC's default handling of a plain return type distinguishes "found nothing" from
"found an empty result." A REST client has no reliable way to tell these apart from the
status code alone. **This is a genuine, common bug in real APIs** — fixing it needs
`ResponseEntity` and an explicit decision about what "not found" should return, which is
exactly lesson 23's subject.

---

## 5. What Spring MVC does with an unmatched request

```bash
curl -o /dev/null -w "%{http_code}" http://localhost:8080/api/nonexistent   # 404
curl -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/books  # 405
```

Two different failures, two different status codes, entirely inferred by Spring MVC
from the registered `@RequestMapping`s — no code in `BookController` handles either
case:

- **`404 Not Found`** — no `@RequestMapping` matches this **path** at all.
- **`405 Method Not Allowed`** — the **path** `/api/books` matches (`GET`, `POST` are
  mapped there), but no method is mapped there for **`DELETE`** specifically.

This distinction is genuinely useful for API consumers: a `404` says "this resource
doesn't exist here," a `405` says "this resource exists, but not for the verb you used" —
and Spring MVC derives both automatically from the same `@GetMapping`/`@PostMapping`/etc.
declarations, with zero extra code.

---

## 6. Summary

- **`@RestController`** = `@Controller` (a stereotype, lesson 07) + `@ResponseBody`
  (serializes return values into the response body instead of resolving a view name).
- **`@RequestMapping`** on a class sets a base path every method's mapping is relative
  to.
- **`@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping`** are shorthand for
  `@RequestMapping(method = RequestMethod.X)` — prefer them; the verbose form still
  works identically.
- **`@RequestBody`** deserializes an incoming JSON body into a Java object;
  **`@ResponseBody`** (implied by `@RestController`) serializes one back out — Jackson
  does both conversions automatically.
- **Returning `null` from a controller method produces `200` with an empty body, not
  `404`** — a real, common bug, and the reason lesson 23 introduces `ResponseEntity`.
- **`404`** means no path matched; **`405`** means the path matched but not for that
  HTTP method — both derived automatically from the registered mappings.

---

**Previous:** [20 — Actuator basics](../../03-spring-boot-fundamentals/20-actuator-basics/20-actuator-basics.md) ·
**Next:** [22 — Path variables, request params and request bodies](../22-path-variables-request-params-and-request-bodies/22-path-variables-request-params-and-request-bodies.md)
