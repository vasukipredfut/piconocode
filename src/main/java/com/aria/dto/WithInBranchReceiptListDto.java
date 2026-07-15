package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithInBranchReceiptListDto {

    private Long id;
    private Long requestId;
    private String trnNo;
    private String itemNames;
    private String requestFromBranch;
    private String requestFromDepartment;
    private String status;
    private String requestDate;
    private String issueFromBranch;
    private String issueFromDepartment;

    public WithInBranchReceiptListDto(WithInBranchReceiptList branchReceiptList) {
        DateUtil dateUtil = new DateUtil();
        this.id = branchReceiptList.getId();
        this.trnNo = branchReceiptList.getTrnNo();
        this.requestId = branchReceiptList.getRequestId();
        this.itemNames = branchReceiptList.getItemNames();
        this.requestFromBranch = branchReceiptList.getRequestFromBranch();
        this.requestFromDepartment = branchReceiptList.getRequestFromDepartment();
        this.status = branchReceiptList.getStatus();
        if (branchReceiptList.getRequestDate() != null) {
            this.requestDate = dateUtil.getDateTOStringWk(branchReceiptList.getRequestDate());
        }
        this.issueFromBranch = branchReceiptList.getIssueFromBranch();
        this.issueFromDepartment = branchReceiptList.getIssueFromDepartment();
    }
}
