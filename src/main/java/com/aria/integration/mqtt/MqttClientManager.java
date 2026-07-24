package com.aria.integration.mqtt;

import com.aria.conveyor.dto.HeartbeatRequest;
import com.aria.conveyor.dto.ScanRequest;
import com.aria.conveyor.service.ConveyorScanService;
import com.aria.conveyor.service.DeviceHeartbeatService;
import com.aria.integration.dao.IntegrationInstanceRepository;
import com.aria.integration.model.IntegrationInstance;
import com.aria.security.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real MQTT transport for the conveyor integration — the piece
 * {@code ConveyorMqttAdapter}'s original stub always deferred to "real impl
 * would use Eclipse Paho". Opens ONE shared {@link MqttAsyncClient} per
 * distinct broker URL (deduped across every ACTIVE, non-dryRun
 * {@code MQTT_CONVEYOR} {@link IntegrationInstance} — this is a background
 * subscriber with no per-request tenant context, so it scans across ALL
 * tenants at startup), subscribes to a wildcard event filter per topicBase,
 * and on message arrival routes inbound events to the same
 * {@link ConveyorScanService} / {@link DeviceHeartbeatService} the HTTP
 * endpoints use — one barcode-scanned handling path regardless of whether
 * the device called the REST endpoint or published over MQTT.
 *
 * <p>No broker exists to verify against in this environment — every connect
 * attempt is async (Paho's {@code connect(options, userContext, listener)})
 * and defensively wrapped so a failed or absent broker can never block or
 * crash application startup; this only becomes live once a real
 * {@code MQTT_CONVEYOR} instance with {@code dryRun=false} exists.
 */
@Service
public class MqttClientManager {

    private static final Logger log = LoggerFactory.getLogger(MqttClientManager.class);
    private static final String CONNECTOR_CODE = "MQTT_CONVEYOR";

    private final IntegrationInstanceRepository instances;
    private final ConveyorScanService scanService;
    private final DeviceHeartbeatService heartbeatService;
    private final ObjectMapper mapper = new ObjectMapper();

    /** brokerUrl -> shared client (one connection per broker, however many instances/lines share it). */
    private final Map<String, MqttAsyncClient> clientsByBroker = new ConcurrentHashMap<>();
    /** "brokerUrl|topicBase" -> the instance whose config supplied this topicBase, for tenant resolution on inbound messages. */
    private final Map<String, IntegrationInstance> instanceByBrokerTopic = new ConcurrentHashMap<>();

    public MqttClientManager(IntegrationInstanceRepository instances, ConveyorScanService scanService,
                              DeviceHeartbeatService heartbeatService) {
        this.instances = instances;
        this.scanService = scanService;
        this.heartbeatService = heartbeatService;
    }

    @PostConstruct
    void start() {
        List<IntegrationInstance> active;
        try {
            active = instances.findByConnectorCodeAndStatus(CONNECTOR_CODE, IntegrationInstance.STATUS_ACTIVE);
        } catch (Exception e) {
            log.warn("MqttClientManager: could not load {} instances at startup: {}", CONNECTOR_CODE, e.toString());
            return;
        }
        for (IntegrationInstance inst : active) {
            try {
                connectAndSubscribe(inst);
            } catch (Exception e) {
                // One broken/unreachable broker must never stop the others — or the app — from starting.
                log.warn("MqttClientManager: setup failed for instance {} ({}): {}", inst.getId(), inst.getCode(), e.toString());
            }
        }
    }

    @PreDestroy
    void stop() {
        for (MqttAsyncClient client : clientsByBroker.values()) {
            try {
                if (client.isConnected()) client.disconnect();
                client.close();
            } catch (Exception e) {
                log.warn("MqttClientManager: error closing client: {}", e.toString());
            }
        }
    }

    private void connectAndSubscribe(IntegrationInstance inst) throws Exception {
        Map<String, Object> config = readConfig(inst);
        if (Boolean.TRUE.equals(config.get("dryRun"))) return; // dryRun instances never touch a real broker
        Object brokerUrlRaw = config.get("brokerUrl");
        if (brokerUrlRaw == null || String.valueOf(brokerUrlRaw).isBlank()) {
            log.warn("MqttClientManager: instance {} has no brokerUrl configured, skipping", inst.getId());
            return;
        }
        String brokerUrl = String.valueOf(brokerUrlRaw);
        String topicBase = String.valueOf(config.getOrDefault("topicBase", "factory/conveyor"));
        String clientId = String.valueOf(config.getOrDefault("clientId", "earms-backend-" + inst.getId()));

        instanceByBrokerTopic.put(brokerUrl + "|" + topicBase, inst);
        String filter = MqttTopicRouter.wildcardEventFilter(topicBase);
        MqttAsyncClient client = clientsByBroker.computeIfAbsent(brokerUrl, url -> newClient(url, clientId));

        if (client.isConnected()) {
            client.subscribe(filter, 1);
            return;
        }
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);
        opts.setConnectionTimeout(10);
        client.connect(opts, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                try {
                    client.subscribe(filter, 1);
                    log.info("MqttClientManager: connected to {} and subscribed to {}", brokerUrl, filter);
                } catch (Exception e) {
                    log.warn("MqttClientManager: subscribe failed after connect ({}): {}", filter, e.toString());
                }
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                log.warn("MqttClientManager: connect failed for {}: {}", brokerUrl, exception.toString());
            }
        });
    }

    private MqttAsyncClient newClient(String brokerUrl, String clientId) {
        try {
            MqttAsyncClient client = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MqttClientManager: connection lost to {}: {}", brokerUrl, cause == null ? "?" : cause.toString());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleInbound(brokerUrl, topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Fire-and-forget publishes; nothing to reconcile here.
                }
            });
            return client;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create MQTT client for " + brokerUrl, e);
        }
    }

    private void handleInbound(String brokerUrl, String topic, MqttMessage message) {
        try {
            Optional<MqttTopicRouter.ParsedTopic> parsed = MqttTopicRouter.parseInbound(topic);
            if (parsed.isEmpty()) return;
            IntegrationInstance owner = instanceByBrokerTopic.get(brokerUrl + "|" + parsed.get().topicBase());
            Long previous = TenantContext.peek();
            try {
                TenantContext.set(owner != null ? owner.getTenantId() : TenantContext.GLOBAL_TENANT_ID);
                dispatch(parsed.get().eventType(), message.getPayload());
            } finally {
                if (previous == null) TenantContext.clear();
                else TenantContext.set(previous);
            }
        } catch (Exception e) {
            log.warn("MqttClientManager: failed handling message on topic {}: {}", topic, e.toString());
        }
    }

    private void dispatch(String eventType, byte[] payload) throws Exception {
        switch (eventType) {
            case "barcode-scanned" -> scanService.scan(mapper.readValue(payload, ScanRequest.class));
            case "heartbeat" -> heartbeatService.upsert(mapper.readValue(payload, HeartbeatRequest.class));
            default -> log.info("MqttClientManager: received {} event ({} bytes) — logged; no further "
                    + "processing wired for this event type yet", eventType, payload.length);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readConfig(IntegrationInstance instance) throws Exception {
        String json = instance.getConfigJson();
        if (json == null || json.isBlank()) return Map.of();
        return mapper.readValue(json, Map.class);
    }

    /**
     * Used by {@code ConveyorMqttAdapter.invoke()} for real (non-dryRun)
     * publishes. Fire-and-forget: does not block for broker acknowledgement
     * (QoS handles redelivery); returns false if there's no connected client
     * for this instance's broker.
     */
    public boolean publish(IntegrationInstance instance, String topic, byte[] payload, int qos) {
        try {
            Map<String, Object> config = readConfig(instance);
            String brokerUrl = String.valueOf(config.get("brokerUrl"));
            MqttAsyncClient client = clientsByBroker.get(brokerUrl);
            if (client == null || !client.isConnected()) {
                log.warn("MqttClientManager: no connected client for broker {} (instance {})", brokerUrl, instance.getId());
                return false;
            }
            MqttMessage msg = new MqttMessage(payload);
            msg.setQos(qos);
            client.publish(topic, msg);
            return true;
        } catch (Exception e) {
            log.warn("MqttClientManager: publish failed for instance {}: {}", instance.getId(), e.toString());
            return false;
        }
    }

    /** Real connect/isConnected probe for {@code ConveyorMqttAdapter.ping()}. */
    public boolean isConnected(IntegrationInstance instance) {
        try {
            Map<String, Object> config = readConfig(instance);
            String brokerUrl = String.valueOf(config.get("brokerUrl"));
            MqttAsyncClient client = clientsByBroker.get(brokerUrl);
            return client != null && client.isConnected();
        } catch (Exception e) {
            return false;
        }
    }
}
