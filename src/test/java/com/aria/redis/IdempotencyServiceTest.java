package com.aria.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link IdempotencyService}. Mocks the {@link StringRedisTemplate}
 * so we can assert the SET-NX-EX contract and the lock/resp key layout without a
 * live Redis. The IT counterpart ({@code IdempotencyFilterIT}) exercises the same
 * paths end-to-end against Testcontainers Redis.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotencyServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> values;

    @InjectMocks
    private IdempotencyService service;

    @BeforeEach
    void wire() {
        when(redis.opsForValue()).thenReturn(values);
    }

    @Test
    @DisplayName("reserve(): writes idem:lock:<key> with SET NX EX and returns true on first call")
    void reserveFirstCallSucceeds() {
        when(values.setIfAbsent(eq("idem:lock:abc"), eq("INFLIGHT"), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        boolean reserved = service.reserve("abc", Duration.ofMinutes(2));

        assertThat(reserved).isTrue();
        verify(values).setIfAbsent("idem:lock:abc", "INFLIGHT", Duration.ofMinutes(2));
    }

    @Test
    @DisplayName("reserve(): returns false when SET NX collides (replay detection)")
    void reserveReplayReturnsFalse() {
        when(values.setIfAbsent(eq("idem:lock:dup"), eq("INFLIGHT"), any(Duration.class)))
                .thenReturn(Boolean.FALSE);

        assertThat(service.reserve("dup", Duration.ofMinutes(2))).isFalse();
    }

    @Test
    @DisplayName("reserve(): null from Redis (lost connection edge case) -> false, never true")
    void reserveNullIsFalse() {
        when(values.setIfAbsent(eq("idem:lock:nul"), eq("INFLIGHT"), any(Duration.class)))
                .thenReturn(null);

        assertThat(service.reserve("nul", Duration.ofMinutes(2))).isFalse();
    }

    @Test
    @DisplayName("find(): looks under idem:resp:<key> and returns the body if present")
    void findReturnsCachedBody() {
        when(values.get("idem:resp:k1")).thenReturn("{\"ok\":true}");

        Optional<String> body = service.find("k1");

        assertThat(body).contains("{\"ok\":true}");
    }

    @Test
    @DisplayName("find(): empty Optional when nothing stored")
    void findEmpty() {
        when(values.get("idem:resp:missing")).thenReturn(null);
        assertThat(service.find("missing")).isEmpty();
    }

    @Test
    @DisplayName("store(): writes idem:resp:<key> with TTL and clears the lock marker")
    void storeWritesRespAndClearsLock() {
        service.store("k2", "BODY", Duration.ofHours(24));

        verify(values).set("idem:resp:k2", "BODY", Duration.ofHours(24));
        verify(redis).delete("idem:lock:k2");
    }

    @Test
    @DisplayName("release(): clears the lock marker without touching the response body")
    void releaseClearsLockOnly() {
        service.release("k3");

        verify(redis).delete("idem:lock:k3");
        verify(values, never()).set(any(), any(), any(Duration.class));
    }

    @Test
    @DisplayName("default TTLs are advertised: 24h response, 2m inflight")
    void defaultTtls() {
        assertThat(IdempotencyService.DEFAULT_RESPONSE_TTL).isEqualTo(Duration.ofHours(24));
        assertThat(IdempotencyService.DEFAULT_INFLIGHT_TTL).isEqualTo(Duration.ofMinutes(2));
    }

    @Test
    @DisplayName("reserve() is invoked exactly once per call (no retry loop hidden in the service)")
    void noHiddenRetry() {
        when(values.setIfAbsent(eq("idem:lock:once"), any(), any(Duration.class)))
                .thenReturn(Boolean.TRUE);

        service.reserve("once", Duration.ofMinutes(2));

        verify(values, times(1)).setIfAbsent(eq("idem:lock:once"), any(), any(Duration.class));
    }
}
