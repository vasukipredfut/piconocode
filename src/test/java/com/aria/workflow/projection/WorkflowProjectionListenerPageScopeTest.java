package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * A page-scoped projection row must take its fields from the triggering page's OWN submission first --
 * qc_final_outbound_detail.status used to be saved as another page's "APPROVED" although the QC had just
 * rejected on aria:qc-final-out.
 */
class WorkflowProjectionListenerPageScopeTest {

    private JdbcTemplate jdbc;
    private WorkflowProjectionListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new WorkflowProjectionListener(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private void config(String pageCode) {
        Map<String, Object> cfg = new LinkedHashMap<>();
        cfg.put("target_table", "qc_final_outbound_detail");
        cfg.put("key_column", "workflow_instance_id");
        cfg.put("state_columns", "current_step");
        cfg.put("variant_column", "flow_variant_code");
        cfg.put("page_code", pageCode);
        cfg.put("field_map_json", "{\"qc_notes\": [\"qc_notes\"], \"status\": [\"status\"]}");
        when(jdbc.queryForList(contains("m0_workflow_projection"), any(Object[].class))).thenReturn(List.of(cfg));
        // the triggering page (LOADING_COMPLETED -> UNLOADING_WEIGHMENT) is aria:qc-final-out
        when(jdbc.queryForObject(contains("workflow_definition"), eq(String.class), any(Object[].class)))
                .thenReturn("{\"transitions\":[{\"from\":\"LOADING_COMPLETED\",\"to\":\"UNLOADING_WEIGHMENT\",\"pageCode\":\"aria:qc-final-out\"}]}");
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(1); // row exists
    }

    @Test
    void qcFinalOut_rejectedByTheQc_isNotOverwrittenByAnotherPagesApproved() {
        config("aria:qc-final-out");
        // another page ("aria:aaa-earlier") comes FIRST in the bag and also carries a status
        Map<String, Object> bag = new LinkedHashMap<>();
        bag.put("aria:aaa-earlier", new HashMap<>(Map.of("status", "APPROVED", "qc_notes", "old note")));
        bag.put("aria:qc-final-out", new HashMap<>(Map.of("status", "REJECTED", "qc_notes", "oil is cloudy")));
        WorkflowInstance inst = WorkflowInstance.builder().id(2028L).workflowCode("M10.TankerOutbound")
                .currentState("UNLOADING_WEIGHMENT").workflowVersion(105).tenantId(1L).build();

        listener.onTransition(inst, "LOADING_COMPLETED", bag);

        Object[] u = mockingDetails(jdbc).getInvocations().stream()
                .filter(inv -> inv.getMethod().getName().equals("update")).findFirst().orElseThrow().getArguments();
        org.assertj.core.api.Assertions.assertThat((String) u[0]).startsWith("UPDATE qc_final_outbound_detail SET");
        List<Object> args = java.util.Arrays.asList(u);
        org.assertj.core.api.Assertions.assertThat(args).contains("REJECTED", "oil is cloudy").doesNotContain("APPROVED", "old note");
    }
}
