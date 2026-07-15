package com.aria.m11.controller;

import com.aria.m11.model.IntegrationConnectorCatalog;
import com.aria.m11.service.IntegrationCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/** Read-only catalog HTTP surface. */
@RestController
@RequestMapping("/api/m11/catalog")
public class IntegrationCatalogController {

    @Autowired private IntegrationCatalogService catalog;

    @GetMapping
    public List<IntegrationConnectorCatalog> list(
            @RequestParam(name = "category", required = false) String category) {
        if (category == null || category.isBlank()) return catalog.listAll();
        return catalog.listByCategory(category.toUpperCase());
    }

    @GetMapping("/{code}")
    public ResponseEntity<IntegrationConnectorCatalog> get(@PathVariable("code") String code) {
        Optional<IntegrationConnectorCatalog> opt = catalog.findLatestByCode(code);
        return opt.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
