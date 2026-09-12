# 24 · DTOs, entities, and mapping between them

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/24-dtos-entities-and-mapping-between-them spring-boot:run
> ```

Every controller so far has returned or accepted the same class the repository stores.
This lesson builds one endpoint that leaks a field it shouldn't, right next to the fixed
version — so the difference is something you can see in real JSON, not just a rule to
take on faith.

---

## 1. The entity carries fields an API has no business exposing

```java
public class Book {
    private final Long id;
    private String title;
    private String author;
    private final Instant createdAt;
    private String internalEditorNotes;   // never meant to leave this service
}
```

`internalEditorNotes` is a realistic stand-in for the kind of field a real entity
accumulates over time — an internal flag, an audit note, eventually something genuinely
sensitive. Nothing about `@RestController` or Jackson stops a method from returning
`Book` directly and serializing every one of its fields, including this one.

---

## 2. The leak, reproduced on purpose

```java
@GetMapping("/{id}/leaky")
public ResponseEntity<Book> findByIdLeaky(@PathVariable Long id) {
    return repository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
}
```

```bash
curl http://localhost:8080/api/books/1/leaky
```
```json
{"id":1,"title":"Effective Java","author":"Joshua Bloch","createdAt":"2026-...","internalEditorNotes":"Editor note: check for a 4th edition next year"}
```

**`internalEditorNotes` is right there in the response** — this compiles cleanly, runs
without error, and is exactly what happens by default whenever a controller returns an
entity type directly. Nothing failed; nothing warned. This is the actual shape of the
bug, not a hypothetical.

---

## 3. The fix: a narrower response type

```java
public class BookResponse {
    private final Long id;
    private final String title;
    private final String author;
    private final Instant createdAt;
    // no internalEditorNotes field - full stop
}
```

```java
@GetMapping("/{id}")
public ResponseEntity<BookResponse> findById(@PathVariable Long id) {
    return repository.findById(id).map(mapper::toResponse).map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
```

```bash
curl http://localhost:8080/api/books/1
```
```json
{"id":1,"title":"Effective Java","author":"Joshua Bloch","createdAt":"2026-..."}
```

Same book, same repository, same underlying `Book` object with the sensitive field still
set on it — **and it's structurally impossible for this endpoint to leak it**, because
`BookResponse` has no field to copy it into. This is the real value of a DTO: not "we
promise not to expose this," but "there is no field here to expose it *through*." Adding
a new sensitive field to `Book` next year cannot silently leak through `BookResponse` —
whoever adds it would have to also, deliberately, add it to `BookResponse` and the
mapper for it to ever appear.

---

## 4. The same idea on the way in: request DTOs

```java
public class CreateBookRequest {
    private String title;
    private String author;
    // no id, no createdAt, no internalEditorNotes
}
```

```bash
curl -X POST http://localhost:8080/api/books -H "Content-Type: application/json" \
     -d '{"title":"Test-Driven Development","author":"Kent Beck","id":999,"internalEditorNotes":"client tried to inject this"}'
```
```json
{"id":2,"title":"Test-Driven Development","author":"Kent Beck","createdAt":"2026-..."}
```

The request body **included** `id: 999` and an `internalEditorNotes` value — and neither
had any effect. `CreateBookRequest` has no field for either, so Jackson has nowhere to
put them; they're silently ignored, and the server assigns its own `id` (`2`) regardless
of what the client sent. This closes off an entire category of bug: a client can never
set a field the request DTO doesn't declare, no matter what the entity behind it looks
like.

---

## 5. The mapping code lives in one place

```java
@Component
public class BookMapper {
    public BookResponse toResponse(Book book) { ... }
    public Book toEntity(CreateBookRequest request) { ... }
}
```

Every conversion between shapes goes through this one class — a controller never calls
`BookResponse`'s or `Book`'s constructor directly for this purpose. This matters for the
same reason a single `NotificationProperties` class mattered in lesson 05: one place to
look, one place to change, instead of the same mapping logic copy-pasted across every
controller method that needs it.

**A note on tooling**: real projects with many entities often reach for **MapStruct**, a
compile-time annotation processor that generates exactly this kind of mapping method from
an interface you declare, eliminating the hand-written boilerplate above. It's a
legitimate, widely used choice — not used in this lesson specifically so the mapping is
visible once, by hand, before reaching for a tool that generates it invisibly. Once this
shape feels obvious, MapStruct is a reasonable next tool to learn.

---

## 6. Summary

- **An entity carries whatever fields the domain or persistence layer needs** — some of
  those fields have no business appearing in an API response.
- **Returning an entity directly from a controller leaks all of its fields** — this
  compiles and runs with no warning; it's a real, easy-to-write bug, not a hypothetical.
- **A response DTO** with a deliberately narrower field set makes leaking a field
  structurally impossible, rather than a matter of remembering not to.
- **A request DTO** makes it impossible for a client to set a field it has no business
  setting — an `id`, a `createdAt`, or anything server-owned — because the field simply
  doesn't exist on that type.
- **Keep all mapping logic in one place** (a dedicated mapper class or, in a larger
  project, a generated one via MapStruct) rather than scattered across every controller
  method that needs a conversion.

---

**Previous:** [23 — `ResponseEntity` and HTTP status codes](../23-responseentity-and-http-status-codes/23-responseentity-and-http-status-codes.md) ·
**Next:** [25 — Exception handling with `@ControllerAdvice`](../25-exception-handling-with-controlleradvice/25-exception-handling-with-controlleradvice.md)
