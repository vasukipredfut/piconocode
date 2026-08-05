package com.aria.m10.loading;

import com.aria.datasource.DataSourceSpec;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link PendingSkuController}. Mock the JdbcTemplate;
 * stub per-position typed matchers rather than a single vararg-array
 * matcher — see ParkingBayScanServiceTest's note on why the array-any()
 * trick doesn't reliably match queryForList in this repo's Mockito setup.
 *
 * <p>{@code pendingSkus} returns the shared {@link DataSourceSpec.Response}
 * page envelope ({@code content}/{@code totalElements}/{@code totalPages}/
 * {@code number}/{@code size}), with {@code content} items shaped
 * {@code {"sku_code": ..., "order_qty": ..., "processed_qty": ...}}.
 * {@code skuStatus} returns a bare boolean for the whole vehicle.
 */
class PendingSkuControllerTest {

    private JdbcTemplate jdbc;
    private PendingSkuController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        controller = new PendingSkuController(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void pendingSkus_returnsPageEnvelopeWithSkuCodeOrderQtyAndProcessedQty() {
        Map<String, Object> row1 = new LinkedHashMap<>();
        row1.put("sku_code", "SKU1024");
        row1.put("order_qty", new BigDecimal("50.000"));
        row1.put("processed_qty", new BigDecimal("30.000"));
        Map<String, Object> row2 = new LinkedHashMap<>();
        row2.put("sku_code", "SKU1220");
        row2.put("order_qty", new BigDecimal("20.000"));
        row2.put("processed_qty", new BigDecimal("10.000"));
        when(jdbc.queryForList(contains("loading_progress_detail_bay"), any(Long.class), any(String.class)))
                .thenReturn(List.of(row1, row2));

        DataSourceSpec.Response result = controller.pendingSkus("TN01AB1234");

        assertThat(result.getContent()).containsExactly(
                Map.of("sku_code", "SKU1024", "order_qty", new BigDecimal("50.000"), "processed_qty", new BigDecimal("30.000")),
                Map.of("sku_code", "SKU1220", "order_qty", new BigDecimal("20.000"), "processed_qty", new BigDecimal("10.000")));
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(500);

        verify(jdbc).queryForList(contains("HAVING SUM(COALESCE(b.processed_quantity, 0)) < SUM(b.quantity)"),
                eq(1L), eq("TN01AB1234"));
    }

    @Test
    void noVehicleFoundOrNothingPending_returnsEmptyPage() {
        when(jdbc.queryForList(contains("loading_progress_detail_bay"), any(Long.class), any(String.class)))
                .thenReturn(List.of());

        DataSourceSpec.Response result = controller.pendingSkus("TN99ZZ9999");

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    void scopesToOpenWorkflowInstanceOnly() {
        when(jdbc.queryForList(any(String.class), any(Long.class), any(String.class))).thenReturn(List.of());

        controller.pendingSkus("TN01AB1234");

        verify(jdbc).queryForList(contains("wi.status = 'OPEN'"), eq(1L), eq("TN01AB1234"));
    }

    @Test
    void skuStatus_falseWhenAnySkuIncomplete() {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Long.class), any(String.class)))
                .thenReturn(1);

        boolean result = controller.skuStatus("TN01AB1234");

        assertThat(result).isFalse();
    }

    @Test
    void skuStatus_trueWhenEverySkuMatches() {
        when(jdbc.queryForObject(contains("loading_progress_detail_bay"), eq(Integer.class), any(Long.class), any(String.class)))
                .thenReturn(0);

        boolean result = controller.skuStatus("TN01AB1234");

        assertThat(result).isTrue();
    }

    @Test
    void skuStatus_scopesToOpenWorkflowInstanceOnly() {
        when(jdbc.queryForObject(any(String.class), eq(Integer.class), any(Long.class), any(String.class)))
                .thenReturn(0);

        controller.skuStatus("TN01AB1234");

        verify(jdbc).queryForObject(contains("wi.status = 'OPEN'"), eq(Integer.class), eq(1L), eq("TN01AB1234"));
    }
}
