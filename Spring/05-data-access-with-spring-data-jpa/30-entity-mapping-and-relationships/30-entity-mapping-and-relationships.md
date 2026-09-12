# 30 · Entity mapping and relationships

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/30-entity-mapping-and-relationships spring-boot:run
> ```
> Watch the `Hibernate:` blocks — every claim below is one of them, unedited.

Lesson 29 mapped one bare entity. This lesson maps a real, connected model — an
`Author` with many `Book`s, each `Book` with an embedded `Price`, an enum `Genre`, and a
many-to-many set of `Tag`s — and deliberately breaks lazy loading once, to show exactly
what that failure looks like.

---

## 1. `@Column`, `@Enumerated`, `@Embedded` — mapping a single entity's shape

```java
@Column(nullable = false, length = 120)
private String name;
```
generates:
```sql
name varchar(120) not null
```

```java
@Enumerated(EnumType.STRING)
private Genre genre;
```
generates:
```sql
genre enum ('FICTION','NON_FICTION','TECHNICAL')
```

**Always use `EnumType.STRING`.** The alternative, `ORDINAL` (the default if
`@Enumerated` is omitted entirely), stores the enum constant's *position* — `0`, `1`,
`2`. Reordering the enum, or inserting a new constant anywhere but the end, silently
changes what every existing row means, with no error at any point.

```java
@Embeddable
public class Price {
    private int amountCents;
    private String currency;
}
```
```java
@Embedded
private Price price;
```
generates two plain columns on `book` itself — `amount_cents`, `currency` — **no
separate table, no foreign key, no join**. `@Embeddable` is for a value that logically
belongs together but has no identity of its own; it can never be looked up by its own id,
because it doesn't have one.

---

## 2. `@OneToMany`/`@ManyToOne` — who owns the relationship

```java
// Author.java - the INVERSE side
@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
private final List<Book> books = new ArrayList<>();
```
```java
// Book.java - the OWNING side
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "author_id")
private Author author;
```

The generated schema settles who owns what immediately:

```sql
create table book (... author_id bigint, primary key (id))
alter table book add constraint ... foreign key (author_id) references author
```

**Only `book` has an `author_id` column.** `Author.books` is `mappedBy = "author"` —
purely a Java-side convenience for navigating from an `Author` to its `Book`s; it has no
column of its own on either table. Confusing which side owns the relationship is a
common source of bugs: only the owning side's changes are ever written to the database.

A helper method keeps both directions of the Java object graph consistent in one call:

```java
public void addBook(Book book) {
    books.add(book);
    book.setAuthor(this);   // easy to forget, if not wrapped in a helper like this
}
```

---

## 3. `@ManyToMany` — a join table, not a foreign key on either side

```java
@ManyToMany
@JoinTable(name = "book_tags",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
private final Set<Tag> tags = new HashSet<>();
```

```sql
create table book_tags (book_id bigint not null, tag_id bigint not null, primary key (book_id, tag_id))
```

Neither `book` nor `tag`'s own table can express "many books, many tags, no natural
owner" — a `book_id` column on `tag` would only allow one book per tag, and vice versa.
The join table's two columns together form its primary key, and it exists purely to
record which pairs are associated.

---

## 4. `cascade` and `orphanRemoval` — two different automatic-delete behaviors

```bash
# removeOneBook: author.removeBook(toRemove) - just editing a Java List
```
```sql
delete from book_tags where book_id=?
delete from book where id=?
```

**No explicit `entityManager.remove()` was called on the `Book`.** `orphanRemoval = true`
means: the moment a `Book` is taken out of its owning `Author`'s list (and nothing else
references it), Hibernate deletes it on the next flush — "orphaned" from its parent is
treated as "should no longer exist."

```bash
# deleteAuthorAndAllBooks: entityManager.remove(author)
```
```sql
delete from book_tags where book_id=?
delete from book where id=?
delete from author where id=?
```

`cascade = CascadeType.ALL` is the different, broader behavior: removing the `Author`
itself propagates the removal to every `Book` still in its collection — no need to
delete each one individually first. Both showed real `DELETE` statements above, for two
genuinely different triggers: removing one item from a collection vs. removing the
parent entirely.

---

## 5. `LazyInitializationException` — the real failure, reproduced

```java
@ManyToOne(fetch = FetchType.LAZY)
private Author author;
```

Fetching a `Book` **outside** a transaction and then touching its lazy `author`
association:

```java
Book book = service.fetchBookWithoutTransaction(bookId);   // no @Transactional
book.getAuthor().getName();                                 // BOOM
```

```
LazyInitializationException: could not initialize proxy [com.danish.spring.mapping.Author#1] - no Session
```

A real, unedited exception. `book.getAuthor()` doesn't actually hold an `Author` — it
holds a **proxy**, a stand-in object that only fetches the real data the first time a
method is called on it. That fetch needs an open persistence context (a "Session," in
Hibernate's own terms) to run its query through — and by the time `getName()` runs here,
the context that loaded `book` has already closed. The exact same access **inside** a
`@Transactional` method works perfectly (this lesson's `demonstrateLazyAccessWithinTransaction`
proves it, with a real `SELECT` for the lazy collection appearing at the moment
`.getBooks()` is actually touched, not before) — the difference is entirely about
*when* the loading code runs relative to the transaction boundary, never about the
mapping being "wrong."

---

## 6. Summary

- **`@Enumerated(EnumType.STRING)`** — always; `ORDINAL` (the default) breaks silently
  the moment enum constants are reordered.
- **`@Embeddable`/`@Embedded`** folds a value object's fields onto the owning entity's
  own table — no separate table, no join, no identity of its own.
- **`@OneToMany(mappedBy = "...")`** is the inverse, convenience side; **`@ManyToOne`
  with `@JoinColumn`** is the owning side that actually gets a foreign key column — only
  the owning side's changes are persisted.
- **`@ManyToMany`** needs a join table (`@JoinTable`) — neither entity's own table can
  express a many-to-many relationship alone.
- **`orphanRemoval = true`** deletes a child the moment it's removed from its parent's
  collection; **`cascade = ALL`** propagates a parent's own removal to every remaining
  child — two different triggers, both producing real `DELETE` statements.
- **`LazyInitializationException`** happens when a lazy association or collection is
  touched after its loading transaction has already closed — the fix is either widening
  the transaction boundary or fetching eagerly up front (a custom `JOIN FETCH` query,
  covered in lesson 32).

---

**Previous:** [29 — JPA and Hibernate fundamentals](../29-jpa-and-hibernate-fundamentals/29-jpa-and-hibernate-fundamentals.md) ·
**Next:** [31 — Spring Data repositories](../31-spring-data-repositories/31-spring-data-repositories.md)
