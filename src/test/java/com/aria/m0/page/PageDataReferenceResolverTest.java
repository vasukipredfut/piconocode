package com.aria.m0.page;

import com.aria.m0.workflow.PageDataMissingException;
import com.aria.m0.workflow.PageDataReferenceResolver;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the {@code pageData.<pageCode>.<fieldCode>} workflow
 * reference resolver. Round 1H wires it to the real workflow_instance row.
 */
class PageDataReferenceResolverTest {

    private final PageDataReferenceResolver svc = new PageDataReferenceResolver();

    @Test
    void resolve_returnsValueWhenPresent() {
        Map<String, Map<String, Object>> data = Map.of(
                "pico:weighment-capture", Map.of("grossKg", 12345));
        Optional<Object> out = svc.resolve("pico:weighment-capture", "grossKg", data);
        assertThat(out).contains(12345);
    }

    @Test
    void resolve_throwsWhenSubmittedDataNull() {
        assertThatThrownBy(() -> svc.resolve("p", "f", null))
                .isInstanceOf(PageDataMissingException.class)
                .hasMessageContaining("pageData.p.f");
    }

    @Test
    void resolve_throwsWhenPageMissing() {
        assertThatThrownBy(() -> svc.resolve("missing", "f", Map.of()))
                .isInstanceOf(PageDataMissingException.class)
                .hasMessageContaining("not yet submitted");
    }

    @Test
    void resolve_throwsWhenFieldMissing() {
        Map<String, Map<String, Object>> data = Map.of("p", Map.of("other", "x"));
        assertThatThrownBy(() -> svc.resolve("p", "missing", data))
                .isInstanceOf(PageDataMissingException.class);
    }

    @Test
    void resolveFromJson_handlesString() {
        Optional<Object> out = svc.resolveFromJson("p", "f", "{\"p\":{\"f\":\"hello\"}}");
        assertThat(out).contains("hello");
    }

    @Test
    void resolveFromJson_handlesNumber() {
        Optional<Object> out = svc.resolveFromJson("p", "f", "{\"p\":{\"f\":42}}");
        assertThat(out).contains(42);
    }

    @Test
    void resolveFromJson_blankThrows() {
        assertThatThrownBy(() -> svc.resolveFromJson("p", "f", ""))
                .isInstanceOf(PageDataMissingException.class);
    }

    @Test
    void resolveFromJson_malformedThrows() {
        assertThatThrownBy(() -> svc.resolveFromJson("p", "f", "{not json"))
                .isInstanceOf(PageDataMissingException.class)
                .hasMessageContaining("malformed");
    }
}
