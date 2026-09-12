# 22 · Path variables, request params and request bodies

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/22-path-variables-request-params-and-request-bodies spring-boot:run
> ```

Lesson 21 used `@PathVariable`, `@RequestBody` and `@RequestParam` (implicitly, through
`page`/`size` — actually not yet). This lesson goes through every binding annotation
Spring MVC offers for pulling data out of an HTTP request, including what happens when
the request doesn't match what the method expects.

---

## 1. `@PathVariable` — named explicitly when it must be

```java
@GetMapping("/{category}/{id}")
public String byCategoryAndId(@PathVariable("category") String categoryName, @PathVariable Long id) {
```

```bash
curl http://localhost:8080/api/catalog/electronics/42
```
```
category=electronics, id=42 (id was converted String -> Long automatically)
```

`id` needs no explicit name because the parameter is already called `id`, matching the
`{id}` placeholder. `categoryName` doesn't match `{category}`, so
`@PathVariable("category")` names the placeholder explicitly — without it, Spring has no
reliable way to connect `categoryName` back to `{category}` unless the project was
compiled with the `-parameters` flag (lesson 15 mentioned this exact flag, in a different
context). Naming it explicitly works regardless of that compiler setting, and is the
safer habit.

**Type conversion happens automatically** — `"42"` in the URL becomes a real `Long`
before the method ever runs. Supplying something that isn't a valid `Long`:

```bash
curl -i http://localhost:8080/api/catalog/electronics/not-a-number
```
```
HTTP/1.1 400
```

A genuine `400 Bad Request`, produced entirely by Spring MVC's own argument-binding
layer — `byCategoryAndId`'s body never runs at all.

---

## 2. `@RequestParam` — required, optional, repeated, and catch-all

```java
public String search(@RequestParam String query,
                      @RequestParam(defaultValue = "0") int page,
                      @RequestParam(defaultValue = "10") int size) {
```

```bash
curl "http://localhost:8080/api/catalog/search?query=java&page=2"
```
```
query="java", page=2, size=10
```

`query` has no default — it's **required**. Omitting it entirely:

```bash
curl -i "http://localhost:8080/api/catalog/search"
```
```
HTTP/1.1 400
{"timestamp":"...","status":400,"error":"Bad Request","path":"/api/catalog/search"}
```

Another `400`, this time from a missing required parameter — not a `null` silently
reaching the method body. `page`/`size` have `defaultValue`, always supplied **as a
`String`** even though the parameter is `int` — the same conversion happens as for any
other request param.

**Repeated query parameters** bind to a `List`:

```java
public String tags(@RequestParam List<String> tag) { ... }
```
```bash
curl "http://localhost:8080/api/catalog/tags?tag=java&tag=spring"   # tags=[java, spring]
```

**A catch-all for parameters not known in advance** binds to a `Map`:

```java
public Map<String, String> filter(@RequestParam Map<String, String> allParams) { ... }
```
```bash
curl "http://localhost:8080/api/catalog/filter?color=red&size=L"   # {"color":"red","size":"L"}
```

---

## 3. `@RequestHeader`

```java
public String whoami(@RequestHeader(value = "X-Client-Id", defaultValue = "anonymous") String clientId) { ... }
```

```bash
curl http://localhost:8080/api/catalog/whoami                          # anonymous
curl -H "X-Client-Id: mobile-app" http://localhost:8080/api/catalog/whoami  # mobile-app
```

Works exactly like `@RequestParam` — required by default, optional with `defaultValue`
— just reading from HTTP headers instead of the query string.

---

## 4. `@RequestBody` and a malformed request

```bash
curl -i -X POST http://localhost:8080/api/catalog -H "Content-Type: application/json" -d '{title: bad json'
```
```
HTTP/1.1 400
{"timestamp":"...","status":400,"error":"Bad Request","path":"/api/catalog"}
```

Invalid JSON never reaches `create`'s body at all — Jackson fails to deserialize it into
a `NewBookRequest` before Spring MVC even calls the controller method, and that failure
becomes a `400` automatically. This is the same category of error as the missing
`query` parameter and the bad `Long` path variable above: **all three are argument
*binding* failures**, handled identically by Spring MVC before any application code
runs, which is why all three come back `400` with the same generic error shape. Lesson
25 replaces that generic `{"timestamp":...,"error":"Bad Request"}` body with a
custom, structured error response.

---

## 5. Summary

- **`@PathVariable`**: name it explicitly with `@PathVariable("name")` whenever the URL
  placeholder and the method parameter name differ. Type conversion (`String` → `Long`,
  etc.) is automatic, and a failed conversion is a `400`.
- **`@RequestParam`**: required by default; `defaultValue` makes it optional (always
  supplied as a `String`, converted like any other value). Repeat the same query key for
  a `List`; bind to a `Map<String, String>` for an unknown-in-advance set of parameters.
- **`@RequestHeader`** works exactly like `@RequestParam`, reading HTTP headers instead
  of the query string.
- **`@RequestBody`** deserializes the request body via Jackson before the controller
  method runs — malformed JSON never reaches application code, and becomes a `400`
  automatically, the same way a missing required parameter or a bad path variable does.
- **Every binding failure covered here returns the same generic `400` error shape** —
  Spring MVC's own default, replaced with something structured in lesson 25.

---

**Previous:** [21 — `@RestController` and the `@RequestMapping` family](../21-restcontroller-and-request-mapping/21-restcontroller-and-request-mapping.md) ·
**Next:** [23 — `ResponseEntity` and HTTP status codes](../23-responseentity-and-http-status-codes/23-responseentity-and-http-status-codes.md)
