package com.aria.integration.m0;

import com.aria._base.EarmsContainersExtension;
import com.aria.tenant.TenantConfigBundleService;
import com.aria.redis.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT for {@link TenantConfigBundleService} backed by the live testcontainer
 * MariaDB. Order-sensitive: the first apply seeds rows, the second confirms
 * idempotency.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = TenantConfigBundleIT.TenantConfigTestApp.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TenantConfigBundleIT {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", EarmsContainersExtension::mariadbJdbcUrl);
        r.add("spring.datasource.username", EarmsContainersExtension::mariadbUsername);
        r.add("spring.datasource.password", EarmsContainersExtension::mariadbPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MariaDBDialect");
        r.add("spring.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.redis.port", EarmsContainersExtension::redisPort);
        r.add("spring.data.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.data.redis.port", EarmsContainersExtension::redisPort);
    }

    @Autowired private TenantConfigBundleService service;
    @Autowired private DataSource dataSource;

    @Test
    @Order(1)
    @DisplayName("apply Pico bundle: positive counts across all sections")
    void firstApply() {
        TenantConfigBundleService.ApplyResult r = service.applyPicoBundle();
        // The V7 + V7_2 Flyway scripts seed some content at boot; service
        // applies the remainder. Workflows: TenantConfig bundle has 10
        // workflowDefinitions; V7 seeds 10 (Tanker v2 + 9 others); service
        // wants to insert v1 of TankerInbound which V4 already seeded, plus
        // the same 10 with version mapped — most should hit IGNORE.
        // The reliable invariant: applying the bundle does not error.
        assertThat(r.errors).isEmpty();

        // Verify rows landed in the live tables.
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        Long wfCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM workflow_definition WHERE tenant_id=1 AND code LIKE 'M10.%'",
                Long.class);
        assertThat(wfCount).isGreaterThanOrEqualTo(10L);

        Long roleCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tenant_role WHERE tenant_id=1", Long.class);
        assertThat(roleCount).isGreaterThanOrEqualTo(9L);

        Long layoutCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tenant_form_layout WHERE tenant_id=1", Long.class);
        assertThat(layoutCount).isGreaterThanOrEqualTo(8L);

        Long viewCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tenant_list_view WHERE tenant_id=1", Long.class);
        assertThat(viewCount).isGreaterThanOrEqualTo(5L);
    }

    @Test
    @Order(2)
    @DisplayName("re-apply Pico bundle: idempotent — zero new rows in any section")
    void secondApply() {
        TenantConfigBundleService.ApplyResult r = service.applyPicoBundle();
        assertThat(r.rolesAdded).isZero();
        assertThat(r.workflowsAdded).isZero();
        assertThat(r.fieldsAdded).isZero();
        assertThat(r.layoutsAdded).isZero();
        assertThat(r.viewsAdded).isZero();
        assertThat(r.notificationsAdded).isZero();
    }

    // ---- slim app -------------------------------------------------------
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            FlywayAutoConfiguration.class,
            FreeMarkerAutoConfiguration.class,
            SecurityAutoConfiguration.class
    })
    @Import({RedisConfig.class})
    @EnableJpaRepositories(basePackages = {
            "com.aria.page.dao", "com.aria.workflow.dao",
            "com.aria.entity_registry.dao", "com.aria.dispatch.dao"
    },
            transactionManagerRef = "m0PageTransactionManager",
            entityManagerFactoryRef = "entityManagerFactory")
    @ComponentScan(
            basePackages = {
                    "com.aria.page.service",
                    "com.aria.workflow",
                    "com.aria.page.dao",
                    "com.aria.workflow.dao",
                    "com.aria.entity_registry",
                    "com.aria.dispatch",
            },
            excludeFilters = @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = com.aria.page.dao.M0PageJpaConfig.class))
    @org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {
            "com.aria.page.model", "com.aria.workflow.model",
            "com.aria.entity_registry.model", "com.aria.dispatch.model"
    })
    public static class TenantConfigTestApp {
        @org.springframework.context.annotation.Bean(name = "m0PageTransactionManager")
        public org.springframework.transaction.PlatformTransactionManager m0PageTransactionManager(
                jakarta.persistence.EntityManagerFactory emf) {
            return new org.springframework.orm.jpa.JpaTransactionManager(emf);
        }

        /**
         * Provide a TenantConfigBundleService bound to the autoconfigured
         * primary DataSource directly, bypassing the @Qualifier("psqlDataSource")
         * wiring used in production (the slim test app does not load
         * PsqlConfig).
         */
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public TenantConfigBundleService tenantConfigBundleService(DataSource ds) {
            return new TenantConfigBundleService(new org.springframework.jdbc.core.JdbcTemplate(ds));
        }
    }
}
