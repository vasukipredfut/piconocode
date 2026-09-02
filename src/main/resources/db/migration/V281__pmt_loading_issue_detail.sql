-- Child table for aria:pmt-camera-verification's "SKU-wise Loading Issue
-- Details" RepeatingGroup (loading_issue_items) -- previously had nowhere to
-- save, so every entry made in that grid was discarded on submit. One row
-- per SKU line, not one row per workflow_instance (unlike the sibling
-- single-row pmt_camera_verification_detail, V89) -- see
-- PmtLoadingIssueItemsListener, which replaces all rows for the instance on
-- every submission of this page (delete-then-reinsert, so edits/removed
-- rows are reflected, not just appended).
--
-- Used by M10.FGLoading and M10.PackLoading today (both reference
-- aria:pmt-camera-verification), but the listener is gated on the
-- PMT_CAMERA_VERIFICATION -> PMT_CHECK_PENDING_MANAGER transition itself,
-- not a hardcoded workflow-code list, so any future workflow reusing this
-- same page picks this up automatically.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS pmt_loading_issue_detail (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 1,
    workflow_instance_id BIGINT NOT NULL,
    flow_variant_code VARCHAR(64) DEFAULT NULL,
    vehicle_no VARCHAR(32) DEFAULT NULL,
    order_no VARCHAR(64) DEFAULT NULL,
    rd_name VARCHAR(64) DEFAULT NULL,
    rd_no VARCHAR(64) DEFAULT NULL,
    sku_code VARCHAR(32) DEFAULT NULL,
    loading_issue VARCHAR(32) DEFAULT NULL COMMENT 'SHORTAGE | EXCESS | DAMAGE | WRONG_SKU | SEAL_TAMPERED | OTHERS',
    box_qty DECIMAL(12,3) DEFAULT NULL,
    pcs_qty DECIMAL(12,3) DEFAULT NULL,
    checking_officer_remarks VARCHAR(512) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_pmt_loading_issue_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO entity_registry (code, display_name, table_name)
SELECT 'PmtLoadingIssueDetail', 'PMT Loading Issue Detail', 'pmt_loading_issue_detail'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'PmtLoadingIssueDetail');
