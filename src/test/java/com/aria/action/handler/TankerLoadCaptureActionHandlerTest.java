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

/** Unit test for {@link TankerLoadCaptureActionHandler}: tank-wise load of the TankerInbound "load again" invoice rows. */
class TankerLoadCaptureActionHandlerTest {

    private JdbcTemplate jdbc;
    private FakeFefo fefo;
    private TankerLoadCaptureActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        fefo = new FakeFefo();
        handler = new TankerLoadCaptureActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        ReflectionTestUtils.setField(handler, "fefoPicker", fefo);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private void openRows(Map<String, Object>... rows) {
        when(jdbc.queryForList(contains("FROM loading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(rows));
    }

    private static Map<String, Object> row(long id, String tank, String qty) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", id);
        r.put("bay_name", tank);
        r.put("quantity", new BigDecimal(qty));
        return r;
    }

    private static ActionInvocation inv(Map<String, Object>... lines) {
        return ActionInvocation.builder().instanceId(1542L).formData(Map.of("tankerLoadItems", List.of(lines))).build();
    }

    private static Map<String, Object> line(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    @Test
    void code() {
        assertThat(handler.code()).isEqualTo("m10.tanker-load-capture");
    }

    @Test
    void missingInstanceId_errors() {
        assertThat(handler.handle(ActionInvocation.builder().build()).isOk()).isFalse();
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLines_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1542L).formData(Map.of()).build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("tankerLoadItems");
    }

    @Test
    void lineWithoutInvoiceOrRoute_errors() {
        ActionResult r = handler.handle(inv(line("sku_code", "SKU1", "load_wgt", 10)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing sku_code or invoice_no");
    }

    @Test
    void zeroWeight_errors() {
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "load_wgt", 0)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("load_wgt > 0");
    }

    @Test
    void noOpenRow_errors() {
        openRows();
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "load_wgt", 10)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open load row").contains("INV1");
    }

    @Test
    void singleTank_takesStockAndClosesRow() {
        openRows(row(11, "ROT-1", "60"));
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "load_wgt", 60)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO m5_stock_movement"), eq(1L), eq("CONSUME"), eq("SKU1"), any(), any(),
                eq(new BigDecimal("60")), any(), eq("TANKER_LOAD"), eq(1542L), eq("INV1"));
        verify(jdbc).update(contains("UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE'"),
                eq(new BigDecimal("60")), eq(1L), eq(11L));
        verify(jdbc).update(contains("UPDATE loading_progress_detail SET processed_quantity"), eq(new BigDecimal("60")), eq(1L), eq(1542L));
    }

    @Test
    void multiTank_splitsByAllocation() {
        openRows(row(11, "ROT-1", "60"), row(12, "ROT-2", "40"));
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "load_wgt", 100)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE'"),
                eq(new BigDecimal("60.000")), eq(1L), eq(11L));
        verify(jdbc).update(contains("UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE'"),
                eq(new BigDecimal("40.000")), eq(1L), eq(12L));
    }

    @Test
    void oneTank_closesOnlyThatRow() {
        openRows(row(11, "ROT-1", "60"), row(12, "ROT-2", "40"));
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "tank_location", "ROT-2", "load_wgt", 40)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("status = 'DONE'"), eq(new BigDecimal("40")), eq(1L), eq(12L));
        verify(jdbc, never()).update(contains("status = 'DONE'"), any(), eq(1L), eq(11L));
    }

    @Test
    void tankSubset_oneTotalSplitAcrossJustThoseTanks() {
        openRows(row(11, "ROT-1", "60"), row(12, "ROT-2", "40"), row(13, "ROT-3", "100"));
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1",
                "tank_locations", List.of("ROT-1", "ROT-2"), "load_wgt", 100)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("status = 'DONE'"), eq(new BigDecimal("60.000")), eq(1L), eq(11L));
        verify(jdbc).update(contains("status = 'DONE'"), eq(new BigDecimal("40.000")), eq(1L), eq(12L));
        verify(jdbc, never()).update(contains("status = 'DONE'"), any(), eq(1L), eq(13L));
    }

    @Test
    void unknownTank_errors() {
        openRows(row(11, "ROT-1", "60"));
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "tank_location", "ROT-9", "load_wgt", 10)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open tank row for ROT-9");
    }

    @Test
    void insufficientStockInTank_errors() {
        openRows(row(11, "ROT-1", "60"));
        fefo.available = new BigDecimal("20");
        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "load_wgt", 60)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("insufficient stock in tank ROT-1").contains("available 20");
        verify(jdbc, never()).update(contains("INSERT INTO m5_stock_movement"), any(Object[].class));
    }

    @Test
    void orderSkuLineItemsAlias_isAccepted() {
        openRows(row(11, "ROT-1", "60"));
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1542L)
                .formData(Map.of("order_sku_line_items", List.of(line("invoice_no", "INV1", "sku_code", "SKU1", "qty", 60)))).build());
        assertThat(r.isOk()).isTrue();
    }

    @Test
    void routeIdLine_matchesByRoute() {
        openRows(row(11, "ROT-1", "60"));
        handler.handle(inv(line("route_id", "ROU1", "sku_code", "SKU1", "load_wgt", 60)));
        verify(jdbc).queryForList(contains("AND route_id = ?"), any(Object[].class));
    }

    // ---- what is loaded back counts against the invoice's unload rows (loaded_back_quantity, V376) ----

    private void unloadRows(Map<String, Object>... rows) {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(rows));
    }

    private static Map<String, Object> unloadRow(long id, String tank, String processed, String loadedBack) {
        Map<String, Object> r = new HashMap<>();
        r.put("id", id);
        r.put("bay_name", tank);
        r.put("processed_quantity", new BigDecimal(processed));
        r.put("loaded_back_quantity", new BigDecimal(loadedBack));
        return r;
    }

    private List<Object[]> countedBack() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArguments())
                .filter(a -> ((String) a[0]).contains("SET status = CASE WHEN COALESCE(loaded_back_quantity"))
                .toList();
    }

    @Test
    void loadBackInPart_countsAgainstTheTanksUnloadRow_whichStaysOpenForTheRest() {
        openRows(row(11, "ROT-1", "60"));
        unloadRows(unloadRow(5, "ROT-1", "100", "0"));

        ActionResult r = handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "tank_location", "ROT-1", "load_wgt", 60)));

        assertThat(r.isOk()).isTrue();
        List<Object[]> counted = countedBack();
        assertThat(counted).hasSize(1);
        // sql, take (status test), take (add), tenant, id
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("60");
        assertThat((BigDecimal) counted.get(0)[2]).isEqualByComparingTo("60");
        assertThat(counted.get(0)[4]).isEqualTo(5L);
        assertThat((String) counted.get(0)[0]).contains("THEN 'REVERSED'");
    }

    @Test
    void theTankItCameOutOfIsAskedFirst_theOtherTanksOfTheLineAfterwards() {
        openRows(row(11, "ROT-1", "1000"));
        unloadRows(unloadRow(5, "ROT-1", "600", "0"), unloadRow(6, "ROT-2", "3000", "0"));

        handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "tank_location", "ROT-1", "load_wgt", 1000)));

        verify(jdbc).queryForList(contains("ORDER BY (bay_name = ?) DESC"), eq(1L), eq(1542L), eq("INV1"), eq("SKU1"), eq("ROT-1"));
        List<Object[]> counted = countedBack();
        assertThat(counted).hasSize(2);
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("600");   // ROT-1 row takes all it had
        assertThat((BigDecimal) counted.get(1)[1]).isEqualByComparingTo("400");   // the rest goes to the next unload row
    }

    @Test
    void aRowWithNothingLeftIsSkipped_andAnExcessIsNotCountedAnywhere() {
        openRows(row(11, "ROT-1", "500"));
        unloadRows(unloadRow(5, "ROT-1", "100", "100"), unloadRow(6, "ROT-2", "200", "150"));

        handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "tank_location", "ROT-1", "load_wgt", 500)));

        List<Object[]> counted = countedBack();
        assertThat(counted).hasSize(1);
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("50");    // only the 50 still room on ROT-2
        assertThat(counted.get(0)[4]).isEqualTo(6L);
    }

    @Test
    void routeBasedLine_hasNoInvoice_soNoUnloadRowIsTouched() {
        openRows(row(11, "ROT-1", "60"));

        handler.handle(inv(line("route_id", "ROU1", "sku_code", "SKU1", "load_wgt", 60)));

        verify(jdbc, never()).queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class));
        assertThat(countedBack()).isEmpty();
    }

    @Test
    void parentTotalIsRecomputed() {
        openRows(row(11, "ROT-1", "60"));
        unloadRows(unloadRow(5, "ROT-1", "100", "0"));

        handler.handle(inv(line("invoice_no", "INV1", "sku_code", "SKU1", "load_wgt", 60)));

        verify(jdbc).update(contains("UPDATE unloading_progress_detail p SET p.loaded_back_quantity"), eq(1L), eq(1542L));
    }

    /** Stock picker whose tank holds {@code available} (default plenty). */
    private static class FakeFefo extends FefoStockPickerService {
        BigDecimal available = new BigDecimal("999999");

        FakeFefo() { super(null); }

        @Override
        protected List<Map<String, Object>> findAvailableLots(long tenantId, String itemCode, String binCode) {
            Map<String, Object> lot = new LinkedHashMap<>();
            lot.put("lot_no", "LOT-1");
            lot.put("bin_code", binCode);
            lot.put("uom", "KG");
            lot.put("qty_on_hand", available);
            lot.put("last_movement_at", null);
            return List.of(lot);
        }
    }
}
