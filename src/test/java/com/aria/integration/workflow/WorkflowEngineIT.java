package com.aria.integration.workflow;

import com.aria._base.EarmsContainersExtension;
import com.aria.workflow.dao.WorkflowDefinitionRepository;
import com.aria.workflow.dao.WorkflowEventRepository;
import com.aria.workflow.dao.WorkflowInstanceRepository;
import com.aria.workflow.dto.WorkflowQueueItem;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.engine.WorkflowTransitionNotFoundException;
import com.aria.workflow.model.WorkflowInstance;
import com.aria.redis.RedisConfig;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end IT for the workflow runtime.
 *
 * <p>Drives the M10.TankerInbound workflow through all 4 happy-path
 * transitions, then a guard-rejected QC variant. Also covers queue
 * scoping by persona and cross-tenant isolation (TenantContext seeded
 * with tid=2 sees no tenant-1 instances).
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = WorkflowEngineIT.WorkflowTestApp.class)
@ActiveProfiles("test")
class WorkflowEngineIT {

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

    @Autowired private WorkflowEngineService engine;
    @Autowired private WorkflowDefinitionRepository defs;
    @Autowired private WorkflowInstanceRepository instances;
    @Autowired private WorkflowEventRepository events;

    @BeforeEach
    void setTenant() {
        TenantContext.set(1L);
    }

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("V4 seed inserts M10.TankerInbound v1")
    void seedPresent() {
        var def = defs.findLatestPublishedSingle(1L, "M10.TankerInbound");
        assertThat(def).isPresent();
        assertThat(def.get().getVersion()).isEqualTo(1);
        assertThat(def.get().getBodyJson()).contains("GATE_PASS_CREATED");
    }

    @Test
    @DisplayName("startInstance lands in DRAFT and writes STATE_TRANSITION event")
    void startsInDraft() {
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
        assertThat(inst.getCurrentState()).isEqualTo("DRAFT");
        assertThat(inst.getStatus()).isEqualTo("OPEN");
        assertThat(events.findByInstanceIdOrderByTsAsc(inst.getId()))
                .anyMatch(e -> "STATE_TRANSITION".equals(e.getKind()) && "DRAFT".equals(e.getToState()));
    }

    @Test
    @DisplayName("happy path: 4 transitions DRAFT -> GATE_PASS_CREATED -> PARKING_IN -> DISPATCH_QUEUED -> QC_APPROVED")
    void happyPathFourTransitions() {
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());

        WorkflowTransitionResult r1 = engine.handlePageSubmission(
                inst.getId(), "pico:front-gate-create",
                Map.of("vehicleNo", "TN-46-CJ-1234", "driverName", "Suresh", "driverPhone", "+919876543210"),
                1L);
        assertThat(r1.isTransitioned()).isTrue();
        assertThat(r1.getNewState()).isEqualTo("GATE_PASS_CREATED");
        assertThat(r1.getAssignedRole()).isEqualTo("ParkingSecurity");

        WorkflowTransitionResult r2 = engine.handlePageSubmission(
                inst.getId(), "pico:parking-scan", Map.of("lane", "L1"), 2L);
        assertThat(r2.getNewState()).isEqualTo("PARKING_IN");
        assertThat(r2.getAssignedRole()).isEqualTo("Dispatch");

        WorkflowTransitionResult r3 = engine.handlePageSubmission(
                inst.getId(), "pico:dispatch-vehicle-entry",
                Map.of("invoiceNo", "INV-1", "invoiceQty", 12000), 3L);
        assertThat(r3.getNewState()).isEqualTo("DISPATCH_QUEUED");
        assertThat(r3.getAssignedRole()).isEqualTo("QC");

        WorkflowTransitionResult r4 = engine.handlePageSubmission(
                inst.getId(), "pico:qc-approval",
                Map.of("status", "APPROVED", "notes", "ok"), 4L);
        assertThat(r4.isTransitioned()).isTrue();
        assertThat(r4.getNewState()).isEqualTo("QC_APPROVED");
    }

    @Test
    @DisplayName("guard branch: status=REJECTED routes to QC_REJECTED (terminal)")
    void guardRejectsToTerminal() {
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
        engine.handlePageSubmission(inst.getId(), "pico:front-gate-create",
                Map.of("vehicleNo", "X"), 1L);
        engine.handlePageSubmission(inst.getId(), "pico:parking-scan", Map.of(), 2L);
        engine.handlePageSubmission(inst.getId(), "pico:dispatch-vehicle-entry", Map.of(), 3L);
        WorkflowTransitionResult r = engine.handlePageSubmission(
                inst.getId(), "pico:qc-approval", Map.of("status", "REJECTED"), 4L);
        assertThat(r.getNewState()).isEqualTo("QC_REJECTED");
        WorkflowInstance reloaded = instances.findByIdAndTenantId(inst.getId(), 1L).get();
        assertThat(reloaded.getStatus()).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("no matching transition -> 422 (WorkflowTransitionNotFoundException)")
    void noMatchingTransition() {
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
        // pico:qc-approval is not valid from DRAFT
        assertThatThrownBy(() -> engine.handlePageSubmission(
                inst.getId(), "pico:qc-approval", Map.of("status", "APPROVED"), 1L))
                .isInstanceOf(WorkflowTransitionNotFoundException.class);
    }

    @Test
    @DisplayName("queueForPersona returns only OPEN instances assigned to that role")
    void queueScoping() {
        // Create 3 instances at GATE_PASS_CREATED
        for (int i = 0; i < 3; i++) {
            WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
            engine.handlePageSubmission(inst.getId(), "pico:front-gate-create",
                    Map.of("vehicleNo", "V" + i), 1L);
        }
        List<WorkflowQueueItem> q = engine.queueForPersona("ParkingSecurity", 0, 25);
        assertThat(q).isNotEmpty();
        q.forEach(item -> {
            assertThat(item.getAssignedRole()).isEqualTo("ParkingSecurity");
            assertThat(item.getStatus()).isEqualTo("OPEN");
        });
    }

    @Test
    @DisplayName("tenant isolation: tid=2 sees no tenant-1 data")
    void tenantIsolation() {
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
        TenantContext.set(2L);
        try {
            assertThat(engine.loadInstance(inst.getId())).isEmpty();
        } finally {
            TenantContext.set(1L);
        }
    }

    @Test
    @DisplayName("loadInstance returns submittedData and recent events")
    void loadInstanceHydratesData() {
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
        engine.handlePageSubmission(inst.getId(), "pico:front-gate-create",
                Map.of("vehicleNo", "X-1", "driverName", "Driver"), 1L);
        var dto = engine.loadInstance(inst.getId()).orElseThrow();
        assertThat(dto.getCurrentState()).isEqualTo("GATE_PASS_CREATED");
        assertThat(dto.getSubmittedData()).containsKey("pico:front-gate-create");
        assertThat(dto.getRecentEvents()).isNotEmpty();
    }

    // ============================================================ slim app
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
                    "com.aria.workflow.engine",
                    "com.aria.workflow.dao",
                    "com.aria.page.dao",
            },
            excludeFilters = @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = com.aria.page.dao.M0PageJpaConfig.class))
    @org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {"com.aria.page.model", "com.aria.workflow.model"})
    public static class WorkflowTestApp {
        @org.springframework.context.annotation.Bean(name = "m0PageTransactionManager")
        public org.springframework.transaction.PlatformTransactionManager m0PageTransactionManager(
                jakarta.persistence.EntityManagerFactory emf) {
            return new org.springframework.orm.jpa.JpaTransactionManager(emf);
        }
    }
}
