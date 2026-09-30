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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link M5PackScanActionHandler} — the manual (handheld
 * scanner, web page) barcode-scan validation for a Packing Run, sibling to
 * {@link com.aria.integration.conveyor.ConveyorAgentController#boxEvent}
 * which does the same job for the automated PLC/conveyor line.
 */
class M5PackScanActionHandlerTest {

    private JdbcTemplate jdbc;
    private M5PackScanActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(eq("SELECT LAST_INSERT_ID()"), eq(Long.class))).thenReturn(231L);
        handler = new M5PackScanActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void code_isPackScan() {
        assertThat(handler.code()).isEqualTo("m5.pack-scan");
    }

    @Test
    void missingPackingRunId_errors() {
        ActionResult r = handler.handle(invocation(Map.of("barcode", "890123")));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("packingRunId required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingBarcode_errors() {
        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("barcode required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void packingRunNotFound_errors() {
        when(jdbc.queryForMap(contains("FROM m5_packing_run"), eq(1L), eq(4L)))
                .thenThrow(new EmptyResultDataAccessException(1));

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("not found");
    }

    @Test
    void packingRunNotRunning_errors() {
        stubRun("OPEN", null);

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("not RUNNING").contains("Start Bay");
        verify(jdbc, never()).update(contains("INSERT INTO m5_packing_scan_log"), any(Object[].class));
    }

    @Test
    void unknownBarcode_rejectedAndLogged() {
        stubRun("RUNNING", null);
        stubSkuMatches(List.of());

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "999999")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("barcode not recognised");
        verify(jdbc).update(contains("INSERT INTO m5_packing_scan_log"),
                eq(1L), eq(4L), isNull(), eq("999999"), isNull(), isNull(), isNull(), eq("REJECTED"), eq("UNAUTHORIZED_BARCODE"), isNull());
        verify(jdbc, never()).update(contains("UPDATE m5_packing_run"), any(Object[].class));
    }

    @Test
    void singleActiveMatch_acceptedAndBumpsActualPacks() {
        stubRun("RUNNING", null);
        stubSkuMatches(List.of(skuRow("SKU1", "Sunflower Oil 1L", "STANDARD", "0.950")));

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123", "bay_code", "BAY-A")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("outcome", "ACCEPTED").containsEntry("skuCode", "SKU1")
                .containsEntry("scanLogId", 231L);
        verify(jdbc).update(contains("INSERT INTO m5_packing_scan_log"),
                eq(1L), eq(4L), eq("BAY-A"), eq("890123"), eq("SKU1"), eq("STANDARD"), eq(BigDecimal.ONE), eq("ACCEPTED"), isNull(), isNull());
        verify(jdbc).update(contains("UPDATE m5_packing_run"), eq(BigDecimal.ONE), eq(1L), eq(4L));
    }

    @Test
    void multipleOffersNoSelection_returnsNeedsOfferSelection() {
        stubRun("RUNNING", null);
        stubSkuMatches(List.of(
                skuRow("SKU1", "Sunflower Oil 1L", "STANDARD", "0.950"),
                skuRow("SKU1", "Sunflower Oil 1L", "FESTIVE_PACK", "0.950")));

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("needsOfferSelection", true);
        assertThat((List<?>) r.getData().get("offers")).hasSize(2);
        verify(jdbc, never()).update(contains("INSERT INTO m5_packing_scan_log"), any(Object[].class));
    }

    @Test
    void multipleOffers_withSelection_acceptsChosenOffer() {
        stubRun("RUNNING", null);
        stubSkuMatches(List.of(
                skuRow("SKU1", "Sunflower Oil 1L", "STANDARD", "0.950"),
                skuRow("SKU1", "Sunflower Oil 1L", "FESTIVE_PACK", "0.950")));

        ActionResult r = handler.handle(invocation(Map.of(
                "packingRunId", 4, "barcode", "890123", "offer_variant", "FESTIVE_PACK")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("offerVariant", "FESTIVE_PACK");
        verify(jdbc).update(contains("INSERT INTO m5_packing_scan_log"),
                eq(1L), eq(4L), isNull(), eq("890123"), eq("SKU1"), eq("FESTIVE_PACK"), eq(BigDecimal.ONE), eq("ACCEPTED"), isNull(), isNull());
    }

    @Test
    void invalidOfferVariant_rejectedAsWrongSku() {
        stubRun("RUNNING", null);
        stubSkuMatches(List.of(
                skuRow("SKU1", "Sunflower Oil 1L", "STANDARD", "0.950"),
                skuRow("SKU1", "Sunflower Oil 1L", "FESTIVE_PACK", "0.950")));

        ActionResult r = handler.handle(invocation(Map.of(
                "packingRunId", 4, "barcode", "890123", "offer_variant", "NOT_REAL")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("not valid for barcode");
        verify(jdbc).update(contains("INSERT INTO m5_packing_scan_log"),
                eq(1L), eq(4L), isNull(), eq("890123"), isNull(), eq("NOT_REAL"), isNull(), eq("REJECTED"), eq("WRONG_SKU"), isNull());
    }

    @Test
    void plannedQuantityReached_rejectedAsQtyExceeded() {
        stubRun("RUNNING", 5);
        stubSkuMatches(List.of(skuRow("SKU1", "Sunflower Oil 1L", "STANDARD", "0.950")));
        when(jdbc.queryForObject(contains("m5_packing_scan_log"), eq(Integer.class), any(Object[].class)))
                .thenReturn(5);

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123")));

        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("planned quantity (5) already reached");
        verify(jdbc).update(contains("INSERT INTO m5_packing_scan_log"),
                eq(1L), eq(4L), isNull(), eq("890123"), eq("SKU1"), eq("STANDARD"), isNull(), eq("REJECTED"), eq("QTY_EXCEEDED"), isNull());
        verify(jdbc, never()).update(contains("UPDATE m5_packing_run"), any(Object[].class));
    }

    @Test
    void plannedQuantityNotYetReached_stillAccepted() {
        stubRun("RUNNING", 5);
        stubSkuMatches(List.of(skuRow("SKU1", "Sunflower Oil 1L", "STANDARD", "0.950")));
        when(jdbc.queryForObject(contains("m5_packing_scan_log"), eq(Integer.class), any(Object[].class)))
                .thenReturn(3);

        ActionResult r = handler.handle(invocation(Map.of("packingRunId", 4, "barcode", "890123")));

        assertThat(r.isOk()).isTrue();
        assertThat(r.getData()).containsEntry("outcome", "ACCEPTED");
    }

    private ActionInvocation invocation(Map<String, Object> formData) {
        return ActionInvocation.builder().formData(new HashMap<>(formData)).build();
    }

    private void stubRun(String status, Integer plannedTotalPieces) {
        Map<String, Object> row = new HashMap<>();
        row.put("status", status);
        row.put("planned_total_pieces", plannedTotalPieces);
        when(jdbc.queryForMap(contains("FROM m5_packing_run"), eq(1L), eq(4L))).thenReturn(row);
    }

    private void stubSkuMatches(List<Map<String, Object>> rows) {
        when(jdbc.queryForList(contains("FROM m5_sku_master"), eq(1L), anyString())).thenReturn(rows);
    }

    private Map<String, Object> skuRow(String skuCode, String skuName, String offerVariant, String cartonWeightKg) {
        Map<String, Object> row = new HashMap<>();
        row.put("sku_code", skuCode);
        row.put("sku_name", skuName);
        row.put("offer_variant", offerVariant);
        row.put("carton_weight_kg", cartonWeightKg);
        return row;
    }
}
