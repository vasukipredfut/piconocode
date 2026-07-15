package com.aria.m0.action.handler;

import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import com.aria.m0.workflow.dto.WorkflowTransitionResult;
import com.aria.m0.workflow.engine.WorkflowEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Unit test for {@link WorkflowTransitionActionHandler}: delegates to engine. */
class WorkflowTransitionActionHandlerTest {

    @Test
    void code_isWorkflowTransition() {
        assertThat(new WorkflowTransitionActionHandler().code()).isEqualTo("workflow.transition");
    }

    @Test
    void delegatesToEngine_andReturnsTransitionFacts() {
        WorkflowEngineService engine = mock(WorkflowEngineService.class);
        when(engine.handlePageSubmission(eq(11L), eq("pico:front-gate-create"), anyMap(), eq(2001L)))
                .thenReturn(WorkflowTransitionResult.builder()
                        .instanceId(11L)
                        .previousState("DRAFT")
                        .newState("GATE_PASS_CREATED")
                        .assignedRole("ParkingSecurity")
                        .transitioned(true)
                        .build());

        WorkflowTransitionActionHandler h = new WorkflowTransitionActionHandler();
        ReflectionTestUtils.setField(h, "engine", engine);

        ActionResult r = h.handle(ActionInvocation.builder()
                .instanceId(11L)
                .pageCode("pico:front-gate-create")
                .formData(Map.of("vehicleNo", "TN-1"))
                .context(Map.of("actorUserId", 2001))
                .build());

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("fromState", "DRAFT");
        assertThat(r.getData()).containsEntry("toState", "GATE_PASS_CREATED");
        assertThat(r.getData()).containsEntry("transitioned", true);
        assertThat(r.getData()).containsEntry("assignedRole", "ParkingSecurity");
    }

    @Test
    void missingInstanceId_throws() {
        WorkflowTransitionActionHandler h = new WorkflowTransitionActionHandler();
        ReflectionTestUtils.setField(h, "engine", mock(WorkflowEngineService.class));
        assertThatThrownBy(() -> h.handle(ActionInvocation.builder().pageCode("x").build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingPageCode_throws() {
        WorkflowTransitionActionHandler h = new WorkflowTransitionActionHandler();
        ReflectionTestUtils.setField(h, "engine", mock(WorkflowEngineService.class));
        assertThatThrownBy(() -> h.handle(ActionInvocation.builder().instanceId(1L).build()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
