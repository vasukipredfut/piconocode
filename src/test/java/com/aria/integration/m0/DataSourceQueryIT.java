package com.aria.integration.m0;

import com.aria._base.EarmsContainersExtension;
import com.aria.m0.datasource.DataSourceQueryService;
import com.aria.m0.datasource.DataSourceSpec;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IT for the Schema V2 DataSource compiler against a real MariaDB.
 * Exercises entity-only query, joins, filter pushdown, and cascading-
 * filter binding resolution against the seeded m0_vendor demo data.
 */
@ExtendWith({SpringExtension.class, EarmsContainersExtension.class})
@SpringBootTest(classes = M0SubstrateTestApp.class)
@ActiveProfiles("test")
class DataSourceQueryIT {

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

    @Autowired private DataSourceQueryService ds;

    @BeforeEach
    void setTenant() { TenantContext.set(1L); }

    @AfterEach
    void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("entity-only query returns the seeded vendor rows for tenant 1")
    void vendorListPaged() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .select(List.of("id", "name", "gstin", "status"))
                .build();
        DataSourceSpec.Response r = ds.query(spec, 0, 50, Map.of());
        assertThat(r.getTotalElements()).isGreaterThanOrEqualTo(20);
        assertThat(r.getContent()).isNotEmpty();
        assertThat(r.getContent().get(0)).containsKeys("id", "name");
    }

    @Test
    @DisplayName("joins to city + state populate display fields via aliased select")
    void joinsResolveDisplayFields() {
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
        DataSourceSpec.Response r = ds.query(spec, 0, 10, Map.of());
        assertThat(r.getContent()).isNotEmpty();
        Map<String, Object> row = r.getContent().get(0);
        assertThat(row).containsKey("cityName");
        assertThat(row).containsKey("stateName");
    }

    @Test
    @DisplayName("contains filter narrows rows; bind args are parameterised")
    void containsFilter() {
        DataSourceSpec spec = DataSourceSpec.builder()
                .type("entity").entity("Vendor")
                .filter(DataSourceSpec.Filter.builder()
                        .field("name").op("contains").value("TES").build())
                .select(List.of("id", "name"))
                .build();
        DataSourceSpec.Response r = ds.query(spec, 0, 50, Map.of());
        // Seed has 3 vendors whose names start with TES (TESTRA, TESTRO, TESTUR).
        assertThat(r.getTotalElements()).isGreaterThanOrEqualTo(3);
        for (Map<String, Object> row : r.getContent()) {
            assertThat(((String) row.get("name")).toUpperCase()).contains("TES");
        }
    }

    @Test
    @DisplayName("cascading binding resolution: city query bound by page:stateId")
    void cascadingBinding() {
        // First query: pick a state id from the seed (Tamil Nadu).
        DataSourceSpec stateSpec = DataSourceSpec.builder()
                .type("entity").entity("State")
                .filter(DataSourceSpec.Filter.builder().field("name").op("eq").value("Tamil Nadu").build())
                .select(List.of("id", "name"))
                .build();
        DataSourceSpec.Response stateRows = ds.query(stateSpec, 0, 1, Map.of());
        assertThat(stateRows.getContent()).isNotEmpty();
        Object tnId = stateRows.getContent().get(0).get("id");

        // Second query: cities depending on the state id via page:stateId.
        DataSourceSpec citySpec = DataSourceSpec.builder()
                .type("entity").entity("City")
                .filter(DataSourceSpec.Filter.builder().field("stateid").op("eq")
                        .value("page:stateId").build())
                .select(List.of("id", "name"))
                .build();
        DataSourceSpec.Response cityRows = ds.query(citySpec, 0, 50, Map.of("page:stateId", tnId));
        assertThat(cityRows.getTotalElements()).isGreaterThanOrEqualTo(4); // TN cities: 4 in seed
        List<String> names = cityRows.getContent().stream().map(m -> (String) m.get("name")).toList();
        assertThat(names).contains("Chennai", "Coimbatore");
    }
}
