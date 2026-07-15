package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.MaterialIssue;

@Getter
@Setter
@NoArgsConstructor

public class MaterialIssueDto {

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

    private String requestFromBranch;

    private Long requestFromBranchId;

    private String requestFromFirm;

    private Long requestFromFirmId;

    private String issueFromDepartment;

    private Long issueFromDepartmentId;

    private String issueFromBranch;

    private Long issueFromBranchId;

    private String requestDate;

    private String trnNo;

    private String requestDateString;

    private String status;

    private String issueDate;

    private String issueDateString;

    private String issueDescription;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<IssueItemsDto> issueItems;
    private List<IssueItemsDto> allIssueItems;

    private List<IssueItemsBriefDto> issueBriefItems;

    private String type;

    private List<Long> binStockIds;

    private List<IssueItemsDto> requestItems;

    public MaterialIssueDto(MaterialIssue materialIssue) {

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
        this.status = materialIssue.getStatus();
        this.trnNo = materialIssue.getTrnNo();
        this.createdBy = materialIssue.getCreatedBy();
        this.updatedBy = materialIssue.getUpdatedBy();
        this.created_date = materialIssue.getCreated_date();
        if (materialIssue.getUpdated_date() != null) {
            this.updated_date = materialIssue.getUpdated_date();
        } else {
            this.updated_date = materialIssue.getCreated_date();
        }
        this.issueItems = materialIssue.getIssueItems() != null ? materialIssue
                .getIssueItems().stream().map(obj -> new IssueItemsDto(obj)).collect(Collectors.toList()) : null;

        this.allIssueItems = materialIssue.getAllIssueItems() != null ? materialIssue
                .getAllIssueItems().stream().map(obj -> new IssueItemsDto(obj)).collect(Collectors.toList()) : null;
    }

}