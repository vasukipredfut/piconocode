package com.aria.workflow.engine;

import com.aria.security.TenantContext;
import com.aria.workflow.dao.WorkflowDefinitionRepository;
import com.aria.workflow.dao.WorkflowEventRepository;
import com.aria.workflow.dao.WorkflowInstanceRepository;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.model.WorkflowDefinition;
import com.aria.workflow.model.WorkflowEvent;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the auto-resync-on-submit behavior in {@link
 * WorkflowEngineService#handlePageSubmission} and {@link
 * WorkflowEngineService#availableTransitions} (via the shared private
 * {@code resolveEffectiveDefinition}).
 *
 * <p>Background: {@link WorkflowEngineService#startInstance} pins (owner,
 * code, version) onto every instance, and both methods above used to always
 * re-read that EXACT pinned version — editing a workflow's states/
 * transitions via the Designer never reached an already-running instance.
 * Now, on every page submission, a newer PUBLISHED version is adopted
 * automatically IF the instance's current state still exists in it;
 * otherwise the instance safely stays on its pin. This proves the behavior
 * by giving the pinned and latest definitions DIFFERENT transition targets
 * for the same (state, page) pair and asserting which one actually fired —
 * a black-box proof that doesn't depend on reading private resolution logic.
 *
 * <p>{@code guardEvaluator} is a real {@link SpelGuardEvaluator} (no
 * dependencies of its own) rather than a mock — every transition here is
 * unconditional (no guard), so {@code evaluate} is never even reached, but
 * using the real evaluator means these tests exercise the actual guard path
 * exactly as production does.
 */
class WorkflowEngineServiceAutoResyncTest {

    private static final Long TENANT_ID = 1L;
    private static final Long INSTANCE_ID = 500L;
    private static final String WORKFLOW_CODE = "M10.FGLoading";
    private static final String PAGE_CODE = "aria:loading-in-progress-entry";

    private WorkflowDefinitionRepository definitions;
    private WorkflowInstanceRepository instances;
    private WorkflowEventRepository events;
    private WorkflowEngineService engine;

    @BeforeEach
    void setUp() {
        definitions = mock(WorkflowDefinitionRepository.class);
        instances = mock(WorkflowInstanceRepository.class);
        events = mock(WorkflowEventRepository.class);
        engine = new WorkflowEngineService();
        ReflectionTestUtils.setField(engine, "definitions", definitions);
        ReflectionTestUtils.setField(engine, "instances", instances);
        ReflectionTestUtils.setField(engine, "events", events);
        ReflectionTestUtils.setField(engine, "guardEvaluator", new SpelGuardEvaluator());
        TenantContext.set(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ---- handlePageSubmission: compatible upgrade --------------------------

    @Test
    void compatibleNewerVersion_isAutoAdoptedAndItsTransitionFires() {
        WorkflowInstance inst = openInstance("BAY_ASSIGNED", 3);
        when(instances.findByIdForUpdate(INSTANCE_ID, TENANT_ID)).thenReturn(Optional.of(inst));
        when(definitions.findByTenantIdAndCodeAndVersion(TENANT_ID, WORKFLOW_CODE, 3))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));
        when(definitions.findLatestPublishedSingle(TENANT_ID, WORKFLOW_CODE))
                .thenReturn(Optional.of(definition(5, "NEW_ONLY_TARGET", "PlantManager")));

        WorkflowTransitionResult result = engine.handlePageSubmission(INSTANCE_ID, PAGE_CODE, Map.of(), 9L);

        assertThat(result.isTransitioned()).isTrue();
        assertThat(result.getNewState()).isEqualTo("NEW_ONLY_TARGET");
        assertThat(inst.getWorkflowVersion()).isEqualTo(5);

        // One DEFINITION_RESYNC event (the auto-upgrade) plus one STATE_TRANSITION event.
        org.mockito.ArgumentCaptor<WorkflowEvent> captor = org.mockito.ArgumentCaptor.forClass(WorkflowEvent.class);
        verify(events, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        List<WorkflowEvent> saved = captor.getAllValues();
        assertThat(saved).anySatisfy(e -> {
            assertThat(e.getKind()).isEqualTo(WorkflowEvent.KIND_DEFINITION_RESYNC);
            assertThat(e.getPayloadJson()).contains("\"fromVersion\":3").contains("\"toVersion\":5");
        });
        assertThat(saved).anySatisfy(e -> assertThat(e.getKind()).isEqualTo(WorkflowEvent.KIND_STATE_TRANSITION));
    }

    @Test
    void compatibleNewerVersion_stillAdoptedWhenNoTransitionMatches_roleRefreshedForCurrentState() {
        WorkflowInstance inst = openInstance("BAY_ASSIGNED", 3);
        inst.setAssignedRole("Supervisor");
        when(instances.findByIdForUpdate(INSTANCE_ID, TENANT_ID)).thenReturn(Optional.of(inst));
        when(definitions.findByTenantIdAndCodeAndVersion(TENANT_ID, WORKFLOW_CODE, 3))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));
        // v7 keeps BAY_ASSIGNED (role changed) but has NO transition for this page at all.
        WorkflowDefinition noTransitionDef = WorkflowDefinition.builder()
                .id(1007L).tenantId(TENANT_ID).code(WORKFLOW_CODE).version(7).status("PUBLISHED")
                .bodyJson("{\"states\":[{\"code\":\"BAY_ASSIGNED\",\"assignedRole\":\"PlantManager\"}],\"transitions\":[]}")
                .build();
        when(definitions.findLatestPublishedSingle(TENANT_ID, WORKFLOW_CODE)).thenReturn(Optional.of(noTransitionDef));

        try {
            engine.handlePageSubmission(INSTANCE_ID, PAGE_CODE, Map.of(), 9L);
        } catch (WorkflowTransitionNotFoundException expected) {
            // No transition for this page from BAY_ASSIGNED in v7 — expected.
        }

        assertThat(inst.getWorkflowVersion()).isEqualTo(7);
        assertThat(inst.getAssignedRole()).isEqualTo("PlantManager");
    }

    // ---- handlePageSubmission: incompatible upgrade -------------------------

    @Test
    void incompatibleNewerVersion_staysOnPin_pinnedTransitionFiresInstead() {
        WorkflowInstance inst = openInstance("BAY_ASSIGNED", 3);
        when(instances.findByIdForUpdate(INSTANCE_ID, TENANT_ID)).thenReturn(Optional.of(inst));
        when(definitions.findByTenantIdAndCodeAndVersion(TENANT_ID, WORKFLOW_CODE, 3))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));
        // v8 renamed BAY_ASSIGNED away entirely.
        WorkflowDefinition incompatible = WorkflowDefinition.builder()
                .id(1008L).tenantId(TENANT_ID).code(WORKFLOW_CODE).version(8).status("PUBLISHED")
                .bodyJson("{\"states\":[{\"code\":\"BAY_ASSIGNED_V2\"}],\"transitions\":[]}")
                .build();
        when(definitions.findLatestPublishedSingle(TENANT_ID, WORKFLOW_CODE)).thenReturn(Optional.of(incompatible));

        WorkflowTransitionResult result = engine.handlePageSubmission(INSTANCE_ID, PAGE_CODE, Map.of(), 9L);

        assertThat(result.isTransitioned()).isTrue();
        assertThat(result.getNewState()).isEqualTo("OLD_ONLY_TARGET"); // pinned v3's transition, not v8's (nonexistent) one
        assertThat(inst.getWorkflowVersion()).isEqualTo(3); // never bumped

        verify(events, never()).save(org.mockito.ArgumentMatchers.argThat(
                e -> e != null && WorkflowEvent.KIND_DEFINITION_RESYNC.equals(e.getKind())));
    }

    // ---- handlePageSubmission: no-op when already on latest -----------------

    @Test
    void alreadyOnLatestVersion_noResyncEventWritten() {
        WorkflowInstance inst = openInstance("BAY_ASSIGNED", 3);
        when(instances.findByIdForUpdate(INSTANCE_ID, TENANT_ID)).thenReturn(Optional.of(inst));
        when(definitions.findByTenantIdAndCodeAndVersion(TENANT_ID, WORKFLOW_CODE, 3))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));
        when(definitions.findLatestPublishedSingle(TENANT_ID, WORKFLOW_CODE))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));

        WorkflowTransitionResult result = engine.handlePageSubmission(INSTANCE_ID, PAGE_CODE, Map.of(), 9L);

        assertThat(result.getNewState()).isEqualTo("OLD_ONLY_TARGET");
        assertThat(inst.getWorkflowVersion()).isEqualTo(3);
        verify(events, never()).save(org.mockito.ArgumentMatchers.argThat(
                e -> e != null && WorkflowEvent.KIND_DEFINITION_RESYNC.equals(e.getKind())));
    }

    // ---- availableTransitions consistency ------------------------------------

    @Test
    void availableTransitions_reflectsCompatibleLatestVersion_notThePin() {
        WorkflowInstance inst = openInstance("BAY_ASSIGNED", 3);
        when(instances.findById(INSTANCE_ID)).thenReturn(Optional.of(inst));
        when(definitions.findByTenantIdAndCodeAndVersion(TENANT_ID, WORKFLOW_CODE, 3))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));
        when(definitions.findLatestPublishedSingle(TENANT_ID, WORKFLOW_CODE))
                .thenReturn(Optional.of(definition(5, "NEW_ONLY_TARGET", "PlantManager")));

        List<Map<String, Object>> available = engine.availableTransitions(INSTANCE_ID, PAGE_CODE);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).get("to")).isEqualTo("NEW_ONLY_TARGET");
        // Read-only: must never persist.
        verify(instances, never()).save(any());
    }

    @Test
    void availableTransitions_staysOnPinWhenLatestIsIncompatible() {
        WorkflowInstance inst = openInstance("BAY_ASSIGNED", 3);
        when(instances.findById(INSTANCE_ID)).thenReturn(Optional.of(inst));
        when(definitions.findByTenantIdAndCodeAndVersion(TENANT_ID, WORKFLOW_CODE, 3))
                .thenReturn(Optional.of(definition(3, "OLD_ONLY_TARGET", "Supervisor")));
        WorkflowDefinition incompatible = WorkflowDefinition.builder()
                .id(1009L).tenantId(TENANT_ID).code(WORKFLOW_CODE).version(9).status("PUBLISHED")
                .bodyJson("{\"states\":[{\"code\":\"BAY_ASSIGNED_V2\"}],\"transitions\":[]}")
                .build();
        when(definitions.findLatestPublishedSingle(TENANT_ID, WORKFLOW_CODE)).thenReturn(Optional.of(incompatible));

        List<Map<String, Object>> available = engine.availableTransitions(INSTANCE_ID, PAGE_CODE);

        assertThat(available).hasSize(1);
        assertThat(available.get(0).get("to")).isEqualTo("OLD_ONLY_TARGET");
    }

    // ---- fixtures -------------------------------------------------------------

    private WorkflowInstance openInstance(String currentState, int version) {
        return WorkflowInstance.builder()
                .id(INSTANCE_ID)
                .tenantId(TENANT_ID)
                .workflowCode(WORKFLOW_CODE)
                .workflowVersion(version)
                .workflowOwnerTenantId(TENANT_ID)
                .currentState(currentState)
                .status("OPEN")
                .submittedDataJson("{}")
                .build();
    }

    private WorkflowDefinition definition(int version, String transitionTarget, String assignedRole) {
        String bodyJson = "{\"states\":["
                + "{\"code\":\"BAY_ASSIGNED\",\"assignedRole\":\"" + assignedRole + "\"},"
                + "{\"code\":\"" + transitionTarget + "\"}"
                + "],\"transitions\":[{\"trigger\":\"page.submitted\",\"from\":\"BAY_ASSIGNED\","
                + "\"to\":\"" + transitionTarget + "\",\"pageCode\":\"" + PAGE_CODE + "\"}]}";
        return WorkflowDefinition.builder()
                .id(900L + version).tenantId(TENANT_ID).code(WORKFLOW_CODE).version(version).status("PUBLISHED")
                .bodyJson(bodyJson)
                .build();
    }
}
