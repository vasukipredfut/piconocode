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
 * Unit test for {@link QcPreloadTankAllocationListener} — focused on the
 * multiple-tank-locations-per-product behavior: the page's BatchAllocationTable
 * submits parent lines under {@code tankAllocation} and child allocation rows
 * (keyed back to a parent by {@code order_dispatch_detail_id}) under a
 * sibling {@code tankLocationAllocations} list; one {@code
 * loading_progress_detail_bay} row is written per matched allocation instead
 * of one per whole line.
 */
class QcPreloadTankAllocationListenerTest {

    private JdbcTemplate jdbc;
    private QcPreloadTankAllocationListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new QcPreloadTankAllocationListener(jdbc);
        TenantContext.set(1L);
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(0); // not already seeded
        when(jdbc.queryForObject(contains("SELECT id FROM loading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(777L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void wrongWorkflowOrState_doesNothing() {
        WorkflowInstance inst = instance("M10.FGLoading", "QC_PRELOAD_APPROVED");
        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void alreadySeeded_skipsInsert() {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(1);
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));

        verify(jdbc, never()).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void noAllocationsListAtAll_fallsBackToSingleTankLocation() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-1"), eq(new BigDecimal("20")));
    }

    @Test
    void childAllocations_splitOneLineAcrossMultipleBayRows() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> l = line("SKU1", "ROT-1", "100"); // line's own tank_location should be ignored
        l.put("id", 501);
        Map<String, Object> bag = bagWith(List.of(l));
        pageData(bag).put("tankLocationAllocations", List.of(
                Map.of("order_dispatch_detail_id", 501, "tank_location", "ROT-1", "alloc_qty", "60"),
                Map.of("order_dispatch_detail_id", 501, "tank_location", "ROT-2", "alloc_qty", "40")
        ));

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-1"), eq(new BigDecimal("60")));
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-2"), eq(new BigDecimal("40")));
        // head row's total_quantity must reflect the sum of allocations (100).
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail"),
                eq(1L), eq(1495L), any(), any(), any(), any(), eq(new BigDecimal("100")));
    }

    @Test
    void childAllocations_idTypeMismatch_stillMatchesByStringValue() {
        // Parent line's own id arrives as an Integer (501); the allocation's
        // order_dispatch_detail_id arrives as a String ("501") -- a realistic
        // case since both come from independently-serialized JSON lists.
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> l = line("SKU1", "ROT-1", "100");
        l.put("id", 501);
        Map<String, Object> bag = bagWith(List.of(l));
        pageData(bag).put("tankLocationAllocations", List.of(
                Map.of("order_dispatch_detail_id", "501", "tank_location", "ROT-3", "alloc_qty", "25")
        ));

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-3"), eq(new BigDecimal("25")));
    }

    @Test
    void noMatchingAllocations_fallsBackToLineLevelTankLocation() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> l = line("SKU1", "ROT-1", "20");
        l.put("id", 501);
        Map<String, Object> bag = bagWith(List.of(l));
        // Allocation exists but for a DIFFERENT parent id -- line 501 has none.
        pageData(bag).put("tankLocationAllocations", List.of(
                Map.of("order_dispatch_detail_id", 999, "tank_location", "ROT-9", "alloc_qty", "5")
        ));

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-1"), eq(new BigDecimal("20")));
    }

    @Test
    void noTankLocationAndNoAllocations_lineSkipped() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> l = line("SKU1", null, "20");

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(l)));

        verify(jdbc, never()).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
    }

    private static WorkflowInstance instance(String workflowCode, String currentState) {
        return WorkflowInstance.builder().id(1495L).workflowCode(workflowCode).currentState(currentState).build();
    }

    private static Map<String, Object> line(String skuCode, String tankLocation, String qty) {
        Map<String, Object> line = new HashMap<>();
        line.put("route_id", "ROU1");
        line.put("order_id", "ORD1");
        line.put("sku_code", skuCode);
        line.put("sku_desc", skuCode + " Desc");
        if (tankLocation != null) line.put("tank_location", tankLocation);
        line.put("load_wgt", qty);
        return line;
    }

    private static Map<String, Object> bagWith(List<Map<String, Object>> lines) {
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("tankAllocation", lines);
        Map<String, Object> bag = new HashMap<>();
        bag.put("aria:qc-preload-approval", pageData);
        return bag;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> pageData(Map<String, Object> bag) {
        return (Map<String, Object>) bag.get("aria:qc-preload-approval");
    }
}
