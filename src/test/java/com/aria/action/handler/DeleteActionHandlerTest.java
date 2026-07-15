package com.aria.action.handler;

import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** Unit test for {@link DeleteActionHandler}: happy path + guards. */
class DeleteActionHandlerTest {

    @Test
    void code_isDelete() {
        assertThat(new DeleteActionHandler().code()).isEqualTo("delete");
    }

    @Test
    void delegatesToEntityWriter() {
        EntityWriter writer = mock(EntityWriter.class);
        DeleteActionHandler h = new DeleteActionHandler();
        ReflectionTestUtils.setField(h, "writer", writer);

        ActionResult r = h.handle(ActionInvocation.builder()
                .entity("Vendor").recordId(42L).build());

        assertThat(r.isOk()).isTrue();
        assertThat(r.getRecordId()).isEqualTo(42L);
        verify(writer).softDelete("Vendor", 42L);
    }

    @Test
    void missingEntity_throws() {
        DeleteActionHandler h = new DeleteActionHandler();
        ReflectionTestUtils.setField(h, "writer", mock(EntityWriter.class));
        assertThatThrownBy(() -> h.handle(ActionInvocation.builder().recordId(1L).build()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingRecordId_throws() {
        DeleteActionHandler h = new DeleteActionHandler();
        ReflectionTestUtils.setField(h, "writer", mock(EntityWriter.class));
        assertThatThrownBy(() -> h.handle(ActionInvocation.builder().entity("Vendor").build()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
