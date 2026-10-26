package com.aria.workflow.controller;

import com.aria.workflow.dto.WorkflowInstanceDto;
import com.aria.workflow.dto.WorkflowQueueItem;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowDefinitionNotFoundException;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.engine.WorkflowGuardErrorException;
import com.aria.workflow.engine.WorkflowInstanceNotFoundException;
import com.aria.workflow.engine.WorkflowTransitionConflictException;
import com.aria.workflow.engine.WorkflowTransitionNotFoundException;
import com.aria.auth.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST surface for the workflow runtime.
 *
 * <p>All endpoints JWT-required + tenant-scoped via {@link com.aria.security.TenantContext}.
 * Idempotency: POSTs honour the existing {@code Idempotency-Key} header via
 * the global {@code IdempotencyFilter}.
 */
@RestController
@RequestMapping("/api/workflow")
@Tag(name = "M0 Workflow Runtime")
public class WorkflowController {

    @Autowired private WorkflowEngineService engine;

    // ---- Requests ------------------------------------------------------
    public static class StartInstanceRequest {
        public String workflowCode;
        public Map<String, Object> initialContext;
    }

    public static class TransitionRequest {
        public Long instanceId;
        public String pageCode;
        public Map<String, Object> formData;
    }

    public static class CancelRequest {
        public String reason;
    }

    // ---- Endpoints ------------------------------------------------------
    @PostMapping("/instances")
    @Operation(summary = "Start a new workflow instance.")
    public ResponseEntity<Map<String, Object>> create(@RequestBody StartInstanceRequest req) {
        var inst = engine.startInstance(req.workflowCode, req.initialContext);
        return ResponseEntity.status(201).body(Map.of(
                "instanceId", inst.getId(),
                "currentState", inst.getCurrentState(),
                "assignedRole", inst.getAssignedRole() == null ? "" : inst.getAssignedRole(),
                "workflowCode", inst.getWorkflowCode(),
                "workflowVersion", inst.getWorkflowVersion()));
    }

    @PostMapping("/transition")
    @Operation(summary = "Submit a page against a workflow instance.")
    public ResponseEntity<WorkflowTransitionResult> transition(@RequestBody TransitionRequest req) {
        WorkflowTransitionResult result = engine.handlePageSubmission(
                req.instanceId, req.pageCode, req.formData, currentUserId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/instances/{id}")
    @Operation(summary = "Load instance + last 20 events.")
    public ResponseEntity<WorkflowInstanceDto> get(@PathVariable Long id) {
        return engine.loadInstance(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/instances/{id}/available")
    @Operation(summary = "List transitions reachable from current state via the given page — UI gates buttons by this.")
    public ResponseEntity<List<Map<String, Object>>> available(
            @PathVariable Long id, @RequestParam("pageCode") String pageCode) {
        return ResponseEntity.ok(engine.availableTransitions(id, pageCode));
    }

    @GetMapping("/queue/{persona}")
    @Operation(summary = "List open instances assigned to a persona (work queue).")
    public ResponseEntity<List<WorkflowQueueItem>> queue(
            @PathVariable String persona,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "25") @Min(1) int size) {
        return ResponseEntity.ok(engine.queueForPersona(persona, page, size));
    }

    @GetMapping("/queue-for-page")
    @Operation(summary = "List open instances currently at a state this page acts on (step-scoped queue). "
            + "404 when the page is not a workflow step, so the UI can fall back to the persona queue.")
    public ResponseEntity<List<WorkflowQueueItem>> queueForPage(
            @RequestParam("pageCode") String pageCode,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) int size) {
        var q = engine.queuePageForPage(pageCode, page, size);
        if (q == null) return ResponseEntity.notFound().build();
        // The body stays a plain list (the UI reads it as one); the paging facts ride in headers: X-Total-Count = the whole queue.
        // The page holds as many items as the caller asks for (size, default 100) -- no cap.
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(q.total()))
                .header("X-Page", String.valueOf(Math.max(page, 0)))
                .header("X-Page-Size", String.valueOf(size))
                .header("Access-Control-Expose-Headers", "X-Total-Count, X-Page, X-Page-Size")
                .body(q.items());
    }

    @PostMapping("/instances/{id}/cancel")
    @Operation(summary = "Admin cancel of an open instance.")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long id, @RequestBody(required = false) CancelRequest req) {
        var inst = engine.cancel(id, currentUserId(), req == null ? null : req.reason);
        return ResponseEntity.ok(Map.of(
                "instanceId", inst.getId(),
                "status", inst.getStatus(),
                "currentState", inst.getCurrentState()));
    }

    // ---- Error mapping --------------------------------------------------
    @ExceptionHandler(WorkflowInstanceNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(WorkflowInstanceNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
    @ExceptionHandler(WorkflowDefinitionNotFoundException.class)
    public ResponseEntity<Map<String, String>> defNotFound(WorkflowDefinitionNotFoundException e) {
        return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
    }
    @ExceptionHandler(WorkflowTransitionNotFoundException.class)
    public ResponseEntity<Map<String, String>> noTransition(WorkflowTransitionNotFoundException e) {
        return ResponseEntity.status(422).body(Map.of("error", e.getMessage()));
    }
    @ExceptionHandler(WorkflowTransitionConflictException.class)
    public ResponseEntity<Map<String, String>> conflict(WorkflowTransitionConflictException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
    @ExceptionHandler(WorkflowGuardErrorException.class)
    public ResponseEntity<Map<String, String>> guardErr(WorkflowGuardErrorException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
    @ExceptionHandler(com.aria.workflow.engine.WorkflowValidationException.class)
    public ResponseEntity<Map<String, String>> validationRejected(com.aria.workflow.engine.WorkflowValidationException e) {
        return ResponseEntity.status(422).body(Map.of("error", "validation_failed", "message", e.getMessage()));
    }
    @ExceptionHandler(com.aria.workflow.engine.WorkflowGuardRejectedException.class)
    public ResponseEntity<Map<String, Object>> guardRejected(
            com.aria.workflow.engine.WorkflowGuardRejectedException e) {
        // 409 Conflict = your transition was understood but rejected by a
        // guard — i.e. the page submission is saved but state didn't move.
        // Frontend axios interceptor turns this into a visible error toast
        // so the user doesn't see a false "Approved" success.
        return ResponseEntity.status(409).body(Map.of(
                "error", "guard_rejected",
                "transitioned", false,
                "currentState", e.getCurrentState(),
                "pageCode", e.getPageCode(),
                "reason", e.getMessage(),
                "triedGuards", e.getTriedGuards()));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthUserDetails udi) return udi.getId();
        return null;
    }
}
