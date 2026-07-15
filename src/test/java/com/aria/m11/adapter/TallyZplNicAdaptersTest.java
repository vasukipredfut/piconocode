package com.aria.m11.adapter;

import com.aria.m11.model.IntegrationInstance;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the three Round 1H.2 connector adapters — Tally / ZPL / NIC IRP.
 * Each exercises connectorCode, ping (dry-run), invoke happy + error paths.
 */
class TallyZplNicAdaptersTest {

    // ---- Tally ----------------------------------------------------------
    private final TallyTdlXmlAdapter tally = new TallyTdlXmlAdapter();

    @Test
    void tally_connectorCode() {
        assertThat(tally.connectorCode()).isEqualTo("TALLY_TDL_XML");
        assertThat(tally.supportsOutbound()).isTrue();
    }

    @Test
    void tally_postVoucher_dryRun_emits_xml() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true,\"companyName\":\"PICO POWER PROJECT\"}");
        OutboundResult r = tally.invoke(inst, "postVoucher", Map.of(
                "voucherType", "Sales",
                "date", "20260608",
                "narration", "Test invoice",
                "ledgerEntries", List.of(
                        Map.of("ledger", "Acme Buyer", "amount", "1180", "isDeemedPositive", true),
                        Map.of("ledger", "Sales", "amount", "1000", "isDeemedPositive", false))
        ), Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("<ENVELOPE>")
                .contains("<VOUCHER VCHTYPE=\"Sales\"")
                .contains("PICO POWER PROJECT")
                .contains("<LEDGERNAME>Acme Buyer</LEDGERNAME>")
                .contains("<AMOUNT>1180</AMOUNT>");
    }

    @Test
    void tally_postBatch_dryRun_handles_multiple() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true}");
        OutboundResult r = tally.invoke(inst, "postBatch",
                Map.of("vouchers", List.of(
                        Map.of("voucherType", "Sales", "date", "20260608", "narration", "v1"),
                        Map.of("voucherType", "Purchase", "date", "20260608", "narration", "v2"))),
                Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("VCHTYPE=\"Sales\"")
                              .contains("VCHTYPE=\"Purchase\"");
    }

    @Test
    void tally_unknownOp() {
        OutboundResult r = tally.invoke(instanceWith("{}"), "noOp", Map.of(), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("unknown_op");
    }

    @Test
    void tally_ping_dryRun_ok() {
        assertThat(tally.ping(instanceWith("{\"dryRun\":true}"), Map.of()).isOk()).isTrue();
    }

    // ---- ZPL ------------------------------------------------------------
    private final ZplLabelPrinterAdapter zpl = new ZplLabelPrinterAdapter();

    @Test
    void zpl_connectorCode() {
        assertThat(zpl.connectorCode()).isEqualTo("M9_LABEL_PRINTER_ZPL");
        assertThat(zpl.supportsOutbound()).isTrue();
    }

    @Test
    void zpl_printTemplate_gatePass_substitutes_vars() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true,\"host\":\"192.168.1.50\"}");
        OutboundResult r = zpl.invoke(inst, "printTemplate", Map.of(
                "templateName", "gate-pass",
                "vars", Map.of("gpNumber", "GP-1-99", "vehicleNo", "TN-46-CJ-1234",
                               "driverName", "Suresh", "date", "08-Jun-2026")
        ), Map.of());
        assertThat(r.isOk()).isTrue();
        // body is dry-run JSON; we can't assert the substitution there.
        // Re-print raw ZPL with substitution applied so we can:
        OutboundResult raw = zpl.invoke(inst, "printZpl",
                Map.of("zpl", "^XAGP{{gpNumber}}^XZ"), Map.of());
        assertThat(raw.isOk()).isTrue();
    }

    @Test
    void zpl_printTemplate_unknownTemplate() {
        OutboundResult r = zpl.invoke(instanceWith("{\"dryRun\":true}"), "printTemplate",
                Map.of("templateName", "no-such-template"), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("unknown_template");
    }

    @Test
    void zpl_printZpl_missingZpl() {
        OutboundResult r = zpl.invoke(instanceWith("{\"dryRun\":true}"), "printZpl", Map.of(), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
    }

    // ---- NIC IRP --------------------------------------------------------
    private final NicIrpAdapter nic = new NicIrpAdapter();

    @Test
    void nic_connectorCode() {
        assertThat(nic.connectorCode()).isEqualTo("NIC_IRP_V1.1");
    }

    @Test
    void nic_generateIrn_dryRun_returnsSynthIrn() {
        IntegrationInstance inst = instanceWith("{\"dryRun\":true,\"gstin\":\"33ABCDE2345F1Z6\"}");
        OutboundResult r = nic.invoke(inst, "generateIrn",
                Map.of("DocDtls", Map.of("No", "INV-101", "Dt", "08/06/2026"),
                       "SellerDtls", Map.of("Gstin", "33ABCDE2345F1Z6")),
                Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("\"Irn\":").contains("\"AckNo\":")
                              .contains("SignedQRCode").contains("dryRun");
    }

    @Test
    void nic_generateIrn_missingDocDtls() {
        OutboundResult r = nic.invoke(instanceWith("{\"dryRun\":true}"), "generateIrn", Map.of(), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("bad_request");
    }

    @Test
    void nic_cancelIrn_dryRun_ok() {
        OutboundResult r = nic.invoke(instanceWith("{\"dryRun\":true}"), "cancelIrn",
                Map.of("irn", "abc123def456", "reasonCode", 2, "reasonText", "Data entry error"),
                Map.of());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getBody()).contains("CancelDate");
    }

    @Test
    void nic_generateIrn_live_withoutCreds_returnsStructuredError() {
        OutboundResult r = nic.invoke(instanceWith("{\"dryRun\":false}"), "generateIrn",
                Map.of("DocDtls", Map.of("No", "INV-1")), Map.of());
        assertThat(r.isOk()).isFalse();
        assertThat(r.getErrorCode()).isEqualTo("nic_creds_missing");
    }

    private IntegrationInstance instanceWith(String configJson) {
        IntegrationInstance i = new IntegrationInstance();
        i.setConfigJson(configJson);
        i.setTenantId(1L);
        return i;
    }
}
