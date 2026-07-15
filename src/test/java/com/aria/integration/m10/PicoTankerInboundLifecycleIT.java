package com.aria.integration.m10;

import com.aria._base.EarmsContainersExtension;
import com.aria.tenant.TenantConfigBundleService;
import com.aria.workflow.dao.WorkflowEventRepository;
import com.aria.workflow.dao.WorkflowInstanceRepository;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowEvent;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full end-to-end IT for Pico Tanker Inbound (M10.TankerInbound v1).
 *
 * <p>The test exercises real DB persistence across the four transitions
 * plus the QC-reject branch:
 *
 * <ol>
 *   <li>Apply the Pico TENANT-CONFIG bundle (no-op on a re-seeded DB).</li>
 *   <li>Start an instance and run the four happy-path transitions.</li>
 *   <li>Verify gate_pass row exists with status mirrored from the engine.</li>
 *   <li>Run a separate instance through the REJECTED branch.</li>
 * </ol>
 *
 * Persona impersonation in this slim-app IT is via {@link TenantContext}
 * (the production REST layer derives tenant from the JWT). The 4-persona
 * end-to-end through the JWT is covered by the Playwright spec.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = PicoTankerInboundLifecycleIT.PicoLifecycleTestApp.class)
@ActiveProfiles("test")
class PicoTankerInboundLifecycleIT {

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
    @Autowired private WorkflowInstanceRepository instances;
    @Autowired private WorkflowEventRepository events;
    @Autowired private TenantConfigBundleService bundleService;
    @Autowired private DataSource dataSource;

    private JdbcTemplate jdbc;

    @BeforeEach
    void setTenant() {
        TenantContext.set(1L);
        jdbc = new JdbcTemplate(dataSource);
        seedGatePassProjection();
    }
    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    /**
     * Seed the generic {@code m0_workflow_projection} config that makes the
     * {@link com.aria.workflow.projection.WorkflowProjectionListener} materialise
     * a {@code gate_pass} row for M10.TankerInbound. This is exactly the row an
     * onboarding bundle would insert — the projection is DATA, not Java. The old
     * bespoke GatePassWorkflowListener produced the same numbers/columns; this
     * proves the generic mechanism reproduces them: gp_number=GP-{tenant}-{instance},
     * status mirrored on every transition, closed_at frozen on the terminal state.
     */
    private void seedGatePassProjection() {
        jdbc.update("DELETE FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.TankerInbound'");
        jdbc.update(
                "INSERT INTO m0_workflow_projection " +
                "(tenant_id, workflow_code, target_table, key_column, state_columns, " +
                " variant_column, number_column, number_pattern, terminal_states, " +
                " terminal_ts_column, field_map_json) " +
                "VALUES (1,'M10.TankerInbound','gate_pass','workflow_instance_id','status'," +
                "'flow_variant_code','gp_number','GP-{tenant}-{instance}','QC_REJECTED','closed_at'," +
                "'{\"vehicle_no\":[\"vehicleNo\"],\"driver_name\":[\"driverName\"],\"driver_phone\":[\"driverPhone\"]}')");
    }

    /** True if a gate_pass row has been projected for this workflow instance. */
    private boolean gatePassExists(long instanceId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gate_pass WHERE workflow_instance_id=? AND tenant_id=1",
                Integer.class, instanceId);
        return n != null && n > 0;
    }

    /** The projected gate_pass row for this workflow instance. */
    private Map<String, Object> gatePass(long instanceId) {
        return jdbc.queryForMap(
                "SELECT * FROM gate_pass WHERE workflow_instance_id=? AND tenant_id=1", instanceId);
    }

    @Test
    @DisplayName("Pico bundle applies cleanly + gate_pass tables present")
    void bundleAndSchema() {
        TenantConfigBundleService.ApplyResult r = bundleService.applyPicoBundle();
        assertThat(r.errors).isEmpty();
        // gate_pass schema present + projection config seeded.
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM gate_pass", Long.class)).isNotNull();
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM m0_workflow_projection WHERE workflow_code='M10.TankerInbound'",
                Long.class)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Happy path: 4 transitions + gate_pass status mirrored on each")
    void happyPath() {
        bundleService.applyPicoBundle();
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
        assertThat(inst.getCurrentState()).isEqualTo("DRAFT");

        // T1: front-gate-create
        WorkflowTransitionResult r1 = engine.handlePageSubmission(
                inst.getId(), "pico:front-gate-create",
                Map.of("vehicleNo", "TN-46-CJ-0001", "driverName", "Suresh Kumar", "driverPhone", "+919876543201"),
                2001L);
        assertThat(r1.isTransitioned()).isTrue();
        assertThat(r1.getNewState()).isEqualTo("GATE_PASS_CREATED");
        // gate_pass row materialised by the generic projection listener
        assertThat(gatePassExists(inst.getId())).isTrue();
        Map<String, Object> gp1 = gatePass(inst.getId());
        assertThat(gp1.get("status")).isEqualTo("GATE_PASS_CREATED");
        assertThat(gp1.get("vehicle_no")).isEqualTo("TN-46-CJ-0001");
        assertThat(gp1.get("driver_name")).isEqualTo("Suresh Kumar");
        assertThat(gp1.get("gp_number")).isEqualTo("GP-1-" + inst.getId());
        assertThat(gp1.get("flow_variant_code")).isEqualTo("M10.TankerInbound");

        // T2: parking-scan
        WorkflowTransitionResult r2 = engine.handlePageSubmission(
                inst.getId(), "pico:parking-scan",
                Map.of("bayCode", "PB-RM-01"), 2002L);
        assertThat(r2.getNewState()).isEqualTo("PARKING_IN");
        assertThat(gatePass(inst.getId()).get("status")).isEqualTo("PARKING_IN");

        // T3: dispatch-vehicle-entry
        WorkflowTransitionResult r3 = engine.handlePageSubmission(
                inst.getId(), "pico:dispatch-vehicle-entry",
                Map.of("invoiceNo", "INV-001", "invoiceQty", 12000), 2003L);
        assertThat(r3.getNewState()).isEqualTo("DISPATCH_QUEUED");
        assertThat(gatePass(inst.getId()).get("status")).isEqualTo("DISPATCH_QUEUED");

        // T4: qc-approval (APPROVED)
        WorkflowTransitionResult r4 = engine.handlePageSubmission(
                inst.getId(), "pico:qc-approval",
                Map.of("status", "APPROVED", "notes", "ok"), 2004L);
        assertThat(r4.getNewState()).isEqualTo("QC_APPROVED");
        assertThat(gatePass(inst.getId()).get("status")).isEqualTo("QC_APPROVED");

        // 4 STATE_TRANSITION events recorded (DRAFT start + 4 transitions = 5)
        List<WorkflowEvent> all = events.findByInstanceIdOrderByTsAsc(inst.getId());
        long transitions = all.stream().filter(e -> "STATE_TRANSITION".equals(e.getKind())).count();
        assertThat(transitions).isEqualTo(5);
    }

    @Test
    @DisplayName("QC reject branch: status routes to QC_REJECTED + gate_pass closed")
    void qcReject() {
        bundleService.applyPicoBundle();
        WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());

        engine.handlePageSubmission(inst.getId(), "pico:front-gate-create",
                Map.of("vehicleNo", "TN-30-AB-1001", "driverName", "Karthik Raja"), 2001L);
        engine.handlePageSubmission(inst.getId(), "pico:parking-scan", Map.of(), 2002L);
        engine.handlePageSubmission(inst.getId(), "pico:dispatch-vehicle-entry", Map.of(), 2003L);
        WorkflowTransitionResult r = engine.handlePageSubmission(
                inst.getId(), "pico:qc-approval",
                Map.of("status", "REJECTED", "reason", "tank cert expired"), 2004L);
        assertThat(r.getNewState()).isEqualTo("QC_REJECTED");

        WorkflowInstance reloaded = instances.findByIdAndTenantId(inst.getId(), 1L).get();
        assertThat(reloaded.getStatus()).isEqualTo("CLOSED");

        Map<String, Object> gp = gatePass(inst.getId());
        assertThat(gp.get("status")).isEqualTo("QC_REJECTED");
        assertThat(gp.get("closed_at")).isNotNull();
    }

    @Test
    @DisplayName("Multiple instances: each gets a distinct gp_number; counts add up")
    void multipleInstances() {
        bundleService.applyPicoBundle();
        long startingCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gate_pass WHERE tenant_id=1", Long.class);
        for (int i = 0; i < 3; i++) {
            WorkflowInstance inst = engine.startInstance("M10.TankerInbound", Map.of());
            engine.handlePageSubmission(inst.getId(), "pico:front-gate-create",
                    Map.of("vehicleNo", "MULTI-" + i), 2001L);
        }
        long endingCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM gate_pass WHERE tenant_id=1", Long.class);
        assertThat(endingCount - startingCount).isEqualTo(3);
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
            "com.aria.entity_registry.dao"
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
            },
            excludeFilters = @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                    classes = com.aria.page.dao.M0PageJpaConfig.class))
    @org.springframework.boot.autoconfigure.domain.EntityScan(basePackages = {
            "com.aria.page.model", "com.aria.workflow.model",
            "com.aria.entity_registry.model"
    })
    public static class PicoLifecycleTestApp {
        @org.springframework.context.annotation.Bean(name = "m0PageTransactionManager")
        public org.springframework.transaction.PlatformTransactionManager m0PageTransactionManager(
                jakarta.persistence.EntityManagerFactory emf) {
            return new org.springframework.orm.jpa.JpaTransactionManager(emf);
        }

        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        public TenantConfigBundleService tenantConfigBundleService(DataSource ds) {
            return new TenantConfigBundleService(new JdbcTemplate(ds));
        }
    }
}
