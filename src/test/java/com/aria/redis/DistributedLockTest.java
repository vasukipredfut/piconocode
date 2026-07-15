package com.aria.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DistributedLock}. Validates:
 *   - tryLock() issues SET NX PX with a fresh UUID token on each acquire.
 *   - unlock() runs the Lua CAS script so we cannot delete somebody else's lock
 *     after a TTL roll-over (the canonical Redlock pitfall).
 *
 * Contention is covered end-to-end in {@code DistributedLockContentionIT}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DistributedLockTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> values;

    @InjectMocks
    private DistributedLock lock;

    @BeforeEach
    void wire() {
        when(redis.opsForValue()).thenReturn(values);
    }

    @Test
    @DisplayName("tryLock(): returns a UUID-shaped token when SET NX succeeds")
    void tryLockAcquires() {
        when(values.setIfAbsent(eq("lock:k1"), any(String.class), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        Optional<String> token = lock.tryLock("lock:k1", Duration.ofSeconds(15));

        assertThat(token).isPresent();
        // UUIDs are 36 chars with 4 dashes — we don't need a regex, length is enough.
        assertThat(token.get()).hasSize(36);
    }

    @Test
    @DisplayName("tryLock(): empty Optional when SET NX collides with an existing holder")
    void tryLockCollides() {
        when(values.setIfAbsent(eq("lock:held"), any(String.class), any(Duration.class)))
                .thenReturn(Boolean.FALSE);

        assertThat(lock.tryLock("lock:held", Duration.ofSeconds(15))).isEmpty();
    }

    @Test
    @DisplayName("tryLock(): each call generates a distinct token (no collisions)")
    void tokensAreUnique() {
        when(values.setIfAbsent(any(String.class), any(String.class), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        ArgumentCaptor<String> tokenCap = ArgumentCaptor.forClass(String.class);
        lock.tryLock("a", Duration.ofSeconds(15));
        lock.tryLock("b", Duration.ofSeconds(15));
        org.mockito.Mockito.verify(values, org.mockito.Mockito.times(2))
                .setIfAbsent(any(String.class), tokenCap.capture(), any(Duration.class));

        List<String> tokens = tokenCap.getAllValues();
        assertThat(tokens).hasSize(2);
        assertThat(tokens.get(0)).isNotEqualTo(tokens.get(1));
    }

    @Test
    @DisplayName("unlock(): runs the CAS Lua and reports true when 1 row deleted")
    void unlockSucceedsWhenScriptReturnsOne() {
        when(redis.execute(any(RedisScript.class), anyList(), eq("mytoken")))
                .thenReturn(1L);

        boolean released = lock.unlock("lock:k1", "mytoken");

        assertThat(released).isTrue();
    }

    @Test
    @DisplayName("unlock(): false when the script returns 0 (stale / stolen)")
    void unlockNoopWhenScriptReturnsZero() {
        when(redis.execute(any(RedisScript.class), anyList(), eq("stale")))
                .thenReturn(0L);

        assertThat(lock.unlock("lock:k1", "stale")).isFalse();
    }

    @Test
    @DisplayName("unlock(): null script result tolerated (timeout) -> false, never NPE")
    void unlockNullTolerated() {
        when(redis.execute(any(RedisScript.class), anyList(), eq("none")))
                .thenReturn(null);

        assertThat(lock.unlock("lock:k1", "none")).isFalse();
    }
}
