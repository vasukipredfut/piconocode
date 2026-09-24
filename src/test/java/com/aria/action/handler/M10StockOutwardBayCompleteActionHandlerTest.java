package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.mes.stock.FefoStockPickerService;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M10StockOutwardBayCompleteActionHandler} — same
 * mechanics as {@link M10BayCompleteActionHandlerTest} (verified there:
 * every validation/error message in this handler is byte-for-byte identical
 * to the shared {@link M10BayCompleteActionHandler}), so this file focuses
 * on the delta: kind='STOCK_OUTWARD'/ref_type='UNLOAD_BAY', from_bin=Godown
 * (not the picked lot's bin_code), to_bin=rd_code (order-sheet destination
 * code) — both read off the loading_progress_detail_bay row.
 */
class M10StockOutwardBayCompleteActionHandlerTest {

    private JdbcTemplate jdbc;
    private FakeFefoStockPickerService fefoPicker;
    private M10StockOutwardBayCompleteActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        fefoPicker = new FakeFefoStockPickerService();
        handler = new M10StockOutwardBayCompleteActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        ReflectionTestUtils.setField(handler, "fefoPicker", fefoPicker);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isStockOutwardBayComplete() {
        assertThat(handler.code()).isEqualTo("m10.stockoutward-bay-complete");
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
    void lineItemMissingSkuCode_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("loading_qty", 45);
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing route_id or sku_code");
    }

    @Test
    void noOpenBayRowMatches_errors() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open bay row found for route_id=ROU028912 order_id=null sku_code=SKU1215");
    }

    @Test
    void singleLine_closesRowAndRollsUpParent() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("45"));
        verify(jdbc).update(startsWith("UPDATE loading_progress_detail SET processed_quantity"),
                eq(new BigDecimal("45")), eq(1L), eq(1495L));
    }

    @Test
    void insufficientStock_errorsBeforeClosingBayRow() {
        fefoPicker.seed("SKU1215", lotRow("LOT-A", "BIN-A", "KG", "10"));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("insufficient stock").contains("requested 45").contains("available 10");
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc, never()).update(startsWith("INSERT INTO m5_stock_movement"), any(Object[].class));
    }

    // ---- the delta: STOCK_OUTWARD movement shape ----------------------------

    @Test
    void stockMovement_kindAndRefType_areStockOutwardAndUnloadBay() {
        stubBayRow("SRG0001", "CUST001");
        fefoPicker.seed("SKU1215", lotRow("LOT-A", "BIN-A", "SRG0001", "KG", "100"));

        handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_OUTWARD"), eq("SKU1215"), eq("LOT-A"), eq("SRG0001"), eq("CUST001"),
                eq(new BigDecimal("45")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
    }

    @Test
    void fromBin_isTheGodown_notThePickedLotsBinCode() {
        // The picked lot physically sits in BIN-A, but the movement's from_bin
        // must be the Godown (SRG0002) the row was started against, not BIN-A.
        stubBayRow("SRG0002", null);
        fefoPicker.seed("SKU1215", lotRow("LOT-A", "BIN-A", "SRG0002", "KG", "100"));

        handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_OUTWARD"), eq("SKU1215"), eq("LOT-A"), eq("SRG0002"), isNull(),
                eq(new BigDecimal("45")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
        // The actual balance deduction still targets the real lot/bin, for correctness.
        verify(jdbc).update(
                eq("UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand - ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND "
                        + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ? AND qty_on_hand >= ?"),
                eq(new BigDecimal("45")), eq(1L), eq("SKU1215"), eq("LOT-A"), eq("LOT-A"), eq("BIN-A"), eq(new BigDecimal("45")));
    }

    @Test
    void noGodownOrRdCodeOnRow_movementStillWritesWithNulls() {
        // Row never got a Godown/vendor code (older bay, or pickers not used) —
        // default Mockito empty-list behavior for the unstubbed queryForList
        // means godownCode/rdCode both resolve to null, same "degrade
        // gracefully" convention as every other optional field in this module.
        fefoPicker.seed("SKU1215", lotRow("LOT-A", "BIN-A", "KG", "100"));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_OUTWARD"), eq("SKU1215"), eq("LOT-A"), isNull(), isNull(),
                eq(new BigDecimal("45")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
    }

    @Test
    void stockSpansMultipleLots_eachMovementRowSharesSameFromToBin() {
        stubBayRow("SRG0001", "CUST001");
        fefoPicker.seed("SKU1215",
                lotRow("LOT-OLD", "BIN-A", "SRG0001", "KG", "20"),
                lotRow("LOT-NEW", "BIN-B", "SRG0001", "KG", "1000"));

        handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_OUTWARD"), eq("SKU1215"), eq("LOT-OLD"), eq("SRG0001"), eq("CUST001"),
                eq(new BigDecimal("20")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("STOCK_OUTWARD"), eq("SKU1215"), eq("LOT-NEW"), eq("SRG0001"), eq("CUST001"),
                eq(new BigDecimal("25")), eq("KG"), eq("UNLOAD_BAY"), eq(1495L), eq("ROU028912"));
    }

    // ---- helpers -------------------------------------------------------------

    private void stubBayRow(String godownCode, String rdCode) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("godown_code", godownCode);
        row.put("rd_code", rdCode);
        when(jdbc.queryForList(startsWith("SELECT godown_code, rd_code FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row));
    }

    private ActionInvocation invocationWithLines(List<Map<String, Object>> lines) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(Map.of("order_sku_line_items", lines))
                .build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int loadingQty) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("loading_qty", loadingQty);
        return line;
    }

    private static Map<String, Object> lotRow(String lotNo, String binCode, String uom, String qtyOnHand) {
        return lotRow(lotNo, binCode, null, uom, qtyOnHand);
    }

    /**
     * {@code warehouse} matters here specifically: {@link FefoStockPickerService#pick}
     * post-filters candidate rows on it when a non-null Godown is passed in, so a
     * test that stubs a Godown on the bay row (via {@link #stubBayRow}) and expects
     * a successful pick must seed a lot whose {@code warehouse} matches that same
     * Godown code, or every row gets filtered out and the pick comes back empty.
     */
    private static Map<String, Object> lotRow(String lotNo, String binCode, String warehouse, String uom, String qtyOnHand) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("lot_no", lotNo);
        row.put("bin_code", binCode);
        row.put("warehouse", warehouse);
        row.put("uom", uom);
        row.put("qty_on_hand", new BigDecimal(qtyOnHand));
        row.put("last_movement_at", null);
        return row;
    }

    private static class FakeFefoStockPickerService extends FefoStockPickerService {
        private final Map<String, List<Map<String, Object>>> lotsByItem = new HashMap<>();

        FakeFefoStockPickerService() {
            super(null);
        }

        void seed(String itemCode, Map<String, Object>... rows) {
            lotsByItem.put(itemCode, List.of(rows));
        }

        @Override
        protected List<Map<String, Object>> findAvailableLots(long tenantId, String itemCode, String binCode) {
            if (lotsByItem.containsKey(itemCode)) return lotsByItem.get(itemCode);
            return List.of(lotRow("LOT-DEFAULT", "BIN-DEFAULT", "KG", "999999"));
        }
    }
}
