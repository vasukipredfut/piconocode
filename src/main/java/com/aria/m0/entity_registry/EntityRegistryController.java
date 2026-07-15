package com.aria.m0.entity_registry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST surface for the entity registry (Schema V2 §18 #3). One read-only
 * GET that powers the Builder UI and the DataSource debug overlay.
 */
@RestController
@Tag(name = "M0 Entity Registry", description = "Schema V2 entity catalogue (Round 1G)")
public class EntityRegistryController {

    @Autowired private EntityRegistryService svc;

    @GetMapping("/api/m0/entity-registry")
    @Operation(summary = "List all registered entities with their fields.")
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(svc.listAllWithFields());
    }
}
