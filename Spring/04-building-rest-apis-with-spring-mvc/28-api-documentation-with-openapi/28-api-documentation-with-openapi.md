# 28 · API documentation with OpenAPI

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/28-api-documentation-with-openapi spring-boot:run
> ```
> Raw spec: `curl http://localhost:8080/v3/api-docs` ·
> Human UI: open `http://localhost:8080/swagger-ui/index.html` in a browser

Every lesson in this section built an API a human had to read the source code to
understand. This lesson adds a dependency that generates a complete, accurate API
specification **from that same source code**, automatically, every time it changes.

---

## 1. Not a Spring Boot dependency — needs its own version

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.6.0</version>
</dependency>
```

Unlike every dependency used since lesson 16, this one **needs an explicit `<version>`**
— it isn't part of `spring-boot-dependencies`, the BOM `spring-boot-starter-parent`
imports. springdoc is a third-party project (not maintained by the Spring team) that
inspects a running application's own `@RestController`, `@RequestMapping`, and DTO
classes via reflection at startup, and builds a live OpenAPI document from what it finds
— nothing here is a separate spec file written and maintained by hand.

---

## 2. The generated spec, unedited

```bash
curl http://localhost:8080/v3/api-docs
```

Real output (formatted for readability) from this lesson's two-endpoint controller:

```json
{
  "openapi": "3.0.1",
  "info": {"title": "Book Catalog API", "version": "1.0.0", "description": "..."},
  "tags": [{"name": "Books", "description": "Operations for managing the book catalog"}],
  "paths": {
    "/api/books/{id}": { "get": {
        "tags": ["Books"], "summary": "Fetch a single book",
        "description": "Returns 404 if the id does not exist.",
        "parameters": [{"name": "id", "in": "path", "description": "The book's numeric id",
                         "required": true, "schema": {"type": "integer", "format": "int64"}}]
    }},
    "/api/books": { "post": {
        "tags": ["Books"], "summary": "Create a new book",
        "requestBody": {"content": {"application/json": {"schema": {"$ref": "#/components/schemas/CreateBookRequest"}}}}
    }}
  },
  "components": { "schemas": {
      "CreateBookRequest": {
        "required": ["author", "title"],
        "properties": {
          "title": {"type": "string", "description": "The book's title", "example": "Effective Java"},
          "priceCents": {"minimum": 0, "type": "integer", "description": "Price in cents (never negative)", "example": 2999}
        }
      }
  }}
}
```

Every piece of this was inferred or read from annotations already on the code — nothing
in this JSON was typed by hand into a spec file.

---

## 3. Bean Validation constraints become schema rules, automatically

```json
"required": ["author", "title"],
"priceCents": {"minimum": 0, ...}
```

`CreateBookRequest` has `@NotBlank` on `title`/`author` and `@Min(0)` on `priceCents`
(lesson 26's exact annotations). springdoc reads these and translates them directly:
`@NotBlank` becomes a `required` schema entry, `@Min(0)` becomes `"minimum": 0`. **This
is exactly why lesson 26's validation should be written on the DTO, not re-implemented as
free-standing checks in the controller body** — the same annotations that make the API
*reject* bad input also make the generated documentation *describe* what valid input
looks like, for free, from one source of truth.

---

## 4. Documenting what validation alone can't express

```java
@NotBlank
@Schema(description = "The book's title", example = "Effective Java")
private String title;
```

```java
@Tag(name = "Books", description = "Operations for managing the book catalog")
@RestController
public class BookController { ... }
```

```java
@Operation(summary = "Fetch a single book", description = "Returns 404 if the id does not exist.")
@GetMapping("/{id}")
public ResponseEntity<BookResponse> findById(
        @Parameter(description = "The book's numeric id") @PathVariable Long id) { ... }
```

| Annotation | Documents |
| --- | --- |
| `@Schema` | A field's description and a concrete example value |
| `@Tag` | Groups every endpoint in a controller under one heading |
| `@Operation` | An endpoint's summary and free-text description |
| `@Parameter` | What a specific path variable or request param actually means |
| `@ApiResponse` | What a specific status code means for this endpoint |

None of this is inferable from Bean Validation or the method signature alone — a
`@PathVariable Long id` tells a reader *that* an id is required, not *what it identifies*
or *why a 404 is possible*. These annotations fill exactly that gap, and — because
they're read from the same source as the code that runs — can never drift out of sync
with it the way a hand-maintained separate spec document eventually does.

---

## 5. Document-level metadata

```java
@Bean
public OpenAPI bookApiInfo() {
    return new OpenAPI().info(new Info()
            .title("Book Catalog API").version("1.0.0")
            .description("Lesson 28 of the Spring stack..."));
}
```

Without this bean, springdoc still generates a complete, correct spec — it just uses
generic placeholder text for the title/version/description shown at the top of the
document and the Swagger UI page. This bean is the one piece of the whole spec that
genuinely has to be written by hand, because nothing in the code says what the *API as a
whole* is called.

---

## 6. Summary

- **springdoc** generates a live OpenAPI document by reflecting over a running
  application's own controllers and DTOs — not a separate file to keep in sync by hand.
- It needs its **own explicit `<version>`** — it isn't part of the
  `spring-boot-dependencies` BOM (lesson 16).
- **Bean Validation constraints (lesson 26) become schema rules automatically** —
  `@NotBlank` → `required`, `@Min(0)` → `"minimum": 0` — one source of truth for both
  runtime validation and generated documentation.
- **`@Schema`, `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse`** document intent that
  can't be inferred from the code alone (what a value means, why a status code appears),
  and stay attached to the code they describe.
- **`/v3/api-docs`** is the raw, machine-readable spec; **`/swagger-ui/index.html`** is a
  human-browsable, interactive rendering of the same document — both generated from the
  same running application, always in sync with it by construction.

---

**Previous:** [27 — Pagination, sorting and filtering](../27-pagination-sorting-and-filtering/27-pagination-sorting-and-filtering.md) ·
**Next:** [29 — JPA and Hibernate fundamentals](../../05-data-access-with-spring-data-jpa/29-jpa-and-hibernate-fundamentals/29-jpa-and-hibernate-fundamentals.md)
