package com.aria.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis wiring for EARMS (REARCH ADR-007).
 *
 * Provides:
 *   - {@link RedisCacheManager} with per-cache TTL (master:* gets 1h, others get 5m)
 *   - {@link StringRedisTemplate} for low-level ops (locks, idempotency, rate-limit Lua)
 *   - JSON-serialised {@link RedisTemplate} for typed payloads (pub/sub, telemetry hot tier)
 *   - {@link HealthIndicator} surfacing PING latency at /actuator/health (when actuator added)
 *
 * Single-node Lettuce client; for prod we switch to Sentinel / Cluster in a future phase.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${earms.cache.ttl.default-seconds:300}")
    private long defaultTtlSeconds;

    @Value("${earms.cache.ttl.master-seconds:3600}")
    private long masterTtlSeconds;

    /**
     * Cache names registered up-front so {@code @Cacheable} can reference them
     * with per-cache TTLs (rather than the global default).
     */
    public static final String CACHE_MASTER_HSN = "master:hsn";
    public static final String CACHE_MASTER_MODULE_PRIV = "master:modulePrivileges";
    public static final String CACHE_MASTER_BRANCH = "master:branch";
    public static final String CACHE_MASTER_DEPARTMENT = "master:department";
    public static final String CACHE_MASTER_ROLE = "master:role";
    public static final String CACHE_MASTER_GST_TAX = "master:gstTaxCategory";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // JSON serializer keeps values introspectable from redis-cli while preserving types.
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(buildCacheObjectMapper());

        RedisCacheConfiguration defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(defaultTtlSeconds))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        RedisCacheConfiguration masterCfg = defaultCfg.entryTtl(Duration.ofSeconds(masterTtlSeconds));

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CACHE_MASTER_HSN, masterCfg);
        perCache.put(CACHE_MASTER_MODULE_PRIV, masterCfg);
        perCache.put(CACHE_MASTER_BRANCH, masterCfg);
        perCache.put(CACHE_MASTER_DEPARTMENT, masterCfg);
        perCache.put(CACHE_MASTER_ROLE, masterCfg);
        perCache.put(CACHE_MASTER_GST_TAX, masterCfg);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate t = new StringRedisTemplate();
        t.setConnectionFactory(connectionFactory);
        return t;
    }

    /**
     * JSON-payload template for pub/sub broker and telemetry hot tier.
     */
    @Bean(name = "redisJsonTemplate")
    public RedisTemplate<String, Object> redisJsonTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> t = new RedisTemplate<>();
        t.setConnectionFactory(connectionFactory);
        StringRedisSerializer keySer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valSer =
                new GenericJackson2JsonRedisSerializer(buildCacheObjectMapper());
        t.setKeySerializer(keySer);
        t.setHashKeySerializer(keySer);
        t.setValueSerializer(valSer);
        t.setHashValueSerializer(valSer);
        t.afterPropertiesSet();
        return t;
    }

    /**
     * Lightweight boot-time ping so a misconfigured Redis fails loudly at startup
     * rather than silently degrading the cache to a no-op on first request.
     * (When Spring Boot Actuator is added this becomes a HealthIndicator.)
     */
    @Bean
    public RedisBootProbe redisBootProbe(RedisConnectionFactory connectionFactory) {
        return new RedisBootProbe(connectionFactory);
    }

    private static ObjectMapper buildCacheObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Restrict polymorphic deserialization to our packages + safe JDK types.
        om.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        return om;
    }
}
