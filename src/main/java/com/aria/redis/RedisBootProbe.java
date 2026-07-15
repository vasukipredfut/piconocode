package com.aria.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import jakarta.annotation.PostConstruct;

/**
 * Boot-time PING that surfaces Redis reachability in the application log.
 * Promote to a HealthIndicator once spring-boot-starter-actuator is on the classpath.
 */
public class RedisBootProbe {

    private static final Logger log = LoggerFactory.getLogger(RedisBootProbe.class);

    private final RedisConnectionFactory factory;

    public RedisBootProbe(RedisConnectionFactory factory) {
        this.factory = factory;
    }

    @PostConstruct
    public void probe() {
        long start = System.nanoTime();
        try (RedisConnection conn = factory.getConnection()) {
            String pong = conn.ping();
            long micros = (System.nanoTime() - start) / 1_000L;
            log.info("REDIS_BOOT_PROBE ping={} latencyMicros={}", pong, micros);
        } catch (Exception e) {
            log.error("REDIS_BOOT_PROBE_FAIL — cache/lock/idem features will not work", e);
        }
    }
}
