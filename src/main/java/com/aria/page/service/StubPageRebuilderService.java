package com.aria.page.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Replaces "stub" workflow-step pages with real Inbox-style list bodies.
 *
 * <p>Many V1 seed pages (pico:mr-approve, pico:grn-qc, pico:dispatch-load-entry, …)
 * were stamped as a single-Section Markdown placeholder — "click here to approve".
 * That made them functionally useless: the user opened them and saw a paragraph,
 * not a list of pending records with action buttons.
 *
 * <p>This service runs on startup and, for every page that is BOTH (a) the
 * pageCode target of a workflow transition AND (b) still holds a markdown-only
 * body, generates a proper inbox body:
 *
 * <pre>
 *   { kind: "list",
 *     title: "<page> — Inbox",
 *     dataSource: { type: "entity", entity: "<mapped entity>" },
 *     columns:  [ #, status (colored badge) ],
 *     filters:  [ status default to from_state ],
 *     rowActions: [ for each transition.to → workflow.transition button ] }
 * </pre>
 *
 * <p>It publishes a new {@code page_version}, advances {@code current_version_id},
 * and evicts the Redis cache so the next render returns the rebuilt body. Safe
 * to run repeatedly — already-rebuilt pages are detected and skipped.
 */
@Component
public class StubPageRebuilderService {

    private static final Logger log = LoggerFactory.getLogger(StubPageRebuilderService.class);

    /** Workflow code → backing entity_registry code. */
    private static final Map<String, String> WF_TO_ENTITY = Map.<String, String>ofEntries(
        Map.entry("M5.MaterialRequisition",   "MaterialRequisition"),
        Map.entry("M5.InwardReceipt",         "GoodsReceiptNote"),
        Map.entry("M5.PurchaseOrder",         "PurchaseOrder"),
        Map.entry("M5.SalesOrder",            "SalesOrder"),
        Map.entry("M5.ProductionOrder",       "ProductionOrder"),
        Map.entry("M5.JobCard",               "JobCard"),
        Map.entry("M5.PackingRun",            "PackingRun"),
        Map.entry("M5.FGDispatch",            "FGDispatch"),
        Map.entry("M5.ReturnNote",            "ReturnNote"),
        Map.entry("M5.QualityTest",           "QualityTest"),
        Map.entry("M5.MaintenanceOrder",      "MaintenanceOrder"),
        Map.entry("M5.AmcContract",           "AmcContract"),
        Map.entry("M5.Subscription",          "Subscription"),
        Map.entry("M5.KycOnboarding",         "KycRecord"),
        Map.entry("M5.PlantTransfer",         "PlantTransfer"),
        Map.entry("M5.EndToEnd",              "GatePass"),
        Map.entry("M5.Rfq",                   "Rfq"),
        Map.entry("M5.JobWork",               "JobWorkIssue"),
        Map.entry("M5.AccidentInvestigation", "AccidentLog"),
        Map.entry("M10.TankerInbound",        "GatePass"),
        Map.entry("M10.TankerOutbound",       "GatePass"),
        Map.entry("M10.FGLoading",            "GatePass"),
        Map.entry("M10.IntTransferIn",        "GatePass"),
        Map.entry("M10.IntTransferOut",       "GatePass"),
        Map.entry("M10.PackLoading",          "GatePass"),
        Map.entry("M10.PackUnload",           "GatePass"),
        Map.entry("M10.ReturnStockIn",        "GatePass"),
        Map.entry("M10.ThirdPartyIn",         "GatePass"),
        Map.entry("M10.ThirdPartyOut",        "GatePass"),
        Map.entry("M10.EwayBill",             "EwayBill"),
        Map.entry("M6.PurchaseInvoice",       "PurchaseInvoice"),
        Map.entry("M6.SalesInvoice",          "SalesInvoice"),
        Map.entry("M6.GLPeriodClose",         "GlEntry"),
        Map.entry("M7.IncrementLetter",       "IncrementLetter"),
        Map.entry("M7.Appraisal",             "AppraisalForm"),
        Map.entry("M7.Hiring",                "JobApplication"),
        Map.entry("M7.Recruitment",           "Candidate"),
        Map.entry("M7.LeaveRequest",          "LeaveRequest"),
        Map.entry("M7.PayrollRun",            "PayrollRun"),
        Map.entry("M8.Lead",                  "Lead"),
        Map.entry("M8.Quote",                 "Quote"),
        Map.entry("M9.ServiceTicket",         "ServiceTicket")
    );

    private static final Set<String> POSITIVE_STATES = Set.of(
        "APPROVED","CLOSED","STORED","DELIVERED","ISSUED","RELEASED",
        "CEO_APPROVED","HR_APPROVED","MGR_APPROVED","RECEIVED","SUBMITTED",
        "GENERATED","ACCEPTED","ITEMS_LOGGED","QC_CHECKED","WON","CLOSED_WON",
        "RESOLVED","ONBOARDED","SHORTLISTED","SELECTED","CONDITIONAL_APPROVAL",
        "PROVISIONAL","PARTIAL_RECEIVED");
    private static final Set<String> NEGATIVE_STATES = Set.of(
        "REJECTED","CANCELLED","QC_REJECTED","LOST","CLOSED_LOST","WITHDRAWN");

    private static final Map<String, String> STATUS_COLOR = Map.ofEntries(
        Map.entry("DRAFT","grey"), Map.entry("SUBMITTED","blue"), Map.entry("APPROVED","green"),
        Map.entry("REJECTED","red"), Map.entry("CLOSED","grey"), Map.entry("ISSUED","amber"),
        Map.entry("STORED","green"), Map.entry("ITEMS_LOGGED","amber"), Map.entry("QC_CHECKED","amber"),
        Map.entry("QC_REJECTED","red"), Map.entry("DELIVERED","green"), Map.entry("HOLD_CREDIT","red"),
        Map.entry("PLANNED","grey"), Map.entry("IN_PROGRESS","blue"), Map.entry("ON_HOLD","amber"),
        Map.entry("CANCELLED","red"), Map.entry("ACTIVE","green"), Map.entry("RUNNING","blue"),
        Map.entry("CONDITIONAL_APPROVAL","amber"), Map.entry("RECEIVED","green"),
        Map.entry("MGR_APPROVED","blue"), Map.entry("HR_APPROVED","blue"), Map.entry("CEO_APPROVED","green"),
        Map.entry("RELEASED","green"), Map.entry("ACCEPTED","green"), Map.entry("GENERATED","green"),
        Map.entry("PROVISIONAL","amber"), Map.entry("WON","green"), Map.entry("LOST","red"),
        Map.entry("RESOLVED","green"), Map.entry("ONBOARDED","green"));

    @Autowired private JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    @PostConstruct
    public void rebuildStubsOnce() {
        try {
            int rebuilt = rebuild();
            if (rebuilt > 0) log.info("StubPageRebuilder — replaced {} markdown-only workflow-step pages with Inbox lists", rebuilt);
        } catch (Exception e) {
            log.warn("StubPageRebuilder failed: {}", e.getMessage());
        }
    }

    /** Find every (page_code, workflow, from_states, to_states) tuple via JSON_TABLE. */
    public int rebuild() {
        List<Map<String, Object>> transitions;
        try {
            transitions = jdbc.queryForList(
                "SELECT w.code AS wf, " +
                "       JSON_UNQUOTE(JSON_EXTRACT(t.value, '$.pageCode')) AS page_code, " +
                "       JSON_UNQUOTE(JSON_EXTRACT(t.value, '$.from'))     AS from_state, " +
                "       JSON_UNQUOTE(JSON_EXTRACT(t.value, '$.to'))       AS to_state " +
                "FROM workflow_definition w " +
                "JOIN JSON_TABLE(JSON_EXTRACT(w.body_json, '$.transitions'), " +
                "                '$[*]' COLUMNS(value JSON PATH '$')) t " +
                "WHERE w.status = 'PUBLISHED' " +
                "  AND JSON_EXTRACT(t.value, '$.pageCode') IS NOT NULL");
        } catch (Exception e) {
            log.debug("Stub probe skipped (JSON_TABLE unavailable?): {}", e.getMessage());
            return 0;
        }

        // Group transitions by page_code
        Map<String, PageInfo> byPage = new LinkedHashMap<>();
        for (Map<String, Object> r : transitions) {
            String code = (String) r.get("page_code");
            String wf   = (String) r.get("wf");
            String from = (String) r.get("from_state");
            String to   = (String) r.get("to_state");
            if (code == null || code.isBlank()) continue;
            PageInfo info = byPage.computeIfAbsent(code, k -> new PageInfo(wf));
            if (from != null && !"NULL".equals(from)) info.fromStates.add(from);
            if (to   != null && !"NULL".equals(to)   && !info.toStates.contains(to)) info.toStates.add(to);
        }

        int rebuilt = 0;
        for (Map.Entry<String, PageInfo> e : byPage.entrySet()) {
            String code = e.getKey();
            PageInfo info = e.getValue();
            if (rebuildIfStub(code, info)) rebuilt++;
        }
        return rebuilt;
    }

    private boolean rebuildIfStub(String code, PageInfo info) {
        try {
            Map<String, Object> row;
            try {
                row = jdbc.queryForMap(
                    "SELECT pd.id AS pd_id, pv.body_json " +
                    "FROM page_definition pd JOIN page_version pv ON pv.id = pd.current_version_id " +
                    "WHERE pd.code = ?", code);
            } catch (Exception notFound) { return false; }

            String existing = String.valueOf(row.get("body_json"));
            if (!looksLikeStub(existing)) return false;     // already real

            String entity = WF_TO_ENTITY.get(info.workflow);
            if (entity == null) {
                log.debug("StubPageRebuilder — no entity mapped for workflow {} (page {})", info.workflow, code);
                return false;
            }

            String body = buildInboxBody(code, info, entity);
            Long pageId = ((Number) row.get("pd_id")).longValue();
            Integer nextVersion = jdbc.queryForObject(
                "SELECT COALESCE(MAX(version),0)+1 FROM page_version WHERE page_id=?", Integer.class, pageId);
            jdbc.update(
                "INSERT INTO page_version (page_id, version, status, body_json, published_at) " +
                "VALUES (?, ?, 'PUBLISHED', ?, NOW())", pageId, nextVersion, body);
            jdbc.update(
                "UPDATE page_definition SET current_version_id = " +
                "(SELECT id FROM page_version WHERE page_id=? ORDER BY version DESC LIMIT 1) " +
                "WHERE id = ?", pageId, pageId);
            return true;
        } catch (Exception e) {
            log.warn("StubPageRebuilder couldn't rebuild {}: {}", code, e.getMessage());
            return false;
        }
    }

    /** Heuristic: a single Section with only Markdown children + length < 700. */
    private boolean looksLikeStub(String bodyJson) {
        if (bodyJson == null) return false;
        if (bodyJson.length() > 700) return false;
        try {
            JsonNode n = json.readTree(bodyJson);
            JsonNode layout = n.path("layout");
            if (!layout.isArray() || layout.size() != 1) return false;
            JsonNode section = layout.get(0);
            JsonNode children = section.path("children");
            JsonNode fields = section.path("fields");
            // markdown stub OR fields-with-FormField (legacy V1 — also worth rebuilding)
            if (children.isArray() && children.size() > 0) {
                for (JsonNode c : children) {
                    if (!"Markdown".equals(c.path("kind").asText())) return false;
                }
                return true;
            }
            if (fields.isArray() && fields.size() > 0) {
                // Treat as stub-shaped if every field is a plain text/number/select with no
                // dataSource — that's the legacy "click to approve" placeholder form.
                return bodyJson.contains("\"FormField\"") && !bodyJson.contains("\"dataSource\"");
            }
            return false;
        } catch (Exception e) { return false; }
    }

    private String buildInboxBody(String code, PageInfo info, String entity) throws Exception {
        ObjectNode body = json.createObjectNode();
        body.put("kind", "list");
        body.put("title", titleFromCode(code));

        ObjectNode ds = body.putObject("dataSource");
        ds.put("type", "entity");
        ds.put("entity", entity);
        // Server-side filter: only show records currently in the from_state(s).
        // Without this the Inbox showed every row regardless of status, so
        // the "Approve" button appeared next to already-approved records.
        if (!info.fromStates.isEmpty()) {
            ObjectNode filter = ds.putObject("filter");
            if (info.fromStates.size() == 1) {
                filter.put("field", "status");
                filter.put("op", "eq");
                filter.put("value", info.fromStates.iterator().next());
            } else {
                filter.put("field", "status");
                filter.put("op", "in");
                ArrayNode vals = filter.putArray("value");
                info.fromStates.forEach(vals::add);
            }
        }

        // Columns: # + Status badge + a few entity-specific extras when known
        ArrayNode cols = body.putArray("columns");
        ObjectNode idCol = cols.addObject();
        idCol.put("field", "id"); idCol.put("label", "#"); idCol.put("sortable", true);
        // Try to add 4-6 business-meaningful columns derived from entity_field_registry
        try {
            List<Map<String, Object>> fields = jdbc.queryForList(
                "SELECT field_code, display_label FROM entity_field_registry " +
                "WHERE entity_code = ? " +
                "  AND field_code NOT IN ('id','tenant_id','created_at','updated_at','status','current_step','workflow_instance_id','notes') " +
                "ORDER BY id LIMIT 6", entity);
            for (Map<String, Object> f : fields) {
                ObjectNode c = cols.addObject();
                c.put("field", String.valueOf(f.get("field_code")));
                c.put("label", String.valueOf(f.get("display_label")));
                c.put("sortable", true);
            }
        } catch (Exception ignored) {}
        ObjectNode statusCol = cols.addObject();
        statusCol.put("field", "status"); statusCol.put("label", "Status");
        statusCol.put("sortable", true); statusCol.put("render", "badge");
        ObjectNode colorMap = statusCol.putObject("colorMap");
        STATUS_COLOR.forEach(colorMap::put);

        // Optional UI status filter — use multi-select (the registered widget) so the
        // user can also narrow further client-side after the default server filter.
        if (!info.fromStates.isEmpty()) {
            ArrayNode filters = body.putArray("filters");
            ObjectNode f = filters.addObject();
            f.put("field", "status"); f.put("label", "Status"); f.put("type", "multi-select");
            ArrayNode opts = f.putArray("options");
            for (String s : info.fromStates) {
                ObjectNode o = opts.addObject(); o.put("value", s); o.put("label", s);
            }
        }

        // Row actions — one per next state
        ArrayNode rowActions = body.putArray("rowActions");
        int max = Math.min(3, info.toStates.size());
        for (int i = 0; i < max; i++) {
            String to = info.toStates.get(i);
            ObjectNode action = rowActions.addObject();
            action.put("label", humanise(to));
            action.put("action", "entity.update-field");
            ObjectNode args = action.putObject("args");
            args.put("field", "status");
            args.put("value", to);
            
            action.put("color",
                NEGATIVE_STATES.contains(to) ? "error" :
                POSITIVE_STATES.contains(to) ? "primary" : "secondary");
        }

        // Bulk approve/reject for manager-style pages
        if (code.contains("manager") || code.contains("approval")) {
            ArrayNode bulk = body.putArray("bulkActions");
            ObjectNode ba = bulk.addObject();
            ba.put("label", "Bulk Approve"); ba.put("action", "workflow.transition");
            ba.putObject("args").put("workflowCode", info.workflow).put("targetState", "APPROVED");
            ba.put("color", "primary");
            ObjectNode br = bulk.addObject();
            br.put("label", "Bulk Reject"); br.put("action", "workflow.transition");
            br.putObject("args").put("workflowCode", info.workflow).put("targetState", "REJECTED");
            br.put("color", "error");
        }

        body.put("pageSize", 25);
        return json.writeValueAsString(body);
    }

    private String titleFromCode(String code) {
        String slug = code.contains(":") ? code.substring(code.indexOf(':') + 1) : code;
        StringBuilder out = new StringBuilder();
        for (String part : slug.split("-")) {
            if (part.isEmpty()) continue;
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return out.toString().trim() + " — Inbox";
    }
    private String humanise(String s) {
        StringBuilder out = new StringBuilder();
        for (String part : s.split("_")) {
            if (part.isEmpty()) continue;
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase()).append(' ');
        }
        return out.toString().trim();
    }

    private static final class PageInfo {
        final String workflow;
        final Set<String> fromStates = new LinkedHashSet<>();
        final List<String> toStates  = new ArrayList<>();
        PageInfo(String wf) { this.workflow = wf; }
    }
}
