# 23 · `ResponseEntity` and HTTP status codes

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/23-responseentity-and-http-status-codes spring-boot:run
> ```

Lesson 21 ended with a real bug: a missing book returned `200` with an empty body
instead of `404`, because a plain `Book` return type gives Spring MVC nothing to decide
a status code *from*. This lesson fixes it — the exact same repository and controller
shape, wrapped in `ResponseEntity<T>` everywhere a status code is an actual decision.

---

## 1. `Optional<Book>` — the return type says "might not exist"

```java
public Optional<Book> findById(Long id) {
    return Optional.ofNullable(books.get(id));
}
```

Compare this to lesson 21's plain `Book findById(Long id)`, which silently returned
`null` for a missing book. `Optional<Book>` makes "this might not exist" part of the
method's own signature — the compiler and the caller both know absence is a real,
expected outcome, not an oversight.

---

## 2. `ResponseEntity<T>` — status, headers, and body, together

```java
@GetMapping("/{id}")
public ResponseEntity<Book> findById(@PathVariable Long id) {
    return repository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

```bash
curl -i http://localhost:8080/api/books/1     # HTTP/1.1 200, body: {"id":1,...}
curl -i http://localhost:8080/api/books/999   # HTTP/1.1 404, Content-Length: 0
```

**A genuine `404` this time** — `Content-Length: 0`, no body at all, not the `200` with an
empty body lesson 21 produced. `Optional.map`/`orElseGet` reads naturally as exactly the
two outcomes: found → `200` with the book; not found → `404` with nothing. This is the
fix lesson 21 promised.

---

## 3. `201 Created`, with a `Location` header

```java
@PostMapping
public ResponseEntity<Book> create(@RequestBody Book book) {
    Book saved = repository.save(book);
    URI location = URI.create("/api/books/" + saved.getId());
    return ResponseEntity.created(location).body(saved);
}
```

```bash
curl -i -X POST http://localhost:8080/api/books -H "Content-Type: application/json" -d '{"title":"Refactoring","author":"Martin Fowler"}'
```
```
HTTP/1.1 201
Location: /api/books/2

{"id":2,"title":"Refactoring","author":"Martin Fowler"}
```

**`201`, not `200`** — a genuinely new resource was created, and REST convention (and
many API clients/tools) expect `201` specifically for that, distinct from `200`'s "an
existing operation succeeded." The `Location` header names exactly where the new
resource can now be fetched from — `ResponseEntity.created(uri)` sets both the status and
the header in one call.

---

## 4. `204 No Content` — success, with nothing to say

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    boolean deleted = repository.deleteById(id);
    return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
}
```

```bash
curl -i -X DELETE http://localhost:8080/api/books/1   # HTTP/1.1 204, no body
curl -i -X DELETE http://localhost:8080/api/books/1   # HTTP/1.1 404 - already gone
```

`204` means "the operation succeeded, and there is genuinely nothing meaningful to
return" — distinct from `200`, which implies a body is coming. Deleting an
already-deleted (or never-existing) book correctly falls through to `404` instead — the
same "does it exist" check `findById` used, reused for a different verb.

---

## 5. `ResponseEntity.status(...)` — the general escape hatch

```java
@GetMapping("/{id}/reserve")
public ResponseEntity<String> reserve(@PathVariable Long id) {
    if (repository.findById(id).isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No book with id " + id);
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Reservation for book " + id + " is being processed");
}
```

```bash
curl -i http://localhost:8080/api/books/1/reserve   # 404, "No book with id 1"
curl -i http://localhost:8080/api/books/2/reserve   # 202, "Reservation for book 2 is being processed"
```

`202 Accepted` — a real status meaning "the request is valid and understood, but the
work isn't finished yet" (used for long-running or asynchronous operations). Convenience
methods (`ok`, `created`, `notFound`, `noContent`) don't exist for every status code;
`ResponseEntity.status(HttpStatus.X)` covers any of them, and — unlike `notFound()` and
`noContent()`, which are always empty-bodied — still accepts a `.body(...)` when the
status needs an explanation.

---

## 6. Summary

- **`Optional<T>`** as a repository's return type makes "might not exist" part of the
  method signature, instead of a `null` the caller has to remember to check.
- **`ResponseEntity<T>`** bundles status code, headers, and body into one explicit
  return value — `Optional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build())`
  is the standard shape for "found vs not found."
- **`201 Created`** (with `ResponseEntity.created(uri)`) is the correct status for a
  successful creation, and sets the `Location` header pointing at the new resource in
  one call.
- **`204 No Content`** is the correct status for a successful operation with nothing
  meaningful to return — distinct from `200`, which implies a body.
- **`ResponseEntity.status(HttpStatus.X)`** is the general-purpose form for any status
  the named convenience methods don't cover, and still accepts a body.
- Returning a plain object and letting Spring MVC infer a status code (lesson 21) is
  fine for the always-succeeds case; the moment "not found," "created," or "accepted but
  not finished" are real possible outcomes, `ResponseEntity` makes the status an explicit
  decision instead of an accident.

---

**Previous:** [22 — Path variables, request params and request bodies](../22-path-variables-request-params-and-request-bodies/22-path-variables-request-params-and-request-bodies.md) ·
**Next:** [24 — DTOs, entities, and mapping between them](../24-dtos-entities-and-mapping-between-them/24-dtos-entities-and-mapping-between-them.md)
