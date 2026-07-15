package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import com.aria.redis.RateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real Redis Lua token-bucket behaviour.
 *
 * Hammer the limiter past its rated capacity and assert the deny kicks in,
 * then wait long enough for the refill window to add tokens back and assert
 * we recover. This is the property that protects {@code /api/hsnList} from
 * a runaway client; the demo cap is 5 r/s.
 */
class RateLimitIT extends BaseRedisIntegrationTest {

    @Autowired
    private RateLimiter limiter;

    @Test
    @DisplayName("Token bucket: 5/s + burst 5 -> denies before 7 attempts in the same instant")
    void deniesAfterBurst() {
        String key = "ratelimit:test:" + System.nanoTime();
        int denied = 0;
        int allowed = 0;
        for (int i = 0; i < 7; i++) {
            if (limiter.allow(key, 5, 5)) allowed++;
            else denied++;
        }
        // First 5 calls drain the bucket; refill in the same wall-clock millisecond is negligible.
        assertThat(allowed).isLessThanOrEqualTo(6);
        assertThat(denied).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Bucket refills over time: a wait restores at least one token")
    void refillRecovers() throws InterruptedException {
        String key = "ratelimit:refill:" + System.nanoTime();
        // Drain
        for (int i = 0; i < 5; i++) limiter.allow(key, 5, 5);
        // The bucket is now ~empty. Wait long enough for ~3 tokens (3 * 200 ms at 5 r/s).
        Thread.sleep(700);
        assertThat(limiter.allow(key, 5, 5)).isTrue();
    }
}
