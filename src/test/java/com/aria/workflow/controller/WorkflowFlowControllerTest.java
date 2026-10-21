package com.aria.workflow.controller;

import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The flow tracker shows the first-time journey; re-entry / reject routes stay off the main line but stay in `next`. */
class WorkflowFlowControllerTest {

    private WorkflowFlowController controller;

    @BeforeEach
    void setUp() { TenantContext.set(1L); }

    @SuppressWarnings("unchecked")
    private WorkflowFlowController controllerFor(String resource) throws Exception {
        String body;
        try (var in = getClass().getResourceAsStream(resource)) {
            body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.query(contains("FROM workflow_definition"), any(RowMapper.class), any(Object[].class))).thenReturn(List.of(body));
        return new WorkflowFlowController(jdbc);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @SuppressWarnings("unchecked")
    @Test
    void tankerOutbound_mainLine_isTheFullFirstTimeJourney_withTheWeightPage() throws Exception {
        controller = controllerFor("/workflow/M10.TankerOutbound.json");
        Map<String, Object> out = controller.flow("M10.TankerOutbound", "DISPATCH_VEHICLE_ENTRY").getBody();

        List<Map<String, Object>> steps = (List<Map<String, Object>>) out.get("steps");
        assertThat(steps).extracting(s -> s.get("state")).containsExactly(
                "DRAFT", "DISPATCH_VEHICLE_ENTRY", "DISPATCH_LOADING_PLANNING", "QC_PRELOAD_APPROVED", "LOADING_COMPLETED",
                "WEIGHMENT_2", "WEIGHT_DIFF_OK", "BILLING_DONE", "QC_FINAL_CHECKED", "DISPATCH_VERIFIED", "WEIGHMENT_3",
                "READY_FOR_EXIT", "GATE_PASS_OUT_CLOSED");
        // QC pre-load approved -> loading completed is done by the weight page
        assertThat(steps.get(3).get("pageCode")).isEqualTo("aria:weighment-capture");
        assertThat(out.get("currentIndex")).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void next_stillListsEveryTransitionOutOfTheState() throws Exception {
        controller = controllerFor("/workflow/M10.TankerOutbound.json");
        List<Map<String, Object>> next = (List<Map<String, Object>>)
                controller.flow("M10.TankerOutbound", "DISPATCH_LOADING_PLANNING").getBody().get("next");

        assertThat(next).extracting(n -> n.get("to")).containsExactly("QC_PRELOAD_APPROVED", "LOADING_COMPLETED", "QC_SAMPLE_APPROVED");
    }

    @SuppressWarnings("unchecked")
    @Test
    void tankerInbound_mainLine_isUnchanged() throws Exception {
        controller = controllerFor("/workflow/M10.TankerInbound.json");

        List<Map<String, Object>> steps = (List<Map<String, Object>>) controller.flow("M10.TankerInbound", "WEIGHMENT_1").getBody().get("steps");

        assertThat(steps).extracting(x -> x.get("state")).containsExactly(
                "DRAFT", "DISPATCH_QUEUED", "WEIGHMENT_1", "WEIGHMENT_2", "DOC_VERIFICATION_PENDING", "QTY_RECONCILED", "WEIGHMENT_3", "CLOSED");
    }
}
