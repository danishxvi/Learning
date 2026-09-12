# 34 · Pagination and projections

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/34-pagination-and-projections spring-boot:run
> ```

Lesson 27 built a page envelope from scratch, over an in-memory list, with manual
`skip`/`limit`. This lesson is the real version: `Pageable`, `Page<T>`, and `Sort`,
backed by an actual database doing the `LIMIT`/`OFFSET`/`ORDER BY` work — plus
projections, which return less data than a full entity on purpose.

---

## 1. `Page<T>` — `Pageable` in, real SQL out

```java
Page<Book> page0 = repository.findAll(PageRequest.of(0, 3, Sort.by("title")));
```
```sql
select ... from book b1_0 order by b1_0.title offset ? rows fetch first ? rows only
select count(b1_0.id) from book b1_0
```

**Two queries, automatically** — the actual page of rows, and a `COUNT` to compute
`totalElements`/`totalPages`. `findAll(Pageable)` is inherited straight from
`JpaRepository` (lesson 31) — nothing was written for it. `Page<T>` carries everything
lesson 27's hand-built `PageResponse` did:

```java
page.getContent();       // the actual rows for this page
page.getNumber();        // 0-based page index
page.getTotalElements(); // count across ALL pages, not just this one
page.getTotalPages();
page.isFirst(); page.isLast(); page.hasNext();
```

Running this lesson: page `0` of size `3` returns `isFirst=true, hasNext=true`; page `1`
of the same query returns `isFirst=false` — real state, computed from the real `COUNT`,
not tracked manually.

---

## 2. A derived query with `Pageable` — lesson 32 and pagination combined

```java
Page<Book> findByAuthor(String author, Pageable pageable);
```
```sql
select ... from book b1_0 where b1_0.author=? fetch first ? rows only
select count(b1_0.id) from book b1_0 where b1_0.author=?
```

The `WHERE` clause (derived from the method name, lesson 32) and the pagination
(`Pageable`) compose automatically — **and the `COUNT` query correctly includes the same
`WHERE` clause**, so `totalElements` reflects the filtered count, not the whole table.
This is the same correctness requirement lesson 27 built by hand (filter before paging);
here, Spring Data and the database handle it together, with no separate step to get
wrong.

---

## 3. Interface projections — selecting fewer columns, not filtering rows

```java
public interface BookTitleOnly {
    String getTitle();
}
```
```java
List<BookTitleOnly> findByAuthorOrderByTitle(String author);
```
```sql
select b1_0.title from book b1_0 where b1_0.author=? order by 1
```

**Only the `title` column is selected** — not `id`, `author`, or `price_cents`. `BookTitleOnly`
has no implementation anywhere, the same "declare the shape, Spring Data builds it"
pattern as the repository interfaces themselves (lesson 31): its method names
(`getTitle`) are matched against `Book`'s property names, and Spring Data narrows the
generated `SELECT` to exactly those columns. This matters for a wide entity where a
caller genuinely only needs one or two fields — no reason to transfer, deserialize, and
hold the rest in memory.

---

## 4. DTO projections — a real class, built directly by the query

```java
@Query("SELECT new com.danish.spring.projections.BookSummary(b.title, b.author) FROM Book b WHERE b.priceCents > :minPriceCents")
List<BookSummary> findSummaries(@Param("minPriceCents") int minPriceCents);
```
```sql
select b1_0.title, b1_0.author from book b1_0 where b1_0.price_cents>?
```

The `new package.ClassName(...)` syntax inside JPQL is a **constructor expression** —
Hibernate selects exactly the listed columns and constructs `BookSummary` objects
directly from the result set. **No `Book` entity is ever materialized at all** for this
query — not created and discarded, never created in the first place. `BookSummary` is an
ordinary class (unlike the interface projection): it can be passed around, serialized, or
returned directly from a `@RestController` (lesson 24's response-DTO pattern) with no
further mapping step needed.

---

## 5. Interface vs. DTO projection — when to use which

| | Interface projection | DTO (class) projection |
| --- | --- | --- |
| Written as | An interface, no implementation | A real class with a matching constructor |
| Query source | Inferred from the interface's getters | An explicit `@Query` constructor expression |
| Best for | A quick, single-purpose "just these fields" read | A shape reused elsewhere (a response DTO, a report row) |

Both narrow the `SELECT` to fewer columns than the full entity — the choice is mostly
about whether the projected shape is a one-off or something worth being a real,
reusable class.

---

## 6. Summary

- **`Page<T>`** (from `findAll(Pageable)`, inherited free from `JpaRepository`) is the
  real version of lesson 27's hand-built page envelope — backed by an actual `COUNT`
  query and real `LIMIT`/`OFFSET` SQL.
- **A derived query can take a `Pageable` parameter** — the `WHERE` clause and the
  pagination compose automatically, and the companion `COUNT` query correctly includes
  the same filter.
- **An interface projection** (a plain interface, no implementation) narrows the
  generated `SELECT` to only the columns its getters name.
- **A DTO projection** (`@Query` with a JPQL constructor expression) builds a real class
  directly from selected columns, with no intermediate entity ever created.
- Both projection styles reduce data transferred and objects created — reach for one
  the moment a query genuinely doesn't need every column of the full entity.

---

**Previous:** [33 — Transactions and `@Transactional`](../33-transactions-and-transactional/33-transactions-and-transactional.md) ·
**Next:** [35 — Auditing and timestamps](../35-auditing-and-timestamps/35-auditing-and-timestamps.md)
