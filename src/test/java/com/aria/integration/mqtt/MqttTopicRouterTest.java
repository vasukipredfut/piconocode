package com.aria.integration.mqtt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure topic parsing/building tests — zero MQTT dependency, no broker needed. */
class MqttTopicRouterTest {

    @Test
    void parsesWellFormedEventTopic_multiSegmentTopicBase() {
        var parsed = MqttTopicRouter.parseInbound("factory/conveyor/LINE1/evt/barcode-scanned");
        assertThat(parsed).isPresent();
        assertThat(parsed.get().topicBase()).isEqualTo("factory/conveyor");
        assertThat(parsed.get().lineId()).isEqualTo("LINE1");
        assertThat(parsed.get().eventType()).isEqualTo("barcode-scanned");
    }

    @Test
    void parsesWellFormedEventTopic_singleSegmentTopicBase() {
        var parsed = MqttTopicRouter.parseInbound("factory/LINE7/evt/heartbeat");
        assertThat(parsed).isPresent();
        assertThat(parsed.get().topicBase()).isEqualTo("factory");
        assertThat(parsed.get().lineId()).isEqualTo("LINE7");
        assertThat(parsed.get().eventType()).isEqualTo("heartbeat");
    }

    @Test
    void allSixEventTypesParse() {
        for (String eventType : MqttTopicRouter.EVENT_TYPES) {
            var parsed = MqttTopicRouter.parseInbound("factory/conveyor/LINE1/evt/" + eventType);
            assertThat(parsed).as("eventType=" + eventType).isPresent();
            assertThat(parsed.get().eventType()).isEqualTo(eventType);
        }
        assertThat(MqttTopicRouter.EVENT_TYPES).hasSize(6);
    }

    @Test
    void commandTopic_isRejected_notAnEventTopic() {
        assertThat(MqttTopicRouter.parseInbound("factory/conveyor/LINE1/cmd")).isEmpty();
    }

    @Test
    void unknownEventType_isRejected() {
        assertThat(MqttTopicRouter.parseInbound("factory/conveyor/LINE1/evt/unknown-thing")).isEmpty();
    }

    @Test
    void tooFewSegments_isRejected() {
        assertThat(MqttTopicRouter.parseInbound("LINE1/evt/heartbeat")).isEmpty();
    }

    @Test
    void blankTopicBase_isRejected() {
        assertThat(MqttTopicRouter.parseInbound("/LINE1/evt/heartbeat")).isEmpty();
    }

    @Test
    void nullTopic_isRejected() {
        assertThat(MqttTopicRouter.parseInbound(null)).isEmpty();
    }

    @Test
    void commandTopicBuilder_matchesConvention() {
        assertThat(MqttTopicRouter.commandTopic("factory/conveyor", "LINE1")).isEqualTo("factory/conveyor/LINE1/cmd");
    }

    @Test
    void eventTopicBuilder_roundTripsThroughParseInbound() {
        String topic = MqttTopicRouter.eventTopic("factory/conveyor", "LINE1", "task-progress");
        var parsed = MqttTopicRouter.parseInbound(topic);
        assertThat(parsed).isPresent();
        assertThat(parsed.get().topicBase()).isEqualTo("factory/conveyor");
        assertThat(parsed.get().lineId()).isEqualTo("LINE1");
        assertThat(parsed.get().eventType()).isEqualTo("task-progress");
    }

    @Test
    void wildcardEventFilter_matchesConvention() {
        assertThat(MqttTopicRouter.wildcardEventFilter("factory/conveyor")).isEqualTo("factory/conveyor/+/evt/#");
    }
}
