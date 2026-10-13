package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QcSampleRejectListenerTest {

    private JdbcTemplate jdbc;
    private QcSampleRejectListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new QcSampleRejectListener(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static WorkflowInstance inst(String code) {
        return WorkflowInstance.builder().id(2028L).workflowCode(code).currentState("UNLOADING_WEIGHMENT").build();
    }

    private static Map<String, Object> bag(String status) {
        return Map.of("aria:qc-final-out", Map.of("status", status));
    }

    @Test
    void fullReject_marksUnweighedRowsRejected_andRecomputesTotal() {
        listener.onTransition(inst("M10.TankerOutbound"), "LOADING_COMPLETED", bag("REJECTED"));

        verify(jdbc).update(eq("UPDATE loading_progress_detail_bay SET status = 'REJECTED' "
                + "WHERE tenant_id = ? AND workflow_instance_id = ? AND status = 'IN_PROGRESS'"), eq(1L), eq(2028L));
        verify(jdbc).update(startsWith("UPDATE loading_progress_detail SET total_quantity"), eq(1L), eq(2028L), eq(1L), eq(2028L));
    }

    @Test
    void passOrSkuWeighmentOrOtherWorkflow_doNothing() {
        listener.onTransition(inst("M10.TankerOutbound"), "LOADING_COMPLETED", bag("APPROVED"));
        listener.onTransition(inst("M10.TankerOutbound"), "LOADING_COMPLETED", bag("SKU_WEIGHMENT"));
        listener.onTransition(inst("M10.TankerOutbound"), "WEIGHMENT_2", bag("REJECTED"));
        listener.onTransition(inst("M10.FGLoading"), "LOADING_COMPLETED", bag("REJECTED"));
        listener.onTransition(inst("M10.TankerOutbound"), "LOADING_COMPLETED", Map.of());
        verifyNoInteractions(jdbc);
    }
}
