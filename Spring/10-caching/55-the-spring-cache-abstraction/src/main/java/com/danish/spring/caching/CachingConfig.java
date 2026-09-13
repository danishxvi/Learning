package com.danish.spring.caching;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @EnableCaching turns @Cacheable/@CachePut/@CacheEvict from inert annotations into a
// real AOP proxy (the same proxy mechanism as @Transactional and @Async) that
// intercepts the call, checks the CacheManager first, and only invokes the real method
// on a miss. ConcurrentMapCacheManager backs every cache with a plain
// ConcurrentHashMap - in-memory, single-JVM, no eviction policy or TTL. Lesson 56
// swaps this for Redis, a real distributed cache, without touching any @Cacheable code.
@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("prices");
    }
}
