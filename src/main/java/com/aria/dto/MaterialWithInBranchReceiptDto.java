package com.aria.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.MaterialWithInBranchReceipt;
import com.aria.model.MaterialWithInBranchReceiptItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MaterialWithInBranchReceiptDto {

    private Long id;

    private String trnNo;

    private Long requestId;

    private Long issueId;


    private String issuePerson;

    private Long issuePersonId;


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

    private String issueFromFirm;

    private Long issueFromFirmId;


    private String requestDate;

    private String requestDateString;


    private String receivedOnDate;

    private String receivedOnDateString;

    private Long receivedBy;

    private String receivedByName;



    private String entryOnDate;

    private String entryOnDateString;


    private String status;

    private String issueDate;

    private String issueDateString;


    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<MaterialWithInBranchReceiptItemDto> withInBranchReceiptItems;


    public MaterialWithInBranchReceiptDto(MaterialWithInBranchReceipt withInBranchReceipt) {

        this.id = withInBranchReceipt.getId();
        this.trnNo = withInBranchReceipt.getTrnNo();
        this.requestId = withInBranchReceipt.getRequestId();
        this.issueId = withInBranchReceipt.getIssueId();

        if (withInBranchReceipt.getIssueBy() != null) {
            this.issuePerson = withInBranchReceipt.getIssueBy().getEmployeename();
            this.issuePersonId = withInBranchReceipt.getIssueBy().getId();
        }
        if (withInBranchReceipt.getRequestBranch() != null) {
            this.requestFromBranch = withInBranchReceipt.getRequestBranch().getBranchName();
            this.requestFromBranchId = withInBranchReceipt.getRequestBranch().getId();
        }
        if (withInBranchReceipt.getRequestOrg() != null) {
            this.requestFromFirm = withInBranchReceipt.getRequestOrg().getName();
            this.requestFromFirmId = withInBranchReceipt.getRequestOrg().getId();
        }
        if (withInBranchReceipt.getRequestDepartment() != null) {
            this.requestFromDepartment = withInBranchReceipt.getRequestDepartment().getName();
            this.requestFromDepartmentId = withInBranchReceipt.getRequestDepartment().getId();
        }

        if (withInBranchReceipt.getIssueBranch() != null) {
            this.issueFromBranch = withInBranchReceipt.getIssueBranch().getBranchName();
            this.issueFromBranchId = withInBranchReceipt.getIssueBranch().getId();
        }
        if (withInBranchReceipt.getIssueOrg() != null) {
            this.issueFromFirm = withInBranchReceipt.getIssueOrg().getName();
            this.issueFromFirmId = withInBranchReceipt.getIssueOrg().getId();
        }
        if (withInBranchReceipt.getIssueDepartment() != null) {
            this.issueFromDepartment = withInBranchReceipt.getIssueDepartment().getName();
            this.issueFromDepartmentId = withInBranchReceipt.getIssueDepartment().getId();
        }

        if (withInBranchReceipt.getReceivedBy() != null) {
            this.receivedBy = withInBranchReceipt.getReceivedBy().getId();
            this.receivedByName = withInBranchReceipt.getReceivedBy().getEmployeename();
        }

        this.status = withInBranchReceipt.getStatus();
        this.createdBy = withInBranchReceipt.getCreatedBy();
        this.updatedBy = withInBranchReceipt.getUpdatedBy();
        this.created_date = withInBranchReceipt.getCreated_date();
        this.updated_date = withInBranchReceipt.getUpdated_date();

        this.withInBranchReceiptItems = withInBranchReceipt.getWithInBranchReceiptItems() != null ? withInBranchReceipt.getWithInBranchReceiptItems()
                .stream().map(obj -> new MaterialWithInBranchReceiptItemDto(obj)).collect(Collectors.toList()) : null;
    }
}
