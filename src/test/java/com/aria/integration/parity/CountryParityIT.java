package com.aria.integration.parity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

/**
 * Parity proof for {@code Country} — the first pilot of the v2 substrate
 * migration (spec §18 tranche 2a). Demonstrates that the additive substrate
 * registration for {@code country} is faithful to the legacy
 * {@code com.aria.model.Country} table BEFORE the legacy {@code @Entity} +
 * {@code CountryRepository} are deleted and {@code CommonService} /
 * {@code VendorService} are repointed.
 *
 * <p>The registration asserted here (entity + 3 fields) is exactly what the
 * additive V110 migration ships. Green here == the registration is safe to ship.
 */
@DisplayName("Parity · Country: substrate registration is faithful to the legacy table")
class CountryParityIT extends SubstrateParityHarness {

    private static final String CODE = "Country";
    private static final String TABLE = "country";

    @BeforeEach
    void seedLegacyAndRegister() {
        // 1. The legacy physical table (mirrors com.aria.model.Country's @Table).
        jdbc.execute("DROP TABLE IF EXISTS " + TABLE);
        jdbc.execute("CREATE TABLE " + TABLE + " (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(128), " +
                "currency VARCHAR(32), " +
                "symbols VARCHAR(16)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

        // 2. Ground truth — rows a legacy write path would have produced.
        //    (ASCII symbol stand-ins keep the assertion charset-independent.)
        seedRow(TABLE, cols("id", 1L, "name", "India", "currency", "INR", "symbols", "Rs"));
        seedRow(TABLE, cols("id", 2L, "name", "United States", "currency", "USD", "symbols", "$"));
        seedRow(TABLE, cols("id", 3L, "name", "United Kingdom", "currency", "GBP", "symbols", "GBP"));

        // 3. The PROPOSED additive registration (== the content of V110).
        registerEntity(CODE, TABLE);
        registerField(CODE, "name", "STRING");
        registerField(CODE, "currency", "STRING");
        registerField(CODE, "symbols", "STRING");
    }

    @AfterEach
    void cleanup() {
        unregister(CODE);
        jdbc.execute("DROP TABLE IF EXISTS " + TABLE);
    }

    @Test
    @DisplayName("P1 · every physical column is registered and every field maps to a column")
    void schemaFaithful() {
        // id is the surrogate key, surfaced by getRow but not an editable field.
        assertSchemaFaithful(CODE, TABLE, Set.of("id"));
    }

    @Test
    @DisplayName("P2 · substrate GET returns the seeded values byte-for-byte")
    void readParity() {
        assertReadParity(CODE, 1L, Map.of("name", "India", "currency", "INR", "symbols", "Rs"));
        assertReadParity(CODE, 2L, Map.of("name", "United States", "currency", "USD", "symbols", "$"));
        assertReadParity(CODE, 3L, Map.of("name", "United Kingdom", "currency", "GBP", "symbols", "GBP"));
    }

    @Test
    @DisplayName("P3 · substrate POST then GET round-trips a new country")
    void crudRoundTrip() {
        assertCrudRoundTrip(CODE, cols("name", "Germany", "currency", "EUR", "symbols", "EUR"));
    }
}
