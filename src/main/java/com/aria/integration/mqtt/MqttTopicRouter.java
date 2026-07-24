package com.aria.integration.mqtt;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * Pure MQTT topic parsing/building for the conveyor integration — zero
 * dependencies, no broker needed to test it.
 *
 * <p>Convention:
 * <ul>
 *   <li>Outbound (backend → device): {@code {topicBase}/{lineId}/cmd}</li>
 *   <li>Inbound (device → backend): {@code {topicBase}/{lineId}/evt/{eventType}}</li>
 * </ul>
 * {@code eventType} is one of {@link #EVENT_TYPES}: barcode scanned, dispatch
 * completed, carton rejected, task progress updated, PLC execution status,
 * heartbeat. {@code topicBase} and {@code lineId} are plain data (whatever a
 * given {@code IntegrationInstance}'s config says) — nothing here is
 * hardcoded to a specific line.
 */
public final class MqttTopicRouter {

    public static final Set<String> EVENT_TYPES = Set.of(
            "barcode-scanned", "heartbeat", "dispatch-completed",
            "carton-rejected", "task-progress", "plc-status");

    private MqttTopicRouter() {
    }

    public record ParsedTopic(String topicBase, String lineId, String eventType) {
    }

    /**
     * Parses an inbound event topic. Returns empty for anything malformed,
     * shaped like a command topic instead, or with an unrecognized event type.
     */
    public static Optional<ParsedTopic> parseInbound(String topic) {
        if (topic == null) return Optional.empty();
        String[] parts = topic.split("/");
        if (parts.length < 4) return Optional.empty();
        String eventType = parts[parts.length - 1];
        String marker = parts[parts.length - 2];
        String lineId = parts[parts.length - 3];
        if (!"evt".equals(marker)) return Optional.empty();
        if (!EVENT_TYPES.contains(eventType)) return Optional.empty();
        if (lineId.isBlank()) return Optional.empty();
        String topicBase = String.join("/", Arrays.copyOfRange(parts, 0, parts.length - 3));
        if (topicBase.isBlank()) return Optional.empty();
        return Optional.of(new ParsedTopic(topicBase, lineId, eventType));
    }

    /** The outbound command topic for one line. */
    public static String commandTopic(String topicBase, String lineId) {
        return topicBase + "/" + lineId + "/cmd";
    }

    /** The inbound event topic for one line + event type (mainly useful for tests). */
    public static String eventTopic(String topicBase, String lineId, String eventType) {
        return topicBase + "/" + lineId + "/evt/" + eventType;
    }

    /** Subscription filter matching every event, on every line, under a topicBase. */
    public static String wildcardEventFilter(String topicBase) {
        return topicBase + "/+/evt/#";
    }
}
