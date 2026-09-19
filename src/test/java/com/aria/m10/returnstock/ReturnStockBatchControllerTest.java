package com.aria.m10.returnstock;

import com.aria.datasource.DataSourceSpec;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ReturnStockBatchController}. Mock the JdbcTemplate,
 * same per-position typed matcher style as {@code PendingSkuControllerTest}.
 */
class ReturnStockBatchControllerTest {

    private JdbcTemplate jdbc;
    private ReturnStockBatchController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        controller = new ReturnStockBatchController(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static Map<String, Object> row(String batch, String skuCode, String skuName, String bin,
                                            String orderBox, String orderPc, String unloaded) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("batch_no", batch);
        m.put("sku_code", skuCode);
        m.put("sku_name", skuName);
        m.put("processed_area", bin);
        m.put("order_box_qty", new BigDecimal(orderBox));
        m.put("order_pc_qty", new BigDecimal(orderPc));
        m.put("unloaded_qty", new BigDecimal(unloaded));
        return m;
    }

    @Test
    void unloadedBatches_returnsEveryLocationRegardlessOfShortfall() {
        Map<String, Object> full = row("BACT-003", "SKU-1624", "Coconut Oil 100ml", "G1", "5", "3", "5");
        Map<String, Object> short_ = row("BACT-004", "SKU-1625", "Coconut Oil 200ml", "G2", "5", "0", "4");
        when(jdbc.queryForList(contains("qc_processed_location"), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of(full, short_));

        DataSourceSpec.Response result = controller.unloadedBatches(42L);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(500);
    }

    @Test
    void pendingBatches_filtersOutRowsWhereUnloadedMeetsOrExceedsOrder() {
        Map<String, Object> full = row("BACT-003", "SKU-1624", "Coconut Oil 100ml", "G1", "5", "3", "5");
        Map<String, Object> short_ = row("BACT-004", "SKU-1625", "Coconut Oil 200ml", "G2", "5", "0", "4");
        when(jdbc.queryForList(contains("qc_processed_location"), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of(full, short_));

        DataSourceSpec.Response result = controller.pendingBatches(42L);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).get("batch_no")).isEqualTo("BACT-004");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void pendingBatches_treatsMissingMovementAsZeroUnloaded() {
        Map<String, Object> neverPosted = row("BACT-005", "SKU-1626", "Mustard Oil 1L", "G3", "10", "0", "0");
        when(jdbc.queryForList(contains("qc_processed_location"), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of(neverPosted));

        DataSourceSpec.Response result = controller.pendingBatches(42L);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void noQcAllocatedLocations_returnsEmptyPage() {
        when(jdbc.queryForList(contains("qc_processed_location"), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of());

        assertThat(controller.unloadedBatches(42L).getContent()).isEmpty();
        assertThat(controller.pendingBatches(42L).getContent()).isEmpty();
    }

    @Test
    void scopesMovementLookupToDspRefTypeAndBoxUomOnly() {
        when(jdbc.queryForList(any(String.class), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of());

        controller.unloadedBatches(42L);

        verify(jdbc).queryForList(
                contains("ref_type = 'DSP' AND ref_id = ? AND uom = 'BOX'"),
                eq(1L), eq(42L), eq(1L), eq(42L));
    }

    private static Map<String, Object> invoiceRow(String invoiceNo, String vendorName, String rdCode,
                                                    String skuCode, String skuName,
                                                    String orderBox, String orderPc, String unloaded) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("invoice_no", invoiceNo);
        m.put("vendor_name", vendorName);
        m.put("rd_code", rdCode);
        m.put("sku_code", skuCode);
        m.put("sku_name", skuName);
        m.put("order_box_qty", new BigDecimal(orderBox));
        m.put("order_pc_qty", new BigDecimal(orderPc));
        m.put("unloaded_qty", new BigDecimal(unloaded));
        return m;
    }

    @Test
    void unloadingDetailsByInvoice_returnsInvoiceGroupedRows() {
        Map<String, Object> inv1 = invoiceRow("INV-2001", "ABC Traders", "RD001", "SKU-1624", "Coconut Oil 100ml", "5", "3", "4");
        Map<String, Object> inv2 = invoiceRow("INV-2002", "XYZ Distributors", "RD002", "SKU-1625", "Mustard Oil 1L", "10", "0", "10");
        when(jdbc.queryForList(contains("dispatch_invoice_item"), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of(inv1, inv2));

        DataSourceSpec.Response result = controller.unloadingDetailsByInvoice(101L);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0)).containsEntry("invoice_no", "INV-2001")
                .containsEntry("vendor_name", "ABC Traders")
                .containsEntry("rd_code", "RD001");
        assertThat(result.getContent().get(1)).containsEntry("invoice_no", "INV-2002");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void unloadingDetailsByInvoice_lineWithNoInvoiceNo_stillIncludedUnderNullGroup() {
        Map<String, Object> untyped = invoiceRow(null, null, null, "SKU-1627", "Groundnut Oil", "2", "0", "2");
        when(jdbc.queryForList(contains("dispatch_invoice_item"), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of(untyped));

        DataSourceSpec.Response result = controller.unloadingDetailsByInvoice(101L);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).get("invoice_no")).isNull();
    }

    @Test
    void unloadingDetailsByInvoice_joinsDispatchInvoiceItemOnInvoiceNoAndSkuCode() {
        when(jdbc.queryForList(any(String.class), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of());

        controller.unloadingDetailsByInvoice(101L);

        verify(jdbc).queryForList(
                contains("dii.invoice_no = rsoi.invoice_no AND dii.sku_code = rsoi.sku_code"),
                eq(1L), eq(101L), eq(1L), eq(101L));
    }

    @Test
    void unloadingDetailsByInvoice_groupsByInvoiceAndSkuNotByLocation() {
        when(jdbc.queryForList(any(String.class), any(Long.class), any(Long.class), any(Long.class), any(Long.class)))
                .thenReturn(List.of());

        controller.unloadingDetailsByInvoice(101L);

        verify(jdbc).queryForList(
                contains("GROUP BY rsoi.invoice_no, dii.vendor_name, dii.rd_code, rsoi.sku_code, rsoi.sku_name"),
                eq(1L), eq(101L), eq(1L), eq(101L));
    }
}
