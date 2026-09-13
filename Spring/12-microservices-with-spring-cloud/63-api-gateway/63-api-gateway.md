# 63 - API Gateway

Every previous lesson in this section has one service calling another
directly - `order-service` knew (or discovered) exactly which service to
call. An **API gateway** sits in front of a whole fleet of services as a
single entry point: external clients talk only to the gateway, and the
gateway routes each request to the right backend service based on the path.
Spring Cloud Gateway is Spring's reactive, WebFlux-based implementation of
this pattern. Every route below was tested against real, separately-running
services.

Three separate runnable Maven projects:

```
63-api-gateway/
├── eureka-server/       - the registry (same role as lessons 60 and 62)
├── inventory-service/   - a backend service
└── api-gateway/         - routes incoming requests to backend services
```

```bash
mvn -f Spring/12-microservices-with-spring-cloud/63-api-gateway/eureka-server spring-boot:run
mvn -f Spring/12-microservices-with-spring-cloud/63-api-gateway/inventory-service spring-boot:run
mvn -f Spring/12-microservices-with-spring-cloud/63-api-gateway/api-gateway spring-boot:run
```

## A real, immediate gotcha: Spring MVC and Spring Cloud Gateway don't mix

The very first version of `api-gateway`'s `pom.xml` for this lesson included
`spring-boot-starter-web` out of habit - every other service in this
curriculum has it. Starting the application with it present produced a real,
immediate startup failure:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Spring MVC found on classpath, which is incompatible with Spring Cloud Gateway.

Action:

Please set spring.main.web-application-type=reactive or remove spring-boot-starter-web dependency.
```

**Why**: Spring Cloud Gateway is built entirely on **WebFlux** - a reactive,
non-blocking, Netty-based web stack - not the Servlet-based Spring MVC every
other lesson in this curriculum has used. The two web stacks are mutually
exclusive within one application; Spring even ships a dedicated
`GatewayClassPathWarningAutoConfiguration` specifically to catch this
combination early and fail with a clear, actionable message rather than a
confusing runtime error later. The fix was simply removing
`spring-boot-starter-web` from [`pom.xml`](api-gateway/pom.xml) - `spring-cloud-starter-gateway`
already brings WebFlux transitively, and nothing else in this lesson needs
the Servlet stack.

## Discovery-based routing: one property, every registered service routable

```properties
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
```

With this enabled, the gateway automatically creates a route for **every**
service currently registered with Eureka, at
`/<lowercased-service-id>/**` → that service, load-balanced across its
instances. No route needs to be declared by hand for `inventory-service`
specifically - it's just discoverable. Real output, with `eureka-server` and
`inventory-service` running:

```bash
curl -s http://localhost:8080/inventory-service/inventory/widget
```
```
product widget has 42 units in stock (answered by inventory-service on port 8081)
```

The gateway (port `8080`) received this request, recognized
`/inventory-service/**` as matching the auto-generated route for the
`inventory-service` registered with Eureka, and forwarded it to the real
instance on port `8081` - the response confirms it, naming the actual
backend port that answered.

## A hand-written route: path rewriting

```properties
spring.cloud.gateway.routes[0].id=stock-route
spring.cloud.gateway.routes[0].uri=lb://inventory-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/stock/**
spring.cloud.gateway.routes[0].filters[0]=RewritePath=/api/stock/(?<id>.*), /inventory/${id}
```

This route matches a **different** external path shape
(`/api/stock/{id}`, closer to what an external API contract might actually
look like) and rewrites it to the internal path `inventory-service` actually
exposes (`/inventory/{id}`) before forwarding - the caller and the backend
service can use completely different URL shapes, with the gateway
translating between them. Real output:

```bash
curl -s http://localhost:8080/api/stock/widget
```
```
product widget has 42 units in stock (answered by inventory-service on port 8081)
```

Same backend, same real response - reached through a URL that
`inventory-service` itself has no route for at all
(`inventory-service` only knows `/inventory/{id}`, never `/api/stock/{id}`).
The `lb://inventory-service` URI scheme tells the gateway to resolve
`inventory-service` through the load balancer (Eureka-backed, same
mechanism as the discovery-based routes) rather than treating it as a
literal hostname.

A path that matches no route at all fails cleanly rather than silently:

```bash
curl -s -w "\nSTATUS:%{http_code}\n" http://localhost:8080/nonexistent-service/foo
```
```json
{"timestamp":"...","path":"/nonexistent-service/foo","status":404,"error":"Not Found"}
STATUS:404
```

## Key takeaways

- Spring Cloud Gateway requires WebFlux and is genuinely incompatible with
  `spring-boot-starter-web` in the same application - a real, well-defined
  startup failure with an explicit message, not a subtle runtime bug, thanks
  to Spring's own dedicated check for this exact mistake.
- `spring.cloud.gateway.discovery.locator.enabled=true` auto-generates a
  route per registered Eureka service, at `/<service-id>/**` - zero
  hand-written routing needed for the common case of "just expose this
  service through the gateway."
- Hand-written routes (`predicates` + `filters`, or `RouteLocatorBuilder`
  beans) let the gateway present a completely different external URL shape
  than a backend service's own internal routes - `RewritePath` translated a
  clean `/api/stock/{id}` into the backend's actual `/inventory/{id}` in
  this lesson, transparently to the caller.
- `uri=lb://<service-name>` resolves through the same Eureka-backed load
  balancer used everywhere else in this section - the gateway is not a
  special case bolted on top of service discovery, it's built on the same
  mechanism.
