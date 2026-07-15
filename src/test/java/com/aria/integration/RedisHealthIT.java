package com.aria.integration;

import com.aria._base.BaseRedisIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boot-time integration test: confirm Spring's RedisConnectionFactory talks to
 * the Testcontainers Redis. This is the smoke test that proves the IT harness
 * (Testcontainers + EarmsContainersExtension + @DynamicPropertySource) actually
 * wires the live container into the Spring context.
 */
class RedisHealthIT extends BaseRedisIntegrationTest {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    @DisplayName("PING the Testcontainers Redis through the wired ConnectionFactory")
    void pingViaFactory() {
        try (RedisConnection conn = connectionFactory.getConnection()) {
            assertThat(conn.ping()).isEqualToIgnoringCase("PONG");
        }
    }

    @Test
    @DisplayName("StringRedisTemplate can SET/GET against the container")
    void setAndGet() {
        redis.opsForValue().set("health:probe", "ok");
        assertThat(redis.opsForValue().get("health:probe")).isEqualTo("ok");
        redis.delete("health:probe");
    }
}
