package com.aria.conveyor.service;

import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dispatch Task lifecycle: target-vs-processed quantity tracking with
 * auto-complete, and the accept/reject scan log. Multiple concurrent tasks
 * per bay are just multiple rows — no special-casing needed.
 *
 * <p>The increment+auto-complete reads the row with {@code SELECT ... FOR
 * UPDATE} (row-locking it for the rest of the transaction), computes the new
 * quantity/status in Java via the already-tested {@link DispatchTaskMath},
 * then writes the literal result. A single {@code UPDATE t SET a = a + ?,
 * b = CASE WHEN a + ? >= ... } was tried first and is NOT safe on MariaDB/
 * MySQL: later {@code SET} clauses in the same statement see the
 * ALREADY-UPDATED value of a column set earlier in that same statement, not
 * the original row — so the {@code CASE} double-counted the increment and
 * completed tasks one scan early. Compute-then-write avoids that entirely.
 */
@Service
public class DispatchTaskService {

    private final JdbcTemplate jdbc;

    public DispatchTaskService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Map<String, Object>> findById(Long taskId) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM dispatch_task WHERE tenant_id = ? AND id = ?", tenantId, taskId);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Active (Open) tasks for a bay — there can be several at once. */
    public List<Map<String, Object>> listActiveForBay(String workflowCode, String bayCode) {
        long tenantId = TenantContext.get();
        return jdbc.queryForList(
                "SELECT * FROM dispatch_task WHERE tenant_id = ? AND workflow_code = ? AND bay_code = ? "
                        + "AND status = 'Open' ORDER BY created_at",
                tenantId, workflowCode, bayCode);
    }

    /** The bay's most recently completed task for this SKU: a scan that arrives after it is over the order quantity. */
    public Optional<Map<String, Object>> findLatestCompleted(String workflowCode, String bayCode, String skuCode) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM dispatch_task WHERE tenant_id = ? AND workflow_code = ? AND bay_code = ? AND sku_code = ? "
                        + "AND status = 'Completed' ORDER BY completed_at DESC, id DESC LIMIT 1",
                tenantId, workflowCode, bayCode, skuCode);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * The one task matching (workflowCode, bayCode, skuCode, status) exactly — used by
     * {@code GET /api/conveyor/dispatchdetails} so a device can fetch a specific task's
     * live target_qty/processed_qty right before a scan (not from a cached snapshot).
     * Deterministic on ties via {@code created_at} (oldest first, same convention as
     * {@link #listActiveForBay}).
     */
    public Optional<Map<String, Object>> findOne(String workflowCode, String bayCode, String skuCode, String status) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM dispatch_task WHERE tenant_id = ? AND workflow_code = ? AND bay_code = ? "
                        + "AND sku_code = ? AND status = ? ORDER BY created_at LIMIT 1",
                tenantId, workflowCode, bayCode, skuCode, status);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /** Every task for a workflow (used to build the aggregate GET response' "current dispatch tasks"). */
    public List<Map<String, Object>> listForWorkflow(String workflowCode) {
        return listForWorkflow(workflowCode, null);
    }

    /** Same, optionally scoped to one bay at the SQL level (see WorkflowIntegrationBindingService for why). */
    public List<Map<String, Object>> listForWorkflow(String workflowCode, String bayCode) {
        long tenantId = TenantContext.get();
        if (bayCode == null) {
            return jdbc.queryForList(
                    "SELECT * FROM dispatch_task WHERE tenant_id = ? AND workflow_code = ? ORDER BY created_at DESC",
                    tenantId, workflowCode);
        }
        return jdbc.queryForList(
                "SELECT * FROM dispatch_task WHERE tenant_id = ? AND workflow_code = ? AND bay_code = ? "
                        + "ORDER BY created_at DESC",
                tenantId, workflowCode, bayCode);
    }

    /**
     * Row-locks the task, computes the new processed_qty/status (auto-completing
     * at target) via {@link DispatchTaskMath}, writes it, and logs an ACCEPTED
     * scan. Returns the updated task row, or empty if the task doesn't exist /
     * isn't Open for this tenant (no-op — the caller should treat that
     * as a scan against a task that can no longer accept).
     */
    @Transactional("psqlTransactionManager")
    public Optional<Map<String, Object>> recordAcceptedScan(Long taskId, String barcode, String matchedSku,
                                                             String matchedOrderRef, Long integrationId,
                                                             BigDecimal incrementQty) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> locked = jdbc.queryForList(
                "SELECT processed_qty, target_qty FROM dispatch_task WHERE id = ? AND tenant_id = ? "
                        + "AND status = 'Open' FOR UPDATE",
                taskId, tenantId);
        if (locked.isEmpty()) return Optional.empty();
        BigDecimal processedQty = (BigDecimal) locked.get(0).get("processed_qty");
        BigDecimal targetQty = (BigDecimal) locked.get(0).get("target_qty");
        DispatchTaskMath.Result result = DispatchTaskMath.apply(processedQty, incrementQty, targetQty, "Open");

        if ("Completed".equals(result.newStatus())) {
            jdbc.update(
                    "UPDATE dispatch_task SET processed_qty = ?, status = 'Completed', "
                            + "completed_at = CASE WHEN completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END "
                            + "WHERE id = ? AND tenant_id = ?",
                    result.newProcessedQty(), taskId, tenantId);
        } else {
            jdbc.update("UPDATE dispatch_task SET processed_qty = ? WHERE id = ? AND tenant_id = ?",
                    result.newProcessedQty(), taskId, tenantId);
        }
        jdbc.update(
                "INSERT INTO dispatch_task_scan_log (tenant_id, dispatch_task_id, barcode, outcome, matched_sku, "
                        + "matched_order_ref, integration_id) VALUES (?,?,?,'ACCEPTED',?,?,?)",
                tenantId, taskId, barcode, matchedSku, matchedOrderRef, integrationId);
        return findById(taskId);
    }

    /** Logs a REJECTED scan with a reason; taskId may be null (no matching active task at all). */
    public void recordRejectedScan(Long taskId, String barcode, String rejectReason, Long integrationId) {
        long tenantId = TenantContext.get();
        if (taskId != null) {
            jdbc.update("UPDATE dispatch_task SET rejected_qty = rejected_qty + 1 WHERE id = ? AND tenant_id = ?",
                    taskId, tenantId);
        }
        jdbc.update(
                "INSERT INTO dispatch_task_scan_log (tenant_id, dispatch_task_id, barcode, outcome, reject_reason, "
                        + "integration_id) VALUES (?,?,?,'REJECTED',?,?)",
                tenantId, taskId, barcode, rejectReason, integrationId);
    }

    /** Moves a PENDING task to Open. No-op if it's already past PENDING. */
    public void start(Long taskId) {
        long tenantId = TenantContext.get();
        jdbc.update(
                "UPDATE dispatch_task SET status = 'Open', started_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND tenant_id = ? AND status = 'PENDING'",
                taskId, tenantId);
    }
}
