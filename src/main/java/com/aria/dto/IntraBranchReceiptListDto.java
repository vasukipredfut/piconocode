package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IntraBranchReceiptListDto {

    private Long id;
    private Long requestId;
    private String hupId;
    private String dktNo;
    private String requestFromBranch;
    private String requestFromDepartment;
    private String status;
    private String issueFromBranch;
    private String issueFromDepartment;
    private String issuePerson;
    private String issueDate;
    private String trnNo;

    public IntraBranchReceiptListDto(IntraBranchReceiptList materialRequest) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialRequest.getId();
        this.requestId = materialRequest.getRequestId();
        this.trnNo= materialRequest.getTrnNo();
        this.dktNo = materialRequest.getDocketNo();
        this.hupId = materialRequest.getHupId();
        if (materialRequest.getIssueDate() != null) {
            this.issueDate = dateUtil.getDateTOStringWk(materialRequest.getIssueDate());
        }
        this.requestFromBranch = materialRequest.getRequestFromBranch();
        this.requestFromDepartment = materialRequest.getRequestFromDepartment();
        this.status = materialRequest.getStatus();
        this.issueFromBranch = materialRequest.getIssueFromBranch();
        this.issueFromDepartment = materialRequest.getIssueFromDepartment();
        this.issuePerson = materialRequest.getIssuePerson();
    }
}
