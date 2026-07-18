package com.aria.workflow.controller;

import com.aria.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * WorkflowFlowController — turns a workflow definition into a human "progress
 * bar": the ordered main-line of steps, where the record currently sits, and
 * which page advances it next. Powers the flow tracker on the gate-pass detail
 * so an operator never has to guess which page comes after the one they're on.
 *
 * Generic: works for any workflow code + state, not just gate passes.
 */
@RestController
@RequestMapping("/api/workflow")
public class WorkflowFlowController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    public WorkflowFlowController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/flow/{code}")
    @Operation(summary = "Ordered main-line steps for a workflow + where `state` sits + the next page(s).")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> flow(
            @PathVariable String code,
            @RequestParam(required = false) String state) {

        // Latest definition for this tenant (tenant row wins over the -1 product row).
        List<String> bodies = jdbc.query(
            "SELECT body_json FROM workflow_definition " +
            "WHERE code = ? AND tenant_id IN (?, " + TenantContext.GLOBAL_TENANT_ID + ") " +
            "ORDER BY tenant_id DESC, version DESC LIMIT 1",
            (rs, i) -> rs.getString(1), code, TenantContext.get());
        if (bodies.isEmpty()) {
            return ResponseEntity.ok(Map.of("code", code, "steps", List.of(), "currentIndex", -1, "next", List.of()));
        }

        Map<String, Object> body;
        try { body = json.readValue(bodies.get(0), Map.class); }
        catch (Exception e) { return ResponseEntity.ok(Map.of("code", code, "steps", List.of(), "currentIndex", -1, "next", List.of())); }

        String initial = String.valueOf(body.getOrDefault("initialState", ""));
        List<Map<String, Object>> transitions = new ArrayList<>();
        Object rawT = body.get("transitions");
        if (rawT instanceof List<?> list) {
            for (Object o : list) if (o instanceof Map<?, ?> m) transitions.add((Map<String, Object>) m);
        }

        // Adjacency: from -> [{to, pageCode}]. Skip self-loops.
        Map<String, List<Map<String, String>>> adj = new LinkedHashMap<>();
        for (Map<String, Object> t : transitions) {
            String from = str(t.get("from")), to = str(t.get("to"));
            if (from.isEmpty() || to.isEmpty() || from.equals(to)) continue;
            adj.computeIfAbsent(from, k -> new ArrayList<>())
               .add(Map.of("to", to, "pageCode", str(t.get("pageCode"))));
        }

        // Main line = the happy path: shortest route from the initial state to the
        // success terminal. Exception detours (QC_REJECTED, *_PENDING_MANAGER,
        // REJECTED_BY_*) are longer/side branches and fall off this path.
        List<String> mainline = happyPath(initial, adj);

        // Friendly page titles for every pageCode we might return.
        Map<String, String> titles = pageTitles(mainline, adj);

        // Steps: each main-line state, labelled, with the page that ADVANCES it.
        List<Map<String, Object>> steps = new ArrayList<>();
        for (int i = 0; i < mainline.size(); i++) {
            String s = mainline.get(i);
            String nextPage = "";
            List<Map<String, String>> outs = adj.getOrDefault(s, List.of());
            for (Map<String, String> e : outs) {
                if (i + 1 < mainline.size() && mainline.get(i + 1).equals(e.get("to"))) { nextPage = e.get("pageCode"); break; }
            }
            steps.add(new LinkedHashMap<>(Map.of(
                "state", s,
                "label", humanize(s),
                "pageCode", nextPage,
                "pageTitle", titles.getOrDefault(nextPage, ""))));
        }

        int currentIndex = state == null ? -1 : mainline.indexOf(state);

        // Next: the transitions leaving the current state (may branch: approve/reject).
        List<Map<String, Object>> next = new ArrayList<>();
        if (state != null) {
            for (Map<String, String> e : adj.getOrDefault(state, List.of())) {
                String pc = e.get("pageCode");
                next.add(new LinkedHashMap<>(Map.of(
                    "to", e.get("to"),
                    "label", humanize(e.get("to")),
                    "pageCode", pc,
                    "pageTitle", titles.getOrDefault(pc, ""))));
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("currentState", state);
        out.put("currentIndex", currentIndex);
        out.put("steps", steps);
        out.put("next", next);
        return ResponseEntity.ok(out);
    }

    /**
     * The happy path: shortest route from `start` to the success terminal. The
     * success terminal is a no-outgoing state named like CLOSED/COMPLETE/DONE,
     * else the terminal furthest from start (the end of the longest normal run).
     * Shortest-path then naturally skips the manager-approval side loops.
     */
    private List<String> happyPath(String start, Map<String, List<Map<String, String>>> adj) {
        if (start == null || start.isEmpty()) return List.of();
        Set<String> all = new HashSet<>(adj.keySet());
        for (List<Map<String, String>> outs : adj.values()) for (Map<String, String> e : outs) all.add(e.get("to"));
        List<String> terminals = new ArrayList<>();
        for (String s : all) if (adj.getOrDefault(s, List.of()).isEmpty()) terminals.add(s);
        if (terminals.isEmpty()) return List.of(start);

        String target = null;
        for (String t : terminals) if (t.matches("(?i).*(CLOSED|COMPLETE|DONE|FINISH).*")) { target = t; break; }
        if (target == null) {
            int far = -1;
            for (String t : terminals) {
                List<String> p = bfsPath(start, t, adj);
                if (p.size() > far) { far = p.size(); target = t; }
            }
        }
        List<String> path = bfsPath(start, target, adj);
        return path.isEmpty() ? List.of(start) : path;
    }

    /** Shortest path start -> target via BFS (edges unweighted). */
    private List<String> bfsPath(String start, String target, Map<String, List<Map<String, String>>> adj) {
        Map<String, String> parent = new HashMap<>();
        Deque<String> q = new ArrayDeque<>();
        q.add(start); parent.put(start, null);
        while (!q.isEmpty()) {
            String cur = q.poll();
            if (cur.equals(target)) break;
            for (Map<String, String> e : adj.getOrDefault(cur, List.of())) {
                String to = e.get("to");
                if (!parent.containsKey(to)) { parent.put(to, cur); q.add(to); }
            }
        }
        if (!parent.containsKey(target)) return List.of();
        LinkedList<String> path = new LinkedList<>();
        for (String s = target; s != null; s = parent.get(s)) path.addFirst(s);
        return path;
    }

    private Map<String, String> pageTitles(List<String> mainline, Map<String, List<Map<String, String>>> adj) {
        Set<String> codes = new HashSet<>();
        for (List<Map<String, String>> outs : adj.values())
            for (Map<String, String> e : outs) if (!e.get("pageCode").isEmpty()) codes.add(e.get("pageCode"));
        Map<String, String> titles = new HashMap<>();
        if (codes.isEmpty()) return titles;
        String in = String.join(",", Collections.nCopies(codes.size(), "?"));
        List<Object> args = new ArrayList<>(codes);
        args.add(TenantContext.get());
        jdbc.query(
            "SELECT code, title FROM ( " +
            "  SELECT code, title, ROW_NUMBER() OVER (PARTITION BY code ORDER BY tenant_id DESC) rn " +
            "  FROM page_definition WHERE code IN (" + in + ") AND tenant_id IN (?, " + TenantContext.GLOBAL_TENANT_ID + ") " +
            ") x WHERE rn = 1",
            rs -> { titles.put(rs.getString(1), rs.getString(2)); }, args.toArray());
        return titles;
    }

    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }

    private static final Set<String> ACRONYMS = Set.of("QC", "FG", "RM", "PO", "GP", "W1", "W2", "W3", "QTY");

    /** GATE_PASS_CREATED -> "Gate Pass Created"; QC_APPROVED -> "QC Approved". */
    private static String humanize(String code) {
        if (code == null || code.isEmpty()) return "";
        String[] parts = code.replace('.', '_').split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            if (ACRONYMS.contains(p.toUpperCase())) sb.append(p.toUpperCase());
            else sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
