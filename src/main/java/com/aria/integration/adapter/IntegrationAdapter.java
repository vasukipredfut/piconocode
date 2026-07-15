package com.aria.integration.adapter;

import com.aria.integration.model.IntegrationInstance;

import java.util.Map;

/**
 * SPI every connector adapter implements. {@link com.aria.integration.service.IntegrationOutboundCaller}
 * and {@link com.aria.integration.service.IntegrationInboundReceiver} dispatch by
 * {@link #connectorCode()}.
 *
 * <p>All methods take resolved decrypted secrets in {@code secrets} (a
 * {@code Map<String,Object>}); adapters must NEVER log secret values.
 */
public interface IntegrationAdapter {

    String connectorCode();

    default boolean supportsInbound()  { return false; }
    default boolean supportsOutbound() { return false; }
    default boolean supportsStream()   { return false; }

    default PingResult ping(IntegrationInstance instance, Map<String, Object> secrets) {
        return PingResult.notImplemented(connectorCode());
    }

    default OutboundResult invoke(IntegrationInstance instance, String operation,
                                  Map<String, Object> payload, Map<String, Object> secrets) {
        return OutboundResult.failed("not_implemented", "Adapter " + connectorCode() +
                " does not implement invoke()");
    }

    default InboundResult ingest(IntegrationInstance instance, Map<String, String> headers,
                                 byte[] body, Map<String, Object> secrets) {
        return InboundResult.rejected("not_implemented");
    }
}
