package com.aria.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.AdminInspection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor

public class AdminInspectionDto {

    private Long id;

    private String date;

    private Long branchId;

    private String branchName;

    private String createdByName;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Map<String, Object> inspectionDetailsValue;

    public AdminInspectionDto(AdminInspection adminInspection) {
        this.id = adminInspection.getId();
        if (adminInspection.getBranch() != null) {
            this.branchId = adminInspection.getBranch().getId();
            this.branchName = adminInspection.getBranch().getBranchName();
        }
        this.createdBy = adminInspection.getCreatedBy();
        this.updatedBy = adminInspection.getUpdatedBy();
        this.created_date = adminInspection.getCreated_date();
        if (adminInspection.getUpdated_date() != null) {
            this.updated_date = adminInspection.getUpdated_date();
        } else {
            this.updated_date = adminInspection.getCreated_date();
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.inspectionDetailsValue = mapper.readValue(adminInspection.getInspectionDetailsValue(), HashMap.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
