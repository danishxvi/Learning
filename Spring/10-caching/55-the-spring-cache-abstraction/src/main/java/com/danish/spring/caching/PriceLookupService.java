package com.danish.spring.caching;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PriceLookupService {

    private static final Map<String, BigDecimal> REAL_PRICES = Map.of(
            "widget", BigDecimal.valueOf(50),
            "gadget", BigDecimal.valueOf(150),
            "gizmo", BigDecimal.valueOf(25)
    );

    // Counts how many times the method BODY actually ran - a cache hit never touches
    // this, so this counter is the proof a call was served from cache vs. really computed.
    private final AtomicInteger lookupCount = new AtomicInteger();

    // key = "#productId" - without an explicit key, Spring's default key generator
    // would build a key from ALL arguments; here there's only one, so it wouldn't
    // matter, but naming it explicitly is what makes the cache entries predictable
    // enough to evict or update by the same key later.
    @Cacheable(cacheNames = "prices", key = "#productId")
    public BigDecimal getPrice(String productId) {
        lookupCount.incrementAndGet();
        simulateSlowLookup();
        return REAL_PRICES.getOrDefault(productId, BigDecimal.valueOf(75));
    }

    // unless is evaluated AFTER the method runs, against #result - the method result is
    // NOT cached (but is still returned to the caller) whenever this is true. Useful for
    // "don't cache errors, nulls, or values not worth caching" without needing an if
    // block that skips the method's own logic.
    @Cacheable(cacheNames = "prices", key = "'unless:' + #productId", unless = "#result.compareTo(new java.math.BigDecimal('100')) > 0")
    public BigDecimal getPriceUnlessExpensive(String productId) {
        lookupCount.incrementAndGet();
        simulateSlowLookup();
        return REAL_PRICES.getOrDefault(productId, BigDecimal.valueOf(75));
    }

    // @CachePut ALWAYS runs the method body - unlike @Cacheable it never skips the call
    // - but still writes whatever it returns into the cache under the given key,
    // overwriting whatever was there. The use case is "I am updating this value; make
    // sure the cache reflects the update" as opposed to "give me the value, from cache
    // if possible."
    @CachePut(cacheNames = "prices", key = "#productId")
    public BigDecimal updatePrice(String productId, BigDecimal newPrice) {
        lookupCount.incrementAndGet();
        return newPrice;
    }

    @CacheEvict(cacheNames = "prices", key = "#productId")
    public void evictPrice(String productId) {
    }

    public int getLookupCount() {
        return lookupCount.get();
    }

    private void simulateSlowLookup() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
