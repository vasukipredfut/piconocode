package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M5PackWeighActionHandler} — the checkweigher step of
 * the Packing Process, always after {@link M5PackScanActionHandler}
 * (m5.pack-scan) has already ACCEPTED the barcode.
 */
class M5PackWeighActionHandlerTest {

    private JdbcTemplate jdbc;
    private M5PackWeighActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        handler = new M5PackWeighActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isPackWeigh() {
        assertThat(handler.code()).isEqualTo("m5.pack-weigh");
    }

    @Test
    void missingScanLogIdAndBarcodeTarget_errors() {
        ActionResult r = handler.handle(invocation(Map.of("weightKg", "0.95")));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("scanLogId, or formData.packingRunId + barcode, required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingWeight_errors() {
        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("weightKg must be a positive number");
        verifyNoInteractions(jdbc);
    }

    @Test
    void negativeWeight_errors() {
        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "-1")));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("positive number");
        verifyNoInteractions(jdbc);
    }

    @Test
    void scanLogNotFound_errors() {
        when(jdbc.queryForMap(contains("FROM m5_packing_scan_log"), eq(1L), eq(231L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "0.95")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("not found");
    }

    @Test
    void scanLogWasRejectedAtBarcodeStep_errors() {
        stubScanLog(4L, "SKU1", null, "REJECTED", null);

        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "0.95")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("was not ACCEPTED");
        verify(jdbc, never()).update(contains("UPDATE m5_packing_scan_log"), any(Object[].class));
    }

    @Test
    void alreadyWeighed_errors() {
        stubScanLog(4L, "SKU1", BigDecimal.ONE, "ACCEPTED", new BigDecimal("0.95"));

        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "0.95")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already been weighed");
    }

    @Test
    void withinTolerance_staysAcceptedAndRecordsWeight() {
        stubScanLog(4L, "SKU1", BigDecimal.ONE, "ACCEPTED", null);
        stubExpectedWeight("SKU1", "0.950");

        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "0.98")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("outcome", "ACCEPTED");
        verify(jdbc).update(contains("UPDATE m5_packing_scan_log SET weight_kg = ?"),
                eq(new BigDecimal("0.98")), eq(1L), eq(231L));
        verify(jdbc, never()).update(contains("UPDATE m5_packing_run"), any(Object[].class));
    }

    @Test
    void outOfTolerance_rejectedAndReversesActualPacks() {
        stubScanLog(4L, "SKU1", new BigDecimal("2"), "ACCEPTED", null);
        stubExpectedWeight("SKU1", "0.950");

        // 0.70 vs 0.95 expected -> ~26% diff, well over 10% tolerance
        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "0.70")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("out of spec");
        assertThat(r.getData()).containsEntry("outcome", "REJECTED").containsEntry("rejectReason", "WEIGHT_OUT_OF_SPEC");
        verify(jdbc).update(contains("outcome = 'REJECTED'"),
                eq(new BigDecimal("0.70")), eq(1L), eq(231L));
        verify(jdbc).update(contains("UPDATE m5_packing_run"), eq(new BigDecimal("2")), eq(1L), eq(4L));
    }

    @Test
    void noExpectedWeightOnFile_recordsWithoutValidating() {
        stubScanLog(4L, "SKU1", BigDecimal.ONE, "ACCEPTED", null);
        when(jdbc.queryForObject(contains("FROM m5_sku_master"), eq(Object.class), eq(1L), eq("SKU1")))
                .thenReturn(null);

        ActionResult r = handler.handle(invocation(Map.of("scanLogId", 231, "weightKg", "0.98")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("outcome", "ACCEPTED");
        verify(jdbc, never()).update(contains("UPDATE m5_packing_run"), any(Object[].class));
    }

    @Test
    void resolvedByPackingRunIdAndBarcode_whenNoScanLogIdGiven() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 231L);
        row.put("packing_run_id", 4L);
        row.put("sku_code", "SKU1");
        row.put("bundle_count", BigDecimal.ONE);
        row.put("outcome", "ACCEPTED");
        row.put("weight_kg", null);
        when(jdbc.queryForMap(contains("FROM m5_packing_scan_log"), eq(1L), eq(4L), eq("890123"))).thenReturn(row);
        stubExpectedWeight("SKU1", "0.950");

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123", "weightKg", "0.96")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("scanLogId", 231L).containsEntry("outcome", "ACCEPTED");
        verify(jdbc).update(contains("UPDATE m5_packing_scan_log SET weight_kg = ?"),
                eq(new BigDecimal("0.96")), eq(1L), eq(231L));
    }

    @Test
    void noUnweighedScanForBarcode_errors() {
        when(jdbc.queryForMap(contains("FROM m5_packing_scan_log"), eq(1L), eq(4L), eq("999999")))
                .thenThrow(new EmptyResultDataAccessException(1));

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "999999", "weightKg", "0.96")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("no unweighed ACCEPTED scan found");
    }

    private ActionInvocation invocation(Map<String, Object> formData) {
        return ActionInvocation.builder().formData(new HashMap<>(formData)).build();
    }

    private void stubScanLog(Long packingRunId, String skuCode, BigDecimal bundleCount, String outcome, BigDecimal weightKg) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 231L);
        row.put("packing_run_id", packingRunId);
        row.put("sku_code", skuCode);
        row.put("bundle_count", bundleCount);
        row.put("outcome", outcome);
        row.put("weight_kg", weightKg);
        when(jdbc.queryForMap(contains("FROM m5_packing_scan_log"), eq(1L), eq(231L))).thenReturn(row);
    }

    private void stubExpectedWeight(String skuCode, String cartonWeightKg) {
        when(jdbc.queryForObject(contains("FROM m5_sku_master"), eq(Object.class), eq(1L), eq(skuCode)))
                .thenReturn(cartonWeightKg);
    }
}
