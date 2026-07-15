package com.aria.integration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Append-only event-log row. Written by every outbound caller and inbound
 * receiver. PII-redacted view is in {@code redactedJson}; raw payloads in
 * {@code requestJson}/{@code responseJson} are retained for trusted
 * audit-role consumption only.
 */
@Entity
@Table(name = "integration_event_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationEventLog {

    public static final String DIR_INBOUND  = "INBOUND";
    public static final String DIR_OUTBOUND = "OUTBOUND";
    public static final String DIR_STREAM   = "STREAM";
    public static final String DIR_SCHEDULE = "SCHEDULE";

    public static final String STATUS_OK       = "OK";
    public static final String STATUS_FAIL     = "FAIL";
    public static final String STATUS_DUP      = "DUP";
    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_CB_OPEN  = "CB_OPEN";
    public static final String STATUS_REJECTED = "REJECTED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(nullable = false, length = 16)
    private String direction;

    @Column(insertable = false, updatable = false)
    private Instant ts;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "request_json", columnDefinition = "JSON")
    private String requestJson;

    @Column(name = "response_json", columnDefinition = "JSON")
    private String responseJson;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "redacted_json", columnDefinition = "JSON")
    private String redactedJson;
}
