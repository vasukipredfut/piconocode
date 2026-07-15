package com.aria.m0.datasource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST surface for Schema V2 §3 DataSource grammar.
 *
 * <p>The Renderer's {@code ListView} primitive POSTs a {@code Request}
 * envelope here; the response is a paginated map of rows ready for table
 * rendering. Filter / sort / pagination state live on the client; the
 * server is stateless.
 */
@RestController
@Tag(name = "M0 DataSource", description = "Schema V2 DataSource query compiler (Round 1G)")
public class DataSourceController {

    @Autowired private DataSourceQueryService service;

    @PostMapping("/api/m0/datasource/query")
    @Operation(summary = "Execute a DataSource query.")
    public ResponseEntity<DataSourceSpec.Response> query(@RequestBody DataSourceSpec.Request req) {
        int page = req.getPage() == null ? 0 : req.getPage();
        int size = req.getSize() == null ? 25 : req.getSize();
        return ResponseEntity.ok(service.query(req.getSpec(), page, size, req.getBindings()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badInput(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "spec_invalid", "message", e.getMessage()));
    }
}
