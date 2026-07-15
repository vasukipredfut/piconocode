package com.aria.audit;

import com.aria.workflow.model.WorkflowInstance;
import com.aria.integration.adapter.EmailImapInboundAdapter;
import com.aria.integration.adapter.ExcelBulkImportAdapter;
import com.aria.integration.adapter.ModbusTcpAdapter;
import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.model.IntegrationInstance;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Round-9 components:
 *   - AuditTrailListener      writes one row per workflow transition
 *   - ModbusTcpAdapter         dryRun read/write of registers & coils
 *   - EmailImapInboundAdapter  dryRun poll + markRead
 *   - ExcelBulkImportAdapter   validate + import with dryRun
 */
class AuditAndAdaptersTest {

    // ---- AuditTrailListener -------------------------------------------

    @Test
    void auditTrail_writes_one_row_per_transition() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        AuditTrailListener listener = new AuditTrailListener(jdbc);
        WorkflowInstance wi = new WorkflowInstance();
        wi.setId(42L);
        wi.setTenantId(1L);
        wi.setWorkflowCode("M5.MaterialRequisition");
        wi.setCurrentState("APPROVED");
        listener.onTransition(wi, "SUBMITTED",
                Map.of("pico:mr-approve", Map.of("status", "APPROVED", "reason", "OK")));
        List<String> sqls = mockingDetails(jdbc).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals("update"))
                .map(i -> i.getArgument(0).toString())
                .collect(Collectors.toList());
        assertThat(sqls).anyMatch(s -> s.contains("INSERT INTO m0_audit_log"));
    }

    @Test
    void auditTrail_failure_isSwallowed() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        doThrow(new RuntimeException("DB down")).when(jdbc).update(anyString(), (Object[]) any());
        AuditTrailListener listener = new AuditTrailListener(jdbc);
        WorkflowInstance wi = new WorkflowInstance();
        wi.setId(43L);
        wi.setWorkflowCode("M10.TankerInbound");
        wi.setCurrentState("CLOSED");
        // Must not throw
        listener.onTransition(wi, "WEIGHMENT_RECHECK", Map.of());
    }

    // ---- ModbusTcpAdapter ---------------------------------------------

    @Test
    void modbus_connectorCode() {
        ModbusTcpAdapter m = new ModbusTcpAdapter();
        assertThat(m.connectorCode()).isEqualTo("MODBUS_TCP");
        assertThat(m.supportsOutbound()).isTrue();
    }

    @Test
    void modbus_readHolding_dryRun_returns_synthetic_values() {
        OutboundResult r = new ModbusTcpAdapter().invoke(inst("{\"dryRun\":true}"),
                "readHoldingRegisters", Map.of("unitId", 1, "address", 100, "count", 4), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("HOLDING").contains("1000").contains("1003");
    }

    @Test
    void modbus_writeCoil_dryRun_echoes_value() {
        OutboundResult r = new ModbusTcpAdapter().invoke(inst("{\"dryRun\":true}"),
                "writeSingleCoil", Map.of("unitId", 1, "address", 50, "value", true), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("\"value\":true").contains("COIL");
    }

    @Test
    void modbus_missingAddress_isBadRequest() {
        OutboundResult r = new ModbusTcpAdapter().invoke(inst("{\"dryRun\":true}"),
                "readHoldingRegisters", Map.of("unitId", 1), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
    }

    // ---- EmailImapInboundAdapter --------------------------------------

    @Test
    void email_poll_dryRun_returns_2_synthetic_messages() {
        OutboundResult r = new EmailImapInboundAdapter().invoke(inst("{\"dryRun\":true,\"folder\":\"INBOX\"}"),
                "poll", Map.of(), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("Invoice INV-99876")
                              .contains("sunflower oil")
                              .contains("suggestedRoute");
    }

    @Test
    void email_markRead_dryRun_ok() {
        OutboundResult r = new EmailImapInboundAdapter().invoke(inst("{\"dryRun\":true}"),
                "markRead", Map.of("uid", 1001), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("READ");
    }

    // ---- ExcelBulkImportAdapter ---------------------------------------

    @Test
    void bulkImport_validate_returns_inferred_types_for_csv() {
        String csv = "item_code,qty,date\nOIL-X,100,2026-06-08\nOIL-Y,200,2026-06-09\n";
        OutboundResult r = new ExcelBulkImportAdapter().invoke(inst("{}"),
                "validate", Map.of("entity", "Vendor", "csvContent", csv), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("\"qty\":\"DECIMAL\"")
                              .contains("\"date\":\"DATE\"")
                              .contains("\"item_code\":\"STRING\"")
                              .contains("\"rowCount\":2");
    }

    @Test
    void bulkImport_import_dryRun_inserts_zero() {
        String csv = "name\nABC\nDEF\n";
        OutboundResult r = new ExcelBulkImportAdapter().invoke(inst("{}"),
                "import", Map.of("entity", "Vendor", "csvContent", csv, "dryRun", true), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("\"rowCount\":2").contains("\"inserted\":0");
    }

    @Test
    void bulkImport_missingEntity_isBadRequest() {
        OutboundResult r = new ExcelBulkImportAdapter().invoke(inst("{}"),
                "import", Map.of("csvContent", "a\nb"), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
    }

    private IntegrationInstance inst(String configJson) {
        IntegrationInstance i = new IntegrationInstance();
        i.setTenantId(1L);
        i.setConfigJson(configJson);
        return i;
    }
}
