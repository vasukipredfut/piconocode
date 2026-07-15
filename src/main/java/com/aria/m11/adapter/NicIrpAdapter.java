package com.aria.m11.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * NIC IRP (e-invoice) connector — generates IRN against the NIC Invoice
 * Registration Portal v1.1 schema.
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code generateIrn} — payload = the full IRP invoice JSON
 *       (TransactionDetails/DocumentDetails/SellerDetails/BuyerDetails/
 *       ValueDetails/ItemList). Returns {ackNo, irn, qrCode}.</li>
 *   <li>{@code cancelIrn} — payload {irn, reasonCode, reasonText}.
 *       24-hour window enforced by IRP; this adapter passes through.</li>
 * </ul>
 *
 * <p>Config: {@code { "baseUrl": "https://einv-apisandbox.nic.in",
 * "clientId": "...", "gstin": "33ABCDE2345F1Z6", "dryRun": true }}.
 *
 * <p>dryRun=true returns a synthesized IRN+QR without calling NIC — lets
 * the workflow drive end-to-end without burning real IRP credits.
 * Real-call path is structured but stubbed (returns 503 with a guidance
 * message until NIC sandbox credentials are wired into the secret store).
 */
@Component
public class NicIrpAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(NicIrpAdapter.class);
    public static final String CODE = "NIC_IRP_V1.1";
    private static final String DEFAULT_BASE_URL = "https://einv-apisandbox.nic.in";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        try {
            Map<String, Object> config = readConfig(instance);
            if (Boolean.TRUE.equals(config.get("dryRun"))) {
                return PingResult.builder().ok(true).message("dryRun mode").build();
            }
            String baseUrl = String.valueOf(config.getOrDefault("baseUrl", DEFAULT_BASE_URL));
            RestClient client = RestClient.builder().baseUrl(baseUrl).build();
            try {
                ResponseEntity<String> resp = client.get().uri("/eivital/v1.04/auth")
                        .retrieve().toEntity(String.class);
                int code = resp.getStatusCode().value();
                return PingResult.builder().ok(code < 500).message("HTTP " + code)
                        .httpStatus(code).build();
            } catch (RestClientResponseException server) {
                int code = server.getStatusCode().value();
                return PingResult.builder().ok(code != 401 && code != 403)
                        .message("HTTP " + code + " (auth " + (code == 401 || code == 403 ? "FAILED" : "ok") + ")")
                        .httpStatus(code).build();
            }
        } catch (Exception e) {
            return PingResult.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public OutboundResult invoke(IntegrationInstance instance, String operation,
                                  Map<String, Object> payload, Map<String, Object> secrets) {
        try {
            Map<String, Object> config = readConfig(instance);
            boolean dryRun = Boolean.TRUE.equals(config.get("dryRun"));
            String op = operation == null ? "" : operation.trim();
            return switch (op) {
                case "generateIrn" -> generateIrn(payload, config, secrets, dryRun);
                case "cancelIrn" -> cancelIrn(payload, config, secrets, dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "NIC IRP supports generateIrn and cancelIrn; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("NIC IRP invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ---- internal ----------------------------------------------------------

    private OutboundResult generateIrn(Map<String, Object> p, Map<String, Object> config,
                                       Map<String, Object> secrets, boolean dryRun) throws IOException {
        if (!p.containsKey("DocDtls")) {
            return OutboundResult.failed("bad_request", "payload.DocDtls required (IRP v1.1 invoice shape)");
        }
        if (dryRun) {
            // IRN is 64 hex chars per NIC spec — concat two UUIDs (32 hex each) to match length.
            String fakeIrn = (UUID.randomUUID().toString() + UUID.randomUUID().toString())
                    .replace("-", "").substring(0, 64);
            String fakeAck = "AWB" + System.nanoTime() % 1_000_000_000;
            log.info("NIC IRP DRY-RUN generateIrn DocNo={} -> IRN {}",
                    extractDocNo(p), fakeIrn);
            return OutboundResult.builder().ok(true).status(200).body(
                    mapper.writeValueAsString(Map.of(
                            "AckNo", fakeAck,
                            "AckDt", java.time.LocalDateTime.now().toString(),
                            "Irn", fakeIrn,
                            "SignedQRCode", "DRYRUN-QR-" + fakeIrn.substring(0, 16),
                            "dryRun", true))
            ).build();
        }
        // Real-call path — requires NIC sandbox auth + signed payload.
        // Returns a structured 503 until the secret store is loaded with
        // ClientId / ClientSecret / Username / Password.
        if (secrets == null || !secrets.containsKey("clientId") || !secrets.containsKey("clientSecret")) {
            return OutboundResult.failed("nic_creds_missing",
                    "Set instance secrets clientId + clientSecret + username + password to call NIC IRP live");
        }
        return OutboundResult.failed("not_implemented_live",
                "Live NIC IRP call is structured but unwired; flip dryRun=true OR provision sandbox creds");
    }

    private OutboundResult cancelIrn(Map<String, Object> p, Map<String, Object> config,
                                     Map<String, Object> secrets, boolean dryRun) throws IOException {
        Object irn = p.get("irn");
        Object reasonCode = p.get("reasonCode");
        if (irn == null) return OutboundResult.failed("bad_request", "payload.irn required");
        if (reasonCode == null) return OutboundResult.failed("bad_request", "payload.reasonCode required (1-4)");
        if (dryRun) {
            log.info("NIC IRP DRY-RUN cancelIrn irn={} reason={}", irn, reasonCode);
            return OutboundResult.builder().ok(true).status(200).body(
                    mapper.writeValueAsString(Map.of(
                            "Irn", irn,
                            "CancelDate", java.time.LocalDateTime.now().toString(),
                            "dryRun", true))
            ).build();
        }
        return OutboundResult.failed("not_implemented_live",
                "Live NIC IRP cancel is structured but unwired; flip dryRun=true OR provision sandbox creds");
    }

    @SuppressWarnings("unchecked")
    private String extractDocNo(Map<String, Object> p) {
        Object doc = p.get("DocDtls");
        if (doc instanceof Map<?, ?> m) {
            Object no = ((Map<String, Object>) m).get("No");
            if (no != null) return String.valueOf(no);
        }
        return "?";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
