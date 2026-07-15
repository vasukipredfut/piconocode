package com.aria.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/** Request body for {@code POST /api/m11/instances} (also PUT for updates). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationInstanceRequest {
    private String code;
    private String connectorCode;
    private Integer connectorVersion;
    private String name;
    private String description;
    private Map<String, Object> configJson;
    private Map<String, Object> mappingJson;
    private List<String> mappedWorkflowCodes;
    private String dataResidencyFlag;
    private List<String> ipAllowlistCidr;
    private Integer rateLimitRps;
    private Map<String, Object> customFields;
    private Map<String, Object> credentials;
}
