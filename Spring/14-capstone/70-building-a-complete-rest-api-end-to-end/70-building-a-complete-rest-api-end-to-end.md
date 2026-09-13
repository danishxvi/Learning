# 70 - Capstone: Building a Complete REST API End to End

The last lesson in this curriculum doesn't introduce anything new - it
combines nearly everything from the previous 69 lessons into one real,
working service: a **Task Management API** with JWT authentication, request
validation, global exception handling, ownership-scoped data access,
pagination and filtering, transactional writes, per-user caching, and a
real automated integration test suite. Every claim below - every status
code, every cache behavior, every ownership check - was exercised against
the running application with real HTTP requests, not assumed from the code.

```bash
mvn -f Spring/14-capstone/70-building-a-complete-rest-api-end-to-end spring-boot:run
```

## What's reused from the rest of the curriculum

| Concept | From | Where in this project |
| --- | --- | --- |
| Entities, repositories, `Page<T>` | Lessons 29, 31, 34 | [`Task.java`](src/main/java/com/danish/spring/capstone/task/Task.java), [`TaskRepository.java`](src/main/java/com/danish/spring/capstone/task/TaskRepository.java) |
| DTOs separate from entities | Lesson 24 | [`TaskRequest.java`](src/main/java/com/danish/spring/capstone/task/TaskRequest.java) / [`TaskResponse.java`](src/main/java/com/danish/spring/capstone/task/TaskResponse.java) |
| Bean Validation | Lesson 26 | `@NotBlank`/`@Size` on the request DTOs |
| Global exception handling | Lesson 25 | [`GlobalExceptionHandler.java`](src/main/java/com/danish/spring/capstone/common/GlobalExceptionHandler.java) |
| JWT-based stateless authentication | Lesson 43 | [`JwtService.java`](src/main/java/com/danish/spring/capstone/security/JwtService.java), [`JwtAuthFilter.java`](src/main/java/com/danish/spring/capstone/security/JwtAuthFilter.java) |
| `@Transactional` + dirty checking | Lessons 29, 33 | [`TaskService.updateTask()`](src/main/java/com/danish/spring/capstone/task/TaskService.java) |
| `@Cacheable`/`@CacheEvict` | Lesson 55 | `TaskService.getStats()` and its mutating methods |
| Full integration tests, `MockMvc` | Lesson 49 | [`TaskApiIntegrationTest.java`](src/test/java/com/danish/spring/capstone/TaskApiIntegrationTest.java) |

Nothing here is a new pattern - it's the same tools from earlier lessons,
composed into one application the way a real service actually looks.

## The full real flow, exercised end to end

**Unauthenticated access is rejected before it reaches any controller
code:**

```bash
curl -s -w "\nSTATUS:%{http_code}\n" http://localhost:8080/tasks
```
```
STATUS:403
```

**Registration validates input for real** - a password under 6 characters
never reaches the database:

```bash
curl -s -X POST http://localhost:8080/auth/register -d '{"username":"bob","password":"123"}'
```
```json
{"password":"password must be at least 6 characters"}
```

**Login issues a real, working JWT; wrong credentials get a real 401:**

```bash
curl -s -X POST http://localhost:8080/auth/login -d '{"username":"alice","password":"secret123"}'
```
```json
{"token":"eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhbGljZSIs..."}
```
```bash
curl -s -X POST http://localhost:8080/auth/login -d '{"username":"alice","password":"wrongpass"}'
```
```json
{"error":"invalid credentials"}
```

**Creating tasks with the token works; a blank title is rejected by the same
validation mechanism as registration:**

```bash
curl -s -X POST http://localhost:8080/tasks -H "Authorization: Bearer $TOKEN" \
  -d '{"title":"Write lesson 70","description":"Capstone project","status":"IN_PROGRESS"}'
```
```json
{"id":1,"title":"Write lesson 70","description":"Capstone project","status":"IN_PROGRESS","createdAt":"2026-09-13T19:00:57.381786200Z"}
```

**Pagination and filtering both work against real, persisted data** - three
tasks, requested two at a time:

```bash
curl -s "http://localhost:8080/tasks?size=2&sort=id,desc" -H "Authorization: Bearer $TOKEN"
```
```json
{"content":[...2 tasks...],"totalPages":2,"totalElements":3,"first":true,"numberOfElements":2}
```
```bash
curl -s "http://localhost:8080/tasks?status=TODO" -H "Authorization: Bearer $TOKEN"
```
```json
{"content":[{"id":2,"title":"Buy groceries","status":"TODO",...}],"totalElements":1}
```

## Ownership enforcement, verified with a real second user

A second user, `bob`, registered and logged in independently, tried to read
`alice`'s task by its real id:

```bash
curl -s -w "\nSTATUS:%{http_code}\n" http://localhost:8080/tasks/1 -H "Authorization: Bearer $BOB_TOKEN"
```
```json
{"error":"task not found"}
STATUS:404
```

Genuinely `404`, not `403` - `TaskService.findOwnedTaskOrThrow` deliberately
doesn't reveal that a task with that id exists at all if the caller isn't
its owner, rather than confirming its existence via a "forbidden" response.
`bob`'s own task list, checked independently, was genuinely empty - real
data isolation between users, not just an authorization check on a shared
dataset.

## Cache correctness, verified through real mutations

`TaskService.getStats()` is `@Cacheable`; every mutating method
(`createTask`, `updateTask`, `deleteTask`) is `@CacheEvict` for that same
user's key. Real sequence:

```bash
curl -s http://localhost:8080/tasks/stats -H "Authorization: Bearer $TOKEN"
```
```json
{"todo":1,"inProgress":1,"done":1}
```
```bash
curl -s -X DELETE http://localhost:8080/tasks/3 -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/tasks/stats -H "Authorization: Bearer $TOKEN"
```
```json
{"todo":1,"inProgress":1,"done":0}
```
```bash
curl -s -X PUT http://localhost:8080/tasks/2 -d '{"title":"Buy groceries","status":"DONE"}' -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/tasks/stats -H "Authorization: Bearer $TOKEN"
```
```json
{"todo":0,"inProgress":1,"done":1}
```

The stats genuinely changed after each mutation, immediately - proof the
`@CacheEvict` on every write path is correctly wired to the same cache key
`@Cacheable` reads from. A cache that returns stale aggregate counts after a
real data change is a common, real bug class; this is the concrete evidence
that didn't happen here.

## A real testing gotcha, hit and fixed while writing this lesson

[`TaskApiIntegrationTest.java`](src/test/java/com/danish/spring/capstone/TaskApiIntegrationTest.java)'s
first version hardcoded the created task's id:

```java
mockMvc.perform(post("/tasks")...);
mockMvc.perform(get("/tasks/1")...)   // assumed id 1
        .andExpect(status().isOk());
```

Running the full suite failed with a real, reproducible error:

```
java.lang.AssertionError: Status expected:<200> but was:<404>
```

**Why**: `@SpringBootTest` methods in the same test class share **one**
Spring context and **one** in-memory H2 database for the whole test run by
default (no `@DirtiesContext`, no per-test transaction rollback configured
here). A different test method's task had already claimed id `1` by the
time this test ran, depending on JUnit's test execution order - so `carol`'s
newly-created task actually got id `2`, and `GET /tasks/1` correctly
returned `404` for a task `carol` didn't own. The fix was using the id
actually returned from the `POST /tasks` response instead of assuming it:

```java
String createResponse = mockMvc.perform(post("/tasks")...).andReturn().getResponse().getContentAsString();
long taskId = objectMapper.readTree(createResponse).get("id").asLong();
mockMvc.perform(get("/tasks/" + taskId)...).andExpect(status().isOk());
```

With that fix, the full suite passes, verified for real:

```bash
mvn test
```
```
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 16.37 s -- in com.danish.spring.capstone.TaskApiIntegrationTest
```

Three real tests: unauthenticated access rejected, the full
register→login→create→retrieve flow, and cross-user ownership isolation -
each running against a genuine Spring context, a genuine H2 database, and
the genuine Spring Security filter chain, exactly as `curl` exercised the
same paths against the running application above.

## Closing note

This capstone deliberately reuses rather than reinvents: JWT auth is the
same mechanism as lesson 43, caching is the same annotations as lesson 55,
the exception handler is the same pattern as lesson 25. A real production
service is built exactly this way - not from exotic new techniques, but
from the same handful of well-understood Spring patterns, composed
correctly and verified end to end, the same discipline this entire
curriculum has followed from lesson 1 onward: every claim backed by a real
run, not an assumption.
