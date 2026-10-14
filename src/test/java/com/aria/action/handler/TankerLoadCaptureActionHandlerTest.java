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
