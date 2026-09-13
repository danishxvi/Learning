package com.danish.spring.caching;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SelfInvokingPriceService {

    private final AtomicInteger lookupCount = new AtomicInteger();

    @Cacheable(cacheNames = "prices", key = "'self:' + #productId")
    public BigDecimal cachedLookup(String productId) {
        lookupCount.incrementAndGet();
        return BigDecimal.valueOf(75);
    }

    // Same proxy limitation as @Transactional (lesson 38) and @Async (lesson 53): a call
    // to "this.cachedLookup(...)" from inside the same bean never passes through the
    // Spring-managed proxy that actually implements caching, so it runs the real method
    // body EVERY time regardless of what's already cached.
    public BigDecimal callCachedLookupViaThis(String productId) {
        return cachedLookup(productId);
    }

    public int getLookupCount() {
        return lookupCount.get();
    }
}
