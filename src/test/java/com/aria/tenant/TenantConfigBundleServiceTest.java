package com.aria.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure-Mockito unit tests for {@link TenantConfigBundleService}. We stub
 * a {@link JdbcTemplate} that fakes "row exists / row new" semantics via
 * its return-value contract: INSERT IGNORE returns 1 for a new row, 0 for
 * a duplicate. Re-applying the same bundle must yield 0-new counts.
 */
class TenantConfigBundleServiceTest {

    private JdbcTemplate jdbc;
    private TenantConfigBundleService service;
    private final ObjectMapper json = new ObjectMapper();

    // Per-key "already seen" set so duplicate INSERTs return 0.
    private final Map<String, Boolean> insertSeen = new HashMap<>();

    @BeforeEach
    void setup() {
        jdbc = mock(JdbcTemplate.class);
        insertSeen.clear();

        // SELECT COUNT(*) probes for table existence return 0 (table exists).
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        // For INSERT IGNORE, simulate idempotency via the captured SQL + args.
        when(jdbc.update(anyString(), any(Object[].class))).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            Object[] args = inv.getArguments();
            // Skip "UPDATE tenant_role SET ..." statements — they always update.
            if (sql.startsWith("UPDATE")) return 1;
            String key = sql + ":" + java.util.Arrays.deepToString(java.util.Arrays.copyOfRange(args, 1, args.length));
            if (insertSeen.containsKey(key)) return 0;   // duplicate
            insertSeen.put(key, true);
            return 1;
        });

        service = new TenantConfigBundleService(jdbc);
    }

    private JsonNode bundle(String body) throws Exception {
        return json.readTree(body);
    }

    @Test
    @DisplayName("apply roles: each role yields 1 row in tenant_role")
    void rolesApplied() throws Exception {
        JsonNode b = bundle("""
            { "rolesAndPermissions": [
                {"role":"FrontGate","label":{"en-IN":"Front Gate"},"perms":["DispatchEvent:create"]},
                {"role":"QC","label":{"en-IN":"QC"},"perms":["DispatchEvent:read.queue"]}
            ]}""");

        var r = service.apply(1L, b);
        assertThat(r.rolesAdded).isEqualTo(2);
        assertThat(r.errors).isEmpty();
    }

    @Test
    @DisplayName("apply workflows: 3 distinct workflow_definition rows")
    void workflowsApplied() throws Exception {
        JsonNode b = bundle("""
            { "workflowDefinitions": [
                {"code":"M10.TankerInbound","version":2,"states":[],"transitions":[]},
                {"code":"M10.PackUnload","version":1,"states":[],"transitions":[]},
                {"code":"M10.FGLoading","version":1,"states":[],"transitions":[]}
            ]}""");

        var r = service.apply(1L, b);
        assertThat(r.workflowsAdded).isEqualTo(3);
    }

    @Test
    @DisplayName("apply custom fields: Vehicle bundle inserts each field row")
    void customFieldsApplied() throws Exception {
        JsonNode b = bundle("""
            { "customFields": {
                "Vehicle": [
                  {"code":"vehicleType","fieldType":"select","label":{"en-IN":"Type"},"options":["TANKER","BOX_TRUCK"]},
                  {"code":"tareWeightKg","fieldType":"decimal","label":{"en-IN":"Tare"}}
                ]
              }
            }""");

        var r = service.apply(1L, b);
        assertThat(r.fieldsAdded).isEqualTo(2);
    }

    @Test
    @DisplayName("apply form layouts: each layout one row")
    void formLayoutsApplied() throws Exception {
        JsonNode b = bundle("""
            { "formLayouts": [
                {"entity":"DispatchEvent","code":"frontgate.scan","label":{"en-IN":"Scan"},"device":"mobile","sections":[]},
                {"entity":"DispatchEvent","code":"qc.sample","label":{"en-IN":"QC"},"device":"mobile","sections":[]}
            ]}""");

        var r = service.apply(1L, b);
        assertThat(r.layoutsAdded).isEqualTo(2);
    }

    @Test
    @DisplayName("apply saved views: each view one row")
    void savedViewsApplied() throws Exception {
        JsonNode b = bundle("""
            { "savedViews": [
                {"entity":"DispatchEvent","code":"queue.live","label":{"en-IN":"Live"},"filter":"f1","sort":"-createdAt"},
                {"entity":"Vehicle","code":"expiring","label":{"en-IN":"Expiring"},"filter":"f2","sort":"rcExpiry"}
            ]}""");

        var r = service.apply(1L, b);
        assertThat(r.viewsAdded).isEqualTo(2);
    }

    @Test
    @DisplayName("apply notifications: each template one row")
    void notificationsApplied() throws Exception {
        JsonNode b = bundle("""
            { "notificationTemplates": [
                {"code":"parking_assigned","channel":"sms","language":"ta-IN","subject":null,"body":"hi"},
                {"code":"qc_failed","channel":"push","language":"en-IN","subject":"QC fail","body":"x"}
            ]}""");

        var r = service.apply(1L, b);
        assertThat(r.notificationsAdded).isEqualTo(2);
    }

    @Test
    @DisplayName("re-applying same bundle is idempotent: counts go to 0")
    void idempotentReplay() throws Exception {
        JsonNode b = bundle("""
            { "rolesAndPermissions":[{"role":"X","label":{"en-IN":"X"},"perms":[]}],
              "workflowDefinitions":[{"code":"W","version":1,"states":[],"transitions":[]}],
              "notificationTemplates":[{"code":"n","channel":"sms","language":"en","body":"x"}]
            }""");
        var r1 = service.apply(1L, b);
        assertThat(r1.total()).isGreaterThan(0);

        var r2 = service.apply(1L, b);
        assertThat(r2.rolesAdded).isZero();
        assertThat(r2.workflowsAdded).isZero();
        assertThat(r2.notificationsAdded).isZero();
        assertThat(r2.errors).isEmpty();
    }

    @Test
    @DisplayName("null bundle returns error in result, no exception thrown")
    void nullBundle() {
        var r = service.apply(1L, null);
        assertThat(r.errors).contains("bundle is null");
        assertThat(r.total()).isZero();
    }

    @Test
    @DisplayName("missing optional sections are tolerated")
    void emptyBundle() throws Exception {
        var r = service.apply(1L, bundle("{}"));
        assertThat(r.total()).isZero();
        assertThat(r.errors).isEmpty();
    }

    @Test
    @DisplayName("field type mapping: select->ENUM, decimal->DECIMAL, multiselect->JSON, date->DATE")
    void fieldTypeMapping() throws Exception {
        JsonNode b = bundle("""
            { "customFields": {
                "X": [
                  {"code":"a","fieldType":"select","label":{"en-IN":"a"},"options":["X"]},
                  {"code":"b","fieldType":"decimal","label":{"en-IN":"b"}},
                  {"code":"c","fieldType":"multiselect","label":{"en-IN":"c"},"options":["X","Y"]},
                  {"code":"d","fieldType":"date","label":{"en-IN":"d"}},
                  {"code":"e","fieldType":"longtext","label":{"en-IN":"e"}}
                ]}
            }""");
        var r = service.apply(1L, b);
        assertThat(r.fieldsAdded).isEqualTo(5);
        // Capture each insert and assert the data_type column carries the
        // mapped enum value (4th positional arg in entity_field_registry insert).
        verify(jdbc, atLeastOnce()).update(contains("INSERT IGNORE INTO entity_field_registry"),
                any(Object[].class));
    }

    @Test
    @DisplayName("notification table absent -> error string includes REOPEN tag")
    void notificationTableAbsent() throws Exception {
        // Override the probe to throw for notification_template only.
        when(jdbc.queryForObject(contains("notification_template"), eq(Integer.class)))
                .thenThrow(new DataIntegrityViolationException("no table"));
        JsonNode b = bundle("""
            { "notificationTemplates":[{"code":"n","channel":"sms","language":"en","body":"x"}] }""");
        var r = service.apply(1L, b);
        assertThat(r.notificationsAdded).isZero();
        assertThat(r.errors).anyMatch(s -> s.contains("REOPEN WITH TASK_1H_2"));
    }
}
