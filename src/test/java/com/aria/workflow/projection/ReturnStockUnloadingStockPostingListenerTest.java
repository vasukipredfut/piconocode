package com.aria.workflow.projection;

import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.Invocation;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReturnStockUnloadingStockPostingListener}. Mock the
 * JdbcTemplate; capture every SQL the listener fires and assert via
 * substring — same style as {@code StockMovementListenerTest}.
 */
class ReturnStockUnloadingStockPostingListenerTest {

    private JdbcTemplate jdbc;
    private ReturnStockUnloadingStockPostingListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        when(jdbc.update(anyString(), ArgumentMatchers.<Object[]>any())).thenReturn(1);
        when(jdbc.queryForList(anyString(), eq(String.class), any(), any()))
                .thenReturn(List.of("GP-1"));
        listener = new ReturnStockUnloadingStockPostingListener(jdbc);
    }

    @Test
    void wrongWorkflow_doesNothing() {
        WorkflowInstance wi = wi("M10.PackUnload", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "UNLOADING_IN_PROGRESS",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void wrongState_doesNothing() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "WEIGHMENT_2");
        listener.onTransition(wi, "UNLOADING_COMPLETED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        verifyNoInteractions(jdbc);
    }

    @Test
    void noPageData_doesNothing() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED", Map.of("aria:qc-approval", Map.of("status", "APPROVED")));
        verifyNoInteractions(jdbc);
    }

    @Test
    void emptyList_doesNothing() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of())));
        verifyNoInteractions(jdbc);
    }

    @Test
    void fromQcApprovedDirectly_posts() {
        // The new direct QC_APPROVED -> UNLOADING_COMPLETED edge (V325) must
        // post identically to the legacy UNLOADING_IN_PROGRESS -> UNLOADING_COMPLETED
        // hop — checked against CURRENT state, not previousState.
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    @Test
    void fromUnloadingInProgress_postsToo() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "UNLOADING_IN_PROGRESS",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
    }

    @Test
    void onePosted_writesMovementAndBalanceUpdate() {
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        List<String> sql = capturedSql();
        assertThat(sql).anyMatch(s -> s.contains("INSERT INTO m5_stock_movement"));
        assertThat(sql).anyMatch(s -> s.contains("UPDATE m5_stock_balance") && s.contains("pc_qty_on_hand"));
    }

    @Test
    void pcLine_postsToPcQtyOnHandOnly() {
        // A PC-type split line's confirmed qty is a loose piece count, not a
        // box count — it must land in pc_qty_on_hand, and its qty_on_hand
        // delta must be zero (asserted via the bound parameter order: boxDelta
        // comes first in the UPDATE, pcDelta second).
        Map<String, Object> pcRow = row("SKU-1624", "Coconut Oil 100ml", "BACT-003", "G1", "PC", "3");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(pcRow))));
        List<Object[]> balanceUpdates = mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .filter(i -> i.getArgument(0).toString().startsWith("UPDATE m5_stock_balance"))
                .map(Invocation::getArguments) // [sql, boxDelta, pcDelta, ...bind params]
                .collect(Collectors.toList());
        assertThat(balanceUpdates).hasSize(1);
        assertThat(balanceUpdates.get(0)[1]).isEqualTo(BigDecimal.ZERO); // qty_on_hand delta
        assertThat((BigDecimal) balanceUpdates.get(0)[2]).isEqualByComparingTo("3"); // pc_qty_on_hand delta
    }

    @Test
    void boxAndPcLinesForSameLocation_postIndependently() {
        // The frontend's splitRows expands one qc_processed_location (box_qty=5,
        // pc_qty=3) into two submitted lines sharing the same batch/bin — both
        // must post, each to its own balance column.
        Map<String, Object> boxLine = row("SKU-1624", "Coconut Oil 100ml", "BACT-003", "G1", "BOX", "5");
        Map<String, Object> pcLine = row("SKU-1624", "Coconut Oil 100ml", "BACT-003", "G1", "PC", "3");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(boxLine, pcLine))));
        long movementInserts = capturedSql().stream().filter(s -> s.contains("INSERT INTO m5_stock_movement")).count();
        assertThat(movementInserts).isEqualTo(2);
    }

    @Test
    void missingLineType_defaultsToBox() {
        // Backward compatibility: a line with no line_type (pre-split shape,
        // or a location that only ever had one quantity) posts as BOX, same
        // as before splitRows existed.
        Map<String, Object> untyped = row("SKU-1624", "Coconut Oil 100ml", "BACT-003", "G1", null, "5");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(untyped))));
        List<Object[]> balanceUpdates = mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .filter(i -> i.getArgument(0).toString().startsWith("UPDATE m5_stock_balance"))
                .map(Invocation::getArguments) // [sql, boxDelta, pcDelta, ...bind params]
                .collect(Collectors.toList());
        assertThat(balanceUpdates).hasSize(1);
        assertThat((BigDecimal) balanceUpdates.get(0)[1]).isEqualByComparingTo("5"); // qty_on_hand delta
        assertThat(balanceUpdates.get(0)[2]).isEqualTo(BigDecimal.ZERO); // pc_qty_on_hand delta
    }

    @Test
    void balanceUpdateAffectsZeroRows_fallsBackToInsert() {
        when(jdbc.update(startsWith("UPDATE m5_stock_balance"), ArgumentMatchers.<Object[]>any()))
                .thenReturn(0);
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(oneRow()))));
        assertThat(capturedSql()).anyMatch(s -> s.contains("INSERT INTO m5_stock_balance") && s.contains("pc_qty_on_hand"));
    }

    @Test
    void twoLocations_postsTwoMovements() {
        Map<String, Object> row1 = row("SKU-A", "Item A", "B26-01", "G1", "BOX", "10");
        Map<String, Object> row2 = row("SKU-A", "Item A", "B26-01", "G2", "BOX", "3");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(row1, row2))));
        long movementInserts = capturedSql().stream().filter(s -> s.contains("INSERT INTO m5_stock_movement")).count();
        assertThat(movementInserts).isEqualTo(2);
    }

    @Test
    void rowWithZeroQty_isSkipped_othersStillPost() {
        Map<String, Object> zeroRow = row("SKU-B", "Item B", "B26-02", "G1", "BOX", "0");
        Map<String, Object> goodRow = oneRow();
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(zeroRow, goodRow))));
        long movementInserts = capturedSql().stream().filter(s -> s.contains("INSERT INTO m5_stock_movement")).count();
        assertThat(movementInserts).isEqualTo(1);
    }

    @Test
    void rowMissingBin_isSkipped() {
        // A gate-pass-number lookup (queryForList) always runs once per
        // invocation regardless of row validity — only the per-row
        // movement/balance writes (update calls) are expected to be skipped.
        Map<String, Object> badRow = row("SKU-C", "Item C", "B26-03", null, "BOX", "5");
        WorkflowInstance wi = wi("M10.ReturnStockIn", "UNLOADING_COMPLETED");
        listener.onTransition(wi, "QC_APPROVED",
                Map.of("aria:unloading-progress", Map.of("qcAllocatedLocations", List.of(badRow))));
        assertThat(capturedSql()).isEmpty();
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

    private static Map<String, Object> oneRow() {
        return row("SKU-1624", "Coconut Oil 100ml", "BACT-003", "G1", "BOX", "5");
    }

    private static Map<String, Object> row(String skuCode, String skuName, String batchNo, String bin,
                                            String lineType, String unloadingQty) {
        java.util.HashMap<String, Object> m = new java.util.HashMap<>();
        m.put("sku_code", skuCode);
        m.put("sku_name", skuName);
        m.put("batch_no", batchNo);
        m.put("processed_area", bin);
        m.put("line_type", lineType);
        m.put("unloading_qty", unloadingQty);
        return m;
    }

    private List<String> capturedSql() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArgument(0))
                .map(Object::toString)
                .collect(Collectors.toList());
    }
}
