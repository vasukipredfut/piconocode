package com.aria.conveyor;

import com.aria.conveyor.dto.RuleEvalResult;
import com.aria.conveyor.dto.ScanRequest;
import com.aria.conveyor.service.ConveyorScanService;
import com.aria.conveyor.service.DispatchTaskMath;
import com.aria.conveyor.service.DispatchTaskService;
import com.aria.conveyor.service.PlcSignalService;
import com.aria.conveyor.service.RuleEvaluationService;
import com.aria.conveyor.service.SkuMasterLookupService;
import com.aria.conveyor.service.WorkflowIntegrationBindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * A scan over the order quantity is rejected AND counted against the task (dispatch_task.rejected_qty, through
 * recordRejectedScan(taskId, ...)); an unknown barcode / inactive SKU / no task at all stays a scan-log-only reject
 * (taskId null).
 */
class ConveyorScanOverOrderQtyTest {

    private DispatchTaskService tasks;
    private RuleEvaluationService rules;
    private WorkflowIntegrationBindingService bindings;
    private PlcSignalService plc;
    private SkuMasterLookupService skuLookup;
    private ConveyorScanService service;

    @BeforeEach
    void setUp() {
        tasks = mock(DispatchTaskService.class);
        rules = mock(RuleEvaluationService.class);
        bindings = mock(WorkflowIntegrationBindingService.class);
        plc = mock(PlcSignalService.class);
        skuLookup = mock(SkuMasterLookupService.class);
        service = new ConveyorScanService(tasks, rules, bindings, plc, skuLookup);
        when(skuLookup.resolveByBarcode(anyString())).thenReturn(Optional.of(Map.of("sku_code", "SKU1", "status", "ACTIVE")));
        when(rules.loadAndEvaluate(any(), any(), any(), any(), any())).thenReturn(RuleEvalResult.pass());
        when(bindings.resolve(any(), any(), any(), any())).thenReturn(Optional.empty());
    }

    private static ScanRequest scan(BigDecimal qty) {
        return new ScanRequest("M10.PackLoading", "STEP", "BAY-1", "8901", null, 5L, null, null, qty);
    }

    private static Map<String, Object> task(String status, String processed, String target) {
        Map<String, Object> t = new HashMap<>();
        t.put("id", 11L);
        t.put("sku_code", "SKU1");
        t.put("status", status);
        t.put("processed_qty", new BigDecimal(processed));
        t.put("target_qty", new BigDecimal(target));
        return t;
    }

    @Test
    void scanAfterTheTaskIsFilled_isRejected_andCountedOnThatTask() {
        when(tasks.listActiveForBay(any(), any())).thenReturn(List.of());
        when(tasks.findLatestCompleted("M10.PackLoading", "BAY-1", "SKU1")).thenReturn(Optional.of(task("Completed", "100", "100")));

        Map<String, Object> r = service.scan(scan(null));

        assertThat(r).containsEntry("outcome", "REJECT").containsEntry("reason", "ORDER_QTY_EXCEEDED");
        verify(tasks).recordRejectedScan(11L, "8901", "ORDER_QTY_EXCEEDED", 5L);
        verify(tasks, never()).recordAcceptedScan(any(), any(), any(), any(), any(), any());
    }

    @Test
    void aScanThatWouldTakeAnOpenTaskPastItsTarget_isRejected_andNothingIsAdded() {
        when(tasks.listActiveForBay(any(), any())).thenReturn(List.of(task("Open", "98", "100")));

        Map<String, Object> r = service.scan(scan(new BigDecimal("5")));

        assertThat(r).containsEntry("reason", "ORDER_QTY_EXCEEDED");
        verify(tasks).recordRejectedScan(11L, "8901", "ORDER_QTY_EXCEEDED", 5L);
        verify(tasks, never()).recordAcceptedScan(any(), any(), any(), any(), any(), any());
    }

    @Test
    void aScanThatExactlyFillsTheTask_isAccepted() {
        when(tasks.listActiveForBay(any(), any())).thenReturn(List.of(task("Open", "99", "100")));
        when(tasks.recordAcceptedScan(any(), any(), any(), any(), any(), any())).thenReturn(Optional.of(task("Completed", "100", "100")));

        Map<String, Object> r = service.scan(scan(null));

        assertThat(r).containsEntry("outcome", "ACCEPT");
        verify(tasks, never()).recordRejectedScan(any(), any(), any(), any());
    }

    @Test
    void noTaskAtAll_isLogOnly_noTaskIdSoNoRejectedQty() {
        when(tasks.listActiveForBay(any(), any())).thenReturn(List.of());
        when(tasks.findLatestCompleted(any(), any(), any())).thenReturn(Optional.empty());

        Map<String, Object> r = service.scan(scan(null));

        assertThat(r).containsEntry("reason", "NO_ACTIVE_TASK");
        verify(tasks).recordRejectedScan(isNull(), eq("8901"), eq("NO_ACTIVE_TASK"), eq(5L));
    }

    @Test
    void unknownBarcode_andInactiveSku_areLogOnly() {
        when(skuLookup.resolveByBarcode("bad")).thenReturn(Optional.empty());
        when(skuLookup.resolveByBarcode("off")).thenReturn(Optional.of(Map.of("sku_code", "SKU9", "status", "INACTIVE")));

        service.scan(new ScanRequest("M10.PackLoading", "STEP", "BAY-1", "bad", null, 5L, null, null, null));
        service.scan(new ScanRequest("M10.PackLoading", "STEP", "BAY-1", "off", null, 5L, null, null, null));

        verify(tasks).recordRejectedScan(isNull(), eq("bad"), eq("UNKNOWN_BARCODE"), eq(5L));
        verify(tasks).recordRejectedScan(isNull(), eq("off"), eq("SKU_INACTIVE"), eq(5L));
        verify(tasks, never()).recordRejectedScan(notNull(), any(), any(), any());
    }

    @Test
    void exceedsTarget_math() {
        assertThat(DispatchTaskMath.exceedsTarget(new BigDecimal("99"), BigDecimal.ONE, new BigDecimal("100"))).isFalse();
        assertThat(DispatchTaskMath.exceedsTarget(new BigDecimal("100"), BigDecimal.ONE, new BigDecimal("100"))).isTrue();
        assertThat(DispatchTaskMath.exceedsTarget(new BigDecimal("98"), new BigDecimal("5"), new BigDecimal("100"))).isTrue();
        assertThat(DispatchTaskMath.exceedsTarget(null, BigDecimal.ONE, null)).isFalse();
    }
}
