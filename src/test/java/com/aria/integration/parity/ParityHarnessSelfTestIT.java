package com.aria.integration.parity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Self-test for {@link SubstrateParityHarness}: proves each guard actually
 * FAILS on a faulty registration. A verification harness that can only go green
 * is worthless — these tests are the evidence that a bad migration would be
 * caught rather than waved through.
 *
 * <p>Uses a synthetic {@code parity_selftest} table so it never touches a real
 * entity.
 */
@DisplayName("Parity harness · self-test: the guards bite on bad registrations")
class ParityHarnessSelfTestIT extends SubstrateParityHarness {

    private static final String CODE = "ParitySelfTest";
    private static final String TABLE = "parity_selftest";

    @BeforeEach
    void makeTable() {
        jdbc.execute("DROP TABLE IF EXISTS " + TABLE);
        jdbc.execute("CREATE TABLE " + TABLE + " (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(64), " +
                "qty INT" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        seedRow(TABLE, cols("id", 1L, "name", "widget", "qty", 7));
    }

    @AfterEach
    void drop() {
        unregister(CODE);
        jdbc.execute("DROP TABLE IF EXISTS " + TABLE);
    }

    @Test
    @DisplayName("P1 catches an UNREGISTERED physical column (silent save-drop)")
    void catchesUnregisteredColumn() {
        registerEntity(CODE, TABLE);
        registerField(CODE, "name", "STRING");
        // 'qty' deliberately left unregistered — the substrate form would drop it.
        assertThatThrownBy(() -> assertSchemaFaithful(CODE, TABLE, Set.of("id")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("qty");
    }

    @Test
    @DisplayName("P1 catches a PHANTOM field with no column (silent read/write fail)")
    void catchesPhantomField() {
        registerEntity(CODE, TABLE);
        registerField(CODE, "name", "STRING");
        registerField(CODE, "qty", "INT");
        registerField(CODE, "nam", "STRING"); // typo — the camelCase/snake_case bug class
        assertThatThrownBy(() -> assertSchemaFaithful(CODE, TABLE, Set.of("id")))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("nam");
    }

    @Test
    @DisplayName("P2 catches a value mismatch between substrate read and ground truth")
    void catchesReadMismatch() {
        registerEntity(CODE, TABLE);
        registerField(CODE, "name", "STRING");
        registerField(CODE, "qty", "INT");
        // Ground truth is name='widget'; assert a wrong expectation and require it to fail.
        assertThatThrownBy(() -> assertReadParity(CODE, 1L, Map.of("name", "gadget")))
                .isInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("a CORRECT registration passes all three (positive control)")
    void correctRegistrationPasses() {
        registerEntity(CODE, TABLE);
        registerField(CODE, "name", "STRING");
        registerField(CODE, "qty", "INT");
        assertSchemaFaithful(CODE, TABLE, Set.of("id"));
        assertReadParity(CODE, 1L, Map.of("name", "widget", "qty", 7));
        assertThat(true).isTrue();
    }
}
