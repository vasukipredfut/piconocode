package com.aria.integration.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.integration.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Conveyor MQTT / OPC-UA outbound connector — sends commands to the
 * shop-floor packing-line conveyor PLC ({@code start / stop / set-speed /
 * divert}). The actual MQTT publish is stubbed in this first cut — real
 * impl would use Eclipse Paho / Spring Integration MQTT and publish to
 * {@code factory/{lineId}/cmd}. dryRun=true logs the command and returns
 * success without a broker connection.
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code start}     — payload {lineId} → publish "START"</li>
 *   <li>{@code stop}      — payload {lineId} → publish "STOP"</li>
 *   <li>{@code setSpeed}  — payload {lineId, speedRpm}</li>
 *   <li>{@code divert}    — payload {lineId, divertTo} (e.g. SPOIL-BIN)</li>
 * </ul>
 *
 * <p>Config: {@code { "brokerUrl": "tcp://factory.local:1883",
 *   "clientId": "earms-pico", "topicBase": "factory/conveyor", "dryRun": true }}.
 */
@Component
public class ConveyorMqttAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(ConveyorMqttAdapter.class);
    public static final String CODE = "MQTT_CONVEYOR";

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
            // Real impl would do a Paho MqttClient.connect() probe — stub.
            return PingResult.builder().ok(false).message(
                    "Live MQTT probe unwired; flip dryRun=true (broker: " +
                            config.getOrDefault("brokerUrl", "?") + ")").build();
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
            Object lineId = payload.get("lineId");
            if (lineId == null) return OutboundResult.failed("bad_request", "payload.lineId required");

            return switch (op) {
                case "start"    -> publish(config, lineId, "START",    null, dryRun);
                case "stop"     -> publish(config, lineId, "STOP",     null, dryRun);
                case "setSpeed" -> publish(config, lineId, "SET_SPEED", Map.of("speedRpm", payload.get("speedRpm")), dryRun);
                case "divert"   -> publish(config, lineId, "DIVERT",   Map.of("divertTo", payload.get("divertTo")), dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "Conveyor supports start/stop/setSpeed/divert; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("Conveyor invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ---- internal ----------------------------------------------------------

    private OutboundResult publish(Map<String, Object> config, Object lineId, String cmd,
                                   Map<String, Object> args, boolean dryRun) throws IOException {
        String topic = String.valueOf(config.getOrDefault("topicBase", "factory/conveyor"))
                + "/" + lineId + "/cmd";
        Map<String, Object> body = new HashMap<>();
        body.put("cmd", cmd);
        body.put("lineId", lineId);
        body.put("topic", topic);
        body.put("ts", System.currentTimeMillis());
        if (args != null) body.put("args", args);
        if (dryRun) {
            log.info("Conveyor DRY-RUN publish topic={} cmd={} args={}", topic, cmd, args);
            body.put("dryRun", true);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(body)).build();
        }
        return OutboundResult.failed("not_implemented_live",
                "Live MQTT publish unwired; flip dryRun=true");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
