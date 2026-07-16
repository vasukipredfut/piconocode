package com.aria.page.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Bootstraps the M0 page-definition schema on application start.
 *
 * <p>Flyway is configured but DORMANT in this project (the Flyway-mysql
 * shim required by Flyway 10 on MariaDB is not on the classpath, and the
 * V3 migration must not block boot if a fresh test DB shows up). This
 * component takes responsibility for the V3 + V3_2 migrations: on startup
 * it checks {@code page_definition} existence and, if missing, runs each
 * SQL resource via Spring's {@link ScriptUtils}. Idempotent and cheap
 * (one {@code SHOW TABLES} per boot).
 *
 * <p>Disable via {@code earms.m0.pages.bootstrap.enabled=false} — useful in
 * tests that own their own schema (the IT for the page substrate uses the
 * testcontainer extension's V1 baseline + this initialiser to bring V3 up).
 */
@Component
public class M0PageSchemaInitialiser {

    private static final Logger log = LoggerFactory.getLogger(M0PageSchemaInitialiser.class);

    private static final List<String> SCRIPTS = List.of(
            "db/migration/V3__m0_pages.sql",
            "db/migration/V3_2__pico_pages_seed.sql",
            "db/migration/V4__m0_workflow.sql");

    // V4 is idempotent (CREATE TABLE IF NOT EXISTS + ON DUPLICATE KEY +
    // EXISTS guard on the seed). The probe below decides which scripts
    // need running based on the youngest table.

    @Autowired private DataSource dataSource;

    @Value("${earms.m0.pages.bootstrap.enabled:true}")
    private boolean enabled;

    @PostConstruct
    public void run() {
        if (!enabled) {
            log.info("M0 page-schema bootstrap disabled by config");
            return;
        }
        try (Connection c = dataSource.getConnection()) {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            boolean pagesPresent = tableExists(jdbc, "page_definition");
            boolean workflowPresent = tableExists(jdbc, "workflow_definition");
            if (!pagesPresent) {
                runScript(c, "db/migration/V3__m0_pages.sql");
                runScript(c, "db/migration/V3_2__pico_pages_seed.sql");
            } else {
                log.info("M0 page-schema already present; V3 + V3_2 skipped");
            }
            // V5 substrate (Round 1G — Schema V2). Idempotent on every
            // CREATE TABLE / INSERT IGNORE so we always probe + replay.
            if (!tableExists(jdbc, "action_registry")) {
                log.info("Applying V5 Schema V2 substrate");
                runScript(c, "db/migration/V5__schema_v2.sql");
            } else {
                // V5 may have been applied before its field-registry seed
                // grew (e.g. SavedView writable columns added post-1G).
                // Probe a representative new row and replay the INSERTs if
                // missing — CREATE TABLE IF NOT EXISTS is a no-op, INSERT
                // IGNORE skips existing rows.
                try {
                    Integer svFieldCount = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM entity_field_registry WHERE entity_code='SavedView'",
                            Integer.class);
                    if (svFieldCount == null || svFieldCount < 8) {
                        log.info("V5 entity_field_registry under-seeded ({} SavedView rows); replaying V5",
                                svFieldCount);
                        try { runScript(c, "db/migration/V5__schema_v2.sql"); }
                        catch (RuntimeException e) { log.warn("V5 top-up failed: {}", e.getMessage()); }
                    }
                } catch (Exception probe) {
                    log.warn("V5 field-registry probe failed: {}", probe.getMessage());
                }
            }
            if (!tableExists(jdbc, "m0_vendor")) {
                log.info("Applying V5_1 vendor demo seed");
                runScript(c, "db/migration/V5_1__vendor_demo_seed.sql");
            } else if (tableExists(jdbc, "state") && tableExists(jdbc, "city")) {
                // Tables present — top up rows only if missing.
                Integer vendorCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM m0_vendor WHERE tenant_id=1", Integer.class);
                if (vendorCount == null || vendorCount < 20) {
                    log.info("m0_vendor under-seeded ({}); replaying V5_1", vendorCount);
                    try { runScript(c, "db/migration/V5_1__vendor_demo_seed.sql"); }
                    catch (RuntimeException e) { log.warn("V5_1 replay failed: {}", e.getMessage()); }
                }
            }
            // pico vendor pages — V5_2 is idempotent on (tenant_id, code).
            Integer vendorPageCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code IN ('pico:vendor-list','pico:vendor-detail')",
                    Integer.class);
            if (vendorPageCount == null || vendorPageCount < 2) {
                log.info("Applying V5_2 pico vendor pages seed");
                try { runScript(c, "db/migration/V5_2__pico_vendor_pages.sql"); }
                catch (RuntimeException e) { log.warn("V5_2 application failed: {}", e.getMessage()); }
            }
            if (!workflowPresent) {
                runScript(c, "db/migration/V4__m0_workflow.sql");
            } else {
                // V4 is idempotent; rerun only when its seed row is missing.
                Integer count = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM workflow_definition WHERE code='M10.TankerInbound'",
                        Integer.class);
                if (count == null || count == 0) {
                    log.info("workflow_definition present but seed missing — re-running V4");
                    runScript(c, "db/migration/V4__m0_workflow.sql");
                } else {
                    log.info("M0 workflow schema + seed already present; V4 skipped");
                }
            }
            // V132 — Core auth/HR tables (employee_details, role, department_details,
            // permissions, refresh_token) were referenced everywhere but never had a
            // CREATE TABLE — assumed to come from an external legacy DB dump. Fresh
            // installs without that dump had silently-broken login. Create them here,
            // before the V4_1 probe below (which requires employee_details + role).
            try {
                if (!tableExists(jdbc, "employee_details") || !tableExists(jdbc, "role")) {
                    log.info("Core auth tables (employee_details/role/...) missing; applying V132");
                    try { runScript(c, "db/migration/V132__hr_core_auth_tables.sql"); }
                    catch (RuntimeException e) { log.warn("V132 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V132 probe failed: {}", probe.getMessage()); }

            // V123 — generic workflow->row projection config table. No prior probe
            // existed for this file; V134 below ALTERs this table, so it must exist
            // first. Missing on any DB never booted through a hand-seeded dump.
            try {
                if (!tableExists(jdbc, "m0_workflow_projection")) {
                    log.info("m0_workflow_projection missing; applying V123");
                    try { runScript(c, "db/migration/V123__workflow_projection.sql"); }
                    catch (RuntimeException e) { log.warn("V123 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V123 probe failed: {}", probe.getMessage()); }

            // V124/V125 — print_template (config-driven print/voucher feature).
            // V136 below INSERTs into this table, so it must exist first.
            try {
                if (!tableExists(jdbc, "print_template")) {
                    log.info("print_template missing; applying V124");
                    try { runScript(c, "db/migration/V124__print_template.sql"); }
                    catch (RuntimeException e) { log.warn("V124 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V124 probe failed: {}", probe.getMessage()); }
            try {
                Integer missingOutputKind = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema=DATABASE() AND table_name='print_template' AND column_name='output_kind'",
                        Integer.class);
                if (tableExists(jdbc, "print_template") && (missingOutputKind == null || missingOutputKind == 0)) {
                    log.info("print_template.output_kind missing; applying V125");
                    try { runScript(c, "db/migration/V125__print_template_label.sql"); }
                    catch (RuntimeException e) { log.warn("V125 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V125 probe failed: {}", probe.getMessage()); }

            // V133 — Parking Bay Entry (per-vehicle in/out log against a bay,
            // distinct from dispatch_parking_bay master data).
            try {
                if (!tableExists(jdbc, "dispatch_parking_bay_entry")) {
                    log.info("dispatch_parking_bay_entry missing; applying V133");
                    try { runScript(c, "db/migration/V133__parking_bay_entry.sql"); }
                    catch (RuntimeException e) { log.warn("V133 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V133 probe failed: {}", probe.getMessage()); }

            // V134 — Dispatch Vehicle Details as its own entity (Tanker/Packing/FG
            // variant fields) + Parking Queue page. Guard on the LAST artifact the
            // script creates (pico:dispatch-parking-queue), not just the table —
            // a DB that already has dispatch_vehicle_detail from a prior partial
            // run (aborted once it hit the not-yet-existent m0_workflow_projection
            // ALTER) must still re-run the rest; every statement in V134 is its own
            // idempotent IF NOT EXISTS/WHERE NOT EXISTS guard, so re-running in full
            // is safe.
            try {
                Integer missingQueuePage = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE code='pico:dispatch-parking-queue'",
                        Integer.class);
                if (!tableExists(jdbc, "dispatch_vehicle_detail") || missingQueuePage == null || missingQueuePage == 0) {
                    log.info("dispatch_vehicle_detail table or pico:dispatch-parking-queue missing; applying V134");
                    try { runScript(c, "db/migration/V134__dispatch_vehicle_details_variants.sql"); }
                    catch (RuntimeException e) { log.warn("V134 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V134 probe failed: {}", probe.getMessage()); }

            // V135 — Dispatch Parking Bay In screen (gate-pass picker + auto-filled
            // vehicle/flow/loading context).
            try {
                Integer missingScanPage = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE code='pico:dispatch-parking-scan'",
                        Integer.class);
                if (missingScanPage == null || missingScanPage == 0) {
                    log.info("pico:dispatch-parking-scan missing; applying V135");
                    try { runScript(c, "db/migration/V135__dispatch_parking_bay_scan.sql"); }
                    catch (RuntimeException e) { log.warn("V135 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V135 probe failed: {}", probe.getMessage()); }

            // V136 — front-gate-create: Loading Type / Vehicle Category / dynamic
            // Flow Variant + real QR print template for gate pass.
            try {
                Integer missingPrintTpl = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM print_template WHERE code='gate-pass'", Integer.class);
                if (missingPrintTpl == null || missingPrintTpl == 0) {
                    log.info("print_template 'gate-pass' missing; applying V136");
                    try { runScript(c, "db/migration/V136__front_gate_create_variants_and_print.sql"); }
                    catch (RuntimeException e) { log.warn("V136 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V136 probe failed: {}", probe.getMessage()); }

            // M10 outbound dispatch (Tanker-Outbound / Packing / FG loading flow —
            // Gate Pass -> Parking -> Dispatch Entry -> QC -> Weighment x3 ->
            // Loading -> Sealing -> PMT check -> Billing -> Gate Pass Closure).
            // V87/V127/V129/V130/V131 shipped as files but had no boot hook, so on
            // any DB not seeded by hand they silently never took effect (gate_pass
            // was missing the dispatch/QC/weight columns and pico:loading-order-sheet
            // didn't exist at all). Order matters: V87 creates the page rows +
            // workflow bodies that V127/V131 then repair/extend.
            try {
                Integer missingDispatchPage = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE code='pico:dispatch-vehicle-details'",
                        Integer.class);
                if (missingDispatchPage == null || missingDispatchPage == 0) {
                    log.info("M10 loading workflows/pages (V87) missing; applying");
                    try { runScript(c, "db/migration/V87__m10_loading_workflows.sql"); }
                    catch (RuntimeException e) { log.warn("V87 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V87 probe failed: {}", probe.getMessage()); }

            try {
                Integer needsFrontGateRepair = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_version pv JOIN page_definition pd ON pd.id = pv.page_id " +
                        "WHERE pd.code='pico:front-gate-create' AND pv.status='PUBLISHED' " +
                        "  AND pv.body_json NOT LIKE '%WorkflowQueueList%'",
                        Integer.class);
                if (needsFrontGateRepair != null && needsFrontGateRepair > 0) {
                    log.info("pico:front-gate-create still regressed to list-only body; applying V127");
                    try { runScript(c, "db/migration/V127__front_gate_create_form.sql"); }
                    catch (RuntimeException e) { log.warn("V127 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V127 probe failed: {}", probe.getMessage()); }

            try {
                Integer missingGatePassCols = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema=DATABASE() AND table_name='gate_pass' AND column_name='broker_name'",
                        Integer.class);
                if (tableExists(jdbc, "gate_pass") && (missingGatePassCols == null || missingGatePassCols == 0)) {
                    log.info("M10 outbound dispatch/QC/weight/loading fields missing on gate_pass; applying V129");
                    try { runScript(c, "db/migration/V129__m10_outbound_dispatch_fields.sql"); }
                    catch (RuntimeException e) { log.warn("V129 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V129 probe failed: {}", probe.getMessage()); }

            try {
                Integer missingOrderLineCols = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema=DATABASE() AND table_name='m5_sales_order_line' AND column_name='offer'",
                        Integer.class);
                if (tableExists(jdbc, "m5_sales_order_line") && (missingOrderLineCols == null || missingOrderLineCols == 0)) {
                    log.info("Loading order-sheet fields missing on m5_sales_order_line; applying V130");
                    try { runScript(c, "db/migration/V130__order_line_loading_fields.sql"); }
                    catch (RuntimeException e) { log.warn("V130 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V130 probe failed: {}", probe.getMessage()); }

            try {
                Integer needsLoadingSheetPage = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_version pv JOIN page_definition pd ON pd.id = pv.page_id " +
                        "WHERE pd.code='pico:loading-order-sheet' AND pv.status='PUBLISHED' " +
                        "  AND pv.body_json NOT LIKE '%ListView%'",
                        Integer.class);
                if (needsLoadingSheetPage != null && needsLoadingSheetPage > 0) {
                    log.info("pico:loading-order-sheet still on the upload-only body; applying V131");
                    try { runScript(c, "db/migration/V131__loading_order_sheet_page.sql"); }
                    catch (RuntimeException e) { log.warn("V131 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V131 probe failed: {}", probe.getMessage()); }

            // V4_1 is conditional: only applies when the legacy
            // employee_details + role tables exist (production DB does,
            // testcontainer slim DB does not).
            if (tableExists(jdbc, "employee_details") && tableExists(jdbc, "role")) {
                try {
                    Integer userCount = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM employee_details WHERE id IN (2001,2002,2003,2004)",
                            Integer.class);
                    if (userCount == null || userCount < 4) {
                        log.info("Persona seed missing — applying V4_1");
                        runScript(c, "db/migration/V4_1__m0_workflow_personas.sql");
                    } else {
                        log.info("Persona seed already present; V4_1 skipped");
                    }
                } catch (Exception probe) {
                    log.warn("Persona seed probe failed; attempting V4_1 anyway: {}", probe.getMessage());
                    try { runScript(c, "db/migration/V4_1__m0_workflow_personas.sql"); }
                    catch (RuntimeException e) { log.warn("V4_1 application failed: {}", e.getMessage()); }
                }
            } else {
                log.info("employee_details/role tables absent; V4_1 persona seed skipped (test environment)");
            }
            // V6 — M11 Integration Hub substrate (Round 1H.1). All CREATE
            // TABLE statements use IF NOT EXISTS so re-applying is cheap.
            if (!tableExists(jdbc, "integration_connector_catalog")
                    || !tableExists(jdbc, "integration_instance")) {
                log.info("Applying V6 M11 integration_hub substrate");
                try { runScript(c, "db/migration/V6__m11_integration_hub.sql"); }
                catch (RuntimeException e) { log.warn("V6 application failed: {}", e.getMessage()); }
            }
            // V6_1 — connector catalog seed. INSERT IGNORE on (code, version).
            try {
                Integer catCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM integration_connector_catalog", Integer.class);
                if (catCount == null || catCount < 66) {
                    log.info("integration_connector_catalog under-seeded ({}); applying V6_1", catCount);
                    try { runScript(c, "db/migration/V6_1__m11_connector_catalog_seed.sql"); }
                    catch (RuntimeException e) { log.warn("V6_1 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V6_1 probe failed: {}", probe.getMessage());
            }
            // V6_2 — Integration pages + entity registry seed.
            try {
                Integer pageCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:m11-integrations-list'",
                        Integer.class);
                if (pageCount == null || pageCount < 1) {
                    log.info("M11 page seed missing; applying V6_2");
                    try { runScript(c, "db/migration/V6_2__m11_pages.sql"); }
                    catch (RuntimeException e) { log.warn("V6_2 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V6_2 probe failed: {}", probe.getMessage());
            }
            // V7_3 — M10 minimal dispatch DDL. Idempotent CREATEs; run when
            // gate_pass is absent OR companion tables are missing.
            if (!tableExists(jdbc, "gate_pass")
                    || !tableExists(jdbc, "dispatch_vehicle")
                    || !tableExists(jdbc, "tenant_form_layout")
                    || !tableExists(jdbc, "tenant_role")
                    || !tableExists(jdbc, "notification_template")) {
                log.info("Applying V7_3 M10 dispatch DDL");
                try { runScript(c, "db/migration/V7_3__m10_dispatch_tables.sql"); }
                catch (RuntimeException e) { log.warn("V7_3 application failed: {}", e.getMessage()); }
            }
            // V7 — Pico TENANT-CONFIG apply (workflows / fields / layouts /
            // saved views / notification templates). Idempotent on natural keys.
            try {
                Integer wfCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM workflow_definition WHERE tenant_id=1 AND code LIKE 'M10.%'",
                        Integer.class);
                if (wfCount == null || wfCount < 10) {
                    log.info("workflow_definition under-seeded ({}); applying V7", wfCount);
                    try { runScript(c, "db/migration/V7__pico_tenant_config.sql"); }
                    catch (RuntimeException e) { log.warn("V7 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V7 probe failed: {}", probe.getMessage());
            }
            // V7_1 — Pico master data seed. Probe vehicle count.
            try {
                Integer vCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM dispatch_vehicle WHERE tenant_id=1", Integer.class);
                if (vCount == null || vCount < 50) {
                    log.info("dispatch_vehicle under-seeded ({}); applying V7_1", vCount);
                    try { runScript(c, "db/migration/V7_1__pico_master_data_seed.sql"); }
                    catch (RuntimeException e) { log.warn("V7_1 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V7_1 probe failed: {}", probe.getMessage());
            }
            // V7_2 — Pico role permissions. Run only when the legacy role +
            // employee_details tables exist.
            if (tableExists(jdbc, "role") && tableExists(jdbc, "employee_details")
                    && tableExists(jdbc, "tenant_role")) {
                try {
                    Integer roleCount = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM tenant_role WHERE tenant_id=1", Integer.class);
                    if (roleCount == null || roleCount < 9) {
                        log.info("tenant_role under-seeded ({}); applying V7_2", roleCount);
                        try { runScript(c, "db/migration/V7_2__pico_role_permissions.sql"); }
                        catch (RuntimeException e) { log.warn("V7_2 application failed: {}", e.getMessage()); }
                    }
                } catch (Exception probe) {
                    log.warn("V7_2 probe failed: {}", probe.getMessage());
                }
            }
            // V7_4 — register GatePass entity + seed pico:recent-gate-passes
            // list view bound to it (Round 3 follow-up). Idempotent on the
            // page_definition.code; safe to re-run on every boot.
            try {
                Integer pageCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:recent-gate-passes'",
                        Integer.class);
                if (pageCount == null || pageCount < 1) {
                    log.info("pico:recent-gate-passes page missing; applying V7_4");
                    try { runScript(c, "db/migration/V7_4__gatepass_entity_and_recent_page.sql"); }
                    catch (RuntimeException e) { log.warn("V7_4 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V7_4 probe failed: {}", probe.getMessage());
            }
            // V7_5 — additional configurable list pages (Driver / TankLocation
            // / WorkflowEvent) — pure config, zero React. Probe on the
            // pico:driver-master page existence (NOT the Driver entity, which
            // was registered by an earlier migration). The V7_5 script itself
            // is idempotent via NOT EXISTS guards on every INSERT.
            try {
                Integer pageCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:driver-master'",
                        Integer.class);
                if (pageCount == null || pageCount < 1) {
                    log.info("pico:driver-master page missing; applying V7_5");
                    try { runScript(c, "db/migration/V7_5__more_configurable_pages.sql"); }
                    catch (RuntimeException e) { log.warn("V7_5 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V7_5 probe failed: {}", probe.getMessage());
            }

            // V7_6 — Vehicle + Gate master pages.
            try {
                Integer pageCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:vehicle-master'",
                        Integer.class);
                if (pageCount == null || pageCount < 1) {
                    log.info("pico:vehicle-master page missing; applying V7_6");
                    try { runScript(c, "db/migration/V7_6__vehicle_gate_pages.sql"); }
                    catch (RuntimeException e) { log.warn("V7_6 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) {
                log.warn("V7_6 probe failed: {}", probe.getMessage());
            }

            // V7_7 — Weighbridge + ParkingBay + Lane master pages.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:weighbridge-master'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:weighbridge-master page missing; applying V7_7");
                    try { runScript(c, "db/migration/V7_7__weighbridge_parking_lane_pages.sql"); }
                    catch (RuntimeException e) { log.warn("V7_7 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_7 probe failed: {}", probe.getMessage()); }

            // V7_8 — Extend M10.TankerInbound to 7 steps + add gate-pass-closure page.
            // Probe: closure page exists.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:gate-pass-closure'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:gate-pass-closure missing; applying V7_8");
                    try { runScript(c, "db/migration/V7_8__extend_tanker_inbound_workflow.sql"); }
                    catch (RuntimeException e) { log.warn("V7_8 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_8 probe failed: {}", probe.getMessage()); }

            // V7_9 — Plantowner dashboard with live KPI tiles.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:plantowner-dashboard'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:plantowner-dashboard missing; applying V7_9");
                    try { runScript(c, "db/migration/V7_9__plantowner_dashboard.sql"); }
                    catch (RuntimeException e) { log.warn("V7_9 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_9 probe failed: {}", probe.getMessage()); }

            // V7_10 — Manager inbox + Operator dashboard persona pages.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:manager-inbox'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:manager-inbox missing; applying V7_10");
                    try { runScript(c, "db/migration/V7_10__manager_inbox_and_persona_dashes.sql"); }
                    catch (RuntimeException e) { log.warn("V7_10 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_10 probe failed: {}", probe.getMessage()); }

            // V7_11 — Detail-view pages for master entities.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:gate-pass-detail'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:gate-pass-detail missing; applying V7_11");
                    try { runScript(c, "db/migration/V7_11__detail_pages.sql"); }
                    catch (RuntimeException e) { log.warn("V7_11 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_11 probe failed: {}", probe.getMessage()); }

            // V7_12 — TankerOutbound workflow + 4 pages.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:gate-pass-out-closure'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:gate-pass-out-closure missing; applying V7_12");
                    try { runScript(c, "db/migration/V7_12__tanker_outbound_workflow.sql"); }
                    catch (RuntimeException e) { log.warn("V7_12 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_12 probe failed: {}", probe.getMessage()); }

            // V7_13 — Real workflow bodies for the 8 skeleton variants.
            // Probe: PackUnload has more than 2 states (skeleton was 2).
            try {
                Integer states = jdbc.queryForObject(
                        "SELECT JSON_LENGTH(JSON_EXTRACT(body_json,\"$.states\")) FROM workflow_definition WHERE tenant_id=1 AND code='M10.PackUnload'",
                        Integer.class);
                if (states == null || states < 4) {
                    log.info("M10.PackUnload skeleton detected ({} states); applying V7_13", states);
                    try { runScript(c, "db/migration/V7_13__remaining_workflow_variants.sql"); }
                    catch (RuntimeException e) { log.warn("V7_13 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_13 probe failed: {}", probe.getMessage()); }

            // V7_14 — Full 16-step TankerInbound + reconciliation/manager-approval pages.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:reconciliation'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:reconciliation missing; applying V7_14");
                    try { runScript(c, "db/migration/V7_14__tanker_inbound_full_16_steps.sql"); }
                    catch (RuntimeException e) { log.warn("V7_14 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_14 probe failed: {}", probe.getMessage()); }

            // V7_15 — HR domain pages (Employee/Role/Department).
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:employee-master'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("pico:employee-master missing; applying V7_15");
                    try { runScript(c, "db/migration/V7_15__hr_pages.sql"); }
                    catch (RuntimeException e) { log.warn("V7_15 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_15 probe failed: {}", probe.getMessage()); }

            // V7_16 — Flip 14 more action_registry rows to implemented=true.
            try {
                Integer impl = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM action_registry WHERE implemented=true", Integer.class);
                if (impl == null || impl < 14) {
                    log.info("action_registry under-implemented ({}); applying V7_16", impl);
                    try { runScript(c, "db/migration/V7_16__activate_more_actions.sql"); }
                    catch (RuntimeException e) { log.warn("V7_16 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V7_16 probe failed: {}", probe.getMessage()); }

            // V8 — M5 Material Requisition + Inward GRN.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:mr-list'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("M5 MR/GRN pages missing; applying V8");
                    try { runScript(c, "db/migration/V8__material_requisition_and_inward_grn.sql"); }
                    catch (RuntimeException e) { log.warn("V8 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V8 probe failed: {}", probe.getMessage()); }

            // V9_1 — M5 Purchase Order + Sales Order (uses m5_ prefix to avoid
            // legacy V1 baseline name collision; V9 itself is superseded).
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m5_purchase_order'",
                        Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m5_purchase_order missing; applying V9_1");
                    try { runScript(c, "db/migration/V9_1__rename_to_m5_prefix.sql"); }
                    catch (RuntimeException e) { log.warn("V9_1 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V9_1 probe failed: {}", probe.getMessage()); }

            // V10 — Inward entry variants (chooser + 3 alt-source pages).
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE tenant_id=1 AND code='pico:inward-chooser'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("inward chooser missing; applying V10");
                    try { runScript(c, "db/migration/V10__inward_variants.sql"); }
                    catch (RuntimeException e) { log.warn("V10 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V10 probe failed: {}", probe.getMessage()); }
            // V11 — Stock primitives (balance + movement ledger).
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m5_stock_balance'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m5_stock_balance missing; applying V11");
                    try { runScript(c, "db/migration/V11__stock_primitives.sql"); }
                    catch (RuntimeException e) { log.warn("V11 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V11 probe failed: {}", probe.getMessage()); }

            // V12 — Production + Packing + FG Dispatch + Returns + Reports + Shifts (mega).
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m5_job_card'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m5_job_card missing; applying V12");
                    try { runScript(c, "db/migration/V12__production_packing_returns_reports_shifts.sql"); }
                    catch (RuntimeException e) { log.warn("V12 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V12 probe failed: {}", probe.getMessage()); }

            // V13 — BOM + Quality Plan/Test + Asset/Maintenance + Energy + KYC.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m5_bom'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m5_bom missing; applying V13");
                    try { runScript(c, "db/migration/V13__bom_quality_asset_energy_kyc.sql"); }
                    catch (RuntimeException e) { log.warn("V13 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V13 probe failed: {}", probe.getMessage()); }

            // V14 — M6 Finance.
            try {
                Integer pc = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m6_sales_invoice'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m6_sales_invoice missing; applying V14");
                    try { runScript(c, "db/migration/V14__m6_finance.sql"); }
                    catch (RuntimeException e) { log.warn("V14 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V14 probe failed: {}", probe.getMessage()); }
            // V15 — Subscription + AMC + Depreciation.
            try {
                Integer pc = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m5_subscription'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m5_subscription missing; applying V15");
                    try { runScript(c, "db/migration/V15__subscription_amc_depreciation.sql"); }
                    catch (RuntimeException e) { log.warn("V15 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V15 probe failed: {}", probe.getMessage()); }
            // V16 — Saved views + pivot + onboarding wizard.
            try {
                Integer pc = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m0_pivot_view'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m0_pivot_view missing; applying V16");
                    try { runScript(c, "db/migration/V16__saved_views_and_pivot.sql"); }
                    catch (RuntimeException e) { log.warn("V16 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V16 probe failed: {}", probe.getMessage()); }

            // V17 — HR/CRM/FieldService/Compliance/DMS mega.
            try {
                Integer pc = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m7_attendance'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("m7_attendance missing; applying V17");
                    try { runScript(c, "db/migration/V17__hr_crm_fieldservice_compliance_docs.sql"); }
                    catch (RuntimeException e) { log.warn("V17 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V17 probe failed: {}", probe.getMessage()); }

            // V18 — seed integration_instance rows (so M11 page isn't empty).
            try {
                Integer iiCount = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM integration_instance WHERE tenant_id=1", Integer.class);
                if (iiCount == null || iiCount < 1) {
                    log.info("integration_instance empty; applying V18 seed");
                    try { runScript(c, "db/migration/V18__seed_m11_integration_instances.sql"); }
                    catch (RuntimeException e) { log.warn("V18 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V18 probe failed: {}", probe.getMessage()); }

            // V19 — Widget hints on entity_field_registry + Employee lifecycle.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='entity_field_registry' AND column_name='widget_kind'", Integer.class);
                if (pc == null || pc < 1) {
                    log.info("widget_kind column missing; applying V19");
                    try { runScript(c, "db/migration/V19__widget_hints_employee_lifecycle.sql"); }
                    catch (RuntimeException e) { log.warn("V19 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V19 probe failed: {}", probe.getMessage()); }

            // V20 — Bulk widget assignment across all entity_field_registry rows.
            try {
                Integer pc = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_field_registry WHERE widget_kind IS NOT NULL", Integer.class);
                if (pc == null || pc < 80) {
                    log.info("widget coverage low ({}); applying V20", pc);
                    try { runScript(c, "db/migration/V20__bulk_widget_hints.sql"); }
                    catch (RuntimeException e) { log.warn("V20 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V20 probe failed: {}", probe.getMessage()); }

            // V21 — Fix field_code mismatches (camelCase rows that don't match snake_case columns).
            try {
                Integer mis = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_field_registry WHERE entity_code='Driver' AND field_code='dlNumber'", Integer.class);
                if (mis != null && mis > 0) {
                    log.info("Driver still has camelCase dlNumber; applying V21");
                    try { runScript(c, "db/migration/V21__fix_field_code_mismatches.sql"); }
                    catch (RuntimeException e) { log.warn("V21 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V21 probe failed: {}", probe.getMessage()); }

            // V21_1 — Clean up camelCase rows that V21 couldn't rename due to uniq collision.
            try {
                Integer mis = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_field_registry WHERE entity_code='Driver' AND field_code='dlNumber'", Integer.class);
                if (mis != null && mis > 0) {
                    log.info("camelCase Driver fields still present; applying V21_1");
                    try { runScript(c, "db/migration/V21_1__cleanup_camelcase_field_rows.sql"); }
                    catch (RuntimeException e) { log.warn("V21_1 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V21_1 probe failed: {}", probe.getMessage()); }

            // V22 — Consolidate duplicate page categories.
            try {
                Integer n = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition WHERE category='M10.Dispatch' OR category='Persona' OR category='M3'", Integer.class);
                if (n != null && n > 0) {
                    log.info("split categories detected ({}); applying V22", n);
                    try { runScript(c, "db/migration/V22__normalize_page_categories.sql"); }
                    catch (RuntimeException e) { log.warn("V22 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V22 probe failed: {}", probe.getMessage()); }

            // V23 — Comprehensive Indian-statutory HR + Payroll backbone.
            try {
                Integer n = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema=DATABASE() AND table_name='m7_employee'", Integer.class);
                if (n == null || n == 0) {
                    log.info("m7_employee absent; applying V23 (HR/Payroll backbone)");
                    try { runScript(c, "db/migration/V23__hr_payroll_comprehensive.sql"); }
                    catch (RuntimeException e) { log.warn("V23 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V23 probe failed: {}", probe.getMessage()); }

            // V78 — Add visible_when / required_when / computed_from rule
            // columns to entity_field_registry (conditional field visibility
            // + auto-computed fields).
            try {
                Integer hasCol = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema=DATABASE() AND table_name='entity_field_registry' " +
                        "  AND column_name='visible_when'", Integer.class);
                if (hasCol == null || hasCol == 0) {
                    log.info("entity_field_registry.visible_when absent; applying V78");
                    try { runScript(c, "db/migration/V78__field_rules_visibleWhen_requiredWhen_computedFrom.sql"); }
                    catch (RuntimeException e) { log.warn("V78 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V78 probe failed: {}", probe.getMessage()); }

            // V79 — demo wiring for V78 (visibleWhen on qc-approval fields).
            try {
                Integer hasV79 = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_field_registry " +
                        "WHERE entity_code='WorkflowInstance' AND field_code='reject_reason' " +
                        "  AND visible_when IS NOT NULL", Integer.class);
                if (hasV79 == null || hasV79 == 0) {
                    log.info("V78 demo wiring absent; applying V79");
                    try { runScript(c, "db/migration/V79__demo_visible_when_qc_approval.sql"); }
                    catch (RuntimeException e) { log.warn("V79 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V79 probe failed: {}", probe.getMessage()); }

            // V77 — Clean up probe-* test pollution from earlier agent runs.
            try {
                Integer pollution = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_registry WHERE code LIKE 'probe-%'", Integer.class);
                if (pollution != null && pollution > 0) {
                    log.info("{} probe-* test pollution rows; applying V77", pollution);
                    try { runScript(c, "db/migration/V77__cleanup_probe_test_pollution.sql"); }
                    catch (RuntimeException e) { log.warn("V77 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V77 probe failed: {}", probe.getMessage()); }

            // V80 — Rich page kit: KanbanView + WorkflowStepper catalog entries
            // + two demo pages (pico:dispatch-kanban, pico:workflow-stepper-demo).
            try {
                Integer hasV80 = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_element_catalog WHERE code IN ('KanbanView','WorkflowStepper')",
                        Integer.class);
                if (hasV80 == null || hasV80 < 2) {
                    log.info("Rich-page-kit catalog absent; applying V80");
                    try { runScript(c, "db/migration/V80__rich_page_kit.sql"); }
                    catch (RuntimeException e) { log.warn("V80 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V80 probe failed: {}", probe.getMessage()); }

            // V81 — Repair dangling page_definition.current_version_id pointers.
            // Triggered by finding pages whose current_version_id references a
            // non-existent page_version row. Symptom masked as 401 via /error.
            try {
                Integer dangling = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition p " +
                        "LEFT JOIN page_version v ON v.id = p.current_version_id " +
                        "WHERE p.current_version_id IS NOT NULL AND v.id IS NULL", Integer.class);
                if (dangling != null && dangling > 0) {
                    log.info("{} pages with dangling current_version_id; applying V81", dangling);
                    try { runScript(c, "db/migration/V81__repair_dangling_page_current_version.sql"); }
                    catch (RuntimeException e) { log.warn("V81 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V81 probe failed: {}", probe.getMessage()); }

            // V76 — Fix M10.EwayBill SUBMITTED→GENERATED transition trigger
            // (was "workflow.transition", should be "page.submitted").
            try {
                Integer bad = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM workflow_definition " +
                        "WHERE code='M10.EwayBill' AND status='PUBLISHED' " +
                        "  AND body_json LIKE '%\"trigger\":\"workflow.transition\",\"pageCode\":\"pico:eway-create\"%'",
                        Integer.class);
                if (bad != null && bad > 0) {
                    log.info("M10.EwayBill has bad transition trigger; applying V76");
                    try { runScript(c, "db/migration/V76__fix_eway_bill_transition_trigger.sql"); }
                    catch (RuntimeException e) { log.warn("V76 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V76 probe failed: {}", probe.getMessage()); }

            // V74 — Department field rename (departmentname → name) to match
            // the JPA-mapped column. Surfaced by field-level test agent via
            // "Unknown column 'department_details.departmentname'".
            try {
                Integer drift = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_field_registry " +
                        " WHERE entity_code='Department' AND field_code='departmentname'", Integer.class);
                if (drift != null && drift > 0) {
                    log.info("Department.departmentname stale row present; applying V74");
                    try { runScript(c, "db/migration/V74__fix_department_field_name_drift.sql"); }
                    catch (RuntimeException e) { log.warn("V74 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V74 probe failed: {}", probe.getMessage()); }

            // V75 — Auto-register ALL DB columns into entity_field_registry
            // (V72 was scoped to 17 entities; this re-runs without that
            // scope so every NOT NULL column the field-level test agent
            // hits is registered). Idempotent via (entity_code,
            // field_code) uniq + INSERT IGNORE.
            try {
                Integer missing = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns c " +
                        " JOIN entity_registry e " +
                        "   ON CONVERT(e.table_name USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
                        "    = CONVERT(c.table_name USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
                        " LEFT JOIN entity_field_registry efr " +
                        "   ON efr.entity_code COLLATE utf8mb4_unicode_ci = e.code COLLATE utf8mb4_unicode_ci " +
                        "  AND efr.field_code  COLLATE utf8mb4_unicode_ci = CONVERT(c.column_name USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
                        " WHERE c.table_schema=DATABASE() " +
                        "   AND efr.field_code IS NULL " +
                        "   AND c.is_nullable='NO' AND c.column_default IS NULL " +
                        "   AND c.extra NOT LIKE '%auto_increment%' " +
                        "   AND c.extra NOT LIKE '%GENERATED%' " +
                        "   AND c.column_name NOT IN ('id','tenant_id','created_at','updated_at','created_by','updated_by')",
                        Integer.class);
                if (missing != null && missing > 0) {
                    log.info("{} NOT-NULL columns missing from registry; applying V75", missing);
                    try { runScript(c, "db/migration/V75__auto_register_all_missing_fields.sql"); }
                    catch (RuntimeException e) { log.warn("V75 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V75 probe failed: {}", probe.getMessage()); }

            // V73 — Sync entity_field_registry.required with INFORMATION_SCHEMA
            // nullability. Field-level test agent surfaced ~26 entities where
            // the registry said required=0 but the DB column is NOT NULL with
            // no default, breaking the no-code form's POST round-trip with
            // "Field X doesn't have a default value".
            try {
                Integer drift = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM entity_field_registry efr " +
                        "JOIN entity_registry er " +
                        "  ON er.code COLLATE utf8mb4_unicode_ci = efr.entity_code COLLATE utf8mb4_unicode_ci " +
                        "JOIN INFORMATION_SCHEMA.COLUMNS c " +
                        "  ON CONVERT(c.TABLE_SCHEMA USING utf8mb4) COLLATE utf8mb4_unicode_ci = DATABASE() " +
                        " AND CONVERT(c.TABLE_NAME   USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
                        "        = er.table_name COLLATE utf8mb4_unicode_ci " +
                        " AND CONVERT(c.COLUMN_NAME  USING utf8mb4) COLLATE utf8mb4_unicode_ci " +
                        "        = efr.field_code  COLLATE utf8mb4_unicode_ci " +
                        "WHERE c.IS_NULLABLE='NO' AND c.COLUMN_DEFAULT IS NULL " +
                        "  AND c.EXTRA NOT LIKE '%auto_increment%' " +
                        "  AND c.COLUMN_NAME NOT IN ('id','tenant_id','created_at','updated_at','created_by','updated_by') " +
                        "  AND efr.required=0", Integer.class);
                if (drift != null && drift > 0) {
                    log.info("entity_field_registry has {} required-flag drift rows; applying V73", drift);
                    try { runScript(c, "db/migration/V73__sync_entity_field_registry_required.sql"); }
                    catch (RuntimeException e) { log.warn("V73 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V73 probe failed: {}", probe.getMessage()); }

            // V91 — On-prem AI: apply once if the settings page is still at
            // version 1 (V84's initial body). Idempotent: the migration itself
            // only inserts version 2 when missing.
            try {
                Integer needs = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM page_definition p " +
                        "WHERE p.code='ai:tenant-credentials' " +
                        "  AND NOT EXISTS (SELECT 1 FROM page_version v " +
                        "                  WHERE v.page_id=p.id AND v.version=2)",
                        Integer.class);
                if (needs != null && needs > 0) {
                    log.info("ai:tenant-credentials still at V1 body; applying V91");
                    try { runScript(c, "db/migration/V91__tenant_ai_credential_openai_and_test.sql"); }
                    catch (RuntimeException e) { log.warn("V91 application failed: {}", e.getMessage()); }
                }
            } catch (Exception probe) { log.warn("V91 probe failed: {}", probe.getMessage()); }

            log.info("M0 page-schema bootstrap complete");
        } catch (SQLException e) {
            log.error("M0 page-schema bootstrap FAILED — controller endpoints will 500 until repaired", e);
        }
    }

    private boolean tableExists(JdbcTemplate jdbc, String tableName) {
        try {
            jdbc.queryForObject("SELECT COUNT(*) FROM " + tableName + " LIMIT 1", Integer.class);
            return true;
        } catch (Exception probe) {
            return false;
        }
    }

    private void runScript(Connection c, String resourcePath) {
        ClassPathResource res = new ClassPathResource(resourcePath);
        try (InputStream is = res.getInputStream()) {
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // ScriptUtils handles ';' splits + comment stripping; we feed
            // it via a temporary EncodedResource adapter inline.
            org.springframework.core.io.support.EncodedResource enc =
                    new org.springframework.core.io.support.EncodedResource(res, StandardCharsets.UTF_8);
            ScriptUtils.executeSqlScript(c, enc);
            // unused but kept for diagnostics if the script grows huge:
            if (sql.isBlank()) log.warn("Empty SQL resource: {}", resourcePath);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read SQL resource " + resourcePath, e);
        }
    }
}
