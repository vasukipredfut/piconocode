package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoadingTotalAfterSkuRejectListenerTest {

    private JdbcTemplate jdbc;
    private LoadingTotalAfterSkuRejectListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new LoadingTotalAfterSkuRejectListener(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static WorkflowInstance inst(String code, String state) {
        return WorkflowInstance.builder().id(2028L).workflowCode(code).currentState(state).build();
    }

    @Test
    void qcPass_recomputesTotalExcludingRejectedRows() {
        listener.onTransition(inst("M10.TankerOutbound", "WEIGHMENT_2"), "LOADING_COMPLETED", Map.of());

        verify(jdbc).update(argThat((String sql) -> sql.startsWith("UPDATE loading_progress_detail SET total_quantity")
                        && sql.contains("b.status <> 'REJECTED'")),
                eq(1L), eq(2028L), eq(1L), eq(2028L));
    }

    @Test
    void otherTransitionsOrWorkflows_doNothing() {
        listener.onTransition(inst("M10.TankerOutbound", "QC_SAMPLE_APPROVED"), "LOADING_COMPLETED", Map.of());
        listener.onTransition(inst("M10.TankerOutbound", "QC_SAMPLE_REJECTED"), "LOADING_COMPLETED", Map.of());
        listener.onTransition(inst("M10.FGLoading", "WEIGHMENT_2"), "LOADING_COMPLETED", Map.of());
        verifyNoInteractions(jdbc);
        assertThat(true).isTrue();
    }
}
