package com.aria.conveyor.service;

import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.service.IntegrationOutboundCaller;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Thin wrapper over {@link IntegrationOutboundCaller} for "tell the PLC
 * whether this carton was accepted or rejected." The caller only ever says
 * accept/reject by Integration ID — it never knows a coil address; that
 * mapping lives in the PLC IntegrationInstance's own config, resolved by
 * {@code ModbusTcpAdapter}. Reuses the existing circuit-breaker/retry/audit
 * wrapping unchanged.
 */
@Service
public class PlcSignalService {

    private final IntegrationOutboundCaller outboundCaller;

    public PlcSignalService(IntegrationOutboundCaller outboundCaller) {
        this.outboundCaller = outboundCaller;
    }

    public OutboundResult signalAccept(Long plcIntegrationId) {
        return outboundCaller.invoke(plcIntegrationId, "signalAccept", Map.of());
    }

    public OutboundResult signalReject(Long plcIntegrationId) {
        return outboundCaller.invoke(plcIntegrationId, "signalReject", Map.of());
    }
}
