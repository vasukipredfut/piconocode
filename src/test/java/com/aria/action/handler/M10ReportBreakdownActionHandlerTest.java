package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M10ReportBreakdownActionHandler} — the
 * workflow-agnostic "vehicle breakdown" escape hatch. Covers both outcomes:
 * nothing loaded yet (closes the gate pass via {@link WorkflowEngineService#cancel}
 * + a direct {@code gate_pass} update) and already-loaded (refused, points
 * the operator at Reject Flow Unload).
 */
class M10ReportBreakdownActionHandlerTest {

    private JdbcTemplate jdbc;
    private WorkflowEngineService engine;
    private M10ReportBreakdownActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        engine = mock(WorkflowEngineService.class);
        handler = new M10ReportBreakdownActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        ReflectionTestUtils.setField(handler, "engine", engine);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isReportBreakdown() {
        assertThat(handler.code()).isEqualTo("m10.report-breakdown");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void instanceNotFound_errors() {
        when(jdbc.queryForMap(contains("FROM workflow_instance"), eq(1495L), eq(1L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ActionResult r = handler.handle(baseInvocation(Map.of()));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("not found");
        verifyNoInteractions(engine);
    }

    @Test
    void alreadyClosed_refuses() {
        stubInstance("M10.FGLoading", "EXIT_STAMPED", "CLOSED");

        ActionResult r = handler.handle(baseInvocation(Map.of()));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already closed");
        verifyNoInteractions(engine);
    }

    @Test
    void alreadyCancelled_refuses() {
        stubInstance("M10.FGLoading", "BAY_ASSIGNED", "CANCELLED");

        ActionResult r = handler.handle(baseInvocation(Map.of()));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already closed");
        verifyNoInteractions(engine);
    }

    @Test
    void stockAlreadyLoaded_refusesAndPointsToRejectFlowUnload() {
        stubInstance("M10.FGLoading", "BILLING_PENDING", "OPEN");
        stubLoadedRows(1);

        ActionResult r = handler.handle(baseInvocation(Map.of("reason", "engine failure")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already loaded").contains("Start Bay").contains("Stop Bay");
        verifyNoInteractions(engine);
        verify(jdbc, never()).update(contains("UPDATE gate_pass"), any(Object[].class));
    }

    @Test
    void nothingLoaded_cancelsInstanceAndClosesGatePass() {
        stubInstance("M10.FGLoading", "WEIGHMENT_1", "OPEN");
        stubLoadedRows(0);

        ActionResult r = handler.handle(baseInvocation(Map.of("reason", "tyre burst")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getMessage()).contains("closed");
        assertThat(r.getData()).containsEntry("closedFromState", "WEIGHMENT_1");
        verify(engine).cancel(eq(1495L), isNull(), contains("tyre burst"));
        verify(jdbc).update(contains("UPDATE gate_pass"), eq(1L), eq(1495L));
    }

    @Test
    void nothingLoaded_noReasonGiven_stillCancelsWithGenericReason() {
        stubInstance("M10.StockOutward", "UNLOADING_IN_PROGRESS", "OPEN");
        stubLoadedRows(0);

        ActionResult r = handler.handle(baseInvocation(Map.of()));

        assertThat(r.isOk()).isTrue();
        verify(engine).cancel(eq(1495L), isNull(), contains("vehicle breakdown"));
    }

    @Test
    void loadedRowsChecked_includesUnloadInProgressStatus() {
        stubInstance("M10.FGLoading", "BAY_ASSIGNED", "OPEN");
        // Simulate the loaded-rows count query itself so we can assert the
        // status list it filters on, rather than just stubbing a count.
        when(jdbc.queryForObject(argThat(sql -> sql != null
                        && sql.contains("loading_progress_detail_bay")
                        && sql.contains("'DONE','UNLOAD_IN_PROGRESS'")),
                eq(Integer.class), any(Object[].class)))
                .thenReturn(0);

        ActionResult r = handler.handle(baseInvocation(Map.of()));

        assertThat(r.isOk()).isTrue();
    }

    private ActionInvocation baseInvocation(Map<String, Object> formData) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(new HashMap<>(formData))
                .build();
    }

    private void stubInstance(String workflowCode, String currentState, String status) {
        Map<String, Object> row = new HashMap<>();
        row.put("workflow_code", workflowCode);
        row.put("current_state", currentState);
        row.put("status", status);
        when(jdbc.queryForMap(contains("FROM workflow_instance"), eq(1495L), eq(1L))).thenReturn(row);
    }

    private void stubLoadedRows(int count) {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }
}
