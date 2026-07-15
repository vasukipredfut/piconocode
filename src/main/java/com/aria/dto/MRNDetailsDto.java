package com.aria.dto;

import com.aria.model.MaterialWithInBranchReceipt;
import com.aria.model.WithInBranchReceive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class MRNDetailsDto {

    private Long id;


    private String trnNo;

    private Long requestId;

    private Long issueId;


    private String issuePerson;

    private Long issuePersonId;


    private String requestName;

    private Long requestPerson;

    private String requestPersonName;


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

    private String issueDate;

    private String issueDateString;


    private List<Map<String, Object>> stringObjectMapValue ;


    public MRNDetailsDto(MaterialWithInBranchReceipt withInBranchReceipt) {
        this.id = withInBranchReceipt.getId();
        this.trnNo =  withInBranchReceipt.getTrnNo();
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

    }

    public MRNDetailsDto(WithInBranchReceive withInBranchReceipt) {
        this.id = withInBranchReceipt.getId();
        this.trnNo =  withInBranchReceipt.getTrnNo();
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

    }
}
