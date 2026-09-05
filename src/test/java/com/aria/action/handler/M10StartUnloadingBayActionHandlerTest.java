package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
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
 * Unit test for {@link M10StartUnloadingBayActionHandler}: M10.PackUnload's
 * receiving-side "Start Bay" button. Unlike {@link M10StartUnloadBayActionHandlerTest}
 * (the Reject Flow Unload sibling) this handler requires no prior loaded row
 * and, since it opens conveyor {@code dispatch_task} rows the reject-flow
 * sibling doesn't, also requires {@code scanning_status}/{@code conveyor_mode}
 * per line to decide whether one is opened (same gating as
 * {@link M10StartBayActionHandlerTest}).
 */
class M10StartUnloadingBayActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10StartUnloadingBayActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(contains("FROM workflow_instance"), any(Object[].class)))
                .thenReturn(Map.of("workflow_code", "M10.PackUnload", "current_state", "UNLOADING_IN_PROGRESS"));
        handler = new M10StartUnloadingBayActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10StartUnloadingBay() {
        assertThat(handler.code()).isEqualTo("m10.start-unloading-bay");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingBayName_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("formData.bay_name is required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void bayNameOutOfRange_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay13", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12").contains("Bay13");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLineItemsArray_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1")));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_sku_line_items must be a non-empty array");
    }

    @Test
    void lineItemMissingSkuCode_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("quantity", 20);
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing route_id or sku_code");
    }

    @Test
    void lineItemNegativeQty_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        badLine.put("quantity", -5);
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU1215").contains("quantity >= 0");
    }

    @Test
    void lineItemMissingScanningStatus_errors() {
        Map<String, Object> badLine = line("ROU028912", "SKU1215", 20);
        badLine.remove("scanning_status");
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU1215").contains("missing scanning_status");
        verify(jdbc, never()).update(contains("INSERT INTO unloading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void lineItemMissingConveyorMode_errors() {
        Map<String, Object> badLine = line("ROU028912", "SKU1215", 20);
        badLine.remove("conveyor_mode");
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU1215").contains("missing conveyor_mode");
    }

    @Test
    void alreadyOpenUnloadBayForInstance_refusesToStartAnother() {
        stubOpenUnloadingBayCount(1);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already has an open unload bay");
    }

    @Test
    void barcodeConveyorLine_opensUnloadBayRowAndDispatchTask() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "vehicle_no", "TN86J8070", "gpNumber", "GP-1-1495", "bay_name", "Bay1",
                "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("bayName", "Bay1");
        assertThat(r.getData()).containsEntry("totalQuantity", new BigDecimal("20"));
        assertThat(r.getData()).containsEntry("dispatchTasksSkipped", 0);

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"), eq(new BigDecimal("20")), isNull());

        // One dispatch_task row, type='UnLoading', stamped with the same
        // fixed conveyor workflow code as the loading side.
        verify(jdbc, times(1)).update(contains("INSERT INTO dispatch_task"), any(Object[].class));
        verify(jdbc).update(contains("'UnLoading'"),
                eq(1L), eq("M12.ConveyorTest"), eq(1495L), eq("Bay1"), eq("ROU028912"), isNull(),
                anyString(), isNull(), eq("SKU1215"), eq("GP-1-1495"), eq(new BigDecimal("20")));
    }

    @Test
    void withoutConveyorLine_skipsDispatchTask() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        Map<String, Object> l = line("ROU028912", "SKU1215", 20);
        l.put("conveyor_mode", "WITHOUT_CONVEYOR");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(l))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("dispatchTasksSkipped", 1);
        verify(jdbc, never()).update(contains("INSERT INTO dispatch_task"), any(Object[].class));
        // the unloading_progress_detail_bay row is still opened regardless
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void withoutBarcodeLine_skipsDispatchTaskEvenOnConveyor() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        Map<String, Object> l = line("ROU028912", "SKU1215", 20);
        l.put("scanning_status", "WITHOUT_BARCODE");
        l.put("conveyor_mode", "CONVEYOR_MANUAL_SELECT");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(l))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("dispatchTasksSkipped", 1);
        verify(jdbc, never()).update(contains("INSERT INTO dispatch_task"), any(Object[].class));
    }

    @Test
    void multipleSkuLines_mixedGating_onlyConveyorLineGetsDispatchTask() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        Map<String, Object> line1 = line("ROU028912", "SKU1215", 20);
        Map<String, Object> line2 = line("ROU028912", "SKU1461", 30);
        line2.put("conveyor_mode", "WITHOUT_CONVEYOR");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line1, line2))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("lineCount", 2);
        assertThat(r.getData()).containsEntry("totalQuantity", new BigDecimal("50"));
        assertThat(r.getData()).containsEntry("dispatchTasksSkipped", 1);

        verify(jdbc, times(2)).update(contains("INSERT INTO unloading_progress_detail_bay"), any(Object[].class));
        verify(jdbc, times(1)).update(contains("INSERT INTO dispatch_task"), any(Object[].class));
    }

    private ActionInvocation baseInvocation(Map<String, Object> extraFormData) {
        Map<String, Object> formData = new LinkedHashMap<>(extraFormData);
        return ActionInvocation.builder().instanceId(1495L).formData(formData).build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int quantity) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("sku_desc", skuCode + " desc");
        line.put("quantity", quantity);
        line.put("scanning_status", "BARCODE");
        line.put("conveyor_mode", "CONVEYOR");
        return line;
    }

    private void stubOpenUnloadingBayCount(int count) {
        when(jdbc.queryForObject(contains("unloading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }

    private void stubParentId(Long id) {
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(id);
    }
}
