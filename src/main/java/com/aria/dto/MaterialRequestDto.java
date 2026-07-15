
package com.aria.dto;


import com.aria.model.Branch;
import com.aria.model.MaterialRequest;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class MaterialRequestDto {

    private Long id;

    private String requestId;

    private String requestName;

    private String requestDescription;

    private Long requestById;

    private String requestBy;

    private String requestFrom;

    private String requestFromDepartment;

    private Long requestFromDepartmentId;

    private String requestFromBranch;

    private Long requestFromBranchId;

    private String requestFromFirm;

    private Long requestFromFirmId;

    private String requestDate;

    private String status;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<RequestItemDto> requestItems;

    public MaterialRequestDto(MaterialRequest materialRequest, Branch branch) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        this.requestId = "RQ - " + materialRequest.getId();
        this.requestName = materialRequest.getRequestDescription();
        if (materialRequest.getRequestBy() != null) {
            this.requestById = materialRequest.getRequestBy().getId();
            this.requestBy = materialRequest.getRequestBy().getEmployeename();
            if (branch != null) {
                this.requestFrom = branch.getBranchName();
            }
        }
        if (materialRequest.getBranch() != null) {
            this.requestFromBranch = materialRequest.getBranch().getBranchName();
            this.requestFromBranchId = materialRequest.getBranch().getId();
        }
        if (materialRequest.getOrganisation() != null) {
            this.requestFromFirm = materialRequest.getOrganisation().getName();
            this.requestFromFirmId = materialRequest.getOrganisation().getId();
        }
        if (materialRequest.getDepartment() != null) {
            this.requestFromDepartment = materialRequest.getDepartment().getName();
            this.requestFromDepartmentId = materialRequest.getDepartment().getId();
        }
        this.status = materialRequest.getStatus();
        this.requestDescription = materialRequest.getRequestDescription();
        this.createdBy = materialRequest.getCreatedBy();
        this.updatedBy = materialRequest.getUpdatedBy();
        this.created_date = materialRequest.getCreated_date();
        this.requestDate = dateUtil.getDateTOStringWk(materialRequest.getRequestDate());
        if (materialRequest.getUpdated_date() != null) {
            this.updated_date = materialRequest.getUpdated_date();
        } else {
            this.updated_date = materialRequest.getCreated_date();
        }
        this.requestItems = materialRequest.getRequestItems() != null ? materialRequest
                .getRequestItems().stream().map(obj -> new RequestItemDto(obj)).collect(Collectors.toList()) : null;
    }

    public MaterialRequestDto(MaterialRequestList materialRequest) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        this.requestId = "RQ - " + materialRequest.getId();
        this.requestName = materialRequest.getRequestDescription();
        this.requestFromBranch = materialRequest.getRequestFromBranch();
        this.requestFromBranchId = materialRequest.getRequestFromBranchId();
        this.requestFromDepartment = materialRequest.getRequestFromDepartment();
        this.requestFromDepartmentId = materialRequest.getRequestFromDepartmentId();
        this.status = materialRequest.getStatus();
        this.requestDescription = materialRequest.getRequestDescription();
        this.requestDate = dateUtil.getDateTOStringWk(materialRequest.getRequestDate());


    }
}