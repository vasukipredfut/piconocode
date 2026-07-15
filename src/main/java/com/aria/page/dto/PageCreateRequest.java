package com.aria.page.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /api/m0/pages request body. */
@Data
@NoArgsConstructor
public class PageCreateRequest {
    @NotBlank @Size(max = 128) private String code;
    @NotBlank @Size(max = 256) private String title;
    @Size(max = 64) private String category;
    private String description;
    /**
     * Initial draft body. Stored verbatim — the runtime Renderer validates
     * against the JSON Page Schema; the server keeps it opaque for now.
     */
    private String bodyJson;
}
