package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M10BayQuantityCompletionValidator}: blocks
 * "Completed" only while a bay is still open, never on the quantity totals
 * (that gate was deliberately removed — see the class javadoc), and only
 * for the 3 M10 workflows page 428 actually serves.
 */
class M10BayQuantityCompletionValidatorTest {

    private JdbcTemplate jdbc;
    private M10BayQuantityCompletionValidator validator;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        validator = new M10BayQuantityCompletionValidator(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void notWeighment2_isNoOp() {
        WorkflowInstance inst = instance(4821L, "M10.FGLoading");
        assertThatCode(() -> validator.validate(inst, "BAY_ASSIGNED", Map.of())).doesNotThrowAnyException();
        verifyNoInteractions(jdbc);
    }

    @Test
    void weighment2_butUnrelatedWorkflow_isNoOp() {
        WorkflowInstance inst = instance(4821L, "M6.SalesInvoice");
        assertThatCode(() -> validator.validate(inst, "WEIGHMENT_2", Map.of())).doesNotThrowAnyException();
        verifyNoInteractions(jdbc);
    }

    @Test
    void weighment2_scopedWorkflow_noOpenBays_allows() {
        WorkflowInstance inst = instance(4821L, "M10.FGLoading");
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1L), eq(4821L))).thenReturn(0);

        assertThatCode(() -> validator.validate(inst, "WEIGHMENT_2", Map.of())).doesNotThrowAnyException();

        verify(jdbc).queryForObject(
                eq("SELECT COUNT(*) FROM loading_progress_detail_bay WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'"),
                eq(Integer.class), eq(1L), eq(4821L));
    }

    @Test
    void weighment2_scopedWorkflow_zeroBaysUsedAtAll_allows() {
        // Same as "no open bays" from this validator's point of view — a
        // vehicle that never used Start Bay has count == 0 too, and this
        // feature is opt-in per vehicle.
        WorkflowInstance inst = instance(4821L, "M10.PackLoading");
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(0);

        assertThatCode(() -> validator.validate(inst, "WEIGHMENT_2", Map.of())).doesNotThrowAnyException();
    }

    @Test
    void weighment2_scopedWorkflow_openBayStillInProgress_rejects() {
        WorkflowInstance inst = instance(4821L, "M10.TankerOutbound");
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(2);

        assertThatThrownBy(() -> validator.validate(inst, "WEIGHMENT_2", Map.of()))
                .isInstanceOf(WorkflowValidationException.class)
                .hasMessageContaining("2 bay(s) still open");
    }

    @Test
    void doesNotGateOnQuantityTotals_onlyOnOpenBays() {
        // No target_qty/processed_qty comparison exists in this validator at
        // all anymore — proven here by the fact that supplying formData that
        // would represent a huge shortfall changes nothing: only openCount
        // (mocked to 0) determines the outcome.
        WorkflowInstance inst = instance(4821L, "M10.FGLoading");
        when(jdbc.queryForObject(anyString(), eq(Integer.class), any(), any())).thenReturn(0);

        assertThatCode(() -> validator.validate(inst, "WEIGHMENT_2",
                Map.of("order_sku_line_items", java.util.List.of()))).doesNotThrowAnyException();
    }

    private WorkflowInstance instance(Long id, String workflowCode) {
        return WorkflowInstance.builder().id(id).workflowCode(workflowCode).currentState("LOADING_IN_PROGRESS").build();
    }
}
