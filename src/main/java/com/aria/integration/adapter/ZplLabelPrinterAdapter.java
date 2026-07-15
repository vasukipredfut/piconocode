package com.aria.integration.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.integration.model.IntegrationInstance;
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

/**
 * Zebra ZPL label-printer connector — POSTs ZPL II strings to a printer's
 * HTTP endpoint (port 9100 / printer web UI).
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code printZpl} — payload {zpl} sends raw ZPL II to the printer.</li>
 *   <li>{@code printTemplate} — payload {templateName, vars} renders a
 *       classpath ZPL template (variables substituted as {{key}}) and prints.</li>
 * </ul>
 *
 * <p>Config: {@code { "host": "192.168.1.50", "port": 9100, "dryRun": true }}.
 * dryRun=true logs the ZPL bytes and returns ok without a network call.
 */
@Component
public class ZplLabelPrinterAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(ZplLabelPrinterAdapter.class);
    public static final String CODE = "M9_LABEL_PRINTER_ZPL";

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
            String host = String.valueOf(config.getOrDefault("host", "localhost"));
            int port = ((Number) config.getOrDefault("port", 9100)).intValue();
            // Light probe: HTTP HEAD on the printer's web UI (most ZPL
            // printers expose one on the same host on port 80).
            String url = "http://" + host + ":" + (port == 9100 ? 80 : port) + "/";
            RestClient client = RestClient.builder().baseUrl(url).build();
            try {
                ResponseEntity<String> resp = client.get().uri("/").retrieve().toEntity(String.class);
                return PingResult.builder().ok(true).message("HTTP " + resp.getStatusCode().value())
                        .httpStatus(resp.getStatusCode().value()).build();
            } catch (RestClientResponseException server) {
                return PingResult.builder().ok(true).message("HTTP " + server.getStatusCode().value())
                        .httpStatus(server.getStatusCode().value()).build();
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
                case "printZpl" -> printZpl(payload, config, dryRun);
                case "printTemplate" -> printTemplate(payload, config, dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "ZPL supports printZpl and printTemplate; got: " + operation);
            };
        } catch (Exception e) {
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private OutboundResult printZpl(Map<String, Object> p, Map<String, Object> config, boolean dryRun) {
        Object zpl = p.get("zpl");
        if (zpl == null) return OutboundResult.failed("bad_request", "payload.zpl required");
        String zplStr = String.valueOf(zpl);
        if (dryRun) {
            log.info("ZPL DRY-RUN printZpl bytes={}", zplStr.length());
            return OutboundResult.builder().ok(true).status(200)
                    .body("{\"dryRun\":true,\"bytes\":" + zplStr.length() + "}").build();
        }
        String host = String.valueOf(config.getOrDefault("host", "localhost"));
        int port = ((Number) config.getOrDefault("port", 9100)).intValue();
        String url = "http://" + host + ":" + port + "/";
        RestClient client = RestClient.builder().build();
        try {
            ResponseEntity<String> resp = client.post().uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, "application/x-zpl")
                    .body(zplStr).retrieve().toEntity(String.class);
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

    @SuppressWarnings("unchecked")
    private OutboundResult printTemplate(Map<String, Object> p, Map<String, Object> config, boolean dryRun) {
        Object name = p.get("templateName");
        if (name == null) return OutboundResult.failed("bad_request", "payload.templateName required");
        Map<String, Object> vars = p.get("vars") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        String tpl = builtInTemplate(String.valueOf(name));
        if (tpl == null) return OutboundResult.failed("unknown_template",
                "Template " + name + " not in built-in set (gate-pass, weighment-slip)");
        String rendered = renderTemplate(tpl, vars);
        return printZpl(Map.of("zpl", rendered), config, dryRun);
    }

    private String renderTemplate(String tpl, Map<String, Object> vars) {
        String s = tpl;
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            s = s.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : String.valueOf(e.getValue()));
        }
        return s;
    }

    /**
     * Built-in templates — in production these come from
     * {@code src/main/resources/templates/labels/*.zpl} via classpath
     * loading. Two are hardcoded here to keep the adapter self-contained
     * for the M11.2 milestone; richer loader lands later.
     */
    private String builtInTemplate(String name) {
        return switch (name) {
            case "gate-pass" ->
                    "^XA^FO50,50^A0N,40,40^FDPico Power Project^FS" +
                    "^FO50,100^A0N,30,30^FDGate Pass {{gpNumber}}^FS" +
                    "^FO50,140^A0N,25,25^FDVehicle: {{vehicleNo}}^FS" +
                    "^FO50,180^A0N,25,25^FDDriver:  {{driverName}}^FS" +
                    "^FO50,220^A0N,20,20^FDDate:    {{date}}^FS" +
                    "^FO50,260^BQN,2,5^FDQA,{{gpNumber}}^FS^XZ";
            case "weighment-slip" ->
                    "^XA^FO50,50^A0N,40,40^FDWeighment Slip^FS" +
                    "^FO50,100^A0N,30,30^FDGP {{gpNumber}}  WB {{wbCode}}  Seq {{seq}}^FS" +
                    "^FO50,150^A0N,50,50^FD{{weightKg}} kg^FS" +
                    "^FO50,220^A0N,20,20^FDOperator: {{operator}}  At: {{ts}}^FS^XZ";
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
