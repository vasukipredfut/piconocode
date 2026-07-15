package com.aria.m0.tenantconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST surface for tenant-configuration bundle application.
 *
 * <p>Two endpoints:
 * <ul>
 *   <li>{@code POST /api/m0/tenant-config/apply} — body {tenantId,bundleJson}</li>
 *   <li>{@code POST /api/m0/tenant-config/apply-pico} — loads the bundled
 *       Pico bundle from classpath and applies for tenant_id=1</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/m0/tenant-config")
@Tag(name = "M0 Tenant Configuration Bundle")
public class TenantConfigBundleController {

    @Autowired private TenantConfigBundleService service;
    private final ObjectMapper json = new ObjectMapper();

    public static class ApplyRequest {
        public Long tenantId;
        public Object bundleJson;
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply a parsed bundle JSON for a tenant.")
    public ResponseEntity<Map<String, Object>> apply(@RequestBody ApplyRequest req) {
        if (req == null || req.tenantId == null || req.bundleJson == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "tenantId + bundleJson required"));
        }
        JsonNode node = json.valueToTree(req.bundleJson);
        TenantConfigBundleService.ApplyResult r = service.apply(req.tenantId, node);
        return ResponseEntity.ok(toMap(r));
    }

    @PostMapping("/apply-pico")
    @Operation(summary = "Apply the bundled Pico TENANT-CONFIG for tenant_id=1.")
    public ResponseEntity<Map<String, Object>> applyPico() {
        TenantConfigBundleService.ApplyResult r = service.applyPicoBundle();
        return ResponseEntity.ok(toMap(r));
    }

    private Map<String, Object> toMap(TenantConfigBundleService.ApplyResult r) {
        Map<String, Object> m = new HashMap<>();
        m.put("rolesAdded", r.rolesAdded);
        m.put("workflowsAdded", r.workflowsAdded);
        m.put("fieldsAdded", r.fieldsAdded);
        m.put("layoutsAdded", r.layoutsAdded);
        m.put("viewsAdded", r.viewsAdded);
        m.put("notificationsAdded", r.notificationsAdded);
        m.put("errors", r.errors);
        m.put("total", r.total());
        return m;
    }
}
