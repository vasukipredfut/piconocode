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
 * Unit test for {@link M10StockInwardStartBayActionHandler} — same mechanics
 * as {@link M10StartUnloadingBayActionHandlerTest} (verified: every
 * validation/error message here is byte-for-byte identical to the shared
 * {@link M10StartUnloadingBayActionHandler}), so this file focuses on the
 * delta: capturing {@code rd_code}/{@code vendor_name} (order-sheet vendor
 * code) per line onto the opened row, for {@link
 * M10StockInwardBayCompleteActionHandler} to read back later.
 */
class M10StockInwardStartBayActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10StockInwardStartBayActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(contains("FROM workflow_instance"), any(Object[].class)))
                .thenReturn(Map.of("workflow_code", "M10.StockInward", "current_state", "UNLOADING_IN_PROGRESS"));
        handler = new M10StockInwardStartBayActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isStockInwardStartBay() {
        assertThat(handler.code()).isEqualTo("m10.stockinward-start-bay");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingBayName_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("formData.bay_name is required");
    }

    @Test
    void alreadyOpenUnloadBayForInstance_refusesToStartAnother() {
        stubOpenUnloadingBayCount(1);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already has an open unload bay");
    }

    // ---- the delta: rd_code / vendor_name capture ----------------------------

    @Test
    void lineWithRdCodeAndVendorName_areStoredOnUnloadBayRow() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        Map<String, Object> l = line("ROU028912", "SKU1215", 20);
        l.put("rd_code", "VEND001");
        l.put("vendor_name", "ABC Traders");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "vehicle_no", "TN86J8070", "gpNumber", "GP-1-1495", "bay_name", "Bay1",
                "order_sku_line_items", List.of(l))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"),
                eq(new BigDecimal("20")), isNull(), isNull(), isNull(), eq("VEND001"), eq("ABC Traders"));
    }

    @Test
    void lineWithoutRdCode_storesNullsForBothFields() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"),
                eq(new BigDecimal("20")), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void godownAndPlantCode_alsoCapturedAlongsideVendorFields() {
        stubOpenUnloadingBayCount(0);
        stubParentId(777L);

        Map<String, Object> l = line("ROU028912", "SKU1215", 20);
        l.put("rd_code", "VEND001");

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "plant_code", "SR0001", "godown_code", "SRG0001",
                "order_sku_line_items", List.of(l))));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"),
                eq(new BigDecimal("20")), isNull(), eq("SRG0001"), eq("SR0001"), eq("VEND001"), isNull());
    }

    private ActionInvocation baseInvocation(Map<String, Object> extraFormData) {
        Map<String, Object> formData = new LinkedHashMap<>(extraFormData);
        return ActionInvocation.builder().instanceId(1495L).formData(formData).build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int quantity) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("sku_desc", skuCode + " desc");
        line.put("quantity", quantity);
        line.put("scanning_status", "BARCODE");
        line.put("conveyor_mode", "CONVEYOR");
        return line;
    }

    private void stubOpenUnloadingBayCount(int count) {
        when(jdbc.queryForObject(contains("unloading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }

    private void stubParentId(Long id) {
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(id);
    }
}
