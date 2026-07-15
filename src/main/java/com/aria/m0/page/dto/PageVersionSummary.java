package com.aria.m0.page.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** GET /api/m0/pages/{code}/versions list row. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageVersionSummary {
    private Long id;
    private Integer version;
    private String status;
    private Instant publishedAt;
    private Long publishedBy;
    private Instant createdAt;
    private String rationale;
}
