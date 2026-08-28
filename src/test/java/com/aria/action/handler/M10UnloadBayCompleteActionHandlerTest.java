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
 * Unit test for {@link M10UnloadBayCompleteActionHandler}: the Reject Flow
 * Unloading page's Stop Bay button. Closes unloading_progress_detail_bay
 * rows, adds stock back (kind RETURN, reversing the original CONSUME
 * lots/bins recorded for this instance+sku), and soft-reverses the matching
 * loading_progress_detail_bay row.
 */
class M10UnloadBayCompleteActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10UnloadBayCompleteActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForList(contains("m5_stock_movement"), any(Object[].class))).thenReturn(List.of());
        handler = new M10UnloadBayCompleteActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10UnloadBayComplete() {
        assertThat(handler.code()).isEqualTo("m10.unload-bay-complete");
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
        ActionResult r = handler.handle(invocationWithLines(null));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_sku_line_items must be a non-empty array");
    }

    @Test
    void noOpenUnloadBayRowMatches_errors() {
        when(jdbc.update(startsWith("UPDATE unloading_progress_detail_bay"), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload bay row found for route_id=ROU028912 order_id=null sku_code=SKU1215");
        verify(jdbc, never()).update(startsWith("UPDATE unloading_progress_detail SET"), any(Object[].class));
    }

    @Test
    void noPriorConsumeRecord_returnsWholeQtyToFallbackBin() {
        // No CONSUME movement found for this instance+sku -> everything falls
        // back to the synthetic RETURNS bin instead of failing the request.
        when(jdbc.queryForList(contains("m5_stock_movement"), any(Object[].class))).thenReturn(List.of());

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("RETURN"), eq("SKU1215"), isNull(), eq("RETURNS"),
                eq(new BigDecimal("20")), eq("EA"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
    }

    @Test
    void priorConsumeRecord_reversesSameLotAndBin() {
        Map<String, Object> consumedRow = new LinkedHashMap<>();
        consumedRow.put("lot_no", "LOT-A");
        consumedRow.put("from_bin", "BIN-A");
        consumedRow.put("uom", "KG");
        consumedRow.put("qty", new BigDecimal("45"));
        when(jdbc.queryForList(contains("m5_stock_movement"), any(Object[].class))).thenReturn(List.of(consumedRow));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("RETURN"), eq("SKU1215"), eq("LOT-A"), eq("BIN-A"),
                eq(new BigDecimal("20")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
        verify(jdbc).update(
                eq("UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND "
                        + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ?"),
                eq(new BigDecimal("20")), eq(1L), eq("SKU1215"), eq("LOT-A"), eq("LOT-A"), eq("BIN-A"));
    }

    @Test
    void unloadQtyExceedsConsumedRow_spillsResidualToFallbackBin() {
        Map<String, Object> consumedRow = new LinkedHashMap<>();
        consumedRow.put("lot_no", "LOT-A");
        consumedRow.put("from_bin", "BIN-A");
        consumedRow.put("uom", "KG");
        consumedRow.put("qty", new BigDecimal("10"));
        when(jdbc.queryForList(contains("m5_stock_movement"), any(Object[].class))).thenReturn(List.of(consumedRow));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("RETURN"), eq("SKU1215"), eq("LOT-A"), eq("BIN-A"),
                eq(new BigDecimal("10")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("RETURN"), eq("SKU1215"), isNull(), eq("RETURNS"),
                eq(new BigDecimal("10")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
    }

    @Test
    void closesRowAndReversesLoadingRow_rollsUpParent() {
        when(jdbc.update(startsWith("UPDATE unloading_progress_detail_bay"), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        when(jdbc.queryForObject(eq("SELECT current_state FROM workflow_instance WHERE id = ? AND tenant_id = ?"),
                eq(String.class), eq(1495L), eq(1L))).thenReturn("REJECT_FLOW_UNLOADING");
        // Fully unloaded in this one session: originally loaded 20, and this
        // is the only unload attempt on record for it -> loading row goes
        // straight to REVERSED (not left DONE for a further partial bay).
        when(jdbc.queryForList(contains("SELECT quantity FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(Map.of("quantity", new BigDecimal("20"))));
        when(jdbc.queryForObject(contains("SUM(processed_quantity)"), eq(BigDecimal.class), any(Object[].class)))
                .thenReturn(new BigDecimal("20"));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("20"));

        verify(jdbc).update(
                eq("UPDATE loading_progress_detail_bay SET status = ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'UNLOAD_IN_PROGRESS'"),
                eq("REVERSED"), eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));
        // Every loading_progress_detail_bay row for the instance is REVERSED -> parent flips too.
        verify(jdbc).update(
                eq("UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(1L), eq(1495L));
        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail SET processed_quantity = processed_quantity + ?, current_step = ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(new BigDecimal("20")), eq("REJECT_FLOW_UNLOADING"), eq(1L), eq(1495L));
    }

    @Test
    void partialUnload_leavesLoadingRowDoneForAnotherBay() {
        when(jdbc.update(startsWith("UPDATE unloading_progress_detail_bay"), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        // Originally loaded 20, only 8 unloaded so far (across all unload
        // sessions, including this one) -> 12 left, loading row must go
        // back to DONE, not REVERSED, so a further Start Bay can find it.
        when(jdbc.queryForList(contains("SELECT quantity FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(Map.of("quantity", new BigDecimal("20"))));
        when(jdbc.queryForObject(contains("SUM(processed_quantity)"), eq(BigDecimal.class), any(Object[].class)))
                .thenReturn(new BigDecimal("8"));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 8))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("UPDATE loading_progress_detail_bay SET status = ? "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'UNLOAD_IN_PROGRESS'"),
                eq("DONE"), eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));
        verify(jdbc, never()).update(
                eq("UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(1L), eq(1495L));
    }

    @Test
    void otherLoadingBayRowsStillActive_parentNotReversed() {
        when(jdbc.update(startsWith("UPDATE unloading_progress_detail_bay"), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc, never()).update(
                eq("UPDATE loading_progress_detail SET status = 'REVERSED' WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(1L), eq(1495L));
    }

    private ActionInvocation invocationWithLines(List<Map<String, Object>> lines) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(lines == null ? Map.of() : Map.of("order_sku_line_items", lines))
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
