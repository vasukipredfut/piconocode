package com.aria.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Single-node Redlock-style mutex (REARCH ADR-007).
 *
 * Acquire with {@code SET key token NX PX ttl}. Release with a Lua CAS
 * that only deletes the key if the value matches the token we got back —
 * prevents accidentally releasing somebody else's lock after a TTL expiry.
 *
 * Use sites:
 *   - IRN generation (M1 US-M1-04) — single in-flight request per invoice
 *   - oee_daily recompute per (machine,date) (M6) — single writer
 *   - master-data CRUD writes — prevent racing create/update against the same id
 *
 * NOTE: this is a single-node lock. For prod we move to multi-node Redlock or
 * (better) push the contract to Redis Cluster + Sentinel. See REDIS.md.
 */
@Component
public class DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(DistributedLock.class);

    /** Lua: delete only if value equals our token. Atomic CAS. */
    private static final String UNLOCK_LUA =
            "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('DEL', KEYS[1]) " +
            "else " +
            "  return 0 " +
            "end";

    private final DefaultRedisScript<Long> unlockScript;

    @Autowired
    private StringRedisTemplate redis;

    public DistributedLock() {
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(UNLOCK_LUA);
        this.unlockScript.setResultType(Long.class);
    }

    /**
     * Try to acquire the lock once (non-blocking).
     *
     * @return the unique token if acquired, {@code Optional.empty()} if somebody else holds it.
     *         The token MUST be passed back to {@link #unlock(String, String)} so we don't
     *         release a lock owned by another holder after a TTL roll-over.
     */
    public Optional<String> tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttl);
        if (Boolean.TRUE.equals(ok)) {
            log.debug("LOCK_ACQUIRED key={} token={} ttl={}ms", key, token, ttl.toMillis());
            return Optional.of(token);
        }
        return Optional.empty();
    }

    /**
     * Release the lock if and only if we still own it (token matches).
     * @return true if our token was the holder and the lock was released.
     */
    public boolean unlock(String key, String token) {
        Long deleted = redis.execute(unlockScript, Collections.singletonList(key), token);
        boolean released = deleted != null && deleted == 1L;
        if (released) {
            log.debug("LOCK_RELEASED key={} token={}", key, token);
        } else {
            log.warn("LOCK_RELEASE_NOOP key={} token={} (already expired or stolen)", key, token);
        }
        return released;
    }
}
