package com.aria.mes.stock;

import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StockMovementListener}. Mock the JdbcTemplate;
 * capture every SQL the listener fires and assert via substring.
 */
class StockMovementListenerTest {

    private JdbcTemplate jdbc;
    private StockMovementListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        // Generic stub: any update() returns 1 (one row affected).
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        listener = new StockMovementListener(jdbc);
    }

    @Test
    void inwardReceipt_stored_writes_receipt_and_upserts_balance() {
        WorkflowInstance wi = wi("M5.InwardReceipt", "STORED");
        Map<String, Object> page = Map.of(
                "itemCode", "OIL-SUN-RAW", "itemName", "Raw Sunflower Oil",
                "lotNo", "LOT-X-001", "storeTankCode", "T-OIL-09",
                "qtyReceived", "12500", "uom", "KG", "grnNumber", "GRN-T-1");
        listener.onTransition(wi, "QC_CHECKED", Map.of("pico:grn-store", page));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    @Test
    void mrIssued_writes_issue() {
        WorkflowInstance wi = wi("M5.MaterialRequisition", "ISSUED");
        Map<String, Object> page = Map.of(
                "itemCode", "OIL-SUN-RFD", "lotNo", "LOT-OUT-1",
                "binLocation", "T-OIL-08", "qtyIssued", "1500",
                "uom", "KG", "mrNumber", "MR-T-1");
        listener.onTransition(wi, "APPROVED", Map.of("pico:mr-issue", page));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    @Test
    void unhandled_workflow_doesNothing() {
        WorkflowInstance wi = wi("M10.TankerInbound", "CLOSED");
        listener.onTransition(wi, "WEIGHMENT_RECHECK", Map.of("pico:gate-pass-closure", Map.of("gpNumber", "GP-1")));
        verifyNoInteractions(jdbc);
    }

    @Test
    void missing_itemCode_skips_silently() {
        WorkflowInstance wi = wi("M5.InwardReceipt", "STORED");
        listener.onTransition(wi, "QC_CHECKED",
                Map.of("pico:grn-store", Map.of("storeTankCode", "T-OIL-09", "qtyReceived", "100")));
        verifyNoInteractions(jdbc);
    }

    @Test
    void emptyBag_skips() {
        WorkflowInstance wi = wi("M5.InwardReceipt", "STORED");
        listener.onTransition(wi, "QC_CHECKED", Map.of());
        verifyNoInteractions(jdbc);
    }

    @Test
    void mostRecentPageData_returnsLastMapInBag() {
        Map<String, Object> bag = new LinkedHashMap<>();
        bag.put("a", Map.of("x", 1));
        bag.put("b", Map.of("y", 2));
        assertThat(StockMovementListener.mostRecentPageData(bag)).containsEntry("y", 2);
    }

    @Test
    void jcConsumed_mapsToCONSUME() {
        WorkflowInstance wi = wi("M5.JobCard", "CONSUMED");
        Map<String, Object> page = Map.of(
                "itemCode", "OIL-SUN-RFD", "lotNo", "LOT-A", "fromBin", "T-OIL-08",
                "qtyConsumed", "500", "uom", "KG", "jcNumber", "JC-1");
        listener.onTransition(wi, "OPEN", Map.of("pico:jc-consume", page));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    // ---- helpers ---------------------------------------------------------
    private static WorkflowInstance wi(String code, String state) {
        WorkflowInstance i = new WorkflowInstance();
        i.setId(1L);
        i.setTenantId(1L);
        i.setWorkflowCode(code);
        i.setCurrentState(state);
        return i;
    }

    /** Pull every SQL string passed to any jdbc.update(...) call. Avoids the
     *  Mockito varargs-matcher rabbit-hole — inspect raw invocations. */
    private List<String> capturedSql() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArgument(0))
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
