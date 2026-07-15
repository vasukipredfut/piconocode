package com.aria.savedview;

import com.aria.savedview.model.SavedView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST surface for tenant_list_view CRUD (Schema V2 §18 #2).
 *
 * <p>{@code userId} is sourced from the request header
 * {@code X-Earms-User-Id} during Round 1G; Round 1H lifts to JWT claim.
 */
@RestController
@Tag(name = "M0 Saved Views", description = "Per-page saved-view CRUD (Round 1G)")
public class SavedViewController {

    @Autowired private SavedViewService svc;

    @GetMapping("/api/m0/saved-views")
    @Operation(summary = "List visible saved views for a page.")
    public ResponseEntity<List<SavedView>> list(
            @RequestParam("page") String pageSlug,
            @RequestHeader(value = "X-Earms-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(svc.listVisible(pageSlug, userId));
    }

    @PostMapping("/api/m0/saved-views")
    @Operation(summary = "Create a saved view.")
    public ResponseEntity<SavedView> create(@RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Earms-User-Id", required = false) Long userId) {
        String slug = String.valueOf(body.get("page"));
        String name = String.valueOf(body.get("name"));
        String scope = String.valueOf(body.getOrDefault("scope", "USER"));
        String scopeValue = body.get("scopeValue") == null ? null : String.valueOf(body.get("scopeValue"));
        Object bj = body.get("body");
        String bodyJson = bj == null ? "{}" : (bj instanceof String ? (String) bj : new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(bj).toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(svc.create(slug, name, scope, scopeValue, bodyJson, userId));
    }

    @PutMapping("/api/m0/saved-views/{id}")
    @Operation(summary = "Update a saved view.")
    public ResponseEntity<SavedView> update(@PathVariable Long id, @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Earms-User-Id", required = false) Long userId) {
        String name = body.get("name") == null ? null : String.valueOf(body.get("name"));
        Object bj = body.get("body");
        String bodyJson = bj == null ? null : (bj instanceof String ? (String) bj : new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(bj).toString());
        return ResponseEntity.ok(svc.update(id, name, bodyJson, userId));
    }

    @DeleteMapping("/api/m0/saved-views/{id}")
    @Operation(summary = "Delete a saved view.")
    public ResponseEntity<Void> delete(@PathVariable Long id,
            @RequestHeader(value = "X-Earms-User-Id", required = false) Long userId) {
        svc.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> bad(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "bad_request", "message", e.getMessage()));
    }
}
