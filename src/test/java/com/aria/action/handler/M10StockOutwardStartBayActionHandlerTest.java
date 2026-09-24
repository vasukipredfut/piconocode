package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import com.aria.workflow.dto.WorkflowTransitionResult;
import com.aria.workflow.engine.WorkflowEngineService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M10StockOutwardStartBayActionHandler} — same
 * mechanics as {@link M10StartBayActionHandler} (verified: every
 * validation/error message here is byte-for-byte identical to the shared
 * handler), so this file focuses on the delta: capturing {@code rd_code}/
 * {@code vendor_name} (order-sheet destination code) per line onto the
 * opened row, for {@link M10StockOutwardBayCompleteActionHandler} to read
 * back later as the stock movement's {@code to_bin}.
 */
class M10StockOutwardStartBayActionHandlerTest {

    private JdbcTemplate jdbc;
    private WorkflowEngineService engine;
    private M10StockOutwardStartBayActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        engine = mock(WorkflowEngineService.class);
        handler = new M10StockOutwardStartBayActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        ReflectionTestUtils.setField(handler, "engine", engine);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isStockOutwardStartBay() {
        assertThat(handler.code()).isEqualTo("m10.stockoutward-start-bay");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().pageCode("aria:loading-in-progress-entry").build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void bayNameOutOfRange_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay13", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12").contains("Bay13");
        verifyNoInteractions(jdbc, engine);
    }

    @Test
    void alreadyOpenBayForInstance_refusesToStartAnother() {
        stubOpenBayCount(1);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay2", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already has an open bay");
        verifyNoInteractions(engine);
    }

    @Test
    void wrongWorkflowState_refusesToStart() {
        stubOpenBayCount(0);
        stubCurrentState("WEIGHMENT_2");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("cannot start a bay from workflow state WEIGHMENT_2");
        verifyNoInteractions(engine);
    }

    // ---- the delta: rd_code / vendor_name capture ----------------------------

    @Test
    void lineWithRdCodeAndVendorName_areStoredOnLoadingBayRow() {
        stubOpenBayCount(0);
        stubCurrentState("BAY_ASSIGNED");
        stubParentId(777L);

        Map<String, Object> l = line("ROU028912", "SKU1215", 45);
        l.put("rd_code", "CUST001");
        l.put("vendor_name", "XYZ Distributors");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(l))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"),
                eq(new BigDecimal("45")), isNull(), isNull(), eq("CUST001"), eq("XYZ Distributors"));
    }

    @Test
    void godownAndPlantCode_alsoCapturedAlongsideDestinationFields() {
        stubOpenBayCount(0);
        stubCurrentState("BAY_ASSIGNED");
        stubParentId(777L);

        Map<String, Object> l = line("ROU028912", "SKU1215", 45);
        l.put("rd_code", "CUST001");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "plant_code", "SR0001", "godown_code", "SRG0001",
                "order_sku_line_items", List.of(l))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"),
                eq(new BigDecimal("45")), eq("SRG0001"), eq("SR0001"), eq("CUST001"), isNull());
    }

    @Test
    void firstBay_fromLoadingInProgress_firesTransitionAndOpensRows() {
        stubOpenBayCount(0);
        stubCurrentState("LOADING_IN_PROGRESS");
        stubParentId(777L);
        when(engine.handlePageSubmission(eq(1495L), eq("aria:loading-in-progress-entry"), anyMap(), any()))
                .thenReturn(WorkflowTransitionResult.builder()
                        .instanceId(1495L).previousState("LOADING_IN_PROGRESS").newState("BAY_ASSIGNED")
                        .transitioned(true).build());

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "vehicle_no", "TN86J8070", "gpNumber", "GP-1-1495", "bay_name", "Bay1",
                "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 45)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("toState", "BAY_ASSIGNED");
        assertThat(r.getData()).containsEntry("transitioned", true);
        verify(engine, times(1)).handlePageSubmission(eq(1495L), eq("aria:loading-in-progress-entry"),
                argThat(fd -> "start".equals(fd.get("bayAction"))), any());
    }

    private ActionInvocation baseInvocation(Map<String, Object> extraFormData) {
        Map<String, Object> formData = new LinkedHashMap<>();
        formData.put("route_id", "ROU028912");
        formData.putAll(extraFormData);
        return ActionInvocation.builder()
                .instanceId(1495L)
                .pageCode("aria:loading-in-progress-entry")
                .formData(formData)
                .build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int orderQty) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("sku_desc", skuCode + " desc");
        line.put("order_qty", orderQty);
        line.put("scanning_status", "BARCODE");
        line.put("conveyor_mode", "CONVEYOR");
        return line;
    }

    private void stubOpenBayCount(int count) {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }

    private void stubCurrentState(String state) {
        when(jdbc.queryForObject(contains("current_state"), eq(String.class), any(Object[].class)))
                .thenReturn(state);
    }

    private void stubParentId(Long id) {
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(id);
    }
}
