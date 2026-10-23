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

    /** The tanks the SKU was loaded from (first loaded first). Without a QC plan the unload works off these, not off unload rows. */
    private void openRows(Map<String, Object> a, Map<String, Object> b) {
        List<String> tanks = new java.util.ArrayList<>();
        tanks.add((String) a.get("bay_name"));
        if (b != null) tanks.add((String) b.get("bay_name"));
        when(jdbc.queryForList(contains("SELECT bay_name FROM loading_progress_detail_bay"), eq(String.class), any(Object[].class))).thenReturn(tanks);
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

    // ---- loading rows keep what was loaded and count what came back (unloaded_quantity, V375), first loaded first ----

    private void loadingRows(Map<String, Object>... rows) {
        when(jdbc.queryForList(contains("SELECT id, processed_quantity, unloaded_quantity FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(rows));
    }

    private static Map<String, Object> loadingRow(long id, String processed, String unloaded) {
        return row("id", id, "processed_quantity", new BigDecimal(processed), "unloaded_quantity", new BigDecimal(unloaded));
    }

    private List<Object[]> counted() {
        return updates("UPDATE loading_progress_detail_bay SET unloaded_quantity = COALESCE(unloaded_quantity, 0) + ?");
    }

    @Test
    void fifo_oneTotal_takenFromTheFirstLoadedTankFirst_thenTheNext_noRatio() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));

        ActionResult r = unload(line(3500, null));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0)[5]).isEqualTo("COT-1");                                   // from_bin: first loaded
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("3000");            // all of it, not 2100
        assertThat(moves.get(1)[5]).isEqualTo("COT-2");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("500");             // only the rest, not 1400
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);                        // COT-1 is empty, COT-2 is not
    }

    @Test
    void fifo_aSmallUnload_staysInTheFirstTank_theSecondIsNotTouched() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));

        unload(line(1000, null));

        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(1);
        assertThat(moves.get(0)[5]).isEqualTo("COT-1");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("1000");
        assertThat(updates("SET status = 'REVERSED'")).isEmpty();
    }

    @Test
    void theSelectedLocation_isWhereTheOilGoes_notWhichTankItComesFrom() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));
        when(jdbc.queryForObject(contains("m10_tanker_location"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        Map<String, Object> l = line(3500, null);
        l.put("tank_locations", List.of("COT-2"));   // no longer narrows the source
        l.put("to_location", "ROT-9");

        unload(l);

        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0)[5]).isEqualTo("COT-1");
        assertThat(moves.get(0)[6]).isEqualTo("ROT-9");
        assertThat(moves.get(1)[5]).isEqualTo("COT-2");
        assertThat(moves.get(1)[6]).isEqualTo("ROT-9");
    }

    @Test
    void aTankLocationGiven_isTheDestination_andEveryLoadedTankStillGivesFifo() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));

        unload(line(3500, "COT-2"));

        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0)[5]).isEqualTo("COT-1");
        assertThat(moves.get(0)[6]).isEqualTo("COT-2");
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("3000");
    }

    @Test
    void loadingRowsCountWhatCameBack_firstLoadedRowFirst_eachUpToItsLoadedQty() {
        // COT-1 was loaded in two rows: 446.429 then 267.857 -- 1000 coming back fills the first, then the second, rest on the last
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("2500")), null);
        consumed(Map.of("COT-1", "1000"));
        loadingRows(loadingRow(1, "446.429", "0"), loadingRow(2, "267.857", "0"));

        unload(line(1000, null));

        List<Object[]> counted = counted();
        assertThat(counted).hasSize(2);
        // sql, take, tenant, id
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("446.429");
        assertThat(counted.get(0)[3]).isEqualTo(1L);
        assertThat((BigDecimal) counted.get(1)[1]).isEqualByComparingTo("553.571");   // 267.857 + the surplus on the last row
        assertThat(counted.get(1)[3]).isEqualTo(2L);
    }

    @Test
    void aPartUnload_countsOnTheFirstRowOnly_theLoadedQuantityIsNeverTouched() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")), null);
        consumed(Map.of("COT-1", "3000"));
        loadingRows(loadingRow(1, "2000", "0"), loadingRow(2, "1000", "0"));

        unload(line(1500, null));

        List<Object[]> counted = counted();
        assertThat(counted).hasSize(1);
        assertThat((BigDecimal) counted.get(0)[1]).isEqualByComparingTo("1500");
        assertThat(counted.get(0)[3]).isEqualTo(1L);
        assertThat(updates("UPDATE loading_progress_detail_bay SET processed_quantity")).isEmpty();
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
        assertThat(updates("SET status = 'REVERSED'")).isEmpty();       // the origin tank still holds some, its loading row stays
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
    void firstSave_createsTheDestinationsUnloadRow_noRowPerOriginTank() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));
        when(jdbc.queryForObject(contains("m10_tanker_location"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class))).thenReturn(7L);
        // the destination has no row yet: the update touches nothing, so the row is inserted
        when(jdbc.update(contains("UPDATE unloading_progress_detail_bay SET quantity"), any(Object[].class))).thenReturn(0);
        Map<String, Object> l = line(3500, null);
        l.put("to_location", "ROT-9");

        ActionResult r = unload(l);

        assertThat(r.isOk()).isTrue();
        List<Object[]> inserted = updates("INSERT INTO unloading_progress_detail_bay");
        assertThat(inserted).hasSize(2);   // the first save for ROT-9 (3000 from COT-1), then 500 from COT-2 re-tries and also finds none
        // sql, tenant, parent, instance, route, order, sku, desc, bay_name (destination), quantity, processed
        assertThat(inserted.get(0)[8]).isEqualTo("ROT-9");
        assertThat((BigDecimal) inserted.get(0)[9]).isEqualByComparingTo("3000");
        assertThat(updates("SET status = 'UNLOAD_IN_PROGRESS'")).isEmpty();
    }

    @Test
    void theUnloadTableKeepsTheDestination_oneRowPerPlaceTheOilWent_growingOnEachSave() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));
        when(jdbc.queryForObject(contains("m10_tanker_location"), eq(Integer.class), any(Object[].class))).thenReturn(1);
        Map<String, Object> l = line(3500, null);
        l.put("to_location", "ROT-9");

        unload(l);

        // both origin tanks' oil went to ROT-9: the one destination row is grown twice (3000, then 500), never keyed by COT-1 / COT-2
        List<Object[]> grown = updates("UPDATE unloading_progress_detail_bay SET quantity");
        assertThat(grown).hasSize(2);
        // sql, qty, qty, tenant, instance, route, order, order, sku, bay_name
        assertThat((BigDecimal) grown.get(0)[1]).isEqualByComparingTo("3000");
        assertThat(grown.get(0)[9]).isEqualTo("ROT-9");
        assertThat((BigDecimal) grown.get(1)[1]).isEqualByComparingTo("500");
        assertThat(grown.get(1)[9]).isEqualTo("ROT-9");
        assertThat((String) grown.get(0)[0]).contains("status = 'DONE'").contains("plan_source IS NULL");
    }

    @Test
    void withNoLocationSelected_eachOriginTankGetsItsOwnUnloadRow() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")),
                row("id", 12L, "bay_name", "COT-2", "quantity", new BigDecimal("2000")));
        consumed(Map.of("COT-1", "3000", "COT-2", "2000"));

        unload(line(3500, null));

        List<Object[]> grown = updates("UPDATE unloading_progress_detail_bay SET quantity");
        assertThat(grown).hasSize(2);
        assertThat(grown.get(0)[9]).isEqualTo("COT-1");
        assertThat(grown.get(1)[9]).isEqualTo("COT-2");
    }

    @Test
    void parentTotalGrowsWithWhatWasUnloaded() {
        openRows(row("id", 11L, "bay_name", "COT-1", "quantity", new BigDecimal("3000")), null);
        consumed(Map.of("COT-1", "3000"));

        unload(line(1000, null));

        List<Object[]> parent = updates("INSERT INTO unloading_progress_detail (");
        assertThat(parent).hasSize(1);
        assertThat((BigDecimal) parent.get(0)[7]).isEqualByComparingTo("1000");
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
    void tankLocations_isIgnored_itIsNoLongerAnOriginFilter() {
        openRows(row("id", 11L, "bay_name", "T1", "quantity", new BigDecimal("4000")), null);
        consumed(Map.of("T1", "4000"));
        Map<String, Object> l = line(100, null);
        l.put("tank_locations", "T1,T9");

        ActionResult r = unload(l);

        assertThat(r.isOk()).isTrue();
        assertThat(updates("INSERT INTO m5_stock_movement")).hasSize(1);
    }

    @Test
    void loggedInUser_isStampedOnTheUnloadedTankRow() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));
        when(jdbc.queryForList(contains("SELECT id FROM unloading_progress_detail_bay"), eq(Long.class), any(Object[].class))).thenReturn(List.of(11L));
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
        when(jdbc.queryForList(contains("SELECT bay_name FROM loading_progress_detail_bay"), eq(String.class), any(Object[].class)))
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
    void plan_loadedTankKnownButNoConsumptionRecorded_theMovementStillComesFromTheLoadedTank() {
        // two plan rows for the same destination; loaded from COT-1 and COT-9 but no per-lot consumption to draw from
        when(jdbc.queryForList(contains("FROM unloading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(planRow(31L, "COT-4", "1000", null), planRow(32L, "COT-4", "3000", null)));
        loadedFrom("COT-1", "COT-9");

        ActionResult r = unload(line(3000, "COT-4"));

        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves).hasSize(2);
        for (Object[] m : moves) {
            assertThat(m[5]).isEqualTo("COT-9");     // from: the loaded tank, not the destination
            assertThat(m[6]).isEqualTo("COT-4");
        }
        assertThat((BigDecimal) moves.get(0)[7]).isEqualByComparingTo("750");
        assertThat((BigDecimal) moves.get(1)[7]).isEqualByComparingTo("2250");
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
    void aTankLocationNotAmongTheLoadedTanks_isJustWhereTheOilGoes() {
        openRows(row("id", 11L, "bay_name", "TANK-1", "quantity", new BigDecimal("1000")), null);
        consumed(Map.of("TANK-1", "1000"));
        ActionResult r = unload(line(100, "TANK-9"));
        assertThat(r.isOk()).isTrue();
        List<Object[]> moves = updates("INSERT INTO m5_stock_movement");
        assertThat(moves.get(0)[5]).isEqualTo("TANK-1");
        assertThat(moves.get(0)[6]).isEqualTo("TANK-9");
    }
}
