package com.aria.page.dto;

import com.aria.page.model.PageWorkflowBinding.BindingRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

/** POST /api/m0/pages/{code}/binding request body. */
@Data
@NoArgsConstructor
public class PageBindingRequest {
    @NotBlank private String workflowCode;
    @NotBlank private String stepCode;
    private BindingRole role = BindingRole.INPUT;
    private String fieldMapJson;
}
