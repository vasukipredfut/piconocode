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
        assertThat(moves.get(0)[5]).isEqualTo("TANK-1");
        assertThat((BigDecimal) moves.get(0)[6]).isEqualByComparingTo("1000");
        assertThat(moves.get(0)[8]).isEqualTo("TANKER_UNLOAD");
        assertThat(moves.get(1)[5]).isEqualTo("TANK-2");
        assertThat((BigDecimal) moves.get(1)[6]).isEqualByComparingTo("800");
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
        assertThat(moves.get(0)[5]).isEqualTo("TANK-2");
        assertThat(updates("SET status = 'REVERSED'")).hasSize(1);
    }

    @Test
    void moreThanLoaded_rejected_nothingWritten() {
        openRows(row("id", 11L, "bay_name", "COT-9", "quantity", new BigDecimal("2000")), null);
        consumed(Map.of("COT-9", "2000"));

        ActionResult r = unload(line(2500, "COT-9"));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("is more than was loaded");
        assertThat(updates("INSERT INTO m5_stock_movement")).isEmpty();
        assertThat(updates("m5_stock_balance")).isEmpty();
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

        assertThat(unload(line(600, "COT-9")).isOk()).isFalse();      // only 500 still returnable
        assertThat(unload(line(500, "COT-9")).isOk()).isTrue();
    }

    @Test
    void noOpenUnloadRow_errors() {
        ActionResult r = unload(line(100, "COT-9"));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload row found");
    }

    @Test
    void unknownTank_errors() {
        openRows(row("id", 11L, "bay_name", "TANK-1", "quantity", new BigDecimal("1000")), null);
        ActionResult r = unload(line(100, "TANK-9"));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open unload row for tank TANK-9");
    }
}
