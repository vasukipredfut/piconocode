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
 * Unit test for {@link M10UnloadingBayCompleteActionHandler}: M10.PackUnload's
 * receiving-side "Bay Complete" button. Closes every open
 * unloading_progress_detail_bay row named in formData.order_sku_line_items,
 * matched by (route_id, order_id, sku_code), posts a RECEIPT stock movement
 * per line, and force-closes the matching conveyor dispatch_task row(s)
 * {@link M10StartUnloadingBayActionHandler} opened — same reconciliation
 * {@link M10BayCompleteActionHandlerTest} covers on the loading side.
 */
class M10UnloadingBayCompleteActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10UnloadingBayCompleteActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(Map.of("id", 1L, "bay_name", "Bay1")));
        when(jdbc.queryForList(eq("SELECT uom_primary FROM m5_sku_master WHERE tenant_id = ? AND sku_code = ?"), eq(String.class), any(), any()))
                .thenReturn(List.of("KG"));
        when(jdbc.queryForObject(contains("current_state"), eq(String.class), any(Object[].class)))
                .thenReturn("UNLOADING_IN_PROGRESS");
        when(jdbc.queryForObject(contains("status != 'DONE'"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);
        handler = new M10UnloadingBayCompleteActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10UnloadingBayComplete() {
        assertThat(handler.code()).isEqualTo("m10.unloading-bay-complete");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLineItemsArray_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1495L).formData(Map.of()).build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_sku_line_items must be a non-empty array");
        verifyNoInteractions(jdbc);
    }

    @Test
    void noOpenUnloadBayRowMatches_errors() {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of());

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload bay row found");
        verify(jdbc, never()).update(contains("UPDATE dispatch_task"), any(Object[].class));
    }

    @Test
    void singleLine_closesRowPostsReceiptAndForceClosesDispatchTask() {
        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalReceived", new BigDecimal("20"));

        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'"),
                eq(new BigDecimal("20")), eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));

        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("RECEIPT"), eq("SKU1215"), eq("Bay1"), eq(new BigDecimal("20")), eq("KG"), eq("BAY"), eq(1495L), eq("ROU028912"));

        // Force-closes the matching conveyor dispatch_task, same as the loading
        // side's Bay Complete -- regardless of whether the scanner drove
        // processed_qty to target_qty on its own.
        verify(jdbc).update(
                eq("UPDATE dispatch_task SET status = 'Completed', "
                        + "completed_at = CASE WHEN completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'Open'"),
                eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));
    }

    @Test
    void noMatchingDispatchTask_stillSucceeds() {
        // WITHOUT_CONVEYOR/WITHOUT_BARCODE lines never had a dispatch_task
        // opened for them at Start Bay -- zero matching rows here is not an
        // error, the unload bay row itself still closes fine.
        when(jdbc.update(startsWith("UPDATE dispatch_task"), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalReceived", new BigDecimal("20"));
    }

    @Test
    void zeroQty_skipsStockMovementButStillClosesRowAndDispatchTask() {
        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 0))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc, never()).update(startsWith("INSERT INTO m5_stock_movement"), any(Object[].class));
        verify(jdbc).update(startsWith("UPDATE dispatch_task"), any(Object[].class));
    }

    @Test
    void allLinesClosed_marksParentDone() {
        when(jdbc.queryForObject(contains("status != 'DONE'"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail SET status = 'DONE' WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(1L), eq(1495L));
    }

    private ActionInvocation invocationWithLines(List<Map<String, Object>> lines) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(Map.of("order_sku_line_items", lines))
                .build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int unloadingQty) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("unloading_qty", unloadingQty);
        return line;
    }
}
