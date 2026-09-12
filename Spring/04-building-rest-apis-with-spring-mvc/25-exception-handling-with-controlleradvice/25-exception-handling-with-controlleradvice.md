# 25 · Exception handling with `@ControllerAdvice`

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/25-exception-handling-with-controlleradvice spring-boot:run
> ```

Lesson 22 left every binding failure returning Spring's generic
`{"timestamp":...,"error":"Bad Request"}` body. This lesson replaces it with a
structured, predictable shape — and along the way, reproduces a real bug this exact
pattern can introduce if you're not careful with it.

---

## 1. A custom exception, thrown with no error-handling code nearby

```java
public class BookNotFoundException extends RuntimeException {
    private final Long bookId;
    public BookNotFoundException(Long bookId) {
        super("No book found with id " + bookId);
        this.bookId = bookId;
    }
}
```

```java
@GetMapping("/{id}")
public Book findById(@PathVariable Long id) {
    return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
}
```

`findById` reads as pure happy-path logic — no `try`/`catch`, no `if (missing) return 404`.
Deciding what a `BookNotFoundException` *means* as an HTTP response is handled entirely
somewhere else.

---

## 2. `@RestControllerAdvice` — one place for the whole application

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFound(BookNotFoundException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(404, "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
```

`@RestControllerAdvice` is `@ControllerAdvice` + `@ResponseBody` — the exact relationship
lesson 21 described between `@RestController` and `@Controller`. It applies to **every**
`@RestController` in the application, not one — exception-handling logic for the whole
API lives in this one class, instead of a `try`/`catch` copy-pasted into every controller
method that could fail.

```bash
curl -i http://localhost:8080/api/books/999
```
```
HTTP/1.1 404
{"timestamp":"...","status":404,"error":"Not Found","message":"No book found with id 999","path":"/api/books/999"}
```

A real, structured body — the same field set every time, with an actual message a client
can act on, instead of Spring's generic default.

---

## 3. The same mechanism, fixing lesson 22's generic type-mismatch error

```java
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String message = "Parameter '" + ex.getName() + "' should be of type "
            + ex.getRequiredType().getSimpleName() + ", but was '" + ex.getValue() + "'";
    ...
}
```

```bash
curl -i http://localhost:8080/api/books/not-a-number
```
```
HTTP/1.1 400
{"...","message":"Parameter 'id' should be of type Long, but was 'not-a-number'","path":"/api/books/not-a-number"}
```

The exact same failure lesson 22 produced with a bare `{"error":"Bad Request"}` — this
`@ExceptionHandler` names Spring MVC's own internal exception type
(`MethodArgumentTypeMismatchException`) directly, and replaces its default body with one
that actually explains what went wrong.

---

## 4. A real bug this pattern can introduce — caught and fixed here

```java
@GetMapping("/{id}/quick-check")
public String quickCheck(@PathVariable Long id) {
    if (repository.findById(id).isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No book with id " + id);
    }
    return "Book " + id + " exists.";
}
```

`ResponseStatusException` is a lighter-weight alternative to a custom exception class —
Spring MVC has built-in handling for it, so it should produce a `404` with no
`@ExceptionHandler` needed at all. **Adding this lesson's catch-all handler (next
section) before adding a specific one for `ResponseStatusException` broke this
completely** — hitting `/api/books/999/quick-check` came back **`500`**, not `404`:

```
HTTP/1.1 500
{"...","error":"Internal Server Error","message":"Something went wrong. Please try again later.",...}
```

The reason: `ResponseStatusException` **is** an `Exception`, and any `@ExceptionHandler`
registered in a `@RestControllerAdvice` takes priority over Spring MVC's own built-in
default handling — including a broad `@ExceptionHandler(Exception.class)` catch-all. With
no more specific handler in the way, the catch-all intercepted it first, silently turning
an intended `404` into a misleading `500`. The fix is a dedicated handler:

```java
@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(ex.getStatusCode().value(), ex.getStatusCode().toString(), ex.getReason(), request.getRequestURI());
    return ResponseEntity.status(ex.getStatusCode()).body(body);
}
```

```bash
curl -i http://localhost:8080/api/books/999/quick-check
```
```
HTTP/1.1 404
{"...","status":404,"error":"404 NOT_FOUND","message":"No book with id 999",...}
```

**The lesson**: once a `@RestControllerAdvice` includes a broad
`@ExceptionHandler(Exception.class)`, every exception type that used to be handled by
Spring MVC's own defaults — `ResponseStatusException` included — now needs its own
explicit handler in the same class, or it silently falls through to the catch-all
instead.

---

## 5. The catch-all, and why it hides real detail

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    ErrorResponse body = new ErrorResponse(500, "Internal Server Error",
            "Something went wrong. Please try again later.", request.getRequestURI());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
}
```

```bash
curl -i http://localhost:8080/api/books/boom   # triggers a real NullPointerException
```
```
HTTP/1.1 500
{"...","message":"Something went wrong. Please try again later.",...}
```

`/api/books/boom` throws a genuine, unplanned `NullPointerException` — and the response
never mentions it. **This is deliberate.** `ex.getMessage()` for a truly unexpected
exception could leak internal detail (a file path, a fragment of a query) into a client
response; the safe default is a fixed, generic message every time, regardless of what
actually broke. The real detail still belongs in the server's own logs
(`log.error("...", ex)`, lesson 18) — just never in the HTTP response.

---

## 6. Summary

- **`@RestControllerAdvice`** applies exception handling to every `@RestController` in
  the application at once — one place, instead of duplicated `try`/`catch` blocks.
- **`@ExceptionHandler(SpecificType.class)`** can target a custom exception, or one of
  Spring MVC's own internal exceptions (`MethodArgumentTypeMismatchException`), replacing
  its default response body with a structured one.
- **A broad `@ExceptionHandler(Exception.class)` intercepts everything**, including
  exception types Spring MVC would otherwise handle itself (like
  `ResponseStatusException`) — verified here by actually breaking it, then adding the
  missing specific handler to fix it.
- **The catch-all should return a fixed, generic message** — never the real exception's
  message — to avoid leaking internal detail to a client; the real detail belongs in
  server-side logs instead.
- **`ResponseStatusException`** is a lighter-weight alternative to a dedicated exception
  class for one-off cases, but still needs its own handler the moment a broad catch-all
  exists in the same `@RestControllerAdvice`.

---

**Previous:** [24 — DTOs, entities, and mapping between them](../24-dtos-entities-and-mapping-between-them/24-dtos-entities-and-mapping-between-them.md) ·
**Next:** [26 — Bean Validation](../26-bean-validation/26-bean-validation.md)
