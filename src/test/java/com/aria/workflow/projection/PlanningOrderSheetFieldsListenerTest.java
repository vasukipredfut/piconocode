package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlanningOrderSheetFieldsListenerTest {

    private static final String PAGE = "aria:loading-planning-order-sheet-upload";
    private JdbcTemplate jdbc;
    private PlanningOrderSheetFieldsListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new PlanningOrderSheetFieldsListener(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static WorkflowInstance inst(String code, String state) {
        return WorkflowInstance.builder().id(2019L).workflowCode(code).currentState(state).build();
    }

    private static Map<String, Object> bag(Map<String, Object> page) {
        Map<String, Object> b = new HashMap<>();
        b.put(PAGE, page);
        return b;
    }

    private Object[] updateCall() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update")).findFirst().orElseThrow().getArguments();
    }

    @Test
    void wrongWorkflowOrTransition_doesNothing() {
        Map<String, Object> page = Map.of("vehicle_no", "TN1", "remark", "x");
        listener.onTransition(inst("M10.FGLoading", "DISPATCH_LOADING_PLANNING"), "DISPATCH_VEHICLE_ENTRY", bag(page));
        listener.onTransition(inst("M10.TankerOutbound", "QC_PRELOAD_APPROVED"), "DISPATCH_LOADING_PLANNING", bag(page));
        verifyNoInteractions(jdbc);
    }

    @Test
    void savesAllFiveFieldsOntoVehiclesActiveRows() {
        Map<String, Object> page = new HashMap<>();
        page.put("vehicle_no", "TN01DS0022");
        page.put("po_execution", "EXECUTED");
        page.put("payment_type", "CREDIT");
        page.put("loading_type", "LOADING");
        page.put("delivery_datetime", "2026-10-08T14:30");
        page.put("remark", "  handle with care ");

        listener.onTransition(inst("M10.TankerOutbound", "DISPATCH_LOADING_PLANNING"), "DISPATCH_VEHICLE_ENTRY", bag(page));

        Object[] a = updateCall();
        assertThat((String) a[0]).startsWith("UPDATE m5_order_dispatch_detail SET")
                .contains("po_execution = ?", "payment_type = ?", "loading_type = ?", "delivery_datetime = ?", "remark = ?")
                .endsWith("WHERE tenant_id = ? AND vehicle_no = ? AND vehicle_status = 'active'");
        assertThat(List.of(a).subList(1, a.length)).containsExactlyInAnyOrder(
                "EXECUTED", "CREDIT", "LOADING", Timestamp.valueOf(LocalDateTime.of(2026, 10, 8, 14, 30)),
                "handle with care", 1L, "TN01DS0022");
    }

    @Test
    void onlyKeysPresentAreWritten_blankBecomesNull() {
        Map<String, Object> page = new HashMap<>();
        page.put("vehicle_no", "TN1");
        page.put("remark", "");

        listener.onTransition(inst("M10.TankerOutbound", "DISPATCH_LOADING_PLANNING"), "DISPATCH_VEHICLE_ENTRY", bag(page));

        Object[] a = updateCall();
        assertThat((String) a[0]).contains("remark = ?").doesNotContain("po_execution").doesNotContain("delivery_datetime");
        assertThat(a[1]).isNull();
    }

    @Test
    void noFieldsSubmitted_noUpdate() {
        listener.onTransition(inst("M10.TankerOutbound", "DISPATCH_LOADING_PLANNING"), "DISPATCH_VEHICLE_ENTRY",
                bag(new HashMap<>(Map.of("vehicle_no", "TN1"))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void vehicleNoMissingOnPage_fallsBackToGatePass() {
        when(jdbc.queryForList(contains("FROM gate_pass"), eq(String.class), any(Object[].class))).thenReturn(List.of("TN-GP"));
        Map<String, Object> page = new HashMap<>(Map.of("payment_type", "ADVANCE"));

        listener.onTransition(inst("M10.TankerOutbound", "DISPATCH_LOADING_PLANNING"), "DISPATCH_VEHICLE_ENTRY", bag(page));

        Object[] a = updateCall();
        assertThat(a[a.length - 1]).isEqualTo("TN-GP");
    }

    @Test
    void camelCaseKeysAndOffsetDateTimeAccepted() {
        Map<String, Object> page = new HashMap<>();
        page.put("vehicleNo", "TN1");
        page.put("paymentType", "CASH");
        page.put("deliveryDatetime", "2026-10-08T09:00:00+05:30");

        listener.onTransition(inst("M10.TankerOutbound", "DISPATCH_LOADING_PLANNING"), "DISPATCH_VEHICLE_ENTRY", bag(page));

        Object[] a = updateCall();
        assertThat(List.of(a)).contains("CASH", Timestamp.valueOf(LocalDateTime.of(2026, 10, 8, 9, 0)));
    }
}
