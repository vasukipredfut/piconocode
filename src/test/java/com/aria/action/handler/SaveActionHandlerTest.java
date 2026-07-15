package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit test for {@link SaveActionHandler}: happy path + missing entity guard. */
class SaveActionHandlerTest {

    @Test
    void code_isSave() {
        assertThat(new SaveActionHandler().code()).isEqualTo("save");
    }

    @Test
    void happyPath_invokesWriter_andReturnsRecordId() {
        EntityWriter writer = mock(EntityWriter.class);
        when(writer.upsert(eq("Vendor"), any(), any())).thenReturn(99L);
        SaveActionHandler h = new SaveActionHandler();
        ReflectionTestUtils.setField(h, "writer", writer);

        ActionInvocation inv = ActionInvocation.builder()
                .entity("Vendor")
                .formData(Map.of("name", "Acme"))
                .build();
        ActionResult r = h.handle(inv);

        assertThat(r.isOk()).isTrue();
        assertThat(r.getRecordId()).isEqualTo(99L);
        assertThat(r.getMessage()).isEqualTo("saved");
        verify(writer).upsert(eq("Vendor"), eq(null), eq(Map.of("name", "Acme")));
    }

    @Test
    void missingEntity_throws() {
        SaveActionHandler h = new SaveActionHandler();
        ReflectionTestUtils.setField(h, "writer", mock(EntityWriter.class));
        assertThatThrownBy(() -> h.handle(new ActionInvocation()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entity");
    }
}
