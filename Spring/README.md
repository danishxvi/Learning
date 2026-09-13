# Spring

A complete, ordered walk through the Spring ecosystem — from the dependency-injection
problem Spring exists to solve, through Spring Boot, REST APIs, Spring Data JPA, security,
testing, and the operational concerns of running a Spring Boot service in production.

**Prerequisite:** this stack assumes the Java covered in [`Java/`](../Java/README.md)
through at least section 09 (generics and collections) — interfaces, generics, lambdas
where noted, and the collections framework. Spring is a Java framework; it does not
re-teach Java.

**Target versions: Java 21 (LTS) and Spring Boot 3.x.** Every lesson states the exact
starter dependencies it needs at the top of its `.md`.

---

## How to run these files

Unlike the `Java/` stack, a Spring lesson cannot be a single bare file — Spring needs
Maven's `pom.xml` + `src/main/java` layout to resolve dependencies and run. So each lesson
is its own **small, self-contained Maven project**, living in a folder that shares the
lesson's number and name:

```
Spring/01-introduction-and-setup/03-your-first-spring-boot-project/
├── 03-your-first-spring-boot-project.md   ← the lesson
├── pom.xml                                ← the project's dependencies
└── src/main/java/com/danish/...           ← the runnable proof
```

Install the JDK (21+) and [Apache Maven](https://maven.apache.org/download.cgi), then
confirm:

```bash
java -version
```

```bash
mvn -version
```

Run a lesson by pointing Maven's `-f` flag at its folder — this works from anywhere,
without `cd`-ing into it:

```bash
mvn -f Spring/01-introduction-and-setup/03-your-first-spring-boot-project spring-boot:run
```

Lessons before Spring Boot is introduced (the plain Spring Framework core-container
lessons) are not web applications and have nothing for `spring-boot:run` to start. Those
use a plain `main` method instead, run with:

```bash
mvn -f Spring/<lesson-folder> compile exec:java
```

**Every lesson's `.md` states the exact run command at the top**, so you never have to
work out which of the two it needs.

> The first time you run a lesson, Maven downloads its dependencies from Maven Central —
> that needs an internet connection. Maven caches everything under `~/.m2`, so every run
> after the first works offline, and later lessons that reuse a dependency you already
> fetched need no further download at all.

Every lesson is a standalone Maven project. You never need to have completed an earlier
lesson for a later one to build or run.

---

## Progress

**Lessons 01-60 are complete** - written, run and verified. Everything below that line is
planned and listed here so you can see the full route.

| Section | Status |
| --- | --- |
| 01 Introduction and setup (01-05) | Complete |
| 02 The IoC container and DI (06-14) | Complete |
| 03 Spring Boot fundamentals (15-20) | Complete |
| 04 Building REST APIs with Spring MVC (21-28) | Complete |
| 05 Data access with Spring Data JPA (29-37) | Complete |
| 06 Aspect-oriented programming (38-39) | Complete |
| 07 Spring Security (40-45) | Complete |
| 08 Testing Spring applications (46-50) | Complete |
| 09 Transactions, async and scheduling (51-54) | Complete |
| 10 Caching (55-56) | Complete |
| 11 Events and messaging (57-59) | Complete |
| 12 Microservices with Spring Cloud (60-64) | In progress (60/5) |
| 13-14 (65-70) | Next |

---

## Syllabus

Read in numeric order. Each topic is a folder: the `.md` is the lesson, `pom.xml` +
`src/` is the runnable proof.

### 01 · Introduction and setup

| # | Topic |
| --- | --- |
| 01 | [Why Spring — the problem dependency injection solves](01-introduction-and-setup/01-why-spring-the-problem-di-solves/01-why-spring-the-problem-di-solves.md) |
| 02 | [Spring vs Spring Boot, and the ecosystem around them](01-introduction-and-setup/02-spring-vs-spring-boot-and-the-ecosystem/02-spring-vs-spring-boot-and-the-ecosystem.md) |
| 03 | [Your first Spring Boot project](01-introduction-and-setup/03-your-first-spring-boot-project/03-your-first-spring-boot-project.md) |
| 04 | [Anatomy of a Spring Boot project](01-introduction-and-setup/04-anatomy-of-a-spring-boot-project/04-anatomy-of-a-spring-boot-project.md) |
| 05 | [Configuration with `application.properties` and YAML](01-introduction-and-setup/05-configuration-with-properties-and-yaml/05-configuration-with-properties-and-yaml.md) |

### 02 · The IoC container and dependency injection

| # | Topic |
| --- | --- |
| 06 | [The `ApplicationContext` and the bean lifecycle](02-the-ioc-container-and-dependency-injection/06-the-applicationcontext-and-the-bean-lifecycle/06-the-applicationcontext-and-the-bean-lifecycle.md) |
| 07 | [Stereotype annotations and component scanning](02-the-ioc-container-and-dependency-injection/07-stereotype-annotations-and-component-scanning/07-stereotype-annotations-and-component-scanning.md) |
| 08 | [Constructor vs field vs setter injection](02-the-ioc-container-and-dependency-injection/08-constructor-vs-field-vs-setter-injection/08-constructor-vs-field-vs-setter-injection.md) |
| 09 | [`@Configuration` classes and `@Bean` methods](02-the-ioc-container-and-dependency-injection/09-configuration-classes-and-bean-methods/09-configuration-classes-and-bean-methods.md) |
| 10 | [Bean scopes](02-the-ioc-container-and-dependency-injection/10-bean-scopes/10-bean-scopes.md) |
| 11 | [Qualifiers and resolving ambiguous beans](02-the-ioc-container-and-dependency-injection/11-qualifiers-and-resolving-ambiguous-beans/11-qualifiers-and-resolving-ambiguous-beans.md) |
| 12 | [Conditional beans and profiles](02-the-ioc-container-and-dependency-injection/12-conditional-beans-and-profiles/12-conditional-beans-and-profiles.md) |
| 13 | [`@Value` injection and externalized configuration (recap and deeper dive)](02-the-ioc-container-and-dependency-injection/13-value-injection-and-externalized-configuration/13-value-injection-and-externalized-configuration.md) |
| 14 | [The `Environment` abstraction](02-the-ioc-container-and-dependency-injection/14-the-environment-abstraction/14-the-environment-abstraction.md) |

### 03 · Spring Boot fundamentals

| # | Topic |
| --- | --- |
| 15 | [Auto-configuration, explained](03-spring-boot-fundamentals/15-autoconfiguration-explained/15-autoconfiguration-explained.md) |
| 16 | [Starters and dependency management](03-spring-boot-fundamentals/16-starters-and-dependency-management/16-starters-and-dependency-management.md) |
| 17 | [`CommandLineRunner` and `ApplicationRunner`](03-spring-boot-fundamentals/17-commandlinerunner-and-applicationrunner/17-commandlinerunner-and-applicationrunner.md) |
| 18 | [Logging with SLF4J and Logback](03-spring-boot-fundamentals/18-logging-with-slf4j-and-logback/18-logging-with-slf4j-and-logback.md) |
| 19 | [DevTools and the inner development loop](03-spring-boot-fundamentals/19-devtools-and-the-inner-development-loop/19-devtools-and-the-inner-development-loop.md) |
| 20 | [Actuator basics](03-spring-boot-fundamentals/20-actuator-basics/20-actuator-basics.md) |

### 04 · Building REST APIs with Spring MVC

| # | Topic |
| --- | --- |
| 21 | [`@RestController` and the `@RequestMapping` family](04-building-rest-apis-with-spring-mvc/21-restcontroller-and-request-mapping/21-restcontroller-and-request-mapping.md) |
| 22 | [Path variables, request params and request bodies](04-building-rest-apis-with-spring-mvc/22-path-variables-request-params-and-request-bodies/22-path-variables-request-params-and-request-bodies.md) |
| 23 | [`ResponseEntity` and HTTP status codes](04-building-rest-apis-with-spring-mvc/23-responseentity-and-http-status-codes/23-responseentity-and-http-status-codes.md) |
| 24 | [DTOs, entities, and mapping between them](04-building-rest-apis-with-spring-mvc/24-dtos-entities-and-mapping-between-them/24-dtos-entities-and-mapping-between-them.md) |
| 25 | [Exception handling with `@ControllerAdvice`](04-building-rest-apis-with-spring-mvc/25-exception-handling-with-controlleradvice/25-exception-handling-with-controlleradvice.md) |
| 26 | [Bean Validation](04-building-rest-apis-with-spring-mvc/26-bean-validation/26-bean-validation.md) |
| 27 | [Pagination, sorting and filtering](04-building-rest-apis-with-spring-mvc/27-pagination-sorting-and-filtering/27-pagination-sorting-and-filtering.md) |
| 28 | [API documentation with OpenAPI](04-building-rest-apis-with-spring-mvc/28-api-documentation-with-openapi/28-api-documentation-with-openapi.md) |

### 05 · Data access with Spring Data JPA

| # | Topic |
| --- | --- |
| 29 | [JPA and Hibernate fundamentals](05-data-access-with-spring-data-jpa/29-jpa-and-hibernate-fundamentals/29-jpa-and-hibernate-fundamentals.md) |
| 30 | [Entity mapping and relationships](05-data-access-with-spring-data-jpa/30-entity-mapping-and-relationships/30-entity-mapping-and-relationships.md) |
| 31 | [Spring Data repositories](05-data-access-with-spring-data-jpa/31-spring-data-repositories/31-spring-data-repositories.md) |
| 32 | [Derived and custom queries](05-data-access-with-spring-data-jpa/32-derived-and-custom-queries/32-derived-and-custom-queries.md) |
| 33 | [Transactions and `@Transactional`](05-data-access-with-spring-data-jpa/33-transactions-and-transactional/33-transactions-and-transactional.md) |
| 34 | [Pagination and projections](05-data-access-with-spring-data-jpa/34-pagination-and-projections/34-pagination-and-projections.md) |
| 35 | [Auditing and timestamps](05-data-access-with-spring-data-jpa/35-auditing-and-timestamps/35-auditing-and-timestamps.md) |
| 36 | [Database migrations with Flyway](05-data-access-with-spring-data-jpa/36-database-migrations-with-flyway/36-database-migrations-with-flyway.md) |
| 37 | [Connecting to a real database](05-data-access-with-spring-data-jpa/37-connecting-to-a-real-database/37-connecting-to-a-real-database.md) |

### 06 · Aspect-oriented programming

| # | Topic |
| --- | --- |
| 38 | [Cross-cutting concerns and the proxy problem](06-aspect-oriented-programming/38-cross-cutting-concerns-and-the-proxy-problem/38-cross-cutting-concerns-and-the-proxy-problem.md) |
| 39 | [Aspects, pointcuts and advice](06-aspect-oriented-programming/39-aspects-pointcuts-and-advice/39-aspects-pointcuts-and-advice.md) |

### 07 · Spring Security

| # | Topic |
| --- | --- |
| 40 | [Authentication vs authorization](07-spring-security/40-authentication-vs-authorization/40-authentication-vs-authorization.md) |
| 41 | [The security filter chain](07-spring-security/41-the-security-filter-chain/41-the-security-filter-chain.md) |
| 42 | [Password encoding and `UserDetails`](07-spring-security/42-password-encoding-and-userdetails/42-password-encoding-and-userdetails.md) |
| 43 | [JWT-based stateless authentication](07-spring-security/43-jwt-based-stateless-authentication/43-jwt-based-stateless-authentication.md) |
| 44 | [Method-level security](07-spring-security/44-method-level-security/44-method-level-security.md) |
| 45 | [CORS and CSRF](07-spring-security/45-cors-and-csrf/45-cors-and-csrf.md) |

### 08 · Testing Spring applications

| # | Topic |
| --- | --- |
| 46 | [Unit testing with JUnit 5 and Mockito](08-testing-spring-applications/46-unit-testing-with-junit5-and-mockito/46-unit-testing-with-junit5-and-mockito.md) |
| 47 | [Testing the web layer with `@WebMvcTest`](08-testing-spring-applications/47-testing-the-web-layer-with-webmvctest/47-testing-the-web-layer-with-webmvctest.md) |
| 48 | [Testing the data layer with `@DataJpaTest`](08-testing-spring-applications/48-testing-the-data-layer-with-datajputest/48-testing-the-data-layer-with-datajputest.md) |
| 49 | [Full integration tests with `@SpringBootTest`](08-testing-spring-applications/49-full-integration-tests-with-springboottest/49-full-integration-tests-with-springboottest.md) |
| 50 | [Integration testing with Testcontainers](08-testing-spring-applications/50-integration-testing-with-testcontainers/50-integration-testing-with-testcontainers.md) |

### 09 · Transactions, async and scheduling

| # | Topic |
| --- | --- |
| 51 | [Transaction propagation and isolation](09-transactions-async-and-scheduling/51-transaction-propagation-and-isolation/51-transaction-propagation-and-isolation.md) |
| 52 | [Optimistic and pessimistic locking](09-transactions-async-and-scheduling/52-optimistic-and-pessimistic-locking/52-optimistic-and-pessimistic-locking.md) |
| 53 | [Asynchronous methods with `@Async`](09-transactions-async-and-scheduling/53-asynchronous-methods-with-async/53-asynchronous-methods-with-async.md) |
| 54 | [Scheduled tasks with `@Scheduled`](09-transactions-async-and-scheduling/54-scheduled-tasks-with-scheduled/54-scheduled-tasks-with-scheduled.md) |

### 10 · Caching

| # | Topic |
| --- | --- |
| 55 | [The Spring Cache abstraction](10-caching/55-the-spring-cache-abstraction/55-the-spring-cache-abstraction.md) |
| 56 | [Caching with Redis](10-caching/56-caching-with-redis/56-caching-with-redis.md) |

### 11 · Events and messaging

| # | Topic |
| --- | --- |
| 57 | [Application events and `@EventListener`](11-events-and-messaging/57-application-events-and-eventlistener/57-application-events-and-eventlistener.md) |
| 58 | [Messaging with RabbitMQ](11-events-and-messaging/58-messaging-with-rabbitmq/58-messaging-with-rabbitmq.md) |
| 59 | [Messaging with Kafka](11-events-and-messaging/59-messaging-with-kafka/59-messaging-with-kafka.md) |

### 12 · Microservices with Spring Cloud

| # | Topic |
| --- | --- |
| 60 | [Service discovery with Eureka](12-microservices-with-spring-cloud/60-service-discovery-with-eureka/60-service-discovery-with-eureka.md) |
| 61 | Centralized configuration with Config Server |
| 62 | Inter-service calls with Feign |
| 63 | API gateway |
| 64 | Resilience with circuit breakers (Resilience4j) |

### 13 · Production readiness

| # | Topic |
| --- | --- |
| 65 | Actuator deep dive and metrics with Micrometer |
| 66 | Profiles for real environments |
| 67 | Packaging and running the fat jar |
| 68 | Dockerizing a Spring Boot application |
| 69 | Graceful shutdown and health checks |

### 14 · Capstone

| # | Topic |
| --- | --- |
| 70 | Building a complete REST API end to end |

---

## Suggested pace

| If you have | Do this |
| --- | --- |
| A week | 01-20. You understand the container and can start a Boot app. |
| A month | 01-45. You can build and secure a real REST API. |
| Two to three months | All 70. You can hold a senior conversation about Spring in production. |

Do not rush 06-14 (the IoC container) — everything else in Spring is built on top of the
container, auto-configuration, and bean lifecycle covered there.

---

**Previous:** [Java](../Java/README.md)
