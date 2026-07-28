package com.aria.conveyor.controller;

import com.aria.conveyor.service.ConveyorLoadingBayService;
import com.aria.conveyor.service.DispatchTaskService;
import com.aria.conveyor.service.WorkflowIntegrationBindingService;
import com.aria.security.TenantContext;
import com.aria.workflow.dao.WorkflowDefinitionRepository;
import com.aria.workflow.model.WorkflowDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code GET /api/scanner/workflows/{workflowId}} — the single call a Raspberry Pi's
 * Python integration service makes to learn everything it needs to run a
 * conveyor line: the workflow definition, which Integration ID handles which
 * role at each step, loading bay config, the rule sets in force (metadata
 * only — rules are evaluated on the backend, never on the device, see
 * {@code com.aria.conveyor.service.RuleEvaluationService}), and the
 * currently active dispatch tasks. No hardcoded line-specific logic on the
 * device: everything here is data.
 *
 * <p>{@code workflowId} is {@link WorkflowDefinition#getCode()} (a string
 * like {@code "M12.ConveyorLine1"}), resolved via the same tenant-fork-first
 * / latest-PUBLISHED rule every other workflow lookup in this codebase uses
 * ({@link WorkflowDefinitionRepository#findLatestPublishedSingle}).
 */
@RestController
@RequestMapping("/api/scanner/workflows")
public class WorkflowAggregateController {

    private final WorkflowDefinitionRepository definitions;
    private final WorkflowIntegrationBindingService bindings;
    private final ConveyorLoadingBayService loadingBays;
    private final DispatchTaskService dispatchTasks;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public WorkflowAggregateController(WorkflowDefinitionRepository definitions,
                                        WorkflowIntegrationBindingService bindings,
                                        ConveyorLoadingBayService loadingBays,
                                        DispatchTaskService dispatchTasks,
                                        JdbcTemplate jdbc) {
        this.definitions = definitions;
        this.bindings = bindings;
        this.loadingBays = loadingBays;
        this.dispatchTasks = dispatchTasks;
        this.jdbc = jdbc;
    }

    @GetMapping("/{workflowId}")
    @Operation(summary = "Aggregate workflow config for a conveyor device: definition, integration bindings, "
            + "loading bays, rule set metadata, PLC integrations, and current dispatch tasks. Optional "
            + "?bayCode= scopes integrations/loadingBays/plcIntegrations/dispatchTasks to that bay only.")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String workflowId,
                                                    @RequestParam(required = false) String bayCode) {
        long tenantId = TenantContext.get();
        WorkflowDefinition def = definitions.findLatestPublishedSingle(tenantId, workflowId).orElse(null);
        if (def == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Unknown or unpublished workflow: " + workflowId));
        }

        Map<String, Object> body;
        try {
            body = mapper.readValue(def.getBodyJson(), Map.class);
        } catch (Exception e) {
            body = Map.of();
        }

        // bayCode is pushed down to the SQL WHERE clause in each service (not
        // fetched-then-filtered in Java) — a workflow with many bays/bindings/
        // tasks shouldn't pull every row just to discard most of them here.
        List<Map<String, Object>> allBindings = bindings.listForWorkflow(workflowId, bayCode);
        List<Map<String, Object>> plcBindings = allBindings.stream()
                .filter(b -> "PLC".equals(b.get("integration_role")))
                .toList();

        List<Map<String, Object>> bays = loadingBays.listForWorkflow(workflowId, bayCode);
        List<Map<String, Object>> tasks = dispatchTasks.listForWorkflow(workflowId, bayCode);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("workflowId", workflowId);
        out.put("workflowCode", def.getCode());
        out.put("workflowVersion", def.getVersion());
        out.put("workflowStatus", def.getStatus());
        out.put("workflowDetails", body);
        out.put("integrations", allBindings);
        out.put("loadingBays", bays);
        out.put("ruleBuilder", listRuleSets(workflowId));
        out.put("plcIntegrations", plcBindings);
        out.put("dispatchTasks", tasks);
        return ResponseEntity.ok(out);
    }

    /**
     * Rule set metadata only (code/name/status) — never the rule expressions
     * themselves. Rule evaluation happens exclusively on the backend
     * (POST /api/conveyor/scan); a device never runs rule logic, so there is
     * nothing for it to do with a raw SpEL expression except potentially leak
     * business logic.
     */
    private List<Map<String, Object>> listRuleSets(String workflowCode) {
        long tenantId = TenantContext.get();
        return jdbc.queryForList(
                "SELECT id, code, version, name, status FROM rule_set WHERE tenant_id = ? AND workflow_code = ? "
                        + "ORDER BY code, version DESC",
                tenantId, workflowCode);
    }
}
