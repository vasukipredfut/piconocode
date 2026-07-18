package com.aria.page.controller;

import com.aria.page.dao.PageElementCatalogRepository;
import com.aria.page.dto.PageBindingRequest;
import com.aria.page.dto.PageCreateRequest;
import com.aria.page.dto.PageResponse;
import com.aria.page.dto.PageUpdateRequest;
import com.aria.page.dto.PageVersionSummary;
import com.aria.page.model.PageDefinition;
import com.aria.page.model.PageElementCatalog;
import com.aria.page.model.PageVersion;
import com.aria.page.model.PageWorkflowBinding;
import com.aria.page.service.PageDefinitionService;
import com.aria.page.service.PageNotFoundException;
import com.aria.page.service.PageRendererService;
import com.aria.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST surface for the M0 page-definition substrate.
 *
 * <p>All routes under {@code /api/m0/pages/**} are JWT-required (the existing
 * {@code WebSecurityConfig} authorises any non-{@code /api/auth} request).
 * {@code /api/m0/element-catalog} is explicitly permit-all so the Builder UI
 * can populate before login. POSTs honour the existing {@code IdempotencyFilter}
 * via the {@code Idempotency-Key} header — no extra wiring required.
 *
 * <p>Tenant id: hardcoded to 1 with a TODO. The M0-tenant-context task in
 * Round 1H lifts it to {@code SecurityContext.getAuthentication().getPrincipal()}.
 */
@RestController
@Tag(name = "M0 Pages", description = "Page-definition substrate (Round 1F)")
public class PageDefinitionController {

    /** Tenant id resolved from the request's JWT 'tid' claim via the filter. */
    private static Long tenant() { return TenantContext.get(); }

    @Autowired private PageDefinitionService pages;
    @Autowired private PageRendererService renderer;
    @Autowired private PageElementCatalogRepository catalogRepo;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;

    @GetMapping("/api/m0/pages")
    @Operation(summary = "List pages (paginated). Optional category + status filters.")
    public ResponseEntity<Page<PageDefinition>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(pages.list(tenant(), category, status, PageRequest.of(page, size)));
    }

    /**
     * Sidebar category display metadata (label, icon, order), resolved per tenant.
     *
     * <p>The tenant row wins over the {@code -1} product row on the same code, so a
     * factory can rename a group for itself alone. This replaces the hardcoded
     * CATEGORY_META in the React shell — labels are data, not code, so renaming or
     * reordering a group is a row edit, not a redeploy. Falls back gracefully:
     * if the table is empty the frontend uses the raw category code.
     */
    @GetMapping("/api/m0/categories")
    @Operation(summary = "Sidebar category labels/icons/order, resolved for the tenant.")
    public ResponseEntity<List<java.util.Map<String, Object>>> categories() {
        List<java.util.Map<String, Object>> rows = jdbc.queryForList(
            "SELECT code, label, icon, sort_order FROM ( " +
            "  SELECT code, label, icon, sort_order, " +
            "         ROW_NUMBER() OVER (PARTITION BY code ORDER BY tenant_id DESC) rn " +
            "  FROM page_category WHERE tenant_id IN (?, " + TenantContext.GLOBAL_TENANT_ID + ") " +
            "    AND status='ACTIVE' " +
            ") x WHERE rn = 1 ORDER BY sort_order, label", tenant());
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/api/m0/slip-fields/{flow}")
    @Operation(summary = "Detail lines to print on a slip for a flow variant, resolved for the tenant.")
    public ResponseEntity<List<java.util.Map<String, Object>>> slipFields(@PathVariable String flow) {
        List<java.util.Map<String, Object>> rows = jdbc.queryForList(
            "SELECT field, label FROM ( " +
            "  SELECT field, label, sort_order, " +
            "         ROW_NUMBER() OVER (PARTITION BY field ORDER BY tenant_id DESC) rn " +
            "  FROM slip_field_map WHERE flow_variant_code = ? " +
            "    AND tenant_id IN (?, " + TenantContext.GLOBAL_TENANT_ID + ") " +
            "    AND status='ACTIVE' " +
            ") x WHERE rn = 1 ORDER BY sort_order, label", flow, tenant());
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/api/m0/pages/{code}")
    @Operation(summary = "Load the published version (404 if none).")
    public ResponseEntity<PageResponse> getPublished(@PathVariable String code) {
        return renderer.loadPublishedFor(tenant(), code)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new PageNotFoundException("No published version for " + code));
    }

    @GetMapping("/api/m0/pages/{code}/draft")
    @Operation(summary = "Load the active draft version (404 if none).")
    public ResponseEntity<PageResponse> getDraft(@PathVariable String code) {
        PageDefinition def = pages.mustFind(tenant(), code);
        PageVersion v = pages.loadDraft(tenant(), code)
                .orElseThrow(() -> new PageNotFoundException("No DRAFT for " + code));
        return ResponseEntity.ok(PageResponse.builder()
                .id(def.getId())
                .code(def.getCode())
                .title(def.getTitle())
                .description(def.getDescription())
                .category(def.getCategory())
                .status(v.getStatus())
                .version(v.getVersion())
                .bodyJson(v.getBodyJson())
                .updatedAt(def.getUpdatedAt())
                .build());
    }

    @PostMapping("/api/m0/pages")
    @Operation(summary = "Create a page (v1 DRAFT).")
    public ResponseEntity<PageDefinition> create(@Valid @RequestBody PageCreateRequest req) {
        return ResponseEntity.status(201).body(pages.create(tenant(), req));
    }

    @PutMapping("/api/m0/pages/{code}")
    @Operation(summary = "Update the active draft.")
    public ResponseEntity<PageVersion> updateDraft(
            @PathVariable String code, @Valid @RequestBody PageUpdateRequest req) {
        return ResponseEntity.ok(pages.updateDraft(tenant(), code, req));
    }

    @PostMapping("/api/m0/pages/{code}/publish")
    @Operation(summary = "Publish the active draft as a new version.")
    public ResponseEntity<PageVersion> publish(@PathVariable String code) {
        // TODO M0-tenant-context (Round 1H): actor id from JWT principal.
        return ResponseEntity.ok(pages.publish(tenant(), code, null));
    }

    @PostMapping("/api/m0/pages/{code}/rollback")
    @Operation(summary = "Republish an older version forward as a new version.")
    public ResponseEntity<PageVersion> rollback(
            @PathVariable String code, @RequestParam("toVersion") Integer toVersion) {
        return ResponseEntity.ok(pages.rollback(tenant(), code, toVersion, null));
    }

    @GetMapping("/api/m0/pages/{code}/versions")
    @Operation(summary = "List version history (newest first).")
    public ResponseEntity<List<PageVersionSummary>> versions(@PathVariable String code) {
        List<PageVersionSummary> out = pages.listVersions(tenant(), code).stream()
                .map(v -> PageVersionSummary.builder()
                        .id(v.getId())
                        .version(v.getVersion())
                        .status(v.getStatus())
                        .publishedAt(v.getPublishedAt())
                        .publishedBy(v.getPublishedBy())
                        .createdAt(v.getCreatedAt())
                        .rationale(v.getRationale())
                        .build())
                .toList();
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/m0/pages/{code}/binding")
    @Operation(summary = "List workflow bindings for this page.")
    public ResponseEntity<List<PageWorkflowBinding>> bindings(@PathVariable String code) {
        return ResponseEntity.ok(pages.listBindings(tenant(), code));
    }

    @PostMapping("/api/m0/pages/{code}/binding")
    @Operation(summary = "Bind a page to a workflow step.")
    public ResponseEntity<PageWorkflowBinding> createBinding(
            @PathVariable String code, @Valid @RequestBody PageBindingRequest req) {
        return ResponseEntity.status(201).body(pages.createBinding(tenant(), code, req));
    }

    @GetMapping("/api/m0/element-catalog")
    @Operation(summary = "List Renderer primitives (palette source). Permit-all.")
    public ResponseEntity<List<PageElementCatalog>> catalog() {
        return ResponseEntity.ok(catalogRepo.findAll());
    }
}
