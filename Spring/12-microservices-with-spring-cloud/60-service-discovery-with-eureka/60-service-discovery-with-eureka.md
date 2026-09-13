# 60 - Service Discovery with Eureka

Every previous lesson in this curriculum has had one service talking to one
database, cache, or broker at a known, fixed address. A real microservice
architecture doesn't work that way: services come and go, scale up and down,
and move between hosts and ports - hardcoding `http://localhost:8081` into
another service's code breaks the moment that address changes. **Service
discovery** solves this: services register themselves with a directory by
*name*, and other services look up that name to find out where they
currently live. Netflix Eureka, via Spring Cloud, is the classic
implementation. Everything below is real: three separate Spring Boot
processes, actually started, actually talking to each other over real HTTP.

This lesson has three separate runnable Maven projects, not one:

```
60-service-discovery-with-eureka/
├── eureka-server/       - the registry itself
├── inventory-service/   - a service that REGISTERS
└── order-service/       - a service that DISCOVERS and calls another
```

## The registry: `eureka-server`

[`EurekaServerApplication.java`](eureka-server/src/main/java/com/danish/spring/eurekaserver/EurekaServerApplication.java):

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

`@EnableEurekaServer` turns a plain Spring Boot app into a Eureka registry -
a directory service other apps register with and query. The two
`false` properties keep this instance standalone (it would otherwise try to
register with *itself* as a client, which only matters in a
multi-node/highly-available Eureka setup - out of scope here).

```bash
mvn -f Spring/12-microservices-with-spring-cloud/60-service-discovery-with-eureka/eureka-server spring-boot:run
```

Its dashboard lives at `http://localhost:8761`.

## A service that registers: `inventory-service`

[`InventoryController.java`](inventory-service/src/main/java/com/danish/spring/inventoryservice/InventoryController.java)
exposes one trivial endpoint; the interesting part is entirely in
`application.properties`:

```properties
spring.application.name=inventory-service
server.port=8081
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
```

`spring-cloud-starter-netflix-eureka-client` on the classpath plus that one
property is enough - Spring Boot auto-configures a Eureka client that
registers this instance under the name `inventory-service` on startup, and
sends a heartbeat every 30 seconds to keep the registration alive. Started
and confirmed with real output:

```bash
mvn -f Spring/12-microservices-with-spring-cloud/60-service-discovery-with-eureka/inventory-service spring-boot:run
```

```bash
curl -s http://localhost:8761/eureka/apps/INVENTORY-SERVICE
```
```xml
<application>
  <name>INVENTORY-SERVICE</name>
  <instance>
    <instanceId>DESKTOP-CAFHDVM.mshome.net:inventory-service:8081</instanceId>
    <status>UP</status>
    <port enabled="true">8081</port>
    <vipAddress>inventory-service</vipAddress>
    ...
```

This is real registry state, fetched with a plain `curl` - nothing about
this required the Spring app to be involved in producing this data once
it registered.

## A service that discovers: `order-service`

[`OrderController.java`](order-service/src/main/java/com/danish/spring/orderservice/OrderController.java)
uses Spring Cloud's `DiscoveryClient` abstraction to look up
`inventory-service` **by name** and call it - no hardcoded host or port
anywhere:

```java
@GetMapping("/order-with-stock/{productId}")
public String orderWithStock(@PathVariable String productId) {
    List<ServiceInstance> instances = discoveryClient.getInstances("inventory-service");
    if (instances.isEmpty()) {
        return "inventory-service is not currently registered with Eureka";
    }
    ServiceInstance instance = instances.get(0);
    String url = instance.getUri() + "/inventory/" + productId;
    String inventoryResponse = restTemplate.getForObject(url, String.class);
    return "order-service discovered inventory-service at " + instance.getUri() + " and got: " + inventoryResponse;
}
```

With all three processes running, real output:

```bash
curl -s http://localhost:8082/discover/inventory-service
```
```
INVENTORY-SERVICE @ http://172.23.160.1:8081
```

```bash
curl -s http://localhost:8082/order-with-stock/widget
```
```
order-service discovered inventory-service at http://172.23.160.1:8081 and got: product widget has 42 units in stock (answered by inventory-service on port 8081)
```

`order-service` never had `8081` written anywhere in its code or config -
it asked Eureka "where is inventory-service right now?" at the moment of the
call, got back a real address, and used it. If `inventory-service` were
restarted on a different port, this code would need zero changes.

## A real, valuable gotcha: Eureka's self-preservation mode

To see what happens when a registered service actually disappears,
`inventory-service`'s process was force-killed (`taskkill /F`, simulating a
crash rather than a graceful shutdown - a graceful shutdown would have sent
Eureka an explicit deregister request instead). Eureka's lease mechanism is
supposed to notice a service stopped sending heartbeats and evict it after
the lease expires (default: 90 seconds). After waiting **well over two
minutes**, real output:

```bash
curl -s http://localhost:8761/eureka/apps/INVENTORY-SERVICE
```
```xml
<instance>
  <instanceId>DESKTOP-CAFHDVM.mshome.net:inventory-service:8081</instanceId>
  <status>UP</status>
  ...
```

Still `UP`. Still fully registered. The dashboard revealed why:

```bash
curl -s http://localhost:8761/ | grep -i emergency
```
```html
<h4><font color="red"><b>EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.</b></font></h4>
```

**Self-preservation mode.** Eureka expects a minimum rate of renewal
heartbeats across all registered instances; when that rate drops too low
(in this tiny two-service setup, killing one of two instances alone was
enough to cross the threshold), Eureka assumes it might be experiencing a
network partition between itself and its clients rather than genuinely dead
instances - and, to avoid mass-evicting services that are actually still
healthy but just can't currently reach the registry, it stops expiring
*anything* until renewals recover. This is a deliberate, documented Eureka
design choice, not a bug: in a real network partition, evicting every
service that can't currently renew its lease would be far more destructive
than temporarily serving stale registry data.

**The real, concrete consequence**: `order-service`'s discovery call still
happily returned the dead instance:

```bash
curl -s http://localhost:8082/discover/inventory-service
```
```
INVENTORY-SERVICE @ http://172.23.160.1:8081
```

And attempting to actually use it failed - not with a clean "not found," but
with a real connection failure, because the address was real once and Eureka
was simply still reporting it:

```bash
curl -s http://localhost:8082/order-with-stock/widget
```
```json
{"timestamp":"2026-09-13T08:13:37.368+00:00","status":500,"error":"Internal Server Error","path":"/order-with-stock/widget"}
```

The actual exception, from `order-service`'s own log:

```
org.springframework.web.client.ResourceAccessException: I/O error on GET request for "http://172.23.160.1:8081/inventory/widget": Connection refused: connect
Caused by: java.net.ConnectException: Connection refused: connect
```

**The takeaway that matters**: service discovery tells you where a service
*was last known to be registered* - it is not a live health check performed
at the moment you ask. A discovered address can be stale, especially under
self-preservation, and code that calls a discovered instance still needs to
handle the call itself failing (this is exactly the gap circuit breakers,
lesson 64, exist to cover - discovery finds an address; resilience patterns
handle that address not actually working).

## Key takeaways

- `@EnableEurekaServer` plus two `false` client properties makes a plain
  Spring Boot app into a standalone service registry.
- Any service with `spring-cloud-starter-netflix-eureka-client` and a
  `eureka.client.service-url.defaultZone` property registers itself under
  `spring.application.name` automatically, with periodic heartbeats keeping
  the registration alive.
- `DiscoveryClient.getInstances(name)` looks up real, currently-registered
  addresses for a logical service name - calling code never hardcodes a
  host or port for another service.
- Eureka's **self-preservation mode** stops evicting stale registrations
  when the overall heartbeat renewal rate drops too low, on the assumption
  that a network partition (not a real mass outage) is more likely - this is
  a genuine, documented trade-off, and it means a discovered address can be
  stale even well past the normal lease-expiry window.
- Service discovery answers "where is this service registered?", not "is
  this service currently reachable?" - a successful discovery lookup can
  still be followed by a real connection failure, exactly as demonstrated
  here with a real `ConnectException`.
