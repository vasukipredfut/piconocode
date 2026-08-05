package com.aria.datasource;

import com.aria.entity_registry.EntityRegistryService;
import com.aria.entity_registry.model.EntityFieldRegistryEntry;
import com.aria.entity_registry.model.EntityRegistryEntry;
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
        // m0_vendor carries a tenant_id; city/state are shared lookups that do
        // not. In production this membership is read from information_schema.
        svc = new DataSourceQueryService(registry, jdbc,
                com.aria.security.TenantScope.forTables("m0_vendor"));
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
                && s.contains("WHERE m0_vendor.tenant_id IN (?, -1)")
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

    // ---------------------------------------------------------- groupBy

    @Test
    @DisplayName("groupBy emits a max-id-per-group subquery joined back for the full latest row, still auto-tenant-scoped")
    void groupBySingleField() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .groupBy(List.of("status"))
                .build();
        svc.query(spec, 0, 25, Map.of());
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("SELECT m0_vendor.*, g.count AS count FROM (SELECT m0_vendor.status, MAX(m0_vendor.id) AS max_id, COUNT(*) AS count FROM m0_vendor")
                && s.contains("WHERE m0_vendor.tenant_id IN (?, -1)")
                && s.contains("GROUP BY m0_vendor.status) g")
                && s.contains("JOIN m0_vendor ON m0_vendor.id = g.max_id")
                && s.contains("ORDER BY g.max_id DESC")
                && s.contains("LIMIT 25")), any(Object[].class));
    }

    @Test
    @DisplayName("groupBy with multiple fields groups by all of them")
    void groupByMultiField() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .groupBy(List.of("status", "name"))
                .build();
        svc.query(spec, 0, 25, Map.of());
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("GROUP BY m0_vendor.status, m0_vendor.name) g")), any(Object[].class));
    }

    @Test
    @DisplayName("groupBy honours an explicit sort, including sorting by count")
    void groupBySortByCount() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .groupBy(List.of("status"))
                .sort(List.of(DataSourceSpec.Sort.builder().field("count").dir("desc").build()))
                .build();
        svc.query(spec, 0, 25, Map.of());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("ORDER BY g.count DESC")), any(Object[].class));
    }

    @Test
    @DisplayName("groupBy still applies the user filter")
    void groupByAppliesFilter() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .groupBy(List.of("status"))
                .filter(DataSourceSpec.Filter.builder().field("name").op("contains").value("acme").build())
                .build();
        svc.query(spec, 0, 25, Map.of());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.name LIKE ?")), any(Object[].class));
    }

    @Test
    @DisplayName("groupBy pagination counts distinct groups, not raw rows")
    void groupByPaginationCountsGroups() {
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(4L);
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .groupBy(List.of("status"))
                .build();
        DataSourceSpec.Response r = svc.query(spec, 0, 25, Map.of());
        assertThat(r.getTotalElements()).isEqualTo(4L);
        verify(jdbc).queryForObject(argThat((String s) ->
                s.contains("SELECT COUNT(*) FROM (SELECT 1 FROM m0_vendor")
                && s.contains("GROUP BY m0_vendor.status) grouped_")), eq(Long.class), any(Object[].class));
    }

    // ---------------------------------------------------------- filter ops

    private DataSourceSpec.Response queryWithFilter(DataSourceSpec.Filter f) {
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").entity("Vendor").filter(f).build();
        return svc.query(spec, 0, 10, Map.of());
    }

    @Test
    @DisplayName("op=ne compiles to <>")
    void opNe() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("status").op("ne").value("CLOSED").build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.status <> ?")), any(Object[].class));
    }

    @Test
    @DisplayName("op=lt/lte/gt/gte compile to the matching comparison operator")
    void opComparisons() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("id").op("lt").value(10).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.id < ?")), any(Object[].class));

        queryWithFilter(DataSourceSpec.Filter.builder().field("id").op("lte").value(10).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.id <= ?")), any(Object[].class));

        queryWithFilter(DataSourceSpec.Filter.builder().field("id").op("gt").value(10).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.id > ?")), any(Object[].class));

        queryWithFilter(DataSourceSpec.Filter.builder().field("id").op("gte").value(10).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.id >= ?")), any(Object[].class));
    }

    @Test
    @DisplayName("op=in with a non-empty list binds N placeholders")
    void opInNonEmpty() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("status").op("in")
                .value(List.of("ACTIVE", "PENDING", "CLOSED")).build());
        org.mockito.ArgumentCaptor<Object[]> args = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.status IN (?, ?, ?)")), args.capture());
        assertThat(args.getValue()).contains("ACTIVE", "PENDING", "CLOSED");
    }

    @Test
    @DisplayName("op=in with an empty array binds no placeholders and short-circuits to 1=0")
    void opInEmptyArray() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("status").op("in").value(new String[0]).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("1 = 0")), any(Object[].class));
    }

    @Test
    @DisplayName("op=in accepts a raw array value, not just a List")
    void opInArrayValue() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("id").op("in").value(new Long[]{1L, 2L}).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.id IN (?, ?)")), any(Object[].class));
    }

    @Test
    @DisplayName("op=nin with a non-empty list compiles to NOT IN; empty list short-circuits to 1=1")
    void opNin() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("status").op("nin").value(List.of("CLOSED")).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.status NOT IN (?)")), any(Object[].class));

        queryWithFilter(DataSourceSpec.Filter.builder().field("status").op("nin").value(List.of()).build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("1 = 1")), any(Object[].class));
    }

    @Test
    @DisplayName("op=startswith anchors the LIKE pattern to the start of the value only")
    void opStartsWith() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("name").op("startswith").value("Acme").build());
        org.mockito.ArgumentCaptor<Object[]> args = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.name LIKE ?")), args.capture());
        assertThat(args.getValue()).contains("Acme%");
    }

    @Test
    @DisplayName("op=endswith anchors the LIKE pattern to the end of the value only")
    void opEndsWith() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("name").op("endswith").value("Corp").build());
        org.mockito.ArgumentCaptor<Object[]> args = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.name LIKE ?")), args.capture());
        assertThat(args.getValue()).contains("%Corp");
    }

    @Test
    @DisplayName("op=isnull/isnotnull emit no bind parameter of their own — only the auto tenant-scope arg is present")
    void opNullChecks() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("gstin").op("isnull").build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.gstin IS NULL")), any(Object[].class));

        queryWithFilter(DataSourceSpec.Filter.builder().field("gstin").op("isnotnull").build());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.gstin IS NOT NULL")), any(Object[].class));
    }

    @Test
    @DisplayName("op=between binds both the low and high value")
    void opBetween() {
        queryWithFilter(DataSourceSpec.Filter.builder().field("id").op("between").value(1).valueTo(100).build());
        org.mockito.ArgumentCaptor<Object[]> args = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForList(argThat((String s) -> s.contains("m0_vendor.id BETWEEN ? AND ?")), args.capture());
        assertThat(args.getValue()).contains(1, 100);
    }

    @Test
    @DisplayName("top-level bare OR (no enclosing AND) compiles directly")
    void topLevelOr() {
        DataSourceSpec.Filter or = DataSourceSpec.Filter.builder().or(List.of(
                DataSourceSpec.Filter.builder().field("status").op("eq").value("A").build(),
                DataSourceSpec.Filter.builder().field("status").op("eq").value("B").build()
        )).build();
        queryWithFilter(or);
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("(m0_vendor.status = ? OR m0_vendor.status = ?)")), any(Object[].class));
    }

    @Test
    @DisplayName("top-level NOT negates the wrapped predicate")
    void topLevelNot() {
        DataSourceSpec.Filter not = DataSourceSpec.Filter.builder()
                .not(DataSourceSpec.Filter.builder().field("status").op("eq").value("CLOSED").build())
                .build();
        queryWithFilter(not);
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("NOT m0_vendor.status = ?")), any(Object[].class));
    }

    // ---------------------------------------------------------- bindings

    @Test
    @DisplayName("ctx: prefix with an unrecognised key and no matching binding resolves to null")
    void ctxBindingUnresolvedReturnsNull() {
        Object v = svc.resolveBinding("ctx:branchId", Map.of());
        assertThat(v).isNull();
    }

    @Test
    @DisplayName("ctx: prefix falls back to the bindings map under \"ctx.<rest>\"")
    void ctxBindingFromMap() {
        Object v = svc.resolveBinding("ctx:branchId", Map.of("ctx.branchId", 77L));
        assertThat(v).isEqualTo(77L);
    }

    @Test
    @DisplayName("user:/route:/pageData:/workflowState: prefixes resolve from the prefixed key first")
    void otherPrefixesResolveFromPrefixedKey() {
        assertThat(svc.resolveBinding("user:branch", Map.of("user:branch", "north"))).isEqualTo("north");
        assertThat(svc.resolveBinding("route:id", Map.of("route:id", 5L))).isEqualTo(5L);
        assertThat(svc.resolveBinding("pageData:orderId", Map.of("pageData:orderId", 9L))).isEqualTo(9L);
        assertThat(svc.resolveBinding("workflowState:toState", Map.of("workflowState:toState", "DONE")))
                .isEqualTo("DONE");
    }

    @Test
    @DisplayName("user:/page: prefixes fall back to the bare key when the prefixed key is absent")
    void prefixesFallBackToBareKey() {
        assertThat(svc.resolveBinding("page:stateId", Map.of("stateId", 42L))).isEqualTo(42L);
        assertThat(svc.resolveBinding("user:branch", Map.of("branch", "south"))).isEqualTo("south");
    }

    @Test
    @DisplayName("recognised-prefix binding absent from a null bindings map resolves to null, not NPE")
    void prefixBindingNullMap() {
        assertThat(svc.resolveBinding("page:stateId", null)).isNull();
    }

    @Test
    @DisplayName("an unrecognised prefix is treated as a literal value, not a binding")
    void unknownPrefixPassesThroughAsLiteral() {
        assertThat(svc.resolveBinding("foo:bar", Map.of())).isEqualTo("foo:bar");
    }

    @Test
    @DisplayName("a plain string with no colon is returned unchanged")
    void noColonPassesThrough() {
        assertThat(svc.resolveBinding("plain-value", Map.of())).isEqualTo("plain-value");
    }

    @Test
    @DisplayName("a non-string value (already resolved) passes through untouched")
    void nonStringValuePassesThrough() {
        assertThat(svc.resolveBinding(42L, Map.of())).isEqualTo(42L);
    }

    // ---------------------------------------------------------- joins

    @Test
    @DisplayName("kind=inner emits INNER JOIN instead of the LEFT JOIN default")
    void innerJoinKind() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(DataSourceSpec.Join.builder().entity("City").as("city")
                        .kind("inner").on("m0_vendor.city_id = city.id").build()))
                .build();
        svc.query(spec, 0, 10, Map.of());
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("INNER JOIN city city ON m0_vendor.city_id = city.id")), any(Object[].class));
    }

    @Test
    @DisplayName("join missing entity/as/on throws")
    void joinMissingFieldsThrows() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(DataSourceSpec.Join.builder().entity("City").build()))
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("join requires entity/as/on");
    }

    @Test
    @DisplayName("join referencing an unregistered entity throws")
    void joinUnknownEntityThrows() {
        when(registry.findByCode("Nope")).thenReturn(Optional.empty());
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(DataSourceSpec.Join.builder().entity("Nope").as("n")
                        .on("m0_vendor.id = n.id").build()))
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown join entity: Nope");
    }

    // ---------------------------------------------------------- select

    @Test
    @DisplayName("no select list defaults to rootTable.*")
    void defaultSelectStar() {
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").entity("Vendor").build();
        svc.query(spec, 0, 10, Map.of());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("SELECT m0_vendor.*")), any(Object[].class));
    }

    @Test
    @DisplayName("select field with an unsafe identifier throws before hitting the DB")
    void selectUnsafeIdentifierThrows() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .select(List.of("name; DROP TABLE m0_vendor"))
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---------------------------------------------------------- sort

    @Test
    @DisplayName("sorting by a bare join-alias column (camelCase) is left unqualified, not prefixed with rootTable")
    void sortByJoinAliasNotQualified() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(DataSourceSpec.Join.builder().entity("City").as("city")
                        .on("m0_vendor.city_id = city.id")
                        .fields(List.of("city.name as cityName")).build()))
                .sort(List.of(DataSourceSpec.Sort.builder().field("cityName").dir("asc").build()))
                .build();
        svc.query(spec, 0, 10, Map.of());
        verify(jdbc).queryForList(argThat((String s) ->
                s.contains("ORDER BY cityName ASC") && !s.contains("m0_vendor.cityName")), any(Object[].class));
    }

    @Test
    @DisplayName("sorting by a dotted alias.field resolves verbatim")
    void sortByAliasQualifiedField() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .joins(List.of(DataSourceSpec.Join.builder().entity("City").as("city")
                        .on("m0_vendor.city_id = city.id").build()))
                .sort(List.of(DataSourceSpec.Sort.builder().field("city.name").dir("desc").build()))
                .build();
        svc.query(spec, 0, 10, Map.of());
        verify(jdbc).queryForList(argThat((String s) -> s.contains("ORDER BY city.name DESC")), any(Object[].class));
    }

    @Test
    @DisplayName("field qualified with an alias not present in joins throws")
    void filterFieldUnknownAliasThrows() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .filter(DataSourceSpec.Filter.builder().field("ghost.name").op("eq").value("x").build())
                .build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown alias in field: ghost.name");
    }

    // ---------------------------------------------------------- spec validation

    @Test
    @DisplayName("null spec throws")
    void nullSpecThrows() {
        assertThatThrownBy(() -> svc.query(null, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spec.entity is required");
    }

    @Test
    @DisplayName("spec with no entity throws")
    void missingEntityThrows() {
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spec.entity is required");
    }

    @Test
    @DisplayName("a non-entity type is rejected — only type=entity is implemented")
    void nonEntityTypeThrows() {
        DataSourceSpec spec = DataSourceSpec.builder().type("lookup").entity("Vendor").build();
        assertThatThrownBy(() -> svc.query(spec, 0, 10, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only type=entity is implemented");
    }

    @Test
    @DisplayName("size <= 0 falls back to the default page size of 25")
    void nonPositiveSizeDefaultsTo25() {
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").entity("Vendor").build();
        DataSourceSpec.Response r = svc.query(spec, 0, 0, Map.of());
        assertThat(r.getSize()).isEqualTo(25);
        verify(jdbc).queryForList(argThat((String s) -> s.contains("LIMIT 25 OFFSET 0")), any(Object[].class));
    }

    @Test
    @DisplayName("a negative page number is clamped to 0")
    void negativePageClampedToZero() {
        DataSourceSpec spec = DataSourceSpec.builder().type("entity").entity("Vendor").build();
        DataSourceSpec.Response r = svc.query(spec, -5, 10, Map.of());
        assertThat(r.getNumber()).isEqualTo(0);
        verify(jdbc).queryForList(argThat((String s) -> s.contains("LIMIT 10 OFFSET 0")), any(Object[].class));
    }
}
