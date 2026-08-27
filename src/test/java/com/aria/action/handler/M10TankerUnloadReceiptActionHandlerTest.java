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
 * Unit test for {@link M10TankerUnloadReceiptActionHandler}: closes
 * unloading_progress_detail_bay row(s) with the operator-entered weight, posts a
 * RECEIPT stock movement into the tank location recorded in that row's
 * bay_name (never from the payload), and rolls up unloading_progress_detail.
 */
class M10TankerUnloadReceiptActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10TankerUnloadReceiptActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForList(contains("m5_sku_master"), eq(String.class), any(Object[].class))).thenReturn(List.of("KG"));
        handler = new M10TankerUnloadReceiptActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10TankerUnloadReceipt() {
        assertThat(handler.code()).isEqualTo("m10.tanker-unload-receipt");
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
        assertThat(r.getMessage()).contains("unload_receipt_items must be a non-empty array");
    }

    @Test
    void lineItemMissingInvoiceOrSku_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("weight", 500);
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing invoice_no or sku_code");
    }

    @Test
    void negativeWeight_errors() {
        Map<String, Object> l = line("INV-1001", "SKU1215", -5);
        ActionResult r = handler.handle(invocationWithLines(List.of(l)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("needs weight >= 0");
    }

    @Test
    void noOpenRowFound_errors() {
        when(jdbc.queryForList(contains("unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of());

        ActionResult r = handler.handle(invocationWithLines(List.of(line("INV-1001", "SKU1215", 500))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload row found for invoice_no=INV-1001");
        verify(jdbc, never()).update(contains("m5_stock_balance"), any(Object[].class));
    }

    @Test
    void openRowWithNoTankLocation_errors() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 10L);
        row.put("bay_name", null);
        when(jdbc.queryForList(contains("unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(row));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("INV-1001", "SKU1215", 500))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("has no tank location recorded");
    }

    @Test
    void validLine_postsReceiptAndClosesRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 10L);
        row.put("bay_name", "ROT-1");
        when(jdbc.queryForList(contains("unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(row));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("INV-1001", "SKU1215", 500))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalReceived", new BigDecimal("500"));

        verify(jdbc).update(
                eq("UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?"),
                eq(new BigDecimal("500")), eq(1L), eq("SKU1215"), eq("ROT-1"));
        verify(jdbc).update(
                eq("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, to_bin, qty, uom, "
                        + "ref_type, ref_id, ref_number) VALUES (?,?,?,?,?,?,?,?,?)"),
                eq(1L), eq("RECEIPT"), eq("SKU1215"), eq("ROT-1"), eq(new BigDecimal("500")), eq("KG"), eq("TANKER_UNLOAD"), eq(1495L), eq("INV-1001"));
        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' WHERE id = ?"),
                eq(new BigDecimal("500")), eq(10L));
    }

    @Test
    void zeroWeight_closesRowButSkipsStockPosting() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 10L);
        row.put("bay_name", "ROT-1");
        when(jdbc.queryForList(contains("unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(row));

        ActionResult r = handler.handle(invocationWithLines(List.of(line("INV-1001", "SKU1215", 0))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc, never()).update(contains("m5_stock_balance"), any(Object[].class));
        verify(jdbc, never()).update(contains("m5_stock_movement"), any(Object[].class));
        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' WHERE id = ?"),
                eq(BigDecimal.ZERO), eq(10L));
    }

    @Test
    void allLinesClosed_flipsParentStatusDone() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 10L);
        row.put("bay_name", "ROT-1");
        when(jdbc.queryForList(contains("unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(row));
        when(jdbc.queryForObject(contains("unloading_progress_detail_bay"), eq(Integer.class), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("INV-1001", "SKU1215", 500))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(
                eq("UPDATE unloading_progress_detail SET status = 'DONE' WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(1L), eq(1495L));
    }

    @Test
    void someLinesStillOpen_parentStaysInProgress() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 10L);
        row.put("bay_name", "ROT-1");
        when(jdbc.queryForList(contains("unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(row));
        when(jdbc.queryForObject(contains("unloading_progress_detail_bay"), eq(Integer.class), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("INV-1001", "SKU1215", 500))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc, never()).update(
                eq("UPDATE unloading_progress_detail SET status = 'DONE' WHERE tenant_id = ? AND workflow_instance_id = ?"),
                eq(1L), eq(1495L));
    }

    private ActionInvocation invocationWithLines(List<Map<String, Object>> lines) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(lines == null ? Map.of() : Map.of("unload_receipt_items", lines))
                .build();
    }

    private Map<String, Object> line(String invoiceNo, String skuCode, int weight) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("invoice_no", invoiceNo);
        line.put("sku_code", skuCode);
        line.put("weight", weight);
        return line;
    }
}
