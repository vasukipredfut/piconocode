package com.aria.page.controller;

import com.aria.security.TenantContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Lookup: which workflow(s) reference a page in any of their transitions?
 *
 * Used by the WorkflowActionButton frontend primitive to auto-start a
 * workflow when the user submits a form on a "create" page without
 * an existing instance id.
 *
 * Endpoint:
 *   GET /api/m0/pages/{code}/workflow
 *     → { pageCode, workflowCode, allWorkflowCodes: [...] }
 *
 * Picks the FIRST matching workflow as primary. allWorkflowCodes lists
 * every workflow whose body_json.transitions[*].pageCode equals the
 * requested page code (a page can drive multiple workflow variants).
 */
@RestController
@RequestMapping("/api/m0/pages")
public class PageWorkflowLookupController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public PageWorkflowLookupController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/{code}/workflow")
    public Map<String, Object> getWorkflowsForPage(@PathVariable String code) {
        // Resolve the caller's own flows plus the inherited product catalogue.
        // This was pinned to `tenant_id=1`, which (a) served tenant 1's flows to
        // every firm and (b) stopped returning anything at all once the workflow
        // catalogue moved to -1 — silently breaking every WorkflowActionButton
        // that auto-looks-up its workflow, with no error anywhere.
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT code, body_json FROM workflow_definition " +
                "WHERE tenant_id IN (?, " + TenantContext.GLOBAL_TENANT_ID + ") " +
                "  AND status = 'PUBLISHED'",
                TenantContext.get());
        List<String> matches = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            String wfCode = (String) r.get("code");
            String body = (String) r.get("body_json");
            if (body == null) continue;
            try {
                JsonNode root = mapper.readTree(body);
                JsonNode transitions = root.get("transitions");
                if (transitions == null || !transitions.isArray()) continue;
                for (JsonNode t : transitions) {
                    JsonNode pc = t.get("pageCode");
                    if (pc != null && code.equals(pc.asText())) {
                        matches.add(wfCode);
                        break;
                    }
                }
            } catch (Exception ignored) { /* skip bad bodies */ }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("pageCode", code);
        out.put("workflowCode", matches.isEmpty() ? null : matches.get(0));
        out.put("allWorkflowCodes", matches);
        return out;
    }
}
