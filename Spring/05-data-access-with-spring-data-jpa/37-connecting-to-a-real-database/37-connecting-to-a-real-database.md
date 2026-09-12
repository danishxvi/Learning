# 37 · Connecting to a real database

> **Run the code for this lesson.** This one needs a real Postgres server reachable —
> the fastest way, with no local install beyond Docker:
> ```bash
> docker run -d --name learning-postgres -e POSTGRES_DB=learningdb -e POSTGRES_USER=learning -e POSTGRES_PASSWORD=learning -p 55432:5432 postgres:16-alpine
> ```
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/37-connecting-to-a-real-database spring-boot:run
> ```
> ```bash
> docker stop learning-postgres && docker rm learning-postgres
> ```

Lessons 29–36 used H2 specifically because it needs no install. This lesson swaps it for
a real Postgres server — and the honest headline is **how little actually changes**.

---

## 1. What actually changes: one dependency, one config block

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:55432/learningdb
    driver-class-name: org.postgresql.Driver
    username: learning
    password: learning
```

That's the entire migration from H2. **`Book.java`, `BookRepository.java`, and the
Flyway `V1__create_book_table.sql` file are byte-for-byte the same as they would be
targeting H2** — nothing in any of them mentions a specific database. This is one of
JPA's genuine selling points: the application code stays portable; only the connection
details and the driver jar change.

---

## 2. Proof this is real Postgres, not a compatibility mode

```java
entityManager.createNativeQuery("SELECT version()").getSingleResult();
```
```
PostgreSQL 16.15 on x86_64-pc-linux-musl, compiled by gcc (Alpine 15.2.0) 15.2.0, 64-bit
```

A real server, running in a real Docker container, answering a real query — not H2's
Postgres-compatibility mode (which some earlier lessons could have used, but didn't
need to, since none of them relied on a Postgres-specific feature).

---

## 3. No `hibernate.dialect` property — and why that's correct, not an oversight

Older Hibernate tutorials always set `spring.jpa.database-platform` or
`hibernate.dialect` explicitly (`org.hibernate.dialect.PostgreSQLDialect`). **This
lesson's `application.yml` sets neither, on purpose.** Modern Hibernate (6.x, what this
whole stack uses) inspects the JDBC connection itself at startup and auto-detects the
correct dialect — this lesson's real log line proves it:

```
Database: jdbc:postgresql://localhost:55432/learningdb (PostgreSQL 16.15)
```

The dialect decision is visible in the SQL Hibernate actually generates. Compare the
`INSERT` statement here against lesson 29's identical entity shape running on H2:

```sql
-- Postgres (this lesson) - uses RETURNING to get the generated id back in the same round trip
insert into book (author, title) values (?, ?) returning id
```
```sql
-- H2 (lesson 29) - uses a placeholder default and a separate identity lookup
insert into book (author, title, id) values (?, ?, default)
```

**Same entity, same annotations, two genuinely different SQL statements** — each one
using the idiom its own database supports best, chosen automatically, with zero
configuration telling Hibernate which database it's talking to beyond the JDBC URL
itself.

---

## 4. Flyway needs one more module for Postgres specifically

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

Lesson 36's H2 setup needed only `flyway-core` — H2 support ships inside it. Postgres
(along with several other databases) needs this additional module since Flyway 10 split
database-specific code out of the core artifact. Both are managed by
`spring-boot-dependencies` (lesson 16), so neither needs an explicit `<version>`.

---

## 5. `HikariCP` — the pool that was always there

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
```

Every lesson since 29 has used HikariCP already — it's Spring Boot's default connection
pool, silently doing its job against H2 the whole time. Connecting to a real network
database is where its settings actually start to matter: `maximum-pool-size` caps how
many concurrent connections the application can hold open against the database server at
once, which matters the moment a real server enforces its own connection limit — a
concern that simply doesn't exist for an in-process H2 database.

---

## 6. Summary

- **Moving from H2 to a real database changes almost nothing in application code** —
  swap the JDBC driver dependency and the connection URL/credentials; entities,
  repositories, and Flyway migrations stay identical.
- **Docker is the zero-install way to run a real database locally** — one command
  produces a genuine Postgres server, torn down just as easily afterward.
- **No `hibernate.dialect` property is needed** with a modern Hibernate/Boot version —
  it's auto-detected from the JDBC connection, and produces genuinely different,
  database-idiomatic SQL (`RETURNING id` on Postgres vs. an identity column default on
  H2) for the exact same entity.
- **Flyway needs a database-specific module** (`flyway-database-postgresql`) for
  Postgres, MySQL, and several others — H2 was the exception in lesson 36, not the rule.
- **HikariCP's pool settings** matter once a real, connection-limited server is involved
  — invisible against H2, load-bearing against anything real.

This closes section 05. Every concept from `EntityManager` (lesson 29) through Flyway
migrations (lesson 36) transfers directly to this real database, unchanged.

---

**Previous:** [36 — Database migrations with Flyway](../36-database-migrations-with-flyway/36-database-migrations-with-flyway.md) ·
**Next:** [38 — Cross-cutting concerns and the proxy problem](../../06-aspect-oriented-programming/38-cross-cutting-concerns-and-the-proxy-problem/38-cross-cutting-concerns-and-the-proxy-problem.md)
