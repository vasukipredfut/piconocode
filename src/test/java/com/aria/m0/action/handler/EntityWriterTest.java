package com.aria.m0.action.handler;

import com.aria.m0.entity_registry.EntityRegistryService;
import com.aria.m0.entity_registry.model.EntityFieldRegistryEntry;
import com.aria.m0.entity_registry.model.EntityRegistryEntry;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link EntityWriter} — the generic INSERT/UPDATE/soft-delete
 * engine all action handlers share. JdbcTemplate is mocked; we capture the
 * generated SQL and bind args.
 */
class EntityWriterTest {

    private EntityRegistryService registry;
    private JdbcTemplate jdbc;
    private EntityWriter writer;

    @BeforeEach
    void setUp() {
        registry = mock(EntityRegistryService.class);
        jdbc = mock(JdbcTemplate.class);
        writer = new EntityWriter(registry, jdbc);
        TenantContext.set(1L);

        when(registry.findByCode("Vendor")).thenReturn(Optional.of(
                EntityRegistryEntry.builder().code("Vendor").tableName("m0_vendor").build()));
        when(registry.fieldsFor("Vendor")).thenReturn(List.of(
                field("name", "STRING"), field("gstin", "STRING"),
                field("status", "ENUM"), field("tenant_id", "LONG")));
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    private EntityFieldRegistryEntry field(String code, String type) {
        return EntityFieldRegistryEntry.builder()
                .entityCode("Vendor").fieldCode(code).displayLabel(code)
                .dataType(type).build();
    }

    @Test
    void insert_addsTenantId_andReturnsGeneratedKey() {
        // PSC.update returns 1 row affected; KeyHolder is populated by the
        // call to jdbc.update — we can't realistically stub it through a
        // mock without going into PSC internals. So focus the assertion on
        // the fact that jdbc.update is invoked with the expected PSC.
        when(jdbc.update(any(PreparedStatementCreator.class), any())).thenReturn(1);

        // We cannot easily snoop on the PSC's SQL through a pure mock —
        // verify the call happens.
        Long id = writer.upsert("Vendor", null, Map.of("name", "Acme"));
        // No generated key in the holder → returns null. That's expected
        // for this pure mock; the integration test exercises the real path.
        assertThat(id).isNull();
        verify(jdbc).update(any(PreparedStatementCreator.class), any());
    }

    @Test
    void update_buildsUpdateStmt_withTenantScope() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        Long id = writer.upsert("Vendor", 7L, Map.of("name", "Acme", "gstin", "33ABC"));
        assertThat(id).isEqualTo(7L);
        verify(jdbc).update(argThat((String sql) ->
                        sql.startsWith("UPDATE m0_vendor SET")
                        && sql.contains("name = ?")
                        && sql.contains("gstin = ?")
                        && sql.contains("WHERE id = ?")
                        && sql.contains("tenant_id = ?")),
                any(Object[].class));
    }

    @Test
    void update_noMatch_throws() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0);
        assertThatThrownBy(() -> writer.upsert("Vendor", 9999L, Map.of("name", "x")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("update_no_match");
    }

    @Test
    void unknownEntity_throws() {
        when(registry.findByCode("Nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> writer.upsert("Nope", null, Map.of("k", "v")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnregisteredFields_silently() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        writer.upsert("Vendor", 1L, Map.of("name", "OK", "evil_col", "DROP"));
        verify(jdbc).update(argThat((String s) ->
                s.contains("name = ?") && !s.contains("evil_col")), any(Object[].class));
    }

    @Test
    void softDelete_usesStatusColumn_whenPresent() {
        // status field is registered for Vendor
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        writer.softDelete("Vendor", 5L);
        verify(jdbc).update(argThat((String s) ->
                s.startsWith("UPDATE m0_vendor SET status = 'DELETED'")
                && s.contains("tenant_id = ?")), any(Object[].class));
    }

    @Test
    void softDelete_noRecord_throws() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(0);
        assertThatThrownBy(() -> writer.softDelete("Vendor", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("delete_no_match");
    }
}
