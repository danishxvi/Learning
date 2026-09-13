# 62 - Inter-Service Calls with Feign

Lesson 60 called `inventory-service` from `order-service` the manual way:
inject `DiscoveryClient`, call `getInstances(...)`, build a URL by hand,
inject `RestTemplate`, call `getForObject(...)`. That's a lot of repeated
boilerplate for what is, conceptually, just "call this other service's
endpoint." **OpenFeign** turns it into a plain Java interface with no
implementation body - Spring generates a real HTTP client from the method
signature. Everything below ran against real, separately-started services.

Three separate runnable Maven projects, same shape as lesson 60:

```
62-inter-service-calls-with-feign/
├── eureka-server/       - the registry (same role as lesson 60)
├── inventory-service/   - the service being called
└── order-service/       - calls inventory-service DECLARATIVELY via Feign
```

```bash
mvn -f Spring/12-microservices-with-spring-cloud/62-inter-service-calls-with-feign/eureka-server spring-boot:run
mvn -f Spring/12-microservices-with-spring-cloud/62-inter-service-calls-with-feign/inventory-service spring-boot:run
mvn -f Spring/12-microservices-with-spring-cloud/62-inter-service-calls-with-feign/order-service spring-boot:run
```

## The declarative client

[`InventoryClient.java`](order-service/src/main/java/com/danish/spring/orderservice/InventoryClient.java):

```java
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/inventory/{productId}")
    String getStock(@PathVariable("productId") String productId);
}
```

`name = "inventory-service"` is the Eureka service name - Feign resolves it
through the same service discovery mechanism `DiscoveryClient` used manually
in lesson 60, then load-balances across whatever instances are registered.
[`OrderController.java`](order-service/src/main/java/com/danish/spring/orderservice/OrderController.java)
just calls it like a normal method:

```java
public String orderWithStock(@PathVariable String productId) {
    String stockResponse = inventoryClient.getStock(productId);
    return "order-service called inventory-service via Feign and got: " + stockResponse;
}
```

There is no `RestTemplate`, no manually-built URL, no `DiscoveryClient`
anywhere in `order-service` - `@EnableFeignClients` on the main application
class is enough for Spring to generate a real implementation of
`InventoryClient` at startup. With all three services running, real output:

```bash
curl -s http://localhost:8082/order-with-stock/widget
```
```
order-service called inventory-service via Feign and got: product widget has 42 units in stock (answered by inventory-service on port 8081)
```

## A real gotcha: `@PathVariable` is not optional here

The first version of `InventoryClient` written for this lesson omitted the
annotation, on the assumption that Spring's usual parameter-name-based
binding (which works on `@Controller` methods compiled with `-parameters`,
Spring Boot's default) would apply here too:

```java
@GetMapping("/inventory/{productId}")
String getStock(String productId);  // no @PathVariable
```

This compiled fine and the application **started up with no error at all** -
there is no startup-time validation catching this. The real problem only
appeared at call time:

```
feign.FeignException$NotFound: [404] during [GET] to [http://inventory-service/inventory/] [InventoryClient#getStock(String)]: [{"timestamp":"2026-09-13T08:24:34.410+00:00","status":404,"error":"Not Found","path":"/inventory/"}]
```

**What actually happened**: Feign's contract (`SpringMvcContract`, used by
`spring-cloud-starter-openfeign`) needs an *explicit* annotation
(`@PathVariable`, `@RequestParam`, `@RequestBody`, ...) to know what an
interface method's parameter is for - it does not fall back to compiled
parameter names the way Spring MVC controllers can. Without one, Feign built
the request URL by substituting *nothing* into `{productId}`, sent a GET
request to the literal `http://inventory-service/inventory/` (trailing
slash, no product id at all), and `inventory-service` correctly replied
`404` because no route matches an empty path segment. That 404 came back to
`order-service` as a real `FeignException.NotFound`, uncaught, surfacing as
a `500` to whoever called `order-service`.

The fix was one annotation:

```java
String getStock(@PathVariable("productId") String productId);
```

With that change, the exact same call succeeded, as shown above.

## `FeignException` carries the real downstream status

`inventory-service`'s `/inventory/out-of-stock-error` endpoint deliberately
throws, producing a real `500`. Calling it through the now-fixed
`InventoryClient` produced a different, precisely-typed exception:

```
feign.FeignException$InternalServerError: [500] during [GET] to [http://inventory-service/inventory/out-of-stock-error] [InventoryClient#getStock(String)]: [{"timestamp":"2026-09-13T08:25:25.645+00:00","status":500,"error":"Internal Server Error","path":"/inventory/out-of-stock-error"}]
```

Note the difference from the earlier gotcha: `FeignException$NotFound` for
the 404, `FeignException$InternalServerError` for the 500 - Feign's default
error decoder maps the actual downstream HTTP status to a specific
`FeignException` subtype, both carrying the full response body from the
real remote call. Code that calls a `@FeignClient` method can catch these
specific subtypes to distinguish "the other service said no such resource"
from "the other service is broken," without parsing status codes manually.

## Key takeaways

- `@FeignClient(name = "...")` plus a plain interface method, annotated like
  a controller method (`@GetMapping`, `@PathVariable`, etc.), is enough to
  generate a real, working, load-balanced HTTP client - no implementation
  code, no manual URL-building, no injected `RestTemplate`.
- Feign's contract requires explicit parameter annotations
  (`@PathVariable`, `@RequestParam`, ...) - unlike a Spring MVC controller,
  it does not infer binding from a compiled parameter name, and a missing
  annotation is not caught at startup. It fails silently at *call* time,
  producing a request with the parameter simply missing from the URL.
- `FeignException` subtypes (`NotFound`, `InternalServerError`, etc.) mirror
  the real HTTP status code returned by the actual downstream service,
  making it possible to handle different failure modes from a remote call
  distinctly, using the genuine response rather than a guess.
- This lesson's client code does exactly what lesson 60's manual
  `DiscoveryClient` + `RestTemplate` code did, in a fraction of the lines -
  Feign is a convenience layer over the same discovery and HTTP mechanisms,
  not a different underlying model.
