package com.danish.spring.caching;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// ============================================================================
// 55 - THE SPRING CACHE ABSTRACTION
// ============================================================================
// Run: mvn -f Spring/10-caching/55-the-spring-cache-abstraction spring-boot:run
// ============================================================================
@SpringBootApplication
public class CachingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CachingApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final PriceLookupService priceLookupService;
        private final SelfInvokingPriceService selfInvokingPriceService;

        Demo(PriceLookupService priceLookupService, SelfInvokingPriceService selfInvokingPriceService) {
            this.priceLookupService = priceLookupService;
            this.selfInvokingPriceService = selfInvokingPriceService;
        }

        @Override
        public void run(String... args) throws Exception {
            runBasicHitMissScenario();
            System.out.println();
            runUnlessScenario();
            System.out.println();
            runCachePutAndEvictScenario();
            System.out.println();
            runSelfInvocationScenario();
        }

        private void runBasicHitMissScenario() {
            System.out.println("=".repeat(74));
            System.out.println("@Cacheable - first call is a real (slow) lookup, second is a cache hit");
            System.out.println("=".repeat(74));

            long t1 = timeIt(() -> priceLookupService.getPrice("widget"));
            System.out.println("  1st call, widget: " + t1 + "ms, lookupCount=" + priceLookupService.getLookupCount());

            long t2 = timeIt(() -> priceLookupService.getPrice("widget"));
            System.out.println("  2nd call, widget: " + t2 + "ms, lookupCount=" + priceLookupService.getLookupCount()
                    + "  (unchanged - served from cache)");

            long t3 = timeIt(() -> priceLookupService.getPrice("gadget"));
            System.out.println("  1st call, gadget (different key): " + t3 + "ms, lookupCount=" + priceLookupService.getLookupCount()
                    + "  (a genuine miss - different cache key)");
        }

        private void runUnlessScenario() {
            System.out.println("=".repeat(74));
            System.out.println("unless - the result is computed either way, but only cached conditionally");
            System.out.println("=".repeat(74));

            int before = priceLookupService.getLookupCount();
            BigDecimal cheap1 = priceLookupService.getPriceUnlessExpensive("widget");
            BigDecimal cheap2 = priceLookupService.getPriceUnlessExpensive("widget");
            System.out.println("  widget (" + cheap1 + ", <= 100): 2 calls, lookupCount went from " + before
                    + " to " + priceLookupService.getLookupCount() + "  (2nd call was a cache hit)");

            int beforeExpensive = priceLookupService.getLookupCount();
            BigDecimal expensive1 = priceLookupService.getPriceUnlessExpensive("gadget");
            BigDecimal expensive2 = priceLookupService.getPriceUnlessExpensive("gadget");
            System.out.println("  gadget (" + expensive1 + ", > 100): 2 calls, lookupCount went from " + beforeExpensive
                    + " to " + priceLookupService.getLookupCount() + "  (both ran for real - unless blocked caching)");
        }

        private void runCachePutAndEvictScenario() {
            System.out.println("=".repeat(74));
            System.out.println("@CachePut always runs, but updates the cache; @CacheEvict clears it");
            System.out.println("=".repeat(74));

            System.out.println("  cached price for widget before update: " + priceLookupService.getPrice("widget"));

            int beforePut = priceLookupService.getLookupCount();
            BigDecimal updated = priceLookupService.updatePrice("widget", BigDecimal.valueOf(999));
            System.out.println("  updatePrice(widget, 999) returned " + updated + ", lookupCount went from " + beforePut
                    + " to " + priceLookupService.getLookupCount() + "  (ran for real, as @CachePut always does)");

            int afterPut = priceLookupService.getLookupCount();
            BigDecimal afterUpdate = priceLookupService.getPrice("widget");
            System.out.println("  getPrice(widget) now returns " + afterUpdate + ", lookupCount unchanged at "
                    + priceLookupService.getLookupCount() + " (" + (afterPut == priceLookupService.getLookupCount() ? "cache hit, sees the update" : "unexpected miss") + ")");

            priceLookupService.evictPrice("widget");
            int beforeAfterEvict = priceLookupService.getLookupCount();
            BigDecimal afterEvict = priceLookupService.getPrice("widget");
            System.out.println("  after evictPrice(widget), getPrice(widget) returns " + afterEvict
                    + " (back to the real source value), lookupCount went from " + beforeAfterEvict
                    + " to " + priceLookupService.getLookupCount() + "  (a genuine miss again)");
        }

        private void runSelfInvocationScenario() {
            System.out.println("=".repeat(74));
            System.out.println("self-invocation bypasses the @Cacheable proxy entirely");
            System.out.println("=".repeat(74));

            selfInvokingPriceService.cachedLookup("through-proxy");
            selfInvokingPriceService.cachedLookup("through-proxy");
            System.out.println("  2 calls THROUGH the proxy (cachedLookup directly): lookupCount="
                    + selfInvokingPriceService.getLookupCount() + "  (2nd was a cache hit)");

            int before = selfInvokingPriceService.getLookupCount();
            selfInvokingPriceService.callCachedLookupViaThis("via-this");
            selfInvokingPriceService.callCachedLookupViaThis("via-this");
            System.out.println("  2 calls via callCachedLookupViaThis (self-invocation): lookupCount went from "
                    + before + " to " + selfInvokingPriceService.getLookupCount()
                    + "  (BOTH ran for real - @Cacheable was silently ignored)");
        }

        private long timeIt(Runnable work) {
            long start = System.currentTimeMillis();
            work.run();
            return System.currentTimeMillis() - start;
        }
    }
}
