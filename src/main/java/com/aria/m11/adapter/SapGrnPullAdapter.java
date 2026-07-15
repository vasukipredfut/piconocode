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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SAP GRN pull connector — fetches a draft GRN payload from SAP S/4HANA's
 * material-document API and returns it as a normalized JSON the M5 inward
 * page can consume.
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code pullGrn} — payload {sapDocNo, sapPlant}; calls SAP
 *       {@code /sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader('docNo')}
 *       and returns {grnNumber, vendor, lines[]}.</li>
 *   <li>{@code pushAck} — payload {sapDocNo, grnNumber}; posts back to SAP
 *       that the GRN was created on the EARMS side (idempotent).</li>
 * </ul>
 *
 * <p>Config: {@code { "baseUrl": "https://sap.example.com",
 *   "username": "EARMS_INT", "dryRun": true }}.
 * dryRun=true returns a synthetic GRN payload — lets the inward chooser
 * page drive an end-to-end test without a real SAP system.
 */
@Component
public class SapGrnPullAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(SapGrnPullAdapter.class);
    public static final String CODE = "SAP_S4_OUTBOUND";

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
            String baseUrl = String.valueOf(config.getOrDefault("baseUrl", "https://sap.example.com"));
            try {
                ResponseEntity<String> resp = RestClient.builder().baseUrl(baseUrl).build()
                        .get().uri("/sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/")
                        .retrieve().toEntity(String.class);
                return PingResult.builder().ok(true).message("HTTP " + resp.getStatusCode().value())
                        .httpStatus(resp.getStatusCode().value()).build();
            } catch (RestClientResponseException server) {
                int code = server.getStatusCode().value();
                return PingResult.builder().ok(code != 401 && code != 403)
                        .message("HTTP " + code).httpStatus(code).build();
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
                case "pullGrn" -> pullGrn(payload, config, secrets, dryRun);
                case "pushAck" -> pushAck(payload, config, secrets, dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "SAP supports pullGrn and pushAck; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("SAP invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ---- internal ----------------------------------------------------------

    private OutboundResult pullGrn(Map<String, Object> p, Map<String, Object> config,
                                   Map<String, Object> secrets, boolean dryRun) throws IOException {
        Object docNo = p.get("sapDocNo");
        if (docNo == null) return OutboundResult.failed("bad_request", "payload.sapDocNo required");
        if (dryRun) {
            String grnNo = "GRN-SAP-" + System.nanoTime() % 1_000_000;
            Map<String, Object> body = new HashMap<>();
            body.put("dryRun", true);
            body.put("sapDocNo", docNo);
            body.put("sapPlant", p.getOrDefault("sapPlant", "PICO-CBE"));
            body.put("suggestedGrnNumber", grnNo);
            body.put("vendor", "TESTRO Industries (auto-pulled)");
            body.put("lines", List.of(
                    Map.of("itemCode", "OIL-SUN-RAW", "qty", 25000.0, "uom", "KG",
                            "lotNoSuggested", "LOT-SAP-" + UUID.randomUUID().toString().substring(0, 8)),
                    Map.of("itemCode", "OIL-GND-RAW", "qty", 15000.0, "uom", "KG",
                            "lotNoSuggested", "LOT-SAP-" + UUID.randomUUID().toString().substring(0, 8))
            ));
            log.info("SAP DRY-RUN pullGrn doc={} -> {}", docNo, grnNo);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(body)).build();
        }
        if (secrets == null || !secrets.containsKey("username") || !secrets.containsKey("password")) {
            return OutboundResult.failed("sap_creds_missing",
                    "Set instance secrets username + password to call SAP live");
        }
        // Real-call path structured but unwired
        return OutboundResult.failed("not_implemented_live",
                "Live SAP call structured but unwired; flip dryRun=true OR provision SAP creds");
    }

    private OutboundResult pushAck(Map<String, Object> p, Map<String, Object> config,
                                    Map<String, Object> secrets, boolean dryRun) throws IOException {
        Object docNo = p.get("sapDocNo");
        Object grnNo = p.get("grnNumber");
        if (docNo == null) return OutboundResult.failed("bad_request", "payload.sapDocNo required");
        if (grnNo == null) return OutboundResult.failed("bad_request", "payload.grnNumber required");
        if (dryRun) {
            log.info("SAP DRY-RUN pushAck doc={} grn={}", docNo, grnNo);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(Map.of("acknowledged", true, "sapDocNo", docNo, "grnNumber", grnNo)))
                    .build();
        }
        return OutboundResult.failed("not_implemented_live", "Live SAP ack unwired");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
