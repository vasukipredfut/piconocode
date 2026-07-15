package com.aria.m0.datasource;

import com.aria.m0.entity_registry.EntityRegistryService;
import com.aria.m0.entity_registry.model.EntityFieldRegistryEntry;
import com.aria.m0.entity_registry.model.EntityRegistryEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link DataSourceQueryService} — verifies the SQL
 * compilation pipeline (joins, filter tree → WHERE, binding resolution,
 * pagination) without touching a database.
 *
 * <p>The JdbcTemplate is mocked; we capture the SQL passed in and assert
 * on its shape + the bind-parameter array.
 */
class DataSourceQueryServiceTest {

    private EntityRegistryService registry;
    private JdbcTemplate jdbc;
    private DataSourceQueryService svc;

    @BeforeEach
    void setUp() {
        registry = mock(EntityRegistryService.class);
        jdbc = mock(JdbcTemplate.class);
        svc = new DataSourceQueryService(registry, jdbc);
        com.aria.security.TenantContext.set(1L);

        // Common entity defs
        EntityRegistryEntry vendor = EntityRegistryEntry.builder()
                .code("Vendor").tableName("m0_vendor").displayName("Vendor").build();
        EntityRegistryEntry city = EntityRegistryEntry.builder()
                .code("City").tableName("city").displayName("City").build();
        EntityRegistryEntry state = EntityRegistryEntry.builder()
                .code("State").tableName("state").displayName("State").build();
        when(registry.findByCode("Vendor")).thenReturn(Optional.of(vendor));
        when(registry.findByCode("City")).thenReturn(Optional.of(city));
        when(registry.findByCode("State")).thenReturn(Optional.of(state));

        // Default: count → 5; rows → empty list
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(5L);
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() { com.aria.security.TenantContext.clear(); }

    @Test
    @DisplayName("entity-only query emits SELECT/FROM/LIMIT and auto-tenant-scopes m0_vendor")
    void simpleEntityQuery() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .select(List.of("id", "name", "gstin"))
                .build();
        DataSourceSpec.Response r = svc.query(spec, 0, 25, Map.of());
        assertThat(r).isNotNull();
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("FROM m0_vendor")
                && s.contains("m0_vendor.id")
                && s.contains("m0_vendor.name")
                && s.contains("WHERE m0_vendor.tenant_id = ?")
                && s.contains("LIMIT 25")), any(Object[].class));
    }

    @Test
    @DisplayName("joins resolved via entity_registry; LEFT JOIN by default")
    void joinsPlanned() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(
                        DataSourceSpec.Join.builder().entity("City").as("city")
                                .on("m0_vendor.city_id = city.id")
                                .fields(List.of("city.name as cityName")).build(),
                        DataSourceSpec.Join.builder().entity("State").as("state")
                                .on("m0_vendor.state_id = state.id")
                                .fields(List.of("state.name as stateName")).build()))
                .select(List.of("id", "name"))
                .build();
        svc.query(spec, 0, 10, Map.of());
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("LEFT JOIN city city ON m0_vendor.city_id = city.id")
                && s.contains("LEFT JOIN state state ON m0_vendor.state_id = state.id")
                && s.contains("city.name as cityName")
                && s.contains("state.name as stateName")), any(Object[].class));
    }

    @Test
    @DisplayName("filter tree AND/OR/NOT compiles to parenthesised WHERE")
    void filterTree() {
        DataSourceSpec.Filter and = DataSourceSpec.Filter.builder()
                .and(List.of(
                        DataSourceSpec.Filter.builder().field("name").op("contains").value("test").build(),
                        DataSourceSpec.Filter.builder().or(List.of(
                                DataSourceSpec.Filter.builder().field("status").op("eq").value("ACTIVE").build(),
                                DataSourceSpec.Filter.builder().field("status").op("eq").value("PENDING").build()
                        )).build()
                ))
                .build();
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor").filter(and).build();
        svc.query(spec, 0, 10, Map.of());
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("LIKE ?")
                && s.contains("(m0_vendor.status = ? OR m0_vendor.status = ?)")), any(Object[].class));
    }

    @Test
    @DisplayName("ctx:tenantId resolves to TenantContext value")
    void ctxBinding() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .filter(DataSourceSpec.Filter.builder()
                        .field("tenant_id").op("eq").value("ctx:tenantId").build())
                .build();
        svc.query(spec, 0, 10, Map.of());
        // JdbcTemplate.queryForList(String, Object...) — Mockito sees each
        // arg as a separate vararg slot. Auto tenant scope + ctx-resolved
        // tenant id are both 1L → two consecutive Long args.
        org.mockito.ArgumentCaptor<Object> a = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(jdbc).queryForList(anyString(), a.capture(), a.capture());
        assertThat(a.getAllValues()).containsExactly(1L, 1L);
    }

    @Test
    @DisplayName("page:<field> binding resolves from the supplied map")
    void pageBinding() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("City")
                .filter(DataSourceSpec.Filter.builder()
                        .field("stateid").op("eq").value("page:stateId").build())
                .build();
        svc.query(spec, 0, 10, Map.of("page:stateId", 42L));
        // City has no tenant_id auto-scope → only the explicit filter binding.
        org.mockito.ArgumentCaptor<Object> a = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(jdbc).queryForList(anyString(), a.capture());
        assertThat(a.getValue()).isEqualTo(42L);
    }

    @Test
    @DisplayName("unknown op → 422 IllegalArgumentException")
    void unknownOp() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .filter(DataSourceSpec.Filter.builder().field("name").op("REGEX").value("x").build())
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported");
    }

    @Test
    @DisplayName("identifier injection attempt rejected")
    void identifierInjection() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .filter(DataSourceSpec.Filter.builder()
                        .field("name; DROP TABLE m0_vendor").op("eq").value("x").build())
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("on-clause injection (comment / semicolon) rejected")
    void onClauseInjection() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(DataSourceSpec.Join.builder().entity("City").as("city")
                        .on("m0_vendor.city_id = city.id; DROP TABLE m0_vendor").build()))
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("forbidden");
    }

    @Test
    @DisplayName("pagination computes correct LIMIT/OFFSET + totalPages")
    void paginationMath() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(57L);
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").entity("Vendor").build();
        DataSourceSpec.Response r = svc.query(spec, 2, 25, Map.of());
        assertThat(r.getTotalElements()).isEqualTo(57L);
        assertThat(r.getTotalPages()).isEqualTo(3);
        assertThat(r.getNumber()).isEqualTo(2);
        assertThat(r.getSize()).isEqualTo(25);
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("LIMIT 25 OFFSET 50")), any(Object[].class));
    }

    @Test
    @DisplayName("unknown entity → 422")
    void unknownEntity() {
        when(registry.findByCode("Nope")).thenReturn(Optional.empty());
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").entity("Nope").build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown entity");
    }
}
