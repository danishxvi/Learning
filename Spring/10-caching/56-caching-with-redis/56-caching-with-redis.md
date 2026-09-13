# 56 - Caching with Redis

Lesson 55 built `@Cacheable` on `ConcurrentMapCacheManager` - a cache backed
by a plain `ConcurrentHashMap` living inside this one JVM's heap, gone the
moment the process exits. This lesson swaps in `RedisCacheManager`, backed by
a real, separate Redis server - **without changing a single `@Cacheable`
annotation**. That swap, and what it actually buys you, is the whole lesson.

## Prerequisite: a real Redis instance

This lesson needs an actual Redis server. Start one with Docker:

```bash
docker run -d --name learning-redis -p 6380:6379 redis:7-alpine
```

> **Why plain `docker run` and not Testcontainers?** Lesson 50 documented a
> real, unresolved incompatibility between Testcontainers' Java client and
> this machine's Docker Desktop named-pipe API. That issue is specific to
> Testcontainers' `docker-java` client library - the Docker CLI itself, and a
> container started directly with `docker run`, work completely normally,
> which is exactly what this lesson relies on instead.

`application.properties` points Spring at that container:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6380
```

```bash
mvn -f Spring/10-caching/56-caching-with-redis spring-boot:run
```

## The only thing that changes: the `CacheManager` bean

[`RedisCacheConfig.java`](src/main/java/com/danish/spring/rediscaching/RedisCacheConfig.java):

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        RedisCacheConfiguration shortLived = defaults.entryTtl(Duration.ofSeconds(3));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration("shortlived", shortLived)
                .build();
    }
}
```

[`PriceLookupService.java`](src/main/java/com/danish/spring/rediscaching/PriceLookupService.java)
is annotated exactly like lesson 55's version - `@Cacheable(cacheNames =
"prices", key = "#productId")` - because `@Cacheable` talks to the `Cache`
and `CacheManager` interfaces, never to a concrete implementation. Swapping
`ConcurrentMapCacheManager` for `RedisCacheManager` is the entire migration.

**A real dependency gotcha hit while building this**: the first run failed at
startup with `NoClassDefFoundError: com/fasterxml/jackson/databind/jsontype/TypeResolverBuilder`.
`GenericJackson2JsonRedisSerializer` needs `jackson-databind` on the
classpath, and unlike a typical Spring Boot web application,
`spring-boot-starter-cache` + `spring-boot-starter-data-redis` alone don't
pull it in transitively - that normally arrives via
`spring-boot-starter-web`/`-json`, which this lesson doesn't have. The fix
was adding `com.fasterxml.jackson.core:jackson-databind` explicitly to
[`pom.xml`](pom.xml).

## Same hit/miss behavior, a different (and slower) store

Real output, same shape as lesson 55's basic scenario:

```
@Cacheable backed by Redis - same behavior as lesson 55, different store
==========================================================================
  1st call: 1103ms, lookupCount=1
  2nd call: 24ms, lookupCount=1  (unchanged - served from Redis, not this JVM's heap)
```

Note the numbers: the first call took **1103ms**, not the ~500ms the
simulated slow lookup alone accounts for - the extra ~600ms is a genuine
network round trip to Redis to check for (and then write) the cache entry,
something `ConcurrentMapCacheManager` in lesson 55 never pays. The second
call, a cache hit, still took 24ms rather than ~0ms - reading from Redis over
the network is real work too, just far less than the full lookup. Caching
with a network-attached store is not free the way an in-process map is; it
trades some latency for the properties below.

**Verified directly against Redis**, outside the application entirely:

```bash
docker exec learning-redis redis-cli keys '*'
```
```
prices::widget
prices::basic-1789285721325
```

```bash
docker exec learning-redis redis-cli get 'prices::widget'
```
```
["java.math.BigDecimal",92]
```

The key is genuinely `prices::widget` (Redis's default key-namespacing:
`<cacheName>::<key>`), and the value is genuinely stored as the
`GenericJackson2JsonRedisSerializer` JSON format - a `[type, value]` pair
that lets Redis deserialize it back to a real `BigDecimal` later. This is
data sitting in a completely separate process, inspectable with a plain
Redis client that has never heard of Spring.

## The actual payoff: the cache survives this JVM

[`RedisCachingApplication.java`](src/main/java/com/danish/spring/rediscaching/RedisCachingApplication.java)
checks, via the plain `CacheManager`/`Cache` API, whether `"widget"` is
already cached *before* calling the service method - this reveals whether the
value was left behind by a **previous, now-dead JVM**:

```java
Cache prices = cacheManager.getCache("prices");
Cache.ValueWrapper existing = prices.get("widget");
System.out.println("  before calling getPrice(\"widget\"): already cached in Redis from a PREVIOUS run? "
        + (existing != null));
```

**First real run** of the whole app (a fresh Redis instance, never touched
before):

```
Redis survives this JVM restarting - re-run this app and watch
==========================================================================
  before calling getPrice("widget"): already cached in Redis from a PREVIOUS run? false
  getPrice("widget") = 92, lookupCount went from 1 to 2  (MISS - first time this key has ever been requested)
```

The app then exited completely - process gone, JVM heap gone,
`ConcurrentMapCacheManager` would have gone with it. But the Redis container
kept running independently. **Second real run**, a brand-new JVM,
`lookupCount` starting fresh at 0:

```
Redis survives this JVM restarting - re-run this app and watch
==========================================================================
  before calling getPrice("widget"): already cached in Redis from a PREVIOUS run? true
    existing value found in Redis: 92
  getPrice("widget") = 92, lookupCount went from 1 to 1  (HIT - this brand-new JVM never computed this value, Redis already had it)
```

This new process's `PriceLookupService.getPrice("widget")` returned `92`
without its own `lookupCount` moving at all - it retrieved a value some
*other*, now-terminated process computed. This is the property
`ConcurrentMapCacheManager` structurally cannot offer: a cache that survives
application restarts and can be shared by multiple application instances
(e.g. several load-balanced copies of the same service, all sharing one
Redis-backed cache instead of each keeping its own, inconsistent, in-memory
copy).

## TTL: Redis expires entries on its own, with no code involved

The `"shortlived"` cache was configured with `entryTtl(Duration.ofSeconds(3))`
- `ConcurrentMapCacheManager` in lesson 55 had no TTL concept at all; entries
lived until explicitly evicted or the JVM died. Real output:

```
the "shortlived" cache has a 3-second TTL - entries expire on their own
==========================================================================
  2 calls immediately: lookupCount went from 2 to 3  (2nd was a cache hit)
  sleeping 3500ms for the TTL to expire...
  call after TTL expiry: lookupCount went from 3 to 4  (a genuine miss - Redis expired the key itself)
```

No application code evicted anything - Redis itself deleted the key after
3 seconds, using its own internal expiry mechanism, and the very next call
was a genuine miss (`lookupCount` moved). This is a real Redis feature the
in-memory cache from lesson 55 has no equivalent for at all.

## Key takeaways

- Swapping the `CacheManager` bean (`ConcurrentMapCacheManager` →
  `RedisCacheManager`) is the entire migration from an in-process cache to a
  distributed one - `@Cacheable`/`@CachePut`/`@CacheEvict` code never
  references the store directly and needs zero changes.
- A Redis-backed cache pays real network latency per call (measured: ~600ms
  extra on a miss, real double-digit milliseconds even on a hit) in exchange
  for surviving application restarts and being shareable across multiple
  application instances - properties an in-process map cannot offer at all.
- `GenericJackson2JsonRedisSerializer` needs `jackson-databind` on the
  classpath explicitly in a non-web application; it isn't pulled in
  transitively without `spring-boot-starter-web`/`-json`.
- Per-cache TTLs (`RedisCacheConfiguration.entryTtl(...)`, applied per named
  cache via `withCacheConfiguration`) let Redis expire stale entries
  automatically, with no eviction code required - something
  `ConcurrentMapCacheManager` cannot do.
- Because Redis is a separate, inspectable process, the cache's real
  contents can be checked directly with `redis-cli`, independent of the
  Spring application - useful for debugging caching behavior that would be
  invisible inside another process's heap.
