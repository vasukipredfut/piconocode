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
 * Unit test for {@link M10StockInwardBayCompleteActionHandler} — same
 * mechanics as {@link M10UnloadingBayCompleteActionHandlerTest} (verified:
 * every validation/error message here is byte-for-byte identical to the
 * shared {@link M10UnloadingBayCompleteActionHandler}), so this file focuses
 * on the delta: kind='STOCK_INWARD'/ref_type='BAY', from_bin=rd_code
 * (order-sheet vendor code), to_bin=Godown — both read off the
 * unloading_progress_detail_bay row.
 */
class M10StockInwardBayCompleteActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10StockInwardBayCompleteActionHandler handler;

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
        handler = new M10StockInwardBayCompleteActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isStockInwardBayComplete() {
        assertThat(handler.code()).isEqualTo("m10.stockinward-bay-complete");
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
    }

    @Test
    void noOpenUnloadBayRowMatches_errors() {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of());

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload bay row found");
    }

    @Test
    void singleLine_closesRowAndRollsUpParent() {
        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalReceived", new BigDecimal("20"));
        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'"),
                eq(new BigDecimal("20")), eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));
    }

    @Test
    void zeroQty_skipsStockMovementButStillClosesRow() {
        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 0))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc, never()).update(startsWith("INSERT INTO m5_stock_movement"), any(Object[].class));
    }

    // ---- the delta: STOCK_INWARD movement shape ------------------------------

    @Test
    void stockMovement_kindAndRefType_areStockInwardAndBay() {
        stubBayRow("Bay1", "SRG0001", "VEND001");

        handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_INWARD"), eq("SKU1215"), eq("VEND001"), eq("SRG0001"),
                eq(new BigDecimal("20")), eq("KG"), eq("BAY"), eq(1495L), eq("ROU028912"));
    }

    @Test
    void toBin_isTheGodown_fromBin_isTheVendorCode() {
        stubBayRow("Bay1", "SRG0002", "VEND007");

        handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_INWARD"), eq("SKU1215"), eq("VEND007"), eq("SRG0002"),
                eq(new BigDecimal("20")), eq("KG"), eq("BAY"), eq(1495L), eq("ROU028912"));
        // balance still keyed by the physical bay_name bin, same as the shared handler.
        verify(jdbc).update(
                eq("UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, warehouse = COALESCE(?, warehouse), last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?"),
                eq(new BigDecimal("20")), eq("SRG0002"), eq(1L), eq("SKU1215"), eq("Bay1"));
    }

    @Test
    void noGodownOrRdCodeOnRow_movementStillWritesWithNulls() {
        // Default setUp() stub has no godown_code/rd_code keys -- both resolve
        // to null, same "degrade gracefully" convention as every other
        // optional field in this module.
        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 20))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_INWARD"), eq("SKU1215"), isNull(), isNull(),
                eq(new BigDecimal("20")), eq("KG"), eq("BAY"), eq(1495L), eq("ROU028912"));
    }

    // ---- helpers -------------------------------------------------------------

    private void stubBayRow(String bayName, String godownCode, String rdCode) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1L);
        row.put("bay_name", bayName);
        row.put("godown_code", godownCode);
        row.put("rd_code", rdCode);
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row));
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
