package com.aria.m10.summary;

import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Unit test for {@link GatePassSummaryController}: one read-only response with every part of a gate pass. */
class GatePassSummaryControllerTest {

    private JdbcTemplate jdbc;
    private GatePassSummaryController controller;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        controller = new GatePassSummaryController(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private void gatePass(Long instanceId) {
        Map<String, Object> gp = row("id", 5L, "gp_number", "GP-1-54", "vehicle_no", "TN1");
        gp.put("workflow_instance_id", instanceId);
        when(jdbc.queryForList(contains("FROM gate_pass WHERE"), any(Object[].class))).thenReturn(List.of(gp));
    }

    @SuppressWarnings("unchecked")
    @Test
    void unknownGatePass_is404() {
        when(jdbc.queryForList(contains("FROM gate_pass WHERE"), any(Object[].class))).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> r = controller.summary("GP-X");

        assertThat(r.getStatusCode().value()).isEqualTo(404);
        assertThat(r.getBody()).containsEntry("error", "not_found");
    }

    @SuppressWarnings("unchecked")
    @Test
    void everySectionIsReadForTheInstance_andTheHeaderComesFirst() {
        gatePass(2040L);
        when(jdbc.queryForList(contains("FROM workflow_instance"), any(Object[].class)))
                .thenReturn(List.of(row("id", 2040L, "workflow_code", "M10.TankerOutbound", "current_state", "WEIGHMENT_2")));
        when(jdbc.queryForList(contains("FROM workflow_event"), any(Object[].class)))
                .thenReturn(List.of(row("kind", "TRANSITION", "to_state", "DISPATCH_VEHICLE_ENTRY")));
        when(jdbc.queryForList(contains("FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row("bay_name", "COT-1", "processed_quantity", 3000, "unloaded_quantity", 1000)));
        when(jdbc.queryForList(contains("FROM m5_stock_movement"), any(Object[].class)))
                .thenReturn(List.of(row("kind", "CONSUME", "qty", 3000)));

        Map<String, Object> body = controller.summary("GP-1-54").getBody();

        assertThat(body).isNotNull();
        assertThat(((Map<String, Object>) body.get("gatePass")).get("gp_number")).isEqualTo("GP-1-54");
        assertThat(((Map<String, Object>) body.get("workflow")).get("workflow_code")).isEqualTo("M10.TankerOutbound");
        assertThat((List<?>) body.get("timeline")).hasSize(1);
        assertThat((List<Map<String, Object>>) body.get("loadingBays")).hasSize(1);
        assertThat((List<?>) body.get("stockMovements")).hasSize(1);
        // sections the gate pass never reached are simply empty
        assertThat((List<?>) body.get("qcPreload")).isEmpty();
        assertThat((List<?>) body.get("sampleVehicles")).isEmpty();
        assertThat((List<?>) body.get("unavailable")).isEmpty();
        // the instance id and tenant scope every query
        verify(jdbc).queryForList(contains("FROM qc_sample_vehicle"), eq(1L), eq(2040L));
        verify(jdbc).queryForList(contains("FROM unloading_progress_detail_bay"), eq(1L), eq(2040L));
    }

    @SuppressWarnings("unchecked")
    @Test
    void aSectionThatCannotBeRead_isListedAsUnavailable_theRestStillComes() {
        gatePass(2040L);
        when(jdbc.queryForList(contains("FROM qc_sample_vehicle"), any(Object[].class)))
                .thenThrow(new org.springframework.jdbc.BadSqlGrammarException("x", "SELECT", new java.sql.SQLException("no table")));
        when(jdbc.queryForList(contains("FROM loading_progress_detail_bay"), any(Object[].class)))
                .thenReturn(List.of(row("bay_name", "COT-1")));

        Map<String, Object> body = controller.summary("GP-1-54").getBody();

        assertThat((List<String>) body.get("unavailable")).containsExactly("sampleVehicles");
        assertThat((List<?>) body.get("sampleVehicles")).isEmpty();
        assertThat((List<?>) body.get("loadingBays")).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    @Test
    void gatePassWithoutAWorkflow_returnsTheHeaderAndEmptySections() {
        gatePass(null);

        Map<String, Object> body = controller.summary("GP-1-54").getBody();

        assertThat(body.get("workflow")).isNull();
        assertThat((List<?>) body.get("timeline")).isEmpty();
        assertThat((List<?>) body.get("loadingBays")).isEmpty();
        verify(jdbc, never()).queryForList(contains("FROM loading_progress_detail_bay"), any(Object[].class));
    }

    @Test
    void itOnlyEverReads_neverWrites() {
        gatePass(2040L);

        controller.summary("GP-1-54");

        verify(jdbc, never()).update(anyString(), any(Object[].class));
        verify(jdbc, never()).update(anyString());
    }

    // ---- search: by gate pass, vehicle number or transport name, within a date range ----

    @SuppressWarnings("unchecked")
    private Map<String, Object> search(String gp, String vehicle, String transport, String from, String to) {
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"), eq(Long.class), any(Object[].class))).thenReturn(2L);
        when(jdbc.queryForList(contains("SELECT gp.id"), any(Object[].class))).thenReturn(List.of(row("gp_number", "GP-1"), row("gp_number", "GP-2")));
        when(jdbc.queryForList(contains("GROUP BY gp.vehicle_no"), any(Object[].class))).thenReturn(List.of(row("vehicle_no", "TN1", "gate_pass_count", 2)));
        ResponseEntity<Map<String, Object>> r = controller.search(gp, vehicle, transport, from, to, 0, 50);
        assertThat(r.getStatusCode().value()).isEqualTo(200);
        return r.getBody();
    }

    private String lastSql(String startsWith) {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getArguments().length > 0 && i.getArgument(0) instanceof String q && q.startsWith(startsWith))
                .map(i -> (String) i.getArgument(0)).reduce((a, b) -> b).orElseThrow();
    }

    @Test
    void search_needsAtLeastOneCriterion_aDateRangeAloneIsNotEnough() {
        ResponseEntity<Map<String, Object>> r = controller.search(null, " ", null, "2026-10-01", "2026-10-20", 0, 50);
        assertThat(r.getStatusCode().value()).isEqualTo(400);
        assertThat(r.getBody()).containsEntry("error", "criteria_required");
        verifyNoInteractions(jdbc);
    }

    @SuppressWarnings("unchecked")
    @Test
    void search_byVehicleNumber_ignoresDashesSpacesAndCase_withTheDateRange() {
        Map<String, Object> body = search(null, "tn-23 j1144", null, "2026-10-01", "2026-10-20");

        assertThat((List<?>) body.get("content")).hasSize(2);
        assertThat(body.get("totalElements")).isEqualTo(2L);
        assertThat((List<Map<String, Object>>) body.get("vehicles")).hasSize(1);
        String sql = lastSql("SELECT gp.id");
        assertThat(sql).contains("UPPER(REPLACE(REPLACE(gp.vehicle_no").contains("gp.created_at >= ?").contains("gp.created_at < ?")
                .contains("ORDER BY gp.created_at DESC").contains("LIMIT ? OFFSET ?");
        // tenant, vehicle pattern, from, to (the day after), page size, offset
        verify(jdbc).queryForList(contains("SELECT gp.id"), eq(1L), eq("%TN23J1144%"),
                eq(java.sql.Timestamp.valueOf("2026-10-01 00:00:00")), eq(java.sql.Timestamp.valueOf("2026-10-21 00:00:00")), eq(50), eq(0L));
    }

    @Test
    void search_byTransportName_looksAtTheGatePassAndTheDispatchDetail() {
        search(null, null, "Sri Murugan", null, null);

        assertThat(lastSql("SELECT gp.id")).contains("COALESCE(NULLIF(gp.transport_name, ''), dvd.transport_name) LIKE ?")
                .contains("LEFT JOIN dispatch_vehicle_detail dvd");
        verify(jdbc).queryForList(contains("SELECT gp.id"), eq(1L), eq("%Sri Murugan%"), eq(50), eq(0L));
    }

    @Test
    void search_byGatePassNumber_isExact() {
        search("GP-1-54", null, null, null, null);

        assertThat(lastSql("SELECT gp.id")).contains("gp.gp_number = ?");
        verify(jdbc).queryForList(contains("SELECT gp.id"), eq(1L), eq("GP-1-54"), eq(50), eq(0L));
    }

    @Test
    void search_badDates_areRefused() {
        assertThat(controller.search(null, "TN1", null, "01-10-2026", null, 0, 50).getBody()).containsEntry("error", "bad_date");
        assertThat(controller.search(null, "TN1", null, "2026-10-20", "2026-10-01", 0, 50).getBody()).containsEntry("error", "bad_range");
    }

    @Test
    void search_pageSizeIsCapped_andPagingMovesTheOffset() {
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"), eq(Long.class), any(Object[].class))).thenReturn(0L);

        controller.search(null, "TN1", null, null, null, 2, 9999);

        verify(jdbc).queryForList(contains("SELECT gp.id"), eq(1L), eq("%TN1%"), eq(200), eq(400L));
    }

    @Test
    void search_neverWrites() {
        search(null, "TN1", null, null, null);
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }

    // ---- journey: the gate pass as a story, step by step ----

    private static java.sql.Timestamp at(String ts) { return java.sql.Timestamp.valueOf(ts); }

    @SuppressWarnings("unchecked")
    @Test
    void journey_listsEveryStepInOrder_withWhoWhenMinutesAndWhatThePageSaved() {
        gatePass(2040L);
        when(jdbc.queryForList(contains("FROM workflow_event"), any(Object[].class))).thenReturn(List.of(
                row("ts", at("2026-10-10 09:00:00"), "kind", "TRANSITION", "to_state", "DISPATCH_QUEUED",
                        "page_code", "aria:dispatch-vehicle-details", "actor_user_id", 7L),
                row("ts", at("2026-10-10 09:38:00"), "kind", "TRANSITION", "from_state", "DISPATCH_QUEUED", "to_state", "WEIGHMENT_1",
                        "page_code", "aria:weighment-capture", "actor_user_id", 8L)));
        when(jdbc.queryForList(contains("FROM page_submitter_map"), any(Object[].class))).thenReturn(List.of(
                row("page_code", "aria:dispatch-vehicle-details", "table_name", "dispatch_vehicle_detail", "key_column", "workflow_instance_id"),
                row("page_code", "aria:weighment-capture", "table_name", "weighment_capture_detail", "key_column", "workflow_instance_id")));
        when(jdbc.queryForList(startsWith("SELECT * FROM dispatch_vehicle_detail"), any(Object[].class)))
                .thenReturn(List.of(row("vehicle_no", "TN1", "performed_by_name", "Ravi")));
        when(jdbc.queryForList(startsWith("SELECT * FROM weighment_capture_detail"), any(Object[].class)))
                .thenReturn(List.of(row("w1_weight_kg", 18200)));

        List<Map<String, Object>> journey = (List<Map<String, Object>>) controller.summary("GP-1-54").getBody().get("journey");

        assertThat(journey).hasSize(2);
        assertThat(journey.get(0).get("step")).isEqualTo(1);
        assertThat(journey.get(0).get("minutes_since_previous")).isNull();
        assertThat(journey.get(0).get("performed_by_name")).isEqualTo("Ravi");
        assertThat(((List<Map<String, Object>>) journey.get(0).get("entry")).get(0)).containsEntry("vehicle_no", "TN1");
        assertThat(journey.get(1).get("step")).isEqualTo(2);
        assertThat(((java.math.BigDecimal) journey.get(1).get("minutes_since_previous")).doubleValue()).isEqualTo(38.0);
        assertThat(journey.get(1).get("actor_user_id")).isEqualTo(8L);
        assertThat(((List<Map<String, Object>>) journey.get(1).get("entry")).get(0)).containsEntry("w1_weight_kg", 18200);
    }

    @SuppressWarnings("unchecked")
    @Test
    void journey_aTableNameThatIsNotAPlainName_isNeverQueried() {
        gatePass(2040L);
        when(jdbc.queryForList(contains("FROM workflow_event"), any(Object[].class)))
                .thenReturn(List.of(row("ts", at("2026-10-10 09:00:00"), "page_code", "aria:x")));
        when(jdbc.queryForList(contains("FROM page_submitter_map"), any(Object[].class)))
                .thenReturn(List.of(row("page_code", "aria:x", "table_name", "gate_pass; DROP TABLE x", "key_column", "workflow_instance_id")));

        List<Map<String, Object>> journey = (List<Map<String, Object>>) controller.summary("GP-1-54").getBody().get("journey");

        assertThat((List<?>) journey.get(0).get("entry")).isEmpty();
        verify(jdbc, never()).queryForList(contains("DROP TABLE"), any(Object[].class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void journey_aStepWithNoPageOrNoMappedTable_stillAppears_withAnEmptyEntry() {
        gatePass(2040L);
        when(jdbc.queryForList(contains("FROM workflow_event"), any(Object[].class)))
                .thenReturn(List.of(row("ts", at("2026-10-10 09:00:00"), "kind", "CREATED")));

        List<Map<String, Object>> journey = (List<Map<String, Object>>) controller.summary("GP-1-54").getBody().get("journey");

        assertThat(journey).hasSize(1);
        assertThat((List<?>) journey.get(0).get("entry")).isEmpty();
        assertThat(journey.get(0).get("performed_by_name")).isNull();
    }

    // ---- history: the full summary of every gate pass of a vehicle / transport ----

    @SuppressWarnings("unchecked")
    @Test
    void history_returnsTheFullSummaryOfEachMatchingGatePass() {
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"), eq(Long.class), any(Object[].class))).thenReturn(2L);
        when(jdbc.queryForList(contains("SELECT gp.id"), any(Object[].class))).thenReturn(List.of(row("gp_number", "GP-1"), row("gp_number", "GP-2")));
        Map<String, Object> gp1 = row("id", 1L, "gp_number", "GP-1");
        gp1.put("workflow_instance_id", null);
        Map<String, Object> gp2 = row("id", 2L, "gp_number", "GP-2");
        gp2.put("workflow_instance_id", null);
        when(jdbc.queryForList(contains("FROM gate_pass WHERE"), any(Object[].class)))
                .thenAnswer(inv -> "GP-1".equals(inv.getArgument(2)) ? List.of(gp1) : List.of(gp2));

        ResponseEntity<Map<String, Object>> r = controller.history(null, "TN1", null, null, null, 0, 5);

        assertThat(r.getStatusCode().value()).isEqualTo(200);
        List<Map<String, Object>> items = (List<Map<String, Object>>) r.getBody().get("content");
        assertThat(items).hasSize(2);
        assertThat(((Map<String, Object>) items.get(0).get("gatePass")).get("gp_number")).isEqualTo("GP-1");
        assertThat(items.get(1)).containsKeys("journey", "timeline", "stockMovements", "unavailable");
        assertThat(r.getBody().get("totalElements")).isEqualTo(2L);
    }

    @Test
    void history_needsACriterion_andCapsThePageAtTwenty() {
        assertThat(controller.history(null, null, null, "2026-10-01", null, 0, 5).getStatusCode().value()).isEqualTo(400);

        when(jdbc.queryForObject(contains("SELECT COUNT(*)"), eq(Long.class), any(Object[].class))).thenReturn(0L);
        controller.history(null, "TN1", null, null, null, 0, 500);
        verify(jdbc).queryForList(contains("SELECT gp.id"), eq(1L), eq("%TN1%"), eq(20), eq(0L));
    }
}
