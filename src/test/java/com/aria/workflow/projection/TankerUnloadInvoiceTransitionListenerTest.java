package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link TankerUnloadInvoiceTransitionListener} — focused on
 * the multiple-tank-locations-per-product behavior: a line's nested
 * {@code tank_allocations} array splits one invoice/SKU line across several
 * {@code unloading_progress_detail_bay} rows instead of one.
 */
class TankerUnloadInvoiceTransitionListenerTest {

    private JdbcTemplate jdbc;
    private TankerUnloadInvoiceTransitionListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new TankerUnloadInvoiceTransitionListener(jdbc);
        TenantContext.set(1L);
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(777L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void wrongWorkflowOrState_doesNothing() {
        WorkflowInstance inst = instance("M10.FGLoading", "QC_APPROVED");
        listener.onTransition(inst, "DISPATCH_QUEUED", Map.of("aria:qc-approval", Map.of("unloadingInvoices", List.of())));
        verifyNoInteractions(jdbc);
    }

    @Test
    void noNestedAllocations_fallsBackToSingleTankLocation() {
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = baseLine("SKU1", "20");
        line.put("tank_location", "ROT-1");

        listener.onTransition(inst, "DISPATCH_QUEUED", bag(line));

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("20")), isNull(), isNull());
    }

    @Test
    void nestedAllocations_splitsOneLineAcrossMultipleBayRows() {
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = baseLine("SKU1", "100");
        line.put("tank_location", "ROT-1"); // should be ignored in favor of tank_allocations
        line.put("tank_allocations", List.of(
                Map.of("tank_location", "ROT-1", "qty", "60"),
                Map.of("tank_location", "ROT-2", "qty", "40")
        ));

        listener.onTransition(inst, "DISPATCH_QUEUED", bag(line));

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("60")), isNull(), isNull());
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-2"), eq(new BigDecimal("40")), isNull(), isNull());
        // total_quantity on the parent row must reflect the sum of allocations (100), not double it.
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail"),
                eq(1L), eq(1495L), any(), any(), eq(new BigDecimal("100")));
    }

    @Test
    void emptyAllocationsList_fallsBackToLineLevelTankLocation() {
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = baseLine("SKU1", "20");
        line.put("tank_location", "ROT-1");
        line.put("tank_allocations", List.of());

        listener.onTransition(inst, "DISPATCH_QUEUED", bag(line));

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("20")), isNull(), isNull());
    }

    @Test
    void diffAndReason_savedOnEveryTankRowOfTheLine() {
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = baseLine("SKU1", "100");
        line.put("diff_box", "5");
        line.put("diff_reason", "Short supply");
        line.put("tank_allocations", List.of(
                Map.of("tank_location", "ROT-1", "qty", "60"),
                Map.of("tank_location", "ROT-2", "qty", "40")));

        listener.onTransition(inst, "DISPATCH_QUEUED", bag(line));

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("60")), eq(new BigDecimal("5")), eq("Short supply"));
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-2"), eq(new BigDecimal("40")), eq(new BigDecimal("5")), eq("Short supply"));
    }

    // ---- "load again": loadingInvoices -> loading_progress_detail(_bay), status IN_PROGRESS ----

    @Test
    void loadingInvoices_writeOneInProgressBayRowPerTankAllocation() {
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(888L);
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = loadingLine("SKU1", "100");
        line.put("tank_allocations", List.of(
                Map.of("tank_location", "ROT-1", "qty", "60"),
                Map.of("tank_location", "ROT-2", "qty", "40")));

        listener.onTransition(inst, "DISPATCH_QUEUED", loadingBag(line));

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("INV1"), eq("ORD1"), eq("Vendor A"), eq("RD1"),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("60")));
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("INV1"), eq("ORD1"), eq("Vendor A"), eq("RD1"),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-2"), eq(new BigDecimal("40")));
        // every inserted bay row is IN_PROGRESS
        verify(jdbc, times(2)).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc, atLeastOnce()).update(argThat((String sql) -> sql.contains("INSERT INTO loading_progress_detail_bay") && sql.contains("'IN_PROGRESS'")), any(Object[].class));
    }

    @Test
    void loadingInvoices_withoutUnloadingInvoices_doesNotTouchUnloadingTables() {
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(888L);
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = loadingLine("SKU1", "50");
        line.put("tank_allocations", List.of(Map.of("tank_location", "ROT-1", "qty", "50")));

        listener.onTransition(inst, "DISPATCH_QUEUED", loadingBag(line));

        verify(jdbc, never()).update(contains("unloading_progress_detail"), any(Object[].class));
    }

    @Test
    void loadingInvoices_lineWithoutAllocations_usesLineLevelTankLocation() {
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(888L);
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = loadingLine("SKU1", "20");
        line.put("tank_location", "ROT-1");

        listener.onTransition(inst, "DISPATCH_QUEUED", loadingBag(line));

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("INV1"), eq("ORD1"), eq("Vendor A"), eq("RD1"),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("20")));
    }

    @Test
    void loadingInvoices_rerunReplacesOnlyUnstartedInvoiceRows() {
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(888L);
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = loadingLine("SKU1", "20");
        line.put("tank_allocations", List.of(Map.of("tank_location", "ROT-1", "qty", "20")));

        listener.onTransition(inst, "DISPATCH_QUEUED", loadingBag(line));

        verify(jdbc).update(argThat((String sql) -> sql.startsWith("DELETE FROM loading_progress_detail_bay")
                && sql.contains("status = 'IN_PROGRESS'") && sql.contains("invoice_no IS NOT NULL")
                && sql.contains("processed_quantity")), any(Object[].class));
    }

    @Test
    void loadingInvoices_allocationsWithoutTankOrQty_writeNothing() {
        WorkflowInstance inst = instance("M10.TankerInbound", "QC_APPROVED");
        Map<String, Object> line = loadingLine("SKU1", "20");
        line.put("tank_allocations", List.of(Map.of("qty", "20"), Map.of("tank_location", "ROT-1", "qty", "0")));

        listener.onTransition(inst, "DISPATCH_QUEUED", loadingBag(line));

        verify(jdbc, never()).update(contains("loading_progress_detail"), any(Object[].class));
    }

    private static Map<String, Object> loadingLine(String skuCode, String qty) {
        Map<String, Object> line = baseLine(skuCode, qty);
        line.put("order_id", "ORD1");
        line.put("vendor_name", "Vendor A");
        line.put("rd_code", "RD1");
        return line;
    }

    private static Map<String, Object> loadingBag(Map<String, Object> line) {
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("loadingInvoices", List.of(line));
        return Map.of("aria:qc-approval", pageData);
    }

    private static WorkflowInstance instance(String workflowCode, String currentState) {
        return WorkflowInstance.builder().id(1495L).workflowCode(workflowCode).currentState(currentState).build();
    }

    private static Map<String, Object> baseLine(String skuCode, String qty) {
        Map<String, Object> line = new HashMap<>();
        line.put("invoice_no", "INV1");
        line.put("sku_code", skuCode);
        line.put("sku_name", skuCode + " Desc");
        line.put("qty", qty);
        return line;
    }

    private static Map<String, Object> bag(Map<String, Object> line) {
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("unloadingInvoices", List.of(line));
        return Map.of("aria:qc-approval", pageData);
    }
}
