package com.aria.m0.action;

import com.aria.m0.action.dao.ActionRegistryRepository;
import com.aria.m0.action.model.ActionRegistryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActionDispatcherTest {

    private ActionRegistryRepository registry;
    private ActionDispatcher dispatcher;
    private ActionHandler saveHandler;

    @BeforeEach
    void setUp() {
        registry = mock(ActionRegistryRepository.class);
        saveHandler = mock(ActionHandler.class);
        when(saveHandler.code()).thenReturn("save");
        when(saveHandler.handle(any())).thenReturn(
                ActionResult.builder().ok(true).recordId(123L).build());

        dispatcher = new ActionDispatcher();
        // Spring-style injection done manually so we don't need a context.
        org.springframework.test.util.ReflectionTestUtils.setField(dispatcher, "registry", registry);
        org.springframework.test.util.ReflectionTestUtils.setField(dispatcher, "handlers", List.of(saveHandler));
        dispatcher.index();
    }

    @Test
    void unknownAction_throws() {
        when(registry.findById("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dispatcher.dispatch("nope", new ActionInvocation()))
                .isInstanceOf(ActionDispatcher.ActionUnknownException.class);
    }

    @Test
    void pendingAction_throws() {
        ActionRegistryEntry row = ActionRegistryEntry.builder()
                .code("print").category("PRINT").displayName("Print")
                .paramsSchemaJson("{}").implemented(false).build();
        when(registry.findById("print")).thenReturn(Optional.of(row));
        assertThatThrownBy(() -> dispatcher.dispatch("print", new ActionInvocation()))
                .isInstanceOf(ActionDispatcher.ActionPendingException.class)
                .hasMessageContaining("implementation_pending");
    }

    @Test
    void implementedAction_dispatchedToHandler() {
        ActionRegistryEntry row = ActionRegistryEntry.builder()
                .code("save").category("PERSIST").displayName("Save")
                .paramsSchemaJson("{}").implemented(true).build();
        when(registry.findById("save")).thenReturn(Optional.of(row));
        ActionResult r = dispatcher.dispatch("save", new ActionInvocation());
        assertThat(r.isOk()).isTrue();
        assertThat(r.getRecordId()).isEqualTo(123L);
        verify(saveHandler).handle(any());
    }

    @Test
    void implementedButNoHandlerBean_throwsConfigError() {
        ActionRegistryEntry row = ActionRegistryEntry.builder()
                .code("delete").category("PERSIST").displayName("Delete")
                .paramsSchemaJson("{}").implemented(true).build();
        when(registry.findById("delete")).thenReturn(Optional.of(row));
        assertThatThrownBy(() -> dispatcher.dispatch("delete", new ActionInvocation()))
                .isInstanceOf(IllegalStateException.class);
    }
}
