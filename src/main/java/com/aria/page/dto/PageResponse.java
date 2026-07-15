package com.aria.page.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** GET /api/m0/pages/{code} response — metadata merged with the body. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private String category;
    private String status;
    private Integer version;
    /** Raw JSON body of the served version (PUBLISHED or DRAFT). */
    private String bodyJson;
    private Instant updatedAt;
}
