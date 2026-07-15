package com.aria.m0.action.handler;

import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit test for {@link PageOpenActionHandler}: directive shape + missing-page guard. */
class PageOpenActionHandlerTest {

    @Test
    void code_isPageOpen() {
        assertThat(new PageOpenActionHandler().code()).isEqualTo("page.open");
    }

    @Test
    void emitsNavigateDirectiveWithParams() {
        PageOpenActionHandler h = new PageOpenActionHandler();
        ActionResult r = h.handle(ActionInvocation.builder()
                .page("pico:vendor-detail")
                .params(Map.of("id", 7))
                .build());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getNextPageState()).containsEntry("navigate", "page.open");
        assertThat(r.getNextPageState()).containsEntry("page", "pico:vendor-detail");
        assertThat(((Map<?, ?>) r.getNextPageState().get("params")).get("id")).isEqualTo(7);
    }

    @Test
    void noParams_directiveStillEmitted() {
        PageOpenActionHandler h = new PageOpenActionHandler();
        ActionResult r = h.handle(ActionInvocation.builder().page("pico:vendor-list").build());
        assertThat(r.getNextPageState()).containsEntry("navigate", "page.open");
        assertThat(r.getNextPageState()).containsEntry("page", "pico:vendor-list");
        assertThat(r.getNextPageState()).doesNotContainKey("params");
    }

    @Test
    void missingPage_throws() {
        assertThatThrownBy(() -> new PageOpenActionHandler().handle(new ActionInvocation()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }
}
