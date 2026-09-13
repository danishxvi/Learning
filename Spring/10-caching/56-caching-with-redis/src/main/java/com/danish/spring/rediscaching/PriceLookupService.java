package com.danish.spring.rediscaching;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PriceLookupService {

    private final AtomicInteger lookupCount = new AtomicInteger();

    @Cacheable(cacheNames = "prices", key = "#productId")
    public BigDecimal getPrice(String productId) {
        lookupCount.incrementAndGet();
        simulateSlowLookup();
        return BigDecimal.valueOf(Math.abs(productId.hashCode() % 200));
    }

    @Cacheable(cacheNames = "shortlived", key = "#productId")
    public BigDecimal getShortLivedPrice(String productId) {
        lookupCount.incrementAndGet();
        simulateSlowLookup();
        return BigDecimal.valueOf(Math.abs(productId.hashCode() % 200));
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
