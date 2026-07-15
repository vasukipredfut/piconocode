package com.aria._base;

import com.aria.redis.DistributedLock;
import com.aria.redis.IdempotencyService;
import com.aria.redis.RateLimiter;
import com.aria.redis.RedisConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Slim Spring context for Redis-only integration tests.
 *
 * The full EARMS application context is heavy (468 JPA entities + SQL Server +
 * Hikari pools + Freemarker etc.) and slow to boot. For the Redis IT suite we
 * only need the Redis primitives — {@link IdempotencyService},
 * {@link DistributedLock}, {@link RateLimiter} — and their {@link RedisConfig}
 * wiring. The {@link RedisOnlyTestApplication} below is the only Spring entry
 * point this base class loads.
 *
 * For tests that genuinely need the full app (Auth, PG-ism regression), use
 * {@code @SpringBootTest(classes = AriaApplication.class)} directly — those
 * tests are marked @Disabled today with REOPEN-WITH comments naming the next
 * task that will wire the heavy boot path.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = BaseRedisIntegrationTest.RedisOnlyTestApplication.class)
@ActiveProfiles("test")
public abstract class BaseRedisIntegrationTest {

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry r) {
        r.add("spring.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.redis.port", EarmsContainersExtension::redisPort);
    }

    /**
     * Minimal Spring Boot application: just the Redis primitives. Excludes the
     * main {@code AriaApplication} entry point so JPA / SQL Server / Freemarker
     * autoconfig stays out of the test class path.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            FlywayAutoConfiguration.class,
            FreeMarkerAutoConfiguration.class,
            SqlInitializationAutoConfiguration.class,
            SecurityAutoConfiguration.class
    })
    @Import({RedisAutoConfiguration.class, RedisConfig.class})
    @ComponentScan(basePackages = "com.aria.redis")
    public static class RedisOnlyTestApplication {
    }
}
