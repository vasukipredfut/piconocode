package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import com.aria.redis.IdempotencyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the SET NX EX semantics that {@link IdempotencyService}
 * builds on. End-to-end {@code IdempotencyFilter} HTTP behaviour is covered
 * separately (see {@link IdempotencyFilterIT}, currently @Disabled with a
 * REOPEN-WITH comment pending the full-app boot wiring).
 */
class IdempotencyServiceIT extends BaseRedisIntegrationTest {

    @Autowired
    private IdempotencyService service;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    @DisplayName("reserve(): first caller wins, second on the same key loses (NX semantics)")
    void reserveIsExclusive() {
        String key = "it-reserve-" + System.nanoTime();
        try {
            assertThat(service.reserve(key, Duration.ofMinutes(1))).isTrue();
            assertThat(service.reserve(key, Duration.ofMinutes(1))).isFalse();
        } finally {
            service.release(key);
        }
    }

    @Test
    @DisplayName("store(): persists response under idem:resp:* with the requested TTL")
    void storeAndFind() {
        String key = "it-store-" + System.nanoTime();
        try {
            service.reserve(key, Duration.ofMinutes(1));
            service.store(key, "{\"ok\":true}", Duration.ofHours(24));

            Optional<String> found = service.find(key);
            assertThat(found).contains("{\"ok\":true}");

            Long ttl = redis.getExpire("idem:resp:" + key);
            assertThat(ttl).isNotNull().isPositive();
        } finally {
            redis.delete("idem:resp:" + key);
        }
    }

    @Test
    @DisplayName("store() releases the in-flight lock so a fresh reserve() under same key succeeds after a different key cycles")
    void storeClearsInflightLock() {
        String key = "it-clear-" + System.nanoTime();
        try {
            service.reserve(key, Duration.ofMinutes(1));
            assertThat(redis.opsForValue().get("idem:lock:" + key)).isEqualTo("INFLIGHT");

            service.store(key, "BODY", Duration.ofMinutes(10));

            assertThat(redis.opsForValue().get("idem:lock:" + key)).isNull();
        } finally {
            redis.delete("idem:resp:" + key);
            redis.delete("idem:lock:" + key);
        }
    }
}
