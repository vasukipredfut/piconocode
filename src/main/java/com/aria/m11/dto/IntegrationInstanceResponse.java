package com.aria.m11.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.m11.model.IntegrationInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/** Response shape for instance reads. config/mapping unmarshalled into
 *  trees so the UI consumes structured JSON instead of escaped strings. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationInstanceResponse {
    private Long id;
    private Long tenantId;
    private String code;
    private String connectorCode;
    private Integer connectorVersion;
    private String name;
    private String description;
    private String status;
    private String statusReason;
    private Integer currentVersion;
    private JsonNode configJson;
    private JsonNode mappingJson;
    private JsonNode mappedWorkflowCodes;
    private String dataResidencyFlag;
    private Integer rateLimitRps;
    private Instant lastHealthAt;
    private Instant lastEventAt;
    private Integer errorCount24h;
    private Instant createdAt;
    private Instant updatedAt;

    public static IntegrationInstanceResponse from(IntegrationInstance i, ObjectMapper mapper) {
        try {
            return IntegrationInstanceResponse.builder()
                    .id(i.getId()).tenantId(i.getTenantId())
                    .code(i.getCode()).connectorCode(i.getConnectorCode())
                    .connectorVersion(i.getConnectorVersion())
                    .name(i.getName()).description(i.getDescription())
                    .status(i.getStatus()).statusReason(i.getStatusReason())
                    .currentVersion(i.getCurrentVersion())
                    .configJson(safeTree(mapper, i.getConfigJson()))
                    .mappingJson(safeTree(mapper, i.getMappingJson()))
                    .mappedWorkflowCodes(safeTree(mapper, i.getMappedWorkflowCodesJson()))
                    .dataResidencyFlag(i.getDataResidencyFlag())
                    .rateLimitRps(i.getRateLimitRps())
                    .lastHealthAt(i.getLastHealthAt()).lastEventAt(i.getLastEventAt())
                    .errorCount24h(i.getErrorCount24h())
                    .createdAt(i.getCreatedAt()).updatedAt(i.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("response build failed", e);
        }
    }

    private static JsonNode safeTree(ObjectMapper m, String json) {
        if (json == null || json.isBlank()) return null;
        try { return m.readTree(json); }
        catch (Exception e) { return null; }
    }

    /** Convenience used by tests/controllers needing a Map view. */
    public Map<String, Object> asMap(ObjectMapper m) {
        return m.convertValue(this, Map.class);
    }
}
