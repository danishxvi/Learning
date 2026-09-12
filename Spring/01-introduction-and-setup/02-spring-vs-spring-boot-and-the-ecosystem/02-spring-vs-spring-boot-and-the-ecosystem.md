# 02 · Spring vs Spring Boot, and the ecosystem around them

> **Run the code for this lesson**
> ```bash
> mvn -f Spring/01-introduction-and-setup/02-spring-vs-spring-boot-and-the-ecosystem compile exec:java
> ```

Lesson 01 built a forty-line toy container by hand. This lesson replaces it with the real
one — plain Spring Framework, no Spring Boot yet — so you see exactly what Boot adds
before you start using it in lesson 03.

---

## 1. What "Spring" actually refers to

"Spring" is not one thing. It is a family of projects, all built on top of one core:

| Project | What it's for |
| --- | --- |
| **Spring Framework** | The core: the IoC container, dependency injection, AOP, and Spring MVC. Everything else depends on this. |
| **Spring Boot** | Auto-configuration, starters, and an embedded server on top of the Framework — this whole stack, from lesson 03 onward. |
| **Spring Data** | Repository abstractions over JPA, MongoDB, Redis and others — section 05. |
| **Spring Security** | Authentication and authorization — section 07. |
| **Spring Cloud** | Microservices concerns: service discovery, config servers, gateways — section 12. |

Every one of these is still "Spring" — a plugin into the same container. This lesson uses
only the first row.

---

## 2. What the Spring Framework actually is

At its core, the Spring Framework is an implementation of the IoC container from lesson
01, generalised with reflection and annotations instead of a hand-written `HashMap` of
factories. This project depends on exactly one Spring artifact:

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.1.14</version>
</dependency>
```

`spring-context` pulls in `spring-core`, `spring-beans` and `spring-expression`
transitively — this is the container itself, with nothing web-related in it at all.

---

## 3. Turning lesson 01's classes into Spring beans

Compare `OrderService` here to lesson 01's manually-written version. The constructor is
identical:

```java
@Component
public class OrderService {
    private final Notifier notifier;

    public OrderService(Notifier notifier) {   // same as lesson 01 — still just DI
        this.notifier = notifier;
    }
    ...
}
```

The only new thing is `@Component`. That single annotation is Spring's replacement for
this line from lesson 01's `MiniContainer`:

```java
container.register(OrderService.class, c -> new OrderService(c.get(Notifier.class)));
```

You never write that line yourself. Instead, `@ComponentScan` tells Spring which package
to search:

```java
@Configuration
@ComponentScan(basePackages = "com.danish.spring.intro")
public class AppConfig {
}
```

At startup, Spring walks that package with **reflection**, finds every class annotated
`@Component` (or one of its specialisations — `@Service`, `@Repository`, `@Controller`,
covered in lesson 07), inspects each one's constructor, and works out which other beans
to pass in — recursively, exactly like `MiniContainer.get` did, just automatic.

A bean discovered this way is a **managed bean**: an object whose entire lifecycle —
creation, dependency injection, and (later) destruction — belongs to the container, not
to your code.

---

## 4. Starting the container by hand

Without Spring Boot, you start the container yourself, with one line:

```java
ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
```

`AnnotationConfigApplicationContext` is one implementation of the `ApplicationContext`
interface — the interface version of `MiniContainer`. Constructing it does all the work:
scanning, building, and wiring every bean, before the constructor returns. Then you can
ask it for a fully-wired object:

```java
OrderService orderService = context.getBean(OrderService.class);
orderService.placeOrder("desk-lamp");   // Notifier was injected automatically
```

Ask twice and you get the **same instance** — Spring beans are singletons by default
(the full story, including the other scopes, is lesson 10):

```java
context.getBean(OrderService.class) == context.getBean(OrderService.class)   // true
```

Plain Spring applications must close their own context (`context.close()`), which runs
each bean's destruction callbacks. Spring Boot registers a JVM shutdown hook for you, so
you rarely call `close()` yourself in a Boot application.

---

## 5. What this project deliberately does not have

Running this lesson's `main` method prints a few lines and exits. That is normal — this
is plain Spring, not Spring Boot, and highlights exactly what Boot adds next:

| Missing here | What Spring Boot adds (lesson 03+) |
| --- | --- |
| No embedded web server | An embedded Tomcat (or Jetty/Undertow), started automatically |
| No auto-configuration | Sensible defaults inferred from what's on the classpath |
| A hand-picked `spring-context` version | A single **starter** dependency that manages compatible versions for you |
| No `application.properties`/`.yml` | Externalized configuration, read automatically (lesson 05) |

**Spring Boot does not replace the Spring Framework — it configures it for you.** The
`ApplicationContext` you started by hand above is the exact same class Spring Boot starts
internally; Boot just decides *which* beans to register based on what dependencies it
finds on the classpath, instead of you writing an `AppConfig` by hand.

---

## 6. Why Spring Boot exists at all

Before Boot (pre-2014), a real Spring web application needed:

- an XML or Java `AppConfig` listing every bean by hand,
- a `web.xml` wiring a `DispatcherServlet` into a servlet container,
- a WAR file, deployed into an external Tomcat you installed and managed yourself,
- and manually-chosen, manually-matched versions of a dozen Spring modules that had to be
  compatible with each other.

Spring Boot's insight: almost every Spring web application starts from the same handful of
decisions. So Boot ships **opinionated defaults** — "if `spring-boot-starter-web` is on
the classpath, register a `DispatcherServlet` and start an embedded Tomcat on port 8080,
unless told otherwise" — and lets you override any single decision without losing the
rest. That mechanism is called **auto-configuration**, and it is the entire subject of
lesson 15.

---

## 7. Summary

- **Spring** is a family of projects (Framework, Boot, Data, Security, Cloud) built on one
  IoC container.
- The **Spring Framework** is the container itself — `@Component`, `@ComponentScan`, and
  `ApplicationContext` are the annotation-and-reflection version of lesson 01's
  `MiniContainer`.
- `@Component` replaces a manual `container.register(...)` call; `@ComponentScan` tells
  Spring which package to search for them.
- `AnnotationConfigApplicationContext` starts the container and resolves the whole bean
  graph before its constructor returns.
- Beans are **singletons by default** — repeated `getBean()` calls return the same
  instance.
- **Spring Boot does not replace the Framework.** It adds auto-configuration, starters, an
  embedded server, and externalized configuration on top of the exact same container.
- Auto-configuration exists because most Spring web applications start from the same
  handful of decisions — Boot makes those decisions for you, and lets you override any of
  them individually.

---

**Previous:** [01 — Why Spring, the problem DI solves](../01-why-spring-the-problem-di-solves/01-why-spring-the-problem-di-solves.md) ·
**Next:** [03 — Your first Spring Boot project](../03-your-first-spring-boot-project/03-your-first-spring-boot-project.md)
