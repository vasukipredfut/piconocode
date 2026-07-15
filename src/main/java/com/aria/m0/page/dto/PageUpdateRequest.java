package com.aria.m0.page.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** PUT /api/m0/pages/{code} request body. Updates the active DRAFT version. */
@Data
@NoArgsConstructor
public class PageUpdateRequest {
    @Size(max = 256) private String title;
    private String description;
    @Size(max = 64) private String category;
    private String bodyJson;
    private String rationale;
}
