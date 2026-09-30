package com.aria.integration.conveyor;

import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link ConveyorAgentController#boxEvent}, focused on the
 * multi-offer disambiguation fix: a barcode matching more than one active
 * {@code m5_sku_master} row (different {@code offer_variant}s) used to
 * silently take the first row returned — no operator exists on this fully
 * automated PLC/conveyor line to ask mid-stream, unlike the manual scan path
 * ({@link com.aria.action.handler.M5PackScanActionHandler}). It now resolves
 * via the line's pre-set {@code m11_agent_acceptance_scope.expected_offer},
 * and rejects (rather than guesses) when that isn't configured.
 */
class ConveyorAgentControllerTest {

    private JdbcTemplate jdbc;
    private ConveyorAgentController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        controller = new ConveyorAgentController(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void noBarcode_rejectsWithoutQuerying() {
        ResponseEntity<Map<String, Object>> r = controller.boxEvent(Map.of("lineId", "LINE-1"));

        assertThat(r.getBody()).containsEntry("action", "REJECT_DIVERT").containsEntry("reason", "no barcode");
        verifyNoInteractions(jdbc);
    }

    @Test
    void unknownBarcode_rejects() {
        stubMatches(List.of());

        ResponseEntity<Map<String, Object>> r = controller.boxEvent(baseBody());

        assertThat(r.getBody()).containsEntry("action", "REJECT_DIVERT").containsEntry("reason", "UNAUTHORIZED_BARCODE");
    }

    @Test
    void singleMatch_acceptsWithoutNeedingAcceptanceScope() {
        stubMatches(List.of(skuRow("SKU1", "STANDARD", "0.950")));

        ResponseEntity<Map<String, Object>> r = controller.boxEvent(baseBody());

        assertThat(r.getBody()).containsEntry("action", "ACCEPT").containsEntry("matchedOffer", "STANDARD");
        verify(jdbc, never()).queryForObject(contains("m11_agent_acceptance_scope"), eq(String.class), any(Object[].class));
    }

    @Test
    void multipleMatches_noAcceptanceScopeConfigured_rejectsAsAmbiguous() {
        stubMatches(List.of(skuRow("SKU1", "STANDARD", "0.950"), skuRow("SKU1", "FESTIVE_PACK", "0.950")));
        when(jdbc.queryForObject(contains("m11_agent_acceptance_scope"), eq(String.class), any(Object[].class)))
                .thenReturn(null);

        ResponseEntity<Map<String, Object>> r = controller.boxEvent(baseBody());

        assertThat(r.getBody()).containsEntry("action", "REJECT_DIVERT").containsEntry("reason", "AMBIGUOUS_OFFER_NOT_CONFIGURED");
        assertThat(r.getBody()).doesNotContainKey("matchedSku");
    }

    @Test
    void multipleMatches_expectedOfferConfigured_resolvesAndAccepts() {
        stubMatches(List.of(skuRow("SKU1", "STANDARD", "0.950"), skuRow("SKU1", "FESTIVE_PACK", "0.950")));
        when(jdbc.queryForObject(contains("m11_agent_acceptance_scope"), eq(String.class), any(Object[].class)))
                .thenReturn("FESTIVE_PACK");

        ResponseEntity<Map<String, Object>> r = controller.boxEvent(baseBody());

        assertThat(r.getBody()).containsEntry("action", "ACCEPT").containsEntry("matchedOffer", "FESTIVE_PACK").containsEntry("matchedSku", "SKU1");
    }

    @Test
    void multipleMatches_expectedOfferDoesNotMatchAnyCandidate_rejectsAsAmbiguous() {
        stubMatches(List.of(skuRow("SKU1", "STANDARD", "0.950"), skuRow("SKU1", "FESTIVE_PACK", "0.950")));
        when(jdbc.queryForObject(contains("m11_agent_acceptance_scope"), eq(String.class), any(Object[].class)))
                .thenReturn("COMBO_2L_500ML");

        ResponseEntity<Map<String, Object>> r = controller.boxEvent(baseBody());

        assertThat(r.getBody()).containsEntry("action", "REJECT_DIVERT").containsEntry("reason", "AMBIGUOUS_OFFER_NOT_CONFIGURED");
    }

    @Test
    void multipleMatches_blockedSkuAfterResolution_rejectsAsBlocked() {
        Map<String, Object> blocked = skuRow("SKU1", "FESTIVE_PACK", "0.950");
        blocked.put("status", "DISCONTINUED");
        stubMatches(List.of(skuRow("SKU1", "STANDARD", "0.950"), blocked));
        when(jdbc.queryForObject(contains("m11_agent_acceptance_scope"), eq(String.class), any(Object[].class)))
                .thenReturn("FESTIVE_PACK");

        ResponseEntity<Map<String, Object>> r = controller.boxEvent(baseBody());

        assertThat(r.getBody()).containsEntry("action", "REJECT_DIVERT").containsEntry("reason", "SKU_BLOCKED_OR_DISCONTINUED");
    }

    private Map<String, Object> baseBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("lineId", "LINE-1");
        body.put("barcode", "8901234567890");
        body.put("weightKg", 0.95);
        body.put("bayCode", "BAY-A");
        return body;
    }

    private void stubMatches(List<Map<String, Object>> rows) {
        when(jdbc.queryForList(contains("FROM m5_sku_master"), eq(1L), anyString(), anyString())).thenReturn(rows);
    }

    private Map<String, Object> skuRow(String skuCode, String offerVariant, String cartonWeightKg) {
        Map<String, Object> row = new HashMap<>();
        row.put("sku_code", skuCode);
        row.put("offer_variant", offerVariant);
        row.put("carton_weight_kg", new BigDecimal(cartonWeightKg));
        row.put("status", "ACTIVE");
        return row;
    }
}
