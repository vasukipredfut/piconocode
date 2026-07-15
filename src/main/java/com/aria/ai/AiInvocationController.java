package com.aria.ai;

import com.aria.security.TenantContext;
import com.aria.service.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP entry point for AI agent invocation from the browser.
 *
 * <p>Sanctioned substrate controller — lives under {@code com.aria.ai}
 * because it's the platform's own AI substrate entry point (allowed per
 * {@code docs/architecture/CODE-REVIEW-develope-2026-07-11.md} substrate-
 * scope rule), NOT a bespoke per-customer controller.
 *
 * <p>Contract:
 * <pre>
 *   POST /api/m0/ai/agents/{code}/invoke
 *   Body: { inputs: {...}, workflowInstanceId?: number }
 *   → { runId, ok, decision: { action, actionArgs, reason, confidence, telemetry } }
 * </pre>
 */
@RestController
@RequestMapping("/api/m0/ai")
public class AiInvocationController {

    private static final Logger log = LoggerFactory.getLogger(AiInvocationController.class);

    private final AiAgentDispatcher dispatcher;
    private final JdbcTemplate jdbc;

    public AiInvocationController(AiAgentDispatcher dispatcher, JdbcTemplate jdbc) {
        this.dispatcher = dispatcher;
        this.jdbc = jdbc;
    }

    @PostMapping("/agents/{code}/invoke")
    public ResponseEntity<Map<String, Object>> invoke(@PathVariable String code,
                                                      @RequestBody Map<String, Object> body) {
        Map<String, Object> inputs = safeMap(body.get("inputs"));
        Long workflowInstanceId = asLong(body.get("workflowInstanceId"));
        Long operatorId = currentUserId();
        String triggeredBy = "operator." + (operatorId == null ? "?" : operatorId);

        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "error", "no tenant context"));
        }

        try {
            Long runId = dispatcher.invoke(code, inputs, workflowInstanceId, operatorId, triggeredBy);
            if (runId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("ok", false,
                                     "error", "agent not registered / not published / kill-switch on"));
            }

            Map<String, Object> run = jdbc.queryForMap(
                    "SELECT id, outputs_json, action_taken, mode_at_run, error " +
                    "FROM ai_agent_run WHERE id=?", runId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("ok", true);
            response.put("runId", runId);
            response.put("actionTaken", run.get("action_taken"));
            response.put("mode", run.get("mode_at_run"));
            response.put("outputs", run.get("outputs_json"));
            if (run.get("error") != null) response.put("error", run.get("error"));
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            log.error("agent invoke failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                    .body(Map.of("ok", false, "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        }
    }

    // ---- private helpers ---------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeMap(Object o) {
        if (o instanceof Map) return (Map<String, Object>) o;
        return Map.of();
    }

    private Long asLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return null; }
    }

    private Long currentUserId() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a == null || !(a.getPrincipal() instanceof UserDetailsImpl u)) return null;
            return u.getId();
        } catch (Exception e) {
            return null;
        }
    }
}
