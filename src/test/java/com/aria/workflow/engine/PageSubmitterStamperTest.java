package com.aria.workflow.engine;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PageSubmitterStamperTest {

    private JdbcTemplate jdbc;
    private PageSubmitterStamper stamper;
    private final WorkflowInstance inst = WorkflowInstance.builder().id(2028L).workflowCode("M10.TankerOutbound").currentState("X").build();

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        stamper = new PageSubmitterStamper(jdbc);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private void mapRow(String page, String table, String userColumn) {
        when(jdbc.queryForList(contains("FROM page_submitter_map"), eq(page)))
                .thenReturn(List.of(Map.of("table_name", table, "key_column", "workflow_instance_id", "user_column", userColumn)));
    }

    private void mapRowFull(String page, String table, String userColumn, String nameColumn, String contactColumn) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("table_name", table);
        m.put("key_column", "workflow_instance_id");
        m.put("user_column", userColumn);
        m.put("name_column", nameColumn);
        m.put("contact_column", contactColumn);
        when(jdbc.queryForList(contains("FROM page_submitter_map"), eq(page))).thenReturn(List.of(m));
    }

    @Test
    void performedByName_typedOnThePage_isSavedBesideTheLoginId() {
        mapRowFull("aria:manager-qty-approval", "manager_qty_approval_detail", "submitted_by_id", "performed_by_name", null);

        // logged in as 77, but the form says Ravi did the work
        stamper.stamp(inst, "WEIGHMENT_2", "aria:manager-qty-approval", 77L, Map.of("performed_by_name", "  Ravi Kumar "));

        verify(jdbc).update("UPDATE manager_qty_approval_detail SET submitted_by_id = ?, performed_by_name = ? WHERE tenant_id = ? AND workflow_instance_id = ?",
                77L, "Ravi Kumar", 1L, 2028L);
    }

    @Test
    void weighment_eachWeightKeepsItsOwnOfficerNameAndContact() {
        mapRowFull("aria:weighment-capture", "weighment_capture_detail", "w#_by_id", "w#_by_name", "w#_contact");

        stamper.stamp(inst, "QC_PRELOAD_APPROVED", "aria:weighment-capture", 11L,
                Map.of("weight_seq", "FIRST", "weight_officer", "Anand", "weight_officer_contact", "9000000001"));
        stamper.stamp(inst, "QC_SAMPLE_APPROVED", "aria:weighment-capture", 22L,
                Map.of("weight_seq", "SECOND", "performed_by_name", "Bala", "weight_officer", "ignored", "weight_officer_contact", "9000000002"));
        stamper.stamp(inst, "DISPATCH_VERIFIED", "aria:weighment-capture", 33L, Map.of("weight_seq", "THIRD", "weight_officer", "Chitra"));

        verify(jdbc).update("UPDATE weighment_capture_detail SET w1_by_id = ?, w1_by_name = ?, w1_contact = ? WHERE tenant_id = ? AND workflow_instance_id = ?",
                11L, "Anand", "9000000001", 1L, 2028L);
        // performed_by_name wins over the page's own officer field
        verify(jdbc).update("UPDATE weighment_capture_detail SET w2_by_id = ?, w2_by_name = ?, w2_contact = ? WHERE tenant_id = ? AND workflow_instance_id = ?",
                22L, "Bala", "9000000002", 1L, 2028L);
        // no contact typed -> that column is left alone
        verify(jdbc).update("UPDATE weighment_capture_detail SET w3_by_id = ?, w3_by_name = ? WHERE tenant_id = ? AND workflow_instance_id = ?",
                33L, "Chitra", 1L, 2028L);
    }

    @Test
    void otherVehicleWeighment_firstAndSecondWeight_byWeightSeqOrByState() {
        mapRowFull("aria:weighment-capture-other-vehicle", "weighment_capture_detail", "w#_by_id", "w#_by_name", "w#_contact");

        stamper.stamp(inst, "DRAFT", "aria:weighment-capture-other-vehicle", 11L, Map.of("weight_seq", "FIRST", "weight_officer", "Anand"));
        // second weight with no weight_seq in the form -> decided by the state it was submitted from
        stamper.stamp(inst, "LOADING_UNLOADING_IN_PROGRESS", "aria:weighment-capture-other-vehicle", 22L, Map.of("weight_officer", "Bala"));

        verify(jdbc).update("UPDATE weighment_capture_detail SET w1_by_id = ?, w1_by_name = ? WHERE tenant_id = ? AND workflow_instance_id = ?", 11L, "Anand", 1L, 2028L);
        verify(jdbc).update("UPDATE weighment_capture_detail SET w2_by_id = ?, w2_by_name = ? WHERE tenant_id = ? AND workflow_instance_id = ?", 22L, "Bala", 1L, 2028L);
    }

    @Test
    void stampsTheLoggedInUserOnThePagesOwnTable() {
        mapRow("aria:billing-entry", "billing_entry", "submitted_by_id");

        stamper.stamp(inst, "WEIGHT_DIFF_OK", "aria:billing-entry", 77L, Map.of());

        verify(jdbc).update("UPDATE billing_entry SET submitted_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ?",
                77L, 1L, 2028L);
    }

    @Test
    void weighmentCapture_firstSecondThirdGoToTheirOwnColumns_fromWeightSeq() {
        mapRow("aria:weighment-capture", "weighment_capture_detail", "w#_by_id");

        stamper.stamp(inst, "QC_PRELOAD_APPROVED", "aria:weighment-capture", 11L, Map.of("weight_seq", "FIRST"));
        stamper.stamp(inst, "QC_SAMPLE_APPROVED", "aria:weighment-capture", 22L, Map.of("weight_seq", "SECOND"));
        stamper.stamp(inst, "DISPATCH_VERIFIED", "aria:weighment-capture", 33L, Map.of("weight_seq", "THIRD"));

        verify(jdbc).update("UPDATE weighment_capture_detail SET w1_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ?", 11L, 1L, 2028L);
        verify(jdbc).update("UPDATE weighment_capture_detail SET w2_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ?", 22L, 1L, 2028L);
        verify(jdbc).update("UPDATE weighment_capture_detail SET w3_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ?", 33L, 1L, 2028L);
    }

    @Test
    void weighmentCapture_noWeightSeqInForm_fallsBackToThePreviousState() {
        mapRow("aria:weighment-capture", "weighment_capture_detail", "w#_by_id");

        stamper.stamp(inst, "UNLOADING_WEIGHMENT", "aria:weighment-capture", 22L, Map.of());
        stamper.stamp(inst, "SOME_UNKNOWN_STATE", "aria:weighment-capture", 99L, Map.of());   // unknown -> skipped

        verify(jdbc).update("UPDATE weighment_capture_detail SET w2_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ?", 22L, 1L, 2028L);
        verify(jdbc, times(1)).update(startsWith("UPDATE weighment_capture_detail"), any(Object[].class));
    }

    @Test
    void noUser_unmappedPage_unsafeIdentifier_orFailure_neverThrowOrWrite() {
        // no user
        stamper.stamp(inst, "A", "aria:billing-entry", null, Map.of());
        verifyNoInteractions(jdbc);

        // unmapped page -> only the lookup
        when(jdbc.queryForList(contains("FROM page_submitter_map"), eq("aria:nope"))).thenReturn(List.of());
        stamper.stamp(inst, "A", "aria:nope", 5L, Map.of());
        verify(jdbc, never()).update(anyString(), any(Object[].class));

        // unsafe identifier in the config row -> refused
        mapRow("aria:evil", "billing_entry; DROP TABLE x", "submitted_by_id");
        stamper.stamp(inst, "A", "aria:evil", 5L, Map.of());
        verify(jdbc, never()).update(anyString(), any(Object[].class));

        // a DB failure is swallowed
        mapRow("aria:billing-entry", "billing_entry", "submitted_by_id");
        when(jdbc.update(anyString(), any(Object[].class))).thenThrow(new RuntimeException("db down"));
        stamper.stamp(inst, "A", "aria:billing-entry", 5L, Map.of());
    }
}
