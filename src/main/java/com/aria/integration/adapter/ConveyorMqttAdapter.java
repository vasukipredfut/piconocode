package com.aria.integration.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.integration.model.IntegrationInstance;
import com.aria.integration.mqtt.MqttClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Conveyor MQTT outbound connector — sends commands to the shop-floor
 * packing-line conveyor PLC ({@code start / stop / set-speed / divert}) and
 * (via {@link MqttClientManager}) is also the transport a device's published
 * events ({@code barcode-scanned}, {@code heartbeat}, ...) arrive through,
 * inbound.
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
 *   "clientId": "earms-edge", "topicBase": "factory/conveyor", "dryRun": true }}.
 * dryRun=true logs the command and returns success without a broker
 * connection — the same convention every stubbed adapter in this package
 * uses. dryRun=false delegates to {@link MqttClientManager}, which owns the
 * real Paho client (no broker exists to verify this against in this
 * environment, but the code path is real, not stubbed).
 */
@Component
public class ConveyorMqttAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(ConveyorMqttAdapter.class);
    public static final String CODE = "MQTT_CONVEYOR";

    private final ObjectMapper mapper = new ObjectMapper();
    private final MqttClientManager mqtt;

    /**
     * {@code @Lazy}: {@code IntegrationOutboundCaller} indexes every
     * {@code IntegrationAdapter} bean (including this one), and
     * {@code MqttClientManager} depends (transitively, via
     * {@code ConveyorScanService} → {@code PlcSignalService}) on
     * {@code IntegrationOutboundCaller} — a genuine cycle. This adapter only
     * ever calls {@code mqtt.publish()}/{@code isConnected()} at request
     * time, never during construction, so deferring resolution to first use
     * breaks the cycle with no behavioural change.
     */
    public ConveyorMqttAdapter(@Lazy MqttClientManager mqtt) {
        this.mqtt = mqtt;
    }

    @Override public String connectorCode() { return CODE; }
    @Override public boolean supportsOutbound() { return true; }

    @Override
    public PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        try {
            Map<String, Object> config = readConfig(instance);
            if (Boolean.TRUE.equals(config.get("dryRun"))) {
                return PingResult.builder().ok(true).message("dryRun mode").build();
            }
            boolean connected = mqtt.isConnected(instance);
            return PingResult.builder().ok(connected)
                    .message(connected ? "connected" : "not connected (broker: " + config.getOrDefault("brokerUrl", "?") + ")")
                    .build();
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
                case "start"    -> publish(instance, config, lineId, "START",    null, dryRun);
                case "stop"     -> publish(instance, config, lineId, "STOP",     null, dryRun);
                case "setSpeed" -> publish(instance, config, lineId, "SET_SPEED", Map.of("speedRpm", payload.get("speedRpm")), dryRun);
                case "divert"   -> publish(instance, config, lineId, "DIVERT",   Map.of("divertTo", payload.get("divertTo")), dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "Conveyor supports start/stop/setSpeed/divert; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("Conveyor invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // ---- internal ----------------------------------------------------------

    private OutboundResult publish(IntegrationInstance instance, Map<String, Object> config, Object lineId, String cmd,
                                   Map<String, Object> args, boolean dryRun) throws IOException {
        String topicBase = String.valueOf(config.getOrDefault("topicBase", "factory/conveyor"));
        String topic = topicBase + "/" + lineId + "/cmd";
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
        boolean sent = mqtt.publish(instance, topic, mapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8), 1);
        if (!sent) {
            return OutboundResult.retryable("mqtt_publish_failed", "No connected MQTT client for this instance's broker");
        }
        return OutboundResult.builder().ok(true).status(200).body(mapper.writeValueAsString(body)).build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
