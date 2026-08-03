package com.aria.m10.parking.service;

import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ParkingBayScanService}. Mock the JdbcTemplate;
 * distinguish the gate_pass lookup from the dispatch_parking_bay_entry
 * lookup by SQL substring, capture every SQL the service fires and assert
 * via substring — same convention as StockMovementListenerTest.
 *
 * <p>Note: queryForList/queryForMap take {@code Object...}, but stubbing
 * with a single {@code ArgumentMatchers.<Object[]>any()} for "match any
 * varargs" (the convention used for {@code jdbc.update(...)} elsewhere in
 * this repo) does NOT reliably match here — empirically verified to return
 * the mock's default (empty list) instead. Stubbing each positional argument
 * explicitly (e.g. {@code any(Long.class), any(String.class)}) does work
 * reliably and is what every stub below uses.
 */
class ParkingBayScanServiceTest {

    private JdbcTemplate jdbc;
    private ParkingBayScanService service;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        // Generic stub: any update() returns 1 (one row affected).
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        service = new ParkingBayScanService(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void scan_nullGpNumber_returnsErrorWithoutTouchingDb() {
        Map<String, Object> result = service.scan(null);
        assertThat(result.get("outcome")).isEqualTo("ERROR");
        assertThat(result.get("reason")).isEqualTo("gpNumber is required");
        verifyNoInteractions(jdbc);
    }

    @Test
    void scan_blankGpNumber_returnsErrorWithoutTouchingDb() {
        Map<String, Object> result = service.scan("   ");
        assertThat(result.get("outcome")).isEqualTo("ERROR");
        verifyNoInteractions(jdbc);
    }

    @Test
    void scan_unknownGatePass_returnsReject() {
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of());

        Map<String, Object> result = service.scan("GP-UNKNOWN");

        assertThat(result.get("outcome")).isEqualTo("REJECT");
        assertThat(result.get("reason")).isEqualTo("GATE_PASS_NOT_FOUND");
        assertThat(result.get("parkingBayEntry")).isNull();
        assertThat(capturedUpdateSql()).isEmpty();
    }

    @Test
    void scan_noOpenEntry_insertsNewRow_outcomeIn() {
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePassRow()));
        when(jdbc.queryForList(contains("dispatch_parking_bay_entry"), any(Long.class), any(String.class)))
                .thenReturn(List.of());
        when(jdbc.queryForObject(eq("SELECT LAST_INSERT_ID()"), eq(Long.class))).thenReturn(7L);
        Map<String, Object> entryRow = entryRow(7L, null);
        when(jdbc.queryForMap(anyString(), any(Long.class))).thenReturn(entryRow);

        Map<String, Object> result = service.scan("GP-1");

        assertThat(result.get("outcome")).isEqualTo("IN");
        assertThat(result.get("reason")).isNull();
        assertThat(result.get("parkingBayEntry")).isEqualTo(entryRow);
        assertThat(capturedUpdateSql()).anyMatch(s -> s.contains("INSERT INTO dispatch_parking_bay_entry"));
        assertThat(capturedUpdateSql()).noneMatch(s -> s.contains("UPDATE dispatch_parking_bay_entry"));
        assertThat(capturedUpdateSql()).anyMatch(s -> s.contains("INSERT INTO m0_audit_log"));
    }

    @Test
    void scan_openEntryExists_closesRow_outcomeOut() {
        when(jdbc.queryForList(contains("FROM gate_pass"), any(Long.class), any(String.class)))
                .thenReturn(List.of(gatePassRow()));
        Map<String, Object> openRow = new LinkedHashMap<>();
        openRow.put("id", 9L);
        when(jdbc.queryForList(contains("dispatch_parking_bay_entry"), any(Long.class), any(String.class)))
                .thenReturn(List.of(openRow));
        Map<String, Object> closedRow = entryRow(9L, "2026-08-03T02:00:00Z");
        when(jdbc.queryForMap(anyString(), any(Long.class))).thenReturn(closedRow);

        Map<String, Object> result = service.scan("GP-1");

        assertThat(result.get("outcome")).isEqualTo("OUT");
        assertThat(result.get("reason")).isNull();
        assertThat(result.get("parkingBayEntry")).isEqualTo(closedRow);
        assertThat(capturedUpdateSql()).anyMatch(s -> s.contains("UPDATE dispatch_parking_bay_entry SET out_time"));
        assertThat(capturedUpdateSql()).noneMatch(s -> s.contains("INSERT INTO dispatch_parking_bay_entry"));
        assertThat(capturedUpdateSql()).anyMatch(s -> s.contains("INSERT INTO m0_audit_log"));
    }

    // ---- helpers -----------------------------------------------------------

    private static Map<String, Object> gatePassRow() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 42L);
        row.put("vehicle_no", "TN01AB1234");
        row.put("flow_variant_code", "M10.TankerInbound");
        row.put("loading_type", null);
        return row;
    }

    private static Map<String, Object> entryRow(Long id, String outTime) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("gate_pass_no", "GP-1");
        row.put("vehicle_no", "TN01AB1234");
        row.put("out_time", outTime);
        return row;
    }

    /** Pull every SQL string passed to any jdbc.update(...) call. */
    private List<String> capturedUpdateSql() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArgument(0))
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
