package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.model.WorkflowInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** billing_entry.billing_documents: several uploaded files persisted as a real JSON array. */
class WorkflowProjectionListenerBillingDocumentsTest {

    private JdbcTemplate jdbc;
    private WorkflowProjectionListener listener;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        listener = new WorkflowProjectionListener(jdbc);
        TenantContext.set(1L);
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("target_table", "billing_entry");
        cfg.put("key_column", "workflow_instance_id");
        cfg.put("state_columns", "");
        cfg.put("field_map_json", "{\"billing_documents\":[\"billing_documents\",\"billingDocuments\"]}");
        when(jdbc.queryForList(contains("FROM m0_workflow_projection"), any(Object[].class))).thenReturn(List.of(cfg));
        when(jdbc.queryForObject(contains("SELECT COUNT(*)"), eq(Integer.class), any(Object[].class))).thenReturn(1);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private Object[] updateArgs() {
        return mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update")).findFirst().orElseThrow().getArguments();
    }

    private WorkflowInstance inst() {
        return WorkflowInstance.builder().id(7L).tenantId(1L).workflowCode("M10.TankerOutbound").currentState("BILLING_DONE").build();
    }

    @Test
    void multipleUploadedFiles_savedAsJsonArrayOfObjects() {
        Map<String, Object> f1 = new HashMap<>(Map.of("url", "/uploads/a.pdf", "doc_no", "DOC-1", "sha256", "aa", "size_kb", 12));
        Map<String, Object> f2 = new HashMap<>(Map.of("url", "/uploads/b.jpg", "doc_no", "DOC-2", "sha256", "bb", "size_kb", 40));
        Map<String, Object> bag = Map.of("aria:billing-entry", Map.of("billing_documents", List.of(f1, f2)));

        listener.onTransition(inst(), "WEIGHMENT_2", bag);

        Object[] a = updateArgs();
        assertThat((String) a[0]).contains("billing_documents");
        String json = (String) List.of(a).stream().filter(o -> o instanceof String s && s.startsWith("[")).findFirst().orElseThrow();
        assertThat(json).contains("\"url\":\"/uploads/a.pdf\"").contains("\"url\":\"/uploads/b.jpg\"")
                .contains("\"doc_no\":\"DOC-1\"").contains("\"doc_no\":\"DOC-2\"");
    }

    @Test
    void billingCheckList_multipleFiles_savedAsJsonArray_besideBillingDocuments() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("target_table", "billing_entry");
        cfg.put("key_column", "workflow_instance_id");
        cfg.put("state_columns", "");
        cfg.put("field_map_json", "{\"billing_documents\":[\"billing_documents\"],\"billing_check_list\":[\"billing_check_list\",\"billingCheckList\"]}");
        when(jdbc.queryForList(contains("FROM m0_workflow_projection"), any(Object[].class))).thenReturn(List.of(cfg));
        Map<String, Object> c1 = new HashMap<>(Map.of("url", "/uploads/c1.pdf", "doc_no", "DOC-31"));
        Map<String, Object> c2 = new HashMap<>(Map.of("url", "/uploads/c2.jpg", "doc_no", "DOC-32"));
        Map<String, Object> page = new HashMap<>();
        page.put("billing_check_list", List.of(c1, c2));
        page.put("billing_documents", List.of(new HashMap<>(Map.of("url", "/uploads/b.pdf", "doc_no", "DOC-33"))));

        listener.onTransition(inst(), "WEIGHMENT_2", Map.of("aria:billing-entry", page));

        Object[] a = updateArgs();
        assertThat((String) a[0]).contains("billing_check_list").contains("billing_documents");
        List<String> arrays = List.of(a).stream().filter(o -> o instanceof String s && s.startsWith("[")).map(String.class::cast).toList();
        assertThat(arrays).anyMatch(j -> j.contains("/uploads/c1.pdf") && j.contains("/uploads/c2.jpg") && j.contains("\"doc_no\":\"DOC-32\""));
        assertThat(arrays).anyMatch(j -> j.contains("/uploads/b.pdf") && !j.contains("c1.pdf"));
    }

    @Test
    void qcOfficerFinalFiles_savedAsJsonArrayOnItsOwnTable() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("target_table", "qc_officer_final_check_detail");
        cfg.put("key_column", "workflow_instance_id");
        cfg.put("state_columns", "current_step");
        cfg.put("field_map_json", "{\"qc_final_files\":[\"qc_final_files\"],\"qc_final_remarks\":[\"qc_final_remarks\"]}");
        when(jdbc.queryForList(contains("FROM m0_workflow_projection"), any(Object[].class))).thenReturn(List.of(cfg));
        Map<String, Object> f1 = new HashMap<>(Map.of("url", "/uploads/p1.jpg", "doc_no", "DOC-9"));
        Map<String, Object> f2 = new HashMap<>(Map.of("url", "/uploads/p2.pdf", "doc_no", "DOC-10"));
        Map<String, Object> page = new HashMap<>();
        page.put("qc_final_files", List.of(f1, f2));
        page.put("qc_final_remarks", "all good");

        listener.onTransition(inst(), "BILLING_DONE", Map.of("aria:qc-officer-final-check", page));

        Object[] a = updateArgs();
        assertThat((String) a[0]).contains("qc_officer_final_check_detail").contains("qc_final_files").contains("qc_final_remarks");
        String json = (String) List.of(a).stream().filter(o -> o instanceof String s && s.startsWith("[")).findFirst().orElseThrow();
        assertThat(json).contains("/uploads/p1.jpg").contains("/uploads/p2.pdf").contains("\"doc_no\":\"DOC-10\"");
        assertThat(List.of(a)).contains("all good");
    }

    @Test
    void documentVerificationAttachments_savedAsJsonArrayOnItsOwnTable() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("target_table", "document_verification_detail");
        cfg.put("key_column", "workflow_instance_id");
        cfg.put("state_columns", "current_step");
        cfg.put("page_code", "aria:document-verification");
        cfg.put("field_map_json", "{\"verification_documents\":[\"verification_documents\",\"verificationDocuments\"],\"grn_number\":[\"grn_number\"]}");
        when(jdbc.queryForList(contains("FROM m0_workflow_projection"), any(Object[].class))).thenReturn(List.of(cfg));
        Map<String, Object> f1 = new HashMap<>(Map.of("url", "/uploads/inv.pdf", "doc_no", "DOC-21"));
        Map<String, Object> f2 = new HashMap<>(Map.of("url", "/uploads/seal.jpg", "doc_no", "DOC-22"));
        Map<String, Object> page = new HashMap<>();
        page.put("verification_documents", List.of(f1, f2));
        page.put("grn_number", "GRN-77");
        WorkflowInstance inbound = WorkflowInstance.builder().id(7L).tenantId(1L).workflowCode("M10.TankerInbound").currentState("QTY_RECONCILED").build();

        listener.onTransition(inbound, "DOC_VERIFICATION_PENDING", Map.of("aria:document-verification", page));

        Object[] a = updateArgs();
        assertThat((String) a[0]).contains("document_verification_detail").contains("verification_documents").contains("grn_number");
        String json = (String) List.of(a).stream().filter(o -> o instanceof String s && s.startsWith("[")).findFirst().orElseThrow();
        assertThat(json).contains("/uploads/inv.pdf").contains("/uploads/seal.jpg").contains("\"doc_no\":\"DOC-22\"");
        assertThat(List.of(a)).contains("GRN-77");
    }

    @Test
    void noDocumentsSubmitted_columnNotWritten() {
        listener.onTransition(inst(), "WEIGHMENT_2", Map.of("aria:billing-entry", Map.of("other", "x")));
        verify(jdbc, never()).update(contains("billing_documents"), any(Object[].class));
    }
}
