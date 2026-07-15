package com.aria.integration.m0;

import com.aria.redis.RedisConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
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
 * <p>Excludes the production {@link com.aria.m0.page.dao.M0PageJpaConfig}
 * because it expects a qualified {@code psqlDataSource} from the legacy
 * PsqlConfig wiring. We synthesize the equivalent {@code m0PageTransactionManager}
 * bean here and let Spring's auto-config provide the single primary
 * datasource + EMF over the testcontainer MariaDB.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        FlywayAutoConfiguration.class,
        FreeMarkerAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
@Import({RedisConfig.class})
@EnableJpaRepositories(
        basePackages = {
                "com.aria.m0.page.dao",
                "com.aria.m0.workflow.dao",
                "com.aria.m0.savedview.dao",
                "com.aria.m0.entity_registry.dao",
                "com.aria.m0.action.dao"
        },
        transactionManagerRef = "m0PageTransactionManager",
        entityManagerFactoryRef = "entityManagerFactory")
@ComponentScan(
        basePackages = {
                "com.aria.m0.page.service",
                "com.aria.m0.workflow",
                "com.aria.m0.workflow.engine",
                "com.aria.m0.workflow.dao",
                "com.aria.m0.page.dao",
                // Round 1G additions
                "com.aria.m0.action",
                "com.aria.m0.datasource",
                "com.aria.m0.entity_registry",
                "com.aria.m0.savedview"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = com.aria.m0.page.dao.M0PageJpaConfig.class))
@EntityScan(basePackages = {
        "com.aria.m0.page.model",
        "com.aria.m0.workflow.model",
        "com.aria.m0.savedview.model",
        "com.aria.m0.entity_registry.model",
        "com.aria.m0.action.model"
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
}
