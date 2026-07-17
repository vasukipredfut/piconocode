package com.aria.integration.m0;

import com.aria.redis.RedisConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

/**
 * Slim Spring Boot application for the M0 Schema V2 integration tests
 * (Round 1G). Mirrors the {@code PageSubstrateTestApp} from
 * {@code PageDefinitionIT} but additionally scans the action / datasource /
 * savedview / entity_registry packages so the new controllers + services
 * are wired.
 *
 * <p>Excludes the production {@link com.aria.page.dao.M0PageJpaConfig}
 * because it expects a qualified {@code psqlDataSource} from the legacy
 * PsqlConfig wiring. We synthesize the equivalent {@code m0PageTransactionManager}
 * bean here and let Spring's auto-config provide the single primary
 * datasource + EMF over the testcontainer MariaDB.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        FreeMarkerAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@Import({RedisConfig.class})
@EnableJpaRepositories(
        basePackages = {
                "com.aria.page.dao",
                "com.aria.workflow.dao",
                "com.aria.savedview.dao",
                "com.aria.entity_registry.dao",
                "com.aria.action.dao"
        },
        transactionManagerRef = "m0PageTransactionManager",
        entityManagerFactoryRef = "entityManagerFactory")
@ComponentScan(
        basePackages = {
                "com.aria.page.service",
                "com.aria.workflow",
                "com.aria.workflow.engine",
                "com.aria.workflow.dao",
                "com.aria.page.dao",
                // Round 1G additions
                "com.aria.action",
                "com.aria.datasource",
                "com.aria.entity_registry",
                "com.aria.savedview"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.aria.page.dao.M0PageJpaConfig.class))
@EntityScan(basePackages = {
        "com.aria.page.model",
        "com.aria.workflow.model",
        "com.aria.savedview.model",
        "com.aria.entity_registry.model",
        "com.aria.action.model"
})
public class M0SubstrateTestApp {

    /** Test-only alias for the autoconfigured PlatformTransactionManager. */
    @Bean(name = "m0PageTransactionManager")
    public PlatformTransactionManager m0PageTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    /** Register the autoconfigured DataSource under the additional qualifier
     *  {@code psqlDataSource} so {@code @Qualifier("psqlDataSource")}
     *  injections used by the Round 1G services resolve in the slim test
     *  context (production wires this bean via {@code PsqlConfig}). */
    @Bean
    public static org.springframework.beans.factory.config.BeanFactoryPostProcessor psqlAliasRegistrar() {
        return bf -> {
            String[] candidates = bf.getBeanNamesForType(DataSource.class);
            if (candidates.length > 0 && !bf.containsBean("psqlDataSource")) {
                ((org.springframework.beans.factory.support.DefaultListableBeanFactory) bf)
                        .registerAlias(candidates[0], "psqlDataSource");
            }
        };
    }

    /** Generic JdbcTemplate for tests that need to seed / probe directly. */
    @Bean
    public JdbcTemplate testJdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    /**
     * The tenant-scope authority. Lives in {@code com.aria.security}, which
     * this slim context does not component-scan, so declare it explicitly
     * rather than widen the scan and drag in the whole auth chain.
     *
     * <p>It reads its membership from the container's own information_schema,
     * so the ITs scope against the real Flyway-built schema — not a fixture.
     */
    @Bean
    public com.aria.security.TenantScope tenantScope(DataSource ds) {
        return new com.aria.security.TenantScope(ds);
    }
}
