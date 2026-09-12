# 27 · Pagination, sorting and filtering

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/04-building-rest-apis-with-spring-mvc/27-pagination-sorting-and-filtering spring-boot:run
> ```

Every list endpoint so far has returned everything at once. A real API with thousands of
rows can't — this lesson builds pagination, sorting, and filtering from scratch, with no
database involved yet, so the concepts are visible before section 05 hands them to
Spring Data to do automatically.

---

## 1. The page envelope

```java
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> of(List<T> pageContent, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(pageContent, page, size, totalElements, totalPages);
    }
}
```

Almost every paginated API returns this same shape: the actual slice of results
(`content`), which page this is, how big a page was requested, and enough metadata
(`totalElements`, `totalPages`) for a client to build "next page" / "page 3 of 5" UI
without a separate request. Section 05 introduces Spring Data's own `Page<T>`
interface, which carries exactly this information once a real database and repository
are involved — this record is the plain, from-scratch version of the same idea.

---

## 2. Order of operations: filter, then sort, then page

```java
List<Book> filtered = repository.findAll().stream()
        .filter(book -> author == null || book.author().toLowerCase().contains(author.toLowerCase()))
        .toList();

Comparator<Book> comparator = switch (sortBy) {
    case "author" -> Comparator.comparing(Book::author);
    default -> Comparator.comparing(Book::title);
};
if ("desc".equalsIgnoreCase(direction)) comparator = comparator.reversed();
List<Book> sorted = filtered.stream().sorted(comparator).toList();

List<Book> pageContent = sorted.stream().skip((long) page * size).limit(size).toList();
```

**This order matters.** Paging before filtering would slice a page out of the *entire*
unfiltered dataset, then filter that slice — silently returning fewer results than the
requested page size, or an empty page that isn't actually the last one. Filter first, to
establish the real working set; sort that working set; then page the sorted result. Real
database-backed pagination (Spring Data, in section 05) applies the same order, just
pushed down into the SQL itself (`WHERE` clause, then `ORDER BY`, then `LIMIT`/`OFFSET`)
instead of Java streams.

---

## 3. Seeing all three together

```bash
curl "http://localhost:8080/api/books?page=0&size=5"
```
```json
{"content":[...,"Building Microservices",...,"Design Patterns"],"page":0,"size":5,"totalElements":12,"totalPages":3}
```

```bash
curl "http://localhost:8080/api/books?sortBy=author&direction=desc&size=3"
```
```json
{"content":[{"author":"Sam Newman",...},{"author":"Robert C. Martin",...},{"author":"Robert C. Martin",...}],...,"totalPages":4}
```

```bash
curl "http://localhost:8080/api/books?author=martin&size=10"
```
```json
{"content":[...,"Robert C. Martin",...,"Robert C. Martin",...,"Martin Fowler"],"totalElements":3,"totalPages":1}
```

`totalElements` in the filtered response is `3`, not `12` — it reflects the **filtered**
count, which is exactly why filtering had to happen before pagination math: `totalPages`
depends on how many rows actually matched, not how many exist overall.

---

## 4. Sorting: allow-list the fields, never sort reflectively

```java
Comparator<Book> comparator = switch (sortBy) {
    case "author" -> Comparator.comparing(Book::author);
    default -> Comparator.comparing(Book::title);
};
```

`sortBy` is a client-supplied string, matched against an explicit, small set of known
fields — **never** used to reflectively look up a field or getter by that name. Sorting
reflectively on an arbitrary client-supplied string would let a client probe internal
field names that were never meant to be part of the API's public contract (an internal
`id` ordering revealing insertion order, or worse, a field that doesn't exist triggering
an unhandled exception). An unrecognised `sortBy` value here just falls through to the
`default` case instead of failing — a deliberate, permissive choice; a stricter API might
instead return a `400` for an unknown sort field.

---

## 5. Summary

- **A page envelope** (`content`, `page`, `size`, `totalElements`, `totalPages`) is the
  standard shape almost every paginated API returns — Spring Data's `Page<T>`
  (section 05) carries the same information once a database is involved.
- **Filter, then sort, then page — in that order.** Paging before filtering produces
  wrong results: a short page, or a page count based on the wrong total.
- **`totalElements`/`totalPages` must reflect the filtered set**, not the whole dataset —
  proven here by a filtered request returning `totalElements: 3` instead of `12`.
- **Allow-list sortable fields explicitly** (a `switch` over known field names) rather
  than sorting reflectively off a client-supplied string — reflection here would expose
  internal field names as part of the API's contract, unintentionally.

---

**Previous:** [26 — Bean Validation](../26-bean-validation/26-bean-validation.md) ·
**Next:** [28 — API documentation with OpenAPI](../28-api-documentation-with-openapi/28-api-documentation-with-openapi.md)
