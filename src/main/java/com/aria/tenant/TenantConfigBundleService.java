package com.aria.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Applies a tenant-configuration bundle JSON to the live M0 substrate.
 *
 * <p>Each section is upserted idempotently: re-applying the same bundle is a
 * cheap no-op (counts are reported as 0-new).
 *
 * <p>The bundle is always supplied by the caller and applied to the tenant they
 * name. No customer's config ships inside the jar — onboarding any customer is
 * the same call with a different payload, which is what keeps one build usable
 * for every deployment.
 */
@Service
public class TenantConfigBundleService {

    private static final Logger log = LoggerFactory.getLogger(TenantConfigBundleService.class);

    private final ObjectMapper json = new ObjectMapper();
    private final JdbcTemplate jdbc;

    @Autowired
    public TenantConfigBundleService(@Qualifier("psqlDataSource") DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /** Test-only constructor accepting a pre-built JdbcTemplate. */
    public TenantConfigBundleService(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    /** Aggregate per-section apply counts. */
    public static class ApplyResult {
        public int rolesAdded;
        public int workflowsAdded;
        public int fieldsAdded;
        public int layoutsAdded;
        public int viewsAdded;
        public int notificationsAdded;
        public List<String> errors = new ArrayList<>();

        public int rolesAdded() { return rolesAdded; }
        public int workflowsAdded() { return workflowsAdded; }
        public int fieldsAdded() { return fieldsAdded; }
        public int layoutsAdded() { return layoutsAdded; }
        public int viewsAdded() { return viewsAdded; }
        public int notificationsAdded() { return notificationsAdded; }
        public List<String> errors() { return errors; }

        public int total() {
            return rolesAdded + workflowsAdded + fieldsAdded
                 + layoutsAdded + viewsAdded + notificationsAdded;
        }
    }

    /** Apply a parsed bundle to the given tenant. */
    @Transactional("m0PageTransactionManager")
    public ApplyResult apply(Long tenantId, JsonNode bundle) {
        ApplyResult r = new ApplyResult();
        if (bundle == null || bundle.isMissingNode() || bundle.isNull()) {
            r.errors.add("bundle is null");
            return r;
        }
        applyRoles(tenantId, bundle.path("rolesAndPermissions"), r);
        applyCustomFields(tenantId, bundle.path("customFields"), r);
        applyWorkflowDefinitions(tenantId, bundle.path("workflowDefinitions"), r);
        applyNotificationTemplates(tenantId, bundle.path("notificationTemplates"), r);
        applyFormLayouts(tenantId, bundle.path("formLayouts"), r);
        applySavedViews(tenantId, bundle.path("savedViews"), r);
        log.info("tenant-config.apply tenant={} roles={} workflows={} fields={} layouts={} views={} notifications={} errors={}",
                tenantId, r.rolesAdded, r.workflowsAdded, r.fieldsAdded,
                r.layoutsAdded, r.viewsAdded, r.notificationsAdded, r.errors.size());
        return r;
    }

    // ---- per-section upserts -------------------------------------------
    private void applyRoles(Long tenantId, JsonNode roles, ApplyResult r) {
        if (!roles.isArray()) return;
        for (JsonNode role : roles) {
            String code = role.path("role").asText(null);
            if (code == null) continue;
            String label = role.path("label").path("en-IN").asText(code);
            String perms = role.path("perms").toString();
            try {
                int updated = jdbc.update(
                        "INSERT IGNORE INTO tenant_role (tenant_id, code, label, permissions_json) VALUES (?, ?, ?, ?)",
                        tenantId, code, label, perms);
                if (updated > 0) r.rolesAdded++;
                // Always overwrite permissions to keep them in sync.
                jdbc.update(
                        "UPDATE tenant_role SET label = ?, permissions_json = ? WHERE tenant_id = ? AND code = ?",
                        label, perms, tenantId, code);
            } catch (Exception ex) {
                r.errors.add("role " + code + ": " + ex.getMessage());
            }
        }
    }

    private void applyCustomFields(Long tenantId, JsonNode customFields, ApplyResult r) {
        if (!customFields.isObject()) return;
        Iterator<String> entities = customFields.fieldNames();
        while (entities.hasNext()) {
            String entityCode = entities.next();
            // Ensure entity row exists (registry is a parent FK).
            try {
                jdbc.update("INSERT IGNORE INTO entity_registry (code, display_name, table_name, category) VALUES (?, ?, ?, ?)",
                        entityCode, entityCode, "tenant_" + entityCode.toLowerCase(), "Tenant");
            } catch (Exception ignored) { /* table may not exist in slim tests */ }
            JsonNode fields = customFields.path(entityCode);
            if (!fields.isArray()) continue;
            for (JsonNode f : fields) {
                String code = f.path("code").asText(null);
                if (code == null) continue;
                String label = f.path("label").path("en-IN").asText(code);
                String dataType = mapType(f.path("fieldType").asText("STRING"));
                boolean required = f.path("required").asBoolean(false);
                boolean searchable = f.path("searchableIndexed").asBoolean(false);
                String defaultValue = f.has("defaultValue") ? f.path("defaultValue").asText() : null;
                String optionsJson = f.has("options") ? f.path("options").toString() : null;
                String relation = f.path("relationEntity").asText(null);
                try {
                    int updated = jdbc.update(
                            "INSERT IGNORE INTO entity_field_registry " +
                            "(entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, default_value, options_source_json, relation_entity) " +
                            "VALUES (?, ?, ?, ?, 1, 1, ?, ?, ?, ?, ?)",
                            entityCode, code, label, dataType,
                            searchable ? 1 : 0, required ? 1 : 0,
                            defaultValue, optionsJson, relation);
                    if (updated > 0) r.fieldsAdded++;
                } catch (Exception ex) {
                    r.errors.add("field " + entityCode + "." + code + ": " + ex.getMessage());
                }
            }
        }
    }

    private String mapType(String wdlType) {
        if (wdlType == null) return "STRING";
        switch (wdlType.toLowerCase()) {
            case "number":      return "INT";
            case "decimal":     return "DECIMAL";
            case "date":        return "DATE";
            case "datetime":    return "DATETIME";
            case "boolean":     return "BOOLEAN";
            case "select":      return "ENUM";
            case "multiselect": return "JSON";
            case "longtext":    return "TEXT";
            case "computed":    return "COMPUTED";
            case "relation":    return "RELATION";
            default:            return "STRING";
        }
    }

    private void applyWorkflowDefinitions(Long tenantId, JsonNode wfs, ApplyResult r) {
        if (!wfs.isArray()) return;
        for (JsonNode wf : wfs) {
            String code = wf.path("code").asText(null);
            int version = wf.path("version").asInt(1);
            if (code == null) continue;
            try {
                int updated = jdbc.update(
                        "INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES (?, ?, ?, 'PUBLISHED', ?)",
                        tenantId, code, version, wf.toString());
                if (updated > 0) r.workflowsAdded++;
            } catch (Exception ex) {
                r.errors.add("workflow " + code + " v" + version + ": " + ex.getMessage());
            }
        }
    }

    private void applyNotificationTemplates(Long tenantId, JsonNode templates, ApplyResult r) {
        if (!templates.isArray()) return;
        // Detect table existence cheaply.
        try {
            jdbc.queryForObject("SELECT COUNT(*) FROM notification_template LIMIT 1", Integer.class);
        } catch (Exception probe) {
            r.errors.add("notification_template table absent — REOPEN WITH TASK_1H_2_NOTIFICATION_PIPELINE");
            return;
        }
        for (JsonNode t : templates) {
            String code = t.path("code").asText(null);
            if (code == null) continue;
            String channel = t.path("channel").asText("push");
            String language = t.path("language").asText("en-IN");
            String subject = t.path("subject").isNull() ? null : t.path("subject").asText(null);
            String body = t.path("body").asText("");
            try {
                int updated = jdbc.update(
                        "INSERT IGNORE INTO notification_template (tenant_id, code, channel, language, subject, body) VALUES (?, ?, ?, ?, ?, ?)",
                        tenantId, code, channel, language, subject, body);
                if (updated > 0) r.notificationsAdded++;
            } catch (Exception ex) {
                r.errors.add("notification " + code + ": " + ex.getMessage());
            }
        }
    }

    private void applyFormLayouts(Long tenantId, JsonNode layouts, ApplyResult r) {
        if (!layouts.isArray()) return;
        try {
            jdbc.queryForObject("SELECT COUNT(*) FROM tenant_form_layout LIMIT 1", Integer.class);
        } catch (Exception probe) {
            r.errors.add("tenant_form_layout table absent");
            return;
        }
        for (JsonNode l : layouts) {
            String entity = l.path("entity").asText(null);
            String code = l.path("code").asText(null);
            if (entity == null || code == null) continue;
            String label = l.path("label").path("en-IN").asText(code);
            String device = l.path("device").asText(null);
            String bodyJson = l.path("sections").isMissingNode()
                    ? "{}"
                    : "{\"sections\":" + l.path("sections").toString() + "}";
            try {
                int updated = jdbc.update(
                        "INSERT IGNORE INTO tenant_form_layout (tenant_id, entity_code, code, label, device, body_json) VALUES (?, ?, ?, ?, ?, ?)",
                        tenantId, entity, code, label, device, bodyJson);
                if (updated > 0) r.layoutsAdded++;
            } catch (Exception ex) {
                r.errors.add("layout " + code + ": " + ex.getMessage());
            }
        }
    }

    private void applySavedViews(Long tenantId, JsonNode views, ApplyResult r) {
        if (!views.isArray()) return;
        for (JsonNode v : views) {
            String entity = v.path("entity").asText(null);
            String code = v.path("code").asText(null);
            if (entity == null || code == null) continue;
            String name = v.path("label").path("en-IN").asText(code);
            String pageSlug = pageSlugFor(entity, code);
            String filter = v.path("filter").asText("");
            String sort = v.path("sort").asText("");
            String bodyJson = String.format(
                    "{\"code\":\"%s\",\"entity\":\"%s\",\"filter\":{\"expr\":%s},\"sort\":%s}",
                    escape(code), escape(entity),
                    json.valueToTree(filter).toString(),
                    json.valueToTree(sort).toString());
            try {
                int updated = jdbc.update(
                        "INSERT IGNORE INTO tenant_list_view (tenant_id, page_slug, name, scope, is_default, body_json) VALUES (?, ?, ?, 'TENANT', 0, ?)",
                        tenantId, pageSlug, name, bodyJson);
                if (updated > 0) r.viewsAdded++;
            } catch (Exception ex) {
                r.errors.add("view " + code + ": " + ex.getMessage());
            }
        }
    }

    private static String pageSlugFor(String entity, String code) {
        if (entity == null) return code;
        return entity.toLowerCase() + "." + code;
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
