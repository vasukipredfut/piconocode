package com.aria.m10.handscanner.service;

import com.aria.action.ActionDispatcher;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HandScannerService}. Mock JdbcTemplate and
 * ActionDispatcher; stub queryForList per-position typed matchers — same
 * convention as ParkingBayScanServiceTest (array-any() vararg matching does
 * not reliably match queryForList in this repo's Mockito setup). Unstubbed
 * queryForList calls fall back to Mockito's default empty-list answer, so
 * only the queries relevant to each scenario are stubbed explicitly.
 */
class HandScannerServiceTest {

    private JdbcTemplate jdbc;
    private ActionDispatcher actions;
    private HandScannerService service;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        actions = mock(ActionDispatcher.class);
        service = new HandScannerService(jdbc, actions);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void scanGatePass_unknownGpNumber_returnsErrorWithoutAssigningBay() {
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of());

        Map<String, Object> result = service.scanGatePass("GP-DOES-NOT-EXIST");

        assertThat(result).containsExactly(Map.entry("error", "gate pass not found: GP-DOES-NOT-EXIST"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_noLinkedWorkflowInstance_bayAssignmentNotAttempted() {
        Map<String, Object> gatePass = gatePassRow(null, "TN01AB1234");
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));

        Map<String, Object> result = service.scanGatePass("GP-1");

        assertThat(result.get("gatePass")).isEqualTo(gatePass);
        assertThat(result.get("workflowInstance")).isNull();
        assertThat(result.get("bayAssignment")).isEqualTo(
                Map.of("attempted", false, "reason", "gate pass has no linked workflow instance yet"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_workflowInstanceRowMissing_treatsCurrentStateAsNull() {
        Map<String, Object> gatePass = gatePassRow(1624L, "TN01AB1234");
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of());

        Map<String, Object> result = service.scanGatePass("GP-1");

        assertThat(result.get("workflowInstance")).isNull();
        assertThat(result.get("bayAssignment")).isEqualTo(Map.of("attempted", false, "reason",
                "workflow is not ready for bay assignment (current state: null, expected LOADING_IN_PROGRESS)"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_workflowNotLoadingInProgress_bayAssignmentNotAttempted() {
        Map<String, Object> gatePass = gatePassRow(1624L, "TN01AB1234");
        Map<String, Object> workflowInstance = workflowInstanceRow(1624L, "GATE_PASS_OUT_CLOSED");
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of(workflowInstance));

        Map<String, Object> result = service.scanGatePass("GP-1");

        assertThat(result.get("workflowInstance")).isEqualTo(workflowInstance);
        assertThat(result.get("orderDispatchDetails")).isEqualTo(List.of());
        assertThat(result.get("bayAssignment")).isEqualTo(Map.of("attempted", false, "reason",
                "workflow is not ready for bay assignment (current state: GATE_PASS_OUT_CLOSED, "
                        + "expected LOADING_IN_PROGRESS)"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_loadingInProgressButNoOrderDispatchRows_bayAssignmentNotAttempted() {
        Map<String, Object> gatePass = gatePassRow(1493L, "TN28AM6489");
        Map<String, Object> workflowInstance = workflowInstanceRow(1493L, "LOADING_IN_PROGRESS");
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of(workflowInstance));
        when(jdbc.queryForList(contains("FROM m5_order_dispatch_detail"), any(Long.class), any(String.class)))
                .thenReturn(List.of());

        Map<String, Object> result = service.scanGatePass("GP-1-1493");

        assertThat(result.get("bayAssignment")).isEqualTo(Map.of("attempted", false, "reason",
                "no order dispatch details found for vehicle TN28AM6489"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_orderDispatchRowsMissingRouteIdOrSku_bayAssignmentNotAttempted() {
        Map<String, Object> gatePass = gatePassRow(1493L, "TN28AM6489");
        Map<String, Object> workflowInstance = workflowInstanceRow(1493L, "LOADING_IN_PROGRESS");
        Map<String, Object> incompleteRow = orderDispatchRow(null, "SKU1436", "REF SUNFLOWER OIL",
                null, new BigDecimal("12000"));
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of(workflowInstance));
        when(jdbc.queryForList(contains("FROM m5_order_dispatch_detail"), any(Long.class), any(String.class)))
                .thenReturn(List.of(incompleteRow));

        Map<String, Object> result = service.scanGatePass("GP-1-1493");

        assertThat(result.get("bayAssignment")).isEqualTo(
                Map.of("attempted", false, "reason", "order dispatch rows are missing route_id/sku_code"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_noFreeBayAvailable_bayAssignmentNotAttempted() {
        Map<String, Object> gatePass = gatePassRow(1493L, "TN28AM6489");
        Map<String, Object> workflowInstance = workflowInstanceRow(1493L, "LOADING_IN_PROGRESS");
        Map<String, Object> row = orderDispatchRow("ROU028934", "SKU1436", "REF SUNFLOWER OIL",
                new BigDecimal("0"), new BigDecimal("12000"));
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of(workflowInstance));
        when(jdbc.queryForList(contains("FROM m5_order_dispatch_detail"), any(Long.class), any(String.class)))
                .thenReturn(List.of(row));
        when(jdbc.queryForList(contains("bay_name"), eq(String.class), any(Long.class)))
                .thenReturn(allBays());

        Map<String, Object> result = service.scanGatePass("GP-1-1493");

        assertThat(result.get("bayAssignment")).isEqualTo(Map.of("attempted", false, "reason",
                "no free bay available (Bay1..Bay12 all occupied)"));
        verifyNoInteractions(actions);
    }

    @Test
    void scanGatePass_freeBayFound_dispatchesStartBayActionAndReturnsItsResult() {
        Map<String, Object> gatePass = gatePassRow(1493L, "TN28AM6489");
        Map<String, Object> workflowInstance = workflowInstanceRow(1493L, "LOADING_IN_PROGRESS");
        // Row 1: remaining_qty present (and zero) -> line item qty must come from remaining_qty, not order_qty.
        Map<String, Object> row1 = orderDispatchRow("ROU028934", "SKU1436", "REF SUNFLOWER OIL",
                new BigDecimal("0"), new BigDecimal("12000"));
        // Row 2: no remaining_qty -> line item qty falls back to order_qty.
        Map<String, Object> row2 = orderDispatchRow("ROU029019", "SKU1365", "SUPER OLIEN OIL",
                null, new BigDecimal("6000"));
        // Row 3: missing sku_code -> filtered out of the line items sent to the action.
        Map<String, Object> row3 = orderDispatchRow("ROU029099", null, "UNUSABLE ROW",
                new BigDecimal("100"), new BigDecimal("100"));
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of(workflowInstance));
        when(jdbc.queryForList(contains("FROM m5_order_dispatch_detail"), any(Long.class), any(String.class)))
                .thenReturn(List.of(row1, row2, row3));
        when(jdbc.queryForList(contains("bay_name"), eq(String.class), any(Long.class)))
                .thenReturn(List.of("Bay1", "Bay2", "Bay3", "Bay4"));
        ActionResult rejected = ActionResult.error("order_sku_line_items[0] (SKU1436) needs order_qty > 0");
        when(actions.dispatch(eq("m10.start-bay"), any(ActionInvocation.class))).thenReturn(rejected);

        Map<String, Object> result = service.scanGatePass("GP-1-1493");

        Map<String, Object> bayAssignment = (Map<String, Object>) result.get("bayAssignment");
        assertThat(bayAssignment.get("attempted")).isEqualTo(true);
        assertThat(bayAssignment.get("bayName")).isEqualTo("Bay5");
        assertThat(bayAssignment.get("ok")).isEqualTo(false);
        assertThat(bayAssignment.get("message")).isEqualTo("order_sku_line_items[0] (SKU1436) needs order_qty > 0");
        assertThat(bayAssignment.get("data")).isNull();

        ArgumentCaptor<ActionInvocation> captor = ArgumentCaptor.forClass(ActionInvocation.class);
        verify(actions).dispatch(eq("m10.start-bay"), captor.capture());
        ActionInvocation invocation = captor.getValue();
        assertThat(invocation.getInstanceId()).isEqualTo(1493L);
        assertThat(invocation.getPageCode()).isEqualTo("aria:loading-in-progress-entry");
        Map<String, Object> formData = invocation.getFormData();
        assertThat(formData.get("vehicle_no")).isEqualTo("TN28AM6489");
        assertThat(formData.get("gpNumber")).isEqualTo("GP-1-1493");
        assertThat(formData.get("bay_name")).isEqualTo("Bay5");
        List<Map<String, Object>> lineItems = (List<Map<String, Object>>) formData.get("order_sku_line_items");
        assertThat(lineItems).hasSize(2);
        assertThat(lineItems.get(0)).isEqualTo(Map.of(
                "route_id", "ROU028934", "sku_code", "SKU1436", "sku_desc", "REF SUNFLOWER OIL",
                "order_qty", new BigDecimal("0")));
        assertThat(lineItems.get(1)).isEqualTo(Map.of(
                "route_id", "ROU029019", "sku_code", "SKU1365", "sku_desc", "SUPER OLIEN OIL",
                "order_qty", new BigDecimal("6000")));
    }

    @Test
    void scanGatePass_startBayActionSucceeds_bayAssignmentOkTrueWithData() {
        Map<String, Object> gatePass = gatePassRow(1493L, "TN28AM6489");
        Map<String, Object> workflowInstance = workflowInstanceRow(1493L, "LOADING_IN_PROGRESS");
        Map<String, Object> row = orderDispatchRow("ROU028934", "SKU1436", "REF SUNFLOWER OIL",
                new BigDecimal("500"), new BigDecimal("12000"));
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePass));
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Long.class), any(Long.class)))
                .thenReturn(List.of(workflowInstance));
        when(jdbc.queryForList(contains("FROM m5_order_dispatch_detail"), any(Long.class), any(String.class)))
                .thenReturn(List.of(row));
        when(jdbc.queryForList(contains("bay_name"), eq(String.class), any(Long.class)))
                .thenReturn(List.of());
        Map<String, Object> data = Map.of("loadingProgressDetailBayId", 501L);
        ActionResult ok = ActionResult.builder().ok(true).message(null).data(data).build();
        when(actions.dispatch(eq("m10.start-bay"), any(ActionInvocation.class))).thenReturn(ok);

        Map<String, Object> result = service.scanGatePass("GP-1-1493");

        Map<String, Object> bayAssignment = (Map<String, Object>) result.get("bayAssignment");
        assertThat(bayAssignment.get("attempted")).isEqualTo(true);
        assertThat(bayAssignment.get("bayName")).isEqualTo("Bay1");
        assertThat(bayAssignment.get("ok")).isEqualTo(true);
        assertThat(bayAssignment.get("data")).isEqualTo(data);
    }

    // ---- helpers -------------------------------------------------------------

    private static Map<String, Object> gatePassRow(Long workflowInstanceId, String vehicleNo) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 459L);
        row.put("gp_number", "GP-1-1493");
        row.put("vehicle_no", vehicleNo);
        row.put("status", "LOADING_IN_PROGRESS");
        row.put("workflow_instance_id", workflowInstanceId);
        return row;
    }

    private static Map<String, Object> workflowInstanceRow(long id, String currentState) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("workflow_code", "M10.FGLoading");
        row.put("current_state", currentState);
        row.put("status", "OPEN".equals(currentState) ? "OPEN" : "CLOSED");
        row.put("assigned_role", "WeighmentOfficer");
        row.put("business_ref", "TN28AM6489");
        return row;
    }

    private static Map<String, Object> orderDispatchRow(String routeId, String skuCode, String skuDesc,
                                                          BigDecimal remainingQty, BigDecimal orderQty) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("route_id", routeId);
        row.put("sku_code", skuCode);
        row.put("sku_desc", skuDesc);
        row.put("remaining_qty", remainingQty);
        row.put("order_qty", orderQty);
        return row;
    }

    private static List<String> allBays() {
        return List.of("Bay1", "Bay2", "Bay3", "Bay4", "Bay5", "Bay6", "Bay7", "Bay8", "Bay9", "Bay10", "Bay11",
                "Bay12");
    }
}
