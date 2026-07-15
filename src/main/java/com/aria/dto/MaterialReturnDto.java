package com.aria.dto;


import com.aria.model.MaterialReturn;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Getter
@Setter
@NoArgsConstructor
public class MaterialReturnDto {

    private Long id;

    private Long requestId;

    private String requestName;

    private String requestDescription;

    private String issuePerson;

    private Long issuePersonId;

    private String requestIdString;

    private String requestFrom;

    private String requestFromDepartment;

    private Long requestFromDepartmentId;

    private String requestFromName;

    private Long requestFromId;

    private String requestFromFirm;

    private Long requestFromFirmId;


    private String issueFromDepartment;

    private Long issueFromDepartmentId;

    private String issueFrom;

    private String issueFromName;

    private Long issueFromId;

    private String requestDate;

    private String trnNo;

    private String requestDateString;

    private String status;

    private String issueDate;

    private String issueDateString;

    private String issueDescription;


    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<MaterialReturnItemsDto> returnItems;


    private String dispatchOn;
    private String returnDate;


    public MaterialReturnDto(MaterialReturn materialIssue) {
        this.id = materialIssue.getId();
        this.requestName = materialIssue.getRequestName();
        this.requestDescription = materialIssue.getRequestDescription();
        this.requestFrom = materialIssue.getFromName();
        if(materialIssue.getRequestId() != null) {
            this.requestId = materialIssue.getRequestId();
            this.requestIdString = "RQ-" + materialIssue.getRequestId();
        }
        if(materialIssue.getIssueBy() != null){
            this.issuePerson = materialIssue.getIssueBy().getEmployeename();
            this.issuePersonId = materialIssue.getIssueBy().getId();
        }

        this.trnNo = materialIssue.getTrnNo();
        this.createdBy = materialIssue.getCreatedBy();
        this.updatedBy = materialIssue.getUpdatedBy();
        this.created_date = materialIssue.getCreated_date();


        this.issueFromDepartment = materialIssue.getToDeptName();
        this.issueFromDepartmentId = materialIssue.getToDeptId();

        this.requestFromDepartment = materialIssue.getFromDeptName();
        this.requestFromDepartmentId = materialIssue.getFromDeptId();


        if (materialIssue.getUpdated_date() != null) {
            this.updated_date = materialIssue.getUpdated_date();
        } else {
            this.updated_date = materialIssue.getCreated_date();
        }
        this.returnItems = materialIssue.getReturnItems() != null ? materialIssue
                .getReturnItems().stream().map(obj -> new MaterialReturnItemsDto(obj)).collect(Collectors.toList()) : null;
    }

   /* public MaterialReturnDto(MaterialIssue materialIssue) {
        this.id = materialIssue.getId();
        this.requestName = materialIssue.getRequestName();
        this.requestDescription = materialIssue.getRequestDescription();
        this.requestId = materialIssue.getRequestId();
        this.requestIdString = "RQ-"+materialIssue.getRequestId();
        if(materialIssue.getIssueBy() != null){
            this.issuePerson = materialIssue.getIssueBy().getEmployeename();
            this.issuePersonId = materialIssue.getIssueBy().getId();
        }
        if (materialIssue.getRequestBranch() != null) {
            this.requestFromBranch = materialIssue.getRequestBranch().getBranchName();
            this.requestFromBranchId = materialIssue.getRequestBranch().getId();
        }
        if (materialIssue.getOrganisation() != null) {
            this.requestFromFirm = materialIssue.getOrganisation().getName();
            this.requestFromFirmId = materialIssue.getOrganisation().getId();
        }
        if (materialIssue.getRequestDepartment() != null) {
            this.requestFromDepartment = materialIssue.getRequestDepartment().getName();
            this.requestFromDepartmentId = materialIssue.getRequestDepartment().getId();
        }
        if (materialIssue.getIssueBranch() != null) {
            this.issueFromBranch = materialIssue.getIssueBranch().getBranchName();
            this.issueFromBranchId = materialIssue.getIssueBranch().getId();
        }
        if (materialIssue.getIssueDepartment() != null) {
            this.issueFromDepartment = materialIssue.getIssueDepartment().getName();
            this.issueFromDepartmentId = materialIssue.getIssueDepartment().getId();
        }
        this.trnNo = materialIssue.getTrnNo();
        this.createdBy = materialIssue.getCreatedBy();
        this.updatedBy = materialIssue.getUpdatedBy();
        this.created_date = materialIssue.getCreated_date();
        if (materialIssue.getUpdated_date() != null) {
            this.updated_date = materialIssue.getUpdated_date();
        } else {
            this.updated_date = materialIssue.getCreated_date();
        }
        this.returnItems = materialIssue.getIssueItems() != null ? materialIssue
                .getIssueItems().stream().map(obj -> new MaterialReturnItemsDto(obj)).collect(Collectors.toList()) : null;
    }*/
}
