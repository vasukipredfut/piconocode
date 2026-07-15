package com.aria.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RateLimiter#allow(String, int, int)}.
 *
 * The Lua script lives entirely in Redis (atomic refill + decision), so the
 * Java side is just "execute and translate 1/0". That's what we cover here;
 * the actual token-bucket semantics under load are covered in {@code RateLimitIT}.
 */
@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

    @Mock
    private StringRedisTemplate redis;

    @InjectMocks
    private RateLimiter limiter;

    @Test
    @DisplayName("allow(): script returns 1 -> request allowed")
    void allowedWhenScriptReturnsOne() {
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L);

        assertThat(limiter.allow("ratelimit:r1:u1", 5, 5)).isTrue();
    }

    @Test
    @DisplayName("allow(): script returns 0 -> request denied")
    void deniedWhenScriptReturnsZero() {
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(0L);

        assertThat(limiter.allow("ratelimit:r1:u1", 5, 5)).isFalse();
    }

    @Test
    @DisplayName("allow(): null result (Redis hiccup) -> denied, NOT allowed")
    void nullScriptResultIsDeny() {
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        assertThat(limiter.allow("ratelimit:r1:u1", 5, 5)).isFalse();
    }

    @Test
    @DisplayName("allow(): passes rate, burst, now-ms as ARGV in that order")
    void scriptArgsOrder() {
        org.mockito.ArgumentCaptor<String> rateCap = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> burstCap = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> nowCap = org.mockito.ArgumentCaptor.forClass(String.class);
        when(redis.execute(any(RedisScript.class), anyList(),
                rateCap.capture(), burstCap.capture(), nowCap.capture()))
                .thenReturn(1L);

        long before = System.currentTimeMillis();
        limiter.allow("ratelimit:x:y", 5, 10);
        long after = System.currentTimeMillis();

        assertThat(rateCap.getValue()).isEqualTo("5");
        assertThat(burstCap.getValue()).isEqualTo("10");
        long now = Long.parseLong(nowCap.getValue());
        assertThat(now).isBetween(before, after);
    }

    @Test
    @DisplayName("allow(): keys list is exactly [bucketKey] (single-key Lua => cluster-safe)")
    void scriptUsesSingleKey() {
        org.mockito.ArgumentCaptor<java.util.List<String>> keysCap =
                org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        when(redis.execute(any(RedisScript.class), keysCap.capture(), anyString(), anyString(), anyString()))
                .thenReturn(1L);

        limiter.allow("ratelimit:cluster:safety", 5, 5);

        assertThat(keysCap.getValue()).containsExactly("ratelimit:cluster:safety");
    }
}
