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
 * Unit test for {@link M10StartUnloadBayActionHandler}: the Reject Flow
 * Unloading page's Start Bay button. Unlike {@link M10StartBayActionHandler}
 * this never touches the workflow engine — it only opens
 * unloading_progress_detail_bay rows and flags the matching, already-loaded
 * loading_progress_detail_bay row UNLOAD_IN_PROGRESS.
 */
class M10StartUnloadBayActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10StartUnloadBayActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForMap(contains("FROM workflow_instance"), any(Object[].class)))
                .thenReturn(Map.of("workflow_code", "M10.FGLoading", "current_state", "RELOAD_REQUIRED"));
        handler = new M10StartUnloadBayActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10StartUnloadBay() {
        assertThat(handler.code()).isEqualTo("m10.start-unload-bay");
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
        ActionResult r = handler.handle(baseInvocation(Map.of("order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("formData.bay_name is required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void bayNameOutOfRange_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay13", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("must be one of Bay1..Bay12").contains("Bay13");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLineItemsArray_errors() {
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1")));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_sku_line_items must be a non-empty array");
    }

    @Test
    void lineItemMissingSkuCode_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("unloading_qty", 20);
        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing route_id or sku_code");
    }

    @Test
    void lineItemZeroQty_isAllowed() {
        stubLoadedDoneCount(1);
        stubOpenUnloadBayCount(0);
        stubParentId(777L);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 0)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalQuantity", new BigDecimal("0"));
    }

    @Test
    void lineItemNegativeQty_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        badLine.put("unloading_qty", -5);

        ActionResult r = handler.handle(baseInvocation(Map.of("bay_name", "Bay1", "order_sku_line_items", List.of(badLine))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU1215").contains("unloading_qty >= 0");
    }

    @Test
    void noLoadedRowToUnloadAgainst_errors() {
        stubLoadedDoneCount(0);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no loaded record to unload against");
        verify(jdbc, never()).update(contains("INSERT INTO unloading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void alreadyOpenUnloadBayForInstance_refusesToStartAnother() {
        stubLoadedDoneCount(1);
        stubOpenUnloadBayCount(1);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "bay_name", "Bay1", "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already has an open unload bay");
    }

    @Test
    void validLine_opensUnloadBayRowAndFlagsLoadedRow() {
        stubLoadedDoneCount(1);
        stubOpenUnloadBayCount(0);
        stubParentId(777L);

        ActionResult r = handler.handle(baseInvocation(Map.of(
                "vehicle_no", "TN86J8070", "gpNumber", "GP-1-1495", "bay_name", "Bay1",
                "order_sku_line_items", List.of(line("ROU028912", "SKU1215", 20)))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("bayName", "Bay1");
        assertThat(r.getData()).containsEntry("totalQuantity", new BigDecimal("20"));

        verify(jdbc).update(contains("INSERT INTO unloading_progress_detail_bay"),
                eq(1L), eq(777L), eq(1495L), eq("ROU028912"), isNull(), eq("SKU1215"), eq("SKU1215 desc"), eq("Bay1"), eq(new BigDecimal("20")));
        verify(jdbc).update(
                eq("UPDATE loading_progress_detail_bay SET status = 'UNLOAD_IN_PROGRESS' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND "
                        + "((order_id IS NULL AND ? IS NULL) OR order_id = ?) AND sku_code = ? AND status = 'DONE'"),
                eq(1L), eq(1495L), eq("ROU028912"), isNull(), isNull(), eq("SKU1215"));
    }

    private ActionInvocation baseInvocation(Map<String, Object> extraFormData) {
        Map<String, Object> formData = new LinkedHashMap<>(extraFormData);
        return ActionInvocation.builder().instanceId(1495L).formData(formData).build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int unloadingQty) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("sku_desc", skuCode + " desc");
        line.put("unloading_qty", unloadingQty);
        return line;
    }

    private void stubLoadedDoneCount(int count) {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }

    private void stubOpenUnloadBayCount(int count) {
        when(jdbc.queryForObject(contains("unloading_progress_detail_bay"), eq(Integer.class), any(Object[].class)))
                .thenReturn(count);
    }

    private void stubParentId(Long id) {
        when(jdbc.queryForObject(contains("SELECT id FROM unloading_progress_detail"), eq(Long.class), any(Object[].class)))
                .thenReturn(id);
    }
}
