package com.aria.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Idempotency-Key store (REARCH ADR-007, satisfies M1 US-M1-04 IRN dedupe).
 *
 * Contract:
 *   - Client sends header {@code Idempotency-Key: &lt;opaque&gt;} on a POST.
 *   - On first request: {@link #reserve(String, Duration)} returns true (SET NX EX),
 *     handler runs, then {@link #store(String, String, Duration)} persists the response.
 *   - On retry within TTL: {@link #find(String)} returns the cached response — we
 *     short-circuit the handler so the side-effecting work runs once only.
 *
 * Storage layout:
 *   - "idem:lock:{key}" — short-lived NX marker held while the handler is running
 *   - "idem:resp:{key}" — JSON-encoded response, TTL 24h by default
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    public static final Duration DEFAULT_RESPONSE_TTL = Duration.ofHours(24);
    /** While the request is in flight nobody else with the same key should proceed. */
    public static final Duration DEFAULT_INFLIGHT_TTL = Duration.ofMinutes(2);

    @Autowired
    private StringRedisTemplate redis;

    /**
     * Reserve the key (NX). Returns true if this caller is the first one in the window.
     * Hold this reservation while the handler runs; release by writing the response.
     */
    public boolean reserve(String key, Duration ttl) {
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey(key), "INFLIGHT", ttl);
        return Boolean.TRUE.equals(ok);
    }

    /**
     * Look up a cached response from a previous successful execution.
     */
    public Optional<String> find(String key) {
        String body = redis.opsForValue().get(respKey(key));
        return Optional.ofNullable(body);
    }

    /**
     * Persist the response body for {@code ttl} and clear the in-flight marker.
     */
    public void store(String key, String responseBody, Duration ttl) {
        redis.opsForValue().set(respKey(key), responseBody, ttl);
        redis.delete(lockKey(key));
        log.debug("IDEM_STORED key={} bytes={} ttl={}h", key, responseBody.length(), ttl.toHours());
    }

    /**
     * Release the in-flight marker without storing a response (e.g. on handler exception
     * we want the next retry to actually re-run, not see a stale "in-flight" forever).
     */
    public void release(String key) {
        redis.delete(lockKey(key));
    }

    private static String lockKey(String key) { return "idem:lock:" + key; }
    private static String respKey(String key) { return "idem:resp:" + key; }
}
