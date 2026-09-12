# 35 · Auditing and timestamps

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/35-auditing-and-timestamps spring-boot:run
> ```

"Who created this row, and when — who last touched it, and when" is one of the most
common requirements in any real system, and one of the easiest to get wrong by hand
(forgetting to set a timestamp on just one code path). Spring Data JPA auditing fills
all four fields automatically, from annotations alone.

---

## 1. Four annotations, and the one thing that has to enable them

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Note {
    @CreatedDate      private Instant createdDate;
    @LastModifiedDate private Instant lastModifiedDate;
    @CreatedBy        private String createdBy;
    @LastModifiedBy   private String lastModifiedBy;
}
```

```java
@EnableJpaAuditing
@SpringBootApplication
public class AuditingApplication { ... }
```

**Without `@EnableJpaAuditing`, these four field annotations are inert** — Spring Data
never looks at them, and every one of these columns would stay `null` forever.
`@EntityListeners(AuditingEntityListener.class)` is what actually wires the mechanism
into this specific entity, by hooking JPA's own `@PrePersist`/`@PreUpdate` lifecycle
callbacks (lesson 06's exact lifecycle, applied here automatically instead of by hand).

---

## 2. `@CreatedDate`/`@LastModifiedDate` — no code sets these, anywhere

```bash
# creating a Note
createdDate      = 2026-09-12T19:26:12.333169Z
lastModifiedDate = 2026-09-12T19:26:12.333169Z
```
```bash
# updating the SAME Note's content
createdDate      = 2026-09-12T19:26:12.333169Z   <-- unchanged
lastModifiedDate = 2026-09-12T19:26:12.478090Z   <-- updated
```

Neither `NoteService.create` nor `NoteService.updateContent` mentions either field —
`updateContent` just does `note.setContent(newContent)` and relies on dirty checking
(lesson 29) to produce the `UPDATE`. `@PreUpdate` fires as part of that same flush, and
sets `lastModifiedDate` to the current instant, leaving `createdDate` alone because
`@CreatedDate` only ever fires on the `@PrePersist` callback — once, at creation, never
again.

---

## 3. `@CreatedBy`/`@LastModifiedBy` — the one piece you have to supply

```java
public class SpringSecurityFreeAuditorAware implements AuditorAware<String> {
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(CurrentUserHolder.get());
    }
}
```
```java
@Bean
public AuditorAware<String> auditorAware() {
    return new SpringSecurityFreeAuditorAware();
}
```

Spring Data has no way to know **who** is making a change — that's genuinely
application-specific, so `AuditorAware<T>` is the one piece of this mechanism that must
be supplied by hand, registered as a bean. This lesson's implementation reads a
thread-local set manually, purely because Spring Security (section 07) doesn't exist yet
in this stack — a real application would read the authenticated principal from
`SecurityContextHolder` here instead, with the entity and the rest of the auditing
mechanism completely unchanged.

Running the lesson: creating as `"danish"` and then updating as `"editor-bot"` produces
exactly what each annotation promises —

```
createdBy      = danish        <-- set once, at creation, never touched again
lastModifiedBy = editor-bot    <-- updated to whoever made the LAST change
```

---

## 4. Why this beats doing it by hand

The alternative — setting `createdAt`/`updatedAt`/`createdBy`/`updatedBy` manually in
every service method that creates or modifies an entity — is exactly the kind of
repetitive, easy-to-forget code this mechanism replaces. Miss it in one new method
(a bulk import job, a new endpoint added six months later) and that one code path
silently produces rows with no audit trail, with no error at any point. Declaring the
four fields once, on the entity, makes it structurally impossible to forget — every
`@PrePersist`/`@PreUpdate` cycle fills them in, for every entity carrying
`@EntityListeners(AuditingEntityListener.class)`, regardless of which service or
method triggered the write.

---

## 5. Summary

- **`@EnableJpaAuditing`** turns the whole mechanism on — without it, every auditing
  annotation is inert.
- **`@EntityListeners(AuditingEntityListener.class)`** wires JPA's own
  `@PrePersist`/`@PreUpdate` lifecycle callbacks (lesson 06) into filling the annotated
  fields automatically.
- **`@CreatedDate`/`@CreatedBy`** are set once, on creation, and never touched again;
  **`@LastModifiedDate`/`@LastModifiedBy`** update on every subsequent change — verified
  here by creating as one user and updating as another.
- **`AuditorAware<T>`** is the one piece that must be supplied by hand — Spring Data has
  no way to know "who" without an application-specific answer. In a real application,
  this reads Spring Security's authenticated principal instead of a thread-local.
- Declaring these fields once on the entity makes forgetting to set a timestamp or
  author on some new code path structurally impossible, instead of a matter of
  remembering.

---

**Previous:** [34 — Pagination and projections](../34-pagination-and-projections/34-pagination-and-projections.md) ·
**Next:** [36 — Database migrations with Flyway](../36-database-migrations-with-flyway/36-database-migrations-with-flyway.md)
