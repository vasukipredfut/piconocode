package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TankerUnloadCaptureActionHandlerTest {

    private JdbcTemplate jdbc;
    private TankerUnloadCaptureActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        handler = new TankerUnloadCaptureActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private void openRows(Map<String, Object> a, Map<String, Object> b) {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class))).thenReturn(b == null ? List.of(a) : List.of(a, b));
    }

    /** loading consumed {@code qty} of lot L-&lt;tank&gt; from each listed tank (qty per tank). */
    private void consumed(Map<String, String> qtyByTank) {
        when(jdbc.queryForList(contains("kind = 'CONSUME'"), any(Object[].class))).thenAnswer(inv -> {
            Object[] a = inv.getArguments();
            String bin = (String) a[a.length - 1];
            String q = qtyByTank.get(bin);
            return q == null ? List.of() : List.of(row("lot_no", "L-" + bin, "uom", "KG", "qty", new BigDecimal(q)));
        });
    }

    private ActionResult unload(Map<String, Object> theLine) {
        return handler.handle(ActionInvocation.builder().instanceId(2028L)
                .formData(Map.of("tankerUnloadItems", List.of(theLine))).build());
    }

    private static Map<String, Object> line(Object qty, String tank) {
        Map<String, Object> l = new LinkedHashMap<>();
        l.put("route_id", "ROU1");
        l.put("order_id", "ORD1");
        l.put("sku_code", "SKU1");
        l.put("unloading_qty", qty);
        if (tank != null) l.put("tank_location", tank);
        return l;
    }

    private List<Object[]> updates(String sqlPart) {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(Invocation::getArguments)
                .filter(a -> ((String) a[0]).contains(sqlPart))
                .toList();
    }

    @Test
    void code_isTankerUnloadCapture() {
        assertThat(handler.code()).isEqualTo("m10.tanker-unload-capture");
    }

    @Test
    void missingInstanceOrItems_errors() {
        assertThat(handler.handle(ActionInvocation.builder().build()).getMessage()).contains("instanceId required");
        assertThat(handler.handle(ActionInvocation.builder().instanceId(1L).formData(Map.of()).build()).getMessage())
                .contains("tankerUnloadItems must be a non-empty array");
    }

    @Test
    void totalQty_splitAcrossTanksByWhatEachGave_stockBackIntoEachTankBin() {
        openRows(row("id", 11L, "bay_name", "TANK-1", "quantity", new BigDecimal("1000")),
                row("id", 12L, "bay_name", "TANK-2", "quantity", new BigDecimal("800")));
        consumed(Map.of("TANK-1", "1000", "TANK-2", "800"));

        ActionResult r = unload(line(1800, null));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        // sql, tenant, kind, item, lot, to_bin, qty, uom, ref_type, ref_id, ref_number
        assertThat(moves.get(0)[6]).isEqualTo("TANK-1");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("1000");
        assertThat(moves.get(0)[9]).isEqualTo("TANKER_UNLOAD");
        assertThat(moves.get(1)[6]).isEqualTo("TANK-2");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("800");
        assertThat(updates("status = 'DONE'")).hasSize(2);
        assertThat(updates("SET status = 'REVERSED'")).hasSize(2);
    }

    @Test
    void tankNamed_onlyThatTankUnloads() {
        openRows(row("id", 11L, "bay_name", "TANK-1", "quantity", new BigDecimal("1000")),
                row("id", 12L, "bay_name", "TANK-2", "quantity", new BigDecimal("800")));
        consumed(Map.of("TANK-2", "800"));

        ActionResult r = unload(line(800, "TANK-2"));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(1);
        assertThat(moves.get(0)[6]).isEqualTo("TANK-2");
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
    }

    @Test
    void moreThanLoaded_isSavedAsEntered_theSurplusGoesInAsLotlessStock() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));

        ActionResult r = unload(line(2500, "COT-9"));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        // sql, tenant, kind, item, lot, from_bin, to_bin, qty, uom, ref_type, ref_id, ref_number
        assertThat(moves.get(0)[4]).isEqualTo("L-COT-9");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("2000");
        assertThat(moves.get(1)[4]).isNull();
        assertThat(moves.get(1)[6]).isEqualTo("COT-9");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("500");
        // the tank counts as given back: its unload row is DONE and its loading row REVERSED
        assertThat(updates("status = 'DONE'")).hasSize(1);
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
    }

    @Test
    void zeroQty_isStillRefused_nothingWritten() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));

        ActionResult r = unload(line(0, "COT-9"));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("needs unloading_qty > 0");
        assertThat(updates("INSERT INTO m5_stock_movement")).isEmpty();
    }

    // ---- loading rows keep what was loaded and count what came back (unloaded_quantity, V375) ----

    @Test
    void oneOfTwoLoadedTanks_unloadedInPart_onlyThatTanksLoadingRowCountsIt() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));

        ActionResult r = unload(line(1000, "COT-1"));

        assertThat(r.isOk()).isTrue();
        List<Object[]> counted = updates("SET unloaded_quantity = COALESCE(unloaded_quantity, 0) + ?");
        assertThat(counted).hasSize(1);
        // sql, qty, tenant, instance, route, sku, bay
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("1000");
        assertThat(counted.get(0)[5]).isEqualTo("SKU1");
        assertThat(counted.get(0)[6]).isEqualTo("COT-1");
        // the loaded quantity is never touched, and the half-unloaded tank is not reversed
        assertThat(updates("UPDATE loading_progress_detail_bay SET processed_quantity")).isEmpty();
        assertThat(updates("SET status = 'REVERSED'")).isEmpty();
    }

    @Test
    void totalAcrossTwoTanks_eachLoadingRowCountsItsOwnShare() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));

        unload(line(2500, null));

        List<Object[]> counted = updates("SET unloaded_quantity = COALESCE(unloaded_quantity, 0) + ?");
        assertThat(counted).hasSize(2);
        assertThat(counted.get(0)[6]).isEqualTo("COT-1");
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("1500");
        assertThat(counted.get(1)[6]).isEqualTo("COT-2");
        assertThat((BigDecimal) counted.get(1)[1]).isEqualByComparingTo("1000");
    }

    @Test
    void toLocation_countsAgainstTheOriginTank_notTheDestination() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")), null);
        consumed(Map.of("COT-1", "3000"));
        when(jdbc.queryForObject(contains("m10_tanker_location"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        Map<String, Object> l = line(1200, "COT-1");
        l.put("to_location", "ROT-2");

        unload(l);

        List<Object[]> counted = updates("SET unloaded_quantity = COALESCE(unloaded_quantity, 0) + ?");
        assertThat(counted).hasSize(1);
        assertThat(counted.get(0)[6]).isEqualTo("COT-1");
    }

    @Test
    void parentTotal_isRecomputedFromItsRows() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")), null);
        consumed(Map.of("COT-1", "3000"));

        unload(line(1000, "COT-1"));

        assertThat(updates("UPDATE loading_progress_detail SET unloaded_quantity")).hasSize(1);
    }

    @Test
    void smallerUnload_tankStaysOpen() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));

        ActionResult r = unload(line(1500, "COT-9"));

        assertThat(r.isOk()).isTrue();
        assertThat(updates("INSERT INTO m5_stock_movement")).hasSize(1);
        assertThat(updates("status = 'DONE'")).isEmpty();
        assertThat(updates("SET status = 'REVERSED'")).isEmpty();
    }

    @Test
    void smallerUnload_withFinalFlag_closesTank() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));
        Map<String, Object> l = line(1998, "COT-9");
        l.put("final", true);

        ActionResult r = unload(l);

        assertThat(r.isOk()).isTrue();
        assertThat(updates("status = 'DONE'")).hasSize(1);
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
    }

    @Test
    void earlierReturnsNettedOff() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));
        when(jdbc.queryForList(contains("kind = 'RETURN'"), any(Object[].class)))
                .thenReturn(List.of(row("lot_no", "L-COT-9", "qty", new BigDecimal("1500"))));

        // only 500 is still returnable by lot (2000 loaded - 1500 already back); 600 entered: 500 by lot + 100 lot-less surplus
        assertThat(unload(line(600, "COT-9")).isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0)[4]).isEqualTo("L-COT-9");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("500");
        assertThat(moves.get(1)[4]).isNull();
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("100");

        clearInvocations(jdbc);
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        assertThat(unload(line(500, "COT-9")).isOk()).isTrue();
        assertThat(updates("INSERT INTO m5_stock_movement")).hasSize(1);  // exactly what is left, no surplus
    }

    @Test
    void noOpenUnloadRow_errors() {
        ActionResult r = unload(line(100, "COT-9"));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("nothing loaded to unload");
    }

    @Test
    void firstSave_opensUnloadRowsFromLoadedRows_thenUnloads() {
        // no unload row yet -> handler seeds from the loaded (DONE) tank rows, then re-reads them
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of())
                .thenReturn(List.of(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000"))));
        when(jdbc.queryForList(contains("FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row("sku_desc", "Oil", "bay_name", "COT-9", "quantity", new BigDecimal("2000"))));
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class))).thenReturn(7L);
        consumed(Map.of("COT-9", "2000"));

        ActionResult r = unload(line(2000, "COT-9"));

        assertThat(r.isOk()).isTrue();
        assertThat(updates("INSERT INTO unloading_progress_detail_bay")).hasSize(1);
        assertThat(updates("SET status = 'UNLOAD_IN_PROGRESS'")).hasSize(1);
        assertThat(updates("INSERT INTO m5_stock_movement")).hasSize(1);
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
    }

    @Test
    void toLocation_putsStockIntoDifferentTank_fromBinStillTheOrigin() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));
        when(jdbc.queryForObject(contains("m10_tanker_location"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        Map<String, Object> l = line(2000, "COT-9");
        l.put("to_location", "COT-3");

        ActionResult r = unload(l);

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(1);
        // sql, tenant, kind, item, lot, from_bin, to_bin, qty, uom, ref_type, ref_id, ref_number
        assertThat(moves.get(0)[5]).isEqualTo("COT-9");
        assertThat(moves.get(0)[6]).isEqualTo("COT-3");
        assertThat(updates("m5_stock_balance").get(0)[6]).isEqualTo("COT-3");
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
    }

    @Test
    void toLocation_notAnActiveTank_refusedNothingWritten() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));
        when(jdbc.queryForObject(contains("m10_tanker_location"), eq(Integer.class), any(Object[].class))).thenReturn(0);
        Map<String, Object> l = line(2000, "COT-9");
        l.put("to_location", "NOPE");

        ActionResult r = unload(l);

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("not an active tank location");
        assertThat(updates("INSERT INTO m5_stock_movement")).isEmpty();
    }

    @Test
    void tankLocations_oneTotalSplitOverJustTheSelectedTanks_otherTankStaysOpen() {
        openRows(row("id", 11L, "bay_name", "T1", "quantity", new BigDecimal("4000")),
                row("id", 12L, "bay_name", "T2", "quantity", new BigDecimal("3000")));
        // a third open row exists but is not selected
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row("id", 11L, "bay_name", "T1", "quantity", new BigDecimal("4000")),
                        row("id", 12L, "bay_name", "T2", "quantity", new BigDecimal("3000")),
                        row("id", 13L, "bay_name", "T3", "quantity", new BigDecimal("2000"))));
        consumed(Map.of("T1", "4000", "T2", "3000", "T3", "2000"));
        Map<String, Object> l = line(7000, null);
        l.put("tank_locations", List.of("T1", "T2"));

        ActionResult r = unload(l);

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);                           // T3 never touched
        assertThat(moves.get(0)[6]).isEqualTo("T1");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("4000");
        assertThat(moves.get(1)[6]).isEqualTo("T2");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("3000");
        assertThat(updates("SET status = 'REVERSED'")).hasSize(2);
    }

    @Test
    void tankLocations_aNamedTankWithNoOpenRow_errors() {
        openRows(row("id", 11L, "bay_name", "T1", "quantity", new BigDecimal("4000")), null);
        Map<String, Object> l = line(100, null);
        l.put("tank_locations", "T1,T9");

        ActionResult r = unload(l);

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload row for tank T9");
        assertThat(updates("INSERT INTO m5_stock_movement")).isEmpty();
    }

    @Test
    void loggedInUser_isStampedOnTheUnloadedTankRow() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        new com.aria.auth.AuthUserDetails(42L, "officer", "x", 1L), "n/a", List.of()));
        try {
            unload(line(2000, "COT-9"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
        verify(jdbc).update("UPDATE unloading_progress_detail_bay SET unloaded_by_id = ? WHERE tenant_id = ? AND id = ?", 42L, 1L, 11L);
    }

    // ---- QC preload unload plan (plan_source QC_PRELOAD): bay_name is the DESTINATION tank ----

    private Map<String, Object> planRow(long id, String destTank, String planned, String done) {
        Map<String, Object> m = row("id", id, "bay_name", destTank, "quantity", new BigDecimal(planned), "plan_source", "QC_PRELOAD");
        m.put("processed_quantity", done == null ? null : new BigDecimal(done));
        return m;
    }

    private void loadedFrom(String... tanks) {
        when(jdbc.queryForList(contains("SELECT DISTINCT bay_name FROM loading_progress_detail_bay"), eq(String.class), any(Object[].class)))
                .thenReturn(List.of(tanks));
    }

    @Test
    void plan_oilLoadedInCot1_isTakenOutOfCot1_andPutIntoThePlannedTanks() {
        // 20 loaded from COT-1; the QC planned COT-5: 10 and COT-8: 10
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(planRow(31L, "COT-5", "10", null), planRow(32L, "COT-8", "10", null)));
        loadedFrom("COT-1");
        consumed(Map.of("COT-1", "20"));

        ActionResult r = unload(line(20, null));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        // sql, tenant, kind, item, lot, from_bin, to_bin, qty, uom, ref_type, ref_id, ref_number
        assertThat(moves.get(0)[5]).isEqualTo("COT-1");
        assertThat(moves.get(0)[6]).isEqualTo("COT-5");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("10");
        assertThat(moves.get(1)[5]).isEqualTo("COT-1");
        assertThat(moves.get(1)[6]).isEqualTo("COT-8");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("10");
        assertThat(updates("status = 'DONE'")).hasSize(2);                 // both plan rows filled
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);         // COT-1 gave everything back
    }

    @Test
    void plan_managerUnloadRows_areHandledLikeQcPreloadPlanRows_destinationTank() {
        // a deviation unload planned at pre-load is tagged MANAGER_UNLOAD; bay_name is the DESTINATION tank there too
        Map<String, Object> p1 = planRow(31L, "COT-5", "10", null);
        Map<String, Object> p2 = planRow(32L, "COT-8", "10", null);
        p1.put("plan_source", "MANAGER_UNLOAD");
        p2.put("plan_source", "MANAGER_UNLOAD");
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class))).thenReturn(List.of(p1, p2));
        loadedFrom("COT-1");
        consumed(Map.of("COT-1", "20"));

        ActionResult r = unload(line(20, null));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0)[5]).isEqualTo("COT-1");
        assertThat(moves.get(0)[6]).isEqualTo("COT-5");
        assertThat(moves.get(1)[6]).isEqualTo("COT-8");
        assertThat(updates("status = 'DONE'")).hasSize(2);
    }

    @Test
    void plan_aNamedDestination_unloadsJustThatPlanRow_originStaysLoaded() {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(planRow(31L, "COT-5", "10", null), planRow(32L, "COT-8", "10", null)));
        loadedFrom("COT-1");
        consumed(Map.of("COT-1", "20"));
        Map<String, Object> l = line(10, "COT-5");

        ActionResult r = unload(l);

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(1);
        assertThat(moves.get(0)[6]).isEqualTo("COT-5");
        assertThat(updates("status = 'DONE'")).hasSize(1);                 // only COT-5's plan row
        assertThat(updates("SET status = 'REVERSED'")).isEmpty();          // COT-1 still has 10 to give back
    }

    @Test
    void plan_moreThanThePlanStillNeeds_orMoreThanLoaded_isSavedAsEntered() {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(planRow(31L, "COT-5", "10", "4")));   // only 6 more planned
        loadedFrom("COT-1");
        consumed(Map.of("COT-1", "20"));

        // 8 entered although the plan needs 6 more: saved as entered, the plan row closes
        ActionResult aboveThePlan = unload(line(8, null));
        assertThat(aboveThePlan.isOk()).isTrue();
        assertThat(updates("INSERT INTO m5_stock_movement")).hasSize(1);
        assertThat(updates("status = 'DONE'")).hasSize(1);

        // 6 entered although loading drew only 5: 5 go back by lot, the extra 1 goes in as lot-less stock
        clearInvocations(jdbc);
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(planRow(31L, "COT-5", "10", "4")));
        consumed(Map.of("COT-1", "5"));
        ActionResult aboveTheLoad = unload(line(6, null));
        assertThat(aboveTheLoad.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("5");
        assertThat(moves.get(1)[4]).isNull();                              // lot-less surplus
        assertThat(moves.get(1)[6]).isEqualTo("COT-5");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("1");
        // the origin tank gave everything back (5 of 5): its loading row is REVERSED; the plan row is DONE
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
        assertThat(updates("status = 'DONE'")).hasSize(1);
    }

    @Test
    void plan_nothingLoaded_orUnknownDestination_errors() {
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(planRow(31L, "COT-5", "10", null)));
        loadedFrom();                                                       // no origin tank: nothing to draw from, the qty goes in as entered
        ActionResult none = unload(line(5, null));
        assertThat(none.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(1);
        assertThat(moves.get(0)[4]).isNull();
        assertThat(moves.get(0)[6]).isEqualTo("COT-5");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("5");

        loadedFrom("COT-1");
        consumed(Map.of("COT-1", "20"));
        ActionResult unknown = unload(line(5, "COT-9"));
        assertThat(unknown.isOk()).isFalse();
        assertThat(unknown.getMessage()).contains("no open unload plan row for tank COT-9");
    }

    @Test
    void unknownTank_errors() {
        openRows(row("id", 11L, "bay_name", "TANK-1", "quantity", new BigDecimal("1000")), null);
        ActionResult r = unload(line(100, "TANK-9"));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload row for tank TANK-9");
    }
}
