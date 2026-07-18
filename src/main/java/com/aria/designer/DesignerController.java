package com.aria.designer;

import com.aria.page.service.PageRendererService;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

/**
 * No-code Page Designer API.
 *
 * Lets an admin add new entities / fields / pages from the UI without writing
 * SQL or Java. Every mutation goes through this controller so we can apply
 * the corresponding DDL atomically with the registry update.
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   GET    /api/m0/designer/entities                    list every entity_registry row
 *   POST   /api/m0/designer/entities                    create entity_registry + CREATE TABLE
 *   GET    /api/m0/designer/entities/{code}/fields      every entity_field_registry row
 *   POST   /api/m0/designer/entities/{code}/fields      add field + ALTER TABLE ADD COLUMN
 *   PUT    /api/m0/designer/entities/{code}/fields/{fc} update display label/widget/options
 *   DELETE /api/m0/designer/entities/{code}/fields/{fc} remove from registry (keeps column)
 *   GET    /api/m0/designer/pages                       list page_definition rows (optional ?entity=)
 *   POST   /api/m0/designer/pages                       create page_definition with auto body
 *   PUT    /api/m0/designer/pages/{code}                update page body_json / title / category
 *   DELETE /api/m0/designer/pages/{code}                soft-delete (mark inactive)
 * </pre>
 *
 * <p>SQL safety: every identifier is matched against {@code [A-Za-z0-9_]+}
 * before being concatenated into any DDL or DML.
 */
@RestController
@RequestMapping("/api/m0/designer")
public class DesignerController {

    private static final Logger log = LoggerFactory.getLogger(DesignerController.class);
    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");
    private final JdbcTemplate jdbc;
    private final PageRendererService renderer;

    public DesignerController(JdbcTemplate jdbc, PageRendererService renderer) {
        this.jdbc = jdbc;
        this.renderer = renderer;
    }

    /**
     * Resolve the page this tenant may WRITE to, forking the global row first if
     * they have not customised it yet. Returns null when no such page exists.
     *
     * <h3>Why this exists</h3>
     * Every write here used to be {@code WHERE code = ?} with no tenant filter.
     * The product catalogue lives at tenant_id = -1 and is the ONLY row for most
     * codes, so that predicate resolved to the global row and happily wrote to
     * it. One tenant editing "aria:vendor-list" in the designer published their
     * body onto the product page — changing that page for every other firm, and
     * returning {@code {"ok":true}} while doing it. Nothing failed, so nothing
     * surfaced; the only symptom would have been another customer's list quietly
     * looking wrong.
     *
     * <p>This is exactly the case the substrate's read side already models:
     * resolution is {@code tenant_id IN (tid, -1) ORDER BY tenant_id DESC},
     * so a tenant row shadows the global one. The write side simply never
     * created that tenant row. Customising a global page now COPIES it —
     * definition + current body — into a row owned by the caller, and every
     * subsequent write lands on the copy. The product is immutable from here.
     */
    private Long resolveWritablePageId(String code) {
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> own = jdbc.queryForList(
                "SELECT id FROM page_definition WHERE code=? AND tenant_id=?", code, tenantId);
        if (!own.isEmpty()) return ((Number) own.get(0).get("id")).longValue();

        List<Map<String, Object>> global = jdbc.queryForList(
                "SELECT id, title, category, description, status FROM page_definition " +
                "WHERE code=? AND tenant_id=?", code, TenantContext.GLOBAL_TENANT_ID);
        if (global.isEmpty()) return null;

        Map<String, Object> g = global.get(0);
        Long globalId = ((Number) g.get("id")).longValue();
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO page_definition (tenant_id, code, title, category, description, status) " +
                    "VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, tenantId);
            ps.setString(2, code);
            ps.setString(3, (String) g.get("title"));
            ps.setString(4, (String) g.get("category"));
            ps.setString(5, (String) g.get("description"));
            ps.setString(6, (String) g.get("status"));
            return ps;
        }, kh);
        Long forkedId = kh.getKey().longValue();

        // Carry the product's current body across, so a customisation starts from
        // what the tenant can actually see rather than from an empty page.
        jdbc.update(
                "INSERT INTO page_version (page_id, version, body_json, status, published_at) " +
                "SELECT ?, 1, pv.body_json, 'PUBLISHED', NOW() FROM page_version pv " +
                " WHERE pv.page_id=? AND pv.status='PUBLISHED' ORDER BY pv.version DESC LIMIT 1",
                forkedId, globalId);
        jdbc.update("UPDATE page_definition pd SET current_version_id = " +
                "(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1) " +
                "WHERE pd.id=?", forkedId);
        log.info("Tenant {} customised global page {} — forked to page_definition id {}",
                tenantId, code, forkedId);
        return forkedId;
    }

    /**
     * Resolve the entity_field_registry row this tenant may WRITE to, forking the
     * global one first if they have not customised it yet. Null when neither a
     * tenant nor a global row exists.
     *
     * <p>Same defect as {@link #resolveWritablePageId}, on the field registry:
     * {@code WHERE entity_code=? AND field_code=?} with no tenant filter resolved
     * to the -1 product row, so one tenant relabelling a field relabelled it for
     * every firm, and one tenant deleting a field DELETED IT FROM THE PRODUCT.
     *
     * <p>The schema already anticipated this and nothing used it: the unique key
     * is {@code (tenant_id, entity_code, field_code)}, so a tenant row is designed
     * to coexist with the global row and shadow it, and {@code hidden} exists to
     * tombstone a global field the tenant does not want.
     */
    private Long resolveWritableFieldId(String entityCode, String fieldCode) {
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> own = jdbc.queryForList(
                "SELECT id FROM entity_field_registry " +
                "WHERE entity_code=? AND field_code=? AND tenant_id=?", entityCode, fieldCode, tenantId);
        if (!own.isEmpty()) return ((Number) own.get(0).get("id")).longValue();

        // Copy the product's row, verbatim, into one this tenant owns.
        int n = jdbc.update(
                "INSERT INTO entity_field_registry " +
                "  (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, " +
                "   searchable, required, default_value, options_source_json, relation_entity, widget_kind, " +
                "   options_json, visible_when, required_when, computed_from, hidden) " +
                "SELECT ?, entity_code, field_code, display_label, data_type, filterable, sortable, " +
                "   searchable, required, default_value, options_source_json, relation_entity, widget_kind, " +
                "   options_json, visible_when, required_when, computed_from, hidden " +
                "  FROM entity_field_registry WHERE entity_code=? AND field_code=? AND tenant_id=?",
                tenantId, entityCode, fieldCode, TenantContext.GLOBAL_TENANT_ID);
        if (n == 0) return null;
        log.info("Tenant {} customised global field {}.{} — forked a tenant copy", tenantId, entityCode, fieldCode);
        return jdbc.queryForObject(
                "SELECT id FROM entity_field_registry WHERE entity_code=? AND field_code=? AND tenant_id=?",
                Long.class, entityCode, fieldCode, tenantId);
    }

    /**
     * Publish a body as the page's single live version.
     *
     * <p>Callers MUST pass a pageId from {@link #resolveWritablePageId} — this
     * method does not check ownership, and publishing onto a -1 page rewrites the
     * product for every tenant.
     *
     * <p>Two invariants this restores, both of which the raw inserts here used to break:
     * <ul>
     *   <li><b>Exactly one PUBLISHED row per page.</b> Prior versions are demoted to
     *       ARCHIVED. Leaving several PUBLISHED rows corrupts every SQL migration that
     *       targets {@code WHERE status='PUBLISHED'} — it silently updates all of them.</li>
     *   <li><b>The Redis body cache is evicted.</b> {@code m0:page:*} has a 1h TTL and is
     *       only invalidated explicitly, so without this a freshly designed page serves
     *       its stale predecessor for up to an hour.</li>
     * </ul>
     */
    private void publishBody(Long pageId, String code, String bodyJson) {
        jdbc.update(
            "INSERT INTO page_version (page_id, version, body_json, status, published_at) " +
            "VALUES (?, COALESCE((SELECT MAX(version)+1 FROM page_version pv WHERE pv.page_id=?), 1), ?, 'PUBLISHED', NOW())",
            pageId, pageId, bodyJson);
        Long newVersionId = jdbc.queryForObject(
            "SELECT id FROM page_version WHERE page_id=? ORDER BY version DESC LIMIT 1", Long.class, pageId);
        // Demote every other published row for this page.
        jdbc.update(
            "UPDATE page_version SET status='ARCHIVED' " +
            "WHERE page_id=? AND status='PUBLISHED' AND id <> ?", pageId, newVersionId);
        jdbc.update("UPDATE page_definition SET current_version_id=? WHERE id=?", newVersionId, pageId);
        renderer.evict(TenantContext.get(), code);
    }

    // ===== Entities =========================================================

    @GetMapping("/entities")
    public List<Map<String, Object>> listEntities() {
        return jdbc.queryForList(
                "SELECT code, display_name, table_name FROM entity_registry ORDER BY code");
    }

    @PostMapping("/entities")
    public Map<String, Object> createEntity(@RequestBody Map<String, Object> body) {
        String code      = str(body.get("code"));
        String name      = strOr(body.get("display_name"), code);
        String table     = str(body.get("table_name"));
        assertSafe(code);
        assertSafe(table);
        try {
            // Create the underlying table (id PK + tenant_id only — fields added via /fields POST)
            jdbc.execute(
                "CREATE TABLE IF NOT EXISTS " + table + " (" +
                "  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                "  tenant_id BIGINT NOT NULL DEFAULT 1," +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4"
            );
            jdbc.update(
                "INSERT IGNORE INTO entity_registry (code, display_name, table_name) VALUES (?, ?, ?)",
                code, name, table);
            return Map.of("ok", true, "entity", code, "table", table);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    // ===== Fields ===========================================================

    @GetMapping("/entities/{code}/fields")
    public List<Map<String, Object>> listFields(@PathVariable String code) {
        assertSafe(code);
        try {
            return jdbc.queryForList(
                    "SELECT id, field_code, display_label, data_type, " +
                            "filterable, sortable, searchable, required, " +
                            "widget_kind, options_json " +
                            "FROM entity_field_registry WHERE entity_code=? ORDER BY id", code);
        } catch (Exception e) {
            return jdbc.queryForList(
                    "SELECT id, field_code, display_label, data_type, " +
                            "filterable, sortable, searchable, required " +
                            "FROM entity_field_registry WHERE entity_code=? ORDER BY id", code);
        }
    }

    @PostMapping("/entities/{code}/fields")
    public Map<String, Object> addField(@PathVariable String code,
                                         @RequestBody Map<String, Object> body) {
        assertSafe(code);
        String fc        = str(body.get("field_code"));
        String label     = strOr(body.get("display_label"), fc);
        String dataType  = strOr(body.get("data_type"), "STRING").toUpperCase();
        String widget    = strOr(body.get("widget_kind"), "text");
        Object opts      = body.get("options_json");
        boolean req      = bool(body.get("required"));
        boolean filt     = boolOr(body.get("filterable"), true);
        boolean sort     = boolOr(body.get("sortable"), true);
        boolean search   = boolOr(body.get("searchable"), dataType.equals("STRING") || dataType.equals("TEXT"));

        assertSafe(fc);

        // Look up the table for this entity
        Map<String, Object> reg;
        try {
            reg = jdbc.queryForMap("SELECT table_name FROM entity_registry WHERE code=?", code);
        } catch (Exception e) {
            return Map.of("ok", false, "error", "Unknown entity: " + code);
        }
        String table = (String) reg.get("table_name");
        assertSafe(table);

        // Add the column on the underlying table if missing
        if (!columnExists(table, fc)) {
            String colType = sqlTypeFor(dataType);
            try {
                jdbc.execute("ALTER TABLE " + table + " ADD COLUMN " + fc + " " + colType);
            } catch (Exception ddl) {
                return Map.of("ok", false, "error", "DDL failed: " + ddl.getMessage());
            }
        }

        // Rule expressions produced by the RuleBuilder UI on the frontend.
        // Null / blank means "no rule" — field is always visible / never
        // conditionally required / not computed.
        String visibleWhen  = trimOrNull(body.get("visible_when"));
        String requiredWhen = trimOrNull(body.get("required_when"));
        String computedFrom = trimOrNull(body.get("computed_from"));

        // Insert / upsert registry row
        try {
            jdbc.update(
                "INSERT INTO entity_field_registry " +
                "(entity_code, field_code, display_label, data_type, " +
                " filterable, sortable, searchable, required, widget_kind, options_json, " +
                " visible_when, required_when, computed_from) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                " display_label=VALUES(display_label), data_type=VALUES(data_type), " +
                " filterable=VALUES(filterable), sortable=VALUES(sortable), " +
                " searchable=VALUES(searchable), required=VALUES(required), " +
                " widget_kind=VALUES(widget_kind), options_json=VALUES(options_json), " +
                " visible_when=VALUES(visible_when), required_when=VALUES(required_when), " +
                " computed_from=VALUES(computed_from)",
                code, fc, label, dataType, filt, sort, search, req, widget,
                opts == null ? null : opts.toString(),
                visibleWhen, requiredWhen, computedFrom);
            return Map.of("ok", true, "entity", code, "field", fc, "table", table);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    @PutMapping("/entities/{code}/fields/{fc}")
    public Map<String, Object> updateField(@PathVariable String code,
                                            @PathVariable String fc,
                                            @RequestBody Map<String, Object> body) {
        assertSafe(code);
        assertSafe(fc);

        Map<String, Object> patch = new LinkedHashMap<>();
        if (body.containsKey("display_label")) patch.put("display_label", str(body.get("display_label")));
        if (body.containsKey("widget_kind"))   patch.put("widget_kind",   str(body.get("widget_kind")));
        if (body.containsKey("options_json"))  patch.put("options_json",  body.get("options_json") == null ? null : body.get("options_json").toString());
        if (body.containsKey("required"))      patch.put("required",      bool(body.get("required")));
        if (body.containsKey("filterable"))    patch.put("filterable",    bool(body.get("filterable")));
        if (body.containsKey("sortable"))      patch.put("sortable",      bool(body.get("sortable")));
        if (body.containsKey("searchable"))    patch.put("searchable",    bool(body.get("searchable")));
        // Rule expressions — the UI sends null to clear a rule.
        if (body.containsKey("visible_when"))  patch.put("visible_when",  trimOrNull(body.get("visible_when")));
        if (body.containsKey("required_when")) patch.put("required_when", trimOrNull(body.get("required_when")));
        if (body.containsKey("computed_from")) patch.put("computed_from", trimOrNull(body.get("computed_from")));
        if (patch.isEmpty()) return Map.of("ok", false, "error", "no updatable fields");

        StringBuilder sets = new StringBuilder();
        List<Object> args = new ArrayList<>();
        for (Map.Entry<String, Object> e : patch.entrySet()) {
            if (sets.length() > 0) sets.append(", ");
            sets.append(e.getKey()).append("=?");
            args.add(e.getValue());
        }
        try {
            // Fork-on-write: customise a COPY, never the -1 product row.
            Long fieldId = resolveWritableFieldId(code, fc);
            if (fieldId == null) return Map.of("ok", false, "error", "No such field: " + code + "." + fc);
            args.add(fieldId);
            args.add(TenantContext.get());
            int n = jdbc.update("UPDATE entity_field_registry SET " + sets +
                                " WHERE id=? AND tenant_id=?", args.toArray());
            return Map.of("ok", n > 0, "updated", n, "entity", code, "field", fc);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    /**
     * Remove a field FOR THIS TENANT.
     *
     * <p>Two different removals hide behind one button:
     * <ul>
     *   <li><b>Their own field</b> — a row they added. Really delete it.</li>
     *   <li><b>A product field</b> — shared at -1 with every other firm. You cannot
     *       delete a shared row for one owner, so this <b>tombstones</b>: fork a
     *       tenant copy and set {@code hidden=1}. The field vanishes from their
     *       forms and lists; every other tenant still has it.</li>
     * </ul>
     *
     * <p>This ran {@code DELETE FROM entity_field_registry WHERE entity_code=? AND
     * field_code=?} — unscoped. One tenant removing a field they did not want
     * deleted that field from the PRODUCT, for every customer, and reported
     * {@code "ok": true}. The read path already filters {@code hidden=0} and
     * prefers the tenant row, so the tombstone needed no new machinery.
     *
     * <p>The underlying table column is never dropped either way: data outlives a
     * registry entry, and un-hiding must stay possible.
     */
    @DeleteMapping("/entities/{code}/fields/{fc}")
    public Map<String, Object> deleteField(@PathVariable String code, @PathVariable String fc) {
        assertSafe(code);
        assertSafe(fc);
        Long tenantId = TenantContext.get();
        try {
            int owned = jdbc.update(
                    "DELETE FROM entity_field_registry WHERE entity_code=? AND field_code=? AND tenant_id=?",
                    code, fc, tenantId);
            if (owned > 0) {
                return Map.of("ok", true, "deleted", owned, "tombstoned", false,
                        "note", "column on underlying table NOT dropped (safe-by-default)");
            }
            // Not theirs: it is the product's. Hide it for them alone.
            Long fieldId = resolveWritableFieldId(code, fc);
            if (fieldId == null) return Map.of("ok", false, "error", "No such field: " + code + "." + fc);
            int n = jdbc.update("UPDATE entity_field_registry SET hidden=1 WHERE id=? AND tenant_id=?",
                    fieldId, tenantId);
            return Map.of("ok", n > 0, "deleted", 0, "tombstoned", true,
                    "note", "product field hidden for this tenant only; other tenants keep it");
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    // ===== Pages ============================================================

    @GetMapping("/pages")
    public List<Map<String, Object>> listPages(@RequestParam(required = false) String entity) {
        // persona_role was removed from page_definition in an earlier migration;
        // referencing it here 500s + Spring forwards to /error which re-enters
        // the auth filter and returns a misleading 401. Select the columns
        // that actually exist.
        if (entity == null) {
            return jdbc.queryForList("SELECT code, title, category FROM page_definition ORDER BY category, code");
        }
        // Find pages whose body_json references the entity — join through
        // page_version because page_definition doesn't hold body_json.
        // Hand-authored pages pretty-print body_json ("entity": "X", with a
        // space after the colon); Designer-generated pages write it compact
        // ("entity":"X"). Match both so hand-authored pages aren't invisible
        // to this filter.
        String compact = "%\"entity\":\"" + entity + "\"%";
        String spaced  = "%\"entity\": \"" + entity + "\"%";
        return jdbc.queryForList(
            "SELECT pd.code, pd.title, pd.category FROM page_definition pd " +
            "JOIN page_version pv ON pv.id = pd.current_version_id " +
            "WHERE pv.body_json LIKE ? OR pv.body_json LIKE ? ORDER BY pd.code", compact, spaced);
    }

    @PostMapping("/pages")
    public Map<String, Object> createPage(@RequestBody Map<String, Object> body) {
        String code     = str(body.get("code"));
        String title    = strOr(body.get("title"), code);
        String category = strOr(body.get("category"), "Custom");
        String entity   = str(body.get("entity"));
        String kind     = strOr(body.get("kind"), "list");
        assertSafe(entity);

        // Caller can supply an explicit fields[] (from the PageDesigner
        // field-picker) — if present, use it verbatim; if absent, keep the
        // existing "first 18 fields" auto-derive so plain POSTs stay
        // backwards compatible. Each fields[] entry is
        //   {fieldCode, section?, label?, sortable?, filterable?}.
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pickedFields =
                body.get("fields") instanceof List<?> raw
                    ? raw.stream()
                         .filter(o -> o instanceof Map)
                         .map(o -> (Map<String, Object>) o)
                         .toList()
                    : List.of();

        StringBuilder bodyJson = new StringBuilder();
        if ("list".equals(kind)) {
            List<Map<String, Object>> fields;
            if (!pickedFields.isEmpty()) {
                // Hydrate labels from registry so caller only needs to
                // supply fieldCode. Preserves caller-supplied order.
                Map<String, Map<String, Object>> byCode = new LinkedHashMap<>();
                for (Map<String, Object> r : jdbc.queryForList(
                        "SELECT field_code, display_label FROM entity_field_registry WHERE entity_code=?",
                        entity)) {
                    byCode.put(str(r.get("field_code")), r);
                }
                fields = new ArrayList<>();
                for (Map<String, Object> f : pickedFields) {
                    String fc = str(f.getOrDefault("fieldCode", f.get("field_code")));
                    if (fc == null || fc.isBlank()) continue;
                    Map<String, Object> reg = byCode.getOrDefault(fc, Map.of());
                    Map<String, Object> merged = new LinkedHashMap<>();
                    merged.put("field_code", fc);
                    merged.put("display_label", strOr(f.get("label"), str(reg.get("display_label"))));
                    fields.add(merged);
                }
            } else {
                try {
                    fields = jdbc.queryForList(
                        "SELECT field_code, display_label FROM entity_field_registry " +
                        "WHERE entity_code=? AND field_code NOT IN ('id','tenant_id','created_at','updated_at') " +
                        "ORDER BY id LIMIT 18", entity);
                } catch (Exception e) { fields = Collections.emptyList(); }
            }
            StringBuilder cols = new StringBuilder();
            for (Map<String, Object> f : fields) {
                if (cols.length() > 0) cols.append(",");
                cols.append("{\"field\":\"").append(f.get("field_code"))
                    .append("\",\"label\":\"").append(escape((String) f.get("display_label")))
                    .append("\",\"sortable\":true}");
            }
            bodyJson.append("{\"kind\":\"list\",\"dataSource\":{\"type\":\"entity\",\"entity\":\"")
                    .append(entity)
                    .append("\"},\"columns\":[").append(cols).append("],\"pageSize\":25}");
        } else {
            // form — if caller picked fields, embed them so FormField
            // renders only those (and in that order + section grouping).
            if (!pickedFields.isEmpty()) {
                StringBuilder fs = new StringBuilder();
                for (Map<String, Object> f : pickedFields) {
                    String fc = str(f.getOrDefault("fieldCode", f.get("field_code")));
                    if (fc == null || fc.isBlank()) continue;
                    if (fs.length() > 0) fs.append(",");
                    fs.append("{\"field_code\":\"").append(fc).append("\"");
                    if (f.get("section") != null)
                        fs.append(",\"section\":\"").append(escape(str(f.get("section")))).append("\"");
                    fs.append("}");
                }
                bodyJson.append("{\"kind\":\"form\",\"entity\":\"").append(entity)
                        .append("\",\"fields\":[").append(fs).append("]}");
            } else {
                bodyJson.append("{\"kind\":\"form\",\"entity\":\"").append(entity).append("\"}");
            }
        }

        try {
            jdbc.update(
                "INSERT INTO page_definition (tenant_id, code, title, category, status) " +
                "VALUES (1, ?, ?, ?, 'PUBLISHED') " +
                "ON DUPLICATE KEY UPDATE title=VALUES(title), category=VALUES(category)",
                code, title, category);
            // Body lives in page_version keyed by page_id (BIGINT FK to page_definition).
            Long pageId = jdbc.queryForObject("SELECT id FROM page_definition WHERE code=?", Long.class, code);
            publishBody(pageId, code, bodyJson.toString());
            return Map.of("ok", true, "code", code, "category", category, "preview", "/configurable/" + code);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    @PutMapping("/pages/{code}")
    public Map<String, Object> updatePage(@PathVariable String code, @RequestBody Map<String, Object> body) {
        try {
            // Fork-on-write: never touch the -1 product row. See resolveWritablePageId.
            Long pid = resolveWritablePageId(code);
            if (pid == null) return Map.of("ok", false, "error", "No such page: " + code);

            if (body.containsKey("title") || body.containsKey("category")) {
                jdbc.update("UPDATE page_definition SET title=COALESCE(?, title), category=COALESCE(?, category) " +
                            "WHERE id=? AND tenant_id=?",
                        body.get("title"), body.get("category"), pid, TenantContext.get());
            }
            if (body.containsKey("body_json")) {
                publishBody(pid, code, body.get("body_json").toString());
            } else if (body.containsKey("title") || body.containsKey("category")) {
                // Title/category live on page_definition, which the renderer caches alongside
                // the body — a metadata-only edit still has to invalidate.
                renderer.evict(TenantContext.get(), code);
            }
            return Map.of("ok", true);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    /**
     * Remove a page FOR THIS TENANT.
     *
     * <p>You cannot delete a shared row for one owner, so removing a product page
     * is a <b>tombstone</b>, not a delete: fork the global row to the caller,
     * then archive the copy. The resolver's NOT EXISTS clause suppresses the
     * global row whenever a tenant row exists on that code — it does not look at
     * the tenant row's status — so the archived copy hides the page for this
     * tenant and leaves every other firm untouched.
     *
     * <p>Previously this ran {@code UPDATE page_definition SET status='ARCHIVED'
     * WHERE code=?}, unscoped: one tenant deleting a page deleted it for the
     * entire product.
     */
    @DeleteMapping("/pages/{code}")
    public Map<String, Object> deletePage(@PathVariable String code) {
        try {
            Long pid = resolveWritablePageId(code);
            if (pid == null) return Map.of("ok", false, "error", "No such page: " + code);
            Long tenantId = TenantContext.get();
            jdbc.update("UPDATE page_version SET status='ARCHIVED' WHERE page_id=?", pid);
            jdbc.update("UPDATE page_definition SET status='ARCHIVED' WHERE id=? AND tenant_id=?",
                    pid, tenantId);
            renderer.evict(tenantId, code);
            return Map.of("ok", true, "tombstoned", true);
        } catch (Exception ex) {
            return Map.of("ok", false, "error", ex.getMessage());
        }
    }

    // ===== Helpers ==========================================================

    private String sqlTypeFor(String dataType) {
        switch (dataType.toUpperCase()) {
            case "INTEGER":  return "INT NULL";
            case "LONG":     return "BIGINT NULL";
            case "DECIMAL":  return "DECIMAL(18,3) NULL";
            case "BOOLEAN":  return "TINYINT(1) NOT NULL DEFAULT 0";
            case "DATE":     return "DATE NULL";
            case "DATETIME":
            case "TIMESTAMP": return "DATETIME NULL";
            case "TEXT":     return "TEXT NULL";
            case "JSON":     return "JSON NULL";
            case "STRING":
            default:         return "VARCHAR(255) NULL";
        }
    }

    private boolean columnExists(String table, String col) {
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                            "WHERE table_schema=DATABASE() AND table_name=? AND column_name=?",
                    Integer.class, table, col);
            return n != null && n > 0;
        } catch (Exception e) { return false; }
    }

    private void assertSafe(String s) {
        if (s == null || !SAFE_IDENT.matcher(s).matches())
            throw new IllegalArgumentException("Unsafe identifier: " + s);
    }
    private String str(Object o) { return o == null ? null : o.toString(); }
    private String strOr(Object o, String d) { return o == null ? d : o.toString(); }
    /** Returns null for null / blank / whitespace-only inputs, else the trimmed string.
     *  Used for rule expressions: empty string from the UI means "no rule". */
    private String trimOrNull(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }
    private boolean bool(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        return "true".equalsIgnoreCase(o.toString()) || "1".equals(o.toString());
    }
    private boolean boolOr(Object o, boolean d) { return o == null ? d : bool(o); }
    private String escape(String s) { return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); }
}
