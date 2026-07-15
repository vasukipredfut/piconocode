package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class JournalOrMrnNotClosingListDto {


    private Long branchId;
    private Long firmId;
    private String branchName;

    private Long deptId;
    private String deptName;
    private Long journalId;
    private String journalName;
    private String batchNo;
    private Long shiftId;
    private String shiftName;
    private String shiftDate;
    private String trnNo;
    private String dcType;
    private Long requestId;
    private String requestDate;
    private String issueDate;
    private Long requestBranchId;
    private String requestBranchName;
    private Long requestDeptId;
    private String requestDeptName;
    private Long issueBranchId;
    private String issueBranchName;
    private Long issueDeptId;
    private String issueDeptName;
    private String issueStatus;
    private String withInBranchReceiveDate;
    private String withInBranchStatus;
    private String otherBranchStatus;
    private String otherBranchReceiveDate;
    private String billNo;
    private Long inwardId;
    private String inwardDate;
    private String asrs;



    public JournalOrMrnNotClosingListDto(JournalOrMrnNotClosingList journalOrMrnNotClosingList) {
        DateUtil dateUtil = new DateUtil();
        this.branchId = journalOrMrnNotClosingList.getBranchId();
        this.branchName = journalOrMrnNotClosingList.getBranchName();
        this.deptId = journalOrMrnNotClosingList.getDeptId();
        this.deptName = journalOrMrnNotClosingList.getDeptName();
        this.journalId = journalOrMrnNotClosingList.getJournalId();
        this.journalName = journalOrMrnNotClosingList.getJournalName();
        this.batchNo = journalOrMrnNotClosingList.getBatchNo();
        this.shiftId = journalOrMrnNotClosingList.getShiftId();
        this.shiftName = journalOrMrnNotClosingList.getShiftName();
        this.shiftDate = dateUtil.getDateTOStringWk(journalOrMrnNotClosingList.getShiftDate());
        this.trnNo = journalOrMrnNotClosingList.getTrnNo();
        this.dcType = journalOrMrnNotClosingList.getDcType();
        this.asrs=journalOrMrnNotClosingList.getAsrsMapped();
    }


    public JournalOrMrnNotClosingListDto(JournalOrMrnNotClosingList journalOrMrnNotClosingList, boolean status) {
        DateUtil dateUtil = new DateUtil();
        this.requestId = journalOrMrnNotClosingList.getRequestId();
        this.trnNo =  journalOrMrnNotClosingList.getTrnNo();
        if (journalOrMrnNotClosingList.getIssueDate() != null) {
            this.issueDate = dateUtil.getDateTOStringWk(journalOrMrnNotClosingList.getIssueDate());
        }
        if (journalOrMrnNotClosingList.getRequestDate() != null) {
            this.requestDate = dateUtil.getDateTOStringWk(journalOrMrnNotClosingList.getRequestDate());
        }
        if (journalOrMrnNotClosingList.getWithInBranchReceiveDate() != null) {
            this.withInBranchReceiveDate = dateUtil.getDateTOStringWk(journalOrMrnNotClosingList.getWithInBranchReceiveDate());
        }

        if (journalOrMrnNotClosingList.getOtherBranchReceiveDate() != null) {
            this.otherBranchReceiveDate = dateUtil.getDateTOStringWk(journalOrMrnNotClosingList.getOtherBranchReceiveDate());
        }

        this.requestBranchId = journalOrMrnNotClosingList.getRequestBranchId();
        this.requestBranchName = journalOrMrnNotClosingList.getRequestBranchName();

        this.requestDeptId = journalOrMrnNotClosingList.getRequestDeptId();
        this.requestDeptName = journalOrMrnNotClosingList.getRequestDeptName();

        this.issueBranchId = journalOrMrnNotClosingList.getIssueBranchId();
        this.issueBranchName = journalOrMrnNotClosingList.getIssueBranchName();

        this.issueDeptId = journalOrMrnNotClosingList.getIssueDeptId();
        this.issueDeptName = journalOrMrnNotClosingList.getIssueDeptName();

        this.issueStatus = journalOrMrnNotClosingList.getIssueStatus();

        this.withInBranchStatus = journalOrMrnNotClosingList.getWithInBranchStatus();

        this.otherBranchStatus = journalOrMrnNotClosingList.getOtherBranchStatus();

        this.inwardId = journalOrMrnNotClosingList.getInwardId();
        this.billNo = journalOrMrnNotClosingList.getBillNo();
        this.inwardDate = dateUtil.getDateTOStringWk(journalOrMrnNotClosingList.getInwardDate());
    }
}
