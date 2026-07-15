package com.aria.datasource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * POJO mirror of the Schema V2 §3 DataSource grammar — the JSON object the
 * frontend sends to {@code /api/m0/datasource/query}.
 *
 * <p>Field codes are case-sensitive and validated against the entity
 * registry by {@link DataSourceQueryService}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceSpec {

    /** entity | lookup | api | expr | static | csv | rest. Round 1G only
     *  honours {@code entity}; the others are reserved. */
    private String type;

    /** Canonical entity code (Vendor, City, …) resolved via entity_registry. */
    private String entity;

    /** Filter tree (AND/OR/NOT with predicates). See {@link Filter}. */
    private Filter filter;

    /** Zero or more joins (LEFT JOIN by default). */
    private List<Join> joins;

    /** Output column list. Plain field codes (relative to the entity) or
     *  alias-qualified field codes (e.g. "city.name as cityName"). */
    private List<String> select;

    /** Sort clauses. */
    private List<Sort> sort;

    /** Hard cap on result rows; pagination uses LIMIT/OFFSET on top. */
    private Integer limit;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Filter {
        private List<Filter> and;
        private List<Filter> or;
        private Filter not;
        private String field;
        private String op;
        private Object value;
        private Object valueTo;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Join {
        private String entity;
        private String as;
        private String on;
        /** left | inner. Default left. */
        private String kind;
        private List<String> fields;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sort {
        private String field;
        private String dir;
    }

    /** Request envelope. */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        private DataSourceSpec spec;
        private Integer page;
        private Integer size;
        private Map<String, Object> bindings;
    }

    /** Response envelope (frontend-friendly shape mirroring Spring's Page). */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private List<Map<String, Object>> content;
        private long totalElements;
        private int totalPages;
        private int number;
        private int size;
    }
}
