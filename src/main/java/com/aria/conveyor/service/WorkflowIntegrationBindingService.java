package com.aria.conveyor.service;

import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves which Integration ID handles a given role (BARCODE_SCANNER, PLC,
 * MQTT_BROKER, ...) at a workflow step — the data-driven mechanism that lets
 * a Raspberry Pi run with zero hardcoded line-specific logic.
 */
@Service
public class WorkflowIntegrationBindingService {

    private final JdbcTemplate jdbc;

    public WorkflowIntegrationBindingService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * A bay-specific binding wins over the workflow-wide default (bay_code='').
     * bayCode may be null/blank, meaning "no specific bay" — only the default matches then.
     */
    public Optional<Long> resolve(String workflowCode, String stepCode, String integrationRole, String bayCode) {
        long tenantId = TenantContext.get();
        String bay = bayCode == null ? "" : bayCode;
        List<Long> rows = jdbc.queryForList(
                "SELECT integration_id FROM workflow_integration_binding WHERE tenant_id = ? AND workflow_code = ? "
                        + "AND step_code = ? AND integration_role = ? AND bay_code IN (?, '') ORDER BY bay_code DESC LIMIT 1",
                Long.class, tenantId, workflowCode, stepCode, integrationRole, bay);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.get(0));
    }

    /** Every binding configured for a workflow (used to build the aggregate GET response). */
    public List<Map<String, Object>> listForWorkflow(String workflowCode) {
        return listForWorkflow(workflowCode, null);
    }

    /**
     * Same, optionally scoped to one bay at the SQL level (not fetch-then-filter)
     * — a workflow with many bays/bindings shouldn't pull every row just to
     * discard most of them in Java when the caller only ever wanted one bay.
     */
    public List<Map<String, Object>> listForWorkflow(String workflowCode, String bayCode) {
        long tenantId = TenantContext.get();
        if (bayCode == null) {
            return jdbc.queryForList(
                    "SELECT step_code, bay_code, integration_role, integration_id, config_overrides_json "
                            + "FROM workflow_integration_binding WHERE tenant_id = ? AND workflow_code = ? "
                            + "ORDER BY step_code, bay_code, integration_role",
                    tenantId, workflowCode);
        }
        return jdbc.queryForList(
                "SELECT step_code, bay_code, integration_role, integration_id, config_overrides_json "
                        + "FROM workflow_integration_binding WHERE tenant_id = ? AND workflow_code = ? AND bay_code = ? "
                        + "ORDER BY step_code, bay_code, integration_role",
                tenantId, workflowCode, bayCode);
    }
}
