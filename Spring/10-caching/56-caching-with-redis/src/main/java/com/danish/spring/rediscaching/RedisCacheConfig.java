package com.danish.spring.rediscaching;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

// Same @EnableCaching, same @Cacheable annotations as lesson 55 - only the CacheManager
// bean changes. That's the entire point of the abstraction: application code that
// depends on Cache/CacheManager interfaces doesn't know or care whether entries live in
// a ConcurrentHashMap in this JVM's heap or in a separate Redis process that survives
// this JVM restarting.
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        // defaultCacheConfig() has NO time-to-live by default - entries in "prices" live
        // in Redis until evicted or the app restarts THIS Redis process, unlike
        // ConcurrentMapCacheManager (lesson 55), which loses everything the moment the
        // JVM exits.
        RedisCacheConfiguration shortLived = defaults.entryTtl(Duration.ofSeconds(3));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withCacheConfiguration("shortlived", shortLived)
                .build();
    }
}
