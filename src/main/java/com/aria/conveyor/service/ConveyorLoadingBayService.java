package com.aria.conveyor.service;

import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Per-workflow conveyor bay config — NOT the M10 truck-dock LoadingPoint
 * master; a distinct concept for Pi-driven conveyor lines (see
 * V103__conveyor_loading_bay.sql).
 */
@Service
public class ConveyorLoadingBayService {

    private final JdbcTemplate jdbc;

    public ConveyorLoadingBayService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> listForWorkflow(String workflowCode) {
        long tenantId = TenantContext.get();
        return jdbc.queryForList(
                "SELECT bay_code, display_name, line_id, default_rule_set_id, active FROM conveyor_loading_bay "
                        + "WHERE tenant_id = ? AND workflow_code = ? ORDER BY bay_code",
                tenantId, workflowCode);
    }

    public Optional<Map<String, Object>> find(String workflowCode, String bayCode) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT bay_code, display_name, line_id, default_rule_set_id, active FROM conveyor_loading_bay "
                        + "WHERE tenant_id = ? AND workflow_code = ? AND bay_code = ?",
                tenantId, workflowCode, bayCode);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }
}
