package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the {@link org.springframework.data.redis.cache.RedisCacheManager}
 * configured in {@link com.aria.redis.RedisConfig} actually round-trips a
 * value through Redis (not a no-op fallback). The HSN controller-driven
 * cache-hit scenario from REDIS.md is exercised end-to-end by the dedicated
 * HsnCache IT once the full-app boot path is wired (see disabled IdempotencyFilterIT
 * for the same caveat).
 */
class CacheBehaviorIT extends BaseRedisIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    @DisplayName("master:hsn cache is registered with the configured RedisCacheManager")
    void masterHsnCacheRegistered() {
        Cache cache = cacheManager.getCache("master:hsn");
        assertThat(cache).isNotNull();
    }

    @Test
    @DisplayName("put() and get() round-trip a value through the Redis-backed cache")
    void putGetRoundTrip() {
        Cache cache = cacheManager.getCache("master:hsn");
        assertThat(cache).isNotNull();

        String key = "cache-it-" + System.nanoTime();
        cache.put(key, "value-payload");

        Cache.ValueWrapper got = cache.get(key);
        assertThat(got).isNotNull();
        assertThat(got.get()).isEqualTo("value-payload");

        // Inspect Redis directly: the cache key namespace should be visible.
        Set<String> keys = redis.keys("master:hsn::*");
        assertThat(keys).isNotEmpty();

        cache.evict(key);
    }
}
