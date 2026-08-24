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
 * Unit test for {@link TankerWeighmentSkuCaptureActionHandler}: closes every
 * open loading_progress_detail_bay row named in formData.tankerSkuWeights,
 * matched by (route_id, sku_code) — same match key as
 * {@link M10BayCompleteActionHandler} — rolls the total (+ loading_finish_time)
 * into loading_progress_detail, and deducts the weighed qty from stock (FEFO
 * across bins via FefoStockPickerService — one m5_stock_movement (kind
 * CONSUME, ref_type TANKER) + m5_stock_balance update per lot picked). Never
 * touches workflow state or m5_order_dispatch_detail.
 *
 * <p>Uses {@link FakeFefoStockPickerService}, same technique as
 * {@code M10BayCompleteActionHandlerTest} — overrides the protected
 * {@code findAvailableLots} hook instead of mocking JdbcTemplate's query
 * methods directly.
 */
class TankerWeighmentSkuCaptureActionHandlerTest {

    private JdbcTemplate jdbc;
    private FakeFefoStockPickerService fefoPicker;
    private TankerWeighmentSkuCaptureActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        fefoPicker = new FakeFefoStockPickerService();
        handler = new TankerWeighmentSkuCaptureActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        ReflectionTestUtils.setField(handler, "fefoPicker", fefoPicker);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10TankerWeighmentCapture() {
        assertThat(handler.code()).isEqualTo("m10.tanker-weighment-capture");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLinesArray_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1495L).formData(Map.of()).build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("formData.tankerSkuWeights must be a non-empty array");
        verifyNoInteractions(jdbc);
    }

    @Test
    void emptyLinesArray_errors() {
        ActionResult r = handler.handle(invocationWithLines(List.of()));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("non-empty array");
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
    void lineItemMissingWeight_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("needs loading_qty > 0");
    }

    @Test
    void zeroWeight_errors() {
        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 0))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("needs loading_qty > 0");
    }

    @Test
    void negativeWeight_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        badLine.put("loading_qty", -5);
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("needs loading_qty > 0");
    }

    @Test
    void lineItemNonNumericWeight_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        badLine.put("loading_qty", "oops");
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("loading_qty must be a number");
    }

    @Test
    void loadWgtKey_acceptedAsFallbackForLoadingQty() {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("route_id", "ROU028912");
        l.put("sku_code", "SKU1215");
        l.put("load_wgt", 45); // no "loading_qty" key at all

        ActionResult r = handler.handle(invocationWithLines(List.of(l)));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("45"));
    }

    @Test
    void noOpenBayRowMatches_errors() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open bay row found for route_id=ROU028912 order_id=null sku_code=SKU1215");
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail SET"), any(Object[].class));
    }

    @Test
    void singleLine_closesRowAndRollsUpParentWithFinishTime() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getMessage()).contains("1 SKU line(s) weighed, 45 kg total issued from stock");
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("45"));

        verify(jdbc).update(
                eq("UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'"),
                eq(new BigDecimal("45")), eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));
        verify(jdbc).update(
                eq("UPDATE loading_progress_detail SET processed_quantity = processed_quantity + ?, "
                        + "loading_finish_time = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(new BigDecimal("45")), eq(1L), eq(1495L));
        // Never touches the order line itself.
        verify(jdbc, never()).update(startsWith("UPDATE m5_order_dispatch_detail"), any(Object[].class));
    }

    @Test
    void lineWithOrderId_matchesUsingOrderIdToo() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(1);

        Map<String, Object> l = new LinkedHashMap<>();
        l.put("route_id", "ROU028912");
        l.put("order_id", "ORD1123");
        l.put("sku_code", "SKU1215");
        l.put("loading_qty", 45);

        ActionResult r = handler.handle(invocationWithLines(List.of(l)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'IN_PROGRESS'"),
                eq(new BigDecimal("45")), eq(1L), eq(1495L), eq("ROU028912"), eq("ORD1123"), eq("ORD1123"), eq("SKU1215"));
    }

    @Test
    void multipleLines_sumsTotalProcessed() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(
                line("ROU028912", "SKU1215", 45),
                line("ROU028912", "SKU1461", 250))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("295"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> closed = (List<Map<String, Object>>) r.getData().get("closed");
        assertThat(closed).hasSize(2);

        verify(jdbc, times(2)).update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc).update(startsWith("UPDATE loading_progress_detail SET processed_quantity"),
                eq(new BigDecimal("295")), eq(1L), eq(1495L));
    }

    @Test
    void secondLineFails_stopsBeforeParentRollup_firstLineAlreadyCommitted() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(1)
                .thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(
                line("ROU028912", "SKU1215", 45),
                line("ROU028912", "SKU9999", 999))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU9999");
        verify(jdbc, times(2)).update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail SET"), any(Object[].class));
    }

    // ---- stock deduction ----------------------------------------------------

    @Test
    void sufficientStock_writesOneConsumeMovementWithTankerRefTypeAndDeductsBalance() {
        fefoPicker.seed("SKU1215", lotRow("LOT-A", "BIN-A", "KG", "100"));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("CONSUME"), eq("SKU1215"), eq("LOT-A"), eq("BIN-A"),
                eq(new BigDecimal("45")), eq("KG"), eq("TANKER"), eq(1495L), eq("ROU028912"));
        verify(jdbc).update(
                eq("UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand - ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND "
                        + "((lot_no IS NULL AND ? IS NULL) OR lot_no = ?) AND bin_code = ? AND qty_on_hand >= ?"),
                eq(new BigDecimal("45")), eq(1L), eq("SKU1215"), eq("LOT-A"), eq("LOT-A"), eq("BIN-A"), eq(new BigDecimal("45")));
    }

    @Test
    void raceConditionDetected_balanceGuardAffectsZeroRows_errorsBeforeClosingBayRowOrLoggingMovement() {
        fefoPicker.seed("SKU1215", lotRow("LOT-A", "BIN-A", "KG", "100"));
        when(jdbc.update(startsWith("UPDATE m5_stock_balance"), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("stock changed concurrently").contains("LOT-A").contains("BIN-A");
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc, never()).update(startsWith("INSERT INTO m5_stock_movement"), any(Object[].class));
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail SET"), any(Object[].class));
    }

    @Test
    void lineItemNotAnObject_errorsCleanlyInsteadOfClassCastException() {
        Object linesObj = List.of("this is not a line item object");
        ActionResult r = handler.handle(ActionInvocation.builder()
                .instanceId(1495L)
                .formData(Map.of("tankerSkuWeights", linesObj))
                .build());

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be an object");
        verifyNoInteractions(jdbc);
    }

    @Test
    void blankRouteIdOrSkuCode_treatedAsMissing() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "   ");
        badLine.put("sku_code", "SKU1215");
        badLine.put("loading_qty", 45);

        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing route_id or sku_code");
    }

    @Test
    void stockSpansMultipleLots_writesOneMovementPerLotPickedOldestFirst() {
        fefoPicker.seed("SKU1215",
                lotRow("LOT-OLD", "BIN-A", "KG", "20"),
                lotRow("LOT-NEW", "BIN-B", "KG", "1000"));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("CONSUME"), eq("SKU1215"), eq("LOT-OLD"), eq("BIN-A"),
                eq(new BigDecimal("20")), eq("KG"), eq("TANKER"), eq(1495L), eq("ROU028912"));
        verify(jdbc).update(eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, lot_no, from_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("CONSUME"), eq("SKU1215"), eq("LOT-NEW"), eq("BIN-B"),
                eq(new BigDecimal("25")), eq("KG"), eq("TANKER"), eq(1495L), eq("ROU028912"));
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

    @Test
    void noStockAtAll_errorsWithZeroAvailable() {
        fefoPicker.seed("SKU1215"); // no lots at all

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("available 0");
    }

    // ---- helpers -------------------------------------------------------------

    private ActionInvocation invocationWithLines(List<Map<String, Object>> lines) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(Map.of("tankerSkuWeights", lines))
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
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("lot_no", lotNo);
        row.put("bin_code", binCode);
        row.put("uom", uom);
        row.put("qty_on_hand", new BigDecimal(qtyOnHand));
        row.put("last_movement_at", null);
        return row;
    }

    /** Same technique as {@code M10BayCompleteActionHandlerTest}'s own fake — see its javadoc. */
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
