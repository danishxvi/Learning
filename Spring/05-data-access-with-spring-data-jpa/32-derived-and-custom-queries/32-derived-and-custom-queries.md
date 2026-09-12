# 32 · Derived and custom queries

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/32-derived-and-custom-queries spring-boot:run
> ```

Lesson 31's `JpaRepository` gave every entity `save`/`findById`/`findAll` for free. This
lesson covers everything beyond that: queries specific to your own domain, expressed
three different ways.

---

## 1. Derived queries — the method name *is* the query

```java
List<Book> findByTitleContainingIgnoreCase(String titleFragment);
```
```sql
select ... from book b1_0 where upper(b1_0.title) like upper(?) escape '\'
```

Spring Data parses this method name at startup — `findBy` + `Title` + `ContainingIgnoreCase`
— and builds this exact JPQL query from it, with no query string written anywhere. The
method's *name* is the entire specification. Common keywords: `Containing`, `StartingWith`,
`GreaterThan`, `LessThan`, `Between`, `In`, `IsNull`, `And`, `Or`, `OrderBy...Asc/Desc` —
all composable in one method name.

```java
List<Book> findByAuthorName(String name);
```
```sql
select ... from book b1_0 left join author a1_0 on a1_0.id=b1_0.author_id where a1_0.name=?
```

`AuthorName` isn't a single property of `Book` — it's a **path**: `Book.author.name`.
Spring Data recognises this and generates the `JOIN` automatically. This is genuinely
useful and genuinely easy to get subtly wrong at scale — a method name with several
chained conditions and an `OrderBy` clause (`findByAuthorNameOrderByPriceCentsDesc`,
also in this lesson) can become long enough that a hand-written `@Query` reads more
clearly, even though the derived form still works correctly.

**Derived queries aren't limited to fetching full rows** — `countByAuthorName` compiles
to `SELECT COUNT(...)`, and `existsByTitle` compiles to a `SELECT id ... FETCH FIRST 1
ROWS ONLY` in this lesson's real output — an efficient existence check, not a full row
fetch just to check if the list is non-empty.

---

## 2. `@Query` with JPQL — for anything a name can't comfortably express

```java
@Query("SELECT b FROM Book b WHERE b.priceCents > :minPriceCents")
List<Book> expensiveBooks(@Param("minPriceCents") int minPriceCents);
```
```sql
select ... from book b1_0 where b1_0.price_cents>?
```

JPQL (Java Persistence Query Language) queries operate on **entity and field names**
(`Book`, `priceCents`) — not table and column names (`book`, `price_cents`). Hibernate
translates the field names to columns the same way it would for any other query.
`@Param("minPriceCents")` binds the method parameter to the named `:minPriceCents`
placeholder in the query string explicitly.

---

## 3. `JOIN FETCH` — the actual fix for lesson 30's exception

```java
@Query("SELECT b FROM Book b JOIN FETCH b.author WHERE b.author.name = :authorName")
List<Book> findByAuthorNameWithAuthorFetched(@Param("authorName") String authorName);
```
```sql
select b1_0.id, a1_0.id, a1_0.name, b1_0.price_cents, b1_0.title
from book b1_0 join author a1_0 on a1_0.id=b1_0.author_id where a1_0.name=?
```

**One query, both entities, fully loaded** — running this lesson accesses
`book.getAuthor().getName()` on every result with zero transaction issues, because the
`author` data already arrived with the `Book` row, in this single `SELECT`. This is the
fix lesson 30 promised for `LazyInitializationException`: rather than widening a
transaction boundary to cover wherever a lazy association happens to get touched,
`JOIN FETCH` loads exactly what a specific use case needs, eagerly, for that one query —
`Book.author`'s mapping itself stays `LAZY` everywhere else, unaffected.

---

## 4. Native SQL — for what JPQL genuinely can't express

```java
@Query(value = "SELECT * FROM book WHERE price_cents > ?1", nativeQuery = true)
List<Book> expensiveBooksNative(int minPriceCents);
```
```sql
SELECT * FROM book WHERE price_cents > ?
```

`nativeQuery = true` means this string is **real SQL**, sent to the database almost
unmodified — table and column names (`book`, `price_cents`), not entity and field names.
`?1` is positional (the first method parameter), the native-query equivalent of a named
`:param`. Reach for this only when JPQL genuinely cannot express something needed — a
database-specific function, a query hand-tuned for a specific execution plan — since a
native query loses JPQL's database-independence and its awareness of entity mappings.

---

## 5. Choosing between the three

| Need | Use |
| --- | --- |
| A simple filter, one or two conditions | A **derived query** — fastest to write, self-documenting |
| A longer condition chain, or a name that's getting hard to read | **`@Query` (JPQL)** — clearer than an increasingly long method name |
| Eagerly loading an association for one specific use case | **`@Query` with `JOIN FETCH`** — without changing the entity's own fetch type |
| A database-specific feature JPQL can't express | **`@Query(nativeQuery = true)`** — a last resort, not a default |

---

## 6. Summary

- **Derived queries** build JPQL from a method's name alone — `ContainingIgnoreCase`,
  nested property paths (`AuthorName` → an automatic `JOIN`), comparison keywords, and
  `OrderBy`, all composable.
- **`count`/`exists` derived queries generate shaped SQL** (`COUNT`, a `LIMIT 1`
  existence check) — not a full row fetch discarded afterward.
- **`@Query` with JPQL** operates on entity/field names and is the right choice once a
  method name would become long or hard to read.
- **`JOIN FETCH`** in a custom query is the real, targeted fix for
  `LazyInitializationException` — eager loading for one query, with the entity's actual
  mapping left `LAZY` everywhere else.
- **`@Query(nativeQuery = true)`** sends real SQL, table/column names and all — a last
  resort for what JPQL genuinely cannot express.

---

**Previous:** [31 — Spring Data repositories](../31-spring-data-repositories/31-spring-data-repositories.md) ·
**Next:** [33 — Transactions and `@Transactional`](../33-transactions-and-transactional/33-transactions-and-transactional.md)
