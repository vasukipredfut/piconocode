package com.aria.m11.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Modbus-TCP outbound connector — reads/writes registers on PLCs.
 *
 * <p>Operations:
 * <ul>
 *   <li>{@code readHoldingRegisters} — payload {unitId, address, count}</li>
 *   <li>{@code writeSingleRegister}  — payload {unitId, address, value}</li>
 *   <li>{@code readCoils}            — payload {unitId, address, count}</li>
 *   <li>{@code writeSingleCoil}      — payload {unitId, address, value(bool)}</li>
 * </ul>
 *
 * <p>Config: {@code { "host":"plc-01.factory.local", "port":502, "timeoutMs":2000, "dryRun":true }}.
 * dryRun=true logs the request and returns synthetic values — lets shop-floor
 * pages drive end-to-end without a real PLC. Real impl would use j2mod's
 * {@code ModbusTCPMaster}.
 */
@Component
public class ModbusTcpAdapter implements IntegrationAdapter {

    private static final Logger log = LoggerFactory.getLogger(ModbusTcpAdapter.class);
    public static final String CODE = "MODBUS_TCP";
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
            return PingResult.builder().ok(false).message(
                    "Live Modbus probe unwired; flip dryRun=true (target: " +
                            config.getOrDefault("host", "?") + ":" + config.getOrDefault("port", 502) + ")").build();
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
                case "readHoldingRegisters" -> readRegs(payload, "HOLDING", dryRun);
                case "writeSingleRegister" -> writeReg(payload, "HOLDING", dryRun);
                case "readCoils" -> readRegs(payload, "COIL", dryRun);
                case "writeSingleCoil" -> writeReg(payload, "COIL", dryRun);
                default -> OutboundResult.failed("unknown_op",
                        "Modbus supports readHoldingRegisters/writeSingleRegister/readCoils/writeSingleCoil; got: " + operation);
            };
        } catch (Exception e) {
            log.warn("Modbus invoke failed: {}", e.toString());
            return OutboundResult.failed(e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private OutboundResult readRegs(Map<String, Object> p, String kind, boolean dryRun) throws IOException {
        Object addr = p.get("address");
        if (addr == null) return OutboundResult.failed("bad_request", "payload.address required");
        int count = p.get("count") instanceof Number ? ((Number) p.get("count")).intValue() : 1;
        if (dryRun) {
            // Synthesise count int values
            int[] values = new int[count];
            for (int i = 0; i < count; i++) values[i] = 1000 + i; // deterministic for tests
            log.info("Modbus DRY-RUN read{} addr={} count={}", kind, addr, count);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(Map.of(
                            "kind", kind, "address", addr, "count", count, "values", values, "dryRun", true)))
                    .build();
        }
        return OutboundResult.failed("not_implemented_live", "Live Modbus read unwired; flip dryRun=true");
    }

    private OutboundResult writeReg(Map<String, Object> p, String kind, boolean dryRun) throws IOException {
        Object addr = p.get("address");
        Object value = p.get("value");
        if (addr == null) return OutboundResult.failed("bad_request", "payload.address required");
        if (value == null) return OutboundResult.failed("bad_request", "payload.value required");
        if (dryRun) {
            log.info("Modbus DRY-RUN write{} addr={} value={}", kind, addr, value);
            return OutboundResult.builder().ok(true).status(200)
                    .body(mapper.writeValueAsString(Map.of(
                            "kind", kind, "address", addr, "value", value, "dryRun", true)))
                    .build();
        }
        return OutboundResult.failed("not_implemented_live", "Live Modbus write unwired; flip dryRun=true");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws IOException {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return new HashMap<>();
        return mapper.readValue(json, Map.class);
    }
}
