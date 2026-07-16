package com.aria.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;

/**
 * Optional startup helper that clears Redis storage on deployment boot when enabled.
 * This is useful for new-build deployments where stale cache entries must be discarded.
 */
public class RedisStartupCacheClearer {
    private static final Logger log = LoggerFactory.getLogger(RedisStartupCacheClearer.class);

    private final RedisConnectionFactory connectionFactory;
    private final boolean enabled;

    public RedisStartupCacheClearer(RedisConnectionFactory connectionFactory, boolean enabled) {
        this.connectionFactory = connectionFactory;
        this.enabled = enabled;
    }

    public void clearIfEnabled() {
        if (!enabled) {
            return;
        }

        try (RedisConnection connection = connectionFactory.getConnection()) {
            RedisServerCommands serverCommands = connection.serverCommands();
            serverCommands.flushAll();
            log.info("Redis cache storage cleared at startup");
        } catch (Exception e) {
            log.warn("Unable to clear Redis cache storage at startup", e);
        }
    }
}
