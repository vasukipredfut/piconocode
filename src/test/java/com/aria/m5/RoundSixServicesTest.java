package com.aria.m5;

import com.aria.m11.adapter.ConveyorMqttAdapter;
import com.aria.m11.adapter.OutboundResult;
import com.aria.m11.adapter.SapGrnPullAdapter;
import com.aria.m11.model.IntegrationInstance;
import com.aria.m5.quality.QualityAlgorithmService;
import com.aria.m5.stock.FefoStockPickerService;
import com.aria.m5.shift.ShiftRotationScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Round-6 backend services + adapters:
 *   - SapGrnPullAdapter (dryRun pull/ack)
 *   - ConveyorMqttAdapter (dryRun start/stop/setSpeed/divert)
 *   - FefoStockPickerService (oldest-lot-first picking + residual)
 *   - QualityAlgorithmService (moving-avg tolerance)
 *   - ShiftRotationScheduler (handover row insertion)
 */
class RoundSixServicesTest {

    // ---- SAP adapter ---------------------------------------------------

    @Test
    void sap_pullGrn_dryRun_returns_synth_payload() {
        SapGrnPullAdapter sap = new SapGrnPullAdapter();
        OutboundResult r = sap.invoke(inst("{\"dryRun\":true}"), "pullGrn",
                Map.of("sapDocNo", "5100001234", "sapPlant", "PICO-CBE"), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("suggestedGrnNumber")
                .contains("OIL-SUN-RAW")
                .contains("lines");
    }

    @Test
    void sap_pushAck_dryRun_ok() {
        SapGrnPullAdapter sap = new SapGrnPullAdapter();
        OutboundResult r = sap.invoke(inst("{\"dryRun\":true}"), "pushAck",
                Map.of("sapDocNo", "5100001234", "grnNumber", "GRN-1"), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("acknowledged");
    }

    @Test
    void sap_pullGrn_missingDocNo_isBadRequest() {
        OutboundResult r = new SapGrnPullAdapter().invoke(inst("{\"dryRun\":true}"),
                "pullGrn", Map.of(), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
    }

    // ---- Conveyor adapter ----------------------------------------------

    @Test
    void conveyor_start_dryRun_emits_topic() {
        OutboundResult r = new ConveyorMqttAdapter().invoke(inst("{\"dryRun\":true,\"topicBase\":\"factory/conveyor\"}"),
                "start", Map.of("lineId", "PACK-1"), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("factory/conveyor/PACK-1/cmd").contains("START");
    }

    @Test
    void conveyor_setSpeed_dryRun_emits_args() {
        OutboundResult r = new ConveyorMqttAdapter().invoke(inst("{\"dryRun\":true}"),
                "setSpeed", Map.of("lineId", "PACK-1", "speedRpm", 240), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("SET_SPEED").contains("240");
    }

    @Test
    void conveyor_divert_dryRun_emits_destination() {
        OutboundResult r = new ConveyorMqttAdapter().invoke(inst("{\"dryRun\":true}"),
                "divert", Map.of("lineId", "PACK-1", "divertTo", "SPOIL-BIN"), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("DIVERT").contains("SPOIL-BIN");
    }

    @Test
    void conveyor_missingLineId_isBadRequest() {
        OutboundResult r = new ConveyorMqttAdapter().invoke(inst("{\"dryRun\":true}"),
                "start", Map.of(), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
    }

    // ---- FEFO picker ---------------------------------------------------

    @Test
    void fefo_picks_oldest_lot_first_under_qty() {
        // Spy via subclass — override the DB hook to return fixed rows.
        var p = new FefoStockPickerService(null) {
            @Override
            protected java.util.List<java.util.Map<String, Object>> findAvailableLots(long t, String i, String b) {
                return java.util.List.of(
                        Map.of("lot_no", "LOT-OLD", "bin_code", "T-1", "uom", "KG",
                                "qty_on_hand", new BigDecimal("100.000"), "last_movement_at", "2026-06-01"),
                        Map.of("lot_no", "LOT-NEW", "bin_code", "T-1", "uom", "KG",
                                "qty_on_hand", new BigDecimal("500.000"), "last_movement_at", "2026-06-05"));
            }
        };
        var picks = p.pick(1L, "OIL-SUN-RAW", "T-1", new BigDecimal("300"));
        assertThat(picks).hasSize(2);
        assertThat(picks.get(0).lotNo()).isEqualTo("LOT-OLD");
        assertThat(picks.get(0).qty()).isEqualByComparingTo("100");
        assertThat(picks.get(1).lotNo()).isEqualTo("LOT-NEW");
        assertThat(picks.get(1).qty()).isEqualByComparingTo("200");
        assertThat(p.residualQty(new BigDecimal("300"), picks)).isEqualByComparingTo("0");
    }

    @Test
    void fefo_residual_when_under_picked() {
        var p = new FefoStockPickerService(null) {
            @Override
            protected java.util.List<java.util.Map<String, Object>> findAvailableLots(long t, String i, String b) {
                return java.util.List.of(
                        Map.of("lot_no", "LOT-X", "bin_code", "T-1", "uom", "KG",
                                "qty_on_hand", new BigDecimal("50.000"), "last_movement_at", "2026-06-01"));
            }
        };
        var picks = p.pick(1L, "OIL-SUN-RAW", "T-1", new BigDecimal("200"));
        assertThat(picks).hasSize(1);
        assertThat(p.residualQty(new BigDecimal("200"), picks)).isEqualByComparingTo("150");
    }

    // ---- Quality algorithm ---------------------------------------------

    @Test
    void quality_allInTolerance_score100() {
        QualityAlgorithmService q = new QualityAlgorithmService(null);
        var r = q.qualityScore(
                List.of(new BigDecimal("100"), new BigDecimal("101"), new BigDecimal("99"), new BigDecimal("100")),
                3, new BigDecimal("5"));
        assertThat(r.score()).isEqualByComparingTo("100.00");
        assertThat(r.flagged()).isZero();
    }

    @Test
    void quality_outliers_lowerScore() {
        QualityAlgorithmService q = new QualityAlgorithmService(null);
        var r = q.qualityScore(
                List.of(new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("100"), new BigDecimal("500")),
                3, new BigDecimal("50"));
        assertThat(r.flagged()).isGreaterThanOrEqualTo(1);
        assertThat(r.score().doubleValue()).isLessThan(100.0);
    }

    @Test
    void quality_emptySeries_returnsZeroScore() {
        QualityAlgorithmService q = new QualityAlgorithmService(null);
        var r = q.qualityScore(List.of(), 5, new BigDecimal("10"));
        assertThat(r.score()).isEqualByComparingTo("0");
        assertThat(r.total()).isZero();
    }

    // ---- Shift rotation -----------------------------------------------

    @Test
    void shiftRotation_writes_handover_row_when_boundary_crossed() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(3);
        var s = new ShiftRotationScheduler(jdbc) {
            @Override
            protected java.util.List<java.util.Map<String, Object>> findShiftsStartingNow(LocalDateTime now) {
                return java.util.List.of(Map.of("id", 2L, "shift_code", "B",
                        "start_time", "14:00:00", "supervisor_id", 2004L));
            }
            @Override
            protected java.util.List<java.util.Map<String, Object>> findPreviousShift(LocalDateTime now, String incoming) {
                return java.util.List.of(Map.of("shift_code", "A", "supervisor_id", 2003L));
            }
        };
        boolean handed = s.rotate(LocalDateTime.of(2026, 6, 8, 14, 2));
        assertThat(handed).isTrue();
        // Verify an INSERT was attempted on jdbc (varargs-friendly: capture via mockingDetails).
        boolean sawInsert = mockingDetails(jdbc).getInvocations().stream()
                .anyMatch(i -> i.getMethod().getName().equals("update")
                        && i.getArgument(0).toString().contains("INSERT INTO m5_shift_handover"));
        assertThat(sawInsert).isTrue();
    }

    @Test
    void shiftRotation_noOp_when_not_at_boundary() {
        var s = new ShiftRotationScheduler(mock(JdbcTemplate.class)) {
            @Override
            protected java.util.List<java.util.Map<String, Object>> findShiftsStartingNow(LocalDateTime now) {
                return java.util.List.of();
            }
        };
        boolean handed = s.rotate(LocalDateTime.of(2026, 6, 8, 10, 30));
        assertThat(handed).isFalse();
    }

    // ---- helpers --------------------------------------------------------
    private IntegrationInstance inst(String configJson) {
        IntegrationInstance i = new IntegrationInstance();
        i.setTenantId(1L);
        i.setConfigJson(configJson);
        return i;
    }
}
