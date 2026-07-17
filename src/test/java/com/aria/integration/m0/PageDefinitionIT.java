package com.aria.integration.m0;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria._base.EarmsContainersExtension;
import com.aria.page.dao.PageDefinitionRepository;
import com.aria.page.dao.PageVersionRepository;
import com.aria.page.dto.PageCreateRequest;
import com.aria.page.dto.PageUpdateRequest;
import com.aria.page.model.PageDefinition;
import com.aria.page.model.PageVersion;
import com.aria.page.service.PageDefinitionService;
import com.aria.page.service.PageRendererService;
import com.aria.redis.RedisConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test for the M0 page-definition substrate.
 *
 * <p>Spring context = minimal: MariaDB datasource against the testcontainer,
 * JPA over the m0 entities, Redis via {@link RedisConfig}, and the m0
 * services / controllers / dao package. We deliberately exclude the full
 * EARMS application boot path (Freemarker, the 468 legacy entities, the
 * SQL-Server pool) — full-boot IT for the page substrate will land with
 * the {@code TASK_M0_TENANT_AUTH} task.
 *
 * <p>Schema comes from Flyway: the container starts empty, so V1 (schema)
 * and V2 (the Aria product catalogue at tenant_id = -1) both run from
 * scratch. That is deliberately the SAME path a new customer install
 * takes, so an IT failing here means a customer install would fail too.
 * It replaces M0PageSchemaInitialiser, which used to build this schema by
 * probing for data and re-running migrations it guessed were missing.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = PageDefinitionIT.PageSubstrateTestApp.class)
@ActiveProfiles("test")
class PageDefinitionIT {

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

    @Autowired private PageDefinitionService svc;
    @Autowired private PageRendererService renderer;
    @Autowired private PageDefinitionRepository defRepo;
    @Autowired private PageVersionRepository versionRepo;
    @Autowired private StringRedisTemplate redis;

    @PersistenceContext private EntityManager em;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    @DisplayName("create + load draft round-trips body json")
    void createAndLoadDraft() {
        PageCreateRequest req = new PageCreateRequest();
        req.setCode("it:create-load");
        req.setTitle("Create Load");
        req.setBodyJson("{\"layout\":{\"type\":\"Grid\"}}");
        PageDefinition def = svc.create(1L, req);
        assertThat(def.getId()).isNotNull();

        Optional<PageVersion> draft = svc.loadDraft(1L, "it:create-load");
        assertThat(draft).isPresent();
        assertThat(draft.get().getBodyJson()).contains("Grid");
    }

    @Test
    @DisplayName("publish flips status and serves via renderer; cache populated")
    void publishAndServe() throws Exception {
        PageCreateRequest req = new PageCreateRequest();
        req.setCode("it:publish-serve");
        req.setTitle("Publish Serve");
        req.setBodyJson("{\"v\":\"draft1\"}");
        svc.create(1L, req);

        PageVersion v = svc.publish(1L, "it:publish-serve", null);
        assertThat(v.getStatus()).isEqualTo("PUBLISHED");

        var served = renderer.loadPublishedFor(1L, "it:publish-serve");
        assertThat(served).isPresent();
        JsonNode body = json.readTree(served.get().getBodyJson());
        assertThat(body.get("v").asText()).isEqualTo("draft1");

        // Cache key should now be set in Redis.
        String key = PageRendererService.cacheKey(1L, "it:publish-serve");
        assertThat(redis.opsForValue().get(key)).isNotNull();
    }

    @Test
    @DisplayName("re-publishing a modified draft evicts Redis cache")
    void republishEvictsCache() {
        PageCreateRequest req = new PageCreateRequest();
        req.setCode("it:evict");
        req.setTitle("Evict");
        req.setBodyJson("{\"v\":1}");
        svc.create(1L, req);
        svc.publish(1L, "it:evict", null);

        // Prime cache.
        renderer.loadPublishedFor(1L, "it:evict");
        String key = PageRendererService.cacheKey(1L, "it:evict");
        assertThat(redis.opsForValue().get(key)).isNotNull();

        // Create new draft + publish.
        PageUpdateRequest upd = new PageUpdateRequest();
        upd.setBodyJson("{\"v\":2}");
        svc.updateDraft(1L, "it:evict", upd);
        svc.publish(1L, "it:evict", null);

        // Immediately after publish, the entry should be gone.
        assertThat(redis.opsForValue().get(key)).isNull();

        // Re-read populates with the new body.
        var fresh = renderer.loadPublishedFor(1L, "it:evict");
        assertThat(fresh).isPresent();
        assertThat(fresh.get().getBodyJson()).contains("\"v\":2");
    }

    @Test
    @DisplayName("element-catalog seeded with 14 first-party primitives")
    void catalogSeed() {
        // The schema initialiser populates page_element_catalog on first boot.
        Long count = em.createQuery("SELECT COUNT(c) FROM PageElementCatalog c", Long.class)
                .getSingleResult();
        assertThat(count).isGreaterThanOrEqualTo(14L);
    }

    @Test
    @DisplayName("V3_2 + V5_2 + V6_2 seeds install M10 personas + vendor list/detail + M11 integrations")
    void picoSeed() {
        List<String> codes = em.createQuery(
                        "SELECT p.code FROM PageDefinition p WHERE p.code LIKE 'pico:%'", String.class)
                .getResultList();
        // Assert containment rather than exact size — successive rounds add
        // pages and this test should grow naturally without breaking.
        assertThat(codes).contains(
                "pico:front-gate-create",          // V3_2 (Round 1F)
                "pico:parking-scan",                // V3_2
                "pico:dispatch-vehicle-entry",      // V3_2
                "pico:qc-approval",                 // V3_2
                "pico:weighment-capture",           // V3_2
                "pico:supervisor-unloading",        // V3_2
                "pico:manager-approval-inbox",      // V3_2
                "pico:vendor-list",                 // V5_2 (Round 1G)
                "pico:vendor-detail",               // V5_2
                "pico:m11-integrations-list");      // V6_2 (Round 1H)
        assertThat(codes).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("rollback creates a new PUBLISHED version forward")
    void rollbackForward() {
        PageCreateRequest req = new PageCreateRequest();
        req.setCode("it:rollback");
        req.setTitle("Rollback");
        req.setBodyJson("{\"v\":1}");
        svc.create(1L, req);
        svc.publish(1L, "it:rollback", null);

        PageUpdateRequest upd = new PageUpdateRequest();
        upd.setBodyJson("{\"v\":2}");
        svc.updateDraft(1L, "it:rollback", upd);
        svc.publish(1L, "it:rollback", null);

        // Now roll back to v1.
        PageVersion forward = svc.rollback(1L, "it:rollback", 1, null);
        assertThat(forward.getStatus()).isEqualTo("PUBLISHED");
        assertThat(forward.getBodyJson()).contains("\"v\":1");
        // It should be a NEW version row, not v1 itself.
        assertThat(forward.getVersion()).isGreaterThan(2);
    }

    @Test
    @Disabled("REOPEN WITH TASK_M0_TENANT_AUTH — cross-tenant isolation lands with the JWT-derived tenant context")
    void crossTenantIsolation() {
        // Tenant 1 creates page; Tenant 2 must not see it via renderer or list.
    }

    // ---- Slim Spring application for the M0 page substrate. -----------
    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            FreeMarkerAutoConfiguration.class,
            SecurityAutoConfiguration.class
    })
    @Import({RedisConfig.class})
    @EnableJpaRepositories(basePackages = {"com.aria.page.dao", "com.aria.workflow.dao"},
            transactionManagerRef = "m0PageTransactionManager",
            entityManagerFactoryRef = "entityManagerFactory")
    @ComponentScan(
            basePackages = {
                    "com.aria.page.service",
                    "com.aria.workflow",
                    "com.aria.page.dao",
                    "com.aria.workflow.dao",
            },
            // Skip the production M0PageJpaConfig — it expects a qualified
            // 'psqlDataSource' bean from the legacy PsqlConfig wiring, which
            // we don't load in this slim test context.
            excludeFilters = @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = com.aria.page.dao.M0PageJpaConfig.class))
    @org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.aria.page.model", "com.aria.workflow.model"})
    public static class PageSubstrateTestApp {

        /**
         * Test-only alias for the autoconfigured PlatformTransactionManager.
         * Production wiring registers the same bean under this name via
         * {@code M0PageJpaConfig}; the slim test boot path doesn't load that
         * config, so we synthesize the bean here.
         */
        @org.springframework.context.annotation.Bean(name = "m0PageTransactionManager")
        public org.springframework.transaction.PlatformTransactionManager m0PageTransactionManager(
                jakarta.persistence.EntityManagerFactory emf) {
            return new org.springframework.orm.jpa.JpaTransactionManager(emf);
        }
    }
}
