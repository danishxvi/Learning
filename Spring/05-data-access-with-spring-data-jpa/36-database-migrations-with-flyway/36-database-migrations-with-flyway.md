# 36 · Database migrations with Flyway

> **Run the code for this lesson** — run it **twice**, in order, to see the point:
> ```bash
> mvn -f Spring/05-data-access-with-spring-data-jpa/36-database-migrations-with-flyway spring-boot:run
> ```

Every lesson since 29 used `ddl-auto: update` — Hibernate guessing what the schema
should look like from `@Entity` classes. That's fine for learning; it's not something
you want happening automatically against a database that matters. This lesson hands
schema ownership to **Flyway**, a real migration tool, and runs it twice to prove what
that actually buys.

---

## 1. Handing ownership over: `ddl-auto: validate`

```yaml
jpa:
  hibernate:
    ddl-auto: validate
```

Hibernate no longer creates or alters anything — it only **checks** that `@Entity`
classes match what's already there, and fails loudly if they disagree. From this lesson
on, the schema itself is owned entirely by SQL files Flyway applies, not by Hibernate's
guesswork.

---

## 2. A migration file, and what its name means

```sql
-- V1__create_note_table.sql
CREATE TABLE note (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL
);
```

`V<version>__<description>.sql` — **two underscores** after the version number — is not
a stylistic convention; Flyway parses this filename directly to determine the version
number and the description shown in its history. Placed under
`src/main/resources/db/migration/`, Flyway finds it automatically at startup.

Running the app for the first time against a fresh database:

```
Migrating schema "PUBLIC" to version "1 - create note table"
Successfully applied 1 migration to schema "PUBLIC", now at version v1
```

---

## 3. `flyway_schema_history` — the real, queryable record

```sql
SELECT "installed_rank", "version", "description", "checksum", "success" FROM "flyway_schema_history"
```
```
rank=-1 version=null description="<< Flyway Schema History table created >>" checksum=null success=true
rank=1  version=1    description="create note table"                       checksum=-928538422 success=true
```

Flyway creates and manages this table itself — every migration it has ever applied, with
a **checksum** of that migration file's exact content at the time it ran. This table is
what makes the next two sections possible.

---

## 4. Adding a second migration — only the new one runs

```sql
-- V2__add_archived_column.sql
ALTER TABLE note ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE;
```

Running the app again, **without touching the database** — same file, same process, a
second invocation:

```
Current version of schema "PUBLIC": 1
Migrating schema "PUBLIC" to version "2 - add archived column"
Successfully applied 1 migration to schema "PUBLIC", now at version v2
```

**Flyway checked its history table, saw V1 already applied, and ran only V2.** The note
inserted on the very first run is still there — `id=1 ... archived=false` — the
`DEFAULT FALSE` from the `ALTER TABLE` filled it in automatically for every existing row.
This is the actual value proposition: incremental, ordered, one-way changes, applied
exactly once, tracked durably, safe to run against a database that already has real data
in it.

---

## 5. Modifying an already-applied migration — a real, reproduced failure

Editing `V1__create_note_table.sql` after it has already been applied (adding one
harmless comment line) and running the app again produces this, unedited:

```
FlywayValidateException: Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 1
-> Applied to database : -928538422
-> Resolved locally    : 1044663907
Either revert the changes to the migration, or run repair to update the schema history.
```

**The application refuses to start.** Flyway recomputed V1's checksum from the file on
disk and compared it against the checksum recorded when it actually ran — they no longer
match, so Flyway assumes the file has been tampered with after the fact and stops rather
than guess whether that change is safe. This is a deliberate, important safety property:
**a migration that has already run against a real database must never be edited** — any
further change belongs in a new migration file (a `V3__...sql`), never a rewrite of `V1`.
The fix, as the error message itself says, is either reverting the edit or running
`flyway:repair` if the change genuinely was intentional and every environment needs to
accept the new checksum.

---

## 6. Summary

- **`ddl-auto: validate`** hands schema ownership to Flyway — Hibernate only checks that
  entities match, never creates or alters anything itself.
- **`V<version>__<description>.sql`** in `db/migration/` is Flyway's entire
  specification — the filename is parsed directly, not just a label.
- **`flyway_schema_history`** is a real table Flyway creates and queries, recording every
  applied migration's version, description, and a checksum of its exact content.
- **Only migrations not yet in the history table run** — verified here by adding a V2
  and confirming a second startup applied just that one, leaving existing data (and its
  new column's `DEFAULT`) intact.
- **Editing an already-applied migration is a hard failure by design** — a real,
  reproduced `FlywayValidateException` from a checksum mismatch, refusing to start rather
  than risk applying an unreviewed change silently. New changes always go in a new
  migration file.

---

**Previous:** [35 — Auditing and timestamps](../35-auditing-and-timestamps/35-auditing-and-timestamps.md) ·
**Next:** [37 — Connecting to a real database](../37-connecting-to-a-real-database/37-connecting-to-a-real-database.md)
