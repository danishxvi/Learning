package com.danish.spring.rediscaching;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// ============================================================================
// 56 - CACHING WITH REDIS
// ============================================================================
// Requires a real Redis instance. Start one with:
//   docker run -d --name learning-redis -p 6380:6379 redis:7-alpine
// Run: mvn -f Spring/10-caching/56-caching-with-redis spring-boot:run
// ============================================================================
@SpringBootApplication
public class RedisCachingApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedisCachingApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final PriceLookupService priceLookupService;
        private final CacheManager cacheManager;

        Demo(PriceLookupService priceLookupService, CacheManager cacheManager) {
            this.priceLookupService = priceLookupService;
            this.cacheManager = cacheManager;
        }

        @Override
        public void run(String... args) throws Exception {
            runBasicHitMissScenario();
            System.out.println();
            runCrossProcessPersistenceScenario();
            System.out.println();
            runTtlExpiryScenario();
        }

        private void runBasicHitMissScenario() {
            System.out.println("=".repeat(74));
            System.out.println("@Cacheable backed by Redis - same behavior as lesson 55, different store");
            System.out.println("=".repeat(74));

            String key = "basic-" + System.currentTimeMillis();
            long t1 = timeIt(() -> priceLookupService.getPrice(key));
            System.out.println("  1st call: " + t1 + "ms, lookupCount=" + priceLookupService.getLookupCount());

            long t2 = timeIt(() -> priceLookupService.getPrice(key));
            System.out.println("  2nd call: " + t2 + "ms, lookupCount=" + priceLookupService.getLookupCount()
                    + "  (unchanged - served from Redis, not this JVM's heap)");
        }

        private void runCrossProcessPersistenceScenario() {
            System.out.println("=".repeat(74));
            System.out.println("Redis survives this JVM restarting - re-run this app and watch");
            System.out.println("=".repeat(74));

            Cache prices = cacheManager.getCache("prices");
            Cache.ValueWrapper existing = prices.get("widget");
            System.out.println("  before calling getPrice(\"widget\"): already cached in Redis from a PREVIOUS run? "
                    + (existing != null));
            if (existing != null) {
                System.out.println("    existing value found in Redis: " + existing.get());
            }

            int before = priceLookupService.getLookupCount();
            BigDecimal price = priceLookupService.getPrice("widget");
            int after = priceLookupService.getLookupCount();
            System.out.println("  getPrice(\"widget\") = " + price + ", lookupCount went from " + before + " to " + after
                    + (before == after
                        ? "  (HIT - this brand-new JVM never computed this value, Redis already had it)"
                        : "  (MISS - first time this key has ever been requested)"));
        }

        private void runTtlExpiryScenario() throws InterruptedException {
            System.out.println("=".repeat(74));
            System.out.println("the \"shortlived\" cache has a 3-second TTL - entries expire on their own");
            System.out.println("=".repeat(74));

            String key = "ttl-" + System.currentTimeMillis();
            int before = priceLookupService.getLookupCount();
            priceLookupService.getShortLivedPrice(key);
            priceLookupService.getShortLivedPrice(key);
            System.out.println("  2 calls immediately: lookupCount went from " + before + " to "
                    + priceLookupService.getLookupCount() + "  (2nd was a cache hit)");

            System.out.println("  sleeping 3500ms for the TTL to expire...");
            Thread.sleep(3500);

            int beforeExpiry = priceLookupService.getLookupCount();
            priceLookupService.getShortLivedPrice(key);
            System.out.println("  call after TTL expiry: lookupCount went from " + beforeExpiry + " to "
                    + priceLookupService.getLookupCount() + "  (a genuine miss - Redis expired the key itself)");
        }

        private long timeIt(Runnable work) {
            long start = System.currentTimeMillis();
            work.run();
            return System.currentTimeMillis() - start;
        }
    }
}
