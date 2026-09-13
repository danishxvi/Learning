# 55 - The Spring Cache Abstraction

`@Cacheable` intercepts a method call: on the first call with a given set of
arguments, it lets the real method run and remembers the result; on every
later call with the *same* arguments, it returns the remembered result
without ever running the method body again. Everything below was measured
with real timing and a real invocation counter, not assumed.

```bash
mvn -f Spring/10-caching/55-the-spring-cache-abstraction spring-boot:run
```

## Turning it on

[`CachingConfig.java`](src/main/java/com/danish/spring/caching/CachingConfig.java):

```java
@Configuration
@EnableCaching
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("prices");
    }
}
```

`@EnableCaching` is what turns `@Cacheable` from an inert annotation into a
real AOP proxy - the same proxy mechanism `@Transactional` (lesson 38) and
`@Async` (lesson 53) use. `ConcurrentMapCacheManager` backs the named cache
("prices" here) with a plain `ConcurrentHashMap`: in-memory, single-JVM, no
eviction policy, no TTL, gone when the process exits. Lesson 56 swaps this
for a real Redis-backed `CacheManager` without changing a single `@Cacheable`
annotation - the abstraction is the point.

## `@Cacheable`: first call is real, later calls with the same key are not

[`PriceLookupService.java`](src/main/java/com/danish/spring/caching/PriceLookupService.java):

```java
@Cacheable(cacheNames = "prices", key = "#productId")
public BigDecimal getPrice(String productId) {
    lookupCount.incrementAndGet();
    simulateSlowLookup();  // Thread.sleep(500)
    return REAL_PRICES.getOrDefault(productId, BigDecimal.valueOf(75));
}
```

`lookupCount` only increments inside the real method body - a cache hit never
reaches it, so it's proof a call was actually computed rather than served
from cache. Real output:

```
@Cacheable - first call is a real (slow) lookup, second is a cache hit
==========================================================================
  1st call, widget: 551ms, lookupCount=1
  2nd call, widget: 0ms, lookupCount=1  (unchanged - served from cache)
  1st call, gadget (different key): 507ms, lookupCount=2  (a genuine miss - different cache key)
```

The first call to `getPrice("widget")` took 551ms - the simulated slow
lookup genuinely ran. The second call, same argument, took 0ms and
`lookupCount` didn't move: the method body never ran at all, the proxy
returned the cached value directly. `getPrice("gadget")` - a different
argument, therefore a different cache key under `key = "#productId"` - was a
genuine 507ms miss, exactly as expected: caching is per-key, not per-method.

## `unless`: computed either way, cached only conditionally

```java
@Cacheable(cacheNames = "prices", key = "'unless:' + #productId",
        unless = "#result.compareTo(new java.math.BigDecimal('100')) > 0")
public BigDecimal getPriceUnlessExpensive(String productId) {
    lookupCount.incrementAndGet();
    simulateSlowLookup();
    return REAL_PRICES.getOrDefault(productId, BigDecimal.valueOf(75));
}
```

`unless` is a SpEL expression evaluated *after* the method runs, against
`#result` - if it's `true`, the result is still returned to the caller
normally, but is **not** written to the cache. Real output:

```
unless - the result is computed either way, but only cached conditionally
==========================================================================
  widget (50, <= 100): 2 calls, lookupCount went from 2 to 3  (2nd call was a cache hit)
  gadget (150, > 100): 2 calls, lookupCount went from 3 to 5  (both ran for real - unless blocked caching)
```

`widget` costs 50 (`<= 100`), so `unless` evaluates to `false` and it caches
normally - the second call was a hit (`lookupCount` moved by only 1 across
both calls). `gadget` costs 150 (`> 100`), so `unless` evaluates to `true`
every time - it's never cached, and both calls genuinely ran
(`lookupCount` moved by 2).

**A real gotcha hit while writing this**: the first attempt wrote `unless`
as `#result.compareTo(T(java.math.BigDecimal).valueOf(100)) > 0` and the
application failed to start with:

```
SpelEvaluationException: EL1033E: Method call of 'valueOf' is ambiguous,
supported type conversions allow multiple variants to match
```

`BigDecimal.valueOf` is overloaded (`valueOf(long)` and `valueOf(double)`),
and SpEL's reflective method resolver couldn't pick one for the plain integer
literal `100` - both are reachable via a widening/boxing conversion. The fix
was to sidestep overload resolution entirely with the single-argument
`String` constructor, `new java.math.BigDecimal('100')`, which has no
overload ambiguity. SpEL expressions in annotations are still regular method
calls resolved at runtime - the same overload-resolution rules and pitfalls
apply as anywhere else, just without a compiler to catch them before you run
the code.

## `@CachePut` always runs; `@CacheEvict` clears the entry

```java
@CachePut(cacheNames = "prices", key = "#productId")
public BigDecimal updatePrice(String productId, BigDecimal newPrice) {
    lookupCount.incrementAndGet();
    return newPrice;
}

@CacheEvict(cacheNames = "prices", key = "#productId")
public void evictPrice(String productId) {
}
```

Real output:

```
@CachePut always runs, but updates the cache; @CacheEvict clears it
==========================================================================
  cached price for widget before update: 50
  updatePrice(widget, 999) returned 999, lookupCount went from 5 to 6  (ran for real, as @CachePut always does)
  getPrice(widget) now returns 999, lookupCount unchanged at 6 (cache hit, sees the update)
  after evictPrice(widget), getPrice(widget) returns 50 (back to the real source value), lookupCount went from 6 to 7  (a genuine miss again)
```

`@CachePut` is NOT a shortcut for "cache if not cached" - it unconditionally
runs the method body (`lookupCount` moved) and unconditionally overwrites
whatever was in the cache under that key with whatever the method returned.
The very next `getPrice("widget")` call is a hit that returns `999`, proving
the cache genuinely holds the *updated* value now, not the original 50.
`evictPrice("widget")` removes that entry entirely; the next `getPrice`
call is a real miss again, and - since `updatePrice` only ever touched the
cache, never the underlying `REAL_PRICES` map - it returns the original `50`,
confirming the eviction really cleared the stale `999`.

## Self-invocation silently bypasses `@Cacheable`

[`SelfInvokingPriceService.java`](src/main/java/com/danish/spring/caching/SelfInvokingPriceService.java):

```java
@Cacheable(cacheNames = "prices", key = "'self:' + #productId")
public BigDecimal cachedLookup(String productId) {
    lookupCount.incrementAndGet();
    return BigDecimal.valueOf(75);
}

public BigDecimal callCachedLookupViaThis(String productId) {
    return cachedLookup(productId);
}
```

Real output:

```
self-invocation bypasses the @Cacheable proxy entirely
==========================================================================
  2 calls THROUGH the proxy (cachedLookup directly): lookupCount=1  (2nd was a cache hit)
  2 calls via callCachedLookupViaThis (self-invocation): lookupCount went from 1 to 3  (BOTH ran for real - @Cacheable was silently ignored)
```

Calling `cachedLookup` directly (through the injected bean, i.e. through the
proxy) behaves exactly as expected - one real call, one cache hit. But
`callCachedLookupViaThis` calls `cachedLookup(productId)` as a plain,
unqualified call on `this` from inside the same class - that call never
passes through the Spring-managed proxy that actually implements caching, so
it runs the real method body **every single time**, with no error, warning,
or indication that `@Cacheable` was ignored. This is the exact same proxy
limitation documented for `@Transactional` in lesson 38 and `@Async` in
lesson 53 - any Spring AOP annotation only works when the call arrives
through the proxy. The fix is the same one used throughout this section: move
the cached method to a separate bean and call it through an injected
reference, never through `this`.

## Key takeaways

- `@Cacheable` skips the real method entirely on a cache hit; `@CachePut`
  always runs the method but updates the cache with its result; `@CacheEvict`
  removes an entry so the next call is a genuine miss.
- `key` controls which calls share a cache entry - different key values are
  entirely independent cache slots, even on the same method.
- `unless` is evaluated against the method's actual result, after it runs -
  it decides whether to *store* the result, not whether to *compute* it.
- SpEL expressions inside cache annotations are regular runtime method calls
  with regular Java overload-resolution rules (and their ambiguities) -
  `BigDecimal.valueOf(100)` is genuinely ambiguous to SpEL's resolver; a
  single-argument constructor call sidesteps it.
- Self-invocation (`this.cachedMethod()` from inside the same bean) bypasses
  the caching proxy entirely and always runs the real method, silently -
  the same limitation `@Transactional` and `@Async` share, for the same
  underlying reason (Spring AOP proxies only intercept calls that arrive from
  outside the bean).
