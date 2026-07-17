package com.aria.workflow.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Workflow Designer API — admin can change approval steps for any workflow
 * at any time, without redeploying.
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   GET    /api/m0/workflow-designer                            list every workflow + latest version
 *   GET    /api/m0/workflow-designer/{code}                     fetch full body_json
 *   POST   /api/m0/workflow-designer                            create blank workflow
 *   PUT    /api/m0/workflow-designer/{code}                     publish a new version
 *   POST   /api/m0/workflow-designer/{code}/states              add a state
 *   PUT    /api/m0/workflow-designer/{code}/states/{state}      rename / role-update / terminal toggle
 *   DELETE /api/m0/workflow-designer/{code}/states/{state}      remove a state + all transitions touching it
 *   POST   /api/m0/workflow-designer/{code}/transitions         add a transition
 *   PUT    /api/m0/workflow-designer/{code}/transitions/{idx}   update transition by 0-based index
 *   DELETE /api/m0/workflow-designer/{code}/transitions/{idx}   remove transition by index
 * </pre>
 *
 * <p>Every mutation publishes a new {@code workflow_definition} row (version+1)
 * so previous versions stay archived for audit.
 */
@RestController
@RequestMapping("/api/m0/workflow-designer")
public class WorkflowDesignerController {

    private static final Pattern SAFE_CODE = Pattern.compile("^[A-Za-z0-9._-]+$");
    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    public WorkflowDesignerController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ====================================================================
    // 1. List + load
    // ====================================================================

    @GetMapping
    public List<Map<String, Object>> list() {
        return jdbc.queryForList(
            "SELECT wd.code, MAX(wd.version) AS version, " +
            "       (SELECT status FROM workflow_definition WHERE code=wd.code ORDER BY version DESC LIMIT 1) AS status, " +
            "       (SELECT LENGTH(body_json) FROM workflow_definition WHERE code=wd.code ORDER BY version DESC LIMIT 1) AS body_size " +
            "FROM workflow_definition wd " +
            "GROUP BY wd.code ORDER BY wd.code");
    }

    @GetMapping("/{code}")
    public Map<String, Object> load(@PathVariable String code) {
        assertSafe(code);
        try {
            Map<String, Object> row = jdbc.queryForMap(
                "SELECT code, version, status, body_json FROM workflow_definition " +
                "WHERE code=? ORDER BY version DESC LIMIT 1", code);
            String body = row.get("body_json") == null ? "{}" : row.get("body_json").toString();
            try {
                row.put("body", json.readTree(body));
            } catch (Exception je) {
                row.put("body", null);
                row.put("bodyParseError", je.getMessage());
            }
            return row;
        } catch (Exception e) {
            return Map.of("error", "Workflow not found: " + code);
        }
    }

    // ====================================================================
    // 2. Create new (blank)
    // ====================================================================

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        String code = str(body.get("code"));
        assertSafe(code);
        ObjectNode wf = json.createObjectNode();
        wf.put("code", code);
        wf.put("version", 1);
        wf.put("initialState", "DRAFT");
        ArrayNode states = json.createArrayNode();
        states.add(json.createObjectNode().put("code", "DRAFT").put("assignedRole", "Admin"));
        states.add(json.createObjectNode().put("code", "APPROVED").put("terminal", true));
        states.add(json.createObjectNode().put("code", "REJECTED").put("terminal", true));
        wf.set("states", states);
        wf.set("transitions", json.createArrayNode());
        return publish(code, wf.toString(), "initial");
    }

    // ====================================================================
    // 3. Full-body publish (replaces — versions everything)
    // ====================================================================

    @PutMapping("/{code}")
    public Map<String, Object> putBody(@PathVariable String code, @RequestBody Map<String, Object> body) {
        assertSafe(code);
        try {
            String bodyJson = json.writeValueAsString(unwrapWorkflow(body));
            String rationale = str(body.getOrDefault("rationale", "via designer"));
            return publish(code, bodyJson, rationale);
        } catch (Exception e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    /**
     * Accepts either shape of PUT payload:
     * <pre>
     *   {"body": {"initialState":..., "states":[...]}, "rationale": "..."}   // wrapped (canonical, mirrors GET)
     *   {"initialState":..., "states":[...], "transitions":[...]}            // bare workflow at top level
     * </pre>
     * The bare shape is what the visual designer sends. Previously it fell through
     * {@code body.get("body")} -> null -> the literal string "null", which published
     * a bricked definition while reporting success.
     */
    private Object unwrapWorkflow(Map<String, Object> body) {
        if (body.get("body") != null) return body.get("body");
        Map<String, Object> bare = new LinkedHashMap<>(body);
        bare.remove("rationale");
        return bare;
    }

    // ====================================================================
    // 4. Granular mutations (state / transition CRUD)
    // ====================================================================

    @PostMapping("/{code}/states")
    public Map<String, Object> addState(@PathVariable String code, @RequestBody Map<String, Object> req) {
        return mutate(code, "add state " + req.get("state_code"), wf -> {
            ArrayNode states = (ArrayNode) wf.get("states");
            ObjectNode s = json.createObjectNode().put("code", req.get("state_code").toString());
            if (req.get("assignedRole") != null) s.put("assignedRole", req.get("assignedRole").toString());
            if (Boolean.TRUE.equals(req.get("terminal"))) s.put("terminal", true);
            states.add(s);
            return wf;
        });
    }

    @PutMapping("/{code}/states/{state}")
    public Map<String, Object> updateState(@PathVariable String code, @PathVariable String state,
                                            @RequestBody Map<String, Object> req) {
        return mutate(code, "update state " + state, wf -> {
            ArrayNode states = (ArrayNode) wf.get("states");
            for (JsonNode s : states) {
                if (state.equals(s.path("code").asText())) {
                    if (req.get("rename_to") != null) ((ObjectNode) s).put("code", req.get("rename_to").toString());
                    if (req.get("assignedRole") != null) ((ObjectNode) s).put("assignedRole", req.get("assignedRole").toString());
                    if (req.get("terminal") != null) ((ObjectNode) s).put("terminal", Boolean.TRUE.equals(req.get("terminal")));
                }
            }
            // rename also touches transitions
            if (req.get("rename_to") != null) {
                String to = req.get("rename_to").toString();
                ArrayNode trs = (ArrayNode) wf.get("transitions");
                for (JsonNode tr : trs) {
                    if (state.equals(tr.path("from").asText())) ((ObjectNode) tr).put("from", to);
                    if (state.equals(tr.path("to").asText()))   ((ObjectNode) tr).put("to", to);
                }
                if (state.equals(wf.path("initialState").asText())) ((ObjectNode) wf).put("initialState", to);
            }
            return wf;
        });
    }

    @DeleteMapping("/{code}/states/{state}")
    public Map<String, Object> deleteState(@PathVariable String code, @PathVariable String state) {
        return mutate(code, "remove state " + state, wf -> {
            ArrayNode states = (ArrayNode) wf.get("states");
            ArrayNode keep = json.createArrayNode();
            for (JsonNode s : states) {
                if (!state.equals(s.path("code").asText())) keep.add(s);
            }
            ((ObjectNode) wf).set("states", keep);
            // drop transitions referencing the deleted state
            ArrayNode trs = (ArrayNode) wf.get("transitions");
            ArrayNode keepTrs = json.createArrayNode();
            for (JsonNode tr : trs) {
                if (!state.equals(tr.path("from").asText()) && !state.equals(tr.path("to").asText())) {
                    keepTrs.add(tr);
                }
            }
            ((ObjectNode) wf).set("transitions", keepTrs);
            return wf;
        });
    }

    @PostMapping("/{code}/transitions")
    public Map<String, Object> addTransition(@PathVariable String code, @RequestBody Map<String, Object> req) {
        return mutate(code, "add transition " + req.get("from") + " -> " + req.get("to"), wf -> {
            ArrayNode trs = (ArrayNode) wf.get("transitions");
            ObjectNode t = json.createObjectNode();
            for (String k : new String[]{"from", "to", "trigger", "pageCode", "guard"}) {
                if (req.get(k) != null) t.put(k, req.get(k).toString());
            }
            trs.add(t);
            return wf;
        });
    }

    @PutMapping("/{code}/transitions/{idx}")
    public Map<String, Object> updateTransition(@PathVariable String code, @PathVariable int idx,
                                                  @RequestBody Map<String, Object> req) {
        return mutate(code, "update transition #" + idx, wf -> {
            ArrayNode trs = (ArrayNode) wf.get("transitions");
            if (idx < 0 || idx >= trs.size()) throw new IllegalArgumentException("Index out of bounds");
            ObjectNode t = (ObjectNode) trs.get(idx);
            for (String k : new String[]{"from", "to", "trigger", "pageCode", "guard"}) {
                if (req.containsKey(k)) {
                    if (req.get(k) == null || req.get(k).toString().isEmpty()) t.remove(k);
                    else t.put(k, req.get(k).toString());
                }
            }
            return wf;
        });
    }

    @DeleteMapping("/{code}/transitions/{idx}")
    public Map<String, Object> deleteTransition(@PathVariable String code, @PathVariable int idx) {
        return mutate(code, "remove transition #" + idx, wf -> {
            ArrayNode trs = (ArrayNode) wf.get("transitions");
            if (idx < 0 || idx >= trs.size()) throw new IllegalArgumentException("Index out of bounds");
            ArrayNode keep = json.createArrayNode();
            for (int i = 0; i < trs.size(); i++) if (i != idx) keep.add(trs.get(i));
            ((ObjectNode) wf).set("transitions", keep);
            return wf;
        });
    }

    // ====================================================================
    // Helpers
    // ====================================================================

    private interface BodyMutator {
        ObjectNode apply(ObjectNode wf) throws Exception;
    }

    private Map<String, Object> mutate(String code, String rationale, BodyMutator fn) {
        assertSafe(code);
        try {
            Map<String, Object> latest = jdbc.queryForMap(
                "SELECT body_json FROM workflow_definition WHERE code=? ORDER BY version DESC LIMIT 1", code);
            ObjectNode wf = (ObjectNode) json.readTree(latest.get("body_json").toString());
            ObjectNode out = fn.apply(wf);
            return publish(code, json.writeValueAsString(out), rationale);
        } catch (Exception e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    private Map<String, Object> publish(String code, String bodyJson, String rationale) {
        // Validate BEFORE touching any existing row. A malformed body must never be
        // able to supersede a working definition.
        try {
            assertPublishable(bodyJson);
        } catch (IllegalArgumentException bad) {
            return Map.of("ok", false, "error", "Refusing to publish: " + bad.getMessage());
        }
        try {
            Integer next = jdbc.queryForObject(
                "SELECT COALESCE(MAX(version),0) + 1 FROM workflow_definition WHERE code=?",
                Integer.class, code);
            // Insert FIRST, then demote the others. If the insert fails, nothing was
            // superseded and the prior version still serves. (The reverse order could
            // leave the workflow with zero PUBLISHED rows, which bricks startInstance.)
            jdbc.update(
                "INSERT INTO workflow_definition (tenant_id, code, version, status, body_json) " +
                "VALUES (1, ?, ?, 'PUBLISHED', ?)", code, next, bodyJson);
            jdbc.update(
                "UPDATE workflow_definition SET status='SUPERSEDED' " +
                "WHERE code=? AND status='PUBLISHED' AND version <> ?", code, next);
            return Map.of("ok", true, "code", code, "version", next, "rationale", rationale);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    /**
     * Structural gate for a workflow body. Rejects the shapes that the engine cannot
     * run — chiefly the literal "null" the visual designer used to publish, but also
     * bodies whose transitions point at states that were never declared.
     *
     * @throws IllegalArgumentException with an admin-readable reason
     */
    private void assertPublishable(String bodyJson) {
        JsonNode root;
        try {
            root = json.readTree(bodyJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("body is not valid JSON (" + e.getMessage() + ")");
        }
        if (root == null || root.isNull() || !root.isObject())
            throw new IllegalArgumentException("body must be a JSON object, got: " + bodyJson);

        JsonNode statesNode = root.get("states");
        if (statesNode == null || !statesNode.isArray() || statesNode.isEmpty())
            throw new IllegalArgumentException("body.states must be a non-empty array");

        Set<String> codes = new LinkedHashSet<>();
        for (JsonNode s : statesNode) {
            String c = s.path("code").asText(null);
            if (c == null || c.isBlank())
                throw new IllegalArgumentException("every state needs a non-blank code");
            codes.add(c);
        }

        String initial = root.path("initialState").asText(null);
        if (initial == null || initial.isBlank())
            throw new IllegalArgumentException("body.initialState is required");
        if (!codes.contains(initial))
            throw new IllegalArgumentException("initialState '" + initial + "' is not a declared state");

        JsonNode trans = root.get("transitions");
        if (trans != null && !trans.isNull()) {
            if (!trans.isArray())
                throw new IllegalArgumentException("body.transitions must be an array");
            for (JsonNode t : trans) {
                String from = t.path("from").asText(null);
                String to = t.path("to").asText(null);
                if (from == null || !codes.contains(from))
                    throw new IllegalArgumentException("transition.from '" + from + "' is not a declared state");
                if (to == null || !codes.contains(to))
                    throw new IllegalArgumentException("transition.to '" + to + "' is not a declared state");
            }
        }
    }

    private void assertSafe(String s) {
        if (s == null || !SAFE_CODE.matcher(s).matches())
            throw new IllegalArgumentException("Unsafe code: " + s);
    }
    private String str(Object o) { return o == null ? null : o.toString(); }
}
