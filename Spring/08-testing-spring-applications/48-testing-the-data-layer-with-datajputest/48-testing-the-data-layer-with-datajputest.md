# 48 · Testing the data layer with `@DataJpaTest`

> **Run the tests for this lesson**
> ```bash
> mvn -f Spring/08-testing-spring-applications/48-testing-the-data-layer-with-datajputest test
> ```

Lesson 47 sliced out the web layer. This lesson slices out the **data** layer instead —
entities, repositories, and a real database — with neither the web layer nor any
`@Service` bean anywhere in sight.

---

## 1. `@DataJpaTest` auto-replaces the database with an in-memory one

```java
@DataJpaTest
class BookRepositoryTest { ... }
```

Real, unedited log line:
```
Replacing 'dataSource' DataSource bean with embedded version
Starting embedded database: url='jdbc:h2:mem:f2a4c247-...'
```

`@DataJpaTest` loads only JPA-related infrastructure — entities, Spring Data
repositories, the `EntityManager` — and, because H2 is present on the **test**
classpath (scoped `test` in this lesson's `pom.xml`, unlike section 05's lessons where
it was `runtime`), automatically swaps in a fresh in-memory database for this test
class, regardless of what real database might be configured for production. No web
layer, no `TestDispatcherServlet` from lesson 47, no real Postgres from lesson 37.

---

## 2. `TestEntityManager` — write-then-read without waiting on a transaction

```java
Book saved = entityManager.persistAndFlush(new Book("Effective Java", "Joshua Bloch"));
Optional<Book> found = bookRepository.findById(saved.getId());
```

`persistAndFlush` writes the row **and** sends the SQL immediately, rather than
deferring it to the next flush point — necessary here because the very next line
queries for that same row through a completely different path (`bookRepository`, not
the `EntityManager` that wrote it). Real SQL confirms both operations actually hit the
database:
```sql
insert into book (author, title, id) values (?, ?, default)
select count(*) from book b1_0
```

---

## 3. Automatic rollback — verified by making the next test depend on it

```java
@Test @Order(1)
void savesAndFindsABook() {
    entityManager.persistAndFlush(new Book("Effective Java", "Joshua Bloch"));
    assertThat(bookRepository.count()).isEqualTo(1);
}

@Test @Order(2)
void eachTestRollsBackAutomatically() {
    assertThat(bookRepository.count()).isEqualTo(0);   // would be 1 if rollback failed
}
```

Both pass, in this guaranteed order (`@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`).
**`@DataJpaTest` wraps every test method in a transaction that rolls back automatically
at the end of that method — with zero configuration.** The row inserted in the first
test genuinely does not exist by the time the second one runs; if rollback weren't
happening, `eachTestRollsBackAutomatically` would fail its own assertion immediately.
This is what makes data-layer tests safe to run in any order, repeatedly, without ever
manually cleaning up — each test starts from a guaranteed-empty table.

---

## 4. A derived query, proven against a real database

```java
List<Book> byMartin = bookRepository.findByAuthor("Robert C. Martin");
assertThat(byMartin).hasSize(2);
```

```sql
select b1_0.id, b1_0.author, b1_0.title from book b1_0 where b1_0.author=?
```

`findByAuthor` (lesson 32's derived query mechanism) is proven here to generate correct
SQL and return the right rows — not just to compile, which is all a plain unit test
(lesson 46, mocking the repository entirely) could ever confirm about a query method's
actual behavior.

---

## 5. Where this slice sits

| Test type | What's real | What's mocked/absent |
| --- | --- | --- |
| Lesson 46 (plain unit test) | The class under test only | Everything else — no Spring at all |
| Lesson 47 (`@WebMvcTest`) | Controllers, `@RestControllerAdvice`, validation | Services, repositories, database |
| **This lesson (`@DataJpaTest`)** | **Entities, repositories, a real (in-memory) database** | Controllers, services, the web layer entirely |
| Lesson 49 (`@SpringBootTest`) | The whole application | Nothing — the full, real context |

Each slice answers a different question with only the setup cost that question needs —
`@DataJpaTest` is the right tool specifically for "does this query actually work,"
without paying for a web server or a service layer it doesn't need to prove that.

---

## 6. Summary

- **`@DataJpaTest`** loads only JPA infrastructure and auto-configures an in-memory
  database in place of any real one — verified by a real "Replacing 'dataSource' ...
  with embedded version" log line.
- **`TestEntityManager.persistAndFlush`** writes and sends SQL immediately, so a
  different path (the repository) can safely read it back in the same test.
- **Every test method runs in its own transaction, rolled back automatically** — proven
  here by making one test's passing assertion depend on the previous test's insert
  having been undone.
- **Derived queries (lesson 32) are proven against a real database in this slice** —
  something no mock-based unit test (lesson 46) could ever confirm.
- **Each testing slice trades scope for setup cost** — `@DataJpaTest` sits between a
  zero-Spring unit test and a full `@SpringBootTest`, proving exactly the data layer and
  nothing more.

---

**Previous:** [47 — Testing the web layer with `@WebMvcTest`](../47-testing-the-web-layer-with-webmvctest/47-testing-the-web-layer-with-webmvctest.md) ·
**Next:** [49 — Full integration tests with `@SpringBootTest`](../49-full-integration-tests-with-springboottest/49-full-integration-tests-with-springboottest.md)
