package com.aria.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.adapter.PingResult;
import com.aria.integration.dao.IntegrationEventLogRepository;
import com.aria.integration.dto.IntegrationInstanceRequest;
import com.aria.integration.dto.IntegrationInstanceResponse;
import com.aria.integration.model.IntegrationEventLog;
import com.aria.integration.model.IntegrationInstance;
import com.aria.integration.model.IntegrationInstanceVersion;
import com.aria.integration.service.IntegrationInstanceService;
import com.aria.integration.service.IntegrationInstanceService.*;
import com.aria.integration.service.IntegrationOutboundCaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** REST surface for the M11 integration-instance lifecycle. */
@RestController
@RequestMapping("/api/m11/instances")
public class IntegrationInstanceController {

    @Autowired private IntegrationInstanceService service;
    @Autowired private IntegrationOutboundCaller caller;
    @Autowired private IntegrationEventLogRepository events;
    private final ObjectMapper json = new ObjectMapper();

    @GetMapping
    public List<IntegrationInstanceResponse> list(
            @RequestParam(name = "status", required = false) String status) {
        List<IntegrationInstance> rows = (status == null || status.isBlank())
                ? service.listByTenant()
                : service.listByTenantAndStatus(status.toUpperCase());
        return rows.stream().map(i -> IntegrationInstanceResponse.from(i, json))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<IntegrationInstanceResponse> create(
            @RequestBody IntegrationInstanceRequest req) {
        IntegrationInstance inst = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IntegrationInstanceResponse.from(inst, json));
    }

    @GetMapping("/{id}")
    public IntegrationInstanceResponse get(@PathVariable Long id) {
        return IntegrationInstanceResponse.from(service.require(id), json);
    }

    @PutMapping("/{id}")
    public IntegrationInstanceResponse update(@PathVariable Long id,
                                              @RequestBody IntegrationInstanceRequest req) {
        return IntegrationInstanceResponse.from(service.updateDraft(id, req), json);
    }

    @PostMapping("/{id}/publish")
    public Map<String, Object> publish(@PathVariable Long id, @RequestBody(required = false) Map<String,Object> body) {
        String rationale = body == null ? null : String.valueOf(body.getOrDefault("rationale", ""));
        IntegrationInstanceVersion v = service.publish(id, rationale, null);
        return Map.of("ok", true, "instanceId", id, "version", v.getVersion(),
                "publishedAt", v.getPublishedAt());
    }

    @PostMapping("/{id}/rollback")
    public Map<String, Object> rollback(@PathVariable Long id,
                                        @RequestParam("toVersion") int toVersion) {
        IntegrationInstanceVersion v = service.rollback(id, toVersion, null);
        return Map.of("ok", true, "instanceId", id, "newVersion", v.getVersion());
    }

    @PostMapping("/{id}/test")
    public Map<String, Object> test(@PathVariable Long id) {
        PingResult r = caller.ping(id);
        Map<String, Object> out = new HashMap<>();
        out.put("ok", r.isOk()); out.put("message", r.getMessage());
        out.put("httpStatus", r.getHttpStatus()); out.put("latencyMs", r.getLatencyMs());
        return out;
    }

    @PostMapping("/{id}/enable")
    public IntegrationInstanceResponse enable(@PathVariable Long id) {
        return IntegrationInstanceResponse.from(service.enable(id), json);
    }

    @PostMapping("/{id}/disable")
    public IntegrationInstanceResponse disable(@PathVariable Long id,
                                               @RequestBody(required = false) Map<String,Object> body) {
        String reason = body == null ? null : String.valueOf(body.getOrDefault("reason", ""));
        return IntegrationInstanceResponse.from(service.disable(id, reason), json);
    }

    @PostMapping("/{id}/retire")
    public IntegrationInstanceResponse retire(@PathVariable Long id) {
        return IntegrationInstanceResponse.from(service.retire(id), json);
    }

    @PostMapping("/{id}/run-now")
    public Map<String, Object> runNow(@PathVariable Long id,
                                       @RequestBody Map<String, Object> body) {
        String op = String.valueOf(body.getOrDefault("operation", ""));
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) body.getOrDefault("payload", Map.of());
        OutboundResult r = caller.invoke(id, op, payload);
        Map<String, Object> out = new HashMap<>();
        out.put("ok", r.isOk()); out.put("status", r.getStatus());
        out.put("body", r.getBody()); out.put("errorCode", r.getErrorCode());
        out.put("errorMessage", r.getErrorMessage());
        return out;
    }

    @GetMapping("/{id}/events")
    public List<Map<String, Object>> events(@PathVariable Long id,
                                            @RequestParam(name = "since", required = false) String sinceIso) {
        // Tenant-scope check via the service.
        service.require(id);
        List<IntegrationEventLog> rows = sinceIso == null
                ? events.findTop100ByInstanceIdOrderByTsDesc(id)
                : events.findByInstanceIdAndTsAfterOrderByTsDesc(id, Instant.parse(sinceIso));
        return rows.stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", e.getId()); m.put("ts", e.getTs());
            m.put("direction", e.getDirection()); m.put("status", e.getStatus());
            m.put("latencyMs", e.getLatencyMs()); m.put("errorCode", e.getErrorCode());
            return m;
        }).collect(Collectors.toList());
    }

    // ----------------- exception mappers (4xx) -----------------
    @ExceptionHandler(InstanceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> notFound(InstanceNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", "not_found", "message", e.getMessage()));
    }
    @ExceptionHandler(InstanceConflictException.class)
    public ResponseEntity<Map<String,Object>> conflict(InstanceConflictException e) {
        return ResponseEntity.status(409).body(Map.of("error", "conflict", "message", e.getMessage()));
    }
    @ExceptionHandler(ConnectorUnknownException.class)
    public ResponseEntity<Map<String,Object>> unknownConnector(ConnectorUnknownException e) {
        return ResponseEntity.status(422).body(Map.of("error", "unknown_connector", "message", e.getMessage()));
    }
    @ExceptionHandler(InvalidConfigException.class)
    public ResponseEntity<Map<String,Object>> invalid(InvalidConfigException e) {
        return ResponseEntity.status(422).body(Map.of("error", "invalid_config", "message", e.getMessage()));
    }
    @ExceptionHandler(InvalidLifecycleException.class)
    public ResponseEntity<Map<String,Object>> lifecycle(InvalidLifecycleException e) {
        return ResponseEntity.status(409).body(Map.of("error", "invalid_lifecycle", "message", e.getMessage()));
    }
}
