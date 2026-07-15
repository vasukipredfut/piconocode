package com.aria._base;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Single JVM-wide MariaDB + Redis container pair shared across the IT suite.
 *
 * Implementation notes:
 *   - JUnit5 extensions are not @Autowired into Spring contexts. Use it together
 *     with {@link org.springframework.test.context.DynamicPropertySource} on the
 *     test class (see {@link BaseIntegrationTest}). The extension exposes static
 *     accessors that the @DynamicPropertySource method calls.
 *   - Containers are static so they are reused across the suite — start-up cost
 *     is paid once per JVM. JUnit5 lifecycle: BeforeAllCallback fires per test
 *     class but the static {@code start()} on a {@link GenericContainer} is a
 *     no-op once running.
 *   - Tagged image versions are pinned for reproducibility (see TESTING.md).
 *
 * Schema management: {@link TestDataSeed} loads the V1__baseline.sql against
 * the MariaDB container the first time it is requested. Per-class isolation is
 * achieved by truncating only the tables the test touches.
 */
public class EarmsContainersExtension implements BeforeAllCallback {

    /** Shared MariaDB. Schema is {@code earms_test}, matching the local dev schema name shape. */
    public static final MariaDBContainer<?> MARIADB =
            new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4"))
                    .withDatabaseName("earms_test")
                    .withUsername("earms_test")
                    .withPassword("earms_test")
                    .withReuse(false);

    /** Shared Redis. {@code redis:7-alpine} matches the dev compose pin. */
    @SuppressWarnings("resource")
    public static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    private static volatile boolean started = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        synchronized (EarmsContainersExtension.class) {
            if (!started) {
                MARIADB.start();
                REDIS.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try { REDIS.stop(); } catch (Exception ignored) {}
                    try { MARIADB.stop(); } catch (Exception ignored) {}
                }));
                started = true;
            }
        }
    }

    public static String mariadbJdbcUrl() {
        return MARIADB.getJdbcUrl();
    }

    public static String mariadbUsername() {
        return MARIADB.getUsername();
    }

    public static String mariadbPassword() {
        return MARIADB.getPassword();
    }

    public static String redisHost() {
        return REDIS.getHost();
    }

    public static Integer redisPort() {
        return REDIS.getMappedPort(6379);
    }
}
