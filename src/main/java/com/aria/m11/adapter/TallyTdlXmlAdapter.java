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

/**
 * Tally TDL XML connector — posts vouchers to a Tally listener over HTTP.
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code postVoucher} — payload {voucherType, date, narration, ledgerEntries[]}
 *       wrapped in Tally's ENVELOPE/HEADER/BODY/IMPORTDATA XML and POSTed
 *       to {@code config.baseUrl} (default {@code http://localhost:9000}).</li>
 *   <li>{@code postBatch} — payload {vouchers: [...]} sends N voucher
 *       tags in one envelope (Tally's batch import).</li>
 * </ul>
 *
 * <p>Config:
 * <pre>{@code
 * { "baseUrl": "http://localhost:9000", "companyName": "PICO POWER PROJECT",
 *   "dryRun": true }
 * }</pre>
 *
 * <p>dryRun=true logs the generated XML and returns ok without making a
 * network call — lets the workflow drive end-to-end without a real
 * Tally listener running. No secrets required (Tally HTTP is local LAN).
 */
@Component
public class TallyTdlXmlAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(TallyTdlXmlAdapter.class);
    public static final String CODE = "TALLY_TDL_XML";
    private static final String DEFAULT_BASE_URL = "http://localhost:9000";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        long t0 = System.nanoTime();
        try {
            Map<String, Object> config = readConfig(instance);
            if (Boolean.TRUE.equals(config.get("dryRun"))) {
                long ms = (System.nanoTime() - t0) / 1_000_000;
                return PingResult.builder().ok(true).message("dryRun mode").latencyMs(ms).build();
            }
            String baseUrl = String.valueOf(config.getOrDefault("baseUrl", DEFAULT_BASE_URL));
            RestClient client = RestClient.builder().baseUrl(baseUrl).build();
            try {
                ResponseEntity<String> resp = client.get().uri("/").retrieve().toEntity(String.class);
                long ms = (System.nanoTime() - t0) / 1_000_000;
                return PingResult.builder().ok(true).message("HTTP " + resp.getStatusCode().value())
                        .httpStatus(resp.getStatusCode().value()).latencyMs(ms).build();
            } catch (RestClientResponseException server) {
                long ms = (System.nanoTime() - t0) / 1_000_000;
                int code = server.getStatusCode().value();
                return PingResult.builder().ok(code < 500).message("HTTP " + code)
                        .httpStatus(code).latencyMs(ms).build();
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
            String baseUrl = String.valueOf(config.getOrDefault("baseUrl", DEFAULT_BASE_URL));
            String company = String.valueOf(config.getOrDefault("companyName", "Default Company"));
            boolean dryRun = Boolean.TRUE.equals(config.get("dryRun"));
            return switch (operation == null ? "" : operation.trim()) {
                case "postVoucher" -> postVoucher(payload, baseUrl, company, dryRun);
                case "postBatch" -> postBatch(payload, baseUrl, company, dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "Tally supports postVoucher and postBatch; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("Tally invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ---- internal ----------------------------------------------------------

    private OutboundResult postVoucher(Map<String, Object> p, String baseUrl, String company, boolean dryRun) {
        String xml = buildVoucherEnvelope(company, List.of(p));
        if (dryRun) {
            log.info("Tally DRY-RUN postVoucher company={} bytes={}", company, xml.length());
            return OutboundResult.builder().ok(true).status(200).body(xml).build();
        }
        return doPost(baseUrl, xml);
    }

    @SuppressWarnings("unchecked")
    private OutboundResult postBatch(Map<String, Object> p, String baseUrl, String company, boolean dryRun) {
        Object vs = p.get("vouchers");
        if (!(vs instanceof List<?>)) {
            return OutboundResult.failed("bad_request", "payload.vouchers (list) required");
        }
        List<Map<String, Object>> vouchers = (List<Map<String, Object>>) vs;
        String xml = buildVoucherEnvelope(company, vouchers);
        if (dryRun) {
            log.info("Tally DRY-RUN postBatch company={} count={} bytes={}", company, vouchers.size(), xml.length());
            return OutboundResult.builder().ok(true).status(200).body(xml).build();
        }
        return doPost(baseUrl, xml);
    }

    /**
     * Builds a Tally Import-Data envelope wrapping N vouchers. Each voucher
     * map keys: voucherType, date (YYYYMMDD), narration, ledgerEntries
     * (list of {ledger, amount, isDeemedPositive}).
     */
    String buildVoucherEnvelope(String company, List<Map<String, Object>> vouchers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ENVELOPE>");
        sb.append("<HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>");
        sb.append("<BODY><IMPORTDATA>");
        sb.append("<REQUESTDESC><REPORTNAME>All Masters</REPORTNAME>");
        sb.append("<STATICVARIABLES><SVCURRENTCOMPANY>").append(esc(company)).append("</SVCURRENTCOMPANY></STATICVARIABLES>");
        sb.append("</REQUESTDESC>");
        sb.append("<REQUESTDATA>");
        for (Map<String, Object> v : vouchers) {
            sb.append(buildVoucher(v));
        }
        sb.append("</REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String buildVoucher(Map<String, Object> v) {
        String type = String.valueOf(v.getOrDefault("voucherType", "Sales"));
        String date = String.valueOf(v.getOrDefault("date", ""));
        String narration = String.valueOf(v.getOrDefault("narration", ""));
        StringBuilder sb = new StringBuilder();
        sb.append("<TALLYMESSAGE><VOUCHER VCHTYPE=\"").append(esc(type)).append("\" ACTION=\"Create\">");
        sb.append("<DATE>").append(esc(date)).append("</DATE>");
        sb.append("<NARRATION>").append(esc(narration)).append("</NARRATION>");
        sb.append("<VOUCHERTYPENAME>").append(esc(type)).append("</VOUCHERTYPENAME>");
        Object entries = v.get("ledgerEntries");
        if (entries instanceof List<?>) {
            for (Map<String, Object> e : (List<Map<String, Object>>) entries) {
                String ledger = String.valueOf(e.getOrDefault("ledger", ""));
                String amount = String.valueOf(e.getOrDefault("amount", "0"));
                boolean isPositive = Boolean.TRUE.equals(e.get("isDeemedPositive"));
                sb.append("<ALLLEDGERENTRIES.LIST>");
                sb.append("<LEDGERNAME>").append(esc(ledger)).append("</LEDGERNAME>");
                sb.append("<ISDEEMEDPOSITIVE>").append(isPositive ? "Yes" : "No").append("</ISDEEMEDPOSITIVE>");
                sb.append("<AMOUNT>").append(esc(amount)).append("</AMOUNT>");
                sb.append("</ALLLEDGERENTRIES.LIST>");
            }
        }
        sb.append("</VOUCHER></TALLYMESSAGE>");
        return sb.toString();
    }

    private OutboundResult doPost(String url, String xml) {
        RestClient client = RestClient.builder().build();
        try {
            ResponseEntity<String> resp = client.post().uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, "application/xml")
                    .body(xml).retrieve().toEntity(String.class);
            return OutboundResult.builder().ok(resp.getStatusCode().is2xxSuccessful())
                    .status(resp.getStatusCode().value()).body(resp.getBody()).build();
        } catch (RestClientResponseException server) {
            return OutboundResult.builder().ok(false).status(server.getStatusCode().value())
                    .errorCode("http_" + server.getStatusCode().value())
                    .errorMessage(server.getResponseBodyAsString()).build();
        } catch (Exception e) {
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
