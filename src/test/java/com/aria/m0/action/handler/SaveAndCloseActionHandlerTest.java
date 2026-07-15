package com.aria.m0.action.handler;

import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit test for {@link SaveAndCloseActionHandler}: nextPageState navigate-back. */
class SaveAndCloseActionHandlerTest {

    @Test
    void code_isSaveAndClose() {
        assertThat(new SaveAndCloseActionHandler().code()).isEqualTo("save.and.close");
    }

    @Test
    void emitsPageBackNavigateDirective() {
        EntityWriter writer = mock(EntityWriter.class);
        when(writer.upsert(eq("Vendor"), any(), any())).thenReturn(5L);
        SaveAndCloseActionHandler h = new SaveAndCloseActionHandler();
        ReflectionTestUtils.setField(h, "writer", writer);

        ActionResult r = h.handle(ActionInvocation.builder()
                .entity("Vendor").formData(Map.of("name", "X")).build());

        assertThat(r.isOk()).isTrue();
        assertThat(r.getRecordId()).isEqualTo(5L);
        assertThat(r.getNextPageState()).containsEntry("navigate", "page.back");
    }

    @Test
    void missingEntity_throws() {
        SaveAndCloseActionHandler h = new SaveAndCloseActionHandler();
        ReflectionTestUtils.setField(h, "writer", mock(EntityWriter.class));
        assertThatThrownBy(() -> h.handle(new ActionInvocation()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
