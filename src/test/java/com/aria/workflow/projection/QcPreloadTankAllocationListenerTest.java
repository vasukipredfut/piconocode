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
    void reloadTopUp_inProgressLineNotDuplicated_newLineSeeded() {
        // SKU-OLD already has an IN_PROGRESS bay row on this tank (e.g. a re-delivered event); SKU-NEW has none.
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenAnswer(inv -> {
                    Object[] a = inv.getArguments();
                    return java.util.Arrays.asList(a).contains("SKU-OLD") ? 1 : 0;
                });
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING",
                bagWith(List.of(line("SKU-OLD", "ROT-1", "50"), line("SKU-NEW", "ROT-2", "30"))));

        verify(jdbc, never()).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU-OLD"), any(), any(), any(), any(), any());
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU-NEW"), eq("SKU-NEW Desc"),
                eq("ROT-2"), eq(new BigDecimal("30")), isNull(), isNull());
        // parent total grows by the NEW lines only (30), not by the already-seeded 50.
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail"),
                eq(1L), eq(1495L), any(), any(), any(), any(), eq(new BigDecimal("30")));
    }

    @Test
    void doneRowsDoNotBlock_aTopUpOnTheSameLineAndTankIsSeeded() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));

        // only an IN_PROGRESS row blocks; DONE (loaded earlier) / REVERSED / REJECTED rows never do, so a
        // reload can top up the same order line (and tank) for what is still short
        verify(jdbc).queryForObject(argThat((String s) -> s.contains("AND status = 'IN_PROGRESS'")
                        && !s.contains("'DONE'") && !s.contains("REVERSED") && !s.contains("REJECTED")),
                eq(Integer.class), any(Object[].class));
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void reloadSkippingFirstWeighment_seedsWhenQcPreloadGoesStraightToLoadingCompleted() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "LOADING_COMPLETED");

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void otherTargetStates_doNothing() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_REJECTED");
        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void livePayload_unloadAllocations_savedAsInProgressPlanRows_evenWithNoNewLoading() {
        // Shape captured off a live reload transition (instance 2040): no NEW loading allocation (tank_location blank on
        // the parent lines; tankLocationAllocations carries parent-shaped objects), but tankUnloadAllocations has the plan.
        WorkflowInstance inst = instance("M10.TankerOutbound", "LOADING_COMPLETED");
        Map<String, Object> p1 = new HashMap<>();
        p1.put("sku_code", "SKU1013"); p1.put("order_id", "OR2256762"); p1.put("order_qty", 20);
        Map<String, Object> bag = bagWith(List.of(p1));
        pageData(bag).put("tankLocationAllocations", List.of(new HashMap<>(Map.of("sku_code", "SKU1013", "order_qty", 20))));
        pageData(bag).put("tankUnloadAllocations", List.of(
                Map.of("order_dispatch_detail_id", 3886, "tank_location", "COT-5", "alloc_qty", "10"),
                Map.of("order_dispatch_detail_id", 3886, "tank_location", "COT-8", "alloc_qty", "10")));
        when(jdbc.queryForMap(contains("FROM m5_order_dispatch_detail"), eq(1L), eq(3886L)))
                .thenReturn(Map.of("route_id", "ROU638342", "order_id", "OR2256762", "sku_code", "SKU1013", "sku_desc", "SF ACID OIL"));
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(888L);

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        // the previous plan is replaced, the parent row is ensured with the planned total (20)
        verify(jdbc).update(contains("DELETE FROM unloading_progress_detail_bay"), eq(1L), eq(1495L));
        // PLAN rows are IN_PROGRESS + plan_source QC_PRELOAD
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail ("),
                eq(1L), eq(1495L), eq("M10.TankerOutbound"), eq("LOADING_COMPLETED"), any(), any(), eq(new BigDecimal("20")));
        // one plan row per destination tank
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("ROU638342"), eq("OR2256762"), eq("SKU1013"), eq("SF ACID OIL"),
                eq("COT-5"), eq(new BigDecimal("10")), isNull(), isNull());
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("ROU638342"), eq("OR2256762"), eq("SKU1013"), eq("SF ACID OIL"),
                eq("COT-8"), eq(new BigDecimal("10")), isNull(), isNull());
        // and nothing was loaded
        verify(jdbc, never()).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void reloadGrid_alreadyLoadedSlots_areNotSeededAgain_onlyTheNewAllocationIs() {
        // the grid is pre-filled from what is already loaded (status DONE + the loading row id) -- loading it again would
        // double the oil; only the genuinely new slot (ROT-2) is seeded
        WorkflowInstance inst = instance("M10.TankerOutbound", "LOADING_COMPLETED");
        Map<String, Object> parent = new HashMap<>();
        parent.put("sku_code", "SKU1"); parent.put("order_id", "ORD1"); parent.put("route_id", "ROU1");
        parent.put("sku_desc", "SKU1 Desc"); parent.put("id", 501); parent.put("order_qty", 100);
        Map<String, Object> bag = bagWith(List.of(parent));
        Map<String, Object> done = new HashMap<>(Map.of("order_dispatch_detail_id", 501, "tank_location", "ROT-1", "alloc_qty", "60", "status", "DONE"));
        Map<String, Object> withId = new HashMap<>(Map.of("order_dispatch_detail_id", 501, "tank_location", "ROT-1", "alloc_qty", "10", "id", 1379));
        Map<String, Object> fresh = new HashMap<>(Map.of("order_dispatch_detail_id", 501, "tank_location", "ROT-2", "alloc_qty", "30"));
        pageData(bag).put("tankLocationAllocations", List.of(done, withId, fresh));

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc, times(1)).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-2"), eq(new BigDecimal("30")), isNull(), isNull());
    }

    @Test
    void unloadRows_orderSheetRowGone_fallBackToTheSkuRouteOrderTheUiSendsOnEachRow() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "LOADING_COMPLETED");
        Map<String, Object> bag = bagWith(List.of(new HashMap<>(Map.of("sku_code", "S", "order_qty", 1))));
        pageData(bag).put("tankUnloadAllocations", List.of(new HashMap<>(Map.of(
                "order_dispatch_detail_id", 777, "tank_location", "COT-5", "alloc_qty", "10",
                "sku_code", "SKU1013", "sku_desc", "SF ACID OIL", "order_id", "OR22", "route_id", "ROU6"))));
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(888L);   // queryForMap unstubbed -> order-sheet row not found

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("ROU6"), eq("OR22"), eq("SKU1013"), eq("SF ACID OIL"),
                eq("COT-5"), eq(new BigDecimal("10")), isNull(), isNull());
    }

    @Test
    void uiUnloadPayload_everyKeyItSends_isUsed_rdCodeAndNameSavedToo() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "LOADING_COMPLETED");
        Map<String, Object> bag = bagWith(List.of(new HashMap<>(Map.of("sku_code", "SKU1013", "order_qty", 20))));
        Map<String, Object> row = new HashMap<>();
        row.put("order_dispatch_detail_id", 3886); row.put("tank_location", "COT-5"); row.put("alloc_qty", "10");
        row.put("sku_code", "SKU1013"); row.put("sku_desc", "SF ACID OIL"); row.put("order_id", "ORD1");
        row.put("rd_code", "RD2"); row.put("rd_desc", "Navi Mumbai"); row.put("route_id", "ROU1");
        pageData(bag).put("tankUnloadAllocations", List.of(row));
        when(jdbc.queryForMap(contains("FROM m5_order_dispatch_detail"), eq(1L), eq(3886L)))
                .thenReturn(Map.of("route_id", "ROU1", "order_id", "ORD1", "sku_code", "SKU1013", "sku_desc", "SF ACID OIL"));
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class))).thenReturn(888L);

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(888L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1013"), eq("SF ACID OIL"),
                eq("COT-5"), eq(new BigDecimal("10")), eq("RD2"), eq("Navi Mumbai"));
    }

    @Test
    void unloadAllocation_withUnknownDetailOrBlankTankOrZeroQty_isSkipped() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "LOADING_COMPLETED");
        Map<String, Object> bag = bagWith(List.of(new HashMap<>(Map.of("sku_code", "S", "order_qty", 1))));
        pageData(bag).put("tankUnloadAllocations", List.of(
                Map.of("order_dispatch_detail_id", 999, "tank_location", "COT-5", "alloc_qty", "10"),   // detail not found
                Map.of("order_dispatch_detail_id", 3886, "tank_location", "", "alloc_qty", "10"),         // no tank
                Map.of("order_dispatch_detail_id", 3886, "tank_location", "COT-5", "alloc_qty", "0")));   // no qty

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc, never()).update(contains("unloading_progress_detail"), any(Object[].class));
    }

    @Test
    void noAllocationsListAtAll_fallsBackToSingleTankLocation() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bagWith(List.of(line("SKU1", "ROT-1", "20"))));

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-1"), eq(new BigDecimal("20")), isNull(), isNull());
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
                eq("ROT-1"), eq(new BigDecimal("60")), isNull(), isNull());
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU1"), eq("ORD1"), eq("SKU1"), eq("SKU1 Desc"),
                eq("ROT-2"), eq(new BigDecimal("40")), isNull(), isNull());
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
                eq("ROT-3"), eq(new BigDecimal("25")), isNull(), isNull());
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
                eq("ROT-1"), eq(new BigDecimal("20")), isNull(), isNull());
    }

    @Test
    void livePayload_parentLinesHaveNoIdOrRouteId_childRowsResolvedViaOrderDispatchDetail() {
        // Shape captured off a live transition: parent lines carry sku_code/order_id/order_qty
        // only (no id, no route_id, no tank_location); the child list is the only reliable key.
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> p1 = new HashMap<>();
        p1.put("sku_code", "SKU1393"); p1.put("order_id", "ORD110642116"); p1.put("order_qty", 2000);
        Map<String, Object> p2 = new HashMap<>();
        p2.put("sku_code", "SKU1325"); p2.put("order_id", "ORD110871116"); p2.put("order_qty", 1000);
        Map<String, Object> bag = bagWith(List.of(p1, p2));
        pageData(bag).put("tankLocationAllocations", List.of(
                Map.of("order_dispatch_detail_id", 3828, "tank_location", "COT-1", "alloc_qty", "1000"),
                Map.of("order_dispatch_detail_id", 3828, "tank_location", "COT-4", "alloc_qty", "1000"),
                Map.of("order_dispatch_detail_id", 3829, "tank_location", "COT-5", "alloc_qty", "1000")));
        when(jdbc.queryForMap(contains("FROM m5_order_dispatch_detail"), eq(1L), eq(3828L)))
                .thenReturn(Map.of("route_id", "ROU-A", "order_id", "ORD110642116", "sku_code", "SKU1393", "sku_desc", "REF RICE BRAN OIL"));
        when(jdbc.queryForMap(contains("FROM m5_order_dispatch_detail"), eq(1L), eq(3829L)))
                .thenReturn(Map.of("route_id", "ROU-A", "order_id", "ORD110871116", "sku_code", "SKU1325", "sku_desc", "SF ACID OIL"));

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU-A"), eq("ORD110642116"), eq("SKU1393"), eq("REF RICE BRAN OIL"),
                eq("COT-1"), eq(new BigDecimal("1000")), isNull(), isNull());
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU-A"), eq("ORD110642116"), eq("SKU1393"), eq("REF RICE BRAN OIL"),
                eq("COT-4"), eq(new BigDecimal("1000")), isNull(), isNull());
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU-A"), eq("ORD110871116"), eq("SKU1325"), eq("SF ACID OIL"),
                eq("COT-5"), eq(new BigDecimal("1000")), isNull(), isNull());
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail"),
                eq(1L), eq(1495L), any(), any(), any(), any(), eq(new BigDecimal("3000")));
    }

    @Test
    void livePayload_orderDispatchDetailMissing_allocationSkippedNotCrashed() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> p1 = new HashMap<>();
        p1.put("sku_code", "SKU1393"); p1.put("order_id", "ORD1");
        Map<String, Object> bag = bagWith(List.of(p1));
        pageData(bag).put("tankLocationAllocations", List.of(
                Map.of("order_dispatch_detail_id", 4040, "tank_location", "COT-1", "alloc_qty", "10")));
        // queryForMap unstubbed -> null, same as a missing row

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc, never()).update(contains("INSERT INTO loading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void diffAndReason_onParentLine_savedOnEveryTankRowOfThatSku() {
        WorkflowInstance inst = instance("M10.TankerOutbound", "QC_PRELOAD_APPROVED");
        Map<String, Object> p1 = new HashMap<>();
        p1.put("sku_code", "SKU1393"); p1.put("order_id", "ORD1");
        p1.put("diff_box", "-50"); p1.put("diff_reason", "Short in tank");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("sku_code", "SKU1325"); p2.put("order_id", "ORD2");
        p2.put("diff_box", ""); p2.put("diff_reason", ""); // untouched -> no diff
        Map<String, Object> bag = bagWith(List.of(p1, p2));
        pageData(bag).put("tankLocationAllocations", List.of(
                Map.of("order_dispatch_detail_id", 1, "tank_location", "COT-1", "alloc_qty", "10"),
                Map.of("order_dispatch_detail_id", 1, "tank_location", "COT-4", "alloc_qty", "20"),
                Map.of("order_dispatch_detail_id", 2, "tank_location", "COT-5", "alloc_qty", "30")));
        when(jdbc.queryForMap(contains("FROM m5_order_dispatch_detail"), eq(1L), eq(1L)))
                .thenReturn(Map.of("route_id", "R", "order_id", "ORD1", "sku_code", "SKU1393", "sku_desc", "d1"));
        when(jdbc.queryForMap(contains("FROM m5_order_dispatch_detail"), eq(1L), eq(2L)))
                .thenReturn(Map.of("route_id", "R", "order_id", "ORD2", "sku_code", "SKU1325", "sku_desc", "d2"));

        listener.onTransition(inst, "DISPATCH_LOADING_PLANNING", bag);

        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("R"), eq("ORD1"), eq("SKU1393"), eq("d1"),
                eq("COT-1"), eq(new BigDecimal("10")), eq(new BigDecimal("-50")), eq("Short in tank"));
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("R"), eq("ORD1"), eq("SKU1393"), eq("d1"),
                eq("COT-4"), eq(new BigDecimal("20")), eq(new BigDecimal("-50")), eq("Short in tank"));
        verify(jdbc).update(contains("INSERT INTO loading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("R"), eq("ORD2"), eq("SKU1325"), eq("d2"),
                eq("COT-5"), eq(new BigDecimal("30")), isNull(), isNull());
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
