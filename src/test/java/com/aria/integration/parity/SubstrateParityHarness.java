package com.aria.integration.parity;

import com.aria._base.EarmsContainersExtension;
import com.aria.integration.m0.M0SubstrateTestApp;
import com.aria.entity.GenericEntityController;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reusable base for <b>substrate-parity</b> integration tests — the verification
 * tool the v2 substrate migration depends on (spec §17 step 7, §18).
 *
 * <p>The 1.5-person-year v2 rewrite deletes ~470 legacy {@code @Entity} classes
 * and repoints their reads/writes at {@link GenericEntityController} (registry-
 * driven CRUD). A deletion is only safe once we can <em>prove</em> the substrate
 * path reproduces the legacy table's data faithfully. This harness is that proof.
 *
 * <p>For a candidate entity it asserts three properties against a live MariaDB
 * (Testcontainers), all of which must hold before its legacy class may be deleted:
 * <ol>
 *   <li><b>Schema faithfulness</b> — every physical, non-generated column is a
 *       registered field, and every registered field maps to a real column.
 *       Catches the {@code field_code} camelCase-vs-snake_case mismatch class
 *       (see earmsbackend/CLAUDE.md) that silently drops columns from the
 *       substrate form, and columns nobody registered (silent data loss on save).</li>
 *   <li><b>Read parity</b> — {@code GET} through the substrate returns the same
 *       values that were written to the physical table by the legacy write path
 *       (here approximated by a direct seed — the DB is the shared ground truth).</li>
 *   <li><b>CRUD round-trip</b> — a substrate {@code POST} then {@code GET}
 *       returns byte-identical field values, proving writes land correctly.</li>
 * </ol>
 *
 * <p>Wiring: reuses {@link M0SubstrateTestApp} (already boots green across the
 * IT suite; Flyway (V1 + V2) builds the real
 * {@code entity_registry} / {@code entity_field_registry} tables). The
 * controller is a plain class taking a {@link JdbcTemplate}, so we construct it
 * directly from the test template — no new Spring wiring, no risk to sibling ITs.
 *
 * <p>Subclasses create the legacy table, seed ground-truth rows, and call
 * {@link #registerEntity}/{@link #registerField} with the <em>proposed</em>
 * registration (the exact rows the additive V-migration will ship), then assert
 * the three properties. A green subclass is the sign-off that the entity's
 * additive substrate registration is faithful.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = M0SubstrateTestApp.class)
@ActiveProfiles("test")
public abstract class SubstrateParityHarness {

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", EarmsContainersExtension::mariadbJdbcUrl);
        r.add("spring.datasource.username", EarmsContainersExtension::mariadbUsername);
        r.add("spring.datasource.password", EarmsContainersExtension::mariadbPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.mariadb.jdbc.Driver");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MariaDBDialect");
        r.add("spring.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.redis.port", EarmsContainersExtension::redisPort);
        r.add("spring.data.redis.host", EarmsContainersExtension::redisHost);
        r.add("spring.data.redis.port", EarmsContainersExtension::redisPort);
    }

    @Autowired
    protected JdbcTemplate jdbc;

    /** The substrate CRUD under test, constructed over the live container template. */
    protected GenericEntityController substrate;

    @BeforeEach
    void parityBaseSetup() {
        TenantContext.set(1L);
        substrate = new GenericEntityController(jdbc, new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
    }

    @AfterEach
    void parityBaseTeardown() {
        TenantContext.clear();
    }

    // ---- registration helpers (mirror exactly what an additive V-migration inserts) ----

    /**
     * Register a candidate entity with no RBAC restriction (read/write open) so
     * the parity assertions exercise the data path, not auth. Deletes any prior
     * rows first so the harness is re-runnable within a shared-container JVM.
     * Inserts only the NOT-NULL base columns so it works regardless of which
     * later registry ALTERs the test schema has picked up.
     */
    protected void registerEntity(String code, String table) {
        jdbc.update("DELETE FROM entity_field_registry WHERE entity_code=?", code);
        jdbc.update("DELETE FROM entity_registry WHERE code=?", code);
        jdbc.update("INSERT INTO entity_registry (code, display_name, table_name) VALUES (?,?,?)",
                code, code, table);
    }

    /** Register one field. {@code dataType} must be a valid registry ENUM member. */
    protected void registerField(String code, String fieldCode, String dataType) {
        jdbc.update("INSERT INTO entity_field_registry " +
                        "(entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) " +
                        "VALUES (?,?,?,?,?,?,?,?)",
                code, fieldCode, humanize(fieldCode), dataType, true, true, false, false);
    }

    /** Remove all registry rows for an entity (subclass @AfterEach hygiene). */
    protected void unregister(String code) {
        jdbc.update("DELETE FROM entity_field_registry WHERE entity_code=?", code);
        jdbc.update("DELETE FROM entity_registry WHERE code=?", code);
    }

    // ---- ground-truth seeding ----

    /** Insert a physical row with explicit column values; returns the id supplied in {@code cols}. */
    protected void seedRow(String table, LinkedHashMap<String, Object> cols) {
        String colList = String.join(",", cols.keySet());
        String marks = String.join(",", Collections.nCopies(cols.size(), "?"));
        jdbc.update("INSERT INTO " + table + " (" + colList + ") VALUES (" + marks + ")",
                cols.values().toArray());
    }

    // ---- the three parity assertions ----

    /**
     * P1 — schema faithfulness. Every physical, non-generated column (except the
     * ignore set, typically {@code id} + audit columns) must be a registered
     * field, and every registered field must map to a real column.
     */
    protected void assertSchemaFaithful(String code, String table, Set<String> ignoreColumns) {
        List<String> physical = jdbc.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema=DATABASE() AND table_name=? AND EXTRA NOT LIKE '%GENERATED%'",
                String.class, table);
        Set<String> registered = new HashSet<>(jdbc.queryForList(
                "SELECT field_code FROM entity_field_registry WHERE entity_code=?", String.class, code));

        List<String> unregistered = physical.stream()
                .filter(c -> !ignoreColumns.contains(c))
                .filter(c -> !registered.contains(c))
                .sorted().toList();
        assertThat(unregistered)
                .as("columns in %s with no registered field — substrate form would SILENTLY DROP these on save", table)
                .isEmpty();

        Set<String> physicalSet = new HashSet<>(physical);
        List<String> phantom = registered.stream()
                .filter(f -> !physicalSet.contains(f))
                .sorted().toList();
        assertThat(phantom)
                .as("registered fields for %s with no matching column — substrate reads/writes SILENTLY FAIL", code)
                .isEmpty();
    }

    /**
     * P2 — read parity. The substrate {@code GET} of {@code id} returns the
     * expected (ground-truth) value for every field in {@code expected}.
     * Comparison is value-based (stringified) to normalise JDBC's numeric
     * boxing across the legacy vs substrate read.
     */
    protected void assertReadParity(String code, long id, Map<String, Object> expected) {
        ResponseEntity<?> resp = substrate.getRow(code, id);
        assertThat(resp.getStatusCode().is2xxSuccessful())
                .as("substrate getRow %s#%d should succeed, got %s / %s", code, id, resp.getStatusCode(), resp.getBody())
                .isTrue();
        @SuppressWarnings("unchecked")
        Map<String, Object> row = (Map<String, Object>) resp.getBody();
        assertThat(row).as("substrate getRow %s#%d body", code, id).isNotNull();
        for (Map.Entry<String, Object> e : expected.entrySet()) {
            assertThat(str(row.get(e.getKey())))
                    .as("field '%s' of %s#%d", e.getKey(), code, id)
                    .isEqualTo(str(e.getValue()));
        }
    }

    /**
     * P3 — CRUD round-trip. A substrate {@code POST} of {@code payload} followed
     * by a {@code GET} of the generated id returns those same field values.
     */
    protected void assertCrudRoundTrip(String code, LinkedHashMap<String, Object> payload) {
        ResponseEntity<?> ins = substrate.insert(code, new LinkedHashMap<>(payload));
        assertThat(ins.getStatusCode().is2xxSuccessful())
                .as("substrate insert %s should succeed, got %s / %s", code, ins.getStatusCode(), ins.getBody())
                .isTrue();
        Map<?, ?> body = (Map<?, ?>) ins.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("ok")).as("insert ok flag (%s)", body).isEqualTo(true);
        Object idObj = body.get("id");
        assertThat(idObj).as("insert must return the generated id (%s)", body).isNotNull();
        long id = ((Number) idObj).longValue();
        assertReadParity(code, id, payload);
    }

    // ---- small utils ----

    /** Ordered map literal helper: {@code cols("name","India","currency","INR")}. */
    protected static LinkedHashMap<String, Object> cols(Object... kv) {
        if (kv.length % 2 != 0) throw new IllegalArgumentException("cols() needs key/value pairs");
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
        return m;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String humanize(String fieldCode) {
        String s = fieldCode.replace('_', ' ').trim();
        return s.isEmpty() ? fieldCode : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
