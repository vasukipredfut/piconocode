package com.aria.workflow.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.workflow.dao.WorkflowDefinitionRepository;
import com.aria.workflow.dao.WorkflowEventRepository;
import com.aria.workflow.dao.WorkflowInstanceRepository;
import com.aria.workflow.dto.WorkflowInstanceDto;
import com.aria.workflow.dto.WorkflowQueueItem;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.model.WorkflowDefinition;
import com.aria.workflow.model.WorkflowEvent;
import com.aria.workflow.model.WorkflowInstance;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Core workflow runtime. Single primary class — by design — that owns:
 * <ul>
 *   <li>Instance creation against the latest PUBLISHED workflow definition.</li>
 *   <li>Page-submission handling with pessimistic instance lock + SpEL guards.</li>
 *   <li>Action dispatch (the loop is real; the action catalogue is empty in
 *       this round — Round 1H lands notification/webhook/etc actions).</li>
 *   <li>Persona-keyed queue reads.</li>
 *   <li>Read-only instance + recent-event hydration.</li>
 * </ul>
 *
 * <p>Tenant scoping comes from {@link TenantContext}; the filter populates
 * it from the JWT {@code tid} claim.
 */
@Service
public class WorkflowEngineService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngineService.class);

    @Autowired private WorkflowDefinitionRepository definitions;
    @Autowired private WorkflowInstanceRepository instances;
    @Autowired private WorkflowEventRepository events;
    @Autowired private SpelGuardEvaluator guardEvaluator;
    @Autowired(required = false) private java.util.List<WorkflowTransitionListener> transitionListeners;

    private final ObjectMapper json = new ObjectMapper();

    /**
     * The tenant that owns this instance's definition.
     *
     * <p>Falls back to the instance's own tenant for rows created before the
     * owner column existed: back then every definition was tenant-owned, so that
     * is the correct historical answer and old instances keep resolving.
     */
    private Long ownerOf(WorkflowInstance inst) {
        return inst.getWorkflowOwnerTenantId() != null
                ? inst.getWorkflowOwnerTenantId()
                : inst.getTenantId();
    }

    // ---------------------------------------------------------------- start
    @Transactional("m0PageTransactionManager")
    public WorkflowInstance startInstance(String workflowCode, Map<String, Object> initialContext) {
        Long tenantId = TenantContext.get();
        WorkflowDefinition def = definitions.findLatestPublishedSingle(tenantId, workflowCode)
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(
                        "No PUBLISHED workflow_definition for code=" + workflowCode));
        Map<String, Object> body = parseBody(def);
        String initialState = stringOr(body.get("initialState"), "DRAFT");
        String assignedRole = roleForState(body, initialState);

        WorkflowInstance inst = WorkflowInstance.builder()
                .tenantId(tenantId)
                .workflowCode(def.getCode())
                .workflowVersion(def.getVersion())
                // Record WHICH definition this instance resolved to (-1 product
                // flow, or the tenant's own fork). Every later transition re-reads
                // by (owner, code, version), so a fork created after this instance
                // started cannot silently swap its state machine.
                .workflowOwnerTenantId(def.getTenantId())
                .currentState(initialState)
                .status("OPEN")
                .assignedRole(assignedRole)
                .businessRef(stringOr(initialContext == null ? null : initialContext.get("businessRef"), null))
                .submittedDataJson(writeJson(Map.of()))
                .build();
        inst = instances.save(inst);

        events.save(WorkflowEvent.builder()
                .tenantId(tenantId)
                .instanceId(inst.getId())
                .kind(WorkflowEvent.KIND_STATE_TRANSITION)
                .fromState(null)
                .toState(initialState)
                .payloadJson(writeJson(initialContext == null ? Map.of() : initialContext))
                .build());

        log.info("workflow.start tenant={} code={} version={} instance={} state={}",
                tenantId, def.getCode(), def.getVersion(), inst.getId(), initialState);
        return inst;
    }

    // ------------------------------------------------- handle page submission
    @Transactional("m0PageTransactionManager")
    public WorkflowTransitionResult handlePageSubmission(
            Long instanceId, String pageCode, Map<String, Object> formData, Long actorUserId) {
        Long tenantId = TenantContext.get();
        WorkflowInstance inst = instances.findByIdForUpdate(instanceId, tenantId)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException(
                        "instance " + instanceId + " not found for tenant " + tenantId));

        if (!"OPEN".equals(inst.getStatus())) {
            throw new WorkflowTransitionConflictException(
                    "instance " + instanceId + " is " + inst.getStatus());
        }

        // Merge formData into accumulated submitted_data_json keyed by pageCode.
        Map<String, Object> bag = readBag(inst.getSubmittedDataJson());
        bag.put(pageCode, formData == null ? Map.of() : formData);
        inst.setSubmittedDataJson(writeJson(bag));

        // Find transition.
        WorkflowDefinition def = definitions.findByTenantIdAndCodeAndVersion(
                ownerOf(inst), inst.getWorkflowCode(), inst.getWorkflowVersion())
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(
                        "definition gone for instance " + instanceId));
        Map<String, Object> body = parseBody(def);
        List<Map<String, Object>> candidates = transitionsFor(body, inst.getCurrentState(), pageCode);
        if (candidates.isEmpty()) {
            instances.save(inst);
            events.save(WorkflowEvent.builder()
                    .tenantId(tenantId)
                    .instanceId(inst.getId())
                    .kind(WorkflowEvent.KIND_PAGE_SUBMITTED)
                    .fromState(inst.getCurrentState())
                    .toState(inst.getCurrentState())
                    .pageCode(pageCode)
                    .actorUserId(actorUserId)
                    .payloadJson(writeJson(formData == null ? Map.of() : formData))
                    .build());
            throw new WorkflowTransitionNotFoundException(
                    "no transition from " + inst.getCurrentState() + " via page " + pageCode);
        }

        SpelGuardEvaluator.InstanceView view = new InstanceViewImpl(inst.getCurrentState(), bag);

        Map<String, Object> matched = null;
        for (Map<String, Object> t : candidates) {
            String guard = stringOr(t.get("guard"), null);
            if (guard == null) { matched = t; break; }
            SpelGuardEvaluator.Outcome outcome = guardEvaluator.evaluate(
                    guard, formData, view, actorUserId, tenantId);
            if (outcome.errored()) {
                events.save(WorkflowEvent.builder()
                        .tenantId(tenantId)
                        .instanceId(inst.getId())
                        .kind(WorkflowEvent.KIND_ERROR)
                        .fromState(inst.getCurrentState())
                        .pageCode(pageCode)
                        .actorUserId(actorUserId)
                        .payloadJson(writeJson(Map.of(
                                "guard", guard, "error", outcome.reason)))
                        .build());
                throw new WorkflowGuardErrorException("guard evaluation failed: " + outcome.reason);
            }
            if (outcome.allowed()) { matched = t; break; }
        }

        // Persist the page submission either way.
        instances.save(inst);
        events.save(WorkflowEvent.builder()
                .tenantId(tenantId)
                .instanceId(inst.getId())
                .kind(WorkflowEvent.KIND_PAGE_SUBMITTED)
                .fromState(inst.getCurrentState())
                .pageCode(pageCode)
                .actorUserId(actorUserId)
                .payloadJson(writeJson(formData == null ? Map.of() : formData))
                .build());

        if (matched == null) {
            // All guards denied. Surfacing this as a silent HTTP 200 with
            // transitioned:false caused both the UI ("Approved" toast even
            // when nothing happened) and the test agents to falsely
            // assume success. Throw an exception that maps to 409 so any
            // caller — browser, axios interceptor, automation — gets a
            // clear "your guard didn't pass" signal with the guard text
            // and the keys you submitted.
            String triedGuards = candidates.stream()
                    .map(t -> stringOr(t.get("guard"), "(no guard)"))
                    .collect(java.util.stream.Collectors.joining(" || "));
            String submittedFields = (formData == null || formData.isEmpty())
                    ? "(empty)"
                    : String.join(",", formData.keySet());
            throw new WorkflowGuardRejectedException(
                    inst.getCurrentState(), pageCode, triedGuards, submittedFields);
        }

        // Apply transition.
        String previousState = inst.getCurrentState();
        String toState = stringOr(matched.get("to"), null);
        Objects.requireNonNull(toState, "transition missing 'to'");
        String newRole = roleForState(body, toState);
        boolean terminal = terminalState(body, toState);

        inst.setCurrentState(toState);
        inst.setAssignedRole(newRole);
        if (terminal) inst.setStatus("CLOSED");
        instances.save(inst);

        events.save(WorkflowEvent.builder()
                .tenantId(tenantId)
                .instanceId(inst.getId())
                .kind(WorkflowEvent.KIND_STATE_TRANSITION)
                .fromState(previousState)
                .toState(toState)
                .pageCode(pageCode)
                .actorUserId(actorUserId)
                .build());

        List<String> firedActions = fireActions(inst, matched, formData, actorUserId);

        // Post-transition listeners (e.g. M10 GatePass write-through).
        // Listener exceptions are caught + logged; they cannot fail the
        // transition (the engine is authoritative).
        if (transitionListeners != null && !transitionListeners.isEmpty()) {
            for (WorkflowTransitionListener l : transitionListeners) {
                try {
                    l.onTransition(inst, previousState, bag);
                } catch (Exception ex) {
                    log.warn("workflow listener {} failed for instance={}: {}",
                            l.getClass().getSimpleName(), inst.getId(), ex.getMessage());
                }
            }
        }

        log.info("workflow.transition tenant={} instance={} {}->{} role={} actions={}",
                tenantId, inst.getId(), previousState, toState, newRole, firedActions.size());

        return WorkflowTransitionResult.builder()
                .instanceId(inst.getId())
                .previousState(previousState)
                .newState(toState)
                .assignedRole(newRole)
                .transitioned(true)
                .transitionedAt(Instant.now())
                .actionsFired(firedActions)
                .build();
    }

    // ----------------------------------------------------------------- queue
    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public List<WorkflowQueueItem> queueForPersona(String persona, int page, int size) {
        Long tenantId = TenantContext.get();
        Pageable p = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)));
        return instances.queueForPersona(tenantId, persona, p).getContent().stream()
                .map(this::toQueueItem)
                .toList();
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Optional<WorkflowInstanceDto> loadInstance(Long id) {
        Long tenantId = TenantContext.get();
        return instances.findByIdAndTenantId(id, tenantId).map(inst -> {
            List<WorkflowEvent> recent = events.findByInstanceIdOrderByTsDesc(
                    inst.getId(), PageRequest.of(0, 20));
            List<WorkflowInstanceDto.EventDto> evs = recent.stream()
                    .map(e -> WorkflowInstanceDto.EventDto.builder()
                            .id(e.getId()).ts(e.getTs()).kind(e.getKind())
                            .fromState(e.getFromState()).toState(e.getToState())
                            .pageCode(e.getPageCode()).actorUserId(e.getActorUserId())
                            .build())
                    .toList();
            return WorkflowInstanceDto.builder()
                    .id(inst.getId())
                    .workflowCode(inst.getWorkflowCode())
                    .workflowVersion(inst.getWorkflowVersion())
                    .currentState(inst.getCurrentState())
                    .status(inst.getStatus())
                    .assignedRole(inst.getAssignedRole())
                    .businessRef(inst.getBusinessRef())
                    .createdAt(inst.getCreatedAt())
                    .updatedAt(inst.getUpdatedAt())
                    .submittedData(readBag(inst.getSubmittedDataJson()))
                    .recentEvents(evs)
                    .build();
        });
    }

    @Transactional("m0PageTransactionManager")
    public WorkflowInstance cancel(Long id, Long actorUserId, String reason) {
        Long tenantId = TenantContext.get();
        WorkflowInstance inst = instances.findByIdForUpdate(id, tenantId)
                .orElseThrow(() -> new WorkflowInstanceNotFoundException("instance " + id + " not found"));
        if ("CLOSED".equals(inst.getStatus()) || "CANCELLED".equals(inst.getStatus())) {
            return inst;
        }
        inst.setStatus("CANCELLED");
        instances.save(inst);
        events.save(WorkflowEvent.builder()
                .tenantId(tenantId)
                .instanceId(inst.getId())
                .kind(WorkflowEvent.KIND_STATE_TRANSITION)
                .fromState(inst.getCurrentState())
                .toState(inst.getCurrentState())
                .actorUserId(actorUserId)
                .payloadJson(writeJson(Map.of("cancelled", true, "reason", reason == null ? "" : reason)))
                .build());
        return inst;
    }

    // ============================================================ helpers
    private List<String> fireActions(WorkflowInstance inst, Map<String, Object> matchedTransition,
                                     Map<String, Object> formData, Long actorUserId) {
        Object raw = matchedTransition.get("actions");
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<String> fired = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> action)) continue;
            String name = String.valueOf(action.get("name"));
            // Action catalogue is empty in this round — Round 1H adds
            // notification/webhook/etc. Each is dispatchable but no-op for now.
            // We always record ACTION_FIRED so audit is complete.
            events.save(WorkflowEvent.builder()
                    .tenantId(inst.getTenantId())
                    .instanceId(inst.getId())
                    .kind(WorkflowEvent.KIND_ACTION_FIRED)
                    .pageCode(null)
                    .actorUserId(actorUserId)
                    .payloadJson(writeJson(Map.of("action", name, "result", "no-op-round1F")))
                    .build());
            fired.add(name);
        }
        return fired;
    }

    private WorkflowQueueItem toQueueItem(WorkflowInstance i) {
        return WorkflowQueueItem.builder()
                .id(i.getId())
                .workflowCode(i.getWorkflowCode())
                .currentState(i.getCurrentState())
                .status(i.getStatus())
                .businessRef(i.getBusinessRef())
                .assignedRole(i.getAssignedRole())
                .updatedAt(i.getUpdatedAt())
                .submittedData(readBag(i.getSubmittedDataJson()))
                .build();
    }

    /**
     * Return the list of transitions that COULD fire from the instance's
     * current state for the given page. Lets the UI render only the
     * action buttons that are actually viable — no more "Approve" button
     * that 409s the moment the user clicks it. Guards are NOT evaluated
     * here (they may depend on formData that's still being entered); the
     * UI gets the guard expression in the response so it can show a
     * tooltip ("requires status == APPROVED").
     */
    public List<Map<String, Object>> availableTransitions(Long instanceId, String pageCode) {
        Long tenantId = TenantContext.get();
        WorkflowInstance inst = instances.findById(instanceId)
                .filter(i -> i.getTenantId().equals(tenantId))
                .orElseThrow(() -> new WorkflowInstanceNotFoundException(
                        "instance " + instanceId + " not found for tenant " + tenantId));
        // Read the version this instance was STARTED on, not the latest published.
        // handlePageSubmission enforces the pinned version, so reading "latest" here
        // meant that republishing a workflow made in-flight instances render buttons
        // from the new definition while the engine still enforced the old one — the
        // operator clicked and got a 409. Admins must be able to edit a live workflow
        // without breaking vehicles already moving through the plant.
        WorkflowDefinition def = definitions.findByTenantIdAndCodeAndVersion(
                ownerOf(inst), inst.getWorkflowCode(), inst.getWorkflowVersion())
                .orElseThrow(() -> new WorkflowDefinitionNotFoundException(
                        "definition v" + inst.getWorkflowVersion() + " gone for instance " + instanceId));
        Map<String, Object> body = parseBody(def);
        return transitionsFor(body, inst.getCurrentState(), pageCode);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseBody(WorkflowDefinition def) {
        if (def.getBodyJson() == null || def.getBodyJson().isBlank()) return Map.of();
        try {
            return json.readValue(def.getBodyJson(), Map.class);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse workflow body for " + def.getCode(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> transitionsFor(
            Map<String, Object> body, String currentState, String pageCode) {
        Object raw = body.get("transitions");
        if (!(raw instanceof List<?> list)) return List.of();
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> t)) continue;
            if (!"page.submitted".equals(t.get("trigger"))) continue;
            if (!currentState.equals(t.get("from"))) continue;
            // Either side can be null — null pageCode in the body_json
            // means "fires regardless of which page submits". Without
            // this guard the engine NPEs and Spring surfaces a 401
            // (the /error path requires auth), masking the real cause.
            Object defPage = t.get("pageCode");
            if (defPage != null && pageCode != null && !defPage.equals(pageCode)) continue;
            if (defPage != null && pageCode == null) continue;
            out.add((Map<String, Object>) t);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private String roleForState(Map<String, Object> body, String stateCode) {
        Object raw = body.get("states");
        if (!(raw instanceof List<?> list)) return null;
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> s)) continue;
            if (stateCode.equals(s.get("code"))) {
                Object role = s.get("assignedRole");
                return role == null ? null : role.toString();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean terminalState(Map<String, Object> body, String stateCode) {
        Object raw = body.get("states");
        if (!(raw instanceof List<?> list)) return false;
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> s)) continue;
            if (stateCode.equals(s.get("code"))) {
                return Boolean.TRUE.equals(s.get("terminal"));
            }
        }
        return false;
    }

    private Map<String, Object> readBag(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) return new HashMap<>();
        try {
            Map<String, Object> m = json.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
            return m == null ? new HashMap<>() : new HashMap<>(m);
        } catch (Exception e) {
            log.warn("malformed submitted_data_json: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return json.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static String stringOr(Object o, String fallback) {
        return o == null ? fallback : o.toString();
    }

    // --- InstanceView impl for the guard evaluator -----------------------
    private static class InstanceViewImpl implements SpelGuardEvaluator.InstanceView {
        private final String currentState;
        private final Map<String, Object> submitted;
        InstanceViewImpl(String s, Map<String, Object> data) { this.currentState = s; this.submitted = data; }
        @Override public String getCurrentState() { return currentState; }
        @Override public Map<String, Object> getSubmittedData() { return submitted; }
    }
}
