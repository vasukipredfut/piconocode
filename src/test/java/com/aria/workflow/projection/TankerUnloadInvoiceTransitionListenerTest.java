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
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("20")));
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
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("60")));
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("INV1"), isNull(), isNull(), isNull(),
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-2"), eq(new BigDecimal("40")));
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
                eq("SKU1"), eq("SKU1 Desc"), eq("ROT-1"), eq(new BigDecimal("20")));
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
