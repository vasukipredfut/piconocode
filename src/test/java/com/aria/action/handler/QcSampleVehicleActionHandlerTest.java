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

/** Unit test for {@link QcSampleVehicleActionHandler}: the sample vehicle of the QC oil sample (vehicle no, W1, W2, stock). */
class QcSampleVehicleActionHandlerTest {

    private JdbcTemplate jdbc;
    private QcSampleVehicleActionHandler handler;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Object[].class))).thenReturn(List.of(Map.of("gp_number", "GP-1-54")));
        when(jdbc.queryForList(contains("FROM qc_sample_vehicle"), any(Object[].class))).thenReturn(List.of());
        handler = new QcSampleVehicleActionHandler();
        ReflectionTestUtils.setField(handler, "jdbc", jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private static Map<String, Object> line(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private static ActionInvocation inv(Map<String, Object>... lines) {
        return ActionInvocation.builder().instanceId(1559L).formData(Map.of("sampleVehicles", List.of(lines))).build();
    }

    private static Map<String, Object> vehicle(Object... extra) {
        Map<String, Object> m = line("vehicle_no", "TN23J1144", "invoice_no", "INV1", "sku_code", "SKU1", "sample_location", "SAMPLE-1");
        for (int i = 0; i < extra.length; i += 2) m.put((String) extra[i], extra[i + 1]);
        return m;
    }

    private void existing(String status, String w1, String w2) {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("id", 9L);
        row.put("status", status);
        row.put("sample_qty", new BigDecimal("5"));
        row.put("w1_weight_kg", w1 == null ? null : new BigDecimal(w1));
        row.put("w2_weight_kg", w2 == null ? null : new BigDecimal(w2));
        when(jdbc.queryForList(contains("FROM qc_sample_vehicle"), any(Object[].class))).thenReturn(List.of(row));
    }

    @Test
    void code() {
        assertThat(handler.code()).isEqualTo("m10.qc-sample-vehicle");
    }

    @Test
    void missingInstanceId_errors() {
        assertThat(handler.handle(ActionInvocation.builder().build()).isOk()).isFalse();
        verifyNoInteractions(jdbc);
    }

    @Test
    void missingVehicles_errors() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1559L).formData(Map.of()).build());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("sampleVehicles");
    }

    @Test
    void enteredOnly_savesOpenRow_andTouchesNoStock() {
        ActionResult r = handler.handle(inv(vehicle("sample_qty", 5)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO qc_sample_vehicle"), eq(1L), eq(1559L), eq("GP-1-54"), eq("INV1"), isNull(),
                eq("SKU1"), isNull(), isNull(), isNull(), eq("TN23J1144"), eq("SAMPLE-1"), eq(new BigDecimal("5")),
                isNull(), isNull(), isNull(), eq("OPEN"), isNull(), any(), any(), any(), any(), isNull(), isNull());
        verify(jdbc, never()).update(contains("m5_stock"), any(Object[].class));
    }

    @Test
    void firstWeight_marksW1Done_noStockYet() {
        ActionResult r = handler.handle(inv(vehicle("w1_weight_kg", 8200)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO qc_sample_vehicle"), any(Object[].class));
        verify(jdbc, never()).update(contains("INSERT INTO m5_stock_movement"), any(Object[].class));
        assertThat(((List<?>) ((Map<?, ?>) r.getData()).get("saved")).get(0).toString()).contains("W1_DONE");
    }

    @Test
    void secondWeight_closesRow_andReceivesTheDifferenceIntoStockAtTheLocation() {
        existing("W1_DONE", "8200", null);

        ActionResult r = handler.handle(inv(vehicle("w2_weight_kg", 8195)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("qty_on_hand = qty_on_hand + ?"), eq(new BigDecimal("5")), eq(1L), eq("SKU1"), eq("SAMPLE-1"));
        verify(jdbc).update(contains("INSERT INTO m5_stock_movement"), eq(1L), eq("RECEIPT"), eq("SKU1"), eq("SAMPLE-1"),
                eq(new BigDecimal("5")), eq("EA"), eq("QC_SAMPLE"), eq(1559L), eq("TN23J1144"));
        verify(jdbc).update(contains("UPDATE qc_sample_vehicle SET"), eq(new BigDecimal("5")), eq(new BigDecimal("8200")),
                eq(new BigDecimal("8195")), eq(new BigDecimal("5")), eq("DONE"), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), isNull(), eq(false), isNull(), eq(1L), eq(9L));
    }

    @Test
    void bothWeightsInOneCall_receivesAbsoluteDifference() {
        ActionResult r = handler.handle(inv(vehicle("w1_weight_kg", 8195, "w2_weight_kg", 8200)));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("INSERT INTO m5_stock_movement"), eq(1L), eq("RECEIPT"), eq("SKU1"), eq("SAMPLE-1"),
                eq(new BigDecimal("5")), any(), eq("QC_SAMPLE"), any(), any());
    }

    @Test
    void destinationBinWithNoBalance_getsANewBalanceRow() {
        existing("W1_DONE", "8200", null);
        when(jdbc.update(contains("qty_on_hand = qty_on_hand + ?"), any(Object[].class))).thenReturn(0);

        handler.handle(inv(vehicle("w2_weight_kg", 8195)));

        verify(jdbc).update(contains("INSERT INTO m5_stock_balance"), eq(1L), eq("SKU1"), eq("SAMPLE-1"), eq("EA"), eq(new BigDecimal("5")));
    }

    @Test
    void secondWeightWithoutFirst_errors() {
        ActionResult r = handler.handle(inv(vehicle("w2_weight_kg", 8195)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("first weight before the second");
        verify(jdbc, never()).update(contains("m5_stock"), any(Object[].class));
    }

    @Test
    void sameWeights_errors() {
        existing("W1_DONE", "8200", null);
        ActionResult r = handler.handle(inv(vehicle("w2_weight_kg", 8200)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("same");
        verify(jdbc, never()).update(contains("INSERT INTO m5_stock_movement"), any(Object[].class));
    }

    @Test
    void doneRow_cannotBeWeighedAgain_soStockIsPostedOnce() {
        existing("DONE", "8200", "8195");
        ActionResult r = handler.handle(inv(vehicle("w2_weight_kg", 8100)));
        assertThat(r.isOk()).isFalse();
        assertThat(r.getMessage()).contains("already weighed and unloaded");
        verify(jdbc, never()).update(contains("INSERT INTO m5_stock_movement"), any(Object[].class));
    }

    @Test
    void validation_vehicleLocationSkuQtyAndWeights() {
        assertThat(handler.handle(inv(line("sku_code", "SKU1", "sample_location", "S", "invoice_no", "I"))).getMessage()).contains("vehicle_no");
        assertThat(handler.handle(inv(line("vehicle_no", "V", "sku_code", "SKU1", "invoice_no", "I"))).getMessage()).contains("sample_location");
        assertThat(handler.handle(inv(line("vehicle_no", "V", "sample_location", "S", "invoice_no", "I"))).getMessage()).contains("sku_code");
        assertThat(handler.handle(inv(vehicle("sample_qty", 0))).getMessage()).contains("sample_qty");
        assertThat(handler.handle(inv(vehicle("w1_weight_kg", -5))).getMessage()).contains("w1_weight_kg");
        assertThat(handler.handle(inv(vehicle("w1_weight_kg", "abc"))).getMessage()).contains("must be numbers");
    }

    @Test
    void singleFlatVehicle_isAccepted() {
        ActionResult r = handler.handle(ActionInvocation.builder().instanceId(1559L).formData(
                Map.of("vehicle_no", "TN1", "sku_code", "SKU1", "sample_location", "S1", "w1_weight_kg", 100)).build());
        assertThat(r.isOk()).isTrue();
    }

    // ---- the QC's Approved / Rebate decision on the sample, and the rebate value ----

    @Test
    void rebateDecision_isSavedWithItsValue() {
        ActionResult r = handler.handle(inv(vehicle("sample_qty", 5, "sample_decision", "REBATE", "rebate_value", "12.5")));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("sample_decision, rebate_value) VALUES"), any(Object[].class));
        verify(jdbc).update(contains("INSERT INTO qc_sample_vehicle"), eq(1L), eq(1559L), eq("GP-1-54"), eq("INV1"), isNull(),
                eq("SKU1"), isNull(), isNull(), isNull(), eq("TN23J1144"), eq("SAMPLE-1"), eq(new BigDecimal("5")),
                isNull(), isNull(), isNull(), eq("OPEN"), isNull(), any(), any(), any(), any(), eq("REBATE"), eq("12.5"));
    }

    @Test
    void approvedDecision_hasNoRebateValue_evenIfOneIsSent() {
        handler.handle(inv(vehicle("sample_qty", 5, "approved_status", "approved", "rebate_reason", "9")));

        verify(jdbc).update(contains("INSERT INTO qc_sample_vehicle"), eq(1L), eq(1559L), eq("GP-1-54"), eq("INV1"), isNull(),
                eq("SKU1"), isNull(), isNull(), isNull(), eq("TN23J1144"), eq("SAMPLE-1"), eq(new BigDecimal("5")),
                isNull(), isNull(), isNull(), eq("OPEN"), isNull(), any(), any(), any(), any(), eq("APPROVED"), isNull());
    }

    @Test
    void rebateWithoutAValue_orAnUnknownDecision_errors() {
        ActionResult noValue = handler.handle(inv(vehicle("sample_decision", "REBATE")));
        assertThat(noValue.isOk()).isFalse();
        assertThat(noValue.getMessage()).contains("needs its rebate_value");

        ActionResult bad = handler.handle(inv(vehicle("sample_decision", "MAYBE")));
        assertThat(bad.isOk()).isFalse();
        assertThat(bad.getMessage()).contains("APPROVED or REBATE");
    }

    @Test
    void decisionOnly_canBeSavedOnADoneRow_withoutWeighingOrStock() {
        existing("DONE", "8200", "8195");

        ActionResult r = handler.handle(inv(vehicle("sample_decision", "REBATE", "rebate_value", "7")));

        assertThat(r.isOk()).isTrue();
        verify(jdbc).update(contains("UPDATE qc_sample_vehicle SET sample_decision = ?"), eq("REBATE"), eq(false), eq("7"), eq(1L), eq(9L));
        verify(jdbc, never()).update(contains("m5_stock"), any(Object[].class));
    }
}
