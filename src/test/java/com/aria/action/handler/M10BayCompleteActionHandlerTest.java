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
 * Unit test for {@link M10BayCompleteActionHandler}: closes every open
 * loading_progress_detail_bay row named in formData.order_sku_line_items,
 * matched by (route_id, sku_code), and rolls the total into
 * loading_progress_detail. Never touches workflow state.
 */
class M10BayCompleteActionHandlerTest {

    private JdbcTemplate jdbc;
    private M10BayCompleteActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        handler = new M10BayCompleteActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isM10BayComplete() {
        assertThat(handler.code()).isEqualTo("m10.bay-complete");
    }

    @Test
    void missingInstanceId_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("instanceId required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingLineItemsArray_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1495L).formData(Map.of()).build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("order_sku_line_items must be a non-empty array");
        verifyNoInteractions(jdbc);
    }

    @Test
    void emptyLineItemsArray_errors() {
        ActionResult r = handler.handle(invocationWithLines(List.of()));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("non-empty array");
    }

    @Test
    void lineItemMissingSkuCode_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("loading_qty", 45);
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("missing route_id or sku_code");
    }

    @Test
    void lineItemMissingLoadingQty_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("needs loading_qty >= 0");
    }

    @Test
    void lineItemNonNumericLoadingQty_errors() {
        Map<String, Object> badLine = new LinkedHashMap<>();
        badLine.put("route_id", "ROU028912");
        badLine.put("sku_code", "SKU1215");
        badLine.put("loading_qty", "oops");
        ActionResult r = handler.handle(invocationWithLines(List.of(badLine)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("loading_qty must be a number");
    }

    @Test
    void noOpenBayRowMatches_errors() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no open bay row found for route_id=ROU028912 sku_code=SKU1215");
        // Never reaches the parent rollup when a line fails to close.
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail SET"), any(Object[].class));
    }

    @Test
    void singleLine_closesRowAndRollsUpParent() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(line("ROU028912", "SKU1215", 45))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getMessage()).contains("1 bay line(s) closed, processed 45 total");
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("45"));

        verify(jdbc).update(
                eq("UPDATE loading_progress_detail_bay SET processed_quantity = ?, status = 'DONE' "
                        + "WHERE tenant_id = ? AND workflow_instance_id = ? AND route_id = ? AND sku_code = ? AND status = 'IN_PROGRESS'"),
                eq(new BigDecimal("45")), eq(1L), eq(1495L), eq("ROU028912"), eq("SKU1215"));
        verify(jdbc).update(startsWith("UPDATE loading_progress_detail SET processed_quantity"),
                eq(new BigDecimal("45")), eq(1L), eq(1495L));
    }

    @Test
    void multipleLines_sumsTotalProcessed() {
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class))).thenReturn(1);

        ActionResult r = handler.handle(invocationWithLines(List.of(
                line("ROU028912", "SKU1215", 45),
                line("ROU028912", "SKU1461", 250))));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("totalProcessed", new BigDecimal("295"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> closed = (List<Map<String, Object>>) r.getData().get("closed");
        assertThat(closed).hasSize(2);

        verify(jdbc, times(2)).update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc).update(startsWith("UPDATE loading_progress_detail SET processed_quantity"),
                eq(new BigDecimal("295")), eq(1L), eq(1495L));
    }

    @Test
    void secondLineFails_stopsBeforeParentRollup_firstLineAlreadyCommitted() {
        // First line closes fine, second finds no open row.
        when(jdbc.update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(1)
                .thenReturn(0);

        ActionResult r = handler.handle(invocationWithLines(List.of(
                line("ROU028912", "SKU1215", 45),
                line("ROU028912", "SKU9999", 999))));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("SKU9999");
        verify(jdbc, times(2)).update(startsWith("UPDATE loading_progress_detail_bay"), any(Object[].class));
        verify(jdbc, never()).update(startsWith("UPDATE loading_progress_detail SET"), any(Object[].class));
    }

    private ActionInvocation invocationWithLines(List<Map<String, Object>> lines) {
        return ActionInvocation.builder()
                .instanceId(1495L)
                .formData(Map.of("order_sku_line_items", lines))
                .build();
    }

    private Map<String, Object> line(String routeId, String skuCode, int loadingQty) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("route_id", routeId);
        line.put("sku_code", skuCode);
        line.put("loading_qty", loadingQty);
        return line;
    }
}
